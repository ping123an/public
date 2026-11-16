<script setup lang="ts">
import { reactive, ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance, UploadRawFile } from 'element-plus'
import { useUserStore } from '@/stores/user'
import { updatePassword } from '@/api/auth'

const userStore = useUserStore()
const pageLoading = ref(false)

const profileForm = reactive({ nickname: '', email: '' })
const profileRef = ref<FormInstance>()
const savingProfile = ref(false)

const passwordForm = reactive({ oldPassword: '', newPassword: '', confirmPassword: '' })
const passwordRef = ref<FormInstance>()
const savingPassword = ref(false)

const avatarUploading = ref(false)

onMounted(async () => {
  pageLoading.value = true
  try {
    // 拉取最新用户信息，确保表单为最新数据
    await userStore.loadUserInfo()
    const info = userStore.userInfo
    profileForm.nickname = info?.nickname || ''
    profileForm.email = info?.email || ''
  } finally {
    pageLoading.value = false
  }
})

const profileRules = {
  nickname: [{ max: 32, message: '昵称长度不能超过 32 位', trigger: 'blur' }],
  email: [{ type: 'email', message: '邮箱格式不正确', trigger: 'blur' }],
}

const passwordRules = {
  oldPassword: [{ required: true, message: '请输入原密码', trigger: 'blur' }],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, message: '新密码长度至少 6 位', trigger: 'blur' },
  ],
  confirmPassword: [
    { required: true, message: '请再次输入新密码', trigger: 'blur' },
    {
      validator: (_rule: unknown, value: string, callback: (e?: Error) => void) => {
        if (value !== passwordForm.newPassword) callback(new Error('两次输入的密码不一致'))
        else callback()
      },
      trigger: 'blur',
    },
  ],
}

async function saveProfile() {
  const valid = await profileRef.value?.validate().catch(() => false)
  if (!valid) return
  savingProfile.value = true
  try {
    await userStore.updateProfile({ nickname: profileForm.nickname, email: profileForm.email })
    ElMessage.success('个人资料已保存')
  } catch {
    // 请求拦截器已统一提示
  } finally {
    savingProfile.value = false
  }
}

/** 头像：读取文件 -> canvas 压缩到 160x160 JPEG -> base64 上传 */
function compressImage(file: UploadRawFile): Promise<string> {
  return new Promise((resolve, reject) => {
    const reader = new FileReader()
    reader.onload = () => {
      const img = new Image()
      img.onload = () => {
        const size = 160
        const canvas = document.createElement('canvas')
        canvas.width = size
        canvas.height = size
        const ctx = canvas.getContext('2d')
        if (!ctx) {
          reject(new Error('浏览器不支持图片处理'))
          return
        }
        // 居中裁剪为正方形并绘制
        const minSide = Math.min(img.width, img.height)
        const sx = (img.width - minSide) / 2
        const sy = (img.height - minSide) / 2
        ctx.drawImage(img, sx, sy, minSide, minSide, 0, 0, size, size)
        resolve(canvas.toDataURL('image/jpeg', 0.8))
      }
      img.onerror = () => reject(new Error('图片加载失败'))
      img.src = reader.result as string
    }
    reader.onerror = () => reject(new Error('读取文件失败'))
    reader.readAsDataURL(file)
  })
}

async function handleAvatarChange(file: UploadRawFile) {
  const isImage = file.type.startsWith('image/')
  if (!isImage) {
    ElMessage.error('请选择图片文件')
    return
  }
  if (file.size > 5 * 1024 * 1024) {
    ElMessage.error('图片不能超过 5MB')
    return
  }
  avatarUploading.value = true
  try {
    const base64 = await compressImage(file)
    await userStore.updateAvatar(base64)
    ElMessage.success('头像已更新')
  } catch {
    // 上传接口失败由请求拦截器统一提示；本地压缩失败极少发生
  } finally {
    avatarUploading.value = false
  }
}

async function removeAvatar() {
  try {
    await ElMessageBox.confirm('确定移除当前头像吗？', '提示', {
      type: 'warning', confirmButtonText: '移除', cancelButtonText: '取消',
    })
  } catch {
    return
  }
  try {
    await userStore.clearAvatar()
    ElMessage.success('头像已移除')
  } catch {
    // 拦截器已统一提示
  }
}

async function changePassword() {
  const valid = await passwordRef.value?.validate().catch(() => false)
  if (!valid) return
  savingPassword.value = true
  try {
    await updatePassword(passwordForm.oldPassword, passwordForm.newPassword)
    ElMessage.success('密码修改成功，请重新登录')
    passwordForm.oldPassword = ''
    passwordForm.newPassword = ''
    passwordForm.confirmPassword = ''
    passwordRef.value?.resetFields()
    // 密码变更后 token 仍有效，此处仅提示用户；如需强制重新登录可在此处登出
  } catch {
    // 请求拦截器已统一提示
  } finally {
    savingPassword.value = false
  }
}
</script>

<template>
  <div class="user-center" v-loading="pageLoading">
    <!-- 头像卡片 -->
    <el-card class="info-card" shadow="never">
      <template #header>
        <div class="card-header"><span class="card-title">头像设置</span></div>
      </template>
      <div class="avatar-section">
        <div class="avatar-wrap" v-loading="avatarUploading">
          <el-avatar :size="96" :src="userStore.userInfo?.avatar || undefined" class="avatar">
            {{ (userStore.userInfo?.nickname || userStore.userInfo?.username || '?').charAt(0).toUpperCase() }}
          </el-avatar>
        </div>
        <div class="avatar-actions">
          <el-upload
            :show-file-list="false"
            :auto-upload="false"
            accept="image/*"
            :on-change="(uploadFile: any) => handleAvatarChange(uploadFile.raw as UploadRawFile)"
          >
            <el-button type="primary" :loading="avatarUploading">上传头像</el-button>
          </el-upload>
          <el-button v-if="userStore.userInfo?.avatar" @click="removeAvatar">移除头像</el-button>
          <div class="avatar-tip">支持 JPG/PNG/WebP/GIF，不超过 5MB，将自动压缩为 160×160</div>
        </div>
      </div>
    </el-card>

    <!-- 个人资料 -->
    <el-card class="info-card" shadow="never">
      <template #header>
        <div class="card-header">
          <span class="card-title">个人资料</span>
          <el-tag type="info" size="small">账号：{{ userStore.userInfo?.username }}</el-tag>
        </div>
      </template>
      <el-form ref="profileRef" :model="profileForm" :rules="profileRules" label-width="90px" class="form">
        <el-form-item label="昵称" prop="nickname">
          <el-input v-model="profileForm.nickname" placeholder="请输入昵称" maxlength="32" />
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="profileForm.email" placeholder="请输入邮箱（选填）" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="savingProfile" @click="saveProfile">保存资料</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 修改密码 -->
    <el-card class="info-card" shadow="never">
      <template #header>
        <div class="card-header"><span class="card-title">修改密码</span></div>
      </template>
      <el-form ref="passwordRef" :model="passwordForm" :rules="passwordRules" label-width="90px" class="form">
        <el-form-item label="原密码" prop="oldPassword">
          <el-input v-model="passwordForm.oldPassword" type="password" show-password placeholder="请输入原密码" />
        </el-form-item>
        <el-form-item label="新密码" prop="newPassword">
          <el-input v-model="passwordForm.newPassword" type="password" show-password placeholder="请输入新密码（至少6位）" />
        </el-form-item>
        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input v-model="passwordForm.confirmPassword" type="password" show-password placeholder="请再次输入新密码" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="savingPassword" @click="changePassword">修改密码</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<style scoped>
.user-center {
  display: flex;
  flex-direction: column;
  gap: 16px;
  max-width: 720px;
}
.info-card { border-radius: 12px; }
.card-header { display: flex; align-items: center; justify-content: space-between; }
.card-title { font-size: 16px; font-weight: 600; color: #2c3e35; }
.form { margin-top: 12px; }
.avatar-section { display: flex; align-items: center; gap: 24px; padding: 8px 4px; }
.avatar-wrap { flex-shrink: 0; }
.avatar { background: #2e9e6b; color: #fff; font-size: 36px; }
.avatar-actions { display: flex; flex-direction: column; align-items: flex-start; gap: 10px; }
.avatar-tip { font-size: 12px; color: #8a9a92; }
</style>
