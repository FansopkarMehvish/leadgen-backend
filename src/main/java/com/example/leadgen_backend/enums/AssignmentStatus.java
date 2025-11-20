package com.example.leadgen_backend.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum AssignmentStatus {
    NOTIFIED, CLAIMED, EXPIRED;

    @JsonValue
    public String toJson() {
        return this.name();
    }

    @JsonCreator
    public static AssignmentStatus fromJson(String value) {
        return AssignmentStatus.valueOf(value.toUpperCase());
    }
}
