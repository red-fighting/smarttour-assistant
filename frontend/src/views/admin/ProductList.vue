<template>
  <div class="productlist">
    <div class="toolbar">
      <div class="title">
        <van-icon name="shop-o" size="18" color="#185a9d" />
        <span>旅游商品 ({{ products.length }})</span>
      </div>
      <van-button size="small" type="primary" @click="openAdd">
        <template #icon><van-icon name="add" /></template>
        新增商品
      </van-button>
    </div>

    <van-loading v-if="loading" class="loader" />
    <van-empty v-else-if="products.length === 0" description="暂无商品，点击右上角新增" />

    <table v-else class="tbl">
      <thead>
        <tr>
          <th>ID</th><th>主图</th><th>标题</th><th>线路</th><th>时长</th>
          <th>价格</th><th>销量</th><th>导游</th><th>操作</th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="p in products" :key="p.id">
          <td class="muted">#{{ p.id }}</td>
          <td><img class="thumb" :src="p.image" @error="onImgErr" /></td>
          <td class="bold title-cell">{{ p.title }}</td>
          <td>
            <div class="muted small">{{ p.origin || '—' }}</div>
            <div class="arrow-cell"><van-icon name="arrow-down" size="12" color="#43cea2" /></div>
            <div>{{ p.destination || '—' }}</div>
          </td>
          <td>{{ p.duration || '—' }}</td>
          <td class="price">¥ {{ Number(p.price ?? 0).toFixed(2) }}</td>
          <td class="muted">{{ p.soldCount || 0 }}</td>
          <td>
            <div>{{ p.guideName || '—' }}</div>
            <div class="muted small">{{ p.guidePhone || '—' }}</div>
          </td>
          <td>
            <div class="acts">
              <van-button size="mini" plain type="primary" @click="openEdit(p)">编辑</van-button>
              <van-button size="mini" type="danger" @click="doDelete(p)">删除</van-button>
            </div>
          </td>
        </tr>
      </tbody>
    </table>

    <!-- 新增/编辑弹窗 -->
    <van-popup v-model:show="popupShow" position="right" :style="{ width: '100%', maxWidth: '520px', height: '100%' }">
      <div class="pop">
        <div class="pop-header">
          <div class="pop-title">{{ editing.id ? '编辑商品' : '新增商品' }}</div>
          <van-icon name="cross" size="20" @click="popupShow = false" />
        </div>
        <div class="pop-body">
          <van-field v-model="editing.title" placeholder="标题（如：九寨沟3日游）" label="标题" />
          <van-field v-model="editing.origin" placeholder="如：成都" label="出发地" />
          <van-field v-model="editing.destination" placeholder="如：九寨沟" label="目的地" />
          <van-field v-model="editing.duration" placeholder="如：3天2晚" label="游玩时间" />
          <van-field v-model="editing.price" placeholder="如：1999" label="价格" type="number" />
          <van-field v-model="editing.image" placeholder="http(s):// 或 /uploads/xxx.jpg" label="主图URL" />
          <div v-if="editing.image" class="preview-row">
            <img :src="editing.image" class="preview-img" @error="onImgErr" />
          </div>
          <van-field v-model="editing.soldCount" placeholder="默认0" label="初始销量" type="number" />
          <van-field v-model="editing.guideName" placeholder="导游姓名" label="导游姓名" />
          <van-field v-model="editing.guidePhone" placeholder="导游电话" label="导游电话" />
          <van-field v-model="editing.routeDesc" type="textarea" rows="2" placeholder="每日行程概览" label="路线描述" />
          <van-field v-model="editing.foodFeature" type="textarea" rows="2" placeholder="当地特色餐饮" label="美食特色" />
        </div>
        <div class="pop-footer">
          <van-button block type="primary" :loading="submitting" @click="submit">
            {{ editing.id ? '保存修改' : '新增商品' }}
          </van-button>
        </div>
      </div>
    </van-popup>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { showToast, showConfirmDialog } from 'vant'
import { adminListProducts, adminSaveProduct, adminDeleteProduct } from '../../api/index'

const products = ref([])
const loading = ref(false)
const popupShow = ref(false)
const submitting = ref(false)
const defaultImg = 'https://fastly.jsdelivr.net/npm/@vant/assets/cat.jpeg'
const onImgErr = (e) => { try { e.target.src = defaultImg } catch {} }

const EMPTY = () => ({
  id: null, title: '', origin: '', destination: '', duration: '',
  price: '', image: '', soldCount: 0,
  guideName: '', guidePhone: '', routeDesc: '', foodFeature: '', images: '[]'
})
const editing = reactive(EMPTY())

function openAdd() { Object.assign(editing, EMPTY()); popupShow.value = true }
function openEdit(p) { Object.assign(editing, EMPTY(), p); popupShow.value = true }

async function loadProducts() {
  loading.value = true
  try {
    const res = await adminListProducts()
    if (res.code === 200) products.value = res.data || []
  } finally { loading.value = false }
}

async function submit() {
  if (!editing.title?.trim()) { showToast('请填写标题'); return }
  submitting.value = true
  try {
    const payload = {
      id: editing.id || undefined,
      title: editing.title.trim(),
      origin: editing.origin || '', destination: editing.destination || '',
      duration: editing.duration || '',
      price: Number(editing.price ?? 0),
      image: editing.image || defaultImg,
      images: editing.images || '[]',
      soldCount: Number(editing.soldCount ?? 0),
      guideName: editing.guideName || '', guidePhone: editing.guidePhone || '',
      routeDesc: editing.routeDesc || '', foodFeature: editing.foodFeature || ''
    }
    const res = await adminSaveProduct(payload)
    if (res.code === 200) {
      showToast(editing.id ? '更新成功' : '新增成功')
      popupShow.value = false
      loadProducts()
    } else {
      showToast(res.message || '保存失败')
    }
  } finally { submitting.value = false }
}

async function doDelete(p) {
  try { await showConfirmDialog({ title: '删除商品', message: `确定删除 "${p.title}"？该操作不可撤销。` }) } catch { return }
  const res = await adminDeleteProduct(p.id)
  if (res.code === 200) { showToast('删除成功'); loadProducts() }
  else showToast(res.message || '删除失败')
}

onMounted(loadProducts)
</script>

<style scoped>
.productlist { padding: 4px 2px; }
.toolbar {
  display: flex; align-items: center; justify-content: space-between;
  padding: 4px 6px 14px;
}
.title { font-weight: 600; display: flex; gap: 8px; align-items: center; }
.tbl {
  width: 100%; border-collapse: collapse; background: #fff;
  border-radius: 12px; overflow: hidden; font-size: 13px;
  box-shadow: 0 2px 10px rgba(0,0,0,0.03);
}
.tbl th, .tbl td { padding: 10px 10px; text-align: left; border-bottom: 1px solid #eef2f7; vertical-align: middle; }
.tbl th { background: #eef4fc; color: #185a9d; font-weight: 600; font-size: 12.5px; white-space: nowrap; }
.tbl tbody tr:hover { background: #f7fafc; }
.thumb { width: 60px; height: 44px; object-fit: cover; border-radius: 8px; background: #e8f0fe; }
.bold { font-weight: 600; }
.title-cell { max-width: 220px; }
.price { color: #e04358; font-weight: 700; }
.muted { color: #9aa4b2; }
.small { font-size: 12px; }
.arrow-cell { line-height: 1; padding: 2px 0; }
.acts { display: flex; gap: 6px; flex-wrap: wrap; }
.loader { display: block; text-align: center; padding: 60px 0; }

/* 弹窗 */
.pop { height: 100%; display: flex; flex-direction: column; background: #fff; }
.pop-header { display: flex; justify-content: space-between; align-items: center; padding: 16px 18px; border-bottom: 1px solid #eef2f7; }
.pop-title { font-weight: 700; color: #185a9d; font-size: 16px; }
.pop-body { flex: 1; overflow-y: auto; }
.preview-row { padding: 8px 16px; }
.preview-img { width: 100%; height: 160px; object-fit: cover; border-radius: 10px; background: #f0f9ff; }
.pop-footer { padding: 12px 16px; border-top: 1px solid #eef2f7; }
</style>
