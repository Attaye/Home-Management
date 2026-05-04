package util;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.io.FileWriter;
import java.nio.file.Files;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Integration tests for ImportedCSVReader.
 * Uses in-memory H2 database to simulate inserts and verify results.
 */
public class importedcsvreaderTest {

  private Connection connection;
  private File csvFile;

  /** Prepare in-memory DB and temporary CSV file. */
  @BeforeEach
  void setUp() throws Exception {
    connection = DriverManager.getConnection("jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1");
    try (Statement stmt = connection.createStatement()) {
      stmt.execute(
              "CREATE TABLE reading ("
                      + "id VARCHAR(36) PRIMARY KEY,"
                      + "date_of_reading DATE,"
                      + "meter_count DOUBLE,"
                      + "meter_id VARCHAR(255),"
                      + "comment VARCHAR(255),"
                      + "kind_of_meter VARCHAR(50),"
                      + "customer_id VARCHAR(36),"
                      + "substitute BOOLEAN"
                      + ")");
    }
    csvFile = Files.createTempFile("import", ".csv").toFile();
  }

  /** Clean up DB and temp file. */
  @AfterEach
  void tearDown() throws Exception {
    try (Statement stmt = connection.createStatement()) {
      stmt.execute("DROP TABLE reading");
    }
    if (csvFile.exists()) {
      assertTrue(csvFile.delete());
    }
    connection.close();
  }

  @Test
  void testProcessCsvFileWithVariousLines() throws Exception {
    try (FileWriter writer = new FileWriter(csvFile)) {
      writer.write("\"Kunde\";12345;\n");
      writer.write("\"Zählernummer\";Z123;\n");
      writer.write("\"Zählerstand in kWh\";\n");

      // Testzeile ohne Kommentar
      writer.write("01.01.2024;100,5;\n");

      // Testzeile mit Zählertausch
      writer.write("02.01.2024;150,2;Zählertausch neue Nummer Z999\n");

      // Testzeile mit Kommentar ohne Zählertausch
      writer.write("03.01.2024;50,0;Nach Tausch\n");
    }

    boolean result = Importedcsvreader.processCsvFile(csvFile.getAbsolutePath(), connection);
    assertTrue(result);

    try (Statement stmt = connection.createStatement()) {
      try (ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM reading")) {
        assertTrue(rs.next());
        assertEquals(3, rs.getInt(1));
      }

      try (ResultSet rs = stmt.executeQuery("SELECT * FROM reading ORDER BY date_of_reading")) {
        // Erste Zeile: kein Kommentar
        assertTrue(rs.next());
        assertEquals("Z123", rs.getString("meter_id"));
        assertEquals("STROM", rs.getString("kind_of_meter"));
        assertFalse(rs.getBoolean("substitute"));

        // Zweite Zeile: Zählertausch
        assertTrue(rs.next());
        assertTrue(rs.getBoolean("substitute"));
        assertEquals("Z999", rs.getString("meter_id"));

        // Dritte Zeile: Kommentar ohne Zählertausch
        assertTrue(rs.next());
        assertEquals("Z999", rs.getString("meter_id"));
      }
    }
  }

  @Test
  void testDetectMeterType() {
    assertEquals("HEIZUNG", Importedcsvreader.detectMeterType("Zählerstand in MWh"));
    assertEquals("STROM", Importedcsvreader.detectMeterType("Zählerstand in kWh"));
    assertEquals("WASSER", Importedcsvreader.detectMeterType("Zählerstand in m³"));
    assertEquals("UNBEKANNT", Importedcsvreader.detectMeterType("something else"));
  }

  @Test
  void testProcessCsvFileWithMinimalLines() throws Exception {
    try (FileWriter writer = new FileWriter(csvFile)) {
      writer.write("\"Kunde\";C1;\n");
      writer.write("\"Zählernummer\";M1;\n");
      writer.write("\"Zählerstand in MWh\";\n");
      writer.write("01.01.2025;200,0;\n");
    }

    boolean success = Importedcsvreader.processCsvFile(csvFile.getAbsolutePath(), connection);
    assertTrue(success);

    try (Statement stmt = connection.createStatement()) {
      try (ResultSet rs = stmt.executeQuery("SELECT * FROM reading")) {
        assertTrue(rs.next());
        assertEquals("HEIZUNG", rs.getString("kind_of_meter"));
        assertEquals(200.0, rs.getDouble("meter_count"), 0.01);
      }
    }
  }

  @Test
  void testExtractNewMeterId_withValidPattern() {
    String comment = "Zählertausch neue Nummer Z321";
    String result = Importedcsvreader.extractNewMeterId(comment);
    assertEquals("Z321", result);
  }


  @Test
  void testExtractNewMeterId_withEmptyString() {
    String comment = "";
    String result = Importedcsvreader.extractNewMeterId(comment);
    assertNull(result);
  }

  @Test
  void testExtractNewMeterId_withMultipleNumbers() {
    String comment = "Tausch neue Nummer Z123 und neue Nummer Z456";
    String result = Importedcsvreader.extractNewMeterId(comment);
    // sollte nur die erste passende Nummer zurückgeben
    assertEquals("Z123", result);
  }

  @Test
  void testEmptyFileReturnsTrue() throws Exception {
    try (FileWriter writer = new FileWriter(csvFile)) {
      writer.write("");
    }

    boolean result = Importedcsvreader.processCsvFile(csvFile.getAbsolutePath(), connection);
    assertTrue(result);

    try (Statement stmt = connection.createStatement();
         ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM reading")) {
      assertTrue(rs.next());
      assertEquals(0, rs.getInt(1));
    }
  }
}
