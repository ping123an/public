<script setup lang="ts">
import { reactive, ref, computed, onMounted, onBeforeUnmount, nextTick } from 'vue'
import * as echarts from 'echarts/core'
import { BarChart, GaugeChart } from 'echarts/charts'
import {
  TooltipComponent,
  GridComponent,
  MarkLineComponent,
} from 'echarts/components'
import { CanvasRenderer } from 'echarts/renderers'
import type { ECharts } from 'echarts/core'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance } from 'element-plus'
import { waterApi, getWaterStats, setWaterGoal } from '@/api/records'
import type { WaterRecord, PageResult, WaterStats } from '@/types'

echarts.use([BarChart, GaugeChart, TooltipComponent, GridComponent, MarkLineComponent, CanvasRenderer])

// ================= 目标与统计 =================
const statsLoading = ref(false)
const stats = ref<WaterStats>({
  daily: [],
  summary: { totalAmount: 0, activeDays: 0, avgAmount: 0, reachedDays: 0, dailyGoal: 2000 },
})
const range = ref<number>(7)
const gaugeEl = ref<HTMLDivElement | null>(null)
const barEl = ref<HTMLDivElement | null>(null)
let gaugeChart: ECharts | null = null
let barChart: ECharts | null = null

const rangeOptions = [
  { label: '近 7 天', value: 7 },
  { label: '近 30 天', value: 30 },
  { label: '近 90 天', value: 90 },
  { label: '全部', value: 0 },
]

async function loadStats() {
  statsLoading.value = true
  try {
    const params = range.value > 0 ? { days: range.value } : {}
    stats.value = await getWaterStats(params)
    await nextTick()
    renderCharts()
  } catch {
    // 拦截器已统一提示
  } finally {
    statsLoading.value = false
  }
}

/** 最新一天的完成率（用于进度环），无数据则 0 */
const latestRate = computed(() => {
  const daily = stats.value.daily
  return daily.length ? daily[daily.length - 1].completionRate : 0
})
const latestDate = computed(() => {
  const daily = stats.value.daily
  return daily.length ? daily[daily.length - 1].date : '—'
})

function renderCharts() {
  const goal = stats.value.summary.dailyGoal
  const rate = Math.min(latestRate.value, 100)

  // 进度环
  if (gaugeEl.value) {
    if (!gaugeChart) gaugeChart = echarts.init(gaugeEl.value)
    gaugeChart.setOption({
      series: [{
        type: 'gauge',
        startAngle: 90,
        endAngle: -270,
        radius: '92%',
        center: ['50%', '50%'],
        min: 0,
        max: 100,
        progress: { show: true, width: 14, roundCap: true, itemStyle: { color: '#409eff' } },
        axisLine: { lineStyle: { width: 14, color: [[1, '#e4e7ed']] } },
        pointer: { show: false },
        axisTick: { show: false },
        splitLine: { show: false },
        axisLabel: { show: false },
        detail: {
          valueAnimation: true,
          offsetCenter: [0, '0%'],
          formatter: '{value}%',
          fontSize: 26,
          fontWeight: 'bold',
          color: '#2c3e50',
        },
        title: { show: false },
        data: [{ value: rate }],
      }],
    }, true)
  }

  // 柱状图：每日总量 + 目标参考线
  if (barEl.value) {
    if (!barChart) barChart = echarts.init(barEl.value)
    const daily = stats.value.daily
    barChart.setOption({
      tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
      grid: { left: 56, right: 20, top: 30, bottom: 40 },
      xAxis: {
        type: 'category',
        data: daily.map((d) => d.date.slice(5)),
        axisLabel: { color: '#909399', fontSize: 11 },
        axisLine: { lineStyle: { color: '#dcdfe6' } },
      },
      yAxis: {
        type: 'value', name: 'ml',
        splitLine: { lineStyle: { color: '#f0f0f0' } },
        axisLabel: { color: '#909399' },
      },
      series: [{
        name: '饮水量', type: 'bar', barMaxWidth: 32,
        data: daily.map((d) => ({
          value: d.totalAmount,
          itemStyle: { color: d.reached ? '#409eff' : '#c0c4cc', borderRadius: [4, 4, 0, 0] },
        })),
        markLine: {
          silent: true,
          symbol: 'none',
          label: { formatter: `目标 ${goal}`, position: 'insideEndTop', color: '#e6a23c' },
          lineStyle: { color: '#e6a23c', type: 'dashed', width: 2 },
          data: [{ yAxis: goal }],
        },
      }],
    }, true)
  }
}

function resizeCharts() {
  gaugeChart?.resize()
  barChart?.resize()
}

function onRangeChange() {
  loadStats()
}

onBeforeUnmount(() => {
  window.removeEventListener('resize', resizeCharts)
  gaugeChart?.dispose()
  barChart?.dispose()
  gaugeChart = null
  barChart = null
})

const sm = computed(() => stats.value.summary)

// ---------- 目标设置 ----------
const goalDialogVisible = ref(false)
const goalSaving = ref(false)
const goalForm = ref({ dailyWaterGoal: 2000 })
const goalRules = {
  dailyWaterGoal: [
    { required: true, message: '请输入饮水目标', trigger: 'blur' },
    { type: 'number', min: 200, max: 10000, message: '目标应在 200~10000 ml', trigger: 'blur' },
  ],
}
const goalFormRef = ref<FormInstance>()

function openGoal() {
  goalForm.value.dailyWaterGoal = sm.value.dailyGoal || 2000
  goalDialogVisible.value = true
}

async function saveGoal() {
  const valid = await goalFormRef.value?.validate().catch(() => false)
  if (!valid) return
  goalSaving.value = true
  try {
    const newGoal = await setWaterGoal(goalForm.value.dailyWaterGoal)
    ElMessage.success('目标已更新')
    goalDialogVisible.value = false
    await loadStats()
    // 目标变化后，达标判定需重算
    void newGoal
  } catch {
    // 拦截器已统一提示
  } finally {
    goalSaving.value = false
  }
}

// ================= 列表与分页 =================
const loading = ref(false)
const list = ref<WaterRecord[]>([])
const total = ref(0)
const query = reactive({ current: 1, size: 10, startDate: '', endDate: '' })
const dateRange = ref<[string, string] | null>(null)
const pageTotalAmount = computed(() => list.value.reduce((sum, r) => sum + (r.amount || 0), 0))
const quickAmounts = [200, 300, 500, 800]

function onDateChange(val: [string, string] | null) {
  query.startDate = val?.[0] ?? ''
  query.endDate = val?.[1] ?? ''
}

async function loadList() {
  loading.value = true
  try {
    const res: PageResult<WaterRecord> = await waterApi.list({
      current: query.current,
      size: query.size,
      startDate: query.startDate || undefined,
      endDate: query.endDate || undefined,
    })
    list.value = res.records
    total.value = res.total
  } catch {
    // 拦截器已统一提示
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  query.current = 1
  loadList()
}

function handleReset() {
  dateRange.value = null
  query.startDate = ''
  query.endDate = ''
  query.current = 1
  loadList()
}

function handlePageChange(page: number) {
  query.current = page
  loadList()
}

function handleSizeChange(size: number) {
  query.size = size
  query.current = 1
  loadList()
}

// ================= 新增 / 编辑 =================
const dialogVisible = ref(false)
const dialogTitle = ref('新增饮水记录')
const saving = ref(false)
const editId = ref<number | null>(null)
const formRef = ref<FormInstance>()
const form = reactive({
  amount: undefined as number | undefined,
  recordDate: '' as string,
})

const rules = {
  amount: [
    { required: true, message: '请输入饮水量', trigger: 'blur' },
    { type: 'number', min: 1, max: 10000, message: '饮水量应在 1~10000 ml', trigger: 'blur' },
  ],
  recordDate: [{ required: true, message: '请选择记录日期', trigger: 'change' }],
}

function openCreate() {
  editId.value = null
  dialogTitle.value = '新增饮水记录'
  form.amount = 300
  form.recordDate = new Date().toISOString().slice(0, 10)
  dialogVisible.value = true
}

function openEdit(row: WaterRecord) {
  editId.value = row.id!
  dialogTitle.value = '编辑饮水记录'
  form.amount = row.amount
  form.recordDate = row.recordDate
  dialogVisible.value = true
}

function fillQuick(amount: number) {
  form.amount = amount
}

async function submit() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return
  saving.value = true
  try {
    const payload = { amount: form.amount!, recordDate: form.recordDate }
    if (editId.value == null) {
      await waterApi.create(payload)
      ElMessage.success('新增成功')
    } else {
      await waterApi.update(editId.value, payload)
      ElMessage.success('修改成功')
    }
    dialogVisible.value = false
    loadList()
    loadStats()
  } catch {
    // 拦截器已统一提示
  } finally {
    saving.value = false
  }
}

// ================= 删除 =================
async function handleDelete(row: WaterRecord) {
  try {
    await ElMessageBox.confirm(
      `确定删除 ${row.recordDate} 的 ${row.amount}ml 饮水记录吗？此操作不可恢复。`,
      '删除确认',
      { type: 'warning', confirmButtonText: '删除', cancelButtonText: '取消' },
    )
  } catch {
    return
  }
  try {
    await waterApi.remove(row.id!)
    ElMessage.success('删除成功')
    loadList()
    loadStats()
  } catch {
    // 拦截器已统一提示
  }
}

// ================= 详情 =================
const detailVisible = ref(false)
const detail = ref<WaterRecord | null>(null)
const detailLoading = ref(false)

async function handleDetail(row: WaterRecord) {
  detailVisible.value = true
  detail.value = null
  detailLoading.value = true
  try {
    detail.value = await waterApi.detail(row.id!)
  } catch {
    detailVisible.value = false
  } finally {
    detailLoading.value = false
  }
}

onMounted(() => {
  window.addEventListener('resize', resizeCharts)
  loadStats()
  loadList()
})
</script>

<template>
  <div class="water-page">
    <!-- 目标与统计区 -->
    <el-card shadow="never" class="stats-card">
      <template #header>
        <div class="card-header">
          <span class="title">饮水目标与统计</span>
          <div class="header-actions">
            <span class="goal-label">每日目标：<b>{{ sm.dailyGoal }} ml</b></span>
            <el-button size="small" @click="openGoal">设置目标</el-button>
            <el-radio-group v-model="range" size="small" @change="onRangeChange">
              <el-radio-button v-for="opt in rangeOptions" :key="opt.value" :value="opt.value">
                {{ opt.label }}
              </el-radio-button>
            </el-radio-group>
          </div>
        </div>
      </template>

      <div v-loading="statsLoading" class="stats-body">
        <!-- 进度环 -->
        <div class="gauge-box">
          <div ref="gaugeEl" class="gauge"></div>
          <div class="gauge-caption">
            <div class="gauge-date">{{ latestDate }} 完成率</div>
            <div class="gauge-sub">目标 {{ sm.dailyGoal }} ml</div>
          </div>
        </div>

        <!-- 统计卡片 -->
        <div class="stat-grid">
          <div class="stat-item">
            <div class="stat-label">区间总饮水</div>
            <div class="stat-value water">{{ sm.totalAmount }}<span class="unit">ml</span></div>
          </div>
          <div class="stat-item">
            <div class="stat-label">有记录天数</div>
            <div class="stat-value">{{ sm.activeDays }}<span class="unit">天</span></div>
          </div>
          <div class="stat-item">
            <div class="stat-label">日均饮水</div>
            <div class="stat-value">{{ sm.avgAmount }}<span class="unit">ml</span></div>
          </div>
          <div class="stat-item">
            <div class="stat-label">达标天数</div>
            <div class="stat-value success">{{ sm.reachedDays }}<span class="unit">天</span></div>
          </div>
        </div>

        <!-- 柱状图 -->
        <div class="chart-box">
          <div class="chart-title">每日饮水量（蓝色为达标，灰色为未达标）</div>
          <div ref="barEl" class="chart"></div>
        </div>
        <div v-if="!statsLoading && sm.totalAmount === 0" class="chart-empty">暂无饮水数据，添加记录后即可查看统计</div>
      </div>
    </el-card>

    <!-- 记录管理区 -->
    <el-card shadow="never" class="list-card">
      <template #header>
        <div class="card-header">
          <span class="title">饮水记录</span>
          <el-button type="primary" @click="openCreate">新增记录</el-button>
        </div>
      </template>

      <div class="filter-bar">
        <span class="filter-label">记录日期</span>
        <el-date-picker
          v-model="dateRange"
          type="daterange"
          value-format="YYYY-MM-DD"
          range-separator="至"
          start-placeholder="开始日期"
          end-placeholder="结束日期"
          @change="onDateChange"
        />
        <el-button type="primary" @click="handleSearch">查询</el-button>
        <el-button @click="handleReset">重置</el-button>
        <span class="page-sum">当前页合计：<b>{{ pageTotalAmount }}</b> ml</span>
      </div>

      <el-table v-loading="loading" :data="list" border stripe style="width: 100%">
        <el-table-column prop="recordDate" label="记录日期" width="150" />
        <el-table-column prop="amount" label="饮水量(ml)" width="150">
          <template #default="{ row }"><b>{{ row.amount }}</b></template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" min-width="180" />
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="handleDetail(row)">详情</el-button>
            <el-button link type="primary" @click="openEdit(row)">编辑</el-button>
            <el-button link type="danger" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
        <template #empty><div class="empty">暂无记录，点击「新增记录」开始吧</div></template>
      </el-table>

      <div class="pagination-bar">
        <el-pagination
          background
          layout="total, sizes, prev, pager, next, jumper"
          :total="total"
          v-model:current-page="query.current"
          v-model:page-size="query.size"
          :page-sizes="[5, 10, 20, 50]"
          @current-change="handlePageChange"
          @size-change="handleSizeChange"
        />
      </div>
    </el-card>

    <!-- 新增/编辑弹窗 -->
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="460px" destroy-on-close>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
        <el-form-item label="饮水量(ml)" prop="amount">
          <el-input-number v-model="form.amount" :min="1" :max="10000" :step="100" style="width: 100%" />
        </el-form-item>
        <el-form-item label="快捷选择">
          <el-button
            v-for="a in quickAmounts"
            :key="a"
            size="small"
            :type="form.amount === a ? 'primary' : 'default'"
            @click="fillQuick(a)"
          >{{ a }}ml</el-button>
        </el-form-item>
        <el-form-item label="记录日期" prop="recordDate">
          <el-date-picker v-model="form.recordDate" type="date" value-format="YYYY-MM-DD" placeholder="选择日期" style="width: 100%" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="submit">保存</el-button>
      </template>
    </el-dialog>

    <!-- 目标设置弹窗 -->
    <el-dialog v-model="goalDialogVisible" title="设置每日饮水目标" width="420px" destroy-on-close>
      <el-form ref="goalFormRef" :model="goalForm" :rules="goalRules" label-width="100px">
        <el-form-item label="每日目标(ml)" prop="dailyWaterGoal">
          <el-input-number v-model="goalForm.dailyWaterGoal" :min="200" :max="10000" :step="100" style="width: 100%" />
        </el-form-item>
        <div class="goal-tip">建议每日饮水 1500~2500 ml，可根据个人情况调整。</div>
      </el-form>
      <template #footer>
        <el-button @click="goalDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="goalSaving" @click="saveGoal">保存</el-button>
      </template>
    </el-dialog>

    <!-- 详情弹窗 -->
    <el-dialog v-model="detailVisible" title="记录详情" width="420px">
      <div v-loading="detailLoading" class="detail-body">
        <template v-if="detail">
          <div class="detail-row"><span>记录日期</span><b>{{ detail.recordDate }}</b></div>
          <div class="detail-row"><span>饮水量</span><b>{{ detail.amount }} ml</b></div>
          <div class="detail-row"><span>创建时间</span><span>{{ detail.createTime }}</span></div>
        </template>
      </div>
    </el-dialog>
  </div>
</template>

<style scoped>
.water-page { padding: 4px; display: flex; flex-direction: column; gap: 16px; }
.stats-card, .list-card { margin-bottom: 0; }
.card-header { display: flex; align-items: center; justify-content: space-between; flex-wrap: wrap; gap: 8px; }
.card-header .title { font-size: 16px; font-weight: 600; }
.header-actions { display: flex; align-items: center; gap: 12px; flex-wrap: wrap; }
.goal-label { color: #606266; font-size: 13px; }
.goal-label b { color: #409eff; }
.filter-bar { display: flex; align-items: center; gap: 12px; margin-bottom: 16px; flex-wrap: wrap; }
.filter-label { color: #606266; font-size: 14px; }
.page-sum { margin-left: auto; color: #606266; font-size: 14px; }
.page-sum b { color: #409eff; }
.pagination-bar { display: flex; justify-content: flex-end; margin-top: 16px; }
.empty { padding: 24px; color: #909399; }

.stats-body { display: flex; gap: 20px; flex-wrap: wrap; align-items: stretch; }
.gauge-box { width: 220px; display: flex; flex-direction: column; align-items: center; justify-content: center; }
.gauge { width: 180px; height: 180px; }
.gauge-caption { text-align: center; margin-top: -8px; }
.gauge-date { font-size: 13px; color: #606266; font-weight: 600; }
.gauge-sub { font-size: 12px; color: #909399; margin-top: 2px; }
.stat-grid { flex: 1; display: grid; grid-template-columns: repeat(4, 1fr); gap: 12px; align-content: center; min-width: 380px; }
.stat-item { background: #f7fbf9; border: 1px solid #e8f3ed; border-radius: 8px; padding: 14px; }
.stat-label { font-size: 12px; color: #909399; margin-bottom: 6px; }
.stat-value { font-size: 22px; font-weight: 600; color: #2c3e50; line-height: 1.2; }
.stat-value.water { color: #409eff; }
.stat-value.success { color: #67c23a; }
.stat-value .unit { font-size: 12px; font-weight: 400; margin-left: 3px; color: #909399; }
.chart-box { width: 100%; }
.chart-title { font-size: 13px; color: #606266; font-weight: 600; margin-bottom: 4px; }
.chart { width: 100%; height: 220px; }
.chart-empty { text-align: center; color: #909399; padding: 8px; font-size: 13px; }
.goal-tip { font-size: 12px; color: #909399; padding-left: 100px; }

@media (max-width: 1100px) {
  .stat-grid { grid-template-columns: repeat(2, 1fr); }
  .gauge-box { width: 100%; }
}

.detail-body { min-height: 100px; }
.detail-row { display: flex; justify-content: space-between; padding: 10px 4px; border-bottom: 1px solid #f0f0f0; }
.detail-row > span:first-child { color: #909399; }
</style>
