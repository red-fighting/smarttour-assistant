<template>
  <div class="page-container page">
    <!-- 顶部欢迎栏 -->
    <div class="welcome-bar">
      <div class="welcome-left">
        <div class="welcome-greeting">{{ greetingText }}，{{ username }}</div>
        <div class="welcome-subtitle">开启你的旅行计划 ✈️</div>
      </div>
      <div class="welcome-right" @click="goProfile">
        <van-image round width="44" height="44" :src="userAvatar" />
      </div>
    </div>

    <!-- 美景轮播图 -->
    <div class="banner-wrapper">
      <van-swipe class="banner" :autoplay="3000" :show-indicators="true" :init-swipe="bannerInit">
        <van-swipe-item v-for="(img, idx) in bannerImages" :key="idx">
          <img :src="img" class="banner-img" />
          <div class="banner-overlay">
            <div class="banner-title">{{ bannerTitles[idx] }}</div>
            <div class="banner-subtitle">{{ bannerSubtitles[idx] }}</div>
          </div>
        </van-swipe-item>
      </van-swipe>
    </div>

    <!-- 行程规划卡片 -->
    <div class="card plan-card">
      <div class="section-title">
        <van-icon name="location-o" size="20" color="#43cea2" />
        <span>规划你的旅程</span>
      </div>
      <van-field
        label="起始地"
        is-link
        readonly
        @click="showOriginPicker = true"
        v-model="formData.origin"
        placeholder="请选择起始地"
        class="form-field"
      />
      <van-field
        label="目的地"
        is-link
        readonly
        @click="showCityPicker = true"
        v-model="formData.city"
        placeholder="请选择目的地"
        class="form-field"
      />
      <van-field
        label="预算"
        v-model="formData.budget"
        placeholder="请输入预算（元）"
        class="form-field"
      />
      <div class="date-range-row">
        <van-field
          label="出发日期"
          type="date"
          v-model="formData.startDate"
          :min="minDateObj"
          class="form-field date-field"
        />
        <van-field
          label="返回日期"
          type="date"
          v-model="formData.endDate"
          :min="formData.startDate ? new Date(formData.startDate) : minDateObj"
          class="form-field date-field"
        />
      </div>
      <div v-if="formData.startDate && formData.endDate" class="days-hint">
        共 <strong>{{ computedDays }}</strong> 天
      </div>
      <div class="plan-btn-row">
        <van-button type="primary" size="large" round class="plan-btn plan-btn--primary" :loading="loading" @click="handleSubmit">
          开始AI规划
        </van-button>
        <van-button size="large" round class="plan-btn plan-btn--ghost" :loading="searchLoading" @click="handleSearchOrder">
          查询合适产品
        </van-button>
      </div>
    </div>

    <!-- 查询结果弹窗 -->
    <van-popup v-model:show="showSearchResult" position="bottom" round :style="{ height: '70%' }">
      <div class="search-result-header">
        <span>查询结果（{{ searchResults.length }} 条）</span>
        <van-icon name="cross" size="22" @click="showSearchResult = false" />
      </div>
      <div v-if="searchResults.length === 0" class="empty-hint">
        <van-empty description="没有匹配的产品，试试放宽条件" />
      </div>
      <div v-else class="search-result-list">
        <div
          v-for="product in searchResults"
          :key="product.id"
          class="product-card"
          @click="showSearchResult = false; openProductDetail(product)"
        >
          <img :src="product.image" class="product-img" @error="onProductImgError(product)" />
          <div class="product-info">
            <div class="product-title">{{ product.title }}</div>
            <div class="product-meta">
              <span>{{ product.origin || '?' }}→{{ product.destination }}</span>
              <span class="divider">·</span>
              <span>{{ product.duration }}</span>
            </div>
            <div class="product-bottom">
              <span class="product-price">¥{{ formatPrice(product.price) }}</span>
              <span class="product-sold">已售 {{ product.soldCount || 0 }}</span>
            </div>
          </div>
        </div>
      </div>
    </van-popup>

    <!-- 热门目的地 -->
    <div class="card">
      <div class="section-title">
        <van-icon name="fire-o" size="20" color="#ff6b6b" />
        <span>热门目的地</span>
      </div>
      <van-grid :gutter="10" :column-num="4">
        <van-grid-item @click="selectCity(city)" v-for="city in hotCities" :key="city">
          <div class="city-tag" :class="{'active': formData.city === city}">{{ city }}</div>
        </van-grid-item>
      </van-grid>
    </div>

    <!-- 发现世界 旅游商品 -->
    <div class="card discover-card">
      <div class="section-title">
        <van-icon name="hot-o" size="20" color="#ff7e5f" />
        <span>发现世界</span>
      </div>
      <div class="product-list">
        <div
          v-for="product in products"
          :key="product.id"
          class="product-card"
          @click="openProductDetail(product)"
        >
          <img :src="product.image" class="product-img" @error="onProductImgError(product)" />
          <div class="product-info">
            <div class="product-title">{{ product.title }}</div>
            <div class="product-meta">
              <span>{{ product.destination }}</span>
              <span class="divider">·</span>
              <span>已售：{{ product.soldCount || 0 }}</span>
            </div>
            <div class="product-bottom">
              <span class="product-price">¥{{ formatPrice(product.price) }}</span>
              <span class="add-cart-btn" @click.stop="handleAddCart(product)">
                <van-icon name="cart-o" size="20" color="#ff7e5f" />
              </span>
            </div>
          </div>
        </div>
      </div>
      <van-empty v-if="!products.length && !productLoading" description="暂无旅游商品" />
    </div>

    <!-- 起始地选择弹窗 -->
    <van-popup round v-model:show="showOriginPicker" position="bottom">
      <van-picker
        title="选择起始地"
        :columns="columns"
        @confirm="onOriginConfirm"
        @cancel="onCancel"
        @change="onChange"
      />
    </van-popup>

    <!-- 目的地选择弹窗 -->
    <van-popup round v-model:show="showCityPicker" position="bottom">
      <van-picker
        title="选择目的地"
        :columns="columns"
        @confirm="onConfirm"
        @cancel="onCancel"
        @change="onChange"
      />
    </van-popup>

    <!-- 商品详情弹窗 -->
    <van-popup v-model:show="showDetail" round position="bottom" :style="{ height: '85%' }">
      <div class="detail-popup" v-if="detail">
        <img :src="detail.image" class="detail-img" @error="onDetailImgError" />
        <div class="detail-header">
          <div class="detail-title">{{ detail.title }}</div>
          <div class="detail-price">¥{{ formatPrice(detail.price) }}</div>
        </div>
        <div class="detail-tags">
          <van-tag type="primary" plain>出发地：{{ detail.origin || '待定' }}</van-tag>
          <van-tag type="success" plain>目的地：{{ detail.destination }}</van-tag>
          <van-tag type="warning" plain>行程：{{ detail.duration }}</van-tag>
          <van-tag type="danger" plain>已售：{{ detail.soldCount || 0 }}</van-tag>
        </div>

        <div class="detail-section">
          <div class="detail-section-title">📅 详细路线</div>
          <div class="detail-section-body">{{ detail.routeDesc || '暂无详细路线介绍' }}</div>
        </div>

        <div class="detail-section">
          <div class="detail-section-title">🍜 美食特色</div>
          <div class="detail-section-body">{{ detail.foodFeature || '暂无美食特色介绍' }}</div>
        </div>

        <div class="detail-section">
          <div class="detail-section-title">🧑‍🏫 导游信息</div>
          <div class="detail-section-body guide-info">
            <div>导游姓名：{{ detail.guideName || '待定' }}</div>
            <div>联系电话：{{ detail.guidePhone || '待定' }}</div>
          </div>
        </div>

        <div class="detail-bottom-bar">
          <van-button icon="cart-o" type="warning" block round @click="handleAddCart(detail)">
            加入购物车
          </van-button>
        </div>
      </div>
      <van-loading v-else type="spinner" color="#43cea2" />
    </van-popup>
  </div>
</template>

<script setup>
import { reactive, ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { showToast } from 'vant'
import { recommendTravel, getUserInfo, getProductList, addToCart, searchProducts, getBannerList } from '../api/index'

const router = useRouter()

// ================= 轮播图（接口获取，失败回退默认） =================
const bannerImages = ref([
  'https://picsum.photos/seed/banner_jiuzhaigou/1200/500',
  'https://picsum.photos/seed/banner_zhangjiajie/1200/500',
  'https://picsum.photos/seed/banner_xihu/1200/500',
  'https://picsum.photos/seed/banner_huangshan/1200/500'
])
const bannerTitles = ref(['九寨沟自然风景区', '张家界国家森林公园', '西湖风景名胜区', '黄山风景区'])
const bannerSubtitles = ref(['四川 · 世界自然遗产', '湖南 · 阿凡达取景地', '浙江 · 人间天堂', '安徽 · 天下第一奇山'])
const bannerInit = (swiper) => { swiper.on('change', () => {}) }

const loadBanners = async () => {
  try {
    const res = await getBannerList()
    if (res.code === 200 && Array.isArray(res.data) && res.data.length > 0) {
      bannerImages.value = res.data.map(b => b.imageUrl)
      bannerTitles.value = res.data.map(b => b.title || '')
      bannerSubtitles.value = res.data.map(b => b.subtitle || '')
    }
  } catch (e) { /* 接口异常时保留默认轮播 */ }
}

// ================= 欢迎栏 =================
const greetingText = (() => {
  const h = new Date().getHours()
  if (h < 6) return '凌晨好'
  if (h < 12) return '早上好'
  if (h < 14) return '中午好'
  if (h < 18) return '下午好'
  return '晚上好'
})()
const _userInfo = JSON.parse(localStorage.getItem('userInfo') || '{}')
const username = ref(_userInfo?.username || '旅行者')
const userAvatar = ref(_userInfo?.avatar || 'https://fastly.jsdelivr.net/npm/@vant/assets/cat.jpeg')
const defaultImg = 'https://fastly.jsdelivr.net/npm/@vant/assets/cat.jpeg'

const goProfile = () => router.push('/profile')

onMounted(async () => {
  loadBanners()
  try {
    const res = await getUserInfo()
    if (res.code === 200 && res.data) {
      username.value = res.data.username || username.value
      if (res.data.avatar && res.data.avatar !== 'null') {
        userAvatar.value = res.data.avatar
      }
      localStorage.setItem('userInfo', JSON.stringify({
        ...res.data, avatar: userAvatar.value
      }))
    }
  } catch (e) { /* use localStorage fallback */ }
  loadProducts()
})

// ================= 行程规划 =================
const showCityPicker = ref(false)
const showOriginPicker = ref(false)
const today = new Date()
const fmtDate = (d) => d.toISOString().slice(0, 10)
const minDateObj = new Date(today)           // 给 van-field type=date 用（Date 对象）
const minDate = fmtDate(today)               // 给字符串兜底用
const formData = reactive({ origin: '', city: '', budget: '', startDate: '', endDate: '' })
const allCities = ['北京','上海','广州','深圳','成都','西安','重庆','杭州','南京','天津','武汉','长沙','昆明','兰州','乌鲁木齐']
const hotCities = ['北京','上海','广州','深圳','成都','西安','重庆','杭州']
const loading = ref(false)
const searchLoading = ref(false)
const columns = allCities.map(city => ({ text: city, value: city }))
const computedDays = computed(() => {
  if (!formData.startDate || !formData.endDate) return 0
  const s = new Date(formData.startDate)
  const e = new Date(formData.endDate)
  const diff = Math.round((e - s) / (1000 * 60 * 60 * 24))
  if (diff < 0) return 0   // 返回早于出发
  return diff + 1          // 含首尾
})
const onOriginConfirm = ({ selectedOptions }) => { formData.origin = selectedOptions[0].value; showOriginPicker.value = false }
const onConfirm = ({ selectedOptions }) => { formData.city = selectedOptions[0].value; showCityPicker.value = false }
const onCancel = () => { showCityPicker.value = false; showOriginPicker.value = false }
const onChange = () => {}
const selectCity = (city) => { formData.city = city }

const handleSubmit = async () => {
  if (!formData.city) { showToast('请选择目的地'); return }
  const budget = Number(formData.budget)
  if (isNaN(budget) || budget <= 100) { showToast('请输入大于等于100的预算'); return }
  const days = computedDays.value
  if (!formData.startDate || !formData.endDate) { showToast('请选择出发和返回日期'); return }
  if (days < 2) { showToast('行程至少需要2天'); return }
  try {
    loading.value = true
    const res = await recommendTravel({ city: formData.city, budget, days })
    if (res && res.data) {
      router.push({ path: '/detail', state: { recommendData: res.data } })
    } else {
      showToast('获取推荐失败，请重试')
    }
  } catch (err) {
    showToast(err.message || '获取推荐失败，请重试')
  } finally { loading.value = false }
}

// ================= 查询合适产品 =================
const showSearchResult = ref(false)
const searchResults = ref([])
const handleSearchOrder = async () => {
  // 允许部分条件为空，但至少要有一个
  const params = {}
  if (formData.origin) params.origin = formData.origin
  if (formData.city) params.destination = formData.city
  if (formData.budget && Number(formData.budget) > 0) params.budget = Number(formData.budget)
  const days = computedDays.value
  if (days > 0) params.duration = days
  if (Object.keys(params).length === 0) {
    showToast('请至少填写一个筛选条件')
    return
  }
  try {
    searchLoading.value = true
    const res = await searchProducts(params)
    if (res.code === 200) {
      searchResults.value = res.data || []
      showSearchResult.value = true
      if (searchResults.value.length === 0) {
        showToast('没有匹配的产品')
      }
    } else {
      showToast(res.message || '查询失败')
    }
  } catch (err) {
    showToast('查询失败，请重试')
  } finally { searchLoading.value = false }
}

// ================= 发现世界 商品列表 =================
const products = ref([])
const productLoading = ref(false)
const loadProducts = async () => {
  productLoading.value = true
  try {
    const res = await getProductList()
    if (res.code === 200 && Array.isArray(res.data)) {
      products.value = res.data
    }
  } catch (e) {
    console.warn('商品列表加载失败:', e)
  } finally {
    productLoading.value = false
  }
}
const formatPrice = (p) => {
  const n = Number(p)
  return isNaN(n) ? '0' : n.toFixed(2)
}
const onProductImgError = (p) => { if (p.image !== defaultImg) p.image = defaultImg }

// ================= 商品详情弹窗 =================
const showDetail = ref(false)
const detail = ref(null)
const openProductDetail = async (product) => {
  detail.value = product // 先显示概要
  showDetail.value = true
  // 尝试加载详情（拿 routeDesc / foodFeature / guide 等完整字段）
  try {
    const res = await getProductList()
    if (res.code === 200) {
      const full = res.data.find(x => x.id === product.id)
      if (full) detail.value = full
    }
  } catch (e) { /* use existing */ }
}
const onDetailImgError = () => { if (detail.value && detail.value.image !== defaultImg) detail.value.image = defaultImg }

// ================= 加入购物车 =================
/** 从 product.duration 提取天数："3天2晚" → 3 */
const extractDays = (duration) => {
  if (!duration) return 0
  const m = duration.match(/(\d+)/)
  return m ? Number(m[1]) : 0
}
const handleAddCart = async (product) => {
  const token = localStorage.getItem('token')
  if (!token) { showToast('请先登录'); router.push('/login'); return }

  // 校验 1：出发日期 + 返回日期必须填写
  if (!formData.startDate || !formData.endDate) {
    showToast('请先在规划卡片选择出发日期和返回日期')
    return
  }

  // 校验 2：起始地匹配（如果用户填了起始地）
  if (formData.origin) {
    const productOrigin = product.origin || ''
    if (!productOrigin.includes(formData.origin)) {
      showToast(`起始地不匹配！产品起始地：${productOrigin}`)
      return
    }
  }

  // 校验 3：目的地匹配（product.destination 带省前缀如 "四川省 九寨沟"，formData.city 是纯城市名）
  if (formData.city) {
    const productDest = product.destination || ''
    if (!productDest.includes(formData.city)) {
      showToast(`目的地不匹配！产品目的地：${productDest}`)
      return
    }
  }

  // 校验 4：天数匹配（严格相等——用户说"天数一样就能选择"）
  const userDays = computedDays.value
  const productDays = extractDays(product.duration)
  if (userDays > 0 && productDays > 0 && userDays !== productDays) {
    showToast(`天数不匹配！产品行程：${product.duration}（${productDays}天），您选了${userDays}天`)
    return
  }

  // 全部校验通过 → 带日期调 addToCart
  try {
    const addParams = {
      productId: product.id,
      quantity: 1,
      startDate: formData.startDate,
      endDate: formData.endDate
    }
    const res = await addToCart(addParams)
    if (res.code === 200) {
      showToast('已加入购物车 🛒')
    } else {
      showToast(res.message || '加入失败')
    }
  } catch (e) {
    console.warn('加入购物车失败:', e)
    showToast('加入购物车失败，请重试')
  }
}
</script>

<style scoped>
/* ===== 顶部欢迎栏 ===== */
.welcome-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 14px 16px 18px;
  background: linear-gradient(135deg, #43cea2 0%, #185a9d 100%);
  border-radius: 0 0 20px 20px;
}
.welcome-left { display: flex; flex-direction: column; gap: 4px; }
.welcome-greeting { color: #fff; font-size: 18px; font-weight: 700; letter-spacing: 0.5px; }
.welcome-subtitle { color: rgba(255,255,255,0.85); font-size: 13px; }
.welcome-right { flex-shrink: 0; border: 3px solid rgba(255,255,255,0.5); border-radius: 50%; overflow: hidden; box-shadow: 0 2px 10px rgba(0,0,0,0.15); }

/* ===== 轮播图 ===== */
.banner-wrapper { margin: 14px 16px 0; border-radius: 16px; overflow: hidden; box-shadow: 0 6px 20px rgba(67,206,162,0.15); position: relative; }
.banner { height: 170px; }
.banner-img { width: 100%; height: 100%; object-fit: cover; display: block; }
.banner-overlay { position: absolute; left: 0; right: 0; bottom: 0; padding: 20px 16px 16px; background: linear-gradient(transparent, rgba(0,0,0,0.5)); color: #fff; }
.banner-title { font-size: 17px; font-weight: 700; letter-spacing: 0.5px; margin-bottom: 4px; }
.banner-subtitle { font-size: 12px; opacity: 0.9; }
.banner-wrapper :deep(.van-swipe__indicator) { background: rgba(255,255,255,0.5); width: 18px; }
.banner-wrapper :deep(.van-swipe__indicator--active) { background: #fff; width: 24px; }

/* ===== 卡片通用 ===== */
.card { background: #fff; border-radius: 16px; padding: 20px 16px; margin: 14px 16px 0; box-shadow: 0 4px 20px rgba(67,206,162,0.06); }

/* ===== Section 标题 ===== */
.section-title { display: flex; align-items: center; gap: 6px; font-size: 17px; font-weight: 700; color: #1a1a2e; margin-bottom: 16px; }
.section-title span { letter-spacing: 0.5px; }

/* ===== 表单字段 ===== */
.form-field { background: #f7f8fa !important; border-radius: 10px !important; margin-bottom: 10px; }

/* ===== 渐变按钮 ===== */
.plan-btn { background: linear-gradient(135deg, #43cea2 0%, #185a9d 100%) !important; border: none !important; font-size: 16px; font-weight: 600; letter-spacing: 1px; box-shadow: 0 4px 16px rgba(67,206,162,0.3); flex: 1; }
.plan-btn-row { display: flex; gap: 12px; margin-top: 8px; }
.plan-btn--ghost { background: #fff !important; color: #185a9d !important; border: 1.5px solid #185a9d !important; box-shadow: none; font-size: 15px; }

/* ===== 日期范围行 ===== */
.date-range-row { display: flex; gap: 8px; }
.date-field { flex: 1; }
.date-field :deep(.van-field__label) { font-size: 14px; }
.days-hint { text-align: center; padding: 6px 0 4px; font-size: 13px; color: #646566; }
.days-hint strong { color: #43cea2; font-size: 16px; margin: 0 3px; }
.search-result-header { display: flex; justify-content: space-between; align-items: center; padding: 16px 18px; font-weight: 600; font-size: 16px; border-bottom: 1px solid #f0f0f0; }
.search-result-list { padding: 10px 16px 24px; overflow-y: auto; max-height: calc(70vh - 60px); }
.empty-hint { padding: 40px 0; }

/* ===== 热门城市 ===== */
.city-tag { padding: 8px 14px; background: #f0f2f5; border-radius: 20px; font-size: 14px; color: #646566; transition: all 0.3s; border: 1px solid transparent; }
.city-tag.active { background: linear-gradient(135deg, #43cea2, #185a9d); color: #fff; border-color: rgba(67,206,162,0.3); box-shadow: 0 2px 8px rgba(67,206,162,0.25); }

/* ===== 发现世界 商品卡片 ===== */
.discover-card { margin-bottom: 24px; }
.product-list { display: flex; flex-direction: column; gap: 14px; }
.product-card { display: flex; gap: 12px; padding: 10px; border-radius: 14px; background: linear-gradient(135deg, #f7f8fc 0%, #f0f4ff 100%); border: 1px solid rgba(67,206,162,0.1); transition: all 0.25s; }
.product-card:active { transform: scale(0.98); box-shadow: 0 4px 12px rgba(67,206,162,0.2); }
.product-img { width: 110px; height: 90px; border-radius: 10px; object-fit: cover; flex-shrink: 0; background: #eee; }
.product-info { flex: 1; display: flex; flex-direction: column; justify-content: space-between; min-width: 0; }
.product-title { font-size: 15px; font-weight: 600; color: #1a1a2e; line-height: 1.35; display: -webkit-box; -webkit-line-clamp: 2; -webkit-box-orient: vertical; overflow: hidden; }
.product-meta { font-size: 12px; color: #878c99; margin-top: 4px; }
.product-meta .divider { margin: 0 4px; color: #d0d3dc; }
.product-bottom { display: flex; justify-content: space-between; align-items: center; margin-top: auto; }
.product-price { font-size: 18px; font-weight: 700; color: #ff6b6b; letter-spacing: 0.5px; }
.add-cart-btn { width: 34px; height: 34px; border-radius: 50%; background: #fff; display: flex; align-items: center; justify-content: center; box-shadow: 0 2px 10px rgba(255,126,95,0.25); transition: transform 0.15s; }
.add-cart-btn:active { transform: scale(0.9); }

/* ===== 商品详情弹窗 ===== */
.detail-popup { display: flex; flex-direction: column; height: 100%; overflow-y: auto; padding-bottom: 90px; }
.detail-img { width: 100%; height: 240px; object-fit: cover; background: #eee; }
.detail-header { padding: 16px 16px 8px; }
.detail-title { font-size: 20px; font-weight: 700; color: #1a1a2e; line-height: 1.4; margin-bottom: 10px; }
.detail-price { font-size: 26px; font-weight: 700; color: #ff6b6b; }
.detail-tags { display: flex; flex-wrap: wrap; gap: 8px; padding: 0 16px 12px; }
.detail-section { margin: 10px 16px; padding: 14px; border-radius: 12px; background: #f7f8fc; }
.detail-section-title { font-size: 15px; font-weight: 700; color: #1a1a2e; margin-bottom: 10px; }
.detail-section-body { font-size: 14px; color: #494d5f; line-height: 1.8; white-space: pre-wrap; word-break: break-word; }
.guide-info { display: flex; flex-direction: column; gap: 6px; }
.detail-bottom-bar { position: fixed; left: 0; right: 0; bottom: 0; padding: 12px 16px calc(12px + env(safe-area-inset-bottom)); background: #fff; border-top: 1px solid #ebedf0; z-index: 10; }

/* ===== 页面容器 ===== */
.page-container { min-height: 100vh; padding-bottom: 80px; }
</style>
