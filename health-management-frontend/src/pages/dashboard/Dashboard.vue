<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { TrendCharts, Football, CoffeeCup, Connection, Right } from '@element-plus/icons-vue'
import { useRouter } from 'vue-router'
import { ping } from '@/api/health'
import { getOverview } from '@/api/statistics'
import type { HealthStatus, StatisticsOverview } from '@/types'

const router = useRouter()
const backendStatus = ref<'checking' | 'ok' | 'fail'>('checking')
const backendTime = ref('')
const overview = ref<StatisticsOverview | null>(null)
const overviewLoading = ref(true)

onMounted(async () => {
  try {
    const data: HealthStatus = await ping()
    if (data.status === 'UP') {
      backendStatus.value = 'ok'
      backendTime.value = data.time
    } else {
      backendStatus.value = 'fail'
    }
  } catch {
    backendStatus.value = 'fail'
  }
  try {
    overview.value = await getOverview(7)
  } catch {
    // 拦截器已统一提示
  } finally {
    overviewLoading.value = false
  }
})

const today = () => overview.value?.today
const goals = () => overview.value?.goals
function fmt(v: number | undefined | null, digits = 0): string {
  return v == null ? '—' : Number(v).toFixed(digits)
}
</script>

<template>
  <div class="dashboard" v-loading="overviewLoading" element-loading-text="正在加载健康数据...">
    <el-alert
      v-if="overview && !overview.today.hasRecord"
      title="今天还没有健康记录"
      description="记录体重、运动或饮水后，这里会实时展示你的健康概览。"
      type="info"
      show-icon
      :closable="false"
      style="margin-bottom: 16px"
    />
    <el-row :gutter="16">
      <el-col :span="8">
        <el-card class="stat-card" shadow="never">
          <div class="stat-icon weight"><el-icon><TrendCharts /></el-icon></div>
          <div class="stat-info">
            <div class="stat-label">当前体重</div>
            <div class="stat-value">{{ fmt(today()?.weight, 1) }} <small>kg</small></div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card class="stat-card" shadow="never">
          <div class="stat-icon sport"><el-icon><Football /></el-icon></div>
          <div class="stat-info">
            <div class="stat-label">今日运动时长</div>
            <div class="stat-value">{{ today()?.sportDuration ?? 0 }} <small>分钟</small></div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card class="stat-card" shadow="never">
          <div class="stat-icon water"><el-icon><CoffeeCup /></el-icon></div>
          <div class="stat-info">
            <div class="stat-label">今日饮水量</div>
            <div class="stat-value">{{ today()?.waterAmount ?? 0 }} <small>ml</small></div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="16" style="margin-top: 16px">
      <el-col :span="16">
        <el-card class="status-card" shadow="never">
          <template #header>
            <div class="card-header"><span class="card-title">今日目标完成情况</span></div>
          </template>
          <div class="goal-list">
            <div class="goal-row">
              <span class="goal-name">饮水</span>
              <el-progress :percentage="Math.min(goals()?.waterRate ?? 0, 100)" style="flex: 1" />
              <span class="goal-desc">{{ goals()?.todayWater ?? 0 }} / {{ goals()?.waterGoal ?? 2000 }} ml</span>
            </div>
            <div class="goal-row">
              <span class="goal-name">每周运动</span>
              <el-progress :percentage="Math.min(goals()?.sportRate ?? 0, 100)" style="flex: 1" />
              <span class="goal-desc">{{ goals()?.weekSportDuration ?? 0 }} / {{ goals()?.weeklyExerciseGoal ?? 150 }} 分</span>
            </div>
            <div class="goal-row">
              <span class="goal-name">体重</span>
              <el-progress :percentage="goals()?.weightReached ? 100 : 50" style="flex: 1" :color="goals()?.weightReached ? '#67c23a' : '#409eff'" />
              <span class="goal-desc">{{ fmt(goals()?.currentWeight, 1) }} / {{ fmt(goals()?.targetWeight, 1) }} kg</span>
            </div>
          </div>
          <div class="more-link" @click="router.push('/statistics')">
            查看完整统计中心 <el-icon><Right /></el-icon>
          </div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card class="status-card" shadow="never">
          <template #header>
            <div class="card-header"><span class="card-title">前后端通信状态</span></div>
          </template>
          <div class="status-row">
            <el-icon class="status-icon"><Connection /></el-icon>
            <span>后端服务：</span>
            <el-tag v-if="backendStatus === 'checking'" type="info" size="small">检测中...</el-tag>
            <el-tag v-else-if="backendStatus === 'ok'" type="success" size="small">连接正常</el-tag>
            <el-tag v-else type="danger" size="small">连接失败</el-tag>
          </div>
          <div v-if="backendTime" class="status-time">服务端时间：{{ backendTime }}</div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<style scoped>
.stat-card { border-radius: 12px; }
.stat-card :deep(.el-card__body) { display: flex; align-items: center; gap: 16px; padding: 20px; }
.stat-icon { width: 52px; height: 52px; border-radius: 12px; display: flex; align-items: center; justify-content: center; font-size: 26px; flex-shrink: 0; }
.stat-icon.weight { background: #e3f3ea; color: #2e9e6b; }
.stat-icon.sport { background: #e6f0fb; color: #3b82c4; }
.stat-icon.water { background: #e4f4f7; color: #2a98a8; }
.stat-label { font-size: 13px; color: #8a9a92; }
.stat-value { font-size: 26px; font-weight: 700; color: #2c3e35; }
.stat-value small { font-size: 14px; font-weight: 400; color: #8a9a92; }
.card-header, .card-title { font-weight: 600; color: #2c3e35; }
.status-card { border-radius: 12px; }
.status-row { display: flex; align-items: center; gap: 10px; color: #4a5a52; }
.status-icon { font-size: 20px; color: #2e9e6b; }
.status-time { margin-top: 10px; color: #8a9a92; font-size: 13px; }
.goal-list { display: flex; flex-direction: column; gap: 14px; }
.goal-row { display: flex; align-items: center; gap: 12px; }
.goal-name { width: 64px; font-size: 13px; color: #4a5a52; flex-shrink: 0; }
.goal-desc { width: 130px; text-align: right; font-size: 12px; color: #8a9a92; flex-shrink: 0; }
.more-link { margin-top: 16px; text-align: right; font-size: 13px; color: #2e9e6b; cursor: pointer; display: flex; align-items: center; justify-content: flex-end; gap: 2px; }
.more-link:hover { text-decoration: underline; }
</style>
