<script setup lang="ts">
import { computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import {
  House,
  User,
  TrendCharts,
  Football,
  CoffeeCup,
  DataAnalysis,
  Setting,
  UserFilled,
} from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const currentTitle = computed(() => (route.meta.title as string) || '健康管理系统')

const menuItems = [
  { path: '/dashboard', title: '健康首页', icon: House },
  { path: '/profile', title: '健康档案', icon: User },
  { path: '/weight', title: '体重管理', icon: TrendCharts },
  { path: '/sport', title: '运动管理', icon: Football },
  { path: '/water', title: '饮水管理', icon: CoffeeCup },
  { path: '/statistics', title: '数据统计', icon: DataAnalysis },
  { path: '/user-center', title: '个人中心', icon: UserFilled },
]

// 进入主布局时加载最新用户信息（如刷新页面后）
onMounted(() => {
  if (!userStore.userInfo) {
    userStore.loadUserInfo()
  }
})

async function handleCommand(command: string) {
  if (command === 'logout') {
    await userStore.logoutAccount()
    ElMessage.success('已退出登录')
    router.push('/login')
  } else if (command === 'user-center') {
    router.push('/user-center')
  }
}
</script>

<template>
  <el-container class="layout">
    <el-aside width="220px" class="layout-aside">
      <div class="logo">
        <span class="logo-mark">健</span>
        <span class="logo-text">健康管理系统</span>
      </div>
      <el-menu
        :default-active="route.path"
        router
        class="side-menu"
        background-color="transparent"
        text-color="#4a5a52"
        active-text-color="#2e9e6b"
      >
        <el-menu-item v-for="item in menuItems" :key="item.path" :index="item.path">
          <el-icon><component :is="item.icon" /></el-icon>
          <span>{{ item.title }}</span>
        </el-menu-item>
      </el-menu>
    </el-aside>

    <el-container>
      <el-header class="layout-header">
        <div class="header-title">{{ currentTitle }}</div>
        <el-dropdown @command="handleCommand" trigger="click">
          <span class="user-info">
            <el-avatar :size="30" :src="userStore.userInfo?.avatar || undefined" class="header-avatar">
              {{ (userStore.userInfo?.nickname || userStore.userInfo?.username || '健').charAt(0).toUpperCase() }}
            </el-avatar>
            <span>{{ userStore.userInfo?.nickname || '健康用户' }}</span>
            <el-icon><Setting /></el-icon>
          </span>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item command="user-center">个人中心</el-dropdown-item>
              <el-dropdown-item command="logout" divided>退出登录</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </el-header>

      <el-main class="layout-main">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<style scoped>
.layout {
  min-height: 100vh;
}
.layout-aside {
  background: linear-gradient(180deg, #f4fbf7 0%, #e8f6ee 100%);
  border-right: 1px solid #dcefe4;
}
.logo {
  height: 60px;
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 0 20px;
  border-bottom: 1px solid #dcefe4;
}
.logo-mark {
  width: 32px;
  height: 32px;
  border-radius: 8px;
  background: #2e9e6b;
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 700;
}
.logo-text {
  font-size: 16px;
  font-weight: 700;
  color: #2c6a4f;
}
.side-menu {
  border: none;
  margin-top: 12px;
}
.side-menu :deep(.el-menu-item.is-active) {
  background: #d9f0e3 !important;
  border-radius: 8px;
  margin: 2px 8px;
}
.layout-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: #fff;
  border-bottom: 1px solid #eef2f0;
}
.header-title {
  font-size: 18px;
  font-weight: 600;
  color: #2c3e35;
}
.header-avatar {
  background: #2e9e6b;
  color: #fff;
  font-size: 14px;
  margin-right: 4px;
}
.user-info {
  display: flex;
  align-items: center;
  gap: 6px;
  cursor: pointer;
  color: #4a5a52;
}
.layout-main {
  background: #f5f9f7;
  padding: 24px;
}
</style>
