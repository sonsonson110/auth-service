package com.example.ecommerce.service;


import com.example.ecommerce.dto.RegisterRequest;
import com.example.ecommerce.entity.User;
import com.example.ecommerce.exception.DuplicateResourceException;
import com.example.ecommerce.exception.ResourceNotFoundException;
import com.example.ecommerce.exception.ValidationException;
import com.example.ecommerce.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService implements UserDetailsService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserSessionService userSessionService;

    public UserService(UserRepository userRepository, UserSessionService userSessionService) {
        this.userRepository = userRepository;
        this.userSessionService = userSessionService;
        this.passwordEncoder = new BCryptPasswordEncoder();
    }

    //<editor-fold desc="Validation">
    public void existsById(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User", userId.toString());
        }
    }

    public void existsActivelyByEmail(String email) {
        if (!userRepository.existsByEmailAndEnabledTrue(email)) {
            throw new ResourceNotFoundException("User", email);
        }
    }

    //</editor-fold>
    @Override
    public User loadUserByUsername(String username) throws UsernameNotFoundException {
        final var user = userRepository.findByUsername(username).orElseThrow(() -> new ResourceNotFoundException("User", username));
        if (!user.isEnabled()) throw new ValidationException("User is not enabled");
        return user;
    }

    public User getActiveUserByEmail(String email) {
        return userRepository.findByEmailAndEnabledTrue(email).orElseThrow(() -> new ResourceNotFoundException("User", email));
    }

    public void createUser(RegisterRequest dto) {
        //<editor-fold desc="Validation">
        final var existedByUsername = userRepository.existsByUsername(dto.getUsername());
        if (existedByUsername) {
            throw new DuplicateResourceException("User", "username", dto.getUsername());
        }
        final var existedByEmail = userRepository.existsByEmail(dto.getEmail());
        if (existedByEmail) {
            throw new DuplicateResourceException("User", "email", dto.getEmail());
        }
        //</editor-fold>
        final var user = new User();
        user.setUsername(dto.getUsername().trim());
        user.setEmail(dto.getEmail().trim().toLowerCase());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setFirstName(dto.getFirstName().trim());
        user.setLastName(dto.getLastName().trim());

        userRepository.save(user);
    }

    public void deleteUser(Long userId) {
        existsById(userId);
        userRepository.deleteById(userId);
    }

    @Transactional(rollbackFor = Exception.class)
    public void resetPasswordAndInvalidateSessions(String email, String newPassword) {
        var user = getActiveUserByEmail(email);
        user.setPassword(passwordEncoder.encode(newPassword));
        user = userRepository.save(user);
        userSessionService.revokeAllUserSessions(user.getId());
    }
}
