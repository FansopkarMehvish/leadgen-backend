package com.example.leadgen_backend.controller;

import com.example.leadgen_backend.dto.BusinessProfileRequest;
import com.example.leadgen_backend.model.BusinessProfile;
import com.example.leadgen_backend.model.User;
import com.example.leadgen_backend.repository.BusinessProfileRepository;
import com.example.leadgen_backend.repository.UserRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserProfileController {

    private final UserRepository userRepository;
    private final BusinessProfileRepository businessProfileRepository;

    @GetMapping("/profile")
    @PreAuthorize("hasAnyRole('CUSTOMER','BUSINESS','ADMIN')")
    public ResponseEntity<?> getCurrentUserProfile(Authentication authentication) {
        User user = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));

        return ResponseEntity.ok(user);
    }

    @PutMapping("/profile")
    @PreAuthorize("hasAnyRole('CUSTOMER','BUSINESS','ADMIN')")
    public ResponseEntity<?> updateCurrentUserProfile(
            Authentication authentication,
            @RequestBody User userUpdate) {

        User user = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setName(userUpdate.getName());
        user.setPhone(userUpdate.getPhone());
        user.setLatitude(userUpdate.getLatitude());
        user.setLongitude(userUpdate.getLongitude());

        User updated = userRepository.save(user);
        return ResponseEntity.ok(updated);
    }

    @GetMapping("/business-profile")
    @PreAuthorize("hasAnyRole('BUSINESS','ADMIN')")
    public ResponseEntity<?> getBusinessProfile(Authentication authentication) {
        User user = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));

        BusinessProfile profile = businessProfileRepository.findById(user.getId())
                .orElseThrow(() -> new RuntimeException("Business profile not found"));

        return ResponseEntity.ok(profile);
    }

    @PutMapping("/business-profile")
    @PreAuthorize("hasAnyRole('BUSINESS','ADMIN')")
    public ResponseEntity<?> updateBusinessProfile(
            Authentication authentication,
            @Valid @RequestBody BusinessProfileRequest request) {

        User user = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));

        BusinessProfile profile = businessProfileRepository.findById(user.getId())
                .orElseThrow(() -> new RuntimeException("Business profile not found"));

        profile.setBusinessName(request.businessName());
        profile.setDescription(request.description());
        profile.setMinBudget(request.minBudget());
        profile.setMaxBudget(request.maxBudget());
        profile.setServiceRadiusKm(request.serviceRadiusKm());
        profile.setCategoryId(request.categoryId());

        BusinessProfile updated = businessProfileRepository.save(profile);
        return ResponseEntity.ok(updated);
    }
}
