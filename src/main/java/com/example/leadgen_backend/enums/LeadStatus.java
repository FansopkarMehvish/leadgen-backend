package com.example.leadgen_backend.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum LeadStatus {
    NEW, ASSIGNED, CLAIMED, IN_PROGRESS, CLOSED, REJECTED;

    @JsonValue
    public String toJson() {
        return this.name();
    }

    @JsonCreator
    public static LeadStatus fromJson(String value) {
        return LeadStatus.valueOf(value.toUpperCase());
    }
}
