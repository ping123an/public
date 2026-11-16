import { get, post, put, del } from '@/utils/request'
import type { LoginRequest, LoginResponse, RegisterRequest, UserInfo, UserProfileUpdate } from '@/types'

/** 用户注册 */
export function register(data: RegisterRequest): Promise<void> {
  return post<void>('/auth/register', data)
}

/** 用户登录 */
export function login(data: LoginRequest): Promise<LoginResponse> {
  return post<LoginResponse>('/auth/login', data)
}

/** 获取当前登录用户信息 */
export function getCurrentUser(): Promise<UserInfo> {
  return get<UserInfo>('/auth/me')
}

/** 修改个人资料（昵称、邮箱），返回最新用户信息 */
export function updateProfile(data: UserProfileUpdate): Promise<UserInfo> {
  return put<UserInfo>('/auth/profile', data)
}

/** 设置头像（base64 DataURL），返回最新用户信息 */
export function updateAvatar(avatar: string): Promise<UserInfo> {
  return put<UserInfo>('/auth/avatar', { avatar })
}

/** 清除头像，返回最新用户信息 */
export function clearAvatar(): Promise<UserInfo> {
  return del<UserInfo>('/auth/avatar')
}

/** 退出登录 */
export function logout(): Promise<void> {
  return post<void>('/auth/logout')
}

/** 修改密码 */
export function updatePassword(oldPassword: string, newPassword: string): Promise<void> {
  return put<void>('/auth/password', { oldPassword, newPassword })
}
