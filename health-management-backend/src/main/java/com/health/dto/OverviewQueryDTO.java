package com.health.dto;

import lombok.Data;

/**
 * 统计中心聚合查询参数。
 */
@Data
public class OverviewQueryDTO {

    /** 图表数据的时间范围：近 N 天（7/30/90），不传默认 7 */
    private Integer days = 7;
}
