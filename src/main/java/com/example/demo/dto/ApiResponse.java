package com.example.demo.dto;

public class ApiResponse<T> {
    private boolean success;
    private T data;
    private String message;

    public ApiResponse(boolean success, T data, String message) {
        this.success = success;
        this.data = data;
        this.message = message;
    }

    // Getter and setter for success
    public boolean isSuccess() {
        return success;
    }
    public void setSuccess(boolean success) {
        this.success = success;
    }

    // Getter and setter for data
    public T getData() {
        return data;
    }
    public void setData(T data) {
        this.data = data;
    }

    // Getter and setter for message
    public String getMessage() {
        return message;
    }
    public void setMessage(String message) {
        this.message = message;
    }
}
