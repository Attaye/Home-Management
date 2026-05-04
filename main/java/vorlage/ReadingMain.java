package vorlage;

import dao.ReadingDao;
import databaseconnection.MariaDbConnection;
import model.Reading;

import java.sql.Connection;
import java.util.Properties;
import java.util.UUID;

import static util.Importedcsvreader.processCsvFile;

public class ReadingMain {

  private MariaDbConnection db;
  private ReadingDao readingDao;
  public ReadingMain(ReadingDao mockDao) {
    this.readingDao = mockDao;
  }

  public ReadingMain(MariaDbConnection db) {
    this.db = db;
    this.readingDao = new ReadingDao(db);
  }

  public void importCsvFiles(String[] files, Connection conn) throws Exception {
    for (String file : files) {
      processCsvFile(file, conn);
    }
  }

  public boolean createReading(Reading reading) {
    return readingDao.create(reading);
  }

  public Reading findReading(UUID id) {
    Iterable<Reading> it = readingDao.findById(id);
    return it.iterator().hasNext() ? it.iterator().next() : null;
  }

  public void updateReading(Reading reading) {
    readingDao.update(reading);
  }

  public void deleteReading(Reading reading) {
    readingDao.delete(reading);
  }

  public Iterable<Reading> findAllReadings() {
    return readingDao.findAll();
  }

  public static void main(String[] args) throws Exception {
    Properties props = new Properties();
    props.setProperty("db.url", "jdbc:mariadb://localhost:3306/itp-datenbank");
    props.setProperty("db.user", "admin");
    props.setProperty("db.password", "itpmariadb2025");

    MariaDbConnection db = new MariaDbConnection();
    db.openConnection(props).createAllTables();

    ReadingMain app = new ReadingMain(db);

    /// CSV data importing
    // Keep connection open
    Connection conn = db.getConnection();
    app.importCsvFiles(
        new String[] {"src/data/heizung.csv", "src/data/strom.csv", "src/data/wasser.csv"}, conn);

    /// Reading all the Reading table
    //    app.findAllReadings().forEach(System.out::println);

    /// Create test Reading object
    /* Reading reading = new Reading();
        reading.setId(UUID.fromString("3056f1fa-318e-4b1d-9e3c-9e2b57808295"));
        reading.setDateOfReading(LocalDate.now());
        reading.setMeterCount(123.45);
        reading.setMeterId("MTR-001");
        reading.setComment("Initial test reading");
        reading.setKindOfMeter(KindOfMeter.HEIZUNG); // Assuming enum KindOfMeter exists
        reading.setCustomerId(UUID.fromString("00d326bf-8873-4aba-be4c-065d506665ca"));
        app.createReading(reading);
    */
    /// Reading the By id
    /* UUID targetIdFind = UUID.fromString("3056f1fa-318e-4b1d-9e3c-9e2b57808295"); /// id must be checking with Reading Table!
    System.out.println(app.findReading(targetIdFind));*/

    /// Update the Reading object
    /*reading.setId(UUID.fromString("3056f1fa-318e-4b1d-9e3c-9e2b57808295")); ///beim abrufe muss ich auf id achten ob aktuel ist!
      reading.setComment("Updated test reading");
      reading.setMeterCount(543.21);
      reading.setCustomerId(UUID.fromString("007522c8-54cc-4637-a164-b6f72609deb6"));
      app.updateReading(reading);
      System.out.println("reading updated" + app.findReading(reading.getId()));

      /// Reading the By id
      UUID targetIdFind1 = UUID.fromString("3056f1fa-318e-4b1d-9e3c-9e2b57808295"); /// id must be checking with Reading Table!
      System.out.println(app.findReading(targetIdFind1));

      /// Delete a Reading
      reading.setId(UUID.fromString("3056f1fa-318e-4b1d-9e3c-9e2b57808295"));/// id must be checking with Reading Table!
      app.deleteReading(reading);

      /// Reading the By id
      UUID targetIdFind2 = UUID.fromString("3056f1fa-318e-4b1d-9e3c-9e2b57808295"); /// id must be checking with Reading Table!
      System.out.println(app.findReading(targetIdFind2));

      db.closeConnection();
      System.out.println("✅ Import complete.");
    }*/
  }
}
