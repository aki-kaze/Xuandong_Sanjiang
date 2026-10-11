/**
 * 地理距离计算工具
 * 使用 Haversine 公式计算两点间的直线距离（公里）
 */

const EARTH_RADIUS_KM = 6371

/**
 * 将角度转换为弧度
 */
function toRad(deg) {
  return (deg * Math.PI) / 180
}

/**
 * 使用 Haversine 公式计算两点间的直线距离（公里）
 * @param {number} lat1 - 纬度1
 * @param {number} lng1 - 经度1
 * @param {number} lat2 - 纬度2
 * @param {number} lng2 - 经度2
 * @returns {number} 距离（公里），保留一位小数
 */
export function getDistance(lat1, lng1, lat2, lng2) {
  if (!lat1 || !lng1 || !lat2 || !lng2) return 0

  const dLat = toRad(lat2 - lat1)
  const dLng = toRad(lng2 - lng1)
  const a =
    Math.sin(dLat / 2) * Math.sin(dLat / 2) +
    Math.cos(toRad(lat1)) * Math.cos(toRad(lat2)) *
    Math.sin(dLng / 2) * Math.sin(dLng / 2)
  const c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
  const distance = EARTH_RADIUS_KM * c

  return Math.round(distance * 10) / 10
}

/**
 * 计算多个坐标点之间的总距离
 * @param {Array<{lat: number, lng: number}>} points - 坐标点数组
 * @returns {number} 总距离（公里）
 */
export function getTotalDistance(points) {
  if (!points || points.length < 2) return 0

  let total = 0
  for (let i = 0; i < points.length - 1; i++) {
    total += getDistance(points[i].lat, points[i].lng, points[i + 1].lat, points[i + 1].lng)
  }
  return Math.round(total * 10) / 10
}

/**
 * 根据交通方式估算实际行驶距离（直线距离 × 系数）
 * 道路通常不是直线，需要乘以绕行系数
 * @param {number} straightDistance - 直线距离（公里）
 * @param {string} transport - 交通方式：drive/bus/walk
 * @returns {number} 估算行驶距离（公里）
 */
export function getRouteDistance(straightDistance, transport = 'drive') {
  const factors = {
    drive: 1.3, // 自驾绕行系数
    bus: 1.4,   // 公交绕行系数更大
    walk: 1.1   // 步行接近直线
  }
  const factor = factors[transport] || 1.3
  return Math.round(straightDistance * factor * 10) / 10
}

/**
 * 根据距离和交通方式估算耗时（分钟）
 * @param {number} distance - 距离（公里）
 * @param {string} transport - 交通方式
 * @returns {number} 预估耗时（分钟）
 */
export function getEstimatedTime(distance, transport = 'drive') {
  const speeds = {
    drive: 40,  // 平均时速 40km/h（含城区）
    bus: 25,    // 公交平均时速
    walk: 5     // 步行时速
  }
  const speed = speeds[transport] || 40
  return Math.max(5, Math.round((distance / speed) * 60))
}

/**
 * 格式化距离显示
 * @param {number} km - 距离（公里）
 * @returns {string} 格式化后的字符串
 */
export function formatDistance(km) {
  if (km < 1) {
    return `${Math.round(km * 1000)}m`
  }
  return `${km}km`
}

/**
 * 格式化时间显示
 * @param {number} minutes - 分钟数
 * @returns {string} 格式化后的字符串
 */
export function formatTime(minutes) {
  if (minutes < 60) {
    return `约${minutes}分钟`
  }
  const hours = Math.floor(minutes / 60)
  const mins = minutes % 60
  if (mins === 0) return `约${hours}小时`
  return `约${hours}小时${mins}分钟`
}
