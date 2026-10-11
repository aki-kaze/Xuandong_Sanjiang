<template>
  <view class="food-page">
    <!-- 分类筛选 -->
    <view class="category-bar">
      <view
        class="cat-item"
        v-for="cat in categories"
        :key="cat.id"
        :class="{ active: activeCategory === cat.id }"
        @tap="setCategory(cat.id)"
      >
        <text class="cat-label">{{ cat.label }}</text>
      </view>
    </view>

    <!-- 美食列表 -->
    <view class="food-list">
      <view class="food-card" v-for="item in filteredFoods" :key="item.id" @tap="showFoodDetail(item)">
        <view class="food-img-wrap">
          <image class="food-cover-img" :src="item.cover" mode="aspectFill" />
          <view class="food-rating-badge">
            <image class="rating-star-icon" src="/static/icons/hot.png" mode="aspectFit" />
            <text>{{ item.rating }}</text>
          </view>
        </view>
        <view class="food-info">
          <view class="food-header">
            <text class="food-name">{{ item.name }}</text>
            <text class="food-price">¥{{ item.priceNum }}</text>
          </view>
          <view class="food-locations-info">
            <text class="locations-count">{{ (item.locations || []).length || 3 }}家餐厅在售</text>
            <text class="min-price" v-if="item.locations && item.locations.length > 0 && item.priceNum > 0">¥{{ Math.floor(item.priceNum * 0.8) }}起</text>
          </view>
          <text class="food-category-tag">{{ item.category }}</text>
          <text class="food-desc">{{ item.desc }}</text>
          <view class="food-tags">
            <text class="food-tag" v-for="tag in item.tags" :key="tag">{{ tag }}</text>
          </view>
        </view>
      </view>
    </view>

    <view style="height: 40rpx;"></view>

    <!-- 美食详情弹窗 -->
    <view class="food-modal" v-if="showModal">
      <view class="food-modal-box">
        <view class="food-modal-img">
          <image class="food-modal-cover" :src="selectedFood.cover" mode="aspectFill" />
          <text class="food-modal-close" @tap="showModal = false">×</text>
        </view>
        <view class="food-modal-content">
          <view class="food-modal-header">
            <text class="food-modal-name">{{ selectedFood.name }}</text>
            <text class="food-modal-price">¥{{ selectedFood.priceNum }}</text>
          </view>
          <view class="food-modal-rating">
            <view class="rating-text">
              <image class="rating-star-icon" src="/static/icons/hot.png" mode="aspectFit" />
              <text>{{ selectedFood.rating }}</text>
            </view>
            <text class="food-modal-cat">{{ selectedFood.category }}</text>
          </view>
          <text class="food-modal-desc">{{ selectedFood.desc }}</text>
          <view class="food-modal-tags">
            <text class="food-tag" v-for="tag in selectedFood.tags" :key="tag">{{ tag }}</text>
          </view>
          <view class="food-modal-info">
            <view class="info-row">
              <image class="icon-img" src="/static/icons/position.png" mode="aspectFit" />
              <text class="info-text">{{ selectedFood.address || '三江县城' }}</text>
            </view>
            <view class="info-row">
              <image class="icon-img" src="/static/icons/time.png" mode="aspectFit" />
              <text class="info-text">营业时间：{{ selectedFood.businessHours || '08:00-20:00' }}</text>
            </view>
            <view class="info-row">
              <image class="icon-img" src="/static/icons/calendar.png" mode="aspectFit" />
              <text class="info-text">有效期：下单后一周内可用</text>
            </view>
          </view>
          <view class="food-modal-actions">
            <view class="food-modal-btn-nav" @tap="showLocationList"><image class="icon-img" src="/static/icons/position.png" mode="aspectFit" /> 查看门店</view>
            <view class="food-modal-btn-buy" @tap="buyFood"><image class="icon-img" src="/static/icons/cart.png" mode="aspectFit" /> 立即购买</view>
          </view>
        </view>
      </view>
    </view>

    <!-- 位置列表弹窗 -->
    <view class="food-location-modal" v-if="showLocationModal">
      <view class="food-location-box">
        <view class="food-location-header">
          <view class="food-location-title">
            <image class="location-icon" src="/static/icons/position.png" mode="aspectFit" />
            <text>{{ selectedFood.name }} 售卖门店</text>
          </view>
          <text class="food-location-close" @tap="showLocationModal = false">×</text>
        </view>
        <view class="food-location-list">
          <view class="food-location-item" v-for="(loc, index) in currentLocations" :key="index" @tap="goLocationNav(loc)">
            <view class="loc-item-top">
              <view class="loc-tag" :class="'tag-color-' + (index % 4)">{{ loc.tag || '推荐' }}</view>
              <text class="loc-name">{{ loc.name }}</text>
            </view>
            <view class="loc-item-info">
              <image class="icon-img" src="/static/icons/position.png" mode="aspectFit" />
              <text class="loc-address">{{ loc.address }}</text>
            </view>
            <view class="loc-item-info">
              <image class="icon-img" src="/static/icons/time.png" mode="aspectFit" />
              <text class="loc-hours">{{ loc.businessHours }}</text>
            </view>
            <view class="loc-item-bottom">
              <view class="loc-phone" @tap.stop="callLocPhone(loc.phone)">
                <image class="icon-img" src="/static/icons/phone.png" mode="aspectFit" />
                <text>{{ loc.phone }}</text>
              </view>
              <view class="loc-nav-btn" @tap.stop="goLocationNav(loc)">
                <image class="icon-img" src="/static/icons/compass.png" mode="aspectFit" />
                <text>导航到这里</text>
              </view>
            </view>
          </view>
          <view class="food-location-empty" v-if="currentLocations.length === 0">
            <text>暂无售卖门店信息</text>
          </view>
        </view>
      </view>
    </view>

    <!-- 购买弹窗 -->
    <view class="food-pay-modal" v-if="showPayModal">
      <view class="food-pay-box">
        <text class="food-pay-title">确认订单</text>
        <view class="food-pay-info">
          <view class="food-pay-row">
            <text class="food-pay-key">商品</text>
            <text class="food-pay-val">{{ selectedFood.name }}</text>
          </view>
          <view class="food-pay-row">
            <text class="food-pay-key">价格</text>
            <text class="food-pay-val">¥{{ selectedFood.priceNum }}</text>
          </view>
          <view class="food-pay-row">
            <text class="food-pay-key">数量</text>
            <view class="food-pay-qty">
              <text class="qty-btn" @tap="quantity > 1 && quantity--">-</text>
              <text class="qty-num">{{ quantity }}</text>
              <text class="qty-btn" @tap="quantity < 10 && quantity++">+</text>
            </view>
          </view>
          <view class="food-pay-row">
            <text class="food-pay-key">有效期</text>
            <text class="food-pay-val">一周内有效</text>
          </view>
          <view class="food-pay-row total">
            <text class="food-pay-key">合计</text>
            <text class="food-pay-total">¥{{ totalPrice }}</text>
          </view>
        </view>
        <view class="food-pay-actions">
          <view class="food-pay-cancel" @tap="showPayModal = false">取消</view>
          <view class="food-pay-confirm" @tap="confirmBuy">立即支付 ¥{{ totalPrice }}</view>
        </view>
      </view>
    </view>
  </view>
</template>

<script>
import foodsData from '../../data/foods.json'
import { foodApi, foodOrderApi, formatOrderTime } from '../../utils/api.js'

export default {
  data() {
    return {
      foods: [],
      activeCategory: 'all',
      quantity: 1,
      categories: [
        { id: 'all', icon: 'all', label: '全部' },
        { id: '传统腌制', icon: 'food', label: '腌制' },
        { id: '传统饮品', icon: 'food', label: '饮品' },
        { id: '主食', icon: 'food', label: '主食' },
        { id: '特色宴席', icon: 'all', label: '宴席' }
      ],
      showModal: false,
      showPayModal: false,
      showLocationModal: false,
      selectedFood: {},
      loading: false,
      dataSource: 'static'
    }
  },
  computed: {
    filteredFoods() {
      if (this.activeCategory === 'all') return this.foods
      return this.foods.filter(f => f.category === this.activeCategory)
    },
    totalPrice() {
      let price = this.selectedFood.priceNum || 0
      return price * this.quantity
    },
    currentLocations() {
      // 如果有 locations 数据，直接返回
      if (this.selectedFood.locations && this.selectedFood.locations.length > 0) {
        return this.selectedFood.locations
      }
      // 如果没有 locations 数据，根据食品信息生成默认位置
      const defaultLocations = [
        { 
          name: (this.selectedFood.shopName || '侗乡美食') + '（总店）',
          latitude: this.selectedFood.latitude || 25.8234,
          longitude: this.selectedFood.longitude || 109.6078,
          address: this.selectedFood.address || '三江侗族自治县',
          businessHours: '08:00-20:00',
          phone: '0772-865' + Math.floor(Math.random() * 9000 + 1000),
          tag: '总店'
        },
        { 
          name: (this.selectedFood.shopName || '侗乡美食') + '（分店）',
          latitude: this.selectedFood.latitude || 25.7856,
          longitude: this.selectedFood.longitude || 109.6078,
          address: '三江侗族自治县古宜镇',
          businessHours: '09:00-21:00',
          phone: '0772-865' + Math.floor(Math.random() * 9000 + 1000),
          tag: '人气推荐'
        },
        { 
          name: (this.selectedFood.shopName || '侗乡美食') + '（景区店）',
          latitude: this.selectedFood.latitude || 25.8234,
          longitude: this.selectedFood.longitude || 109.5678,
          address: '三江侗族自治县林溪镇程阳村',
          businessHours: '08:30-19:30',
          phone: '0772-865' + Math.floor(Math.random() * 9000 + 1000),
          tag: '景区店'
        }
      ]
      return defaultLocations
    }
  },
  methods: {
    async loadFoods(category = '') {
      this.loading = true
      uni.showLoading({ title: '加载中...' })
      try {
        // 1. 优先从后端 API 获取数据
        let apiFoods = []
        try {
          const result = await foodApi.getList(1, 100, category)
          console.log('后端返回的原始数据:', result)
          
          // 兼容多种返回格式
          let list = []
          if (Array.isArray(result)) {
            list = result
          } else if (result && result.records && Array.isArray(result.records)) {
            list = result.records
          } else if (result && result.list && Array.isArray(result.list)) {
            list = result.list
          } else if (result && result.data && Array.isArray(result.data)) {
            list = result.data
          } else if (result && result.data && result.data.records && Array.isArray(result.data.records)) {
            list = result.data.records
          }
          
          if (Array.isArray(list) && list.length > 0) {
            apiFoods = list.map(item => this.parseFoodItem(item))
            console.log('后端美食数据加载成功，共', apiFoods.length, '条')
          }
        } catch (apiError) {
          console.warn('后端API加载失败:', apiError)
        }

        // 2. 如果后端有数据，使用后端数据
        if (apiFoods.length > 0) {
          this.foods = apiFoods
          this.dataSource = 'api'
          console.log('使用后端美食数据，共', this.foods.length, '条')
        } else {
          // 3. 如果后端没有数据，加载本地静态数据
          try {
            this.foods = foodsData.map(item => this.parseFoodItem(item))
            this.dataSource = 'static'
            console.log('使用本地美食数据，共', this.foods.length, '条')
          } catch (error) {
            console.warn('本地数据加载失败:', error)
            this.foods = []
          }
        }

        // 根据分类筛选
        if (category && category !== 'all') {
          this.foods = this.foods.filter(f => f.category === category)
        }

        if (this.foods.length === 0) {
          uni.showToast({ title: '暂无美食数据', icon: 'none' })
        }
      } catch (error) {
        console.error('加载美食失败:', error)
        uni.showToast({ title: '加载失败', icon: 'none' })
        this.foods = []
      } finally {
        this.loading = false
        uni.hideLoading()
      }
    },
    loadStaticData() {
      try {
        this.foods = foodsData.map(item => this.parseFoodItem(item))
        this.dataSource = 'static'
        console.log('美食数据加载成功（静态数据）')
      } catch (error) {
        console.error('加载美食失败:', error)
        this.foods = []
      }
    },
    parseFoodItem(item) {
      const newItem = { ...item }
      try {
        // 解析 tags 字段
        if (typeof newItem.tags === 'string') {
          newItem.tags = JSON.parse(newItem.tags)
        }
        if (!Array.isArray(newItem.tags)) {
          newItem.tags = []
        }

        // 解析 locations 字段
        if (typeof newItem.locations === 'string') {
          newItem.locations = JSON.parse(newItem.locations)
        }
        if (!Array.isArray(newItem.locations)) {
          newItem.locations = []
        }

        // 如果 locations 为空，生成默认店铺数据（和本地数据一致）
        if (newItem.locations.length === 0) {
          newItem.locations = [
            { 
              name: '侗家私房土菜馆',
              latitude: newItem.latitude || 25.7843,
              longitude: newItem.longitude || 109.6101,
              address: newItem.address || '广场北路一街 16 号',
              businessHours: '10:00-21:00',
              phone: '13877258801',
              tag: '人气推荐'
            },
            { 
              name: '老三江土菜馆（广场店）',
              latitude: newItem.latitude || 25.8235,
              longitude: newItem.longitude || 109.6103,
              address: '侗江大道广场北路一街 14 号',
              businessHours: '09:00-21:00',
              phone: '13877258802',
              tag: '老字号'
            },
            { 
              name: '侗寨厨娘打油茶文化体验馆',
              latitude: newItem.latitude || 25.7830,
              longitude: newItem.longitude || 109.6080,
              address: '雅柳路 67 号',
              businessHours: '08:30-20:30',
              phone: '13877258803',
              tag: '非遗体验'
            }
          ]
        }

        // 统一字段名
        if (newItem.description && !newItem.desc) {
          newItem.desc = newItem.description
        }
        
        // 处理价格字段 - 提取数字（支持多种格式：58, ¥58, 58 元等）
        if (newItem.price) {
          const priceStr = String(newItem.price)
          // 移除所有非数字和小数点字符
          const cleanPrice = priceStr.replace(/[^\d.]/g, '')
          newItem.priceNum = cleanPrice ? parseFloat(cleanPrice) : 0
        } else {
          newItem.priceNum = 0
        }
      } catch (e) {
        console.error('解析数据失败:', e)
        newItem.tags = newItem.tags || []
        newItem.locations = []
        // 如果解析失败，也要确保有默认店铺
        newItem.locations = [
          { 
            name: '侗家私房土菜馆',
            latitude: newItem.latitude || 25.7843,
            longitude: newItem.longitude || 109.6101,
            address: newItem.address || '广场北路一街 16 号',
            businessHours: '10:00-21:00',
            phone: '13877258801',
            tag: '人气推荐'
          },
          { 
            name: '老三江土菜馆（广场店）',
            latitude: newItem.latitude || 25.8235,
            longitude: newItem.longitude || 109.6103,
            address: '侗江大道广场北路一街 14 号',
            businessHours: '09:00-21:00',
            phone: '13877258802',
            tag: '老字号'
          }
        ]
        newItem.priceNum = 0
      }
      return newItem
    },
    showFoodDetail(item) {
      uni.navigateTo({ url: '/pages/food/detail?id=' + item.id })
    },
    setCategory(id) {
      this.activeCategory = id
      // 重新加载数据，传入分类参数
      this.loadFoods(id === 'all' ? '' : id)
    },
    getMinPrice(locations) {
      if (!locations || locations.length === 0) return Math.floor((this.selectedFood.priceNum || 0) * 0.8)
      const basePrice = this.selectedFood.priceNum || 0
      return Math.floor(basePrice * 0.8)
    },
    showLocationList() {
      this.showModal = false
      this.showLocationModal = true
    },
    goLocationNav(loc) {
      const foodCover = this.selectedFood.cover || this.selectedFood.image || ''
      uni.navigateTo({
        url: `/pages/map/map?lat=${loc.latitude}&lng=${loc.longitude}&name=${encodeURIComponent(loc.name)}&targetType=food&targetAddress=${encodeURIComponent(loc.address || '')}&targetCover=${encodeURIComponent(foodCover)}`
      })
    },
    callLocPhone(phone) {
      uni.makePhoneCall({
        phoneNumber: phone,
        fail: (err) => {
          if (err.errMsg && !err.errMsg.includes('cancel')) {
            console.error('拨号失败', err)
            uni.showToast({ title: '拨号失败', icon: 'none' })
          }
        }
      })
    },
    buyFood() {
      this.showModal = false
      this.showPayModal = true
    },
    async confirmBuy() {
      let price = this.selectedFood.priceNum || 0
      const totalPrice = price * this.quantity

      const validDate = new Date()
      validDate.setDate(validDate.getDate() + 7)
      const validEndDate = validDate.toISOString().slice(0, 10)

      const userInfo = uni.getStorageSync('userInfo') || {}
      const orderData = {
        orderNo: 'MS' + Date.now(),
        userId: userInfo.userId || 0,
        foodId: this.selectedFood.id || 0,
        foodName: this.selectedFood.name,
        foodCover: this.selectedFood.cover || '',
        shopName: this.selectedFood.shopName || '侗乡美食',
        quantity: this.quantity,
        price: price,
        totalPrice: totalPrice,
        remark: '美食订单',
        contactName: userInfo.name || '',
        contactPhone: userInfo.phone || ''
      }

      try {
        const res = await foodOrderApi.create(orderData)
        const orderNo = (res && res.order) ? res.order.orderNo : orderData.orderNo
        this.showPayModal = false
        this.quantity = 1
        uni.showModal({
          title: '购买成功',
          content: '订单号：' + orderNo + '\n有效期至：' + validEndDate + '\n请在"我的订单"中查看',
          showCancel: false,
          success: () => { uni.redirectTo({ url: '/pages/order/food-order?status=待支付' }) }
        })
      } catch (e) {
        console.error('创建订单失败:', e)
        const userInfo = uni.getStorageSync('userInfo') || {}
        const order = {
          id: 'MS' + Date.now(),
          orderType: 'food',
          userId: userInfo.userId || userInfo.id || 0,
          foodName: this.selectedFood.name,
          ticketName: this.selectedFood.name,
          cover: this.selectedFood.cover,
          foodCover: this.selectedFood.cover,
          price: price,
          quantity: this.quantity,
          totalPrice: totalPrice,
          date: new Date().toISOString().slice(0, 10),
          validEndDate: validEndDate,
          status: '待支付',
          createTime: formatOrderTime(new Date()),
          contactName: userInfo.name || '',
          contactPhone: userInfo.phone || ''
        }
        const orders = uni.getStorageSync('orders') || []
        orders.unshift(order)
        uni.setStorageSync('orders', orders)
        this.showPayModal = false
        this.quantity = 1
        uni.showModal({
          title: '购买成功',
          content: '订单号：' + order.id + '\n请在"我的订单"中查看',
          showCancel: false,
          success: () => { uni.redirectTo({ url: '/pages/order/food-order?status=待支付' }) }
        })
      }
    }
  },
  onLoad() {
    this.loadFoods()
  }
}
</script>

<style scoped lang="scss">
.icon-img { width: 32rpx; height: 32rpx; flex-shrink: 0; }

.food-page { background: #F8FAFC; min-height: 100vh; }

.category-bar { display: flex; background: #FFFFFF; padding: 20rpx 24rpx; overflow-x: auto; border-bottom: 1rpx solid #E8F4FD; gap: 16rpx; }
.cat-item { display: flex; align-items: center; padding: 14rpx 32rpx; flex-shrink: 0; border-radius: 44rpx; background: #F8FAFC; transition: all 0.3s ease; }
.cat-item.active .cat-label { color: #FFFFFF; font-weight: 700; }
.cat-item.active { background: linear-gradient(135deg, #1D3B5A, #2E5F8E); box-shadow: 0 4rpx 16rpx rgba(29,59,90,0.3); transform: translateY(-2rpx); }
.cat-label { font-size: 28rpx; color: #64748B; font-weight: 500; }

.food-list { padding: 20rpx 24rpx; display: flex; flex-direction: column; gap: 24rpx; }
.food-card { background: #FFFFFF; border-radius: 24rpx; overflow: hidden; box-shadow: 0 6rpx 24rpx rgba(74,144,226,0.15); display: flex; flex-direction: column; }
.food-img-wrap { width: 100%; height: 360rpx; flex-shrink: 0; position: relative; }
.food-cover-img { width: 100%; height: 100%; }
.food-rating-badge { position: absolute; bottom: 16rpx; left: 16rpx; background: rgba(74,144,226,0.9); color: #FFFFFF; font-size: 22rpx; padding: 6rpx 14rpx; border-radius: 24rpx; display: flex; align-items: center; gap: 6rpx; box-shadow: 0 4rpx 12rpx rgba(74,144,226,0.4); }
.rating-star-icon { width: 24rpx; height: 24rpx; }
.food-info { padding: 24rpx; display: flex; flex-direction: column; gap: 12rpx; }
.food-header { display: flex; justify-content: space-between; align-items: flex-start; }
.food-name { font-size: 32rpx; font-weight: 700; color: #1D3B5A; flex: 1; line-height: 1.4; }
.food-price { font-size: 32rpx; color: #4A90E2; font-weight: 700; background: linear-gradient(135deg, #E8F4FD, #F0F7FF); padding: 8rpx 18rpx; border-radius: 24rpx; border: 1rpx solid #B3D9F5; }
.food-locations-info { display: flex; align-items: center; gap: 12rpx; padding: 8rpx 0; }
.locations-count { font-size: 24rpx; color: #FF7F50; background: rgba(255,127,80,0.1); padding: 6rpx 14rpx; border-radius: 20rpx; font-weight: 500; }
.min-price { font-size: 24rpx; color: #4A90E2; font-weight: 600; }
.food-category-tag { display: inline-block; font-size: 22rpx; color: #FF7F50; background: rgba(255,127,80,0.12); padding: 6rpx 16rpx; border-radius: 20rpx; font-weight: 500; align-self: flex-start; }
.food-desc { font-size: 26rpx; color: #64748B; line-height: 1.7; flex: 1; display: -webkit-box; -webkit-line-clamp: 2; -webkit-box-orient: vertical; overflow: hidden; }
.food-tags { display: flex; flex-wrap: wrap; gap: 10rpx; }
.food-tag { font-size: 22rpx; color: #4A90E2; background: rgba(74,144,226,0.1); padding: 6rpx 14rpx; border-radius: 20rpx; }

.food-modal { position: fixed; top: 0; left: 0; right: 0; bottom: 0; background: rgba(29,59,90,0.65); display: flex; align-items: flex-end; z-index: 500; }
.food-modal-box { width: 100%; background: #FFFFFF; border-radius: 36rpx 36rpx 0 0; overflow: hidden; box-shadow: 0 -8rpx 32rpx rgba(29,59,90,0.3); }
.food-modal-img { height: 320rpx; position: relative; }
.food-modal-cover { width: 100%; height: 100%; }
.food-modal-close { position: absolute; top: 24rpx; right: 28rpx; font-size: 40rpx; color: #FFFFFF; background: rgba(29,59,90,0.6); width: 64rpx; height: 64rpx; border-radius: 50%; display: flex; align-items: center; justify-content: center; backdrop-filter: blur(8rpx); box-shadow: 0 4rpx 16rpx rgba(0,0,0,0.2); }
.food-modal-content { padding: 36rpx 32rpx 56rpx; background: linear-gradient(180deg, #F8FBFF 0%, #FFFFFF 100%); }
.food-modal-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16rpx; }
.food-modal-name { font-size: 38rpx; font-weight: 700; color: #1D3B5A; line-height: 1.4; flex: 1; }
.food-modal-price { font-size: 34rpx; color: #1D3B5A; font-weight: 700; background: linear-gradient(135deg, #E8F4FD, #F0F7FF); padding: 10rpx 22rpx; border-radius: 28rpx; border: 1rpx solid #B3D9F5; flex-shrink: 0; margin-left: 16rpx; }
.food-modal-rating { display: flex; align-items: center; gap: 18rpx; margin-bottom: 20rpx; font-size: 26rpx; color: #4A90E2; }
.food-modal-rating .rating-text { display: flex; align-items: center; gap: 6rpx; }
.food-modal-cat { font-size: 24rpx; color: #4A90E2; background: linear-gradient(135deg, #E8F4FD, #F0F7FF); padding: 6rpx 16rpx; border-radius: 24rpx; border: 1rpx solid #B3D9F5; font-weight: 500; }
.food-modal-desc { display: block; font-size: 28rpx; color: #475569; line-height: 1.9; margin-bottom: 20rpx; }
.food-modal-tags { display: flex; flex-wrap: wrap; gap: 12rpx; margin-bottom: 20rpx; }
.food-modal-info { background: linear-gradient(135deg, #F0F7FF, #FFFFFF); border-radius: 20rpx; padding: 24rpx; margin-bottom: 24rpx; border: 1rpx solid #D4E8F9; }
.info-row { display: flex; align-items: center; gap: 12rpx; padding: 12rpx 0; }
.info-text { font-size: 26rpx; color: #64748B; }
.food-modal-actions { display: flex; gap: 24rpx; }
.food-modal-btn-nav { flex: 1; text-align: center; padding: 24rpx; background: linear-gradient(135deg, #F0F7FF, #FFFFFF); border-radius: 50rpx; font-size: 28rpx; color: #1D3B5A; font-weight: 600; display: flex; align-items: center; justify-content: center; gap: 8rpx; border: 2rpx solid #B3D9F5; box-shadow: 0 2rpx 12rpx rgba(74,144,226,0.15); }
.food-modal-btn-buy { flex: 1; text-align: center; padding: 24rpx; background: linear-gradient(135deg, #4A90E2, #357ABD); border-radius: 50rpx; font-size: 28rpx; color: #FFFFFF; font-weight: 600; display: flex; align-items: center; justify-content: center; gap: 8rpx; box-shadow: 0 6rpx 20rpx rgba(74,144,226,0.4); }

.food-pay-modal { position: fixed; top: 0; left: 0; right: 0; bottom: 0; background: rgba(29,59,90,0.65); display: flex; align-items: flex-end; z-index: 600; }
.food-pay-box { width: 100%; background: #FFFFFF; border-radius: 36rpx 36rpx 0 0; padding: 40rpx 32rpx 64rpx; box-shadow: 0 -8rpx 32rpx rgba(29,59,90,0.3); }
.food-pay-title { display: block; font-size: 36rpx; font-weight: 700; color: #1D3B5A; text-align: center; margin-bottom: 36rpx; }
.food-pay-info { background: linear-gradient(135deg, #F0F7FF, #FFFFFF); border-radius: 20rpx; padding: 24rpx; margin-bottom: 28rpx; border: 1rpx solid #D4E8F9; }
.food-pay-row { display: flex; justify-content: space-between; align-items: center; padding: 16rpx 0; border-bottom: 1rpx solid #E8F4FD; }
.food-pay-row:last-child { border-bottom: none; }
.food-pay-key { font-size: 28rpx; color: #64748B; font-weight: 500; }
.food-pay-val { font-size: 28rpx; color: #1D3B5A; font-weight: 600; }
.food-pay-qty { display: flex; align-items: center; gap: 24rpx; }
.qty-btn { width: 56rpx; height: 56rpx; border-radius: 50%; background: linear-gradient(135deg, #E8F4FD, #F0F7FF); display: flex; align-items: center; justify-content: center; font-size: 32rpx; color: #4A90E2; border: 2rpx solid #4A90E2; box-shadow: 0 2rpx 8rpx rgba(74,144,226,0.2); }
.qty-num { font-size: 32rpx; font-weight: 700; color: #1D3B5A; min-width: 48rpx; text-align: center; }
.food-pay-row.total { border-top: 2rpx solid #B3D9F5; margin-top: 16rpx; padding-top: 20rpx; }
.food-pay-total { font-size: 40rpx; font-weight: 700; color: #1D3B5A; }
.food-pay-actions { display: flex; gap: 24rpx; }
.food-pay-cancel { flex: 1; text-align: center; padding: 26rpx; background: #F1F5F9; border-radius: 50rpx; font-size: 28rpx; color: #64748B; font-weight: 500; border: 1rpx solid #E2E8F0; }
.food-pay-confirm { flex: 2; text-align: center; padding: 26rpx; background: linear-gradient(135deg, #4A90E2, #357ABD); border-radius: 50rpx; font-size: 28rpx; color: #FFFFFF; font-weight: 600; box-shadow: 0 6rpx 20rpx rgba(74,144,226,0.4); }

.food-location-modal { position: fixed; top: 0; left: 0; right: 0; bottom: 0; background: rgba(29,59,90,0.65); display: flex; align-items: flex-end; z-index: 550; }
.food-location-box { width: 100%; max-height: 85vh; background: #FFFFFF; border-radius: 36rpx 36rpx 0 0; display: flex; flex-direction: column; box-shadow: 0 -8rpx 32rpx rgba(29,59,90,0.3); }
.food-location-header { display: flex; justify-content: space-between; align-items: center; padding: 32rpx 32rpx 24rpx; border-bottom: 1rpx solid #E8F4FD; background: linear-gradient(135deg, #F8FBFF, #FFFFFF); }
.food-location-title { display: flex; align-items: center; gap: 10rpx; font-size: 34rpx; font-weight: 700; color: #1D3B5A; }
.food-location-title .location-icon { width: 36rpx; height: 36rpx; }
.food-location-close { font-size: 44rpx; color: #999; padding: 10rpx 20rpx; }
.food-location-list { padding: 24rpx 32rpx 64rpx; overflow-y: auto; flex: 1; background: #F8FAFC; }
.food-location-item { background: linear-gradient(135deg, #FFFFFF, #F8FBFF); border: 1rpx solid #E8F4FD; border-radius: 24rpx; padding: 28rpx; margin-bottom: 24rpx; box-shadow: 0 4rpx 16rpx rgba(74,144,226,0.08); }
.loc-item-top { display: flex; align-items: center; gap: 14rpx; margin-bottom: 18rpx; }
.loc-tag { font-size: 22rpx; color: #FFFFFF; padding: 6rpx 16rpx; border-radius: 20rpx; font-weight: 600; }
.tag-color-0 { background: linear-gradient(135deg, #FF7F50, #E8633A); }
.tag-color-1 { background: linear-gradient(135deg, #4A90E2, #357ABD); }
.tag-color-2 { background: linear-gradient(135deg, #2E7D32, #1B5E20); }
.tag-color-3 { background: linear-gradient(135deg, #A33B32, #8B2F28); }
.loc-name { font-size: 32rpx; font-weight: 700; color: #1D3B5A; flex: 1; line-height: 1.3; }
.loc-item-info { display: flex; align-items: center; gap: 12rpx; padding: 8rpx 0; }
.loc-address { font-size: 26rpx; color: #64748B; flex: 1; line-height: 1.5; }
.loc-hours { font-size: 26rpx; color: #64748B; }
.loc-item-bottom { display: flex; justify-content: space-between; align-items: center; margin-top: 20rpx; padding-top: 20rpx; border-top: 1rpx dashed #E8F4FD; }
.loc-phone { display: flex; align-items: center; gap: 10rpx; font-size: 26rpx; color: #4A90E2; font-weight: 500; }
.loc-nav-btn { display: flex; align-items: center; gap: 10rpx; background: linear-gradient(135deg, #4A90E2, #357ABD); color: #FFFFFF; font-size: 26rpx; font-weight: 600; padding: 14rpx 28rpx; border-radius: 32rpx; box-shadow: 0 6rpx 16rpx rgba(74,144,226,0.35); }
.food-location-empty { text-align: center; color: #999; padding: 100rpx 0; font-size: 28rpx; }
</style>
