package com.example.auth_service.repository;

import com.example.auth_service.entity.UserSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Repository
public interface UserSessionRepository extends JpaRepository<UserSession, Long> {
    Optional<UserSession> findDistinctFirstBySessionId(String sessionId);
    boolean existsBySessionIdAndIsRevokedFalse(String sessionId);
    @Modifying
    @Transactional
    @Query("UPDATE UserSession us SET us.isRevoked = true WHERE us.user.id = :userId")
    int revokeAllSessionsByUserId(Long userId);
}
