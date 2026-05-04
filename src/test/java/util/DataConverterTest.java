package util;

import model.Gender;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class DataConverterTest {

  @Test
  void testMapGender() {
    assertEquals(Gender.M, DataConverter.mapGender("Herr"));
    assertEquals(Gender.W, DataConverter.mapGender("Frau"));
    assertEquals(Gender.U, DataConverter.mapGender("Divers"));
    assertEquals(Gender.U, DataConverter.mapGender("Andere"));
    assertEquals(Gender.U, DataConverter.mapGender(null));
    assertEquals(Gender.M, DataConverter.mapGender("  herr  "));
  }

  @Test
  void testParseDate_valid() {
    LocalDate expected = LocalDate.of(2025, 10, 31);
    assertEquals(expected, DataConverter.parseDate("31.10.2025"));
  }

  @Test
  void testParseGermanDate_nullOrEmpty() {
    assertNull(DataConverter.parseGermanDate(null));
    assertNull(DataConverter.parseGermanDate(""));
    assertNull(DataConverter.parseGermanDate("   "));
  }

  @Test
  void testParseGermanDate_valid() {
    LocalDate expected = LocalDate.of(2025, 10, 31);
    assertEquals(expected, DataConverter.parseGermanDate("31.10.2025"));
    assertEquals(expected, DataConverter.parseGermanDate(" 31.10.2025 "));
  }

  @Test
  void testParseNumber() {
    assertEquals(1234.56, DataConverter.parseNumber("1.234,56"));
    assertEquals(1000.0, DataConverter.parseNumber("1.000"));
    assertEquals(0.0, DataConverter.parseNumber(null));
    assertEquals(1234.0, DataConverter.parseNumber("1234"));
    assertEquals(1234.12, DataConverter.parseNumber("1.234,12"));
    assertEquals(1234.12, DataConverter.parseNumber(" 1.234,12 "));
  }

  @Test
  void testParseNumber_invalidFormat() {
    // Testet, dass ungültige Eingaben NumberFormatException werfen
    assertThrows(NumberFormatException.class, () -> DataConverter.parseNumber("abc"));
  }
}
