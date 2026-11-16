package com.health.vo;

import lombok.Data;

/**
 * 统计中心聚合响应：今日汇总 + 近7天 + 近30天 + 目标完成 + 指定范围的图表数据。
 */
@Data
public class StatisticsOverviewVO {

    /** 今日汇总 */
    private TodaySummaryVO today;

    /** 近7天汇总 */
    private PeriodSummaryVO week;

    /** 近30天汇总 */
    private PeriodSummaryVO month;

    /** 健康目标完成情况 */
    private GoalProgressVO goals;

    /** 图表数据（时间范围由 days 参数决定） */
    private ChartsVO charts;

    @Data
    public static class ChartsVO {
        /** 体重趋势 */
        private WeightTrendVO weight;
        /** 运动统计 */
        private SportStatsVO sport;
        /** 饮水统计 */
        private WaterStatsVO water;
    }
}
