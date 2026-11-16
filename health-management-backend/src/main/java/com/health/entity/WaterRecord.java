package com.health.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 饮水记录实体，对应 water_record 表
 */
@Data
@TableName("water_record")
public class WaterRecord {

    private Long id;

    /** 用户 ID（始终来自 JWT 上下文） */
    @TableField("user_id")
    private Long userId;

    /** 饮水量 ml */
    private Integer amount;

    /** 记录日期 */
    @TableField("record_date")
    private LocalDate recordDate;

    @TableField("create_time")
    private LocalDateTime createTime;
}
