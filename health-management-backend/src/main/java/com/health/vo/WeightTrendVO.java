package com.health.vo;

import lombok.Data;

import java.util.List;

/**
 * 体重趋势聚合结果：按日期升序的数据点 + 变化统计。
 */
@Data
public class WeightTrendVO {

    /** 趋势数据点（按日期升序） */
    private List<WeightPointVO> points;

    /** 变化统计汇总 */
    private WeightStatsVO stats;
}
