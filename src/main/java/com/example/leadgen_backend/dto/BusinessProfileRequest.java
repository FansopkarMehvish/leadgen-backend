package com.example.leadgen_backend.dto;

public record BusinessProfileRequest(
        String businessName,
        String description,
        Integer minBudget,
        Integer maxBudget,
        Integer serviceRadiusKm
) {}

