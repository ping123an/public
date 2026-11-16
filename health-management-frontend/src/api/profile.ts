import { get, put } from '@/utils/request'
import type { HealthProfile, HealthProfileSaveRequest } from '@/types'

/** 获取当前用户健康档案 */
export function getProfile(): Promise<HealthProfile | null> {
  return get<HealthProfile | null>('/profile')
}

/** 保存当前用户健康档案 */
export function saveProfile(data: HealthProfileSaveRequest): Promise<HealthProfile> {
  return put<HealthProfile>('/profile', data)
}
