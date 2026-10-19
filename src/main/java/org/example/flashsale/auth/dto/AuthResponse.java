package org.example.flashsale.auth.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.example.flashsale.user.dto.UserResponse;

@Getter
@Setter
@Builder
public class AuthResponse {
    private String accessToken;
    private String refreshToken;
    private UserResponse user;
}
