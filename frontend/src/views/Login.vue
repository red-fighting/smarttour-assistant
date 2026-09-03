<template>
  <div class="login-container page">
    <div class="login-box">
      <h2 class="title">{{ isRegister ? '注册' : '登录' }}</h2>
      <!-- 用户头像 -->
      <div class="avatar-wrapper" @click="selectAvatar">
        <img :src="form.avatar || defaultAvatar" class="avatar-img" />
        <!-- 注册模式才显示编辑图标 -->
        <div v-if="isRegister" class="avatar-edit-badge">
          <van-icon name="photograph" size="16" color="#fff" />
        </div>
      </div>
      <!-- 隐藏的文件选择器（仅注册模式可用） -->
      <input type="file" ref="fileInput" accept="image/*" style="display:none" @change="handleFileChange" />
      <van-form @submit="onSubmit" @failed="onFormFailed" ref="formRef">
        <van-field
          v-model="form.username"
          name="username"
          label="用户名"
          placeholder="请输入用户名"
          :rules="[{ required: true, message: '请输入用户名' }]"
        />
        <van-field
          v-model="form.password"
          type="password"
          name="password"
          label="密码"
          placeholder="请输入密码"
          :rules="[{ required: true, message: '请输入密码' }]"
        />
        <!-- 用户类型选择（仅注册时显示；label + 两个卡片同一行） -->
        <div v-if="isRegister" class="role-select">
          <span class="role-label">注册为</span>
          <div class="role-cards">
            <div class="role-option" :class="{ 'role-option--active': form.role === '0' }" @click="form.role = '0'">
              <van-icon name="user-o" size="16" />
              <span>普通用户</span>
            </div>
            <div class="role-option" :class="{ 'role-option--active': form.role === '1' }" @click="form.role = '1'">
              <van-icon name="manager-o" size="16" />
              <span>管理员</span>
            </div>
          </div>
        </div>
        <!-- 注册管理员时额外需要管理员注册码 -->
        <van-field
          v-if="isRegister && form.role === '1'"
          v-model="form.adminCode"
          name="registerCode"
          label="管理员注册码"
          placeholder="请输入管理员注册码"
          :rules="[{ required: true, message: '请输入管理员注册码' }]"
        />
        <van-field 
          v-if="isRegister"
          v-model="form.confirmPassword"
          type="password"
          name="confirmpassword"
          label="确认密码"
          placeholder="请再次输入密码"
          :rules="[{ required: true, message: '请再次输入密码' }]"
        />
        <van-field 
          v-if="isRegister"
          v-model="form.phone"
          name="phone"
          label="手机号"
          placeholder="请输入手机号"
          :rules="[{ required: true, message: '请输入手机号' }]"
        />
        <van-field 
          v-if="isRegister"
          v-model="form.email"
          name="email"
          label="邮箱"
          placeholder="请输入邮箱"
          :rules="[{ required: true, message: '请输入邮箱' }]"
        />
        <div style="margin:16px">
          <van-button round type="primary" block :loading="loading" :native-type="isRegister ? 'submit' : 'button'" @click="handlePrimaryClick">{{ isRegister ? '注册' : '登录' }}</van-button>
        </div>
        <div style="text-align:center;">
          <span class="switch-link" @click="toggleMode">
            {{ isRegister ? '已有帐号？去登录' : '没有账号？去注册' }}  
          </span>
        </div>
      </van-form>
    </div>
  </div>
</template>

<script setup>
import { ref, watch, onMounted, onBeforeUnmount } from 'vue'
import { useRouter } from 'vue-router'
import { showToast } from 'vant'
import request from '../utils/request'
import { getAvatarByUsername } from '../api/index'
import { clearAllUserStorage } from '../utils/storage'

//  1. 路由 & DOM 引用
// ============================================================
const router = useRouter()
const isRegister = ref(false)
const fileInput = ref(null)
const formRef = ref(null)

/**
 * 登录模式：不用 native-type=submit，手动触发 van-form 校验。
 * Vant 在部分情况下会让校验失败的表单触发浏览器原生 GET 提交，
 * 导致 URL 变成 /login?username=xxx&password=xxx，为了避免这个，
 * 登录按钮改成普通 button，然后通过 formRef.submit() 触发校验。
 */
const handlePrimaryClick = () => {
  if (isRegister.value) {
    // 注册模式：用原生 submit（form 字段多，避免重复写逻辑）
    formRef.value?.submit()
    return
  }
  // 登录模式：自定义校验
  if (!form.value.username) {
    showToast('请输入用户名')
    return
  }
  if (!form.value.password) {
    showToast('请输入密码')
    return
  }
  onSubmit()
}
const onFormFailed = () => {
  // 表单校验失败：提示
  showToast('请完整填写表单')
}
const form = ref({
  username: '',
  password: '',
  confirmPassword: '',
  phone: '',
  email: '',
  avatar: '',
  role: '0', // 0=普通用户 1=管理员
  adminCode: '' // 管理员注册码（仅注册管理员时需要）
})
const loading = ref(false)
const defaultAvatar = 'https://api.dicebear.com/7.x/avataaars/svg?seed=default'

// ========= 每次进入/离开页面强制清空表单 =========
const resetForm = () => {
  if (avatarTimer) {
    clearTimeout(avatarTimer)
    avatarTimer = null
  }
  isRegister.value = false
  form.value.username = ''
  form.value.password = ''
  form.value.confirmPassword = ''
  form.value.phone = ''
  form.value.email = ''
  form.value.avatar = ''
  form.value.role = '0'
  form.value.adminCode = ''
  loading.value = false
}

onMounted(() => {
  resetForm()
})

onBeforeUnmount(() => {
  if (avatarTimer) {
    clearTimeout(avatarTimer)
    avatarTimer = null
  }
})

//  2. 响应式数据
// ============================================================
// 切换登录/注册模式
const toggleMode = () => {
  // 清除头像查询计时器
  if (avatarTimer) {
    clearTimeout(avatarTimer)
    avatarTimer = null
  }
  isRegister.value = !isRegister.value
  // 清空所有表单字段
  form.value.username = ''
  form.value.password = ''
  form.value.confirmPassword = ''
  form.value.phone = ''
  form.value.email = ''
  form.value.avatar = ''
  form.value.role = '0'
  form.value.adminCode = ''
  loading.value = false
}

//  3. 登录模式：输入用户名后自动获取头像
// ============================================================
let avatarTimer = null
watch(() => form.value.username, (val) => {
  // 仅登录模式 + 用户名长度 >= 2 时才查询
  if (isRegister.value || !val || val.length < 2) {
    if (!isRegister.value) form.value.avatar = ''
    return
  }
  // 防抖：输入停止 500ms 后再请求
  if (avatarTimer) clearTimeout(avatarTimer)
  avatarTimer = setTimeout(async () => {
    try {
      const res = await getAvatarByUsername(val)
      if (res.code === 200 && res.data?.avatar) {
        form.value.avatar = res.data.avatar
      } else {
        form.value.avatar = ''
      }
    } catch (e) {
      // 用户不存在或后端不可用，不显示头像
      form.value.avatar = ''
    }
  }, 500)
})

//  4. 核心业务逻辑
// ============================================================
const onSubmit = async () => {
  try {
    loading.value = true
    let res
    const reqUrl = isRegister.value ? '/user/register' : '/user/login'

    if (isRegister.value) {
      // 注册：用 FormData 发送（后端用 @RequestParam 接收）
      const formData = new FormData()
      formData.append('username', form.value.username)
      formData.append('password', form.value.password)
      formData.append('confirmPassword', form.value.confirmPassword)
      formData.append('phone', form.value.phone)
      formData.append('email', form.value.email)
      if (form.value.avatar) {
        formData.append('avatar', form.value.avatar)
      }
      formData.append('role', form.value.role)
      if (form.value.role === '1' && form.value.adminCode) {
        formData.append('registerCode', form.value.adminCode)
      }
      res = await request.post(reqUrl, formData)
    } else {
      // 登录：JSON 发送
      res = await request.post(reqUrl, {
        username: form.value.username,
        password: form.value.password
      }, {
        headers: { 'Content-Type': 'application/json' }
      })
    }

    if (res.code === 200) {
      // 1. 先清掉旧用户的隔离数据（防止账号切换时数据残留）
      try {
        const oldInfo = JSON.parse(localStorage.getItem('userInfo') || '{}')
        if (oldInfo?.userId && oldInfo.userId !== res.data.userId) {
          clearAllUserStorage(oldInfo.userId)
        }
      } catch (e) { /* noop */ }

      // 2. 保存新用户 token + 信息
      localStorage.setItem('token', res.data.token)
      request.defaults.headers.common['Authorization'] = `Bearer ${res.data.token}`
      localStorage.setItem('userInfo', JSON.stringify(res.data))
      showToast(isRegister.value ? '注册成功' : '登录成功')
      // 提交成功后清空表单，防止下次进入残留
      form.value.username = ''
      form.value.password = ''
      form.value.confirmPassword = ''
      form.value.phone = ''
      form.value.email = ''
      form.value.avatar = ''
      form.value.adminCode = ''
      // 3. 按后端返回的 role 自动跳转（登录/注册通用）
      const userRole = Number(res.data.role ?? 0)
      router.push(userRole === 1 ? '/admin' : '/')
    } else {
      showToast(res.message || '操作失败')
    }
  } catch (error) {
    console.error('===== 注册/登录请求失败 =====')
    console.error('完整 error 对象:', error)
    console.error('HTTP status:', error.response?.status)
    console.error('响应体:', error.response?.data)
    console.error('error.message:', error.message)
    showToast(error.response?.data?.message || error.message || '网络错误')
  }
}

//  5. 注册模式：选择头像
// ============================================================
const selectAvatar = () => {
  // 仅注册模式可以选头像
  if (!isRegister.value) return
  fileInput.value.click()
}

const handleFileChange = async (event) => {
  const file = event.target.files[0]
  if (!file) return

  // 校验文件大小和类型
  if (file.size > 2 * 1024 * 1024) {
    showToast('图片大小不能超过2MB')
    return
  }
  if (!file.type.startsWith('image/')) {
    showToast('请上传图片')
    return
  }
  // 读取为 base64 预览
  const reader = new FileReader()
  reader.onload = (e) => {
    form.value.avatar = e.target.result
  }
  reader.readAsDataURL(file)
  // 清空 input
  event.target.value = ''
}
</script>
<style scoped>
/* ===== 整体渐变背景 ===== */
.login-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  padding: 20px;
  background: linear-gradient(135deg, #43cea2 0%, #185a9d 100%);
}

/* ===== 登录卡片：增大圆角 + 增强阴影 ===== */
.login-box {
  width: 400px;
  max-width: 100%;
  background: #fff;
  border-radius: 20px;
  padding: 40px 30px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.15);
}

/* ===== 头像区域：白色环形边框 + 柔和光晕 ===== */
.avatar-wrapper {
  position: relative;
  width: 100px;
  height: 100px;
  margin: 8px auto 28px;
  padding: 4px;
  border-radius: 50%;
  background: linear-gradient(135deg, #43cea2 0%, #185a9d 100%);
  box-shadow: 0 8px 28px rgba(67, 206, 162, 0.35);
}
.avatar-img {
  width: 100%;
  height: 100%;
  border-radius: 50%;
  object-fit: cover;
  border: 3px solid #fff;
  display: block;
}
.avatar-edit-badge {
  position: absolute;
  bottom: 2px;
  right: 2px;
  background: linear-gradient(135deg, #43cea2 0%, #185a9d 100%);
  border-radius: 50%;
  width: 28px;
  height: 28px;
  display: flex;
  align-items: center;
  justify-content: center;
  border: 2px solid #fff;
  box-shadow: 0 2px 8px rgba(67, 206, 162, 0.35);
}

/* ===== 标题 ===== */
.title {
  text-align: center;
  margin-bottom: 28px;
  color: #1a1a2e;
  font-weight: 700;
  font-size: 22px;
  letter-spacing: 1px;
}

/* ===== 输入框：浅灰圆角背景 ===== */
.login-box :deep(.van-cell) {
  background: #f5f7fa !important;
  border-radius: 12px !important;
  margin-bottom: 12px;
  padding: 12px 14px;
  overflow: hidden;
}
.login-box :deep(.van-cell::after) {
  border-bottom-width: 0;
}
.login-box :deep(.van-field__label) {
  color: #1a1a2e;
  font-weight: 500;
}
.login-box :deep(.van-field__control) {
  color: #1a1a2e;
}
.login-box :deep(.van-field__control::placeholder) {
  color: #C8C9CC;
}

/* ===== 登录/注册按钮：渐变背景 + 阴影 ===== */
.login-box :deep(.van-button--primary) {
  background: linear-gradient(135deg, #43cea2 0%, #185a9d 100%) !important;
  border: none !important;
  font-size: 16px;
  font-weight: 600;
  letter-spacing: 1px;
  height: 46px;
  box-shadow: 0 6px 20px rgba(67, 206, 162, 0.3);
  transition: all 0.3s ease;
}
.login-box :deep(.van-button--primary:active) {
  transform: scale(0.98);
  box-shadow: 0 3px 12px rgba(67, 206, 162, 0.25);
}

/* ===== 切换链接：精致样式 ===== */
.switch-link {
  color: #43cea2;
  cursor: pointer;
  font-size: 14px;
  font-weight: 500;
  letter-spacing: 0.5px;
  display: inline-block;
  transition: all 0.3s ease;
}
.switch-link:hover {
  color: #185a9d;
  text-decoration: underline;
}
.switch-link:active {
  opacity: 0.7;
}

/* ===== 用户类型选择（label + 两个卡片同一行，完全不用 Vant radio） ===== */
.role-select {
  margin: 0 0 12px;
  padding: 10px 14px;
  background: #f5f7fa;
  border-radius: 12px;
  display: flex;
  flex-direction: row;
  align-items: center;
  gap: 16px;
}
.role-label {
  font-weight: 500;
  color: #1a1a2e;
  font-size: 14px;
  white-space: nowrap;
  /* 让 span 表现为固定宽度的 inline block */
  flex: 0 0 auto;
  width: auto;
  min-width: auto;
}
/* 卡片容器：flex:1 占剩余空间，内部两个卡片等宽 */
.role-cards {
  flex: 1;
  display: flex;
  gap: 10px;
  width: auto;
}
.role-option {
  flex: 1 1 0;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  padding: 10px 8px;
  border-radius: 10px;
  background: #fff;
  color: #666;
  font-size: 13px;
  font-weight: 500;
  border: 1.5px solid transparent;
  cursor: pointer;
  user-select: none;
  transition: all 0.22s ease;
  min-width: 0;
}
.role-option:hover {
  background: #eef4fc;
}
/* 选中态：浅色高亮 + 主色文字（和整体登录页主题呼应但不抢眼） */
.role-option--active {
  background: linear-gradient(135deg, #e8f8f0 0%, #dbeafc 100%);
  color: #185a9d;
  border-color: #43cea2;
  box-shadow: 0 2px 10px rgba(67, 206, 162, 0.18);
}
</style>
