package com.example.auth_service.controller;

import com.example.auth_service.dto.ApiResponse;
import com.example.auth_service.dto.LoginRequest;
import com.example.auth_service.service.AuthService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {
    @Value("${jwt.access-token-expiration}")
    private int accessTokenExpiration;
    @Value("${jwt.refresh-token-expiration}")
    private int refreshTokenExpiration;

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<?>> login(
            @RequestBody @Valid LoginRequest loginRequest,
            @RequestParam(required = false) String responseType,
            HttpServletResponse response
    ) {
        final var result = authService.login(loginRequest);

        if ("body".equals(responseType)) {
            return ResponseEntity.ok(ApiResponse.success("Login successful", result));
        }
        Cookie accessCookie = new Cookie("accessToken", result.accessToken());
        accessCookie.setHttpOnly(true);
//            accessCookie.setSecure(true);
        accessCookie.setPath("/");
        accessCookie.setMaxAge(accessTokenExpiration / 1000);

        Cookie refreshCookie = new Cookie("refreshToken", result.refreshToken());
        refreshCookie.setHttpOnly(true);
//            refreshCookie.setSecure(true);
        refreshCookie.setPath("/");
        refreshCookie.setMaxAge(refreshTokenExpiration / 1000);

        response.addCookie(accessCookie);
        response.addCookie(refreshCookie);

        return ResponseEntity.ok(ApiResponse.success("Login successful"));
    }
}
