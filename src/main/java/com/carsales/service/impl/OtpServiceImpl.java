package com.carsales.service.impl;

import com.carsales.enums.OtpType;
import com.carsales.exception.BadRequestException;
import com.carsales.service.OtpService;
import com.carsales.util.RedisKeys;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Duration;

@Service
public class OtpServiceImpl implements OtpService {

    private static final int OTP_LENGTH = 6;
    private static final Duration OTP_TTL = Duration.ofMinutes(5);
    private static final Duration RESEND_WINDOW_TTL = Duration.ofHours(1);
    private static final int MAX_RESEND_PER_WINDOW = 5;

    private final StringRedisTemplate stringRedisTemplate;

    public OtpServiceImpl(StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
    }

    @Override
    public String generateOtp() {
        SecureRandom random = new SecureRandom();
        int bound = (int) Math.pow(10, OTP_LENGTH);
        int value = random.nextInt(bound);
        return String.format("%0" + OTP_LENGTH + "d", value);
    }

    @Override
    public void checkResendLimit(String email) {
        String key = RedisKeys.otpCountKey(email);
        String current = stringRedisTemplate.opsForValue().get(key);
        int count = current == null ? 0 : Integer.parseInt(current);
        if (count >= MAX_RESEND_PER_WINDOW) {
            throw new BadRequestException("OTP limit exceeded");
        }

        Long next = stringRedisTemplate.opsForValue().increment(key);
        if (next != null && next == 1L) {
            stringRedisTemplate.expire(key, RESEND_WINDOW_TTL);
        }
    }

    @Override
    public void saveOtpToRedis(String email, String otp, OtpType type) {
        String key = RedisKeys.otpKey(type, email);
        stringRedisTemplate.opsForValue().set(key, otp, OTP_TTL);
    }

    @Override
    public void verifyOtp(String email, String otp, OtpType type) {
        String key = RedisKeys.otpKey(type, email);
        String stored = stringRedisTemplate.opsForValue().get(key);
        if (stored == null) {
            throw new BadRequestException("OTP expired");
        }
        if (!stored.equals(otp)) {
            throw new BadRequestException("OTP invalid");
        }
    }

    @Override
    public void deleteOtp(String email, OtpType type) {
        stringRedisTemplate.delete(RedisKeys.otpKey(type, email));
    }
}
