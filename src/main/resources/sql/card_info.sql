-- 创建card数据库（如果不存在）
CREATE DATABASE IF NOT EXISTS `card` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- 使用card数据库
USE `card`;

-- 删除card_info表（如果已存在）
DROP TABLE IF EXISTS `card_info`;

-- 创建card_info表
CREATE TABLE `card_info` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `card_number` varchar(20) NOT NULL COMMENT '卡号',
  `expiry_date` datetime DEFAULT NULL COMMENT '有效期',
  `bound` tinyint(1) DEFAULT 0 COMMENT '是否已绑定设备',
  `bound_device_fingerprint` varchar(64) DEFAULT NULL COMMENT '绑定设备指纹',
  `last_login_time` datetime DEFAULT NULL COMMENT '上次登录时间',
  `login_attempts` int(11) DEFAULT 0 COMMENT '登录尝试次数',
  `username` varchar(50) DEFAULT NULL COMMENT '用户名',
  `password` varchar(255) DEFAULT NULL COMMENT '密码',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_card_number` (`card_number`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='卡信息表';

-- 添加索引
CREATE INDEX idx_card_username ON card_info(username);
CREATE INDEX idx_card_bound ON card_info(bound); 