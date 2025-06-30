package com.cardlogin.controller;

import com.cardlogin.model.LoginRequest;
import com.cardlogin.model.ApiResponse;
import com.cardlogin.service.LoginService;
import com.cardlogin.util.RateLimiter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

@RestController
@RequestMapping("/api")
public class LoginController {

    @Autowired
    private LoginService loginService;

    @Autowired
    private RateLimiter rateLimiter;

    @PostMapping("/login")
    public ResponseEntity<ApiResponse> login(
            @Valid @RequestBody LoginRequest loginRequest,
            @RequestHeader(value = "X-Device-Fingerprint", required = false) String deviceFingerprint,
            HttpServletRequest request) {
        
        // 如果没有设备指纹，使用默认值
        if (deviceFingerprint == null || deviceFingerprint.trim().isEmpty()) {
            deviceFingerprint = loginRequest.getDeviceFingerprint();
            if (deviceFingerprint == null) {
                deviceFingerprint = "default-device-" + request.getRemoteAddr();
            }
        }

        // 检查请求频率限制
        String clientIp = request.getRemoteAddr();
        if (rateLimiter != null && !rateLimiter.tryAcquire(clientIp)) {
            return ResponseEntity
                    .status(HttpStatus.TOO_MANY_REQUESTS)
                    .body(ApiResponse.error("请求过于频繁，请稍后再试"));
        }

        try {
            return ResponseEntity.ok(loginService.login(loginRequest, deviceFingerprint));
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    @PostMapping("/unbind")
    public ResponseEntity<ApiResponse> unbind(@Valid @RequestBody LoginRequest loginRequest) {
        try {
            return ResponseEntity.ok(loginService.unbind(loginRequest.getCardNumber()));
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/query/{cardNumber}")
    public ResponseEntity<ApiResponse> query(@PathVariable String cardNumber) {
        try {
            return ResponseEntity.ok(loginService.query(cardNumber));
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(e.getMessage()));
        }
    }
} 