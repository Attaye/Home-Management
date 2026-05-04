package dao;

import model.KindOfMeter;
import model.Reading;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import databaseconnection.MariaDbConnection;

import java.sql.*;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ReadingDaoTest {

    private Connection mockConnection;
    private PreparedStatement mockStatement;
    private ResultSet mockResultSet;
    private ReadingDao readingDao;

    @BeforeEach
    void setup() throws SQLException {
        MariaDbConnection mockDb = mock(MariaDbConnection.class);
        mockConnection = mock(Connection.class);
        mockStatement = mock(PreparedStatement.class);
        mockResultSet = mock(ResultSet.class);

        when(mockDb.getConnection()).thenReturn(mockConnection);
        readingDao = new ReadingDao(mockDb);

        when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);
        when(mockStatement.executeQuery()).thenReturn(mockResultSet);
    }

    // ---------- CREATE ----------
    @Test
    void testCreate_Success() throws SQLException {
        Reading r = new Reading();
        r.setId(UUID.randomUUID());
        r.setDateOfReading(LocalDate.now());
        r.setMeterCount(123.45);
        r.setMeterId("M1");
        r.setComment("test");
        r.setKindOfMeter(KindOfMeter.STROM);
        r.setCustomerId(UUID.randomUUID());

        // exists -> false
        ReadingDao spyDao = spy(readingDao);
        doReturn(false).when(spyDao).exists(any(UUID.class));
        when(mockStatement.executeUpdate()).thenReturn(1);

        boolean result = spyDao.create(r);
        assertTrue(result);
        verify(mockStatement).executeUpdate();
    }

    @Test
    void testCreate_AlreadyExists()  {
        Reading r = new Reading();
        r.setId(UUID.randomUUID());

        ReadingDao spyDao = spy(readingDao);
        doReturn(true).when(spyDao).exists(any(UUID.class));

        boolean result = spyDao.create(r);
        assertFalse(result);
    }

    @Test
    void testCreate_ThrowsSQLException() throws SQLException {
        Reading r = new Reading();
        r.setId(UUID.randomUUID());

        ReadingDao spyDao = spy(readingDao);
        doReturn(false).when(spyDao).exists(any(UUID.class));
        when(mockConnection.prepareStatement(anyString())).thenThrow(new SQLException("DB error"));

        boolean result = spyDao.create(r);
        assertFalse(result);
    }

    // ---------- UPDATE ----------
    @Test
    void testUpdate_Success() throws SQLException {
        Reading r = new Reading();
        r.setId(UUID.randomUUID());
        r.setDateOfReading(LocalDate.now());
        r.setMeterCount(50);
        r.setMeterId("X");
        r.setComment("c");
        r.setKindOfMeter(KindOfMeter.WASSER);
        r.setCustomerId(UUID.randomUUID());

        ReadingDao spyDao = spy(readingDao);
        doReturn(true).when(spyDao).exists(r.getId());
        when(mockStatement.executeUpdate()).thenReturn(1);
        doReturn(mockStatement).when(mockConnection).prepareStatement(anyString());

        spyDao.update(r);
        verify(mockStatement).executeUpdate();
    }

    @Test
    void testUpdate_NotExists() throws SQLException {
        Reading r = new Reading();
        r.setId(UUID.randomUUID());

        ReadingDao spyDao = spy(readingDao);
        doReturn(false).when(spyDao).exists(r.getId());

        spyDao.update(r);
        // verify: executeUpdate never called
        verify(mockStatement, never()).executeUpdate();
    }

    @Test
    void testUpdate_ThrowsSQLException() throws SQLException {
        Reading r = new Reading();
        r.setId(UUID.randomUUID());
        r.setDateOfReading(LocalDate.now());

        ReadingDao spyDao = spy(readingDao);
        doReturn(true).when(spyDao).exists(r.getId());
        when(mockConnection.prepareStatement(anyString())).thenThrow(new SQLException("DB error"));

        spyDao.update(r);
    }

    // ---------- DELETE ----------
    @Test
    void testDelete_Success() throws SQLException {
        Reading r = new Reading();
        r.setId(UUID.randomUUID());

        ReadingDao spyDao = spy(readingDao);
        doReturn(true).when(spyDao).exists(r.getId());
        when(mockStatement.executeUpdate()).thenReturn(1);

        spyDao.delete(r);
        verify(mockStatement).executeUpdate();
    }

    @Test
    void testDelete_NotExists() throws SQLException {
        Reading r = new Reading();
        r.setId(UUID.randomUUID());

        ReadingDao spyDao = spy(readingDao);
        doReturn(false).when(spyDao).exists(r.getId());

        spyDao.delete(r);
        verify(mockStatement, never()).executeUpdate();
    }

    @Test
    void testDelete_ThrowsSQLException() throws SQLException {
        Reading r = new Reading();
        r.setId(UUID.randomUUID());

        ReadingDao spyDao = spy(readingDao);
        doReturn(true).when(spyDao).exists(r.getId());
        when(mockConnection.prepareStatement(anyString())).thenThrow(new SQLException("DB error"));

        spyDao.delete(r);
    }

    // ---------- FIND BY ID ----------
    @Test
    void testFindById_Found() throws SQLException {
        UUID id = UUID.randomUUID();
        when(mockResultSet.next()).thenReturn(true);
        when(mockResultSet.getString("id")).thenReturn(id.toString());
        when(mockResultSet.getDate("date_of_reading")).thenReturn(Date.valueOf(LocalDate.now()));
        when(mockResultSet.getDouble("meter_count")).thenReturn(100.0);
        when(mockResultSet.getString("meter_id")).thenReturn("M1");
        when(mockResultSet.getString("comment")).thenReturn("c");
        when(mockResultSet.getString("kind_of_meter")).thenReturn("STROM");

        List<Reading> result = (List<Reading>) readingDao.findById(id);
        assertEquals(1, result.size());
    }

    @Test
    void testFindById_NotFound() throws SQLException {
        UUID id = UUID.randomUUID();
        when(mockResultSet.next()).thenReturn(false);

        List<Reading> result = (List<Reading>) readingDao.findById(id);
        assertTrue(result.isEmpty());
    }

    @Test
    void testFindById_ThrowsSQLException() throws SQLException {
        UUID id = UUID.randomUUID();
        when(mockConnection.prepareStatement(anyString())).thenThrow(new SQLException("DB error"));

        List<Reading> result = (List<Reading>) readingDao.findById(id);
        assertTrue(result.isEmpty());
    }

    // ---------- FIND ALL ----------
    @Test
    void testFindAll() throws SQLException {
        when(mockResultSet.next()).thenReturn(true, true, false);
        when(mockResultSet.getString("id")).thenReturn(UUID.randomUUID().toString(), UUID.randomUUID().toString());
        when(mockResultSet.getDate("date_of_reading")).thenReturn(Date.valueOf(LocalDate.now()), (Date) null);
        when(mockResultSet.getDouble("meter_count")).thenReturn(1.0, 2.0);
        when(mockResultSet.getString("meter_id")).thenReturn("M1", "M2");
        when(mockResultSet.getString("comment")).thenReturn("c1", "c2");
        when(mockResultSet.getString("kind_of_meter")).thenReturn("STROM", "WASSER");

        List<Reading> result = readingDao.findAll();
        assertEquals(2, result.size());
    }

    @Test
    void testFindAll_ThrowsSQLException() throws SQLException {
        when(mockConnection.prepareStatement(anyString())).thenThrow(new SQLException("DB error"));
        List<Reading> result = readingDao.findAll();
        assertTrue(result.isEmpty());
    }

    // ---------- EXISTS ----------
    @Test
    void testExists_True() throws SQLException {
        UUID id = UUID.randomUUID();
        when(mockResultSet.next()).thenReturn(true);
        when(mockResultSet.getInt(1)).thenReturn(1);
        assertTrue(readingDao.exists(id));
    }

    @Test
    void testExists_False() throws SQLException {
        UUID id = UUID.randomUUID();
        when(mockResultSet.next()).thenReturn(false);
        assertFalse(readingDao.exists(id));
    }

    @Test
    void testExists_ThrowsSQLException() throws SQLException {
        UUID id = UUID.randomUUID();
        when(mockConnection.prepareStatement(anyString())).thenThrow(new SQLException("DB error"));
        assertFalse(readingDao.exists(id));
    }

    // ---------- SAFE VALUE ----------
    @Test
    void testSafeValue() {
        assertEquals("null", readingDao.safeValue(null));
        assertEquals("null", readingDao.safeValue(""));
        assertEquals("John", readingDao.safeValue("  John "));
    }

    // ---------- FORMAT ----------
    @Test
    void testFormatGermanStyle() {
        assertEquals("1.234,56", ReadingDao.formatGermanStyle(1234.56));
    }

    @Test
    void testFindFiltered_allFilters() throws Exception {
        UUID customerId = UUID.randomUUID();
        String start = "2024-01-01";
        String end = "2024-12-31";
        String kindOfMeter = "WASSER";

        // Mock ResultSet data
        when(mockResultSet.next()).thenReturn(true).thenReturn(false);
        when(mockResultSet.getString("id")).thenReturn(UUID.randomUUID().toString());
        when(mockResultSet.getString("customer_id")).thenReturn(customerId.toString());
        when(mockResultSet.getDate("date_of_reading")).thenReturn(Date.valueOf("2024-06-01"));
        when(mockResultSet.getString("kind_of_meter")).thenReturn("WASSER");
        when(mockResultSet.getFloat("value")).thenReturn(123.45f);

        List<Reading> result = readingDao.findFiltered(customerId, start, end, kindOfMeter);

        // Verify PreparedStatement parameters
        verify(mockStatement).setString(1, customerId.toString());
        verify(mockStatement).setDate(2, Date.valueOf(start));
        verify(mockStatement).setDate(3, Date.valueOf(end));
        verify(mockStatement).setString(4, kindOfMeter);

        assertEquals(1, result.size());
        assertEquals(KindOfMeter.WASSER, result.getFirst().getKindOfMeter());
    }

    @Test
    void testFindFiltered_insuccess_shouldReturnEmptyList() throws Exception {
        UUID customerId = UUID.randomUUID();
        String start = "2024-01-01";
        String end = "2024-12-31";
        String kindOfMeter = "WASSER";

        // Make prepareStatement throw SQL exception
        when(mockConnection.prepareStatement(anyString()))
                .thenThrow(new SQLException("DB error"));

        List<Reading> result = readingDao.findFiltered(customerId, start, end, kindOfMeter);

        // Because your DAO catches SQLExceptions internally:
        assertNotNull(result);
        assertTrue(result.isEmpty(), "Should return empty list on SQL failure");
    }


}


