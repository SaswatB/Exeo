package com.hstar.exeo.server.security.jwt;

import com.hstar.exeo.objects.db.ExeoUser;
import com.nimbusds.jwt.JWT;
import com.nimbusds.jwt.ReadOnlyJWTClaimsSet;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.text.ParseException;
import java.util.Collection;
import java.util.Collections;

/**
 * Spring authentication wrapper around jwt
 * Created by Saswat on 7/29/2015.
 */
public class JWTToken implements Authentication {

    public static final String JWT_PTAG_CLAIM = "ptag";      //password change tag claim
    public static final String JWT_XSRF_CLAIM = "xsrfToken"; //cross site scripting claim

    private JWT jwt;
    private final Collection<GrantedAuthority> authorities;
    private boolean authenticated;
    private ReadOnlyJWTClaimsSet claims;
    private ExeoUser user;

    public JWTToken(JWT jwt) throws ParseException {
        this.jwt = jwt;
        this.authorities = Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"));
        this.claims = jwt.getJWTClaimsSet();
        authenticated = false;
    }

    public JWT getJwt() {
        return jwt;
    }

    public ReadOnlyJWTClaimsSet getClaims() {
        return claims;
    }

    @Override
    public Object getCredentials() {
        try {
            return claims.getStringClaim(JWT_PTAG_CLAIM);
        } catch (ParseException e) {
            return "";
        }
    }

    @Override
    public Object getPrincipal() {
        return claims.getSubject();
    }

    @Override
    public String getName() {
        return claims.getSubject();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public Object getDetails() {
        return claims.toJSONObject();
    }

    public ExeoUser getUser() {
        return user;
    }

    public void setUser(ExeoUser user) {
        this.user = user;
    }

    @Override
    public boolean isAuthenticated() {
        return authenticated;
    }

    @Override
    public void setAuthenticated(boolean isAuthenticated) throws IllegalArgumentException {
        this.authenticated = isAuthenticated;
    }

}
