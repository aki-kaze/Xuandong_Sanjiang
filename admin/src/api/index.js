import axios from 'axios'
import { ElMessage } from 'element-plus'
import router from '@/router'

const request = axios.create({
  baseURL: '/api',
  timeout: 10000,
  headers: {
    'Content-Type': 'application/json'
  }
})

// 文件上传专用实例（不使用 /api 前缀）
const uploadRequest = axios.create({
  baseURL: '',
  timeout: 30000,
  headers: {
    'Content-Type': 'multipart/form-data'
  }
})

// 为上传实例添加 token 拦截器
uploadRequest.interceptors.request.use(config => {
  const token = localStorage.getItem('admin_token')
  if (token) config.headers['Authorization'] = `Bearer ${token}`
  return config
})

// 为上传实例添加响应拦截器
uploadRequest.interceptors.response.use(
  response => {
    const data = response.data
    if (data.code === 200) return data
    ElMessage.error(data.message || '上传失败')
    return Promise.reject(data)
  },
  error => {
    if (error.response?.status === 401) {
      localStorage.removeItem('admin_token')
      router.push('/login')
    } else if (!error.response) {
      ElMessage.error('无法连接到服务器，请确保后端服务已启动')
    } else {
      ElMessage.error(error.response?.data?.message || error.message || '上传失败')
    }
    return Promise.reject(error)
  }
)

request.interceptors.request.use(config => {
  const token = localStorage.getItem('admin_token')
  if (token) config.headers['Authorization'] = `Bearer ${token}`
  return config
})

request.interceptors.response.use(
  response => {
    const data = response.data
    if (data.code === 200) return data
    ElMessage.error(data.message || '请求失败')
    return Promise.reject(data)
  },
  error => {
    if (error.response?.status === 401) {
      localStorage.removeItem('admin_token')
      router.push('/login')
    } else if (error.response?.status === 404) {
      ElMessage.error('请求的资源不存在')
    } else if (error.code === 'ECONNABORTED' || error.message.includes('timeout')) {
      ElMessage.error('请求超时，请检查网络连接')
    } else if (!error.response) {
      ElMessage.error('无法连接到服务器，请确保后端服务已启动')
    } else {
      ElMessage.error(error.response?.data?.message || error.message || '网络错误')
    }
    return Promise.reject(error)
  }
)

export const authApi = {
  login: (data) => request.post('/auth/login', data),
  register: (data) => request.post('/auth/register', data),
  getUserInfo: () => request.get('/auth/info'),
  logout: () => request.post('/auth/logout')
}

export const attractionApi = {
  getAll: (page = 1, size = 10, category = '', keyword = '') => {
    let url = `/attractions?page=${page}&size=${size}&admin=true`
    if (category) url += `&category=${encodeURIComponent(category)}`
    if (keyword) url += `&keyword=${encodeURIComponent(keyword)}`
    return request.get(url)
  },
  getPage: (page = 1, size = 10, category = '', keyword = '') => {
    let url = `/attractions?page=${page}&size=${size}&admin=true`
    if (category) url += `&category=${encodeURIComponent(category)}`
    if (keyword) url += `&keyword=${encodeURIComponent(keyword)}`
    return request.get(url)
  },
  getById: (id) => request.get(`/attractions/${id}`),
  search: (keyword) => request.get(`/attractions/search?keyword=${keyword}`),
  save: (data) => request.post('/attractions', data),
  update: (data) => request.put('/attractions', data),
  delete: (id) => request.delete(`/attractions/${id}`),
  count: () => request.get('/attractions/count')
}

export const orderApi = {
  getAll: (page = 1, size = 10) => request.get(`/orders?page=${page}&size=${size}`),
  getPage: (page = 1, size = 10) => request.get(`/orders?page=${page}&size=${size}`),
  getById: (id) => request.get(`/orders/${id}`),
  pay: (orderNo) => request.post(`/orders/pay?orderNo=${orderNo}`),
  cancel: (orderNo) => request.post(`/orders/cancel?orderNo=${orderNo}`),
  use: (orderNo) => request.post(`/orders/use?orderNo=${orderNo}`),
  delete: (id) => request.delete(`/orders/${id}`),
  getStats: () => request.get('/orders/stats')
}

export const userApi = {
  getAll: (page = 1, size = 10, keyword = '') => {
    let url = `/users?page=${page}&size=${size}`
    if (keyword) url += `&keyword=${encodeURIComponent(keyword)}`
    return request.get(url)
  },
  getPage: (page = 1, size = 10, keyword = '') => {
    let url = `/users?page=${page}&size=${size}`
    if (keyword) url += `&keyword=${encodeURIComponent(keyword)}`
    return request.get(url)
  },
  getById: (id) => request.get(`/users/${id}`),
  delete: (id) => request.delete(`/users/${id}`),
  update: (data) => request.put('/users', data),
  count: () => request.get('/users/count')
}

export const momentApi = {
  getAll: (page = 1, size = 10, status = '', keyword = '') => {
    let url = `/moments?page=${page}&size=${size}`
    if (status) url += `&status=${encodeURIComponent(status)}`
    if (keyword) url += `&keyword=${encodeURIComponent(keyword)}`
    return request.get(url)
  },
  getPage: (page = 1, size = 10, status = '', keyword = '') => {
    let url = `/moments?page=${page}&size=${size}`
    if (status) url += `&status=${encodeURIComponent(status)}`
    if (keyword) url += `&keyword=${encodeURIComponent(keyword)}`
    return request.get(url)
  },
  getByStatus: (status, page = 1, size = 10) => request.get(`/moments/status/${status}?page=${page}&size=${size}`),
  approve: (id) => request.post(`/moments/approve/${id}`),
  reject: (id) => request.post(`/moments/reject/${id}`),
  delete: (id) => request.delete(`/moments/${id}`),
  countPending: () => request.get('/moments/count/pending')
}

export const heritageApi = {
  getAll: (page = 1, size = 10, level = '', keyword = '', admin = false) => {
    let url = `/heritage?page=${page}&size=${size}`
    if (admin) url += `&admin=true`
    if (level) url += `&level=${encodeURIComponent(level)}`
    if (keyword) url += `&keyword=${encodeURIComponent(keyword)}`
    return request.get(url)
  },
  getPage: (page = 1, size = 10, level = '', keyword = '', admin = false) => {
    let url = `/heritage?page=${page}&size=${size}`
    if (admin) url += `&admin=true`
    if (level) url += `&level=${encodeURIComponent(level)}`
    if (keyword) url += `&keyword=${encodeURIComponent(keyword)}`
    return request.get(url)
  },
  getById: (id) => request.get(`/heritage/${id}`),
  save: (data) => request.post('/heritage', data),
  update: (data) => request.put('/heritage', data),
  delete: (id) => request.delete(`/heritage/${id}`)
}

export const activityApi = {
  getAll: (page = 1, size = 10, status = '', keyword = '') => {
    let url = `/activities?page=${page}&size=${size}`
    if (status) url += `&status=${encodeURIComponent(status)}`
    if (keyword) url += `&keyword=${encodeURIComponent(keyword)}`
    return request.get(url)
  },
  getPage: (page = 1, size = 10, status = '', keyword = '') => {
    let url = `/activities?page=${page}&size=${size}`
    if (status) url += `&status=${encodeURIComponent(status)}`
    if (keyword) url += `&keyword=${encodeURIComponent(keyword)}`
    return request.get(url)
  },
  getPublished: (page = 1, size = 10) => request.get(`/activities/published?page=${page}&size=${size}`),
  getById: (id) => request.get(`/activities/${id}`),
  save: (data) => request.post('/activities', data),
  update: (data) => request.put('/activities', data),
  delete: (id) => request.delete(`/activities/${id}`),
  publish: (id) => request.post(`/activities/publish/${id}`),
  unpublish: (id) => request.post(`/activities/unpublish/${id}`)
}

export const foodApi = {
  getAll: (page = 1, size = 10, category = '', keyword = '') => {
    let url = `/foods?page=${page}&size=${size}&admin=true`
    if (category) url += `&category=${encodeURIComponent(category)}`
    if (keyword) url += `&keyword=${encodeURIComponent(keyword)}`
    return request.get(url)
  },
  getPage: (page = 1, size = 10, category = '', keyword = '') => {
    let url = `/foods?page=${page}&size=${size}&admin=true`
    if (category) url += `&category=${encodeURIComponent(category)}`
    if (keyword) url += `&keyword=${encodeURIComponent(keyword)}`
    return request.get(url)
  },
  getById: (id) => request.get(`/foods/${id}`),
  save: (data) => request.post('/foods', data),
  update: (data) => request.put('/foods', data),
  delete: (id) => request.delete(`/foods/${id}`)
}

export const ticketApi = {
  getAll: (page = 1, size = 10, type = '', keyword = '') => {
    let url = `/tickets?page=${page}&size=${size}&admin=true`
    if (type) url += `&type=${encodeURIComponent(type)}`
    if (keyword) url += `&keyword=${encodeURIComponent(keyword)}`
    return request.get(url)
  },
  getPage: (page = 1, size = 10, type = '', keyword = '') => {
    let url = `/tickets?page=${page}&size=${size}&admin=true`
    if (type) url += `&type=${encodeURIComponent(type)}`
    if (keyword) url += `&keyword=${encodeURIComponent(keyword)}`
    return request.get(url)
  },
  getById: (id) => request.get(`/tickets/${id}`),
  save: (data) => request.post('/tickets', data),
  update: (data) => request.put('/tickets', data),
  delete: (id) => request.delete(`/tickets/${id}`)
}

export const productApi = {
  getAll: (page = 1, size = 10, category = '', keyword = '') => {
    let url = `/products?page=${page}&size=${size}&admin=true`
    if (category) url += `&category=${encodeURIComponent(category)}`
    if (keyword) url += `&keyword=${encodeURIComponent(keyword)}`
    return request.get(url)
  },
  getPage: (page = 1, size = 10, category = '', keyword = '') => {
    let url = `/products?page=${page}&size=${size}&admin=true`
    if (category) url += `&category=${encodeURIComponent(category)}`
    if (keyword) url += `&keyword=${encodeURIComponent(keyword)}`
    return request.get(url)
  },
  getById: (id) => request.get(`/products/${id}`),
  save: (data) => request.post('/products', data),
  update: (data) => request.put('/products', data),
  delete: (id) => request.delete(`/products/${id}`)
}

export const hotelApi = {
  getAll: (page = 1, size = 10, type = '', keyword = '') => {
    let url = `/hotels?page=${page}&size=${size}&admin=true`
    if (type) url += `&type=${encodeURIComponent(type)}`
    if (keyword) url += `&keyword=${encodeURIComponent(keyword)}`
    return request.get(url)
  },
  getPage: (page = 1, size = 10, type = '', keyword = '') => {
    let url = `/hotels?page=${page}&size=${size}&admin=true`
    if (type) url += `&type=${encodeURIComponent(type)}`
    if (keyword) url += `&keyword=${encodeURIComponent(keyword)}`
    return request.get(url)
  },
  getById: (id) => request.get(`/hotels/${id}`),
  save: (data) => request.post('/hotels', data),
  update: (data) => request.put('/hotels', data),
  delete: (id) => request.delete(`/hotels/${id}`)
}

export const routeApi = {
  getAll: (page = 1, size = 10, difficulty = '', keyword = '', admin = false) => {
    let url = `/routes?page=${page}&size=${size}`
    if (admin) url += `&admin=true`
    if (difficulty) url += `&difficulty=${encodeURIComponent(difficulty)}`
    if (keyword) url += `&keyword=${encodeURIComponent(keyword)}`
    return request.get(url)
  },
  getPage: (page = 1, size = 10, difficulty = '', keyword = '', admin = false) => {
    let url = `/routes?page=${page}&size=${size}`
    if (admin) url += `&admin=true`
    if (difficulty) url += `&difficulty=${encodeURIComponent(difficulty)}`
    if (keyword) url += `&keyword=${encodeURIComponent(keyword)}`
    return request.get(url)
  },
  getById: (id) => request.get(`/routes/${id}`),
  save: (data) => request.post('/routes', data),
  update: (data) => request.put('/routes', data),
  delete: (id) => request.delete(`/routes/${id}`)
}

export const userRouteApi = {
  getAll: (page = 1, size = 10, keyword = '', userId = null, status = '') => {
    let url = `/user-routes?page=${page}&size=${size}`
    if (keyword) url += `&keyword=${encodeURIComponent(keyword)}`
    if (userId) url += `&userId=${userId}`
    if (status) url += `&status=${encodeURIComponent(status)}`
    return request.get(url)
  },
  getPage: (page = 1, size = 10, keyword = '', userId = null, status = '') => {
    let url = `/user-routes?page=${page}&size=${size}`
    if (keyword) url += `&keyword=${encodeURIComponent(keyword)}`
    if (userId) url += `&userId=${userId}`
    if (status) url += `&status=${encodeURIComponent(status)}`
    return request.get(url)
  },
  getByUserId: (userId) => request.get(`/user-routes/my?userId=${userId}`),
  getById: (id) => request.get(`/user-routes/${id}`),
  save: (data) => request.post('/user-routes', data),
  update: (data) => request.put(`/user-routes/${data.id}`, data),
  delete: (id) => request.delete(`/user-routes/${id}`)
}

export const postApi = {
  getAll: (page = 1, size = 10, status = '', keyword = '') => {
    let url = `/posts?page=${page}&size=${size}`
    if (status) url += `&status=${encodeURIComponent(status)}`
    if (keyword) url += `&keyword=${encodeURIComponent(keyword)}`
    return request.get(url)
  },
  getPage: (page = 1, size = 10, status = '', keyword = '') => {
    let url = `/posts?page=${page}&size=${size}`
    if (status) url += `&status=${encodeURIComponent(status)}`
    if (keyword) url += `&keyword=${encodeURIComponent(keyword)}`
    return request.get(url)
  },
  getById: (id) => request.get(`/posts/${id}`),
  save: (data) => request.post('/posts', data),
  update: (data) => request.put('/posts', data),
  delete: (id) => request.delete(`/posts/${id}`),
  approve: (id) => request.post(`/posts/approve/${id}`),
  reject: (id) => request.post(`/posts/reject/${id}`),
  countPending: () => request.get('/posts/count/pending')
}

export const guideApi = {
  getAll: (page = 1, size = 10, status = '', keyword = '') => {
    let url = `/guides?page=${page}&size=${size}`
    if (status !== '') url += `&status=${status}`
    if (keyword) url += `&keyword=${encodeURIComponent(keyword)}`
    return request.get(url)
  },
  getPage: (page = 1, size = 10, auditStatus = '', keyword = '') => {
    let url = `/guides?page=${page}&size=${size}`
    if (auditStatus !== '') url += `&auditStatus=${auditStatus}`
    if (keyword) url += `&keyword=${encodeURIComponent(keyword)}`
    return request.get(url)
  },
  getById: (id) => request.get(`/guides/${id}`),
  save: (data) => request.post('/guides', data),
  update: (data) => request.put('/guides', data),
  delete: (id) => request.delete(`/guides/${id}`),
  countPending: () => request.get('/guides/count/pending')
}

export const journeyApi = {
  getAll: (page = 1, size = 10, keyword = '', userId = '') => {
    let url = `/journeys?page=${page}&size=${size}`
    if (keyword) url += `&keyword=${encodeURIComponent(keyword)}`
    if (userId) url += `&userId=${userId}`
    return request.get(url)
  },
  getPage: (page = 1, size = 10, keyword = '', userId = '') => {
    let url = `/journeys?page=${page}&size=${size}`
    if (keyword) url += `&keyword=${encodeURIComponent(keyword)}`
    if (userId) url += `&userId=${userId}`
    return request.get(url)
  },
  getByUserId: (userId, page = 1, size = 10) => request.get(`/journeys/user/${userId}?page=${page}&size=${size}`),
  getById: (id) => request.get(`/journeys/${id}`),
  save: (data) => request.post('/journeys', data),
  update: (data) => request.put('/journeys', data),
  delete: (id) => request.delete(`/journeys/${id}`)
}

export const ticketOrderApi = {
  getAll: (page = 1, size = 10, status = '', searchKey = '') => {
    let url = `/ticket-orders?page=${page}&size=${size}`
    if (status) url += `&status=${status}`
    if (searchKey) url += `&searchKey=${searchKey}`
    return request.get(url)
  },
  getPage: (page = 1, size = 10, status = '', searchKey = '') => {
    let url = `/ticket-orders?page=${page}&size=${size}`
    if (status) url += `&status=${status}`
    if (searchKey) url += `&searchKey=${searchKey}`
    return request.get(url)
  },
  getById: (id) => request.get(`/ticket-orders/${id}`),
  getByUserId: (userId) => request.get(`/ticket-orders/user/${userId}`),
  create: (data) => request.post('/ticket-orders', data),
  pay: (orderNo) => request.post(`/ticket-orders/pay?orderNo=${orderNo}`),
  cancel: (orderNo) => request.post(`/ticket-orders/cancel?orderNo=${orderNo}`),
  use: (orderNo) => request.post(`/ticket-orders/use?orderNo=${orderNo}`),
  delete: (id) => request.delete(`/ticket-orders/${id}`),
  getStats: () => request.get('/ticket-orders/stats')
}

export const foodOrderApi = {
  getAll: (page = 1, size = 10, status = '', searchKey = '') => {
    let url = `/food-orders?page=${page}&size=${size}`
    if (status) url += `&status=${status}`
    if (searchKey) url += `&searchKey=${searchKey}`
    return request.get(url)
  },
  getPage: (page = 1, size = 10, status = '', searchKey = '') => {
    let url = `/food-orders?page=${page}&size=${size}`
    if (status) url += `&status=${status}`
    if (searchKey) url += `&searchKey=${searchKey}`
    return request.get(url)
  },
  getById: (id) => request.get(`/food-orders/${id}`),
  getByUserId: (userId) => request.get(`/food-orders/user/${userId}`),
  create: (data) => request.post('/food-orders', data),
  pay: (orderNo) => request.post(`/food-orders/pay?orderNo=${orderNo}`),
  cancel: (orderNo) => request.post(`/food-orders/cancel?orderNo=${orderNo}`),
  use: (orderNo) => request.post(`/food-orders/use?orderNo=${orderNo}`),
  delete: (id) => request.delete(`/food-orders/${id}`),
  getStats: () => request.get('/food-orders/stats')
}

export const hotelOrderApi = {
  getAll: (page = 1, size = 10, status = '', searchKey = '') => {
    let url = `/hotel-orders?page=${page}&size=${size}`
    if (status) url += `&status=${status}`
    if (searchKey) url += `&searchKey=${searchKey}`
    return request.get(url)
  },
  getPage: (page = 1, size = 10, status = '', searchKey = '') => {
    let url = `/hotel-orders?page=${page}&size=${size}`
    if (status) url += `&status=${status}`
    if (searchKey) url += `&searchKey=${searchKey}`
    return request.get(url)
  },
  getById: (id) => request.get(`/hotel-orders/${id}`),
  getByUserId: (userId) => request.get(`/hotel-orders/user/${userId}`),
  create: (data) => request.post('/hotel-orders', data),
  pay: (orderNo) => request.post(`/hotel-orders/pay?orderNo=${orderNo}`),
  cancel: (orderNo) => request.post(`/hotel-orders/cancel?orderNo=${orderNo}`),
  checkIn: (orderNo) => request.post(`/hotel-orders/check-in?orderNo=${orderNo}`),
  checkOut: (orderNo) => request.post(`/hotel-orders/check-out?orderNo=${orderNo}`),
  delete: (id) => request.delete(`/hotel-orders/${id}`),
  getStats: () => request.get('/hotel-orders/stats')
}

export const productOrderApi = {
  getAll: (page = 1, size = 10, status = '', searchKey = '') => {
    let url = `/product-orders?page=${page}&size=${size}`
    if (status) url += `&status=${status}`
    if (searchKey) url += `&searchKey=${searchKey}`
    return request.get(url)
  },
  getPage: (page = 1, size = 10, status = '', searchKey = '') => {
    let url = `/product-orders?page=${page}&size=${size}`
    if (status) url += `&status=${status}`
    if (searchKey) url += `&searchKey=${searchKey}`
    return request.get(url)
  },
  getById: (id) => request.get(`/product-orders/${id}`),
  getByUserId: (userId) => request.get(`/product-orders/user/${userId}`),
  create: (data) => request.post('/product-orders', data),
  pay: (orderNo) => request.post(`/product-orders/pay?orderNo=${orderNo}`),
  cancel: (orderNo) => request.post(`/product-orders/cancel?orderNo=${orderNo}`),
  ship: (orderNo) => request.post(`/product-orders/ship?orderNo=${orderNo}`),
  deliver: (orderNo) => request.post(`/product-orders/deliver?orderNo=${orderNo}`),
  delete: (id) => request.delete(`/product-orders/${id}`),
  getStats: () => request.get('/product-orders/stats')
}

export const experienceOrderApi = {
  getAll: (page = 1, size = 10, status = '', searchKey = '') => {
    let url = `/experience-orders?page=${page}&size=${size}`
    if (status) url += `&status=${status}`
    if (searchKey) url += `&searchKey=${searchKey}`
    return request.get(url)
  },
  getPage: (page = 1, size = 10, status = '', searchKey = '') => {
    let url = `/experience-orders?page=${page}&size=${size}`
    if (status) url += `&status=${status}`
    if (searchKey) url += `&searchKey=${searchKey}`
    return request.get(url)
  },
  getById: (id) => request.get(`/experience-orders/${id}`),
  getByUserId: (userId) => request.get(`/experience-orders/user/${userId}`),
  confirm: (orderNo) => request.post(`/experience-orders/confirm?orderNo=${orderNo}`),
  complete: (orderNo) => request.post(`/experience-orders/complete?orderNo=${orderNo}`),
  cancel: (orderNo, reason) => request.post(`/experience-orders/cancel?orderNo=${orderNo}${reason ? '&reason=' + encodeURIComponent(reason) : ''}`),
  delete: (id) => request.delete(`/experience-orders/${id}`),
  getStats: () => request.get('/experience-orders/stats')
}

export const dashboardApi = {
  getStats: () => request.get('/dashboard/stats')
}

export const mapApi = {
  getData: () => request.get('/map/data')
}

export const bannerApi = {
  getAll: (position = '') => {
    let url = '/banners'
    if (position) url += `?position=${encodeURIComponent(position)}`
    return request.get(url)
  },
  getAllAdmin: () => request.get('/banners/admin'),
  getById: (id) => request.get(`/banners/${id}`),
  save: (data) => request.post('/banners', data),
  update: (data) => request.put('/banners', data),
  delete: (id) => request.delete(`/banners/${id}`)
}

export const aiKnowledgeApi = {
  getAll: (page = 1, size = 10, params = {}) => {
    let url = `/ai-knowledge?page=${page}&size=${size}`
    if (params.keyword) url += `&keyword=${encodeURIComponent(params.keyword)}`
    if (params.category) url += `&category=${encodeURIComponent(params.category)}`
    return request.get(url)
  },
  getPage: (page = 1, size = 10, params = {}) => {
    let url = `/ai-knowledge?page=${page}&size=${size}`
    if (params.keyword) url += `&keyword=${encodeURIComponent(params.keyword)}`
    if (params.category) url += `&category=${encodeURIComponent(params.category)}`
    return request.get(url)
  },
  getById: (id) => request.get(`/ai-knowledge/${id}`),
  save: (data) => request.post('/ai-knowledge', data),
  update: (data) => request.put('/ai-knowledge', data),
  delete: (id) => request.delete(`/ai-knowledge/${id}`),
  batchEnabled: (ids, enabled) => request.put('/ai-knowledge/batch-enabled', { ids, enabled }),
  batchDelete: (ids) => request.delete('/ai-knowledge/batch', { data: ids })
}

export const aiChatApi = {
  chat: (data) => request.post('/ai/chat', data),
  createSession: (data) => request.post('/chat/sessions', data),
  getSessions: (page = 1, size = 20) => request.get(`/chat/sessions?page=${page}&size=${size}`),
  getSessionMessages: (sessionId) => request.get(`/chat/sessions/${sessionId}/messages`),
  getAll: (page = 1, size = 10, params = {}) => {
    let url = `/ai-chat?page=${page}&size=${size}`
    if (params.flagged !== undefined && params.flagged !== '') url += `&flagged=${params.flagged}`
    if (params.keyword) url += `&keyword=${encodeURIComponent(params.keyword)}`
    if (params.today) url += `&today=true`
    return request.get(url)
  },
  getPage: (page = 1, size = 10, params = {}) => {
    let url = `/ai-chat?page=${page}&size=${size}`
    if (params.flagged !== undefined && params.flagged !== '') url += `&flagged=${params.flagged}`
    if (params.keyword) url += `&keyword=${encodeURIComponent(params.keyword)}`
    if (params.today) url += `&today=true`
    return request.get(url)
  },
  getStats: () => request.get('/ai-chat/stats'),
  getAnalytics: () => request.get('/ai-chat/analytics'),
  getAdvancedAnalytics: () => request.get('/ai-chat/analytics/advanced'),
  toggleFlag: (id, flagged) => request.put(`/ai-chat/${id}/flag`, { flagged }),
  rateChat: (id, rating) => request.put(`/ai-chat/${id}/rate`, { rating }),
  hotQuestions: () => request.get('/ai-chat/hot-questions'),
  lowRatedChats: () => request.get('/ai-chat/low-rated'),
  ratingStats: () => request.get('/ai-chat/rating-stats')
}

export const sensitiveWordApi = {
  getAll: () => request.get('/sensitive-words'),
  getStats: () => request.get('/sensitive-words/stats'),
  save: (data) => request.post('/sensitive-words', data),
  update: (data) => request.put('/sensitive-words', data),
  delete: (id) => request.delete(`/sensitive-words/${id}`),
  check: (text) => request.post('/sensitive-words/check', { text })
}

export const commentApi = {
  getAll: (page = 1, size = 10, status = '', keyword = '') => {
    let url = `/comments/admin?page=${page}&size=${size}`
    if (status !== '') url += `&status=${status}`
    if (keyword) url += `&keyword=${encodeURIComponent(keyword)}`
    return request.get(url)
  },
  getPage: (page = 1, size = 10, status = '', keyword = '') => {
    let url = `/comments/admin?page=${page}&size=${size}`
    if (status !== '') url += `&status=${status}`
    if (keyword) url += `&keyword=${encodeURIComponent(keyword)}`
    return request.get(url)
  },
  getByPostId: (postId, page = 1, size = 20) => request.get(`/posts/${postId}/comments?page=${page}&size=${size}`),
  delete: (id) => request.delete(`/comments/${id}`),
  like: (id) => request.post(`/comments/${id}/like`),
  approve: (id) => request.post(`/comments/${id}/approve`),
  reject: (id) => request.post(`/comments/${id}/reject`),
  count: () => request.get('/comments/count'),
  getRecent: (limit = 20) => request.get(`/comments/recent?limit=${limit}`)
}

export const miniProgramChatApi = {
  getSessions: (page = 1, size = 10, params = {}) => {
    let url = `/mini-program-chat/sessions?page=${page}&size=${size}`
    if (params.keyword) url += `&keyword=${encodeURIComponent(params.keyword)}`
    if (params.today) url += `&today=true`
    return request.get(url)
  },
  getPage: (page = 1, size = 10, params = {}) => {
    let url = `/mini-program-chat/sessions?page=${page}&size=${size}`
    if (params.keyword) url += `&keyword=${encodeURIComponent(params.keyword)}`
    if (params.today) url += `&today=true`
    return request.get(url)
  },
  getSessionMessages: (sessionId) => request.get(`/mini-program-chat/sessions/${sessionId}/messages`),
  getStats: () => request.get('/mini-program-chat/stats')
}

export default request
export { uploadRequest }
