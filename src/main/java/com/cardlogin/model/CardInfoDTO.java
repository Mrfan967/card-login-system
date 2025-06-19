package com.cardlogin.model;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class CardInfoDTO {
    private Long id;
    private String cardNumber;
    private LocalDateTime expiryDate;
    private boolean bound;
    private String boundDeviceFingerprint;
    private LocalDateTime lastLoginTime;
    private int loginAttempts;
    private String username;
} 