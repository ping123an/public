<script setup lang="ts">
import { reactive, ref, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getProfile, saveProfile } from '@/api/profile'
import type { HealthProfile } from '@/types'

const loading = ref(false)
const saving = ref(false)
const editing = ref(false)
const profile = ref<HealthProfile | null>(null)

/** 表单数据（数字用 number|undefined，便于 el-input-number 绑定） */
const form = reactive({
  gender: 0,
  birthday: '' as string,
  height: undefined as number | undefined,
  targetWeight: undefined as number | undefined,
  dailyWaterGoal: 2000,
  weeklyExerciseGoal: 150,
})

const genderOptions = [
  { label: '未知', value: 0 },
  { label: '男', value: 1 },
  { label: '女', value: 2 },
]

const genderText = computed(() => genderOptions.find((o) => o.value === profile.value?.gender)?.label || '未设置')

async function load() {
  loading.value = true
  try {
    const data: HealthProfile | null = await getProfile()
    profile.value = data
    if (data) {
      form.gender = data.gender ?? 0
      form.birthday = data.birthday ?? ''
      form.height = data.height != null ? Number(data.height) : undefined
      form.targetWeight = data.targetWeight != null ? Number(data.targetWeight) : undefined
      form.dailyWaterGoal = data.dailyWaterGoal ?? 2000
      form.weeklyExerciseGoal = data.weeklyExerciseGoal ?? 150
    } else {
      // 无档案时给出默认目标值，方便直接填写
      form.gender = 0
      form.birthday = ''
      form.height = undefined
      form.targetWeight = undefined
      form.dailyWaterGoal = 2000
      form.weeklyExerciseGoal = 150
    }
  } catch {
    // 请求拦截器已统一提示
  } finally {
    loading.value = false
  }
}

function startEdit() {
  editing.value = true
}

function cancelEdit() {
  editing.value = false
  // 还原为服务端数据
  const data = profile.value
  if (data) {
    form.gender = data.gender ?? 0
    form.birthday = data.birthday ?? ''
    form.height = data.height != null ? Number(data.height) : undefined
    form.targetWeight = data.targetWeight != null ? Number(data.targetWeight) : undefined
    form.dailyWaterGoal = data.dailyWaterGoal ?? 2000
    form.weeklyExerciseGoal = data.weeklyExerciseGoal ?? 150
  }
}

async function handleSave() {
  saving.value = true
  try {
    const result = await saveProfile({
      gender: form.gender,
      birthday: form.birthday || undefined,
      height: form.height,
      targetWeight: form.targetWeight,
      dailyWaterGoal: form.dailyWaterGoal,
      weeklyExerciseGoal: form.weeklyExerciseGoal,
    })
    profile.value = result
    editing.value = false
    ElMessage.success('健康档案已保存到数据库')
  } catch {
    // 请求拦截器已统一提示
  } finally {
    saving.value = false
  }
}

onMounted(load)
</script>

<template>
  <div v-loading="loading">
    <!-- 编辑模式 -->
    <el-card v-if="editing" class="profile-card" shadow="never">
      <template #header>
        <div class="card-header">
          <span class="card-title">编辑健康档案</span>
          <el-tag type="success" size="small">仅本人可见</el-tag>
        </div>
      </template>

      <el-form :model="form" label-width="110px" class="profile-form">
        <div class="section-title">基本信息</div>
        <el-form-item label="性别">
          <el-radio-group v-model="form.gender">
            <el-radio v-for="item in genderOptions" :key="item.value" :value="item.value">
              {{ item.label }}
            </el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="出生日期">
          <el-date-picker
            v-model="form.birthday"
            type="date"
            value-format="YYYY-MM-DD"
            placeholder="请选择出生日期"
            style="width: 260px"
          />
        </el-form-item>
        <el-form-item label="身高">
          <el-input-number v-model="form.height" :min="50" :max="250" :precision="1" controls-position="right" />
          <span class="unit">cm</span>
        </el-form-item>

        <div class="section-title goal-section">健康目标</div>
        <el-form-item label="目标体重">
          <el-input-number v-model="form.targetWeight" :min="20" :max="300" :precision="1" controls-position="right" />
          <span class="unit">kg</span>
        </el-form-item>
        <el-form-item label="每日饮水目标">
          <el-input-number v-model="form.dailyWaterGoal" :min="200" :max="10000" :step="100" controls-position="right" />
          <span class="unit">ml</span>
        </el-form-item>
        <el-form-item label="每周运动目标">
          <el-input-number v-model="form.weeklyExerciseGoal" :min="0" :max="10080" :step="30" controls-position="right" />
          <span class="unit">分钟</span>
        </el-form-item>
      </el-form>

      <div class="actions">
        <el-button @click="cancelEdit">取消</el-button>
        <el-button type="primary" :loading="saving" @click="handleSave">保存</el-button>
      </div>
    </el-card>

    <!-- 查看模式 -->
    <el-card v-else class="profile-card" shadow="never">
      <template #header>
        <div class="card-header">
          <span class="card-title">个人健康档案</span>
          <el-button type="primary" size="small" @click="startEdit">
            {{ profile ? '编辑' : '填写档案' }}
          </el-button>
        </div>
      </template>

      <template v-if="profile">
        <div class="section-title">基本信息</div>
        <el-descriptions :column="2" border class="info-desc">
          <el-descriptions-item label="性别">{{ genderText }}</el-descriptions-item>
          <el-descriptions-item label="出生日期">{{ profile.birthday || '未设置' }}</el-descriptions-item>
          <el-descriptions-item label="身高">{{ profile.height != null ? profile.height + ' cm' : '未设置' }}</el-descriptions-item>
          <el-descriptions-item label="最后更新">{{ profile.updateTime || '-' }}</el-descriptions-item>
        </el-descriptions>

        <div class="section-title goal-section">健康目标</div>
        <el-row :gutter="16">
          <el-col :span="8">
            <div class="goal-card">
              <div class="goal-label">目标体重</div>
              <div class="goal-value">{{ profile.targetWeight != null ? profile.targetWeight : '--' }}<small> kg</small></div>
            </div>
          </el-col>
          <el-col :span="8">
            <div class="goal-card water">
              <div class="goal-label">每日饮水目标</div>
              <div class="goal-value">{{ profile.dailyWaterGoal ?? '--' }}<small> ml</small></div>
            </div>
          </el-col>
          <el-col :span="8">
            <div class="goal-card sport">
              <div class="goal-label">每周运动目标</div>
              <div class="goal-value">{{ profile.weeklyExerciseGoal ?? '--' }}<small> 分钟</small></div>
            </div>
          </el-col>
        </el-row>
      </template>

      <el-empty v-else description="还没有健康档案，点击右上角「填写档案」开始记录" />
    </el-card>

    <el-alert
      class="tip"
      type="info"
      :closable="false"
      show-icon
      title="健康档案仅您本人可见，数据已按账号隔离并持久化保存到数据库。"
    />
  </div>
</template>

<style scoped>
.profile-card {
  border-radius: 12px;
}
.profile-form {
  max-width: 480px;
  margin-top: 8px;
}
.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}
.card-title {
  font-size: 16px;
  font-weight: 600;
  color: #2c3e35;
}
.section-title {
  font-size: 14px;
  font-weight: 600;
  color: #2e9e6b;
  padding: 8px 0 14px;
  border-bottom: 1px solid #eef2f0;
  margin-bottom: 18px;
}
.goal-section {
  margin-top: 16px;
}
.unit {
  margin-left: 10px;
  color: #8a9a92;
}
.actions {
  margin-top: 12px;
  padding-left: 110px;
}
.info-desc {
  max-width: 560px;
}
.goal-card {
  background: #e3f3ea;
  border-radius: 12px;
  padding: 18px;
  text-align: center;
}
.goal-card.water { background: #e4f4f7; }
.goal-card.sport { background: #e6f0fb; }
.goal-label {
  font-size: 13px;
  color: #6a8a7a;
}
.goal-value {
  font-size: 26px;
  font-weight: 700;
  color: #2c6a4f;
  margin-top: 6px;
}
.goal-value small {
  font-size: 13px;
  font-weight: 400;
}
.tip {
  margin-top: 20px;
  max-width: 720px;
}
</style>
