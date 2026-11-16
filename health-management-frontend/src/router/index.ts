import { createRouter, createWebHistory, type RouteRecordRaw } from 'vue-router'
import MainLayout from '@/layouts/MainLayout.vue'
import { TOKEN_KEY } from '@/utils/request'

/**
 * 路由配置
 * public: true 的页面无需登录即可访问，其余页面受路由守卫保护
 */
const routes: RouteRecordRaw[] = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/pages/login/Login.vue'),
    meta: { title: '登录', public: true },
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('@/pages/login/Register.vue'),
    meta: { title: '注册', public: true },
  },
  {
    path: '/',
    component: MainLayout,
    redirect: '/dashboard',
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('@/pages/dashboard/Dashboard.vue'),
        meta: { title: '健康首页' },
      },
      {
        path: 'profile',
        name: 'Profile',
        component: () => import('@/pages/profile/Profile.vue'),
        meta: { title: '健康档案' },
      },
      {
        path: 'user-center',
        name: 'UserCenter',
        component: () => import('@/pages/profile/UserCenter.vue'),
        meta: { title: '个人中心' },
      },
      {
        path: 'weight',
        name: 'Weight',
        component: () => import('@/pages/weight/Weight.vue'),
        meta: { title: '体重管理' },
      },
      {
        path: 'sport',
        name: 'Sport',
        component: () => import('@/pages/sport/Sport.vue'),
        meta: { title: '运动管理' },
      },
      {
        path: 'water',
        name: 'Water',
        component: () => import('@/pages/water/Water.vue'),
        meta: { title: '饮水管理' },
      },
      {
        path: 'statistics',
        name: 'Statistics',
        component: () => import('@/pages/statistics/Statistics.vue'),
        meta: { title: '数据统计' },
      },
      {
        path: ':pathMatch(.*)*',
        name: 'NotFound',
        component: () => import('@/components/NotFound.vue'),
        meta: { title: '页面不存在' },
      },
    ],
  },
]

const router = createRouter({
  history: createWebHistory(),
  routes,
})

/** 路由权限守卫 */
router.beforeEach((to) => {
  const token = localStorage.getItem(TOKEN_KEY)
  const isPublic = to.meta.public === true

  if (!isPublic && !token) {
    // 未登录访问受保护页面：跳转登录，并记录来源路径
    return { path: '/login', query: { redirect: to.fullPath } }
  }
  if (isPublic && token && (to.path === '/login' || to.path === '/register')) {
    // 已登录访问登录/注册页：跳转首页
    return { path: '/dashboard' }
  }
  return true
})

// 路由标题
router.afterEach((to) => {
  const title = to.meta.title as string | undefined
  document.title = title ? `${title} - 健康管理系统` : '健康管理系统'
})

export default router
