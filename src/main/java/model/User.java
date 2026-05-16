package model;

import java.util.UUID;

/** Repräsentiert einen Benutzer für die Anmeldung. */
public class User {
    private UUID   id;
    private String username;
    private String passwordHash; // BCrypt-Hash
    private String role;         // "ADMIN" oder "USER"

    public User() {}

    public User(UUID id, String username, String passwordHash, String role) {
        this.id           = id;
        this.username     = username;
        this.passwordHash = passwordHash;
        this.role         = role;
    }

    public UUID   getId()                        { return id; }
    public void   setId(UUID id)                 { this.id = id; }
    public String getUsername()                  { return username; }
    public void   setUsername(String username)   { this.username = username; }
    public String getPasswordHash()              { return passwordHash; }
    public void   setPasswordHash(String h)      { this.passwordHash = h; }
    public String getRole()                      { return role; }
    public void   setRole(String role)           { this.role = role; }
}