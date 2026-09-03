<template>
  <div class="visualization">
    <!-- 顶部概览指标（和仪表盘一致但更紧凑） -->
    <div class="metric-row">
      <div v-for="(m, idx) in metrics" :key="idx" class="metric-card" :style="{ background: m.bg }">
        <div class="metric-label">{{ m.label }}</div>
        <div class="metric-value">{{ m.value }}</div>
      </div>
    </div>

    <!-- 两列图表区 -->
    <div class="chart-grid">
      <!-- 左：用户角色分布 + 订单状态分布 -->
      <div class="col">
        <section class="chart-panel">
          <div class="chart-title">用户角色分布</div>
          <div ref="rolePieRef" class="chart-box"></div>
        </section>
        <section class="chart-panel">
          <div class="chart-title">订单状态分布</div>
          <div ref="orderPieRef" class="chart-box"></div>
        </section>
      </div>

      <!-- 右：商品销量排行 + 商品销售金额 -->
      <div class="col">
        <section class="chart-panel">
          <div class="chart-title">商品销量排行 Top6</div>
          <div ref="salesBarRef" class="chart-box"></div>
        </section>
        <section class="chart-panel">
          <div class="chart-title">商品销售金额（¥）</div>
          <div ref="amountBarRef" class="chart-box"></div>
        </section>
      </div>
    </div>

    <!-- 全宽：订单总览统计 -->
    <section class="chart-panel full">
      <div class="chart-title">订单金额汇总</div>
      <div class="summary-line">
        <div class="sum-item">
          <span class="sum-label">已支付总金额</span>
          <span class="sum-value paid">¥ {{ fmt(summary.paidAmount) }}</span>
        </div>
        <div class="sum-item">
          <span class="sum-label">购物车待支付</span>
          <span class="sum-value cart">¥ {{ fmt(summary.cartAmount) }}</span>
        </div>
        <div class="sum-item">
          <span class="sum-label">已支付订单数</span>
          <span class="sum-value">{{ summary.paidCount || 0 }}</span>
        </div>
        <div class="sum-item">
          <span class="sum-label">待支付订单数</span>
          <span class="sum-value">{{ summary.cartCount || 0 }}</span>
        </div>
        <div class="sum-item">
          <span class="sum-label">订单总数</span>
          <span class="sum-value">{{ (summary.paidCount || 0) + (summary.cartCount || 0) }}</span>
        </div>
      </div>
    </section>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, onBeforeUnmount, nextTick } from 'vue'
import * as echarts from 'echarts'
import { adminDashboard, adminListUsers, adminListProducts } from '../../api/index'

const fmt = (n) => {
  if (n == null || Number.isNaN(Number(n))) return '0.00'
  return Number(n).toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 })
}

const summary = reactive({})
const users = ref([])
const products = ref([])

const metrics = computed(() => [
  { label: '总用户',   value: summary.userCount || 0,     bg: 'linear-gradient(135deg,#43cea2,#36b2f4)' },
  { label: '在售商品', value: summary.productCount || 0,  bg: 'linear-gradient(135deg,#f7971e,#ffd200)' },
  { label: '已支付',   value: summary.paidCount || 0,     bg: 'linear-gradient(135deg,#00c6ff,#0072ff)' },
  { label: '已付金额', value: '¥ ' + fmt(summary.paidAmount), bg: 'linear-gradient(135deg,#8e2de2,#4a00e0)' }
])

// 图表 ref
const rolePieRef  = ref(null)
const orderPieRef = ref(null)
const salesBarRef = ref(null)
const amountBarRef= ref(null)
let charts = []

function disposeAll() {
  charts.forEach(c => { try { c.dispose() } catch {} })
  charts = []
}
function makeChart(el) {
  const c = echarts.init(el)
  charts.push(c)
  return c
}

// 空数据提示
function showEmpty(chart, tip = '暂无数据') {
  chart.clear()
  chart.setOption({
    title: { text: tip, left: 'center', top: 'center', textStyle: { color: '#bbb', fontSize: 14, fontWeight: 'normal' } }
  })
}

function initRolePie() {
  const chart = makeChart(rolePieRef.value)
  const adminCount  = users.value.filter(u => Number(u.role) === 1).length
  const normalCount = users.value.length - adminCount
  if (users.value.length === 0) { showEmpty(chart); return }
  chart.setOption({
    tooltip: { trigger: 'item', formatter: '{b}: {c} ({d}%)' },
    legend: { bottom: 0, itemWidth: 12, itemHeight: 12, textStyle: { fontSize: 12 } },
    color: ['#43cea2', '#185a9d'],
    series: [{
      type: 'pie',
      radius: ['42%', '68%'],
      center: ['50%', '45%'],
      label: { formatter: '{b}\n{c}人' },
      data: [
        { value: normalCount, name: '普通用户' },
        { value: adminCount,  name: '管理员' }
      ]
    }]
  })
}

function initOrderPie() {
  const chart = makeChart(orderPieRef.value)
  // 直接用 summary 的计数字段（后端 dashboard 接口一定返回）
  const paidCnt = Number(summary.paidCount || 0)
  const cartCnt = Number(summary.cartCount || 0)
  if (paidCnt + cartCnt === 0) { showEmpty(chart); return }
  chart.setOption({
    tooltip: { trigger: 'item', formatter: '{b}: {c}单 ({d}%)' },
    legend: { bottom: 0, itemWidth: 12, itemHeight: 12, textStyle: { fontSize: 12 } },
    color: ['#f7971e', '#43cea2'],
    series: [{
      type: 'pie',
      radius: ['42%', '68%'],
      center: ['50%', '45%'],
      label: { formatter: '{b}\n{c}单' },
      data: [
        { value: cartCnt, name: '购物车未支付' },
        { value: paidCnt, name: '已支付' }
      ]
    }]
  })
}

function initSalesBar() {
  const chart = makeChart(salesBarRef.value)
  const list = [...products.value].sort((a,b) => (b.soldCount || 0) - (a.soldCount || 0)).slice(0, 6)
  if (list.length === 0) { showEmpty(chart); return }
  chart.setOption({
    tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
    grid: { top: 18, right: 16, bottom: 50, left: 16, containLabel: true },
    xAxis: {
      type: 'category',
      data: list.map(p => p.title?.slice(0, 8) + (p.title?.length > 8 ? '…' : '')),
      axisLabel: { interval: 0, rotate: 22, fontSize: 11 }
    },
    yAxis: { type: 'value', name: '销量' },
    color: '#43cea2',
    series: [{
      type: 'bar',
      barWidth: '48%',
      itemStyle: { borderRadius: [6, 6, 0, 0] },
      data: list.map(p => p.soldCount || 0)
    }]
  })
}

function initAmountBar() {
  const chart = makeChart(amountBarRef.value)
  const list = [...products.value].sort((a,b) => (b.price || 0) - (a.price || 0)).slice(0, 6)
  if (list.length === 0) { showEmpty(chart); return }
  chart.setOption({
    tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' }, valueFormatter: v => '¥ ' + Number(v).toFixed(2) },
    grid: { top: 18, right: 16, bottom: 50, left: 16, containLabel: true },
    xAxis: {
      type: 'category',
      data: list.map(p => p.title?.slice(0, 8) + (p.title?.length > 8 ? '…' : '')),
      axisLabel: { interval: 0, rotate: 22, fontSize: 11 }
    },
    yAxis: { type: 'value', name: '单价(¥)' },
    color: '#185a9d',
    series: [{
      type: 'bar',
      barWidth: '48%',
      itemStyle: { borderRadius: [6, 6, 0, 0] },
      data: list.map(p => Number(p.price || 0))
    }]
  })
}

function redrawAll() {
  nextTick(() => {
    disposeAll()
    if (rolePieRef.value)  initRolePie()
    if (orderPieRef.value) initOrderPie()
    if (salesBarRef.value) initSalesBar()
    if (amountBarRef.value) initAmountBar()
  })
}

async function loadAll() {
  try {
    const [dashRes, uRes, pRes] = await Promise.all([
      adminDashboard(), adminListUsers(), adminListProducts()
    ])
    if (dashRes.code === 200) Object.assign(summary, dashRes.data || {})
    if (uRes.code === 200) users.value = uRes.data || []
    if (pRes.code === 200) products.value = pRes.data || []
  } catch (e) { /* 403 会被守卫踢 */ }
  redrawAll()
}

let resizeHandler
onMounted(() => {
  loadAll()
  resizeHandler = () => charts.forEach(c => c.resize())
  window.addEventListener('resize', resizeHandler)
})
onBeforeUnmount(() => {
  window.removeEventListener('resize', resizeHandler)
  disposeAll()
})
</script>

<style scoped>
.visualization { padding: 4px 2px; }

/* 顶部指标 */
.metric-row {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 14px;
  margin-bottom: 18px;
}
.metric-card {
  border-radius: 12px;
  padding: 14px 18px;
  color: #fff;
  box-shadow: 0 4px 14px rgba(0,0,0,0.08);
}
.metric-label { font-size: 12px; opacity: 0.85; }
.metric-value { font-size: 22px; font-weight: 700; margin-top: 4px; }

/* 图表网格 */
.chart-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
  margin-bottom: 16px;
}
.col { display: flex; flex-direction: column; gap: 16px; }

.chart-panel {
  background: #fff;
  border-radius: 12px;
  padding: 14px 16px 10px;
  box-shadow: 0 2px 10px rgba(0,0,0,0.03);
}
.chart-panel.full { grid-column: 1 / -1; }
.chart-title {
  font-weight: 600;
  color: #1a1a2e;
  font-size: 14px;
  margin-bottom: 6px;
}
.chart-box {
  width: 100%;
  height: 240px;
}

/* 订单汇总行 */
.summary-line {
  display: grid;
  grid-template-columns: repeat(5, 1fr);
  gap: 14px;
  padding: 14px 4px 4px;
}
.sum-item {
  display: flex; flex-direction: column; gap: 4px;
  padding: 12px 14px;
  background: #f7f9fc;
  border-radius: 10px;
}
.sum-label { font-size: 12px; color: #888; }
.sum-value { font-size: 18px; font-weight: 700; color: #1a1a2e; }
.sum-value.paid { color: #43cea2; }
.sum-value.cart { color: #f7971e; }

@media (max-width: 900px) {
  .chart-grid { grid-template-columns: 1fr; }
  .metric-row { grid-template-columns: repeat(2, 1fr); }
  .summary-line { grid-template-columns: repeat(2, 1fr); }
}
</style>
