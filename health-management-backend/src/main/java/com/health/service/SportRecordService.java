package com.health.service;

import com.health.common.PageVO;
import com.health.dto.RecordQueryDTO;
import com.health.dto.SportRecordDTO;
import com.health.dto.SportStatsQueryDTO;
import com.health.vo.SportRecordVO;
import com.health.vo.SportStatsVO;

/**
 * 运动记录业务接口。所有方法隐含当前登录用户上下文，不接收 userId 参数。
 */
public interface SportRecordService {

    SportRecordVO create(SportRecordDTO dto);

    SportRecordVO update(Long id, SportRecordDTO dto);

    void delete(Long id);

    SportRecordVO getById(Long id);

    PageVO<SportRecordVO> page(RecordQueryDTO query);

    /** 运动统计（每日明细 + 区间汇总），仅本人 */
    SportStatsVO stats(SportStatsQueryDTO query);
}
