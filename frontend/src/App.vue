<script setup>
import { ref, watch } from 'vue'
import { useRoute } from 'vue-router'
const route = useRoute()

const active = ref(0)

watch(
  () => route.name,
  (name) => {
    if (name === 'Home') active.value = 0
    else if (name === 'Chat') active.value = 1
    else if (name === 'Community') active.value = 2
    else if (name === 'Profile') active.value = 3
    else active.value = 0
  },
  { immediate: true }
)
</script>

<template>
  <router-view :key="route.fullPath" />
  <van-tabbar v-if="route.name && ['Home','Chat','Community','Profile'].includes(route.name)" v-model="active">
    <van-tabbar-item icon="home-o" to="/">首页</van-tabbar-item>
    <van-tabbar-item icon="chat-o" to="/chat">对话</van-tabbar-item>
    <van-tabbar-item icon="user-o" to="/community">社区</van-tabbar-item>
    <van-tabbar-item icon="friends-o" to="/profile">个人中心</van-tabbar-item>
  </van-tabbar>
</template>
<style scoped>
.page{
  min-height: 100vh;
  background: linear-gradient(180deg, #E8F0FE 0%, #F5F7FA 40%, #FFFFFF 100%);
  display: flex;
  flex-direction: column;
}
:deep(.van-tabbar) {
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(12px);
  box-shadow: 0 -4px 20px rgba(67, 206, 162, 0.08);
  border-top: none;
  z-index: 200;
}
:deep(.van-tabbar-item--active) {
  color: #43cea2;
}
:deep(.van-tabbar-item--active .van-icon) {
  background: linear-gradient(135deg, #43cea2, #185a9d);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
}
</style>
