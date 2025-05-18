package com.localapp.controller;

import com.localapp.model.User;
import com.localapp.repository.UserRepository;
import com.localapp.util.JwtUtil;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    @PostMapping("/register")
    public ResponseEntity<String> register(@Valid @RequestBody User user) {
        logger.info("Registering user: username={}, displayName={}, bio={}",
                user.getUsername(), user.getDisplayName(), user.getBio());
        user.setUserId(UUID.randomUUID().toString()); // Generate unique userId
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        userRepository.save(user);
        return ResponseEntity.ok("User registered");
    }

    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody User loginUser) {
        logger.info("Processing login request for username: {}", loginUser.getUsername());
        User user = userRepository.findByUsername(loginUser.getUsername());
        if (user != null && passwordEncoder.matches(loginUser.getPassword(), user.getPassword())) {
            String token = jwtUtil.generateToken(user.getUserId());
            logger.info("User logged in: {}", user.getUserId());
            return ResponseEntity.ok(token);
        }
        logger.warn("Login failed for username: {}", loginUser.getUsername());
        return ResponseEntity.status(401).body("Invalid credentials");
    }
}