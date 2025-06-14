package com.example.model;

import lombok.Data;

@Data
public class LoginRequest {
    private String username;
    private String password;
    private String cardNumber;
    private String macAddress;

    // 构造函数
    public LoginRequest() {
    }

    public LoginRequest(String cardNumber, String macAddress) {
        this.cardNumber = cardNumber;
        this.macAddress = macAddress;
    }
}
