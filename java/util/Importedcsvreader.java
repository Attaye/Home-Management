package util;

import dao.ReadingDao;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Utility class for reading and importing CSV data into the database. */
public final class Importedcsvreader {

  /** DAO for reading operations. */
  private static ReadingDao readingDao;

  /** Index of the 'id' parameter in the SQL statement. */
  private static final int PARAM_ID = 1;

  /** Index of the 'date_of_reading' parameter in the SQL statement. */
  private static final int PARAM_DATE = 2;

  /** Index of the 'meter_count' parameter in the SQL statement. */
  private static final int PARAM_COUNT = 3;

  /** Index of the 'meter_id' parameter in the SQL statement. */
  private static final int PARAM_METER_ID = 4;

  /** Index of the 'comment' parameter in the SQL statement. */
  private static final int PARAM_COMMENT = 5;

  /** Index of the 'kind_of_meter' parameter in the SQL statement. */
  private static final int PARAM_KIND = 6;

  /** Index of the 'customer_id' parameter in the SQL statement. */
  private static final int PARAM_CUSTOMER = 7;

  /** Index of the 'substitute' parameter in the SQL statement. */
  private static final int PARAM_SUBSTITUTE = 8;

  // Private constructor to prevent instantiation
  private Importedcsvreader() {
    throw new UnsupportedOperationException("Utility class");
  }

  /**
   * Processes a CSV file and inserts readings into the database.
   *
   * @param filePath the path to the CSV file
   * @param conn the database connection
   * @return true if processing succeeds
   * @throws IOException if file reading fails
   * @throws SQLException if database operation fails
   */
  public static boolean processCsvFile(final String filePath, final Connection conn)
      throws IOException, SQLException {

    try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
      String line;
      String customerId = null;
      String currentMeterId = null;
      String kindOfMeter = "UNBEKANNT";

      while ((line = br.readLine()) != null) {
        line = line.trim();

        if (line.startsWith("\"Kunde\"")) {
          customerId = extractValue(line);
        } else if (line.startsWith("\"Zählernummer\"")) {
          currentMeterId = extractValue(line);
        } else if (line.contains("Zählerstand in")) {
          kindOfMeter = detectMeterType(line);
        } else if (line.matches("\\d{2}\\.\\d{2}\\.\\d{4}.*")) {
          String[] parts = line.split(";");
          String date = parts[0];
          String meterCount = parts[1];
          String comment = parts.length > 2 ? parts[2] : null;

          if (comment != null && comment.contains("Zählertausch")) {
            currentMeterId = extractNewMeterId(comment);
          }

          insertReading(conn, customerId, currentMeterId, date, meterCount, comment, kindOfMeter);
        }
      }
    }
    return true;
  }

  /**
   * Extracts the value from a CSV line.
   *
   * @param line the CSV line
   * @return the extracted value
   */
  private static String extractValue(final String line) {
    return line.split(";")[1].replace("\"", "").replace(";", "");
  }

  /**
   * Detects the meter type from the header line.
   *
   * @param header the header line
   * @return the detected meter type
   */
  public static String detectMeterType(final String header) {
    if (header.contains("MWh")) {
      return "HEIZUNG";
    }
    if (header.contains("kWh")) {
      return "STROM";
    }
    if (header.contains("m³")) {
      return "WASSER";
    }
    return "UNBEKANNT";
  }

  /**
   * Extracts the new meter ID from a comment.
   *
   * @param comment the comment string
   * @return the new meter ID or null
   */
  static String extractNewMeterId(final String comment) {
    Pattern p = Pattern.compile("neue Nummer\\s+(\\S+)");
    Matcher m = p.matcher(comment);
    if (m.find()) {
      return m.group(1);
    }
    return null;
  }

  /**
   * Inserts a reading into the database.
   *
   * @param conn the database connection
   * @param customerId the customer ID
   * @param meterId the meter ID
   * @param date the reading date
   * @param meterCount the meter count
   * @param comment the comment
   * @param kindOfMeter the kind of meter
   * @return true if insertion succeeds
   * @throws SQLException if database operation fails
   */
  private static boolean insertReading(
      final Connection conn,
      final String customerId,
      final String meterId,
      final String date,
      final String meterCount,
      final String comment,
      final String kindOfMeter)
      throws SQLException {

    String sql =
        "INSERT INTO reading ("
            + "id, date_of_reading, meter_count, meter_id, "
            + "comment, kind_of_meter, customer_id, substitute) "
            + "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

    try (PreparedStatement ps = conn.prepareStatement(sql)) {

      ps.setString(PARAM_ID, UUID.randomUUID().toString());
      ps.setDate(
          PARAM_DATE,
          java.sql.Date.valueOf(LocalDate.parse(date, DateTimeFormatter.ofPattern("dd.MM.yyyy"))));
      ps.setDouble(PARAM_COUNT, Double.parseDouble(meterCount.replace(",", ".")));
      ps.setString(PARAM_METER_ID, meterId);
      ps.setString(PARAM_COMMENT, comment);
      ps.setString(PARAM_KIND, kindOfMeter);
      ps.setString(PARAM_CUSTOMER, customerId);
      ps.setBoolean(PARAM_SUBSTITUTE, comment != null && comment.contains("Zählertausch"));
      ps.executeUpdate();
    }
    return true;
  }
}
