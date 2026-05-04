package model;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.databind.SerializationFeature;
import dto.ReadingResponse;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class ReadingResponseTest {

    private ObjectMapper mapper() {
        ObjectMapper m = new ObjectMapper();
        // Für LocalDate (dateOfReading)
        m.registerModule(new JavaTimeModule());
        m.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        return m;
    }

    @Test
    void testGetterReturnsSameReadingInstance() {
        // Arrange: Testdaten für Reading
        Reading reading = new Reading();
        UUID id = UUID.randomUUID();
        reading.setId(id);
        reading.setCustomerId(UUID.randomUUID());
        reading.setMeterId("M-123");
        reading.setMeterCount(123.45);
        reading.setDateOfReading(LocalDate.of(2025, 1, 15));
        reading.setComment("test reading");
        reading.setSubstitute(false);
        // kindOfMeter optional (kann null bleiben, abhängig vom Enum)

        // Act
        ReadingResponse response = new ReadingResponse(reading);

        // Assert
        assertNotNull(response.getReading(), "Reading should not be null");
        assertSame(reading, response.getReading(), "getReading() must return the same instance passed to constructor");
        assertEquals("M-123", response.getReading().getMeterId());
        assertEquals(123.45, response.getReading().getMeterCount());
        assertEquals(LocalDate.of(2025, 1, 15), response.getReading().getDateOfReading());
        assertFalse(response.getReading().getSubstitute());
    }

    @Test
    void testAllowsNullReading() {
        // Act
        ReadingResponse response = new ReadingResponse(null);

        // Assert
        assertNull(response.getReading(), "getReading() should be null when constructed with null");
    }

    @Test
    void testJacksonSerialization_hasReadingFieldAndNestedValues() throws Exception {
        // Arrange: Reading mit exemplarischen Feldern
        Reading reading = new Reading();
        reading.setId(UUID.fromString("00000000-0000-0000-0000-000000000001"));
        reading.setCustomerId(UUID.fromString("11111111-1111-1111-1111-111111111111"));
        reading.setMeterId("M-999");
        reading.setMeterCount(999.99);
        reading.setDateOfReading(LocalDate.of(2025, 2, 1));
        reading.setComment("serialized");
        reading.setSubstitute(true);

        ReadingResponse response = new ReadingResponse(reading);

        // Act
        String json = mapper().writeValueAsString(response);

        // Assert: Struktur enthält "reading" und einige Felder des Nested-Objekts
        assertTrue(json.contains("\"reading\""), "Serialized JSON must contain 'reading' field");
        assertTrue(json.contains("\"meterId\":\"M-999\""), "Serialized JSON must contain meterId");
        assertTrue(json.contains("\"meterCount\":999.99"), "Serialized JSON must contain meterCount");
        assertTrue(json.contains("\"comment\":\"serialized\""), "Serialized JSON must contain comment");
        assertTrue(json.contains("\"substitute\":true"), "Serialized JSON must contain substitute");
        // ISO-8601 Datum
        assertTrue(json.contains("\"dateOfReading\":\"2025-02-01\""), "Serialized JSON must contain dateOfReading in ISO format");
        // UUIDs
        assertTrue(json.contains("\"id\":\"00000000-0000-0000-0000-000000000001\""), "Serialized JSON must contain id");
        assertTrue(json.contains("\"customerId\":\"11111111-1111-1111-1111-111111111111\""), "Serialized JSON must contain customerId");
    }

}