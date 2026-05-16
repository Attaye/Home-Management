package controller;

import databaseconnection.MariaDbConnection;
import io.javalin.Javalin;
import io.javalin.http.Context;
import io.javalin.http.HttpStatus;
import io.javalin.http.UploadedFile;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Importiert CSV- und PDF-Dateien (Sprint-Datenformat).
 *
 * PDF-Format (wasser.pdf, heizung.pdf, strom.pdf):
 *   Kunde        <uuid>
 *   Zählernummer <meter_id>
 *   Datum          Zählerstand in m³/MWh/kWh        Kommentar
 *   01.02.2018     473
 *   03.03.2020     0 Zählertausch: neue Nummer 786523123
 *
 * POST /import  –  Multipart-Feld: "file"
 */
public class ImportController {

    private final Connection connection;
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd.MM.yyyy");

    public ImportController(Javalin app, MariaDbConnection db) {
        this.connection = db.getConnection();
        app.post("/import", this::importFile);
    }

    private void importFile(Context ctx) {
        UploadedFile uploaded = ctx.uploadedFile("file");
        if (uploaded == null) {
            ctx.status(HttpStatus.BAD_REQUEST).result("Kein Feld 'file' im Upload gefunden.");
            return;
        }

        String filename = uploaded.filename().toLowerCase();
        if (!filename.endsWith(".csv") && !filename.endsWith(".pdf")) {
            ctx.status(HttpStatus.BAD_REQUEST).result("Nur .csv und .pdf Dateien erlaubt.");
            return;
        }

        // Datei-Inhalt komplett in Byte-Array lesen (InputStream kann nur einmal gelesen werden)
        byte[] fileBytes;
        try (InputStream is = uploaded.content()) {
            fileBytes = is.readAllBytes();
        } catch (Exception e) {
            ctx.status(HttpStatus.INTERNAL_SERVER_ERROR).result("Datei konnte nicht gelesen werden: " + e.getMessage());
            return;
        }

        String text;
        try {
            if (filename.endsWith(".pdf")) {
                // ── PDF → Text mit PDFBox ──────────────────────────────────
                try (PDDocument doc = PDDocument.load(fileBytes)) {
                    PDFTextStripper stripper = new PDFTextStripper();
                    text = stripper.getText(doc);
                }
            } else {
                // ── CSV → direkt als Text lesen ────────────────────────────
                text = new String(fileBytes, java.nio.charset.StandardCharsets.UTF_8);
            }
        } catch (Exception e) {
            ctx.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .result("Fehler beim Lesen der Datei: " + e.getMessage());
            return;
        }

        // Text parsen und in DB speichern
        int[] result = parseAndInsert(text);
        int inserted = result[0], skipped = result[1];

        if (inserted == 0 && skipped == 0) {
            ctx.status(HttpStatus.BAD_REQUEST)
                    .result("Keine gültigen Datenzeilen gefunden. Dateiformat prüfen.\n" +
                            "Erwartet: Zeilen wie '01.02.2018   473' (Datum + Zählerstand)");
            return;
        }

        String msg = inserted + " Ablesungen importiert";
        if (skipped > 0) msg += ", " + skipped + " übersprungen";
        ctx.status(HttpStatus.OK).result(msg);
    }

    /**
     * Parst den extrahierten Text und speichert Ablesungen in der DB.
     * @return int[]{inserted, skipped}
     */
    private int[] parseAndInsert(String text) {
        int inserted = 0, skipped = 0;
        String customerId    = null;
        String currentMeterId = null;
        String kindOfMeter   = "UNBEKANNT";

        for (String line : text.split("\\r?\\n")) {
            line = line.trim();
            if (line.isEmpty()) continue;

            // ── "Kunde  <uuid>" ──────────────────────────────────────────
            if (line.startsWith("Kunde")) {
                customerId = extractAfterKeyword(line, "Kunde");
                continue;
            }

            // ── "Zählernummer  <id>" ─────────────────────────────────────
            if (line.startsWith("Zählernummer") || line.startsWith("Zahlernummer")) {
                currentMeterId = extractAfterKeyword(line, line.contains("ä") ? "Zählernummer" : "Zahlernummer");
                continue;
            }

            // ── "Zählerstand in m³ / kWh / MWh" ─────────────────────────
            if (line.contains("Zählerstand in") || line.contains("Zahlerstand in")) {
                kindOfMeter = detectMeterType(line);
                continue;
            }

            // ── Datenzeile: "dd.MM.yyyy  <wert>  <kommentar>" ───────────
            if (line.matches("\\d{2}\\.\\d{2}\\.\\d{4}.*")) {
                // Zeile in Teile aufteilen (whitespace-getrennt)
                String[] parts = line.split("\\s+", 3);
                if (parts.length < 2) continue;

                String dateStr  = parts[0].trim();
                String countStr = parts[1].trim().replace(",", ".");
                String comment  = parts.length > 2 ? parts[2].trim() : null;

                // Zählertausch → neue Zählernummer merken
                if (comment != null && comment.contains("Zählertausch")) {
                    String newId = extractNewMeterId(comment);
                    if (newId != null) currentMeterId = newId;
                }

                try {
                    double meterCount = Double.parseDouble(countStr);
                    boolean ok = insertReading(customerId, currentMeterId, dateStr, meterCount, comment, kindOfMeter);
                    if (ok) inserted++; else skipped++;
                } catch (NumberFormatException e) {
                    // Zählerstand nicht parsierbar → überspringen
                    skipped++;
                } catch (Exception e) {
                    skipped++;
                    System.err.println("Zeile übersprungen: " + line + " → " + e.getMessage());
                }
            }
        }
        return new int[]{inserted, skipped};
    }

    private boolean insertReading(String customerId, String meterId, String dateStr,
                                  double meterCount, String comment, String kindOfMeter)
            throws SQLException {

        String sql = "INSERT INTO reading " +
                "(id, date_of_reading, meter_count, meter_id, comment, kind_of_meter, customer_id, substitute) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, UUID.randomUUID().toString());
            ps.setDate(2, Date.valueOf(LocalDate.parse(dateStr, DATE_FMT)));
            ps.setDouble(3, meterCount);
            ps.setString(4, meterId != null ? meterId : "");
            ps.setString(5, comment);
            ps.setString(6, kindOfMeter);
            ps.setString(7, customerId);
            ps.setBoolean(8, comment != null && comment.contains("Zählertausch"));
            ps.executeUpdate();
            return true;
        } catch (SQLException e) {
            // FK-Fehler: Kunde existiert nicht in der DB → überspringen
            if (e.getMessage() != null &&
                    (e.getMessage().contains("foreign key") || e.getErrorCode() == 1452)) {
                System.err.println("FK-Fehler: Kunde " + customerId + " nicht in DB.");
                return false;
            }
            throw e;
        }
    }

    // ── Helper-Methoden ───────────────────────────────────────────────────

    /** Extrahiert den Wert nach einem Schlüsselwort (whitespace-getrennt) */
    private String extractAfterKeyword(String line, String keyword) {
        String rest = line.substring(keyword.length()).trim();
        // Ersten Token nehmen (die UUID oder Zählernummer)
        String[] tokens = rest.split("\\s+");
        return tokens.length > 0 ? tokens[0].trim() : null;
    }

    /** Erkennt die Zählerart aus der Kopfzeile */
    private String detectMeterType(String header) {
        if (header.contains("MWh"))               return "HEIZUNG";
        if (header.contains("kWh"))               return "STROM";
        if (header.contains("m³") || header.contains("m3")) return "WASSER";
        return "UNBEKANNT";
    }

    /** Extrahiert die neue Zählernummer aus "Zählertausch: neue Nummer XYZ" */
    private String extractNewMeterId(String comment) {
        Pattern p = Pattern.compile("neue Nummer\\s+(\\S+)");
        Matcher m = p.matcher(comment);
        return m.find() ? m.group(1) : null;
    }
}