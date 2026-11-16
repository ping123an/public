package com.health.vo;

import lombok.Data;

import java.util.List;

/**
 * 运动统计聚合结果：每日明细 + 区间汇总。
 */
@Data
public class SportStatsVO {

    /** 按日期升序的每日统计 */
    private List<SportDailyStatsVO> daily;

    /** 区间汇总 */
    private SportStatsSummaryVO summary;
}
