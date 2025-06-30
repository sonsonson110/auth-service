package com.example.auth_service.repository;

import com.example.auth_service.entity.UserSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserSessionRepository extends JpaRepository<UserSession, Long> {
    Optional<UserSession> findDistinctFirstBySessionId(String sessionId);
    boolean existsBySessionIdAndIsRevokedFalse(String sessionId);
}
