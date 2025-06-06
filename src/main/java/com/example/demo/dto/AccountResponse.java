package com.example.demo.dto;

public class AccountResponse {
    private Long id;
    private String accountType;
    private Double balance;
    private String accountNumber; 

    public AccountResponse(Long id, String accountType, Double balance, String accountNumber) {
        this.id = id;
        this.accountType = accountType;
        this.balance = balance;
        this.accountNumber = accountNumber; 
    }

    // Getters and setters
    public Long getId() { return id; }
    public String getAccountType() { return accountType; }
    public Double getBalance() { return balance; }
    public String getAccountNumber() { return accountNumber; } // <-- add this!
    public void setId(Long id) { this.id = id; }
    public void setAccountType(String accountType) { this.accountType = accountType; }
    public void setBalance(Double balance) { this.balance = balance; }
    public void setAccountNumber(String accountNumber) { this.accountNumber = accountNumber; }
}
