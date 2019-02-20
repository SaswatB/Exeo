package com.hstar.exeo.objects.db;

import javax.persistence.*;
import java.sql.Date;

/**
 * Representation of a profile in the rdbms
 * Created by Saswat on 8/10/2015.
 */
@Entity
@Table(name = "profile")
public class Profile {

    @Id
    @Column(name = "user_id")
    private long user_id;

    @OneToOne
    @PrimaryKeyJoinColumn(name = "user_id")
    private ExeoUser user;

    @Column(name = "profile_firstname")
    private String firstname;

    @Column(name = "profile_lastname")
    private String lastname;

    @Column(name = "profile_birthday")
    private Date birthday;

    protected Profile() {}

    public Profile(ExeoUser user, String firstname, String lastname, Date birthday) {
        this.user_id = user.getId();
        this.user = user;
        this.firstname = firstname;
        this.lastname = lastname;
        this.birthday = birthday;
    }

    public long getUser_id() {
        return user_id;
    }

    public void setUser_id(long user_id) {
        this.user_id = user_id;
    }

    public ExeoUser getUser() {
        return user;
    }

    public void setUser(ExeoUser user) {
        this.user = user;
    }

    public String getFirstname() {
        return firstname;
    }

    public void setFirstname(String firstname) {
        this.firstname = firstname;
    }

    public String getLastname() {
        return lastname;
    }

    public void setLastname(String lastname) {
        this.lastname = lastname;
    }

    public Date getBirthday() {
        return birthday;
    }

    public void setBirthday(Date birthday) {
        this.birthday = birthday;
    }
}
