package com.health.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 体重变化统计汇总。
 */
@Data
public class WeightStatsVO {

    /** 最新体重 kg */
    private BigDecimal latest;

    /** 上一次体重 kg（无则等于最新） */
    private BigDecimal previous;

    /** 较上次变化 kg（正=增加，负=减少） */
    private BigDecimal change;

    /** 较上次变化率 %（保留 1 位小数） */
    private BigDecimal changeRate;

    /** 区间内最高体重 kg */
    private BigDecimal max;

    /** 区间内最低体重 kg */
    private BigDecimal min;

    /** 区间内平均体重 kg（保留 2 位小数） */
    private BigDecimal average;

    /** 区间累计变化 kg（最新 - 最早） */
    private BigDecimal totalChange;

    /** 目标体重 kg（来自健康档案，可为 null） */
    private BigDecimal targetWeight;

    /** 距目标差值 kg（最新 - 目标，正=超出，负=未达；无目标则为 null） */
    private BigDecimal targetDiff;

    /** 数据点数量（天数） */
    private int count;
}
