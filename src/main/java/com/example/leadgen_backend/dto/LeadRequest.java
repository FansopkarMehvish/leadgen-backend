package com.example.leadgen_backend.dto;

import com.example.leadgen_backend.enums.LeadUrgency;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

@Schema(description = "Lead creation request payload")
public record LeadRequest(
        @Schema(description = "Customer's full name", example = "John Doe", required = true)
        @NotBlank(message = "Customer name is required")
        @Size(min = 2, max = 100, message = "Customer name must be between 2 and 100 characters")
        String customerName,

        @Schema(description = "Customer's phone number", example = "+1234567890", required = true)
        @NotBlank(message = "Customer phone is required")
        @Size(min = 10, max = 20, message = "Customer phone must be between 10 and 20 characters")
        String customerPhone,

        @Schema(description = "Customer's email address", example = "customer@example.com", required = true)
        @NotBlank(message = "Customer email is required")
        @Email(message = "Customer email must be valid")
        String customerEmail,

        @Schema(description = "Service category ID", example = "1", required = true)
        @NotNull(message = "Category is required")
        @Positive(message = "Category ID must be positive")
        Long categoryId,

        @Schema(description = "Description of the service needed", example = "Need plumbing help for kitchen sink")
        @Size(max = 2000, message = "Description must not exceed 2000 characters")
        String description,

        @Schema(description = "Minimum budget amount", example = "100", required = true)
        @NotNull(message = "Budget from is required")
        @Positive(message = "Budget from must be positive")
        Integer budgetFrom,

        @Schema(description = "Maximum budget amount", example = "300", required = true)
        @NotNull(message = "Budget to is required")
        @Positive(message = "Budget to must be positive")
        Integer budgetTo,

        @Schema(description = "Location description", example = "123 Main St, New York, NY")
        @Size(max = 500, message = "Location text must not exceed 500 characters")
        String locationText,

        @Schema(description = "Latitude coordinate for geolocation", example = "40.7128")
        Double latitude,

        @Schema(description = "Longitude coordinate for geolocation", example = "-74.0060")
        Double longitude,

        @Schema(description = "Urgency level", example = "HIGH", allowableValues = {"LOW", "MEDIUM", "HIGH"}, required = true)
        @NotNull(message = "Urgency is required")
        LeadUrgency urgency
) {}
