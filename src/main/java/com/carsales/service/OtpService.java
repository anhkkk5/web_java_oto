package com.carsales.service;

import com.carsales.enums.OtpType;

public interface OtpService {
    String generateOtp();

    void checkResendLimit(String email);

    void saveOtpToRedis(String email, String otp, OtpType type);

    void verifyOtp(String email, String otp, OtpType type);

    void deleteOtp(String email, OtpType type);
}
