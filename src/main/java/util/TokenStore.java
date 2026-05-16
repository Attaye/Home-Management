package util;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In-Memory Token-Speicher.
 * Speichert: Token (UUID) → Username.
 *
 * Hinweis: Tokens werden bei Server-Neustart gelöscht.
 * Für Produktion: JWT oder DB-basierte Sessions verwenden.
 */
public class TokenStore {

    private static final Map<String, String> TOKENS = new ConcurrentHashMap<>();

    private TokenStore() {}

    /** Erstellt einen neuen Token für den Benutzer und speichert ihn. */
    public static String createToken(String username) {
        String token = UUID.randomUUID().toString();
        TOKENS.put(token, username);
        return token;
    }

    /** Gibt den Username für einen Token zurück – null wenn ungültig. */
    public static String getUsernameForToken(String token) {
        if (token == null) return null;
        return TOKENS.get(token);
    }

    /** Löscht einen Token (Logout). */
    public static void invalidate(String token) {
        if (token != null) TOKENS.remove(token);
    }

    /** Prüft ob ein Token gültig ist. */
    public static boolean isValid(String token) {
        return token != null && TOKENS.containsKey(token);
    }
}