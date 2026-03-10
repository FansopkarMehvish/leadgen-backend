package com.example.leadgen_backend.service.impl;

import com.example.leadgen_backend.dto.AuthRegisterRequest;
import com.example.leadgen_backend.dto.BusinessProfileRequest;
import com.example.leadgen_backend.enums.Role;
import com.example.leadgen_backend.model.BusinessProfile;
import com.example.leadgen_backend.model.User;
import com.example.leadgen_backend.repository.BusinessProfileRepository;
import com.example.leadgen_backend.repository.UserRepository;
import com.example.leadgen_backend.security.JwtUtil;
import com.example.leadgen_backend.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final BusinessProfileRepository businessProfileRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @Override
    @Transactional
    public User register(AuthRegisterRequest req) {

        User user = User.builder()
                .name(req.name())
                .email(req.email())
                .phone(req.phone())
                .passwordHash(passwordEncoder.encode(req.password()))
                .role(req.role())
                .latitude(req.latitude())
                .longitude(req.longitude())
                .verified(false)
                .build();

        User saved = userRepository.save(user);

        // Only create profile if role = BUSINESS
        if (req.role() == Role.BUSINESS && req.businessProfile() != null) {
            BusinessProfileRequest bp = req.businessProfile();

            BusinessProfile profile = BusinessProfile.builder()
                    .user(saved)
                    .businessName(bp.businessName())
                    .description(bp.description())
                    .minBudget(bp.minBudget())
                    .maxBudget(bp.maxBudget())
                    .serviceRadiusKm(bp.serviceRadiusKm())
                    .categoryId(bp.categoryId())
                    .active(true)
                    .build();

            businessProfileRepository.save(profile);
        }

        return saved;
    }

    @Override
    public String login(String username, String password) {
        User user = userRepository.findByEmail(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!passwordEncoder.matches(password, user.getPasswordHash())) {
            throw new RuntimeException("Invalid credentials");
        }

        return jwtUtil.generateToken(user.getEmail());
    }
}
