package com.cardlogin.model;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import java.time.LocalDateTime;

public class LoginRequest {
    
    @NotBlank(message = "卡号不能为空")
    @Pattern(regexp = "^\\d{18,19}$", message = "卡号必须是18-19位数字")
    private String cardNumber;
    
    private String deviceFingerprint;
    private LocalDateTime timestamp;

    // Getters and Setters
    public String getCardNumber() {
        return cardNumber;
    }

    public void setCardNumber(String cardNumber) {
        this.cardNumber = cardNumber;
    }

    public String getDeviceFingerprint() {
        return deviceFingerprint;
    }

    public void setDeviceFingerprint(String deviceFingerprint) {
        this.deviceFingerprint = deviceFingerprint;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
} 