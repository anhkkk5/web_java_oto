package com.carsales.service.impl;

import com.carsales.service.EmailService;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;

    public EmailServiceImpl(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @Override
    public void sendOtpRegister(String email, String otpCode) {
        sendOtp(email, "Your registration OTP", otpCode);
    }

    @Override
    public void sendOtpForgotPassword(String email, String otpCode) {
        sendOtp(email, "Your forgot password OTP", otpCode);
    }

    private void sendOtp(String email, String subject, String otpCode) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        message.setSubject(subject);
        message.setText("Your OTP code is: " + otpCode + "\nThis code will expire in 5 minutes.");
        mailSender.send(message);
    }
}
