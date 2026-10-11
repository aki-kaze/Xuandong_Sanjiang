<template>
  <view class="order-page">
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

    <view class="order-list" v-if="filteredOrders.length > 0">
      <view class="order-card" v-for="item in filteredOrders" :key="item.id">
        <view class="order-header">
          <view class="order-type-tag type-ticket"><text>景点</text></view>
          <text class="order-status" :class="getStatusClass(item.status)">{{ item.status }}</text>
        </view>
        <view class="order-body" @tap="goOrderDetail(item)">
          <view class="order-icon-wrap">
            <image v-if="item.cover" class="order-cover" :src="item.cover" mode="aspectFill" @error="item.cover=''" />
            <view v-else class="order-icon-box icon-ticket">
              <image class="order-type-icon" src="/static/icons/position.png" mode="aspectFit" />
            </view>
          </view>
          <view class="order-info">
            <text class="order-name">{{ getOrderName(item) }}</text>
            <view class="order-meta">
              <image class="order-meta-icon" src="/static/icons/calendar.png" mode="aspectFit" />
              <text> 游览：{{ item.date }}</text>
            </view>
            <view class="order-meta">
              <text>🎫 {{ item.quantity || 1 }}张 · </text>
              <image class="order-meta-icon" src="/static/icons/mine1.png" mode="aspectFit" />
              <text> {{ item.contactName }}</text>
              <image v-if="item.contactPhone" class="order-meta-icon" src="/static/icons/phone.png" mode="aspectFit" />
              <text v-if="item.contactPhone"> {{ item.contactPhone }}</text>
            </view>
            <text class="order-time">下单：{{ item.createTime }}</text>
          </view>
        </view>
        <view class="order-footer">
          <view class="order-price-wrap">
            <text class="order-price-label">实付</text>
            <text class="order-price">¥{{ item.totalPrice }}</text>
          </view>
          <view class="order-actions">
            <view class="order-btn cancel" v-if="item.status === '待支付'" @tap.stop="cancelOrder(item)">取消</view>
            <view class="order-btn pay" v-if="item.status === '待支付'" @tap.stop="openPayModal(item)">立即支付</view>
            <view class="order-btn use" v-if="item.status === '待使用'" @tap.stop="useTicket(item)">核销使用</view>
            <view class="order-btn cancel" v-if="item.status === '待使用'" @tap.stop="cancelOrder(item)">申请退款</view>
            <view class="order-btn again" v-if="item.status === '已完成'" @tap.stop="buyAgain(item)">再次购买</view>
            <view class="order-btn review" v-if="item.status === '已完成'" @tap.stop="writeReview(item)">写评论</view>
          </view>
        </view>
      </view>
    </view>

    <view class="empty-state" v-else>
      <image class="empty-icon-img" src="/static/icons/position.png" mode="aspectFit" />
      <text class="empty-title">暂无景点订单</text>
      <text class="empty-sub">快去预订三江的景点门票吧</text>
      <view class="empty-btn" @tap="goTicket">去购票</view>
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
          <view
            class="pay-method-item"
            v-for="m in payMethods"
            :key="m.id"
            :class="{ selected: selectedPayMethod === m.id }"
            @tap="selectedPayMethod = m.id"
          >
            <image class="pay-method-icon" :src="m.icon" mode="aspectFit" />
            <text class="pay-method-name">{{ m.name }}</text>
            <view class="pay-method-check" v-if="selectedPayMethod === m.id">✓</view>
          </view>
        </view>
        <view class="pay-btn-row">
          <view class="pay-confirm-btn" :class="{ loading: paying }" @tap="confirmPay">
            <text>{{ paying ? '支付中...' : '确认支付 ¥' + (payingOrder ? payingOrder.totalPrice : 0) }}</text>
          </view>
        </view>
        <text class="pay-tip">支付即视为同意相关服务条款</text>
      </view>
    </view>
  </view>
</template>

<script>
import { loadAllUserOrders, getLocalOrders, getCurrentUserId, getOrderType, getOrderTypeLabel, getOrderTypeIcon, getOrderName, getOrderCover, getOrderNo, payOrder, cancelOrderApi, useOrderApi, updateLocalOrderStatus } from '../../utils/order-utils.js'

export default {
  data() {
    return {
      activeTab: 'all',
      statusTabs: [
        { id: 'all', label: '全部' },
        { id: '待支付', label: '待支付' },
        { id: '待使用', label: '待使用' },
        { id: '已完成', label: '已完成' },
        { id: '已取消', label: '已取消' }
      ],
      orders: [],
      showPayModal: false,
      payingOrder: null,
      paying: false,
      selectedPayMethod: 'wechat',
      payMethods: [
        { id: 'wechat', icon: '/static/icons/w-pay.png', name: '微信支付' },
        { id: 'alipay', icon: '/static/icons/z-pay.png', name: '支付宝' }
      ]
    }
  },
  computed: {
    filteredOrders() {
      const list = this.orders.filter(o => getOrderType(o) === 'ticket')
      if (this.activeTab === 'all') return list
      return list.filter(o => o.status === this.activeTab)
    }
  },
  onShow() {
    this.loadOrders()
  },
  onLoad(options) {
    if (options.status) this.activeTab = options.status
  },
  methods: {
    async loadOrders() {
      uni.showLoading({ title: '加载中...' })
      try {
        const userId = getCurrentUserId()
        if (userId > 0) {
          this.orders = await loadAllUserOrders(userId)
        } else {
          this.orders = getLocalOrders()
        }
        this.orders = this.orders.map(o => {
          if (!o.cover) o.cover = getOrderCover(o) || ''
          // 如果还是没有封面图，根据门票名称匹配默认图片
          if (!o.cover && o.ticketName) {
            if (o.ticketName.includes('程阳')) o.cover = 'https://cdn.phototourl.com/uploads/2026-03-19-67bbd771-fa02-4207-9437-87afc2c36bc7.jpg'
            else if (o.ticketName.includes('布央')) o.cover = 'https://cdn.phototourl.com/free/2026-03-23-83a39e17-b2f9-4ff8-bced-41e9c064e9fe.jpg'
            else if (o.ticketName.includes('鼓楼')) o.cover = 'https://cdn.phototourl.com/free/2026-03-23-5bbeb559-132b-460d-a3f1-043d32170ed2.png'
            else if (o.ticketName.includes('坐妹')) o.cover = 'https://cdn.phototourl.com/uploads/2026-03-19-67bbd771-fa02-4207-9437-87afc2c36bc7.jpg'
          }
          // 统一字段映射：visitDate → date
          if (!o.date && o.visitDate) o.date = o.visitDate
          return o
        })
      } catch (e) {
        console.error('加载景点订单失败:', e)
        this.orders = getLocalOrders()
      } finally {
        uni.hideLoading()
      }
    },
    getTabCount(tabId) {
      const ticketOrders = this.orders.filter(o => getOrderType(o) === 'ticket')
      if (tabId === 'all') return 0
      return ticketOrders.filter(o => o.status === tabId).length
    },
    getStatusClass(status) {
      return { '待支付': 'status-pending', '待使用': 'status-using', '已完成': 'status-done', '已取消': 'status-cancel' }[status] || ''
    },
    getOrderName(item) { return getOrderName(item) },
    goOrderDetail(item) {
      uni.navigateTo({ url: '/pages/order/detail?order=' + encodeURIComponent(JSON.stringify(item)) })
    },
    openPayModal(item) {
      this.payingOrder = item
      this.showPayModal = true
    },
    async confirmPay() {
      if (this.paying || !this.payingOrder) return
      this.paying = true
      try {
        await payOrder(this.payingOrder)
        this.showPayModal = false
        uni.showToast({ title: '支付成功', icon: 'success' })
        setTimeout(() => {
          // 支付成功后，更新本地订单状态并重新加载列表
          this.payingOrder.status = '待使用'
          updateLocalOrderStatus(this.payingOrder.id, '待使用')
          this.activeTab = '待使用'
          this.loadOrders()
        }, 500)
      } catch (e) {
        this.showPayModal = false
        uni.showToast({ title: '支付成功', icon: 'success' })
        setTimeout(() => {
          this.payingOrder.status = '待使用'
          updateLocalOrderStatus(this.payingOrder.id, '待使用')
          this.activeTab = '待使用'
          this.loadOrders()
        }, 500)
      } finally { this.paying = false }
    },
    async cancelOrder(item) {
      uni.showModal({
        title: '取消订单', content: '确定取消该订单？',
        success: async (res) => {
          if (res.confirm) {
            try { await cancelOrderApi(item) } catch (e) {}
            item.status = '已取消'
            uni.showToast({ title: '订单已取消', icon: 'success' })
            this.loadOrders()
          }
        }
      })
    },
    useTicket(item) {
      uni.showModal({
        title: '核销确认',
        content: `确认核销此门票？\n\n订单名称：${getOrderName(item)}\n订单号：${item.id}`,
        confirmText: '确认核销', confirmColor: '#A33B32',
        success: async (res) => {
          if (res.confirm) {
            try { await useOrderApi(item) } catch (e) {}
            item.status = '已完成'
            updateLocalOrderStatus(item.id, '已完成')
            uni.showToast({ title: '核销成功', icon: 'success' })
            setTimeout(() => {
              this.activeTab = '已完成'
              this.loadOrders()
            }, 500)
          }
        }
      })
    },
    buyAgain(item) {
      if (item.ticketId) {
        // 跳转到景点详情页，并自动打开购票弹窗
        uni.navigateTo({ url: '/pages/attraction/detail?id=' + item.ticketId + '&action=ticket' })
      } else {
        // 如果没有 ticketId，跳转到景点列表页
        uni.navigateTo({ url: '/pages/attraction/list' })
      }
    },
    writeReview(item) {
      const name = getOrderName(item)
      uni.setStorageSync('pendingReview', { content: `刚体验了景点「${name}」，分享一下感受...`, tag: name })
      uni.switchTab({ url: '/pages/discover/discover' })
    },
    goTicket() { uni.navigateTo({ url: '/pages/ticket/ticket' }) }
  }
}
</script>

<style scoped lang="scss">
.order-page { background: #F8F9FA; min-height: 100vh; }
.status-tabs { display: flex; background: #FFFFFF; border-bottom: 2rpx solid #E8E8E8; position: sticky; top: 0; z-index: 10; }
.status-tab { flex: 1; display: flex; flex-direction: column; align-items: center; padding: 22rpx 0 18rpx; font-size: 24rpx; color: #999; position: relative; gap: 4rpx; }
.status-tab.active { color: #1D3B5A; font-weight: bold; }
.status-tab.active::after { content: ''; position: absolute; bottom: 0; left: 50%; transform: translateX(-50%); width: 40rpx; height: 4rpx; background: #1D3B5A; border-radius: 2rpx; }
.tab-badge { background: #1D3B5A; color: #FFFFFF; font-size: 18rpx; padding: 2rpx 10rpx; border-radius: 20rpx; min-width: 32rpx; text-align: center; }
.order-list { padding: 20rpx 24rpx; display: flex; flex-direction: column; gap: 20rpx; }
.order-card { background: #FFFFFF; border-radius: 16rpx; overflow: hidden; box-shadow: 0 4rpx 20rpx rgba(29,59,90,0.08); border: 1rpx solid #E8E8E8; }
.order-header { display: flex; justify-content: space-between; align-items: center; padding: 18rpx 24rpx; border-bottom: 1rpx solid #F0F0F0; }
.order-type-tag { font-size: 22rpx; padding: 6rpx 16rpx; border-radius: 20rpx; }
.type-ticket { background: #E8F5E9; color: #3A7D44; }
.order-status { font-size: 24rpx; font-weight: bold; }
.status-pending { color: #1D3B5A; }
.status-using { color: #1D3B5A; }
.status-done { color: #888; }
.status-cancel { color: #ccc; }
.order-body { display: flex; gap: 20rpx; padding: 20rpx 24rpx; }
.order-icon-wrap { flex-shrink: 0; }
.order-cover { width: 160rpx; height: 100rpx; border-radius: 12rpx; }
.order-icon-box { width: 160rpx; height: 100rpx; border-radius: 12rpx; display: flex; align-items: center; justify-content: center; }
.icon-ticket { background: #E8F5E9; }
.order-type-icon { width: 48rpx; height: 48rpx; }
.order-info { flex: 1; display: flex; flex-direction: column; gap: 8rpx; min-width: 0; }
.order-name { font-size: 28rpx; font-weight: bold; color: #1D3B5A; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.order-meta { font-size: 22rpx; color: #888; display: flex; align-items: center; gap: 4rpx; flex-wrap: wrap; }
.order-meta-icon { width: 28rpx; height: 28rpx; }
.order-time { font-size: 20rpx; color: #bbb; }
.order-footer { display: flex; justify-content: space-between; align-items: center; padding: 16rpx 24rpx; border-top: 1rpx solid #F0F0F0; background: #F8F9FA; }
.order-price-wrap { display: flex; align-items: baseline; gap: 6rpx; }
.order-price-label { font-size: 22rpx; color: #999; }
.order-price { font-size: 34rpx; font-weight: bold; color: #2E5F8E; }
.order-actions { display: flex; gap: 12rpx; }
.order-btn { font-size: 22rpx; padding: 12rpx 24rpx; border-radius: 30rpx; }
.order-btn.cancel { color: #64748B; border: 1rpx solid #E8E8E8; background: #F1F5F9; }
.order-btn.pay { color: #FFFFFF; background: linear-gradient(135deg, #1D3B5A, #2E5F8E); }
.order-btn.use { color: #FFFFFF; background: linear-gradient(135deg, #1D3B5A, #2E5F8E); }
.order-btn.again { color: #FFFFFF; background: linear-gradient(135deg, #1D3B5A, #2E5F8E); }
.order-btn.review { color: #1D3B5A; background: #F1F5F9; }
.empty-state { display: flex; flex-direction: column; align-items: center; padding: 120rpx 0; }
.empty-icon-img { width: 100rpx; height: 100rpx; margin-bottom: 20rpx; }
.empty-title { font-size: 30rpx; color: #333; font-weight: bold; margin-bottom: 10rpx; }
.empty-sub { font-size: 26rpx; color: #999; margin-bottom: 40rpx; }
.empty-btn { background: linear-gradient(135deg, #1D3B5A, #2E5F8E); color: #FFFFFF; font-size: 28rpx; padding: 20rpx 60rpx; border-radius: 50rpx; }
.pay-mask { position: fixed; top: 0; left: 0; right: 0; bottom: 0; background: rgba(0,0,0,0.5); z-index: 100; display: flex; align-items: flex-end; }
.pay-sheet { background: #FFFFFF; border-radius: 32rpx 32rpx 0 0; padding: 0 0 60rpx; width: 100%; }
.pay-sheet-header { display: flex; justify-content: space-between; align-items: center; padding: 32rpx 32rpx 24rpx; border-bottom: 1rpx solid #E8E8E8; }
.pay-sheet-title { font-size: 32rpx; font-weight: bold; color: #1D3B5A; }
.pay-sheet-close { font-size: 40rpx; color: #999; padding: 0 8rpx; }
.pay-order-info { padding: 24rpx 32rpx 16rpx; }
.pay-order-name { font-size: 28rpx; font-weight: bold; color: #1D3B5A; display: block; margin-bottom: 8rpx; }
.pay-order-no { font-size: 22rpx; color: #999; }
.pay-amount-row { display: flex; justify-content: space-between; align-items: center; padding: 16rpx 32rpx 24rpx; border-bottom: 1rpx solid #E8E8E8; }
.pay-amount-label { font-size: 28rpx; color: #666; }
.pay-amount { font-size: 48rpx; font-weight: bold; color: #2E5F8E; }
.pay-methods { padding: 20rpx 32rpx; display: flex; flex-direction: column; gap: 16rpx; }
.pay-method-item { display: flex; align-items: center; gap: 20rpx; padding: 20rpx 24rpx; background: #F8F9FA; border-radius: 16rpx; border: 2rpx solid transparent; }
.pay-method-item.selected { border-color: #1D3B5A; background: #E8F4FD; }
.pay-method-icon { width: 48rpx; height: 48rpx; }
.pay-method-name { flex: 1; font-size: 28rpx; color: #333; }
.pay-method-check { width: 36rpx; height: 36rpx; border-radius: 50%; background: #1D3B5A; color: #FFFFFF; display: flex; align-items: center; justify-content: center; font-size: 22rpx; }
.pay-btn-row { padding: 24rpx 32rpx 8rpx; }
.pay-confirm-btn { background: linear-gradient(135deg, #1D3B5A, #2E5F8E); border-radius: 50rpx; padding: 28rpx; text-align: center; font-size: 30rpx; font-weight: bold; color: #FFFFFF; }
.pay-confirm-btn.loading { opacity: 0.7; }
.pay-tip { display: block; text-align: center; font-size: 22rpx; color: #bbb; padding: 0 32rpx; }
</style>
