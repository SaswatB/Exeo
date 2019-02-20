package com.hstar.exeo.server.security.jwt;

import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

/**
 * Retrieves the key to sign jwt token with
 * May create a new key if none exist
 * Created by Saswat on 7/30/2015.
 */
@Service("jwtSignKeyService")
public class JWTSigningKey {

    @Autowired private RedisTemplate<String, String> redis;

    public String get() {
        redis.boundValueOps("jwt-signer").setIfAbsent(RandomStringUtils.randomAlphanumeric(64));
        return redis.boundValueOps("jwt-signer").get();
    }

}
