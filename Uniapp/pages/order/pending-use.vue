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
    <view class="order-list" v-if="filteredOrders.length > 0">
      <view class="order-card" v-for="item in filteredOrders" :key="item.id">
        <view class="order-header">
          <view class="order-type-tag" :class="'type-' + getOrderType(item)">{{ getOrderTypeLabel(item) }}</view>
          <text class="order-status status-using">待使用</text>
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
            <view class="order-btn use" @tap.stop="useOrder(item)">核销使用</view>
            <view class="order-btn cancel" @tap.stop="cancelOrder(item)">申请退款</view>
          </view>
        </view>
      </view>
    </view>

    <!-- 空状态 -->
    <view class="empty-state" v-else>
      <image class="empty-icon-img" src="/static/icons/order.png" mode="aspectFit" />
      <text class="empty-title">暂无待使用订单</text>
      <text class="empty-sub">快去预订三江的精彩体验吧</text>
      <view class="empty-btn" @tap="goHome">去预订</view>
    </view>
  </view>
</template>

<script>
import { loadAllUserOrders, getLocalOrders, getCurrentUserId, getOrderType, getOrderTypeLabel, getOrderTypeIcon, getOrderName, getOrderCover, getOrderNo, getOrderApi, useOrderApi, cancelOrderApi } from '../../utils/order-utils.js'

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
      allOrders: []
    }
  },
  computed: {
    filteredOrders() {
      let list = this.allOrders.filter(o => o.status === '待使用' || o.status === '已入住')
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
        console.error('加载待使用订单失败:', e)
        this.allOrders = getLocalOrders()
      } finally {
        uni.hideLoading()
      }
    },
    getCategoryCount(catId) {
      if (catId === 'all') return this.allOrders.filter(o => o.status === '待使用' || o.status === '已入住').length
      return this.allOrders.filter(o => (o.status === '待使用' || o.status === '已入住') && getOrderType(o) === catId).length
    },
    getOrderType(item) { return getOrderType(item) },
    getOrderTypeLabel(item) { return getOrderTypeLabel(item) },
    getOrderTypeIcon(item) { return getOrderTypeIcon(item) },
    getOrderName(item) { return getOrderName(item) },
    goOrderDetail(item) {
      uni.navigateTo({ url: '/pages/order/detail?order=' + encodeURIComponent(JSON.stringify(item)) })
    },
    goHome() { uni.switchTab({ url: '/pages/index/index' }) },
    useOrder(item) {
      uni.showModal({
        title: '核销确认', content: '确定要核销使用此订单吗？核销后无法撤销。',
        success: async (res) => {
          if (res.confirm) {
            try { await useOrderApi(item) } catch (e) {}
            item.status = '已完成'
            uni.showToast({ title: '核销成功', icon: 'success' })
            setTimeout(() => {
              uni.navigateTo({ url: '/pages/order/completed-order' })
            }, 1000)
          }
        }
      })
    },
    cancelOrder(item) {
      uni.showModal({
        title: '确认退款', content: '确定要申请退款吗？',
        success: async (res) => {
          if (res.confirm) {
            try { await cancelOrderApi(item) } catch (e) {}
            item.status = '已取消'
            uni.showToast({ title: '已申请退款', icon: 'success' })
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
.status-using { color: #1D3B5A; }

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
.order-btn.use { background: linear-gradient(135deg, #1D3B5A, #2E5F8E); color: #FFFFFF; }

.empty-state { display: flex; flex-direction: column; align-items: center; justify-content: center; padding: 120rpx 40rpx; }
.empty-icon-img { width: 160rpx; height: 160rpx; margin-bottom: 30rpx; opacity: 0.4; }
.empty-title { font-size: 30rpx; font-weight: 600; color: #1D3B5A; margin-bottom: 12rpx; }
.empty-sub { font-size: 24rpx; color: #999; margin-bottom: 40rpx; }
.empty-btn { background: linear-gradient(135deg, #1D3B5A, #2E5F8E); color: #FFFFFF; font-size: 28rpx; padding: 18rpx 60rpx; border-radius: 40rpx; font-weight: 500; }
</style>
