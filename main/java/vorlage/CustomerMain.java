package vorlage;
import dao.CustomerDao;
import databaseconnection.MariaDbConnection;
import model.Gender;
import util.CsvReader;
import util.DataConverter;
import model.Customer;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.UUID;

public class CustomerMain {

  // --------------------- Modularer Run ---------------------
  public static void run(MariaDbConnection db, CustomerDao customerDao, String csvPath) throws SQLException {
    // CSV importieren
    importCustomers(customerDao, csvPath);
  }

  // --------------------- Main ---------------------
  public static void main(String[] args) throws SQLException {
    Properties props = new Properties();
    props.setProperty("db.url", "jdbc:mariadb://localhost:3306/itp-datenbank");
    props.setProperty("db.user", "admin");
    props.setProperty("db.password", "itpmariadb2025");

    MariaDbConnection db = new MariaDbConnection();
    db.openConnection(props).createAllTables();
    CustomerDao customerDao = new CustomerDao(db);

    run(db, customerDao, "src/data/kunden_utf8.csv");
    customerDao.findAll().forEach(System.out::println);

    //// eine Kunde erstellen /////
    System.out.println("//// eine Kunde erstellen /////");
    System.out.println("///eine neue Kunde erstellen! ////----------------");
    UUID targetIdCreate = UUID.fromString("007522c8-54cc-4637-a164-b6f72609deb6");
    var c = new Customer();
    c.setId(targetIdCreate);
    c.setFirstName("Ali");
    c.setLastName("Muster");
    c.setGender(Gender.M);
    String bd = "01.01.2000";
    c.setBirthDate(DataConverter.parseGermanDate((bd)));
    System.out.println(customerDao.create(c));
    System.out.print("\n\n\n\n\n\n\n");

    /// Read ById
    System.out.println("// Read ById-----------------------------------------------------------------------------");
    UUID targetIdRead = UUID.fromString("007522c8-54cc-4637-a164-b6f72609deb6");
    System.out.println(customerDao.findById(targetIdRead));
    System.out.print("\n\n\n\n\n\n\n");

    /// Update die Kunden mit neue daten! ////
    System.out.println("/// Update die Kunden mit neue daten! ////-----");
    UUID targetIdUpdate = UUID.fromString("007522c8-54cc-4637-a164-b6f72609deb6");
    var customer = new Customer();
    customer.setId(targetIdUpdate);
    customer.setFirstName("AliUpdated");
    customer.setLastName("Muster");
    customer.setGender(Gender.M);
    String dateOfBirth = "01.01.2000";
    customer.setBirthDate(DataConverter.parseGermanDate((dateOfBirth)));
    customerDao.update(customer);
    System.out.print("\n\n\n\n\n\n\n");

    db.closeConnection();
    System.out.println("✅ Import complete.");
  }

  // --------------------- CSV Import ---------------------
  public static void importCustomers(CustomerDao dao, String path) {
    List<String[]> rows = CsvReader.readCsv(path);
    for (String[] row : rows) {
      try {
        if (row.length < 4) { // Branch: unvollständige Zeile
          System.err.println("⚠️ Skipping incomplete line: " + Arrays.toString(row));
          continue;
        }

        Customer c = parseCustomer(row);

        if (!dao.exists(c.getId())) { // Branch: neuer Kunde
          dao.create(c);
        } else { // Branch: existierender Kunde
          System.out.println("↩ Customer exists: " + c.getId());
        }

      } catch (Exception e) { // Branch: Fehler beim Parsen
        System.err.println("⚠️ Error importing line: " + Arrays.toString(row));
        e.printStackTrace();
      }
    }
  }

  // --------------------- Customer Parsing ---------------------
  public static Customer parseCustomer(String[] row) {
    Customer c = new Customer();
    c.setId(UUID.fromString(row[0].trim()));

    c.setGender(DataConverter.mapGender(row[1]));

    String fn = row[2].trim();
    c.setFirstName(fn.isEmpty() ? "null" : fn);

    String ln = row[3].trim();
    c.setLastName(ln.isEmpty() ? "null" : ln);

    LocalDate bd = null;
    if (row.length >= 5 && row[4] != null && !row[4].trim().isEmpty()) {
      bd = DataConverter.parseGermanDate(row[4].trim());
    }
    c.setBirthDate(bd);

    return c;
  }

}
