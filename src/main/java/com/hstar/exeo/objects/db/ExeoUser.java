package com.hstar.exeo.objects.db;

import com.hstar.exeo.server.security.PasswordUtils;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.Hex;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.sql.Timestamp;
import java.util.UUID;

/**
 * Representation of a user in the rdbms
 * Created by Saswat on 7/31/2015.
 */
@Entity
@Table(name = "users")
public class ExeoUser {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "user_id")
    private long id;

    @Column(name = "user_uuid", unique = true)
    @GeneratedValue(generator="system-uuid")
    @GenericGenerator(name="system-uuid", strategy = "uuid")
    private String uuid;

    @Column(name = "user_local_login", columnDefinition = "TINYINT", length = 1)
    private boolean localLogin;

    @Column(name = "user_email")
    private String email;

    @Column(name = "user_password")
    private String password;

    @Column(name = "user_password_expired", columnDefinition = "TINYINT", length = 1)
    private boolean passwordExpired;

    @Column(name = "user_random")
    private String random;

    @Column(name = "user_token_random")
    private String tokenRandom;

    @Column(name = "date_created")
    private Timestamp dateCreated;

    protected ExeoUser() {}

    public ExeoUser(String email, String password) {
        SecureRandom rand = new SecureRandom();
        byte r[] = new byte[32];

        this.uuid = UUID.randomUUID().toString();

        this.localLogin = password != null;
        this.email = email;
        this.password = password;
        this.passwordExpired = false;

        rand.nextBytes(r);
        this.random = Hex.encodeHexString(r);

        rand.nextBytes(r);
        this.tokenRandom = Hex.encodeHexString(r);

        dateCreated = new Timestamp(System.currentTimeMillis());
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public boolean getLocalLogin() {
        return localLogin;
    }

    public void setLocalLogin(boolean local_login) {
        this.localLogin = local_login;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public boolean getPasswordExpired() {
        return passwordExpired;
    }

    public void setPasswordExpired(boolean password_expired) {
        this.passwordExpired = password_expired;
    }

    public String getRandom() {
        return random;
    }

    public void setRandom(String random) {
        this.random = random;
    }

    public String getTokenRandom() {
        return tokenRandom;
    }

    public void setTokenRandom(String token_random) {
        this.tokenRandom = token_random;
    }

    public Timestamp getDateCreated() {
        return dateCreated;
    }

    public void setDateCreated(Timestamp date_created) {
        this.dateCreated = date_created;
    }

    @Override
    public String toString() {
        return String.format("ExeoUser[id=%d, email=%s]", id, email);
    }
}
