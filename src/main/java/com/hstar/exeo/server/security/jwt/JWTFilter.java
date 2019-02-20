package com.hstar.exeo.server.security.jwt;

import com.nimbusds.jwt.JWT;
import com.nimbusds.jwt.JWTParser;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.GenericFilterBean;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.NotNull;
import java.io.IOException;
import java.text.ParseException;

/**
 * Filter bean that authenticates JWT requests
 * Created by Saswat on 7/30/2015.
 */
public class JWTFilter extends GenericFilterBean {

    private JWTAuthenticationProvider jwtAuthenticationProvider;

    public JWTFilter(JWTAuthenticationProvider jwtAuthenticationProvider) {
        this.jwtAuthenticationProvider = jwtAuthenticationProvider;
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;

        //skip static resource authentication
        String path = req.getServletPath();
        if(path.startsWith("/css/") || path.startsWith("/font/") || path.startsWith("/js/") || path.startsWith("/lib/") || path.startsWith("/images/")) {
            chain.doFilter(request, response);
            return;
        }

        try {
            //retrieve the jwt token from the Authorization header
            String stringToken = req.getHeader("Authorization");
            String authorizationSchema = "Bearer";
            if (stringToken == null || !stringToken.contains(authorizationSchema)) {
                throw new InsufficientAuthenticationException("Authorization header not found");
            }

            // remove schema from token
            stringToken = stringToken.substring(authorizationSchema.length()).trim();

            //authenticate the token
            try {
                SecurityContextHolder.getContext().setAuthentication(authenticateJWT(jwtAuthenticationProvider, stringToken));
                System.out.println("Authenticated " + req.getRequestURL().toString());
            } catch (ParseException e) {
                throw new InvalidTokenException("Invalid token", e);
            }
        } catch (AuthenticationException | InvalidTokenException e) {
            //e.printStackTrace();//todo remove
            SecurityContextHolder.clearContext();
        } catch (Exception e) {
            e.printStackTrace();
            SecurityContextHolder.clearContext();
        }

        //continue with whatever was going to happen anyways
        chain.doFilter(request, response);
    }

    public static JWTToken authenticateJWT(JWTAuthenticationProvider authenticationManager, String jwtToken) throws ParseException {
        return authenticationManager.authenticate(new JWTToken(JWTParser.parse(jwtToken)));
    }

}
