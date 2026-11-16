import { get } from '@/utils/request'
import type { StatisticsOverview } from '@/types'

/** 统计中心聚合概览：一次返回今日/近7天/近30天/目标完成/图表数据 */
export function getOverview(days = 7): Promise<StatisticsOverview> {
  return get<StatisticsOverview>('/statistics/overview', { days })
}
