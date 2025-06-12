package com.example.demo.controller;

import com.example.demo.model.User;
import com.example.demo.repository.UserRepository;
import com.example.demo.dto.LoginRequest;
import com.example.demo.dto.UserResponse;
import org.springframework.beans.factory.annotation.Autowired; 
import org.springframework.web.bind.annotation.*;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;

import java.util.List;
import java.util.Optional;
import com.example.demo.security.JwtUtil;

import com.example.demo.dto.ApiResponse;


@RestController 
@RequestMapping("/api/users")
public class UserController {
    @Autowired 
    private UserRepository userRepository;
    @Autowired 
    private BCryptPasswordEncoder passwordEncoder;
    @Autowired 
    private JwtUtil jwtUtil;

    // Password validation: at least 8 chars, 1 uppercase, 1 special character
    private boolean isValidPassword(String password) {
        return password != null &&
               password.length() >= 8 &&
               password.matches(".*[A-Z].*") &&        // uppercase char
               password.matches(".*[^a-zA-Z0-9].*");   // special char
    }

    // User registration
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody User user){
        if (!isValidPassword(user.getPassword())) {
            return ResponseEntity
                    .badRequest()
                    .body("password must be at least 8 characters long, have an uppercase letter, and one special character.");
        }
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        User savedUser = userRepository.save(user);
        return ResponseEntity.ok(savedUser);
    }

    // Get user by ID
    @GetMapping("/{id}")
    public User getUserById(@PathVariable Long id){
        return userRepository.findById(id).orElse(null);
    }

    // Update user by ID
    @PutMapping("/{id}")
    public UserResponse updateUser(@PathVariable Long id, @RequestBody User updatedUser){
        return userRepository.findById(id).map(user -> {
            user.setUsername(updatedUser.getUsername());
            user.setEmail(updatedUser.getEmail());
            user.setPassword(passwordEncoder.encode(updatedUser.getPassword()));
            User saved = userRepository.save(user);
            return new UserResponse(saved.getId(), saved.getUsername(), saved.getEmail());
        }).orElse(null);
    }

    // Delete user by ID
    @DeleteMapping("/{id}")
    public void deleteUser(@PathVariable Long id){
        userRepository.deleteById(id);
    }

    // Login endpoint
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request){
        Optional<User> userOpt = userRepository.findByEmail(request.getEmail());
        if (userOpt.isPresent()){
            User user = userOpt.get();
            if(passwordEncoder.matches(request.getPassword(), user.getPassword())){
                String token = jwtUtil.generateToken(user.getEmail());
                UserResponse userResponse = new UserResponse(
                    user.getId(),
                    user.getUsername(),
                    user.getEmail()
                );
                return ResponseEntity.ok(
                    new java.util.HashMap<String, Object>() {{
                        put("user", userResponse);
                        put("token", token);
                    }}
                );
            }
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid login credentials");
    }

    // Get current user info via JWT
    @GetMapping("/me")
    public ResponseEntity<?> getMe(@RequestHeader("Authorization") String authHeader) {
        String token = authHeader.startsWith("Bearer ") ? authHeader.substring(7) : authHeader;
        if (!jwtUtil.validateToken(token)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid JWT");
        }

        String email = jwtUtil.extractUsername(token);
        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            UserResponse userResponse = new UserResponse(user.getId(), user.getUsername(), user.getEmail());
            return ResponseEntity.ok(
                new ApiResponse<>(true, userResponse, "User info fetched successfully")
            );
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(new ApiResponse<>(false, null, "User not found"));

        }
}
