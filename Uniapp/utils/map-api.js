const TENCENT_MAP_KEY = 'QJMBZ-E7RCZ-FCFXR-7FS2E-WRIN5-ETBOK'
const BASE_URL = 'https://apis.map.qq.com/ws'

export const tencentMap = {
  async getDrivingRoute(from, to) {
    try {
      const response = await uni.request({
        url: `${BASE_URL}/direction/v1/driving/`,
        method: 'GET',
        data: {
          key: TENCENT_MAP_KEY,
          from: `${from.lat},${from.lng}`,
          to: `${to.lat},${to.lng}`,
          output: 'json'
        }
      })
      return response[0]
    } catch (error) {
      console.error('获取驾车路线失败:', error)
      return null
    }
  },

  async getWalkingRoute(from, to) {
    try {
      const response = await uni.request({
        url: `${BASE_URL}/direction/v1/walking/`,
        method: 'GET',
        data: {
          key: TENCENT_MAP_KEY,
          from: `${from.lat},${from.lng}`,
          to: `${to.lat},${to.lng}`,
          output: 'json'
        }
      })
      return response[0]
    } catch (error) {
      console.error('获取步行路线失败:', error)
      return null
    }
  },

  async getTransitRoute(from, to) {
    try {
      const response = await uni.request({
        url: `${BASE_URL}/direction/v1/transit/`,
        method: 'GET',
        data: {
          key: TENCENT_MAP_KEY,
          from: `${from.lat},${from.lng}`,
          to: `${to.lat},${to.lng}`,
          output: 'json'
        }
      })
      return response[0]
    } catch (error) {
      console.error('获取公交路线失败:', error)
      return null
    }
  },

  openMapNavigation(destination) {
    uni.openLocation({
      latitude: destination.lat,
      longitude: destination.lng,
      name: destination.name,
      address: destination.address || '',
      success: () => {
        console.log('打开地图成功')
      },
      fail: (err) => {
        console.error('打开地图失败:', err)
        uni.showToast({
          title: '打开地图失败',
          icon: 'none'
        })
      }
    })
  }
}

export default tencentMap
