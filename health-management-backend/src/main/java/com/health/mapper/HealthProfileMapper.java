package com.health.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.health.entity.HealthProfile;
import org.apache.ibatis.annotations.Mapper;

/**
 * 健康档案数据访问层
 */
@Mapper
public interface HealthProfileMapper extends BaseMapper<HealthProfile> {
}
