<template>
  <view class="hotel-page">
    <!-- 搜索条件 -->
    <view class="search-bar-card">
      <view class="search-location">
        <image class="search-loc-icon" src="/static/icons/position.png" mode="aspectFit" />
        <text class="search-loc-text">三江侗族自治州</text>
      </view>
      <view class="search-dates">
        <view class="date-box" @tap="showDatePicker('in')">
          <text class="date-label">入住</text>
          <text class="date-val">{{ checkInDate }}</text>
        </view>
        <view class="date-nights">
          <text class="nights-num">{{ nights }}</text>
          <text class="nights-label">晚</text>
        </view>
        <view class="date-box" @tap="showDatePicker('out')">
          <text class="date-label">离店</text>
          <text class="date-val">{{ checkOutDate }}</text>
        </view>
      </view>
    </view>

    <!-- 筛选标签 -->
    <view class="filter-bar">
      <view
        class="filter-item"
        v-for="f in filters"
        :key="f.id"
        :class="{ active: activeFilter === f.id }"
        @tap="setFilter(f.id)"
      >{{ f.label }}</view>
    </view>

    <!-- 酒店列表 -->
    <view class="hotel-list">
      <view class="hotel-card" v-for="item in filteredHotels" :key="item.id" @tap="showHotelDetail(item)">
        <view class="hotel-img">
          <image class="hotel-cover-img" :src="item.cover" mode="aspectFill" />
          <view class="hotel-type-badge">{{ item.type }}</view>
        </view>
        <view class="hotel-info">
          <view class="hotel-header">
            <text class="hotel-name">{{ item.name }}</text>
            <text class="hotel-rating">⭐{{ item.rating }}</text>
          </view>
          <view class="hotel-address">
            <image class="address-icon" src="/static/icons/position.png" mode="aspectFit" />
            <text> {{ item.address }}</text>
          </view>
          <view class="hotel-facilities">
            <text class="facility-tag" v-for="f in item.facilities.slice(0,3)" :key="f">{{ f }}</text>
          </view>
          <text class="hotel-desc">{{ item.desc }}</text>
          <view class="hotel-footer">
            <view class="hotel-price-wrap">
              <text class="hotel-price-label">¥</text>
              <text class="hotel-price">{{ item.price }}</text>
              <text class="hotel-price-unit">/晚起</text>
            </view>
            <view class="hotel-footer-actions">
              <view class="hotel-location-btn" @tap.stop="viewHotelLocation(item)">
                <image class="hotel-location-icon" src="/static/icons/position.png" mode="aspectFit" />
                <text>查看位置</text>
              </view>
              <view class="hotel-book-btn" @tap.stop="bookHotel(item)">立即预订</view>
            </view>
          </view>
        </view>
      </view>
    </view>

    <view style="height: 40rpx;"></view>

    <!-- 酒店详情弹窗 -->
    <view class="hotel-modal" v-if="showModal">
      <view class="hotel-modal-box">
        <view class="hotel-modal-img">
          <image class="hotel-modal-cover" :src="selectedHotel.cover" mode="aspectFill" />
          <text class="hotel-modal-close" @tap="showModal = false">x</text>
        </view>
        <view class="hotel-modal-content">
          <view class="hotel-modal-header">
            <text class="hotel-modal-name">{{ selectedHotel.name }}</text>
            <text class="hotel-modal-type">{{ selectedHotel.type }}</text>
          </view>
          <text class="hotel-modal-rating">⭐{{ selectedHotel.rating }} · {{ selectedHotel.address }}</text>
          <text class="hotel-modal-desc">{{ selectedHotel.desc }}</text>
          <view class="hotel-modal-facilities">
            <text class="facility-tag" v-for="f in selectedHotel.facilities" :key="f">{{ f }}</text>
          </view>
          <view class="hotel-modal-price-row">
            <view>
              <text class="hotel-modal-price-label">入住价格</text>
              <view class="hotel-modal-price-wrap">
                <text class="hotel-modal-price">¥{{ selectedHotel.price }}</text>
                <text class="hotel-modal-price-unit">/晚起</text>
              </view>
            </view>
            <view class="hotel-modal-book" @tap="bookHotel(selectedHotel)">立即预订</view>
          </view>
        </view>
      </view>
    </view>

    <!-- 预订弹窗 -->
    <view class="book-modal" v-if="showBookModal">
      <view class="book-box">
        <text class="book-title">预订 {{ bookingHotel.name }}</text>
        <view class="book-form">
          <view class="book-form-item">
            <text class="book-form-label">入住日期</text>
            <text class="book-form-val">{{ checkInDate }}</text>
          </view>
          <view class="book-form-item">
            <text class="book-form-label">离店日期</text>
            <text class="book-form-val">{{ checkOutDate }}</text>
          </view>
          <view class="book-form-item">
            <text class="book-form-label">入住人数</text>
            <view class="book-qty">
              <view class="qty-btn" @tap="guestCount > 1 && guestCount--">-</view>
              <text class="qty-num">{{ guestCount }}人</text>
              <view class="qty-btn" @tap="guestCount < 10 && guestCount++">+</view>
            </view>
          </view>
          <view class="book-form-item">
            <text class="book-form-label">联系姓名</text>
            <input class="book-input" v-model="guestName" placeholder="请输入姓名" />
          </view>
          <view class="book-form-item">
            <text class="book-form-label">联系手机号</text>
            <input class="book-input" v-model="guestPhone" placeholder="请输入手机号" type="number" maxlength="11" />
          </view>
          <view class="book-total-row">
            <text class="book-total-label">合计费用</text>
            <text class="book-total-price">¥{{ bookingHotel.price * nights }}</text>
          </view>
        </view>
        <view class="book-actions">
          <view class="book-cancel" @tap="showBookModal = false">取消</view>
          <view class="book-confirm" @tap="confirmBooking">确认预订</view>
        </view>
      </view>
    </view>
  </view>
</template>

<script>
import { staticData } from '../../utils/static-data.js'
import { hotelApi, hotelOrderApi, formatOrderTime } from '../../utils/api.js'

export default {
  data() {
    const today = new Date()
    const tomorrow = new Date(today)
    tomorrow.setDate(today.getDate() + 1)
    return {
      hotels: [],
      checkInDate: today.toISOString().slice(0,10),
      checkOutDate: tomorrow.toISOString().slice(0,10),
      activeFilter: 'all',
      filters: [
        { id: 'all', label: '全部' },
        { id: '特色民宿', label: '民宿' },
        { id: '四星酒店', label: '四星' },
        { id: '精品民宿', label: '精品' }
      ],
      hotelColors: ['#E8F4FD', '#FDF6E3', '#F0FFF0', '#FFF0F0'],
      showModal: false,
      selectedHotel: {},
      showBookModal: false,
      bookingHotel: {},
      guestCount: 1,  // 默认 1 人入住
      guestName: '',
      guestPhone: '',
      loading: false,
      dataSource: 'static'
    }
  },
  computed: {
    nights() {
      const d1 = new Date(this.checkInDate)
      const d2 = new Date(this.checkOutDate)
      return Math.max(1, Math.round((d2 - d1) / 86400000))
    },
    filteredHotels() {
      if (this.activeFilter === 'all') return this.hotels
      return this.hotels.filter(h => h.type === this.activeFilter)
    }
  },
  onLoad(options) {
      this.loadHotels().then(() => {
        if (options && options.id) {
          const hotel = this.hotels.find(h => h.id === parseInt(options.id))
          if (hotel) {
            this.selectedHotel = hotel
            this.showBookModal = true
          }
        }
      })
      // 自动填充用户信息
      const userInfo = uni.getStorageSync('userInfo') || {}
      if (userInfo.name) {
        this.guestName = userInfo.name
      } else if (userInfo.nickname) {
        this.guestName = userInfo.nickname
      }
      if (userInfo.phone) {
        this.guestPhone = userInfo.phone
      }
    },
  methods: {
    async loadHotels(type = '') {
      this.loading = true
      uni.showLoading({ title: '加载住宿信息...' })
      try {
        // 优先尝试从后端API获取数据
        const result = await hotelApi.getList(1, 50, type)
        const list = result.records || result.list || result || []
        if (Array.isArray(list) && list.length > 0) {
          this.hotels = list.map(item => {
            // 处理JSON字符串字段
            if (typeof item.facilities === 'string') {
              try {
                item.facilities = JSON.parse(item.facilities)
              } catch (e) {
                item.facilities = []
              }
            }
            if (typeof item.roomTypes === 'string') {
              try {
                item.roomTypes = JSON.parse(item.roomTypes)
              } catch (e) {
                item.roomTypes = []
              }
            }
            return item
          })
          this.dataSource = 'api'
          console.log('酒店数据加载成功（后端API）')
        } else {
          throw new Error('API返回数据为空')
        }
      } catch (error) {
        // 后端不可用，降级到静态数据
        console.warn('API调用失败，使用静态数据:', error.message || error)
        this.loadStaticData()
      } finally {
        this.loading = false
        uni.hideLoading()
      }
    },
    loadStaticData() {
      this.hotels = staticData.hotels || []
      this.dataSource = 'static'
      console.log('酒店数据加载成功（静态数据）')
    },
    setFilter(id) {
      this.activeFilter = id
      // 重新加载数据，传入分类参数
      this.loadHotels(id === 'all' ? '' : id)
    },
    showDatePicker(type) {
      uni.showToast({ title: '请在日历中选择日期', icon: 'none' })
    },
    showHotelDetail(item) {
      this.selectedHotel = item
      // 自动填充用户信息
      const userInfo = uni.getStorageSync('userInfo') || {}
      if (userInfo.name) {
        this.guestName = userInfo.name
      } else if (userInfo.nickname) {
        this.guestName = userInfo.nickname
      }
      if (userInfo.phone) {
        this.guestPhone = userInfo.phone
      }
      this.showBookModal = true
    },
    viewHotelLocation(item) {
      console.log('查看酒店位置:', item)
      const lat = parseFloat(item.latitude || item.lat)
      const lng = parseFloat(item.longitude || item.lng)
      if (!lat || !lng) { uni.showToast({ title: '暂无位置信息', icon: 'none' }); return }
      const name = item.name || '酒店'
      const address = item.address || '三江侗族自治县'

      uni.openLocation({
        latitude: lat,
        longitude: lng,
        name: name,
        address: address,
        scale: 16,
        success: () => {
          console.log('打开地图成功')
        },
        fail: (err) => {
          console.error('打开地图失败:', err)
          uni.showToast({ 
            title: '打开地图失败，请稍后重试', 
            icon: 'none',
            duration: 2000
          })
        }
      })
    },
    bookHotel(item) {
      this.bookingHotel = item
      this.showModal = false
      this.showBookModal = true
    },
    async confirmBooking() {
      if (!this.guestName.trim()) { uni.showToast({ title: '请填写联系人姓名', icon: 'none' }); return }
      if (!this.guestPhone || this.guestPhone.length !== 11) { uni.showToast({ title: '请填写正确手机号', icon: 'none' }); return }
      const userInfo = uni.getStorageSync('userInfo') || {}
      const orderNo = 'HT' + Date.now()
      const orderData = {
        orderNo: orderNo,
        userId: userInfo.userId || userInfo.id || 0,
        hotelId: this.bookingHotel.id,
        hotelName: this.bookingHotel.name,
        hotelCover: this.bookingHotel.cover || '',
        roomType: '标准房',
        quantity: this.guestCount,
        price: this.bookingHotel.price,
        totalPrice: this.bookingHotel.price * this.guestCount * this.nights,
        checkInDate: this.checkInDate,
        checkOutDate: this.checkOutDate,
        contactName: this.guestName,
        contactPhone: this.guestPhone,
        remark: '酒店预订'
      }

      try {
        const res = await hotelOrderApi.create(orderData)
        const serverOrderNo = (res && res.order) ? res.order.orderNo : orderNo
        this.showBookModal = false
        uni.showModal({
          title: '预订成功',
          content: '订单号：' + serverOrderNo + '\n请在"我的订单"中完成支付',
          showCancel: false,
          success: () => { uni.navigateTo({ url: '/pages/order/hotel-order' }) }
        })
      } catch (e) {
        console.error('创建酒店订单失败:', e)
        // 降级到本地存储
        const order = {
          id: orderNo,
          orderType: 'hotel',
          userId: userInfo.userId || userInfo.id || 0,
          hotelName: this.bookingHotel.name + ' ' + this.nights + '晚',
          ticketName: this.bookingHotel.name + ' ' + this.nights + '晚',
          cover: this.bookingHotel.cover,
          hotelCover: this.bookingHotel.cover,
          date: this.checkInDate + ' 至 ' + this.checkOutDate,
          nights: this.nights,
          quantity: this.guestCount,
          totalPrice: this.bookingHotel.price * this.guestCount * this.nights,
          contactName: this.guestName,
          contactPhone: this.guestPhone,
          status: '待支付',
          createTime: formatOrderTime(new Date())
        }
        const orders = uni.getStorageSync('orders') || []
        orders.unshift(order)
        uni.setStorageSync('orders', orders)
        this.showBookModal = false
        uni.showModal({
          title: '预订成功',
          content: '订单号：' + order.id + '\n请在"我的订单"中完成支付',
          showCancel: false,
          success: () => { uni.navigateTo({ url: '/pages/order/hotel-order' }) }
        })
      }
    }
  }
}
</script>

<style scoped lang="scss">
.hotel-page { background: #F8FAFC; min-height: 100vh; }

.search-bar-card { background: #FFFFFF; padding: 24rpx 30rpx 30rpx; }
.search-location { display: flex; align-items: center; gap: 10rpx; margin-bottom: 20rpx; }
.search-loc-icon { width: 28rpx; height: 28rpx; }
.search-loc-text { font-size: 28rpx; color: #1D3B5A; font-weight: 600; }
.search-dates { display: flex; align-items: center; background: #F8F9FA; border-radius: 16rpx; padding: 20rpx; }
.date-box { flex: 1; display: flex; flex-direction: column; align-items: center; gap: 6rpx; }
.date-label { font-size: 22rpx; color: #94A3B8; }
.date-val { font-size: 28rpx; color: #1D3B5A; font-weight: 600; }
.date-nights { display: flex; flex-direction: column; align-items: center; padding: 0 30rpx; border-left: 1rpx solid #E2E8F0; border-right: 1rpx solid #E2E8F0; }
.nights-num { font-size: 40rpx; font-weight: 600; color: #FF7F50; }
.nights-label { font-size: 20rpx; color: #94A3B8; }

.filter-bar { display: flex; gap: 16rpx; padding: 16rpx 24rpx; background: #FFFFFF; overflow-x: auto; border-bottom: 1rpx solid #E2E8F0; }
.filter-item { white-space: nowrap; padding: 10rpx 24rpx; border-radius: 30rpx; font-size: 24rpx; color: #64748B; background: #F1F5F9; }
.filter-item.active { background: #4A90E2; color: #FFFFFF; }

.hotel-list { padding: 20rpx 24rpx; display: flex; flex-direction: column; gap: 20rpx; }
.hotel-card { background: #FFFFFF; border-radius: 20rpx; overflow: hidden; box-shadow: 0 4rpx 20rpx rgba(74,144,226,0.12); }
.hotel-img { height: 240rpx; position: relative; }
.hotel-cover-img { width: 100%; height: 100%; }
.hotel-type-badge { position: absolute; top: 16rpx; left: 16rpx; background: rgba(44,62,80,0.85); color: #FFFFFF; font-size: 22rpx; padding: 6rpx 16rpx; border-radius: 20rpx; }
.hotel-info { padding: 20rpx; }
.hotel-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 10rpx; }
.hotel-name { font-size: 30rpx; font-weight: 600; color: #2C3E50; }
.hotel-rating { font-size: 24rpx; color: #FF7F50; font-weight: 600; }
.hotel-address { display: flex; align-items: center; gap: 6rpx; font-size: 22rpx; color: #94A3B8; margin-bottom: 12rpx; }
.address-icon { width: 22rpx; height: 22rpx; }
.hotel-facilities { display: flex; flex-wrap: wrap; gap: 8rpx; margin-bottom: 12rpx; }
.facility-tag { font-size: 20rpx; color: #2C3E50; background: rgba(44,62,80,0.08); padding: 4rpx 14rpx; border-radius: 20rpx; }
.hotel-desc { display: block; font-size: 24rpx; color: #64748B; line-height: 1.6; margin-bottom: 16rpx; }
.hotel-footer { display: flex; justify-content: space-between; align-items: center; }
.hotel-footer-actions { display: flex; align-items: center; gap: 16rpx; }
.hotel-location-btn {
  display: flex;
  align-items: center;
  gap: 6rpx;
  font-size: 22rpx;
  color: #64748B;
  padding: 10rpx 20rpx;
  border-radius: 20rpx;
  background: #F1F5F9;
}
.hotel-location-icon { width: 24rpx; height: 24rpx; }
.hotel-price-wrap { display: flex; align-items: baseline; gap: 4rpx; }
.hotel-price-label { font-size: 24rpx; color: #4A90E2; }
.hotel-price { font-size: 40rpx; font-weight: 600; color: #4A90E2; }
.hotel-price-unit { font-size: 22rpx; color: #94A3B8; }
.hotel-book-btn { background: linear-gradient(135deg, #1D3B5A, #2E5F8E); color: #FFFFFF; font-size: 26rpx; padding: 16rpx 36rpx; border-radius: 30rpx; box-shadow: 0 4rpx 12rpx rgba(29,59,90,0.3); }

.hotel-modal { position: fixed; top: 0; left: 0; right: 0; bottom: 0; background: rgba(29,59,90,0.6); display: flex; align-items: flex-end; z-index: 500; }
.hotel-modal-box { width: 100%; background: #FFFFFF; border-radius: 32rpx 32rpx 0 0; overflow: hidden; }
.hotel-modal-img { height: 280rpx; position: relative; }
.hotel-modal-cover { width: 100%; height: 100%; }
.hotel-modal-close { position: absolute; top: 20rpx; right: 24rpx; font-size: 36rpx; color: rgba(255,255,255,0.95); background: rgba(29,59,90,0.5); width: 60rpx; height: 60rpx; border-radius: 50%; display: flex; align-items: center; justify-content: center; backdrop-filter: blur(8rpx); }
.hotel-modal-content { padding: 30rpx 30rpx 50rpx; background: linear-gradient(180deg, #F0F7FF 0%, #FFFFFF 100%); }
.hotel-modal-header { display: flex; align-items: center; gap: 16rpx; margin-bottom: 12rpx; }
.hotel-modal-name { font-size: 36rpx; font-weight: 700; color: #1D3B5A; flex: 1; }
.hotel-modal-type { font-size: 22rpx; color: #4A90E2; background: linear-gradient(135deg, #E8F4FD, #F0F7FF); padding: 6rpx 16rpx; border-radius: 20rpx; border: 1rpx solid #B3D9F5; }
.hotel-modal-rating { display: block; font-size: 24rpx; color: #64748B; margin-bottom: 16rpx; }
.hotel-modal-desc { display: block; font-size: 26rpx; color: #475569; line-height: 1.8; margin-bottom: 16rpx; }
.hotel-modal-facilities { display: flex; flex-wrap: wrap; gap: 10rpx; margin-bottom: 24rpx; }
.hotel-modal-price-row { display: flex; justify-content: space-between; align-items: center; background: linear-gradient(135deg, #F8FBFF, #FFFFFF); padding: 20rpx 24rpx; border-radius: 16rpx; border: 1rpx solid #E8F4FD; }
.hotel-modal-price-label { display: block; font-size: 22rpx; color: #64748B; margin-bottom: 4rpx; }
.hotel-modal-price-wrap { display: flex; align-items: baseline; gap: 4rpx; }
.hotel-modal-price { font-size: 44rpx; font-weight: 700; color: #1D3B5A; }
.hotel-modal-price-unit { font-size: 24rpx; color: #94A3B8; }
.hotel-modal-book { background: linear-gradient(135deg, #1D3B5A, #2E5F8E); color: #FFFFFF; font-size: 28rpx; font-weight: 600; padding: 20rpx 48rpx; border-radius: 50rpx; box-shadow: 0 4rpx 16rpx rgba(29,59,90,0.35); }

.book-modal { position: fixed; top: 0; left: 0; right: 0; bottom: 0; background: rgba(29,59,90,0.6); display: flex; align-items: flex-end; z-index: 600; }
.book-box { width: 100%; background: #FFFFFF; border-radius: 32rpx 32rpx 0 0; padding: 30rpx 30rpx 60rpx; }
.book-title { display: block; font-size: 34rpx; font-weight: 700; color: #1D3B5A; text-align: center; margin-bottom: 24rpx; }
.book-form { background: linear-gradient(180deg, #F0F7FF, #FFFFFF); border-radius: 16rpx; padding: 0 20rpx; margin-bottom: 24rpx; border: 1rpx solid #E8F4FD; }
.book-form-item { display: flex; align-items: center; justify-content: space-between; padding: 20rpx 0; border-bottom: 1rpx solid #E8F4FD; }
.book-form-item:last-child { border-bottom: none; }
.book-form-label { font-size: 26rpx; color: #64748B; }
.book-form-val { font-size: 26rpx; color: #1D3B5A; font-weight: 600; }
.book-qty { display: flex; align-items: center; gap: 24rpx; }
.qty-btn { width: 52rpx; height: 52rpx; border-radius: 50%; background: linear-gradient(135deg, #E8F4FD, #F0F7FF); border: 1rpx solid #4A90E2; display: flex; align-items: center; justify-content: center; font-size: 28rpx; color: #4A90E2; }
.qty-num { font-size: 30rpx; font-weight: 600; color: #1D3B5A; min-width: 40rpx; text-align: center; }
.book-input { flex: 1; font-size: 26rpx; color: #1D3B5A; text-align: right; }
.book-total-row { display: flex; justify-content: space-between; align-items: center; padding: 20rpx 24rpx; background: linear-gradient(135deg, #F8FBFF, #FFFFFF); border-radius: 16rpx; border: 1rpx solid #E8F4FD; margin-bottom: 24rpx; }
.book-total-label { font-size: 28rpx; color: #64748B; font-weight: 600; }
.book-total-price { font-size: 40rpx; font-weight: 700; color: #1D3B5A; }
.book-actions { display: flex; gap: 20rpx; }
.book-cancel { flex: 1; text-align: center; padding: 24rpx; background: #F1F5F9; border-radius: 50rpx; font-size: 28rpx; color: #64748B; }
.book-confirm { flex: 2; text-align: center; padding: 24rpx; background: linear-gradient(135deg, #4A90E2, #357ABD); border-radius: 50rpx; font-size: 28rpx; color: #FFFFFF; font-weight: 600; box-shadow: 0 4rpx 16rpx rgba(74,144,226,0.35); }
</style>
