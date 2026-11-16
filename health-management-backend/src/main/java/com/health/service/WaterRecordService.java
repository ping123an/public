package com.health.service;

import com.health.common.PageVO;
import com.health.dto.RecordQueryDTO;
import com.health.dto.WaterGoalDTO;
import com.health.dto.WaterRecordDTO;
import com.health.dto.WaterStatsQueryDTO;
import com.health.vo.WaterRecordVO;
import com.health.vo.WaterStatsVO;

/**
 * 饮水记录业务接口。所有方法隐含当前登录用户上下文，不接收 userId 参数。
 */
public interface WaterRecordService {

    WaterRecordVO create(WaterRecordDTO dto);

    WaterRecordVO update(Long id, WaterRecordDTO dto);

    void delete(Long id);

    WaterRecordVO getById(Long id);

    PageVO<WaterRecordVO> page(RecordQueryDTO query);

    /** 设置当前用户每日饮水目标，返回设置后的目标值 */
    Integer setGoal(WaterGoalDTO dto);

    /** 饮水统计（每日明细 + 汇总，含目标完成率），仅本人 */
    WaterStatsVO stats(WaterStatsQueryDTO query);
}
