
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

@RestController 
@RequestMapping("/api/users")
public class UserController {
    @Autowired 
    private UserRepository userRepository;
    @Autowired 
    private BCryptPasswordEncoder passwordEncoder;
    @Autowired 
    private JwtUtil jwtUtil;

    @PostMapping("/register")
    public UserResponse register(@RequestBody User user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        User saved = userRepository.save(user);
        return new UserResponse(saved.getId(), saved.getUsername(), saved.getEmail());
    }

    @GetMapping("/{id}")  //get user by their ID
    public User getUserById(@PathVariable Long id){
        return userRepository.findById(id).orElse(null);
    
    }

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

    @DeleteMapping("/{id}")   //delete a user by their id 
    public void deleteUser(@PathVariable Long id){
        userRepository.deleteById(id);
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request){
        Optional<User> userOpt = userRepository.findByEmail(request.getEmail());
        if (userOpt.isPresent()){
            User user = userOpt.get();
            if(passwordEncoder.matches(request.getPassword(), user.getPassword())){
                String token = jwtUtil.generateToken(user.getEmail()); // <-- Only email!
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

    @GetMapping("/me")
    public ResponseEntity<?> getMe(@RequestHeader("Authorization") String authHeader) {
        // Extract token
        String token = authHeader.startsWith("Bearer ") ? authHeader.substring(7) : authHeader;

        // Validate and parse
        if (!jwtUtil.validateToken(token)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid JWT");
        }

        String email = jwtUtil.extractUsername(token);
        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            UserResponse userResponse = new UserResponse(user.getId(), user.getUsername(), user.getEmail());
            return ResponseEntity.ok(userResponse);
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
    }



}




