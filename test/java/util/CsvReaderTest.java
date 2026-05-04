package util;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CsvReaderTest {

  @Test
  void testReadCsvWithValidData() throws IOException {
    // Temporäre CSV-Datei erstellen
    Path tempFile = Files.createTempFile("test", ".csv");
    Files.write(tempFile, List.of(
            "UUID, Datum, Wert",                      // Header, soll übersprungen werden
            "123e4567-e89b-12d3-a456-426614174000,2025-10-31,42",
            "223e4567-e89b-12d3-a456-426614174001;2025-11-01;43",
            "   ",                                   // leere Zeile, soll übersprungen werden
            "invalid"                                // ungültige Zeile, soll übersprungen werden
    ));

    List<String[]> lines = CsvReader.readCsv(tempFile.toString());

    assertEquals(2, lines.size(), "Es sollten nur 2 gültige Datenzeilen eingelesen werden");

    assertArrayEquals(
            new String[]{"123e4567-e89b-12d3-a456-426614174000", "2025-10-31", "42"},
            lines.get(0));

    assertArrayEquals(
            new String[]{"223e4567-e89b-12d3-a456-426614174001", "2025-11-01", "43"},
            lines.get(1));

    Files.deleteIfExists(tempFile);
  }

  @Test
  void testReadCsvEmptyFile() throws IOException {
    Path tempFile = Files.createTempFile("empty", ".csv");

    List<String[]> lines = CsvReader.readCsv(tempFile.toString());
    assertTrue(lines.isEmpty(), "Leere Datei sollte eine leere Liste zurückgeben");

    Files.deleteIfExists(tempFile);
  }

  @Test
  void testReadCsvFileNotFound() {
    List<String[]> lines = CsvReader.readCsv("nonexistent.csv");
    assertTrue(lines.isEmpty(), "Nicht existierende Datei sollte eine leere Liste zurückgeben");
  }

  @Test
  void testReadCsvHeaderAndInvalidRows() throws IOException {
    Path tempFile = Files.createTempFile("header", ".csv");
    Files.write(tempFile, List.of(
            "UUID, Datum, Wert",
            " ",
            "onlyonecolumn",
            "123;2025-11-05;99"
    ));

    List<String[]> lines = CsvReader.readCsv(tempFile.toString());

    assertEquals(1, lines.size(), "Nur eine gültige Zeile sollte übrig bleiben");
    assertArrayEquals(new String[]{"123", "2025-11-05", "99"}, lines.getFirst());

    Files.deleteIfExists(tempFile);
  }
}
