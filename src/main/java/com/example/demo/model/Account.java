package com.example.demo.model;

import jakarta.persistence.*;

@Entity
@Table(name = "accounts")
public class Account {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String type; //checking or savings

    @Column(nullable = false)
    private Double balance = 0.0;

    @Column(nullable = false)
    private String accountType;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // Getters & Setters
    public Long getId() { return id; }
    public void setId(Long id)  {this.id = id; }

    public String getType() { return type; }
    public void setType(String type) {this.type = type; }

    public Double getBalance() { return balance; }
    public void setBalance(Double balance) { this.balance = balance; }

    public User getUser() { return user; }
    public void setUser(User user) {this.user = user; }

    public String getAccountType() { return accountType; }
    public void setAccountType(String accountType)  {this.accountType = accountType; }




}
