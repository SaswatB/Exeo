package com.hstar.exeo.server.security.jwt;

/**
 * Created by Saswat on 8/12/2015.
 */
public class InvalidTokenException extends RuntimeException {
    public InvalidTokenException(String s) {
        super(s);
    }

    public InvalidTokenException(String s, Throwable c) {
        super(s, c);
    }

}
