package com.cardlogin.controller;

import com.cardlogin.model.CardInfoDTO;
import com.cardlogin.model.RegisterRequest;
import com.cardlogin.model.ApiResponse;
import com.cardlogin.service.RegisterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class RegisterController {

    @Autowired
    private RegisterService registerService;

    @PostMapping("/register")
    public ApiResponse<CardInfoDTO> register(@RequestBody RegisterRequest request) {
        try {
            CardInfoDTO dto = registerService.register(request);
            return ApiResponse.success(dto);
        } catch (RuntimeException e) {
            return ApiResponse.error(e.getMessage());
        }
    }
} 