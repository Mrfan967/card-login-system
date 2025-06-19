package com.cardlogin.model;

import lombok.Data;

@Data
public class RegisterRequest {
    private String cardNumber;
    private String username;
    private String password;
    private String boundDeviceFingerprint;
} 