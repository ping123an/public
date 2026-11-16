package com.health.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;

/**
 * 运动记录新增/修改请求。user_id 由 JWT 决定，不接受前端传入。
 */
@Data
public class SportRecordDTO {

    /** 运动类型 */
    @NotBlank(message = "运动类型不能为空")
    @Size(max = 32, message = "运动类型不能超过 32 个字符")
    private String sportType;

    /** 运动时长（分钟），1 ~ 1440 */
    @NotNull(message = "运动时长不能为空")
    @Min(value = 1, message = "运动时长至少 1 分钟")
    @Max(value = 1440, message = "运动时长不能超过 1440 分钟")
    private Integer duration;

    /** 消耗热量（千卡），0 ~ 10000，可不填默认 0 */
    @Min(value = 0, message = "消耗热量不能为负数")
    @Max(value = 10000, message = "消耗热量不能超过 10000 千卡")
    private Integer calories;

    /** 运动日期 */
    @NotNull(message = "运动日期不能为空")
    private LocalDate recordDate;

    /** 备注，最多 255 字符 */
    @Size(max = 255, message = "备注不能超过 255 个字符")
    private String remark;
}
