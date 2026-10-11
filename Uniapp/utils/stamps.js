/**
 * 非遗护照 - 集章管理工具
 * 基于 localStorage 存储集章记录
 */

// 6 个非遗印章定义
export const STAMPS = [
  { 
    id: 1, 
    name: '侗族大歌', 
    icon: '/static/icons/explore.png', 
    color: '#4A90E2', 
    unlockMethods: ['location', 'booking'], 
    image: 'https://cdn.phototourl.com/free/2026-03-23-75376aa0-ba75-4269-8839-bbe6aca040a6.jpg' // 侗族大歌表演场景
  },
  { 
    id: 2, 
    name: '侗族风雨桥', 
    icon: '/static/icons/map.png', 
    color: '#2C3E50', 
    unlockMethods: ['location', 'booking'], 
    image: 'https://cdn.phototourl.com/free/2026-03-23-656b106c-9ce1-4ef1-93b3-e205cd8267f4.png' // 程阳永济风雨桥
  },
  { 
    id: 3, 
    name: '侗族鼓楼', 
    icon: '/static/icons/heritage.png', 
    color: '#2C3E50', 
    unlockMethods: ['location', 'booking'], 
    image: 'https://cdn.phototourl.com/free/2026-03-23-4df6b38e-a4a7-4898-a867-eababd51019a.jpg' // 三江鼓楼
  },
  { 
    id: 4, 
    name: '侗锦织造技艺', 
    icon: '/static/icons/love.png', 
    color: '#4A90E2', 
    unlockMethods: ['location', 'booking'], 
    image: 'https://cdn.phototourl.com/free/2026-03-23-83a39e17-b2f9-4ff8-bced-41e9c064e9fe.jpg' // 侗锦织造
  },
  { 
    id: 5, 
    name: '侗族芦笙节', 
    icon: '/static/icons/ticket.png', 
    color: '#FF7F50', 
    unlockMethods: ['location', 'booking'], 
    image: 'https://cdn.phototourl.com/free/2026-03-23-4413c254-1f17-46a6-a102-78ffbd9c05bc.jpg' // 芦笙节表演
  },
  { 
    id: 6, 
    name: '侗族多耶', 
    icon: '/static/icons/discover.png', 
    color: '#FF7F50', 
    unlockMethods: ['location', 'booking'], 
    image: 'https://cdn.phototourl.com/free/2026-03-23-32c0ee5b-709a-4f2c-9098-e9a4478e20c3.png' // 多耶舞蹈
  }
]

const STORAGE_KEY = 'heritage_stamps'

function getStorageKey() {
  const userInfo = uni.getStorageSync('userInfo') || {}
  const userId = userInfo.userId || userInfo.id || 'guest'
  return `${STORAGE_KEY}_${userId}`
}

/**
 * 获取已集印章列表
 * @returns {Array<{id: number, name: string, unlockedAt: string, method: string}>}
 */
export function getCollectedStamps() {
  try {
    const data = uni.getStorageSync(getStorageKey())
    return data ? JSON.parse(data) : []
  } catch (e) {
    return []
  }
}

/**
 * 获取已集印章数量
 */
export function getStampCount() {
  return getCollectedStamps().length
}

/**
 * 检查某个非遗是否已集章
 */
export function hasStamp(heritageId) {
  return getCollectedStamps().some(s => s.id === heritageId)
}

/**
 * 集章 - 解锁一个印章
 * @param {number} heritageId - 非遗项目 ID
 * @param {string} method - 解锁方式：'location'(地图打卡) | 'booking'(体验预约)
 * @returns {{ success: boolean, isNew: boolean, allCollected: boolean, stamp: object|null }}
 */
export function collectStamp(heritageId, method = 'location') {
  const stampDef = STAMPS.find(s => s.id === heritageId)
  if (!stampDef) {
    return { success: false, isNew: false, allCollected: false, stamp: null }
  }

  const collected = getCollectedStamps()

  // 已集过
  if (collected.some(s => s.id === heritageId)) {
    return { success: true, isNew: false, allCollected: collected.length >= 6, stamp: null }
  }

  const newStamp = {
    id: heritageId,
    name: stampDef.name,
    icon: stampDef.icon,
    color: stampDef.color,
    method: method,
    unlockedAt: new Date().toISOString()
  }

  collected.push(newStamp)
  uni.setStorageSync(getStorageKey(), JSON.stringify(collected))

  return {
    success: true,
    isNew: true,
    allCollected: collected.length >= 6,
    stamp: newStamp
  }
}

/**
 * 获取集章进度
 */
export function getStampProgress() {
  const collected = getCollectedStamps()
  return {
    current: collected.length,
    total: 6,
    percentage: Math.round((collected.length / 6) * 100),
    collected: collected,
    remaining: STAMPS.filter(s => !collected.some(c => c.id === s.id))
  }
}

/**
 * 显示集章成功弹窗
 */
export function showStampToast(stamp, allCollected) {
  if (allCollected) {
    // 全部集齐 - 大弹窗
    uni.showModal({
      title: '🎉 恭喜集齐非遗印章！',
      content: '您已成功集齐全部 6 枚非遗印章，获得「非遗守护者」称号！快去非遗护照查看您的荣誉吧。',
      confirmText: '查看护照',
      cancelText: '继续探索',
      success: (res) => {
        if (res.confirm) {
          uni.navigateTo({ url: '/pages/heritage/passport' })
        }
      }
    })
  } else {
    // 单个集章成功 - toast
    uni.showToast({
      title: `获得「${stamp.name}」印章！`,
      icon: 'none',
      duration: 2500
    })
  }
}
