package com.health.service;

import com.health.dto.HealthProfileDTO;
import com.health.vo.HealthProfileVO;

/**
 * 健康档案业务逻辑层
 */
public interface HealthProfileService {

    /** 获取当前用户的档案，无档案时返回 null */
    HealthProfileVO getCurrentProfile();

    /** 保存（新增或更新）当前用户的档案 */
    HealthProfileVO save(HealthProfileDTO dto);
}
