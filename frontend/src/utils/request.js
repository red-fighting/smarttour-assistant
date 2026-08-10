import axios from 'axios'
const request = axios.create({
  baseURL: '/api',
  timeout: 10000
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
//响应拦截器处理 token 过期
request.interceptors.response.use(
  response => {
    return response.data
  },
  error => {
    if (error.response?.status === 401) {
      // token 过期，清除并跳转登录
      localStorage.removeItem('token')
      localStorage.removeItem('userInfo')
      window.location.href = '/login'
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