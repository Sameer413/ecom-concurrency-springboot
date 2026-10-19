package org.example.flashsale.user.controller;

import org.example.flashsale.auth.entity.UserPrincipal;
import org.example.flashsale.common.persistence.ApiResponse;
import org.example.flashsale.user.dto.UserResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/user")
public class UserController {

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserResponse>> me(
            @AuthenticationPrincipal UserPrincipal userPrincipal
    ) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(
                        new ApiResponse<>(
                                true,
                                "User fetched Successfully",
                                UserResponse.builder()
                                        .id(userPrincipal.getId())
                                        .email(userPrincipal.getEmail())
                                        .role(userPrincipal.getRole())
                                        .build()
                                )
                );
    }

    @GetMapping("/test")
//    @PreAuthorize("hasRole('CUSTOMER')")
    @PreAuthorize("hasAnyRole('ADMIN', 'VENDOR')")
    public ResponseEntity<ApiResponse<?>> test() {
        return ResponseEntity.ok(
                new ApiResponse<>(true, "Authorized", null)
        );
    }
}
