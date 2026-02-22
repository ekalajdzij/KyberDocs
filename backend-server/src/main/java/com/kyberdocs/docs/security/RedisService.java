package com.kyberdocs.docs.security;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
public class RedisService {

    private final StringRedisTemplate redisTemplate;

    private static final int MAX_FAILED_ATTEMPTS = 10;
    private static final long LOCK_TIME_DURATION_MINUTES = 15;

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

    public boolean isLocked(String identifier) {
        return Boolean.TRUE.equals(redisTemplate.hasKey("locked:" + identifier));
    }

    public void recordFailedAttempt(String identifier) {
        String attemptKey = "attempts:" + identifier;
        String lockedKey = "locked:" + identifier;

        Long attempts = redisTemplate.opsForValue().increment(attemptKey);

        // Start the 15-minute countdown for the attempts counter on the first strike
        if (attempts != null && attempts == 1) {
            redisTemplate.expire(attemptKey, Duration.ofMinutes(LOCK_TIME_DURATION_MINUTES));
        }

        // Lock the account/IP if max attempts are reached
        if (attempts != null && attempts >= MAX_FAILED_ATTEMPTS) {
            redisTemplate.opsForValue().set(lockedKey, "true", Duration.ofMinutes(LOCK_TIME_DURATION_MINUTES));
            redisTemplate.delete(attemptKey); // Clear the attempts tracker once locked
        }
    }

    public void clearAttempts(String identifier) {
        redisTemplate.delete("attempts:" + identifier);
    }
}