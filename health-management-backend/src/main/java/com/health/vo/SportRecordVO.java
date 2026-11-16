package com.health.vo;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 运动记录响应视图，不暴露 userId。
 */
@Data
public class SportRecordVO {

    private Long id;
    private String sportType;
    private Integer duration;
    private Integer calories;
    private LocalDate recordDate;
    private String remark;
    private LocalDateTime createTime;
}
