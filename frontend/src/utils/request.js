import axios from 'axios'
const request = axios.create({
  baseURL: '/api',
  timeout: 120000
})
//请求拦截器
request.interceptors.request.use(
  config => {
    const token = localStorage.getItem('token')
    if (token) {
      config.headers.Authorization = 'Bearer ' + token
    }
    return config
  },
  error => {
    return Promise.reject(error)
  }
)
/** 检测响应内容或错误信息中是否包含 JWT 过期关键字 */
const isJwtExpired = (data) => {
  if (!data) return false
  const str = typeof data === 'string' ? data : JSON.stringify(data)
  return /JWT expired|token expired|令牌过期|token.*invalid|invalid.*jwt|signature.*exception/i.test(str)
}

/** 统一清除登录态并跳转登录页 */
const clearAndRedirectLogin = () => {
  localStorage.removeItem('token')
  localStorage.removeItem('userInfo')
  window.location.href = '/login'
}

//响应拦截器处理 token 过期
request.interceptors.response.use(
  response => {
    const data = response.data
    // 业务码非 200 时检测 JWT 过期信息（后端可能 HTTP 200 但业务返回 JWT expired）
    if (data && data.code !== 200) {
      const msg = data.message || data.msg || data.errMsg || ''
      if (isJwtExpired(msg) || isJwtExpired(data)) {
        console.warn('[request] 检测到业务响应中 JWT 过期，跳转登录')
        clearAndRedirectLogin()
      }
    }
    return data
  },
  error => {
    const respStatus = error.response?.status
    const respData = error.response?.data
    const errMsg = error.message || ''
    // HTTP 401 直接跳转
    if (respStatus === 401) {
      console.warn('[request] HTTP 401，跳转登录')
      clearAndRedirectLogin()
    }
    // 其他 HTTP 状态码但错误内容包含 JWT 过期信息（如 500 带 JWT expired）
    else if (isJwtExpired(respData) || isJwtExpired(errMsg)) {
      console.warn('[request] 检测到错误响应中 JWT 过期，跳转登录')
      clearAndRedirectLogin()
    }
    return Promise.reject(error)
  }
)
//流式请求  
export async function fetchStream(url, data, onChunk, onComplete, onError) {
  const controller = new AbortController()
  try {
    const fullUrl = `/api/travel${url.startsWith('/') ? url : '/' + url}`
    const response = await fetch(fullUrl, {
      method: 'POST',
      body: JSON.stringify(data),
      headers: {
        'Content-Type': 'application/json'
      },
      signal: controller.signal
    })

    if (!response.ok) {
      throw new Error(`网络请求失败:${response.status}`)
    }

    const reader = response.body.getReader()
    const decoder = new TextDecoder()

    while (true) {
      const { done, value } = await reader.read()
      if (done) break

      const chunk = decoder.decode(value, { stream: true })
      const lines = chunk.split('\n').filter(line => line.trim())

      for (const line of lines) {
        if (line.startsWith('data:')) {
          const jsonStr = line.substring(5)
          if (jsonStr) {
            try {
              const jsonData = JSON.parse(jsonStr)
              if (jsonData.type === 'chunk') {
                onChunk(jsonData.content)
              } else if (jsonData.done) {
                onComplete()
                return
              } else if (jsonData.type === 'error') {
                onError(jsonData.error)
                return
              }
            } catch (err) {
              console.warn('流式数据解析失败:', err)
            }
          }
        }
      }
    }

    onComplete()
  } catch (err) {
    onError(err.message)
  }
}

//导出请求实例
export default request