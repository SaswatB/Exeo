package com.hstar.exeo.server.security;

import org.apache.commons.codec.binary.Base64;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;

/**
 * Password management utilities
 * Created by Saswat on 7/31/2015.
 */
public class PasswordUtils {

    private static PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public static String generatePasswordHash(String password) {
        return passwordEncoder.encode(password);
    }

    public static boolean validatePassword(String originalPassword, String storedPassword) {
        return passwordEncoder.matches(originalPassword, storedPassword);
    }

    public static String generatePTag(String password) {
        if(password.length() > 20) {
            password = password.substring(0, 20);
        }
        byte input[] = password.getBytes();

        MessageDigest digest;
        try {
            digest = MessageDigest.getInstance("SHA-512");
        } catch (NoSuchAlgorithmException e) {
            throw new NullPointerException("SHA-512 Missing");
        }
        for(int i = 0; i < 100; i++) {
            input = digest.digest(input);
        }

        return Base64.encodeBase64String(input).substring(0, 5);
    }
}
