package dao;

import databaseconnection.MariaDbConnection;
import model.KindOfMeter;
import model.Reading;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class ReadingDaoTest {

    private ReadingDao readingDao;
    private Connection mockConnection;

    @BeforeEach
    void setUp() throws SQLException {
        MariaDbConnection mockDb = mock(MariaDbConnection.class);
        mockConnection = mock(Connection.class);
        when(mockDb.getConnection()).thenReturn(mockConnection);
        readingDao = new ReadingDao(mockDb);
    }

    // ── safeValue ────────────────────────────────────────────────────────

    @Test
    void testSafeValue() {
        // ✅ FIX: safeValue ist package-private → direkt aufrufbar im gleichen Package
        assertEquals("null", readingDao.safeValue(null));
        assertEquals("null", readingDao.safeValue(""));
        assertEquals("John", readingDao.safeValue(" John "));
    }

    // ── formatGermanStyle ────────────────────────────────────────────────

    @Test
    void testFormatGermanStyle() {
        // ✅ FIX: static Methode → über Klassenname aufrufen
        assertEquals("1.234,56", ReadingDao.formatGermanStyle(1234.56));
        assertEquals("0,00",     ReadingDao.formatGermanStyle(0));
        assertEquals("12,34",    ReadingDao.formatGermanStyle(12.34));
    }

    // ── exists ────────────────────────────────────────────────────────────

    @Test
    void testExists_ThrowsSQLException() throws SQLException {
        when(mockConnection.prepareStatement(anyString()))
                .thenThrow(new SQLException("DB Error"));

        UUID id = UUID.randomUUID();
        assertFalse(readingDao.exists(id));
    }

    // ── findAll ───────────────────────────────────────────────────────────

    @Test
    void testFindAll_Exception() throws SQLException {
        when(mockConnection.prepareStatement(anyString()))
                .thenThrow(new SQLException("Table not found"));

        List<Reading> result = readingDao.findAll();
        assertTrue(result.isEmpty());
    }

    // ── findFiltered ──────────────────────────────────────────────────────

    @Test
    void testFindFiltered_allFilters() throws Exception {
        UUID customerId = UUID.randomUUID();
        String start       = "2024-01-01";
        String end         = "2024-12-31";
        String kindOfMeter = "STROM";

        PreparedStatement mockPs = mock(PreparedStatement.class);
        ResultSet mockRs = mock(ResultSet.class);

        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPs);
        when(mockPs.executeQuery()).thenReturn(mockRs);
        when(mockRs.next()).thenReturn(false);

        List<Reading> result = readingDao.findFiltered(customerId, start, end, kindOfMeter);
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void testFindFiltered_noFilters() throws Exception {
        PreparedStatement mockPs = mock(PreparedStatement.class);
        ResultSet mockRs = mock(ResultSet.class);

        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPs);
        when(mockPs.executeQuery()).thenReturn(mockRs);
        when(mockRs.next()).thenReturn(false);

        List<Reading> result = readingDao.findFiltered(null, null, null, null);
        assertNotNull(result);
    }

    // ── create ────────────────────────────────────────────────────────────

    @Test
    void testCreate_alreadyExists() throws SQLException {
        PreparedStatement mockPs = mock(PreparedStatement.class);
        ResultSet countRs = mock(ResultSet.class);

        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPs);
        when(mockPs.executeQuery()).thenReturn(countRs);
        when(countRs.next()).thenReturn(true);
        when(countRs.getInt(1)).thenReturn(1); // existiert bereits

        Reading reading = new Reading();
        reading.setId(UUID.randomUUID());
        reading.setDateOfReading(LocalDate.now());
        reading.setKindOfMeter(KindOfMeter.STROM);

        boolean result = readingDao.create(reading);
        assertFalse(result);
    }
}