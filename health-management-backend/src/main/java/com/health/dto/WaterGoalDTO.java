package com.health.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 每日饮水目标设置请求。仅更新 health_profile.daily_water_goal，不影响其他档案字段。
 */
@Data
public class WaterGoalDTO {

    /** 每日饮水目标 ml，200 ~ 10000 */
    @NotNull(message = "饮水目标不能为空")
    @Min(value = 200, message = "饮水目标不能小于 200ml")
    @Max(value = 10000, message = "饮水目标不能大于 10000ml")
    private Integer dailyWaterGoal;
}
