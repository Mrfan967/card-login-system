package com.cardlogin.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;
import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CardInfo implements Serializable {
    private static final long serialVersionUID = 1L;
    
    /** 卡号 */
    private String cardNumber;
    /** 有效期 */
    private LocalDateTime expiryDate;
    /** 是否已绑定设备 */
    private boolean bound;
    /** 绑定设备指纹 */
    private String boundDeviceFingerprint;
    /** 上次登录时间 */
    private LocalDateTime lastLoginTime;
    /** 登录尝试次数 */
    private int loginAttempts;
    /** 用户名 */
    private String username;
    /** 密码（建议加密存储） */
    private String password;
} 