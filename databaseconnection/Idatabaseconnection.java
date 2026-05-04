package databaseconnection;

import java.util.Properties;

/**
 * Schnittstelle für grundlegende Datenbank-Operationen wie Verbindungsaufbau,
 * Tabellenverwaltung und das Schließen von Verbindungen.
 */
public interface Idatabaseconnection {
  /**
   * Öffnet eine Datenbankverbindung basierend auf den übergebenen Eigenschaften.
   */
  Idatabaseconnection openConnection(Properties properties);

  /**
   * Erstellt alle für die Anwendung benötigten Tabellen.
   * Falls Tabellen bereits existieren, sollte die Implementierung entsprechend reagieren
   * (z. B. ignorieren oder neu erstellen, abhängig vom konkreten Verhalten).
   */
  void createAllTables();

  /**
   * Löscht sämtliche Daten aus allen vorhandenen Tabellen, ohne die Tabellenstruktur zu entfernen.
   * Ideal für Tests oder das Zurücksetzen des Systems.
   */
  void truncateAllTables();

  /**
   * Entfernt alle Tabellen vollständig aus der Datenbank, einschließlich ihrer Struktur.
   */
  void removeAllTables();

  /**
   * Schließt die bestehende Datenbankverbindung und gibt alle Ressourcen frei.
   */
  void closeConnection();

  /**
   * Gibt eine Übersicht aller vorhandenen Tabellen aus.
   * Die konkrete Implementierung bestimmt, wie die Ausgabe erfolgt (Konsole, Logger, etc.).
   */
  void showAllTables();

}
