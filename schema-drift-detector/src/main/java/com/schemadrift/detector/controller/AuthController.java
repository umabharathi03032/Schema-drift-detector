package com.schemadrift.detector.controller;

import com.schemadrift.detector.dto.AuthResponse;
import com.schemadrift.detector.dto.LoginRequest;
import com.schemadrift.detector.dto.RegisterRequest;
import com.schemadrift.detector.model.User;
import com.schemadrift.detector.service.JwtService;
import com.schemadrift.detector.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService userService;
    private final JwtService jwtService;

    public AuthController(UserService userService, JwtService jwtService) {
        this.userService = userService;
        this.jwtService = jwtService;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest request) {
        try {
            User user = userService.register(request.getUsername(), request.getEmail(), request.getPassword());
            String token = jwtService.generateToken(user.getId(), user.getUsername());
            return ResponseEntity.ok(new AuthResponse(token, user.getUsername(), user.getId()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request) {
        return userService.authenticate(request.getUsername(), request.getPassword())
                .map(user -> {
                    String token = jwtService.generateToken(user.getId(), user.getUsername());
                    return ResponseEntity.ok(new AuthResponse(token, user.getUsername(), user.getId()));
                })
                .orElseGet(() -> ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid username or password"));
    }
}
