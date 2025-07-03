package com.example.ecommerce.controller;

import com.example.ecommerce.dto.ApiResponse;
import com.example.ecommerce.dto.ForgotPasswordRequest;
import com.example.ecommerce.dto.LoginRequest;
import com.example.ecommerce.dto.ResetPasswordRequest;
import com.example.ecommerce.exception.ValidationException;
import com.example.ecommerce.service.AuthService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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

            final var refreshCookie = new Cookie("refreshToken", result.refreshToken());
            refreshCookie.setHttpOnly(true);
            refreshCookie.setSecure(isProduction);
            refreshCookie.setPath("/");
            refreshCookie.setMaxAge(refreshTokenExpiration / 1000);

            response.addCookie(accessCookie);
            response.addCookie(refreshCookie);
        }
        return ResponseEntity.ok(ApiResponse.success(
                "Login successfully",
                "body".equals(responseType) ? result : null));
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<?>> refresh(
            @RequestParam(required = false) String responseType,
            @CookieValue(name = "refreshToken", required = false) String refreshToken,
            HttpServletResponse response
    ) {
        validateRefreshToken(refreshToken);

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
                "Refresh access token successfully",
                "body".equals(responseType) ? result : null));
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<?>> logout(
            @CookieValue(name = "refreshToken", required = false) String refreshToken,
            HttpServletResponse response) {
        validateRefreshToken(refreshToken);

        authService.logout(refreshToken);

        final var accessCookie = new Cookie("accessToken", "");
        accessCookie.setHttpOnly(true);
        accessCookie.setPath("/");
        accessCookie.setMaxAge(0);

        final var refreshCookie = new Cookie("refreshToken", "");
        refreshCookie.setHttpOnly(true);
        refreshCookie.setPath("/");
        refreshCookie.setMaxAge(0);

        response.addCookie(accessCookie);
        response.addCookie(refreshCookie);

        return ResponseEntity.ok(ApiResponse.success("Logout successfully"));
    }

    private void validateRefreshToken(String refreshToken) {
        if (refreshToken == null) {
            final var errors = Map.of("refreshToken", List.of("missing cookie"));
            throw new ValidationException(errors);
        }
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<ApiResponse<?>> forgotPassword(
            @RequestBody @Valid ForgotPasswordRequest dto
    ) {
        authService.initiatePasswordReset(dto);
        return ResponseEntity.ok(ApiResponse.success("Password reset link sent to your email"));
    }

    @GetMapping("/reset-password/{token}/verify")
    public ResponseEntity<ApiResponse<?>> verifyResetPasswordToken(
            @PathVariable String token
    ) {
        authService.validatePasswordResetToken(token);
        return ResponseEntity.ok(ApiResponse.success("Reset password token verified successfully"));

    }

    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponse<?>> resetPassword(
            @RequestBody @Valid ResetPasswordRequest dto
    ) {
        authService.resetPassword(dto.getResetToken(), dto.getNewPassword());
        return ResponseEntity.ok(ApiResponse.success("Password reset successfully"));
    }
}
