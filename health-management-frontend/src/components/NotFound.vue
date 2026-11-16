<script setup lang="ts">
import { useRouter } from 'vue-router'

const router = useRouter()
</script>

<template>
  <div class="not-found">
    <div class="code">404</div>
    <div class="message">抱歉，你访问的页面不存在</div>
    <div class="hint">请检查地址是否正确，或返回首页继续浏览</div>
    <el-button type="primary" @click="router.push('/dashboard')">返回首页</el-button>
  </div>
</template>

<style scoped>
.not-found {
  min-height: 60vh;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 12px;
  text-align: center;
}
.code {
  font-size: 96px;
  font-weight: 800;
  color: #2e9e6b;
  line-height: 1;
}
.message {
  font-size: 20px;
  font-weight: 600;
  color: #2c3e35;
}
.hint {
  font-size: 13px;
  color: #8a9a92;
  margin-bottom: 8px;
}
</style>
