package com.hstar.exeo.objects.rest;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 *
 * Created by Saswat on 7/29/2015.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class LoginResult {

    private boolean login;
    private String token;

    public LoginResult() {
    }

    public LoginResult(boolean login, String token) {
        this.login = login;
        this.token = token;
    }

    public boolean isLogin() {
        return login;
    }

    public String getToken() {
        return token;
    }

    public void setLogin(boolean login) {
        this.login = login;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
