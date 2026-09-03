import { createRouter, createWebHistory } from 'vue-router'
import Home from '../views/Home.vue'
import Chat from '../views/Chat.vue'
import Profile from '../views/Profile.vue'
import Detail from '../views/Detail.vue'
import Login from '../views/Login.vue'
import Community from '../views/Community.vue'
import Admin from '../views/Admin.vue'
import Dashboard from '../views/admin/Dashboard.vue'
import UserList from '../views/admin/UserList.vue'
import ProductList from '../views/admin/ProductList.vue'
import OrderList from '../views/admin/OrderList.vue'
import BannerList from '../views/admin/BannerList.vue'
import Visualization from '../views/admin/Visualization.vue'

function getRoleFromStorage() {
  try {
    const info = JSON.parse(localStorage.getItem('userInfo') || '{}')
    return Number(info.role ?? 0)
  } catch (e) {
    return 0
  }
}

// =============== 路由表 ===============
const routes = [
  { path: '/login', name: 'Login', component: Login, meta: { public: true } },

  // —— 用户端（仅 role=0 可访问；role=1 访问会被守卫踢回 /admin）——
  { path: '/', name: 'Home', component: Home, meta: { userOnly: true, requiresAuth: true } },
  { path: '/chat', name: 'Chat', component: Chat, meta: { userOnly: true } },
  { path: '/community', name: 'Community', component: Community, meta: { userOnly: true } },
  { path: '/profile', name: 'Profile', component: Profile, meta: { userOnly: true } },
  { path: '/detail', name: 'Detail', component: Detail, meta: { userOnly: true } },

  // —— 管理员端（role=1 专属独立后台）——
  {
    path: '/admin',
    component: Admin, // 带侧边栏的布局壳
    meta: { adminOnly: true, requiresAuth: true },
    redirect: '/admin/dashboard',
    children: [
      { path: 'dashboard', name: 'AdminDashboard', component: Dashboard, meta: { adminOnly: true, title: '仪表盘' } },
      { path: 'visualization', name: 'AdminVisualization', component: Visualization, meta: { adminOnly: true, title: '数据可视化' } },
      { path: 'users', name: 'AdminUsers', component: UserList, meta: { adminOnly: true, title: '用户管理' } },
      { path: 'products', name: 'AdminProducts', component: ProductList, meta: { adminOnly: true, title: '商品管理' } },
      { path: 'orders', name: 'AdminOrders', component: OrderList, meta: { adminOnly: true, title: '订单管理' } },
      { path: 'banners', name: 'AdminBanners', component: BannerList, meta: { adminOnly: true, title: '轮播图管理' } }
    ]
  },

  // 兜底：不存在的路径按 role 分流
  { path: '/:pathMatch(.*)*', redirect: () => (getRoleFromStorage() === 1 ? '/admin' : '/') }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// =============== 全局前置守卫 ===============
router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('token')
  const role = getRoleFromStorage()

  // 1) 管理员页面（/admin*）：需要登录 + role=1
  if (to.meta.adminOnly) {
    if (!token) return next({ name: 'Login', query: { redirect: to.fullPath } })
    if (role !== 1) return next({ name: 'Login' })
    return next()
  }

  // 2) 用户页面（/ 等）：role=1 绝对不能进入 → 强制跳后台
  if (to.meta.userOnly && role === 1) {
    return next('/admin')
  }

  // 3) 普通的 requiresAuth（仅 / 首页用）
  if (to.meta.requiresAuth && !token) {
    return next({ name: 'Login', query: { redirect: to.fullPath } })
  }

  // 4) 已登录访问 /login：按角色分流
  if (to.name === 'Login' && token) {
    if (role === 1) return next('/admin')
    return next('/')
  }

  next()
})

export default router
