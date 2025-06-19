package com.cardlogin.controller;

import com.cardlogin.model.ApiResponse;
import com.cardlogin.model.CardInfo;
import com.cardlogin.service.CardInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/card")
public class CardController {

    @Autowired
    private CardInfoService cardInfoService;

    /**
     * 生成新卡
     */
    @PostMapping("/generate")
    public ResponseEntity<ApiResponse> generateCard(@Valid @RequestBody CardInfo cardInfo) {
        try {
            // 设置卡的默认有效期为6个月
            cardInfo.setExpiryDate(LocalDateTime.now().plusMonths(6));
            cardInfo.setBound(false);
            cardInfo.setLoginAttempts(0);
            
            boolean result = cardInfoService.register(cardInfo);
            return ResponseEntity.ok(ApiResponse.success("卡号创建成功"));
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * 卡绑定设备
     */
    @PostMapping("/bind")
    public ResponseEntity<ApiResponse> bindDevice(
            @RequestParam("cardNumber") String cardNumber,
            @RequestParam("deviceFingerprint") String deviceFingerprint) {
        try {
            CardInfo card = cardInfoService.getByCardNumber(cardNumber);
            if (card == null) {
                return ResponseEntity
                        .status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error("卡号不存在"));
            }
            
            if (card.isBound() && !deviceFingerprint.equals(card.getBoundDeviceFingerprint())) {
                return ResponseEntity
                        .status(HttpStatus.BAD_REQUEST)
                        .body(ApiResponse.error("该卡已绑定其他设备，请先解绑"));
            }
            
            card.setBound(true);
            card.setBoundDeviceFingerprint(deviceFingerprint);
            card.setLastLoginTime(LocalDateTime.now());
            
            boolean result = cardInfoService.update(card);
            return ResponseEntity.ok(ApiResponse.success("设备绑定成功"));
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * 卡解绑设备
     */
    @PostMapping("/unbind")
    public ResponseEntity<ApiResponse> unbindDevice(@RequestParam("cardNumber") String cardNumber) {
        try {
            boolean result = cardInfoService.unbind(cardNumber);
            return ResponseEntity.ok(ApiResponse.success("设备解绑成功"));
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * 查询卡信息
     */
    @GetMapping("/query")
    public ResponseEntity<ApiResponse> queryCard(@RequestParam("cardNumber") String cardNumber) {
        try {
            CardInfo card = cardInfoService.getByCardNumber(cardNumber);
            if (card == null) {
                return ResponseEntity
                        .status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error("卡号不存在"));
            }
            return ResponseEntity.ok(ApiResponse.success(card));
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(e.getMessage()));
        }
    }
    
    /**
     * 查询所有卡
     */
    @GetMapping("/list")
    public ResponseEntity<ApiResponse> getAllCards() {
        try {
            List<CardInfo> cards = cardInfoService.getAllCards();
            return ResponseEntity.ok(ApiResponse.success(cards));
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(e.getMessage()));
        }
    }
} 