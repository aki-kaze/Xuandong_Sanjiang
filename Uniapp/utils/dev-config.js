/**
 * 开发配置
 * 用于控制开发阶段的功能开关
 */

export default {
  // 是否使用本地知识库（避免域名校验问题）
  // true: 使用本地知识库，无需后端服务器
  // false: 使用在线 AI，需要启动后端并关闭域名校验
  USE_LOCAL_AI: false,

  // 千问大模型 API 配置
  QWEN: {
    API_KEY: '',
    MODEL: 'qwen-turbo',
    BASE_URL: 'https://dashscope.aliyuncs.com/compatible-mode/v1/chat/completions',
    MAX_TOKENS: 2048,
    TEMPERATURE: 0.7
  },

  // 是否启用调试模式
  DEBUG: true,

  // 是否模拟数据（当后端未启动时）
  MOCK_DATA: false,

  // API 超时时间（毫秒）
  API_TIMEOUT: 30000,

  // 提示信息
  tips: {
    domainError: '请在微信开发者工具中关闭域名校验：详情 -> 本地设置 -> 勾选"不校验合法域名"',
    serverError: '后端服务器未启动，请先运行 server/start.bat',
    networkError: '网络连接失败，请检查网络设置'
  }
}
