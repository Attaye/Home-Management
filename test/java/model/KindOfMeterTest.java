package model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class KindOfMeterTest {

  @Test
  void testEnumValues() {
    // Prüfen, dass alle Enum-Werte existieren
    KindOfMeter[] meters = KindOfMeter.values();
    assertEquals(4, meters.length, "Es sollten genau 4 KindOfMeter-Werte existieren");

    assertEquals(KindOfMeter.HEIZUNG, KindOfMeter.valueOf("HEIZUNG"));
    assertEquals(KindOfMeter.STROM, KindOfMeter.valueOf("STROM"));
    assertEquals(KindOfMeter.WASSER, KindOfMeter.valueOf("WASSER"));
    assertEquals(KindOfMeter.UNBEKANNT, KindOfMeter.valueOf("UNBEKANNT"));
  }

  @Test
  void testEnumNames() {
    // Prüfen, dass die Namen korrekt sind
    assertEquals("HEIZUNG", KindOfMeter.HEIZUNG.name());
    assertEquals("STROM", KindOfMeter.STROM.name());
    assertEquals("WASSER", KindOfMeter.WASSER.name());
    assertEquals("UNBEKANNT", KindOfMeter.UNBEKANNT.name());
  }

  @Test
  void testEnumValueOfThrowsException() {
    // Prüfen, dass ein falscher Wert eine Exception wirft
    assertThrows(IllegalArgumentException.class, () -> KindOfMeter.valueOf("GAS"));
  }
}
