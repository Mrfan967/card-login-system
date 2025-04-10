package com.example.model;




public class LoginRequest {
    private String username;
    private String password;
    // Getters and Setters
    public String getUsername() {
        return username;
    }
    public void setUsername(String username) {
        this.username = username;
    }
    public String getPassword() {
        return password;
    }
    public void setPassword(String password) {
        this.password = password;
    }
    private String cardNumber;
    private String macAddress;

    // 构造函数
    public LoginRequest() {
    }

    public LoginRequest(String cardNumber, String macAddress) {
        this.cardNumber = cardNumber;
        this.macAddress = macAddress;
    }

    // getter和setter
    public String getCardNumber() {
        return cardNumber;
    }

    public void setCardNumber(String cardNumber) {
        this.cardNumber = cardNumber;
    }

    public String getMacAddress() {
        return macAddress;
    }

    public void setMacAddress(String macAddress) {
        this.macAddress = macAddress;
    }
}