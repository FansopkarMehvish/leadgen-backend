package com.example.leadgen_backend.service.impl;

import com.example.leadgen_backend.model.User;
import com.example.leadgen_backend.repository.UserRepository;
import com.example.leadgen_backend.security.JwtUtil;
import com.example.leadgen_backend.service.AuthService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;

    public AuthServiceImpl(UserRepository userRepository, JwtUtil jwtUtil, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.jwtUtil = jwtUtil;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public User register(User user, String rawPassword) {
        user.setPasswordHash(passwordEncoder.encode(rawPassword));
        return userRepository.save(user);
    }

    @Override
    public String login(String username, String rawPassword) {
        // username could be email or phone - simple example
        User user = userRepository.findByEmail(username).orElseThrow(() -> new RuntimeException("User not found"));
        if (!passwordEncoder.matches(rawPassword, user.getPasswordHash()))
            throw new RuntimeException("Invalid credentials");
        // generate token with username (email) as subject for easier lookup
        return jwtUtil.generateToken(user.getEmail());
    }
}
