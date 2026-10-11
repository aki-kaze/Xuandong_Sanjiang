/**
 * 小程序性能优化工具
 */
import config from './config.js'

// 数据缓存
const cache = {}

/**
 * 设置缓存
 */
export function setCache(key, data, expireTime = null) {
  const ttl = expireTime || config.performance.cacheTime
  cache[key] = {
    data: data,
    timestamp: Date.now(),
    expireTime: ttl
  }
  try {
    uni.setStorageSync('cache_' + key, cache[key])
  } catch (e) {
    console.log('缓存设置失败:', key)
  }
}

/**
 * 获取缓存
 */
export function getCache(key, useStorage = true) {
  try {
    let cached = null
    if (useStorage) {
      cached = uni.getStorageSync('cache_' + key)
    }
    if (!cached) {
      cached = cache[key]
    }
    if (!cached) return null
    
    // 检查是否过期
    const now = Date.now()
    if (now - cached.timestamp > cached.expireTime) {
      // 已过期，清除缓存
      if (useStorage) {
        uni.removeStorageSync('cache_' + key)
      }
      delete cache[key]
      return null
    }
    
    return cached.data
  } catch (e) {
    return null
  }
}

/**
 * 清除缓存
 */
export function clearCache(key) {
  if (key) {
    try {
      uni.removeStorageSync('cache_' + key)
      delete cache[key]
    } catch (e) {}
  } else {
    // 清除所有缓存
    try {
      const keys = uni.getStorageInfoSync().keys
      keys.forEach(k => {
        if (k.startsWith('cache_')) {
          uni.removeStorageSync(k)
        }
      })
    } catch (e) {}
    for (const k in cache) {
      delete cache[k]
    }
  }
}

/**
 * 图片懒加载 mixin
 */
export const imageLazyLoad = {
  data() {
    return {
      lazyLoadImages: {}
    }
  },
  methods: {
    // 图片进入视口时加载
    onImageEnter(key) {
      if (!this.lazyLoadImages[key]) {
        this.$set(this.lazyLoadImages, key, true)
      }
    },
    // 获取图片 URL（懒加载）
    getLazyImage(url, key) {
      return this.lazyLoadImages[key] ? url : ''
    }
  }
}

/**
 * 请求节流（避免重复请求）
 */
const requestLocks = {}
export function requestWithLock(key, fn, lockTime = 1000) {
  if (requestLocks[key]) {
    return Promise.reject(new Error('请求进行中'))
  }
  
  requestLocks[key] = true
  setTimeout(() => {
    delete requestLocks[key]
  }, lockTime)
  
  return fn().finally(() => {
    delete requestLocks[key]
  })
}

/**
 * 预加载数据
 */
export function preloadData(key, fn) {
  if (!config.performance.preloadEnabled) {
    return fn()
  }
  
  // 检查是否有缓存
  const cached = getCache(key)
  if (cached) {
    return Promise.resolve(cached)
  }
  
  // 预加载
  return fn().then(data => {
    setCache(key, data)
    return data
  })
}

/**
 * 列表分页加载
 */
export function loadListWithPagination(loadFn, page = 1, size = null) {
  const pageSize = size || config.performance.pageSize
  return loadFn(page, pageSize)
}

/**
 * 优化图片 URL（添加压缩参数）
 */
export function optimizeImageUrl(url, width = 300, height = 0) {
  if (!url) return ''
  // 如果是本地路径或 dataURI，直接返回
  if (url.startsWith('/') || url.startsWith('data:')) {
    return url
  }
  // 添加图片压缩参数（针对支持云存储的图片服务）
  const separator = url.includes('?') ? '&' : '?'
  return `${url}${separator}imageView2/${width > 0 ? `w/${width}` : ''}${height > 0 ? `/h/${height}` : ''}/q/${Math.round(config.performance.imageQuality * 10)}`
}

/**
 * 防抖函数
 */
export function debounce(fn, delay = 300) {
  let timer = null
  return function(...args) {
    if (timer) clearTimeout(timer)
    timer = setTimeout(() => {
      fn.apply(this, args)
    }, delay)
  }
}

/**
 * 节流函数
 */
export function throttle(fn, delay = 500) {
  let lastTime = 0
  return function(...args) {
    const now = Date.now()
    if (now - lastTime >= delay) {
      lastTime = now
      fn.apply(this, args)
    }
  }
}

/**
 * 性能监控
 */
export function measurePerformance(label, fn) {
  const start = performance.now()
  const result = fn()
  const end = performance.now()
  console.log(`[性能] ${label}: ${(end - start).toFixed(2)}ms`)
  return result
}

/**
 * 异步性能监控
 */
export async function measurePerformanceAsync(label, fn) {
  const start = performance.now()
  const result = await fn()
  const end = performance.now()
  console.log(`[性能] ${label}: ${(end - start).toFixed(2)}ms`)
  return result
}
