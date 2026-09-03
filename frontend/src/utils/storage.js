/**
 * 带 userId 隔离的 localStorage 工具
 * 所有 key 会自动加前缀: "{userId}_{key}"
 * 确保每个用户的 favorites / myShares / settings 互不混淆
 */

/** 从 localStorage.userInfo 里拿到当前登录用户的 userId */
const getCurrentUserId = () => {
  try {
    const info = JSON.parse(localStorage.getItem('userInfo') || '{}')
    return info?.userId || info?.id || null
  } catch {
    return null
  }
}

/** 构造带用户隔离的 key，例如 "user_5_favorites" */
const scopedKey = (key, userId) => {
  const uid = userId ?? getCurrentUserId()
  return uid ? `user_${uid}_${key}` : key
}

/** 读取带用户隔离的数据 */
export function getUserStorage(key, defaultValue = null, userId = null) {
  try {
    const raw = localStorage.getItem(scopedKey(key, userId))
    if (raw === null || raw === undefined) return defaultValue
    return JSON.parse(raw)
  } catch {
    return defaultValue
  }
}

/** 写入带用户隔离的数据 */
export function setUserStorage(key, value, userId = null) {
  localStorage.setItem(scopedKey(key, userId), JSON.stringify(value))
}

/** 删除带用户隔离的数据 */
export function removeUserStorage(key, userId = null) {
  localStorage.removeItem(scopedKey(key, userId))
}

/**
 * 登出时彻底清理当前用户的所有隔离 localStorage
 * （只清 user_{userId}_* 前缀的，不影响全局的 token / userInfo）
 */
export function clearAllUserStorage(userId = null) {
  const uid = userId ?? getCurrentUserId()
  if (!uid) return
  const prefix = `user_${uid}_`
  const keysToRemove = []
  for (let i = 0; i < localStorage.length; i++) {
    const k = localStorage.key(i)
    if (k && k.startsWith(prefix)) {
      keysToRemove.push(k)
    }
  }
  keysToRemove.forEach(k => localStorage.removeItem(k))
}
