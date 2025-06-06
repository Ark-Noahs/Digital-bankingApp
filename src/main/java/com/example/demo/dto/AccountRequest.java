package com.example.demo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size; //limit string length

public class AccountRequest {

    @NotBlank(message = "Account type is required") // Not null/empty/blank
    // @Size(max = 20, message = "Account type must be 20 characters or fewer")
    private String accountType;

    @NotNull(message = "Initial balance is required")
    @PositiveOrZero(message = "Initial balance cannot be negative")
    private Double initialBalance;

    // No-args constructor needed here
    public AccountRequest() {}

    public String getAccountType() { return accountType; }
    public void setAccountType(String accountType) { this.accountType = accountType; }

    public Double getInitialBalance() { return initialBalance; }
    public void setInitialBalance(Double initialBalance) { this.initialBalance = initialBalance; }
}
