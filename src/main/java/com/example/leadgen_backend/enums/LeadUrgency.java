package com.example.leadgen_backend.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum LeadUrgency {
    LOW, MEDIUM, HIGH;

    @JsonValue
    public String toJson() {
        return this.name();
    }

    @JsonCreator
    public static LeadUrgency fromJson(String value) {
        return LeadUrgency.valueOf(value.toUpperCase());
    }
}

