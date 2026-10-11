<template>
  <view class="list-page">
    <!-- 搜索 -->
    <view class="search-bar" @tap="goSearch">
      <image class="search-icon" src="/static/icons/search.png" mode="aspectFit" />
      <text class="search-placeholder">{{ activeTab === 'ticket' ? '搜索三江门票...' : '搜索精选路线...' }}</text>
    </view>

    <!-- 主标签：门票/精选路线 -->
    <view class="main-tabs">
      <view class="main-tab" :class="{ active: activeTab === 'ticket' }" @tap="activeTab = 'ticket'">
        <text>景点门票</text>
      </view>
      <view class="main-tab" :class="{ active: activeTab === 'route' }" @tap="activeTab = 'route'">
        <text>精选路线</text>
      </view>
    </view>

    <!-- 分类筛选（门票用） -->
    <view class="filter-bar" v-if="activeTab === 'ticket'">
      <view class="filter-item" v-for="f in dynamicFilters" :key="f.id" :class="{ active: activeFilter === f.id }" @tap="activeFilter = f.id">
        {{ f.label }}
      </view>
    </view>

    <!-- 路线分类筛选（路线用） -->
    <view class="filter-bar" v-if="activeTab === 'route'">
      <view class="filter-item" v-for="f in routeFilters" :key="f.id" :class="{ active: activeRouteFilter === f.id }" @tap="activeRouteFilter = f.id">
        {{ f.label }}
      </view>
    </view>

    <!-- 排序（门票用） -->
    <view class="sort-bar" v-if="activeTab === 'ticket'">
      <view class="sort-item" v-for="s in sorts" :key="s.id" :class="{ active: activeSort === s.id }" @tap="activeSort = s.id">
        {{ s.label }}
      </view>
    </view>

    <!-- 门票列表 -->
    <view class="ticket-list" v-if="activeTab === 'ticket'">
      <view class="ticket-card card-shadow" v-for="item in sortedTickets" :key="item.id" @tap="showTicketDetail(item)">
        <view class="ticket-img-wrap">
          <image class="ticket-img" :src="item.cover" mode="aspectFill" />
          <view class="ticket-price-badge">¥{{ item.price }}</view>
        </view>
        <view class="ticket-info">
          <view class="ticket-header">
            <text class="ticket-name">{{ item.name }}</text>
            <text class="ticket-rating">⭐{{ item.rating || 4.5 }}</text>
          </view>
          <view class="ticket-tags">
            <text class="t-tag" v-for="tag in (item.includesArray || []).slice(0,2)" :key="tag">{{ tag }}</text>
          </view>
          <text class="ticket-desc">{{ (item.description || '').slice(0, 50) }}...</text>
          <view class="ticket-footer">
            <text class="ticket-addr">📍 {{ (item.address || '').slice(0, 15) }}...</text>
            <view class="ticket-btns">
              <view class="btn-map" @tap.stop="goMap(item)">导航</view>
              <view class="btn-buy" @tap.stop="showTicketDetail(item)">购票</view>
            </view>
          </view>
        </view>
      </view>
    </view>

    <!-- 精选路线列表 -->
    <view class="ticket-list" v-if="activeTab === 'route'">
      <view class="route-card card-shadow" v-for="item in filteredRoutes" :key="item.id" @tap="showRouteDetail(item)">
        <view class="route-img-wrap">
          <image class="route-img" :src="item.cover" mode="aspectFill" />
          <view class="route-price-badge">¥{{ item.price }}/人起</view>
        </view>
        <view class="route-info">
          <view class="route-header">
            <text class="route-name">{{ item.name }}</text>
            <view class="route-meta-mini">
              <text class="mini-meta">{{ item.duration }}</text>
              <text class="mini-meta">{{ item.difficulty }}</text>
            </view>
          </view>
          <view class="route-tags">
            <text class="r-tag" v-for="tag in (item.tags || [])" :key="tag">{{ tag }}</text>
          </view>
          <view class="route-stops">
            <view class="stop-item" v-for="(stop, idx) in (item.stops || []).slice(0, 3)" :key="idx">
              <text class="stop-dot"></text>
              <text class="stop-text">{{ stop.name }}</text>
              <text class="stop-line" v-if="idx < 2"></text>
            </view>
            <text class="more-stops" v-if="(item.stops || []).length > 3">+{{ item.stops.length - 3 }}站</text>
          </view>
          <view class="route-footer">
            <text class="route-addr">{{ (item.stops && item.stops[0] && item.stops[0].name ? '起点: ' + item.stops[0].name : '') }}</text>
            <view class="ticket-btns">
              <view class="btn-buy" @tap.stop="showRouteDetail(item)">预订</view>
            </view>
          </view>
        </view>
      </view>
    </view>

    <view style="height: 160rpx;"></view>

    <!-- 购票弹窗 -->
    <view class="pay-modal" v-if="showTicketModal" @tap="closeModal">
      <view class="pay-box" @tap.stop>
        <view class="pay-header">
          <text class="pay-title">{{ selectedType === 'ticket' ? '预订门票' : '预订路线' }}</text>
          <text class="pay-close" @tap.stop="closeModal">✕</text>
        </view>

        <!-- 订单确认 -->
        <view>
          <view class="ticket-info-card">
            <text class="ticket-info-name">{{ selectedItem.name }}</text>
            <view class="ticket-info-price">
              <text class="price-symbol">¥</text>
              <text class="price-value">{{ selectedItem.price }}</text>
            </view>
          </view>

          <view class="pay-info">
            <view class="pay-row">
              <text class="pay-key">数量</text>
              <view class="pay-qty-ctrl">
                <view class="qty-btn" :class="{ disabled: quantity <= 1 }" @tap="quantity > 1 && quantity--">－</view>
                <text class="qty-num">{{ quantity }}</text>
                <view class="qty-btn" :class="{ disabled: quantity >= 5 }" @tap="quantity < 5 && quantity++">＋</view>
              </view>
            </view>
            <view class="pay-row">
              <text class="pay-key">{{ selectedType === 'ticket' ? '游玩日期' : '出发日期' }}</text>
              <picker mode="date" :value="selectedDate" @change="e => selectedDate = e.detail.value">
                <view class="pay-picker">
                  <text class="pay-picker-text">{{ selectedDate }}</text>
                  <text class="pay-picker-arrow">›</text>
                </view>
              </picker>
            </view>
            <view class="pay-row">
              <text class="pay-key">联系人</text>
              <input class="pay-input" v-model="contactName" placeholder="请输入姓名" />
            </view>
            <view class="pay-row">
              <text class="pay-key">手机号</text>
              <input class="pay-input" v-model="contactPhone" placeholder="请输入手机号" type="number" maxlength="11" />
            </view>
            <view class="pay-row total-row">
              <text class="pay-key">合计</text>
              <text class="pay-total">¥{{ selectedItem.price * quantity }}</text>
            </view>
          </view>

          <view class="pay-actions">
            <view class="pay-back-btn" @tap.stop="closeModal">取消</view>
            <view class="pay-btn" @tap.stop="confirmPay">
              <text class="pay-btn-text">确认支付 ¥{{ selectedItem.price * quantity }}</text>
            </view>
          </view>
        </view>
      </view>
    </view>
  </view>
</template>

<script>
import { attractionApi, ticketOrderApi, routeApi, formatOrderTime } from '../../utils/api.js'
import { staticData } from '../../utils/static-data.js'

export default {
  data() {
    return {
      tickets: [],
      routes: [],
      loading: true,
      activeTab: 'ticket',
      activeFilter: 'all',
      activeSort: 'default',
      activeRouteFilter: 'all',
      filters: [
        { id: 'all', label: '全部' },
        { id: '景区门票', label: '景区门票' },
        { id: '体验套票', label: '体验套票' },
        { id: '演出', label: '演出' },
        { id: '跟团游', label: '跟团游' }
      ],
      routeFilters: [
        { id: 'all', label: '全部路线' },
        { id: '1天', label: '一日游' },
        { id: '2天', label: '两日游' },
        { id: '3天', label: '三日游' }
      ],
      sorts: [
        { id: 'default', label: '综合' },
        { id: 'priceAsc', label: '价格低到高' },
        { id: 'priceDesc', label: '价格高到低' }
      ],
      showTicketModal: false,
      selectedType: 'ticket', // 'ticket' or 'route'
      selectedItem: { name: '', price: 0 },
      quantity: 1,
      selectedDate: new Date().toISOString().slice(0, 10),
      contactName: '',
      contactPhone: ''
    }
  },
  computed: {
    dynamicFilters() {
      const categorySet = new Set()
      this.tickets.forEach(t => {
        if (t.type) categorySet.add(t.type)
      })
      const dynamic = [...categorySet].map(c => ({ id: c, label: c }))
      return [{ id: 'all', label: '全部' }, ...dynamic]
    },
    filteredTickets() {
      if (this.activeFilter === 'all') return this.tickets
      return this.tickets.filter(t => t.type === this.activeFilter)
    },
    sortedTickets() {
      const list = [...this.filteredTickets]
      if (this.activeSort === 'priceAsc') return list.sort((a, b) => a.price - b.price)
      if (this.activeSort === 'priceDesc') return list.sort((a, b) => b.price - a.price)
      return list
    },
    filteredRoutes() {
      if (this.activeRouteFilter === 'all') return this.routes
      
      // 更精确的筛选逻辑
      return this.routes.filter(r => {
        const durationStr = String(r.duration || '')
        if (this.activeRouteFilter === '1天') {
          return durationStr.includes('1') || durationStr.includes('一')
        } else if (this.activeRouteFilter === '2天') {
          return durationStr.includes('2') || durationStr.includes('二')
        } else if (this.activeRouteFilter === '3天') {
          return durationStr.includes('3') || durationStr.includes('三')
        }
        return durationStr.includes(this.activeRouteFilter)
      })
    }
  },
  onLoad(options) {
    if (options.tab === 'route') {
      this.activeTab = 'route'
    }
    // 如果有传递路线数据，直接打开购票弹窗
    if (options.route) {
      try {
        const routeItem = JSON.parse(decodeURIComponent(options.route))
        this.$nextTick(() => {
          this.showRouteDetail(routeItem)
        })
      } catch (e) {
        console.error('解析路线数据失败', e)
      }
    }
    this.loadTickets()
    this.loadRoutes()
    const userInfo = uni.getStorageSync('userInfo') || {}
    if (userInfo.name) {
      this.contactName = userInfo.name || ''
    }
    if (userInfo.phone) {
      this.contactPhone = userInfo.phone || ''
    }
  },
  methods: {
    async loadTickets() {
      try {
        const result = await attractionApi.getList(1, 50)
        let list = result.records || result.list || result || []
        if (Array.isArray(list) && list.length > 0) {
          this.tickets = list.map(item => {
            return {
              ...item,
              price: item.ticket_price || item.price || 0,
              original_price: item.ticket_price || item.price || 0,
              type: item.category || '景点门票',
              includes: '["景区门票"]',
              includesArray: ['景区门票'],
              rating: item.rating || 4.5,
              address: item.address || '',
              lat: item.latitude || item.lat || 0,
              lng: item.longitude || item.lng || 0
            }
          })
        } else {
          throw new Error('API返回数据为空')
        }
      } catch (error) {
        console.warn('API调用失败，使用静态数据:', error.message || error)
        this.loadStaticData()
      }
    },
    async loadRoutes() {
      try {
        const result = await routeApi.getList(1, 50)
        let list = result.records || result.list || result || []
        if (Array.isArray(list) && list.length > 0) {
          this.routes = list.map(item => {
            if (typeof item.stops === 'string') {
              try { item.stops = JSON.parse(item.stops) } catch (e) { item.stops = [] }
            }
            if (typeof item.tags === 'string') {
              try { item.tags = JSON.parse(item.tags) } catch (e) { item.tags = [] }
            }
            // 标准化duration格式
            let duration = item.duration || '1天'
            const durationStr = String(duration)
            if (durationStr.includes('1') || durationStr.includes('一')) {
              duration = '1天'
            } else if (durationStr.includes('2') || durationStr.includes('二')) {
              duration = '2天'
            } else if (durationStr.includes('3') || durationStr.includes('三')) {
              duration = '3天'
            }
            return {
              ...item,
              price: item.price || 0,
              duration: duration,
              difficulty: item.difficulty || '轻松'
            }
          })
        } else {
          throw new Error('API返回数据为空')
        }
      } catch (error) {
        console.warn('路线API调用失败，使用静态数据:', error.message || error)
        this.loadStaticRoutes()
      }
    },
    loadStaticData() {
      this.tickets = (staticData.tickets || []).map(item => {
        try {
          if (typeof item.includes === 'string') {
            item.includesArray = JSON.parse(item.includes)
          } else {
            item.includesArray = item.includes || []
          }
        } catch (e) {
          console.error('解析数据失败:', e)
          if (!item.tags) item.tags = []
        }
        try {
          if (typeof item.notice === 'string') {
            item.noticeArray = JSON.parse(item.notice)
          } else {
            item.noticeArray = item.notice || []
          }
        } catch (e) {
          console.error('解析数据失败:', e)
        }
        if (!item.rating) {
          item.rating = 4.5
        }
        return item
      })
    },
    loadStaticRoutes() {
      this.routes = (staticData.routes || []).map(item => {
        if (typeof item.tags === 'string') {
          try { item.tags = JSON.parse(item.tags) } catch (e) { item.tags = [] }
        }
        // 标准化duration格式
        let duration = item.duration || '1天'
        const durationStr = String(duration)
        if (durationStr.includes('1') || durationStr.includes('一')) {
          duration = '1天'
        } else if (durationStr.includes('2') || durationStr.includes('二')) {
          duration = '2天'
        } else if (durationStr.includes('3') || durationStr.includes('三')) {
          duration = '3天'
        }
        return {
          ...item,
          duration: duration
        }
      })
    },
    goSearch() {
      uni.navigateTo({ url: '/pages/search/search' })
    },
    showTicketDetail(item) {
      // 跳转到景点详情页面，而不是直接打开购买弹窗
      uni.navigateTo({
        url: '/pages/attraction/detail?id=' + item.id
      })
    },
    showRouteDetail(item) {
      this.selectedType = 'route'
      this.quantity = 1
      this.selectedDate = new Date().toISOString().slice(0, 10)
      this.selectedItem = item
      this.contactName = ''
      this.contactPhone = ''
      const userInfo = uni.getStorageSync('userInfo') || {}
      if (userInfo.name) {
        this.contactName = userInfo.name
      }
      if (userInfo.phone) {
        this.contactPhone = userInfo.phone
      }
      if (userInfo.nickname && !userInfo.name) {
        this.contactName = userInfo.nickname
      }
      this.showTicketModal = true
    },
    closeModal() {
      this.showTicketModal = false
    },
    async confirmPay() {
      if (!this.contactName.trim()) {
        uni.showToast({ title: '请填写联系人姓名', icon: 'none' })
        return
      }
      if (!this.contactPhone || String(this.contactPhone).length !== 11) {
        uni.showToast({ title: '请填写正确的手机号', icon: 'none' })
        return
      }
      const userInfo = uni.getStorageSync('userInfo') || {}
      const userId = userInfo.userId || userInfo.id || 0
      // 无论是门票还是路线，都归类为景点订单（ticket类型）
      const orderData = {
        orderNo: 'SJ' + Date.now(),
        ticketId: this.selectedItem.id,
        ticketName: this.selectedItem.name + (this.selectedType === 'route' ? ' - 精选路线' : ''),
        ticketCover: this.selectedItem.cover || '',
        ticketType: this.selectedType === 'route' ? '精选路线' : (this.selectedItem.type || '景区门票'),
        price: this.selectedItem.price,
        quantity: this.quantity,
        totalPrice: this.selectedItem.price * this.quantity,
        visitDate: this.selectedDate,
        contactName: this.contactName,
        contactPhone: String(this.contactPhone),
        remark: '',
        userId: userId
      }
      
      this.closeModal()
      try {
        const res = await ticketOrderApi.create(orderData)
        const orderNo = (res && res.order) ? res.order.orderNo : orderData.orderNo
        uni.showModal({
          title: '预订成功！',
          content: '订单号：' + orderNo + '\n请在"我的订单"中查看',
          showCancel: false,
          success: () => { uni.navigateTo({ url: '/pages/order/ticket-order?status=待支付' }) }
        })
      } catch (e) {
        console.error('创建订单失败:', e)
        uni.showModal({
          title: '预订失败',
          content: '网络异常，订单创建失败，请稍后重试',
          showCancel: false
        })
      }
    },
    goMap(item) {
      this.showTicketModal = false
      const lat = parseFloat(item.lat || item.latitude)
      const lng = parseFloat(item.lng || item.longitude)
      const url = '/pages/map/map?targetId=' + (item.attractionId || item.id) + '&targetLat=' + lat + '&targetLng=' + lng + '&targetName=' + encodeURIComponent(item.name) + '&targetAddress=' + encodeURIComponent(item.address || '') + '&targetCover=' + encodeURIComponent(item.cover || '') + '&targetType=ticket'
      uni.navigateTo({ url: url })
    }
  }
}
</script>

<style scoped lang="scss">
.list-page { background: #F8F9FA; min-height: 100vh; }
.card-shadow { box-shadow: 0 4rpx 20rpx rgba(29,59,90,0.08); }

.search-bar { margin: 16rpx 24rpx; background: #FFFFFF; border-radius: 50rpx; padding: 20rpx 30rpx; display: flex; align-items: center; gap: 12rpx; box-shadow: 0 4rpx 20rpx rgba(29,59,90,0.08); border: 1rpx solid #E8E8E8; }
.search-icon { width: 28rpx; height: 28rpx; }
.search-placeholder { font-size: 26rpx; color: #bbb; }

.main-tabs { display: flex; background: #FFFFFF; border-bottom: 1rpx solid #E8E8E8; }
.main-tab { flex: 1; text-align: center; padding: 24rpx 0; font-size: 28rpx; color: #666; position: relative; }
.main-tab.active { color: #1D3B5A; font-weight: bold; }
.main-tab.active::after { content: ''; position: absolute; bottom: 0; left: 50%; transform: translateX(-50%); width: 60rpx; height: 4rpx; background: #1D3B5A; border-radius: 2rpx; }

.filter-bar { display: flex; background: #FFFFFF; padding: 0 16rpx; overflow-x: auto; border-bottom: 1rpx solid #E8E8E8; }
.filter-item { white-space: nowrap; padding: 20rpx 20rpx; font-size: 26rpx; color: #666; position: relative; flex-shrink: 0; }
.filter-item.active { color: #1D3B5A; font-weight: bold; }
.filter-item.active::after { content: ''; position: absolute; bottom: 0; left: 50%; transform: translateX(-50%); width: 30rpx; height: 4rpx; background: #1D3B5A; border-radius: 2rpx; }

.sort-bar { display: flex; background: #FFFFFF; padding: 12rpx 24rpx; gap: 20rpx; border-bottom: 1rpx solid #E8E8E8; }
.sort-item { font-size: 24rpx; color: #999; padding: 6rpx 16rpx; border-radius: 20rpx; }
.sort-item.active { color: #2E5F8E; background: rgba(29,59,90,0.08); font-weight: bold; }

.ticket-list { padding: 20rpx 24rpx; display: flex; flex-direction: column; gap: 20rpx; }
.ticket-card { background: #FFFFFF; border-radius: 20rpx; overflow: hidden; display: flex; border: 1rpx solid #E8E8E8; }
.ticket-img-wrap { position: relative; width: 220rpx; flex-shrink: 0; height: 220rpx; }
.ticket-img { width: 100%; height: 100%; }
.ticket-price-badge { position: absolute; bottom: 10rpx; left: 10rpx; background: linear-gradient(135deg, #1D3B5A, #2E5F8E); color: #FFFFFF; font-size: 20rpx; padding: 4rpx 14rpx; border-radius: 20rpx; }
.ticket-info { flex: 1; padding: 20rpx; display: flex; flex-direction: column; gap: 8rpx; }
.ticket-header { display: flex; justify-content: space-between; align-items: center; }
.ticket-name { font-size: 28rpx; font-weight: bold; color: #1D3B5A; flex: 1; }
.ticket-rating { font-size: 22rpx; color: #2E5F8E; font-weight: bold; }
.ticket-tags { display: flex; flex-wrap: wrap; gap: 8rpx; }
.t-tag { font-size: 20rpx; color: #1D3B5A; background: rgba(29,59,90,0.08); padding: 4rpx 12rpx; border-radius: 20rpx; }
.ticket-desc { font-size: 22rpx; color: #999; line-height: 1.5; flex: 1; }
.ticket-footer { display: flex; justify-content: space-between; align-items: center; }
.ticket-addr { font-size: 20rpx; color: #bbb; flex: 1; }
.ticket-btns { display: flex; gap: 10rpx; }
.btn-map { font-size: 20rpx; color: #1D3B5A; border: 1rpx solid #1D3B5A; padding: 6rpx 16rpx; border-radius: 20rpx; }
.btn-buy { font-size: 20rpx; color: #FFFFFF; background: linear-gradient(135deg, #1D3B5A, #2E5F8E); padding: 6rpx 16rpx; border-radius: 20rpx; }

/* 路线卡片样式 */
.route-card { background: #FFFFFF; border-radius: 20rpx; overflow: hidden; display: flex; border: 1rpx solid #E8E8E8; }
.route-img-wrap { position: relative; width: 220rpx; flex-shrink: 0; height: 220rpx; }
.route-img { width: 100%; height: 100%; }
.route-price-badge { position: absolute; bottom: 10rpx; left: 10rpx; background: linear-gradient(135deg, #4A90E2, #2E5F8E); color: #FFFFFF; font-size: 20rpx; padding: 4rpx 14rpx; border-radius: 20rpx; }
.route-info { flex: 1; padding: 20rpx; display: flex; flex-direction: column; gap: 8rpx; }
.route-header { display: flex; justify-content: space-between; align-items: flex-start; }
.route-name { font-size: 28rpx; font-weight: bold; color: #1D3B5A; flex: 1; }
.route-meta-mini { display: flex; gap: 10rpx; }
.mini-meta { font-size: 20rpx; color: #2E5F8E; background: rgba(29,59,90,0.08); padding: 4rpx 12rpx; border-radius: 20rpx; }
.route-tags { display: flex; flex-wrap: wrap; gap: 8rpx; }
.r-tag { font-size: 20rpx; color: #4A90E2; background: rgba(74,144,226,0.08); padding: 4rpx 12rpx; border-radius: 20rpx; }
.route-stops { display: flex; align-items: center; gap: 6rpx; overflow: hidden; }
.stop-item { display: flex; align-items: center; gap: 4rpx; flex-shrink: 0; }
.stop-dot { width: 10rpx; height: 10rpx; border-radius: 50%; background: #4A90E2; }
.stop-text { font-size: 20rpx; color: #666; }
.stop-line { width: 20rpx; height: 2rpx; background: #E2E8F0; }
.more-stops { font-size: 20rpx; color: #4A90E2; }
.route-footer { display: flex; justify-content: space-between; align-items: center; }
.route-addr { font-size: 20rpx; color: #bbb; flex: 1; }

/* ===== 购票弹窗 ===== */
.pay-modal { position: fixed; top: 0; left: 0; right: 0; bottom: 0; background: rgba(0,0,0,0.5); display: flex; align-items: flex-end; z-index: 500; }
.pay-box { width: 100%; background: #FFFFFF; border-radius: 32rpx 32rpx 0 0; padding: 30rpx 30rpx 60rpx; max-height: 85vh; overflow-y: auto; }
.pay-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 24rpx; }
.pay-title { font-size: 34rpx; font-weight: bold; color: #1D3B5A; }
.pay-close { width: 60rpx; height: 60rpx; background: #F8F9FA; border-radius: 50%; display: flex; align-items: center; justify-content: center; font-size: 28rpx; color: #666; }

.ticket-info-card { background: linear-gradient(135deg, #1D3B5A, #2E5F8E); border-radius: 20rpx; padding: 24rpx; margin-bottom: 24rpx; display: flex; justify-content: space-between; align-items: center; }
.ticket-info-name { font-size: 30rpx; font-weight: bold; color: #FFFFFF; flex: 1; }
.ticket-info-price { display: flex; align-items: baseline; }
.price-symbol { font-size: 24rpx; color: #FFD700; }
.price-value { font-size: 44rpx; font-weight: bold; color: #FFD700; }

.pay-info { display: flex; flex-direction: column; gap: 20rpx; margin-bottom: 24rpx; }
.pay-row { display: flex; justify-content: space-between; align-items: center; }
.pay-key { font-size: 26rpx; color: #666; min-width: 120rpx; }
.pay-qty-ctrl { display: flex; align-items: center; gap: 20rpx; }
.qty-btn { width: 50rpx; height: 50rpx; border-radius: 50%; border: 2rpx solid #1D3B5A; display: flex; align-items: center; justify-content: center; font-size: 28rpx; color: #1D3B5A; }
.qty-btn.disabled { border-color: #ddd; color: #ddd; }
.qty-num { font-size: 32rpx; font-weight: bold; color: #1D3B5A; min-width: 50rpx; text-align: center; }
.pay-picker { display: flex; align-items: center; gap: 8rpx; padding: 12rpx 20rpx; background: #F8F9FA; border-radius: 12rpx; }
.pay-picker-text { font-size: 26rpx; color: #333; }
.pay-picker-arrow { font-size: 28rpx; color: #888; }
.pay-input { flex: 1; padding: 12rpx 20rpx; background: #F8F9FA; border-radius: 12rpx; font-size: 26rpx; color: #333; }
.total-row { border-top: 2rpx solid #E8E8E8; padding-top: 20rpx; margin-top: 10rpx; }
.pay-total { font-size: 40rpx; font-weight: bold; color: #2E5F8E; }

.pay-actions { display: flex; gap: 16rpx; }
.pay-back-btn { flex: 0 0 160rpx; background: #F8F9FA; border-radius: 50rpx; padding: 28rpx; text-align: center; font-size: 28rpx; color: #666; }
.pay-btn { flex: 1; background: linear-gradient(135deg, #1D3B5A, #2E5F8E); border-radius: 50rpx; padding: 28rpx; text-align: center; }
.pay-btn-text { font-size: 30rpx; font-weight: bold; color: #FFFFFF; }
</style>
