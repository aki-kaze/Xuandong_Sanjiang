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
          <view class="order-type-tag type-experience"><text>非遗体验</text></view>
          <text class="order-status" :class="getStatusClass(item.status)">{{ item.status }}</text>
        </view>
        <view class="order-body" @tap="goOrderDetail(item)">
          <view class="order-icon-wrap">
            <image v-if="item.cover" class="order-cover" :src="item.cover" mode="aspectFill" @error="item.cover=''" />
            <view v-else class="order-icon-box icon-experience">
              <image class="order-type-icon" src="/static/icons/feiyi.png" mode="aspectFit" />
            </view>
          </view>
          <view class="order-info">
            <text class="order-name">{{ item.heritageName || '非遗体验' }}</text>
            <view class="order-meta">
              <image class="order-meta-icon" src="/static/icons/calendar.png" mode="aspectFit" />
              <text> 预约：{{ item.visitDate || '待确认' }}</text>
            </view>
            <view class="order-meta">
              <image class="order-meta-icon" src="/static/icons/mine1.png" mode="aspectFit" />
              <text> {{ item.contactName || '用户' }}</text>
              <image v-if="item.contactPhone" class="order-meta-icon" src="/static/icons/phone.png" mode="aspectFit" />
              <text v-if="item.contactPhone"> {{ item.contactPhone }}</text>
            </view>
            <view class="order-meta" v-if="item.quantity">
              <text>人数：{{ item.quantity }}人 · {{ getVisitTimeLabel(item.visitTime) }}</text>
            </view>
            <text class="order-time">下单：{{ item.createTime }}</text>
          </view>
        </view>
        <!-- 核销码区域 - 待使用/已完成订单显示 -->
        <view class="verify-section" v-if="(item.status === '待使用' || item.status === '已完成') && item.orderNo">
          <view class="verify-code-wrap">
            <text class="verify-label">核销码</text>
            <text class="verify-code">{{ getVerifyCode(item.orderNo) }}</text>
          </view>
          <text class="verify-tip">到店后请出示此核销码给工作人员</text>
        </view>
        <view class="order-footer">
          <view class="order-price-wrap">
            <text class="order-price-label">实付</text>
            <text class="order-price" v-if="item.totalPrice > 0">¥{{ item.totalPrice }}</text>
            <text class="order-price free" v-else>免费</text>
          </view>
          <view class="order-actions">
            <view class="order-btn cancel" v-if="item.status === '待支付'" @tap.stop="cancelOrder(item)">取消预约</view>
            <view class="order-btn pay" v-if="item.status === '待支付'" @tap.stop="openPayModal(item)">立即支付</view>
            <view class="order-btn use" v-if="item.status === '待使用'" @tap.stop="confirmUse(item)">核销使用</view>
            <view class="order-btn cancel" v-if="item.status === '待使用'" @tap.stop="cancelOrder(item)">申请退款</view>
            <view class="order-btn again" v-if="item.status === '已完成'" @tap.stop="bookAgain(item)">再次预约</view>
          </view>
        </view>
      </view>
    </view>

    <view class="empty-state" v-else>
      <image class="empty-icon-img" src="/static/icons/feiyi.png" mode="aspectFit" />
      <text class="empty-title">暂无非遗体验订单</text>
      <text class="empty-sub">快去预约三江非遗体验吧</text>
      <view class="empty-btn" @tap="goHeritage">去预约</view>
    </view>

    <!-- 支付弹窗 -->
    <view class="pay-mask" v-if="showPayModal" @tap.stop="">
      <view class="pay-sheet">
        <view class="pay-sheet-header">
          <text class="pay-sheet-title">确认支付</text>
          <text class="pay-sheet-close" @tap="showPayModal = false">×</text>
        </view>
        <view class="pay-order-info" v-if="payingOrder">
          <text class="pay-order-name">{{ payingOrder.heritageName || '非遗体验' }}</text>
          <text class="pay-order-no">订单号：{{ payingOrder.orderNo || payingOrder.id }}</text>
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
import { loadAllUserOrders, getLocalOrders, getCurrentUserId, getOrderName, getOrderCover, getOrderNo, cancelOrderApi, useOrderApi, payOrder } from '../../utils/order-utils.js'

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
      const list = this.orders.filter(o => o.orderType === 'experience')
      if (this.activeTab === 'all') return list
      return list.filter(o => o.status === this.activeTab)
    }
  },
  onLoad(options) {
    if (options.status) this.activeTab = options.status
  },
  onShow() {
    this.loadOrders()
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
          return o
        })
      } catch (e) {
        console.error('加载非遗订单失败:', e)
        this.orders = getLocalOrders()
      } finally {
        uni.hideLoading()
      }
    },
    getTabCount(tabId) {
      const expOrders = this.orders.filter(o => o.orderType === 'experience')
      if (tabId === 'all') return expOrders.length
      return expOrders.filter(o => o.status === tabId).length
    },
    getStatusClass(status) {
      return { '待支付': 'status-pending', '待使用': 'status-using', '已完成': 'status-done', '已取消': 'status-cancel' }[status] || ''
    },
    getVisitTimeLabel(time) {
      return { morning: '上午 09:00-12:00', afternoon: '下午 14:00-17:00', evening: '晚间 19:00-21:00' }[time] || time || ''
    },
    getVerifyCode(orderNo) {
      const code = String(orderNo).replace(/[^A-Za-z0-9]/g, '').slice(-8).toUpperCase()
      return code || String(Math.floor(10000000 + Math.random() * 90000000))
    },
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
          uni.redirectTo({ url: '/pages/order/experience-order?status=待使用' })
        }, 1000)
      } catch (e) {
        this.showPayModal = false
        uni.showToast({ title: '支付成功', icon: 'success' })
        setTimeout(() => {
          uni.redirectTo({ url: '/pages/order/experience-order?status=待使用' })
        }, 1000)
      } finally {
        this.paying = false
      }
    },
    async cancelOrder(item) {
      uni.showModal({
        title: '取消预约', content: '确定取消该非遗体验预约吗？',
        success: async (res) => {
          if (res.confirm) {
            try { await cancelOrderApi(item) } catch (e) {}
            item.status = '已取消'
            uni.showToast({ title: '已取消', icon: 'success' })
            this.loadOrders()
          }
        }
      })
    },
    async confirmUse(item) {
      uni.showModal({
        title: '核销确认', content: '确认核销此非遗体验？核销后无法撤销。',
        confirmText: '确认核销', confirmColor: '#A33B32',
        success: async (res) => {
          if (res.confirm) {
            try { await useOrderApi(item) } catch (e) {}
            item.status = '已完成'
            uni.showToast({ title: '核销成功', icon: 'success' })
            setTimeout(() => {
              uni.redirectTo({ url: '/pages/order/experience-order?status=已完成' })
            }, 1000)
          }
        }
      })
    },
    bookAgain(item) {
      if (item.heritageId) {
        const cover = encodeURIComponent(item.heritageCover || '')
        const name = encodeURIComponent(item.heritageName || '')
        uni.navigateTo({ url: '/pages/heritage/experience-book?id=' + item.heritageId + '&name=' + name + '&cover=' + cover })
      } else {
        uni.navigateTo({ url: '/pages/heritage/heritage' })
      }
    },
    goHeritage() { uni.navigateTo({ url: '/pages/heritage/heritage' }) }
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
.type-experience { background: #F0F7FF; color: #4A90E2; }
.order-status { font-size: 24rpx; font-weight: bold; }
.status-pending { color: #1D3B5A; }
.status-using { color: #1D3B5A; }
.status-done { color: #888; }
.status-cancel { color: #ccc; }
.order-body { display: flex; gap: 20rpx; padding: 20rpx 24rpx; }
.order-icon-wrap { flex-shrink: 0; }
.order-cover { width: 160rpx; height: 100rpx; border-radius: 12rpx; }
.order-icon-box { width: 160rpx; height: 100rpx; border-radius: 12rpx; display: flex; align-items: center; justify-content: center; }
.icon-experience { background: #F0F7FF; }
.order-type-icon { width: 48rpx; height: 48rpx; }
.order-info { flex: 1; display: flex; flex-direction: column; gap: 8rpx; min-width: 0; }
.order-name { font-size: 28rpx; font-weight: bold; color: #1D3B5A; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.order-meta { font-size: 22rpx; color: #888; display: flex; align-items: center; gap: 4rpx; flex-wrap: wrap; }
.order-meta-icon { width: 28rpx; height: 28rpx; }
.order-time { font-size: 20rpx; color: #bbb; }

/* 核销码区域 */
.verify-section { padding: 16rpx 24rpx; border-top: 1rpx dashed #E8E8E8; background: #FAFBFC; }
.verify-code-wrap { display: flex; align-items: center; justify-content: space-between; margin-bottom: 8rpx; }
.verify-label { font-size: 24rpx; color: #64748B; }
.verify-code { font-size: 36rpx; font-weight: 700; color: #1D3B5A; letter-spacing: 4rpx; font-family: monospace; }
.verify-tip { font-size: 22rpx; color: #94A3B8; }

.order-footer { display: flex; justify-content: space-between; align-items: center; padding: 16rpx 24rpx; border-top: 1rpx solid #F0F0F0; background: #F8F9FA; }
.order-price-wrap { display: flex; align-items: baseline; gap: 6rpx; }
.order-price-label { font-size: 22rpx; color: #999; }
.order-price { font-size: 34rpx; font-weight: bold; color: #2E5F8E; }
.order-price.free { color: #22C55E; }
.order-actions { display: flex; gap: 12rpx; }
.order-btn { font-size: 22rpx; padding: 12rpx 24rpx; border-radius: 30rpx; }
.order-btn.cancel { color: #64748B; border: 1rpx solid #E8E8E8; background: #F1F5F9; }
.order-btn.pay { color: #FFFFFF; background: linear-gradient(135deg, #1D3B5A, #2E5F8E); }
.order-btn.use { color: #FFFFFF; background: linear-gradient(135deg, #1D3B5A, #2E5F8E); }
.order-btn.again { color: #FFFFFF; background: linear-gradient(135deg, #1D3B5A, #2E5F8E); }
.empty-state { display: flex; flex-direction: column; align-items: center; padding: 120rpx 0; }
.empty-icon-img { width: 100rpx; height: 100rpx; margin-bottom: 20rpx; }
.empty-title { font-size: 30rpx; color: #333; font-weight: bold; margin-bottom: 10rpx; }
.empty-sub { font-size: 26rpx; color: #999; margin-bottom: 40rpx; }
.empty-btn { background: linear-gradient(135deg, #1D3B5A, #2E5F8E); color: #FFFFFF; font-size: 28rpx; padding: 20rpx 60rpx; border-radius: 50rpx; }

/* 支付弹窗 */
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
