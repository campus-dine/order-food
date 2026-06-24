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
-- 建表 ：客户表（学生）
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
-- 建表 ：商家表
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

-- ================================
-- 建表 ：菜品分类表
-- ================================
CREATE TABLE IF NOT EXISTS `categories` (
`id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '分类表主键ID',
`category_name` VARCHAR(50) NOT NULL COMMENT '分类名',
`sort` INT NOT NULL DEFAULT 0 COMMENT '排序号',
`create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
`update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
PRIMARY KEY (`id`),
UNIQUE KEY `uk_category_name` (`category_name`) -- 防止分类名字重复
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='菜品分类表';


-- ================================
-- 建表 ：菜品信息表
-- ================================
CREATE TABLE IF NOT EXISTS `dishes` (
`id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '菜品表主键ID',
`dish_name` VARCHAR(100) NOT NULL COMMENT '菜品名称',
`image_url` VARCHAR(500) DEFAULT '' COMMENT '菜品图片URL地址',
`category_id` BIGINT NOT NULL COMMENT '关联分类表的id',
`rating` DECIMAL(3, 2) NOT NULL DEFAULT 5.00 COMMENT '菜品评分',
`stock` INT NOT NULL DEFAULT 0 COMMENT '当前库存',
`price` DECIMAL(10, 2) NOT NULL DEFAULT 0.00 COMMENT '菜品售价(元)',
`sales` INT NOT NULL DEFAULT 0 COMMENT '历史总销量',
`status` TINYINT NOT NULL DEFAULT 1 COMMENT '菜品状态：0-下架，1-上架营业',
`create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
`update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
PRIMARY KEY (`id`),
-- 🚨 核心优化：为流式查询建立联合索引 (分类 + 销量 + ID)，让你的 LIMIT 分页查询瞬间起飞
KEY `idx_category_sales_id` (`category_id`, `sales`, `id`),
KEY `idx_status` (`status`) -- 方便快速过滤下架商品
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='菜品基本信息表';

-- ================================
-- 建表 3：配料表
-- ================================
CREATE TABLE IF NOT EXISTS `ingredients` (
`id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '配料ID',
`name` VARCHAR(50) NOT NULL COMMENT '配料名称(如：珍珠、牛奶、牛肉)',
`unit_price` DECIMAL(10, 2) NOT NULL DEFAULT 0.00 COMMENT '成本的单位价格(元)',
`total_stock` DECIMAL(10, 2) NOT NULL DEFAULT 0.00 COMMENT '库存总量',
`stock_unit` VARCHAR(10) NOT NULL COMMENT '库存单位(如：kg、L、个)',
`create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
`update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
PRIMARY KEY (`id`),
UNIQUE KEY `uk_name` (`name`) -- 🚨 补丁 1：给配料名称加唯一索引，防止重名导致物料混乱
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='配料基础信息表';

-- ================================
-- 建表 4：菜品配方表
-- ================================
CREATE TABLE IF NOT EXISTS `dish_recipes` (
`id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '配方ID',
`dish_id` BIGINT NOT NULL COMMENT '关联的菜品id',
`ingredient_id` BIGINT NOT NULL COMMENT '关联的配料id',
`usage_amount` DECIMAL(10, 2) NOT NULL COMMENT '单份菜品的配料用量',
`cost` DECIMAL(10, 2) NOT NULL DEFAULT 0.00 COMMENT '这道菜中该配料的成本',
`usage_unit` VARCHAR(10) NOT NULL COMMENT '用量单位(如：g、ml、个)',
`create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
PRIMARY KEY (`id`),
-- 🚨 补丁 2：核心守护！一道菜和一种配料的组合必须唯一，杜绝重复配方记录
UNIQUE KEY `uk_dish_ingredient` (`dish_id`, `ingredient_id`),
KEY `idx_ingredient_id` (`ingredient_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='菜品配方关联表';

-- ================================
-- 建表 5：客户地址表
-- ================================
CREATE TABLE IF NOT EXISTS `customer_addresses` (
`id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '地址ID',
`customer_id` BIGINT NOT NULL COMMENT '客户id',
`detail_address` VARCHAR(255) NOT NULL COMMENT '具体外送地址',
`is_default` TINYINT DEFAULT NULL COMMENT '是否常用：1-常用，NULL-普通地址',
`create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
`update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
PRIMARY KEY (`id`),
-- 每个客户只能有一个 1 (常用)，普通地址为 NULL 不受限制
UNIQUE KEY `uk_customer_default` (`customer_id`, `is_default`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='客户外送地址表';


-- ================================
-- 建表 6：购物车表
-- ================================
CREATE TABLE IF NOT EXISTS `carts` (
`id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '购物车记录ID',
`customer_id` BIGINT NOT NULL COMMENT '关联的客户/学生id',
`dish_id` BIGINT NOT NULL COMMENT '关联的菜品id',
`added_price` DECIMAL(10, 2) NOT NULL COMMENT '加入购物车时的价格(元)',
`quantity` INT NOT NULL DEFAULT 1 COMMENT '该菜品在购物车中的数量',
`create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
`update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
PRIMARY KEY (`id`),
-- 🚨 核心守护：同一个客户的同一种菜品，在购物车里只能有一条记录
UNIQUE KEY `uk_customer_dish` (`customer_id`, `dish_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='客户购物车表';
















