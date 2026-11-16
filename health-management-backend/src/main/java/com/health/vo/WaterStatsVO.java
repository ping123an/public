package com.health.vo;

import lombok.Data;

import java.util.List;

/**
 * 饮水统计聚合结果：每日明细 + 区间汇总。
 */
@Data
public class WaterStatsVO {

    /** 按日期升序的每日统计 */
    private List<WaterDailyStatsVO> daily;

    /** 区间汇总 */
    private WaterStatsSummaryVO summary;
}
