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
export const getUserInfo = (data) => {
  return request.get('/userInfo', data)
}

