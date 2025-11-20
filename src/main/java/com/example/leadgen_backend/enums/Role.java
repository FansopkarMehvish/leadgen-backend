package com.example.leadgen_backend.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum Role {
    CUSTOMER,
    BUSINESS,
    ADMIN;

    @JsonValue
    public String toJson() {
        return this.name();
    }

    @JsonCreator
    public static Role fromJson(String value) {
        return Role.valueOf(value.toUpperCase());
    }
}
