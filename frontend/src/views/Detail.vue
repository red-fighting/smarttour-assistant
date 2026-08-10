<template>
  <div class="page-container">
    <van-nav-bar 
    :title="tripData.city + '行程规划'" 
    left-arrow 
    left-text="返回" 
    fixed
    @click-left="onBack"
    />
    <div class="page-content">
      <div class="overview-card">
        <div class="overview-header">
          <h2>{{ tripData.city }} · {{ tripData.days }}天行程</h2>
          <span class="budget-tag">预算：{{ tripData.totalBudget }}元</span>
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
            <div v-if="day.morning && day.morning.length" class="schedule-section">
              <div class="section-label">上午</div>
              <div class="schedule-list">
                <div v-for="(item, idx) in day.morning" :key="idx" class="schedule-item">
                  <span class="time">{{ item.time }}</span>
                  <span class="activity">{{ item.activity }}</span>
                </div>
              </div>
            </div>
            <div v-if="day.afternoon && day.afternoon.length" class="schedule-section">
              <div class="section-label">下午</div>
              <div class="schedule-list">
                <div v-for="(item, idx) in day.afternoon" :key="idx" class="schedule-item">
                  <span class="time">{{ item.time }}</span>
                  <span class="activity">{{ item.activity }}</span>
                </div>
              </div>
            </div>
            <div v-if="day.evening && day.evening.length" class="schedule-section">
              <div class="section-label">晚上</div>
              <div class="schedule-list">
                <div v-for="(item, idx) in day.evening" :key="idx" class="schedule-item">
                  <span class="time">{{ item.time }}</span>
                  <span class="activity">{{ item.activity }}</span>
                </div>
              </div>
            </div>
          </div>
        </van-collapse-item>
      </van-collapse>

      <div class="card budget-card">
        <div class="section-title">预算明细</div>
        <div class="budget-list">
          <div v-for="(item, index) in tripData.budgetBreakdown" :key="index" class="budget-item">
            <span class="budget-label">{{ item.category }}</span>
            <span class="budget-value">¥{{ item.amount }}</span>
          </div>
          <div class="budget-total">
            <span class="budget-label">总计</span>
            <span class="budget-value">¥{{ tripData.totalBudget }}</span>
          </div>
        </div>
      </div>

      <div class="card tips-card">
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
    </div>

    <div class="bottom-bar">
      <van-button @click="goToChat" type="primary" size="large" round>咨询 AI 助手</van-button>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'

const router = useRouter()
const route = useRoute()
const recommendData = ref(null)
const activeDays = ref(['1'])
const onBack = () => {
  router.back()
}




const tripData = reactive({
  city: '',
  days: 0,
  totalBudget: 0,
  dailyItinerary: [],
  budgetBreakdown: [],
  tips: [],
  warnings: []
})

const goToChat = () => {
  router.push({ name: 'Chat' })
}

onMounted(() => {
  // 从 router 的 state 中读取数据
  const data = history.state?.recommendData
  if (data) {
    console.log('✅ 接收到推荐数据:', data)
    tripData.city = data.city
    tripData.days = data.days
    tripData.totalBudget = data.totalBudget
    tripData.dailyItinerary = data.dailyItinerary
    tripData.budgetBreakdown = data.budgetBreakdown
    tripData.tips = data.tips
    tripData.warnings = data.warnings
    recommendData.value = data
    console.log('接收到推荐数据:', data)
  } else {
    console.warn('没有推荐数据')
  }
})
</script>

<style scoped>
.page-container {
  min-height: 100vh;
  background-color: #f5f5f5;
  padding-bottom: 80px;
}
.page-content {
  padding: 16px;
}
.card {
  background-color: #fff;
  border-radius: 8px;
  padding: 16px;
  margin-bottom: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}
.section-title {
  font-size: 16px;
  font-weight: 600;
  color: #323233;
  margin-bottom: 12px;
}
.overview-card {
  background-color: #fff;
  border-radius: 8px;
  padding: 20px;
  margin-bottom: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}
.overview-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.overview-header h2 {
  font-size: 20px;
  font-weight: 600;
  color: #323233;
  margin: 0;
}
.budget-tag {
  font-size: 14px;
  font-weight: 600;
  color: #ee0a24;
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
  font-weight: 500;
  color: #1989fa;
  background-color: #e8f3ff;
  padding: 4px 10px;
  border-radius: 4px;
  display: inline-block;
  margin-bottom: 8px;
}
.schedule-list {
  padding-left: 16px;
}
.schedule-item {
  display: flex;
  align-items: center;
  padding: 8px 0;
  border-bottom: 1px solid #f7f8fa;
}
.schedule-item:last-child {
  border-bottom: none;
}
.schedule-item .time {
  font-size: 13px;
  color: #969799;
  width: 60px;
  flex-shrink: 0;
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
  border-bottom: 1px solid #f7f8fa;
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
}
.budget-total {
  display: flex;
  justify-content: space-between;
  padding: 12px 0 8px;
  margin-top: 8px;
  border-top: 1px dashed #eee;
}
.budget-total .budget-label {
  font-size: 16px;
  font-weight: 600;
  color: #323233;
}
.budget-total .budget-value {
  font-size: 18px;
  font-weight: 600;
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
  content: '·';
  position: absolute;
  left: 0;
  color: #969799;
}
.bottom-bar {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  padding: 12px 16px;
  background-color: #fff;
  box-shadow: 0 -2px 10px rgba(0, 0, 0, 0.05);
}
</style>