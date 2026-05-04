package dao;

import databaseconnection.MariaDbConnection;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.logging.Logger;
import model.KindOfMeter;
import model.Reading;

/** Data Access Object for Reading entity. */
public class ReadingDao {

  /** Database connection. */
  private final Connection connection;

  private static final Logger LOGGER = Logger.getLogger(ReadingDao.class.getName());

  /**
   * Constructs a ReadingDao with the given database connection.
   *
   * @param db the MariaDbConnection to use
   */
  public ReadingDao(final MariaDbConnection db) {
    this.connection = db.getConnection();
  }

  /**
   * Adds a new reading into the reading table.
   *
   * @param r the Reading object to insert
   */
  public boolean create(final Reading r) {
    // Prüfen, ob Reading schon existiert
    if (exists(r.getId())) {
      System.out.println("The Reading already exist: " + r.getId());
      return false;
    }


      final String sql =
              "INSERT INTO reading (id, date_of_reading, meter_count, meter_id, comment, kind_of_meter, customer_id, substitute) "
              + "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
    try (PreparedStatement ps = connection.prepareStatement(sql)) {
      ps.setString(1, r.getId().toString());
      if (r.getDateOfReading() != null) {
        ps.setDate(2, Date.valueOf(r.getDateOfReading()));
      } else {
        ps.setNull(2, Types.DATE);
      }
      ps.setDouble(3, r.getMeterCount());
      ps.setString(4, r.getMeterId() == null ? "null" : r.getMeterId());
      ps.setString(5, r.getComment() == null ? "null" : r.getComment());
      ps.setString(6, r.getKindOfMeter() == null ? "UNBEKANNT" : r.getKindOfMeter().name());
      ps.setString(7, r.getCustomerId() != null ? r.getCustomerId().toString() : null);
      ps.setBoolean(8, r.getSubstitute());

      ps.executeUpdate();
    } catch (SQLException e) {
      System.err.println("Error inserting reading: " + e.getMessage());
      e.printStackTrace();
      return false;
    }
    return true;
  }

  /**
   * Updates the reading table with the given reading.
   *
   * @param reading the Reading object to update
   * @return
   */
  public boolean update(final Reading reading) {
    if (!exists(reading.getId())) {
      System.out.println("Could not find reading with id: " + reading.getId());
      return false;
    }
    final String sql =
        "UPDATE reading SET date_of_reading=?, meter_count=?,"
            + " meter_id=?, "
            + "comment=?, kind_of_meter=?, "
            + "customer_id=? WHERE id=?";
    try (PreparedStatement st = connection.prepareStatement(sql)) {
      st.setDate(1, Date.valueOf(reading.getDateOfReading()));
      st.setDouble(2, reading.getMeterCount());
      st.setString(3, reading.getMeterId());
      st.setString(4, reading.getComment());
      st.setString(5, reading.getKindOfMeter() == null ? "null" : reading.getKindOfMeter().name());

      if (reading.getCustomerId() != null) {
        st.setString(6, String.valueOf(reading.getCustomerId()));
      } else {
        st.setNull(6, Types.VARCHAR);
      }

      st.setString(7, reading.getId().toString());
      st.executeUpdate();
    } catch (SQLException e) {
      System.err.println("Error updating reading: " + reading.getId());
      e.printStackTrace();
    }
    return true;
  }

  /**
   * Deletes a reading from the reading table.
   *
   * @param reading the Reading object to delete
   */
  public void delete(final Reading reading) {
    if (!exists(reading.getId())) {
      System.out.println("Reading ID not found: " + reading.getId());
      return;
    }
    final String sql = "DELETE FROM reading WHERE id = ?";
    try (PreparedStatement st = connection.prepareStatement(sql)) {
      st.setString(1, reading.getId().toString());
      final int deleted = st.executeUpdate();
      if (deleted > 0) {
        System.out.println("Reading successfully deleted.");
      } else {
        System.out.println("No reading found with ID: " + reading.getId());
      }
    } catch (SQLException e) {
      System.err.println("Error deleting reading: " + reading.getId());
      e.printStackTrace();
    }
  }

  /**
   * Finds a reading by its ID.
   *
   * @param id the UUID of the reading
   * @return an Optional containing the Reading if found
   */
  public Iterable<Reading> findById(final UUID id) {
    final List<Reading> readings = new ArrayList<>();
    final String sql = "SELECT * FROM reading WHERE id = ?";
    try (PreparedStatement st = connection.prepareStatement(sql)) {
      st.setString(1, id.toString());
      try (ResultSet rs = st.executeQuery()) {
        if (rs.next()) {
          final Reading reading = new Reading();
          reading.setId(UUID.fromString(rs.getString("id")));
          reading.setDateOfReading(rs.getDate("date_of_reading").toLocalDate());
          reading.setMeterCount(rs.getDouble("meter_count"));
          reading.setMeterId(rs.getString("meter_id"));
          reading.setComment(rs.getString("comment"));
          reading.setKindOfMeter(KindOfMeter.valueOf(rs.getString("kind_of_meter")));
          readings.add(reading);
        }
      }
    } catch (SQLException e) {
      System.err.println("Could not find reading with ID: " + id);
      e.printStackTrace();
    }
    return readings;
  }

  /**
   * Retrieves all readings from the reading table.
   *
   * @return a list of all Reading objects
   */
  public List<Reading> findAll() {
    final List<Reading> readings = new ArrayList<>();
    final String sql = "SELECT * FROM reading";
    try (PreparedStatement stmt = connection.prepareStatement(sql);
         ResultSet rs = stmt.executeQuery()) {
      while (rs.next()) {
        final Reading reading = new Reading();
        reading.setId(UUID.fromString(rs.getString("id")));
        final Date sqlDate = rs.getDate("date_of_reading");
        reading.setDateOfReading(sqlDate != null ? sqlDate.toLocalDate() : null);
        reading.setMeterCount(rs.getDouble("meter_count"));
        reading.setMeterId(rs.getString("meter_id"));
        reading.setComment(rs.getString("comment"));
        reading.setKindOfMeter(KindOfMeter.valueOf(rs.getString("kind_of_meter")));
        readings.add(reading);
      }
    } catch (SQLException e) {
      System.err.println("Error retrieving readings.");
      e.printStackTrace();
    }
    return readings;
  }

    public List<Reading> findFiltered(
            UUID customerId,
            String start,
            String end,
            String kindOfMeter
    ) {
        List<Reading> result = new ArrayList<>();

        StringBuilder sql = new StringBuilder(
                "SELECT * FROM reading WHERE customer_id = ?"
        );

        if (start != null) {
            sql.append(" AND date_of_reading >= ?");
        }
        if (end != null) {
            sql.append(" AND date_of_reading <= ?");
        }
        if (kindOfMeter != null) {
            sql.append(" AND kind_of_meter = ?");
        }

        try (PreparedStatement ps = connection.prepareStatement(sql.toString())) {

            int index = 1;
            ps.setString(index++, customerId.toString());

            if (start != null) {
                ps.setDate(index++, Date.valueOf(start));
            }
            if (end != null) {
                ps.setDate(index++, Date.valueOf(end));
            }
            if (kindOfMeter != null) {
                ps.setString(index++, kindOfMeter);
            }

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                result.add(mapRow(rs));
            }
            System.out.println("---- FILTER DEBUG ----");
            System.out.println("customerId = " + customerId);
            System.out.println("start = " + start);
            System.out.println("end = " + end);
            System.out.println("kindOfMeter = " + kindOfMeter);
            System.out.println("SQL = " + sql);
            System.out.println("----------------------");

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return result;
    }


    private Reading mapRow(ResultSet rs) throws SQLException {
        Reading r = new Reading();

        r.setId(UUID.fromString(rs.getString("id")));
        r.setDateOfReading(rs.getDate("date_of_reading").toLocalDate());
        r.setMeterCount(rs.getDouble("meter_count"));
        r.setMeterId(rs.getString("meter_id"));
        r.setComment(rs.getString("comment"));
        r.setKindOfMeter(KindOfMeter.valueOf(rs.getString("kind_of_meter")));

        String customerIdStr = rs.getString("customer_id");
        if (customerIdStr != null) {
            r.setCustomerId(UUID.fromString(customerIdStr));
        } else {
            r.setCustomerId(null);
        }

        r.setSubstitute(rs.getBoolean("substitute"));

        return r;
    }

    /**
   * Checks if a reading with the given ID exists.
   *
   * @param id the UUID to check
   * @return true if the reading exists, false otherwise
   */
  public boolean exists(final UUID id) {
    final String sql = "SELECT COUNT(*) FROM reading WHERE id = ?";
    try (PreparedStatement ps = connection.prepareStatement(sql)) {
      ps.setString(1, id.toString());
      try (ResultSet rs = ps.executeQuery()) {
        if (rs.next()) {
          return rs.getInt(1) > 0;
        }
      }
    } catch (SQLException e) {
      System.err.println("Error checking existence of reading: " + id);
      e.printStackTrace();
    }
    return false;
  }

  /**
   * Safely returns a trimmed value or "null" if empty.
   *
   * @param value the input string
   * @return the trimmed string or "null"
   */
  String safeValue(final String value) {
    if (value == null || value.trim().isEmpty()) {
      return "null";
    }
    return value.trim();
  }

  /**
   * Formats a number in German style.
   *
   * @param number the number to format
   * @return the formatted string
   */
  public static String formatGermanStyle(final double number) {
    final NumberFormat nf = NumberFormat.getInstance(Locale.GERMANY);
    nf.setMinimumFractionDigits(2);
    return nf.format(number);
  }
}
