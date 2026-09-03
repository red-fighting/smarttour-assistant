<template>
  <div class="page-container page">
    <div class="page-header">
      <van-nav-bar 
        title="个人中心"
        left-arrow
        left-text="返回" 
        fixed/>
    </div>
    <div class="page-content">
      <div class="user-card">
        <div class="user-main">
          <div class="avatar-wrap" @click="triggerAvatarUpload">
            <img :src="userInfo?.avatar || defaultAvatar" class="avatar-img" />
            <input type="file" ref="avatarFileInput" accept="image/*" style="display:none" @change="handleAvatarUpload" />
          </div>
          <div class="user-info">
            <h2 class="username">{{ userInfo?.username || '游客' }}</h2>
            <p class="user-desc">欢迎使用智能旅游助手</p>
          </div>
        </div>
        <div class="user-stats">
          <div class="stat-item">
            <div class="stat-num">{{ favorites.length }}</div>
            <div class="stat-label">收藏</div>
          </div>
          <div class="stat-item">
            <div class="stat-num">{{ myShares.length }}</div>
            <div class="stat-label">分享</div>
          </div>
          <div class="stat-item">
            <div class="stat-num">{{ myShares.reduce((s, i) => s + (i.likes || 0), 0) }}</div>
            <div class="stat-label">点赞</div>
          </div>
        </div>
      </div>

      <div class="menu-card">
        <van-cell-group inset>
          <van-cell icon="orders-o" title="我的订单" is-link @click="openMyOrders" />
          <van-cell icon="like-o" title="我的收藏" is-link @click="openFavorites" />
          <van-cell icon="share-o" title="我的分享" is-link @click="openMyShares" />
          <van-cell icon="location-o" title="常用地址" is-link />
          <van-cell icon="settings-o" title="设置" is-link @click="showSettings = true" />
        </van-cell-group>
      </div>

      <div class="menu-card">
        <van-cell-group inset>
          <van-cell title="关于Smarttour_Assistant" icon="info-o" is-link @click="showAboutApp = true" />
          <van-cell icon="service-o" title="联系客服" is-link />
        </van-cell-group>
      </div>

      <div class="version-info">
        <p>版本 1.0.0</p>
      </div>
    </div>

    <!-- 我的收藏弹窗 -->
    <van-popup
      v-model:show="showFavorites"
      position="bottom"
      round
      :style="{ height: '70%' }"
    >
      <div class="favorites-popup">
        <van-nav-bar title="我的收藏" />
        <div class="favorites-list">
          <van-empty v-if="!favorites.length" description="暂无收藏内容" />
          <div
            v-else
            v-for="item in favorites"
            :key="item.postId || item.id"
            class="fav-card"
          >
            <div class="fav-header">
              <van-image round width="32" height="32" :src="item.postAvatar || defaultAvatar" />
              <div class="fav-user-info">
                <div class="fav-username">{{ item.postUsername }}</div>
                <div class="fav-meta">{{ item.postLocation }}</div>
              </div>
              <van-tag plain type="primary" size="medium">{{ item.postTag }}</van-tag>
            </div>
            <div class="fav-title">{{ item.postTitle }}</div>
            <div class="fav-content">{{ item.postContent }}</div>
            <div v-if="parseImages(item.postImages).length" class="fav-images">
              <van-image
                v-for="(img, idx) in parseImages(item.postImages)"
                :key="idx"
                width="32%"
                height="70"
                radius="6"
                fit="cover"
                :src="img"
              />
            </div>
          </div>
        </div>
      </div>
    </van-popup>

    <!-- 我的分享弹窗 -->
    <van-popup
      v-model:show="showMyShares"
      position="bottom"
      round
      :style="{ height: '70%' }"
    >
      <div class="favorites-popup">
        <van-nav-bar title="我的分享" right-text="发布" @click-right="goCommunity" />
        <div class="favorites-list">
          <van-empty v-if="!myShares.length" description="暂无分享内容" />
          <div v-for="item in myShares" :key="item.id" class="fav-card">
            <div class="fav-header">
              <div class="fav-user-info">
                <div class="fav-username">我</div>
                <div class="fav-meta">{{ formatTime(item.createTime) }}</div>
              </div>
              <van-tag plain type="primary" size="medium">{{ item.tag }}</van-tag>
            </div>
            <div class="fav-title">{{ item.title }}</div>
            <div class="fav-content">{{ item.content }}</div>
            <div v-if="parseImages(item.images).length" class="fav-images">
              <van-image
                v-for="(img, idx) in parseImages(item.images)"
                :key="idx"
                width="32%"
                height="70"
                radius="6"
                fit="cover"
                :src="img"
              />
            </div>
            <div class="share-actions">
              <span>👍 {{ item.likes || 0 }}</span>
              <span>💬 {{ item.comments || 0 }}</span>
              <van-button size="mini" type="danger" plain @click="deleteMyShare(item)">删除</van-button>
            </div>
          </div>
        </div>
      </div>
    </van-popup>

    <!-- 我的订单弹窗（购物车 + 已支付订单） -->
    <van-popup
      v-model:show="showMyOrders"
      position="bottom"
      round
      :style="{ height: '85%' }"
    >
      <div class="orders-popup">
        <van-nav-bar title="我的订单">
          <template #right>
            <span class="order-cart-count" v-if="cartTotal > 0">购物车：{{ cartTotal }} 件</span>
          </template>
        </van-nav-bar>
        <van-tabs v-model:active="orderTab" line-width="24px" color="#43cea2" title-active-color="#1a1a2e">
          <van-tab title="购物车" name="0">
            <div class="orders-list">
              <van-empty v-if="!orders.filter(o => o.status === 0).length" description="购物车是空的，去首页选商品吧~" />
              <template v-else>
                <div
                  v-for="order in orders.filter(o => o.status === 0)"
                  :key="order.id"
                  class="order-card cart-card"
                >
                  <img class="order-img" :src="order.imageSnapshot || defaultAvatar" @error="onOrderImgError(order)" />
                  <div class="order-info">
                    <div class="order-title">{{ order.titleSnapshot }}</div>
                    <div class="order-meta">
                      数量：×{{ order.quantity }}
                    </div>
                    <div class="order-bottom">
                      <span class="order-price">¥{{ formatPrice(order.price * order.quantity) }}</span>
                      <div class="order-actions">
                        <van-button size="small" type="danger" plain @click="deleteOrderItem(order)">删除</van-button>
                        <van-button size="small" type="success" @click="payOneOrder(order)">支付</van-button>
                      </div>
                    </div>
                  </div>
                </div>
                <div class="cart-footer">
                  <div class="cart-total">
                    合计：<span class="cart-total-amount">¥{{ formatPrice(cartTotalAmount) }}</span>
                  </div>
                  <van-button type="primary" round @click="handlePayAllCart">一键支付 ({{ cartTotal }}件)</van-button>
                </div>
              </template>
            </div>
          </van-tab>
          <van-tab title="已支付" name="1">
            <div class="orders-list">
              <van-empty v-if="!orders.filter(o => o.status === 1).length" description="暂无已支付订单" />
              <div
                v-for="order in orders.filter(o => o.status === 1)"
                :key="order.id"
                class="order-card paid-card"
              >
                <img class="order-img" :src="order.imageSnapshot || defaultAvatar" @error="onOrderImgError(order)" />
                <div class="order-info">
                  <div class="order-title">{{ order.titleSnapshot }}</div>
                  <div class="order-meta">数量：×{{ order.quantity }} · 支付时间：{{ formatTime(order.paidTime) }}</div>
                  <div class="order-bottom">
                    <span class="order-price">¥{{ formatPrice(order.price * order.quantity) }}</span>
                    <van-tag type="success" plain>已支付</van-tag>
                  </div>
                </div>
              </div>
            </div>
          </van-tab>
        </van-tabs>
      </div>
    </van-popup>

    <!-- 设置弹窗 -->
    <van-popup
      v-model:show="showSettings"
      position="bottom"
      round
      :style="{ height: '80%' }"
    >
      <div class="settings-popup">
        <van-nav-bar title="设置" />
        <div class="settings-list">
          <!-- 账号 -->
          <van-cell-group inset>
            <van-cell title="个人资料" icon="user-o" is-link @click="showProfile = true" />
            <van-cell title="账号安全" icon="shield-o" is-link @click="showPasswordPopup = true" />
          </van-cell-group>

          <!-- 其他 -->
          <van-cell-group inset style="margin-top: 12px;">
            <van-cell title="帮助与反馈" icon="question-o" is-link @click="showFeedback = true" />
            <van-cell title="隐私协议" icon="balance-o" is-link @click="showPrivacy = true" />
          </van-cell-group>

          <!-- 退出登录 -->
          <div style="padding: 24px 16px 0;">
            <van-button block type="danger" plain class="logout-btn" @click="confirmLogout">退出登录</van-button>
          </div>

          <div class="settings-version">
            <p>Smarttour_Assistant v1.0.0</p>
          </div>
        </div>
      </div>
    </van-popup>

    <!-- 个人资料弹窗 -->
    <van-popup
      v-model:show="showProfile"
      position="bottom"
      round
      :style="{ height: '60%' }"
    >
      <div class="profile-popup">
        <van-nav-bar title="个人资料" right-text="保存" @click-right="saveProfile" />
        <div class="profile-body">
          <div class="profile-avatar-wrap" @click="triggerAvatarUpload">
            <van-image round width="72" height="72" :src="profileForm.avatar || defaultAvatar" />
            <div class="profile-avatar-tip">点击更换头像</div>
            <input type="file" ref="avatarFileInput" accept="image/*" style="display:none" @change="handleAvatarUpload" />
          </div>
          <van-cell-group inset>
            <van-field
              v-model="profileForm.username"
              label="昵称"
              placeholder="请输入昵称"
              :border="false"
            />
            <van-field
              v-model="profileForm.email"
              label="邮箱"
              placeholder="请输入邮箱"
              type="email"
              :border="false"
            />
            <van-field
              v-model="profileForm.phone"
              label="手机号"
              placeholder="请输入手机号"
              type="tel"
              :border="false"
            />
          </van-cell-group>
        </div>
      </div>
    </van-popup>

    <!-- 修改密码弹窗 -->
    <van-popup
      v-model:show="showPasswordPopup"
      position="bottom"
      round
      :style="{ height: '55%' }"
    >
      <div class="pwd-popup">
        <van-nav-bar title="修改密码" right-text="确认" @click-right="submitChangePassword" />
        <div class="pwd-body">
          <van-cell-group inset>
            <van-field
              v-model="pwdForm.oldPassword"
              label="旧密码"
              type="password"
              placeholder="请输入旧密码"
              :border="false"
            />
            <van-field
              v-model="pwdForm.newPassword"
              label="新密码"
              type="password"
              placeholder="请输入新密码（至少6位）"
              :border="false"
            />
            <van-field
              v-model="pwdForm.confirmPassword"
              label="确认密码"
              type="password"
              placeholder="请再次输入新密码"
              :border="false"
            />
          </van-cell-group>
          <div class="pwd-tip">提示：新密码需要与旧密码不同，且长度至少6位</div>
        </div>
      </div>
    </van-popup>

    <!-- 帮助与反馈弹窗 -->
    <van-popup
      v-model:show="showFeedback"
      position="bottom"
      round
      :style="{ height: '50%' }"
    >
      <div class="feedback-popup">
        <van-nav-bar title="帮助与反馈" right-text="提交" @click-right="handleSubmitFeedback" />
        <div class="feedback-body">
          <van-cell-group inset>
            <van-field
              v-model="feedbackForm.type"
              is-link
              readonly
              label="反馈类型"
              placeholder="请选择类型"
              :border="false"
              @click="showFeedbackTypePicker = true"
            />
            <van-field
              v-model="feedbackForm.content"
              type="textarea"
              label="反馈内容"
              placeholder="请详细描述您遇到的问题或建议..."
              rows="3"
              autosize
              :border="false"
            />
            <van-field
              v-model="feedbackForm.contact"
              label="联系方式"
              placeholder="可选，方便我们回复您"
              :border="false"
            />
          </van-cell-group>
        </div>
      </div>
      <!-- 反馈类型 picker -->
      <van-popup v-model:show="showFeedbackTypePicker" position="bottom" round>
        <van-picker
          title="反馈类型"
          :columns="feedbackTypes"
          @confirm="({ selectedOptions }) => {
            feedbackForm.type = selectedOptions[0].value
            showFeedbackTypePicker = false
          }"
          @cancel="showFeedbackTypePicker = false"
        />
      </van-popup>
    </van-popup>

    <!-- 关于 Smarttour_Assistant -->
    <van-dialog
      v-model:show="showAboutApp"
      title="关于Smarttour_Assistant"
      confirm-button-text="知道了"
    >
      <div class="dialog-content about-content">
        <van-icon name="global-o" size="48" color="#1989fa" />
        <h3>Smarttour_Assistant</h3>
        <p>版本：v1.0.0</p>
        <p>基于 AI 的智能景点介绍与行程规划系统，</p>
        <p>融合大模型能力，让每一次旅行都有规划、有温度。</p>
        <p class="copyright">© 2026 Smarttour_Assistant Team</p>
      </div>
    </van-dialog>

    <!-- 隐私协议弹窗 -->
    <van-dialog
      v-model:show="showPrivacy"
      title="隐私协议"
      confirm-button-text="知道了"
    >
      <div class="dialog-content">
        我们重视您的隐私。本应用仅收集必要的用户信息以提供服务，不会在未经您许可的情况下向第三方共享您的个人数据。所有行程数据和聊天记录仅用于为您提供个性化推荐。
      </div>
    </van-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, watch } from 'vue'
import { showToast, showConfirmDialog } from 'vant'
import { useRouter } from 'vue-router'
import { getUserInfo, changePassword, submitFeedbackApi, updateProfile, getFavorites, getMyShares, deleteShare, uploadImage, getMyOrders, deleteOrder, payOrder, payAllCart } from '../api/index'
import { getUserStorage, setUserStorage, removeUserStorage, clearAllUserStorage } from '../utils/storage'
//  1. 路由 & DOM 引用
// ============================================================
const defaultAvatar = 'https://api.dicebear.com/7.x/avataaars/svg?seed=default'
const userInfo = ref({
  userId: '',
  username: '',
  avatar: '',
  email: '',
  phone: ''
})
const router=useRouter()
const showFavorites = ref(false)
const favorites = ref([])
const showMyShares = ref(false)
const myShares = ref([])
const showMyOrders = ref(false)
const orders = ref([])
const orderTab = ref('0')
const showSettings = ref(false)
const showAboutApp = ref(false)
const showPrivacy = ref(false)
const showProfile = ref(false)
const showPasswordPopup = ref(false)
const showFeedback = ref(false)
const showFeedbackTypePicker = ref(false)
const avatarFileInput = ref(null)

const settings = reactive({
  darkMode: false,
  notification: true
})

const profileForm = reactive({
  username: '',
  avatar: '',
  email: '',
  phone: ''
})

const pwdForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

const feedbackForm = reactive({
  type: '',
  content: '',
  contact: ''
})

const feedbackTypes = [
  { text: '功能建议', value: '功能建议' },
  { text: 'Bug 反馈', value: 'Bug 反馈' },
  { text: '体验问题', value: '体验问题' },
  { text: '其他问题', value: '其他问题' }
]
//  2. 响应式数据
// ============================================================
//  3. 工具函数
// ============================================================

/** 兼容读取多种字段命名：驼峰、下划线、数据库原始字段 */
const pick = (obj, ...keys) => {
  for (const k of keys) {
    if (obj[k] !== undefined && obj[k] !== null && obj[k] !== '') return obj[k]
  }
  return undefined
}

/** 将收藏数据归一化为统一字段格式（兼容后端/本地各种命名），并过滤空数据 */
const normalizeFavorites = (list) => {
  if (!Array.isArray(list)) return []
  return list.map(f => {
    const postId = pick(f, 'postId', 'post_id', 'id', 'favoriteId', 'favorite_id')
    const title = pick(f, 'postTitle', 'post_title', 'title')
    const content = pick(f, 'postContent', 'post_content', 'content')
    const images = pick(f, 'postImages', 'post_images', 'images')
    const username = pick(f, 'postUsername', 'post_username', 'username')
    const avatar = pick(f, 'postAvatar', 'post_avatar', 'avatar')
    const location = pick(f, 'postLocation', 'post_location', 'location')
    const tag = pick(f, 'postTag', 'post_tag', 'tag')
    return {
      ...f,
      postId: postId ?? 0,
      postUsername: username ?? '用户',
      postAvatar: avatar ?? defaultAvatar,
      postLocation: location ?? '',
      postTag: tag ?? '',
      postTitle: title ?? '',
      postContent: content ?? '',
      postImages: images ? (typeof images === 'string' ? images : JSON.stringify(images)) : null
    }
  }).filter(f => {
    // 过滤标题+内容+图片都为空的无效收藏（脏数据）
    const hasTitle = !!String(f.postTitle || '').trim()
    const hasContent = !!String(f.postContent || '').trim()
    const hasImages = f.postImages && f.postImages !== '[]' && f.postImages !== 'null'
    return hasTitle || hasContent || hasImages
  })
}

/** 预加载收藏数据（只从后端拿） */
const loadFavorites = async () => {
  try {
    const res = await getFavorites()
    if (res && res.code === 200) {
      favorites.value = normalizeFavorites(res.data || [])
    } else {
      favorites.value = []
    }
  } catch {
    favorites.value = []
  }
}

/** 预加载分享数据（只从后端拿） */
const loadShares = async () => {
  try {
    const res = await getMyShares()
    if (res && res.code === 200) {
      myShares.value = res.data || []
    } else {
      myShares.value = []
    }
  } catch {
    myShares.value = []
  }
}

/** 打开我的收藏（只从后端拿，展示前转义图片字段） */
const openFavorites = async () => {
  showFavorites.value = true
  try {
    const res = await getFavorites()
    if (res && res.code === 200) {
      favorites.value = normalizeFavorites(res.data || [])
    } else {
      showToast(res?.message || '获取收藏失败')
      favorites.value = []
    }
  } catch (e) {
    showToast('网络错误')
    favorites.value = []
  }
}

/** 打开我的分享（只从后端拿） */
const openMyShares = async () => {
  showMyShares.value = true
  try {
    const res = await getMyShares()
    if (res && res.code === 200) {
      myShares.value = res.data || []
    } else {
      showToast(res?.message || '获取分享失败')
      myShares.value = []
    }
  } catch (e) {
    showToast('网络错误')
    myShares.value = []
  }
}

/** 删除我的分享（只走后端） */
const deleteMyShare = (item) => {
  showConfirmDialog({
    title: '提示',
    message: '确定删除这条分享吗？'
  }).then(async () => {
    try {
      const res = await deleteShare({ id: item.id })
      if (res && res.code === 200) {
        myShares.value = myShares.value.filter(s => s.id !== item.id)
        showToast('删除成功')
      } else {
        showToast(res?.message || '删除失败')
      }
    } catch (e) {
      showToast('网络错误，请稍后重试')
    }
  }).catch(() => {})
}

/** 去社区发布 */
const goCommunity = () => {
  showMyShares.value = false
  router.push('/community')
}

// ================= 我的订单 / 购物车 =================
const formatPrice = (p) => {
  const n = Number(p)
  return isNaN(n) ? '0.00' : n.toFixed(2)
}
const cartItems = computed(() => orders.value.filter(o => o.status === 0))
const cartTotal = computed(() => cartItems.value.reduce((s, o) => s + (Number(o.quantity) || 0), 0))
const cartTotalAmount = computed(() => cartItems.value.reduce((s, o) => s + (Number(o.price) || 0) * (Number(o.quantity) || 0), 0))
const onOrderImgError = (order) => {
  if (order.imageSnapshot !== defaultAvatar) order.imageSnapshot = defaultAvatar
}

/** 打开我的订单弹窗（从后端拉全部） */
const openMyOrders = async () => {
  showMyOrders.value = true
  try {
    const res = await getMyOrders(-1) // -1 = all
    if (res && res.code === 200 && Array.isArray(res.data)) {
      orders.value = res.data
    } else {
      orders.value = []
      if (res && res.code !== 200) showToast(res.message || '加载失败')
    }
  } catch (e) {
    console.warn('load orders error:', e)
    orders.value = []
    showToast('加载失败，请稍后重试')
  }
}

/** 删除一条订单/购物车 */
const deleteOrderItem = (order) => {
  showConfirmDialog({ title: '提示', message: '确定删除这条记录吗？' }).then(async () => {
    try {
      const res = await deleteOrder(order.id)
      if (res && res.code === 200) {
        orders.value = orders.value.filter(o => o.id !== order.id)
        showToast('删除成功')
      } else {
        showToast(res?.message || '删除失败')
      }
    } catch (e) {
      showToast('网络错误，请稍后重试')
    }
  }).catch(() => {})
}

/** 单件支付 */
const payOneOrder = async (order) => {
  try {
    const res = await payOrder(order.id)
    if (res && res.code === 200) {
      // 刷新订单列表
      const fresh = await getMyOrders(-1)
      if (fresh.code === 200) orders.value = fresh.data
      showToast('支付成功 🎉')
    } else {
      showToast(res?.message || '支付失败')
    }
  } catch (e) {
    console.warn('pay error:', e)
    showToast('支付失败，请稍后重试')
  }
}

/** 一键支付全部购物车 */
const handlePayAllCart = async () => {
  if (cartTotal.value === 0) { showToast('购物车是空的'); return }
  try {
    const res = await payAllCart()
    if (res && res.code === 200) {
      const fresh = await getMyOrders(-1)
      if (fresh.code === 200) orders.value = fresh.data
      orderTab.value = '1' // 跳到已支付
      showToast(`成功支付 ${res.data?.count || cartTotal.value} 件 🎉`)
    } else {
      showToast(res?.message || '支付失败')
    }
  } catch (e) {
    console.warn('payAll error:', e)
    showToast('支付失败，请稍后重试')
  }
}

/** 解析图片字符串（后端存的是 JSON 数组字符串） */
const parseImages = (images) => {
  if (!images) return []
  try {
    const arr = JSON.parse(images)
    return Array.isArray(arr) ? arr : []
  } catch (e) {
    return []
  }
}

/** 格式化时间 */
const formatTime = (t) => {
  if (!t) return ''
  const d = new Date(t)
  const now = new Date()
  const diff = Math.floor((now - d) / 1000)
  if (diff < 60) return '刚刚'
  if (diff < 3600) return Math.floor(diff / 60) + '分钟前'
  if (diff < 86400) return Math.floor(diff / 3600) + '小时前'
  if (diff < 604800) return Math.floor(diff / 86400) + '天前'
  return d.toLocaleDateString()
}

/** 保存设置到 localStorage */
const saveSettings = () => {
  setUserStorage('appSettings', settings)
}

/** 触发头像文件选择 */
const triggerAvatarUpload = () => {
  avatarFileInput.value?.click()
}

/** 处理头像文件：上传到服务器 → 拿到 URL → 保存后端 → 同步各界面 */
const handleAvatarUpload = async (event) => {
  const file = event.target.files?.[0]
  if (!file) return
  event.target.value = '' // 清空，保证下次同一张图也能触发 change

  // 校验
  if (!file.type.startsWith('image/')) {
    showToast('请选择图片文件')
    return
  }
  if (file.size > 10 * 1024 * 1024) {
    showToast('图片不能超过 10MB')
    return
  }

  try {
    showToast({ message: '上传中...', forbidClick: true, duration: 0 })

    // 1. 上传到服务器 uploads/ 目录
    const uploadRes = await uploadImage(file)
    if (!uploadRes || uploadRes.code !== 200 || !uploadRes.data?.url) {
      showToast(uploadRes?.message || '上传失败')
      return
    }
    const avatarUrl = uploadRes.data.url // 例如: /uploads/xxx.jpg

    // 2. 先更新本地 UI（立即预览）
    profileForm.avatar = avatarUrl
    userInfo.value.avatar = avatarUrl

    // 3. 保存到数据库
    const saveRes = await updateProfile({ avatar: avatarUrl })
    if (saveRes && saveRes.code === 200) {
      // 4. 后端保存成功 → 同步 localStorage（各界面从这里读）
      localStorage.setItem('userInfo', JSON.stringify(userInfo.value))
      showToast('头像更新成功')
    } else {
      showToast(saveRes?.message || '保存失败，请重试')
    }
  } catch (err) {
    console.error('=== [头像] 上传/保存失败 ===', err)
    showToast('上传失败，请检查网络')
  }
}

/** 保存个人资料 */
const saveProfile = async () => {
  try {
    const res = await updateProfile(profileForm)
    if (res && res.code === 200) {
      userInfo.value = { ...userInfo.value, ...profileForm }
      localStorage.setItem('userInfo', JSON.stringify(userInfo.value))
      showToast('保存成功')
      showProfile.value = false
    } else {
      showToast(res?.message || '保存失败')
    }
  } catch (e) {
    // 前端兜底保存
    userInfo.value = { ...userInfo.value, ...profileForm }
    localStorage.setItem('userInfo', JSON.stringify(userInfo.value))
    showToast('已保存（本地）')
    showProfile.value = false
  }
}

/** 修改密码 */
const submitChangePassword = async () => {
  if (!pwdForm.oldPassword) {
    showToast('请输入旧密码')
    return
  }
  if (!pwdForm.newPassword || pwdForm.newPassword.length < 6) {
    showToast('新密码至少6位')
    return
  }
  if (pwdForm.newPassword !== pwdForm.confirmPassword) {
    showToast('两次输入的新密码不一致')
    return
  }
  if (pwdForm.newPassword === pwdForm.oldPassword) {
    showToast('新密码不能与旧密码相同')
    return
  }

  // 从 localStorage 读取用户保存的旧密码进行校验（兜底：如果后端未提供则本地校验）
  const savedPwd = getUserStorage('userPassword')
  if (savedPwd && pwdForm.oldPassword !== savedPwd) {
    showToast('旧密码不正确')
    return
  }

  try {
    const res = await changePassword({
      oldPassword: pwdForm.oldPassword,
      newPassword: pwdForm.newPassword
    })
    if (res && res.code === 200) {
      setUserStorage('userPassword', pwdForm.newPassword)
      showToast('密码修改成功')
      showPasswordPopup.value = false
      pwdForm.oldPassword = ''
      pwdForm.newPassword = ''
      pwdForm.confirmPassword = ''
    } else {
      showToast(res?.message || '密码修改失败')
    }
  } catch (e) {
    // 前端兜底修改成功
    setUserStorage('userPassword', pwdForm.newPassword)
    showToast('密码修改成功（本地）')
    showPasswordPopup.value = false
    pwdForm.oldPassword = ''
    pwdForm.newPassword = ''
    pwdForm.confirmPassword = ''
  }
}

/** 提交反馈 */
const handleSubmitFeedback = async () => {
  if (!feedbackForm.type) {
    showToast('请选择反馈类型')
    return
  }
  if (!feedbackForm.content.trim()) {
    showToast('请填写反馈内容')
    return
  }
  console.log('=== [提交反馈] 开始 ===')
  console.log('反馈数据:', { ...feedbackForm })
  try {
    const res = await submitFeedbackApi(feedbackForm)
    console.log('=== [提交反馈] 接口返回 ===', res)
    if (res && res.code === 200) {
      showToast('反馈已提交，感谢您的帮助！')
    } else {
      console.warn('=== [提交反馈] 业务失败 ===', res)
      showToast(res?.message || '提交失败')
    }
  } catch (e) {
    console.error('=== [提交反馈] 接口异常 ===', e)
    showToast('反馈已提交，感谢您的帮助！')
  } finally {
    console.log('=== [提交反馈] 结束 ===')
    showFeedback.value = false
    feedbackForm.type = ''
    feedbackForm.content = ''
    feedbackForm.contact = ''
  }
}

/** 确认退出登录 */
const confirmLogout = () => {
  showConfirmDialog({
    title: '提示',
    message: '确定要退出登录吗？'
  }).then(() => {
    onLogout()
  }).catch(() => {})
}

/** 返回上一页 */
const onBack = () => {
  router.back()
}

/** 退出登录 */
const onLogout = () => {
  // 清除当前用户所有隔离的 localStorage 数据（favorites / myShares / appSettings / userPassword）
  clearAllUserStorage()
  // 清除全局登录态
  localStorage.removeItem('token')
  localStorage.removeItem('userInfo')
  // 关闭设置弹窗
  showSettings.value = false
  // 跳转到登录页
  router.push('/login')
}
// ====== 从后端获取用户信息 ======
// ✅ 从后端取数据
const loadUserInfo = async () => {
  console.log('开始获取用户信息...')
  try {
    console.log('开始获取用户信息...')
    const res = await getUserInfo()
    console.log('获取用户信息响应:', res)
    if (res.code === 200) {
      // 如果数据库头像为空，保留 localStorage 中的头像（登录时保存的本地头像）
      const stored = JSON.parse(localStorage.getItem('userInfo') || '{}')
      if ((!res.data.avatar || res.data.avatar === 'null') && stored.avatar) {
        res.data.avatar = stored.avatar
      }
      userInfo.value = res.data
      // 更新 localStorage
      localStorage.setItem('userInfo', JSON.stringify(res.data))
      console.log('✅ 获取用户信息成功:', res.data)
    } else {
      showToast(res.message || '获取信息失败')
    }
  } catch (error) {
    console.error('获取用户信息失败:', error)
    showToast('网络错误')
  }
}

// 页面加载时取数据
onMounted(async () => {
  const stored = localStorage.getItem('userInfo')
  if (stored) {
    userInfo.value = JSON.parse(stored)
  }
  loadUserInfo()
  const savedSettings = getUserStorage('appSettings')
  if (savedSettings) {
    Object.assign(settings, savedSettings)
  }
  // 预加载收藏和分享数据，确保统计数不为 0
  loadFavorites()
  loadShares()
})

// 打开个人资料弹窗时同步表单
watch(showProfile, (v) => {
  if (v) {
    profileForm.username = userInfo.value.username || ''
    profileForm.avatar = userInfo.value.avatar || ''
    profileForm.email = userInfo.value.email || ''
    profileForm.phone = userInfo.value.phone || ''
  }
})
//  4. 核心业务逻辑
// ============================================================
//  5. 生命周期 & 监听
// ============================================================
</script>

<style scoped>
/* ===== 页面容器 ===== */
.page-container {
  min-height: 100vh;
  background: #f5f7fa;
  display: flex;
  flex-direction: column;
}

/* ===== 顶部导航栏 ===== */
.page-header :deep(.van-nav-bar) {
  background: #fff !important;
  border: none !important;
  box-shadow: 0 1px 8px rgba(0, 0, 0, 0.04);
}
.page-header :deep(.van-nav-bar__title) {
  color: #1a1a2e !important;
  font-weight: 700;
}
.page-header :deep(.van-nav-bar__text),
.page-header :deep(.van-nav-bar .van-icon) {
  color: #43cea2 !important;
}

/* ===== 内容区 ===== */
.page-content {
  padding: 46px 0 24px;
}

/* ===== 用户信息卡片（渐变 Hero） ===== */
.user-card {
  background: linear-gradient(135deg, #43cea2 0%, #185a9d 100%);
  border-radius: 0 0 24px 24px;
  padding: 24px 20px 28px;
  margin: 0 0 16px;
  box-shadow: 0 4px 20px rgba(67, 206, 162, 0.15);
}
.user-main {
  display: flex;
  align-items: center;
}
.avatar-wrap {
  width: 72px;
  height: 72px;
  border-radius: 50%;
  padding: 3px;
  background: rgba(255, 255, 255, 0.35);
  margin-right: 16px;
  flex-shrink: 0;
  cursor: pointer;
  position: relative;
}
.avatar-wrap::after {
  content: '';
  position: absolute;
  bottom: 0;
  right: 0;
  width: 20px;
  height: 20px;
  background: #07c160;
  border-radius: 50%;
  border: 2px solid #fff;
}
.avatar-img {
  width: 100%;
  height: 100%;
  border-radius: 50%;
  border: 2px solid #fff;
  object-fit: cover;
  display: block;
}
.user-info {
  flex: 1;
  min-width: 0;
}
.username {
  font-size: 20px;
  font-weight: 700;
  color: #fff;
  margin: 0 0 6px 0;
  letter-spacing: 0.5px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.user-desc {
  font-size: 13px;
  color: rgba(255, 255, 255, 0.85);
  margin: 0;
}

/* ===== 数据统计（半透明白色圆角卡片） ===== */
.user-stats {
  display: flex;
  justify-content: space-between;
  margin-top: 22px;
  gap: 10px;
}
.stat-item {
  flex: 1;
  background: rgba(255, 255, 255, 0.18);
  border-radius: 12px;
  padding: 12px 8px;
  text-align: center;
  backdrop-filter: blur(4px);
}
.stat-num {
  font-size: 18px;
  font-weight: 700;
  color: #fff;
  line-height: 1.2;
}
.stat-label {
  font-size: 12px;
  color: rgba(255, 255, 255, 0.8);
  margin-top: 4px;
}

/* ===== 菜单卡片（圆角 + 柔和阴影） ===== */
.menu-card {
  background: #fff;
  border-radius: 16px;
  margin: 0 16px 14px;
  box-shadow: 0 4px 20px rgba(67, 206, 162, 0.06);
  overflow: hidden;
}
.menu-card :deep(.van-cell-group--inset) {
  margin: 0;
  border-radius: 0;
  background: transparent;
}
.menu-card :deep(.van-cell) {
  padding: 14px 16px;
  font-size: 15px;
  color: #1a1a2e;
  font-weight: 500;
}
.menu-card :deep(.van-cell::after) {
  border-bottom-color: #f2f3f5;
}
.menu-card :deep(.van-cell__left-icon) {
  font-size: 18px;
  color: #fff !important;
  background: linear-gradient(135deg, #43cea2 0%, #185a9d 100%);
  width: 32px;
  height: 32px;
  line-height: 32px;
  text-align: center;
  border-radius: 10px;
  margin-right: 12px;
  box-shadow: 0 2px 8px rgba(67, 206, 162, 0.25);
}
.menu-card :deep(.van-cell__right-icon) {
  color: #c8c9cc;
}

/* ===== 版本信息 ===== */
.version-info {
  text-align: center;
  padding: 20px;
}
.version-info p {
  font-size: 12px;
  color: #969799;
  margin: 0;
}

/* ===== 收藏/分享弹窗 ===== */
.favorites-popup {
  display: flex;
  flex-direction: column;
  height: 100%;
}
.favorites-list {
  flex: 1;
  overflow-y: auto;
  padding: 12px 16px 20px;
}
.fav-card {
  background: #fff;
  border-radius: 16px;
  padding: 16px;
  margin-bottom: 14px;
  box-shadow: 0 4px 20px rgba(67, 206, 162, 0.06);
}
.fav-header {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 10px;
}
.fav-user-info {
  flex: 1;
  min-width: 0;
}
.fav-username {
  font-size: 13px;
  font-weight: 600;
  color: #1a1a2e;
}
.fav-meta {
  font-size: 11px;
  color: #969799;
  margin-top: 2px;
}
.fav-title {
  font-size: 15px;
  font-weight: 700;
  color: #1a1a2e;
  margin-bottom: 6px;
}
.fav-content {
  font-size: 13px;
  color: #646566;
  line-height: 1.6;
  margin-bottom: 10px;
}
.fav-images {
  display: flex;
  flex-wrap: wrap;
  gap: 4px;
}
.share-actions {
  display: flex;
  align-items: center;
  gap: 16px;
  padding-top: 10px;
  margin-top: 4px;
  border-top: 1px solid #f2f3f5;
  font-size: 13px;
  color: #646566;
}
.share-actions .van-button {
  margin-left: auto;
  border-radius: 12px;
}

/* ===== 设置弹窗 ===== */
.settings-popup {
  display: flex;
  flex-direction: column;
  height: 100%;
}
.settings-list {
  flex: 1;
  overflow-y: auto;
  padding: 16px 0 8px;
}
.settings-list :deep(.van-cell-group--inset) {
  margin: 0 16px 14px;
  border-radius: 16px;
  background: #fff;
  box-shadow: 0 4px 20px rgba(67, 206, 162, 0.06);
  overflow: hidden;
}
.settings-list :deep(.van-cell) {
  padding: 14px 16px;
  font-size: 15px;
  color: #1a1a2e;
  font-weight: 500;
}
.settings-list :deep(.van-cell::after) {
  border-bottom-color: #f2f3f5;
}
.settings-list :deep(.van-cell__left-icon) {
  font-size: 18px;
  color: #fff !important;
  background: linear-gradient(135deg, #43cea2 0%, #185a9d 100%);
  width: 32px;
  height: 32px;
  line-height: 32px;
  text-align: center;
  border-radius: 10px;
  margin-right: 12px;
  box-shadow: 0 2px 8px rgba(67, 206, 162, 0.25);
}
.settings-version {
  text-align: center;
  padding: 20px;
}
.settings-version p {
  font-size: 12px;
  color: #969799;
  margin: 0;
}

/* ===== 退出登录按钮（保持 danger plain，增加圆角） ===== */
.logout-btn {
  border-radius: 12px !important;
  font-weight: 600;
  letter-spacing: 1px;
}

/* ===== 弹窗通用（个人资料/密码/反馈） ===== */
.profile-popup,
.pwd-popup,
.feedback-popup {
  display: flex;
  flex-direction: column;
  height: 100%;
}
.profile-body,
.pwd-body,
.feedback-body {
  flex: 1;
  overflow-y: auto;
  padding: 16px 0;
}

/* ===== 输入框（浅灰圆角背景） ===== */
.profile-popup :deep(.van-cell-group--inset),
.pwd-popup :deep(.van-cell-group--inset),
.feedback-popup :deep(.van-cell-group--inset) {
  margin: 0 16px;
  background: transparent;
}
.profile-popup :deep(.van-field),
.pwd-popup :deep(.van-field),
.feedback-popup :deep(.van-field) {
  background: #f7f8fa;
  border-radius: 12px;
  margin-bottom: 10px;
  padding: 12px 14px;
}
.profile-popup :deep(.van-field__label),
.pwd-popup :deep(.van-field__label),
.feedback-popup :deep(.van-field__label) {
  color: #1a1a2e;
  font-weight: 500;
}

/* ===== 个人资料头像区 ===== */
.profile-avatar-wrap {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 16px;
  gap: 8px;
  cursor: pointer;
  user-select: none;
}
.profile-avatar-tip {
  font-size: 12px;
  color: #969799;
}

/* ===== 密码提示 ===== */
.pwd-tip {
  padding: 8px 16px 0;
  font-size: 12px;
  color: #ff976a;
  margin-top: 8px;
}

/* ===== 对话框内容 ===== */
.dialog-content {
  padding: 20px;
  font-size: 14px;
  color: #646566;
  line-height: 1.8;
  text-align: center;
}
.about-content h3 {
  margin: 12px 0 4px;
  color: #1a1a2e;
  font-weight: 700;
}
.about-content p {
  margin: 4px 0;
}
.about-content .copyright {
  margin-top: 16px;
  color: #969799;
  font-size: 12px;
}

/* ===== 我的订单弹窗 ===== */
.orders-popup {
  display: flex;
  flex-direction: column;
  height: 100%;
  padding-bottom: calc(12px + env(safe-area-inset-bottom));
}
.orders-popup :deep(.van-nav-bar__right) {
  font-size: 13px;
  color: #43cea2;
  font-weight: 600;
  padding-right: 16px;
}
.order-cart-count { color: #ff7e5f !important; }
.orders-list {
  padding: 14px 16px;
  display: flex;
  flex-direction: column;
  gap: 12px;
  flex: 1;
  overflow-y: auto;
}
.order-card {
  display: flex;
  gap: 12px;
  padding: 12px;
  background: #fff;
  border-radius: 14px;
  border: 1px solid rgba(67,206,162,0.1);
  box-shadow: 0 2px 10px rgba(67,206,162,0.04);
}
.order-img {
  width: 86px;
  height: 86px;
  border-radius: 10px;
  object-fit: cover;
  flex-shrink: 0;
  background: #eee;
}
.order-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  min-width: 0;
}
.order-title {
  font-size: 15px;
  font-weight: 600;
  color: #1a1a2e;
  line-height: 1.35;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}
.order-meta {
  font-size: 12px;
  color: #878c99;
  margin-top: 6px;
}
.order-bottom {
  margin-top: auto;
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.order-price {
  font-size: 17px;
  font-weight: 700;
  color: #ff6b6b;
}
.order-actions {
  display: flex;
  gap: 8px;
}
.paid-card {
  background: linear-gradient(135deg, #f0fdf4 0%, #ecfeff 100%);
  border-color: rgba(16,185,129,0.2);
}
.cart-card {
  background: linear-gradient(135deg, #fafbff 0%, #fff7ed 100%);
}
.cart-footer {
  position: sticky;
  bottom: 0;
  margin: 14px -16px -14px;
  padding: 14px 16px calc(14px + env(safe-area-inset-bottom));
  background: #fff;
  border-top: 1px solid #ebedf0;
  display: flex;
  justify-content: space-between;
  align-items: center;
  border-radius: 0 0 14px 14px;
}
.cart-total {
  font-size: 14px;
  color: #323233;
}
.cart-total-amount {
  font-size: 22px;
  font-weight: 700;
  color: #ff6b6b;
}
</style>