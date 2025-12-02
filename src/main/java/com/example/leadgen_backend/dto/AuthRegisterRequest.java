package com.example.leadgen_backend.dto;

import com.example.leadgen_backend.enums.Role;

public record AuthRegisterRequest(
        String name,
        String email,
        String phone,
        String password,
        Role role,
        Double latitude,
        Double longitude,
        BusinessProfileRequest businessProfile
) {}

