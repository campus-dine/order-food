-- ================================
-- 建库 order_food_db
-- ================================
CREATE DATABASE IF NOT EXISTS order_food_db 
DEFAULT CHARACTER SET utf8mb4 
COLLATE utf8mb4_unicode_ci;

USE order_food_db;


-- ================================
-- 建表 用户
-- ================================
CREATE TABLE IF NOT EXISTS `users` (
                                       `id` int NOT NULL AUTO_INCREMENT COMMENT '用户主键ID',
                                       `open_id` VARCHAR(100) NOT NULL  COMMENT '微信用户open_id，唯一标识',
    `username` VARCHAR(50) NOT NULL DEFAULT '微信用户' COMMENT '用户名',
    `avatar_url` VARCHAR(500) DEFAULT NULL COMMENT '头像URL',
    `role` TINYINT NOT NULL DEFAULT 0 COMMENT '身份角色：0-客户，1-商家，9-超管',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_open_id` (`open_id`),
    KEY `idx_role` (`role`)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';


-- ================================
-- 建表 1：客户表（学生）
-- ================================
CREATE TABLE IF NOT EXISTS `customers` (
                                           `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '客户表主键ID',
                                           `user_id` INT NOT NULL COMMENT '关联users表的id',
                                           `balance` DECIMAL(10, 2) NOT NULL DEFAULT 0.00 COMMENT '客户账户余额(元)',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_id` (`user_id`) -- 每个用户在客户表里只能有一条记录
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='客户/学生扩展信息表';

-- ================================
-- 建表 2：商家表
-- ================================
CREATE TABLE IF NOT EXISTS `merchants` (
                                           `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '商家表主键ID',
                                           `user_id` INT NOT NULL COMMENT '关联users表的id',
                                           `balance` DECIMAL(10, 2) NOT NULL DEFAULT 0.00 COMMENT '商家账户余额(元)', -- 补上余额
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_id` (`user_id`) -- 每个用户在商家表里只能有一条记录
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='商家扩展信息表';

























