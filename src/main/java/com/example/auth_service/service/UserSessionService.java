package com.example.auth_service.service;

import com.example.auth_service.entity.UserSession;
import com.example.auth_service.exception.ResourceNotFoundException;
import com.example.auth_service.exception.ValidationException;
import com.example.auth_service.repository.UserSessionRepository;
import org.springframework.stereotype.Service;

@Service
public class UserSessionService {
    private final UserSessionRepository userSessionRepository;

    public UserSessionService(UserSessionRepository userSessionRepository) {
        this.userSessionRepository = userSessionRepository;
    }

    void existedBySessionId(String sessionId) {
        userSessionRepository.existsBySessionIdAndIsRevokedFalse(sessionId);
        if (!userSessionRepository.existsBySessionIdAndIsRevokedFalse(sessionId)) {
            throw new ResourceNotFoundException("User session", sessionId);
        }
    }

    public UserSession getUserSessionByUserId(String sessionId) {
        final var userSessionOpt = userSessionRepository.findDistinctFirstBySessionId(sessionId);
        if (userSessionOpt.isEmpty()) {
            throw new ResourceNotFoundException("User session", sessionId);
        }
        final var userSession = userSessionOpt.get();
        if (userSession.isRevoked() || userSession.getLogoutTime() != null) {
            throw new ValidationException("Invalid session");
        }
        return userSession;
    }
}
