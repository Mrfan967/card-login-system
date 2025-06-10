package com.cardlogin.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;
import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CardInfo implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private String cardNumber;
    private LocalDateTime expiryDate;
    private boolean bound;
    private String boundDeviceFingerprint;
    private LocalDateTime lastLoginTime;
    private int loginAttempts;
    private String username; // 用户名
    private String password; // 密码（建议加密存储）
} 