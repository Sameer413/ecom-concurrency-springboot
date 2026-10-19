package org.example.flashsale.auth.security;

import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.example.flashsale.auth.entity.UserPrincipal;
import org.example.flashsale.user.entity.User;
import org.example.flashsale.user.repository.UserRepository;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.UUID;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserRepository userRepository;


    public JwtAuthenticationFilter(JwtService jwtService, UserRepository userRepository) {
        this.jwtService = jwtService;
        this.userRepository = userRepository;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        String accessToken = extractAccessToken(request);

        if (
                accessToken != null &&
                        !accessToken.trim().isEmpty() &&
                        SecurityContextHolder.getContext().getAuthentication() == null
        ) {
            try {
                UUID userId = jwtService.extractUserId(accessToken);

                if (userId != null) {
                    User user = userRepository.findById(userId)
                            .orElse(null);

                    if (user == null) {
                        filterChain.doFilter(request, response);
                        return;
                    }

                    UserPrincipal userPrincipal = new UserPrincipal(user);

                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            userPrincipal,
                            null,
                            userPrincipal.getAuthorities()
                    );

                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                    SecurityContext context = SecurityContextHolder.createEmptyContext();
                    context.setAuthentication(authToken);
                    SecurityContextHolder.setContext(context);
                }

            } catch (JwtException | IllegalArgumentException e) {
                System.out.println(e.getMessage());
            }
        }

        filterChain.doFilter(request, response);
    }

    private String extractAccessToken(
            HttpServletRequest request
    ) {

        if (request.getCookies() == null) {
            return null;
        }

        return Arrays.stream(request.getCookies())
                .filter(cookie ->
                        "access_token"
                                .equals(cookie.getName())
                )
                .map(Cookie::getValue)
                .findFirst()
                .orElse(null);
    }
}


//if (accessToken == null) {
//        filterChain.doFilter(request, response);
//            return;
//                    }
//
//                    try {
//                    if(!jwtService.isValid(token)) {
//        filterChain.doFilter(request, response);
//                return;
//                        }
//
//String type = jwtService.extractTokenType(token);
//
//            if (!"access".equals(type)) {
//        filterChain.doFilter(request, response);
//                return;
//                        }
//
//UUID userId = jwtService.extractUserId(token);
//
//User user = userRepository.findById(userId)
//        .orElse(null);
//
//            if (user == null) {
//        filterChain.doFilter(request, response);
//                return;
//                        }
//
//UserPrincipal principal = new UserPrincipal(user);
//
//UsernamePasswordAuthenticationToken authenticationToken =
//        new UsernamePasswordAuthenticationToken(
//                principal,
//                null,
//                principal.getAuthorities()
//        );
//
//            SecurityContextHolder
//                    .getContext()
//                    .setAuthentication(authenticationToken);
//            System.out.println(">>> AUTH SET for: " + principal.getEmail());
//        } catch (JwtException | IllegalArgumentException e) {
//
//        SecurityContextHolder.clearContext();
//        }