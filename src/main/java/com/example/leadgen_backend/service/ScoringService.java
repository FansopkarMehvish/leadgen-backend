package com.example.leadgen_backend.service;

import com.example.leadgen_backend.model.BusinessProfile;
import com.example.leadgen_backend.model.Lead;

public interface ScoringService {
    int computeScore(Lead lead, BusinessProfile business);
}