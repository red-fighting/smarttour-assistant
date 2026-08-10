<script setup>
import { ref } from 'vue'
import { useRoute } from 'vue-router'
const route = useRoute()


const active = ref(0)
</script>

<template>
  <router-view />
  <van-tabbar v-if="route.name && ['Home','Chat','Profile'].includes(route.name)" v-model="active">
    <van-tabbar-item icon="home-o" to="/">首页</van-tabbar-item>
    <van-tabbar-item icon="chat-o" to="/chat">对话</van-tabbar-item>
    <van-tabbar-item icon="friends-o" to="/profile">个人中心</van-tabbar-item>
  </van-tabbar>
</template>
<style scoped>
.page{
  min-height: 100vh;
  background: linear-gradient(180deg, #E8F4FD 0%, #F5F7FA 50%, #FFFFFF 100%);
  display: flex;
  padding: 16px;
  flex-direction: column;
}
</style>
