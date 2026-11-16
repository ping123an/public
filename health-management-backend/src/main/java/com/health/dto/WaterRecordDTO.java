package com.health.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

/**
 * 饮水记录新增/修改请求。user_id 由 JWT 决定，不接受前端传入。
 */
@Data
public class WaterRecordDTO {

    /** 饮水量 ml，1 ~ 10000 */
    @NotNull(message = "饮水量不能为空")
    @Min(value = 1, message = "饮水量至少 1 ml")
    @Max(value = 10000, message = "单次饮水量不能超过 10000 ml")
    private Integer amount;

    /** 记录日期 */
    @NotNull(message = "记录日期不能为空")
    private LocalDate recordDate;
}
