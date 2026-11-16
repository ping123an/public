import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { TOKEN_KEY } from '@/utils/request'
import {
  getCurrentUser,
  login,
  logout as logoutApi,
  register,
  updateProfile as updateProfileApi,
  updateAvatar as updateAvatarApi,
  clearAvatar as clearAvatarApi,
} from '@/api/auth'
import type { LoginRequest, RegisterRequest, UserInfo, UserProfileUpdate } from '@/types'

/**
 * 用户状态管理：登录 Token、用户信息与登录态
 */
export const useUserStore = defineStore('user', () => {
  const token = ref<string>(localStorage.getItem(TOKEN_KEY) || '')
  const userInfo = ref<UserInfo | null>(null)

  const isLoggedIn = computed(() => !!token.value)

  function setToken(value: string) {
    token.value = value
    localStorage.setItem(TOKEN_KEY, value)
  }

  function setUserInfo(info: UserInfo | null) {
    userInfo.value = info
  }

  /** 注册（注册成功后不自动登录，返回登录页由用户手动登录） */
  async function registerAccount(data: RegisterRequest): Promise<void> {
    await register(data)
  }

  /** 登录：保存 Token 与用户信息 */
  async function loginAccount(data: LoginRequest): Promise<void> {
    const res = await login(data)
    setToken(res.token)
    setUserInfo(res.userInfo)
  }

  /** 从后端加载最新用户信息 */
  async function loadUserInfo(): Promise<void> {
    if (!token.value) return
    try {
      const info = await getCurrentUser()
      setUserInfo(info)
    } catch {
      // 401 已由请求拦截器统一处理（清 token + 跳转登录），这里无需重复操作
    }
  }

  /** 修改个人资料并同步本地用户信息 */
  async function updateProfile(data: UserProfileUpdate): Promise<void> {
    const info = await updateProfileApi(data)
    setUserInfo(info)
  }

  /** 设置头像并同步本地用户信息 */
  async function updateAvatar(data: string): Promise<void> {
    const info = await updateAvatarApi(data)
    setUserInfo(info)
  }

  /** 清除头像并同步本地用户信息 */
  async function clearAvatar(): Promise<void> {
    const info = await clearAvatarApi()
    setUserInfo(info)
  }

  /** 退出登录：通知后端并清除本地状态 */
  async function logoutAccount(): Promise<void> {
    try {
      await logoutApi()
    } catch {
      // 退出接口失败也要清除本地登录态
    } finally {
      token.value = ''
      userInfo.value = null
      localStorage.removeItem(TOKEN_KEY)
    }
  }

  return {
    token,
    userInfo,
    isLoggedIn,
    setToken,
    setUserInfo,
    registerAccount,
    loginAccount,
    loadUserInfo,
    updateProfile,
    updateAvatar,
    clearAvatar,
    logoutAccount,
  }
})
