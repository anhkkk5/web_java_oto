package com.carsales.util;

import com.carsales.enums.OtpType;

public class RedisKeys {

    public static String otpKey(OtpType type, String email) {
        return switch (type) {
            case REGISTER -> "otp:register:" + email;
            case FORGOT_PASSWORD -> "otp:forgot:" + email;
        };
    }

    public static String otpCountKey(String email) {
        return "otp:count:" + email;
    }

    public static String tokenBlacklistKey(String jti) {
        return "blacklist:token:" + jti;
    }

    public static String refreshTokenKey(Long userId) {
        return "refresh:token:" + userId;
    }

    public static String logoutAllKey(Long userId) {
        return "logout:all:" + userId;
    }

    public static String resetTokenKey(String resetToken) {
        return "reset:token:" + resetToken;
    }

    private RedisKeys() {
        throw new IllegalStateException("Utility class");
    }
}
