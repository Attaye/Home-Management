package controller;

import dao.UserDao;
import io.javalin.Javalin;
import io.javalin.http.Context;
import io.javalin.http.HttpStatus;
import model.User;
import util.PasswordUtil;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Admin-Endpoints für Benutzerverwaltung.
 *
 * GET    /admin/users              → Alle Benutzer auflisten
 * POST   /admin/users              → Neuen Benutzer anlegen
 * PUT    /admin/users/{username}   → Passwort ändern
 * DELETE /admin/users/{username}   → Benutzer löschen
 */
public class UserController {

    private final UserDao userDao;

    public UserController(Javalin app, UserDao userDao) {
        this.userDao = userDao;
        app.get("/admin/users",                this::getAllUsers);
        app.post("/admin/users",               this::createUser);
        app.put("/admin/users/{username}",     this::changePassword);
        app.delete("/admin/users/{username}",  this::deleteUser);
    }

    // ── GET /admin/users ─────────────────────────────────────────────────

    private void getAllUsers(Context ctx) {
        if (!AuthController.requireAuth(ctx)) return;

        List<User> users = userDao.findAll();
        List<Map<String, String>> response = users.stream().map(u -> Map.of(
                "id",       u.getId().toString(),
                "username", u.getUsername(),
                "role",     u.getRole()
        )).toList();

        ctx.status(HttpStatus.OK).json(response);
    }

    // ── POST /admin/users ─────────────────────────────────────────────────

    private void createUser(Context ctx) {
        if (!AuthController.requireAuth(ctx)) return;

        try {
            @SuppressWarnings("unchecked")
            Map<String, String> body = ctx.bodyAsClass(Map.class);
            String username = body.get("username");
            String password = body.get("password");
            String role     = body.getOrDefault("role", "USER");

            if (username == null || username.isBlank() ||
                    password == null || password.isBlank()) {
                ctx.status(HttpStatus.BAD_REQUEST).result("Username und Passwort erforderlich.");
                return;
            }

            if (userDao.findByUsername(username).isPresent()) {
                ctx.status(HttpStatus.CONFLICT)
                        .result("Benutzername '" + username + "' existiert bereits.");
                return;
            }

            Optional<User> created = userDao.create(username, password, role.toUpperCase());
            if (created.isPresent()) {
                User u = created.get();
                ctx.status(HttpStatus.CREATED).json(Map.of(
                        "id",       u.getId().toString(),
                        "username", u.getUsername(),
                        "role",     u.getRole()
                ));
            } else {
                ctx.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .result("Fehler beim Anlegen des Benutzers.");
            }
        } catch (Exception e) {
            ctx.status(HttpStatus.BAD_REQUEST).result("Ungültiger Request: " + e.getMessage());
        }
    }

    // ── PUT /admin/users/{username} – Passwort ändern ────────────────────

    private void changePassword(Context ctx) {
        if (!AuthController.requireAuth(ctx)) return;

        String username = ctx.pathParam("username");

        try {
            @SuppressWarnings("unchecked")
            Map<String, String> body = ctx.bodyAsClass(Map.class);
            String newPassword = body.get("password");

            if (newPassword == null || newPassword.isBlank() || newPassword.length() < 6) {
                ctx.status(HttpStatus.BAD_REQUEST)
                        .result("Passwort muss mindestens 6 Zeichen haben.");
                return;
            }

            if (userDao.findByUsername(username).isEmpty()) {
                ctx.status(HttpStatus.NOT_FOUND)
                        .result("Benutzer '" + username + "' nicht gefunden.");
                return;
            }

            boolean updated = userDao.updatePassword(username, newPassword);
            if (updated) {
                ctx.status(HttpStatus.OK).result("Passwort für '" + username + "' geändert.");
            } else {
                ctx.status(HttpStatus.INTERNAL_SERVER_ERROR).result("Fehler beim Ändern des Passworts.");
            }
        } catch (Exception e) {
            ctx.status(HttpStatus.BAD_REQUEST).result("Ungültiger Request: " + e.getMessage());
        }
    }

    // ── DELETE /admin/users/{username} ────────────────────────────────────

    private void deleteUser(Context ctx) {
        if (!AuthController.requireAuth(ctx)) return;

        String username    = ctx.pathParam("username");
        String currentUser = util.TokenStore.getUsernameForToken(
                AuthController.extractToken(ctx));

        if (username.equalsIgnoreCase(currentUser)) {
            ctx.status(HttpStatus.FORBIDDEN)
                    .result("Du kannst deinen eigenen Account nicht löschen.");
            return;
        }

        if (userDao.findByUsername(username).isEmpty()) {
            ctx.status(HttpStatus.NOT_FOUND)
                    .result("Benutzer '" + username + "' nicht gefunden.");
            return;
        }

        userDao.deleteByUsername(username);
        ctx.status(HttpStatus.OK).result("Benutzer '" + username + "' gelöscht.");
    }
}