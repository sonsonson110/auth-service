package com.example.auth_service.controller;

import com.example.auth_service.dto.ApiResponse;
import com.example.auth_service.dto.LoginRequest;
import com.example.auth_service.exception.ValidationException;
import com.example.auth_service.service.AuthService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.core.env.Environment;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController {
    @Value("${jwt.access-token-expiration}")
    private int accessTokenExpiration;
    @Value("${jwt.refresh-token-expiration}")
    private int refreshTokenExpiration;

    private final AuthService authService;
    private final boolean isProduction;

    public AuthController(AuthService authService, Environment environment) {
        this.authService = authService;
        isProduction = Arrays.stream(environment.getActiveProfiles()).anyMatch(p -> p.equalsIgnoreCase("prod"));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<?>> login(
            @RequestBody @Valid LoginRequest loginRequest,
            @RequestParam(required = false) String responseType,
            HttpServletResponse response
    ) {
        final var result = authService.login(loginRequest);

        if (!"body".equals(responseType)) {
            final var accessCookie = new Cookie("accessToken", result.accessToken());
            accessCookie.setHttpOnly(true);
            accessCookie.setSecure(isProduction);
            accessCookie.setPath("/");
            accessCookie.setMaxAge(accessTokenExpiration / 1000);

            final var refreshCookie = new Cookie("accessToken", result.refreshToken());
            refreshCookie.setHttpOnly(true);
            refreshCookie.setSecure(isProduction);
            refreshCookie.setPath("/");
            refreshCookie.setMaxAge(refreshTokenExpiration / 1000);

            response.addCookie(accessCookie);
            response.addCookie(refreshCookie);
        }
        return ResponseEntity.ok(ApiResponse.success(
                "Login successful",
                "body".equals(responseType) ? result : null));
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<?>> refresh(
            @RequestParam(required = false) String responseType,
            @CookieValue(name = "refreshToken", required = false) String refreshToken,
            HttpServletResponse response
    ) {
        if (refreshToken == null) {
            final var errors = Map.of("refreshToken", List.of("missing cookie"));
            throw new ValidationException(errors);
        }

        final var result = authService.refresh(refreshToken);
        if (!"body".equals(responseType)) {
            final var accessCookie = new Cookie("accessToken", result.accessToken());
            accessCookie.setHttpOnly(true);
            accessCookie.setSecure(isProduction);
            accessCookie.setPath("/");
            accessCookie.setMaxAge(accessTokenExpiration / 1000);
            response.addCookie(accessCookie);
        }
        return ResponseEntity.ok(ApiResponse.success(
                "Refresh access token successful",
                "body".equals(responseType) ? result : null));
    }
}
