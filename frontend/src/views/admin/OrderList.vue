<template>
  <div class="orderlist">
    <div class="toolbar">
      <div class="title">
        <van-icon name="orders-o" size="18" color="#185a9d" />
        <span>订单管理</span>
      </div>
      <div class="filter-btns">
        <van-button v-for="o in opts" :key="o.v" size="small"
          :type="String(statusFilter) === String(o.v) ? 'primary' : 'default'"
          @click="statusFilter = o.v; loadOrders()">
          {{ o.l }}
        </van-button>
      </div>
    </div>

    <van-loading v-if="loading" class="loader" />
    <van-empty v-else-if="orders.length === 0" description="暂无订单" />

    <table v-else class="tbl">
      <thead>
        <tr>
          <th>订单号</th><th>用户</th><th>商品</th><th>图片</th>
          <th>数量</th><th>单价</th><th>小计</th><th>状态</th>
          <th>下单时间</th><th>支付时间</th><th>操作</th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="o in orders" :key="o.id">
          <td class="muted">#{{ o.id }}</td>
          <td class="bold">
            {{ o.user_name || ('用户#' + o.user_id) }}
            <div class="muted small">uid: {{ o.user_id }}</div>
          </td>
          <td class="title-cell">{{ o.title_snapshot }}</td>
          <td><img class="thumb" :src="o.image_snapshot" @error="onImgErr" /></td>
          <td>× {{ o.quantity }}</td>
          <td>¥ {{ Number(o.price ?? 0).toFixed(2) }}</td>
          <td class="amt">¥ {{ (Number(o.price ?? 0) * Number(o.quantity ?? 0)).toFixed(2) }}</td>
          <td>
            <van-tag v-if="Number(o.status) === 1" type="success">已支付</van-tag>
            <van-tag v-else type="warning">购物车</van-tag>
          </td>
          <td class="muted small">{{ fmt(o.create_time) }}</td>
          <td class="muted small">{{ fmt(o.paid_time) }}</td>
          <td>
            <div class="acts">
              <van-button v-if="Number(o.status) !== 1" size="mini" type="success"
                @click="updateStatus(o, 1)">标记已支付</van-button>
              <van-button v-else size="mini" type="warning"
                @click="updateStatus(o, 0)">撤销支付</van-button>
              <van-button size="mini" type="danger" @click="doDelete(o)">删除</van-button>
            </div>
          </td>
        </tr>
      </tbody>
    </table>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { showToast, showConfirmDialog } from 'vant'
import { adminListOrders, adminUpdateOrderStatus, adminDeleteOrder } from '../../api/index'

const orders = ref([])
const loading = ref(false)
const statusFilter = ref(-1)
const opts = [
  { v: -1, l: '全部' },
  { v: 0,  l: '购物车' },
  { v: 1,  l: '已支付' }
]
const defaultImg = 'https://fastly.jsdelivr.net/npm/@vant/assets/cat.jpeg'
const onImgErr = (e) => { try { e.target.src = defaultImg } catch {} }
const fmt = (t) => {
  if (!t) return '—'
  const d = new Date(t)
  if (Number.isNaN(d.getTime())) return String(t)
  return d.toLocaleString('zh-CN', { hour12: false })
}

async function loadOrders() {
  loading.value = true
  try {
    const res = await adminListOrders(statusFilter.value)
    if (res.code === 200) orders.value = res.data || []
  } finally { loading.value = false }
}

async function updateStatus(o, s) {
  const res = await adminUpdateOrderStatus(o.id, s)
  if (res.code === 200) { showToast('状态已更新'); loadOrders() }
  else showToast(res.message || '操作失败')
}

async function doDelete(o) {
  try { await showConfirmDialog({ title: '删除订单', message: `确定删除 #${o.id} 订单？` }) } catch { return }
  const res = await adminDeleteOrder(o.id)
  if (res.code === 200) { showToast('删除成功'); loadOrders() }
  else showToast(res.message || '删除失败')
}

onMounted(loadOrders)
</script>

<style scoped>
.orderlist { padding: 4px 2px; }
.toolbar {
  display: flex; align-items: center; justify-content: space-between;
  padding: 4px 6px 14px; flex-wrap: wrap; gap: 8px;
}
.title { font-weight: 600; display: flex; gap: 8px; align-items: center; }
.filter-btns { display: flex; gap: 6px; flex-wrap: wrap; }
.tbl {
  width: 100%; border-collapse: collapse; background: #fff;
  border-radius: 12px; overflow: hidden; font-size: 13px;
  box-shadow: 0 2px 10px rgba(0,0,0,0.03);
}
.tbl th, .tbl td { padding: 10px 10px; text-align: left; border-bottom: 1px solid #eef2f7; vertical-align: middle; white-space: nowrap; }
.tbl th { background: #eef4fc; color: #185a9d; font-weight: 600; font-size: 12.5px; }
.tbl tbody tr:hover { background: #f7fafc; }
.title-cell { max-width: 240px; white-space: normal; word-break: break-all; }
.thumb { width: 60px; height: 44px; object-fit: cover; border-radius: 8px; background: #e8f0fe; }
.bold { font-weight: 600; }
.muted { color: #9aa4b2; }
.small { font-size: 12px; }
.amt { color: #e04358; font-weight: 700; }
.acts { display: flex; gap: 6px; flex-wrap: wrap; }
.loader { display: block; text-align: center; padding: 60px 0; }
</style>
