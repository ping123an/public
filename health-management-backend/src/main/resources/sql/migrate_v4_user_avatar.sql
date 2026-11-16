-- ============================================================
-- 增量迁移脚本 V4：为 sys_user 增加头像字段
-- 适用于已初始化的数据库（保留现有数据），可重复执行（幂等）
-- 全新安装请使用 init.sql
-- ============================================================

USE health_manager;

-- 头像（base64 DataURL，前端压缩后上传，TEXT 可容纳）
SET @col_exists = (
    SELECT COUNT(*) FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = 'health_manager'
      AND TABLE_NAME = 'sys_user'
      AND COLUMN_NAME = 'avatar'
);
SET @sql = IF(@col_exists = 0,
    'ALTER TABLE sys_user ADD COLUMN avatar TEXT DEFAULT NULL COMMENT "头像（base64 DataURL，压缩后存储）" AFTER email',
    'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;
