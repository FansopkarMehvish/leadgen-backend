package com.example.leadgen_backend.service;

import com.example.leadgen_backend.dto.AuthRegisterRequest;
import com.example.leadgen_backend.model.User;

public interface AuthService {
    User register(AuthRegisterRequest req);
    String login(String username, String password);
}
