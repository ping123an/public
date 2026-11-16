package com.health.service.impl;

import com.health.dto.OverviewQueryDTO;
import com.health.dto.SportStatsQueryDTO;
import com.health.dto.WaterStatsQueryDTO;
import com.health.dto.WeightTrendQueryDTO;
import com.health.entity.HealthProfile;
import com.health.mapper.HealthProfileMapper;
import com.health.service.SportRecordService;
import com.health.service.StatisticsService;
import com.health.service.WaterRecordService;
import com.health.service.WeightRecordService;
import com.health.common.UserContext;
import com.health.vo.*;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * 统计中心聚合实现：复用各记录模块的 trend/stats 方法，确保统计口径与单独查询完全一致。
 * 所有数据由各 Service 内部按当前 JWT 用户隔离，本类不额外查询用户数据。
 */
@Service
public class StatisticsServiceImpl implements StatisticsService {

    private static final int DEFAULT_WATER_GOAL = 2000;
    private static final int DEFAULT_SPORT_GOAL = 150;

    private final WeightRecordService weightService;
    private final SportRecordService sportService;
    private final WaterRecordService waterService;
    private final HealthProfileMapper profileMapper;

    public StatisticsServiceImpl(WeightRecordService weightService,
                                 SportRecordService sportService,
                                 WaterRecordService waterService,
                                 HealthProfileMapper profileMapper) {
        this.weightService = weightService;
        this.sportService = sportService;
        this.waterService = waterService;
        this.profileMapper = profileMapper;
    }

    @Override
    public StatisticsOverviewVO overview(OverviewQueryDTO query) {
        int chartDays = query.getDays() != null && query.getDays() > 0 ? query.getDays() : 7;

        // 各周期数据（复用现有统计方法，口径一致）
        WeightTrendVO weightAll = weightService.trend(trendQuery(0));
        WeightTrendVO weightWeek = weightService.trend(trendQuery(7));
        WeightTrendVO weightMonth = weightService.trend(trendQuery(30));
        SportStatsVO sportWeek = sportService.stats(statsQuery(7));
        SportStatsVO sportMonth = sportService.stats(statsQuery(30));
        WaterStatsVO waterWeek = waterService.stats(waterQuery(7));
        WaterStatsVO waterMonth = waterService.stats(waterQuery(30));

        StatisticsOverviewVO vo = new StatisticsOverviewVO();
        vo.setToday(buildToday(weightAll, sportWeek, waterWeek));
        vo.setWeek(buildPeriod("近7天", 7, weightWeek, sportWeek, waterWeek));
        vo.setMonth(buildPeriod("近30天", 30, weightMonth, sportMonth, waterMonth));
        vo.setGoals(buildGoals(weightAll, sportWeek, waterWeek));

        // 图表数据（按前端指定范围）
        StatisticsOverviewVO.ChartsVO charts = new StatisticsOverviewVO.ChartsVO();
        charts.setWeight(weightService.trend(trendQuery(chartDays)));
        charts.setSport(sportService.stats(statsQuery(chartDays)));
        charts.setWater(waterService.stats(waterQuery(chartDays)));
        vo.setCharts(charts);
        return vo;
    }

    /** 今日汇总：体重取最近一次，运动/饮水取今天日期的数据 */
    private TodaySummaryVO buildToday(WeightTrendVO weightAll, SportStatsVO sportWeek, WaterStatsVO waterWeek) {
        TodaySummaryVO today = new TodaySummaryVO();
        LocalDate now = LocalDate.now();

        // 体重：取最近一次记录（体重不必每天记录）
        List<WeightPointVO> wPoints = weightAll.getPoints();
        today.setWeight(wPoints.isEmpty() ? null : wPoints.get(wPoints.size() - 1).getWeight());

        // 从近7天明细中找今天
        SportDailyStatsVO todaySport = findDaily(sportWeek.getDaily(), now);
        today.setSportDuration(todaySport == null ? 0 : todaySport.getTotalDuration());
        today.setSportCalories(todaySport == null ? 0 : todaySport.getTotalCalories());
        today.setSportCount(todaySport == null ? 0 : todaySport.getCount());

        WaterDailyStatsVO todayWater = findWaterDaily(waterWeek.getDaily(), now);
        int goal = waterWeek.getSummary() != null && waterWeek.getSummary().getDailyGoal() != null
                ? waterWeek.getSummary().getDailyGoal() : DEFAULT_WATER_GOAL;
        int amount = todayWater == null ? 0 : todayWater.getTotalAmount();
        today.setWaterAmount(amount);
        today.setWaterGoal(goal);
        today.setWaterCompletionRate(goal > 0 ? amount * 100 / goal : 0);
        today.setWaterReached(goal > 0 && amount >= goal);

        today.setHasRecord(todaySport != null || todayWater != null);
        return today;
    }

    /** 周期汇总 */
    private PeriodSummaryVO buildPeriod(String label, int days, WeightTrendVO weight,
                                        SportStatsVO sport, WaterStatsVO water) {
        PeriodSummaryVO vo = new PeriodSummaryVO();
        vo.setLabel(label);
        vo.setDays(days);

        WeightStatsVO ws = weight.getStats();
        if (ws != null) {
            vo.setWeightChange(ws.getTotalChange());
            vo.setLatestWeight(ws.getLatest());
        }

        SportStatsSummaryVO ss = sport.getSummary();
        if (ss != null) {
            vo.setSportDuration(ss.getTotalDuration());
            vo.setSportCalories(ss.getTotalCalories());
            vo.setSportCount(ss.getTotalCount());
        }

        WaterStatsSummaryVO wats = water.getSummary();
        if (wats != null) {
            vo.setWaterAmount(wats.getTotalAmount());
            vo.setWaterReachedDays(wats.getReachedDays());
        }

        // 有记录天数：三类记录日期取并集
        java.util.Set<LocalDate> dates = new java.util.HashSet<>();
        weight.getPoints().forEach(p -> dates.add(p.getDate()));
        sport.getDaily().forEach(d -> dates.add(d.getDate()));
        water.getDaily().forEach(d -> dates.add(d.getDate()));
        vo.setActiveDays(dates.size());
        return vo;
    }

    /** 目标完成情况 */
    private GoalProgressVO buildGoals(WeightTrendVO weightAll, SportStatsVO sportWeek, WaterStatsVO waterWeek) {
        GoalProgressVO vo = new GoalProgressVO();
        Long userId = UserContext.getUserId();
        HealthProfile profile = profileMapper.selectOne(
                new LambdaQueryWrapper<HealthProfile>().eq(HealthProfile::getUserId, userId));

        // 当前体重
        BigDecimal currentWeight = weightAll.getStats() != null ? weightAll.getStats().getLatest() : null;
        vo.setCurrentWeight(currentWeight);

        // 目标体重
        BigDecimal targetWeight = profile != null ? profile.getTargetWeight() : null;
        vo.setTargetWeight(targetWeight);
        if (targetWeight != null && currentWeight != null) {
            vo.setWeightDiff(currentWeight.subtract(targetWeight));
            vo.setWeightReached(currentWeight.compareTo(targetWeight) <= 0);
        } else {
            vo.setWeightReached(false);
        }

        // 饮水目标
        int waterGoal = profile != null && profile.getDailyWaterGoal() != null
                ? profile.getDailyWaterGoal() : DEFAULT_WATER_GOAL;
        int todayWater = 0;
        WaterDailyStatsVO todayW = findWaterDaily(waterWeek.getDaily(), LocalDate.now());
        if (todayW != null) todayWater = todayW.getTotalAmount();
        vo.setWaterGoal(waterGoal);
        vo.setTodayWater(todayWater);
        int waterRate = waterGoal > 0 ? todayWater * 100 / waterGoal : 0;
        vo.setWaterRate(waterRate);
        vo.setWaterReached(waterGoal > 0 && todayWater >= waterGoal);

        // 运动目标（每周）
        int sportGoal = profile != null && profile.getWeeklyExerciseGoal() != null
                ? profile.getWeeklyExerciseGoal() : DEFAULT_SPORT_GOAL;
        int weekSport = sportWeek.getSummary() != null ? sportWeek.getSummary().getTotalDuration() : 0;
        vo.setWeeklyExerciseGoal(sportGoal);
        vo.setWeekSportDuration(weekSport);
        int sportRate = sportGoal > 0 ? weekSport * 100 / sportGoal : 0;
        vo.setSportRate(sportRate);
        vo.setSportReached(sportGoal > 0 && weekSport >= sportGoal);
        return vo;
    }

    private SportDailyStatsVO findDaily(List<SportDailyStatsVO> list, LocalDate date) {
        if (list == null) return null;
        return list.stream().filter(d -> date.equals(d.getDate())).findFirst().orElse(null);
    }

    private WaterDailyStatsVO findWaterDaily(List<WaterDailyStatsVO> list, LocalDate date) {
        if (list == null) return null;
        return list.stream().filter(d -> date.equals(d.getDate())).findFirst().orElse(null);
    }

    private WeightTrendQueryDTO trendQuery(int days) {
        WeightTrendQueryDTO q = new WeightTrendQueryDTO();
        q.setDays(days);
        return q;
    }

    private SportStatsQueryDTO statsQuery(int days) {
        SportStatsQueryDTO q = new SportStatsQueryDTO();
        q.setDays(days);
        return q;
    }

    private WaterStatsQueryDTO waterQuery(int days) {
        WaterStatsQueryDTO q = new WaterStatsQueryDTO();
        q.setDays(days);
        return q;
    }
}
