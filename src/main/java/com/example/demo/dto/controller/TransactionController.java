package com.example.demo.controller;

import com.example.demo.dto.TransactionRequest;
import com.example.demo.dto.TransactionResponse;
import com.example.demo.model.Account;
import com.example.demo.model.Transaction;
import com.example.demo.model.User;
import com.example.demo.repository.AccountRepository;
import com.example.demo.repository.TransactionRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.security.JwtUtil;
import com.example.demo.dto.ApiResponse;
import com.example.demo.exception.UnauthorizedException;
import com.example.demo.exception.ResourceNotFoundException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Page;

import jakarta.validation.Valid;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.example.demo.exception.UnauthorizedException;
import com.example.demo.exception.ResourceNotFoundException;


@RestController
@RequestMapping("/api/transactions")
public class TransactionController {

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtUtil jwtUtil;

    //helper to get current user from JWT.....
    private Optional<User> getCurrentUser(String authHeader) {
        String token = authHeader.startsWith("Bearer ") ? authHeader.substring(7) : authHeader;
        String email = jwtUtil.extractUsername(token);
        return userRepository.findByEmail(email);
    }

    //    DEPOSIT
    @PostMapping("/deposit/{accountId}")
    public ResponseEntity<?> deposit(@RequestHeader("Authorization") String authHeader,
                                     @PathVariable Long accountId,
                                     @Valid @RequestBody TransactionRequest request) {
        Optional<User> userOpt = getCurrentUser(authHeader);
        if (userOpt.isEmpty()) throw new UnauthorizedException("Unauthorized");

        Optional<Account> accOpt = accountRepository.findById(accountId);
        if (accOpt.isEmpty() || !accOpt.get().getUser().getId().equals(userOpt.get().getId()))
            throw new ResourceNotFoundException("Account not found");

        if (request.getAmount() == null || request.getAmount() <= 0)
            throw new IllegalArgumentException("Amount must be positive");

        Account acc = accOpt.get();
        acc.setBalance(acc.getBalance() + request.getAmount());
        accountRepository.save(acc);

        Transaction txn = new Transaction();
        txn.setAccount(acc);
        txn.setType("DEPOSIT");
        txn.setAmount(request.getAmount());
        txn.setDescription(request.getDescription());
        txn.setTimestamp(LocalDateTime.now());
        txn.setToAccount(null);

        Transaction saved = transactionRepository.save(txn);

        return ResponseEntity.ok(
            new ApiResponse<>(true,
                new TransactionResponse(saved.getId(), saved.getType(), saved.getAmount(),
                    saved.getDescription(), saved.getTimestamp(), null),
                "Deposit successful")
        );
    }

    // WITHDRAW
    @PostMapping("/withdraw/{accountId}")
    public ResponseEntity<?> withdraw(@RequestHeader("Authorization") String authHeader,
                                      @PathVariable Long accountId,
                                      @Valid @RequestBody TransactionRequest request) {
        Optional<User> userOpt = getCurrentUser(authHeader);
        if (userOpt.isEmpty()) throw new UnauthorizedException("Unauthorized");

        Optional<Account> accOpt = accountRepository.findById(accountId);
        if (accOpt.isEmpty() || !accOpt.get().getUser().getId().equals(userOpt.get().getId()))
            throw new ResourceNotFoundException("Account not found");

        if (request.getAmount() == null || request.getAmount() <= 0)
            throw new IllegalArgumentException("Amount must be positive");

        Account acc = accOpt.get();
        if (acc.getBalance() < request.getAmount())
            throw new IllegalArgumentException("Insufficient funds");

        acc.setBalance(acc.getBalance() - request.getAmount());
        accountRepository.save(acc);

        Transaction txn = new Transaction();
        txn.setAccount(acc);
        txn.setType("WITHDRAWAL");
        txn.setAmount(request.getAmount());
        txn.setDescription(request.getDescription());
        txn.setTimestamp(LocalDateTime.now());
        txn.setToAccount(null);

        Transaction saved = transactionRepository.save(txn);

        return ResponseEntity.ok(
            new ApiResponse<>(true,
                new TransactionResponse(saved.getId(), saved.getType(), saved.getAmount(),
                    saved.getDescription(), saved.getTimestamp(), null),
                "Withdrawal successful")
        );
    }

    //      TRANSFER
    @PostMapping("/transfer/{accountId}")
    public ResponseEntity<?> transfer(@RequestHeader("Authorization") String authHeader,
                                      @PathVariable Long accountId,
                                      @Valid @RequestBody TransactionRequest request) {
        Optional<User> userOpt = getCurrentUser(authHeader);
        if (userOpt.isEmpty()) throw new UnauthorizedException("Unauthorized");

        Optional<Account> fromAccOpt = accountRepository.findById(accountId);
        if (fromAccOpt.isEmpty() || !fromAccOpt.get().getUser().getId().equals(userOpt.get().getId()))
            throw new ResourceNotFoundException("Source account not found");

        if (request.getAmount() == null || request.getAmount() <= 0)
            throw new IllegalArgumentException("Amount must be positive");

        if (request.getToAccountId() == null)
            throw new IllegalArgumentException("Missing destination account ID");

        if (accountId.equals(request.getToAccountId()))
            throw new IllegalArgumentException("Cannot transfer to the same account");

        Optional<Account> toAccOpt = accountRepository.findById(request.getToAccountId());
        if (toAccOpt.isEmpty()) throw new ResourceNotFoundException("Destination account not found");

        Account fromAcc = fromAccOpt.get();
        Account toAcc = toAccOpt.get();

        if (fromAcc.getBalance() < request.getAmount())
            throw new IllegalArgumentException("Insufficient funds");

        //subtract from sender, add to receiver
        fromAcc.setBalance(fromAcc.getBalance() - request.getAmount());
        toAcc.setBalance(toAcc.getBalance() + request.getAmount());
        accountRepository.save(fromAcc);
        accountRepository.save(toAcc);

        //log the transaction
        Transaction txn = new Transaction();
        txn.setAccount(fromAcc);
        txn.setType("TRANSFER");
        txn.setAmount(request.getAmount());
        txn.setDescription(request.getDescription());
        txn.setTimestamp(LocalDateTime.now());
        txn.setToAccount(toAcc);

        Transaction saved = transactionRepository.save(txn);

        return ResponseEntity.ok(
            new ApiResponse<>(true,
                new TransactionResponse(saved.getId(), saved.getType(), saved.getAmount(),
                    saved.getDescription(), saved.getTimestamp(), toAcc.getId()),
                "Transfer successful")
        );
    }

    //get All transactions for an account(ttransaction history)
    @GetMapping("/history/{accountId}")
    public ResponseEntity<?> getHistory(
        @RequestHeader("Authorization") String authHeader,
        @PathVariable Long accountId,
        @RequestParam(required = false) String type,
        @RequestParam(required = false) String fromDate,
        @RequestParam(required = false) String toDate,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size
    ) {
        Optional<User> userOpt = getCurrentUser(authHeader);
        if (userOpt.isEmpty()) throw new UnauthorizedException("Unauthorized");

        Optional<Account> accOpt = accountRepository.findById(accountId);
        if (accOpt.isEmpty() || !accOpt.get().getUser().getId().equals(userOpt.get().getId()))
            throw new ResourceNotFoundException("Account not found");

        // debug logg...
        // System.out.println("DEBUG: User ID making request: " + userOpt.get().getId());
        // System.out.println("DEBUG: Account ID: " + accountId + ", Account's User ID: " + accOpt.get().getUser().getId());

        //returns ALL transactions (no date/type filter)....
        List<Transaction> txnsList = transactionRepository.findByAccountId(accountId);
        Page<Transaction> txns = new org.springframework.data.domain.PageImpl<>(txnsList);

        List<TransactionResponse> responses = txns.getContent().stream()
                .map(txn -> new TransactionResponse(
                    txn.getId(), txn.getType(), txn.getAmount(),
                    txn.getDescription(), txn.getTimestamp(),
                    txn.getToAccount() != null ? txn.getToAccount().getId() : null))
                .collect(Collectors.toList());

        // Create a response object for paging, then wrap in ApiResponse
        java.util.HashMap<String, Object> result = new java.util.HashMap<>();
        result.put("content", responses);
        result.put("page", page);
        result.put("size", size);
        result.put("count", txns.getTotalElements());

        return ResponseEntity.ok(
            new ApiResponse<>(true, result, "Transaction history fetched successfully")
        );
    }

}
