package controller;

import dao.UserDao;
import databaseconnection.MariaDbConnection;
import io.javalin.Javalin;
import io.javalin.http.Context;
import io.javalin.http.HttpStatus;

/**
 * DELETE /setupDB  – Alle Tabellen löschen, neu anlegen, Admin re-seeden.
 */
public class SetupController {

    private final MariaDbConnection db;
    private final UserDao userDao;

    public SetupController(Javalin app, MariaDbConnection db, UserDao userDao) {
        this.db      = db;
        this.userDao = userDao;
        app.delete("/setupDB", this::resetDatabase);
    }

    private void resetDatabase(Context ctx) {
        try {
            // 1. Alle Tabellen löschen
            db.removeAllTables();

            // 2. Alle Tabellen neu anlegen
            db.createAllTables();

            // 3. Standard-Admin wieder anlegen ← FIX!
            userDao.seedDefaultAdmin();

            ctx.status(HttpStatus.OK)
                    .result("Datenbank zurückgesetzt. Standard-Admin: admin / admin123");
        } catch (Exception e) {
            ctx.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .result("Fehler beim Zurücksetzen: " + e.getMessage());
        }
    }
}