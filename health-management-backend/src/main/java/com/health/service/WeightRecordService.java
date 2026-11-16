package com.health.service;

import com.health.common.PageVO;
import com.health.dto.RecordQueryDTO;
import com.health.dto.WeightRecordDTO;
import com.health.dto.WeightTrendQueryDTO;
import com.health.vo.WeightRecordVO;
import com.health.vo.WeightTrendVO;

/**
 * 体重记录业务接口。所有方法隐含当前登录用户上下文，不接收 userId 参数。
 */
public interface WeightRecordService {

    /** 新增记录 */
    WeightRecordVO create(WeightRecordDTO dto);

    /** 修改记录（仅本人） */
    WeightRecordVO update(Long id, WeightRecordDTO dto);

    /** 删除记录（仅本人） */
    void delete(Long id);

    /** 记录详情（仅本人） */
    WeightRecordVO getById(Long id);

    /** 分页查询（仅本人，支持日期区间） */
    PageVO<WeightRecordVO> page(RecordQueryDTO query);

    /** 体重趋势分析（按日期聚合 + 变化统计），仅本人 */
    WeightTrendVO trend(WeightTrendQueryDTO query);
}
