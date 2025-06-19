package com.cardlogin.service;

import com.cardlogin.model.CardInfo;
import com.cardlogin.model.RegisterRequest;
import com.cardlogin.model.CardInfoDTO;
import com.cardlogin.repository.CardInfoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class RegisterService {

    private static final int DEFAULT_LOGIN_ATTEMPTS = 0;
    private static final int DEFAULT_EXPIRE_YEARS = 1;

    @Autowired
    private CardInfoRepository cardInfoRepository;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public CardInfoDTO register(RegisterRequest request) {
        // 判重：用户名或卡号已存在
        if (cardInfoRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("用户名已存在");
        }
        if (cardInfoRepository.existsByCardNumber(request.getCardNumber())) {
            throw new RuntimeException("卡号已存在");
        }
        CardInfo cardInfo = new CardInfo();
        cardInfo.setCardNumber(request.getCardNumber());
        cardInfo.setUsername(request.getUsername());
        cardInfo.setPassword(passwordEncoder.encode(request.getPassword()));
        cardInfo.setBoundDeviceFingerprint(request.getBoundDeviceFingerprint());
        cardInfo.setBound(false);
        cardInfo.setExpiryDate(LocalDateTime.now().plusYears(DEFAULT_EXPIRE_YEARS));
        cardInfo.setLastLoginTime(null);
        cardInfo.setLoginAttempts(DEFAULT_LOGIN_ATTEMPTS);
        CardInfo saved = cardInfoRepository.save(cardInfo);
        // 转换为DTO
        CardInfoDTO dto = new CardInfoDTO();
        dto.setId(saved.getId());
        dto.setCardNumber(saved.getCardNumber());
        dto.setExpiryDate(saved.getExpiryDate());
        dto.setBound(saved.isBound());
        dto.setBoundDeviceFingerprint(saved.getBoundDeviceFingerprint());
        dto.setLastLoginTime(saved.getLastLoginTime());
        dto.setLoginAttempts(saved.getLoginAttempts());
        dto.setUsername(saved.getUsername());
        return dto;
    }
} 