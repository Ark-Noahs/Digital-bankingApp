package com.example.demo.model;

import jakarta.persistence.*;

@Entity
@Table(name = "accounts")
public class Account {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String accountType;

    @Column(nullable = false)
    private Double balance = 0.0;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // Default constructor required by JPA
    public Account() {}

    // Getters & Setters
    public Long getId() { return id; }
    public void setId(Long id)  {this.id = id; }

    public String getAccountType() { return accountType; }
    public void setAccountType(String accountType)  {this.accountType = accountType; }

    public Double getBalance() { return balance; }
    public void setBalance(Double balance) { this.balance = balance; }

    public User getUser() { return user; }
    public void setUser(User user) {this.user = user; }

    // --- equals and hashCode using id ---
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Account account = (Account) o;
        return id != null && id.equals(account.id);
    }

    @Override
    public int hashCode() {
        return 31;
    }
}
