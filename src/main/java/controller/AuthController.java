package controller;

import dao.UserDao;
import dto.LoginRequest;
import dto.LoginResponse;
import io.javalin.Javalin;
import io.javalin.http.Context;
import io.javalin.http.HttpStatus;
import model.User;
import util.PasswordUtil;
import util.TokenStore;

import java.util.Map;
import java.util.Optional;

/**
 * Verwaltet Authentifizierungs-Endpoints.
 *
 * Endpoints:
 *   POST /auth/login   → { token, username, role }
 *   POST /auth/logout  → Token löschen
 *   GET  /auth/me      → Benutzerinfo (erfordert Bearer Token)
 */
public class AuthController {

    private final UserDao userDao;

    public AuthController(Javalin app, UserDao userDao) {
        this.userDao = userDao;

        app.post("/auth/login",  this::login);
        app.post("/auth/logout", this::logout);
        app.get( "/auth/me",     this::me);
    }

    // ── POST /auth/login ─────────────────────────────────────────────────

    private void login(Context ctx) {
        LoginRequest req;
        try {
            req = ctx.bodyAsClass(LoginRequest.class);
        } catch (Exception e) {
            ctx.status(HttpStatus.BAD_REQUEST).result("Ungültiger Request-Body");
            return;
        }

        if (req.getUsername() == null || req.getUsername().isBlank() ||
                req.getPassword() == null || req.getPassword().isBlank()) {
            ctx.status(HttpStatus.BAD_REQUEST).result("Benutzername und Passwort erforderlich");
            return;
        }

        Optional<User> userOpt = userDao.findByUsername(req.getUsername());

        if (userOpt.isEmpty() || !PasswordUtil.verify(req.getPassword(), userOpt.get().getPasswordHash())) {
            ctx.status(HttpStatus.UNAUTHORIZED).result("Benutzername oder Passwort falsch");
            return;
        }

        User   user  = userOpt.get();
        String token = TokenStore.createToken(user.getUsername());

        System.out.println("Login: " + user.getUsername());
        ctx.status(HttpStatus.OK).json(new LoginResponse(token, user.getUsername(), user.getRole()));
    }

    // ── POST /auth/logout ────────────────────────────────────────────────

    private void logout(Context ctx) {
        String token = extractToken(ctx);
        TokenStore.invalidate(token);
        ctx.status(HttpStatus.OK).result("Erfolgreich abgemeldet");
    }

    // ── GET /auth/me ─────────────────────────────────────────────────────

    private void me(Context ctx) {
        String token    = extractToken(ctx);
        String username = TokenStore.getUsernameForToken(token);

        if (username == null) {
            ctx.status(HttpStatus.UNAUTHORIZED).result("Token ungültig oder abgelaufen");
            return;
        }

        Optional<User> userOpt = userDao.findByUsername(username);
        if (userOpt.isEmpty()) {
            ctx.status(HttpStatus.NOT_FOUND).result("Benutzer nicht gefunden");
            return;
        }

        User u = userOpt.get();
        ctx.status(HttpStatus.OK).json(Map.of(
                "id",       u.getId().toString(),
                "username", u.getUsername(),
                "role",     u.getRole()
        ));
    }

    // ── Hilfsmethoden ─────────────────────────────────────────────────────

    /**
     * Extrahiert den Bearer-Token aus dem Authorization-Header.
     * Fallback: X-Auth-Token Header.
     */
    public static String extractToken(Context ctx) {
        String auth = ctx.header("Authorization");
        if (auth != null && auth.startsWith("Bearer ")) {
            return auth.substring(7).trim();
        }
        return ctx.header("X-Auth-Token");
    }

    /**
     * Prüft ob der Request einen gültigen Token enthält.
     * Verwendung in anderen Controllern:
     *   if (!AuthController.requireAuth(ctx)) return;
     */
    public static boolean requireAuth(Context ctx) {
        String token = extractToken(ctx);
        if (!TokenStore.isValid(token)) {
            ctx.status(HttpStatus.UNAUTHORIZED).result("Anmeldung erforderlich");
            return false;
        }
        return true;
    }
}