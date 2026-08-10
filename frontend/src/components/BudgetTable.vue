<template>
  <div class="budget-table">
    <van-cell-group :border="false">
      <van-cell
      v-for="(value, key) in budgetItems"
      :key="key"
      :title="getLabel(key)"
      :value="`￥${value}`"
      :border="false"
      />
    </van-cell-group>
    <div class="budget-total">
      <span >总计</span>
      <span class="budget-value">¥{{ total }}</span>
    </div>
  </div>
</template>
<script setup>
import {computed} from 'vue'
const props = defineProps({
  Data: {
    type: Object,
    default: () => ({})
  },
  total: {
    type: [Number, String],
    default: 0
  }
})
const budgetItems = computed(() => {
  return {
    accommodation: props.Data.accommodation || 0,
    transportation: props.Data.transportation || 0,
    food: props.Data.food || 0,
    entertainment: props.Data.entertainment || 0,
  }
})
const getLabel = (key) => {
  const labels = {
    accommodation: '住宿',
    transportation: '交通',
    food: '餐饮',
    entertainment: '娱乐'
  }
  return labels[key] || key
}
</script>
<style scoped>
.budget-table {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 12px;
  background-color: #fff;
  border-radius: 8px;
  margin-bottom: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}
.budget-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 8px 0;
}
.budget-label {
  font-size: 14px;
  color: #666;
}
</style>