package com.example.auth_service.service;


import com.example.auth_service.dto.RegisterRequest;
import com.example.auth_service.entity.User;
import com.example.auth_service.exception.DuplicateResourceException;
import com.example.auth_service.exception.ResourceNotFoundException;
import com.example.auth_service.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService implements UserDetailsService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = new BCryptPasswordEncoder();
    }

    public User getUserById(Long id) {
        return userRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("User", id.toString()));
    }

    @Override
    public User loadUserByUsername(String username) throws UsernameNotFoundException {
        final var user = userRepository.findByUsername(username);
        if (user.isEmpty()) {
            throw new UsernameNotFoundException("User is not found with username " + username);
        }
        return user.get();
    }

    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email).orElseThrow(() -> new ResourceNotFoundException("User", email));
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
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User", userId.toString());
        }
        userRepository.deleteById(userId);
    }
}
