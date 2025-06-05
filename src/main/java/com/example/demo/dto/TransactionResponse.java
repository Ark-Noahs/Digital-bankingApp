package com.example.demo.dto;

import java.time.LocalDateTime;

public class TransactionResponse {
    private Long id;
    private String type;
    private Double amount;
    private String description;
    private LocalDateTime timestamp;
    private Long toAccountId;

    public TransactionResponse() {}

    public TransactionResponse(Long id, String type, Double amount, String description, LocalDateTime timestamp, Long toAccountId) {
        this.id = id;
        this.type = type;
        this.amount = amount;
        this.description = description;
        this.timestamp = timestamp;
        this.toAccountId = toAccountId;
    }

    // Getters and setters...
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public Double getAmount() { return amount; }
    public void setAmount(Double amount) { this.amount = amount; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }

    public Long getToAccountId() { return toAccountId; }
    public void setToAccountId(Long toAccountId) { this.toAccountId = toAccountId; }
}
