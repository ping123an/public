package com.health.service;

import com.health.dto.OverviewQueryDTO;
import com.health.vo.StatisticsOverviewVO;

/**
 * 统计中心聚合服务：组合体重/运动/饮水各模块统计，保证口径与数据库一致。
 */
public interface StatisticsService {

    /** 聚合概览：今日 + 近7天 + 近30天 + 目标完成 + 指定范围图表数据 */
    StatisticsOverviewVO overview(OverviewQueryDTO query);
}
