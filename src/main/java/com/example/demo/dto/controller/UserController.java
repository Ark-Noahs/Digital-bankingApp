package com.example.demo.controller;

import com.example.demo.model.User;
import com.example.demo.repository.UserRepository;
import com.example.demo.dto.LoginRequest;
import com.example.demo.dto.UserResponse;
import com.example.demo.dto.ApiResponse;
import com.example.demo.security.JwtUtil;
import com.example.demo.exception.UnauthorizedException;
import com.example.demo.exception.ResourceNotFoundException;

import org.springframework.beans.factory.annotation.Autowired; 
import org.springframework.web.bind.annotation.*;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.http.ResponseEntity;

import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import com.example.demo.exception.UnauthorizedException;
import com.example.demo.exception.ResourceNotFoundException;


@RestController 
@RequestMapping("/api/users")
public class UserController {
    @Autowired 
    private UserRepository userRepository;
    @Autowired 
    private BCryptPasswordEncoder passwordEncoder;
    @Autowired 
    private JwtUtil jwtUtil;

    //password validation: at least 8 chars, 1 uppercase, 1 special character......
    private boolean isValidPassword(String password) {
        return password != null &&
               password.length() >= 8 &&
               password.matches(".*[A-Z].*") &&        // uppercase char
               password.matches(".*[^a-zA-Z0-9].*");   // special char
    }

    //user registration......
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody User user){
        if (!isValidPassword(user.getPassword())) {
            throw new IllegalArgumentException("Password must be at least 8 characters long, have an uppercase letter, and one special character.");
        }
        if (userRepository.findByEmail(user.getEmail()).isPresent()) {
            throw new IllegalArgumentException("Email already exists.");
        }
        if (userRepository.findByUsername(user.getUsername()) !=null ) {
            throw new IllegalArgumentException("Username already exists.");
        }
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        User savedUser = userRepository.save(user);
        UserResponse userResponse = new UserResponse(savedUser.getId(), savedUser.getUsername(), savedUser.getEmail());
        return ResponseEntity.ok(new ApiResponse<>(true, userResponse, "User registered successfully"));
    }

    //get user by ID....
    @GetMapping("/{id}")
    public ResponseEntity<?> getUserById(@PathVariable Long id){
        User user = userRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("User not found"));
        UserResponse userResponse = new UserResponse(user.getId(), user.getUsername(), user.getEmail());
        return ResponseEntity.ok(new ApiResponse<>(true, userResponse, "User found"));
    }

    //update user by ID.....
    @PutMapping("/{id}")
    public ResponseEntity<?> updateUser(@PathVariable Long id, @RequestBody User updatedUser){
        User user = userRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("User not found"));
        user.setUsername(updatedUser.getUsername());
        user.setEmail(updatedUser.getEmail());
        user.setPassword(passwordEncoder.encode(updatedUser.getPassword()));
        User saved = userRepository.save(user);
        UserResponse userResponse = new UserResponse(saved.getId(), saved.getUsername(), saved.getEmail());
        return ResponseEntity.ok(new ApiResponse<>(true, userResponse, "User updated successfully"));
    }

    //delete user by ID/....
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable Long id){
        if (!userRepository.existsById(id)) {
            throw new ResourceNotFoundException("User not found");
        }
        userRepository.deleteById(id);
        return ResponseEntity.ok(new ApiResponse<>(true, null, "User deleted successfully"));
    }

    //login endpoint,....
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
                HashMap<String, Object> data = new HashMap<>();
                data.put("user", userResponse);
                data.put("token", token);
                return ResponseEntity.ok(new ApiResponse<>(true, data, "Login successful"));
            }
        }
        throw new UnauthorizedException("Invalid login credentials");
    }

    //get current user info via JWT.....
    @GetMapping("/me")
    public ResponseEntity<?> getMe(@RequestHeader("Authorization") String authHeader) {
        String token = authHeader.startsWith("Bearer ") ? authHeader.substring(7) : authHeader;
        if (!jwtUtil.validateToken(token)) {
            throw new UnauthorizedException("Invalid JWT");
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
        throw new ResourceNotFoundException("User not found");
    }
}
