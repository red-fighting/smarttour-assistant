import request from '../utils/request'
import { fetchStream } from '../utils/request'

export const recommendTravel = (data) => {
  return request({
    url: '/travel/recommend',
    method: 'post',
    data
  })
}

// 流式聊天接口
export const chatStream = (data, onChunk, onComplete, onError) => {
  return fetchStream('/chat', data, onChunk, onComplete, onError)
}

export const getUserInfo = () => {
  return request.get('/user/userInfo')
}

export const getAvatarByUsername = (username) => {
  return request.get('/user/avatar', { params: { username } })
}

export const changePassword = (data) => {
  return request({
    url: '/user/changePassword',
    method: 'post',
    data
  })
}

export const updateProfile = (data) => {
  return request({
    url: '/user/updateProfile',
    method: 'post',
    data
  })
}

export const submitFeedbackApi = (data) => {
  return request({
    url: '/user/feedback',
    method: 'post',
    data
  })
}

// ============ 收藏接口 ============
export const addFavorite = (data) => {
  return request({
    url: '/favorite/add',
    method: 'post',
    data
  })
}

export const removeFavorite = (data) => {
  return request({
    url: '/favorite/remove',
    method: 'post',
    data
  })
}

export const getFavorites = () => {
  return request.get('/favorite/list')
}

export const checkFavorited = (postId) => {
  return request.get('/favorite/check', { params: { postId } })
}

export const getFavoriteCount = () => {
  return request.get('/favorite/count')
}

// ============ 分享接口 ============
export const createShare = (data) => {
  return request({
    url: '/share/create',
    method: 'post',
    data
  })
}

export const deleteShare = (data) => {
  return request({
    url: '/share/delete',
    method: 'post',
    data
  })
}

export const getMyShares = () => {
  return request.get('/share/mine')
}

export const getAllShares = () => {
  return request.get('/share/all')
}

export const likeShare = (data) => {
  return request({
    url: '/share/like',
    method: 'post',
    data
  })
}

export const incrementComment = (data) => {
  return request({
    url: '/share/comment',
    method: 'post',
    data
  })
}

export const getShareCount = () => {
  return request.get('/share/count')
}

// ============ 评论接口 ============
export const getComments = (shareId) => {
  return request.get('/comment/list', { params: { shareId } })
}

export const createComment = (data) => {
  return request({
    url: '/comment/create',
    method: 'post',
    data
  })
}

// ============ 文件上传接口 ============
/** 上传图片 → 返回 { url: '/uploads/xxx.jpg' } */
export const uploadImage = (file) => {
  const formData = new FormData()
  formData.append('file', file)
  return request({
    url: '/upload/image',
    method: 'post',
    data: formData,
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}

// ============ 旅游商品接口 ============
export const getProductList = () => request.get('/product/list')
export const getProductDetail = (id) => request.get('/product/detail', { params: { id } })
/** 动态条件查询商品（起始地/目的地/预算/天数，全可选） */
export const searchProducts = (params) => request.get('/product/search', { params })

// ============ 购物车 / 订单接口 ============
export const addToCart = (data) => request({ url: '/cart/add', method: 'post', data })
export const getMyOrders = (status) => request.get('/cart/my', { params: { status } })
export const deleteOrder = (id) => request({ url: '/cart/delete', method: 'post', data: { id } })
export const payOrder = (id) => request({ url: '/cart/pay', method: 'post', data: { id } })
export const payAllCart = () => request({ url: '/cart/payAll', method: 'post' })
export const getCartCounts = () => request.get('/cart/counts')

// ============ 轮播图接口 ============
/** 公开：首页轮播列表（仅启用） */
export const getBannerList = () => request.get('/banner/list')
// 管理员：轮播图增删改查
export const adminListBanners = () => request.get('/banner/admin/list')
export const adminAddBanner = (data) => request({ url: '/banner/admin/add', method: 'post', data })
export const adminUpdateBanner = (data) => request({ url: '/banner/admin/update', method: 'post', data })
export const adminDeleteBanner = (id) => request({ url: '/banner/admin/delete', method: 'post', data: { id } })

// ============ 管理员接口 ============
export const adminDashboard = () => request.get('/admin/dashboard')
// 用户管理
export const adminListUsers = () => request.get('/admin/users')
export const adminToggleUserStatus = (id) => request({ url: '/admin/user/toggleStatus', method: 'post', data: { id } })
export const adminSetUserRole = (id, role) => request({ url: '/admin/user/setRole', method: 'post', data: { id, role } })
// 商品管理
export const adminListProducts = () => request.get('/admin/products')
export const adminSaveProduct = (data) => request({ url: '/admin/product/save', method: 'post', data })
export const adminDeleteProduct = (id) => request({ url: '/admin/product/delete', method: 'post', data: { id } })
// 订单管理
export const adminListOrders = (status = -1) => request.get('/admin/orders', { params: { status } })
export const adminUpdateOrderStatus = (id, status) => request({ url: '/admin/order/updateStatus', method: 'post', data: { id, status } })
export const adminDeleteOrder = (id) => request({ url: '/admin/order/delete', method: 'post', data: { id } })

