<script setup lang="ts">
import { InfoFilled } from '@element-plus/icons-vue'

defineProps<{
  title: string
  description: string
}>()
</script>

<template>
  <el-card class="placeholder-card" shadow="never">
    <template #header>
      <div class="card-header">
        <span class="card-title">{{ title }}</span>
        <el-tag type="success" size="small">第一阶段·框架</el-tag>
      </div>
    </template>
    <div class="placeholder-body">
      <el-icon class="placeholder-icon"><InfoFilled /></el-icon>
      <h3>{{ title }}</h3>
      <p>{{ description }}</p>
      <p class="hint">该模块将在后续开发阶段逐步完善功能。</p>
    </div>
  </el-card>
</template>

<style scoped>
.placeholder-card {
  border-radius: 12px;
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
.placeholder-body {
  text-align: center;
  padding: 60px 20px;
  color: #8a9a92;
}
.placeholder-icon {
  font-size: 52px;
  color: #b8d9c7;
}
.placeholder-body h3 {
  margin: 16px 0 8px;
  color: #4a5a52;
  font-size: 18px;
}
.placeholder-body p {
  margin: 4px 0;
}
.hint {
  font-size: 13px;
  color: #b0bdb6;
}
</style>
