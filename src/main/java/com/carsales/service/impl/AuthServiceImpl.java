package com.carsales.service.impl;

import com.carsales.dto.request.ChangePasswordRequest;
import com.carsales.dto.request.ForgotPasswordRequest;
import com.carsales.dto.request.LoginRequest;
import com.carsales.dto.request.RefreshTokenRequest;
import com.carsales.dto.request.RegisterRequest;
import com.carsales.dto.request.ResetPasswordRequest;
import com.carsales.dto.request.SendOtpRequest;
import com.carsales.dto.request.UpdateProfileRequest;
import com.carsales.dto.request.VerifyForgotPasswordOtpRequest;
import com.carsales.dto.request.VerifyOtpRequest;
import com.carsales.dto.response.AuthResponse;
import com.carsales.dto.response.UserResponse;
import com.carsales.enums.OtpType;
import com.carsales.entity.Role;
import com.carsales.entity.User;
import com.carsales.exception.BadRequestException;
import com.carsales.repository.RoleRepository;
import com.carsales.repository.UserRepository;
import com.carsales.security.CustomUserDetails;
import com.carsales.security.JwtTokenProvider;
import com.carsales.service.AuthService;
import com.carsales.service.EmailService;
import com.carsales.service.OtpService;
import com.carsales.util.AppConstants;
import com.carsales.util.RedisKeys;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.Date;
import java.util.UUID;

@Service
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;
    private final AuthenticationManager authenticationManager;
    private final OtpService otpService;
    private final EmailService emailService;
    private final StringRedisTemplate stringRedisTemplate;

    public AuthServiceImpl(UserRepository userRepository, RoleRepository roleRepository,
                          PasswordEncoder passwordEncoder, JwtTokenProvider tokenProvider,
                          AuthenticationManager authenticationManager, OtpService otpService,
                          EmailService emailService, StringRedisTemplate stringRedisTemplate) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenProvider = tokenProvider;
        this.authenticationManager = authenticationManager;
        this.otpService = otpService;
        this.emailService = emailService;
        this.stringRedisTemplate = stringRedisTemplate;
    }

    @Override
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("Email already exists");
        }

        if (request.getPhone() != null && !request.getPhone().isBlank() && userRepository.existsByPhone(request.getPhone())) {
            throw new BadRequestException("Phone already exists");
        }

        User user = new User();
        user.setUsername(request.getEmail());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setFullName(request.getFullName());
        user.setPhone(request.getPhone());
        user.setAddress(request.getAddress());
        user.setIsActive(false);
        user.setIsVerified(false);

        Role userRole = roleRepository.findByName(AppConstants.ROLE_CUSTOMER)
                .orElseThrow(() -> new BadRequestException("User Role not found"));
        user.setRole(userRole);

        User savedUser = userRepository.save(user);

        // User is not verified yet; token will be issued after OTP verification flow
        String token = null;
        UserResponse userResponse = mapToUserResponse(savedUser);

        return new AuthResponse(token, null, userResponse);
    }

    @Override
    public void sendOtp(SendOtpRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new BadRequestException("User not found"));

        if (Boolean.TRUE.equals(user.getIsVerified())) {
            throw new BadRequestException("Account already verified");
        }

        otpService.checkResendLimit(request.getEmail());
        String otp = otpService.generateOtp();
        otpService.saveOtpToRedis(request.getEmail(), otp, OtpType.REGISTER);
        emailService.sendOtpRegister(request.getEmail(), otp);
    }

    @Override
    public void resendOtp(SendOtpRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new BadRequestException("User not found"));

        if (Boolean.TRUE.equals(user.getIsVerified())) {
            throw new BadRequestException("Account already verified");
        }

        otpService.checkResendLimit(request.getEmail());
        otpService.deleteOtp(request.getEmail(), OtpType.REGISTER);
        String otp = otpService.generateOtp();
        otpService.saveOtpToRedis(request.getEmail(), otp, OtpType.REGISTER);
        emailService.sendOtpRegister(request.getEmail(), otp);
    }

    @Override
    @Transactional
    public AuthResponse verifyOtp(VerifyOtpRequest request) {
        otpService.verifyOtp(request.getEmail(), request.getOtpCode(), OtpType.REGISTER);

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new BadRequestException("User not found"));

        user.setIsVerified(true);
        user.setIsActive(true);
        User saved = userRepository.save(user);

        otpService.deleteOtp(request.getEmail(), OtpType.REGISTER);

        CustomUserDetails cud = CustomUserDetails.create(saved);
        String accessToken = tokenProvider.generateAccessToken(cud);
        String refreshToken = tokenProvider.generateRefreshToken(cud);

        stringRedisTemplate.opsForValue().set(RedisKeys.refreshTokenKey(saved.getId()), refreshToken, Duration.ofDays(7));

        return new AuthResponse(accessToken, refreshToken, mapToUserResponse(saved));
    }

    @Override
    public AuthResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String accessToken = tokenProvider.generateToken(authentication);

        CustomUserDetails cud = (CustomUserDetails) authentication.getPrincipal();
        String refreshToken = tokenProvider.generateRefreshToken(cud);
        stringRedisTemplate.opsForValue().set(RedisKeys.refreshTokenKey(cud.getId()), refreshToken, Duration.ofDays(7));

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new BadRequestException("User not found"));

        UserResponse userResponse = mapToUserResponse(user);

        return new AuthResponse(accessToken, refreshToken, userResponse);
    }

    @Override
    public AuthResponse refreshToken(RefreshTokenRequest request) {
        String refreshToken = request.getRefreshToken();
        if (!tokenProvider.validateToken(refreshToken)) {
            throw new BadRequestException("Invalid refresh token");
        }

        String email = tokenProvider.getEmailFromToken(refreshToken);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BadRequestException("User not found"));

        String stored = stringRedisTemplate.opsForValue().get(RedisKeys.refreshTokenKey(user.getId()));
        if (stored == null || !stored.equals(refreshToken)) {
            throw new BadRequestException("Refresh token not found");
        }

        CustomUserDetails cud = CustomUserDetails.create(user);
        String newAccessToken = tokenProvider.generateAccessToken(cud);
        return new AuthResponse(newAccessToken, refreshToken, mapToUserResponse(user));
    }

    @Override
    public void forgotPassword(ForgotPasswordRequest request) {
        userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new BadRequestException("User not found"));

        otpService.checkResendLimit(request.getEmail());
        String otp = otpService.generateOtp();
        otpService.saveOtpToRedis(request.getEmail(), otp, OtpType.FORGOT_PASSWORD);
        emailService.sendOtpForgotPassword(request.getEmail(), otp);
    }

    @Override
    public String verifyForgotPasswordOtp(VerifyForgotPasswordOtpRequest request) {
        otpService.verifyOtp(request.getEmail(), request.getOtpCode(), OtpType.FORGOT_PASSWORD);

        String resetToken = UUID.randomUUID().toString();
        stringRedisTemplate.opsForValue().set(RedisKeys.resetTokenKey(resetToken), request.getEmail(), Duration.ofMinutes(10));

        otpService.deleteOtp(request.getEmail(), OtpType.FORGOT_PASSWORD);
        return resetToken;
    }

    @Override
    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new BadRequestException("Password confirmation does not match");
        }

        String email = stringRedisTemplate.opsForValue().get(RedisKeys.resetTokenKey(request.getResetToken()));
        if (email == null) {
            throw new BadRequestException("Reset token expired");
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BadRequestException("User not found"));

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        stringRedisTemplate.delete(RedisKeys.resetTokenKey(request.getResetToken()));

        stringRedisTemplate.opsForValue().set(RedisKeys.logoutAllKey(user.getId()), Long.toString(System.currentTimeMillis()), Duration.ofDays(7));
        stringRedisTemplate.delete(RedisKeys.refreshTokenKey(user.getId()));
    }

    @Override
    public void logout(String accessToken) {
        if (accessToken == null || accessToken.isBlank() || !tokenProvider.validateToken(accessToken)) {
            return;
        }

        String jti = tokenProvider.getJtiFromToken(accessToken);
        Date exp = tokenProvider.getExpirationFromToken(accessToken);
        long ttlMs = exp.getTime() - System.currentTimeMillis();
        if (ttlMs > 0) {
            stringRedisTemplate.opsForValue().set(RedisKeys.tokenBlacklistKey(jti), "1", Duration.ofMillis(ttlMs));
        }

        String email = tokenProvider.getEmailFromToken(accessToken);
        userRepository.findByEmail(email).ifPresent(user -> stringRedisTemplate.delete(RedisKeys.refreshTokenKey(user.getId())));
    }

    @Override
    public void logoutAll() {
        User user = currentUser();
        stringRedisTemplate.opsForValue().set(RedisKeys.logoutAllKey(user.getId()), Long.toString(System.currentTimeMillis()), Duration.ofDays(7));
        stringRedisTemplate.delete(RedisKeys.refreshTokenKey(user.getId()));
    }

    @Override
    public UserResponse me() {
        return mapToUserResponse(currentUser());
    }

    @Override
    @Transactional
    public UserResponse updateProfile(UpdateProfileRequest request) {
        User user = currentUser();

        if (request.getPhone() != null && !request.getPhone().isBlank() && !request.getPhone().equals(user.getPhone())) {
            if (userRepository.existsByPhone(request.getPhone())) {
                throw new BadRequestException("Phone already exists");
            }
            user.setPhone(request.getPhone());
        }

        if (request.getFullName() != null) {
            user.setFullName(request.getFullName());
        }

        if (request.getAvatar() != null) {
            user.setAvatar(request.getAvatar());
        }

        User saved = userRepository.save(user);
        return mapToUserResponse(saved);
    }

    @Override
    @Transactional
    public void changePassword(ChangePasswordRequest request) {
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new BadRequestException("Password confirmation does not match");
        }

        User user = currentUser();
        if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
            throw new BadRequestException("Old password is incorrect");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        stringRedisTemplate.opsForValue().set(RedisKeys.logoutAllKey(user.getId()), Long.toString(System.currentTimeMillis()), Duration.ofDays(7));
        stringRedisTemplate.delete(RedisKeys.refreshTokenKey(user.getId()));
    }

    private UserResponse mapToUserResponse(User user) {
        UserResponse response = new UserResponse();
        response.setId(user.getId());
        response.setUsername(user.getUsername());
        response.setEmail(user.getEmail());
        response.setFullName(user.getFullName());
        response.setPhone(user.getPhone());
        response.setAddress(user.getAddress());
        response.setAvatar(user.getAvatar());
        response.setRole(user.getRole().getName().name());
        response.setIsActive(user.getIsActive());
        if (user.getCreatedAt() != null) {
            response.setCreatedAt(user.getCreatedAt().toString());
        }
        return response;
    }

    private User currentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof CustomUserDetails cud)) {
            throw new BadRequestException("Unauthorized");
        }

        return userRepository.findById(cud.getId())
                .orElseThrow(() -> new BadRequestException("User not found"));
    }
}
