package com.example.auth_service.repository;

import com.example.auth_service.entity.UserSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserSessionRepository extends JpaRepository<UserSession, Long> {
    boolean existsBySessionIdAndIsRevokedFalse(String sessionId);
}
