package com.example.demo.dto;

public class AccountResponse {
    private Long id;
    private String accountType;
    private Double balance;

    public AccountResponse(Long id, String accountType, Double balance) {
        this.id = id;
        this.accountType = accountType;
        this.balance = balance;
    }

    // Getters and setters
    public Long getId() { return id; }
    public String getAccountType() { return accountType; }
    public Double getBalance() { return balance; }
    public void setId(Long id) { this.id = id; }
    public void setAccountType(String accountType) { this.accountType = accountType; }
    public void setBalance(Double balance) { this.balance = balance; }
}



