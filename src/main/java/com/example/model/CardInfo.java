package com.example.model;

import java.time.LocalDateTime;

import lombok.Data;
import lombok.AllArgsConstructor;

@Data
@AllArgsConstructor
public class CardInfo {
    private String cardNumber;
    private boolean bound;
    private String macAddress;
    private LocalDateTime bindTime;
    private LocalDateTime expiryDate;

    public CardInfo(String cardNumber, LocalDateTime expiryDate) {
        this.cardNumber = cardNumber;
        this.expiryDate = expiryDate;
        this.bound = false;
    }

    public String getCardNumber() {
        return cardNumber;
    }

    public void setCardNumber(String cardNumber) {
        this.cardNumber = cardNumber;
    }

    public boolean isBound() {
        return bound;
    }

    public void setBound(boolean bound) {
        this.bound = bound;
    }

    public String getMacAddress() {
        return macAddress;
    }

    public void setMacAddress(String macAddress) {
        this.macAddress = macAddress;
    }

    public LocalDateTime getBindTime() {
        return bindTime;
    }

    public void setBindTime(LocalDateTime bindTime) {
        this.bindTime = bindTime;
    }

    public LocalDateTime getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(LocalDateTime expiryDate) {
        this.expiryDate = expiryDate;
    }
}