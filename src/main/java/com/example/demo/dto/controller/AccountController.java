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
import org.springframework.http.HttpStatus;


import jakarta.validation.Valid;
import com.example.demo.dto.ApiResponse;

import org.springframework.validation.BindingResult;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Collectors;

import com.example.demo.exception.UnauthorizedException;
import com.example.demo.exception.ResourceNotFoundException;


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
    public ResponseEntity<ApiResponse<AccountResponse>> createAccount(
        @RequestHeader("Authorization") String authHeader,
        @Valid @RequestBody AccountRequest request,
        BindingResult bindingResult) {

        if (bindingResult.hasErrors()) {
            String errors = bindingResult.getAllErrors().stream()
                    .map(e -> e.getDefaultMessage())
                    .collect(Collectors.joining("; "));
            //return ResponseEntity.badRequest().body(new ApiResponse<>(false, null, errors));  normal string error 
            throw new IllegalArgumentException(errors); //now use global handlers catch this error instead of ^^^^
        }   

        Optional<User> userOpt = getCurrentUser(authHeader);
        if (userOpt.isEmpty()) {
            //return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ApiResponse<>(false, null, "Unauthorized: Invalid token or user."));
            throw new UnauthorizedException("Unauthorized: invalid token/user..");

        }

        Account acc = new Account();
        acc.setAccountType(request.getAccountType());
        acc.setBalance(request.getInitialBalance());
        acc.setUser(userOpt.get());

        // generate and assign unique account number...
        String accNum = generateAccountNumber();
        while (accountRepository.existsByAccountNumber(accNum)) {
            accNum = generateAccountNumber();
        }
        acc.setAccountNumber(accNum);

        Account saved = accountRepository.save(acc);

        return ResponseEntity.ok(
            new ApiResponse<>(true,
                new AccountResponse(
                    saved.getId(),
                    saved.getAccountType(),
                    saved.getBalance(),
                    saved.getAccountNumber()
                ),
                "Account created successfully!!!"
            )
        );
    }

    //generates a random 10-digit account number....
    private String generateAccountNumber() {
        long number = (long)(Math.random() * 1_000_000_0000L);
        return String.format("%010d", number);
    }

    @GetMapping("/my")
    public ResponseEntity<?> getMyAccounts(@RequestHeader("Authorization") String authHeader) {
        Optional<User> userOpt = getCurrentUser(authHeader);
        if (userOpt.isEmpty()) {
            //return ResponseEntity.status(401).body("Unauthorized");
            throw new UnauthorizedException("Unauthorized! ");
        }
        List<Account> accounts = accountRepository.findByUser(userOpt.get());
        List<AccountResponse> responses = accounts.stream()// FIXED: Pass all 4 args....
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
            //return ResponseEntity.status(401).body("Unauthorized");
            throw new UnauthorizedException("Unauthorized! ");
        }
        Optional<Account> accOpt = accountRepository.findById(id);
        if (accOpt.isEmpty() || !accOpt.get().getUser().getId().equals(userOpt.get().getId())) {
            //return ResponseEntity.status(404).body("Account not found or access denied");
            throw new ResourceNotFoundException("Account not found or your access is being DENIED..");
        }
        Account acc = accOpt.get();
        return ResponseEntity.ok(new AccountResponse(// FIXED: Pass all 4 args
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
            //return ResponseEntity.badRequest().body(errors);
            throw new IllegalArgumentException(errors); 
        }

        Optional<User> userOpt = getCurrentUser(authHeader);
        if (userOpt.isEmpty()) {
           // return ResponseEntity.status(401).body("Unauthorized");
           throw new UnauthorizedException("Unauthorized! ");
        }
        Optional<Account> accOpt = accountRepository.findById(id);
        if (accOpt.isEmpty() || !accOpt.get().getUser().getId().equals(userOpt.get().getId())) {
            //return ResponseEntity.status(404).body("Account not found or access denied");
            throw new ResourceNotFoundException("Account not found or your access is neing DENIED");
        }
        Account acc = accOpt.get();
        acc.setAccountType(request.getAccountType());
        acc.setBalance(request.getInitialBalance());
        Account saved = accountRepository.save(acc);
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
            //return ResponseEntity.status(401).body("Unauthorized");
            throw new UnauthorizedException("Unauthorized! ");
        }
        Optional<Account> accOpt = accountRepository.findById(id);
        if (accOpt.isEmpty() || !accOpt.get().getUser().getId().equals(userOpt.get().getId())) {
            //return ResponseEntity.status(404).body("Account not found or access denied");
            throw new ResourceNotFoundException("Account not found or your access is being denied");
        }
        accountRepository.deleteById(id);
        return ResponseEntity.ok("Account deleted successfully");
    }

    @GetMapping("/test")
    public ResponseEntity<?> test(@RequestHeader("Authorization") String authHeader) {
        return ResponseEntity.ok("Authenticated! Token: " + authHeader);
    }
}
