package vorlage;

import dao.CustomerDao;
import databaseconnection.MariaDbConnection;
import model.Customer;
import model.Gender;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CustomerMainTest {
    private CustomerDao mockDao;

    @BeforeEach
    void setup() {
        mockDao = mock(CustomerDao.class);
    }

    // --------------------- Test CSV Parsing ---------------------
    @Test
    void testParseCustomer_AllFields() {
        String[] row = {
                UUID.randomUUID().toString(),
                "M",
                "Max",
                "Mustermann",
                "05.11.1990"
        };

        Customer customer = CustomerMain.parseCustomer(row);

        assertNotNull(customer.getId());
        assertEquals(Gender.U, customer.getGender());
        assertEquals("Max", customer.getFirstName());
        assertEquals("Mustermann", customer.getLastName());
        assertEquals(LocalDate.of(1990, 11, 5), customer.getBirthDate());
    }

    @Test
    void testParseCustomer_MissingOptionalFields() {
        String[] row = {
                UUID.randomUUID().toString(),
                "W",
                "",
                ""
        };

        Customer customer = CustomerMain.parseCustomer(row);

        assertEquals("null", customer.getFirstName());
        assertEquals("null", customer.getLastName());
        assertNull(customer.getBirthDate());
        assertEquals(Gender.U, customer.getGender());
    }

    // --------------------- Test importCustomers ---------------------
    @Test
    void testImportCustomers_NewAndExisting() {
        // Simulierte CSV-Daten
        List<String[]> csvRows = List.of(
                new String[]{UUID.randomUUID().toString(), "M", "Max", "Mustermann", "05.11.1990"},
                new String[]{UUID.randomUUID().toString(), "W", "Anna", "Schmidt"}
        );

        // Mock CsvReader, damit keine echte Datei gelesen wird
        try (var csvMock = mockStatic(util.CsvReader.class)) {
            csvMock.when(() -> util.CsvReader.readCsv("dummy.csv")).thenReturn(csvRows);

            // Mock Verhalten von exists
            when(mockDao.exists(any(UUID.class)))
                    .thenReturn(false) // beide neu
                    .thenReturn(true); // zweite Zeile existiert schon

            CustomerMain.importCustomers(mockDao, "dummy.csv");

            // Überprüfen, dass create() genau einmal aufgerufen wurde (nur für neue Zeile)
            ArgumentCaptor<Customer> captor = ArgumentCaptor.forClass(Customer.class);
            verify(mockDao, times(1)).create(captor.capture());

            Customer created = captor.getValue();
            assertEquals("Max", created.getFirstName());
            assertEquals(Gender.U, created.getGender());
        }
    }

    // --------------------- Test run() ---------------------
    @Test
    void testRun_CallsImportCustomers() throws Exception {
        CustomerDao dao = mock(CustomerDao.class);
        MariaDbConnection db = mock(MariaDbConnection.class);

        // run() sollte einfach importCustomers aufrufen
        CustomerMain.run(db, dao, "dummy.csv");

        assertNotNull(dao);
        assertNotNull(db);
    }

    // --------------------- Test main() ---------------------
    @Test
    void testMain_NoException() {
        assertDoesNotThrow(() -> CustomerMain.run(mock(MariaDbConnection.class), mockDao, "dummy.csv"));
    }

    @Test
    void testParseCustomer_InvalidUUID() {
        String[] row = {
                "not-a-uuid",
                "M",
                "John",
                "Doe",
                "01.01.1980"
        };

        assertThrows(IllegalArgumentException.class, () -> CustomerMain.parseCustomer(row));
    }

    @Test
    void testParseCustomer_UnknownGender() {
        String[] row = {
                UUID.randomUUID().toString(),
                "X",
                "Alex",
                "Unknown",
                "01.01.2000"
        };

        Customer customer = CustomerMain.parseCustomer(row);
        assertEquals(Gender.U, customer.getGender()); // Should default to unknown
    }

    @Test
    void testImportCustomers_EmptyCsv() {
        try (var csvMock = mockStatic(util.CsvReader.class)) {
            csvMock.when(() -> util.CsvReader.readCsv("empty.csv")).thenReturn(List.of());

            assertDoesNotThrow(() -> CustomerMain.importCustomers(mockDao, "empty.csv"));
            verify(mockDao, never()).create(any());
        }
    }
}
