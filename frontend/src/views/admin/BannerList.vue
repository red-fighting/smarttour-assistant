<template>
  <div class="bannerlist">
    <div class="toolbar">
      <div class="title">
        <van-icon name="photo-o" size="18" color="#185a9d" />
        <span>轮播图管理 ({{ banners.length }})</span>
      </div>
      <van-button size="small" type="primary" @click="openAdd">
        <template #icon><van-icon name="add" /></template>
        新增轮播图
      </van-button>
    </div>

    <van-loading v-if="loading" class="loader" />
    <van-empty v-else-if="banners.length === 0" description="暂无轮播图，点击右上角新增" />

    <table v-else class="tbl">
      <thead>
        <tr>
          <th>ID</th><th>图片</th><th>主标题</th><th>副标题</th><th>排序</th><th>状态</th><th>操作</th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="b in banners" :key="b.id">
          <td class="muted">#{{ b.id }}</td>
          <td><img class="thumb" :src="b.imageUrl" @error="onImgErr" /></td>
          <td class="bold">{{ b.title || '—' }}</td>
          <td class="muted">{{ b.subtitle || '—' }}</td>
          <td>{{ b.sortOrder ?? 0 }}</td>
          <td>
            <van-tag :type="b.status === 1 ? 'success' : 'default'">
              {{ b.status === 1 ? '启用' : '禁用' }}
            </van-tag>
          </td>
          <td>
            <div class="acts">
              <van-button size="mini" plain type="primary" @click="openEdit(b)">编辑</van-button>
              <van-button size="mini" plain :type="b.status === 1 ? 'warning' : 'success'" @click="toggleStatus(b)">
                {{ b.status === 1 ? '禁用' : '启用' }}
              </van-button>
              <van-button size="mini" type="danger" @click="doDelete(b)">删除</van-button>
            </div>
          </td>
        </tr>
      </tbody>
    </table>

    <!-- 新增/编辑弹窗 -->
    <van-popup v-model:show="popupShow" position="right" :style="{ width: '100%', maxWidth: '480px', height: '100%' }">
      <div class="pop">
        <div class="pop-header">
          <div class="pop-title">{{ editing.id ? '编辑轮播图' : '新增轮播图' }}</div>
          <van-icon name="cross" size="20" @click="popupShow = false" />
        </div>
        <div class="pop-body">
          <div class="upload-block">
            <div class="upload-label">轮播图片</div>
            <div class="upload-row">
              <div class="upload-box" @click="pickFile">
                <van-icon name="plus" size="22" color="#185a9d" />
                <div class="upload-tip">点击上传图片</div>
              </div>
              <img v-if="editing.imageUrl" :src="editing.imageUrl" class="upload-preview" @error="onImgErr" />
            </div>
            <input ref="fileInput" type="file" accept="image/*" hidden @change="onFileChange" />
          </div>
          <van-field v-model="editing.imageUrl" placeholder="也可直接填图片URL：http(s):// 或 /uploads/xxx.png" label="图片URL" />
          <van-field v-model="editing.title" placeholder="主标题（如：九寨沟自然风景区）" label="主标题" maxlength="50" />
          <van-field v-model="editing.subtitle" placeholder="副标题（如：四川 · 世界自然遗产）" label="副标题" maxlength="100" />
          <van-field v-model="editing.sortOrder" type="digit" placeholder="数字越小越靠前，默认 0" label="排序" />
          <div class="switch-row">
            <span>启用状态</span>
            <van-switch v-model="statusOn" size="22px" />
          </div>
        </div>
        <div class="pop-footer">
          <van-button block type="primary" :loading="submitting" @click="submit">
            {{ editing.id ? '保存修改' : '新增轮播图' }}
          </van-button>
        </div>
      </div>
    </van-popup>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { showToast, showConfirmDialog } from 'vant'
import {
  adminListBanners, adminAddBanner, adminUpdateBanner, adminDeleteBanner, uploadImage
} from '../../api/index'

const banners = ref([])
const loading = ref(false)
const popupShow = ref(false)
const submitting = ref(false)
const uploading = ref(false)
const fileInput = ref(null)
const defaultImg = 'https://fastly.jsdelivr.net/npm/@vant/assets/cat.jpeg'
const onImgErr = (e) => { try { e.target.src = defaultImg } catch {} }

const EMPTY = () => ({
  id: null, title: '', subtitle: '', imageUrl: '', sortOrder: 0, status: 1
})
const editing = reactive(EMPTY())
const statusOn = computed({
  get: () => editing.status === 1,
  set: (v) => { editing.status = v ? 1 : 0 }
})

function openAdd() { Object.assign(editing, EMPTY()); popupShow.value = true }
function openEdit(b) { Object.assign(editing, EMPTY(), b); popupShow.value = true }

async function loadBanners() {
  loading.value = true
  try {
    const res = await adminListBanners()
    if (res.code === 200) banners.value = res.data || []
  } finally { loading.value = false }
}

function pickFile() { fileInput.value?.click() }

async function onFileChange(e) {
  const file = e.target.files?.[0]
  if (!file) return
  uploading.value = true
  try {
    const res = await uploadImage(file)
    if (res.code === 200 && res.data?.url) {
      editing.imageUrl = res.data.url
      showToast('图片上传成功')
    } else {
      showToast(res.message || '上传失败')
    }
  } finally {
    uploading.value = false
    e.target.value = ''
  }
}

async function submit() {
  if (!editing.imageUrl?.trim()) { showToast('请上传图片或填写图片URL'); return }
  submitting.value = true
  try {
    const payload = {
      id: editing.id || undefined,
      title: editing.title?.trim() || '',
      subtitle: editing.subtitle?.trim() || '',
      imageUrl: editing.imageUrl.trim(),
      sortOrder: Number(editing.sortOrder ?? 0),
      status: editing.status
    }
    const res = editing.id ? await adminUpdateBanner(payload) : await adminAddBanner(payload)
    if (res.code === 200) {
      showToast(editing.id ? '更新成功' : '新增成功')
      popupShow.value = false
      loadBanners()
    } else {
      showToast(res.message || '保存失败')
    }
  } finally { submitting.value = false }
}

async function toggleStatus(b) {
  const res = await adminUpdateBanner({ ...b, status: b.status === 1 ? 0 : 1 })
  if (res.code === 200) { showToast(b.status === 1 ? '已禁用' : '已启用'); loadBanners() }
  else showToast(res.message || '操作失败')
}

async function doDelete(b) {
  try {
    await showConfirmDialog({ title: '删除轮播图', message: `确定删除 "${b.title || '未命名'}"？该操作不可撤销。` })
  } catch { return }
  const res = await adminDeleteBanner(b.id)
  if (res.code === 200) { showToast('删除成功'); loadBanners() }
  else showToast(res.message || '删除失败')
}

onMounted(loadBanners)
</script>

<style scoped>
.bannerlist { padding: 4px 2px; }
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
.thumb { width: 88px; height: 44px; object-fit: cover; border-radius: 8px; background: #e8f0fe; }
.bold { font-weight: 600; }
.muted { color: #9aa4b2; }
.acts { display: flex; gap: 6px; flex-wrap: wrap; }
.loader { display: block; text-align: center; padding: 60px 0; }

/* 弹窗 */
.pop { height: 100%; display: flex; flex-direction: column; background: #fff; }
.pop-header { display: flex; justify-content: space-between; align-items: center; padding: 16px 18px; border-bottom: 1px solid #eef2f7; }
.pop-title { font-weight: 700; color: #185a9d; font-size: 16px; }
.pop-body { flex: 1; overflow-y: auto; }
.pop-footer { padding: 12px 16px; border-top: 1px solid #eef2f7; }

.upload-block { padding: 14px 16px 4px; }
.upload-label { font-size: 13px; color: #646566; margin-bottom: 8px; }
.upload-row { display: flex; gap: 12px; align-items: center; }
.upload-box {
  width: 120px; height: 72px; border: 1px dashed #b8cfff; border-radius: 10px;
  display: flex; flex-direction: column; align-items: center; justify-content: center;
  gap: 2px; cursor: pointer; background: #f4f9ff; flex-shrink: 0;
}
.upload-box:hover { background: #eaf3ff; }
.upload-tip { font-size: 11px; color: #185a9d; }
.upload-preview {
  width: 200px; height: 72px; object-fit: cover; border-radius: 10px;
  background: #f0f9ff; border: 1px solid #eef2f7;
}
.switch-row {
  display: flex; align-items: center; justify-content: space-between;
  padding: 14px 16px; font-size: 14px; color: #323233;
}
</style>
