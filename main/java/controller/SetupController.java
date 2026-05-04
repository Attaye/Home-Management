package controller;

import dao.CustomerDao;
import dao.ReadingDao;
import databaseconnection.MariaDbConnection;
import io.javalin.Javalin;
import io.javalin.http.Context;

/**
 * Controller responsible for setup-related operations.
 *
 * <p>This controller provides an endpoint to reset the database.
 */
public class SetupController {

    MariaDbConnection db = new MariaDbConnection();
    private final CustomerDao customerDao = new CustomerDao(db);
    private final ReadingDao readingDao = new ReadingDao(db);

    /**
     * Creates a new SetupController and registers the setup endpoint.
     *
     * @param app the Javalin application instance
     */
    public SetupController(Javalin app) {
        app.delete("/setupDB", this::resetDatabase);
    }

    /**
     * Resets the database.
     *
     * <p>Returns HTTP 200 if successful or 500 if an error occurs.
     *
     * @param ctx the Javalin request context
     */
    private void resetDatabase(Context ctx) {
        try {
            ctx.status(200).result("Database reset successfully");
        } catch (Exception e) {
            ctx.status(500).result("Error resetting database");
        }
    }
}