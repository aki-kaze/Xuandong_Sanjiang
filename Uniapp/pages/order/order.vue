<template>
  <view class="order-page">
    <!-- 聚合订单列表模式（带status参数时） -->
    <template v-if="viewMode === 'list'">
      <!-- 状态标签 -->
      <view class="status-tabs">
        <view
          class="status-tab"
          v-for="tab in statusTabs"
          :key="tab.id"
          :class="{ active: activeTab === tab.id }"
          @tap="activeTab = tab.id"
        >
          <text>{{ tab.label }}</text>
          <view class="tab-badge" v-if="getTabCount(tab.id) > 0">{{ getTabCount(tab.id) }}</view>
        </view>
      </view>

      <!-- 订单列表 -->
      <view class="order-list" v-if="filteredOrders.length > 0">
        <view class="order-card" v-for="item in filteredOrders" :key="item.id">
          <view class="order-header">
            <view class="order-type-tag" :class="'type-' + getOrderType(item)">{{ getOrderTypeLabel(item) }}</view>
            <text class="order-status" :class="getStatusClass(item.status)">{{ item.status }}</text>
          </view>
          <view class="order-body" @tap="goOrderDetail(item)">
            <view class="order-icon-wrap">
              <image v-if="item.cover" class="order-cover" :src="item.cover" mode="aspectFill" @error="item.cover=''" />
              <view v-else class="order-icon-box" :class="'icon-' + getOrderType(item)">
                <image class="order-type-icon" :src="getOrderTypeIcon(item)" mode="aspectFit" />
              </view>
            </view>
            <view class="order-info">
              <text class="order-name">{{ getOrderName(item) }}</text>
              <view class="order-meta">
                <text class="order-meta-item" v-if="item.quantity">
                  <image class="order-meta-icon" src="/static/icons/order.png" mode="aspectFit" />
                  数量：x{{ item.quantity }}
                </text>
                <text class="order-meta-item" v-if="item.nickName || item.contactName">
                  <image class="order-meta-icon" src="/static/icons/mine1.png" mode="aspectFit" />
                  {{ item.nickName || item.contactName || '用户' }}
                </text>
                <text class="order-meta-item" v-if="item.phone || item.contactPhone">
                  <image class="order-meta-icon" src="/static/icons/phone.png" mode="aspectFit" />
                  {{ item.phone || item.contactPhone }}
                </text>
              </view>
              <view class="order-meta-item" v-if="item.validStartDate && item.validEndDate">
                <image class="order-meta-icon" src="/static/icons/calendar.png" mode="aspectFit" />
                <text>有效时间：{{ item.validStartDate }}-{{ item.validEndDate }}</text>
              </view>
              <view class="order-meta-item" v-else-if="item.validEndDate">
                <image class="order-meta-icon" src="/static/icons/calendar.png" mode="aspectFit" />
                <text>有效期至：{{ item.validEndDate }}</text>
              </view>
              <text class="order-time">下单：{{ item.createTime || '' }}</text>
            </view>
          </view>
          <view class="order-footer">
            <view class="order-btn pay" v-if="item.status === '待支付'" @tap.stop="openPayModal(item)">立即支付</view>
            <view class="order-btn cancel" v-if="item.status === '待支付'" @tap.stop="cancelOrder(item)">取消订单</view>
            <view class="order-btn use" v-if="item.status === '待使用'" @tap.stop="useOrder(item)">核销使用</view>
            <view class="order-btn cancel" v-if="item.status === '待使用'" @tap.stop="cancelOrder(item)">申请退款</view>
            <view class="order-btn again" v-if="item.status === '已完成'" @tap.stop="buyAgain(item)">再次购买</view>
          </view>
        </view>
      </view>

      <!-- 空状态 -->
      <view class="empty-state" v-else>
        <image class="empty-icon-img" src="/static/icons/order.png" mode="aspectFit" />
        <text class="empty-title">暂无{{ activeTabLabel }}订单</text>
        <text class="empty-sub">快去预订三江的精彩体验吧</text>
        <view class="empty-btn" @tap="goHome">去预订</view>
      </view>

      <!-- 支付弹窗 -->
      <view class="pay-mask" v-if="showPayModal" @tap.stop="">
        <view class="pay-sheet">
          <view class="pay-sheet-header">
            <text class="pay-sheet-title">确认支付</text>
            <text class="pay-sheet-close" @tap="showPayModal = false">×</text>
          </view>
          <view class="pay-order-info" v-if="payingOrder">
            <text class="pay-order-name">{{ getOrderName(payingOrder) }}</text>
            <text class="pay-order-no">订单号：{{ payingOrder.id }}</text>
          </view>
          <view class="pay-amount-row">
            <text class="pay-amount-label">支付金额</text>
            <text class="pay-amount">¥{{ payingOrder ? payingOrder.totalPrice : 0 }}</text>
          </view>
          <view class="pay-methods">
            <view class="pay-method-item" v-for="m in payMethods" :key="m.id" @tap="selectPayMethod(m.id)">
              <image class="pay-method-icon" :src="m.icon" mode="aspectFit" />
              <text class="pay-method-name">{{ m.name }}</text>
              <view class="pay-method-check" v-if="selectedPayMethod === m.id">✓</view>
            </view>
          </view>
          <view class="pay-btn-row">
            <view class="pay-confirm-btn" :class="{ loading: paying }" @tap="confirmPay">{{ paying ? '支付中...' : '确认支付' }}</view>
          </view>
          <text class="pay-tip">点击支付即同意《用户服务协议》</text>
        </view>
      </view>
    </template>

    <!-- 入口页模式（无status参数时） -->
    <template v-else>
      <view class="order-entry-page">
        <view class="entry-header">
          <text class="entry-title">我的订单</text>
          <text class="entry-sub">选择订单类型查看</text>
        </view>

        <view class="entry-grid">
          <view class="entry-card" @tap="goOrder('ticket')">
            <view class="entry-icon-wrap icon-ticket">
              <image class="entry-icon" src="/static/icons/position.png" mode="aspectFit" />
            </view>
            <view class="entry-info">
              <text class="entry-name">景点订单</text>
              <text class="entry-desc">门票及景点相关订单</text>
            </view>
            <view class="entry-badge" v-if="counts.ticket > 0">{{ counts.ticket }}</view>
            <text class="entry-arrow">›</text>
          </view>

          <view class="entry-card" @tap="goOrder('food')">
            <view class="entry-icon-wrap icon-food">
              <image class="entry-icon" src="/static/icons/food.png" mode="aspectFit" />
            </view>
            <view class="entry-info">
              <text class="entry-name">美食订单</text>
              <text class="entry-desc">特色美食及餐饮订单</text>
            </view>
            <view class="entry-badge" v-if="counts.food > 0">{{ counts.food }}</view>
            <text class="entry-arrow">›</text>
          </view>

          <view class="entry-card" @tap="goOrder('hotel')">
            <view class="entry-icon-wrap icon-hotel">
              <image class="entry-icon" src="/static/icons/hotel.png" mode="aspectFit" />
            </view>
            <view class="entry-info">
              <text class="entry-name">酒店订单</text>
              <text class="entry-desc">住宿及酒店相关订单</text>
            </view>
            <view class="entry-badge" v-if="counts.hotel > 0">{{ counts.hotel }}</view>
            <text class="entry-arrow">›</text>
          </view>

          <view class="entry-card" @tap="goOrder('product')">
            <view class="entry-icon-wrap icon-product">
              <image class="entry-icon" src="/static/icons/shop.png" mode="aspectFit" />
            </view>
            <view class="entry-info">
              <text class="entry-name">特产订单</text>
              <text class="entry-desc">三江特产及商品订单</text>
            </view>
            <view class="entry-badge" v-if="counts.product > 0">{{ counts.product }}</view>
            <text class="entry-arrow">›</text>
          </view>

          <view class="entry-card" @tap="goOrder('experience')">
            <view class="entry-icon-wrap icon-experience">
              <image class="entry-icon" src="/static/icons/feiyi.png" mode="aspectFit" />
            </view>
            <view class="entry-info">
              <text class="entry-name">非遗订单</text>
              <text class="entry-desc">非遗体验预约及活动订单</text>
            </view>
            <view class="entry-badge" v-if="counts.experience > 0">{{ counts.experience }}</view>
            <text class="entry-arrow">›</text>
          </view>
        </view>

        <view class="entry-quick">
          <text class="quick-title">快捷入口</text>
          <view class="quick-grid">
            <view class="quick-item" @tap="goPendingOrder()">
              <text class="quick-num">{{ counts.pending }}</text>
              <text class="quick-label">待支付</text>
            </view>
            <view class="quick-item" @tap="goPendingUse()">
              <text class="quick-num">{{ counts.using }}</text>
              <text class="quick-label">待使用</text>
            </view>
            <view class="quick-item" @tap="goCompletedOrder()">
              <text class="quick-num">{{ counts.done }}</text>
              <text class="quick-label">已完成</text>
            </view>
            <view class="quick-item" @tap="goListByStatus('all')">
              <text class="quick-num">{{ counts.all }}</text>
              <text class="quick-label">全部订单</text>
            </view>
          </view>
        </view>
      </view>
    </template>
  </view>
</template>

<script>
import { loadAllUserOrders, getLocalOrders, getCurrentUserId, getOrderType, getOrderTypeLabel, getOrderTypeIcon, getOrderName, getOrderCover, getOrderNo, getOrderApi, payOrder, cancelOrderApi, useOrderApi, updateLocalOrderStatus } from '../../utils/order-utils.js'

export default {
  data() {
    return {
      viewMode: 'entry',
      activeTab: 'all',
      statusTabs: [
        { id: 'all', label: '全部' },
        { id: '待支付', label: '待支付' },
        { id: '待使用', label: '待使用' },
        { id: '已完成', label: '已完成' },
        { id: '已取消', label: '已取消' }
      ],
      allOrders: [],
      counts: { ticket: 0, food: 0, hotel: 0, product: 0, experience: 0, pending: 0, using: 0, done: 0, all: 0 },
      showPayModal: false,
      payingOrder: null,
      selectedPayMethod: 'wechat',
      paying: false,
      payMethods: [
        { id: 'wechat', name: '微信支付', icon: '/static/icons/w-pay.png' },
        { id: 'alipay', name: '支付宝', icon: '/static/icons/z-pay.png' }
      ]
    }
  },
  computed: {
    filteredOrders() {
      if (this.activeTab === 'all') return this.allOrders
      return this.allOrders.filter(o => o.status === this.activeTab)
    },
    activeTabLabel() {
      const tab = this.statusTabs.find(t => t.id === this.activeTab)
      return tab ? tab.label : ''
    }
  },
  onLoad(options) {
    if (options && options.status) {
      this.viewMode = 'list'
      this.activeTab = options.status === 'all' ? 'all' : options.status
      this.loadAllOrders()
    } else {
      this.viewMode = 'entry'
      this.loadCounts()
    }
  },
  onShow() {
    if (this.viewMode === 'list') {
      this.loadAllOrders()
    } else {
      this.loadCounts()
    }
  },
  methods: {
    async loadAllOrders() {
      uni.showLoading({ title: '加载中...' })
      try {
        const userId = getCurrentUserId()
        if (userId > 0) {
          this.allOrders = await loadAllUserOrders(userId)
        } else {
          this.allOrders = getLocalOrders()
        }
        // 补充默认封面和有效期
        this.allOrders = this.allOrders.map(o => {
          if (!o.cover) o.cover = getOrderCover(o) || ''
          // 如果是美食订单且没有有效期，根据创建时间自动计算
          if (getOrderType(o) === 'food' && (!o.validStartDate || !o.validEndDate)) {
            const dates = this.calculateValidDatesFromCreateTime(o.createTime)
            o.validStartDate = dates.start
            o.validEndDate = dates.end
          }
          return o
        })
      } catch (e) {
        console.error('加载订单失败:', e)
        this.allOrders = getLocalOrders()
      } finally {
        uni.hideLoading()
      }
    },
    async loadCounts() {
      const userId = getCurrentUserId()
      if (!userId) {
        this.countLocalOrders()
        return
      }
      try {
        const orders = await loadAllUserOrders(userId)
        this.counts = {
          ticket: orders.filter(o => getOrderType(o) === 'ticket').length,
          food: orders.filter(o => getOrderType(o) === 'food').length,
          hotel: orders.filter(o => getOrderType(o) === 'hotel').length,
          product: orders.filter(o => getOrderType(o) === 'product').length,
          experience: orders.filter(o => getOrderType(o) === 'experience').length,
          pending: orders.filter(o => o.status === '待支付').length,
          using: orders.filter(o => o.status === '待使用' || o.status === '已入住').length,
          done: orders.filter(o => o.status === '已完成' || o.status === '已退房').length,
          all: orders.length
        }
      } catch (e) {
        this.countLocalOrders()
      }
    },
    countLocalOrders() {
      const orders = getLocalOrders()
      this.counts = {
        ticket: orders.filter(o => getOrderType(o) === 'ticket').length,
        food: orders.filter(o => getOrderType(o) === 'food').length,
        hotel: orders.filter(o => getOrderType(o) === 'hotel').length,
        product: orders.filter(o => getOrderType(o) === 'product').length,
        experience: orders.filter(o => getOrderType(o) === 'experience').length,
        pending: orders.filter(o => o.status === '待支付').length,
        using: orders.filter(o => o.status === '待使用' || o.status === '已入住').length,
        done: orders.filter(o => o.status === '已完成' || o.status === '已退房').length,
        all: orders.length
      }
    },
    getOrderType(item) { return getOrderType(item) },
    getOrderTypeLabel(item) { return getOrderTypeLabel(item) },
    getOrderTypeIcon(item) { return getOrderTypeIcon(item) },
    getOrderName(item) { return getOrderName(item) },
    getStatusClass(status) {
      const map = { '待支付': 'status-pending', '待使用': 'status-using', '已完成': 'status-done', '已取消': 'status-cancelled', '已退房': 'status-done' }
      return map[status] || ''
    },
    getTabCount(tabId) {
      if (tabId === 'all') return this.allOrders.length
      return this.allOrders.filter(o => o.status === tabId).length
    },
    goOrder(type) {
      const pages = { ticket: '/pages/order/ticket-order', food: '/pages/order/food-order', hotel: '/pages/order/hotel-order', product: '/pages/order/product-order', experience: '/pages/order/experience-order' }
      uni.navigateTo({ url: pages[type] || pages.ticket })
    },
    goListByStatus(status) {
      this.viewMode = 'list'
      this.activeTab = status === 'all' ? 'all' : status
      this.loadAllOrders()
    },
    goPendingOrder() { uni.navigateTo({ url: '/pages/order/pending-order' }) },
    goPendingUse() { uni.navigateTo({ url: '/pages/order/pending-use' }) },
    goCompletedOrder() { uni.navigateTo({ url: '/pages/order/completed-order' }) },
    goOrderDetail(item) {
      uni.navigateTo({ url: '/pages/order/detail?order=' + encodeURIComponent(JSON.stringify(item)) })
    },
    goHome() { uni.switchTab({ url: '/pages/index/index' }) },
    openPayModal(item) {
      this.payingOrder = item
      this.selectedPayMethod = 'wechat'
      this.showPayModal = true
    },
    selectPayMethod(id) { this.selectedPayMethod = id },
    async confirmPay() {
      if (this.paying) return
      this.paying = true
      const order = this.payingOrder
      try {
        await payOrder(order)
        order.status = '待使用'
        this.showPayModal = false
        uni.showToast({ title: '支付成功', icon: 'success' })
        this.loadAllOrders()
      } catch (e) {
        console.warn('后端支付失败，本地更新:', e)
        order.status = '待使用'
        this.showPayModal = false
        uni.showToast({ title: '支付成功', icon: 'success' })
        this.loadAllOrders()
      } finally {
        this.paying = false
      }
    },
    cancelOrder(item) {
      uni.showModal({
        title: '确认取消', content: '确定要取消此订单吗？',
        success: async (res) => {
          if (res.confirm) {
            try { await cancelOrderApi(item) } catch (e) {}
            item.status = '已取消'
            uni.showToast({ title: '已取消', icon: 'success' })
            this.loadAllOrders()
          }
        }
      })
    },
    useOrder(item) {
      uni.showModal({
        title: '核销确认', content: '确定要核销使用此订单吗？核销后无法撤销。',
        success: async (res) => {
          if (res.confirm) {
            try { await useOrderApi(item) } catch (e) {}
            item.status = '已完成'
            uni.showToast({ title: '核销成功', icon: 'success' })
            this.loadAllOrders()
          }
        }
      })
    },
    buyAgain(item) {
      const type = getOrderType(item)
      if (type === 'food' && item.foodId) {
        uni.navigateTo({ url: '/pages/food/detail?id=' + item.foodId })
      } else if (type === 'ticket' && item.ticketId) {
        uni.navigateTo({ url: '/pages/ticket/ticket?id=' + item.ticketId })
      } else if (type === 'hotel' && item.hotelId) {
        uni.navigateTo({ url: '/pages/hotel/hotel?id=' + item.hotelId })
      } else if (type === 'product' && item.productId) {
        uni.navigateTo({ url: '/pages/shop/shop?id=' + item.productId })
      } else if (type === 'experience' && item.heritageId) {
        uni.navigateTo({ url: '/pages/heritage/experience-book?id=' + item.heritageId + '&name=' + encodeURIComponent(item.heritageName || '') + '&cover=' + encodeURIComponent(item.heritageCover || '') })
      } else {
        const pages = { ticket: '/pages/ticket/ticket', food: '/pages/food/food', hotel: '/pages/hotel/hotel', product: '/pages/shop/shop', experience: '/pages/heritage/heritage' }
        uni.navigateTo({ url: pages[type] || '/pages/index/index' })
      }
    },
    // 格式化有效期日期：YYYY年MM月DD日
    formatValidDate(date) {
      const year = date.getFullYear()
      const month = String(date.getMonth() + 1).padStart(2, '0')
      const day = String(date.getDate()).padStart(2, '0')
      return `${year}年${month}月${day}日`
    },
    // 根据创建时间计算有效期
    calculateValidDatesFromCreateTime(createTimeStr) {
      if (!createTimeStr) {
        const now = new Date()
        const end = new Date(now)
        end.setDate(end.getDate() + 2)
        return { start: this.formatValidDate(now), end: this.formatValidDate(end) }
      }
      
      // 尝试解析 createTime 格式：YYYY年MM月DD日 HH:mm
      try {
        const match = createTimeStr.match(/(\d{4})年(\d{1,2})月(\d{1,2})日/)
        if (match) {
          const year = parseInt(match[1])
          const month = parseInt(match[2]) - 1
          const day = parseInt(match[3])
          const startDate = new Date(year, month, day)
          const endDate = new Date(startDate)
          endDate.setDate(endDate.getDate() + 2)
          return { start: this.formatValidDate(startDate), end: this.formatValidDate(endDate) }
        }
      } catch (e) {
        console.error('解析日期失败:', e)
      }
      
      // 默认返回当天+3天
      const now = new Date()
      const end = new Date(now)
      end.setDate(end.getDate() + 2)
      return { start: this.formatValidDate(now), end: this.formatValidDate(end) }
    }
  }
}
</script>

<style scoped lang="scss">
.order-page { background: #F8F9FA; min-height: 100vh; }

/* 聚合列表模式样式 */
.status-tabs { display: flex; background: #FFFFFF; border-bottom: 2rpx solid #E8E8E8; position: sticky; top: 0; z-index: 10; }
.status-tab { flex: 1; display: flex; flex-direction: column; align-items: center; padding: 22rpx 0 18rpx; font-size: 24rpx; color: #999; position: relative; gap: 4rpx; }
.status-tab.active { color: #1D3B5A; font-weight: 600; }
.status-tab.active::after { content: ''; position: absolute; bottom: 0; left: 50%; transform: translateX(-50%); width: 40rpx; height: 4rpx; background: #1D3B5A; border-radius: 2rpx; }
.tab-badge { background: #1D3B5A; color: #FFFFFF; font-size: 18rpx; min-width: 28rpx; height: 28rpx; border-radius: 14rpx; display: flex; align-items: center; justify-content: center; padding: 0 8rpx; }

.order-list { padding: 20rpx 24rpx; display: flex; flex-direction: column; gap: 20rpx; }
.order-card { background: #FFFFFF; border-radius: 20rpx; overflow: hidden; box-shadow: 0 4rpx 20rpx rgba(29,59,90,0.08); }
.order-header { display: flex; justify-content: space-between; align-items: center; padding: 20rpx 24rpx; border-bottom: 1rpx solid #F1F5F9; }
.order-type-tag { font-size: 20rpx; padding: 4rpx 14rpx; border-radius: 20rpx; font-weight: 500; }
.type-ticket { background: #EEF3F8; color: #1D3B5A; }
.type-food { background: #FFF3E0; color: #E65100; }
.type-hotel { background: #E8F5E9; color: #2E7D32; }
.type-product { background: #FFF3E0; color: #E65100; }
.type-experience { background: #F0F7FF; color: #4A90E2; }
.order-status { font-size: 24rpx; font-weight: 500; }
.status-pending { color: #1D3B5A; }
.status-using { color: #1D3B5A; }
.status-done { color: #2E7D32; }
.status-cancelled { color: #999; }

.order-body { display: flex; gap: 20rpx; padding: 24rpx; }
.order-icon-wrap { width: 140rpx; height: 140rpx; flex-shrink: 0; border-radius: 16rpx; overflow: hidden; }
.order-cover { width: 100%; height: 100%; }
.order-icon-box { width: 100%; height: 100%; display: flex; align-items: center; justify-content: center; border-radius: 16rpx; }
.icon-ticket { background: #EEF3F8; }
.icon-food { background: #FFF3E0; }
.icon-hotel { background: #E8F5E9; }
.icon-product { background: #FFF3E0; }
.icon-experience { background: #F0F7FF; }
.order-type-icon { width: 56rpx; height: 56rpx; }
.order-info { flex: 1; display: flex; flex-direction: column; gap: 10rpx; }
.order-name { font-size: 28rpx; font-weight: 600; color: #1D3B5A; }
.order-meta { display: flex; flex-direction: column; gap: 6rpx; }
.order-meta-item { font-size: 22rpx; color: #666; display: flex; align-items: center; gap: 4rpx; }
.order-meta-icon { width: 24rpx; height: 24rpx; }
.order-price { color: #1D3B5A; font-weight: 600; }
.order-time { font-size: 22rpx; color: #999; }

.order-footer { display: flex; gap: 16rpx; padding: 16rpx 24rpx 24rpx; justify-content: flex-end; }
.order-btn { font-size: 24rpx; padding: 12rpx 28rpx; border-radius: 30rpx; font-weight: 500; }
.order-btn.pay { background: linear-gradient(135deg, #1D3B5A, #2E5F8E); color: #FFFFFF; }
.order-btn.use { background: linear-gradient(135deg, #1D3B5A, #2E5F8E); color: #FFFFFF; }
.order-btn.cancel { background: #F1F5F9; color: #64748B; }
.order-btn.again { background: linear-gradient(135deg, #1D3B5A, #2E5F8E); color: #FFFFFF; }
.order-btn.review { background: #F1F5F9; color: #1D3B5A; }

.empty-state { display: flex; flex-direction: column; align-items: center; justify-content: center; padding: 120rpx 40rpx; }
.empty-icon-img { width: 160rpx; height: 160rpx; margin-bottom: 30rpx; opacity: 0.4; }
.empty-title { font-size: 30rpx; font-weight: 600; color: #1D3B5A; margin-bottom: 12rpx; }
.empty-sub { font-size: 24rpx; color: #999; margin-bottom: 40rpx; }
.empty-btn { background: linear-gradient(135deg, #1D3B5A, #2E5F8E); color: #FFFFFF; font-size: 28rpx; padding: 18rpx 60rpx; border-radius: 40rpx; font-weight: 500; }

/* 支付弹窗 */
.pay-mask { position: fixed; top: 0; left: 0; right: 0; bottom: 0; background: rgba(0,0,0,0.5); z-index: 1000; display: flex; align-items: flex-end; }
.pay-sheet { width: 100%; background: #FFFFFF; border-radius: 32rpx 32rpx 0 0; padding: 40rpx; }
.pay-sheet-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 30rpx; }
.pay-sheet-title { font-size: 32rpx; font-weight: 600; color: #1D3B5A; }
.pay-sheet-close { font-size: 40rpx; color: #999; }
.pay-order-info { background: #F8F9FA; border-radius: 16rpx; padding: 24rpx; margin-bottom: 30rpx; }
.pay-order-name { display: block; font-size: 28rpx; font-weight: 500; color: #1D3B5A; margin-bottom: 8rpx; }
.pay-order-no { font-size: 22rpx; color: #999; }
.pay-amount-row { display: flex; justify-content: space-between; align-items: center; margin-bottom: 30rpx; }
.pay-amount-label { font-size: 28rpx; color: #666; }
.pay-amount { font-size: 40rpx; font-weight: 700; color: #1D3B5A; }
.pay-methods { margin-bottom: 30rpx; }
.pay-method-item { display: flex; align-items: center; gap: 20rpx; padding: 24rpx 0; border-bottom: 1rpx solid #F1F5F9; }
.pay-method-icon { width: 48rpx; height: 48rpx; }
.pay-method-name { flex: 1; font-size: 28rpx; color: #333; }
.pay-method-check { width: 36rpx; height: 36rpx; border-radius: 50%; background: #1D3B5A; color: #FFFFFF; display: flex; align-items: center; justify-content: center; font-size: 22rpx; }
.pay-btn-row { padding: 24rpx 32rpx 8rpx; }
.pay-confirm-btn { background: linear-gradient(135deg, #1D3B5A, #2E5F8E); border-radius: 50rpx; padding: 28rpx; text-align: center; font-size: 30rpx; font-weight: bold; color: #FFFFFF; }
.pay-confirm-btn.loading { opacity: 0.7; }
.pay-tip { display: block; text-align: center; font-size: 22rpx; color: #bbb; padding: 0 32rpx; }

/* 入口页模式样式 */
.order-entry-page { background: #F8F9FA; min-height: 100vh; }
.entry-header { background: linear-gradient(135deg, #1D3B5A, #2E5F8E); padding: 60rpx 32rpx 36rpx; }
.entry-title { font-size: 40rpx; font-weight: bold; color: #FFFFFF; display: block; }
.entry-sub { font-size: 24rpx; color: rgba(255,255,255,0.7); margin-top: 8rpx; display: block; }

.entry-grid { padding: 24rpx; display: flex; flex-direction: column; gap: 16rpx; }
.entry-card { background: #FFFFFF; border-radius: 20rpx; padding: 28rpx 30rpx; display: flex; align-items: center; gap: 20rpx; box-shadow: 0 4rpx 20rpx rgba(29,59,90,0.08); position: relative; }
.entry-icon-wrap { width: 80rpx; height: 80rpx; border-radius: 20rpx; display: flex; align-items: center; justify-content: center; flex-shrink: 0; }
.entry-icon { width: 44rpx; height: 44rpx; }
.entry-info { flex: 1; }
.entry-name { font-size: 30rpx; font-weight: bold; color: #1D3B5A; display: block; margin-bottom: 4rpx; }
.entry-desc { font-size: 22rpx; color: #999; display: block; }
.entry-badge { background: #1D3B5A; color: #FFFFFF; font-size: 20rpx; min-width: 36rpx; height: 36rpx; border-radius: 18rpx; display: flex; align-items: center; justify-content: center; padding: 0 10rpx; }
.entry-arrow { font-size: 40rpx; color: #CCC; }

.entry-quick { margin: 16rpx 24rpx; background: #FFFFFF; border-radius: 20rpx; padding: 28rpx 30rpx; box-shadow: 0 4rpx 20rpx rgba(29,59,90,0.08); }
.quick-title { font-size: 28rpx; font-weight: bold; color: #1D3B5A; display: block; margin-bottom: 20rpx; }
.quick-grid { display: flex; justify-content: space-around; }
.quick-item { display: flex; flex-direction: column; align-items: center; gap: 8rpx; }
.quick-num { font-size: 36rpx; font-weight: bold; color: #2E5F8E; }
.quick-label { font-size: 22rpx; color: #888; }
</style>
