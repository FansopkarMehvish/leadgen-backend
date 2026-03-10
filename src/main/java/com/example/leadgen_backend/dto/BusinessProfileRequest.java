package com.example.leadgen_backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record BusinessProfileRequest(
        @NotBlank(message = "Business name is required")
        @Size(min = 2, max = 200, message = "Business name must be between 2 and 200 characters")
        String businessName,

        @Size(max = 1000, message = "Description must not exceed 1000 characters")
        String description,

        @NotNull(message = "Minimum budget is required")
        @Positive(message = "Minimum budget must be positive")
        Integer minBudget,

        @NotNull(message = "Maximum budget is required")
        @Positive(message = "Maximum budget must be positive")
        Integer maxBudget,

        @NotNull(message = "Service radius is required")
        @Positive(message = "Service radius must be positive")
        Integer serviceRadiusKm,

        @NotNull(message = "Category is required")
        Long categoryId
) {}

