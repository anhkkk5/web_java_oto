package com.carsales.service;

public interface EmailService {
    void sendOtpRegister(String email, String otpCode);

    void sendOtpForgotPassword(String email, String otpCode);
}
