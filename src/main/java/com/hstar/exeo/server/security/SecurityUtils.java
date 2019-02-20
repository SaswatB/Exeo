package com.hstar.exeo.server.security;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.lang3.RandomStringUtils;

import java.security.SecureRandom;
import java.util.Base64;

/**
 * Security Utils
 * Created by Saswat on 9/6/2015.
 */
public class SecurityUtils {
    public static final String EASY_CHARACTERS = "123456789ABCDEFGHIJKLMNPQRSTUVWXYZ";//0, O removed for readability
    private static SecureRandom rand = new SecureRandom();

    public static String randHex(int len) {
        byte r[] = new byte[len/2+1];
        rand.nextBytes(r);

        return Hex.encodeHexString(r).substring(0, len);
    }

    public static String randBase64(int len) {
        byte r[] = new byte[len];
        rand.nextBytes(r);

        return Base64.getEncoder().encodeToString(r).substring(0, len);
    }

    public static String randomEasyUppercaseString(int len){
        char c[] = new char[len];
        for(int i = 0; i < len; i++){
            c[i] = EASY_CHARACTERS.charAt(rand.nextInt(EASY_CHARACTERS.length()));
        }
        return new String(c);
    }
}
