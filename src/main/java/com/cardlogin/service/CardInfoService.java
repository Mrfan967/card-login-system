package com.cardlogin.service;

import com.cardlogin.mapper.CardInfoMapper;
import com.cardlogin.model.CardInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.List;

@Service
public class CardInfoService {
    @Autowired
    private CardInfoMapper cardInfoMapper;
    
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    private static final String CARD_INFO_KEY = "card:info:";

    // 注册
    public boolean register(CardInfo cardInfo) {
        if (cardInfoMapper.selectByCardNumber(cardInfo.getCardNumber()) != null) {
            throw new RuntimeException("卡号已存在");
        }
        cardInfo.setPassword(passwordEncoder.encode(cardInfo.getPassword()));
        boolean result = cardInfoMapper.insert(cardInfo) > 0;
        
        // 同步保存到Redis
        if (result) {
            redisTemplate.opsForValue().set(CARD_INFO_KEY + cardInfo.getCardNumber(), cardInfo);
        }
        
        return result;
    }

    // 登录
    public CardInfo login(String cardNumber, String password) {
        CardInfo card = getByCardNumber(cardNumber);
        if (card == null) {
            throw new RuntimeException("卡号不存在");
        }
        if (!passwordEncoder.matches(password, card.getPassword())) {
            throw new RuntimeException("密码错误");
        }
        return card;
    }

    // 解绑
    public boolean unbind(String cardNumber) {
        CardInfo card = getByCardNumber(cardNumber);
        if (card == null) {
            throw new RuntimeException("卡号不存在");
        }
        card.setBound(false);
        card.setBoundDeviceFingerprint(null);
        return update(card);
    }
    
    // 根据卡号查询卡信息
    public CardInfo getByCardNumber(String cardNumber) {
        // 先从Redis中查询
        CardInfo card = (CardInfo) redisTemplate.opsForValue().get(CARD_INFO_KEY + cardNumber);
        
        // 如果Redis中没有，则从数据库查询
        if (card == null) {
            card = cardInfoMapper.selectByCardNumber(cardNumber);
            
            // 如果数据库中存在，则同步到Redis
            if (card != null) {
                redisTemplate.opsForValue().set(CARD_INFO_KEY + cardNumber, card);
            }
        }
        
        return card;
    }
    
    // 更新卡信息
    public boolean update(CardInfo cardInfo) {
        boolean result = cardInfoMapper.update(cardInfo) > 0;
        
        // 同步更新Redis
        if (result) {
            redisTemplate.opsForValue().set(CARD_INFO_KEY + cardInfo.getCardNumber(), cardInfo);
        }
        
        return result;
    }
    
    // 获取所有卡
    public List<CardInfo> getAllCards() {
        return cardInfoMapper.selectAll();
    }
} 