import config from './config.js'

// API配置
const BASE_URL = config.baseURL

// 图片 URL 前缀替换：后端返回的 localhost URL 替换为实际可用的 base URL
const IMAGE_HOST_REPLACEMENT = BASE_URL.replace(/\/$/, '')

/**
 * 修复图片 URL：将后端返回的 localhost/127.0.0.1 URL 替换为当前环境的 base URL
 * 小程序环境下强制使用 HTTPS（微信要求）
 * 支持单个 URL 和 JSON 数组字符串（如 images 字段）
 */
export function fixImageUrl(url) {
  if (!url) return ''
  // 如果是 JSON 数组字符串，递归修复每个 URL
  if (typeof url === 'string' && url.startsWith('[')) {
    try {
      const arr = JSON.parse(url)
      if (Array.isArray(arr)) {
        return JSON.stringify(arr.map(item => fixImageUrl(item)))
      }
    } catch (e) {
      // 不是合法 JSON，当作普通字符串处理
    }
  }
  // 先替换 host 为当前环境的 base URL
  let result = url.replace(/^https?:\/\/(localhost|127\.0\.0\.1)(:\d+)?/i, IMAGE_HOST_REPLACEMENT)
  // 小程序环境下强制 HTTPS（仅对非 localhost 的外部 URL，并且不影响本地开发）
  // #ifdef MP-WEIXIN
  if (!/localhost|127\.0\.0\.1/i.test(result)) {
    result = result.replace(/^http:\/\//i, 'https://')
  } else {
    // 本地开发环境强制使用 HTTP，避免 HTTPS 错误
    result = result.replace(/^https:\/\//i, 'http://')
  }
  // #endif
  return result
}

/**
 * 递归修复数据对象中的图片 URL（字段名含 cover/avatar/icon/img/image/photo/thumb）
 */
function fixImageUrlsInData(data) {
  if (!data) return data
  if (Array.isArray(data)) {
    return data.map(item => fixImageUrlsInData(item))
  }
  if (typeof data === 'object') {
    const result = {}
    for (const key of Object.keys(data)) {
      const val = data[key]
      if (typeof val === 'string' && /cover|avatar|icon|img|image|photo|thumb/i.test(key)) {
        result[key] = fixImageUrl(val)
      } else {
        result[key] = fixImageUrlsInData(val)
      }
    }
    return result
  }
  return data
}

// 请求拦截器
const request = (options) => {
  return new Promise((resolve, reject) => {
    uni.request({
      url: BASE_URL + options.url,
      method: options.method || 'GET',
      data: options.data || {},
      timeout: options.timeout || 10000,
      header: {
        'Content-Type': 'application/json',
        'Authorization': uni.getStorageSync('token') ? `Bearer ${uni.getStorageSync('token')}` : ''
      },
      success: (res) => {
        if (res.statusCode === 200) {
          if (res.data.code === 200) {
            const data = fixImageUrlsInData(res.data.data)
            resolve(data)
          } else {
            // 业务级错误（如账号被禁用、密码错误等）
            const errorData = {
              code: res.data.code,
              message: res.data.message || res.data.msg || '请求失败',
              data: res.data.data
            }
            // 业务级 401（token过期或被禁用）
            if (res.data.code === 401) {
              const msg = errorData.message
              uni.showToast({
                title: msg.includes('禁用') ? '账号已被禁用，请联系管理员' : '登录已过期，请重新登录',
                icon: 'none'
              })
              uni.removeStorageSync('token')
              uni.removeStorageSync('userInfo')
              setTimeout(() => {
                uni.navigateTo({ url: '/pages/login/login' })
              }, 1000)
            }
            reject(errorData)
          }
        } else if (res.statusCode === 401) {
          // HTTP 级 401
          uni.showToast({
            title: '登录已过期，请重新登录',
            icon: 'none'
          })
          uni.removeStorageSync('token')
          uni.removeStorageSync('userInfo')
          setTimeout(() => {
            uni.navigateTo({ url: '/pages/login/login' })
          }, 1000)
          reject(res)
        } else {
          reject(res)
        }
      },
      fail: (err) => {
        reject(err)
      }
    })
  })
}

// 景点API
export const attractionApi = {
  getList: (page = 1, limit = 10, category = '') => request({
    url: `/api/attractions?page=${page}&size=${limit}${category ? '&category=' + category : ''}`,
    method: 'GET'
  }),
  getDetail: (id) => request({
    url: `/api/attractions/${id}`,
    method: 'GET'
  })
}

// 门票订单API
export const ticketOrderApi = {
  getList: (page = 1, limit = 10, status = '') => request({
    url: `/api/ticket-orders?page=${page}&size=${limit}${status ? '&status=' + status : ''}`,
    method: 'GET'
  }),
  getDetail: (id) => request({
    url: `/api/ticket-orders/${id}`,
    method: 'GET'
  }),
  getByUserId: (userId) => request({
    url: `/api/ticket-orders/user/${userId}`,
    method: 'GET'
  }),
  create: (data) => request({
    url: '/api/ticket-orders',
    method: 'POST',
    data
  }),
  pay: (orderNo) => request({
    url: `/api/ticket-orders/pay?orderNo=${orderNo}`,
    method: 'POST'
  }),
  cancel: (orderNo) => request({
    url: `/api/ticket-orders/cancel?orderNo=${orderNo}`,
    method: 'POST'
  }),
  use: (orderNo) => request({
    url: `/api/ticket-orders/use?orderNo=${orderNo}`,
    method: 'POST'
  }),
  getStats: () => request({
    url: '/api/ticket-orders/stats',
    method: 'GET'
  })
}

// 美食订单API
export const foodOrderApi = {
  getList: (page = 1, limit = 10, status = '') => request({
    url: `/api/food-orders?page=${page}&size=${limit}${status ? '&status=' + status : ''}`,
    method: 'GET'
  }),
  getDetail: (id) => request({
    url: `/api/food-orders/${id}`,
    method: 'GET'
  }),
  getByUserId: (userId) => request({
    url: `/api/food-orders/user/${userId}`,
    method: 'GET'
  }),
  create: (data) => request({
    url: '/api/food-orders',
    method: 'POST',
    data
  }),
  pay: (orderNo) => request({
    url: `/api/food-orders/pay?orderNo=${orderNo}`,
    method: 'POST'
  }),
  cancel: (orderNo) => request({
    url: `/api/food-orders/cancel?orderNo=${orderNo}`,
    method: 'POST'
  }),
  use: (orderNo) => request({
    url: `/api/food-orders/use?orderNo=${orderNo}`,
    method: 'POST'
  }),
  getStats: () => request({
    url: '/api/food-orders/stats',
    method: 'GET'
  })
}

// 酒店订单API
export const hotelOrderApi = {
  getList: (page = 1, limit = 10, status = '') => request({
    url: `/api/hotel-orders?page=${page}&size=${limit}${status ? '&status=' + status : ''}`,
    method: 'GET'
  }),
  getDetail: (id) => request({
    url: `/api/hotel-orders/${id}`,
    method: 'GET'
  }),
  getByUserId: (userId) => request({
    url: `/api/hotel-orders/user/${userId}`,
    method: 'GET'
  }),
  create: (data) => request({
    url: '/api/hotel-orders',
    method: 'POST',
    data
  }),
  pay: (orderNo) => request({
    url: `/api/hotel-orders/pay?orderNo=${orderNo}`,
    method: 'POST'
  }),
  cancel: (orderNo) => request({
    url: `/api/hotel-orders/cancel?orderNo=${orderNo}`,
    method: 'POST'
  }),
  use: (orderNo) => request({
    url: `/api/hotel-orders/use?orderNo=${orderNo}`,
    method: 'POST'
  }),
  checkIn: (orderNo) => request({
    url: `/api/hotel-orders/check-in?orderNo=${orderNo}`,
    method: 'POST'
  }),
  checkOut: (orderNo) => request({
    url: `/api/hotel-orders/check-out?orderNo=${orderNo}`,
    method: 'POST'
  }),
  getStats: () => request({
    url: '/api/hotel-orders/stats',
    method: 'GET'
  })
}

// 商品订单API
export const productOrderApi = {
  getList: (page = 1, limit = 10, status = '') => request({
    url: `/api/product-orders?page=${page}&size=${limit}${status ? '&status=' + status : ''}`,
    method: 'GET'
  }),
  getDetail: (id) => request({
    url: `/api/product-orders/${id}`,
    method: 'GET'
  }),
  getByUserId: (userId) => request({
    url: `/api/product-orders/user/${userId}`,
    method: 'GET'
  }),
  create: (data) => request({
    url: '/api/product-orders',
    method: 'POST',
    data
  }),
  pay: (orderNo) => request({
    url: `/api/product-orders/pay?orderNo=${orderNo}`,
    method: 'POST'
  }),
  cancel: (orderNo) => request({
    url: `/api/product-orders/cancel?orderNo=${orderNo}`,
    method: 'POST'
  }),
  use: (orderNo) => request({
    url: `/api/product-orders/use?orderNo=${orderNo}`,
    method: 'POST'
  }),
  confirmReceive: (orderNo) => request({
    url: `/api/product-orders/use?orderNo=${orderNo}`,
    method: 'POST'
  }),
  ship: (orderNo) => request({
    url: `/api/product-orders/ship?orderNo=${orderNo}`,
    method: 'POST'
  }),
  deliver: (orderNo) => request({
    url: `/api/product-orders/deliver?orderNo=${orderNo}`,
    method: 'POST'
  }),
  getStats: () => request({
    url: '/api/product-orders/stats',
    method: 'GET'
  })
}

// 订单API (兼容旧代码)
export const orderApi = {
  getList: (page = 1, limit = 10, status = '', type = '') => {
    const userInfo = uni.getStorageSync('userInfo') || {}
    const userId = userInfo.userId || userInfo.id
    if (!userId) {
      return Promise.resolve({ records: [], list: [], total: 0 })
    }
    return Promise.all([
      ticketOrderApi.getByUserId(userId),
      foodOrderApi.getByUserId(userId),
      hotelOrderApi.getByUserId(userId),
      productOrderApi.getByUserId(userId)
    ]).then(([ticketRes, foodRes, hotelRes, productRes]) => {
      let allOrders = [
        ...(ticketRes || []).map(o => ({ ...o, type: 'ticket' })),
        ...(foodRes || []).map(o => ({ ...o, type: 'food' })),
        ...(hotelRes || []).map(o => ({ ...o, type: 'hotel' })),
        ...(productRes || []).map(o => ({ ...o, type: 'product' }))
      ]
      if (status) {
        allOrders = allOrders.filter(o => o.status === status)
      }
      if (type) {
        allOrders = allOrders.filter(o => o.type === type)
      }
      return { records: allOrders, list: allOrders, total: allOrders.length }
    })
  },
  getDetail: (id) => request({
    url: `/api/orders/${id}`,
    method: 'GET'
  }),
  create: (data) => request({
    url: '/api/orders',
    method: 'POST',
    data
  }),
  updateStatus: (id, status) => request({
    url: `/api/orders/${id}/status`,
    method: 'PUT',
    data: { status }
  }),
  cancel: (orderNo) => {
    return ticketOrderApi.cancel(orderNo).catch(() => 
      foodOrderApi.cancel(orderNo).catch(() => 
        hotelOrderApi.cancel(orderNo).catch(() => 
          productOrderApi.cancel(orderNo)
        )
      )
    )
  }
}

// 用户API
export const userApi = {
  getMe: (options = {}) => request({
    url: '/api/users/me',
    method: 'GET',
    silent: options.silent || false
  }),
  getDetail: (id) => request({
    url: `/api/users/${id}`,
    method: 'GET'
  }),
  register: (data) => request({
    url: '/api/users/register',
    method: 'POST',
    data
  }),
  login: (data) => request({
    url: '/api/users/login',
    method: 'POST',
    data
  }),
  update: (data) => request({
    url: '/api/users',
    method: 'PUT',
    data
  }),
  changePassword: (data) => request({
    url: '/api/users/change-password',
    method: 'POST',
    data
  })
}

// 认证 API
export const authApi = {
  // uni 一键登录
  univerifyLogin: (data) => request({
    url: '/api/auth/univerify/login',
    method: 'POST',
    data
  })
}

// 社区帖子API (moments)
export const postApi = {
  getList: (page = 1, limit = 10, keyword = '') => request({
    url: `/api/moments?page=${page}&size=${limit}&status=1${keyword ? '&keyword=' + keyword : ''}`,
    method: 'GET'
  }),
  getDetail: (id) => request({
    url: `/api/moments/${id}`,
    method: 'GET'
  }),
  create: (data) => request({
    url: '/api/moments',
    method: 'POST',
    data
  }),
  getMyPosts: (userId, page = 1, limit = 10) => request({
    url: `/api/moments/my?userId=${userId}&page=${page}&size=${limit}`,
    method: 'GET'
  }),
  like: (id) => request({
    url: `/api/moments/like/${id}`,
    method: 'POST'
  }),
  delete: (id) => request({
    url: `/api/moments/${id}`,
    method: 'DELETE'
  })
}

// 评论API
export const commentApi = {
  getList: (postId, page = 1, limit = 20) => request({
    url: `/api/posts/${postId}/comments?page=${page}&size=${limit}`,
    method: 'GET'
  }),
  create: (postId, data) => request({
    url: `/api/posts/${postId}/comments`,
    method: 'POST',
    data
  }),
  delete: (id) => request({
    url: `/api/comments/${id}`,
    method: 'DELETE'
  }),
  like: (id) => request({
    url: `/api/comments/${id}/like`,
    method: 'POST'
  })
}

// 敏感词检测API
export const sensitiveWordApi = {
  check: (text) => request({
    url: '/api/sensitive-words/check',
    method: 'POST',
    data: { text }
  }),
  getList: () => request({
    url: '/api/sensitive-words',
    method: 'GET'
  })
}

// 社区帖子API
export const heritageApi = {
  getList: (page = 1, limit = 10, level = '') => request({
    url: `/api/heritage?page=${page}&size=${limit}${level ? '&level=' + level : ''}`,
    method: 'GET'
  }),
  getDetail: (id) => request({
    url: `/api/heritage/${id}`,
    method: 'GET'
  })
}

// 美食API
export const foodApi = {
  getList: (page = 1, limit = 10, category = '') => request({
    url: `/api/foods?page=${page}&size=${limit}${category ? '&category=' + category : ''}`,
    method: 'GET'
  }),
  getDetail: (id) => request({
    url: `/api/foods/${id}`,
    method: 'GET'
  })
}

// 路线API（推荐路线）
export const routeApi = {
  getList: (page = 1, limit = 10, difficulty = '') => request({
    url: `/api/routes?page=${page}&size=${limit}${difficulty ? '&difficulty=' + difficulty : ''}`,
    method: 'GET'
  }),
  getDetail: (id) => request({
    url: `/api/routes/${id}`,
    method: 'GET'
  })
}

// 用户自定义路线API
export const userRouteApi = {
  getList: (page = 1, limit = 10) => request({
    url: `/api/user-routes?page=${page}&size=${limit}`,
    method: 'GET'
  }),
  getDetail: (id) => request({
    url: `/api/user-routes/${id}`,
    method: 'GET'
  }),
  create: (data) => request({
    url: '/api/user-routes',
    method: 'POST',
    data
  }),
  getMyRoutes: (userId) => request({
    url: `/api/user-routes/my?userId=${userId}`,
    method: 'GET'
  }),
  update: (id, data) => request({
    url: `/api/user-routes/${id}`,
    method: 'PUT',
    data
  }),
  delete: (id) => request({
    url: `/api/user-routes/${id}`,
    method: 'DELETE'
  })
}

// 酒店API
export const hotelApi = {
  getList: (page = 1, limit = 10, type = '') => request({
    url: `/api/hotels?page=${page}&size=${limit}${type ? '&type=' + type : ''}`,
    method: 'GET'
  }),
  getDetail: (id) => request({
    url: `/api/hotels/${id}`,
    method: 'GET'
  })
}

// 非遗体验预约API
export const experienceOrderApi = {
  getList: (page = 1, limit = 10, status = '') => request({
    url: `/api/experience-orders?page=${page}&size=${limit}${status ? '&status=' + status : ''}`,
    method: 'GET'
  }),
  getDetail: (id) => request({
    url: `/api/experience-orders/${id}`,
    method: 'GET'
  }),
  getByUserId: (userId) => request({
    url: `/api/experience-orders/user/${userId}`,
    method: 'GET'
  }),
  create: (data) => request({
    url: '/api/experience-orders',
    method: 'POST',
    data
  }),
  confirm: (orderNo) => request({
    url: `/api/experience-orders/confirm?orderNo=${orderNo}`,
    method: 'POST'
  }),
  pay: (orderNo) => request({
    url: `/api/experience-orders/confirm?orderNo=${orderNo}`,
    method: 'POST'
  }),
  complete: (orderNo) => request({
    url: `/api/experience-orders/complete?orderNo=${orderNo}`,
    method: 'POST'
  }),
  cancel: (orderNo, reason = '') => request({
    url: `/api/experience-orders/cancel?orderNo=${orderNo}${reason ? '&reason=' + encodeURIComponent(reason) : ''}`,
    method: 'POST'
  }),
  getStats: () => request({
    url: '/api/experience-orders/stats',
    method: 'GET'
  })
}

// 商品API
export const productApi = {
  getList: (page = 1, limit = 10, category = '') => request({
    url: `/api/products?page=${page}&size=${limit}${category ? '&category=' + category : ''}`,
    method: 'GET'
  }),
  getDetail: (id) => request({
    url: `/api/products/${id}`,
    method: 'GET'
  })
}

// 活动API
export const activityApi = {
  getList: (page = 1, limit = 10, status = '') => request({
    url: `/api/activities/published?page=${page}&size=${limit}${status ? '&status=' + status : ''}`,
    method: 'GET'
  }),
  getDetail: (id) => request({
    url: `/api/activities/${id}`,
    method: 'GET'
  }),
  signup: (id, data = {}) => request({
    url: `/api/activities/signup/${id}`,
    method: 'POST',
    data
  }),
  cancelSignup: (id, data = {}) => request({
    url: `/api/activities/signup/${id}/cancel`,
    method: 'POST',
    data
  })
}

// 行程API（自定义路线规划）
export const journeyApi = {
  getList: (page = 1, limit = 10) => request({
    url: `/api/journeys?page=${page}&size=${limit}`,
    method: 'GET'
  }),
  getByUserId: (userId, page = 1, limit = 10) => request({
    url: `/api/journeys/user/${userId}?page=${page}&size=${limit}`,
    method: 'GET'
  }),
  getDetail: (id) => request({
    url: `/api/journeys/${id}`,
    method: 'GET'
  }),
  create: (data) => request({
    url: '/api/journeys',
    method: 'POST',
    data
  }),
  update: (data) => request({
    url: '/api/journeys',
    method: 'PUT',
    data
  }),
  delete: (id) => request({
    url: `/api/journeys/${id}`,
    method: 'DELETE'
  })
}

// Banner API (保留以防后续使用)
export const bannerApi = {
  getList: (position = '') => request({
    url: `/api/banners${position ? '?position=' + position : ''}`,
    method: 'GET'
  })
}

// 订单核销API
export const orderVerifyApi = {
  verifyByCode: (code) => request({
    url: '/api/orders/verify',
    method: 'POST',
    data: { code }
  })
}

// 图片上传 API
export const uploadApi = {
  uploadImage: (filePath) => {
    return new Promise((resolve, reject) => {
      uni.uploadFile({
        url: BASE_URL + '/upload',
        filePath: filePath,
        name: 'file',
        header: {
          'Authorization': uni.getStorageSync('token') ? `Bearer ${uni.getStorageSync('token')}` : ''
        },
        success: (res) => {
          if (res.statusCode === 200) {
            const data = JSON.parse(res.data)
            if (data.code === 200) {
              resolve(data.data)
            } else {
              reject(data)
            }
          } else {
            reject(res)
          }
        },
        fail: (err) => {
          reject(err)
        }
      })
    })
  }
}

// AI 聊天 API
export const aiApi = {
  createSession: (data) => request({
    url: '/api/chat/sessions',
    method: 'POST',
    data
  }),
  chat: (message, history = [], sessionId = null) => request({
    url: '/api/ai/chat',
    method: 'POST',
    data: {
      message,
      history,
      session_id: sessionId,
      userId: (uni.getStorageSync('userInfo') || {}).userId || null,
      userName: (uni.getStorageSync('userInfo') || {}).nickname || '匿名用户'
    }
  }),
  getSessions: (page = 1, limit = 20) => request({
    url: `/api/chat/sessions?page=${page}&limit=${limit}`,
    method: 'GET'
  }),
  getSessionMessages: (sessionId, page = 1, limit = 50) => request({
    url: `/api/chat/sessions/${sessionId}/messages?page=${page}&size=${limit}`,
    method: 'GET'
  }),
  deleteSession: (sessionId) => request({
    url: `/api/chat/sessions/${sessionId}`,
    method: 'DELETE'
  }),
  saveMessage: (data) => request({
    url: '/api/chat/messages',
    method: 'POST',
    data
  })
}

// 攻略API
export const guideApi = {
  getList: (page = 1, limit = 10, status = '') => request({
    url: `/api/guides?page=${page}&size=${limit}${status !== '' ? '&status=' + status : ''}`,
    method: 'GET'
  }),
  getDetail: (id) => request({
    url: `/api/guides/${id}`,
    method: 'GET'
  }),
  save: (data) => request({
    url: '/api/guides',
    method: 'POST',
    data
  })
}

// 关注 API（预留后端接口）
export const followApi = {
  follow: (userId) => request({
    url: `/api/follows/${userId}`,
    method: 'POST'
  }),
  unfollow: (userId) => request({
    url: `/api/follows/${userId}`,
    method: 'DELETE'
  }),
  getFollowing: (userId) => request({
    url: `/api/follows/following?userId=${userId}`,
    method: 'GET'
  }),
  isFollowing: (userId, targetId) => request({
    url: `/api/follows/check?userId=${userId}&targetId=${targetId}`,
    method: 'GET'
  })
}

// 订单时间格式化
export const formatOrderTime = (time) => {
  if (!time) {
    // 如果没有时间，返回当前时间
    const date = new Date()
    const y = date.getFullYear()
    const m = date.getMonth() + 1
    const d = date.getDate()
    const h = String(date.getHours()).padStart(2, '0')
    const min = String(date.getMinutes()).padStart(2, '0')
    return `${y}年${m}月${d}日 ${h}:${min}`
  }
  // 如果已经是格式化后的中文时间（如 "2024年1月15日 14:30"），直接返回
  if (typeof time === 'string' && time.includes('年')) return time
  let date
  if (time instanceof Date) {
    date = time
  } else if (typeof time === 'number') {
    date = new Date(time)
  } else if (typeof time === 'string') {
    date = new Date(time)
  } else {
    date = new Date()
  }
  if (isNaN(date.getTime())) {
    date = new Date()
  }
  const y = date.getFullYear()
  const m = date.getMonth() + 1
  const d = date.getDate()
  const h = String(date.getHours()).padStart(2, '0')
  const min = String(date.getMinutes()).padStart(2, '0')
  return `${y}年${m}月${d}日 ${h}:${min}`
}

export default request
