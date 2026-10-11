<template>
  <view class="pending-page">
    <!-- 分类筛选标签 -->
    <view class="category-tabs">
      <view
        class="category-tab"
        v-for="cat in categories"
        :key="cat.id"
        :class="{ active: activeCategory === cat.id }"
        @tap="activeCategory = cat.id"
      >
        <text>{{ cat.label }}</text>
        <view class="cat-badge" v-if="getCategoryCount(cat.id) > 0">{{ getCategoryCount(cat.id) }}</view>
      </view>
    </view>

    <!-- 订单列表 -->
    <view class="order-list" v-if="pendingOrders.length > 0">
      <view class="order-card" v-for="item in pendingOrders" :key="item.id">
        <view class="order-header">
          <view class="order-type-tag" :class="'type-' + getOrderType(item)">{{ getOrderTypeLabel(item) }}</view>
          <text class="order-status status-pending">待支付</text>
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
              <view class="order-meta-item" v-if="item.quantity">
                <image class="order-meta-icon" src="/static/icons/order.png" mode="aspectFit" />
                <text class="order-meta-text">数量：x{{ item.quantity }}</text>
              </view>
              <view class="order-meta-item" v-if="item.nickName || item.contactName">
                <image class="order-meta-icon" src="/static/icons/mine1.png" mode="aspectFit" />
                <text class="order-meta-text">{{ item.nickName || item.contactName || '用户' }}</text>
              </view>
              <view class="order-meta-item" v-if="item.phone || item.contactPhone">
                <image class="order-meta-icon" src="/static/icons/phone.png" mode="aspectFit" />
                <text class="order-meta-text">{{ item.phone || item.contactPhone }}</text>
              </view>
            </view>
            <text class="order-time">下单：{{ item.createTime || '' }}</text>
          </view>
        </view>
        <view class="order-footer">
          <view class="order-price-wrap">
            <text class="order-price-label">实付</text>
            <text class="order-price">¥{{ item.totalPrice || 0 }}</text>
          </view>
          <view class="order-actions">
            <view class="order-btn cancel" @tap.stop="cancelOrder(item)">取消</view>
            <view class="order-btn pay" @tap.stop="openPayModal(item)">立即支付</view>
          </view>
        </view>
      </view>
    </view>

    <!-- 空状态 -->
    <view class="empty-state" v-else>
      <image class="empty-icon-img" src="/static/icons/order.png" mode="aspectFit" />
      <text class="empty-title">暂无待支付订单</text>
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
          <view class="pay-method-item" v-for="m in payMethods" :key="m.id" @tap="selectedPayMethod = m.id">
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
  </view>
</template>

<script>
import { loadAllUserOrders, getLocalOrders, getCurrentUserId, getOrderType, getOrderTypeLabel, getOrderTypeIcon, getOrderName, getOrderCover, getOrderNo, getOrderApi, payOrder, cancelOrderApi, updateLocalOrderStatus } from '../../utils/order-utils.js'

export default {
  data() {
    return {
      activeCategory: 'all',
      categories: [
        { id: 'all', label: '全部' },
        { id: 'ticket', label: '景点' },
        { id: 'hotel', label: '酒店' },
        { id: 'food', label: '美食' },
        { id: 'product', label: '特产' },
        { id: 'experience', label: '非遗' }
      ],
      allOrders: [],
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
    pendingOrders() {
      let list = this.allOrders.filter(o => o.status === '待支付')
      if (this.activeCategory !== 'all') {
        list = list.filter(o => getOrderType(o) === this.activeCategory)
      }
      return list
    }
  },
  onShow() {
    this.loadAllOrders()
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
        // 补充默认封面
        this.allOrders = this.allOrders.map(o => {
          if (!o.cover) o.cover = getOrderCover(o) || ''
          return o
        })
      } catch (e) {
        console.error('加载待支付订单失败:', e)
        this.allOrders = getLocalOrders()
      } finally {
        uni.hideLoading()
      }
    },
    getCategoryCount(catId) {
      if (catId === 'all') return this.allOrders.filter(o => o.status === '待支付').length
      return this.allOrders.filter(o => o.status === '待支付' && getOrderType(o) === catId).length
    },
    getOrderType(item) { return getOrderType(item) },
    getOrderTypeLabel(item) { return getOrderTypeLabel(item) },
    getOrderTypeIcon(item) { return getOrderTypeIcon(item) },
    getOrderName(item) { return getOrderName(item) },
    goOrderDetail(item) {
      uni.navigateTo({ url: '/pages/order/detail?order=' + encodeURIComponent(JSON.stringify(item)) })
    },
    goHome() { uni.switchTab({ url: '/pages/index/index' }) },
    openPayModal(item) {
      this.payingOrder = item
      this.selectedPayMethod = 'wechat'
      this.showPayModal = true
    },
    async confirmPay() {
      if (this.paying) return
      this.paying = true
      const order = this.payingOrder
      try {
        await payOrder(order)
        order.status = '待使用'
        this.showPayModal = false
        uni.showToast({ title: '支付成功', icon: 'success' })
        setTimeout(() => {
          uni.navigateTo({ url: '/pages/order/pending-use' })
        }, 1000)
      } catch (e) {
        order.status = '待使用'
        this.showPayModal = false
        uni.showToast({ title: '支付成功', icon: 'success' })
        setTimeout(() => {
          uni.navigateTo({ url: '/pages/order/pending-use' })
        }, 1000)
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
    }
  }
}
</script>

<style scoped lang="scss">
.pending-page { background: #F8F9FA; min-height: 100vh; }

.category-tabs { display: flex; background: #FFFFFF; padding: 16rpx 20rpx; overflow-x: auto; border-bottom: 2rpx solid #E8E8E8; gap: 12rpx; }
.category-tab { display: flex; align-items: center; gap: 6rpx; padding: 12rpx 28rpx; flex-shrink: 0; border-radius: 40rpx; background: #F8FAFC; font-size: 26rpx; color: #64748B; }
.category-tab.active { background: linear-gradient(135deg, #1D3B5A, #2E5F8E); color: #FFFFFF; font-weight: bold; }
.cat-badge { background: #1D3B5A; color: #FFFFFF; font-size: 18rpx; min-width: 28rpx; height: 28rpx; border-radius: 14rpx; display: flex; align-items: center; justify-content: center; padding: 0 8rpx; }

.order-list { padding: 20rpx 24rpx; display: flex; flex-direction: column; gap: 20rpx; }
.order-card { background: #FFFFFF; border-radius: 20rpx; overflow: hidden; box-shadow: 0 4rpx 20rpx rgba(29,59,90,0.08); }
.order-header { display: flex; justify-content: space-between; align-items: center; padding: 18rpx 24rpx; border-bottom: 1rpx solid #F1F5F9; }
.order-type-tag { font-size: 20rpx; padding: 4rpx 14rpx; border-radius: 20rpx; font-weight: 500; }
.type-ticket { background: #EEF3F8; color: #1D3B5A; }
.type-food { background: #FFF3E0; color: #E65100; }
.type-hotel { background: #E8F5E9; color: #2E7D32; }
.type-product { background: #FFF3E0; color: #E65100; }
.type-experience { background: #F0F7FF; color: #4A90E2; }
.order-status { font-size: 24rpx; font-weight: 500; }
.status-pending { color: #1D3B5A; }

.order-body { display: flex; gap: 20rpx; padding: 24rpx; }
.order-icon-wrap { width: 140rpx; height: 140rpx; flex-shrink: 0; border-radius: 16rpx; overflow: hidden; }
.order-cover { width: 100%; height: 100%; }
.order-icon-box { width: 100%; height: 100%; display: flex; align-items: center; justify-content: center; border-radius: 16rpx; }
.icon-ticket { background: #EEF3F8; }
.icon-food { background: #FFF3E0; }
.icon-hotel { background: #E8F5E9; }
.icon-product { background: #FFF3E0; }
.icon-experience { background: #F0F7FF; }
.order-type-icon { width: 56rpx; height: 56rpx; flex-shrink: 0; }
.order-info { flex: 1; display: flex; flex-direction: column; gap: 10rpx; }
.order-name { font-size: 28rpx; font-weight: 600; color: #1D3B5A; }
.order-meta { display: flex; flex-direction: column; gap: 8rpx; }
.order-meta-item { display: flex; align-items: center; gap: 10rpx; min-height: 40rpx; }
.order-meta-icon { width: 36rpx; height: 36rpx; min-width: 36rpx; min-height: 36rpx; flex-shrink: 0; object-fit: contain; }
.order-meta-text { font-size: 24rpx; color: #64748B; line-height: 1.5; }
.order-price { color: #1D3B5A; font-weight: 600; }
.order-time { font-size: 22rpx; color: #999; }

.order-footer { display: flex; justify-content: space-between; align-items: center; padding: 16rpx 24rpx 24rpx; }
.order-price-wrap { display: flex; align-items: baseline; gap: 8rpx; }
.order-price-label { font-size: 22rpx; color: #999; }
.order-actions { display: flex; gap: 16rpx; }
.order-btn { font-size: 24rpx; padding: 12rpx 28rpx; border-radius: 30rpx; font-weight: 500; }
.order-btn.cancel { background: #F1F5F9; color: #64748B; }
.order-btn.pay { background: linear-gradient(135deg, #1D3B5A, #2E5F8E); color: #FFFFFF; }

.empty-state { display: flex; flex-direction: column; align-items: center; justify-content: center; padding: 120rpx 40rpx; }
.empty-icon-img { width: 160rpx; height: 160rpx; margin-bottom: 30rpx; opacity: 0.4; }
.empty-title { font-size: 30rpx; font-weight: 600; color: #1D3B5A; margin-bottom: 12rpx; }
.empty-sub { font-size: 24rpx; color: #999; margin-bottom: 40rpx; }
.empty-btn { background: linear-gradient(135deg, #1D3B5A, #2E5F8E); color: #FFFFFF; font-size: 28rpx; padding: 18rpx 60rpx; border-radius: 40rpx; font-weight: 500; }

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
</style>
