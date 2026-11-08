package com.dsaquest.auth.dto;

import com.dsaquest.auth.entity.Role;
import com.dsaquest.auth.entity.User;

import java.util.UUID;

public record UserResponse(UUID id, String fullName, String email, Role role) {
    public static UserResponse from(User user) {
        return new UserResponse(user.getId(), user.getFullName(), user.getEmail(), user.getRole());
    }
}
