<script setup lang="ts">
import { reactive, ref, onMounted, onBeforeUnmount, nextTick, computed } from 'vue'
import * as echarts from 'echarts/core'
import { LineChart } from 'echarts/charts'
import {
  TooltipComponent,
  GridComponent,
  MarkLineComponent,
} from 'echarts/components'
import { CanvasRenderer } from 'echarts/renderers'
import type { ECharts } from 'echarts/core'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance } from 'element-plus'
import { weightApi, getWeightTrend } from '@/api/records'
import type { WeightRecord, WeightTrend, PageResult } from '@/types'

echarts.use([LineChart, TooltipComponent, GridComponent, MarkLineComponent, CanvasRenderer])

// ================= 趋势分析 =================
const trendLoading = ref(false)
const trend = ref<WeightTrend>({ points: [], stats: { count: 0 } })
const range = ref<number>(30) // 7 / 30 / 90 / 0=全部
const chartEl = ref<HTMLDivElement | null>(null)
let chart: ECharts | null = null

const rangeOptions = [
  { label: '近 7 天', value: 7 },
  { label: '近 30 天', value: 30 },
  { label: '近 90 天', value: 90 },
  { label: '全部', value: 0 },
]

async function loadTrend() {
  trendLoading.value = true
  try {
    const params = range.value > 0 ? { days: range.value } : {}
    trend.value = await getWeightTrend(params)
    await nextTick()
    renderChart()
  } catch {
    // 拦截器已统一提示
  } finally {
    trendLoading.value = false
  }
}

function renderChart() {
  if (!chartEl.value) return
  if (!chart) {
    chart = echarts.init(chartEl.value)
    window.addEventListener('resize', resizeChart)
  }
  const points = trend.value.points
  const target = trend.value.stats.targetWeight
  const series = [
    {
      name: '体重',
      type: 'line',
      smooth: true,
      symbol: 'circle',
      symbolSize: 7,
      showSymbol: true,
      lineStyle: { width: 3, color: '#34b37c' },
      itemStyle: { color: '#34b37c' },
      areaStyle: {
        color: {
          type: 'linear', x: 0, y: 0, x2: 0, y2: 1,
          colorStops: [
            { offset: 0, color: 'rgba(52,179,124,0.35)' },
            { offset: 1, color: 'rgba(52,179,124,0.02)' },
          ],
        },
      },
      data: points.map((p) => p.weight),
      // 目标体重参考线
      markLine: target != null ? {
        silent: true,
        symbol: 'none',
        label: { formatter: `目标 ${target}kg`, position: 'insideEndTop', color: '#e6a23c' },
        lineStyle: { color: '#e6a23c', type: 'dashed', width: 2 },
        data: [{ yAxis: target }],
      } : undefined,
    },
  ]
  chart.setOption({
    tooltip: {
      trigger: 'axis',
      formatter: (params: unknown) => {
        const arr = params as Array<{ axisValue: string; data: number }>
        const p = arr?.[0]
        if (!p) return ''
        let html = `${p.axisValue}<br/>体重：<b>${p.data} kg</b>`
        if (target != null) html += `<br/>距目标：<b>${(Number(p.data) - target).toFixed(2)} kg</b>`
        return html
      },
    },
    grid: { left: 50, right: 24, top: 40, bottom: 40 },
    xAxis: {
      type: 'category',
      boundaryGap: false,
      data: points.map((p) => p.date),
      axisLabel: { color: '#909399', fontSize: 11 },
      axisLine: { lineStyle: { color: '#dcdfe6' } },
    },
    yAxis: {
      type: 'value',
      scale: true,
      name: 'kg',
      splitLine: { lineStyle: { color: '#f0f0f0' } },
      axisLabel: { color: '#909399' },
    },
    series,
  }, true)
}

function resizeChart() {
  chart?.resize()
}

function onRangeChange() {
  loadTrend()
}

onBeforeUnmount(() => {
  window.removeEventListener('resize', resizeChart)
  chart?.dispose()
  chart = null
})

// 统计卡片展示值
const s = computed(() => trend.value.stats)
function fmt(v: number | undefined, digits = 2): string {
  return v == null ? '—' : Number(v).toFixed(digits)
}
// 变化值着色：增加=红、减少=绿、持平=灰（符合国内习惯）
function changeColor(v: number | undefined): string {
  if (v == null || Number(v) === 0) return '#909399'
  return Number(v) > 0 ? '#f56c6c' : '#34b37c'
}

// ================= 列表与分页 =================
const loading = ref(false)
const list = ref<WeightRecord[]>([])
const total = ref(0)
const query = reactive({ current: 1, size: 10, startDate: '', endDate: '' })
const dateRange = ref<[string, string] | null>(null)

function onDateChange(val: [string, string] | null) {
  query.startDate = val?.[0] ?? ''
  query.endDate = val?.[1] ?? ''
}

async function loadList() {
  loading.value = true
  try {
    const res: PageResult<WeightRecord> = await weightApi.list({
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
const dialogTitle = ref('新增体重记录')
const saving = ref(false)
const editId = ref<number | null>(null)
const formRef = ref<FormInstance>()
const form = reactive({
  weight: undefined as number | undefined,
  recordDate: '' as string,
  remark: '' as string,
})

const rules = {
  weight: [
    { required: true, message: '请输入体重', trigger: 'blur' },
    { type: 'number', min: 1, max: 500, message: '体重应在 1~500 kg', trigger: 'blur' },
  ],
  recordDate: [{ required: true, message: '请选择记录日期', trigger: 'change' }],
}

function openCreate() {
  editId.value = null
  dialogTitle.value = '新增体重记录'
  form.weight = undefined
  form.recordDate = new Date().toISOString().slice(0, 10)
  form.remark = ''
  dialogVisible.value = true
}

function openEdit(row: WeightRecord) {
  editId.value = row.id!
  dialogTitle.value = '编辑体重记录'
  form.weight = Number(row.weight)
  form.recordDate = row.recordDate
  form.remark = row.remark ?? ''
  dialogVisible.value = true
}

/** CRUD 变更后同时刷新列表和趋势 */
async function submit() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return
  saving.value = true
  try {
    const payload = { weight: form.weight!, recordDate: form.recordDate, remark: form.remark }
    if (editId.value == null) {
      await weightApi.create(payload)
      ElMessage.success('新增成功')
    } else {
      await weightApi.update(editId.value, payload)
      ElMessage.success('修改成功')
    }
    dialogVisible.value = false
    loadList()
    loadTrend()
  } catch {
    // 拦截器已统一提示
  } finally {
    saving.value = false
  }
}

// ================= 删除 =================
async function handleDelete(row: WeightRecord) {
  try {
    await ElMessageBox.confirm(
      `确定删除 ${row.recordDate} 的体重记录吗？此操作不可恢复。`,
      '删除确认',
      { type: 'warning', confirmButtonText: '删除', cancelButtonText: '取消' },
    )
  } catch {
    return
  }
  try {
    await weightApi.remove(row.id!)
    ElMessage.success('删除成功')
    loadList()
    loadTrend()
  } catch {
    // 拦截器已统一提示
  }
}

// ================= 详情 =================
const detailVisible = ref(false)
const detail = ref<WeightRecord | null>(null)
const detailLoading = ref(false)

async function handleDetail(row: WeightRecord) {
  detailVisible.value = true
  detail.value = null
  detailLoading.value = true
  try {
    detail.value = await weightApi.detail(row.id!)
  } catch {
    detailVisible.value = false
  } finally {
    detailLoading.value = false
  }
}

onMounted(() => {
  loadTrend()
  loadList()
})
</script>

<template>
  <div class="weight-page">
    <!-- 趋势分析区 -->
    <el-card shadow="never" class="trend-card">
      <template #header>
        <div class="card-header">
          <span class="title">体重变化趋势</span>
          <el-radio-group v-model="range" size="small" @change="onRangeChange">
            <el-radio-button v-for="opt in rangeOptions" :key="opt.value" :value="opt.value">
              {{ opt.label }}
            </el-radio-button>
          </el-radio-group>
        </div>
      </template>

      <!-- 统计卡片 -->
      <div v-loading="trendLoading" class="stat-grid">
        <div class="stat-item">
          <div class="stat-label">最新体重</div>
          <div class="stat-value">{{ fmt(s.latest) }}<span class="unit">kg</span></div>
        </div>
        <div class="stat-item">
          <div class="stat-label">较上次</div>
          <div class="stat-value" :style="{ color: changeColor(s.change) }">
            {{ s.change == null ? '—' : (s.change > 0 ? '+' : '') + fmt(s.change) }}<span class="unit">kg</span>
          </div>
          <div v-if="s.changeRate != null" class="stat-sub" :style="{ color: changeColor(s.changeRate) }">
            {{ s.changeRate > 0 ? '+' : '' }}{{ fmt(s.changeRate, 1) }}%
          </div>
        </div>
        <div class="stat-item">
          <div class="stat-label">平均体重</div>
          <div class="stat-value">{{ fmt(s.average) }}<span class="unit">kg</span></div>
        </div>
        <div class="stat-item">
          <div class="stat-label">最高 / 最低</div>
          <div class="stat-value small">{{ fmt(s.max) }} / {{ fmt(s.min) }}<span class="unit">kg</span></div>
        </div>
        <div class="stat-item">
          <div class="stat-label">累计变化</div>
          <div class="stat-value" :style="{ color: changeColor(s.totalChange) }">
            {{ s.totalChange == null ? '—' : (s.totalChange > 0 ? '+' : '') + fmt(s.totalChange) }}<span class="unit">kg</span>
          </div>
        </div>
        <div class="stat-item">
          <div class="stat-label">距目标</div>
          <div class="stat-value" :style="{ color: changeColor(s.targetDiff) }">
            {{ s.targetDiff == null ? '未设置' : (s.targetDiff > 0 ? '+' : '') + fmt(s.targetDiff) }}<span v-if="s.targetDiff != null" class="unit">kg</span>
          </div>
          <div v-if="s.targetWeight != null" class="stat-sub">目标 {{ fmt(s.targetWeight) }}kg</div>
        </div>
      </div>

      <!-- 折线图 -->
      <div ref="chartEl" class="chart"></div>
      <div v-if="!trendLoading && s.count === 0" class="chart-empty">暂无体重数据，添加记录后即可查看趋势</div>
    </el-card>

    <!-- 记录管理区 -->
    <el-card shadow="never" class="list-card">
      <template #header>
        <div class="card-header">
          <span class="title">体重记录</span>
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
      </div>

      <el-table v-loading="loading" :data="list" border stripe style="width: 100%">
        <el-table-column prop="recordDate" label="记录日期" width="130" />
        <el-table-column prop="weight" label="体重(kg)" width="130">
          <template #default="{ row }"><b>{{ row.weight }}</b></template>
        </el-table-column>
        <el-table-column prop="remark" label="备注" min-width="180" show-overflow-tooltip />
        <el-table-column prop="createTime" label="创建时间" width="180" />
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
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="480px" destroy-on-close>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
        <el-form-item label="体重(kg)" prop="weight">
          <el-input-number v-model="form.weight" :min="1" :max="500" :precision="2" :step="0.5" style="width: 100%" />
        </el-form-item>
        <el-form-item label="记录日期" prop="recordDate">
          <el-date-picker v-model="form.recordDate" type="date" value-format="YYYY-MM-DD" placeholder="选择日期" style="width: 100%" />
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="form.remark" type="textarea" :rows="3" maxlength="255" show-word-limit placeholder="可选" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="submit">保存</el-button>
      </template>
    </el-dialog>

    <!-- 详情弹窗 -->
    <el-dialog v-model="detailVisible" title="记录详情" width="440px">
      <div v-loading="detailLoading" class="detail-body">
        <template v-if="detail">
          <div class="detail-row"><span>记录日期</span><b>{{ detail.recordDate }}</b></div>
          <div class="detail-row"><span>体重</span><b>{{ detail.weight }} kg</b></div>
          <div class="detail-row"><span>备注</span><span>{{ detail.remark || '—' }}</span></div>
          <div class="detail-row"><span>创建时间</span><span>{{ detail.createTime }}</span></div>
        </template>
      </div>
    </el-dialog>
  </div>
</template>

<style scoped>
.weight-page { padding: 4px; display: flex; flex-direction: column; gap: 16px; }
.trend-card, .list-card { margin-bottom: 0; }
.card-header { display: flex; align-items: center; justify-content: space-between; }
.card-header .title { font-size: 16px; font-weight: 600; }
.filter-bar { display: flex; align-items: center; gap: 12px; margin-bottom: 16px; flex-wrap: wrap; }
.filter-label { color: #606266; font-size: 14px; }
.pagination-bar { display: flex; justify-content: flex-end; margin-top: 16px; }
.empty { padding: 24px; color: #909399; }

.stat-grid { display: grid; grid-template-columns: repeat(6, 1fr); gap: 12px; margin-bottom: 12px; }
.stat-item { background: #f7fbf9; border: 1px solid #e8f3ed; border-radius: 8px; padding: 12px 14px; }
.stat-label { font-size: 12px; color: #909399; margin-bottom: 6px; }
.stat-value { font-size: 22px; font-weight: 600; color: #2c3e50; line-height: 1.2; }
.stat-value.small { font-size: 17px; }
.stat-value .unit { font-size: 12px; font-weight: 400; margin-left: 3px; color: #909399; }
.stat-sub { font-size: 12px; color: #909399; margin-top: 4px; }

.chart { width: 100%; height: 300px; }
.chart-empty { text-align: center; color: #909399; padding: 8px; font-size: 13px; }

@media (max-width: 1100px) {
  .stat-grid { grid-template-columns: repeat(3, 1fr); }
}

.detail-body { min-height: 120px; }
.detail-row { display: flex; justify-content: space-between; padding: 10px 4px; border-bottom: 1px solid #f0f0f0; }
.detail-row > span:first-child { color: #909399; }
</style>
