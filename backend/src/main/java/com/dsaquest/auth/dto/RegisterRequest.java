package com.dsaquest.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @NotBlank @Size(max = 120) String fullName,
        @NotBlank @Email @Size(max = 320) String email,
        @NotBlank @Size(min = 12, max = 72)
        @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).+$",
                message = "must contain lowercase, uppercase, and a digit")
        String password,
        @NotBlank String passwordConfirmation) {
}
