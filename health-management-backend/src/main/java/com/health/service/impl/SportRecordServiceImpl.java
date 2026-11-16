package com.health.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.health.common.PageVO;
import com.health.common.UserContext;
import com.health.dto.RecordQueryDTO;
import com.health.dto.SportRecordDTO;
import com.health.dto.SportStatsQueryDTO;
import com.health.entity.SportRecord;
import com.health.exception.BusinessException;
import com.health.mapper.SportRecordMapper;
import com.health.service.SportRecordService;
import com.health.vo.SportDailyStatsVO;
import com.health.vo.SportRecordVO;
import com.health.vo.SportStatsSummaryVO;
import com.health.vo.SportStatsVO;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * 运动记录业务实现：所有数据访问以 JWT 中的 userId 为条件，实现数据隔离。
 */
@Service
public class SportRecordServiceImpl implements SportRecordService {

    private final SportRecordMapper mapper;

    public SportRecordServiceImpl(SportRecordMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public SportRecordVO create(SportRecordDTO dto) {
        Long userId = UserContext.getUserId();
        SportRecord record = new SportRecord();
        record.setUserId(userId);
        copyFromDto(dto, record);
        mapper.insert(record);
        return toVO(mapper.selectById(record.getId()));
    }

    @Override
    public SportRecordVO update(Long id, SportRecordDTO dto) {
        Long userId = UserContext.getUserId();
        SportRecord record = findOwned(id, userId);
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
    public SportRecordVO getById(Long id) {
        Long userId = UserContext.getUserId();
        return toVO(findOwned(id, userId));
    }

    @Override
    public PageVO<SportRecordVO> page(RecordQueryDTO query) {
        Long userId = UserContext.getUserId();
        LocalDate start = query.getStartDate();
        LocalDate end = query.getEndDate();
        if (start != null && end != null && start.isAfter(end)) {
            throw new BusinessException(400, "开始日期不能晚于结束日期");
        }
        LambdaQueryWrapper<SportRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SportRecord::getUserId, userId)
                .ge(start != null, SportRecord::getRecordDate, start)
                .le(end != null, SportRecord::getRecordDate, end)
                .orderByDesc(SportRecord::getRecordDate)
                .orderByDesc(SportRecord::getId);
        IPage<SportRecord> page = mapper.selectPage(
                new Page<>(query.getCurrent(), query.getSize()), wrapper);
        return PageVO.of(page, this::toVO);
    }

    @Override
    public SportStatsVO stats(SportStatsQueryDTO query) {
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

        LambdaQueryWrapper<SportRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SportRecord::getUserId, userId)
                .ge(start != null, SportRecord::getRecordDate, start)
                .le(end != null, SportRecord::getRecordDate, end)
                .orderByAsc(SportRecord::getRecordDate);
        List<SportRecord> records = mapper.selectList(wrapper);

        return aggregate(records);
    }

    /** 按日期聚合并计算汇总 */
    private SportStatsVO aggregate(List<SportRecord> records) {
        // date -> 聚合容器（时长、热量、条数、类型集合）
        Map<LocalDate, int[]> dailyValues = new TreeMap<>();
        Map<LocalDate, LinkedHashMap<String, Boolean>> dailyTypes = new TreeMap<>();
        Map<String, Integer> typeCounter = new LinkedHashMap<>();

        int totalDuration = 0;
        int totalCalories = 0;

        for (SportRecord record : records) {
            LocalDate date = record.getRecordDate();
            int[] values = dailyValues.computeIfAbsent(date, k -> new int[3]);
            values[0] += nullToZero(record.getDuration());   // 时长
            values[1] += nullToZero(record.getCalories());   // 热量
            values[2] += 1;                                   // 条数

            String type = record.getSportType();
            if (type != null && !type.isBlank()) {
                dailyTypes.computeIfAbsent(date, k -> new LinkedHashMap<>()).put(type, true);
                typeCounter.merge(type, 1, Integer::sum);
            }
            totalDuration += nullToZero(record.getDuration());
            totalCalories += nullToZero(record.getCalories());
        }

        // 每日明细
        List<SportDailyStatsVO> daily = new ArrayList<>();
        for (Map.Entry<LocalDate, int[]> entry : dailyValues.entrySet()) {
            SportDailyStatsVO vo = new SportDailyStatsVO();
            vo.setDate(entry.getKey());
            int[] v = entry.getValue();
            vo.setTotalDuration(v[0]);
            vo.setTotalCalories(v[1]);
            vo.setCount(v[2]);
            LinkedHashMap<String, Boolean> types = dailyTypes.get(entry.getKey());
            vo.setSportTypes(types == null ? List.of() : new ArrayList<>(types.keySet()));
            daily.add(vo);
        }

        // 汇总
        SportStatsSummaryVO summary = new SportStatsSummaryVO();
        summary.setTotalDuration(totalDuration);
        summary.setTotalCalories(totalCalories);
        summary.setTotalCount(records.size());
        int activeDays = dailyValues.size();
        summary.setActiveDays(activeDays);
        if (activeDays > 0) {
            summary.setAvgDuration(BigDecimal.valueOf(totalDuration)
                    .divide(BigDecimal.valueOf(activeDays), 1, RoundingMode.HALF_UP));
        } else {
            summary.setAvgDuration(BigDecimal.ZERO);
        }
        summary.setTopSportType(typeCounter.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(null));

        SportStatsVO result = new SportStatsVO();
        result.setDaily(daily);
        result.setSummary(summary);
        return result;
    }

    private int nullToZero(Integer v) {
        return v == null ? 0 : v;
    }

    private SportRecord findOwned(Long id, Long userId) {
        SportRecord record = mapper.selectById(id);
        if (record == null || !userId.equals(record.getUserId())) {
            throw new BusinessException(404, "记录不存在或无权访问");
        }
        return record;
    }

    private void copyFromDto(SportRecordDTO dto, SportRecord record) {
        record.setSportType(dto.getSportType());
        record.setDuration(dto.getDuration());
        record.setCalories(dto.getCalories() == null ? 0 : dto.getCalories());
        record.setRecordDate(dto.getRecordDate());
        record.setRemark(dto.getRemark());
    }

    private SportRecordVO toVO(SportRecord record) {
        if (record == null) {
            return null;
        }
        SportRecordVO vo = new SportRecordVO();
        vo.setId(record.getId());
        vo.setSportType(record.getSportType());
        vo.setDuration(record.getDuration());
        vo.setCalories(record.getCalories());
        vo.setRecordDate(record.getRecordDate());
        vo.setRemark(record.getRemark());
        vo.setCreateTime(record.getCreateTime());
        return vo;
    }
}
