package dao;

import databaseconnection.MariaDbConnection;
import model.Customer;
import model.Gender;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Data Access Object (DAO) for performing CRUD operations on
 * the {@code customer} table.Uses a MariaDB connection to
 * interact with the database.
 */
public class CustomerDao {
  /** The active database connection used for executing SQL statements. */
  private final Connection connection;

  /**
   * Constructs a new {@code CustomerDao} using the provided MariaDB connection.
   *
   * @param db the MariaDB connection wrapper.
   */
  public CustomerDao(final MariaDbConnection db) {
    this.connection = db.getConnection();
  }


  /**
   * Creates a new customer record in the database.
   * If the customer already exists, the method returns {@code false}.
   *
   * @param customer the customer to be created.
   * @return {@code true} if the customer was created successfully,
   * {@code false} otherwise.
   */
  public boolean create(final Customer customer) {

      // check if customer already exists
      if (exists(customer.getId())) {
          System.out.println("Customer already exists: "
                  + customer.getFirstName() + " " + customer.getLastName());
          return false;
      }

      String sql = "INSERT INTO customer (id, first_name, last_name, gender, birth_date)"
              + " VALUES (?, ?, ?, ?, ?)";

      try (PreparedStatement stmt = connection.prepareStatement(sql)) {

          stmt.setString(1, customer.getId().toString());
          stmt.setString(2, safeValue(customer.getFirstName()));
          stmt.setString(3, safeValue(customer.getLastName()));
          stmt.setString(4, customer.getGender() != null ? customer.getGender().name() : null);
          stmt.setDate(5, customer.getBirthDate() != null ? Date.valueOf(customer.getBirthDate()) : null);

          stmt.executeUpdate();
          return true;

      } catch (SQLException e) {
          System.err.println("Error creating customer: " + customer.getId());
          e.printStackTrace();
          return false;
      }
  }



  /**
   * Updates an existing customer record in the database.
   *
   * @param customer the customer with updated information.
   * @return
   */
  public boolean update(final Customer customer) {
    String sql =
            "UPDATE customer SET first_name=? , last_name=?, gender=?"
                    + " , birth_date=?" + "  where id = ?";
    try (PreparedStatement st = connection.prepareStatement(sql)) {
      // 1. first_name (Position 1)
      st.setString(1, safeValue(customer.getFirstName()));
      // 2. last_name (Position 2)
      st.setString(2, safeValue(customer.getLastName()));
      // 3. gender (Position 3)
      st.setString(3, String.valueOf(customer.getGender()));
      // 4. birth_date (Position 4)
      st.setDate(4, customer.getBirthDate() != null ? Date.valueOf(
              customer.getBirthDate()) : null);
      // 5. id (Position 5 - The WHERE clause)
      st.setString(5, customer.getId().toString());
      // Execute the update
      st.executeUpdate();
      System.out.println("Updated Kunde: " + customer);
    } catch (SQLException e) {
      System.err.println("Error in updating customer: " + customer.getId());
      e.printStackTrace();
    }
    return true;
  }


  /**
   * Deletes a customer from the database by their unique ID.
   *
   * @param uuid the UUID of the customer to delete.
   * @return {@code true} if the deletion was attempted, regardless of success.
   */
  public boolean delete(final UUID uuid) {
    String sql = "DELETE FROM customer WHERE id = ?";
    try (PreparedStatement st = connection.prepareStatement(sql)) {
      st.setString(1, uuid.toString());
      int deleteSql = st.executeUpdate();
      if (deleteSql > 0) {
        System.out.println("Kunde erfollgreich gelöcht worden! ");
      } else {
        System.out.println("Keine kunde mit diese ID gefunden: " + uuid);
      }
    } catch (SQLException e) {
      System.err.println("Error beim deleting Kunde: " + uuid);
      e.printStackTrace();
    }
    return true;
  }


  /**
   * Retrieves all customers from the database.
   *
   * @return an iterable list of all customers.
   */
  public List<Customer> findAll() {
    System.out.println("findAll customers entered");
    List<Customer> customers = new ArrayList<>();
    // Example with JDBC or your database access code
    try (PreparedStatement stmt = connection.prepareStatement(
            "SELECT * FROM customer");
         ResultSet rs = stmt.executeQuery()) {
      System.out.println("findAll customers query successful");
      while (rs.next()) {
        Customer c = new Customer();
        c.setId(UUID.fromString(rs.getString("id")));
        c.setFirstName(rs.getString("first_name"));
        c.setLastName(rs.getString("last_name"));
        c.setGender(Gender.valueOf(rs.getString("gender")));
        java.sql.Date sqlDate = rs.getDate("birth_date");
        c.setBirthDate(sqlDate != null ? sqlDate.toLocalDate() : null);
        // map other fields like gender, birthdate etc.
        customers.add(c);
      }
    } catch (SQLException e) {
      e.printStackTrace();
      // handle exceptions properly
    }
    return customers;
  }

  // Find mit ID oder Read
  public Optional<Customer> findById(UUID id) {
    String sql = "SELECT * FROM customer WHERE id = ?";
    try (PreparedStatement ps = connection.prepareStatement(sql)) {
      ps.setString(1, id.toString());
      try (ResultSet rs = ps.executeQuery()) {
        if (rs.next()) {
          Customer c = new Customer();
          c.setId(UUID.fromString(rs.getString("id")));
          c.setFirstName(rs.getString("first_name"));
          c.setLastName(rs.getString("last_name"));
          c.setGender(Gender.valueOf(rs.getString("gender")));
          java.sql.Date sqlDate = rs.getDate("birth_date");
          c.setBirthDate(sqlDate != null ? sqlDate.toLocalDate() : null);
          System.out.println(c);
          return Optional.of(c);
        }
      }
    } catch (SQLException e) {
      e.printStackTrace();
      // handle exceptions properly
    }
    return Optional.empty();
  }

  /**
   * Sanitizes a string value for safe database insertion.
   * Returns {@code "null"} if the input is {@code null} or empty.
   *
   * @param value the string to sanitize.
   * @return a trimmed string or {@code "null"}.
   */
  static String safeValue(final String value) {
    if (value == null || value.trim().isEmpty()) {
      return "null";
    }
    return value.trim();
  }


  /**
   * Checks whether a customer with the given ID exists in the database.
   *
   * @param id the UUID of the customer to check.
   * @return {@code true} if the customer exists, {@code false} otherwise.
   */
  public  boolean exists(final UUID id) {
    String checkSql = "SELECT COUNT(*) FROM customer WHERE id = ?";
    try (PreparedStatement ps = connection.prepareStatement(checkSql)) {
      ps.setString(1, String.valueOf(id));
      try (ResultSet rs = ps.executeQuery()) {
        if (rs.next()) {
          return rs.getInt(1) > 0;
        }
      }
    } catch (SQLException e) {
      e.printStackTrace();
    }
    return false;
  }
}