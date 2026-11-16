<script setup lang="ts">
import { ref, onMounted, onBeforeUnmount, nextTick, computed } from 'vue'
import * as echarts from 'echarts/core'
import { LineChart, BarChart } from 'echarts/charts'
import { TooltipComponent, GridComponent, LegendComponent, MarkLineComponent } from 'echarts/components'
import { CanvasRenderer } from 'echarts/renderers'
import type { ECharts } from 'echarts/core'
import { ElMessage } from 'element-plus'
import { getOverview } from '@/api/statistics'
import type { StatisticsOverview } from '@/types'

echarts.use([LineChart, BarChart, TooltipComponent, GridComponent, LegendComponent, MarkLineComponent, CanvasRenderer])

const loading = ref(false)
const data = ref<StatisticsOverview | null>(null)
const days = ref(7)
const rangeOptions = [
  { label: '近 7 天', value: 7 },
  { label: '近 30 天', value: 30 },
  { label: '近 90 天', value: 90 },
]

const weightEl = ref<HTMLDivElement | null>(null)
const sportEl = ref<HTMLDivElement | null>(null)
const waterEl = ref<HTMLDivElement | null>(null)
let weightChart: ECharts | null = null
let sportChart: ECharts | null = null
let waterChart: ECharts | null = null

const today = computed(() => data.value?.today)
const week = computed(() => data.value?.week)
const month = computed(() => data.value?.month)
const goals = computed(() => data.value?.goals)

async function load() {
  loading.value = true
  try {
    data.value = await getOverview(days.value)
    await nextTick()
    renderCharts()
  } catch {
    ElMessage.error('统计数据加载失败')
  } finally {
    loading.value = false
  }
}

function onRangeChange() {
  load()
}

function number(v: number | undefined | null, digits = 0): string {
  if (v == null) return '—'
  return Number(v).toFixed(digits)
}

function pctColor(v: number): string {
  return v >= 100 ? '#67c23a' : v >= 60 ? '#e6a23c' : '#f56c6c'
}

function renderCharts() {
  const charts = data.value?.charts
  if (!charts) return

  // 体重趋势折线图
  if (weightEl.value) {
    if (!weightChart) weightChart = echarts.init(weightEl.value)
    const pts = charts.weight.points
    const target = charts.weight.stats?.targetWeight
    weightChart.setOption({
      tooltip: { trigger: 'axis' },
      grid: { left: 48, right: 20, top: 30, bottom: 36 },
      xAxis: { type: 'category', boundaryGap: false, data: pts.map(p => p.date.slice(5)), axisLabel: { color: '#909399', fontSize: 11 }, axisLine: { lineStyle: { color: '#dcdfe6' } } },
      yAxis: { type: 'value', scale: true, name: 'kg', splitLine: { lineStyle: { color: '#f0f0f0' } }, axisLabel: { color: '#909399' } },
      series: [{
        name: '体重', type: 'line', smooth: true, symbolSize: 6,
        lineStyle: { width: 3, color: '#34b37c' }, itemStyle: { color: '#34b37c' },
        areaStyle: { color: { type: 'linear', x: 0, y: 0, x2: 0, y2: 1, colorStops: [{ offset: 0, color: 'rgba(52,179,124,0.3)' }, { offset: 1, color: 'rgba(52,179,124,0.02)' }] } },
        data: pts.map(p => p.weight),
        markLine: target != null ? { silent: true, symbol: 'none', label: { formatter: `目标 ${target}`, color: '#e6a23c' }, lineStyle: { color: '#e6a23c', type: 'dashed' }, data: [{ yAxis: target }] } : undefined,
      }],
    }, true)
  }

  // 运动时长柱状图
  if (sportEl.value) {
    if (!sportChart) sportChart = echarts.init(sportEl.value)
    const daily = charts.sport.daily
    sportChart.setOption({
      tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
      grid: { left: 48, right: 20, top: 24, bottom: 36 },
      xAxis: { type: 'category', data: daily.map(d => d.date.slice(5)), axisLabel: { color: '#909399', fontSize: 11 }, axisLine: { lineStyle: { color: '#dcdfe6' } } },
      yAxis: { type: 'value', name: '分钟', splitLine: { lineStyle: { color: '#f0f0f0' } }, axisLabel: { color: '#909399' } },
      series: [{ name: '运动时长', type: 'bar', barMaxWidth: 28, itemStyle: { color: '#409eff', borderRadius: [4, 4, 0, 0] }, data: daily.map(d => d.totalDuration) }],
    }, true)
  }

  // 饮水柱状图（含目标线）
  if (waterEl.value) {
    if (!waterChart) waterChart = echarts.init(waterEl.value)
    const daily = charts.water.daily
    const goal = charts.water.summary?.dailyGoal ?? 2000
    waterChart.setOption({
      tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
      grid: { left: 56, right: 20, top: 24, bottom: 36 },
      xAxis: { type: 'category', data: daily.map(d => d.date.slice(5)), axisLabel: { color: '#909399', fontSize: 11 }, axisLine: { lineStyle: { color: '#dcdfe6' } } },
      yAxis: { type: 'value', name: 'ml', splitLine: { lineStyle: { color: '#f0f0f0' } }, axisLabel: { color: '#909399' } },
      series: [{
        name: '饮水量', type: 'bar', barMaxWidth: 28,
        data: daily.map(d => ({ value: d.totalAmount, itemStyle: { color: d.reached ? '#2a98a8' : '#c0c4cc', borderRadius: [4, 4, 0, 0] } })),
        markLine: { silent: true, symbol: 'none', label: { formatter: `目标 ${goal}`, color: '#e6a23c' }, lineStyle: { color: '#e6a23c', type: 'dashed' }, data: [{ yAxis: goal }] },
      }],
    }, true)
  }
}

function resizeCharts() {
  weightChart?.resize()
  sportChart?.resize()
  waterChart?.resize()
}

onBeforeUnmount(() => {
  window.removeEventListener('resize', resizeCharts)
  weightChart?.dispose()
  sportChart?.dispose()
  waterChart?.dispose()
})

onMounted(() => {
  window.addEventListener('resize', resizeCharts)
  load()
})
</script>

<template>
  <div class="statistics-page" v-loading="loading">
    <!-- 页头 -->
    <div class="page-head">
      <div>
        <h2>健康数据统计中心</h2>
        <p class="subtitle">全面的健康数据汇总、趋势分析与目标完成情况</p>
      </div>
      <div class="head-actions">
        <el-radio-group v-model="days" size="small" @change="onRangeChange">
          <el-radio-button v-for="opt in rangeOptions" :key="opt.value" :value="opt.value">
            {{ opt.label }}
          </el-radio-button>
        </el-radio-group>
        <el-button size="small" @click="load">刷新</el-button>
      </div>
    </div>

    <template v-if="data">
      <!-- 今日汇总 -->
      <div class="section-title">今日健康数据</div>
      <el-row :gutter="16">
        <el-col :span="6">
          <el-card shadow="never" class="metric-card">
            <div class="metric-icon weight"><el-icon><TrendCharts /></el-icon></div>
            <div class="metric-label">当前体重</div>
            <div class="metric-value">{{ number(today?.weight, 1) }} <small>kg</small></div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card shadow="never" class="metric-card">
            <div class="metric-icon sport"><el-icon><Football /></el-icon></div>
            <div class="metric-label">今日运动时长</div>
            <div class="metric-value">{{ today?.sportDuration ?? 0 }} <small>分钟</small></div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card shadow="never" class="metric-card">
            <div class="metric-icon sport"><el-icon><Football /></el-icon></div>
            <div class="metric-label">今日消耗热量</div>
            <div class="metric-value">{{ today?.sportCalories ?? 0 }} <small>千卡</small></div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card shadow="never" class="metric-card">
            <div class="metric-icon water"><el-icon><CoffeeCup /></el-icon></div>
            <div class="metric-label">今日饮水量</div>
            <div class="metric-value">{{ today?.waterAmount ?? 0 }} <small>ml</small></div>
            <div class="metric-sub">目标 {{ today?.waterGoal ?? 2000 }} · 完成 {{ today?.waterCompletionRate ?? 0 }}%</div>
          </el-card>
        </el-col>
      </el-row>

      <!-- 目标完成情况 -->
      <div class="section-title">健康目标完成情况</div>
      <el-card shadow="never">
        <div class="goal-grid">
          <div class="goal-item">
            <div class="goal-head"><span>体重目标</span><b>{{ number(goals?.targetWeight, 1) }} kg</b></div>
            <el-progress :percentage="goals?.weightReached ? 100 : (goals?.currentWeight && goals?.targetWeight ? Math.min(100, Math.round(goals.targetWeight / goals.currentWeight * 100)) : 0)" :color="goals?.weightReached ? '#67c23a' : '#409eff'" />
            <div class="goal-foot">当前 {{ number(goals?.currentWeight, 1) }} kg · {{ goals?.weightReached ? '已达成' : `距目标 ${number(goals?.weightDiff, 1)} kg` }}</div>
          </div>
          <div class="goal-item">
            <div class="goal-head"><span>饮水目标</span><b>{{ goals?.waterGoal ?? 2000 }} ml</b></div>
            <el-progress :percentage="Math.min(goals?.waterRate ?? 0, 100)" :color="pctColor(goals?.waterRate ?? 0)" />
            <div class="goal-foot">今日 {{ goals?.todayWater ?? 0 }} ml · 完成 {{ goals?.waterRate ?? 0 }}%</div>
          </div>
          <div class="goal-item">
            <div class="goal-head"><span>每周运动目标</span><b>{{ goals?.weeklyExerciseGoal ?? 150 }} 分钟</b></div>
            <el-progress :percentage="Math.min(goals?.sportRate ?? 0, 100)" :color="pctColor(goals?.sportRate ?? 0)" />
            <div class="goal-foot">近7天 {{ goals?.weekSportDuration ?? 0 }} 分钟 · 完成 {{ goals?.sportRate ?? 0 }}%</div>
          </div>
        </div>
      </el-card>

      <!-- 周期对比 -->
      <div class="section-title">周期数据对比</div>
      <el-card shadow="never">
        <el-table :data="[week, month].filter(Boolean)" border style="width: 100%">
          <el-table-column prop="label" label="周期" width="120" />
          <el-table-column prop="activeDays" label="有记录天数" width="120" />
          <el-table-column label="体重变化" width="130">
            <template #default="{ row }">
              <span :style="{ color: (row.weightChange ?? 0) > 0 ? '#f56c6c' : (row.weightChange ?? 0) < 0 ? '#34b37c' : '#909399' }">
                {{ row.weightChange == null ? '—' : (row.weightChange > 0 ? '+' : '') + number(row.weightChange, 1) + ' kg' }}
              </span>
            </template>
          </el-table-column>
          <el-table-column label="运动总时长">
            <template #default="{ row }">{{ row.sportDuration }} 分钟 / {{ row.sportCount }} 次</template>
          </el-table-column>
          <el-table-column label="运动总热量">
            <template #default="{ row }">{{ row.sportCalories }} 千卡</template>
          </el-table-column>
          <el-table-column label="饮水总量">
            <template #default="{ row }">{{ row.waterAmount }} ml（达标 {{ row.waterReachedDays }} 天）</template>
          </el-table-column>
        </el-table>
      </el-card>

      <!-- 趋势图表 -->
      <div class="section-title">趋势图表（{{ rangeOptions.find(o => o.value === days)?.label }}）</div>
      <el-alert
        v-if="data.charts.weight.points.length === 0 && data.charts.sport.daily.length === 0 && data.charts.water.daily.length === 0"
        title="所选时间范围内暂无健康数据"
        description="在体重、运动、饮水页面添加记录后，图表将在此展示。"
        type="info"
        show-icon
        :closable="false"
        style="margin-bottom: 12px"
      />
      <el-row :gutter="16">
        <el-col :span="8">
          <el-card shadow="never">
            <template #header><span class="chart-title">体重变化趋势</span></template>
            <div ref="weightEl" class="chart"></div>
          </el-card>
        </el-col>
        <el-col :span="8">
          <el-card shadow="never">
            <template #header><span class="chart-title">运动时长统计</span></template>
            <div ref="sportEl" class="chart"></div>
          </el-card>
        </el-col>
        <el-col :span="8">
          <el-card shadow="never">
            <template #header><span class="chart-title">饮水量统计</span></template>
            <div ref="waterEl" class="chart"></div>
          </el-card>
        </el-col>
      </el-row>
    </template>
  </div>
</template>

<style scoped>
.statistics-page { padding: 4px; }
.page-head { display: flex; align-items: center; justify-content: space-between; flex-wrap: wrap; gap: 12px; margin-bottom: 20px; }
.page-head h2 { margin: 0 0 4px; font-size: 20px; color: #2c3e35; }
.subtitle { margin: 0; font-size: 13px; color: #8a9a92; }
.head-actions { display: flex; align-items: center; gap: 10px; }
.section-title { font-size: 15px; font-weight: 600; color: #2c3e35; margin: 20px 0 12px; padding-left: 10px; border-left: 3px solid #2e9e6b; }
.metric-card { text-align: center; border-radius: 12px; }
.metric-icon { width: 44px; height: 44px; border-radius: 12px; display: flex; align-items: center; justify-content: center; font-size: 22px; margin: 0 auto 8px; }
.metric-icon.weight { background: #e3f3ea; color: #2e9e6b; }
.metric-icon.sport { background: #e6f0fb; color: #3b82c4; }
.metric-icon.water { background: #e4f4f7; color: #2a98a8; }
.metric-label { font-size: 13px; color: #8a9a92; }
.metric-value { font-size: 24px; font-weight: 700; color: #2c3e35; margin-top: 4px; }
.metric-value small { font-size: 13px; font-weight: 400; color: #8a9a92; }
.metric-sub { font-size: 12px; color: #8a9a92; margin-top: 4px; }
.goal-grid { display: grid; grid-template-columns: repeat(3, 1fr); gap: 28px; padding: 8px 4px; }
.goal-head { display: flex; justify-content: space-between; font-size: 13px; color: #606266; margin-bottom: 8px; }
.goal-head b { color: #2c3e35; }
.goal-foot { font-size: 12px; color: #8a9a92; margin-top: 6px; }
.chart-title { font-weight: 600; color: #2c3e35; }
.chart { width: 100%; height: 260px; }
@media (max-width: 1100px) {
  .goal-grid { grid-template-columns: 1fr; }
  .chart { height: 220px; }
}
</style>
