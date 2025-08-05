package com.darro_tech.revengproject.models;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Logger;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.proxy.HibernateProxy;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "users")
public class User {

    @Id
    @Size(max = 36)
    @Column(name = "id", nullable = false, length = 36)
    private String id;

    @Size(max = 255)
    @NotNull
    @Column(name = "username", nullable = false)
    private String username;

    @Size(max = 255)
    @Column(name = "firstName")
    private String firstName;

    @Size(max = 255)
    @Column(name = "lastName")
    private String lastName;

    @NotNull
    @Size(max = 50)
    @Column(name = "password", nullable = false, length = 50)
    private String pwHash;

    @NotNull
    @Column(name = "timestamp", nullable = false)
    private Instant timestamp;

    @NotNull
    @ColumnDefault("0")
    @Column(name = "reset_pswd", nullable = false)
    private Byte resetPswd;

    @Size(max = 6)
    @Column(name = "reset_pass_key", length = 6)
    private String resetPassKey;

    @Column(name = "reset_pass_expires")
    private Instant resetPassExpires;

    // Define many-to-many relationship with roles
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private Set<Role> roles = new HashSet<>();

    // Define many-to-many relationship with companies
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "company_users",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "company_id")
    )
    private Set<Company> companies = new HashSet<>();

    private static final Logger logger = Logger.getLogger(User.class.getName());
    private static final SecureRandom RANDOM = new SecureRandom();

    private String generateSalt() {
        byte[] salt = new byte[3];
        RANDOM.nextBytes(salt);
        StringBuilder sb = new StringBuilder();
        for (byte b : salt) {
            sb.append(String.format("%02x", b));
        }
        return sb.substring(0, 6);
    }

    public User(String username, String password) {
        this.id = UUID.randomUUID().toString();
        this.username = username;
        String salt = generateSalt();
        this.pwHash = salt + sha1(salt + password);
        this.timestamp = Instant.now();
        this.resetPswd = 0;
    }

    public User() {
        this.id = UUID.randomUUID().toString();
        this.timestamp = Instant.now();
        this.resetPswd = 0;
    }

    public boolean isMatchingPassword(String password) {
        if (pwHash == null || pwHash.length() < 6) {
            logger.warning("Stored password hash is invalid or too short");
            return false;
        }

        String salt = pwHash.substring(0, 6);
        String computed = salt + sha1(salt + password);

        boolean match = pwHash.equals(computed);
        if (!match) {
            logger.warning("Password mismatch for user: " + username);
        }

        return match;
    }

    private String sha1(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-1");
            byte[] hashBytes = md.digest(input.getBytes(StandardCharsets.UTF_8));

            StringBuilder sb = new StringBuilder();
            for (byte b : hashBytes) {
                sb.append(String.format("%02x", b));
            }

            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            logger.severe("SHA-1 algorithm not found");
            throw new RuntimeException(e);
        }
    }

    @Override
    public final boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null) {
            return false;
        }
        Class<?> oEffectiveClass = o instanceof HibernateProxy ? ((HibernateProxy) o).getHibernateLazyInitializer().getPersistentClass() : o.getClass();
        Class<?> thisEffectiveClass = this instanceof HibernateProxy ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass() : this.getClass();
        if (thisEffectiveClass != oEffectiveClass) {
            return false;
        }
        User user = (User) o;
        return getId() != null && Objects.equals(getId(), user.getId());
    }

    @Override
    public final int hashCode() {
        return this instanceof HibernateProxy ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass().hashCode() : getClass().hashCode();
    }

    public void setPassword(String password) {
        String salt = generateSalt();
        this.pwHash = salt + sha1(salt + password);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }

    public String getPwHash() {
        return pwHash;
    }

    public void setPwHash(String pwHash) {
        this.pwHash = pwHash;
    }

    public Byte getResetPswd() {
        return resetPswd;
    }

    public void setResetPswd(Byte resetPswd) {
        this.resetPswd = resetPswd;
    }

    public String getResetPassKey() {
        return resetPassKey;
    }

    public void setResetPassKey(String resetPassKey) {
        this.resetPassKey = resetPassKey;
    }

    public Instant getResetPassExpires() {
        return resetPassExpires;
    }

    public void setResetPassExpires(Instant resetPassExpires) {
        this.resetPassExpires = resetPassExpires;
    }

    public Set<Role> getRoles() {
        return roles;
    }

    public void setRoles(Set<Role> roles) {
        this.roles = roles;
    }

    public Set<Company> getCompanies() {
        return companies;
    }

    public void setCompanies(Set<Company> companies) {
        this.companies = companies;
    }
}
