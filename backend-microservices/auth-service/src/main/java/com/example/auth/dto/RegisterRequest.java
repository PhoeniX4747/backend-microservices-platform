package com.example.auth.dto;

import com.example.auth.model.UserRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record RegisterRequest(
        @NotBlank @Email String email,
        @NotBlank String password,
        @NotNull UserRole role
) {
}
