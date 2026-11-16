-- ============================================================
-- 增量迁移脚本 V3：为 sport_record 增加消耗热量字段
-- 适用于已初始化的数据库（保留现有数据），可重复执行（幂等）
-- 全新安装请使用 init.sql
-- ============================================================

USE health_manager;

-- 消耗热量（千卡），默认 0，位于 duration 之后
SET @col_exists = (
    SELECT COUNT(*) FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = 'health_manager'
      AND TABLE_NAME = 'sport_record'
      AND COLUMN_NAME = 'calories'
);
SET @sql = IF(@col_exists = 0,
    'ALTER TABLE sport_record ADD COLUMN calories INT NOT NULL DEFAULT 0 COMMENT "消耗热量（千卡）" AFTER duration',
    'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;
