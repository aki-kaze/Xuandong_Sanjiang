// 环境配置
const ENV = {
  // 开发环境
  development: {
    baseURL: 'http://127.0.0.1:8080',
    timeout: 10000
  },
  // 生产环境 - 请替换为实际的服务器地址
  production: {
    baseURL: 'https://your-server-domain.com',
    timeout: 10000
  }
}

// 当前环境（可以根据需要切换）
const currentEnv = 'development'

// 腾讯地图配置
const TENCENT_MAP_KEY = 'QJMBZ-E7RCZ-FCFXR-7FS2E-WRIN5-ETBOK'

// 性能优化配置
const PERFORMANCE = {
  // 图片懒加载阈值（px）
  imageLazyLoadThreshold: 300,
  // 列表分页大小
  pageSize: 20,
  // 数据缓存时间（毫秒）
  cacheTime: 5 * 60 * 1000, // 5 分钟
  // 预加载开关
  preloadEnabled: true,
  // 图片压缩质量（0.1-1.0）
  imageQuality: 0.8,
  // 请求超时时间（毫秒）
  requestTimeout: 8000,
  // 并发请求限制
  maxConcurrentRequests: 3
}

export default {
  ...ENV[currentEnv],
  env: currentEnv,
  mapKey: TENCENT_MAP_KEY,
  performance: PERFORMANCE
}
