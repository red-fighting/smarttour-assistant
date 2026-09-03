<template>
  <div class="dashboard">
    <!-- 4 指标卡 -->
    <div class="stat-grid">
      <div v-for="(s, idx) in statCards" :key="idx" class="stat-card" :style="{ background: s.bg }">
        <div class="card-inner">
          <div class="card-icon"><van-icon :name="s.icon" size="26" /></div>
          <div class="card-info">
            <div class="card-label">{{ s.label }}</div>
            <div class="card-value">{{ s.unit }}{{ s.value }}</div>
          </div>
        </div>
      </div>
    </div>

    <!-- 订单分段 -->
    <div class="seg-row">
      <div class="seg seg-paid">
        <div class="seg-title">已支付订单</div>
        <div class="seg-main">
          <span class="seg-num">{{ summary.paidCount || 0 }}</span>
          <span class="seg-unit">单</span>
        </div>
        <div class="seg-sub">金额 ¥ {{ fmt(summary.paidAmount) }}</div>
      </div>
      <div class="seg seg-cart">
        <div class="seg-title">待支付（购物车）</div>
        <div class="seg-main">
          <span class="seg-num">{{ summary.cartCount || 0 }}</span>
          <span class="seg-unit">单</span>
        </div>
        <div class="seg-sub">金额 ¥ {{ fmt(summary.cartAmount) }}</div>
      </div>
    </div>

    <!-- 明细 -->
    <section class="panel">
      <div class="panel-title">按状态统计（数据库聚合）</div>
      <table class="tbl">
        <thead><tr><th>状态</th><th>订单数</th><th>合计金额</th></tr></thead>
        <tbody>
          <tr v-if="!stats || stats.length === 0">
            <td colspan="3" class="empty">暂无数据</td>
          </tr>
          <tr v-for="(s, i) in stats" :key="i">
            <td>
              <van-tag :type="Number(s.status) === 1 ? 'success' : 'warning'">
                {{ Number(s.status) === 1 ? '已支付' : '购物车未支付' }}
              </van-tag>
            </td>
            <td>{{ s.cnt || 0 }}</td>
            <td class="amt">¥ {{ fmt(s.amount) }}</td>
          </tr>
        </tbody>
      </table>
    </section>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { adminDashboard } from '../../api/index'

const summary = ref({})
const stats = ref([])

const fmt = (n) => {
  if (n == null || Number.isNaN(Number(n))) return '0.00'
  return Number(n).toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 })
}

const statCards = computed(() => [
  { label: '总用户数',   value: summary.value.userCount || 0,   unit: '', icon: 'friends-o',     bg: 'linear-gradient(135deg, #43cea2, #36b2f4)' },
  { label: '在售商品',   value: summary.value.productCount || 0,unit: '', icon: 'shop-o',         bg: 'linear-gradient(135deg, #f7971e, #ffd200)' },
  { label: '已支付订单', value: summary.value.paidCount || 0,   unit: '', icon: 'balance-o',      bg: 'linear-gradient(135deg, #00c6ff, #0072ff)' },
  { label: '已支付金额', value: fmt(summary.value.paidAmount),  unit: '¥', icon: 'gold-coin-o',  bg: 'linear-gradient(135deg, #8e2de2, #4a00e0)' }
])

onMounted(async () => {
  try {
    const res = await adminDashboard()
    if (res.code === 200) {
      summary.value = res.data || {}
      stats.value = res.data?.statusStats || []
    }
  } catch (e) { /* 403 会被守卫踢 */ }
})
</script>

<style scoped>
.dashboard { padding: 4px; }
.stat-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(220px, 1fr));
  gap: 16px;
  margin-bottom: 20px;
}
.stat-card {
  border-radius: 14px;
  color: #fff;
  box-shadow: 0 6px 20px rgba(0,0,0,0.08);
  min-height: 100px;
  position: relative;
  overflow: hidden;
}
.stat-card::after {
  content: '';
  position: absolute; width: 160px; height: 160px;
  right: -40px; top: -40px;
  background: rgba(255,255,255,0.1);
  border-radius: 50%;
}
.card-inner {
  position: relative; z-index: 2;
  padding: 18px;
  display: flex; align-items: center; gap: 14px;
}
.card-icon {
  width: 48px; height: 48px;
  background: rgba(255,255,255,0.18);
  border-radius: 12px;
  display: flex; align-items: center; justify-content: center;
}
.card-label { font-size: 13px; opacity: 0.9; }
.card-value { font-size: 22px; font-weight: 700; margin-top: 6px; }

.seg-row { display: grid; grid-template-columns: 1fr 1fr; gap: 16px; margin-bottom: 20px; }
.seg {
  border-radius: 14px;
  padding: 22px 24px;
  box-shadow: 0 4px 16px rgba(0,0,0,0.06);
}
.seg-paid { background: linear-gradient(135deg, #00c6ff, #0072ff); color: #fff; }
.seg-cart { background: linear-gradient(135deg, #f7971e, #ffd200); color: #4b2b00; }
.seg-title { font-size: 13px; opacity: 0.85; }
.seg-main { margin-top: 10px; }
.seg-num { font-size: 32px; font-weight: 700; }
.seg-unit { margin-left: 6px; font-size: 14px; }
.seg-sub { margin-top: 6px; font-size: 13px; opacity: 0.9; }

.panel {
  background: #fff;
  border-radius: 14px;
  padding: 18px 20px;
  box-shadow: 0 2px 10px rgba(0,0,0,0.03);
}
.panel-title { font-weight: 600; color: #1a1a2e; margin-bottom: 14px; }
.tbl { width: 100%; border-collapse: collapse; font-size: 14px; }
.tbl th, .tbl td {
  padding: 10px 14px;
  text-align: left;
  border-bottom: 1px solid #eef2f7;
}
.tbl th {
  background: #eef4fc; color: #185a9d;
  font-size: 13px; font-weight: 600;
}
.empty { text-align: center; color: #999; padding: 20px 0; }
.amt { color: #185a9d; font-weight: 600; }
</style>
