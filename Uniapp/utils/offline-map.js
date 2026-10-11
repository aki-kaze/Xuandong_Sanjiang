// 离线地图导航工具
// 在无网络环境下，基于本地景点坐标数据提供导航信息

import staticAttractions from '../data/attractions.json'

// 三江县中心坐标（默认起点）
const SANJIANG_CENTER = { lat: 25.7834, lng: 109.6078 }

// 构建景点坐标索引
const attractionsIndex = {}
staticAttractions.forEach(a => {
  attractionsIndex[a.name] = {
    id: a.id,
    name: a.name,
    lat: parseFloat(a.lat),
    lng: parseFloat(a.lng),
    address: a.address || ''
  }
})

/**
 * Haversine 公式计算两点间距离（公里）
 */
function calcDistance(lat1, lng1, lat2, lng2) {
  const R = 6371 // 地球半径（公里）
  const dLat = toRad(lat2 - lat1)
  const dLng = toRad(lng2 - lng1)
  const a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
    Math.cos(toRad(lat1)) * Math.cos(toRad(lat2)) *
    Math.sin(dLng / 2) * Math.sin(dLng / 2)
  const c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
  return R * c
}

function toRad(deg) {
  return deg * (Math.PI / 180)
}

/**
 * 估算步行时间（基于 5km/h 步行速度）
 */
function estimateWalkTime(distanceKm) {
  const hours = distanceKm / 5
  const mins = Math.round(hours * 60)
  if (mins < 60) return mins + '分钟'
  const h = Math.floor(mins / 60)
  const m = mins % 60
  return h + '小时' + (m > 0 ? m + '分钟' : '')
}

/**
 * 估算驾车时间（基于 40km/h 平均速度，考虑山路）
 */
function estimateDriveTime(distanceKm) {
  const hours = distanceKm / 40
  const mins = Math.round(hours * 60)
  if (mins < 60) return mins + '分钟'
  const h = Math.floor(mins / 60)
  const m = mins % 60
  return h + '小时' + (m > 0 ? m + '分钟' : '')
}

/**
 * 估算驾车费用（基于 0.5元/公里）
 */
function estimateDriveCost(distanceKm) {
  return Math.round(distanceKm * 0.5)
}

/**
 * 根据方向返回大致方位描述
 */
function getDirection(fromLat, fromLng, toLat, toLng) {
  const dLat = toLat - fromLat
  const dLng = toLng - fromLng
  const angle = Math.atan2(dLng, dLat) * (180 / Math.PI)

  if (angle >= -22.5 && angle < 22.5) return '正北方向'
  if (angle >= 22.5 && angle < 67.5) return '东北方向'
  if (angle >= 67.5 && angle < 112.5) return '正东方向'
  if (angle >= 112.5 && angle < 157.5) return '东南方向'
  if (angle >= 157.5 || angle < -157.5) return '正南方向'
  if (angle >= -157.5 && angle < -112.5) return '西南方向'
  if (angle >= -112.5 && angle < -67.5) return '正西方向'
  if (angle >= -67.5 && angle < -22.5) return '西北方向'
  return ''
}

export const offlineMap = {
  /**
   * 获取所有景点坐标数据
   */
  getAllAttractions() {
    return staticAttractions.map(a => ({
      id: a.id,
      name: a.name,
      lat: parseFloat(a.lat),
      lng: parseFloat(a.lng),
      address: a.address || '',
      category: a.category || ''
    }))
  },

  /**
   * 根据名称查找景点
   */
  findAttraction(name) {
    return attractionsIndex[name] || null
  },

  /**
   * 获取附近景点（按距离排序）
   */
  getNearbyAttractions(lat, lng, maxCount = 10) {
    const results = staticAttractions.map(a => {
      const aLat = parseFloat(a.lat)
      const aLng = parseFloat(a.lng)
      const distance = calcDistance(lat, lng, aLat, aLng)
      return {
        id: a.id,
        name: a.name,
        lat: aLat,
        lng: aLng,
        address: a.address,
        distance: distance,
        distanceText: distance < 1 ? Math.round(distance * 1000) + '米' : distance.toFixed(1) + '公里',
        walkTime: estimateWalkTime(distance),
        driveTime: estimateDriveTime(distance)
      }
    })
    results.sort((a, b) => a.distance - b.distance)
    return results.slice(0, maxCount)
  },

  /**
   * 计算两点间的导航信息（离线估算）
   * @param {Object} from - {lat, lng, name?}
   * @param {Object} to - {lat, lng, name?}
   * @returns {Object} 导航信息
   */
  calcRoute(from, to) {
    const distance = calcDistance(from.lat, from.lng, to.lat, to.lng)
    const direction = getDirection(from.lat, from.lng, to.lat, to.lng)

    return {
      from: {
        lat: from.lat,
        lng: from.lng,
        name: from.name || '起点'
      },
      to: {
        lat: to.lat,
        lng: to.lng,
        name: to.name || '终点'
      },
      distance: distance,
      distanceText: distance < 1 ? Math.round(distance * 1000) + '米' : distance.toFixed(1) + '公里',
      direction: direction,
      walk: {
        time: estimateWalkTime(distance),
        timeMinutes: Math.round((distance / 5) * 60)
      },
      drive: {
        time: estimateDriveTime(distance),
        timeMinutes: Math.round((distance / 40) * 60),
        cost: estimateDriveCost(distance)
      },
      tips: this.getRouteTips(distance, direction)
    }
  },

  /**
   * 根据距离和方向给出导航提示
   */
  getRouteTips(distance, direction) {
    const tips = []

    if (distance < 1) {
      tips.push('距离较近，建议步行前往')
    } else if (distance < 5) {
      tips.push('建议驾车或骑行前往')
    } else {
      tips.push('距离较远，建议驾车前往')
    }

    tips.push(`目的地在${direction}`)

    if (distance > 10) {
      tips.push('山路弯道较多，请注意行车安全')
    }

    return tips
  },

  /**
   * 生成离线导航概览文本
   */
  getNavSummary(from, to) {
    const route = this.calcRoute(from, to)
    return [
      `📍 ${route.from.name} → ${route.to.name}`,
      `📏 距离：${route.distanceText}`,
      `🧭 方向：${route.direction}`,
      `🚶 步行约${route.walk.time}`,
      `🚗 驾车约${route.drive.time}（约¥${route.drive.cost}）`,
      `💡 ${route.tips[0]}`
    ].join('\n')
  },

  /**
   * 为路线规划页面计算多站点路线
   */
  calcMultiStopRoute(stops) {
    if (!stops || stops.length < 2) return null

    const segments = []
    let totalDistance = 0
    let totalWalkTime = 0
    let totalDriveTime = 0

    for (let i = 0; i < stops.length - 1; i++) {
      const from = stops[i]
      const to = stops[i + 1]
      const fromData = attractionsIndex[from.name] || { lat: from.lat, lng: from.lng, name: from.name }
      const toData = attractionsIndex[to.name] || { lat: to.lat, lng: to.lng, name: to.name }

      const segment = this.calcRoute(fromData, toData)
      segments.push(segment)
      totalDistance += segment.distance
      totalWalkTime += segment.walk.timeMinutes
      totalDriveTime += segment.drive.timeMinutes
    }

    return {
      stops: stops,
      segments: segments,
      totalDistance: totalDistance,
      totalDistanceText: totalDistance < 1 ? Math.round(totalDistance * 1000) + '米' : totalDistance.toFixed(1) + '公里',
      totalWalkTime: estimateWalkTime(totalDistance),
      totalDriveTime: estimateDriveTime(totalDistance),
      totalDriveCost: estimateDriveCost(totalDistance),
      stopCount: stops.length
    }
  },

  /**
   * 搜索景点（离线，支持名称和地址模糊匹配）
   */
  searchAttractions(keyword) {
    if (!keyword) return []
    const kw = keyword.toLowerCase()
    return staticAttractions
      .filter(a => a.name.toLowerCase().includes(kw) || (a.address && a.address.includes(kw)) || (a.tags && a.tags.some(t => t.includes(kw))))
      .map(a => ({
        id: a.id,
        name: a.name,
        lat: parseFloat(a.lat),
        lng: parseFloat(a.lng),
        address: a.address,
        category: a.category
      }))
  }
}

export default offlineMap
