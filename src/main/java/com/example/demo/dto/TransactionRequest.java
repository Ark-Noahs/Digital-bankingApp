package com.example.demo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public class TransactionRequest {

    @NotBlank(message = "Transaction type is required (DEPOSIT, WITHDRAWAL, or TRANSFER)")
    private String type; // "DEPOSIT", "WITHDRAWAL", "TRANSFER"

    @NotNull(message = "Amount is required")
    @Positive(message = "Amount must be positive")
    private Double amount;

    private String description;

    // Only needed for transfers
    private Long toAccountId;

    public TransactionRequest() {}

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public Double getAmount() { return amount; }
    public void setAmount(Double amount) { this.amount = amount; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Long getToAccountId() { return toAccountId; }
    public void setToAccountId(Long toAccountId) { this.toAccountId = toAccountId; }
}
