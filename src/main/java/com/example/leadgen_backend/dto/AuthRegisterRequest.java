package com.example.leadgen_backend.dto;

import com.example.leadgen_backend.enums.Role;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Schema(description = "User registration request payload")
public record AuthRegisterRequest(
        @Schema(description = "Full name of the user", example = "John Doe", required = true)
        @NotBlank(message = "Name is required")
        @Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
        String name,

        @Schema(description = "Email address (must be unique)", example = "john@example.com", required = true)
        @NotBlank(message = "Email is required")
        @Email(message = "Email must be valid")
        String email,

        @Schema(description = "Phone number", example = "+1234567890", required = true)
        @NotBlank(message = "Phone is required")
        @Size(min = 10, max = 20, message = "Phone must be between 10 and 20 characters")
        String phone,

        @Schema(description = "Password (min 6 characters)", example = "password123", required = true)
        @NotBlank(message = "Password is required")
        @Size(min = 6, message = "Password must be at least 6 characters")
        String password,

        @Schema(description = "User role", example = "CUSTOMER", allowableValues = {"CUSTOMER", "BUSINESS", "ADMIN"}, required = true)
        @NotNull(message = "Role is required")
        Role role,

        @Schema(description = "Latitude for geolocation", example = "40.7128")
        Double latitude,

        @Schema(description = "Longitude for geolocation", example = "-74.0060")
        Double longitude,

        @Schema(description = "Business profile (required for BUSINESS role)")
        @Valid
        BusinessProfileRequest businessProfile
) {}
