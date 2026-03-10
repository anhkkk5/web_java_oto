package com.carsales.service;

import com.carsales.dto.request.LoginRequest;
import com.carsales.dto.request.RegisterRequest;
import com.carsales.dto.request.SendOtpRequest;
import com.carsales.dto.request.VerifyOtpRequest;
import com.carsales.dto.request.RefreshTokenRequest;
import com.carsales.dto.request.ForgotPasswordRequest;
import com.carsales.dto.request.VerifyForgotPasswordOtpRequest;
import com.carsales.dto.request.ResetPasswordRequest;
import com.carsales.dto.request.UpdateProfileRequest;
import com.carsales.dto.request.ChangePasswordRequest;
import com.carsales.dto.response.AuthResponse;
import com.carsales.dto.response.UserResponse;

public interface AuthService {
    AuthResponse register(RegisterRequest request);
    void sendOtp(SendOtpRequest request);
    void resendOtp(SendOtpRequest request);
    AuthResponse verifyOtp(VerifyOtpRequest request);
    AuthResponse login(LoginRequest request);
    AuthResponse refreshToken(RefreshTokenRequest request);
    void forgotPassword(ForgotPasswordRequest request);
    String verifyForgotPasswordOtp(VerifyForgotPasswordOtpRequest request);
    void resetPassword(ResetPasswordRequest request);
    void logout(String accessToken);
    void logoutAll();
    UserResponse me();
    UserResponse updateProfile(UpdateProfileRequest request);
    void changePassword(ChangePasswordRequest request);
}
