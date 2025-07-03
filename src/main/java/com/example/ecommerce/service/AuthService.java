package com.example.ecommerce.service;

import com.example.ecommerce.dto.ForgotPasswordRequest;
import com.example.ecommerce.dto.LoginRequest;
import com.example.ecommerce.dto.LoginResponse;
import com.example.ecommerce.dto.RefreshResponse;
import com.example.ecommerce.entity.User;
import com.example.ecommerce.exception.ValidationException;
import com.example.ecommerce.util.JwtUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.UUID;

@Service
public class AuthService {
    private static final Logger log = LoggerFactory.getLogger(AuthService.class);
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final UserService userService;
    private final UserSessionService userSessionService;
    private final CacheManager cacheManager;

    @Value("${app.security.passwordResetTokenSecret}")
    private String SECRET_KEY;
    private final String RESET_TOKEN_CACHE = "forgotPasswordToken";

    public AuthService(
            AuthenticationManager authenticationManager,
            JwtUtil jwtUtil,
            UserService userService,
            UserSessionService userSessionService, CacheManager cacheManager) {
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
        this.userService = userService;
        this.userSessionService = userSessionService;
        this.cacheManager = cacheManager;
    }

    public LoginResponse login(LoginRequest loginRequest) throws AuthenticationException {
        final var authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword())
        );
        final var user = (User) authentication.getPrincipal();
        final var refreshToken = jwtUtil.generateToken(user, JwtUtil.JwtType.REFRESH);
        final var sessionId = jwtUtil.extractSessionId(refreshToken);
        userSessionService.createSession(sessionId, user);
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
        userSessionService.invalidateSession(sessionId);
    }

    public void initiatePasswordReset(ForgotPasswordRequest dto) {
        userService.existsActivelyByEmail(dto.getEmail());
        // Create reset password token
        final var rawToken = generateRawToken();
        final var hashedToken = hmacHash(rawToken);
        // Save the token (cache)
        log.info("Generated reset password token {} for email: {}", rawToken, dto.getEmail());
        final var cache = cacheManager.getCache(RESET_TOKEN_CACHE);
        if (cache != null) {
            cache.put(hashedToken, dto.getEmail());
        }
        // Send email with reset password link (later)
    }

    private String generateRawToken() {
        return UUID.randomUUID().toString();
    }

    private String hmacHash(String rawToken) {
        try {
            String HMAC_ALGORITHM = "HmacSHA256";
            Mac mac = Mac.getInstance(HMAC_ALGORITHM);
            SecretKeySpec keySpec = new SecretKeySpec(SECRET_KEY.getBytes(StandardCharsets.UTF_8), HMAC_ALGORITHM);
            mac.init(keySpec);
            byte[] rawHmac = mac.doFinal(rawToken.getBytes(StandardCharsets.UTF_8));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(rawHmac);
        } catch (Exception e) {
            throw new RuntimeException("Failed to calculate HMAC hash", e);
        }
    }

    public String validatePasswordResetToken(String token) {
        return getEmailFromToken(token);
    }


    private String getEmailFromToken(String token) {
        final var cache = cacheManager.getCache(RESET_TOKEN_CACHE);
        if (cache != null) {
            final var hashedToken = hmacHash(token);
            Cache.ValueWrapper wrapper = cache.get(hashedToken);
            if (wrapper != null) {
                return (String) wrapper.get();
            }
        }
        throw new ValidationException("Invalid or expired reset password token");
    }

    @Transactional
    public void resetPassword(String resetToken, String newPassword) {
        final var email = getEmailFromToken(resetToken);
        userService.resetPasswordAndInvalidateSessions(email, newPassword);
        final var cache = cacheManager.getCache(RESET_TOKEN_CACHE);
        if (cache != null) {
            cache.evict(hmacHash(resetToken));
        }
    }
}
