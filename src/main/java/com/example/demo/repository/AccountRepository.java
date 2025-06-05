package com.example.demo.repository;

import com.example.demo.model.Account;
import com.example.demo.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AccountRepository extends JpaRepository<Account, Long> {
    // This method finds all accounts for a specific user.
    List<Account> findByUser(User user);
}
