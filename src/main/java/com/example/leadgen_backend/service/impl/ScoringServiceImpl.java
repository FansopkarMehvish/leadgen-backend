package com.example.leadgen_backend.service.impl;

import com.example.leadgen_backend.model.BusinessProfile;
import com.example.leadgen_backend.model.Lead;
import com.example.leadgen_backend.service.ScoringService;
import com.example.leadgen_backend.util.GeoUtil;
import org.springframework.stereotype.Service;

@Service
public class ScoringServiceImpl implements ScoringService {

    // Score weights
    private static final int CATEGORY_MATCH_WEIGHT = 25;
    private static final int DISTANCE_WEIGHT = 30;
    private static final int BUDGET_WEIGHT = 25;
    private static final int URGENCY_WEIGHT = 20;
    private static final int MAX_SCORE = 100;

    @Override
    public int computeScore(Lead lead, BusinessProfile business) {
        int score = 0;

        // 1. Category Match (25 points)
        score += computeCategoryScore(lead, business);

        // 2. Distance Score (30 points)
        score += computeDistanceScore(lead, business);

        // 3. Budget Overlap (25 points)
        score += computeBudgetScore(lead, business);

        // 4. Urgency Bonus (20 points)
        score += computeUrgencyScore(lead, business);

        return Math.min(score, MAX_SCORE);
    }

    private int computeCategoryScore(Lead lead, BusinessProfile business) {
        Long leadCategoryId = lead.getCategoryId();
        Long businessCategoryId = business.getCategoryId();

        // If either doesn't have category, give partial credit
        if (leadCategoryId == null || businessCategoryId == null) {
            return CATEGORY_MATCH_WEIGHT / 2; // 12 points for unknown
        }

        // Exact category match
        if (leadCategoryId.equals(businessCategoryId)) {
            return CATEGORY_MATCH_WEIGHT;
        }

        // No match
        return 0;
    }

    private int computeDistanceScore(Lead lead, BusinessProfile business) {
        Double leadLat = lead.getLatitude();
        Double leadLon = lead.getLongitude();

        // If business has no location, give average score
        if (business.getUser() == null ||
                business.getUser().getLatitude() == null ||
                business.getUser().getLongitude() == null) {
            return DISTANCE_WEIGHT / 2;
        }

        // If lead has no location, give average score
        if (leadLat == null || leadLon == null) {
            return DISTANCE_WEIGHT / 2;
        }

        double distanceKm = GeoUtil.distanceKm(
                leadLat, leadLon,
                business.getUser().getLatitude(),
                business.getUser().getLongitude()
        );

        Integer radius = business.getServiceRadiusKm();
        if (radius == null || radius <= 0) {
            radius = 50; // default 50km radius if not set
        }

        // Score based on proximity within radius
        // Closer = higher score
        if (distanceKm <= radius * 0.25) {
            return DISTANCE_WEIGHT; // Very close (top 25% of radius)
        } else if (distanceKm <= radius * 0.5) {
            return (int) (DISTANCE_WEIGHT * 0.8); // Close (50% of radius)
        } else if (distanceKm <= radius * 0.75) {
            return (int) (DISTANCE_WEIGHT * 0.6); // Moderate (75% of radius)
        } else if (distanceKm <= radius) {
            return (int) (DISTANCE_WEIGHT * 0.4); // Edge of radius
        } else {
            return (int) (DISTANCE_WEIGHT * 0.2); // Beyond radius but still included
        }
    }

    private int computeBudgetScore(Lead lead, BusinessProfile business) {
        Integer leadMin = lead.getBudgetFrom();
        Integer leadMax = lead.getBudgetTo();
        Integer businessMin = business.getMinBudget();
        Integer businessMax = business.getMaxBudget();

        // If any budget info is missing, give average score
        if (leadMin == null || leadMax == null || businessMin == null || businessMax == null) {
            return BUDGET_WEIGHT / 2;
        }

        // Calculate overlap
        int overlapStart = Math.max(leadMin, businessMin);
        int overlapEnd = Math.min(leadMax, businessMax);

        if (overlapStart >= overlapEnd) {
            // No overlap - check if close
            int gap = Math.min(
                    Math.abs(leadMin - businessMax),
                    Math.abs(leadMax - businessMin)
            );

            // Small gap gets partial credit
            if (gap <= 5000) {
                return (int) (BUDGET_WEIGHT * 0.3);
            }
            return 0;
        }

        int overlapSize = overlapEnd - overlapStart;
        int leadBudgetRange = leadMax - leadMin;

        if (leadBudgetRange <= 0) {
            return BUDGET_WEIGHT; // Avoid division by zero
        }

        // Score based on overlap percentage
        double overlapRatio = (double) overlapSize / leadBudgetRange;

        if (overlapRatio >= 0.8) {
            return BUDGET_WEIGHT; // Excellent overlap (80%+)
        } else if (overlapRatio >= 0.5) {
            return (int) (BUDGET_WEIGHT * 0.7); // Good overlap (50%+)
        } else if (overlapRatio >= 0.25) {
            return (int) (BUDGET_WEIGHT * 0.4); // Partial overlap (25%+)
        } else {
            return (int) (BUDGET_WEIGHT * 0.2); // Small overlap
        }
    }

    private int computeUrgencyScore(Lead lead, BusinessProfile business) {
        // This could be enhanced with business availability calendar
        // For now, give full points for HIGH urgency to prioritize important leads

        if (lead.getUrgency() == null) {
            return URGENCY_WEIGHT / 2;
        }

        return switch (lead.getUrgency()) {
            case HIGH -> URGENCY_WEIGHT;      // 20 points - prioritize urgent
            case MEDIUM -> (int) (URGENCY_WEIGHT * 0.7);  // 14 points
            case LOW -> (int) (URGENCY_WEIGHT * 0.4);     // 8 points
        };
    }
}

