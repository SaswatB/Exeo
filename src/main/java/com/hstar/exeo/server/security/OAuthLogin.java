package com.hstar.exeo.server.security;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.hstar.exeo.objects.db.OAuth;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import org.json.JSONObject;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Arrays;

/**
 *
 * Created by Saswat on 8/5/2015.
 */
public class OAuthLogin {


    private static GoogleIdTokenVerifier gverifier;

    public static OAuthUser facebook_oauth(String token) throws UnirestException, GeneralSecurityException {
        HttpResponse<JsonNode> jsonNodeHttpResponse = Unirest.get("https://graph.facebook.com/me?fields=id&access_token=" + token).asJson();

        if(jsonNodeHttpResponse.getStatus() != 200) {
            throw new GeneralSecurityException("Invalid token.");
        }
        JSONObject json = jsonNodeHttpResponse.getBody().getObject();
        if(!json.has("id")) {
            throw new GeneralSecurityException("Invalid token.");
        }

        return new OAuthUser(OAuth.PROVIDER_FACEBOOK, json.getString("id"), json.optString("first_name", null), json.optString("last_name", null));
    }

    public static OAuthUser google_oauth(String token) throws GeneralSecurityException, IOException {
        if(gverifier == null) {
            gverifier = new GoogleIdTokenVerifier.Builder(new NetHttpTransport(), new JacksonFactory())
                    .setAudience(Arrays.asList("608167015536-aspufrt6egm2d7lnvo6mds3lcl1m41p7.apps.googleusercontent.com"))
                    .build();
        }

        String user_id;
        String firstname;
        String lastname;

        GoogleIdToken idToken = gverifier.verify(token);
        if (idToken != null) {
            GoogleIdToken.Payload payload = idToken.getPayload();
            // If multiple clients access the backend server:
            //todo add android and ios client ids
            if (Arrays.asList("608167015536-aspufrt6egm2d7lnvo6mds3lcl1m41p7.apps.googleusercontent.com", "ANDROID_CLIENT_ID", "IOS_CLIENT_ID").contains(payload.getAuthorizedParty())) {
                user_id = payload.getSubject();
                firstname = (String)payload.get("given_name");
                lastname = (String)payload.get("family_name");
            } else {
                throw new GeneralSecurityException("Invalid token.");
            }
        } else {
            throw new GeneralSecurityException("Invalid token.");
        }

        return new OAuthUser(OAuth.PROVIDER_GOOGLE, user_id, firstname, lastname);
    }

    public static OAuthUser microsoft_oauth(String token) throws UnirestException, GeneralSecurityException {
        HttpResponse<JsonNode> jsonNodeHttpResponse = Unirest.get("https://apis.live.net/v5.0/me?access_token=" + token).asJson();

        if(jsonNodeHttpResponse.getStatus() != 200) {
            throw new GeneralSecurityException("Invalid token.");
        }
        JSONObject json = jsonNodeHttpResponse.getBody().getObject();
        if(!json.has("id")) {
            throw new GeneralSecurityException("Invalid token.");
        }

        return new OAuthUser(OAuth.PROVIDER_MICROSOFT, json.getString("id"), json.optString("first_name", null), json.optString("last_name", null));
    }

    public static class OAuthUser {
        public final String provider;
        public final String provider_user_id;
        public final String firstname;
        public final String lastname;

        public OAuthUser(String provider, String provider_user_id, String firstname, String lastname) {
            this.provider = provider;
            this.provider_user_id = provider_user_id;
            this.firstname = firstname;
            this.lastname = lastname;
        }

        @Override
        public String toString() {
            return "OAuthUser{" +
                    "provider='" + provider + '\'' +
                    ", provider_user_id='" + provider_user_id + '\'' +
                    ", firstname='" + firstname + '\'' +
                    ", lastname='" + lastname + '\'' +
                    '}';
        }
    }
}
