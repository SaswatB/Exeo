package com.hstar.exeo.objects.rest;

/**
 * Created by Saswat on 8/9/2015.
 */
public class OAuthLoginRequest {

    private String provider;
    private String token;

    public OAuthLoginRequest() {}

    public OAuthLoginRequest(String provider, String token) {
        this.provider = provider;
        this.token = token;
    }

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
