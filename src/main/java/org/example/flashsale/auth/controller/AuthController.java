package org.example.flashsale.auth.controller;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.example.flashsale.auth.dto.AuthResponse;
import org.example.flashsale.auth.dto.SignInRequest;
import org.example.flashsale.auth.dto.SignUpRequest;
import org.example.flashsale.auth.security.AuthCookieService;
import org.example.flashsale.auth.service.AuthService;
import org.example.flashsale.common.persistence.ApiResponse;
import org.example.flashsale.user.dto.UserResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {
    private final AuthService authService;
    private final AuthCookieService authCookieService;

    public AuthController(AuthService authService, AuthCookieService authCookieService) {
        this.authService = authService;
        this.authCookieService = authCookieService;
    }

    @PostMapping("/sign-up")
    public ResponseEntity<ApiResponse<UserResponse>> createUserHandler(@Valid @RequestBody SignUpRequest request, HttpServletResponse response) {
        AuthResponse tokens = authService.signUp(request);

        authCookieService.addAccessTokenCookie(
                response,
                tokens.getAccessToken()
        );

        authCookieService.addRefreshTokenCookie(
                response,
                tokens.getRefreshToken()
        );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(
                        new ApiResponse<>(true, "User Signed Up Successfully", tokens.getUser())
                );
    }

    @PostMapping("/sign-in")
    public ResponseEntity<ApiResponse<UserResponse>> signInHandler(@Valid @RequestBody SignInRequest request, HttpServletResponse response) {
        AuthResponse signInResponse = authService.signIn(request);

        authCookieService.addAccessTokenCookie(
                response,
                signInResponse.getAccessToken()
        );

        authCookieService.addRefreshTokenCookie(
                response,
                signInResponse.getRefreshToken()
        );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(
                        new ApiResponse<>(true, "User Signed In Successfully", signInResponse.getUser())
                );
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<UserResponse>> refresh(
            HttpServletRequest request,
            HttpServletResponse response
    ) {

        String refreshToken =
                extractCookie(
                        request
                );

        AuthResponse tokens =
                authService.refresh(refreshToken);

        authCookieService.addAccessTokenCookie(
                response,
                tokens.getAccessToken()
        );

        authCookieService.addRefreshTokenCookie(
                response,
                tokens.getRefreshToken()
        );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(
                        new ApiResponse<>(true, "Token Refreshed Successfully", tokens.getUser())
                );
    }

    @PostMapping("/logout")
    public void logout(
            HttpServletRequest request,
            HttpServletResponse response
    ) {

        String refreshToken =
                extractCookie(
                        request
                );

        if (refreshToken != null) {
            authService.logout(refreshToken);
        }

        authCookieService.clearAuthCookies(response);
    }

    private String extractCookie(
            HttpServletRequest request
    ) {

        if (request.getCookies() == null) {
            return null;
        }

        return Arrays.stream(request.getCookies())
                .filter(cookie ->
                        "refresh_token".equals(cookie.getName())
                )
                .map(Cookie::getValue)
                .findFirst()
                .orElse(null);
    }
}
