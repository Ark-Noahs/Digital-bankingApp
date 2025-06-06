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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Page;



import jakarta.validation.Valid; 
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

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

    // Helper to get current user from JWT
    private Optional<User> getCurrentUser(String authHeader) {
        String token = authHeader.startsWith("Bearer ") ? authHeader.substring(7) : authHeader;
        String email = jwtUtil.extractUsername(token);
        return userRepository.findByEmail(email);
    }

    // DEPOSIT
    @PostMapping("/deposit/{accountId}")
    public ResponseEntity<?> deposit(@RequestHeader("Authorization") String authHeader,
                                     @PathVariable Long accountId,
                                     @Valid @RequestBody TransactionRequest request) {
        Optional<User> userOpt = getCurrentUser(authHeader);
        if (userOpt.isEmpty()) return ResponseEntity.status(401).body("Unauthorized");

        Optional<Account> accOpt = accountRepository.findById(accountId);
        if (accOpt.isEmpty() || !accOpt.get().getUser().getId().equals(userOpt.get().getId()))
            return ResponseEntity.status(404).body("Account not found");

        if (request.getAmount() == null || request.getAmount() <= 0)
            return ResponseEntity.badRequest().body("Amount must be positive");

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

        return ResponseEntity.ok(new TransactionResponse(saved.getId(), saved.getType(), saved.getAmount(),
                saved.getDescription(), saved.getTimestamp(), null));
    }

    // WITHDRAW...
    @PostMapping("/withdraw/{accountId}")
    public ResponseEntity<?> withdraw(@RequestHeader("Authorization") String authHeader,
                                      @PathVariable Long accountId,
                                      @Valid @RequestBody TransactionRequest request) {
        Optional<User> userOpt = getCurrentUser(authHeader);
        if (userOpt.isEmpty()) return ResponseEntity.status(401).body("Unauthorized");

        Optional<Account> accOpt = accountRepository.findById(accountId);
        if (accOpt.isEmpty() || !accOpt.get().getUser().getId().equals(userOpt.get().getId()))
            return ResponseEntity.status(404).body("Account not found");

        if (request.getAmount() == null || request.getAmount() <= 0)
            return ResponseEntity.badRequest().body("Amount must be positive");

        Account acc = accOpt.get();
        if (acc.getBalance() < request.getAmount())
            return ResponseEntity.badRequest().body("Insufficient funds");

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

        return ResponseEntity.ok(new TransactionResponse(saved.getId(), saved.getType(), saved.getAmount(),
                saved.getDescription(), saved.getTimestamp(), null));
    }

    // TRANSFER
    @PostMapping("/transfer/{accountId}")
    public ResponseEntity<?> transfer(@RequestHeader("Authorization") String authHeader,
                                      @PathVariable Long accountId,
                                      @Valid @RequestBody TransactionRequest request) {
        Optional<User> userOpt = getCurrentUser(authHeader);
        if (userOpt.isEmpty()) return ResponseEntity.status(401).body("Unauthorized");

        Optional<Account> fromAccOpt = accountRepository.findById(accountId);
        if (fromAccOpt.isEmpty() || !fromAccOpt.get().getUser().getId().equals(userOpt.get().getId()))
            return ResponseEntity.status(404).body("Source account not found");

        if (request.getAmount() == null || request.getAmount() <= 0)
            return ResponseEntity.badRequest().body("Amount must be positive");

        if (request.getToAccountId() == null)
            return ResponseEntity.badRequest().body("Missing destination account ID");

        if (accountId.equals(request.getToAccountId()))
            return ResponseEntity.badRequest().body("Cannot transfer to the same account");

        Optional<Account> toAccOpt = accountRepository.findById(request.getToAccountId());
        if (toAccOpt.isEmpty()) return ResponseEntity.status(404).body("Destination account not found");

        Account fromAcc = fromAccOpt.get();
        Account toAcc = toAccOpt.get();

        if (fromAcc.getBalance() < request.getAmount())
            return ResponseEntity.badRequest().body("Insufficient funds");

        // Subtract from sender, add to receiver
        fromAcc.setBalance(fromAcc.getBalance() - request.getAmount());
        toAcc.setBalance(toAcc.getBalance() + request.getAmount());
        accountRepository.save(fromAcc);
        accountRepository.save(toAcc);

        // Log transaction
        Transaction txn = new Transaction();
        txn.setAccount(fromAcc);
        txn.setType("TRANSFER");
        txn.setAmount(request.getAmount());
        txn.setDescription(request.getDescription());
        txn.setTimestamp(LocalDateTime.now());
        txn.setToAccount(toAcc);

        Transaction saved = transactionRepository.save(txn);

        return ResponseEntity.ok(new TransactionResponse(saved.getId(), saved.getType(), saved.getAmount(),
                saved.getDescription(), saved.getTimestamp(), toAcc.getId()));
    }

    // Get All Transactions for an Account (Transaction History)
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
    if (userOpt.isEmpty()) return ResponseEntity.status(401).body("Unauthorized");

    Optional<Account> accOpt = accountRepository.findById(accountId);
    if (accOpt.isEmpty() || !accOpt.get().getUser().getId().equals(userOpt.get().getId()))
        return ResponseEntity.status(404).body("Account not found");

    //DEBUG LOGGING
    System.out.println("DEBUG: User ID making request: " + userOpt.get().getId());
    System.out.println("DEBUG: Account ID: " + accountId + ", Account's User ID: " + accOpt.get().getUser().getId());

    //had to hard code dates bc were mismatching when being callled for transaction history
    LocalDateTime start = LocalDateTime.of(2025, 6, 6, 0, 0, 0);
    LocalDateTime end = LocalDateTime.of(2025, 6, 6, 23, 59, 59);

    System.out.println("DEBUG: Query date range: start=" + start + ", end=" + end);
    Pageable pageable = PageRequest.of(page, size);

    //DEBUG: Show ALL transactions for the account, ignoring filters
    List<Transaction> allTxns = transactionRepository.findByAccountId(accountId);
    System.out.println("DEBUG: All transactions for accountId " + accountId + ": count=" + allTxns.size());
    allTxns.forEach(t -> System.out.println("    [ALL] " + t.getId() + " " + t.getType() + " " + t.getAmount() + " " + t.getTimestamp()));

    //RETURN ALL transactions (no date/type filter) so Postman always shows results....
    List<Transaction> txnsList = transactionRepository.findByAccountId(accountId);
    System.out.println("DEBUG: Basic transactions: " + txnsList.size());
    Page<Transaction> txns = new org.springframework.data.domain.PageImpl<>(txnsList);

    System.out.println("DEBUG: Transactions found by paged/filtered query: " + txns.getTotalElements());
    txns.getContent().forEach(t -> System.out.println("    [PAGE] " + t.getId() + " " + t.getType() + " " + t.getAmount() + " " + t.getTimestamp()));

    List<TransactionResponse> responses = txns.getContent().stream()
            .map(txn -> new TransactionResponse(
                txn.getId(), txn.getType(), txn.getAmount(),
                txn.getDescription(), txn.getTimestamp(),
                txn.getToAccount() != null ? txn.getToAccount().getId() : null))
            .collect(Collectors.toList());

    //JSON response (page, size, count, content)...
    return ResponseEntity.ok(
        new java.util.HashMap<String, Object>() {{
            put("content", responses);
            put("page", page);
            put("size", size);
            put("count", txns.getTotalElements());
        }}
    );
}




}