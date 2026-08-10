<template>
  <div class="chat-bubble" :class="messageClass">
    <div class="avatar" v-if="message.role === 'ai'">
      <svg viewBox="0 0 40 40" fill="none">
        <circle cx="20" cy="20" r="18" fill="#E8F4FD"/>
        <circle cx="20" cy="18" r="10" fill="#4A90D9"/>
        <path d="M15 15 L15 21" stroke="#fff" stroke-width="1.5" stroke-linecap="round"/>
        <path d="M25 15 L25 21" stroke="#fff" stroke-width="1.5" stroke-linecap="round"/>
        <path d="M13 25 Q20 30 27 25" stroke="#fff" stroke-width="1.5" stroke-linecap="round" fill="none"/>
      </svg>
    </div>
    <div class="message-content">
      <div class="message-text" :class="{ 'ai-message': message.role !== 'user' }">
        <div v-html="formattedContent"></div>
      </div>
      <div class="message-time" v-if="showTime">{{ formatTime }}</div>
    </div>
    <div class="avatar" v-if="message.role === 'user'">
      <svg viewBox="0 0 40 40" fill="none">
        <circle cx="20" cy="20" r="18" fill="#F5F5F5"/>
        <circle cx="20" cy="20" r="12" fill="#1989FA"/>
        <circle cx="16" cy="17" r="2" fill="#fff"/>
        <circle cx="24" cy="17" r="2" fill="#fff"/>
        <path d="M14 24 Q20 28 26 24" stroke="#fff" stroke-width="1.5" stroke-linecap="round" fill="none"/>
      </svg>
    </div>
  </div>
</template>
<script setup>
import { computed } from 'vue'
const props = defineProps({
  message: {
    type: Object,
    required: true
  }
})
const messageClass = computed(() => {
  return props.message.role === 'user' ? 'user-message' : 'ai-message'
})
const showTime = computed(() => {
  return props.message.timestamp && props.message.content
})
const formatTime = computed(() => {
  if (!props.message.timestamp) return ''
  const date = new Date(props.message.timestamp)
  return `${date.getHours().toString().padStart(2, '0')}:${date.getMinutes().toString().padStart(2, '0')}`
})
const formattedContent = computed(() => {
  if (!props.message.content) return ''
  return props.message.content.replace(/\n/g, '<br>')
})
</script>
<style scoped>
.chat-bubble {
  display: flex;
  align-items: flex-start;
  margin-bottom: 16px;
  animation: fadeIn 0.3s ease;
}
@keyframes fadeIn {
  from { opacity: 0; transform: translateY(10px); }
  to { opacity: 1; transform: translateY(0); }
}
.chat-bubble.user-message {
  justify-content: flex-end;
}
.chat-bubble.ai-message {
  justify-content: flex-start;
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
.message-content {
  max-width: 70%;
  display: flex;
  flex-direction: column;
}
.message-text {
  padding: 12px 16px;
  border-radius: 18px;
  font-size: 14px;
  line-height: 1.6;
  word-wrap: break-word;
  white-space: pre-wrap;
}
.message-text.ai-message {
  background-color: #fff;
  color: #323233;
  border-top-left-radius: 4px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
}
.user-message .message-text {
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
.user-message .message-time {
  text-align: right;
}
</style>