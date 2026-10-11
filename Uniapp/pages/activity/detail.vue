<template>
  <view class="detail-page">
    <view class="activity-cover">
      <image class="cover-img" :src="activity.cover || '/static/images/default-activity.png'" mode="aspectFill" />
      <view class="status-badge" :class="getStatusClass(activity.status)">{{ activity.status }}</view>
    </view>

    <view class="activity-content">
      <view class="activity-title">{{ activity.title }}</view>

      <view class="info-section">
        <view class="info-item">
          <image class="info-icon" src="/static/icons/position.png" mode="aspectFit" />
          <text class="info-text">{{ activity.location }}</text>
        </view>
        <view class="info-item">
          <image class="info-icon" src="/static/icons/time.png" mode="aspectFit" />
          <text class="info-text">{{ activity.startTime }} - {{ activity.endTime }}</text>
        </view>
        <view class="info-item">
          <image class="info-icon" src="/static/icons/user.png" mode="aspectFit" />
          <text class="info-text">已报名 {{ activity.currentParticipants || 0 }}/{{ activity.maxParticipants || 0 }}</text>
        </view>
      </view>

      <view class="section-title">活动详情</view>
      <view class="activity-description">{{ activity.description }}</view>

      <view class="section-title" v-if="activity.lat && activity.lng">活动地点</view>
      <view class="map-container" v-if="activity.lat && activity.lng">
        <map
          :latitude="activity.lat"
          :longitude="activity.lng"
          :markers="markers"
          style="width: 100%; height: 400rpx; border-radius: 16rpx;"
        />
      </view>
    </view>

    <view class="bottom-bar">
      <view class="participants-count">
        <text class="count-number">{{ activity.currentParticipants || 0 }}</text>
        <text class="count-label">人已报名</text>
      </view>
      <view class="action-buttons">
        <button class="btn-signup" v-if="activity.status === '报名中' && !isSignedUp" @tap="handleSignup">立即报名</button>
        <button class="btn-cancel" v-else-if="isSignedUp" @tap="handleCancel">取消报名</button>
        <button class="btn-disabled" v-else disabled>{{ activity.status }}</button>
      </view>
    </view>
  </view>
</template>

<script>
import { activityApi } from '../../utils/api.js'

export default {
  data() {
    return {
      activityId: null,
      activity: {},
      isSignedUp: false,
      markers: []
    }
  },
  onLoad(options) {
    this.activityId = options.id
    this.loadActivity()
  },
  methods: {
    async loadActivity() {
      try {
        uni.showLoading({ title: '加载中...' })
        const res = await activityApi.getDetail(this.activityId)
        this.activity = res

        // 设置地图标记
        if (res.lat && res.lng) {
          this.markers = [{
            id: 1,
            latitude: res.lat,
            longitude: res.lng,
            title: res.title,
            iconPath: '/static/icons/marker.png',
            width: 30,
            height: 30
          }]
        }

        // 检查是否已报名（这里简化处理，实际应该从后端获取）
        const signedActivities = uni.getStorageSync('signedActivities') || []
        this.isSignedUp = signedActivities.includes(this.activityId)
      } catch (error) {
        console.error('加载活动详情失败:', error)
        uni.showToast({ title: '加载失败', icon: 'none' })
      } finally {
        uni.hideLoading()
      }
    },
    async handleSignup() {
      try {
        // 检查是否登录
        const token = uni.getStorageSync('token')
        if (!token) {
          uni.showToast({ title: '请先登录', icon: 'none' })
          setTimeout(() => {
            uni.navigateTo({ url: '/pages/login/login' })
          }, 1500)
          return
        }

        uni.showLoading({ title: '报名中...' })
        await activityApi.signup(this.activityId)

        // 保存报名状态
        const signedActivities = uni.getStorageSync('signedActivities') || []
        signedActivities.push(this.activityId)
        uni.setStorageSync('signedActivities', signedActivities)

        this.isSignedUp = true
        uni.showToast({ title: '报名成功', icon: 'success' })
        this.loadActivity()
      } catch (error) {
        console.error('报名失败:', error)
        uni.showToast({ title: error.message || '报名失败', icon: 'none' })
      } finally {
        uni.hideLoading()
      }
    },
    async handleCancel() {
      try {
        const confirm = await uni.showModal({
          title: '提示',
          content: '确定要取消报名吗？'
        })

        if (!confirm.confirm) return

        uni.showLoading({ title: '取消中...' })
        await activityApi.cancelSignup(this.activityId)

        // 移除报名状态
        let signedActivities = uni.getStorageSync('signedActivities') || []
        signedActivities = signedActivities.filter(id => id !== this.activityId)
        uni.setStorageSync('signedActivities', signedActivities)

        this.isSignedUp = false
        uni.showToast({ title: '取消成功', icon: 'success' })
        this.loadActivity()
      } catch (error) {
        console.error('取消报名失败:', error)
        uni.showToast({ title: error.message || '取消失败', icon: 'none' })
      } finally {
        uni.hideLoading()
      }
    }
  },
  computed: {
    getStatusClass() {
      return (status) => {
        if (status === '报名中') return 'status-signup'
        if (status === '进行中') return 'status-ongoing'
        if (status === '已结束') return 'status-ended'
        return ''
      }
    }
  }
}
</script>

<style lang="scss" scoped>
.detail-page {
  min-height: 100vh;
  background: #f8f9fa;
  padding-bottom: 140rpx;
}

.activity-cover {
  position: relative;
  width: 100%;
  height: 500rpx;
}

.cover-img {
  width: 100%;
  height: 100%;
}

.status-badge {
  position: absolute;
  top: 40rpx;
  right: 40rpx;
  padding: 12rpx 28rpx;
  border-radius: 40rpx;
  font-size: 26rpx;
  font-weight: 600;
  color: #ffffff;
}

.status-signup {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.status-ongoing {
  background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
}

.status-ended {
  background: #999999;
}

.activity-content {
  padding: 32rpx;
}

.activity-title {
  font-size: 40rpx;
  font-weight: 700;
  color: #333;
  margin-bottom: 32rpx;
  line-height: 1.4;
}

.info-section {
  background: #ffffff;
  border-radius: 16rpx;
  padding: 24rpx;
  margin-bottom: 32rpx;
  box-shadow: 0 4rpx 12rpx rgba(0, 0, 0, 0.05);
}

.info-item {
  display: flex;
  align-items: center;
  padding: 16rpx 0;
  border-bottom: 1rpx solid #f0f0f0;
}

.info-item:last-child {
  border-bottom: none;
}

.info-icon {
  width: 36rpx;
  height: 36rpx;
  margin-right: 16rpx;
}

.info-text {
  font-size: 28rpx;
  color: #666;
  flex: 1;
}

.section-title {
  font-size: 32rpx;
  font-weight: 600;
  color: #333;
  margin-bottom: 20rpx;
  margin-top: 32rpx;
}

.activity-description {
  font-size: 28rpx;
  color: #666;
  line-height: 1.8;
  background: #ffffff;
  border-radius: 16rpx;
  padding: 24rpx;
  box-shadow: 0 4rpx 12rpx rgba(0, 0, 0, 0.05);
}

.map-container {
  margin-top: 20rpx;
  border-radius: 16rpx;
  overflow: hidden;
  box-shadow: 0 4rpx 12rpx rgba(0, 0, 0, 0.05);
}

.bottom-bar {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 20rpx 32rpx;
  background: #ffffff;
  box-shadow: 0 -4rpx 12rpx rgba(0, 0, 0, 0.05);
}

.participants-count {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
}

.count-number {
  font-size: 36rpx;
  font-weight: 700;
  color: #667eea;
}

.count-label {
  font-size: 24rpx;
  color: #999;
}

.action-buttons {
  flex: 1;
  display: flex;
  justify-content: flex-end;
}

.btn-signup {
  padding: 20rpx 60rpx;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: #ffffff;
  border-radius: 50rpx;
  font-size: 30rpx;
  font-weight: 600;
  border: none;
}

.btn-cancel {
  padding: 20rpx 60rpx;
  background: #ffffff;
  color: #667eea;
  border: 2rpx solid #667eea;
  border-radius: 50rpx;
  font-size: 30rpx;
  font-weight: 600;
}

.btn-disabled {
  padding: 20rpx 60rpx;
  background: #e0e0e0;
  color: #999;
  border-radius: 50rpx;
  font-size: 30rpx;
  border: none;
}
</style>
