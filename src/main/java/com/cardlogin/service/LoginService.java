package com.cardlogin.service;

import com.cardlogin.model.ApiResponse;
import com.cardlogin.model.LoginRequest;

public interface LoginService {
    ApiResponse login(LoginRequest loginRequest, String deviceFingerprint);
    ApiResponse unbind(String cardNumber);
    ApiResponse query(String cardNumber);
} 