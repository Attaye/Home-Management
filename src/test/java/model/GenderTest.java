package model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class GenderTest {

  @Test
  void testEnumValues() {
    // Prüfen, dass alle Enum-Werte existieren
    Gender[] genders = Gender.values();
    assertEquals(4, genders.length, "Es sollten genau 4 Gender-Werte existieren");

    assertEquals(Gender.D, Gender.valueOf("D"));
    assertEquals(Gender.M, Gender.valueOf("M"));
    assertEquals(Gender.W, Gender.valueOf("W"));
    assertEquals(Gender.U, Gender.valueOf("U"));
  }

  @Test
  void testEnumNames() {
    assertEquals("D", Gender.D.name());
    assertEquals("M", Gender.M.name());
    assertEquals("W", Gender.W.name());
    assertEquals("U", Gender.U.name());
  }

  @Test
  void testEnumValueOfThrowsException() {
    assertThrows(IllegalArgumentException.class, () -> Gender.valueOf("X"));
  }
}
