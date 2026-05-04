package databaseconnection;

import org.junit.jupiter.api.*;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class IDatabaseConnectionTest {

  private Idatabaseconnection mockDb;

  @BeforeEach
  void setup() {
    mockDb = mock(Idatabaseconnection.class);

    doNothing().when(mockDb).createAllTables();
    doNothing().when(mockDb).removeAllTables();
    doNothing().when(mockDb).truncateAllTables();
    doNothing().when(mockDb).closeConnection();
    when(mockDb.openConnection(any(Properties.class))).thenReturn(mockDb);
  }


  @AfterAll
  void teardown() {
    // closeConnection aufrufen (gemockt, macht nichts)
    mockDb.closeConnection();
    verify(mockDb, atLeastOnce()).closeConnection();
  }

  @Test
  void testCreateAndRemoveTables() {
    mockDb.createAllTables();
    mockDb.removeAllTables();

    // Überprüfen, dass die Methoden aufgerufen wurden
    verify(mockDb, times(1)).createAllTables();
    verify(mockDb, times(1)).removeAllTables();
  }

  @Test
  void testTruncateTables() {
    mockDb.createAllTables();
    mockDb.truncateAllTables();
    mockDb.removeAllTables();

    verify(mockDb, times(1)).createAllTables();
    verify(mockDb, times(1)).truncateAllTables();
    verify(mockDb, times(1)).removeAllTables();
  }

  @Test
  void testOpenAndCloseConnectionWithMock()  {
    Idatabaseconnection mockDb = mock(Idatabaseconnection.class);

    when(mockDb.openConnection(any(Properties.class))).thenReturn(mockDb);

    Idatabaseconnection conn = mockDb.openConnection(new Properties());

    assertNotNull(conn);
    assertEquals(mockDb, conn);

    conn.closeConnection();

    verify(mockDb, times(1)).closeConnection();
  }
}
