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

    private final Connection connection;
    private static final Logger LOGGER = Logger.getLogger(ReadingDao.class.getName());

    public ReadingDao(final MariaDbConnection db) {
        this.connection = db.getConnection();
    }

    public boolean create(final Reading r) {
        if (exists(r.getId())) return false;
        final String sql =
                "INSERT INTO reading (id, date_of_reading, meter_count, meter_id, comment, kind_of_meter, customer_id, substitute) "
                        + "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, r.getId().toString());
            if (r.getDateOfReading() != null) ps.setDate(2, Date.valueOf(r.getDateOfReading()));
            else ps.setNull(2, Types.DATE);
            ps.setDouble(3, r.getMeterCount());
            ps.setString(4, r.getMeterId() == null ? "null" : r.getMeterId());
            ps.setString(5, r.getComment() == null ? "null" : r.getComment());
            ps.setString(6, r.getKindOfMeter() == null ? "UNBEKANNT" : r.getKindOfMeter().name());
            ps.setString(7, r.getCustomerId() != null ? r.getCustomerId().toString() : null);
            ps.setBoolean(8, r.getSubstitute());
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error inserting reading: " + e.getMessage());
            return false;
        }
        return true;
    }

    public boolean update(final Reading reading) {
        if (!exists(reading.getId())) return false;
        final String sql =
                "UPDATE reading SET date_of_reading=?, meter_count=?, meter_id=?, "
                        + "comment=?, kind_of_meter=?, customer_id=?, substitute=? WHERE id=?";
        try (PreparedStatement st = connection.prepareStatement(sql)) {
            st.setDate(1, Date.valueOf(reading.getDateOfReading()));
            st.setDouble(2, reading.getMeterCount());
            st.setString(3, reading.getMeterId());
            st.setString(4, reading.getComment());
            st.setString(5, reading.getKindOfMeter() == null ? "UNBEKANNT" : reading.getKindOfMeter().name());
            if (reading.getCustomerId() != null) st.setString(6, reading.getCustomerId().toString());
            else st.setNull(6, Types.VARCHAR);
            st.setBoolean(7, reading.getSubstitute());
            st.setString(8, reading.getId().toString());
            st.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error updating reading: " + reading.getId());
            e.printStackTrace();
        }
        return true;
    }

    public void delete(final Reading reading) {
        if (!exists(reading.getId())) return;
        try (PreparedStatement st = connection.prepareStatement("DELETE FROM reading WHERE id = ?")) {
            st.setString(1, reading.getId().toString());
            st.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error deleting reading: " + reading.getId());
        }
    }

    public Iterable<Reading> findById(final UUID id) {
        final List<Reading> readings = new ArrayList<>();
        try (PreparedStatement st = connection.prepareStatement("SELECT * FROM reading WHERE id = ?")) {
            st.setString(1, id.toString());
            try (ResultSet rs = st.executeQuery()) {
                if (rs.next()) readings.add(mapRow(rs));
            }
        } catch (SQLException e) {
            System.err.println("Could not find reading with ID: " + id);
        }
        return readings;
    }

    public List<Reading> findAll() {
        final List<Reading> readings = new ArrayList<>();
        try (PreparedStatement stmt = connection.prepareStatement("SELECT * FROM reading");
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) readings.add(mapRow(rs));
        } catch (SQLException e) {
            System.err.println("Error retrieving readings.");
        }
        return readings;
    }

    public List<Reading> findFiltered(UUID customerId, String start, String end, String kindOfMeter) {
        List<Reading> result = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT * FROM reading WHERE 1=1");
        if (customerId  != null) sql.append(" AND customer_id = ?");
        if (start       != null) sql.append(" AND date_of_reading >= ?");
        if (end         != null) sql.append(" AND date_of_reading <= ?");
        if (kindOfMeter != null) sql.append(" AND kind_of_meter = ?");

        try (PreparedStatement ps = connection.prepareStatement(sql.toString())) {
            int i = 1;
            if (customerId  != null) ps.setString(i++, customerId.toString());
            if (start       != null) ps.setDate(i++,   Date.valueOf(start));
            if (end         != null) ps.setDate(i++,   Date.valueOf(end));
            if (kindOfMeter != null) ps.setString(i++, kindOfMeter);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) result.add(mapRow(rs));
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return result;
    }

    private Reading mapRow(ResultSet rs) throws SQLException {
        Reading r = new Reading();
        r.setId(UUID.fromString(rs.getString("id")));
        Date sqlDate = rs.getDate("date_of_reading");
        r.setDateOfReading(sqlDate != null ? sqlDate.toLocalDate() : null);
        r.setMeterCount(rs.getDouble("meter_count"));
        r.setMeterId(rs.getString("meter_id"));
        r.setComment(rs.getString("comment"));
        r.setKindOfMeter(KindOfMeter.valueOf(rs.getString("kind_of_meter")));
        String cid = rs.getString("customer_id");
        r.setCustomerId(cid != null ? UUID.fromString(cid) : null);
        r.setSubstitute(rs.getBoolean("substitute"));
        return r;
    }

    public boolean exists(final UUID id) {
        try (PreparedStatement ps = connection.prepareStatement(
                "SELECT COUNT(*) FROM reading WHERE id = ?")) {
            ps.setString(1, id.toString());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            System.err.println("Error checking existence: " + id);
        }
        return false;
    }

    // ── Utility-Methoden (werden von Tests getestet) ─────────────────────────

    /**
     * Gibt "null" zurück wenn der Wert leer oder null ist, sonst den getrimmten Wert.
     * Package-private damit Tests zugreifen können.
     */
    String safeValue(final String value) {
        if (value == null || value.trim().isEmpty()) {
            return "null";
        }
        return value.trim();
    }

    /**
     * Formatiert eine Zahl im deutschen Stil (z.B. 1234.56 → "1.234,56").
     */
    public static String formatGermanStyle(final double number) {
        final NumberFormat nf = NumberFormat.getInstance(Locale.GERMANY);
        nf.setMinimumFractionDigits(2);
        return nf.format(number);
    }
}