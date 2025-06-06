package com.example.demo.repository;

import com.example.demo.model.Account;
import com.example.demo.model.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Page;
import java.time.LocalDateTime;
import java.util.List;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    Page<Transaction> findByAccount(Account account, Pageable pageable);

    Page<Transaction> findByAccountAndTypeAndTimestampBetween(
        Account account, String type, LocalDateTime from, LocalDateTime to, Pageable pageable);

    Page<Transaction> findByAccountAndTimestampBetween(
        Account account, LocalDateTime from, LocalDateTime to, Pageable pageable);

    Page<Transaction> findByAccountId(Long accountId, Pageable pageable);

    // ---- ADD THESE TWO BELOW ----
    Page<Transaction> findByAccountIdAndTypeAndTimestampBetween(
        Long accountId, String type, LocalDateTime from, LocalDateTime to, Pageable pageable);

    Page<Transaction> findByAccountIdAndTimestampBetween(
        Long accountId, LocalDateTime from, LocalDateTime to, Pageable pageable);

    List<Transaction> findByAccountId(Long accountId);


    }
