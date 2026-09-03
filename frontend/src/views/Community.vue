<template>
  <div class="page-container page">
    <div class="page-header">
      <van-nav-bar title="社区" fixed :border="false" />
    </div>

    <div class="page-content">
      <!-- 标签筛选 -->
      <van-tabs v-model:active="activeTab" sticky offset-top="46px">
        <van-tab v-for="tab in tabs" :key="tab" :title="tab" />
      </van-tabs>

      <!-- 发帖入口 -->
      <div class="post-input-card" @click="showPostPopup = true">
        <van-icon name="edit" size="20" color="#fff" />
        <span class="post-input-text">分享你的旅行故事...</span>
        <van-icon name="photo-o" size="20" color="rgba(255, 255, 255, 0.85)" />
      </div>

      <!-- 帖子列表 -->
      <div class="post-list">
        <div v-for="post in filteredPosts" :key="post.id" class="post-card">
          <div class="post-header">
            <van-image round width="36" height="36" :src="post.avatar" :placeholder="defaultAvatar" @error="onAvatarError(post)" />
            <div class="post-user-info">
              <div class="post-username">{{ post.username }}</div>
              <div class="post-meta">{{ post.location }} · {{ post.time }}</div>
            </div>
            <van-tag plain type="primary" size="medium">{{ post.tag }}</van-tag>
          </div>

          <div class="post-title">{{ post.title }}</div>
          <div class="post-content">{{ post.content }}</div>

          <div v-if="post.images && post.images.length" class="post-images">
            <van-image
              v-for="(img, idx) in post.images"
              :key="idx"
              width="32%"
              height="80"
              radius="6"
              fit="cover"
              :src="img"
              @click="previewImage(post.images, idx)"
            />
          </div>

          <div class="post-footer">
            <div class="post-action" @click="toggleLike(post)">
              <van-icon
                :name="post.liked ? 'good-job' : 'good-job-o'"
                :color="post.liked ? '#ee0a24' : '#646566'"
                size="18"
              />
              <span :class="{ 'liked': post.liked }">{{ post.likes }}</span>
            </div>
            <div class="post-action" @click="toggleComment(post)">
              <van-icon name="chat-o" size="18" color="#646566" />
              <span>{{ post.comments }}</span>
            </div>
            <div class="post-action" @click="toggleFavorite(post)">
              <van-icon
                :name="post.favorited ? 'star' : 'star-o'"
                :color="post.favorited ? '#ffa940' : '#646566'"
                size="18"
              />
            </div>
          </div>

          <!-- 评论展开区 -->
          <div v-if="post.showComments" class="comment-section">
            <div v-for="comment in post.commentList" :key="comment.id" class="comment-item">
              <van-image round width="24" height="24" :src="comment.avatar" />
              <div class="comment-body">
                <span class="comment-user">{{ comment.username }}：</span>
                <span class="comment-text">{{ comment.text }}</span>
              </div>
            </div>
            <div class="comment-input">
              <van-field
                v-model="commentText"
                placeholder="写评论..."
                size="small"
                :border="false"
              />
              <van-button size="small" type="primary" round @click="submitComment(post)">发送</van-button>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 发帖弹窗 -->
    <van-popup
      v-model:show="showPostPopup"
      position="bottom"
      round
      :style="{ height: '60%' }"
    >
      <div class="post-popup">
        <van-nav-bar title="发布动态" right-text="发布" @click-right="submitPost" />
        <div class="popup-form">
          <van-field
            v-model="newPost.title"
            label="标题"
            placeholder="给你的旅行故事起个标题"
            :border="false"
          />
          <van-field
            v-model="newPost.content"
            type="textarea"
            placeholder="分享你的旅行经历..."
            rows="4"
            autosize
            :border="false"
          />
          <!-- 图片选择区 -->
          <div class="popup-images">
            <div
              v-for="(url, idx) in postImages"
              :key="idx"
              class="popup-image-item"
            >
              <img :src="url" />
              <van-icon name="cross" class="popup-image-del" @click="removePostImage(idx)" />
            </div>
            <div v-if="postImages.length < 9" class="popup-image-add" @click="postImageInput?.click()">
              <van-icon name="plus" size="24" />
              <span>添加图片</span>
            </div>
            <input
              type="file"
              ref="postImageInput"
              accept="image/*"
              multiple
              style="display:none"
              @change="handlePostImageUpload"
            />
          </div>
        </div>
        <div class="popup-footer">
          <van-radio-group v-model="newPost.tag" direction="horizontal">
            <van-radio name="攻略">攻略</van-radio>
            <van-radio name="游记">游记</van-radio>
            <van-radio name="问答">问答</van-radio>
            <van-radio name="晒图">晒图</van-radio>
          </van-radio-group>
        </div>
      </div>
    </van-popup>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { showToast, showLoadingToast, closeToast } from 'vant'
import { addFavorite, removeFavorite, createShare, likeShare, incrementComment, getAllShares, getFavorites, getUserInfo, getComments, createComment, uploadImage } from '../api/index'

const activeTab = ref(0)
const tabs = ['推荐', '攻略', '游记', '问答', '晒图']
const showPostPopup = ref(false)
const commentText = ref('')

const newPost = reactive({
  title: '',
  content: '',
  tag: '攻略'
})

/** 发帖图片：已上传到服务器的 URL 列表（最终存数据库 images 字段） */
const postImages = ref([])
/** 发帖图片选择 input ref */
const postImageInput = ref(null)

/** 默认头像（所有帖子作者无头像时用） */
const defaultAvatar = 'https://api.dicebear.com/7.x/avataaars/svg?seed=tour'

const posts = ref([])

/** 头像加载失败 → 回退到默认头像 */
const onAvatarError = (post) => {
  if (post.avatar !== defaultAvatar) {
    post.avatar = defaultAvatar
  }
}

/** 解析图片字符串 */
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
  if (!t) return '刚刚'
  const d = new Date(t)
  const now = new Date()
  const diff = Math.floor((now - d) / 1000)
  if (diff < 60) return '刚刚'
  if (diff < 3600) return Math.floor(diff / 60) + '分钟前'
  if (diff < 86400) return Math.floor(diff / 3600) + '小时前'
  if (diff < 604800) return Math.floor(diff / 86400) + '天前'
  return d.toLocaleDateString()
}

/** 当前用户信息（响应式） */
const currentUser = ref({})

/** 从后端同步用户信息 */
const syncCurrentUser = async () => {
  // 先读 localStorage 快速展示
  try {
    const u = JSON.parse(localStorage.getItem('userInfo') || '{}')
    if (u && u.username) currentUser.value = u
  } catch (e) { /* ignore */ }
  // 再从后端获取最新数据（含数据库头像）
  try {
    const res = await getUserInfo()
    if (res.code === 200 && res.data) {
      // 如果数据库头像为空，保留 localStorage 中的头像
      if ((!res.data.avatar || res.data.avatar === 'null') && currentUser.value.avatar) {
        res.data.avatar = currentUser.value.avatar
      }
      currentUser.value = res.data
      localStorage.setItem('userInfo', JSON.stringify(res.data))
    }
  } catch (e) { /* 后端不可用时用 localStorage */ }
}

/** 获取当前用户信息（兼容旧代码调用） */
const getCurrentUser = () => currentUser.value

/** 加载帖子列表（只从后端拿数据，彻底去掉 localStorage 兜底和假数据） */
const loadPosts = async () => {
  try {
    const res = await getAllShares()
    if (res && res.code === 200 && Array.isArray(res.data)) {
      posts.value = res.data.map(s => {
        // MyBatis 返回 Map 的 key 是下划线命名（post_username, post_avatar）
        const postUsername = s.postUsername ?? s.post_username ?? (s.username ? s.username + '(我)' : '用户' + s.userId)
        const postAvatar = (s.postAvatar ?? s.post_avatar ?? s.avatar) || defaultAvatar
        return {
          id: s.id,
          userId: s.userId,
          username: postUsername,
          avatar: postAvatar,
          location: '',
          time: formatTime(s.createTime),
          tag: s.tag || '攻略',
          title: s.title || '',
          content: s.content || '',
          images: parseImages(s.images),
          likes: s.likes || 0,
          comments: s.comments || 0,
          liked: !!s.liked,
          favorited: !!s.favorited,
          showComments: false,
          commentList: []
        }
      })
    } else {
      posts.value = []
    }
  } catch (e) {
    console.error('=== [社区] 加载帖子失败 ===', e)
    posts.value = []
  }
}

onMounted(async () => {
  await syncCurrentUser()
  await loadPosts()
})

const filteredPosts = computed(() => {
  if (activeTab.value === 0) return posts.value
  return posts.value.filter(p => p.tag === tabs[activeTab.value])
})

const toggleLike = async (post) => {
  const nextLiked = !post.liked
  // 乐观更新 UI
  post.liked = nextLiked
  post.likes += nextLiked ? 1 : -1
  try {
    const res = await likeShare({ shareId: post.id, liked: nextLiked ? 1 : 0 })
    if (res && res.code === 200 && res.data) {
      // ✅ 用后端返回值确认真实状态（防止重复点赞/后端拦截）
      post.likes = res.data.likes
      post.liked = res.data.liked
    }
  } catch (e) {
    // 后端不可用时回滚 UI
    post.liked = !nextLiked
    post.likes -= nextLiked ? 1 : -1
    showToast('操作失败，请稍后重试')
  }
}

const toggleFavorite = async (post) => {
  const nextFav = !post.favorited
  post.favorited = nextFav // 乐观更新 UI
  const favData = {
    postId: post.id,
    title: post.title,
    content: post.content,
    images: JSON.stringify(post.images || []),
    tag: post.tag,
    username: post.username,
    avatar: post.avatar,
    location: post.location
  }
  try {
    if (nextFav) {
      const res = await addFavorite(favData)
      if (res && res.code === 200) {
        showToast('已收藏')
      } else {
        post.favorited = !nextFav // 失败回滚
        showToast(res?.message || '收藏失败')
      }
    } else {
      const res = await removeFavorite({ postId: post.id })
      if (res && res.code === 200) {
        showToast('取消收藏')
      } else {
        post.favorited = !nextFav // 失败回滚
        showToast(res?.message || '取消失败')
      }
    }
  } catch (e) {
    post.favorited = !nextFav // 网络错误回滚
    showToast('网络错误，请稍后重试')
  }
}

const toggleComment = async (post) => {
  post.showComments = !post.showComments
  if (post.showComments && (!post.commentList || post.commentList.length === 0)) {
    // 打开时从后端加载评论（如果还没加载过）
    try {
      const res = await getComments(post.id)
      if (res && res.code === 200 && Array.isArray(res.data)) {
        post.commentList = res.data.map(c => ({
          id: c.id,
          username: c.username || '匿名',
          avatar: c.avatar || '',
          text: c.text || '',
          createTime: c.createTime
        }))
      }
    } catch (e) {
      console.warn('=== [评论] 加载失败 ===', e)
    }
  }
}

const submitComment = async (post) => {
  if (!commentText.value.trim()) {
    showToast('请输入评论内容')
    return
  }
  const text = commentText.value.trim()
  try {
    const res = await createComment({ shareId: post.id, text })
    if (res && res.code === 200) {
      // ✅ 持久化成功后，把评论加到本地列表 + 评论数+1
      const u = JSON.parse(localStorage.getItem('userInfo') || '{}')
      post.commentList.push({
        id: res.data?.id || Date.now(),
        username: u.username || '我',
        avatar: u.avatar || '',
        text
      })
      post.comments++
      commentText.value = ''
      showToast('评论成功')
    } else {
      showToast(res?.message || '评论失败')
    }
  } catch (e) {
    console.warn('=== [评论] 后端不可用，仅本地展示 ===', e)
    // 后端不可用时降级：本地展示但不持久化
    const u = JSON.parse(localStorage.getItem('userInfo') || '{}')
    post.commentList.push({
      id: Date.now(),
      username: u.username || '我',
      avatar: u.avatar || '',
      text
    })
    post.comments++
    commentText.value = ''
    showToast('评论成功（本地）')
  }
}

/** 发帖图片：选择后立即上传到服务器，拿到 URL 存进 postImages */
const handlePostImageUpload = async (event) => {
  const files = Array.from(event.target.files || [])
  if (files.length === 0) return
  // 校验数量
  if (postImages.value.length + files.length > 9) {
    showToast('最多 9 张图片')
    event.target.value = ''
    return
  }
  showLoadingToast({ message: '上传中...', forbidClick: true, duration: 0 })
  try {
    for (const file of files) {
      if (!file.type.startsWith('image/')) {
        showToast('只能上传图片')
        continue
      }
      if (file.size > 10 * 1024 * 1024) {
        showToast('单张图片不能超过 10MB')
        continue
      }
      const res = await uploadImage(file)
      if (res && res.code === 200 && res.data?.url) {
        postImages.value.push(res.data.url)
      } else {
        showToast(res?.message || '上传失败')
      }
    }
  } finally {
    closeToast()
    event.target.value = ''
  }
}

const removePostImage = (idx) => {
  postImages.value.splice(idx, 1)
}

const submitPost = async () => {
  if (!newPost.title.trim() || !newPost.content.trim()) {
    showToast('请填写标题和内容')
    return
  }
  const token = localStorage.getItem('token')
  if (!token) {
    showToast('请先登录后发布')
    return
  }
  const postData = {
    title: newPost.title,
    content: newPost.content,
    tag: newPost.tag,
    images: JSON.stringify(postImages.value)
  }
  try {
    showLoadingToast({ message: '发布中...', forbidClick: true, duration: 0 })
    const res = await createShare(postData)
    closeToast()
    if (res && res.code === 200) {
      // 发布成功 → 重新拉取社区列表（从数据库拿最新数据）
      await loadPosts()
      showToast('发布成功')
    } else {
      showToast(res?.message || '发布失败')
    }
  } catch (e) {
    closeToast()
    console.error('=== [社区] 发布失败 ===', e)
    showToast('发布失败，请重试')
    return
  }
  // 重置表单
  newPost.title = ''
  newPost.content = ''
  newPost.tag = '攻略'
  postImages.value = []
  showPostPopup.value = false
  activeTab.value = 0
}

const previewImage = (images, index) => {
  // 简单提示，可接 ImagePreview 组件
  showToast(`图片 ${index + 1}/${images.length}`)
}
</script>

<style scoped>
/* ===== 页面容器：顶部叠加品牌色淡渐变 ===== */
.page-container {
  min-height: 100vh;
  background-color: #f5f6fa;
  background-image: linear-gradient(180deg, rgba(67, 206, 162, 0.06) 0%, rgba(67, 206, 162, 0) 180px);
  padding-bottom: 70px;
}
.page-content {
  padding: 12px;
}
.page-header :deep(.van-nav-bar__title) {
  font-weight: 700;
  color: #1a1a2e;
}

/* ===== Tab 标签栏：选中态渐变 / 未选浅灰 pill ===== */
:deep(.van-tabs) {
  margin-bottom: 14px;
}
:deep(.van-tabs__wrap) {
  height: 44px;
  background: #f5f6fa;
  overflow: visible;
}
:deep(.van-tabs__nav) {
  background: transparent;
  gap: 8px;
  padding: 0 12px;
}
:deep(.van-tab) {
  flex: 1;
  padding: 0 10px;
  font-size: 13px;
  color: #646566;
  background: #eef0f4;
  border-radius: 22px;
  transition: all 0.3s ease;
  z-index: 1;
}
:deep(.van-tab.van-tab--active) {
  color: #fff;
  font-weight: 700;
  background: linear-gradient(135deg, #43cea2 0%, #185a9d 100%);
  box-shadow: 0 4px 12px rgba(67, 206, 162, 0.28);
}
:deep(.van-tabs__line) {
  display: none !important;
}

/* ===== 顶部发帖入口：渐变圆角卡片 ===== */
.post-input-card {
  display: flex;
  align-items: center;
  gap: 10px;
  background: linear-gradient(135deg, #43cea2 0%, #185a9d 100%);
  border-radius: 16px;
  padding: 14px 16px;
  margin-bottom: 14px;
  box-shadow: 0 6px 20px rgba(67, 206, 162, 0.22);
  cursor: pointer;
  transition: transform 0.2s;
}
.post-input-card:active {
  transform: scale(0.98);
}
.post-input-text {
  flex: 1;
  font-size: 14px;
  color: rgba(255, 255, 255, 0.92);
  letter-spacing: 0.3px;
}

/* ===== 帖子列表 ===== */
.post-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

/* ===== 帖子卡片：大圆角 + 柔和阴影 ===== */
.post-card {
  background: #fff;
  border-radius: 16px;
  padding: 16px;
  box-shadow: 0 4px 20px rgba(67, 206, 162, 0.06);
  transition: transform 0.2s;
}
.post-card:active {
  transform: scale(0.99);
}

/* ===== 帖子头部：精致头像区 ===== */
.post-header {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 12px;
}
.post-user-info {
  flex: 1;
  min-width: 0;
}
.post-username {
  font-size: 14px;
  font-weight: 700;
  color: #1a1a2e;
}
.post-meta {
  font-size: 12px;
  color: #969799;
  margin-top: 3px;
}
/* 头像：品牌色描边 + 柔和阴影 */
:deep(.post-header .van-image) {
  border: 2px solid rgba(67, 206, 162, 0.18);
  box-shadow: 0 2px 8px rgba(67, 206, 162, 0.14);
  flex-shrink: 0;
}
/* 标签：圆角 pill 样式 */
:deep(.post-header .van-tag) {
  padding: 2px 10px;
  border-radius: 20px !important;
  font-size: 11px;
  background: rgba(67, 206, 162, 0.1) !important;
  color: #43cea2 !important;
  border: none !important;
}

.post-title {
  font-size: 16px;
  font-weight: 700;
  color: #1a1a2e;
  margin-bottom: 8px;
  letter-spacing: 0.3px;
}
.post-content {
  font-size: 14px;
  color: #646566;
  line-height: 1.6;
  margin-bottom: 12px;
}

/* ===== 帖子图片：圆角处理 ===== */
.post-images {
  display: flex;
  flex-wrap: wrap;
  gap: 4px;
  margin-bottom: 12px;
}
:deep(.post-images .van-image) {
  border-radius: 12px;
  overflow: hidden;
}

/* ===== 点赞 / 评论 / 收藏：增大间距、图标更突出 ===== */
.post-footer {
  display: flex;
  gap: 28px;
  padding-top: 12px;
  border-top: 1px solid #f5f6fa;
}
.post-action {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  color: #646566;
  cursor: pointer;
  padding: 4px 2px;
  transition: transform 0.15s;
}
.post-action:active {
  transform: scale(0.9);
}
.post-action .liked {
  color: #ee0a24;
  font-weight: 600;
}

/* ===== 评论列表：清晰层级关系 ===== */
.comment-section {
  margin-top: 12px;
  padding: 12px;
  background: #f7f8fa;
  border-radius: 12px;
}
.comment-item {
  display: flex;
  align-items: flex-start;
  gap: 8px;
  padding: 6px 0;
}
:deep(.comment-item .van-image) {
  border: 1px solid rgba(67, 206, 162, 0.12);
  flex-shrink: 0;
}
.comment-body {
  flex: 1;
  font-size: 13px;
  color: #646566;
  line-height: 1.6;
  background: #fff;
  padding: 8px 12px;
  border-radius: 10px;
}
.comment-user {
  color: #43cea2;
  font-weight: 600;
}
.comment-input {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-top: 10px;
}
.comment-input :deep(.van-field) {
  flex: 1;
  background: #fff;
  border-radius: 16px;
  padding: 6px 12px;
}
.comment-input :deep(.van-button) {
  background: linear-gradient(135deg, #43cea2 0%, #185a9d 100%) !important;
  border: none !important;
  padding: 0 16px;
  box-shadow: 0 2px 8px rgba(67, 206, 162, 0.28);
}

/* ===== 发帖弹窗 ===== */
.post-popup {
  display: flex;
  flex-direction: column;
  height: 100%;
  background: #fff;
  padding-bottom: 12px;
}
:deep(.post-popup .van-nav-bar) {
  border-bottom: 1px solid #f5f6fa;
}
:deep(.post-popup .van-nav-bar__title) {
  font-weight: 700;
  color: #1a1a2e;
}
/* 发布按钮：渐变 pill */
:deep(.post-popup .van-nav-bar__text) {
  color: #fff !important;
  background: linear-gradient(135deg, #43cea2 0%, #185a9d 100%);
  padding: 6px 18px;
  border-radius: 20px;
  font-size: 13px;
  font-weight: 600;
  box-shadow: 0 2px 8px rgba(67, 206, 162, 0.3);
}
/* 弹窗输入框：浅灰圆角背景 */
.popup-form {
  padding: 12px 16px 0;
}
:deep(.popup-form .van-field) {
  background: #f7f8fa;
  border-radius: 12px;
  margin-bottom: 10px;
  padding: 10px 12px;
}
:deep(.popup-form .van-field__label) {
  color: #1a1a2e;
  font-weight: 600;
}
.popup-images {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  padding: 8px 16px;
}
.popup-image-item {
  position: relative;
  width: 72px;
  height: 72px;
  border-radius: 6px;
  overflow: hidden;
}
.popup-image-item img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}
.popup-image-del {
  position: absolute;
  top: 2px;
  right: 2px;
  width: 18px;
  height: 18px;
  line-height: 18px;
  text-align: center;
  background: rgba(0,0,0,0.6);
  color: #fff;
  border-radius: 50%;
  font-size: 12px;
  cursor: pointer;
}
.popup-image-add {
  width: 72px;
  height: 72px;
  border: 1px dashed #dcdee0;
  border-radius: 6px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: #969799;
  font-size: 12px;
  cursor: pointer;
  gap: 4px;
}
.popup-footer {
  padding: 16px;
}
.popup-footer :deep(.van-radio-group) {
  gap: 16px;
}
.popup-footer :deep(.van-radio__label) {
  color: #646566;
  font-size: 13px;
}
</style>
