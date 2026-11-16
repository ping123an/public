/**
 * 全局 TypeScript 类型定义
 */

/** 统一接口响应结构（与后端 Result 对应） */
export interface ApiResult<T = unknown> {
  code: number
  message: string
  data: T
}

/** 用户信息 */
export interface UserInfo {
  id: number
  username: string
  nickname: string
  email?: string
  avatar?: string | null
}

/** 登录请求参数 */
export interface LoginRequest {
  username: string
  password: string
}

/** 注册请求参数 */
export interface RegisterRequest {
  username: string
  password: string
  nickname?: string
  email?: string
}

/** 登录响应 */
export interface LoginResponse {
  token: string
  userInfo: UserInfo
}

/** 健康档案 */
export interface HealthProfile {
  id?: number
  gender?: number
  birthday?: string
  height?: number | string | null
  targetWeight?: number | string | null
  dailyWaterGoal?: number
  weeklyExerciseGoal?: number
  updateTime?: string
}

/** 健康档案保存参数 */
export interface HealthProfileSaveRequest {
  gender: number
  birthday?: string
  height?: number
  targetWeight?: number
  dailyWaterGoal: number
  weeklyExerciseGoal: number
}

/** 个人资料修改参数 */
export interface UserProfileUpdate {
  nickname?: string
  email?: string
}

/** 通用分页查询参数 */
export interface RecordQuery {
  current: number
  size: number
  startDate?: string
  endDate?: string
}

/** 通用分页响应 */
export interface PageResult<T> {
  records: T[]
  total: number
  current: number
  size: number
  pages: number
}

/** 体重记录 */
export interface WeightRecord {
  id?: number
  weight: number
  recordDate: string
  remark?: string
  createTime?: string
}

/** 运动记录 */
export interface SportRecord {
  id?: number
  sportType: string
  duration: number
  calories?: number
  recordDate: string
  remark?: string
  createTime?: string
}

/** 每日运动统计 */
export interface SportDailyStats {
  date: string
  totalDuration: number
  totalCalories: number
  count: number
  sportTypes: string[]
}

/** 运动统计汇总 */
export interface SportStatsSummary {
  totalDuration: number
  totalCalories: number
  totalCount: number
  activeDays: number
  avgDuration: number
  topSportType?: string | null
}

/** 运动统计聚合结果 */
export interface SportStats {
  daily: SportDailyStats[]
  summary: SportStatsSummary
}

/** 饮水记录 */
export interface WaterRecord {
  id?: number
  amount: number
  recordDate: string
  createTime?: string
}

/** 每日饮水统计 */
export interface WaterDailyStats {
  date: string
  totalAmount: number
  count: number
  completionRate: number
  reached: boolean
}

/** 饮水统计汇总 */
export interface WaterStatsSummary {
  totalAmount: number
  activeDays: number
  avgAmount: number
  reachedDays: number
  dailyGoal: number
}

/** 饮水统计聚合结果 */
export interface WaterStats {
  daily: WaterDailyStats[]
  summary: WaterStatsSummary
}

/** 体重趋势数据点 */
export interface WeightPoint {
  date: string
  weight: number
}

/** 体重变化统计 */
export interface WeightStats {
  latest?: number
  previous?: number
  change?: number
  changeRate?: number
  max?: number
  min?: number
  average?: number
  totalChange?: number
  targetWeight?: number
  targetDiff?: number
  count: number
}

/** 体重趋势聚合结果 */
export interface WeightTrend {
  points: WeightPoint[]
  stats: WeightStats
}

/** 今日健康数据汇总 */
export interface TodaySummary {
  weight?: number
  sportDuration: number
  sportCalories: number
  sportCount: number
  waterAmount: number
  waterGoal: number
  waterCompletionRate: number
  waterReached: boolean
  hasRecord: boolean
}

/** 周期统计汇总 */
export interface PeriodSummary {
  label: string
  days: number
  weightChange?: number
  latestWeight?: number
  sportDuration: number
  sportCalories: number
  sportCount: number
  waterAmount: number
  waterReachedDays: number
  activeDays: number
}

/** 健康目标完成情况 */
export interface GoalProgress {
  targetWeight?: number
  currentWeight?: number
  weightDiff?: number
  weightReached: boolean
  waterGoal: number
  todayWater: number
  waterRate: number
  waterReached: boolean
  weeklyExerciseGoal: number
  weekSportDuration: number
  sportRate: number
  sportReached: boolean
}

/** 统计中心聚合响应 */
export interface StatisticsOverview {
  today: TodaySummary
  week: PeriodSummary
  month: PeriodSummary
  goals: GoalProgress
  charts: {
    weight: WeightTrend
    sport: SportStats
    water: WaterStats
  }
}

/** 后端健康检查响应 */
export interface HealthStatus {
  status: string
  service: string
  time: string
}
