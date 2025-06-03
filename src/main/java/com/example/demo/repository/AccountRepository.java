
//this file lets us save,find and delete account objects


package com.example.demo.repository;

import com.example.demo.model.Account;
import com.example.demo.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AccountRepository extends JpaRepository<Account, Long> {
    // Find all accounts belonging to a user
    List<Account> findByUser(User user);
}
