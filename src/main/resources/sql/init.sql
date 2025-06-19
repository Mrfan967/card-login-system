-- 创建数据库（如果不存在）
CREATE DATABASE IF NOT EXISTS card CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- 使用数据库
USE card;

-- 创建 card_info 表
CREATE TABLE IF NOT EXISTS card_info (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    card_number VARCHAR(50) NOT NULL UNIQUE COMMENT '卡号',
    expiry_date DATETIME NOT NULL COMMENT '有效期',
    bound BOOLEAN DEFAULT FALSE COMMENT '是否已绑定设备',
    bound_device_fingerprint VARCHAR(255) COMMENT '绑定设备指纹',
    last_login_time DATETIME COMMENT '上次登录时间',
    login_attempts INT DEFAULT 0 COMMENT '登录尝试次数',
    username VARCHAR(100) NOT NULL COMMENT '用户名',
    password VARCHAR(255) NOT NULL COMMENT '密码（加密存储）',
    created_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='卡片信息表';

-- 创建索引
CREATE INDEX idx_card_number ON card_info(card_number);
CREATE INDEX idx_username ON card_info(username);
CREATE INDEX idx_bound_device ON card_info(bound_device_fingerprint);

-- 插入测试数据
INSERT INTO card_info (card_number, expiry_date, bound, bound_device_fingerprint, last_login_time, login_attempts, username, password) 
VALUES 
('1808451185619763200', DATE_ADD(NOW(), INTERVAL 1 YEAR), FALSE, NULL, NULL, 0, 'user001', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z2EuHKCS2qlJzyMdvlHHpfqa'),
('1808451185619763201', DATE_ADD(NOW(), INTERVAL 1 YEAR), FALSE, NULL, NULL, 0, 'user002', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z2EuHKCS2qlJzyMdvlHHpfqa'),
('1808451185619763202', DATE_ADD(NOW(), INTERVAL 1 YEAR), FALSE, NULL, NULL, 0, 'user003', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z2EuHKCS2qlJzyMdvlHHpfqa'),
('1808451185619763203', DATE_ADD(NOW(), INTERVAL 1 YEAR), FALSE, NULL, NULL, 0, 'user004', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z2EuHKCS2qlJzyMdvlHHpfqa'),
('1808451185619763204', DATE_ADD(NOW(), INTERVAL 1 YEAR), FALSE, NULL, NULL, 0, 'user005', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z2EuHKCS2qlJzyMdvlHHpfqa')
ON DUPLICATE KEY UPDATE 
    expiry_date = VALUES(expiry_date),
    username = VALUES(username),
    password = VALUES(password);

-- 查询验证
SELECT * FROM card_info;
