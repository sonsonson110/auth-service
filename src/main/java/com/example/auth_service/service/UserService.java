package com.example.auth_service.service;


import com.example.auth_service.dto.RegisterRequest;
import com.example.auth_service.entity.User;
import com.example.auth_service.exception.DuplicateResourceException;
import com.example.auth_service.repository.UserRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {
    private final UserRepository userRepository;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public void createUser(RegisterRequest dto) {
        //<editor-fold desc="Validation">
        final var existedByUsername = userRepository.existsByUsername(dto.getUsername());
        if (existedByUsername) {
            throw new DuplicateResourceException("User",  "username", dto.getUsername());
        }
        final var existedByEmail = userRepository.existsByEmail(dto.getEmail());
        if (existedByEmail) {
            throw new DuplicateResourceException("User",  "email", dto.getEmail());
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
}
