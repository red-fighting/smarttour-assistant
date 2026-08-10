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
  router.back()
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


.chat-container {
  height: 630px;
  padding: 16px;
  padding-bottom: 60px;
  overflow-y: auto;
}

.chat-empty {
  height: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 40px 24px;
}

/* 装饰图标 */
.empty-icon-wrapper {
  position: relative;
  margin-bottom: 20px;
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
  width: 90px;
  height: 90px;
  border-radius: 50%;
  background: radial-gradient(circle, rgba(25, 137, 250, 0.08) 0%, transparent 70%);
  z-index: -1;
  animation: pulseRing 3s ease-in-out infinite;
}

@keyframes float {
  0%, 100% { transform: translateY(0); }
  50% { transform: translateY(-10px); }
}

.empty-title {
  font-size: 20px;
  font-weight: 600;
  color: #323233;
  margin-bottom: 8px;
}

.empty-desc {
  font-size: 14px;
  color: #969799;
  margin-bottom: 32px;
}

.quick-questions {
  width: 100%;
}

.quick-label {
  font-size: 13px;
  color: #646566;
  margin-bottom: 12px;
  padding-left: 8px;
}

.quick-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.quick-tag {
  padding: 8px 16px;
  border-radius: 20px;
  font-size: 13px;
  background-color: rgba(25, 137, 250, 0.08);
  border-color: rgba(25, 137, 250, 0.2);
  color: #1989FA;
  transition: all 0.3s ease;
}

.quick-tag:active {
  transform: scale(0.96);
  background-color: rgba(25, 137, 250, 0.15);
}

.message-list {
  height: 100%;
  padding: 16px;
  overflow-y: auto;
}

.message-list::-webkit-scrollbar {
  width: 4px;
}

.message-list::-webkit-scrollbar-track {
  background: transparent;
}

.message-list::-webkit-scrollbar-thumb {
  background: rgba(0, 0, 0, 0.1);
  border-radius: 2px;
}

.message-item {
  display: flex;
  align-items: flex-start;
  margin-bottom: 20px;
  animation: fadeIn 0.3s ease;
}

@keyframes fadeIn {
  from { opacity: 0; transform: translateY(10px); }
  to { opacity: 1; transform: translateY(0); }
}

.message-item.ai {
  justify-content: flex-start;
}

.message-item.user {
  justify-content: flex-end;
}

.avatar {
  width: 36px;
  height: 36px;
  flex-shrink: 0;
  margin: 0 10px;
  border-radius: 50%;
  background-color: #fff;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  display: flex;
  align-items: center;
  justify-content: center;
}

.message-bubble {
  max-width: 70%;
  display: flex;
  flex-direction: column;
}

.message-content {
  padding: 12px 16px;
  border-radius: 18px;
  font-size: 14px;
  line-height: 1.6;
  word-wrap: break-word;
  white-space: pre-wrap;
}

.message-item.ai .message-content {
  background-color: #fff;
  color: #323233;
  border-top-left-radius: 4px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
}

.message-item.user .message-content {
  background: linear-gradient(135deg, #1989FA 0%, #0D6EFD 100%);
  color: #fff;
  border-top-right-radius: 4px;
  box-shadow: 0 4px 12px rgba(25, 137, 250, 0.3);
}

.message-time {
  font-size: 11px;
  color: #C8C9CC;
  margin-top: 6px;
  padding: 0 4px;
}

.message-item.user .message-time {
  text-align: right;
}

.streaming-indicator {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 16px;
  background-color: #fff;
  border-radius: 18px;
  border-top-left-radius: 4px;
  font-size: 13px;
  color: #969799;
  margin-bottom: 16px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
}

.typing-dots {
  display: flex;
  gap: 4px;
}

.typing-dots span {
  width: 6px;
  height: 6px;
  background-color: #1989FA;
  border-radius: 50%;
  animation: typing 1.4s ease-in-out infinite;
}

.typing-dots span:nth-child(2) {
  animation-delay: 0.2s;
}

.typing-dots span:nth-child(3) {
  animation-delay: 0.4s;
}

@keyframes typing {
  0%, 80%, 100% { transform: scale(0.6); opacity: 0.5; }
  40% { transform: scale(1); opacity: 1; }
}

.input-area {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
  padding: 12px 16px;
  padding-bottom: calc(12px + env(safe-area-inset-bottom));
  box-shadow: 0 -4px 20px rgba(0, 0, 0, 0.06);
}

.input-wrapper {
  display: flex;
  align-items: center;
  gap: 12px;
  background-color: #F5F7FA;
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

.send-btn {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  padding: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.3s ease;
}

.send-btn:active {
  transform: scale(0.9);
}

.send-btn svg {
  width: 18px;
  height: 18px;
}

.send-btn:disabled {
  opacity: 0.4;
}
</style>