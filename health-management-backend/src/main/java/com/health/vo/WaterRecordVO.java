package com.health.vo;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 饮水记录响应视图，不暴露 userId。
 */
@Data
public class WaterRecordVO {

    private Long id;
    private Integer amount;
    private LocalDate recordDate;
    private LocalDateTime createTime;
}
