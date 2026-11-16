package com.health.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 体重记录实体，对应 weight_record 表
 */
@Data
@TableName("weight_record")
public class WeightRecord {

    private Long id;

    /** 用户 ID（始终来自 JWT 上下文） */
    @TableField("user_id")
    private Long userId;

    /** 体重 kg */
    private BigDecimal weight;

    /** 记录日期 */
    @TableField("record_date")
    private LocalDate recordDate;

    /** 备注 */
    private String remark;

    @TableField("create_time")
    private LocalDateTime createTime;
}
