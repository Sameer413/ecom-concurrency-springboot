package org.example.flashsale.auth.security;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
public class AuthCookieService {


    public void addAccessTokenCookie(HttpServletResponse response, String token) {

        ResponseCookie cookie = ResponseCookie
                .from("access_token", token)
                .httpOnly(true)
                .secure(false)   // set to true in production (HTTPS only)
                .sameSite("Lax")
                .path("/")
                .maxAge(Duration.ofMinutes(15))
                .build();

//        response.addHeader("Set-Cookie", cookie.toString());
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }

    public void addRefreshTokenCookie(HttpServletResponse response, String token) {

        ResponseCookie cookie = ResponseCookie
                .from("refresh_token", token)
//                .httpOnly(true)
//                .secure(true)
//                .sameSite("Lax")
//                .path("/api/auth")
                .maxAge(Duration.ofDays(7))
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }

    public void clearAuthCookies(HttpServletResponse response) {

        ResponseCookie accessCookie = ResponseCookie
                .from("access_token", "")
                .httpOnly(true)
                .secure(true)
                .sameSite("Lax")
                .path("/")
                .maxAge(Duration.ZERO)
                .build();

        ResponseCookie refreshCookie = ResponseCookie
                .from("refresh_token", "")
                .httpOnly(true)
                .secure(true)
                .sameSite("Lax")
                .path("/api/auth")
                .maxAge(Duration.ZERO)
                .build();

        response.addHeader(
                HttpHeaders.SET_COOKIE,
                accessCookie.toString()
        );

        response.addHeader(
                HttpHeaders.SET_COOKIE,
                refreshCookie.toString()
        );
    }
}
