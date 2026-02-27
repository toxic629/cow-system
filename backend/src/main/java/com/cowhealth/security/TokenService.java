package com.cowhealth.security;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.util.UUID;

@Service
public class TokenService {

    public static final long EXPIRE_SECONDS = 12 * 3600;
    private static final String TOKEN_PREFIX = "token:";

    private final StringRedisTemplate redisTemplate;

    public TokenService(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public String issueToken(String username) {
        String token = UUID.randomUUID().toString().replace("-", "");
        redisTemplate.opsForValue().set(TOKEN_PREFIX + token, username, Duration.ofSeconds(EXPIRE_SECONDS));
        return token;
    }

    public boolean validToken(String token) {
        if (!StringUtils.hasText(token)) {
            return false;
        }
        return Boolean.TRUE.equals(redisTemplate.hasKey(TOKEN_PREFIX + token));
    }

    public String getUsername(String token) {
        return redisTemplate.opsForValue().get(TOKEN_PREFIX + token);
    }
}
