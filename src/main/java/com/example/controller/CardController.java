package com.example.controller;

import com.example.model.CardInfo;
import com.example.model.LoginRequest;
import com.example.model.Response;
import com.example.service.CardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class CardController {
    @Autowired
    private CardService cardService;
    @PostMapping("/login")
    public Response login(@RequestBody LoginRequest request) {
        try {
            boolean success = cardService.login(request.getCardNumber(), request.getMacAddress());
            if (success) {
                return Response.success("登录成功");
            }
            return Response.error("登录失败：卡号无效或已在其他设备上绑定");
        } catch (Exception e) {
            return Response.error("登录失败：" + e.getMessage());
        }
    }
    @PostMapping("/unbind")
    public Response unbind(@RequestBody LoginRequest request) {
        try {
            boolean success = cardService.unbind(request.getCardNumber());
            if (success) {
                return Response.success("解绑成功");
            }
            return Response.error("解绑失败：卡号无效或未绑定");
        } catch (Exception e) {
            return Response.error("解绑失败：" + e.getMessage());
        }
    }

    @GetMapping("/query/{cardNumber}")
    public Response query(@PathVariable String cardNumber) {
        try {
            CardInfo cardInfo = cardService.query(cardNumber);
            if (cardInfo != null) {
                return Response.success(cardInfo);
            }
            return Response.error("查询失败：卡号不存在");
        } catch (Exception e) {
            return Response.error("查询失败：" + e.getMessage());
        }
    }

    @PostMapping("/generate")
    public Response generate() {
        try {
            String cardNumber = cardService.generateCard();
            return Response.success("卡号生成成功", cardNumber);
        } catch (Exception e) {
            return Response.error("生成失败：" + e.getMessage());
        }
    }

    @GetMapping("/getMacAddress")
    public Response getMacAddress() {
        try {
            String macAddress = cardService.getMacAddress();
            return Response.success(macAddress);
        } catch (Exception e) {
            return Response.error("获取MAC地址失败：" + e.getMessage());
        }
    }
} 