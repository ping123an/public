-- ============================================================
-- 个人健康管理系统 数据库初始化脚本
-- 数据库：health_manager  |  MySQL 8.0  |  字符集：utf8mb4
-- ============================================================

CREATE DATABASE IF NOT EXISTS health_manager
DEFAULT CHARACTER SET utf8mb4
COLLATE utf8mb4_unicode_ci;

USE health_manager;

-- ----------------------------
-- 1. 用户表 sys_user
-- ----------------------------
DROP TABLE IF EXISTS sys_user;
CREATE TABLE sys_user (
    id           BIGINT       NOT NULL AUTO_INCREMENT COMMENT '用户ID',
    username     VARCHAR(64)  NOT NULL COMMENT '登录账号',
    password     VARCHAR(128) NOT NULL COMMENT '密码（BCrypt 加密）',
    nickname     VARCHAR(64)  DEFAULT NULL COMMENT '昵称',
    email        VARCHAR(128) DEFAULT NULL COMMENT '邮箱',
    avatar       TEXT         DEFAULT NULL COMMENT '头像（base64 DataURL，压缩后存储）',
    create_time  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_sys_user_username (username)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '用户表';

-- ----------------------------
-- 2. 健康档案表 health_profile
-- ----------------------------
DROP TABLE IF EXISTS health_profile;
CREATE TABLE health_profile (
    id           BIGINT       NOT NULL AUTO_INCREMENT COMMENT '档案ID',
    user_id      BIGINT       NOT NULL COMMENT '用户ID',
    gender       TINYINT      DEFAULT 0 COMMENT '性别：0-未知 1-男 2-女',
    birthday     DATE         DEFAULT NULL COMMENT '出生日期',
    height       DECIMAL(5,2) DEFAULT NULL COMMENT '身高（cm）',
    target_weight       DECIMAL(5,2) DEFAULT NULL COMMENT '目标体重（kg）',
    daily_water_goal    INT          DEFAULT 2000 COMMENT '每日饮水目标（ml）',
    weekly_exercise_goal INT         DEFAULT 150 COMMENT '每周运动目标（分钟）',
    create_time  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_health_profile_user (user_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '健康档案表';

-- ----------------------------
-- 3. 体重记录表 weight_record
-- ----------------------------
DROP TABLE IF EXISTS weight_record;
CREATE TABLE weight_record (
    id           BIGINT       NOT NULL AUTO_INCREMENT COMMENT '记录ID',
    user_id      BIGINT       NOT NULL COMMENT '用户ID',
    weight       DECIMAL(5,2) NOT NULL COMMENT '体重（kg）',
    record_date  DATE         NOT NULL COMMENT '记录日期',
    remark       VARCHAR(255) DEFAULT NULL COMMENT '备注',
    create_time  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (id),
    KEY idx_weight_user_date (user_id, record_date)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '体重记录表';

-- ----------------------------
-- 4. 运动记录表 sport_record
-- ----------------------------
DROP TABLE IF EXISTS sport_record;
CREATE TABLE sport_record (
    id           BIGINT       NOT NULL AUTO_INCREMENT COMMENT '记录ID',
    user_id      BIGINT       NOT NULL COMMENT '用户ID',
    sport_type   VARCHAR(32)  NOT NULL COMMENT '运动类型：跑步/步行/骑行/游泳/健身/其他',
    duration     INT          NOT NULL COMMENT '运动时长（分钟）',
    calories     INT          NOT NULL DEFAULT 0 COMMENT '消耗热量（千卡）',
    record_date  DATE         NOT NULL COMMENT '运动日期',
    remark       VARCHAR(255) DEFAULT NULL COMMENT '备注',
    create_time  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (id),
    KEY idx_sport_user_date (user_id, record_date)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '运动记录表';

-- ----------------------------
-- 5. 饮水记录表 water_record
-- ----------------------------
DROP TABLE IF EXISTS water_record;
CREATE TABLE water_record (
    id           BIGINT       NOT NULL AUTO_INCREMENT COMMENT '记录ID',
    user_id      BIGINT       NOT NULL COMMENT '用户ID',
    amount       INT          NOT NULL COMMENT '饮水量（ml）',
    record_date  DATE         NOT NULL COMMENT '记录日期',
    create_time  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (id),
    KEY idx_water_user_date (user_id, record_date)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '饮水记录表';
