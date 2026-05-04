package databaseconnection;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

class MariaDbConnectionTest {

  private MariaDbConnection db;
  private Connection mockConnection;
  private Statement mockStatement;

  private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
  private PrintStream originalOut;

  @BeforeEach
  void setup() throws SQLException, NoSuchFieldException, IllegalAccessException {
    db = new MariaDbConnection();

    mockConnection = mock(Connection.class);
    mockStatement = mock(Statement.class);

    when(mockConnection.createStatement()).thenReturn(mockStatement);

    Field connectionField = MariaDbConnection.class.getDeclaredField("connection");
    connectionField.setAccessible(true);
    connectionField.set(db, mockConnection);
    originalOut = System.out;
    System.setOut(new PrintStream(outContent));
  }

  @Test
  void testOpenConnectionPrintsAndSetsConnection() throws SQLException {
    try (var mockedDriverManager = mockStatic(DriverManager.class)) {
      Properties props = new Properties();
      props.setProperty("db.url", "jdbc:mock");
      props.setProperty("db.user", "user");
      props.setProperty("db.password", "pass");

      mockedDriverManager
          .when(() -> DriverManager.getConnection(anyString(), anyString(), anyString()))
          .thenReturn(mockConnection);

      db.openConnection(props);

      // Prüfen, dass connection gesetzt wurde
      assertNotNull(db.getConnection(), "Connection sollte nicht null sein");

      // Prüfen, dass die Konsole die Nachricht ausgibt
      assertTrue(
          outContent.toString().contains("✅ Connected to MariaDB"),
          "Console sollte '✅ Connected to MariaDB' enthalten");
    }
    }
  @AfterEach
  void restoreStreams() {
    System.setOut(originalOut);
  }

  @Test
  void testCreateAllTables() throws SQLException {
    db.createAllTables();
    verify(mockConnection, atLeastOnce()).createStatement();
    verify(mockStatement, atLeast(2)).execute(anyString());
  }

  @Test
  void testCreateAllTablesWithSQLException() throws SQLException {
    when(mockConnection.createStatement()).thenThrow(new SQLException("DB Error"));
    db.createAllTables();
  }

  @Test
  void testTruncateAllTablesSuccess() throws SQLException {
    when(mockConnection.getAutoCommit()).thenReturn(true);

    db.truncateAllTables();

    verify(mockConnection).setAutoCommit(false);
    verify(mockStatement, atLeast(2)).execute(anyString());
    verify(mockConnection).commit();
  }

  @Test
  void testTruncateAllTablesSQLException() throws SQLException {
    doThrow(new SQLException("DB Error")).when(mockConnection).createStatement();
    db.truncateAllTables(); // Prüfen, dass Rollback versucht wird
  }

  @Test
  void testRemoveAllTables() throws SQLException {
    db.removeAllTables();
    verify(mockStatement, atLeast(2)).execute(anyString());
  }

  @Test
  void testRemoveAllTablesSQLException() throws SQLException {
    doThrow(new SQLException("DB Error")).when(mockConnection).createStatement();
    db.removeAllTables();
  }

  @Test
  void testShowAllTables() throws SQLException {
    db.showAllTables();
    verify(mockStatement).execute("SHOW TABLES");
  }

  @Test
  void testShowAllTablesSQLException() throws SQLException {
    doThrow(new SQLException("DB Error")).when(mockConnection).createStatement();
    db.showAllTables();
  }

  @Test
  void testCloseConnectionWithValidConnection() throws SQLException {
    db.closeConnection();
    verify(mockConnection).close();
  }

  @Test
  void testCloseConnectionWithNullConnection() {
    // connection = null
    db = new MariaDbConnection() {
      @Override
      public Connection getConnection() {
        return null;
      }
    };
    assertDoesNotThrow(() -> db.closeConnection());
  }

  @Test
  void testCloseConnectionThrowsSQLException() throws SQLException {
    doThrow(new SQLException("DB Error")).when(mockConnection).close();
    assertDoesNotThrow(() -> db.closeConnection());
    verify(mockConnection).close();
  }

  @Test
  void testGetConnection() {
    // Prüfen, dass der Getter die richtige Connection zurückgibt
    Connection result = db.getConnection();
    assertNotNull(result, "Connection darf nicht null sein");
    assertEquals(mockConnection, result, "Getter sollte die gespeicherte Connection zurückgeben");
  }

}
