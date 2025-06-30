package com.example.auth_service.service;

import com.example.auth_service.repository.UserSessionRepository;
import org.springframework.stereotype.Service;

@Service
public class UserSessionService {
    private final UserSessionRepository userSessionRepository;

    public UserSessionService(UserSessionRepository userSessionRepository) {
        this.userSessionRepository = userSessionRepository;
    }
}
