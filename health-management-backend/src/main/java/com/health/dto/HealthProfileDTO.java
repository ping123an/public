package com.health.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 健康档案保存请求参数（不含 userId，身份由 JWT 决定）
 */
@Data
public class HealthProfileDTO {

    /** 性别：0-未知 1-男 2-女 */
    @Min(value = 0, message = "性别取值无效")
    @Max(value = 2, message = "性别取值无效")
    private Integer gender;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate birthday;

    @DecimalMin(value = "50", message = "身高不能小于 50cm")
    @DecimalMax(value = "250", message = "身高不能大于 250cm")
    private BigDecimal height;

    /** 目标体重 kg */
    @DecimalMin(value = "20", message = "目标体重不能小于 20kg")
    @DecimalMax(value = "300", message = "目标体重不能大于 300kg")
    private BigDecimal targetWeight;

    /** 每日饮水目标 ml */
    @Min(value = 200, message = "饮水目标不能小于 200ml")
    @Max(value = 10000, message = "饮水目标不能大于 10000ml")
    private Integer dailyWaterGoal;

    /** 每周运动目标 分钟 */
    @Min(value = 0, message = "运动目标不能小于 0 分钟")
    @Max(value = 10080, message = "运动目标不能大于 10080 分钟")
    private Integer weeklyExerciseGoal;
}
