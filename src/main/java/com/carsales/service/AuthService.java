package com.carsales.service;

import com.carsales.dto.request.LoginRequest;
import com.carsales.dto.request.RegisterRequest;
import com.carsales.dto.response.AuthResponse;

public interface AuthService {
    AuthResponse register(RegisterRequest request);
    AuthResponse login(LoginRequest request);
}
