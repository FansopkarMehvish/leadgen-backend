package com.example.leadgen_backend.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

@Schema(description = "Business profile details for business registration or update")
public record BusinessProfileRequest(
        @Schema(description = "Name of the business", example = "Quick Plumbers", required = true)
        @NotBlank(message = "Business name is required")
        @Size(min = 2, max = 200, message = "Business name must be between 2 and 200 characters")
        String businessName,

        @Schema(description = "Description of services offered", example = "Fast and reliable plumbing services")
        @Size(max = 1000, message = "Description must not exceed 1000 characters")
        String description,

        @Schema(description = "Minimum budget the business accepts", example = "50", required = true)
        @NotNull(message = "Minimum budget is required")
        @Positive(message = "Minimum budget must be positive")
        Integer minBudget,

        @Schema(description = "Maximum budget the business accepts", example = "500", required = true)
        @NotNull(message = "Maximum budget is required")
        @Positive(message = "Maximum budget must be positive")
        Integer maxBudget,

        @Schema(description = "Service radius in kilometers", example = "25", required = true)
        @NotNull(message = "Service radius is required")
        @Positive(message = "Service radius must be positive")
        Integer serviceRadiusKm,

        @Schema(description = "Category ID the business belongs to", example = "1", required = true)
        @NotNull(message = "Category is required")
        Long categoryId
) {}
