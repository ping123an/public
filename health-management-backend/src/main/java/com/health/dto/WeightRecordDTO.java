package com.health.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 体重记录新增/修改请求。user_id 由 JWT 决定，不接受前端传入。
 */
@Data
public class WeightRecordDTO {

    /** 体重 kg，合理范围 1 ~ 500 */
    @NotNull(message = "体重不能为空")
    @DecimalMin(value = "1", message = "体重不能小于 1 kg")
    @DecimalMax(value = "500", message = "体重不能大于 500 kg")
    private BigDecimal weight;

    /** 记录日期 */
    @NotNull(message = "记录日期不能为空")
    private LocalDate recordDate;

    /** 备注，最多 255 字符 */
    @Size(max = 255, message = "备注不能超过 255 个字符")
    private String remark;
}
