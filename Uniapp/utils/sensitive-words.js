// 敏感词检测工具 - 优先使用后端API，降级使用本地词库
import { sensitiveWordApi } from './api.js'

// 本地敏感词库（后端不可用时降级使用）
const SENSITIVE_WORDS = [
  // 政治敏感
  '政治敏感词',
  '政府',
  '国家领导人',
  '党',
  '总书记',
  '主席',
  '国务院',
  
  // 色情低俗
  '色情',
  '淫秽',
  '裸体',
  '成人',
  '性',
  '性爱',
  '性交',
  '色情服务',
  '黄色',
  
  // 暴力恐怖
  '暴力',
  '恐怖',
  '杀人',
  '爆炸',
  '犯罪',
  '犯罪活动',
  '抢劫',
  '绑架',
  '谋杀',
  '纵火',
  '恐怖袭击',
  
  // 广告营销
  '加微信',
  '加 QQ',
  '刷单',
  '兼职',
  '赚钱',
  '投资',
  '理财',
  '贷款',
  '信用卡',
  '代开发票',
  '办证',
  '卖枪',
  '卖药',
  
  // 辱骂诽谤
  '傻逼',
  'sb',
  '垃圾',
  '废物',
  '蠢货',
  '白痴',
  '脑残',
  '弱智',
  '去死',
  '杂种',
  '狗娘养的',
  
  // 其他违规内容
  '赌博',
  '毒品',
  '诈骗',
  '违法',
  '禁词',
  '敏感词',
  '邪教',
  '法轮功',
  '传销',
  '高利贷',
  '六合彩',
  '博彩',
  '窃听',
  '偷拍'
]

// 变体检测（常见变形）
const VARIANT_WORDS = {
  'sb': '傻逼',
  'SB': '傻逼',
  '煞笔': '傻逼',
  '傻 B': '傻逼',
  '傻b': '傻逼',
  '加微': '加微信',
  '薇信': '微信',
  'V 信': '微信',
  '威信': '微信',
  'du品': '毒品',
  '毒pin': '毒品',
  '色请': '色情',
  '色q': '色情',
  'se情': '色情',
  '犯zui': '犯罪',
  'fan罪': '犯罪',
  '赌bo': '赌博',
  'du博': '赌博',
  '骗钱': '诈骗',
  'zp': '诈骗',
  '炸骗': '诈骗'
}

/**
 * 检测文本中是否包含敏感词（优先使用后端API）
 * @param {string} text - 待检测的文本
 * @returns {object} - { hasSensitive: boolean, sensitiveWords: string[] }
 */
export async function checkSensitiveWordsAsync(text) {
  if (!text) {
    return { hasSensitive: false, sensitiveWords: [] }
  }

  try {
    // 优先使用后端API检测
    const res = await sensitiveWordApi.check(text)
    if (res && res.hasSensitive) {
      return {
        hasSensitive: true,
        sensitiveWords: res.words || []
      }
    }
    return { hasSensitive: false, sensitiveWords: [] }
  } catch (e) {
    // 后端不可用，降级使用本地词库
    console.warn('后端敏感词检测不可用，使用本地词库')
    return checkSensitiveWordsLocal(text)
  }
}

/**
 * 本地敏感词检测（同步，降级方案）
 */
function checkSensitiveWordsLocal(text) {
  if (!text) {
    return { hasSensitive: false, sensitiveWords: [] }
  }

  const foundWords = []
  const lowerText = text.toLowerCase()

  for (const word of SENSITIVE_WORDS) {
    if (text.includes(word) || lowerText.includes(word.toLowerCase())) {
      foundWords.push(word)
    }
  }

  for (const [variant, original] of Object.entries(VARIANT_WORDS)) {
    if (text.includes(variant) || lowerText.includes(variant.toLowerCase())) {
      foundWords.push(variant)
    }
  }

  return {
    hasSensitive: foundWords.length > 0,
    sensitiveWords: [...new Set(foundWords)]
  }
}

/**
 * 检测文本中是否包含敏感词（保持向后兼容的同步版本）
 * @param {string} text - 待检测的文本
 * @returns {object} - { hasSensitive: boolean, sensitiveWords: string[] }
 */
export function checkSensitiveWords(text) {
  return checkSensitiveWordsLocal(text)
}

/**
 * 过滤敏感词（替换为*）
 * @param {string} text - 待过滤的文本
 * @returns {string} - 过滤后的文本
 */
export function filterSensitiveWords(text) {
  if (!text) return text

  let result = text

  // 过滤敏感词库
  for (const word of SENSITIVE_WORDS) {
    const regex = new RegExp(word, 'g')
    result = result.replace(regex, '*'.repeat(word.length))
  }

  // 过滤变体词
  for (const variant of Object.keys(VARIANT_WORDS)) {
    const regex = new RegExp(variant, 'gi')
    result = result.replace(regex, '*'.repeat(variant.length))
  }

  return result
}

/**
 * 获取敏感词提示信息
 * @param {string[]} sensitiveWords - 敏感词列表
 * @returns {string} - 提示信息
 */
export function getSensitiveWordMessage(sensitiveWords) {
  if (!sensitiveWords || sensitiveWords.length === 0) {
    return ''
  }
  return `内容包含敏感词：${sensitiveWords.join('、')}，请修改后重新提交`
}
