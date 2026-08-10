<template>
  <div class="login-container page">
    <div class="login-box">
      <h2 class="title">{{ isRegister ? '注册' : '登录' }}</h2>
      <!-- 用户卡片 -->
      <div class="avatar-wrapper" @click="selectAvatar">
        <img :src="form.avatar || defaultAvatar" class="avatar-img" />
        <div class="avatar-edit-badge">
          <van-icon name="photograph" size="16" color="#fff" />
        </div>
      </div>
      <!-- 隐藏的文件选择器 -->
      <input type="file" ref="fileInput" accept="image/*" style="display:none" @change="handleFileChange" />
      <van-form @submit="onSubmit">
        <van-field
          v-model="form.username"
          name="username"
          label="用户名"
          placeholder="请输入用户名"
          :rules="[{ required: true, message: '请输入用户名' }]"
        />
        <van-field
          v-model="form.password"
          name="password"
          label="密码"
          placeholder="请输入密码"
          :rules="[{ required: true, message: '请输入密码' }]"
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
          <van-button round type="primary" block native-type="submit">{{ isRegister ? '注册' : '登录' }}</van-button>
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
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { showToast } from 'vant'
import request from '../utils/request'

//  1. 路由 & DOM 引用
// ============================================================
const router = useRouter()
const isRegister = ref(false)
const fileInput = ref(null)
const selectedFile = ref(null)
const form = ref({
  username: '',
  password: '',
  confirmPassword: '',
  phone: '',
  email: '',
  avatar: ''
})
const loading = ref(false)
const defaultAvatar = 'https://api.dicebear.com/7.x/avataaars/svg?seed=default'
//  2. 响应式数据
// ============================================================
// 切换登录/注册模式
const toggleMode = () => {
  isRegister.value = !isRegister.value
  // 清空表单
  form.username = ''
  form.password = ''
  form.confirmPassword = ''
  form.phone = ''
  form.email = ''
  selectedFile.value = null
}
//  3. 工具函数
// ============================================================
//  4. 核心业务逻辑
// ============================================================
// async:修饰函数，代表这个函数内部有异步等待操作，必须搭配 await 使用。
//异步：网络请求、定时器、文件读写，不会卡住页面。
const onSubmit=async()=>{
  try{
    loading.value = true
    let res
    const reqUrl=isRegister.value?'/user/register':'/user/login' 
    let submitData
    if(isRegister.value){
      submitData={
        username: form.value.username,
        password: form.value.password,
        confirmPassword: form.value.confirmPassword,
        phone: form.value.phone,
        email: form.value.email,
        avatar: selectedFile.value

      }
      res = await request.post(reqUrl, submitData, {
        headers: { 'Content-Type': 'multipart/form-data' }
      })
    }else{
      const submitData = {
        username: form.value.username,
        password: form.value.password
      }

      res = await request.post(reqUrl, submitData, {
        headers: { 'Content-Type': 'application/json' }
      })
    }
    
    if(res.code===200){
      localStorage.setItem('token',res.data.token)
      localStorage.setItem('userInfo',JSON.stringify(res.data))
      //全局挂载token请求头
      request.defaults.headers.common['Authorization']=`Bearer ${res.data.token}`
      showToast(isRegister.value ? '注册成功' : '登录成功')
      router.push('/')
    }else{
      showToast(res.message || '操作失败' )
    }
  }catch(error){
    console.error('请求失败:', error)
    showToast(error.response?.data?.message || '网络错误' )
  }
}
// 选择头像
const selectAvatar = () => {
  fileInput.value.click() // 模拟点击隐藏的 <input type="file">
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
  // 保存文件对象
  selectedFile.value = file
  // 显示预览
  const reader = new FileReader()
  reader.onload = (e) => {
    form.value.avatar = e.target.result  // 预览用
  }
  reader.readAsDataURL(file)
  // 清空input
  event.target.value = ''
}
//  5. 生命周期 & 监听
// ============================================================
</script>
<style scoped>
.login-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}
.login-box {
  width: 400px;
  background: white;
  border-radius: 16px;
  padding: 40px 30px;
  box-shadow: 0 10px 40px rgba(0,0,0,0.2);
}
.avatar-wrapper {
  position: relative;
  width: 100px;
  height: 100px;
  margin:40px 140px;
  
}
.avatar-img {
  width: 100%;
  height: 100%;
  border-radius: 50%;
  object-fit: cover;
  border: 2px solid #f0f0f0;
}
.avatar-edit-badge {
  position: absolute;
  bottom: 0;
  right: 0;
  background: rgba(0, 0, 0, 0.5);
  border-radius: 50%;
  width: 24px;
  height: 24px;
  display: flex;
  align-items: center;
  justify-content: center;
  
}
.title {
  text-align: center;
  margin-bottom: 24px;
  color: #333;
}
.switch-link {
  color: #667eea;
  cursor: pointer;
  font-size: 14px;
}
.switch-link:hover {
  text-decoration: underline;
}
</style>
