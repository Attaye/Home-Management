package util;

import org.mindrot.jbcrypt.BCrypt;

/**
 * Hilfsklasse für Passwort-Hashing mit BCrypt.
 *
 * pom.xml Dependency:
 *   <dependency>
 *       <groupId>org.mindrot</groupId>
 *       <artifactId>jbcrypt</artifactId>
 *       <version>0.4</version>
 *   </dependency>
 */
public class PasswordUtil {

    private PasswordUtil() {}

    /** Erstellt einen BCrypt-Hash des Passworts (Cost-Faktor 12). */
    public static String hash(String plainPassword) {
        return BCrypt.hashpw(plainPassword, BCrypt.gensalt(12));
    }

    /** Prüft ob ein Klartext-Passwort zum gespeicherten Hash passt. */
    public static boolean verify(String plainPassword, String hashedPassword) {
        try {
            return BCrypt.checkpw(plainPassword, hashedPassword);
        } catch (Exception e) {
            return false;
        }
    }
}