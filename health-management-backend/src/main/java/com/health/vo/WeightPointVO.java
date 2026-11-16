package com.health.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 体重趋势数据点：某一天的体重（当天多条记录取最新一条）。
 */
@Data
public class WeightPointVO {

    /** 记录日期 */
    private LocalDate date;

    /** 当天体重 kg */
    private BigDecimal weight;
}
