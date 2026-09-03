<template>
  <div class="userlist">
    <div class="toolbar">
      <div class="title">
        <van-icon name="friends-o" size="18" color="#185a9d" />
        <span>
          {{ roleFilter === 1 ? '管理员' : roleFilter === 0 ? '普通用户' : '全部用户' }}
          ({{ filteredUsers.length }})
          <span class="total-hint"> / 共 {{ users.length }} 人</span>
        </span>
      </div>
      <div class="right-tools">
        <div class="filter-btns">
          <van-button v-for="o in opts" :key="o.v" size="small"
            :type="String(roleFilter) === String(o.v) ? 'primary' : 'default'"
            @click="roleFilter = o.v">
            {{ o.l }}
          </van-button>
        </div>
        <van-button size="small" type="primary" plain @click="loadUsers">
          <template #icon><van-icon name="replay" /></template>
          刷新
        </van-button>
      </div>
    </div>

    <van-loading v-if="loading" class="loader" />

    <table v-else class="tbl">
      <thead>
        <tr>
          <th>ID</th><th>头像</th><th>用户名</th>
          <th>邮箱 / 手机</th><th>角色</th><th>状态</th>
          <th>注册时间</th><th>操作</th>
        </tr>
      </thead>
      <tbody>
        <tr v-if="filteredUsers.length === 0">
          <td colspan="8" class="empty">暂无{{ roleFilter === 1 ? '管理员' : roleFilter === 0 ? '普通用户' : '用户' }}</td>
        </tr>
        <tr v-for="u in filteredUsers" :key="u.id">
          <td class="muted">#{{ u.id }}</td>
          <td><img class="avatar" :src="u.avatar || defaultAvatar" /></td>
          <td class="bold">{{ u.username }}</td>
          <td>
            <div>{{ u.email || '—' }}</div>
            <div class="muted small">{{ u.phone || '—' }}</div>
          </td>
          <td>
            <van-tag v-if="Number(u.role) === 1" type="primary">管理员</van-tag>
            <van-tag v-else>普通用户</van-tag>
          </td>
          <td>
            <van-tag v-if="u.status === 0 || u.status === '0'" type="danger">已禁用</van-tag>
            <van-tag v-else type="success">正常</van-tag>
          </td>
          <td class="muted small">{{ fmt(u.createTime) }}</td>
          <td>
            <div class="acts">
              <van-button size="mini" plain type="primary"
                @click="setRole(u, Number(u.role) === 1 ? 0 : 1)">
                {{ Number(u.role) === 1 ? '降为普通' : '设为管理员' }}
              </van-button>
              <van-button size="mini"
                :type="(u.status === 0 || u.status === '0') ? 'success' : 'warning'"
                @click="toggleStatus(u)">
                {{ (u.status === 0 || u.status === '0') ? '启用' : '禁用' }}
              </van-button>
            </div>
          </td>
        </tr>
      </tbody>
    </table>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { showToast, showConfirmDialog } from 'vant'
import { adminListUsers, adminToggleUserStatus, adminSetUserRole } from '../../api/index'

const users = ref([])
const loading = ref(false)
const defaultAvatar = 'https://api.dicebear.com/7.x/avataaars/svg?seed=default'

const roleFilter = ref(-1)
const opts = [
  { v: -1, l: '全部' },
  { v: 0,  l: '普通用户' },
  { v: 1,  l: '管理员' }
]
const filteredUsers = computed(() =>
  roleFilter.value === -1
    ? users.value
    : users.value.filter(u => Number(u.role) === roleFilter.value)
)

const fmt = (t) => {
  if (!t) return '—'
  const d = new Date(t)
  if (Number.isNaN(d.getTime())) return String(t)
  return d.toLocaleString('zh-CN', { hour12: false })
}

async function loadUsers() {
  loading.value = true
  try {
    const res = await adminListUsers()
    if (res.code === 200) users.value = res.data || []
  } finally { loading.value = false }
}

async function toggleStatus(u) {
  const next = (u.status === 1 || u.status == null) ? '禁用' : '启用'
  try { await showConfirmDialog({ title: `${next}用户`, message: `确定要${next}用户 "${u.username}" 吗？` }) } catch { return }
  const res = await adminToggleUserStatus(u.id)
  if (res.code === 200) { showToast(`${next}成功`); loadUsers() }
  else showToast(res.message || '操作失败')
}

async function setRole(u, role) {
  const label = role === 1 ? '设为管理员' : '降为普通用户'
  try { await showConfirmDialog({ title: label, message: `确定将 "${u.username}" ${label}？` }) } catch { return }
  const res = await adminSetUserRole(u.id, role)
  if (res.code === 200) { showToast(label + '成功'); loadUsers() }
  else showToast(res.message || '操作失败')
}

onMounted(loadUsers)
</script>

<style scoped>
.userlist { padding: 4px 2px; }
.toolbar {
  display: flex; align-items: center; justify-content: space-between;
  padding: 4px 6px 14px; flex-wrap: wrap; gap: 10px;
}
.title { font-weight: 600; display: flex; gap: 8px; align-items: center; }
.title .total-hint { color: #9aa4b2; font-weight: 400; font-size: 12px; margin-left: 4px; }
.right-tools { display: flex; align-items: center; gap: 10px; flex-wrap: wrap; }
.filter-btns { display: flex; gap: 6px; flex-wrap: wrap; }
.tbl {
  width: 100%; border-collapse: collapse; background: #fff;
  border-radius: 12px; overflow: hidden; font-size: 13.5px;
  box-shadow: 0 2px 10px rgba(0,0,0,0.03);
}
.tbl th, .tbl td { padding: 10px 12px; text-align: left; border-bottom: 1px solid #eef2f7; vertical-align: middle; }
.tbl th { background: #eef4fc; color: #185a9d; font-weight: 600; font-size: 13px; }
.tbl tbody tr:hover { background: #f7fafc; }
.avatar { width: 36px; height: 36px; border-radius: 50%; object-fit: cover; border: 2px solid #e8f0fe; background: #e8f0fe; }
.bold { font-weight: 600; color: #1a1a2e; }
.muted { color: #9aa4b2; }
.small { font-size: 12px; }
.acts { display: flex; gap: 6px; flex-wrap: wrap; }
.empty { text-align: center; color: #999; padding: 30px 0; }
.loader { display: block; text-align: center; padding: 60px 0; }
</style>
