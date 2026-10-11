<template>
  <view class="passport-page">
    <!-- 护照封面 -->
    <view class="passport-cover">
      <view class="cover-inner">
        <image class="cover-emblem" src="/static/icons/feiyi.png" mode="aspectFit"></image>
        <text class="cover-title">非遗护照</text>
        <text class="cover-subtitle">HERITAGE PASSPORT</text>
        <view class="cover-line"></view>
        <text class="cover-region">广西 · 三江侗族自治县</text>
        <view class="cover-stamps-count">
          <text class="cover-count-num">{{ progress.current }}</text>
          <text class="cover-count-divider">/</text>
          <text class="cover-count-total">{{ progress.total }}</text>
        </view>
        <text class="cover-count-label">已集印章</text>
      </view>
    </view>

    <!-- 进度条 -->
    <view class="progress-section">
      <view class="progress-bar-bg">
        <view class="progress-bar-fill" :style="{ width: progress.percentage + '%' }"></view>
        <view class="progress-bar-glow"></view>
      </view>
      <text class="progress-text">集章进度 {{ progress.current }}/{{ progress.total }}</text>
    </view>

    <!-- 印章网格 -->
    <view class="stamps-section">
      <text class="section-title">非遗印章集</text>
      <view class="stamps-grid">
        <view
          class="stamp-slot"
          v-for="stamp in allStamps"
          :key="stamp.id"
          :class="{ collected: isCollected(stamp.id), locked: !isCollected(stamp.id) }"
          @tap="onStampTap(stamp)"
        >
          <view class="stamp-circle" :style="isCollected(stamp.id) ? { borderColor: stamp.color, background: stamp.color + '20' } : {}">
            <image class="stamp-icon-img" v-if="isCollected(stamp.id)" :src="getStampIcon(stamp.id)" mode="aspectFill"></image>
            <image class="stamp-lock-img" v-else src="/static/icons/love.png" mode="aspectFit"></image>
          </view>
          <text class="stamp-name" :class="{ 'stamp-name-locked': !isCollected(stamp.id) }">{{ stamp.name }}</text>
          <text class="stamp-date" v-if="getCollectedStamp(stamp.id)">{{ formatDate(getCollectedStamp(stamp.id).unlockedAt) }}</text>
          <view class="stamp-method-wrap" v-if="getCollectedStamp(stamp.id)">
            <image class="method-icon" :src="getCollectedStamp(stamp.id).method === 'location' ? '/static/icons/map.png' : '/static/icons/ticket.png'" mode="aspectFit"></image>
            <text class="stamp-method">{{ getCollectedStamp(stamp.id).method === 'location' ? '打卡' : '预约' }}</text>
          </view>
        </view>
      </view>
    </view>

    <!-- 全部集齐奖励 -->
    <view class="reward-section" v-if="progress.current >= 6">
      <view class="reward-card">
        <image class="reward-badge" src="/static/icons/heritage-active.png" mode="aspectFit"></image>
        <text class="reward-title">非遗守护者</text>
        <text class="reward-desc">恭喜您集齐全部6枚非遗印章！您已获得「非遗守护者」荣誉称号，可享受专属优惠权益。</text>
        <view class="reward-perks">
          <view class="perk-item">
            <image class="perk-icon-img" src="/static/icons/ticket.png" mode="aspectFit"></image>
            <text class="perk-text">门票9折优惠</text>
          </view>
          <view class="perk-item">
            <image class="perk-icon-img" src="/static/icons/hotel.png" mode="aspectFit"></image>
            <text class="perk-text">酒店优先预订</text>
          </view>
          <view class="perk-item">
            <image class="perk-icon-img" src="/static/icons/love.png" mode="aspectFit"></image>
            <text class="perk-text">非遗礼品一份</text>
          </view>
        </view>
      </view>
    </view>

    <!-- 集章攻略 -->
    <view class="tips-section">
      <text class="section-title">集章攻略</text>
      <view class="tip-card">
        <image class="tip-icon-img" src="/static/icons/map.png" mode="aspectFit"></image>
        <view class="tip-content">
          <text class="tip-title">地图打卡</text>
          <text class="tip-desc">前往非遗所在地点，到达后在地图页面点击「打卡集章」即可解锁印章</text>
        </view>
      </view>
    </view>

    <view style="height: 60rpx;"></view>
  </view>
</template>

<script>
import { STAMPS, getCollectedStamps, getStampProgress, hasStamp } from '../../utils/stamps.js'

export default {
  data() {
    return {
      allStamps: STAMPS,
      progress: { current: 0, total: 6, percentage: 0, collected: [], remaining: [] },
      collectedList: []
    }
  },
  onShow() {
    this.loadData()
  },
  methods: {
    loadData() {
      this.collectedList = getCollectedStamps()
      this.progress = getStampProgress()
    },
    isCollected(stampId) {
      return this.collectedList.some(s => s.id === stampId)
    },
    getCollectedStamp(stampId) {
      return this.collectedList.find(s => s.id === stampId) || null
    },
    getStampIcon(stampId) {
      const stamp = this.allStamps.find(s => s.id === stampId)
      return stamp ? stamp.image : '/static/icons/heritage.png'
    },
    formatDate(isoStr) {
      if (!isoStr) return ''
      const d = new Date(isoStr)
      return `${d.getMonth() + 1}/${d.getDate()}`
    },
    onStampTap(stamp) {
      if (this.isCollected(stamp.id)) {
        const cs = this.getCollectedStamp(stamp.id)
        const methodText = cs.method === 'location' ? '地图打卡' : '体验预约'
        uni.showModal({
          title: `${stamp.name}`,
          content: `已集章\n解锁方式：${methodText}\n集章时间：${cs.unlockedAt ? new Date(cs.unlockedAt).toLocaleDateString() : '未知'}`,
          showCancel: false,
          confirmText: '知道了'
        })
      } else {
        uni.showModal({
          title: `${stamp.name}`,
          content: '尚未解锁此印章\n\n前往地图集卡页面，到达对应非遗地点后即可打卡集章。',
          confirmText: '去集卡',
          cancelText: '稍后再说',
          success: (res) => {
            if (res.confirm) {
              uni.navigateTo({ url: `/pages/map/map?filter=stamp&heritageId=${stamp.id}` })
            }
          }
        })
      }
    }
  }
}
</script>

<style scoped lang="scss">
.passport-page {
  background: linear-gradient(180deg, #F0F7FF 0%, #F8FAFC 50%, #FFFFFF 100%);
  min-height: 100vh;
  padding-bottom: 40rpx;
}

/* ===== 护照封面 ===== */
.passport-cover {
  padding: 40rpx 30rpx 30rpx;
}
.cover-inner {
  background: #FFFFFF;
  border-radius: 28rpx;
  padding: 50rpx 40rpx 45rpx;
  display: flex;
  flex-direction: column;
  align-items: center;
  position: relative;
  overflow: hidden;
  box-shadow: 0 12rpx 48rpx rgba(29, 59, 90, 0.20);
  border: 3rpx solid #F0F7FF;
}
.cover-inner::before {
  content: '';
  position: absolute;
  top: -80rpx;
  right: -80rpx;
  width: 240rpx;
  height: 240rpx;
  background: radial-gradient(circle, rgba(255,127,80,0.2) 0%, transparent 70%);
  border-radius: 50%;
}
.cover-inner::after {
  content: '';
  position: absolute;
  bottom: -60rpx;
  left: -60rpx;
  width: 200rpx;
  height: 200rpx;
  background: radial-gradient(circle, rgba(74,144,226,0.18) 0%, transparent 70%);
  border-radius: 50%;
}
.cover-emblem {
  width: 90rpx;
  height: 90rpx;
  margin-bottom: 16rpx;
  position: relative;
  z-index: 1;
}
.cover-title {
  font-size: 52rpx;
  font-weight: 800;
  color: #1D3B5A;
  letter-spacing: 0.12em;
  margin-bottom: 6rpx;
  position: relative;
  z-index: 1;
}
.cover-subtitle {
  font-size: 20rpx;
  color: #64748B;
  letter-spacing: 0.25em;
  margin-bottom: 20rpx;
  position: relative;
  z-index: 1;
}
.cover-line {
  width: 80rpx;
  height: 3rpx;
  background: linear-gradient(90deg, transparent 0%, #4A90E2 50%, transparent 100%);
  margin-bottom: 18rpx;
  position: relative;
  z-index: 1;
}
.cover-region {
  font-size: 22rpx;
  color: #64748B;
  margin-bottom: 28rpx;
  position: relative;
  z-index: 1;
}
.cover-stamps-count {
  display: flex;
  align-items: baseline;
  position: relative;
  z-index: 1;
  padding: 16rpx 32rpx;
  background: #F0F7FF;
  border-radius: 50rpx;
}
.cover-count-num {
  font-size: 68rpx;
  font-weight: 800;
  color: #4A90E2;
}
.cover-count-divider {
  font-size: 36rpx;
  color: #94A3B8;
  margin: 0 6rpx;
}
.cover-count-total {
  font-size: 36rpx;
  font-weight: 600;
  color: #94A3B8;
}
.cover-count-label {
  font-size: 20rpx;
  color: #94A3B8;
  margin-top: 10rpx;
  position: relative;
  z-index: 1;
  letter-spacing: 0.08em;
}

/* ===== 进度条 ===== */
.progress-section {
  padding: 0 30rpx 28rpx;
}
.progress-bar-bg {
  height: 16rpx;
  background: #E2E8F0;
  border-radius: 8rpx;
  overflow: hidden;
  margin-bottom: 12rpx;
  position: relative;
  box-shadow: inset 0 2rpx 6rpx rgba(0,0,0,0.06);
}
.progress-bar-fill {
  height: 100%;
  background: linear-gradient(90deg, #FF7F50 0%, #FF9F50 50%, #4A90E2 100%);
  border-radius: 8rpx;
  transition: width 0.6s cubic-bezier(0.4, 0, 0.2, 1);
  position: relative;
  z-index: 2;
}
.progress-bar-glow {
  position: absolute;
  top: 0;
  left: 0;
  height: 100%;
  width: 100%;
  background: linear-gradient(90deg, transparent 0%, rgba(255,255,255,0.4) 50%, transparent 100%);
  animation: shimmer 2s infinite;
  z-index: 1;
}
@keyframes shimmer {
  0% { transform: translateX(-100%); }
  100% { transform: translateX(100%); }
}
.progress-text {
  font-size: 22rpx;
  color: #64748B;
  text-align: center;
  display: block;
  font-weight: 500;
}

/* ===== 印章网格 ===== */
.stamps-section {
  padding: 0 30rpx 32rpx;
}
.section-title {
  font-size: 32rpx;
  font-weight: 700;
  color: #1D3B5A;
  display: block;
  margin-bottom: 24rpx;
  letter-spacing: 0.02em;
}
.stamps-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 20rpx;
}
.stamp-slot {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 10rpx;
  padding: 24rpx 12rpx;
  background: #FFFFFF;
  border-radius: 24rpx;
  box-shadow: 0 4rpx 20rpx rgba(29, 59, 90, 0.08);
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  border: 2rpx solid transparent;
}
.stamp-slot:active {
  transform: scale(0.94);
}
.stamp-slot.collected {
  background: linear-gradient(135deg, #FFFFFF 0%, #F0F7FF 100%);
  box-shadow: 0 6rpx 24rpx rgba(74,144,226,0.2);
  border-color: rgba(74,144,226,0.2);
}
.stamp-slot.locked {
  background: #F8FAFC;
  box-shadow: 0 2rpx 12rpx rgba(0,0,0,0.04);
}
.stamp-circle {
  width: 100rpx;
  height: 100rpx;
  border-radius: 50%;
  border: 4rpx solid #E2E8F0;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #F8FAFC;
  overflow: hidden;
}
.stamp-icon-img {
  width: 100%;
  height: 100%;
  border-radius: 50%;
}
.stamp-lock-img {
  width: 40rpx;
  height: 40rpx;
  opacity: 0.25;
}
.stamp-name {
  font-size: 23rpx;
  font-weight: 700;
  color: #1D3B5A;
  text-align: center;
  line-height: 1.3;
}
.stamp-name-locked {
  color: #94A3B8;
  font-weight: 500;
}
.stamp-date {
  font-size: 18rpx;
  color: #64748B;
  font-weight: 500;
}
.stamp-method-wrap {
  display: flex;
  align-items: center;
  gap: 4rpx;
  padding: 4rpx 10rpx;
  background: rgba(74,144,226,0.1);
  border-radius: 20rpx;
}
.method-icon {
  width: 20rpx;
  height: 20rpx;
}
.stamp-method {
  font-size: 17rpx;
  color: #4A90E2;
  font-weight: 600;
}

/* ===== 奖励卡片 ===== */
.reward-section {
  padding: 0 30rpx 32rpx;
}
.reward-card {
  background: linear-gradient(135deg, #FFFBEB 0%, #FFF7E6 50%, #FFEED9 100%);
  border: 3rpx solid #FFD591;
  border-radius: 24rpx;
  padding: 36rpx 32rpx;
  text-align: center;
  position: relative;
  overflow: hidden;
  box-shadow: 0 8rpx 32rpx rgba(212,107,8,0.15);
}
.reward-card::before {
  content: '';
  position: absolute;
  top: -40rpx;
  right: -40rpx;
  width: 160rpx;
  height: 160rpx;
  background: radial-gradient(circle, rgba(212,107,8,0.15) 0%, transparent 70%);
  border-radius: 50%;
}
.reward-badge {
  width: 80rpx;
  height: 80rpx;
  margin-bottom: 14rpx;
  position: relative;
  z-index: 1;
}
.reward-title {
  font-size: 34rpx;
  font-weight: 800;
  color: #D46B08;
  display: block;
  margin-bottom: 12rpx;
  position: relative;
  z-index: 1;
  letter-spacing: 0.04em;
}
.reward-desc {
  font-size: 24rpx;
  color: #8C6D32;
  line-height: 1.7;
  display: block;
  margin-bottom: 24rpx;
  position: relative;
  z-index: 1;
}
.reward-perks {
  display: flex;
  justify-content: space-around;
  position: relative;
  z-index: 1;
}
.perk-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8rpx;
  padding: 16rpx 12rpx;
  background: rgba(255,255,255,0.7);
  border-radius: 16rpx;
}
.perk-icon-img {
  width: 40rpx;
  height: 40rpx;
}
.perk-text {
  font-size: 20rpx;
  color: #8C6D32;
  font-weight: 600;
}

/* ===== 集章攻略 ===== */
.tips-section {
  padding: 0 30rpx;
}
.tip-card {
  display: flex;
  gap: 18rpx;
  background: #FFFFFF;
  border-radius: 20rpx;
  padding: 28rpx;
  margin-bottom: 18rpx;
  box-shadow: 0 4rpx 20rpx rgba(29,59,90,0.06);
  border: 2rpx solid #F0F7FF;
  transition: all 0.3s ease;
}
.tip-card:active {
  transform: translateY(-2rpx);
  box-shadow: 0 6rpx 24rpx rgba(29,59,90,0.1);
}
.tip-icon-img {
  width: 48rpx;
  height: 48rpx;
  flex-shrink: 0;
  margin-top: 2rpx;
}
.tip-content {
  flex: 1;
}
.tip-title {
  font-size: 28rpx;
  font-weight: 700;
  color: #1D3B5A;
  display: block;
  margin-bottom: 8rpx;
}
.tip-desc {
  font-size: 23rpx;
  color: #64748B;
  line-height: 1.7;
  display: block;
}
</style>
