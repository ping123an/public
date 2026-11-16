-- ============================================================
-- 增量迁移脚本 V2：为 health_profile 增加健康目标字段
-- 适用于已初始化的数据库（保留现有数据），可重复执行（幂等）
-- 全新安装请使用 init.sql
-- ============================================================

USE health_manager;

-- 目标体重（kg）
SET @col_exists = (
    SELECT COUNT(*) FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = 'health_manager'
      AND TABLE_NAME = 'health_profile'
      AND COLUMN_NAME = 'target_weight'
);
SET @sql = IF(@col_exists = 0,
    'ALTER TABLE health_profile ADD COLUMN target_weight DECIMAL(5,2) DEFAULT NULL COMMENT "目标体重（kg）" AFTER height',
    'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- 每日饮水目标（ml）
SET @col_exists = (
    SELECT COUNT(*) FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = 'health_manager'
      AND TABLE_NAME = 'health_profile'
      AND COLUMN_NAME = 'daily_water_goal'
);
SET @sql = IF(@col_exists = 0,
    'ALTER TABLE health_profile ADD COLUMN daily_water_goal INT DEFAULT 2000 COMMENT "每日饮水目标（ml）" AFTER target_weight',
    'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- 每周运动目标（分钟）
SET @col_exists = (
    SELECT COUNT(*) FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = 'health_manager'
      AND TABLE_NAME = 'health_profile'
      AND COLUMN_NAME = 'weekly_exercise_goal'
);
SET @sql = IF(@col_exists = 0,
    'ALTER TABLE health_profile ADD COLUMN weekly_exercise_goal INT DEFAULT 150 COMMENT "每周运动目标（分钟）" AFTER daily_water_goal',
    'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;
