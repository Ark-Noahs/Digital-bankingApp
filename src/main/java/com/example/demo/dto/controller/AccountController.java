package com.example.demo.controller;

import com.example.demo.model.Account;
import com.example.demo.model.User;
import com.example.demo.repository.AccountRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.security.JwtUtil;
import com.example.demo.dto.AccountRequest;
import com.example.demo.dto.AccountResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import org.springframework.validation.BindingResult;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/accounts")
public class AccountController {

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtUtil jwtUtil;

    // Helper to get current user from JWT
    private Optional<User> getCurrentUser(String authHeader) {
        String token = authHeader.startsWith("Bearer ") ? authHeader.substring(7) : authHeader;
        String email = jwtUtil.extractUsername(token);
        return userRepository.findByEmail(email);
    }

    @PostMapping("/create")
    public ResponseEntity<?> createAccount(
        @RequestHeader("Authorization") String authHeader,
        @Valid @RequestBody AccountRequest request,
        BindingResult bindingResult) {

        if (bindingResult.hasErrors()) {
            String errors = bindingResult.getAllErrors().stream()
                    .map(e -> e.getDefaultMessage())
                    .collect(Collectors.joining("; "));
            return ResponseEntity.badRequest().body(errors);
        }

        Optional<User> userOpt = getCurrentUser(authHeader);
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(401).body("Unauthorized: Invalid token or user.");
        }

        Account acc = new Account();
        acc.setAccountType(request.getAccountType());
        acc.setBalance(request.getInitialBalance());
        acc.setUser(userOpt.get());

        // --- Generate and assign unique account number ---
        String accNum = generateAccountNumber();
        while (accountRepository.existsByAccountNumber(accNum)) {
            accNum = generateAccountNumber();
        }
        acc.setAccountNumber(accNum);

        Account saved = accountRepository.save(acc);

        // --- FIXED: Pass all 4 args
        return ResponseEntity.ok(new AccountResponse(
            saved.getId(),
            saved.getAccountType(),
            saved.getBalance(),
            saved.getAccountNumber()
        ));
    }

    // Generates a random 10-digit account number
    private String generateAccountNumber() {
        long number = (long)(Math.random() * 1_000_000_0000L);
        return String.format("%010d", number);
    }

    @GetMapping("/my")
    public ResponseEntity<?> getMyAccounts(@RequestHeader("Authorization") String authHeader) {
        Optional<User> userOpt = getCurrentUser(authHeader);
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(401).body("Unauthorized");
        }
        List<Account> accounts = accountRepository.findByUser(userOpt.get());
        // FIXED: Pass all 4 args
        List<AccountResponse> responses = accounts.stream()
                .map(acc -> new AccountResponse(
                        acc.getId(),
                        acc.getAccountType(),
                        acc.getBalance(),
                        acc.getAccountNumber()
                ))
                .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getAccountById(@PathVariable Long id, @RequestHeader("Authorization") String authHeader) {
        Optional<User> userOpt = getCurrentUser(authHeader);
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(401).body("Unauthorized");
        }
        Optional<Account> accOpt = accountRepository.findById(id);
        if (accOpt.isEmpty() || !accOpt.get().getUser().getId().equals(userOpt.get().getId())) {
            return ResponseEntity.status(404).body("Account not found or access denied");
        }
        Account acc = accOpt.get();
        // FIXED: Pass all 4 args
        return ResponseEntity.ok(new AccountResponse(
                acc.getId(),
                acc.getAccountType(),
                acc.getBalance(),
                acc.getAccountNumber()
        ));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateAccount (
        @PathVariable Long id,
        @RequestHeader("Authorization") String authHeader,
        @Valid @RequestBody AccountRequest request,
        BindingResult bindingResult) {

        if (bindingResult.hasErrors()) {
            String errors = bindingResult.getAllErrors().stream()
                    .map(e -> e.getDefaultMessage())
                    .collect(Collectors.joining("; "));
            return ResponseEntity.badRequest().body(errors);
        }

        Optional<User> userOpt = getCurrentUser(authHeader);
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(401).body("Unauthorized");
        }
        Optional<Account> accOpt = accountRepository.findById(id);
        if (accOpt.isEmpty() || !accOpt.get().getUser().getId().equals(userOpt.get().getId())) {
            return ResponseEntity.status(404).body("Account not found or access denied");
        }
        Account acc = accOpt.get();
        acc.setAccountType(request.getAccountType());
        acc.setBalance(request.getInitialBalance());
        Account saved = accountRepository.save(acc);
        // FIXED: Pass all 4 args
        return ResponseEntity.ok(new AccountResponse(
                saved.getId(),
                saved.getAccountType(),
                saved.getBalance(),
                saved.getAccountNumber()
        ));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteAccount(@PathVariable Long id, @RequestHeader("Authorization") String authHeader) {
        Optional<User> userOpt = getCurrentUser(authHeader);
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(401).body("Unauthorized");
        }
        Optional<Account> accOpt = accountRepository.findById(id);
        if (accOpt.isEmpty() || !accOpt.get().getUser().getId().equals(userOpt.get().getId())) {
            return ResponseEntity.status(404).body("Account not found or access denied");
        }
        accountRepository.deleteById(id);
        return ResponseEntity.ok("Account deleted successfully");
    }

    @GetMapping("/test")
    public ResponseEntity<?> test(@RequestHeader("Authorization") String authHeader) {
        return ResponseEntity.ok("Authenticated! Token: " + authHeader);
    }
}
