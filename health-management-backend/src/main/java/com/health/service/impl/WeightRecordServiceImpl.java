package com.health.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.health.common.PageVO;
import com.health.common.UserContext;
import com.health.dto.RecordQueryDTO;
import com.health.dto.WeightRecordDTO;
import com.health.dto.WeightTrendQueryDTO;
import com.health.entity.HealthProfile;
import com.health.entity.WeightRecord;
import com.health.exception.BusinessException;
import com.health.mapper.HealthProfileMapper;
import com.health.mapper.WeightRecordMapper;
import com.health.service.WeightRecordService;
import com.health.vo.WeightPointVO;
import com.health.vo.WeightRecordVO;
import com.health.vo.WeightStatsVO;
import com.health.vo.WeightTrendVO;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * 体重记录业务实现：所有数据访问都以 JWT 上下文中的 userId 为条件，
 * 确保用户只能操作自己的记录，实现数据隔离与越权防护。
 */
@Service
public class WeightRecordServiceImpl implements WeightRecordService {

    private final WeightRecordMapper mapper;
    private final HealthProfileMapper profileMapper;

    public WeightRecordServiceImpl(WeightRecordMapper mapper, HealthProfileMapper profileMapper) {
        this.mapper = mapper;
        this.profileMapper = profileMapper;
    }

    @Override
    public WeightRecordVO create(WeightRecordDTO dto) {
        Long userId = UserContext.getUserId();
        WeightRecord record = new WeightRecord();
        record.setUserId(userId);
        copyFromDto(dto, record);
        mapper.insert(record);
        return toVO(mapper.selectById(record.getId()));
    }

    @Override
    public WeightRecordVO update(Long id, WeightRecordDTO dto) {
        Long userId = UserContext.getUserId();
        WeightRecord record = findOwned(id, userId);
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
    public WeightRecordVO getById(Long id) {
        Long userId = UserContext.getUserId();
        return toVO(findOwned(id, userId));
    }

    @Override
    public PageVO<WeightRecordVO> page(RecordQueryDTO query) {
        Long userId = UserContext.getUserId();
        LocalDate start = query.getStartDate();
        LocalDate end = query.getEndDate();
        if (start != null && end != null && start.isAfter(end)) {
            throw new BusinessException(400, "开始日期不能晚于结束日期");
        }
        LambdaQueryWrapper<WeightRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(WeightRecord::getUserId, userId)
                .ge(start != null, WeightRecord::getRecordDate, start)
                .le(end != null, WeightRecord::getRecordDate, end)
                .orderByDesc(WeightRecord::getRecordDate)
                .orderByDesc(WeightRecord::getId);
        IPage<WeightRecord> page = mapper.selectPage(
                new Page<>(query.getCurrent(), query.getSize()), wrapper);
        return PageVO.of(page, this::toVO);
    }

    @Override
    public WeightTrendVO trend(WeightTrendQueryDTO query) {
        Long userId = UserContext.getUserId();

        // 解析时间范围：days 优先，其次自定义日期区间，都不传则查全部
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

        LambdaQueryWrapper<WeightRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(WeightRecord::getUserId, userId)
                .ge(start != null, WeightRecord::getRecordDate, start)
                .le(end != null, WeightRecord::getRecordDate, end)
                .orderByAsc(WeightRecord::getRecordDate)
                .orderByAsc(WeightRecord::getId);
        List<WeightRecord> records = mapper.selectList(wrapper);

        // 按日期聚合，同一天取最后一次记录（id 最大，因已按 id 升序，put 覆盖即可）
        Map<LocalDate, BigDecimal> byDate = new TreeMap<>();
        for (WeightRecord record : records) {
            byDate.put(record.getRecordDate(), record.getWeight());
        }

        List<WeightPointVO> points = new ArrayList<>();
        for (Map.Entry<LocalDate, BigDecimal> entry : byDate.entrySet()) {
            WeightPointVO point = new WeightPointVO();
            point.setDate(entry.getKey());
            point.setWeight(entry.getValue());
            points.add(point);
        }

        WeightTrendVO vo = new WeightTrendVO();
        vo.setPoints(points);
        vo.setStats(buildStats(points, userId));
        return vo;
    }

    /** 基于升序数据点计算变化统计 */
    private WeightStatsVO buildStats(List<WeightPointVO> points, Long userId) {
        WeightStatsVO stats = new WeightStatsVO();
        stats.setCount(points.size());
        if (points.isEmpty()) {
            return stats;
        }

        BigDecimal first = points.get(0).getWeight();
        BigDecimal latest = points.get(points.size() - 1).getWeight();
        stats.setLatest(latest);

        BigDecimal previous = points.size() >= 2 ? points.get(points.size() - 2).getWeight() : latest;
        stats.setPrevious(previous);

        BigDecimal change = latest.subtract(previous);
        stats.setChange(change);
        // 变化率 = 变化 / 上次 * 100，避免除零
        if (previous.compareTo(BigDecimal.ZERO) != 0) {
            stats.setChangeRate(change.multiply(BigDecimal.valueOf(100))
                    .divide(previous, 1, RoundingMode.HALF_UP));
        } else {
            stats.setChangeRate(BigDecimal.ZERO);
        }

        BigDecimal totalChange = latest.subtract(first);
        stats.setTotalChange(totalChange);

        Comparator<WeightPointVO> byWeight = Comparator.comparing(WeightPointVO::getWeight);
        BigDecimal max = points.stream().max(byWeight).map(WeightPointVO::getWeight).orElse(latest);
        BigDecimal min = points.stream().min(byWeight).map(WeightPointVO::getWeight).orElse(latest);
        stats.setMax(max);
        stats.setMin(min);

        BigDecimal sum = points.stream().map(WeightPointVO::getWeight)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        stats.setAverage(sum.divide(BigDecimal.valueOf(points.size()), 2, RoundingMode.HALF_UP));

        // 目标体重来自当前用户健康档案
        HealthProfile profile = profileMapper.selectOne(
                new LambdaQueryWrapper<HealthProfile>().eq(HealthProfile::getUserId, userId));
        if (profile != null && profile.getTargetWeight() != null) {
            stats.setTargetWeight(profile.getTargetWeight());
            stats.setTargetDiff(latest.subtract(profile.getTargetWeight()));
        }
        return stats;
    }

    /** 按 id + userId 查询，找不到视为记录不存在或无权访问 */
    private WeightRecord findOwned(Long id, Long userId) {
        WeightRecord record = mapper.selectById(id);
        if (record == null || !userId.equals(record.getUserId())) {
            throw new BusinessException(404, "记录不存在或无权访问");
        }
        return record;
    }

    private void copyFromDto(WeightRecordDTO dto, WeightRecord record) {
        record.setWeight(dto.getWeight());
        record.setRecordDate(dto.getRecordDate());
        record.setRemark(dto.getRemark());
    }

    private WeightRecordVO toVO(WeightRecord record) {
        if (record == null) {
            return null;
        }
        WeightRecordVO vo = new WeightRecordVO();
        vo.setId(record.getId());
        vo.setWeight(record.getWeight());
        vo.setRecordDate(record.getRecordDate());
        vo.setRemark(record.getRemark());
        vo.setCreateTime(record.getCreateTime());
        return vo;
    }
}
