package com.hstar.exeo.server.security.jwt;

import com.hstar.exeo.objects.db.ExeoUser;
import com.hstar.exeo.server.repos.ExeoUserRepository;
import com.hstar.exeo.server.security.PasswordUtils;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSVerifier;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.*;
import org.eclipse.jetty.websocket.common.events.annotated.InvalidSignatureException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;

/**
 * Spring Authentication Provider that consumes a JWT authorization token
 * In either the http Authorization header denoted by Bearer, or an http-only
 * cookie
 * Created by Saswat on 7/29/2015.
 */
@Service("jwtAuthProviderService")
public class JWTAuthenticationProvider implements AuthenticationProvider {

    @Autowired private JWTSigningKey jwtSigningKey;
    @Autowired private ExeoUserRepository userRepository;

    @Override
    public JWTToken authenticate(Authentication authentication) throws AuthenticationException {
        JWSVerifier verifier = new MACVerifier(jwtSigningKey.get());
        JWTToken jwtToken = (JWTToken) authentication;
        JWT jwt = jwtToken.getJwt();

        // Check type of the parsed JOSE object
        if (jwt instanceof PlainJWT) {
            throw new InvalidTokenException("Unsecured plain tokens are not supported");
        } else if (jwt instanceof SignedJWT) {
            handleSignedToken((SignedJWT) jwt, verifier);
        } else if (jwt instanceof EncryptedJWT) {
            throw new UnsupportedOperationException("Unsupported token type");
        }

        Date referenceTime = new Date();
        ReadOnlyJWTClaimsSet claims = jwtToken.getClaims();

        Date expirationTime = claims.getExpirationTime();
        if (expirationTime == null || expirationTime.before(referenceTime)) {
            throw new InvalidTokenException("The token is expired");
        }

        Date notBeforeTime = claims.getNotBeforeTime();
        if (notBeforeTime == null || notBeforeTime.after(referenceTime)) {
            throw new InvalidTokenException("Not before is after sysdate");
        }

        String issuerReference = "https://localhost:8443";
        String issuer = claims.getIssuer();
        if (!issuerReference.equals(issuer)) {
            throw new InvalidTokenException("Invalid issuer");
        }

        ExeoUser u = userRepository.findByUuidIgnoreCase(claims.getSubject());
        if (u == null) {
            throw new InvalidTokenException("Unknown user");
        }

        try {
            if(!PasswordUtils.generatePTag(u.getPassword()).equals(claims.getStringClaim(JWTToken.JWT_PTAG_CLAIM))) {
                throw new InvalidTokenException("PTag mismatch");
            }
        } catch (ParseException e) {
            throw new InvalidTokenException("Unable to parse ptag", e);
        }

        jwtToken.setUser(u);
        jwtToken.setAuthenticated(true);
        return jwtToken;
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return JWTToken.class.isAssignableFrom(authentication);
    }

    private void handleSignedToken(SignedJWT jwt, JWSVerifier verifier) {
        try {
            if (!jwt.verify(verifier)) {
                throw new InvalidSignatureException("Signature validation failed");
            }
        } catch (JOSEException e) {
            throw new InvalidSignatureException("Signature validation failed");
        }
    }

    public static String createJWTToken(ExeoUser user, JWTSigningKey jwtSigningKey) throws JOSEException {
        return createJWTToken(user.getUuid(), PasswordUtils.generatePTag(user.getPassword()), jwtSigningKey.get());
    }

    public static String createJWTToken(String name, String ptag, String sign) throws JOSEException {
        return createJWTToken(name, ptag, sign, null);
    }

    public static String createJWTToken(String user_uuid, String ptag, String sign, String xsrfToken) throws JOSEException {
        Calendar c = Calendar.getInstance();

        JWTClaimsSet claimsSet = new JWTClaimsSet();
        claimsSet.setSubject(user_uuid);
        claimsSet.setCustomClaim(JWTToken.JWT_PTAG_CLAIM, ptag);
        claimsSet.setIssueTime(c.getTime());
        claimsSet.setIssuer("https://localhost:8443");//TODO change
        claimsSet.setNotBeforeTime(c.getTime());
        c.add(Calendar.DATE, 21);
        claimsSet.setExpirationTime(c.getTime());
        if (xsrfToken != null) {
            claimsSet.setCustomClaim(JWTToken.JWT_XSRF_CLAIM, xsrfToken);
        }

        // Create an HMAC-protected JWS object with the user's identity
        SignedJWT jwsObject = new SignedJWT(new JWSHeader(JWSAlgorithm.HS256), claimsSet);

        // Apply the HMAC to the JWS object
        jwsObject.sign(new MACSigner(sign));
        return jwsObject.serialize();
    }

}
