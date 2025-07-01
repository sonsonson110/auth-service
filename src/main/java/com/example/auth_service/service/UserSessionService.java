package com.example.auth_service.service;

import com.example.auth_service.entity.User;
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

    void invalidateSession(String sessionId) {
        final var userSession = getValidSession(sessionId);
        userSession.setRevoked(true);
        userSessionRepository.save(userSession);
    }

    public UserSession getValidSession(String sessionId) {
        final var userSessionOpt = userSessionRepository.findDistinctFirstBySessionId(sessionId);
        if (userSessionOpt.isEmpty()) {
            throw new ResourceNotFoundException("User session", sessionId);
        }
        final var userSession = userSessionOpt.get();
        if (userSession.isRevoked()) {
            throw new ValidationException("Invalid session");
        }
        return userSession;
    }

    public void createSession(String sessionId, User user) {
        final var userSession = new UserSession();
        userSession.setSessionId(sessionId);
        userSession.setUser(user);
        userSessionRepository.save(userSession);
    }

    public void revokeAllUserSessions(Long userId) {
        userSessionRepository.revokeAllSessionsByUserId(userId);
    }
}
