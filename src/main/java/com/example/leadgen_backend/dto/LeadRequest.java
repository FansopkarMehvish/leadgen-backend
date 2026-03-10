package com.example.leadgen_backend.dto;

import com.example.leadgen_backend.enums.LeadUrgency;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record LeadRequest(
        @NotBlank(message = "Customer name is required")
        @Size(min = 2, max = 100, message = "Customer name must be between 2 and 100 characters")
        String customerName,

        @NotBlank(message = "Customer phone is required")
        @Size(min = 10, max = 20, message = "Customer phone must be between 10 and 20 characters")
        String customerPhone,

        @NotBlank(message = "Customer email is required")
        @Email(message = "Customer email must be valid")
        String customerEmail,

        @NotNull(message = "Category is required")
        @Positive(message = "Category ID must be positive")
        Long categoryId,

        @Size(max = 2000, message = "Description must not exceed 2000 characters")
        String description,

        @NotNull(message = "Budget from is required")
        @Positive(message = "Budget from must be positive")
        Integer budgetFrom,

        @NotNull(message = "Budget to is required")
        @Positive(message = "Budget to must be positive")
        Integer budgetTo,

        @Size(max = 500, message = "Location text must not exceed 500 characters")
        String locationText,

        Double latitude,

        Double longitude,

        @NotNull(message = "Urgency is required")
        LeadUrgency urgency
) {}
