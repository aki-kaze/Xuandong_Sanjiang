/**
 * 分享工具 - 支持微信分享和降级方案
 * 在微信小程序中使用 wx.share，在其他环境下降级为复制链接
 */

// 判断当前是否在微信小程序环境
function isWechatMiniProgram() {
  // #ifdef MP-WEIXIN
  return true
  // #endif
  // #ifndef MP-WEIXIN
  return false
  // #endif
}

/**
 * 分享内容
 * @param {Object} options
 * @param {string} options.title - 分享标题
 * @param {string} options.path - 分享路径（小程序页面路径）
 * @param {string} options.content - 分享描述（降级时使用）
 * @param {string} options.link - 降级时的链接（可选）
 */
export function shareContent(options) {
  const { title, path, content, link } = options

  if (isWechatMiniProgram()) {
    // 微信小程序环境 - 使用原生分享
    // 通过 onShareAppMessage 触发，这里仅做提示
    uni.showToast({
      title: '点击右上角分享给好友',
      icon: 'none',
      duration: 2000
    })
  } else {
    // 非微信环境 - 降级方案
    uni.showActionSheet({
      itemList: ['复制链接', '复制内容'],
      success: (res) => {
        if (res.tapIndex === 0) {
          // 复制链接
          const shareLink = link || `https://xundong.com${path || ''}`
          uni.setClipboardData({
            data: shareLink,
            success: () => {
              uni.showToast({ title: '链接已复制', icon: 'success' })
            }
          })
        } else if (res.tapIndex === 1) {
          // 复制内容
          const shareText = `${title}\n\n${content || ''}\n\n—— 来自寻侗三江`
          uni.setClipboardData({
            data: shareText,
            success: () => {
              uni.showToast({ title: '内容已复制', icon: 'success' })
            }
          })
        }
      }
    })
  }
}

/**
 * 生成分享海报数据（供 canvas 绘制使用）
 * @param {Object} options
 * @returns {Object} 海报配置
 */
export function generatePosterConfig(options) {
  const { title, subtitle, cover, path } = options
  return {
    width: 750,
    height: 1334,
    background: '#FDFCF8',
    elements: [
      {
        type: 'image',
        src: cover || '',
        x: 60,
        y: 120,
        width: 630,
        height: 420,
        borderRadius: 20
      },
      {
        type: 'text',
        content: title || '寻侗三江',
        x: 60,
        y: 580,
        fontSize: 40,
        fontWeight: 'bold',
        color: '#1D3B5A'
      },
      {
        type: 'text',
        content: subtitle || '探索侗族千年文化与自然之美',
        x: 60,
        y: 640,
        fontSize: 26,
        color: '#666666'
      },
      {
        type: 'text',
        content: '长按识别小程序码，了解更多',
        x: 60,
        y: 1180,
        fontSize: 22,
        color: '#999999'
      },
      {
        type: 'qrcode',
        path: path || '',
        x: 540,
        y: 1080,
        size: 180
      }
    ]
  }
}

/**
 * 在微信小程序页面中配置 onShareAppMessage
 * 在页面的 methods 中调用此函数返回分享配置
 * @param {Object} page - 页面实例（this）
 * @returns {Object} 分享配置
 */
export function getShareConfig(page) {
  return {
    title: page.shareTitle || '寻侗三江 - 探索侗族千年文化',
    path: page.sharePath || '/pages/index/index',
    imageUrl: page.shareImage || ''
  }
}

export default {
  shareContent,
  generatePosterConfig,
  getShareConfig,
  isWechatMiniProgram
}
