package com.hstar.exeo.objects.db;

import com.hstar.exeo.server.security.SecurityUtils;

import javax.persistence.*;

/**
 * Representation of a device in the rdbms
 * Created by Saswat on 1/26/2016.
 */
@Entity
@Table(name = "devices")
public class Device {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "device_id")
    private long id;

    @Column(name = "device_public_id")
    private String publicId;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private ExeoUser user;

    @Column(name = "device_name")
    private String name;

    @Column(name = "device_token")
    private String token;

    protected Device() {}

    public Device(ExeoUser user, String name) {
        this.publicId = SecurityUtils.randBase64(20);
        this.user = user;
        this.name = name;
        this.token = SecurityUtils.randBase64(128);
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getPublicId() {
        return publicId;
    }

    public void setPublicId(String publicId) {
        this.publicId = publicId;
    }

    public ExeoUser getUser() {
        return user;
    }

    public void setUser(ExeoUser user) {
        this.user = user;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
