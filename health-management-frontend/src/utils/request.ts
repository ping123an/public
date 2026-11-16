import axios, { type InternalAxiosRequestConfig, type AxiosResponse } from 'axios'
import { ElMessage } from 'element-plus'
import type { ApiResult } from '@/types'

/**
 * 统一 HTTP 请求封装：
 * - baseURL 指向后端（开发期由 Vite 代理 /api -> localhost:8080）
 * - 请求拦截器自动携带 JWT Token
 * - 响应拦截器统一处理业务状态码、401 过期跳转和错误提示
 */
const request = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || '/api',
  timeout: 10000,
})

/** 从本地存储读取 Token 的 key */
export const TOKEN_KEY = 'health_manager_token'

/** 防止多个 401 并发时重复跳转 */
let redirecting = false

/** Token 失效统一处理：清除本地状态并跳转登录页 */
function handleUnauthorized(message: string) {
  if (redirecting) return
  redirecting = true
  localStorage.removeItem(TOKEN_KEY)
  ElMessage.error(message)
  const currentPath = window.location.pathname + window.location.search
  const redirect = currentPath.startsWith('/login') ? '' : `?redirect=${encodeURIComponent(currentPath)}`
  window.location.href = `/login${redirect}`
  // 兜底：若跳转未立即生效，延迟复位标记
  window.setTimeout(() => {
    redirecting = false
  }, 1500)
}

request.interceptors.request.use(
  (config: InternalAxiosRequestConfig) => {
    const token = localStorage.getItem(TOKEN_KEY)
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  (error) => Promise.reject(error),
)

request.interceptors.response.use(
  (response: AxiosResponse<ApiResult>) => {
    const res = response.data
    if (res && typeof res === 'object' && 'code' in res) {
      if (res.code === 200) {
        return res as unknown as AxiosResponse<ApiResult>
      }
      // 业务码 401（后端正常以 HTTP 401 返回，这里是兜底）
      if (res.code === 401) {
        handleUnauthorized('登录已过期，请重新登录')
        return Promise.reject(new Error(res.message))
      }
      ElMessage.error(res.message || '请求失败')
      return Promise.reject(new Error(res.message || '请求失败'))
    }
    return response
  },
  (error) => {
    const status = error.response?.status
    if (status === 401) {
      const message = error.response?.data?.message || '登录已过期，请重新登录'
      handleUnauthorized(message)
    } else if (status === 403) {
      ElMessage.error('没有访问权限')
    } else {
      ElMessage.error(error.response?.data?.message || error.message || '网络异常')
    }
    return Promise.reject(error)
  },
)

/** 泛型 GET 请求，返回业务 data */
export function get<T>(url: string, params?: Record<string, unknown>): Promise<T> {
  return request.get<ApiResult<T>>(url, { params }).then((res) => (res as unknown as ApiResult<T>).data)
}

/** 泛型 POST 请求，返回业务 data */
export function post<T>(url: string, data?: unknown): Promise<T> {
  return request.post<ApiResult<T>>(url, data).then((res) => (res as unknown as ApiResult<T>).data)
}

/** 泛型 PUT 请求，返回业务 data */
export function put<T>(url: string, data?: unknown): Promise<T> {
  return request.put<ApiResult<T>>(url, data).then((res) => (res as unknown as ApiResult<T>).data)
}

/** 泛型 DELETE 请求，返回业务 data */
export function del<T>(url: string, params?: Record<string, unknown>): Promise<T> {
  return request.delete<ApiResult<T>>(url, { params }).then((res) => (res as unknown as ApiResult<T>).data)
}

export default request
