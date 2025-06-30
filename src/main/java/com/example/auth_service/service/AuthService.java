package com.example.auth_service.service;

import com.example.auth_service.dto.LoginRequest;
import com.example.auth_service.dto.LoginResponse;
import com.example.auth_service.dto.RefreshResponse;
import com.example.auth_service.entity.User;
import com.example.auth_service.entity.UserSession;
import com.example.auth_service.repository.UserSessionRepository;
import com.example.auth_service.util.JwtUtil;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class AuthService {
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final UserService userService;
    private final UserSessionRepository userSessionRepository;
    private final UserSessionService userSessionService;

    public AuthService(
            AuthenticationManager authenticationManager,
            JwtUtil jwtUtil,
            UserService userService,
            UserSessionRepository userSessionRepository, UserSessionService userSessionService) {
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
        this.userService = userService;
        this.userSessionRepository = userSessionRepository;
        this.userSessionService = userSessionService;
    }

    public LoginResponse login(LoginRequest loginRequest) throws AuthenticationException {
        final var authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword())
        );
        final var user = (User) authentication.getPrincipal();
        final var refreshToken = jwtUtil.generateToken(user, JwtUtil.JwtType.REFRESH);
        final var sessionId = jwtUtil.extractSessionId(refreshToken);
        final var sessionEntity = new UserSession(sessionId, user);
        userSessionRepository.save(sessionEntity);
        return new LoginResponse(
                jwtUtil.generateToken(user, JwtUtil.JwtType.ACCESS),
                refreshToken
        );
    }

    public RefreshResponse refresh(String refreshToken) {
        //<editor-fold desc="Validation">
        final var username = jwtUtil.extractUsername(refreshToken, JwtUtil.JwtType.REFRESH);
        final var sessionId = jwtUtil.extractSessionId(refreshToken);
        userSessionService.existedBySessionId(sessionId);
        final var user = userService.loadUserByUsername(username);
        //</editor-fold>
        final var newAccessToken = jwtUtil.generateToken(user, JwtUtil.JwtType.ACCESS);
        return new RefreshResponse(newAccessToken);
    }

    public void logout(String refreshToken) {
        final var sessionId = jwtUtil.extractSessionId(refreshToken);
        final var userSession = userSessionService.getUserSessionByUserId(sessionId);
        userSession.setLogoutTime(LocalDateTime.now());
        userSession.setRevoked(true);
        userSessionRepository.save(userSession);
    }
}
