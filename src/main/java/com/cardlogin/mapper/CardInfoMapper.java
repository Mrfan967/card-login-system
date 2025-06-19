package com.cardlogin.mapper;

import com.cardlogin.model.CardInfo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface CardInfoMapper {
    CardInfo selectByCardNumber(@Param("cardNumber") String cardNumber);
    List<CardInfo> selectAll();
    int insert(CardInfo cardInfo);
    int update(CardInfo cardInfo);
    int deleteByCardNumber(@Param("cardNumber") String cardNumber);
} 