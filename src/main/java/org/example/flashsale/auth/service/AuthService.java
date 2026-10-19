package org.example.flashsale.auth.service;

import org.example.flashsale.auth.dto.AuthResponse;
import org.example.flashsale.auth.dto.SignInRequest;
import org.example.flashsale.auth.dto.SignUpRequest;
import org.example.flashsale.auth.entity.RefreshToken;
import org.example.flashsale.auth.repository.RefreshTokenRepository;
import org.example.flashsale.auth.security.JwtService;
import org.example.flashsale.common.exception.DuplicateResourceException;
import org.example.flashsale.common.exception.ResourceNotFoundException;
import org.example.flashsale.common.exception.UnauthorizedException;
import org.example.flashsale.user.dto.CreateUserDTO;
import org.example.flashsale.user.dto.UserResponse;
import org.example.flashsale.user.entity.User;
import org.example.flashsale.user.entity.UserRole;
import org.example.flashsale.user.repository.UserRepository;
import org.example.flashsale.user.service.UserService;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HexFormat;

@Service
public class AuthService {
    private final UserService userService;
    private final UserRepository userRepository;

    // Security
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final RefreshTokenRepository refreshTokenRepository;

    public AuthService(
            UserService userService,
            PasswordEncoder passwordEncoder,
            UserRepository userRepository,
            JwtService jwtService,
            RefreshTokenRepository refreshTokenRepository
    ) {
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
        this.userRepository = userRepository;
        this.jwtService = jwtService;
        this.refreshTokenRepository = refreshTokenRepository;
    }

    public AuthResponse signUp(SignUpRequest request) {
        String email = request.getEmail().trim().toLowerCase();

        if(userRepository.existsByEmail(email)){
            throw new DuplicateResourceException("Email already exists");
        }

        CreateUserDTO userDto = CreateUserDTO.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .role(UserRole.CUSTOMER)
                .email(email)
                .phone(request.getPhone())
                .passwordHash(
                        passwordEncoder.encode(request.getPassword())
                )
                .build();

        User savedUser = userService.createUserObj(userDto);

        String accessToken = jwtService.generateAccessToken(savedUser);

        String refreshToken = jwtService.generateRefreshToken(savedUser);

        saveRefreshToken(savedUser, refreshToken);

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .user(toResponse(savedUser))
                .build();
    }

    public AuthResponse signIn(SignInRequest request) {

        String email = request.getEmail()
                .trim()
                .toLowerCase();

        User user = userRepository.findByEmail(email).orElseThrow(
                () -> new ResourceNotFoundException("User not found")
        );

        boolean passwordMatch = passwordEncoder.matches(
                request.getPassword(),
                user.getPasswordHash()
        );

        if(!passwordMatch) {
            throw new BadCredentialsException(
                    "Invalid email or password"
            );
        }

        String accessToken = jwtService.generateAccessToken(user);

        String refreshToken = jwtService.generateRefreshToken(user);

        saveRefreshToken(user, refreshToken);

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .user(toResponse(user))
                .build();
    }

    private void saveRefreshToken(User savedUser, String refreshToken) {
        String hash = hashToken(refreshToken);

        RefreshToken token = RefreshToken.builder()
                .tokenHash(hash)
                .expiresAt(
                        Instant.now().plus(7, ChronoUnit.DAYS)
                )
                .user(savedUser)
                .revoked(false)
                .build();

        refreshTokenRepository.save(token);
    }

    private String hashToken(String refreshToken) {
        try {
            MessageDigest msg = MessageDigest.getInstance("SHA-256");

            byte[] hash = msg.digest(refreshToken.getBytes(StandardCharsets.UTF_8));

            return HexFormat.of().formatHex(hash);

        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(
                    "SHA-256 algorithm not available",
                    e
            );
        }
    }

    public AuthResponse refresh(String rawRefreshToken) {
        if(rawRefreshToken == null) {
            throw new UnauthorizedException("Refresh token missing");
        }

        if(!jwtService.isValid(rawRefreshToken)) {
            throw new UnauthorizedException("Invalid refresh token");
        }

        String type = jwtService.extractTokenType(rawRefreshToken);

        if (!"refresh".equals(type)) {
            throw new UnauthorizedException(
                    "Invalid token type"
            );
        }

        String hash = hashToken(rawRefreshToken);

        RefreshToken storedToken = refreshTokenRepository
                .findByTokenHashAndRevokedFalse(hash)
                .orElseThrow(
                        () -> new UnauthorizedException("Refresh token revoked or invalid")
                );

        if(storedToken.getExpiresAt()
                .isBefore(Instant.now())) {
            storedToken.setRevoked(true);

            throw new UnauthorizedException("Refresh token expired");
        }

        User user = storedToken.getUser();

        /*
         * Refresh token rotation.
         *
         * Revoke the old refresh token and
         * issue a completely new one.
         */
        storedToken.setRevoked(true);

        String newAccessToken = jwtService.generateAccessToken(user);
        String newRefreshToken = jwtService.generateRefreshToken(user);

        saveRefreshToken(user, newRefreshToken);

        return AuthResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken)
                .user(toResponse(user))
                .build();

    }

    public void logout(String rawRefreshToken) {

        if (!jwtService.isValid(rawRefreshToken)) {
            return;
        }

        String hash = hashToken(rawRefreshToken);

        refreshTokenRepository
                .findByTokenHashAndRevokedFalse(hash)
                .ifPresent(token -> {
                    token.setRevoked(true);
                    refreshTokenRepository.save(token);
                });
    }

    private UserResponse toResponse(User user) {

        return UserResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .phone(user.getPhone())
                .role(user.getRole())
                .build();
    }
}
