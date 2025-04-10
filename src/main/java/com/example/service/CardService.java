package com.example.service;

import com.example.model.CardInfo;
import org.springframework.stereotype.Service;
import javax.annotation.PostConstruct;
import java.net.NetworkInterface;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
public class CardService {
    // 使用ConcurrentHashMap存储卡号信息，确保线程安全
    private final Map<String, CardInfo> cardMap = new ConcurrentHashMap<>();
    // 使用读写锁进行并发控制
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
    // 登录失败计数器
    private final Map<String, Integer> loginFailureCount = new ConcurrentHashMap<>();
    // 登录锁定时间记录
    private final Map<String, LocalDateTime> loginLockTime = new ConcurrentHashMap<>();
    // 最大失败次数
    private static final int MAX_LOGIN_ATTEMPTS = 5;
    // 锁定时间（分钟）
    private static final int LOCK_DURATION_MINUTES = 5;
    @PostConstruct
    public void init() {
        // 生成测试卡号
        String[] testCards = {
            "1808451185619763200",
            "1808451185619763201",
            "1808451185619763202",
            "1808451185619763203",
            "1808451185619763204"
        };
        
        for (String cardNumber : testCards) {
            LocalDateTime expiryDate = LocalDateTime.now().plusMonths(6);
            cardMap.put(cardNumber, new CardInfo(cardNumber, expiryDate));
            System.out.println("测试卡号已生成: " + cardNumber);
        }
    }

    // 登录验证
    public boolean login(String cardNumber, String macAddress) {
        // 检查卡号格式
        if (!isValidCardNumber(cardNumber)) {
            throw new RuntimeException("卡号格式不正确");
        }

        // 检查是否被锁定
        if (isLoginLocked(cardNumber)) {
            long remainingMinutes = getRemainingLockTime(cardNumber);
            throw new RuntimeException("账号已被锁定，请" + remainingMinutes + "分钟后重试");
        }

        try {
            lock.readLock().lock();
            CardInfo cardInfo = cardMap.get(cardNumber);
            if (cardInfo == null) {
                incrementFailureCount(cardNumber);
                throw new RuntimeException("卡号不存在");
            }

            // 检查卡是否过期
            if (LocalDateTime.now().isAfter(cardInfo.getExpiryDate())) {
                incrementFailureCount(cardNumber);
                throw new RuntimeException("卡号已过期");
            }

            // 如果卡已绑定
            if (cardInfo.isBound()) {
                // 检查是否是同一台机器
                if (!cardInfo.getMacAddress().equals(macAddress)) {
                    incrementFailureCount(cardNumber);
                    throw new RuntimeException("此卡已在其他设备上绑定");
                }
                resetFailureCount(cardNumber);
                return true;
            }

            try {
                lock.readLock().unlock();
                lock.writeLock().lock();
                // 再次检查状态（双重检查锁定）
                cardInfo = cardMap.get(cardNumber);
                if (cardInfo.isBound()) {
                    if (!cardInfo.getMacAddress().equals(macAddress)) {
                        throw new RuntimeException("此卡已在其他设备上绑定");
                    }
                    return true;
                }

                // 新绑定
                cardInfo.setBound(true);
                cardInfo.setMacAddress(macAddress);
                cardInfo.setBindTime(LocalDateTime.now());
                resetFailureCount(cardNumber);
                return true;
            } finally {
                lock.writeLock().unlock();
                lock.readLock().lock();
            }
        } finally {
            lock.readLock().unlock();
        }
    }

    // 解绑
    public boolean unbind(String cardNumber) {
        if (!isValidCardNumber(cardNumber)) {
            throw new RuntimeException("卡号格式不正确");
        }

        try {
            lock.writeLock().lock();
            CardInfo cardInfo = cardMap.get(cardNumber);
            if (cardInfo == null) {
                throw new RuntimeException("卡号不存在");
            }
            if (!cardInfo.isBound()) {
                throw new RuntimeException("卡号未绑定");
            }

            cardInfo.setBound(false);
            cardInfo.setMacAddress(null);
            cardInfo.setBindTime(null);
            return true;
        } finally {
            lock.writeLock().unlock();
        }
    }

    // 查询卡号信息
    public CardInfo query(String cardNumber) {
        if (!isValidCardNumber(cardNumber)) {
            throw new RuntimeException("卡号格式不正确");
        }

        try {
            lock.readLock().lock();
            CardInfo cardInfo = cardMap.get(cardNumber);
            if (cardInfo == null) {
                throw new RuntimeException("卡号不存在");
            }
            // 返回副本而不是原始对象
            return cloneCardInfo(cardInfo);
        } finally {
            lock.readLock().unlock();
        }
    }

    // 获取MAC地址
    public String getMacAddress() throws Exception {
        try {
            Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();
            for (NetworkInterface ni : Collections.list(networkInterfaces)) {
                if (!ni.isLoopback() && ni.isUp()) {
                    byte[] hardwareAddress = ni.getHardwareAddress();
                    if (hardwareAddress != null) {
                        return formatMacAddress(hardwareAddress);
                    }
                }
            }
            throw new Exception("无法获取MAC地址");
        } catch (Exception e) {
            throw new Exception("获取MAC地址失败: " + e.getMessage());
        }
    }

    // 验证卡号格式
    private boolean isValidCardNumber(String cardNumber) {
        return cardNumber != null && cardNumber.matches("\\d{18,19}");
    }

    // 格式化MAC地址
    private String formatMacAddress(byte[] hardwareAddress) {
        return IntStream.range(0, hardwareAddress.length)
                .mapToObj(i -> String.format("%02X", hardwareAddress[i]))
                .collect(Collectors.joining(":"));
    }

    // 克隆卡号信息
    private CardInfo cloneCardInfo(CardInfo original) {
        CardInfo clone = new CardInfo(original.getCardNumber(), original.getExpiryDate());
        clone.setBound(original.isBound());
        clone.setMacAddress(original.getMacAddress());
        clone.setBindTime(original.getBindTime());
        return clone;
    }

    // 检查是否被锁定
    private boolean isLoginLocked(String cardNumber) {
        LocalDateTime lockTime = loginLockTime.get(cardNumber);
        if (lockTime != null) {
            if (LocalDateTime.now().isBefore(lockTime.plusMinutes(LOCK_DURATION_MINUTES))) {
                return true;
            } else {
                // 锁定时间已过，清除记录
                loginLockTime.remove(cardNumber);
                loginFailureCount.remove(cardNumber);
            }
        }
        return false;
    }
    // 获取剩余锁定时间（分钟）
    private long getRemainingLockTime(String cardNumber) {
        LocalDateTime lockTime = loginLockTime.get(cardNumber);
        if (lockTime != null) {
            LocalDateTime unlockTime = lockTime.plusMinutes(LOCK_DURATION_MINUTES);
            return java.time.Duration.between(LocalDateTime.now(), unlockTime).toMinutes();
        }
        return 0;
    }

    // 增加失败次数
    private void incrementFailureCount(String cardNumber) {
        int count = loginFailureCount.getOrDefault(cardNumber, 0) + 1;
        loginFailureCount.put(cardNumber, count);
        if (count >= MAX_LOGIN_ATTEMPTS) {
            loginLockTime.put(cardNumber, LocalDateTime.now());
        }
    }
    // 重置失败次数
    private void resetFailureCount(String cardNumber) {
        loginFailureCount.remove(cardNumber);
        loginLockTime.remove(cardNumber);
    }

    // 生成卡号
    public String generateCard() {
        try {
            lock.writeLock().lock();
            String newCardNumber;
            do {
                newCardNumber = generateRandomCardNumber();
            } while (cardMap.containsKey(newCardNumber));

            LocalDateTime expiryDate = LocalDateTime.now().plusMonths(6);
            cardMap.put(newCardNumber, new CardInfo(newCardNumber, expiryDate));
            return newCardNumber;
        } finally {
            lock.writeLock().unlock();
        }
    }

    // 生成随机卡号
    private String generateRandomCardNumber() {
        Random random = new Random();
        return String.format("1808%015d", random.nextInt(1000000000));
    }
}