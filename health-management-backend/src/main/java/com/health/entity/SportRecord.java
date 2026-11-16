package com.health.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 运动记录实体，对应 sport_record 表
 */
@Data
@TableName("sport_record")
public class SportRecord {

    private Long id;

    /** 用户 ID（始终来自 JWT 上下文） */
    @TableField("user_id")
    private Long userId;

    /** 运动类型：跑步/步行/骑行/游泳/健身/其他 */
    @TableField("sport_type")
    private String sportType;

    /** 运动时长（分钟） */
    private Integer duration;

    /** 消耗热量（千卡） */
    private Integer calories;

    /** 运动日期 */
    @TableField("record_date")
    private LocalDate recordDate;

    /** 备注 */
    private String remark;

    @TableField("create_time")
    private LocalDateTime createTime;
}
