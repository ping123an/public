import { get } from '@/utils/request'
import type { HealthStatus } from '@/types'

/** 后端健康检查接口（用于验证前后端通信） */
export function ping(): Promise<HealthStatus> {
  return get<HealthStatus>('/health/ping')
}
