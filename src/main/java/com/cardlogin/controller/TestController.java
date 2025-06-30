package com.cardlogin.controller;

import com.cardlogin.model.ApiResponse;
import com.cardlogin.model.CardInfo;
import com.cardlogin.repository.CardInfoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/test")
public class TestController {

    @Autowired
    private DataSource dataSource;

    @Autowired(required = false)
    private CardInfoRepository cardInfoRepository;

    @GetMapping("/connection")
    public ApiResponse testConnection() {
        Map<String, Object> result = new HashMap<>();
        
        try (Connection connection = dataSource.getConnection()) {
            result.put("status", "success");
            result.put("message", "数据库连接成功");
            result.put("url", connection.getMetaData().getURL());
            result.put("username", connection.getMetaData().getUserName());
            result.put("databaseProductName", connection.getMetaData().getDatabaseProductName());
            result.put("databaseProductVersion", connection.getMetaData().getDatabaseProductVersion());
            return ApiResponse.success(result);
        } catch (SQLException e) {
            result.put("status", "error");
            result.put("message", "数据库连接失败: " + e.getMessage());
            return ApiResponse.error("数据库连接失败: " + e.getMessage());
        }
    }

    @GetMapping("/cards")
    public ApiResponse testCards() {
        if (cardInfoRepository == null) {
            return ApiResponse.error("CardInfoRepository 未初始化");
        }
        
        try {
            List<CardInfo> cards = cardInfoRepository.findAll();
            Map<String, Object> result = new HashMap<>();
            result.put("count", cards.size());
            result.put("cards", cards);
            return ApiResponse.success(result);
        } catch (Exception e) {
            return ApiResponse.error("查询卡片信息失败: " + e.getMessage());
        }
    }
}
