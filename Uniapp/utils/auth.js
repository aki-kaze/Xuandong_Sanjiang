import config from './config.js'

// API 配置
const BASE_URL = config.baseURL
// 是否开启模拟数据（后端未就绪时）
const MOCK_MODE = false

// 随机头像图片池
const AVATAR_POOL = [
  'https://cdn.phototourl.com/free/2026-03-24-955d494f-d01c-4911-87ea-2e684f9a3f29.jpg',
  'https://cdn.phototourl.com/free/2026-03-24-9da6f2f7-f9d3-46ac-9819-695e797b76e3.jpg',
  'https://cdn.phototourl.com/member/2026-03-24-3cd4dc5f-9c84-4b60-b074-1dba51407d7e.png',
  'https://cdn.phototourl.com/member/2026-03-24-1c287f60-cae7-454e-b829-926032cddd96.png',
  'https://cdn.phototourl.com/member/2026-03-24-f1ef810e-bb9a-439b-bc19-526382cc971b.jpg'
]

// 获取用户固定头像（优先使用登录用户的头像）
export const getRandomAvatar = () => {
  const userInfo = uni.getStorageSync('userInfo')
  if (userInfo && userInfo.avatar) {
    return userInfo.avatar
  }
  return AVATAR_POOL[0]
}

// 获取用户信息
export const getUserInfo = () => {
  const userInfo = uni.getStorageSync('userInfo')
  return userInfo || { isLogin: false, nickname: '游客' }
}

// 检查是否为游客模式
export const isGuestMode = () => {
  const userInfo = uni.getStorageSync('userInfo')
  return !userInfo || !userInfo.isLogin || userInfo.loginType === 'guest'
}

// 检查是否已登录（非游客）
export const isLoggedIn = () => {
  const userInfo = uni.getStorageSync('userInfo')
  const token = uni.getStorageSync('token')
  return userInfo && userInfo.isLogin && userInfo.loginType !== 'guest' && token
}

// 需要登录才能操作的功能，游客点击时提示登录
export const requireLogin = (message = '请先登录') => {
  if (isGuestMode()) {
    uni.showModal({
      title: '提示',
      content: `${message}，是否前往登录？`,
      confirmText: '去登录',
      cancelText: '再看看',
      success: (res) => {
        if (res.confirm) {
          uni.navigateTo({ url: '/pages/login/login' })
        }
      }
    })
    return false
  }
  return true
}

// 异步验证登录状态（同时检查后端用户是否被禁用）
// 返回 true 表示可以继续操作，false 表示需要登录或已被禁用
export const checkLoginStatus = async () => {
  if (isGuestMode()) return false
  try {
    await userApi.getMe({ silent: true })
    return true
  } catch (e) {
    // 401 被禁用或过期，拦截器已处理跳转和提示
    return false
  }
}

// 设置用户信息
export const setUserInfo = (userInfo) => {
  uni.setStorageSync('userInfo', userInfo)
  // 确保 token 被保存
  if (userInfo.token) {
    uni.setStorageSync('token', userInfo.token)
  }
}

// 退出登录
export const logout = () => {
  uni.removeStorageSync('token')
  uni.removeStorageSync('userInfo')
}

// 请求拦截器
const request = (options) => {
  return new Promise((resolve, reject) => {
    // 模拟数据模式：直接返回模拟数据，不发起真实请求
    if (MOCK_MODE) {
      const mockData = getMockData(options.url, options.method, options.data)
      if (mockData) {
        resolve(mockData)
        return
      }
    }

    const silent = options.silent || false
    const token = uni.getStorageSync('token')
    console.log('[API 请求]', options.method, options.url, 'Token:', token ? '已设置' : '未设置')

    uni.request({
      url: BASE_URL + options.url,
      method: options.method || 'GET',
      data: options.data || {},
      header: {
        'Content-Type': 'application/json',
        'Authorization': token ? `Bearer ${token}` : ''
      },
      timeout: 15000, // 增加超时时间（15 秒）
      success: (res) => {
        console.log('[API 响应]', options.url, '状态码:', res.statusCode)
        // HTTP 状态码 200
        if (res.statusCode === 200) {
          if (res.data.code === 200) {
            // 保存token（如果返回了token）
            if (res.data.data && res.data.data.token) {
              uni.setStorageSync('token', res.data.data.token)
            }
            resolve(res.data.data)
          } else {
            // 业务错误（如参数错误、账号密码错误）
            const msg = res.data.message || '请求失败'
            // 判断是否为账号被禁用
            if (res.data.code === 401 && msg.includes('禁用')) {
              uni.showToast({
                title: '账号已被禁用，请联系管理员',
                icon: 'none',
                duration: 2000
              })
              // 清除本地登录信息
              uni.removeStorageSync('token')
              uni.removeStorageSync('userInfo')
              setTimeout(() => {
                uni.navigateTo({
                  url: '/pages/login/login'
                })
              }, 2000)
              reject(res.data)
              return
            }
            // 判断是否为登录过期
            if (res.data.code === 401) {
              uni.showToast({
                title: '登录已过期，请重新登录',
                icon: 'none'
              })
              uni.removeStorageSync('token')
              uni.removeStorageSync('userInfo')
              setTimeout(() => {
                uni.navigateTo({
                  url: '/pages/login/login'
                })
              }, 1000)
              reject(res.data)
              return
            }
            uni.showToast({
              title: msg,
              icon: 'none',
              duration: 2000
            })
            reject(res.data)
          }
        } 
        // 401未授权
        else if (res.statusCode === 401) {
          // 判断是否为账号被禁用
          const msg = res.data?.message || ''
          if (msg.includes('禁用')) {
            uni.showToast({
              title: '账号已被禁用，请联系管理员',
              icon: 'none',
              duration: 2000
            })
            // 清除本地登录信息
            uni.removeStorageSync('token')
            uni.removeStorageSync('userInfo')
            setTimeout(() => {
              uni.navigateTo({
                url: '/pages/login/login'
              })
            }, 2000)
            reject(res)
            return
          }
          uni.showToast({
            title: '登录已过期，请重新登录',
            icon: 'none'
          })
          // 清除过期token
          uni.removeStorageSync('token')
          uni.removeStorageSync('userInfo')
          // 延迟跳转，避免提示框被覆盖
          setTimeout(() => {
            uni.navigateTo({
              url: '/pages/login/login'
            })
          }, 1000)
          reject(res)
        } 
        // 其他HTTP错误
        else {
          uni.showToast({
            title: `请求错误：${res.statusCode}`,
            icon: 'none'
          })
          reject(res)
        }
      },
      fail: (err) => {
        // 网络错误（连接拒绝/超时等）
        console.warn('网络请求失败:', err)
        if (!silent) {
          uni.showToast({
            title: '网络连接失败，请检查网络或稍后重试',
            icon: 'none'
          })
        }
        reject(err)
      }
    })
  })
}

// 模拟数据生成函数（后端未就绪时使用）
const getMockData = (url, method, data) => {
  // uni一键登录模拟数据
  if (url === '/api/auth/univerify/login' && method === 'POST') {
    return {
      id: 'univerify_' + Date.now(),
      phone: data.phone || ('139' + Math.floor(Math.random() * 100000000)),
      nickname: '侗乡旅行者' + Math.floor(Math.random() * 1000),
      avatar: '',
      token: 'mock_univerify_token_' + Date.now()
    }
  }

  // 账号密码登录模拟数据
  if (url === '/api/users/login' && method === 'POST') {
    return {
      id: 'phone_' + Date.now(),
      phone: data.phone,
      nickname: '侗乡旅行者',
      avatar: '',
      token: 'mock_phone_token_' + Date.now()
    }
  }

  // 注册模拟数据
  if (url === '/api/users/register' && method === 'POST') {
    return {
      id: 'register_' + Date.now(),
      phone: data.phone,
      nickname: data.nickname || '新用户' + Math.floor(Math.random() * 1000),
      token: 'mock_register_token_' + Date.now()
    }
  }

  // 验证码发送模拟数据
  if (url === '/api/users/verify-code' && method === 'POST') {
    return {
      success: true,
      message: '验证码已发送'
    }
  }

  // 游客登录模拟数据
  if (url === '/api/users/guest' && method === 'POST') {
    return {
      token: 'guest_mock_' + Date.now(),
      isGuest: true,
      nickname: '游客',
      message: '游客模式：只能查看信息，登录后可使用完整功能'
    }
  }

  // 用户信息模拟数据
  if (url === '/api/users/me' && method === 'GET') {
    const userInfo = uni.getStorageSync('userInfo') || {}

    // 处理游客 token
    if (userInfo.loginType === 'guest') {
      return {
        nickname: '游客',
        avatar: '',
        phone: ''
      }
    }

    return {
      id: userInfo.userId || userInfo.id || 'mock_user_' + Date.now(),
      phone: userInfo.phone || '13800000000',
      nickname: userInfo.nickname || '侗乡旅行者',
      avatar: userInfo.avatar || '',
      name: userInfo.name || null,
      gender: userInfo.gender !== undefined ? userInfo.gender : 0
    }
  }

  // 更新用户信息模拟数据
  if (url === '/api/users' && method === 'PUT') {
    const userInfo = uni.getStorageSync('userInfo') || {}
    const updatedUserInfo = {
      ...userInfo,
      id: data.id,
      nickname: data.nickname,
      name: data.name,
      gender: data.gender,
      avatar: data.avatar
    }
    uni.setStorageSync('userInfo', updatedUserInfo)
    return {
      message: '更新成功'
    }
  }

  // 修改密码模拟数据
  if (url === '/api/users/change-password' && method === 'POST') {
    return {
      message: '密码修改成功'
    }
  }

  // 通用列表模拟数据（景点/美食/非遗等）
  if (method === 'GET' && (url.includes('/api/attractions') || url.includes('/api/foods') || url.includes('/api/heritage') || url.includes('/api/routes') || url.includes('/api/hotels') || url.includes('/api/posts'))) {
    return {
      list: Array.from({ length: 10 }, (_, i) => ({
        id: i + 1,
        name: `模拟${url.includes('attractions') ? '景点' : url.includes('foods') ? '美食' : url.includes('heritage') ? '非遗' : url.includes('routes') ? '路线' : url.includes('hotels') ? '酒店' : url.includes('tickets') ? '门票' : '帖子'}${i + 1}`,
        cover: 'https://via.placeholder.com/300x200',
        desc: '这是模拟的内容描述，后端就绪后会替换为真实数据',
        price: (i + 1) * 10,
        create_time: new Date().toISOString()
      })),
      total: 50,
      page: 1,
      limit: 10
    }
  }

  // 通用详情模拟数据
  if (method === 'GET' && (url.includes('/api/attractions/') || url.includes('/api/foods/') || url.includes('/api/heritage/') || url.includes('/api/routes/') || url.includes('/api/hotels/') || url.includes('/api/tickets/') || url.includes('/api/posts/') || url.includes('/api/users/') || url.includes('/api/orders/'))) {
    const id = url.split('/').pop()
    return {
      id,
      name: `模拟详情${id}`,
      cover: 'https://via.placeholder.com/600x400',
      desc: '这是模拟的详情内容，包含丰富的信息展示，后端就绪后会替换为真实数据',
      price: Math.floor(Math.random() * 100) + 10,
      create_time: new Date().toISOString(),
      content: '<p>模拟的富文本内容</p>'
    }
  }

  // 订单相关模拟数据
  if (url.includes('/api/orders')) {
    if (method === 'POST' && url === '/api/orders') {
      return {
        id: 'order_' + Date.now(),
        order_no: 'ORD' + Date.now(),
        status: 'pending',
        create_time: new Date().toISOString()
      }
    } else if (method === 'PUT' && (url.includes('/status') || url.includes('/cancel'))) {
      return {
        success: true,
        message: '操作成功'
      }
    }
  }

  // 帖子点赞模拟数据
  if (url.includes('/api/posts/') && url.includes('/like') && method === 'POST') {
    return {
      success: true,
      message: '点赞成功',
      like_count: Math.floor(Math.random() * 100) + 1
    }
  }

  // 未匹配到模拟数据时返回null（走真实请求）
  return null
}

// 景点API
export const attractionApi = {
  getList: (page = 1, limit = 10, category = '') => request({
    url: `/api/attractions?page=${page}&limit=${limit}${category ? '&category=' + category : ''}`,
    method: 'GET'
  }),
  getDetail: (id) => request({
    url: `/api/attractions/${id}`,
    method: 'GET'
  })
}

// 订单API
export const orderApi = {
  getList: (page = 1, limit = 10, status = '', type = '') => request({
    url: `/api/orders?page=${page}&limit=${limit}${status ? '&status=' + status : ''}${type ? '&type=' + type : ''}`,
    method: 'GET'
  }),
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
  cancel: (id) => request({
    url: `/api/orders/${id}/cancel`,
    method: 'PUT'
  })
}

// 用户API
export const userApi = {
  getMe: () => request({
    url: '/api/users/me',
    method: 'GET'
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
  guestLogin: () => request({
    url: '/api/users/guest',
    method: 'POST'
  }),
  verifyCode: (data) => request({
    url: '/api/users/verify-code',
    method: 'POST',
    data
  }),
  update: (id, data) => request({
    url: `/api/users/${id}`,
    method: 'PUT',
    data
  })
}

// 认证 API
export const authApi = {
  // uni 一键登录（传递手机号、token等参数）
  univerifyLogin: (data) => request({
    url: '/api/auth/univerify/login',
    method: 'POST',
    data
  })
}

// 社区帖子API
export const postApi = {
  getList: (page = 1, limit = 10, user_id = '') => request({
    url: `/api/posts?page=${page}&limit=${limit}${user_id ? '&user_id=' + user_id : ''}`,
    method: 'GET'
  }),
  getDetail: (id) => request({
    url: `/api/posts/${id}`,
    method: 'GET'
  }),
  create: (data) => request({
    url: '/api/posts',
    method: 'POST',
    data
  }),
  update: (id, data) => request({
    url: `/api/posts/${id}`,
    method: 'PUT',
    data
  }),
  like: (id) => request({
    url: `/api/posts/${id}/like`,
    method: 'POST'
  }),
  delete: (id) => request({
    url: `/api/posts/${id}`,
    method: 'DELETE'
  })
}

// 非遗API
export const heritageApi = {
  getList: (page = 1, limit = 10, level = '') => request({
    url: `/api/heritage?page=${page}&limit=${limit}${level ? '&level=' + level : ''}`,
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
    url: `/api/foods?page=${page}&limit=${limit}${category ? '&category=' + category : ''}`,
    method: 'GET'
  }),
  getDetail: (id) => request({
    url: `/api/foods/${id}`,
    method: 'GET'
  })
}

// 路线API
export const routeApi = {
  getList: (page = 1, limit = 10, difficulty = '') => request({
    url: `/api/routes?page=${page}&limit=${limit}${difficulty ? '&difficulty=' + difficulty : ''}`,
    method: 'GET'
  }),
  getDetail: (id) => request({
    url: `/api/routes/${id}`,
    method: 'GET'
  })
}

// 酒店API
export const hotelApi = {
  getList: (page = 1, limit = 10, type = '') => request({
    url: `/api/hotels?page=${page}&limit=${limit}${type ? '&type=' + type : ''}`,
    method: 'GET'
  }),
  getDetail: (id) => request({
    url: `/api/hotels/${id}`,
    method: 'GET'
  })
}

// 默认导出
export default {
  request,
  getUserInfo,
  setUserInfo,
  logout,
  getRandomAvatar,
  isGuestMode,
  isLoggedIn,
  requireLogin,
  checkLoginStatus
}