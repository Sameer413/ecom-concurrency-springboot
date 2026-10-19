package org.example.flashsale.user.service;

import org.example.flashsale.auth.dto.SignUpRequest;
import org.example.flashsale.common.exception.ResourceNotFoundException;
import org.example.flashsale.user.dto.CreateUserDTO;
import org.example.flashsale.user.dto.UserResponse;
import org.example.flashsale.user.entity.User;
import org.example.flashsale.user.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class UserService {
    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public UserResponse createUser(CreateUserDTO request) {
        User user = User.builder()
                .email(request.getEmail())
                .passwordHash(request.getPasswordHash())
                .phone(request.getPhone())
                .role(request.getRole())
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .build();

        User createdUser = userRepository.save(user);

        return toResponse(createdUser);
    }

    public User createUserObj(CreateUserDTO request) {
        User user = User.builder()
                .email(request.getEmail())
                .passwordHash(request.getPasswordHash())
                .phone(request.getPhone())
                .role(request.getRole())
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .build();

        return userRepository.save(user);
    }

    public UserResponse findUserById(UUID id) {
        User user = userRepository.findById(id).orElseThrow(
                () -> new ResourceNotFoundException("User not found with id: " + id)
        );

        return toResponse(user);
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
