package com.example.leadgen_backend.service.impl;

import com.example.leadgen_backend.model.BusinessProfile;
import com.example.leadgen_backend.model.Lead;
import com.example.leadgen_backend.service.ScoringService;
import org.springframework.stereotype.Service;

@Service
public class ScoringServiceImpl implements ScoringService {

    @Override
    public int computeScore(Lead lead, BusinessProfile business) {
        // Basic rule-based scoring placeholder
        int score = 0;
        // category match - placeholder
        score += 30;
        // distance-based scoring - placeholder
        score += 35;
        // budget match - placeholder
        score += 20;
        // availability/urgency - placeholder
        score += 15;
        return Math.min(score, 100);
    }
}

