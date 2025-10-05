package com.localapp.controller;

import com.localapp.model.entity.User;
import com.localapp.model.dto.UserProfileDTO;
import com.localapp.repository.UserRepository;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class UserController {
    private final UserRepository userRepository;

    public UserController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @PutMapping("/profile")
    public ResponseEntity<String> updateProfile(@Valid @RequestBody UserProfileDTO profileDTO,
                                                @AuthenticationPrincipal String userId) {
        userRepository.updateProfile(userId, profileDTO.getDisplayName(), profileDTO.getBio());
        return ResponseEntity.ok("Profile updated");
    }

    @GetMapping("/{userId}")
    public ResponseEntity<UserProfileDTO> getProfile(@PathVariable String userId) {
        User user = userRepository.findByUserId(userId);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
        UserProfileDTO profileDTO = new UserProfileDTO();
        profileDTO.setDisplayName(user.getDisplayName());
        profileDTO.setBio(user.getBio());
        return ResponseEntity.ok(profileDTO);
    }
}