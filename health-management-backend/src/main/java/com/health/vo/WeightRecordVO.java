package com.health.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 体重记录响应视图，不暴露 userId。
 */
@Data
public class WeightRecordVO {

    private Long id;
    private BigDecimal weight;
    private LocalDate recordDate;
    private String remark;
    private LocalDateTime createTime;
}
