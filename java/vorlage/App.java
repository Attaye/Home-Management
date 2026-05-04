package vorlage;

/** Entry point of the application. */
public class App {

  /**
   * Starts the application.
   *
   * @param args command-line arguments
   */
  public static void main(String[] args) {
    Server server = new Server();
    server.startServer();
  }
}
