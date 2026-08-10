<template>
  <div class="page-container page">
    <div class="page-header">
      <van-nav-bar 
        title="个人中心"
        left-arrow
        left-text="返回" 
        fixed
        @click-left="onBack"
        right-action
        right-text="退出登录" 
        @click-right="onLogout"
        :border="false"/>
    </div>
    <div class="page-content">
      <div class="user-card">
        <div class="avatar">
          <!-- 如果有头像 URL 就显示图片，否则显示默认图标 -->
          <img :src="userInfo?.avatar || defaultAvatar" class="avatar" />
        </div>
        <div class="user-info">
          <h2 class="username">游客</h2>
          <p class="user-desc">欢迎使用智能旅游助手</p>
        </div>
      </div>

      <div class="menu-card">
        <van-cell-group inset>
          <van-cell icon="orders-o" title="我的订单" is-link />
          <van-cell icon="like-o" title="我的收藏" is-link />
          <van-cell icon="location-o" title="常用地址" is-link />
          <van-cell icon="settings-o" title="设置" is-link />
        </van-cell-group>
      </div>

      <div class="menu-card">
        <van-cell-group inset>
          <van-cell icon="info-o" title="关于我们" is-link />
          <van-cell icon="service-o" title="联系客服" is-link />
        </van-cell-group>
      </div>

      <div class="version-info">
        <p>版本 1.0.0</p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { showToast } from 'vant'
import { useRouter } from 'vue-router'
import { getUserInfo } from '../api/index'
//  1. 路由 & DOM 引用
// ============================================================
const defaultAvatar = 'https://api.dicebear.com/7.x/avataaars/svg?seed=default'
const userInfo = ref({
  userId: '',
  username: '',
  avatar: '',
  email: '',
  phone: ''
})
const router=useRouter()
//  2. 响应式数据
// ============================================================
//  3. 工具函数
// ============================================================
/** 返回上一页 */
const onBack = () => {
  router.back()
}
/** 退出登录 */
const onLogout = () => {
  // 清除本地存储中的用户信息
  localStorage.removeItem('token')
  localStorage.removeItem('userInfo')
  // 跳转到登录页
  router.push('/login')
}
// ====== 从后端获取用户信息 ======
// ✅ 从后端取数据
const loadUserInfo = async () => {
  console.log('开始获取用户信息...')
  try {
    console.log('开始获取用户信息...')
    const res = await getUserInfo()
    console.log('获取用户信息响应:', res)
    if (res.code === 200) {
      userInfo.value = res.data
      // 更新 localStorage
      localStorage.setItem('userInfo', JSON.stringify(res.data))
      console.log('✅ 获取用户信息成功:', res.data)
    } else {
      showToast(res.message || '获取信息失败')
    }
  } catch (error) {
    console.error('获取用户信息失败:', error)
    showToast('网络错误')
  }
}

// 页面加载时取数据
onMounted(() => {
  // 先从 localStorage 读
  const stored = localStorage.getItem('userInfo')
  if (stored) {
    userInfo.value = JSON.parse(stored)
  }
  // 再从后端取最新数据
  loadUserInfo()
})
//  4. 核心业务逻辑
// ============================================================
//  5. 生命周期 & 监听
// ============================================================
</script>

<style scoped>
.page-container {
  min-height: 100vh;
  background: linear-gradient(180deg, #E8F4FD 0%, #F5F7FA 50%, #FFFFFF 100%);
  display: flex;
  padding: 16px;
  flex-direction: column;
}
.page-content {
  padding: 50px;
}
.user-card {
  display: flex;
  align-items: center;
  background-color: #fff;
  border-radius: 8px;
  padding: 20px;
  margin-bottom: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}
.avatar {
  width: 72px;
  height: 72px;
  background-color: #f7f8fa;
  border-radius: 50%;
  display: flex;
  justify-content: center;
  align-items: center;
  margin-right: 16px;
}
.user-info {
  flex: 1;
}
.username {
  font-size: 18px;
  font-weight: 600;
  color: #323233;
  margin: 0 0 6px 0;
}
.user-desc {
  font-size: 13px;
  color: #969799;
  margin: 0;
}
.menu-card {
  background-color: #fff;
  border-radius: 8px;
  margin-bottom: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
  overflow: hidden;
}
.version-info {
  text-align: center;
  padding: 20px;
}
.version-info p {
  font-size: 12px;
  color: #969799;
  margin: 0;
}
</style>