package model;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class ReadingTest {

  @Test
  void testGettersAndSetters() {
      Reading reading = new Reading();

      UUID id = UUID.randomUUID();
      LocalDate date = LocalDate.of(2025, 11, 7);
      double meterCount = 123.45;
      String meterId = "MTR-001";
      String comment = "Test comment";
      KindOfMeter kindOfMeter = KindOfMeter.HEIZUNG;
      UUID customerId = UUID.randomUUID();
      boolean substitute = true;

      // Setter aufrufen
      reading.setId(id);
      reading.setDateOfReading(date);
      reading.setMeterCount(meterCount);
      reading.setMeterId(meterId);
      reading.setComment(comment);
      reading.setKindOfMeter(kindOfMeter);
      reading.setCustomerId(customerId);
      reading.setSubstitute(substitute);

      // Getter prüfen
      assertEquals(id, reading.getId());
      assertEquals(date, reading.getDateOfReading());
      assertEquals(meterCount, reading.getMeterCount());
      assertEquals(meterId, reading.getMeterId());
      assertEquals(comment, reading.getComment());
      assertEquals(kindOfMeter, reading.getKindOfMeter());
      assertEquals(customerId, reading.getCustomerId());
      assertEquals(substitute, reading.getSubstitute());
    }

  @Test
  void testDefaultConstructorGeneratesId() {
    Reading reading = new Reading();
    assertNotNull(reading.getId(), "UUID sollte im Default-Konstruktor generiert werden");
  }
  @Test
  void testToString() {
    // Reading Objekt erstellen
    Reading reading = new Reading();
    UUID id = UUID.fromString("3056f1fa-318e-4b1d-9e3c-9e2b57808295");
    LocalDate date = LocalDate.of(2025, 11, 7);

    reading.setId(id);
    reading.setDateOfReading(date);
    reading.setMeterCount(123.45);
    reading.setMeterId("MTR-001");
    reading.setComment("Test comment");
    reading.setKindOfMeter(KindOfMeter.HEIZUNG);
    reading.setCustomerId(UUID.fromString("00d326bf-8873-4aba-be4c-065d506665ca"));
    reading.setSubstitute(true);

    // Erwarteten String zusammensetzen
    String expected = "Reading{" +
            "id=" + id +
            ", dateOfReading=" + date +
            ", meterCount=" + 123.45 +
            ", meterId='MTR-001'" +
            ", comment='Test comment'" +
            ", kindOfMeter=" + KindOfMeter.HEIZUNG +
            ", customerId=" + UUID.fromString("00d326bf-8873-4aba-be4c-065d506665ca") +
            ", substitute=" + true +
            '}';

    // Assertion
    assertEquals(expected, reading.toString());
  }
}
