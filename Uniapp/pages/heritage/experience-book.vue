<template>
  <view class="book-page">
    <!-- 顶部体验信息 -->
    <view class="book-hero">
      <image class="book-hero-bg" :src="heritageCover" mode="aspectFill" />
      <view class="book-hero-mask"></view>
      <view class="book-hero-info">
        <text class="book-hero-name">{{ heritageName }}体验</text>
        <text class="book-hero-desc">传承人亲自指导，深度体验非遗技艺</text>
      </view>
    </view>

    <!-- 体验类型选择 -->
    <view class="section">
      <text class="section-title">选择体验项目</text>
      <view class="type-list">
        <view
          class="type-card"
          v-for="t in experienceTypes"
          :key="t.id"
          :class="{ active: form.experienceType === t.id }"
          @tap="selectType(t)"
        >
          <view class="type-icon-wrap" :style="{ background: t.color + '18' }">
            <image class="type-icon" :src="t.icon" mode="aspectFit" />
          </view>
          <view class="type-info">
            <text class="type-name">{{ t.name }}</text>
            <text class="type-desc">{{ t.desc }}</text>
          </view>
          <view class="type-check" :style="form.experienceType === t.id ? { background: t.color, borderColor: t.color } : {}">
            <text class="type-check-icon" v-if="form.experienceType === t.id">✓</text>
          </view>
        </view>
      </view>
    </view>

    <!-- 日期选择 -->
    <view class="section">
      <text class="section-title">预约日期</text>
      <view class="date-picker-wrap">
        <picker mode="date" :start="minDate" :end="maxDate" :value="form.visitDate" @change="onDateChange">
          <view class="date-picker" :class="{ placeholder: !form.visitDate }">
            <image class="picker-icon" src="/static/icons/detail.png" mode="aspectFit" />
            <text class="picker-text">{{ form.visitDate || '请选择预约日期' }}</text>
            <text class="picker-arrow">›</text>
          </view>
        </picker>
      </view>
    </view>

    <!-- 时段选择 -->
    <view class="section">
      <text class="section-title">预约时段</text>
      <view class="time-slots">
        <view
          class="time-slot"
          v-for="slot in timeSlots"
          :key="slot.id"
          :class="{ active: form.visitTime === slot.id, disabled: slot.full }"
          @tap="!slot.full && selectTime(slot)"
        >
          <text class="time-slot-period">{{ slot.period }}</text>
          <text class="time-slot-range">{{ slot.range }}</text>
          <text class="time-slot-status" v-if="slot.full">已满</text>
          <text class="time-slot-status available" v-else>可约</text>
        </view>
      </view>
    </view>

    <!-- 人数选择 -->
    <view class="section">
      <text class="section-title">预约人数</text>
      <view class="quantity-row">
        <view class="quantity-info">
          <text class="quantity-label">体验人数</text>
          <text class="quantity-hint">每场1-8人，含材料费</text>
        </view>
        <view class="quantity-ctrl">
          <view class="qty-btn" :class="{ disabled: form.quantity <= 1 }" @tap="changeQty(-1)">
            <text class="qty-btn-text">-</text>
          </view>
          <text class="qty-num">{{ form.quantity }}</text>
          <view class="qty-btn" :class="{ disabled: form.quantity >= 8 }" @tap="changeQty(1)">
            <text class="qty-btn-text">+</text>
          </view>
        </view>
      </view>
    </view>

    <!-- 联系信息 -->
    <view class="section">
      <text class="section-title">联系信息</text>
      <view class="form-group">
        <view class="form-item">
          <text class="form-label">姓名</text>
          <input class="form-input" v-model="form.contactName" placeholder="请输入您的姓名" />
        </view>
        <view class="form-item">
          <text class="form-label">手机号</text>
          <input class="form-input" v-model="form.contactPhone" type="number" placeholder="请输入手机号码" maxlength="11" />
        </view>
      </view>
    </view>

    <!-- 备注 -->
    <view class="section">
      <text class="section-title">备注信息</text>
      <textarea class="form-textarea" v-model="form.remark" placeholder="如有特殊需求请备注（选填）" maxlength="200" />
    </view>

    <!-- 费用明细 -->
    <view class="section">
      <text class="section-title">费用明细</text>
      <view class="price-card">
        <view class="price-row">
          <text class="price-label">体验费用</text>
          <text class="price-value" v-if="unitPrice > 0">¥{{ unitPrice }}/人 × {{ form.quantity }}人</text>
          <text class="price-value free" v-else>免费</text>
        </view>
        <view class="price-divider"></view>
        <view class="price-row total">
          <text class="price-label">合计</text>
          <text class="price-total" :class="{ free: unitPrice <= 0 }">{{ unitPrice > 0 ? '¥' + totalPrice : '免费' }}</text>
        </view>
      </view>
    </view>

    <!-- 预约须知 -->
    <view class="section">
      <text class="section-title">预约须知</text>
      <view class="notice-list">
        <view class="notice-item" v-for="(n, i) in notices" :key="i">
          <text class="notice-dot">·</text>
          <text class="notice-text">{{ n }}</text>
        </view>
      </view>
    </view>

    <view style="height: 180rpx;"></view>

    <!-- 底部提交栏 -->
    <view class="bottom-bar">
      <view class="bottom-price">
        <text class="bottom-price-label">合计</text>
        <text class="bottom-price-num" v-if="unitPrice > 0">¥{{ totalPrice }}</text>
        <text class="bottom-price-num free" v-else>免费</text>
      </view>
      <view class="submit-btn" @tap="submitOrder">
        <text class="submit-text">提交预约</text>
      </view>
    </view>
  </view>
</template>

<script>
import { experienceOrderApi } from '../../utils/api.js'
import { collectStamp, showStampToast, hasStamp } from '../../utils/stamps.js'

export default {
  data() {
    const today = new Date()
    const max = new Date()
    max.setDate(max.getDate() + 30)
    return {
      heritageId: 0,
      heritageName: '',
      heritageCover: '',
      unitPrice: 88,
      form: {
        experienceType: '',
        visitDate: '',
        visitTime: '',
        quantity: 1,
        contactName: '',
        contactPhone: '',
        remark: ''
      },
      // 各非遗项目对应价格
      priceMap: {
        1: 88,   // 侗族大歌
        2: 0,    // 风雨桥 - 免费
        3: 10,   // 鼓楼
        4: 68,   // 侗锦织造
        5: 78,   // 芦笙
        6: 58    // 多耶
      },
      experienceTypes: [
        { id: 'singing', name: '侗族大歌学唱', desc: '传承人教唱多声部侗歌', icon: '/static/icons/explore.png', color: '#4A90E2' },
        { id: 'craft', name: '侗锦织造体验', desc: '亲手操作传统织锦机', icon: '/static/icons/love.png', color: '#4A90E2' },
        { id: 'lusheng', name: '芦笙吹奏体验', desc: '学习吹奏侗族芦笙', icon: '/static/icons/ticket.png', color: '#FF7F50' },
        { id: 'architecture', name: '风雨桥建筑讲解', desc: '了解榫卯结构与建造工艺', icon: '/static/icons/map.png', color: '#2C3E50' },
        { id: 'duoye', name: '多耶舞体验', desc: '加入侗族集体舞蹈', icon: '/static/icons/discover.png', color: '#FF7F50' },
        { id: 'drum', name: '鼓楼文化体验', desc: '登鼓楼、听侗歌、品油茶', icon: '/static/icons/heritage.png', color: '#2C3E50' }
      ],
      timeSlots: [
        { id: 'morning', period: '上午', range: '09:00 - 12:00', full: false },
        { id: 'afternoon', period: '下午', range: '14:00 - 17:00', full: false },
        { id: 'evening', period: '晚间', range: '19:00 - 21:00', full: false }
      ],
      notices: [
        '请至少提前1天预约，每日各时段限8人',
        '预约成功后工作人员将在24小时内电话确认',
        '体验当天请提前15分钟到达集合地点',
        '如需取消，请提前24小时操作，可全额退款',
        '体验费用含材料费、传承人指导费'
      ],
      submitting: false
    }
  },
  computed: {
    minDate() {
      const d = new Date()
      return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`
    },
    maxDate() {
      const d = new Date()
      d.setDate(d.getDate() + 30)
      return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`
    },
    totalPrice() {
      return (this.unitPrice * this.form.quantity).toFixed(0)
    }
  },
  onLoad(options) {
    this.heritageId = parseInt(options.id) || 0
    this.heritageName = decodeURIComponent(options.name || '')
    this.heritageCover = decodeURIComponent(options.cover || '')
    // 根据非遗项目设置价格
    this.unitPrice = this.priceMap[this.heritageId] !== undefined ? this.priceMap[this.heritageId] : 88
    // 根据非遗项目推荐默认体验类型
    this.autoSelectType()
    // 自动填充用户信息
    const userInfo = uni.getStorageSync('userInfo') || {}
    if (userInfo.name) {
      this.form.contactName = userInfo.name
    } else if (userInfo.nickname) {
      this.form.contactName = userInfo.nickname
    }
    if (userInfo.phone) {
      this.form.contactPhone = userInfo.phone
    }
  },
  methods: {
    autoSelectType() {
      const map = { 1: 'singing', 2: 'architecture', 3: 'drum', 4: 'craft', 5: 'lusheng', 6: 'duoye' }
      this.form.experienceType = map[this.heritageId] || 'singing'
    },
    selectType(type) {
      this.form.experienceType = type.id
    },
    onDateChange(e) {
      this.form.visitDate = e.detail.value
    },
    selectTime(slot) {
      this.form.visitTime = slot.id
    },
    changeQty(delta) {
      const next = this.form.quantity + delta
      if (next >= 1 && next <= 8) {
        this.form.quantity = next
      }
    },
    validate() {
      if (!this.form.experienceType) {
        uni.showToast({ title: '请选择体验项目', icon: 'none' }); return false
      }
      if (!this.form.visitDate) {
        uni.showToast({ title: '请选择预约日期', icon: 'none' }); return false
      }
      if (!this.form.visitTime) {
        uni.showToast({ title: '请选择预约时段', icon: 'none' }); return false
      }
      if (!this.form.contactName.trim()) {
        uni.showToast({ title: '请输入姓名', icon: 'none' }); return false
      }
      if (!this.form.contactPhone || this.form.contactPhone.length !== 11) {
        uni.showToast({ title: '请输入正确的手机号', icon: 'none' }); return false
      }
      return true
    },
    async submitOrder() {
      if (!this.validate() || this.submitting) return
      this.submitting = true
      uni.showLoading({ title: '提交中...' })
      try {
        const userInfo = uni.getStorageSync('userInfo') || {}
        const selectedType = this.experienceTypes.find(t => t.id === this.form.experienceType)
        const data = {
          userId: userInfo.userId || userInfo.id || 0,
          heritageId: this.heritageId,
          heritageName: this.heritageName,
          heritageCover: this.heritageCover,
          experienceType: this.form.experienceType,
          quantity: this.form.quantity,
          price: this.unitPrice,
          totalPrice: this.totalPrice,
          contactName: this.form.contactName.trim(),
          contactPhone: this.form.contactPhone,
          visitDate: this.form.visitDate,
          visitTime: this.form.visitTime,
          remark: this.form.remark
        }
        const result = await experienceOrderApi.create(data)
        uni.hideLoading()
        if (result && result.success) {
          // 预约成功后自动解锁印章
          const stampResult = collectStamp(this.heritageId, 'booking')
          if (stampResult.isNew) {
            showStampToast(stampResult.stamp, stampResult.allCollected)
          }
          uni.showModal({
            title: '预约成功',
            content: `您的「${this.heritageName}」体验预约已提交，工作人员将在24小时内与您电话确认。\n\n预约日期：${this.form.visitDate}\n预约时段：${this.timeSlots.find(s => s.id === this.form.visitTime)?.period || ''}\n预约人数：${this.form.quantity}人\n合计费用：¥${this.totalPrice}`,
            confirmText: '查看订单',
            cancelText: '继续浏览',
            success: (res) => {
              if (res.confirm) {
                uni.redirectTo({ url: '/pages/order/experience-order?status=待支付' })
              } else {
                uni.navigateBack()
              }
            }
          })
        } else {
          uni.showToast({ title: '预约失败，请重试', icon: 'none' })
        }
      } catch (e) {
        uni.hideLoading()
        console.error('预约提交失败:', e)
        uni.showToast({ title: '网络异常，请重试', icon: 'none' })
      } finally {
        this.submitting = false
      }
    }
  }
}
</script>

<style scoped lang="scss">
.book-page {
  background: #F8FAFC;
  min-height: 100vh;
  padding-bottom: env(safe-area-inset-bottom);
}

/* ===== 顶部英雄区 ===== */
.book-hero {
  position: relative;
  height: 320rpx;
  overflow: hidden;
}
.book-hero-bg {
  width: 100%;
  height: 100%;
}
.book-hero-mask {
  position: absolute;
  inset: 0;
  background: linear-gradient(to top, rgba(0,0,0,0.7) 0%, rgba(0,0,0,0.1) 100%);
}
.book-hero-info {
  position: absolute;
  bottom: 36rpx;
  left: 32rpx;
  right: 32rpx;
}
.book-hero-name {
  display: block;
  font-size: 40rpx;
  font-weight: 700;
  color: #FFFFFF;
  margin-bottom: 8rpx;
}
.book-hero-desc {
  display: block;
  font-size: 24rpx;
  color: rgba(255,255,255,0.8);
}

/* ===== 通用区块 ===== */
.section {
  padding: 28rpx 28rpx 0;
}
.section-title {
  display: block;
  font-size: 30rpx;
  font-weight: 700;
  color: #1D3B5A;
  margin-bottom: 20rpx;
}

/* ===== 体验类型 ===== */
.type-list {
  display: flex;
  flex-direction: column;
  gap: 16rpx;
}
.type-card {
  display: flex;
  align-items: center;
  gap: 20rpx;
  background: #FFFFFF;
  border-radius: 20rpx;
  padding: 24rpx;
  border: 2rpx solid #F1F5F9;
  transition: all 0.2s;
}
.type-card.active {
  border-color: #4A90E2;
  background: rgba(74,144,226,0.04);
  box-shadow: 0 4rpx 16rpx rgba(74,144,226,0.12);
}
.type-icon-wrap {
  width: 72rpx;
  height: 72rpx;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}
.type-icon {
  width: 36rpx;
  height: 36rpx;
}
.type-info {
  flex: 1;
}
.type-name {
  display: block;
  font-size: 28rpx;
  font-weight: 600;
  color: #1D3B5A;
  margin-bottom: 4rpx;
}
.type-desc {
  display: block;
  font-size: 22rpx;
  color: #94A3B8;
}
.type-check {
  width: 40rpx;
  height: 40rpx;
  border-radius: 50%;
  border: 3rpx solid #E2E8F0;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}
.type-check-icon {
  font-size: 22rpx;
  color: #FFFFFF;
  font-weight: 700;
}

/* ===== 日期选择 ===== */
.date-picker-wrap {
  background: #FFFFFF;
  border-radius: 16rpx;
  border: 2rpx solid #F1F5F9;
  overflow: hidden;
}
.date-picker {
  display: flex;
  align-items: center;
  gap: 16rpx;
  padding: 24rpx;
}
.date-picker.placeholder .picker-text {
  color: #CBD5E1;
}
.picker-icon {
  width: 32rpx;
  height: 32rpx;
  flex-shrink: 0;
}
.picker-text {
  flex: 1;
  font-size: 28rpx;
  color: #1D3B5A;
}
.picker-arrow {
  font-size: 32rpx;
  color: #CBD5E1;
}

/* ===== 时段选择 ===== */
.time-slots {
  display: flex;
  gap: 16rpx;
}
.time-slot {
  flex: 1;
  background: #FFFFFF;
  border-radius: 16rpx;
  padding: 24rpx 16rpx;
  text-align: center;
  border: 2rpx solid #F1F5F9;
  transition: all 0.2s;
}
.time-slot.active {
  border-color: #4A90E2;
  background: rgba(74,144,226,0.04);
}
.time-slot.disabled {
  opacity: 0.5;
}
.time-slot-period {
  display: block;
  font-size: 28rpx;
  font-weight: 600;
  color: #1D3B5A;
  margin-bottom: 6rpx;
}
.time-slot-range {
  display: block;
  font-size: 20rpx;
  color: #94A3B8;
  margin-bottom: 8rpx;
}
.time-slot-status {
  display: block;
  font-size: 20rpx;
  color: #CBD5E1;
}
.time-slot-status.available {
  color: #22C55E;
}

/* ===== 人数选择 ===== */
.quantity-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: #FFFFFF;
  border-radius: 16rpx;
  padding: 24rpx;
  border: 2rpx solid #F1F5F9;
}
.quantity-info {
  flex: 1;
}
.quantity-label {
  display: block;
  font-size: 28rpx;
  font-weight: 600;
  color: #1D3B5A;
  margin-bottom: 4rpx;
}
.quantity-hint {
  display: block;
  font-size: 22rpx;
  color: #94A3B8;
}
.quantity-ctrl {
  display: flex;
  align-items: center;
  gap: 0;
}
.qty-btn {
  width: 64rpx;
  height: 64rpx;
  background: #F1F5F9;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
}
.qty-btn.disabled {
  opacity: 0.3;
}
.qty-btn-text {
  font-size: 36rpx;
  color: #1D3B5A;
  font-weight: 600;
  line-height: 1;
}
.qty-num {
  font-size: 36rpx;
  font-weight: 700;
  color: #1D3B5A;
  width: 80rpx;
  text-align: center;
}

/* ===== 表单 ===== */
.form-group {
  background: #FFFFFF;
  border-radius: 16rpx;
  border: 2rpx solid #F1F5F9;
  overflow: hidden;
}
.form-item {
  display: flex;
  align-items: center;
  padding: 24rpx;
  border-bottom: 1rpx solid #F1F5F9;
}
.form-item:last-child {
  border-bottom: none;
}
.form-label {
  width: 140rpx;
  font-size: 28rpx;
  font-weight: 600;
  color: #1D3B5A;
  flex-shrink: 0;
}
.form-input {
  flex: 1;
  font-size: 28rpx;
  color: #1D3B5A;
}
.form-textarea {
  width: 100%;
  height: 160rpx;
  background: #FFFFFF;
  border-radius: 16rpx;
  border: 2rpx solid #F1F5F9;
  padding: 24rpx;
  font-size: 28rpx;
  color: #1D3B5A;
  box-sizing: border-box;
}

/* ===== 费用明细 ===== */
.price-card {
  background: #FFFFFF;
  border-radius: 16rpx;
  padding: 24rpx;
  border: 2rpx solid #F1F5F9;
}
.price-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12rpx 0;
}
.price-label {
  font-size: 26rpx;
  color: #64748B;
}
.price-value {
  font-size: 26rpx;
  color: #1D3B5A;
}
.price-divider {
  height: 1rpx;
  background: #F1F5F9;
  margin: 8rpx 0;
}
.price-row.total .price-label {
  font-size: 28rpx;
  font-weight: 700;
  color: #1D3B5A;
}
.price-total {
  font-size: 36rpx;
  font-weight: 700;
  color: #FF7F50;
}
.price-total.free,
.price-value.free {
  color: #22C55E;
}

/* ===== 预约须知 ===== */
.notice-list {
  background: #FFFFFF;
  border-radius: 16rpx;
  padding: 24rpx;
  border: 2rpx solid #F1F5F9;
}
.notice-item {
  display: flex;
  gap: 12rpx;
  margin-bottom: 12rpx;
}
.notice-item:last-child {
  margin-bottom: 0;
}
.notice-dot {
  font-size: 26rpx;
  color: #94A3B8;
  flex-shrink: 0;
  line-height: 1.6;
}
.notice-text {
  font-size: 24rpx;
  color: #64748B;
  line-height: 1.6;
}

/* ===== 底部提交栏 ===== */
.bottom-bar {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  display: flex;
  align-items: center;
  padding: 20rpx 28rpx;
  padding-bottom: calc(20rpx + env(safe-area-inset-bottom));
  background: #FFFFFF;
  box-shadow: 0 -4rpx 20rpx rgba(0,0,0,0.06);
  z-index: 100;
}
.bottom-price {
  flex: 1;
}
.bottom-price-label {
  font-size: 24rpx;
  color: #94A3B8;
}
.bottom-price-num {
  font-size: 40rpx;
  font-weight: 700;
  color: #FF7F50;
  margin-left: 8rpx;
}
.bottom-price-num.free {
  color: #22C55E;
}
.submit-btn {
  background: linear-gradient(135deg, #FF7F50 0%, #E8633A 100%);
  border-radius: 50rpx;
  padding: 22rpx 64rpx;
  box-shadow: 0 4rpx 16rpx rgba(255,127,80,0.3);
}
.submit-text {
  font-size: 30rpx;
  font-weight: 700;
  color: #FFFFFF;
}
</style>
