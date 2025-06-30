package com.cardlogin.repository;

import com.cardlogin.model.CardInfo;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CardInfoRepository extends JpaRepository<CardInfo, Long> {
    // 可自定义查询方法
    boolean existsByUsername(String username);
    boolean existsByCardNumber(String cardNumber);
    CardInfo findByCardNumber(String cardNumber);
}