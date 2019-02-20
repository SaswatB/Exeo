package com.hstar.exeo.objects.rest;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.sql.Date;

/**
 * Created by Saswat on 8/1/2015.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserSummary {

    private String id;
    private String firstname;
    private String lastname;
    private Date birthday;
    private String email;

    public UserSummary() {
    }

    public UserSummary(String id, String firstname, String lastname, Date birthday, String email) {
        this.id = id;
        this.firstname = firstname;
        this.lastname = lastname;
        this.birthday = birthday;
        this.email = email;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
