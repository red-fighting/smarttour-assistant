<template>
  <div class="admin-layout">
    <!-- ===== 左侧边栏 ===== -->
    <aside class="sidebar">
      <div class="brand-block">
        <div class="brand-logo">
          <van-icon name="shield-o" size="24" />
        </div>
        <div class="brand-text">
          <div class="brand-title">智游助手</div>
          <div class="brand-sub">管理后台</div>
        </div>
      </div>

      <nav class="nav-list">
        <router-link
          v-for="item in navItems"
          :key="item.path"
          :to="item.path"
          class="nav-item"
          active-class="nav-item--active"
        >
          <van-icon :name="item.icon" size="20" class="nav-icon" />
          <span class="nav-label">{{ item.label }}</span>
          <van-icon name="arrow" size="12" class="nav-arrow" />
        </router-link>
      </nav>

      <div class="sidebar-footer">
        <div class="version">v1.0.0</div>
      </div>
    </aside>

    <!-- ===== 右侧主体 ===== -->
    <div class="main-area">
      <!-- 顶部栏 -->
      <header class="topbar">
        <div class="breadcrumb">
          <van-icon name="apps-o" size="18" />
          <span>管理后台 / {{ currentTitle }}</span>
        </div>
        <div class="right-actions">
          <div class="user-info">
            <img :src="userInfo.avatar || defaultAvatar" class="user-avatar" />
            <div class="user-meta">
              <span class="user-name">{{ userInfo.username || '管理员' }}</span>
              <span class="user-role">管理员</span>
            </div>
          </div>
          <van-button size="small" type="primary" plain @click="logout">
            <template #icon><van-icon name="log-out" /></template>
            退出
          </van-button>
        </div>
      </header>

      <!-- 子路由内容 -->
      <main class="content">
        <router-view v-slot="{ Component }">
          <transition name="fade" mode="out-in">
            <component :is="Component" />
          </transition>
        </router-view>
      </main>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { showConfirmDialog } from 'vant'
import request from '../utils/request'

const router = useRouter()
const route = useRoute()
const defaultAvatar = 'https://api.dicebear.com/7.x/avataaars/svg?seed=admin'

const navItems = [
  { path: '/admin/dashboard',     label: '仪表盘',     icon: 'chart-trending-o' },
  { path: '/admin/visualization', label: '数据可视化', icon: 'bar-chart-2-o' },
  { path: '/admin/users',         label: '用户管理',   icon: 'friends-o' },
  { path: '/admin/products',      label: '商品管理',   icon: 'shop-o' },
  { path: '/admin/orders',         label: '订单管理',   icon: 'orders-o' },
{ path: '/admin/banners',        label: '轮播图管理', icon: 'photo-o' }
]

const currentTitle = computed(() => route.meta?.title || '管理后台')

const userInfo = computed(() => {
  try { return JSON.parse(localStorage.getItem('userInfo') || '{}') }
  catch (e) { return {} }
})

const logout = async () => {
  try {
    await showConfirmDialog({ title: '退出登录', message: '确定要退出管理后台吗？' })
  } catch { return }
  localStorage.removeItem('token')
  localStorage.removeItem('userInfo')
  try { delete request.defaults.headers.common['Authorization'] } catch (e) {}
  router.push('/login')
}
</script>

<style scoped>
.admin-layout {
  display: flex;
  min-height: 100vh;
  background: #f5f7fa;
  font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", "PingFang SC",
               "Hiragino Sans GB", "Microsoft YaHei", sans-serif;
}

/* ===== 侧边栏 ===== */
.sidebar {
  width: 220px;
  flex-shrink: 0;
  background: linear-gradient(180deg, #1a1a2e 0%, #16213e 100%);
  color: #fff;
  display: flex;
  flex-direction: column;
  position: sticky;
  top: 0;
  height: 100vh;
  overflow-y: auto;
}
.brand-block {
  padding: 22px 18px 20px;
  display: flex;
  align-items: center;
  gap: 12px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.08);
}
.brand-logo {
  width: 40px;
  height: 40px;
  border-radius: 10px;
  background: linear-gradient(135deg, #43cea2, #185a9d);
  display: flex;
  align-items: center;
  justify-content: center;
}
.brand-title { font-weight: 700; font-size: 16px; }
.brand-sub { font-size: 11px; opacity: 0.6; letter-spacing: 1px; }

.nav-list {
  padding: 12px 10px;
  flex: 1;
}
.nav-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 11px 14px;
  border-radius: 10px;
  color: rgba(255, 255, 255, 0.72);
  font-size: 14px;
  margin-bottom: 4px;
  transition: all 0.18s ease;
  text-decoration: none;
}
.nav-item:hover {
  background: rgba(255, 255, 255, 0.06);
  color: #fff;
}
.nav-item--active {
  background: linear-gradient(135deg, rgba(67, 206, 162, 0.28), rgba(24, 90, 157, 0.28));
  color: #fff;
  box-shadow: 0 2px 10px rgba(24, 90, 157, 0.25);
}
.nav-item--active .nav-icon { color: #43cea2; }
.nav-icon { color: rgba(255, 255, 255, 0.6); flex-shrink: 0; }
.nav-label { flex: 1; }
.nav-arrow { opacity: 0.4; }

.sidebar-footer {
  padding: 16px;
  border-top: 1px solid rgba(255, 255, 255, 0.08);
  text-align: center;
}
.version { font-size: 11px; opacity: 0.4; }

/* ===== 右侧主区 ===== */
.main-area {
  flex: 1;
  display: flex;
  flex-direction: column;
  min-width: 0;
}
.topbar {
  height: 58px;
  background: #fff;
  border-bottom: 1px solid #eef2f7;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 22px;
  position: sticky;
  top: 0;
  z-index: 10;
  box-shadow: 0 1px 4px rgba(0,0,0,0.03);
}
.breadcrumb {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
  color: #555;
  font-weight: 500;
}
.right-actions {
  display: flex;
  align-items: center;
  gap: 16px;
}
.user-info {
  display: flex;
  align-items: center;
  gap: 10px;
}
.user-avatar {
  width: 34px; height: 34px;
  border-radius: 50%;
  object-fit: cover;
  border: 2px solid #eef4fc;
  background: #e8f0fe;
}
.user-meta { display: flex; flex-direction: column; line-height: 1.2; }
.user-name { font-size: 13px; font-weight: 600; color: #1a1a2e; }
.user-role { font-size: 11px; color: #185a9d; }

.content {
  flex: 1;
  padding: 20px 24px 40px;
  max-width: 1400px;
  width: 100%;
}

/* ===== 切换动画 ===== */
.fade-enter-active, .fade-leave-active {
  transition: opacity 0.22s ease, transform 0.22s ease;
}
.fade-enter-from { opacity: 0; transform: translateY(6px); }
.fade-leave-to   { opacity: 0; transform: translateY(-6px); }

/* ===== 响应式：窄屏隐藏侧边栏 ===== */
@media (max-width: 768px) {
  .sidebar { width: 160px; }
  .brand-title { font-size: 14px; }
  .nav-item { padding: 10px 12px; font-size: 13px; }
  .content { padding: 14px; }
}
</style>
