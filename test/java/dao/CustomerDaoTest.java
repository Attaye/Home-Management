package dao;

import databaseconnection.MariaDbConnection;
import model.Customer;
import model.Gender;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.*;
import java.sql.Date;
import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class CustomerDaoTest {

    private Connection mockConnection;
    private PreparedStatement mockStatement;
    private ResultSet mockResultSet;
    private CustomerDao customerDao;

    @BeforeEach
    void setup() {
        MariaDbConnection mockDb = mock(MariaDbConnection.class);
        mockConnection = mock(Connection.class);
        mockStatement = mock(PreparedStatement.class);
        mockResultSet = mock(ResultSet.class);

        when(mockDb.getConnection()).thenReturn(mockConnection);
        customerDao = spy(new CustomerDao(mockDb));
    }

    // ---------- CREATE ----------
    @Test
    void testCreate_NewCustomer() throws Exception {
        Customer c = new Customer();
        c.setId(UUID.randomUUID());
        c.setFirstName("Max");
        c.setLastName("Mustermann");
        c.setGender(Gender.M);
        c.setBirthDate(LocalDate.of(1990, 1, 1));

        doReturn(false).when(customerDao).exists(any(UUID.class));
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);

        boolean result = customerDao.create(c);

        assertTrue(result);
        verify(mockStatement, times(1)).executeUpdate();
    }

    @Test
    void testCreate_CustomerAlreadyExists() {
        Customer c = new Customer();
        c.setId(UUID.randomUUID());
        c.setFirstName("Erika");
        c.setLastName("Musterfrau");

        doReturn(true).when(customerDao).exists(any(UUID.class));

        boolean result = customerDao.create(c);

        assertFalse(result);

    }

    // ---------- UPDATE ----------
    @Test
    void testUpdateCustomer() throws Exception {
        Customer c = new Customer();
        c.setId(UUID.randomUUID());
        c.setFirstName("Anna");
        c.setLastName("Schmidt");
        c.setGender(Gender.W);
        c.setBirthDate(LocalDate.of(1985, 5, 15));

        when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);
        when(mockStatement.executeUpdate()).thenReturn(1);

        boolean result = customerDao.update(c);

        assertTrue(result);
        verify(mockStatement, times(1)).executeUpdate();
    }

    // ---------- DELETE ----------
    @Test
    void testDeleteCustomer_Success() throws Exception {
        UUID id = UUID.randomUUID();
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);
        when(mockStatement.executeUpdate()).thenReturn(1);

        boolean result = customerDao.delete(id);

        assertTrue(result);
        verify(mockStatement, times(1)).executeUpdate();
    }

    @Test
    void testDeleteCustomer_NotFound() throws Exception {
        UUID id = UUID.randomUUID();
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);
        when(mockStatement.executeUpdate()).thenReturn(0);

        boolean result = customerDao.delete(id);

        assertTrue(result); // Methode gibt immer true zurück
    }

    // ---------- FIND ALL ----------
    @Test
    void testFindAllCustomers() throws Exception {
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);
        when(mockStatement.executeQuery()).thenReturn(mockResultSet);

        when(mockResultSet.next()).thenReturn(true, true, false);
        when(mockResultSet.getString("id"))
                .thenReturn(UUID.randomUUID().toString(), UUID.randomUUID().toString());
        when(mockResultSet.getString("first_name")).thenReturn("Lisa", "Tom");
        when(mockResultSet.getString("last_name")).thenReturn("Maier", "Fischer");
        when(mockResultSet.getString("gender")).thenReturn("W", "M");
        when(mockResultSet.getDate("birth_date"))
                .thenReturn(Date.valueOf(LocalDate.of(1992, 3, 3)), (Date) null);

        Iterable<Customer> result = customerDao.findAll();
        List<Customer> list = new ArrayList<>();
        result.forEach(list::add);

        assertEquals(2, list.size());
        assertEquals(Gender.W, list.get(0).getGender());
        assertEquals("Tom", list.get(1).getFirstName());
    }

    // ---------- FIND BY ID ----------
    @Test
    void testFindById_Found() throws Exception {
        UUID id = UUID.randomUUID();
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);
        when(mockStatement.executeQuery()).thenReturn(mockResultSet);

        when(mockResultSet.next()).thenReturn(true);
        when(mockResultSet.getString("id")).thenReturn(id.toString());
        when(mockResultSet.getString("first_name")).thenReturn("Karl");
        when(mockResultSet.getString("last_name")).thenReturn("Meier");
        when(mockResultSet.getString("gender")).thenReturn("M");
        when(mockResultSet.getDate("birth_date")).thenReturn(Date.valueOf(LocalDate.of(1980, 2, 2)));

        Optional<Customer> result = customerDao.findById(id);

        assertTrue(result.isPresent());
        assertEquals("Karl", result.get().getFirstName());
    }

    @Test
    void testFindById_NotFound() throws Exception {
        UUID id = UUID.randomUUID();
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);
        when(mockStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(false);

        Optional<Customer> result = customerDao.findById(id);

        assertTrue(result.isEmpty());
    }

    // ---------- EXISTS ----------
    @Test
    void testExists_CustomerExists() throws Exception {
        UUID id = UUID.randomUUID();
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);
        when(mockStatement.executeQuery()).thenReturn(mockResultSet);

        when(mockResultSet.next()).thenReturn(true);
        when(mockResultSet.getInt(1)).thenReturn(1);

        boolean exists = customerDao.exists(id);

        assertTrue(exists);
    }

    @Test
    void testExists_CustomerNotExists() throws Exception {
        UUID id = UUID.randomUUID();

        when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);
        when(mockStatement.executeQuery()).thenReturn(mockResultSet);

        // Simuliere kein Ergebnis
        when(mockResultSet.next()).thenReturn(false);

        boolean exists = customerDao.exists(id);

        assertFalse(exists); // sollte false zurückgeben, weil kein Datensatz existiert

        verify(mockStatement).setString(1, id.toString());
        verify(mockStatement).executeQuery();
    }


    // ---------- SAFE VALUE ----------
    @Test
    void testSafeValue() {
        assertEquals("John", CustomerDao.safeValue("  John "));
        assertEquals("null", CustomerDao.safeValue(""));
        assertEquals("null", CustomerDao.safeValue(null));
    }

    @Test
    void testExists_ThrowsSQLException() throws Exception {
        UUID id = UUID.randomUUID();

        // Simuliere SQLException beim prepareStatement
        when(mockConnection.prepareStatement(anyString())).thenThrow(new SQLException("DB error"));

        boolean exists = customerDao.exists(id);

        // Erwartung: false zurück, weil SQLException aufgetreten ist
        assertFalse(exists);

        // verify, dass prepareStatement aufgerufen wurde
        verify(mockConnection).prepareStatement(anyString());
    }

    @Test
    void testFindById_ThrowsSQLException() throws Exception {
        UUID id = UUID.randomUUID();

        // Simuliere, dass prepareStatement eine SQLException wirft
        when(mockConnection.prepareStatement(anyString())).thenThrow(new SQLException("DB error"));

        Optional<Customer> result = customerDao.findById(id);

        assertTrue(result.isEmpty());

        verify(mockConnection).prepareStatement(anyString());
    }
    @Test
    void testFormatGermanStyle() {
        // Test positive number
        String formatted = ReadingDao.formatGermanStyle(1234.5);
        assertEquals("1.234,50", formatted);

        // Test zero
        formatted = ReadingDao.formatGermanStyle(0);
        assertEquals("0,00", formatted);

        // Test negative number
        formatted = ReadingDao.formatGermanStyle(-9876.54);
        assertEquals("-9.876,54", formatted);

        // Test number with exact two decimals
        formatted = ReadingDao.formatGermanStyle(12.34);
        assertEquals("12,34", formatted);

    }

    @Test
    void testFindAll_Exception() throws SQLException {
        // SQLException beim prepareStatement
        when(mockConnection.prepareStatement(anyString())).thenThrow(new SQLException("DB Fehler"));

        Iterable<Customer> result = customerDao.findAll();

        assertNotNull(result); // Methode gibt trotzdem eine leere Liste zurück
        assertTrue(((List<?>) result).isEmpty());
    }
}
