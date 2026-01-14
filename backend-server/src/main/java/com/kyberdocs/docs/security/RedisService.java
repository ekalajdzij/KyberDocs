package com.kyberdocs.docs.security;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
public class RedisService {

    private final StringRedisTemplate redisTemplate;

    public RedisService(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public void blacklistToken(String token, long durationInMillis) {
        // Key: token, Value: "blacklisted", Timeout: automatic deletion
        redisTemplate.opsForValue().set(token, "blacklisted", Duration.ofMillis(durationInMillis));
    }

    public boolean isBlacklisted(String token) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(token));
    }
}