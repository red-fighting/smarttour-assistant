<template>
  <div class="page-container page">
    <div class="page-header">
      <van-nav-bar 
      title="AI旅游助手"
      left-arrow
      left-text="返回" 
      fixed
      @click-left="onBack"
      :border="false"
      />
    </div>
    <!-- ========== 聊天主体区域 ========== -->
    <div class="chat-container" ref="chatContainer">
      <!--还未开始对话-->
      <div v-if="messages.length===0" class="chat-empty">
        <!-- 装饰性图标（纯 CSS 绘制） -->
        <div class="empty-icon-wrapper">
          <div class="empty-icon">🧳</div>
          <div class="empty-ring"></div>
        </div>
        <h3 class="empty-title">开始你的旅行规划</h3>
        <p class="empty-desc">告诉 AI 你的旅行想法💡，我会帮你定制专属方案</p>
        <!-- 快速问题标签 -->
        <div class="quick-questions">
          <div class="quick-tags">
            <van-tag class="quick-tag" v-for="question in quickQuestions" :key="question" plain round type="primary" @click="inputMessage = question">
              {{ question }}
            </van-tag>
          </div>
        </div>
      </div>
      <!-- 消息列表 -->
      <div class="message-list">
        <ChatBubble v-for="message in messages" :key="message.id" :message="message" />
        <div class="streaming-indicator" v-if="isStreaming">
          <van-loading type="spinner" size="20px"/> 
          <span>AI正在思考中...</span>
        </div>
      </div>
    </div>
    <div class="chat-input-area">
      <div class="input-wrapper">
        <van-field
          v-model="inputMessage"
          placeholder="输入您的问题..."
          :disabled="isStreaming"
          @keyup.enter="sendMessage"
          class="input-field"
          ref="inputFieldRef"
        />
        <van-button 
          :disabled="!inputMessage.trim()||isStreaming  " 
          @click="sendMessage" 
          size="small" 
          type="primary" 
          class="send-button"
          >
          发送
        </van-button>
      </div>
    </div>
  </div>
</template>

<script setup>
import ChatBubble from '../components/ChatBubble.vue'
import { ref,nextTick } from 'vue'
import { useRouter } from 'vue-router'
import {fetchStream} from '../utils/request'
import { showToast } from 'vant'


//  1. 路由 & DOM 引用
// ============================================================
const chatContainer=ref(null)//聊天容器
const router=useRouter()
const inputFieldRef = ref(null)// 输入框 DOM，用于聚焦
//  2. 响应式数据
// ============================================================
const inputMessage = ref('')//接受用户输入
const isStreaming = ref(false)//是否正在流式响应
const messages=ref([])//对话消息
const quickQuestions = ref([  //快速问题
  '去日本旅游需要多少预算？',
  '适合带父母去的国内目的地？',
  '三亚 5 天 4 晚怎么玩？',
  '出国旅游需要准备哪些证件？',
  '我需要携带哪些物品？'
])
//  3. 工具函数
// ============================================================
/**
 * 格式化消息内容：将换行符转为 <br>，支持简单 Markdown
 * 注意：实际项目中建议使用 marked 等库，这里仅做演示
 */
const formatContent = (content) => {
  if (!content) return ''
  // 将换行符转为 <br>
  return content.replace(/\n/g, '<br>')
}
/** 滚动聊天容器到底部 */
const scrollToBottom = () => {
  nextTick(() => {
    if (chatContainer.value) {
      chatContainer.value.scrollTop = chatContainer.value.scrollHeight
    }
  })
}

//  4. 核心业务逻辑
// ============================================================
/** 返回上一页 */
const onBack = () => {
  router.back({ name: 'Home' })
}

/** 添加用户消息到列表 */
const addUserMessage=(msg)=>{
  messages.value.push({
    id:Date.now()+1,
    role:'user',
    content:msg,
    timestamp:new Date().toISOString()
  })
}
/** 发送消息 */
const sendMessage = () => {
  const msg = inputMessage.value.trim()
  // 空消息 或 正在流式响应时禁止发送
  if (!msg || isStreaming.value) return

  // 1) 添加用户消息
  addUserMessage(msg)

  // 2) 清空输入框
  const userMsg = inputMessage.value
  inputMessage.value = ''

  // 3) 调用 AI 接口
  fetchAIResponse(userMsg)
}

//获取流式响应
const fetchAIResponse =(userMsg)=>{
  isStreaming.value=true  
  //创建AI的会话消息数据
  messages.value.push({
    id: Date.now() + 2,
    role: 'ai',
    content: '',
    timestamp: new Date().toISOString()
  })
  let fullResponse=''
  fetchStream('/chat',{message:userMsg},(chunk)=>{
    chunk.replace(/\n/g, '<br>')  
    fullResponse+=chunk
    //更新AI的会话消息数据
    const lastMsg=messages.value[messages.value.length-1]
    if(lastMsg&&lastMsg.role==='ai'){
      lastMsg.content=fullResponse
    }
    //滚动到最底部
    scrollToBottom()
  },()=>{
    //流式响应结束，重置状态
    isStreaming.value=false
    //滚动到最底部
    scrollToBottom()
  },(errMsg)=>{
    /** 更新最后一条 AI 消息的内容（流式追加） */
    const lastMsg=messages.value[messages.value.length-1]
    if(lastMsg&&lastMsg.role==='ai'){
      lastMsg.content=`服务器错误，请稍后重试 ${errMsg}`
    }
    isStreaming.value=false
    showToast('AI旅游助手响应失败，请稍后重试')
    //滚动到最底部
    scrollToBottom()
  })
}

//  5. 生命周期 & 监听
// ============================================================


</script>

<style scoped>
/* ===== 整体背景：浅灰渐变 ===== */
.page-container {
  height: 100%;
  background: linear-gradient(180deg, #f5f7fa 0%, #eef1f6 100%);
}
.page {
  padding: 0;
}

/* ===== 顶部导航栏：渐变背景 + 白色文字 ===== */
.page-header {
  margin-bottom: 0;
}
.page-header :deep(.van-nav-bar) {
  background: linear-gradient(135deg, #43cea2 0%, #185a9d 100%) !important;
}
.page-header :deep(.van-nav-bar__title) {
  color: #fff !important;
  font-weight: 600;
  letter-spacing: 1px;
}
.page-header :deep(.van-nav-bar__text) {
  color: #fff !important;
}
.page-header :deep(.van-nav-bar .van-icon),
.page-header :deep(.van-nav-bar .van-nav-bar__arrow) {
  color: #fff !important;
}
.page-header :deep(.van-nav-bar::after) {
  border-bottom-width: 0;
}

/* ===== 聊天消息区域 ===== */
.chat-container {
  height: 100vh;
  padding: 56px 0 140px;
  overflow-y: auto;
}
.chat-container::-webkit-scrollbar {
  width: 4px;
}
.chat-container::-webkit-scrollbar-track {
  background: transparent;
}
.chat-container::-webkit-scrollbar-thumb {
  background: rgba(67, 206, 162, 0.15);
  border-radius: 2px;
}

/* ===== 空状态提示 ===== */
.chat-empty {
  min-height: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 40px 24px;
}

/* 装饰图标 */
.empty-icon-wrapper {
  position: relative;
  margin-bottom: 24px;
}
.empty-icon {
  font-size: 64px;
  line-height: 1;
  animation: float 3.5s ease-in-out infinite;
  display: block;
}
.empty-ring {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  width: 110px;
  height: 110px;
  border-radius: 50%;
  background: radial-gradient(circle, rgba(67, 206, 162, 0.12) 0%, transparent 70%);
  z-index: -1;
  animation: pulseRing 3s ease-in-out infinite;
}
@keyframes float {
  0%, 100% { transform: translateY(0); }
  50% { transform: translateY(-10px); }
}
@keyframes pulseRing {
  0%, 100% { transform: translate(-50%, -50%) scale(1); opacity: 0.8; }
  50% { transform: translate(-50%, -50%) scale(1.18); opacity: 0.4; }
}

.empty-title {
  font-size: 20px;
  font-weight: 700;
  color: #1a1a2e;
  margin-bottom: 8px;
  letter-spacing: 0.5px;
}
.empty-desc {
  font-size: 14px;
  color: #8a8d99;
  margin-bottom: 32px;
  letter-spacing: 0.3px;
}

.quick-questions {
  width: 100%;
}
.quick-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  justify-content: center;
}
.quick-tag {
  padding: 8px 16px !important;
  border-radius: 20px !important;
  font-size: 13px !important;
  background-color: rgba(67, 206, 162, 0.08) !important;
  border-color: rgba(67, 206, 162, 0.2) !important;
  color: #43cea2 !important;
  transition: all 0.3s ease;
}
.quick-tag:active {
  transform: scale(0.96);
  background-color: rgba(67, 206, 162, 0.15) !important;
}

/* ===== 消息列表 ===== */
.message-list {
  padding: 16px;
}

/* ===== 消息气泡：穿透 ChatBubble 组件 ===== */
:deep(.chat-bubble) {
  margin-bottom: 20px;
}
:deep(.message-text) {
  padding: 12px 16px !important;
  border-radius: 16px !important;
  font-size: 14px;
  line-height: 1.6;
  word-wrap: break-word;
  white-space: pre-wrap;
}
:deep(.message-text.ai-message) {
  background-color: #fff !important;
  color: #1a1a2e !important;
  border-top-left-radius: 4px !important;
  box-shadow: 0 4px 20px rgba(67, 206, 162, 0.06) !important;
}
:deep(.user-message .message-text) {
  background: linear-gradient(135deg, #43cea2 0%, #185a9d 100%) !important;
  color: #fff !important;
  border-top-right-radius: 4px !important;
  box-shadow: 0 4px 16px rgba(67, 206, 162, 0.3) !important;
}
:deep(.avatar) {
  box-shadow: 0 2px 12px rgba(67, 206, 162, 0.1);
}
:deep(.message-time) {
  font-size: 11px;
  color: #C8C9CC;
  margin-top: 6px;
  padding: 0 4px;
}

/* ===== 流式响应指示器 ===== */
.streaming-indicator {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 10px 16px;
  background-color: #fff;
  border-radius: 16px;
  border-top-left-radius: 4px;
  font-size: 13px;
  color: #8a8d99;
  margin: 0 0 16px 16px;
  box-shadow: 0 4px 20px rgba(67, 206, 162, 0.06);
}

/* ===== 输入框区域 ===== */
.chat-input-area {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  background: rgba(255, 255, 255, 0.96);
  backdrop-filter: blur(12px);
  -webkit-backdrop-filter: blur(12px);
  padding: 12px 16px;
  padding-bottom: calc(62px + env(safe-area-inset-bottom));
  box-shadow: 0 -4px 24px rgba(67, 206, 162, 0.08);
}
.input-wrapper {
  display: flex;
  align-items: center;
  gap: 12px;
  background-color: #f5f7fa;
  border-radius: 24px;
  padding: 6px 6px 6px 16px;
}
.input-wrapper :deep(.van-field) {
  flex: 1;
  background: transparent;
}
.input-wrapper :deep(.van-field__control) {
  font-size: 14px;
  padding: 0;
  margin: 0;
  background: transparent;
}
.input-wrapper :deep(.van-field__control::placeholder) {
  color: #C8C9CC;
}

/* ===== 发送按钮：渐变背景 ===== */
.send-button {
  background: linear-gradient(135deg, #43cea2 0%, #185a9d 100%) !important;
  border: none !important;
  border-radius: 18px !important;
  padding: 0 18px !important;
  font-weight: 600;
  letter-spacing: 0.5px;
  box-shadow: 0 4px 12px rgba(67, 206, 162, 0.3);
  transition: all 0.3s ease;
}
.send-button:active {
  transform: scale(0.95);
}
.send-button:disabled {
  opacity: 0.5;
  box-shadow: none;
}
</style>