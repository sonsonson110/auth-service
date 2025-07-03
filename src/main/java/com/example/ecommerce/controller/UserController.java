package com.example.ecommerce.controller;

import com.example.ecommerce.dto.ApiResponse;
import com.example.ecommerce.dto.RegisterRequest;
import com.example.ecommerce.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<?>> createUser(@RequestBody @Valid RegisterRequest dto) {
        userService.createUser(dto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("User registered successfully"));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<?>> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiResponse.success("User deleted successfully"));
    }
}
