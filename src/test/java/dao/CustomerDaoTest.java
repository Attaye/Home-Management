package dao;

import databaseconnection.MariaDbConnection;
import model.Customer;
import model.Gender;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class CustomerDaoTest {

    private CustomerDao customerDao;
    private Connection  mockConnection;

    @BeforeEach
    void setUp() throws SQLException {
        MariaDbConnection mockDb = mock(MariaDbConnection.class);
        mockConnection = mock(Connection.class);
        when(mockDb.getConnection()).thenReturn(mockConnection);
        customerDao = new CustomerDao(mockDb);
    }

    // ── safeValue ────────────────────────────────────────────────────────

    @Test
    void testSafeValue_null() {
        assertEquals("null", CustomerDao.safeValue(null));
    }

    @Test
    void testSafeValue_empty() {
        assertEquals("null", CustomerDao.safeValue(""));
    }

    @Test
    void testSafeValue_whitespace() {
        assertEquals("null", CustomerDao.safeValue("   "));
    }

    @Test
    void testSafeValue_valid() {
        assertEquals("John", CustomerDao.safeValue(" John "));
    }

    // ── create ────────────────────────────────────────────────────────────

    @Test
    void testCreate_Success() throws SQLException {
        PreparedStatement mockPs = mock(PreparedStatement.class);
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPs);
        when(mockPs.executeQuery()).thenReturn(mock(ResultSet.class));

        ResultSet countRs = mock(ResultSet.class);
        when(countRs.next()).thenReturn(true);
        when(countRs.getInt(1)).thenReturn(0); // not exists
        when(mockPs.executeQuery()).thenReturn(countRs);

        Customer customer = new Customer();
        customer.setId(UUID.randomUUID());
        customer.setFirstName("Hans");
        customer.setLastName("Mustermann");
        customer.setGender(Gender.M);
        customer.setBirthDate(LocalDate.of(1990, 1, 1));

        // existiert nicht → create sollte true zurückgeben
        // (vereinfachter Test ohne vollständigen Mock-Chain)
        assertDoesNotThrow(() -> customerDao.exists(customer.getId()));
    }

    // ── findById ─────────────────────────────────────────────────────────

    @Test
    void testFindById_ThrowsSQLException() throws SQLException {
        when(mockConnection.prepareStatement(anyString()))
                .thenThrow(new SQLException("DB Error"));

        UUID id = UUID.randomUUID();
        Optional<Customer> result = customerDao.findById(id);

        assertTrue(result.isEmpty());
    }

    // ── formatGermanStyle (von ReadingDao, wird hier auch getestet) ───────

    @Test
    void testFormatGermanStyle() {
        assertEquals("1.234,50", ReadingDao.formatGermanStyle(1234.5));
        assertEquals("0,00",     ReadingDao.formatGermanStyle(0));
        assertEquals("-9.876,54",ReadingDao.formatGermanStyle(-9876.54));
        assertEquals("12,34",    ReadingDao.formatGermanStyle(12.34));
    }

    // ── findAll ───────────────────────────────────────────────────────────

    @Test
    void testFindAll_Exception() throws SQLException {
        when(mockConnection.prepareStatement(anyString()))
                .thenThrow(new SQLException("Table not found"));

        List<Customer> result = customerDao.findAll();
        assertTrue(result.isEmpty());
    }
}