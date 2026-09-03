<template>
  <div class="page-container">
    <van-nav-bar
      :title="tripData.city ? tripData.city + '行程规划' : '行程规划'"
      left-arrow
      left-text="返回"
      fixed
      @click-left="onBack"
    />
    <div class="page-content">
      <van-empty v-if="!tripData.dailyItinerary.length" description="暂无行程数据" />

      <template v-else>
        <div class="overview-card">
          <div class="overview-header">
            <h2>{{ tripData.city }} · {{ tripData.days }}天行程</h2>
            <span class="budget-tag">预算：¥{{ computedTotalBudget }}</span>
          </div>
        </div>

        <van-collapse v-model="activeDays">
          <van-collapse-item
            v-for="(day, index) in tripData.dailyItinerary"
            :key="index"
            :title="'第' + day.day + '天'"
            :name="day.day"
          >
            <div class="day-section">
              <div
                v-for="section in scheduleSections"
                :key="section.key"
              >
                <template v-if="day[section.key] && day[section.key].length">
                  <div class="section-label">{{ section.label }}</div>
                  <div class="schedule-list">
                    <div
                      v-for="(item, idx) in day[section.key]"
                      :key="idx"
                      class="schedule-item"
                    >
                      <span class="time">{{ item.time }}</span>
                      <span class="activity">{{ item.activity }}</span>
                    </div>
                  </div>
                </template>
              </div>
            </div>
          </van-collapse-item>
        </van-collapse>

        <div v-if="budgetItems.length" class="card budget-card">
          <div class="section-title">预算明细</div>
          <div class="budget-list">
            <div v-for="item in budgetItems" :key="item.key" class="budget-item">
              <span class="budget-label">{{ item.label }}</span>
              <span class="budget-value">¥{{ item.value }}</span>
            </div>
            <div class="budget-total">
              <span class="budget-label">总计</span>
              <span class="budget-value">¥{{ computedTotalBudget }}</span>
            </div>
          </div>
        </div>

        <div v-if="tripData.tips && tripData.tips.length" class="card tips-card">
          <div class="section-title">温馨提示</div>
          <ul class="tips-list">
            <li v-for="(tip, index) in tripData.tips" :key="index">{{ tip }}</li>
          </ul>
        </div>

        <div v-if="tripData.warnings && tripData.warnings.length" class="card warning-card">
          <div class="section-title">注意事项</div>
          <ul class="tips-list">
            <li v-for="(warning, index) in tripData.warnings" :key="index">{{ warning }}</li>
          </ul>
        </div>
      </template>
    </div>

    <div class="bottom-bar">
      <van-button @click="goToChat" type="primary" size="large" round>咨询 AI 助手</van-button>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()
const activeDays = ref([])
const onBack = () => {
  router.back()
}

const tripData = reactive({
  city: '',
  days: 0,
  totalBudget: 0,
  dailyItinerary: [],
  budgetBreakdown: {},
  tips: [],
  warnings: []
})

const scheduleSections = [
  { key: 'morning', label: '上午' },
  { key: 'afternoon', label: '下午' },
  { key: 'evening', label: '晚上' }
]

const budgetLabels = {
  accommmodation: '住宿',
  food: '餐饮',
  transportation: '交通',
  tickets: '门票',
  other: '其他'
}

const budgetItems = computed(() => {
  const bd = tripData.budgetBreakdown
  if (!bd) return []
  return Object.entries(bd)
    .filter(([_, v]) => v != null && v > 0)
    .map(([key, value]) => ({
      key,
      label: budgetLabels[key] || key,
      value: value
    }))
})

const computedTotalBudget = computed(() => {
  if (tripData.totalBudget) return tripData.totalBudget
  const bd = tripData.budgetBreakdown
  if (!bd) return 0
  return Object.values(bd).reduce((sum, v) => sum + (v || 0), 0)
})

const goToChat = () => {
  router.push({ name: 'Chat' })
}

onMounted(() => {
  const data = history.state?.recommendData
  if (data) {
    tripData.city = data.city || ''
    tripData.days = data.days || 0
    tripData.totalBudget = data.totalBudget || 0
    tripData.dailyItinerary = data.dailyItinerary || []
    tripData.budgetBreakdown = data.budgetBreakdown || {}
    tripData.tips = data.tips || []
    tripData.warnings = data.warnings || []
    // 默认展开第一天
    if (tripData.dailyItinerary.length) {
      activeDays.value = [tripData.dailyItinerary[0].day]
    }
  }
})
</script>

<style scoped>
.page-container {
  min-height: 100vh;
  background: linear-gradient(180deg, #f5f7fa 0%, #eef1f6 100%);
  padding-bottom: 80px;
}
.page-content {
  padding: 16px;
}
.card {
  background: #fff;
  border-radius: 16px;
  padding: 20px 16px;
  margin-bottom: 14px;
  box-shadow: 0 4px 20px rgba(67, 206, 162, 0.06);
}
.section-title {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 17px;
  font-weight: 700;
  color: #1a1a2e;
  margin-bottom: 14px;
}
.overview-card {
  background: linear-gradient(135deg, #43cea2 0%, #185a9d 100%);
  border-radius: 16px;
  padding: 24px 20px;
  margin-bottom: 14px;
  box-shadow: 0 6px 24px rgba(67, 206, 162, 0.2);
  color: #fff;
}
.overview-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.overview-header h2 {
  font-size: 20px;
  font-weight: 700;
  color: #fff;
  margin: 0;
  letter-spacing: 0.5px;
}
.budget-tag {
  font-size: 14px;
  font-weight: 600;
  color: #fff;
  background: rgba(255, 255, 255, 0.2);
  padding: 4px 12px;
  border-radius: 20px;
  backdrop-filter: blur(4px);
}
.day-section {
  padding-top: 8px;
}
.schedule-section {
  margin-bottom: 16px;
}
.schedule-section:last-child {
  margin-bottom: 0;
}
.section-label {
  font-size: 12px;
  font-weight: 600;
  color: #43cea2;
  background: rgba(67, 206, 162, 0.1);
  padding: 4px 12px;
  border-radius: 20px;
  display: inline-block;
  margin-bottom: 10px;
}
.schedule-list {
  padding-left: 16px;
}
.schedule-item {
  display: flex;
  align-items: center;
  padding: 10px 0;
  border-bottom: 1px solid #f0f2f5;
}
.schedule-item:last-child {
  border-bottom: none;
}
.schedule-item .time {
  font-size: 13px;
  color: #969799;
  width: 60px;
  flex-shrink: 0;
  font-weight: 500;
}
.schedule-item .activity {
  font-size: 14px;
  color: #323233;
}
.budget-list {
  display: flex;
  flex-direction: column;
}
.budget-item {
  display: flex;
  justify-content: space-between;
  padding: 10px 0;
  border-bottom: 1px solid #f0f2f5;
}
.budget-item:last-of-type {
  border-bottom: none;
}
.budget-label {
  font-size: 14px;
  color: #646566;
}
.budget-value {
  font-size: 14px;
  color: #323233;
  font-weight: 500;
}
.budget-total {
  display: flex;
  justify-content: space-between;
  padding: 14px 0 8px;
  margin-top: 8px;
  border-top: 1px dashed #d8dbe0;
}
.budget-total .budget-label {
  font-size: 16px;
  font-weight: 700;
  color: #1a1a2e;
}
.budget-total .budget-value {
  font-size: 20px;
  font-weight: 700;
  color: #ee0a24;
}
.tips-list {
  margin: 0;
  padding: 0;
  list-style: none;
}
.tips-list li {
  font-size: 13px;
  color: #646566;
  line-height: 1.8;
  padding: 6px 0;
  position: relative;
  padding-left: 20px;
}
.tips-list li::before {
  content: '•';
  position: absolute;
  left: 0;
  color: #43cea2;
  font-size: 16px;
}
.bottom-bar {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  padding: 12px 16px;
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
  box-shadow: 0 -4px 20px rgba(0, 0, 0, 0.06);
}
</style>