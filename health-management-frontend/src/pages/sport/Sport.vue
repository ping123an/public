<script setup lang="ts">
import { reactive, ref, onMounted, onBeforeUnmount, nextTick, computed } from 'vue'
import * as echarts from 'echarts/core'
import { BarChart, PieChart } from 'echarts/charts'
import {
  TooltipComponent,
  GridComponent,
  LegendComponent,
} from 'echarts/components'
import { CanvasRenderer } from 'echarts/renderers'
import type { ECharts } from 'echarts/core'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance } from 'element-plus'
import { sportApi, getSportStats } from '@/api/records'
import type { SportRecord, PageResult, SportStats } from '@/types'

echarts.use([BarChart, PieChart, TooltipComponent, GridComponent, LegendComponent, CanvasRenderer])

// ================= 统计区 =================
const statsLoading = ref(false)
const stats = ref<SportStats>({
  daily: [],
  summary: { totalDuration: 0, totalCalories: 0, totalCount: 0, activeDays: 0, avgDuration: 0 },
})
const range = ref<number>(30)
const barEl = ref<HTMLDivElement | null>(null)
const pieEl = ref<HTMLDivElement | null>(null)
let barChart: ECharts | null = null
let pieChart: ECharts | null = null

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
    stats.value = await getSportStats(params)
    await nextTick()
    renderCharts()
  } catch {
    // 拦截器已统一提示
  } finally {
    statsLoading.value = false
  }
}

function renderCharts() {
  const daily = stats.value.daily
  // 柱状图：每日时长 + 热量（双轴）
  if (barEl.value) {
    if (!barChart) {
      barChart = echarts.init(barEl.value)
    }
    barChart.setOption({
      tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
      legend: { data: ['运动时长', '消耗热量'], top: 0, textStyle: { color: '#606266' } },
      grid: { left: 50, right: 50, top: 36, bottom: 40 },
      xAxis: {
        type: 'category',
        data: daily.map((d) => d.date.slice(5)),
        axisLabel: { color: '#909399', fontSize: 11 },
        axisLine: { lineStyle: { color: '#dcdfe6' } },
      },
      yAxis: [
        { type: 'value', name: '分钟', splitLine: { lineStyle: { color: '#f0f0f0' } }, axisLabel: { color: '#909399' } },
        { type: 'value', name: '千卡', splitLine: { show: false }, axisLabel: { color: '#909399' } },
      ],
      series: [
        {
          name: '运动时长', type: 'bar', barMaxWidth: 24,
          itemStyle: { color: '#409eff', borderRadius: [4, 4, 0, 0] },
          data: daily.map((d) => d.totalDuration),
        },
        {
          name: '消耗热量', type: 'bar', yAxisIndex: 1, barMaxWidth: 24,
          itemStyle: { color: '#67c23a', borderRadius: [4, 4, 0, 0] },
          data: daily.map((d) => d.totalCalories),
        },
      ],
    }, true)
  }

  // 饼图：各运动类型的记录次数分布
  if (pieEl.value) {
    if (!pieChart) {
      pieChart = echarts.init(pieEl.value)
    }
    const counter: Record<string, number> = {}
    stats.value.daily.forEach((d) => {
      d.sportTypes.forEach((t) => {
        counter[t] = (counter[t] ?? 0) + 1
      })
    })
    const pieData = Object.entries(counter).map(([name, value]) => ({ name, value }))
    pieChart.setOption({
      tooltip: { trigger: 'item', formatter: '{b}: {c} 天 ({d}%)' },
      legend: { orient: 'vertical', right: 8, top: 'middle', textStyle: { color: '#606266' } },
      series: [
        {
          name: '运动类型分布', type: 'pie', radius: ['42%', '68%'], center: ['35%', '52%'],
          avoidLabelOverlap: true,
          label: { show: false },
          emphasis: { label: { show: true, fontSize: 13, fontWeight: 'bold' } },
          data: pieData.length ? pieData : [{ name: '暂无数据', value: 1, itemStyle: { color: '#e4e7ed' } }],
        },
      ],
    }, true)
  }
}

function resizeCharts() {
  barChart?.resize()
  pieChart?.resize()
}

function onRangeChange() {
  loadStats()
}

onBeforeUnmount(() => {
  window.removeEventListener('resize', resizeCharts)
  barChart?.dispose()
  pieChart?.dispose()
  barChart = null
  pieChart = null
})

const sm = computed(() => stats.value.summary)

// ================= 列表与分页 =================
const loading = ref(false)
const list = ref<SportRecord[]>([])
const total = ref(0)
const query = reactive({ current: 1, size: 10, startDate: '', endDate: '' })
const dateRange = ref<[string, string] | null>(null)
const sportTypes = ['跑步', '步行', '骑行', '游泳', '健身', '瑜伽', '球类', '其他']

function onDateChange(val: [string, string] | null) {
  query.startDate = val?.[0] ?? ''
  query.endDate = val?.[1] ?? ''
}

async function loadList() {
  loading.value = true
  try {
    const res: PageResult<SportRecord> = await sportApi.list({
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
const dialogTitle = ref('新增运动记录')
const saving = ref(false)
const editId = ref<number | null>(null)
const formRef = ref<FormInstance>()
const form = reactive({
  sportType: '跑步' as string,
  duration: undefined as number | undefined,
  calories: 0 as number,
  recordDate: '' as string,
  remark: '' as string,
})

const rules = {
  sportType: [{ required: true, message: '请选择运动类型', trigger: 'change' }],
  duration: [
    { required: true, message: '请输入运动时长', trigger: 'blur' },
    { type: 'number', min: 1, max: 1440, message: '时长应在 1~1440 分钟', trigger: 'blur' },
  ],
  calories: [
    { type: 'number', min: 0, max: 10000, message: '热量应在 0~10000 千卡', trigger: 'blur' },
  ],
  recordDate: [{ required: true, message: '请选择运动日期', trigger: 'change' }],
}

/** 各运动类型每小时大致消耗（千卡），供快捷估算参考 */
const calorieRef: Record<string, number> = {
  跑步: 600, 步行: 200, 骑行: 400, 游泳: 500, 健身: 400, 瑜伽: 180, 球类: 450, 其他: 300,
}
function estimateCalories() {
  if (!form.duration || !form.sportType) return
  form.calories = Math.round((calorieRef[form.sportType] ?? 300) * form.duration / 60)
}

function openCreate() {
  editId.value = null
  dialogTitle.value = '新增运动记录'
  form.sportType = '跑步'
  form.duration = undefined
  form.calories = 0
  form.recordDate = new Date().toISOString().slice(0, 10)
  form.remark = ''
  dialogVisible.value = true
}

function openEdit(row: SportRecord) {
  editId.value = row.id!
  dialogTitle.value = '编辑运动记录'
  form.sportType = row.sportType
  form.duration = row.duration
  form.calories = row.calories ?? 0
  form.recordDate = row.recordDate
  form.remark = row.remark ?? ''
  dialogVisible.value = true
}

async function submit() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return
  saving.value = true
  try {
    const payload = {
      sportType: form.sportType,
      duration: form.duration!,
      calories: form.calories,
      recordDate: form.recordDate,
      remark: form.remark,
    }
    if (editId.value == null) {
      await sportApi.create(payload)
      ElMessage.success('新增成功')
    } else {
      await sportApi.update(editId.value, payload)
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
async function handleDelete(row: SportRecord) {
  try {
    await ElMessageBox.confirm(
      `确定删除 ${row.recordDate} 的「${row.sportType}」记录吗？此操作不可恢复。`,
      '删除确认',
      { type: 'warning', confirmButtonText: '删除', cancelButtonText: '取消' },
    )
  } catch {
    return
  }
  try {
    await sportApi.remove(row.id!)
    ElMessage.success('删除成功')
    loadList()
    loadStats()
  } catch {
    // 拦截器已统一提示
  }
}

// ================= 详情 =================
const detailVisible = ref(false)
const detail = ref<SportRecord | null>(null)
const detailLoading = ref(false)

async function handleDetail(row: SportRecord) {
  detailVisible.value = true
  detail.value = null
  detailLoading.value = true
  try {
    detail.value = await sportApi.detail(row.id!)
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
  <div class="sport-page">
    <!-- 统计区 -->
    <el-card shadow="never" class="stats-card">
      <template #header>
        <div class="card-header">
          <span class="title">运动统计</span>
          <el-radio-group v-model="range" size="small" @change="onRangeChange">
            <el-radio-button v-for="opt in rangeOptions" :key="opt.value" :value="opt.value">
              {{ opt.label }}
            </el-radio-button>
          </el-radio-group>
        </div>
      </template>

      <div v-loading="statsLoading">
        <div class="stat-grid">
          <div class="stat-item">
            <div class="stat-label">总运动时长</div>
            <div class="stat-value">{{ sm.totalDuration }}<span class="unit">分钟</span></div>
          </div>
          <div class="stat-item">
            <div class="stat-label">总消耗热量</div>
            <div class="stat-value calorie">{{ sm.totalCalories }}<span class="unit">千卡</span></div>
          </div>
          <div class="stat-item">
            <div class="stat-label">运动次数</div>
            <div class="stat-value">{{ sm.totalCount }}<span class="unit">次</span></div>
          </div>
          <div class="stat-item">
            <div class="stat-label">活跃天数</div>
            <div class="stat-value">{{ sm.activeDays }}<span class="unit">天</span></div>
          </div>
          <div class="stat-item">
            <div class="stat-label">日均时长</div>
            <div class="stat-value">{{ sm.avgDuration }}<span class="unit">分钟</span></div>
          </div>
          <div class="stat-item">
            <div class="stat-label">最常运动</div>
            <div class="stat-value small">{{ sm.topSportType || '—' }}</div>
          </div>
        </div>

        <div class="chart-row">
          <div class="chart-box">
            <div class="chart-title">每日运动时长与热量</div>
            <div ref="barEl" class="chart"></div>
          </div>
          <div class="chart-box">
            <div class="chart-title">运动类型分布</div>
            <div ref="pieEl" class="chart"></div>
          </div>
        </div>
        <div v-if="!statsLoading && sm.totalCount === 0" class="chart-empty">暂无运动数据，添加记录后即可查看统计</div>
      </div>
    </el-card>

    <!-- 记录管理区 -->
    <el-card shadow="never" class="list-card">
      <template #header>
        <div class="card-header">
          <span class="title">运动记录</span>
          <el-button type="primary" @click="openCreate">新增记录</el-button>
        </div>
      </template>

      <div class="filter-bar">
        <span class="filter-label">运动日期</span>
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
        <el-table-column prop="recordDate" label="运动日期" width="120" />
        <el-table-column prop="sportType" label="运动类型" width="110">
          <template #default="{ row }"><el-tag type="success" effect="light">{{ row.sportType }}</el-tag></template>
        </el-table-column>
        <el-table-column prop="duration" label="时长(分钟)" width="120">
          <template #default="{ row }"><b>{{ row.duration }}</b></template>
        </el-table-column>
        <el-table-column prop="calories" label="热量(千卡)" width="120">
          <template #default="{ row }"><b class="calorie">{{ row.calories ?? 0 }}</b></template>
        </el-table-column>
        <el-table-column prop="remark" label="备注" min-width="140" show-overflow-tooltip />
        <el-table-column prop="createTime" label="创建时间" width="170" />
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
        <el-form-item label="运动类型" prop="sportType">
          <el-select v-model="form.sportType" placeholder="选择运动类型" style="width: 100%">
            <el-option v-for="t in sportTypes" :key="t" :label="t" :value="t" />
          </el-select>
        </el-form-item>
        <el-form-item label="时长(分钟)" prop="duration">
          <el-input-number v-model="form.duration" :min="1" :max="1440" style="width: 100%" />
        </el-form-item>
        <el-form-item label="消耗热量" prop="calories">
          <div class="calorie-input">
            <el-input-number v-model="form.calories" :min="0" :max="10000" style="width: 100%" />
            <el-button size="small" @click="estimateCalories">估算</el-button>
          </div>
          <div class="form-tip">千卡，可按类型和时长自动估算</div>
        </el-form-item>
        <el-form-item label="运动日期" prop="recordDate">
          <el-date-picker v-model="form.recordDate" type="date" value-format="YYYY-MM-DD" placeholder="选择日期" style="width: 100%" />
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="form.remark" type="textarea" :rows="2" maxlength="255" show-word-limit placeholder="可选" />
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
          <div class="detail-row"><span>运动日期</span><b>{{ detail.recordDate }}</b></div>
          <div class="detail-row"><span>运动类型</span><b>{{ detail.sportType }}</b></div>
          <div class="detail-row"><span>运动时长</span><b>{{ detail.duration }} 分钟</b></div>
          <div class="detail-row"><span>消耗热量</span><b class="calorie">{{ detail.calories ?? 0 }} 千卡</b></div>
          <div class="detail-row"><span>备注</span><span>{{ detail.remark || '—' }}</span></div>
          <div class="detail-row"><span>创建时间</span><span>{{ detail.createTime }}</span></div>
        </template>
      </div>
    </el-dialog>
  </div>
</template>

<style scoped>
.sport-page { padding: 4px; display: flex; flex-direction: column; gap: 16px; }
.stats-card, .list-card { margin-bottom: 0; }
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
.stat-value.calorie, .calorie { color: #67c23a; }
.stat-value .unit { font-size: 12px; font-weight: 400; margin-left: 3px; color: #909399; }

.chart-row { display: flex; gap: 16px; }
.chart-box { flex: 1; min-width: 0; }
.chart-title { font-size: 13px; color: #606266; font-weight: 600; margin-bottom: 4px; }
.chart { width: 100%; height: 260px; }
.chart-empty { text-align: center; color: #909399; padding: 8px; font-size: 13px; }

.calorie-input { display: flex; align-items: center; gap: 8px; width: 100%; }
.form-tip { font-size: 12px; color: #909399; margin-top: 2px; }

@media (max-width: 1100px) {
  .stat-grid { grid-template-columns: repeat(3, 1fr); }
  .chart-row { flex-direction: column; }
}

.detail-body { min-height: 120px; }
.detail-row { display: flex; justify-content: space-between; padding: 10px 4px; border-bottom: 1px solid #f0f0f0; }
.detail-row > span:first-child { color: #909399; }
</style>
