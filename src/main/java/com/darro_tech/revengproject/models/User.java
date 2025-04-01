package com.darro_tech.revengproject.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.proxy.HibernateProxy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.Objects;
import java.util.logging.Logger;

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


    private static final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();


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

    public User(String username, String password) {
        this.username = username;
        this.pwHash = encoder.encode(password);
    }

    public User() {

    }

    private static final Logger logger = Logger.getLogger(User.class.getName());

    public boolean isMatchingPassword(String password) {
        if (pwHash == null || pwHash.length() < 6) {
            logger.warning("Stored password hash is invalid: " + pwHash);
            return false;
        }

        String salt = pwHash.substring(0, 6);
        String computed = salt + sha1(salt + password);

        boolean match = pwHash.equals(computed);
        if (!match) {
            logger.warning("Password mismatch. Input: " + password + ", Computed: " + computed);
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
        if (this == o) return true;
        if (o == null) return false;
        Class<?> oEffectiveClass = o instanceof HibernateProxy ? ((HibernateProxy) o).getHibernateLazyInitializer().getPersistentClass() : o.getClass();
        Class<?> thisEffectiveClass = this instanceof HibernateProxy ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass() : this.getClass();
        if (thisEffectiveClass != oEffectiveClass) return false;
        User user = (User) o;
        return getId() != null && Objects.equals(getId(), user.getId());
    }

    @Override
    public final int hashCode() {
        return this instanceof HibernateProxy ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass().hashCode() : getClass().hashCode();
    }
}