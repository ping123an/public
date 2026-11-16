import { get, post, put, del } from '@/utils/request'
import type {
  PageResult,
  RecordQuery,
  WeightRecord,
  SportRecord,
  WaterRecord,
  WeightTrend,
  SportStats,
  WaterStats,
} from '@/types'

/**
 * 健康记录通用 CRUD API 工厂。
 * 三类记录（体重/运动/饮水）接口结构一致，统一封装减少重复。
 */
type Entity = { id?: number }

function createRecordApi<T extends Entity>(base: string) {
  return {
    /** 分页列表（支持日期区间筛选） */
    list(params: RecordQuery): Promise<PageResult<T>> {
      return get<PageResult<T>>(base, { ...params } as Record<string, unknown>)
    },
    /** 记录详情 */
    detail(id: number): Promise<T> {
      return get<T>(`${base}/${id}`)
    },
    /** 新增记录 */
    create(data: Omit<T, 'id' | 'createTime'>): Promise<T> {
      return post<T>(base, data)
    },
    /** 修改记录 */
    update(id: number, data: Omit<T, 'id' | 'createTime'>): Promise<T> {
      return put<T>(`${base}/${id}`, data)
    },
    /** 删除记录 */
    remove(id: number): Promise<void> {
      return del<void>(`${base}/${id}`)
    },
  }
}

/** 体重趋势分析：days=7/30/90 或 startDate/endDate 自定义区间 */
export function getWeightTrend(params: {
  days?: number
  startDate?: string
  endDate?: string
}): Promise<WeightTrend> {
  return get<WeightTrend>('/records/weight/trend', params as Record<string, unknown>)
}

/** 运动统计：days=7/30/90 或 startDate/endDate 自定义区间 */
export function getSportStats(params: {
  days?: number
  startDate?: string
  endDate?: string
}): Promise<SportStats> {
  return get<SportStats>('/records/sport/stats', params as Record<string, unknown>)
}

/** 饮水统计：days=7/30/90 或 startDate/endDate 自定义区间 */
export function getWaterStats(params: {
  days?: number
  startDate?: string
  endDate?: string
}): Promise<WaterStats> {
  return get<WaterStats>('/records/water/stats', params as Record<string, unknown>)
}

/** 设置每日饮水目标，返回设置后的目标值 */
export function setWaterGoal(dailyWaterGoal: number): Promise<number> {
  return put<number>('/records/water/goal', { dailyWaterGoal })
}

export const weightApi = createRecordApi<WeightRecord>('/records/weight')
export const sportApi = createRecordApi<SportRecord>('/records/sport')
export const waterApi = createRecordApi<WaterRecord>('/records/water')
