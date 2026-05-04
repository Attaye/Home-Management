package databaseconnection;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Diese Klasse implementiert die Schnittstelle {@link Idatabaseconnection} und stellt eine
 * Verbindung zu einer MariaDB-Datenbank her. Sie bietet Methoden zum Öffnen und Schließen der
 * Verbindung, Erstellen, Löschen und Anzeigen von Tabellen sowie zum Leeren aller Tabelleninhalte.
 */
public class MariaDbConnection implements Idatabaseconnection {
  private Connection connection;
  private static final Logger LOGGER = Logger.getLogger(MariaDbConnection.class.getName());

  @Override
  public Idatabaseconnection openConnection(Properties properties) {
    try {
      String url = properties.getProperty("db.url");
      String user = properties.getProperty("db.user");
      String password = properties.getProperty("db.password");
      connection = DriverManager.getConnection(url, user, password);
      System.out.println("✅ Connected to MariaDB");
    } catch (SQLException e) {
      e.printStackTrace();
    }
    return this;
  }

  @Override
  public void createAllTables() {
    try (Statement stmt = connection.createStatement()) {
      stmt.execute(
          """
                CREATE TABLE IF NOT EXISTS customer (
                    id CHAR(36) PRIMARY KEY,
                    first_name VARCHAR(100),
                    last_name VARCHAR(100),
                    gender ENUM('D','M','W','U'),
                    birth_date DATE
                )
            """);

      stmt.execute(
          """
                CREATE TABLE IF NOT EXISTS reading (
                    id CHAR(36) PRIMARY KEY,
                    date_of_reading DATE,
                    meter_count DOUBLE,
                    meter_id VARCHAR(100),
                    comment TEXT,
                    kind_of_meter ENUM('HEIZUNG','STROM','WASSER','UNBEKANNT'),
                    customer_id CHAR(36) null,
                    substitute BOOLEAN,
                    FOREIGN KEY (customer_id) REFERENCES customer(id) ON DELETE SET NULL
                )
            """);

      System.out.println("✅ Tables created successfully.");
    } catch (SQLException e) {
      e.printStackTrace();
    }
  }

  @Override
  public void truncateAllTables() {
    try (Statement stmt = connection.createStatement()) {
      connection.setAutoCommit(false);

      // Delete readings first (child table)
      stmt.execute("DELETE FROM reading");
      System.out.println("Alle Daten aus 'reading' gelöscht.");

      // Delete customers (parent table) -> triggers ON DELETE SET NULL if any references remain
      stmt.execute("DELETE FROM customer");
      System.out.println("Alle Daten aus 'customer' gelöscht.");

      connection.commit();
    } catch (SQLException e) {
      try {
        connection.rollback();
      } catch (SQLException ex) {
        ex.printStackTrace();
      }
      e.printStackTrace();
    }
  }

  @Override
  public void removeAllTables() {
    try (Statement stmt = connection.createStatement()) {
      stmt.execute("DROP TABLE IF EXISTS reading");
      System.out.println("reading Table gelöscht erfolgreich.");
      stmt.execute("DROP TABLE IF EXISTS customer");
      System.out.println("customer Table gelöscht erfolgreich.");
    } catch (SQLException e) {
      e.printStackTrace();
    }
  }

  @Override
  public void showAllTables() {
    try (Statement stmt = connection.createStatement()) {
      stmt.execute("SHOW TABLES");
      System.out.println("Alle Tables were found : " + stmt);
    } catch (SQLException e) {
      LOGGER.log(Level.SEVERE, "Error show all tables: ", e);
    }
  }

  @Override
  public void closeConnection() {
    try {
      if (connection != null) {
        connection.close();
      }
    } catch (SQLException e) {
      LOGGER.log(Level.SEVERE, "Error close connection: ", e);
    }
  }

  public Connection getConnection() {
    return connection;
  }
}
