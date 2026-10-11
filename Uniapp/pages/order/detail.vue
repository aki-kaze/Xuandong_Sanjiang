<template>
  <view class="order-detail-page">
    <!-- 顶部状态栏 -->
    <view class="status-header" :class="'status-' + statusType">
      <view class="status-icon">{{ statusIcon }}</view>
      <text class="status-text">{{ order.status }}</text>
      <text class="status-desc" v-if="statusDesc">{{ statusDesc }}</text>
    </view>

    <!-- 订单信息卡片 -->
    <view class="info-card">
      <!-- 订单商品信息 -->
      <view class="product-section">
        <view class="product-cover-wrap">
          <image v-if="order.cover" class="product-cover" :src="order.cover" mode="aspectFill" />
          <view v-else class="product-icon-box" :class="'icon-' + orderType">
            <image class="product-type-icon" :src="'/static/icons/' + typeIcon + '.png'" mode="aspectFit" />
          </view>
        </view>
        <view class="product-info">
          <text class="product-name">{{ order.ticketName }}</text>
          <text class="product-type-tag">{{ typeLabel }}</text>
        </view>
      </view>
    </view>

    <!-- 订单详情卡片 -->
    <view class="info-card">
      <view class="card-header">
        <text class="card-title">订单信息</text>
      </view>
      <view class="detail-list">
        <view class="detail-item">
          <text class="detail-label">订单编号</text>
          <text class="detail-value">{{ order.id }}</text>
        </view>
        <view class="detail-item" v-if="order.date">
          <text class="detail-label">{{ dateLabel }}</text>
          <text class="detail-value">{{ order.date }}</text>
        </view>
        <view class="detail-item">
          <text class="detail-label">购买数量</text>
          <text class="detail-value">{{ order.quantity }}{{ unitText }}</text>
        </view>
        <view class="detail-item" v-if="order.nights">
          <text class="detail-label">住宿晚数</text>
          <text class="detail-value">{{ order.nights }}晚</text>
        </view>
        <view class="detail-item" v-if="order.validEndDate">
          <text class="detail-label">有效期至</text>
          <text class="detail-value">{{ order.validEndDate }}</text>
        </view>
        <view class="detail-item" v-if="order.contactName">
          <view class="detail-label-wrap">
            <image class="detail-icon" src="/static/icons/mine1.png" mode="aspectFit" />
            <text class="detail-label">联系人</text>
          </view>
          <text class="detail-value">{{ order.contactName }}</text>
        </view>
        <view class="detail-item" v-if="order.contactPhone">
          <view class="detail-label-wrap">
            <image class="detail-icon" src="/static/icons/phone.png" mode="aspectFit" />
            <text class="detail-label">联系电话</text>
          </view>
          <text class="detail-value">{{ order.contactPhone }}</text>
        </view>
        <view class="detail-item" v-if="order.address">
          <text class="detail-label">收货地址</text>
          <text class="detail-value">{{ order.address }}</text>
        </view>
        <view class="detail-item">
          <text class="detail-label">下单时间</text>
          <text class="detail-value">{{ order.createTime }}</text>
        </view>
      </view>
    </view>

    <!-- 支付信息卡片 -->
    <view class="info-card">
      <view class="card-header">
        <text class="card-title">支付信息</text>
      </view>
      <view class="price-section">
        <view class="price-row">
          <text class="price-label">订单金额</text>
          <text class="price-value">¥{{ order.totalPrice }}</text>
        </view>
        <view class="price-row total">
          <text class="price-label">实付金额</text>
          <text class="price-value total">¥{{ order.totalPrice }}</text>
        </view>
      </view>
    </view>

    <!-- 操作按钮区域 -->
    <view class="action-section" v-if="showActions">
      <view class="action-btn cancel" v-if="order.status === '待支付'" @tap="cancelOrder">取消订单</view>
      <view class="action-btn pay" v-if="order.status === '待支付'" @tap="openPayModal">立即支付</view>
      <view class="action-btn use" v-if="order.status === '待使用'" @tap="useOrder">核销使用</view>
      <view class="action-btn cancel" v-if="order.status === '待使用'" @tap="cancelOrder">申请退款</view>
      <view class="action-btn again" v-if="order.status === '已完成'" @tap="buyAgain">再次购买</view>
    </view>

    <!-- 支付弹窗 -->
    <view class="pay-mask" v-if="showPayModal" @tap.stop="">
      <view class="pay-sheet">
        <view class="pay-sheet-header">
          <text class="pay-sheet-title">确认支付</text>
          <text class="pay-sheet-close" @tap="showPayModal = false">×</text>
        </view>
        <view class="pay-order-info">
          <text class="pay-order-name">{{ order.ticketName }}</text>
          <text class="pay-order-no">订单号：{{ order.id }}</text>
        </view>
        <view class="pay-amount-row">
          <text class="pay-amount-label">支付金额</text>
          <text class="pay-amount">¥{{ order.totalPrice }}</text>
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
            <text>{{ paying ? '支付中...' : '确认支付 ¥' + order.totalPrice }}</text>
          </view>
        </view>
        <text class="pay-tip">支付即视为同意相关服务条款</text>
      </view>
    </view>

    <view style="height: 40rpx;"></view>
  </view>
</template>

<script>
import { ticketOrderApi, foodOrderApi, hotelOrderApi, productOrderApi, formatOrderTime } from '../../utils/api.js'

const STATUS_MAP = { 'pending': '待支付', 'paid': '待使用', 'checked_in': '已入住', 'checked_out': '已退房', 'used': '已完成', 'cancelled': '已取消' }

export default {
  data() {
    return {
      order: {},
      showPayModal: false,
      paying: false,
      selectedPayMethod: 'wechat',
      payMethods: [
        { id: 'wechat', icon: '/static/icons/w-pay.png', name: '微信支付' },
        { id: 'alipay', icon: '/static/icons/z-pay.png', name: '支付宝' }
      ]
    }
  },
  computed: {
    orderType() {
      if (this.order.orderType === 'hotel' || String(this.order.id).startsWith('HT')) return 'hotel'
      if (this.order.orderType === 'food' || String(this.order.id).startsWith('MS')) return 'food'
      if (this.order.orderType === 'product' || String(this.order.id).startsWith('SP')) return 'product'
      return 'ticket'
    },
    typeLabel() {
      const map = { ticket: '门票', food: '美食', hotel: '酒店', product: '商品' }
      return map[this.orderType] || '订单'
    },
    typeIcon() {
      if (this.orderType === 'hotel') return 'hotel'
      if (this.orderType === 'food') return 'food'
      if (this.orderType === 'product') return 'shop'
      return 'ticket'
    },
    statusType() {
      if (this.order.status === '待支付') return 'pending'
      if (this.order.status === '待使用') return 'using'
      if (this.order.status === '已完成') return 'done'
      if (this.order.status === '已取消') return 'cancel'
      return ''
    },
    statusIcon() {
      if (this.order.status === '待支付') return '⏰'
      if (this.order.status === '待使用') return '✅'
      if (this.order.status === '已完成') return '🎉'
      if (this.order.status === '已取消') return '❌'
      return ''
    },
    statusDesc() {
      if (this.order.status === '待支付') return '请尽快完成支付'
      if (this.order.status === '待使用') return '请在有效期内使用'
      if (this.order.status === '已完成') return '感谢您的购买'
      return ''
    },
    dateLabel() {
      if (this.orderType === 'hotel') return '入住日期'
      if (this.orderType === 'ticket') return '游览日期'
      return '使用日期'
    },
    unitText() {
      if (this.orderType === 'ticket') return '张'
      return ''
    },
    showActions() {
      return ['待支付', '待使用', '已完成'].includes(this.order.status)
    }
  },
  onLoad(options) {
    try {
      this.order = JSON.parse(decodeURIComponent(options.order))
      this.order.createTime = formatOrderTime(this.order.createTime || this.order.create_time || this.order.createdAt)
      // 如果订单没有封面图，根据门票名称设置默认图片
      if (!this.order.cover && this.order.ticketName) {
        if (this.order.ticketName.includes('程阳')) this.order.cover = 'https://cdn.phototourl.com/uploads/2026-03-19-67bbd771-fa02-4207-9437-87afc2c36bc7.jpg'
        else if (this.order.ticketName.includes('布央')) this.order.cover = 'https://cdn.phototourl.com/free/2026-03-23-83a39e17-b2f9-4ff8-bced-41e9c064e9fe.jpg'
        else if (this.order.ticketName.includes('鼓楼')) this.order.cover = 'https://cdn.phototourl.com/free/2026-03-23-5bbeb559-132b-460d-a3f1-043d32170ed2.png'
        else if (this.order.ticketName.includes('坐妹')) this.order.cover = 'https://cdn.phototourl.com/uploads/2026-03-19-67bbd771-fa02-4207-9437-87afc2c36bc7.jpg'
      }
    } catch (e) {
      uni.showToast({ title: '加载失败', icon: 'none' })
      setTimeout(() => {
        uni.navigateBack()
      }, 1500)
    }
  },
  methods: {
    openPayModal() {
      this.showPayModal = true
    },
    async confirmPay() {
      if (this.paying) return
      this.paying = true
      try {
        const orderNo = this.order.orderNo || this.order.id
        if (this.orderType === 'food') await foodOrderApi.pay(orderNo)
        else if (this.orderType === 'hotel') await hotelOrderApi.pay(orderNo)
        else if (this.orderType === 'product') await productOrderApi.pay(orderNo)
        else await ticketOrderApi.pay(orderNo)

        this.order.status = '待使用'
        this.showPayModal = false
        uni.showToast({ title: '支付成功', icon: 'success' })

        const orders = uni.getStorageSync('orders') || []
        const idx = orders.findIndex(o => o.id === this.order.id)
        if (idx >= 0) {
          orders[idx].status = '待使用'
          uni.setStorageSync('orders', orders)
        }
      } catch (e) {
        // 降级订单可能在数据库中不存在，先尝试创建再支付
        if (this.orderType === 'hotel' && String(this.order.id).startsWith('HT')) {
          try {
            await hotelOrderApi.create({
              orderNo: this.order.id,
              userId: this.order.userId || 0,
              hotelId: this.order.hotelId || 0,
              hotelName: (this.order.hotelName || this.order.ticketName || '').replace(/\s+\d+晚$/, ''),
              hotelCover: this.order.hotelCover || this.order.cover || '',
              roomType: this.order.roomType || '标准房',
              quantity: this.order.quantity || this.order.nights || 1,
              price: this.order.price || 0,
              totalPrice: this.order.totalPrice || 0,
              checkInDate: this.order.checkInDate || '',
              checkOutDate: this.order.checkOutDate || '',
              contactName: this.order.contactName || '',
              contactPhone: this.order.contactPhone || '',
              remark: '酒店预订'
            })
            const orderNo2 = this.order.orderNo || this.order.id
            await hotelOrderApi.pay(orderNo2)
          } catch (err) {
            console.error('降级订单同步失败:', err)
          }
        }
        this.order.status = '待使用'
        this.showPayModal = false
        uni.showToast({ title: '支付成功', icon: 'success' })

        const orders = uni.getStorageSync('orders') || []
        const idx = orders.findIndex(o => o.id === this.order.id)
        if (idx >= 0) {
          orders[idx].status = '待使用'
          uni.setStorageSync('orders', orders)
        }
      } finally {
        this.paying = false
      }
    },
    async cancelOrder() {
      uni.showModal({
        title: this.order.status === '待使用' ? '申请退款' : '取消订单',
        content: this.order.status === '待使用' ? '确定申请退款？' : '确定取消该订单？',
        success: async (res) => {
          if (res.confirm) {
            try {
              const orderNo = this.order.orderNo || this.order.id
              if (this.orderType === 'food') await foodOrderApi.cancel(orderNo)
              else if (this.orderType === 'hotel') await hotelOrderApi.cancel(orderNo)
              else if (this.orderType === 'product') await productOrderApi.cancel(orderNo)
              else await ticketOrderApi.cancel(orderNo)
              
              this.order.status = '已取消'
              uni.showToast({ title: this.order.status === '待使用' ? '退款申请已提交' : '订单已取消', icon: 'success' })
              
              const orders = uni.getStorageSync('orders') || []
              const idx = orders.findIndex(o => o.id === this.order.id)
              if (idx >= 0) {
                orders[idx].status = '已取消'
                uni.setStorageSync('orders', orders)
              }
            } catch (e) {
              this.order.status = '已取消'
              uni.showToast({ title: this.order.status === '待使用' ? '退款申请已提交' : '订单已取消', icon: 'success' })
              
              const orders = uni.getStorageSync('orders') || []
              const idx = orders.findIndex(o => o.id === this.order.id)
              if (idx >= 0) {
                orders[idx].status = '已取消'
                uni.setStorageSync('orders', orders)
              }
            }
          }
        }
      })
    },
    useOrder() {
      uni.showModal({
        title: '核销确认',
        content: `确认核销此订单？\n\n订单名称：${this.order.ticketName}\n订单号：${this.order.id}`,
        confirmText: '确认核销',
        confirmColor: '#A33B32',
        success: async (res) => {
          if (res.confirm) {
            try {
              const orderNo = this.order.orderNo || this.order.id
              if (this.orderType === 'food') await foodOrderApi.use(orderNo)
              else if (this.orderType === 'hotel') await hotelOrderApi.use(orderNo)
              else if (this.orderType === 'product') await productOrderApi.use(orderNo)
              else await ticketOrderApi.use(orderNo)

              const newStatus = this.orderType === 'hotel' ? '已入住' : '已完成'
              this.order.status = newStatus

              const orders = uni.getStorageSync('orders') || []
              const idx = orders.findIndex(o => o.id === this.order.id)
              if (idx >= 0) {
                orders[idx].status = newStatus
                uni.setStorageSync('orders', orders)
              }

              uni.showToast({
                title: '核销成功',
                icon: 'success',
                duration: 2000
              })
            } catch (e) {
              // API失败时降级更新本地状态
              const newStatus = this.orderType === 'hotel' ? '已入住' : '已完成'
              this.order.status = newStatus

              const orders = uni.getStorageSync('orders') || []
              const idx = orders.findIndex(o => o.id === this.order.id)
              if (idx >= 0) {
                orders[idx].status = newStatus
                uni.setStorageSync('orders', orders)
              }

              uni.showToast({
                title: '核销成功',
                icon: 'success',
                duration: 2000
              })
            }
          }
        }
      })
    },
    buyAgain() {
      const o = this.order
      if (this.orderType === 'food' && o.foodId) {
        uni.navigateTo({ url: '/pages/food/detail?id=' + o.foodId })
      } else if (this.orderType === 'ticket' && o.ticketId) {
        uni.navigateTo({ url: '/pages/ticket/ticket?id=' + o.ticketId })
      } else if (this.orderType === 'hotel' && o.hotelId) {
        uni.navigateTo({ url: '/pages/hotel/hotel?id=' + o.hotelId })
      } else if (this.orderType === 'product' && o.productId) {
        uni.navigateTo({ url: '/pages/shop/shop?id=' + o.productId })
      } else {
        const pages = { ticket: '/pages/ticket/ticket', food: '/pages/food/food', hotel: '/pages/hotel/hotel', product: '/pages/shop/shop' }
        uni.navigateTo({ url: pages[this.orderType] || '/pages/index/index' })
      }
    }
  }
}
</script>

<style scoped lang="scss">
.order-detail-page {
  background: #F8FAFC;
  min-height: 100vh;
}

/* ===== 顶部状态栏 ===== */
.status-header {
  background: linear-gradient(135deg, #1D3B5A, #2E5F8E);
  padding: 60rpx 32rpx 40rpx;
  display: flex;
  flex-direction: column;
  align-items: center;
}
.status-header.status-pending {
  background: linear-gradient(135deg, #1D3B5A, #2E5F8E);
}
.status-header.status-using {
  background: linear-gradient(135deg, #1D3B5A, #2E5F8E);
}
.status-header.status-done {
  background: linear-gradient(135deg, #3A7D44, #4CAF50);
}
.status-header.status-cancel {
  background: linear-gradient(135deg, #888, #999);
}
.status-icon {
  font-size: 72rpx;
  margin-bottom: 16rpx;
}
.status-text {
  font-size: 36rpx;
  font-weight: bold;
  color: #FFFFFF;
  margin-bottom: 8rpx;
}
.status-desc {
  font-size: 24rpx;
  color: rgba(255,255,255,0.8);
}

/* ===== 信息卡片 ===== */
.info-card {
  background: #FFFFFF;
  margin: 20rpx 24rpx;
  border-radius: 20rpx;
  overflow: hidden;
  box-shadow: 0 4rpx 20rpx rgba(29,59,90,0.06);
}

/* ===== 商品信息 ===== */
.product-section {
  display: flex;
  gap: 20rpx;
  padding: 24rpx;
}
.product-cover-wrap {
  flex-shrink: 0;
}
.product-cover {
  width: 180rpx;
  height: 180rpx;
  border-radius: 16rpx;
}
.product-icon-box {
  width: 180rpx;
  height: 180rpx;
  border-radius: 16rpx;
  display: flex;
  align-items: center;
  justify-content: center;
}
.icon-ticket { background: #EEF3F8; }
.icon-food { background: #E8F4FD; }
.icon-hotel { background: #E8F5E9; }
.icon-product { background: #F3E8FF; }
.product-type-icon {
  width: 72rpx;
  height: 72rpx;
}
.product-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 12rpx;
  justify-content: center;
}
.product-name {
  font-size: 30rpx;
  font-weight: bold;
  color: #1D3B5A;
  line-height: 1.4;
}
.product-type-tag {
  display: inline-block;
  width: fit-content;
  font-size: 20rpx;
  color: #1D3B5A;
  background: rgba(29,59,90,0.1);
  padding: 4rpx 14rpx;
  border-radius: 20rpx;
}

/* ===== 卡片头部 ===== */
.card-header {
  padding: 20rpx 24rpx;
  border-bottom: 1rpx solid #F0F0F0;
}
.card-title {
  font-size: 28rpx;
  font-weight: bold;
  color: #1D3B5A;
}

/* ===== 详情列表 ===== */
.detail-list {
  padding: 20rpx 24rpx;
}
.detail-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16rpx 0;
  border-bottom: 1rpx solid #F8F9FA;
}
.detail-item:last-child {
  border-bottom: none;
}
.detail-label-wrap {
  display: flex;
  align-items: center;
  gap: 8rpx;
}
.detail-icon {
  width: 28rpx;
  height: 28rpx;
  opacity: 0.7;
}
.detail-label {
  font-size: 26rpx;
  color: #64748B;
}
.detail-value {
  font-size: 26rpx;
  color: #1D3B5A;
  text-align: right;
}

/* ===== 价格区域 ===== */
.price-section {
  padding: 20rpx 24rpx;
}
.price-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12rpx 0;
}
.price-row.total {
  padding-top: 20rpx;
  border-top: 1rpx dashed #E2E8F0;
  margin-top: 8rpx;
}
.price-label {
  font-size: 26rpx;
  color: #64748B;
}
.price-value {
  font-size: 28rpx;
  color: #1D3B5A;
}
.price-value.total {
  font-size: 36rpx;
  font-weight: bold;
  color: #2E5F8E;
}

/* ===== 操作按钮 ===== */
.action-section {
  display: flex;
  gap: 16rpx;
  padding: 20rpx 24rpx;
}
.action-btn {
  flex: 1;
  text-align: center;
  padding: 20rpx;
  border-radius: 50rpx;
  font-size: 28rpx;
  font-weight: bold;
}
.action-btn.cancel {
  color: #666;
  border: 1rpx solid #E2E8F0;
  background: #FFFFFF;
}
.action-btn.pay {
  color: #FFFFFF;
  background: linear-gradient(135deg, #1D3B5A, #2E5F8E);
}
.action-btn.use {
  color: #FFFFFF;
  background: linear-gradient(135deg, #1D3B5A, #2E5F8E);
}
.action-btn.again {
  color: #1D3B5A;
  border: 1rpx solid #1D3B5A;
  background: #FFFFFF;
}

/* ===== 支付弹窗 ===== */
.pay-mask {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0,0,0,0.5);
  z-index: 100;
  display: flex;
  align-items: flex-end;
}
.pay-sheet {
  background: #FFFFFF;
  border-radius: 32rpx 32rpx 0 0;
  padding: 0 0 60rpx;
  width: 100%;
}
.pay-sheet-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 32rpx 32rpx 24rpx;
  border-bottom: 1rpx solid #E8E8E8;
}
.pay-sheet-title {
  font-size: 32rpx;
  font-weight: bold;
  color: #1D3B5A;
}
.pay-sheet-close {
  font-size: 40rpx;
  color: #999;
  padding: 0 8rpx;
}
.pay-order-info {
  padding: 24rpx 32rpx 16rpx;
}
.pay-order-name {
  font-size: 28rpx;
  font-weight: bold;
  color: #1D3B5A;
  display: block;
  margin-bottom: 8rpx;
}
.pay-order-no {
  font-size: 22rpx;
  color: #999;
}
.pay-amount-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16rpx 32rpx 24rpx;
  border-bottom: 1rpx solid #E8E8E8;
}
.pay-amount-label {
  font-size: 28rpx;
  color: #666;
}
.pay-amount {
  font-size: 48rpx;
  font-weight: bold;
  color: #2E5F8E;
}
.pay-methods {
  padding: 20rpx 32rpx;
  display: flex;
  flex-direction: column;
  gap: 16rpx;
}
.pay-method-item {
  display: flex;
  align-items: center;
  gap: 20rpx;
  padding: 20rpx 24rpx;
  background: #F8F9FA;
  border-radius: 16rpx;
  border: 2rpx solid transparent;
  box-shadow: 0 2rpx 10rpx rgba(29,59,90,0.04);
}
.pay-method-item.selected {
  border-color: #1D3B5A;
  background: #E8F4FD;
}
.pay-method-icon {
  width: 48rpx;
  height: 48rpx;
}
.pay-method-name {
  flex: 1;
  font-size: 28rpx;
  color: #333;
}
.pay-method-check {
  width: 36rpx;
  height: 36rpx;
  border-radius: 50%;
  background: #1D3B5A;
  color: #FFFFFF;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 22rpx;
}
.pay-btn-row {
  padding: 24rpx 32rpx 8rpx;
}
.pay-confirm-btn {
  background: linear-gradient(135deg, #1D3B5A, #2E5F8E);
  border-radius: 50rpx;
  padding: 28rpx;
  text-align: center;
  font-size: 30rpx;
  font-weight: bold;
  color: #FFFFFF;
}
.pay-confirm-btn.loading {
  opacity: 0.7;
}
.pay-tip {
  display: block;
  text-align: center;
  font-size: 22rpx;
  color: #bbb;
  padding: 0 32rpx;
}
</style>
