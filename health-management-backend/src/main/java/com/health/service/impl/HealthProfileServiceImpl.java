package com.health.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.health.common.UserContext;
import com.health.dto.HealthProfileDTO;
import com.health.entity.HealthProfile;
import com.health.mapper.HealthProfileMapper;
import com.health.service.HealthProfileService;
import com.health.vo.HealthProfileVO;
import org.springframework.stereotype.Service;

/**
 * 健康档案业务实现：所有数据访问均以 JWT 上下文中的 userId 为条件，
 * 确保用户只能操作自己的档案，实现数据隔离。
 */
@Service
public class HealthProfileServiceImpl implements HealthProfileService {

    private final HealthProfileMapper mapper;

    public HealthProfileServiceImpl(HealthProfileMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public HealthProfileVO getCurrentProfile() {
        Long userId = UserContext.getUserId();
        HealthProfile profile = mapper.selectOne(new LambdaQueryWrapper<HealthProfile>()
                .eq(HealthProfile::getUserId, userId));
        return toVO(profile);
    }

    @Override
    public HealthProfileVO save(HealthProfileDTO dto) {
        // 用户 ID 只从 JWT 上下文获取，忽略任何前端传入的 user_id
        Long userId = UserContext.getUserId();
        HealthProfile profile = mapper.selectOne(new LambdaQueryWrapper<HealthProfile>()
                .eq(HealthProfile::getUserId, userId));
        if (profile == null) {
            profile = new HealthProfile();
            profile.setUserId(userId);
        }
        profile.setGender(dto.getGender() == null ? 0 : dto.getGender());
        profile.setBirthday(dto.getBirthday());
        profile.setHeight(dto.getHeight());
        // 目标字段：未设置时给出合理默认值，保证档案始终有可用的健康目标
        profile.setTargetWeight(dto.getTargetWeight());
        profile.setDailyWaterGoal(dto.getDailyWaterGoal() == null ? 2000 : dto.getDailyWaterGoal());
        profile.setWeeklyExerciseGoal(dto.getWeeklyExerciseGoal() == null ? 150 : dto.getWeeklyExerciseGoal());

        if (profile.getId() == null) {
            mapper.insert(profile);
        } else {
            mapper.updateById(profile);
        }
        return toVO(mapper.selectById(profile.getId()));
    }

    private HealthProfileVO toVO(HealthProfile profile) {
        if (profile == null) {
            return null;
        }
        HealthProfileVO vo = new HealthProfileVO();
        vo.setId(profile.getId());
        vo.setGender(profile.getGender());
        vo.setBirthday(profile.getBirthday());
        vo.setHeight(profile.getHeight());
        vo.setTargetWeight(profile.getTargetWeight());
        vo.setDailyWaterGoal(profile.getDailyWaterGoal());
        vo.setWeeklyExerciseGoal(profile.getWeeklyExerciseGoal());
        vo.setUpdateTime(profile.getUpdateTime());
        return vo;
    }
}
