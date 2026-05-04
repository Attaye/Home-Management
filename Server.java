package vorlage;

import controller.CustomerController;
import controller.ReadingController;
import controller.SetupController;
import dao.CustomerDao;
import dao.ReadingDao;
import databaseconnection.MariaDbConnection;
import io.javalin.Javalin;
import io.javalin.plugin.bundled.CorsPluginConfig;

import java.util.Properties;

/** Server class responsible for starting and stopping the Javalin application. */
public class Server {

  private static Javalin app;

  /**
   * Starts the Javalin server on port 8080 and initializes all controllers and database
   * connections.
   */
  public static void startServer() {


    app = Javalin.create(config -> {
      config.plugins.enableCors(cors -> {
        cors.add(CorsPluginConfig::anyHost);
      });
    }).start(8080);


    Properties props = new Properties();
    props.setProperty("db.url", "jdbc:mariadb://localhost:3306/itp-datenbank");
    props.setProperty("db.user", "admin");
    props.setProperty("db.password", "itpmariadb2025");

    MariaDbConnection db = new MariaDbConnection();
    db.openConnection(props).createAllTables();
    CustomerDao customerDao = new CustomerDao(db);
    ReadingDao readingDao = new ReadingDao(db);
    new CustomerController(app, customerDao);
    new ReadingController(app, readingDao);
    new SetupController(app);

    System.out.println("Server läuft auf http://localhost:8080");
  }

  public static void stopServer() {
    if (app != null) app.stop();
  }
}
