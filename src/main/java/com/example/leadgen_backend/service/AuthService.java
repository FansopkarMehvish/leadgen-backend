package com.example.leadgen_backend.service;

import com.example.leadgen_backend.model.User;

public interface AuthService {
    User register(User user, String rawPassword);
    String login(String username, String rawPassword);
}
