<template>
  <div class="page-container page">
    <div class="page-header">
      <van-nav-bar 
      title="智能旅游助手"
      fixed
      :border="false"/>
    </div>
    <div class="page-content">
      <van-notice-bar
        left-icon="volume-o"
        text="基于AI的智能景点介绍与行程规划系统"
      />
    </div>
    <div class="card" style="margin-top: 16px;"  >
      <div class="section-title">规划你的旅程</div>
      <van-field
        label="目的地"
        is-link
        readonly
        @click="showCityPicker = true"
        v-model="formData.city"
        placeholder="请输入用户名"
        style="background-color: #f7f7f7;border-radius: 8px;margin-bottom: 12px;"
      />
      <van-field
        label="预算"
        v-model="formData.budget"
        placeholder="请输入预算"
        style="background-color: #f7f7f7;border-radius: 8px;margin-bottom: 12px;"
      />
      <van-field
        label="天数"
        v-model="formData.days"
        placeholder="请输入天数"
        style="background-color: #f7f7f7;border-radius: 8px;margin-bottom: 12px;"
      />
      <van-button type="primary" size="large" round @click="handleSubmit">开始规划行程</van-button>
    </div>
    <div class="card">
      <div class="section-title">快捷入口</div>
      <div class="quick-entry">
        <div @click="goPage('/chat')" class="entry-item">
          <van-icon name="chat-o" size="36" color="#646566" />
          <span class="entry-text">AI 对话</span>
        </div>
        <div @click="goPage('/profile')" class="entry-item">
          <van-icon name="user-o" size="36" color="#646566" />
          <span class="entry-text">我的</span>
        </div>
      </div>
    </div>
    <div class="card">
      <div class="section-title">热门目的地</div>
      <van-grid :gutter="12" :column-num="4">
        <van-grid-item @click="selectCity(city)" v-for="city in hotCities" :key="city" > 
          <div class="city-tag" :class="{'active': formData.city === city}">{{ city }}</div>
        </van-grid-item>
      </van-grid>
    </div>
    <van-popup
      round
      v-model:show="showCityPicker"
      position="bottom">
      <van-picker
        title="城市"
        :columns="columns"
        @confirm="onConfirm"
        @cancel="onCancel"
        @change="onChange"
      />
    </van-popup>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { showToast } from 'vant'
import { recommendTravel } from '../api/index'
//  1. 路由 & DOM 引用
// ============================================================
const router = useRouter()
const showCityPicker = ref(false)
const formData = reactive({
  city: '',
  budget: '',
  days: ''
})
const allCities = [
  '北京', '上海', '广州', '深圳', '成都', '西安', '重庆', '杭州',
  '南京', '天津', '武汉', '长沙', '重庆', '杭州', '南京', '天津',
  '武汉', '长沙', '重庆', '杭州'
]
const hotCities = ['北京', '上海', '广州', '深圳', '成都', '西安', '重庆', '杭州']
const loading = ref(false)
//  2. 响应式数据
// ============================================================
//  3. 工具函数
// ============================================================
//  4. 核心业务逻辑
// ============================================================
//  5. 生命周期 & 监听
// ============================================================

const columns = allCities.map(city => ({ text: city, value: city }))
const onConfirm = ({ selectedOptions }) => {
  console.log(selectedOptions)
  formData.city = selectedOptions[0].value
  showCityPicker.value = false
}
const onCancel = () => {
  showCityPicker.value = false
}
const onChange = () => {
}
const selectCity = (city) => {
  formData.city = city
  //showCityPicker.value = false
}
const goPage = (name) => {
  router.push(name)
}
//旅游行程规划提交
const handleSubmit = async () => {
  if (!formData.city) {
    showToast('请选择目的地')
    return
  }
  const budget = Number(formData.budget)
  if (isNaN(budget) || budget <= 100) {
    showToast('请输入大于等于100的预算')
    return
  }
  const days = Number(formData.days)
  if (!formData.days || days <= 1 || days > 30) {
    showToast('请输入大于等于1小于等于30的天数')
    return
  }
  try{
    loading.value=true
    const res=await recommendTravel({
      city: formData.city,
      budget: formData.budget,
      days: formData.days
    })
    console.log('推荐返回:', res)
    if(res&&res.data){
      router.push({
        path: '/detail',
        state: { recommendData: res.data }
      })
    }else {
      showToast('获取推荐失败，请重试')
    }
  }catch(err){
    console.warn('获取推荐失败，使用默认数据:', err)
  }finally{
    loading.value=false
  }
  
}

</script>

<style scoped>
.page-header {
  padding: 16px;
}
.page-content {
  padding: 10px;
  border-radius: 8px;
}
.card {
  background-color: #fff;
  border-radius: 8px;
  padding: 16px;
  margin-bottom: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}
.section-title {
  font-size: 18px;
  font-weight: 600;
  color: #323233;
  margin-bottom: 12px;
}
.page-container {
  min-height: 100vh;
  background-color: #f5f5f5;
  padding-bottom: 70px;
}
.quick-entry {
  display: flex;
  justify-content: space-between;
}
.entry-item {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 16px;
  background-color: #fff;
  border-radius: 8px;
  margin-right: 12px;
}
.entry-item:last-child {
  margin-right: 0;
}
.entry-text {
  font-size: 13px;
  color: #323233;
  margin-top: 8px;
}
.hot-destination {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
}
.city-tag {
  padding: 8px 12px;
  background: #f7f8fa;
  border-radius: 16px;
  font-size: 14px;
  color: #666;
  transition:all 0.3s;
} 
.city-tag.active {
  background-color: #007AFF;
  color: #fff;
}
</style>