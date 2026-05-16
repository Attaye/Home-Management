package vorlage;

import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import controller.AuthController;
import controller.CustomerController;
import controller.ImportController;
import controller.ReadingController;
import controller.SetupController;
import controller.UserController;
import dao.CustomerDao;
import dao.ReadingDao;
import dao.UserDao;
import databaseconnection.MariaDbConnection;
import io.javalin.Javalin;
import io.javalin.json.JavalinJackson;

import java.util.Properties;

public class Server {

  private static Javalin app;

  public static void startServer() {

    app = Javalin.create(config -> {
      config.plugins.enableCors(cors -> {
        cors.add(it -> {
          it.allowHost("http://localhost:4200");
          it.allowHost("http://localhost:80");
          it.allowCredentials = true;
        });
      });
      config.jsonMapper(new JavalinJackson().updateMapper(mapper -> {
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
      }));
    }).start(8080);

    Properties props = new Properties();
    // Umgebungsvariablen nutzen falls vorhanden (Docker), sonst Defaults
    props.setProperty("db.url",      System.getenv().getOrDefault("DB_URL",      "jdbc:mariadb://localhost:3306/itp-datenbank"));
    props.setProperty("db.user",     System.getenv().getOrDefault("DB_USER",     "admin"));
    props.setProperty("db.password", System.getenv().getOrDefault("DB_PASSWORD", "itpmariadb2025"));

    MariaDbConnection db = new MariaDbConnection();
    db.openConnection(props).createAllTables();

    CustomerDao customerDao = new CustomerDao(db);
    ReadingDao  readingDao  = new ReadingDao(db);
    UserDao     userDao     = new UserDao(db);

    userDao.seedDefaultAdmin();

    new AuthController(app, userDao);
    new CustomerController(app, customerDao);
    new ReadingController(app, readingDao);
    new SetupController(app, db, userDao);  // ← db + userDao übergeben
    new ImportController(app, db);
    new UserController(app, userDao);

    System.out.println("Server läuft auf http://localhost:8080");
    System.out.println("Login: admin / admin123");
  }

  public static void stopServer() {
    if (app != null) app.stop();
  }
}