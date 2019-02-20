package com.hstar.exeo.objects.rest;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 *
 * Created by Saswat on 8/9/2015.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RegisterResult {

    public static final int REQUIRED_PARAMETERS_MISSING_ERROR = 101;
    public static final int INVALID_USERNAME_ERROR = 103;
    public static final int USERNAME_TAKEN_ERROR = 110;
    public static final int UNKNOWN_OAUTH_PROVIDER_ERROR = 201;
    public static final int INVALID_OAUTH_LOGIN = 202;
    public static final int OAUTH_USER_TAKEN_ERROR = 210;
    public static final int UNKNOWN_EXCEPTION_ERROR = 1000;

    private boolean register;
    private String token;
    private Integer errorCode;
    private String error;

    public RegisterResult() {
    }

    public RegisterResult(String token) {
        this.register = true;
        this.token = token;
    }

    public RegisterResult(Integer errorCode, String error) {
        this.register = false;
        this.errorCode = errorCode;
        this.error = error;
    }

    public boolean isRegister() {
        return register;
    }

    public void setRegister(boolean register) {
        this.register = register;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public Integer getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(Integer errorCode) {
        this.errorCode = errorCode;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

}
