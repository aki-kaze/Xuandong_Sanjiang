<template>
  <view class="food-detail-container">
    <!-- 图片轮播 -->
    <view class="banner">
      <swiper class="swiper" :indicator-dots="true" :autoplay="true" circular>
        <swiper-item v-for="(img, idx) in bannerImages" :key="idx">
          <image class="banner-img" :src="img.image" mode="aspectFill"></image>
        </swiper-item>
      </swiper>
      <view class="collect-btn" @tap="toggleCollect">
        <text class="collect-icon">{{ isCollected ? '❤️' : '🤍' }}</text>
      </view>
      <view class="banner-price-tag" v-if="food.price">
        <text class="price-text">{{ food.price }}</text>
      </view>
    </view>

    <!-- 基本信息 -->
    <view class="info-card">
      <view class="title-row">
        <text class="title">{{ food.name }}</text>
        <view class="category-badge" v-if="food.category">
          <text>{{ food.category }}</text>
        </view>
      </view>
      <view class="tags" v-if="food.tags && food.tags.length">
        <text class="tag" v-for="tag in food.tags" :key="tag">{{ tag }}</text>
      </view>
      <view class="rating-row">
        <view class="rating">
          <text class="score">⭐{{ food.rating }}</text>
          <text class="count">{{ food.reviewCount }}条评论</text>
        </view>
        <view class="price" v-if="food.price">
          <text class="price-value">¥{{ food.price }}</text>
        </view>
      </view>
    </view>

    <!-- 美食介绍 -->
    <view class="intro-section" v-if="food.desc">
      <view class="section-title">
        <view class="title-bar"></view>
        <text>美食介绍</text>
      </view>
      <view class="intro-content">{{ food.desc }}</view>
    </view>

    <!-- 售卖点/店铺信息 -->
    <view class="shop-section" v-if="locations.length > 0">
      <view class="section-title">
        <view class="title-bar"></view>
        <text>推荐店铺 ({{ locations.length }})</text>
      </view>
      <view class="location-list">
        <view class="location-item" v-for="(loc, index) in locations" :key="index">
          <view class="loc-top">
            <view class="loc-tag" :class="'tag-color-' + (index % 4)">{{ loc.tag || '推荐' }}</view>
            <text class="loc-name">{{ loc.name }}</text>
          </view>
          <view class="loc-info-row">
            <image class="loc-icon" src="/static/icons/position.png" mode="aspectFit"></image>
            <text class="loc-address">{{ loc.address }}</text>
          </view>
          <view class="loc-info-row">
            <image class="loc-icon" src="/static/icons/time.png" mode="aspectFit"></image>
            <text class="loc-hours">{{ loc.businessHours }}</text>
          </view>
          <view class="loc-bottom">
            <view class="loc-phone" @tap="callPhone(loc.phone)">
              <image class="loc-icon" src="/static/icons/phone.png" mode="aspectFit"></image>
              <text>{{ loc.phone }}</text>
            </view>
            <view class="loc-nav-btn" @tap="goLocationNav(loc)">
              <image class="loc-icon" src="/static/icons/compass.png" mode="aspectFit"></image>
              <text>导航</text>
            </view>
          </view>
        </view>
      </view>
    </view>

    <!-- 推荐菜品 -->
    <view class="recommend-section" v-if="recommendList.length > 0">
      <view class="section-title">
        <view class="title-bar"></view>
        <text>推荐菜品</text>
      </view>
      <view class="recommend-list">
        <view class="recommend-item" v-for="item in recommendList" :key="item.id" @tap="goRecommendDetail(item)">
          <image class="thumb" :src="item.cover || item.image" mode="aspectFill"></image>
          <view class="info">
            <text class="name">{{ item.name }}</text>
            <view class="recommend-bottom">
              <text class="recommend-price">{{ item.price }}</text>
              <text class="recommend-rating" v-if="item.rating">⭐{{ item.rating }}</text>
            </view>
          </view>
        </view>
      </view>
    </view>

    <!-- 底部操作栏 -->
    <view class="bottom-bar">
      <view class="bar-left">
        <view class="bar-item" @tap="toggleCollect">
          <text class="bar-icon">{{ isCollected ? '❤️' : '🤍' }}</text>
          <text>{{ isCollected ? '已收藏' : '收藏' }}</text>
        </view>
        <view class="bar-item" @tap="callPhone()">
          <image class="bar-icon-img" src="/static/icons/phone.png" mode="aspectFit"></image>
          <text>电话</text>
        </view>
        <view class="bar-item" @tap="showMap">
          <image class="bar-icon-img" src="/static/icons/position.png" mode="aspectFit"></image>
          <text>导航</text>
        </view>
      </view>
      <view class="bar-right">
        <view class="bottom-buy-btn" @tap="openBuyModal">立即购买</view>
      </view>
    </view>

    <!-- 购买弹窗 -->
    <view class="pay-mask" v-if="showPayModal" @tap.stop="">
      <view class="pay-sheet">
        <view class="pay-sheet-header">
          <text class="pay-sheet-title">确认订单</text>
          <text class="pay-sheet-close" @tap="showPayModal = false">×</text>
        </view>
        <view class="pay-food-info">
          <image class="pay-food-cover" :src="food.cover || food.image" mode="aspectFill" />
          <view class="pay-food-detail">
            <text class="pay-food-name">{{ food.name }}</text>
            <text class="pay-food-price">{{ food.price }}</text>
          </view>
        </view>
        <view class="pay-form">
          <view class="pay-row">
            <text class="pay-label">数量</text>
            <view class="pay-qty">
              <view class="qty-btn" :class="{ disabled: quantity <= 1 }" @tap="quantity > 1 && quantity--">
                <text>-</text>
              </view>
              <text class="qty-num">{{ quantity }}</text>
              <view class="qty-btn" :class="{ disabled: quantity >= 10 }" @tap="quantity < 10 && quantity++">
                <text>+</text>
              </view>
            </view>
          </view>
          <view class="pay-row">
            <text class="pay-label">姓名</text>
            <input class="pay-input" v-model="contactName" placeholder="请输入您的姓名" />
          </view>
          <view class="pay-row">
            <text class="pay-label">手机号</text>
            <input class="pay-input" v-model="contactPhone" type="number" placeholder="请输入手机号码" maxlength="11" />
          </view>
          <view class="pay-row total">
            <text class="pay-label">合计</text>
            <text class="pay-total">¥{{ totalPrice }}</text>
          </view>
        </view>
        <view class="pay-actions">
          <view class="pay-cancel-btn" @tap="showPayModal = false">取消</view>
          <view class="pay-confirm-btn" :class="{ loading: paying }" @tap="confirmBuy">
            {{ paying ? '提交中...' : '立即支付 ¥' + totalPrice }}
          </view>
        </view>
        <text class="pay-tip">支付即视为同意相关服务条款</text>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, computed } from 'vue'
import { onLoad, onShow } from '@dcloudio/uni-app'
import foodsData from '../../data/foods.json'
import { favoritesUtil } from '../../utils/favorites.js'
import { foodOrderApi, formatOrderTime } from '../../utils/api.js'

const foodId = ref(1)
const foodDetail = ref({})
const recommendList = ref([])
const isCollected = ref(false)
const loading = ref(false)
const showPayModal = ref(false)
const quantity = ref(1)
const contactName = ref('')
const contactPhone = ref('')
const paying = ref(false)

const food = computed(() => {
  return foodDetail.value || {}
})

const locations = computed(() => {
  const locs = food.value.locations
  if (Array.isArray(locs) && locs.length > 0) return locs
  return []
})

const bannerImages = computed(() => {
  const img = food.value.image || food.value.cover
  if (!img) return []
  return [{ image: img }]
})

const totalPrice = computed(() => {
  let priceStr = String(food.value.price || '0')
  let match = priceStr.match(/[0-9]+(\.[0-9]+)?/)
  let price = match ? parseFloat(match[0]) : 0
  return (price * quantity.value).toFixed(0)
})

onLoad((options) => {
  if (options.id) {
    foodId.value = parseInt(options.id)
    loadFoodDetail()
  }
})

onShow(() => {
  checkIsCollected()
})

const checkIsCollected = () => {
  isCollected.value = favoritesUtil.isFavorite(foodId.value, 'food')
}

const loadFoodDetail = async () => {
  loading.value = true
  try {
    let foundFood = null

    // 1. 优先从后端 API 加载
    try {
      const res = await uni.request({
        url: 'http://localhost:8080/api/foods/' + foodId.value,
        method: 'GET'
      })
      
      console.log('后端返回的原始数据:', res)

      // 兼容多种返回格式
      let data = null
      if (res.statusCode === 200 && res.data) {
        if (res.data.code === 200 && res.data.data) {
          data = res.data.data
        } else if (res.statusCode === 200 && res.data) {
          data = res.data
        }
      }

      if (data) {
        foundFood = transformApiData(data)
        console.log('使用后端美食数据:', foundFood)
      }
    } catch (error) {
      console.warn('后端API加载失败:', error)
    }

    // 2. 如果后端没有数据，从本地 JSON 数据加载
    if (!foundFood) {
      const localFound = foodsData.find(item => item.id === foodId.value)
      if (localFound) {
        foundFood = transformStaticData(localFound)
        console.log('使用本地美食数据（后端无数据）')
      }
    }

    // 3. 设置数据
    if (foundFood) {
      foodDetail.value = foundFood
      await loadRecommendFromApi()
    } else {
      uni.showToast({ title: '美食信息不存在', icon: 'none' })
    }
  } catch (error) {
    console.error('加载美食详情失败:', error)
    uni.showToast({ title: '加载失败', icon: 'none' })
  } finally {
    loading.value = false
  }
}

const loadRecommendFromApi = async () => {
  try {
    const res = await uni.request({
      url: 'http://localhost:8080/api/foods?page=1&size=10',
      method: 'GET'
    })
    
    console.log('推荐美食后端返回的原始数据:', res)

    // 兼容多种返回格式
    let foods = []
    if (res.statusCode === 200 && res.data) {
      if (res.data.code === 200 && res.data.data) {
        if (res.data.data.records && Array.isArray(res.data.data.records)) {
          foods = res.data.data.records
        } else if (Array.isArray(res.data.data)) {
          foods = res.data.data
        }
      } else if (Array.isArray(res.data)) {
        foods = res.data
      }
    }

    if (foods.length > 0) {
      recommendList.value = foods
        .filter(item => item.id !== foodId.value)
        .slice(0, 4)
        .map(item => ({
          id: item.id,
          name: item.name,
          cover: item.cover || item.image,
          price: item.price,
          rating: item.rating || 4.5
        }))
      console.log('推荐美食加载成功，共', recommendList.value.length, '条')
      return
    }
    loadRecommendFromStatic()
  } catch (error) {
    console.error('加载推荐美食失败:', error)
    loadRecommendFromStatic()
  }
}

const loadRecommendFromStatic = () => {
  recommendList.value = foodsData
    .filter(item => item.id !== foodId.value)
    .slice(0, 4)
    .map(item => ({
      id: item.id,
      name: item.name,
      cover: item.cover,
      price: item.price,
      rating: item.rating
    }))
}

const transformApiData = (item) => {
  if (!item) item = {}
  let tags = item.tags
  if (typeof tags === 'string') {
    try { tags = JSON.parse(tags) } catch (e) { tags = [] }
  }
  let locs = item.locations
  if (typeof locs === 'string') {
    try { locs = JSON.parse(locs) } catch (e) { locs = [] }
  }
  if (!Array.isArray(tags)) tags = []
  if (!Array.isArray(locs)) locs = []
  
  // 如果后端返回的 locations 为空，生成默认店铺数据（和本地数据一致）
  if (locs.length === 0) {
    locs = [
      { 
        name: '侗家私房土菜馆',
        latitude: item.latitude || 25.7843,
        longitude: item.longitude || 109.6101,
        address: item.address || '广场北路一街 16 号',
        businessHours: '10:00-21:00',
        phone: '13877258801',
        tag: '人气推荐'
      },
      { 
        name: '老三江土菜馆（广场店）',
        latitude: item.latitude || 25.8235,
        longitude: item.longitude || 109.6103,
        address: '侗江大道广场北路一街 14 号',
        businessHours: '09:00-21:00',
        phone: '13877258802',
        tag: '老字号'
      },
      { 
        name: '侗寨厨娘打油茶文化体验馆',
        latitude: item.latitude || 25.7830,
        longitude: item.longitude || 109.6080,
        address: '雅柳路 67 号',
        businessHours: '08:30-20:30',
        phone: '13877258803',
        tag: '非遗体验'
      }
    ]
  }
  
  return {
    id: item.id,
    name: item.name,
    image: item.cover || item.image,
    cover: item.cover || item.image,
    tags: tags,
    rating: parseFloat(item.rating) || 4.5,
    reviewCount: item.reviewCount || 128,
    price: item.price,
    desc: item.description || item.desc,
    address: item.address || '广西壮族自治区柳州市三江侗族自治县',
    businessHours: (locs.length > 0) ? locs[0].businessHours : '09:00-21:00',
    phone: (locs.length > 0) ? locs[0].phone : item.phone || '',
    latitude: item.latitude || null,
    longitude: item.longitude || null,
    category: item.category,
    locations: locs
  }
}

const transformStaticData = (item) => {
  if (!item) item = {}
  let tags = item.tags
  if (typeof tags === 'string') {
    try { tags = JSON.parse(tags) } catch (e) { tags = [] }
  }
  let locs = item.locations
  if (typeof locs === 'string') {
    try { locs = JSON.parse(locs) } catch (e) { locs = [] }
  }
  if (!Array.isArray(tags)) tags = []
  if (!Array.isArray(locs)) locs = []
  return {
    id: item.id,
    name: item.name,
    image: item.cover,
    cover: item.cover,
    tags: tags,
    rating: parseFloat(item.rating) || 0,
    reviewCount: 128,
    price: item.price,
    desc: item.description,
    address: item.address || '广西壮族自治区柳州市三江侗族自治县',
    businessHours: (locs.length > 0) ? locs[0].businessHours : '',
    phone: (locs.length > 0) ? locs[0].phone : '',
    latitude: item.latitude || null,
    longitude: item.longitude || null,
    category: item.category,
    locations: locs
  }
}

const toggleCollect = () => {
  const item = {
    id: foodId.value,
    type: 'food',
    name: foodDetail.value.name,
    desc: foodDetail.value.desc || foodDetail.value.description,
    price: foodDetail.value.price,
    cover: foodDetail.value.image || foodDetail.value.cover,
    emoji: '🍜'
  }
  favoritesUtil.toggleFavorite(item)
  checkIsCollected()
}

const callPhone = (phone) => {
  const phoneNumber = phone || food.value.phone || (locations.value.length > 0 ? locations.value[0].phone : '')
  if (phoneNumber) {
    uni.makePhoneCall({
      phoneNumber,
      fail: (err) => {
        // 用户取消拨号是正常行为，不显示错误
        if (err.errMsg && !err.errMsg.includes('cancel')) {
          console.error('拨号失败', err)
          uni.showToast({ title: '拨号失败', icon: 'none' })
        }
      }
    })
  } else {
    uni.showToast({ title: '暂无联系电话', icon: 'none' })
  }
}

const showMap = () => {
  const firstLoc = locations.value.length > 0 ? locations.value[0] : null
  const lat = firstLoc ? parseFloat(firstLoc.latitude) : parseFloat(food.value.latitude)
  const lng = firstLoc ? parseFloat(firstLoc.longitude) : parseFloat(food.value.longitude)
  const name = firstLoc ? firstLoc.name : food.value.name
  const address = firstLoc ? firstLoc.address : (food.value.address || '三江侗族自治县')
  
  if (!lat || !lng || isNaN(lat) || isNaN(lng)) {
    uni.showToast({ title: '暂无位置信息', icon: 'none' })
    return
  }
  
  // 传递美食封面图片
  const foodCover = food.value.cover || food.value.image || ''
  
  uni.navigateTo({
    url: '/pages/map/map?targetLat=' + lat + '&targetLng=' + lng + '&targetName=' + encodeURIComponent(name) + '&targetAddress=' + encodeURIComponent(address) + '&targetCover=' + encodeURIComponent(foodCover) + '&targetType=food'
  })
}

const goLocationNav = (loc) => {
  const lat = parseFloat(loc.latitude)
  const lng = parseFloat(loc.longitude)

  if (!lat || !lng || isNaN(lat) || isNaN(lng)) {
    uni.showToast({ title: '位置信息有误', icon: 'none' })
    return
  }

  // 传递美食封面图片
  const foodCover = food.value.cover || food.value.image || ''

  uni.navigateTo({
    url: '/pages/map/map?targetLat=' + lat + '&targetLng=' + lng + '&targetName=' + encodeURIComponent(loc.name) + '&targetAddress=' + encodeURIComponent(loc.address) + '&targetCover=' + encodeURIComponent(foodCover) + '&targetType=food'
  })
}

const goRecommendDetail = (item) => {
  uni.redirectTo({ url: '/pages/food/detail?id=' + item.id })
}

const openBuyModal = () => {
  const userInfo = uni.getStorageSync('userInfo') || {}
  contactName.value = userInfo.name || ''
  contactPhone.value = userInfo.phone || ''
  quantity.value = 1
  showPayModal.value = true
}

const confirmBuy = async () => {
  if (paying.value) return
  if (!contactName.value.trim()) {
    uni.showToast({ title: '请输入姓名', icon: 'none' }); return
  }
  if (!contactPhone.value || contactPhone.value.length !== 11) {
    uni.showToast({ title: '请输入正确的手机号', icon: 'none' }); return
  }
  paying.value = true

  let priceStr = String(food.value.price || '0')
  let match = priceStr.match(/[0-9]+(\.[0-9]+)?/)
  let price = match ? parseFloat(match[0]) : 0
  const total = price * quantity.value

  // 计算有效期：从当天开始共3天
  const now = new Date()
  const validStartDate = formatValidDate(now)
  const endDate = new Date(now)
  endDate.setDate(endDate.getDate() + 2) // 加上2天得到第三天（包含当天）
  const validEndDate = formatValidDate(endDate)

  const userInfo = uni.getStorageSync('userInfo') || {}
  const orderData = {
    orderNo: 'MS' + Date.now(),
    userId: userInfo.userId || 0,
    foodId: food.value.id || foodId.value,
    foodName: food.value.name,
    foodCover: food.value.cover || food.value.image || '',
    shopName: food.value.shopName || '乡美食',
    quantity: quantity.value,
    price: price,
    totalPrice: total,
    remark: '美食订单',
    contactName: contactName.value.trim(),
    contactPhone: contactPhone.value,
    validStartDate: validStartDate,
    validEndDate: validEndDate
  }

  try {
    const res = await foodOrderApi.create(orderData)
    const orderNo = (res && res.order) ? res.order.orderNo : orderData.orderNo
    showPayModal.value = false
    quantity.value = 1
    uni.showModal({
      title: '购买成功',
      content: '订单号：' + orderNo + '\n请在"我的订单"中查看',
      showCancel: false,
      success: () => { uni.redirectTo({ url: '/pages/order/food-order?status=待支付' }) }
    })
  } catch (e) {
    console.error('创建订单失败:', e)
    const order = {
      id: 'MS' + Date.now(),
      orderType: 'food',
      userId: userInfo.userId || userInfo.id || 0,
      foodName: food.value.name,
      ticketName: food.value.name,
      cover: food.value.cover || food.value.image,
      foodCover: food.value.cover || food.value.image,
      price: price,
      quantity: quantity.value,
      totalPrice: total,
      status: '待支付',
      createTime: formatOrderTime(new Date()),
      contactName: contactName.value.trim(),
      contactPhone: contactPhone.value,
      validStartDate: validStartDate,
      validEndDate: validEndDate
    }
    const orders = uni.getStorageSync('orders') || []
    orders.unshift(order)
    uni.setStorageSync('orders', orders)
    showPayModal.value = false
    quantity.value = 1
    uni.showModal({
      title: '购买成功',
      content: '订单号：' + order.id + '\n请在"我的订单"中查看',
      showCancel: false,
      success: () => { uni.redirectTo({ url: '/pages/order/food-order?status=待支付' }) }
    })
  } finally {
    paying.value = false
  }
}

// 格式化有效期日期：YYYY年MM月DD日
const formatValidDate = (date) => {
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  return `${year}年${month}月${day}日`
}
</script>

<style lang="scss" scoped>
.food-detail-container {
  min-height: 100vh;
  background: #F8F9FA;
  padding-bottom: 140rpx;
}

.banner {
  width: 100%;
  position: relative;
}

.swiper {
  width: 100%;
  height: 500rpx;
}

.banner-img {
  width: 100%;
  height: 100%;
}

.banner-price-tag {
  position: absolute;
  bottom: 30rpx;
  left: 30rpx;
  background: linear-gradient(135deg, #A33B32, #8B2F28);
  color: #FFFFFF;
  padding: 10rpx 24rpx;
  border-radius: 24rpx;
  font-size: 28rpx;
  font-weight: 700;
  box-shadow: 0 4rpx 16rpx rgba(163, 59, 50, 0.4);
  z-index: 10;
}

.collect-btn {
  position: absolute;
  top: 30rpx;
  right: 30rpx;
  width: 72rpx;
  height: 72rpx;
  background: rgba(255, 255, 255, 0.92);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 4rpx 16rpx rgba(0, 0, 0, 0.15);
  z-index: 10;
}

.collect-icon {
  font-size: 36rpx;
}

.info-card {
  background: #FDFCF8;
  padding: 32rpx;

  .title-row {
    display: flex;
    align-items: center;
    gap: 16rpx;
    margin-bottom: 16rpx;
  }

  .title {
    font-size: 38rpx;
    font-weight: bold;
    color: #333;
    flex: 1;
  }

  .category-badge {
    background: linear-gradient(135deg, #FFF3E0, #FFE0B2);
    color: #D4A373;
    font-size: 22rpx;
    padding: 6rpx 18rpx;
    border-radius: 20rpx;
    font-weight: 600;
    flex-shrink: 0;
  }

  .tags {
    display: flex;
    gap: 12rpx;
    margin-bottom: 20rpx;
    flex-wrap: wrap;

    .tag {
      background: #FFF3E0;
      color: #D4A373;
      font-size: 22rpx;
      padding: 6rpx 16rpx;
      border-radius: 8rpx;
    }
  }

  .rating-row {
    display: flex;
    justify-content: space-between;
    align-items: center;

    .rating {
      display: flex;
      align-items: center;
      gap: 8rpx;
      font-size: 26rpx;

      .score {
        color: #D4A373;
        font-weight: bold;
      }

      .count {
        color: #999;
      }
    }

    .price {
      .price-value {
        color: #A33B32;
        font-size: 32rpx;
        font-weight: bold;
      }
    }
  }
}

.section-title {
  display: flex;
  align-items: center;
  gap: 12rpx;
  font-size: 30rpx;
  font-weight: bold;
  color: #333;
  margin-bottom: 24rpx;

  .title-bar {
    width: 8rpx;
    height: 28rpx;
    background: linear-gradient(180deg, #1D3B5A, #2E5F8E);
    border-radius: 4rpx;
  }
}

.intro-section,
.shop-section,
.recommend-section {
  background: #FDFCF8;
  margin-top: 20rpx;
  padding: 32rpx;
}

.intro-content {
  font-size: 28rpx;
  color: #555;
  line-height: 2;
  white-space: pre-line;
}

.location-list {
  display: flex;
  flex-direction: column;
  gap: 20rpx;
}

.location-item {
  background: linear-gradient(135deg, #F8FBFF, #FFFFFF);
  border: 1rpx solid #E8F4FD;
  border-radius: 20rpx;
  padding: 24rpx;

  .loc-top {
    display: flex;
    align-items: center;
    gap: 12rpx;
    margin-bottom: 16rpx;
  }

  .loc-tag {
    font-size: 20rpx;
    color: #FFFFFF;
    padding: 4rpx 14rpx;
    border-radius: 16rpx;
    font-weight: 600;
    flex-shrink: 0;
  }

  .tag-color-0 { background: linear-gradient(135deg, #FF7F50, #E8633A); }
  .tag-color-1 { background: linear-gradient(135deg, #4A90E2, #357ABD); }
  .tag-color-2 { background: linear-gradient(135deg, #1D3B5A, #2E5F8E); }
  .tag-color-3 { background: linear-gradient(135deg, #9C27B0, #7B1FA2); }

  .loc-name {
    font-size: 30rpx;
    font-weight: 700;
    color: #1D3B5A;
    flex: 1;
  }

  .loc-info-row {
    display: flex;
    align-items: center;
    gap: 10rpx;
    padding: 6rpx 0;
  }

  .loc-icon {
    width: 28rpx;
    height: 28rpx;
    flex-shrink: 0;
  }

  .loc-address {
    font-size: 24rpx;
    color: #64748B;
    flex: 1;
  }

  .loc-hours {
    font-size: 24rpx;
    color: #64748B;
  }

  .loc-bottom {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-top: 16rpx;
    padding-top: 16rpx;
    border-top: 1rpx dashed #E8F4FD;
  }

  .loc-phone {
    display: flex;
    align-items: center;
    gap: 8rpx;
    font-size: 24rpx;
    color: #4A90E2;
  }

  .loc-nav-btn {
    display: flex;
    align-items: center;
    gap: 8rpx;
    background: linear-gradient(135deg, #4A90E2, #357ABD);
    color: #FFFFFF;
    font-size: 24rpx;
    font-weight: 600;
    padding: 12rpx 24rpx;
    border-radius: 30rpx;
    box-shadow: 0 4rpx 12rpx rgba(74, 144, 226, 0.3);
  }
}

.recommend-list {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 20rpx;

  .recommend-item {
    background: #F8F9FA;
    border-radius: 16rpx;
    overflow: hidden;

    .thumb {
      width: 100%;
      height: 200rpx;
    }

    .info {
      padding: 16rpx;

      .name {
        font-size: 26rpx;
        color: #333;
        font-weight: 600;
        display: block;
        margin-bottom: 8rpx;
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
      }

      .recommend-bottom {
        display: flex;
        justify-content: space-between;
        align-items: center;
      }

      .recommend-price {
        color: #A33B32;
        font-size: 24rpx;
        font-weight: bold;
      }

      .recommend-rating {
        font-size: 20rpx;
        color: #D4A373;
      }
    }
  }
}

.bottom-bar {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  background: #FFFFFF;
  padding: 20rpx 30rpx;
  padding-bottom: calc(20rpx + env(safe-area-inset-bottom));
  display: flex;
  gap: 20rpx;
  align-items: center;
  box-shadow: 0 -4rpx 20rpx rgba(29,59,90,0.10);
  z-index: 100;

  .bar-left {
    display: flex;
    gap: 30rpx;

    .bar-item {
      display: flex;
      flex-direction: column;
      align-items: center;
      gap: 4rpx;
      font-size: 22rpx;
      color: #666;
    }

    .bar-icon {
      font-size: 32rpx;
    }

    .bar-icon-img {
      width: 36rpx;
      height: 36rpx;
    }
  }

  .bar-right {
    flex: 1;
    display: flex;
  }
}

.bottom-buy-btn {
  flex: 1;
  background: linear-gradient(135deg, #1D3B5A, #2E5F8E);
  color: #FFFFFF;
  font-size: 30rpx;
  font-weight: bold;
  text-align: center;
  padding: 24rpx;
  border-radius: 50rpx;
}

.pay-mask { position: fixed; top: 0; left: 0; right: 0; bottom: 0; background: rgba(0,0,0,0.5); z-index: 100; display: flex; align-items: flex-end; }
.pay-sheet { background: #FFFFFF; border-radius: 32rpx 32rpx 0 0; padding: 0 0 60rpx; width: 100%; }
.pay-sheet-header { display: flex; justify-content: space-between; align-items: center; padding: 32rpx 32rpx 24rpx; border-bottom: 1rpx solid #E8E8E8; }
.pay-sheet-title { font-size: 32rpx; font-weight: bold; color: #1D3B5A; }
.pay-sheet-close { font-size: 40rpx; color: #999; padding: 0 8rpx; }
.pay-food-info { display: flex; gap: 20rpx; padding: 24rpx 32rpx; }
.pay-food-cover { width: 120rpx; height: 120rpx; border-radius: 16rpx; flex-shrink: 0; }
.pay-food-detail { flex: 1; display: flex; flex-direction: column; justify-content: center; gap: 8rpx; }
.pay-food-name { font-size: 30rpx; font-weight: bold; color: #1D3B5A; }
.pay-food-price { font-size: 28rpx; color: #A33B32; font-weight: 600; }
.pay-form { padding: 0 32rpx; }
.pay-row { display: flex; justify-content: space-between; align-items: center; padding: 20rpx 0; border-bottom: 1rpx solid #F1F5F9; }
.pay-row:last-child { border-bottom: none; }
.pay-label { font-size: 28rpx; color: #64748B; flex-shrink: 0; }
.pay-input { flex: 1; text-align: right; font-size: 28rpx; color: #1D3B5A; margin-left: 20rpx; }
.pay-qty { display: flex; align-items: center; gap: 20rpx; }
.qty-btn { width: 56rpx; height: 56rpx; border-radius: 50%; background: #F1F5F9; display: flex; align-items: center; justify-content: center; font-size: 32rpx; color: #1D3B5A; font-weight: 600; }
.qty-btn.disabled { opacity: 0.3; }
.qty-num { font-size: 32rpx; font-weight: 700; color: #1D3B5A; min-width: 48rpx; text-align: center; }
.pay-row.total { border-top: 2rpx solid #E8E8E8; margin-top: 8rpx; padding-top: 24rpx; }
.pay-total { font-size: 36rpx; font-weight: 700; color: #A33B32; }
.pay-actions { display: flex; gap: 20rpx; padding: 24rpx 32rpx 8rpx; }
.pay-cancel-btn { flex: 1; text-align: center; padding: 24rpx; background: #F1F5F9; border-radius: 50rpx; font-size: 28rpx; color: #64748B; }
.pay-confirm-btn { flex: 2; text-align: center; padding: 24rpx; background: linear-gradient(135deg, #1D3B5A, #2E5F8E); border-radius: 50rpx; font-size: 28rpx; color: #FFFFFF; font-weight: 600; }
.pay-confirm-btn.loading { opacity: 0.7; }
.pay-tip { display: block; text-align: center; font-size: 22rpx; color: #bbb; padding: 0 32rpx; }
</style>
