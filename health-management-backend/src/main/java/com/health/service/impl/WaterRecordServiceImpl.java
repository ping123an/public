package com.health.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.health.common.PageVO;
import com.health.common.UserContext;
import com.health.dto.RecordQueryDTO;
import com.health.dto.WaterGoalDTO;
import com.health.dto.WaterRecordDTO;
import com.health.dto.WaterStatsQueryDTO;
import com.health.entity.HealthProfile;
import com.health.entity.WaterRecord;
import com.health.exception.BusinessException;
import com.health.mapper.HealthProfileMapper;
import com.health.mapper.WaterRecordMapper;
import com.health.service.WaterRecordService;
import com.health.vo.WaterDailyStatsVO;
import com.health.vo.WaterRecordVO;
import com.health.vo.WaterStatsSummaryVO;
import com.health.vo.WaterStatsVO;
import org.springframework.stereotype.Service;

import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * 饮水记录业务实现：所有数据访问以 JWT 中的 userId 为条件，实现数据隔离。
 */
@Service
public class WaterRecordServiceImpl implements WaterRecordService {

    private static final int DEFAULT_GOAL = 2000;

    private final WaterRecordMapper mapper;
    private final HealthProfileMapper profileMapper;

    public WaterRecordServiceImpl(WaterRecordMapper mapper, HealthProfileMapper profileMapper) {
        this.mapper = mapper;
        this.profileMapper = profileMapper;
    }

    @Override
    public WaterRecordVO create(WaterRecordDTO dto) {
        Long userId = UserContext.getUserId();
        WaterRecord record = new WaterRecord();
        record.setUserId(userId);
        copyFromDto(dto, record);
        mapper.insert(record);
        return toVO(mapper.selectById(record.getId()));
    }

    @Override
    public WaterRecordVO update(Long id, WaterRecordDTO dto) {
        Long userId = UserContext.getUserId();
        WaterRecord record = findOwned(id, userId);
        copyFromDto(dto, record);
        mapper.updateById(record);
        return toVO(mapper.selectById(id));
    }

    @Override
    public void delete(Long id) {
        Long userId = UserContext.getUserId();
        findOwned(id, userId);
        mapper.deleteById(id);
    }

    @Override
    public WaterRecordVO getById(Long id) {
        Long userId = UserContext.getUserId();
        return toVO(findOwned(id, userId));
    }

    @Override
    public PageVO<WaterRecordVO> page(RecordQueryDTO query) {
        Long userId = UserContext.getUserId();
        LocalDate start = query.getStartDate();
        LocalDate end = query.getEndDate();
        if (start != null && end != null && start.isAfter(end)) {
            throw new BusinessException(400, "开始日期不能晚于结束日期");
        }
        LambdaQueryWrapper<WaterRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(WaterRecord::getUserId, userId)
                .ge(start != null, WaterRecord::getRecordDate, start)
                .le(end != null, WaterRecord::getRecordDate, end)
                .orderByDesc(WaterRecord::getRecordDate)
                .orderByDesc(WaterRecord::getId);
        IPage<WaterRecord> page = mapper.selectPage(
                new Page<>(query.getCurrent(), query.getSize()), wrapper);
        return PageVO.of(page, this::toVO);
    }

    @Override
    public Integer setGoal(WaterGoalDTO dto) {
        Long userId = UserContext.getUserId();
        HealthProfile profile = profileMapper.selectOne(
                new LambdaQueryWrapper<HealthProfile>().eq(HealthProfile::getUserId, userId));
        if (profile == null) {
            // 用户尚无健康档案，仅创建包含饮水目标的档案，其余字段保持默认
            profile = new HealthProfile();
            profile.setUserId(userId);
            profile.setGender(0);
        }
        profile.setDailyWaterGoal(dto.getDailyWaterGoal());
        if (profile.getId() == null) {
            profileMapper.insert(profile);
        } else {
            profileMapper.updateById(profile);
        }
        return profile.getDailyWaterGoal();
    }

    @Override
    public WaterStatsVO stats(WaterStatsQueryDTO query) {
        Long userId = UserContext.getUserId();

        // 解析时间范围：days 优先，其次自定义日期区间，都不传查全部
        LocalDate start = null;
        LocalDate end = null;
        Integer days = query.getDays();
        if (days != null && days > 0) {
            end = LocalDate.now();
            start = end.minusDays(days - 1L);
        } else {
            start = query.getStartDate();
            end = query.getEndDate();
        }
        if (start != null && end != null && start.isAfter(end)) {
            throw new BusinessException(400, "开始日期不能晚于结束日期");
        }

        LambdaQueryWrapper<WaterRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(WaterRecord::getUserId, userId)
                .ge(start != null, WaterRecord::getRecordDate, start)
                .le(end != null, WaterRecord::getRecordDate, end)
                .orderByAsc(WaterRecord::getRecordDate);
        List<WaterRecord> records = mapper.selectList(wrapper);

        // 读取当前饮水目标
        HealthProfile profile = profileMapper.selectOne(
                new LambdaQueryWrapper<HealthProfile>().eq(HealthProfile::getUserId, userId));
        int goal = (profile != null && profile.getDailyWaterGoal() != null)
                ? profile.getDailyWaterGoal() : DEFAULT_GOAL;

        return aggregate(records, goal);
    }

    /** 按日期聚合并计算完成率与汇总 */
    private WaterStatsVO aggregate(List<WaterRecord> records, int goal) {
        // date -> [总量, 次数]
        Map<LocalDate, int[]> byDate = new TreeMap<>();
        for (WaterRecord record : records) {
            int[] v = byDate.computeIfAbsent(record.getRecordDate(), k -> new int[2]);
            v[0] += nullToZero(record.getAmount());
            v[1] += 1;
        }

        List<WaterDailyStatsVO> daily = new ArrayList<>();
        int reachedDays = 0;
        int totalAmount = 0;
        for (Map.Entry<LocalDate, int[]> entry : byDate.entrySet()) {
            int[] v = entry.getValue();
            int amount = v[0];
            totalAmount += amount;
            WaterDailyStatsVO vo = new WaterDailyStatsVO();
            vo.setDate(entry.getKey());
            vo.setTotalAmount(amount);
            vo.setCount(v[1]);
            boolean reached = goal > 0 && amount >= goal;
            vo.setReached(reached);
            vo.setCompletionRate(goal > 0
                    ? java.math.BigDecimal.valueOf(amount).multiply(java.math.BigDecimal.valueOf(100))
                            .divide(java.math.BigDecimal.valueOf(goal), 0, RoundingMode.HALF_UP).intValue()
                    : 0);
            if (reached) {
                reachedDays++;
            }
            daily.add(vo);
        }

        int activeDays = byDate.size();
        WaterStatsSummaryVO summary = new WaterStatsSummaryVO();
        summary.setTotalAmount(totalAmount);
        summary.setActiveDays(activeDays);
        summary.setAvgAmount(activeDays > 0
                ? java.math.BigDecimal.valueOf(totalAmount)
                        .divide(java.math.BigDecimal.valueOf(activeDays), 0, RoundingMode.HALF_UP).intValue()
                : 0);
        summary.setReachedDays(reachedDays);
        summary.setDailyGoal(goal);

        WaterStatsVO result = new WaterStatsVO();
        result.setDaily(daily);
        result.setSummary(summary);
        return result;
    }

    private int nullToZero(Integer v) {
        return v == null ? 0 : v;
    }

    private WaterRecord findOwned(Long id, Long userId) {
        WaterRecord record = mapper.selectById(id);
        if (record == null || !userId.equals(record.getUserId())) {
            throw new BusinessException(404, "记录不存在或无权访问");
        }
        return record;
    }

    private void copyFromDto(WaterRecordDTO dto, WaterRecord record) {
        record.setAmount(dto.getAmount());
        record.setRecordDate(dto.getRecordDate());
    }

    private WaterRecordVO toVO(WaterRecord record) {
        if (record == null) {
            return null;
        }
        WaterRecordVO vo = new WaterRecordVO();
        vo.setId(record.getId());
        vo.setAmount(record.getAmount());
        vo.setRecordDate(record.getRecordDate());
        vo.setCreateTime(record.getCreateTime());
        return vo;
    }
}
