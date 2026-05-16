package dao;

import databaseconnection.MariaDbConnection;
import model.User;
import util.PasswordUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class UserDao {

    private final Connection connection;

    public UserDao(MariaDbConnection db) {
        this.connection = db.getConnection();
    }

    /** Neuen Benutzer anlegen. Passwort wird BCrypt-gehasht. */
    public Optional<User> create(String username, String plainPassword, String role) {
        String id   = UUID.randomUUID().toString();
        String hash = PasswordUtil.hash(plainPassword);
        try (PreparedStatement ps = connection.prepareStatement(
                "INSERT INTO users (id, username, password, role) VALUES (?, ?, ?, ?)")) {
            ps.setString(1, id);
            ps.setString(2, username);
            ps.setString(3, hash);
            ps.setString(4, role);
            ps.executeUpdate();
            return Optional.of(new User(UUID.fromString(id), username, hash, role));
        } catch (SQLException e) {
            System.err.println("Fehler beim Erstellen: " + e.getMessage());
            return Optional.empty();
        }
    }

    /** Benutzer nach Username suchen (case-insensitive). */
    public Optional<User> findByUsername(String username) {
        try (PreparedStatement ps = connection.prepareStatement(
                "SELECT id, username, password, role FROM users WHERE LOWER(username) = LOWER(?)")) {
            ps.setString(1, username);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return Optional.of(new User(
                        UUID.fromString(rs.getString("id")),
                        rs.getString("username"),
                        rs.getString("password"),
                        rs.getString("role")
                ));
            }
        } catch (SQLException e) {
            System.err.println("Fehler beim Suchen: " + e.getMessage());
        }
        return Optional.empty();
    }

    /** Alle Benutzer abrufen (ohne Passwort-Hash). */
    public List<User> findAll() {
        List<User> users = new ArrayList<>();
        try (PreparedStatement ps = connection.prepareStatement(
                "SELECT id, username, role FROM users ORDER BY username");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                users.add(new User(
                        UUID.fromString(rs.getString("id")),
                        rs.getString("username"),
                        "",  // Passwort nicht zurückgeben!
                        rs.getString("role")
                ));
            }
        } catch (SQLException e) {
            System.err.println("Fehler beim Laden: " + e.getMessage());
        }
        return users;
    }

    /** Passwort ändern. Neues Passwort wird gehasht. */
    public boolean updatePassword(String username, String newPlainPassword) {
        String hash = PasswordUtil.hash(newPlainPassword);
        try (PreparedStatement ps = connection.prepareStatement(
                "UPDATE users SET password = ? WHERE LOWER(username) = LOWER(?)")) {
            ps.setString(1, hash);
            ps.setString(2, username);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Fehler beim Passwort ändern: " + e.getMessage());
            return false;
        }
    }

    /** Benutzer löschen. */
    public boolean deleteByUsername(String username) {
        try (PreparedStatement ps = connection.prepareStatement(
                "DELETE FROM users WHERE LOWER(username) = LOWER(?)")) {
            ps.setString(1, username);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Fehler beim Löschen: " + e.getMessage());
            return false;
        }
    }

    /** Admin anlegen wenn Tabelle leer. Standard: admin / admin123 */
    public void seedDefaultAdmin() {
        try (Statement stmt = connection.createStatement();
             ResultSet rs   = stmt.executeQuery("SELECT COUNT(*) FROM users")) {
            if (rs.next() && rs.getInt(1) == 0) {
                create("admin", "admin123", "ADMIN");
                System.out.println("✅ Standard-Admin: admin / admin123");
            }
        } catch (SQLException e) {
            System.err.println("Fehler beim Seeden: " + e.getMessage());
        }
    }
}