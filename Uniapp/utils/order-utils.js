import { ticketOrderApi, foodOrderApi, hotelOrderApi, productOrderApi, experienceOrderApi, formatOrderTime } from './api.js'

// 统一后端状态 → 中文显示状态
export const STATUS_MAP = {
  pending: '待支付',
  paid: '待使用',
  confirmed: '待使用',
  used: '已完成',
  cancelled: '已取消',
  checked_in: '已入住',
  checked_out: '已退房',
  completed: '已完成',
  shipped: '发货中',
  delivered: '已收货'
}

// 根据订单类型获取正确的状态映射
export function getOrderStatusDisplay(order) {
  const orderType = getOrderType(order)
  const status = order.status
  
  // 酒店订单的特殊处理
  if (orderType === 'hotel') {
    const hotelStatusMap = {
      pending: '待支付',
      paid: '待使用',
      checked_in: '已入住',
      checked_out: '已退房',
      cancelled: '已取消'
    }
    return hotelStatusMap[status] || STATUS_MAP[status] || status
  }
  
  // 商品订单的特殊处理
  if (orderType === 'product') {
    const productStatusMap = {
      pending: '待支付',
      paid: '待发货',
      shipped: '发货中',
      delivered: '已收货',
      cancelled: '已取消'
    }
    return productStatusMap[status] || STATUS_MAP[status] || status
  }
  
  return STATUS_MAP[status] || status
}

// 根据订单获取对应 API
export function getOrderApi(orderType) {
  const map = { ticket: ticketOrderApi, food: foodOrderApi, hotel: hotelOrderApi, product: productOrderApi, experience: experienceOrderApi }
  return map[orderType]
}

// 根据订单获取 orderNo
export function getOrderNo(item) {
  return item.orderNo || item.id || ''
}

// 检测订单类型
export function getOrderType(item) {
  if (item.orderType) return item.orderType
  const id = String(item.id || '')
  if (id.startsWith('SJ')) return 'ticket'
  if (id.startsWith('MS')) return 'food'
  if (id.startsWith('HT')) return 'hotel'
  if (id.startsWith('SP')) return 'product'
  if (id.startsWith('EX')) return 'experience'
  // 通过 orderNo 前缀判断
  const orderNo = String(item.orderNo || '')
  if (orderNo.startsWith('EX')) return 'experience'
  if (orderNo.startsWith('TK')) return 'ticket'
  if (orderNo.startsWith('MS')) return 'food'
  if (orderNo.startsWith('HT')) return 'hotel'
  if (orderNo.startsWith('SP')) return 'product'
  return 'ticket'
}

// 类型中文标签
export function getOrderTypeLabel(item) {
  const map = { ticket: '景点', food: '美食', hotel: '酒店', product: '特产', experience: '非遗体验' }
  return map[getOrderType(item)] || '订单'
}

// 类型图标
export function getOrderTypeIcon(item) {
  const map = {
    ticket: '/static/icons/position.png', food: '/static/icons/food.png',
    hotel: '/static/icons/hotel.png', product: '/static/icons/shop.png',
    experience: '/static/icons/feiyi.png'
  }
  return map[getOrderType(item)] || '/static/icons/order.png'
}

// 获取订单显示名称
export function getOrderName(item) {
  return item.ticketName || item.foodName || item.hotelName || item.productName || item.heritageName || '订单'
}

// 获取订单封面图（优先级：cover > 类型封面 > 默认图）
export function getOrderCover(item) {
  if (item.cover) return item.cover
  const type = getOrderType(item)
  const map = {
    ticket: item.ticketCover || '',
    food: item.foodCover || '',
    hotel: item.hotelCover || '',
    product: item.productCover || '',
    experience: item.heritageCover || ''
  }
  return map[type] || ''
}

// 默认封面图（无图时使用）
export function getDefaultCover(type, name) {
  if (type === 'ticket') {
    if (name && name.includes('程阳')) return 'https://cdn.phototourl.com/uploads/2026-03-19-67bbd771-fa02-4207-9437-87afc2c36bc7.jpg'
    if (name && name.includes('布央')) return 'https://cdn.phototourl.com/free/2026-03-23-83a39e17-b2f9-4ff8-bced-41e9c064e9fe.jpg'
    return 'https://cdn.phototourl.com/uploads/2026-03-19-67bbd771-fa02-4207-9437-87afc2c36bc7.jpg'
  }
  if (type === 'food') return 'https://cdn.phototourl.com/free/2026-03-24-f37e50c9-e052-4485-8e58-128c75c91fc4.jpg'
  if (type === 'hotel') return 'https://cdn.phototourl.com/member/2026-03-27-41f55c65-23ae-4fb8-940c-9413cda51f87.jpg'
  return 'https://cdn.phototourl.com/free/2026-03-23-5bbeb559-132b-460d-a3f1-043d32170ed2.png'
}

// 统一映射后端 API 数据为前端显示格式
export function mapOrderData(order, type) {
  const name = order.ticketName || order.foodName || order.hotelName || order.productName || order.heritageName || ''
  const cover = getOrderCover(order) || getDefaultCover(type, name)
  const orderType = order.orderType || type
  const status = getOrderStatusDisplay({ ...order, orderType, status: order.status })

  // 统一日期字段：优先使用 checkInDate，然后是 visitDate，最后是 date
  let displayDate = order.date || order.visitDate || order.checkInDate
  if (orderType === 'hotel') {
    displayDate = order.checkInDate || order.date || order.visitDate
  }

  return {
    ...order,
    orderType: orderType,
    ticketName: order.ticketName || order.foodName || order.hotelName || order.productName || order.heritageName || '',
    cover,
    totalPrice: order.totalAmount || order.totalPrice || 0,
    status: status,
    date: displayDate, // 统一的显示日期字段
    checkInDate: order.checkInDate, // 保留原始字段
    checkOutDate: order.checkOutDate, // 保留原始字段
    nights: order.nights || 1, // 住宿晚数
    createTime: formatOrderTime(order.createTime || order.create_time || order.createdAt),
    validStartDate: order.validStartDate,
    validEndDate: order.validEndDate,
    // 映射用户信息字段（支持多种可能的字段名）
    nickName: order.nickName || order.nickname || order.userName || order.name || order.buyerName || order.contactName || '',
    contactName: order.contactName || order.nickName || order.nickname || order.userName || order.name || '',
    phone: order.phone || order.contactPhone || order.mobile || order.tel || order.buyerPhone || '',
    contactPhone: order.contactPhone || order.phone || order.mobile || order.tel || ''
  }
}

// 统一加载当前用户所有类型订单（5 API 并行 + 本地降级）
export async function loadAllUserOrders(userId) {
  let backendOrders = []
  
  // 只有在 userId > 0 时才请求后端 API
  if (userId > 0) {
    const [ticketRes, foodRes, hotelRes, productRes, experienceRes] = await Promise.allSettled([
      ticketOrderApi.getByUserId(userId),
      foodOrderApi.getByUserId(userId),
      hotelOrderApi.getByUserId(userId),
      productOrderApi.getByUserId(userId),
      experienceOrderApi.getByUserId(userId)
    ])
    const extract = (res) => {
      if (res.status === 'fulfilled') {
        const d = res.value
        return d.records || d.list || d.data || d || []
      }
      return []
    }
    backendOrders = [
      ...extract(ticketRes).map(o => mapOrderData(o, 'ticket')),
      ...extract(foodRes).map(o => mapOrderData(o, 'food')),
      ...extract(hotelRes).map(o => mapOrderData(o, 'hotel')),
      ...extract(productRes).map(o => mapOrderData(o, 'product')),
      ...extract(experienceRes).map(o => mapOrderData(o, 'experience'))
    ].sort((a, b) => {
      try { return new Date(b.createTime) - new Date(a.createTime) } catch { return 0 }
    })
  }

  // 合并本地订单（排除后端已有的）
  const localOrders = (uni.getStorageSync('orders') || []).filter(
    lo => !backendOrders.some(bo => bo.id === lo.id || bo.orderNo === lo.orderNo) &&
         (!lo.userId || lo.userId === userId || userId === 0)
  ).map(o => normalizeLocalOrder(o))

  // 优先返回后端订单 + 本地订单，如果后端订单为空则只返回本地订单
  return backendOrders.length > 0 ? [...backendOrders, ...localOrders] : localOrders
}

// 规范化本地存储订单
export function normalizeLocalOrder(o) {
  if (!o.orderType) {
    const id = String(o.id || '')
    const orderNo = String(o.orderNo || '')
    if (id.startsWith('SJ') || orderNo.startsWith('TK') || orderNo.startsWith('SJ')) o.orderType = 'ticket'
    else if (id.startsWith('MS') || orderNo.startsWith('MS')) o.orderType = 'food'
    else if (id.startsWith('HT') || orderNo.startsWith('HT')) o.orderType = 'hotel'
    else if (id.startsWith('SP') || orderNo.startsWith('SP')) o.orderType = 'product'
    else if (id.startsWith('EX') || orderNo.startsWith('EX')) o.orderType = 'experience'
  }
  if (!o.cover) o.cover = getOrderCover(o) || getDefaultCover(o.orderType, o.ticketName)
  if (!o.ticketName && !o.foodName && !o.hotelName && !o.productName && !o.heritageName) {
    o.ticketName = o.foodName || o.hotelName || o.productName || o.heritageName || ''
  }
  // 映射后端状态码为中文显示状态
  o.status = getOrderStatusDisplay(o)
  // 统一日期字段：优先使用 checkInDate（酒店），然后是 visitDate，最后是 date
  if (o.orderType === 'hotel') {
    if (!o.date && o.checkInDate) o.date = o.checkInDate
  } else {
    if (!o.date && o.visitDate) o.date = o.visitDate
  }
  o.createTime = formatOrderTime(o.createTime || o.create_time || o.createdAt)
  return o
}

// 获取当前用户 ID
export function getCurrentUserId() {
  const userInfo = uni.getStorageSync('userInfo') || {}
  return userInfo.userId || userInfo.id || 0
}

// 获取当前用户所有订单（带用户过滤）
export function getLocalOrders() {
  const userId = getCurrentUserId()
  let orders = uni.getStorageSync('orders') || []
  // 如果已登录，只显示当前用户的订单；如果未登录，显示所有没有 userId 或 userId 为 0 的订单
  if (userId > 0) {
    orders = orders.filter(o => {
      const oid = o.userId || 0
      return oid === userId || oid === 0 || !o.hasOwnProperty('userId')
    })
  } else {
    // 未登录时，显示所有本地订单（没有 userId 或 userId 为 0 的）
    orders = orders.filter(o => {
      const oid = o.userId || 0
      return oid === 0 || !o.hasOwnProperty('userId')
    })
  }
  return orders.map(o => normalizeLocalOrder(o))
}

// 更新本地订单状态
export function updateLocalOrderStatus(orderId, newStatus) {
  const orders = uni.getStorageSync('orders') || []
  const idx = orders.findIndex(o => o.id === orderId || o.orderNo === orderId)
  if (idx >= 0) {
    orders[idx].status = newStatus
    uni.setStorageSync('orders', orders)
  }
}

// 后端支付（尝试调用对应类型 API）
export async function payOrder(item) {
  const type = getOrderType(item)
  const orderNo = getOrderNo(item)
  const api = getOrderApi(type)
  if (api && api.pay) {
    await api.pay(orderNo)
  }
  // 根据订单类型设置不同的支付后状态
  if (type === 'product') {
    updateLocalOrderStatus(item.id, '待发货')
  } else {
    updateLocalOrderStatus(item.id, 'paid')
  }
}

// 后端取消订单
export async function cancelOrderApi(item) {
  const type = getOrderType(item)
  const orderNo = getOrderNo(item)
  const api = getOrderApi(type)
  if (api && api.cancel) {
    await api.cancel(orderNo)
  }
  updateLocalOrderStatus(item.id, 'cancelled')
}

// 后端核销/使用订单
export async function useOrderApi(item) {
  const type = getOrderType(item)
  const orderNo = getOrderNo(item)
  const api = getOrderApi(type)
  if (type === 'hotel' && api.checkOut) {
    await api.checkOut(orderNo)
  } else if (type === 'experience' && api.complete) {
    await api.complete(orderNo)
  } else if (api && api.use) {
    await api.use(orderNo)
  }
  updateLocalOrderStatus(item.id, 'used')
}
