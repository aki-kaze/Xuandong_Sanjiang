<template>
  <view class="list-page">
    <!-- 搜索栏 -->
    <view class="search-bar">
      <image class="search-icon" src="/static/icons/search.png" mode="aspectFit" />
      <input class="search-input" v-model="keyword" placeholder="搜索活动..." @confirm="loadActivities" />
    </view>

    <!-- 状态筛选 -->
    <view class="filter-bar">
      <view class="filter-item" v-for="f in filters" :key="f.id" :class="{ active: activeFilter === f.id }" @tap="changeFilter(f.id)">
        {{ f.label }}
      </view>
    </view>

    <!-- 活动列表 -->
    <view class="activity-list">
      <view class="activity-card card-shadow" v-for="item in activities" :key="item.id" @tap="goDetail(item.id)">
        <view class="activity-img-wrap">
          <image class="activity-img" :src="item.cover || '/static/images/default-activity.png'" mode="aspectFill" />
          <view class="activity-status-badge" :class="getStatusClass(item.status)">{{ item.status }}</view>
        </view>
        <view class="activity-info">
          <view class="activity-header">
            <text class="activity-title">{{ item.title }}</text>
          </view>
          <view class="activity-meta">
            <text class="meta-item">
              <image src="/static/icons/position.png" mode="aspectFit" style="width:24rpx;height:24rpx;margin-right:4rpx;" />
              {{ item.location }}
            </text>
            <text class="meta-item">
              <image src="/static/icons/time.png" mode="aspectFit" style="width:24rpx;height:24rpx;margin-right:4rpx;" />
              {{ item.startTime }}
            </text>
          </view>
          <text class="activity-desc">{{ (item.description || '').slice(0, 60) }}...</text>
          <view class="activity-footer">
            <text class="participants-info">
              已报名 {{ item.currentParticipants || 0 }}/{{ item.maxParticipants || 0 }}
            </text>
            <view class="btn-signup" v-if="item.status === '报名中'">立即报名</view>
            <view class="btn-disabled" v-else>{{ item.status }}</view>
          </view>
        </view>
      </view>
    </view>

    <view class="empty-state" v-if="!loading && activities.length === 0">
      <image src="/static/images/empty.png" mode="aspectFit" style="width:200rpx;height:200rpx;" />
      <text>暂无活动</text>
    </view>

    <view style="height: 40rpx;"></view>
  </view>
</template>

<script>
import { activityApi } from '../../utils/api.js'

export default {
  data() {
    return {
      activities: [],
      loading: true,
      keyword: '',
      activeFilter: 'all',
      filters: [
        { id: 'all', label: '全部' },
        { id: '报名中', label: '报名中' },
        { id: '进行中', label: '进行中' },
        { id: '已结束', label: '已结束' }
      ]
    }
  },
  onLoad() {
    this.loadActivities()
  },
  methods: {
    async loadActivities() {
      try {
        this.loading = true
        const status = this.activeFilter === 'all' ? '' : this.activeFilter
        const res = await activityApi.getList(1, 100, status)
        this.activities = res.records || res.list || []
      } catch (error) {
        console.error('加载活动失败:', error)
        uni.showToast({ title: '加载失败', icon: 'none' })
      } finally {
        this.loading = false
      }
    },
    changeFilter(filterId) {
      this.activeFilter = filterId
      this.loadActivities()
    },
    goDetail(id) {
      uni.navigateTo({ url: `/pages/activity/detail?id=${id}` })
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
.list-page {
  min-height: 100vh;
  background: linear-gradient(180deg, #f8f9fa 0%, #ffffff 100%);
  padding: 20rpx;
}

.search-bar {
  display: flex;
  align-items: center;
  background: #ffffff;
  border-radius: 50rpx;
  padding: 20rpx 30rpx;
  margin-bottom: 20rpx;
  box-shadow: 0 4rpx 12rpx rgba(0, 0, 0, 0.05);
}

.search-icon {
  width: 32rpx;
  height: 32rpx;
  margin-right: 16rpx;
}

.search-input {
  flex: 1;
  font-size: 28rpx;
  color: #333;
}

.filter-bar {
  display: flex;
  gap: 16rpx;
  margin-bottom: 24rpx;
  overflow-x: auto;
  white-space: nowrap;
}

.filter-item {
  padding: 12rpx 28rpx;
  background: #ffffff;
  border-radius: 40rpx;
  font-size: 26rpx;
  color: #666;
  transition: all 0.3s;
  box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.05);
}

.filter-item.active {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: #ffffff;
  font-weight: 600;
}

.activity-list {
  display: flex;
  flex-direction: column;
  gap: 24rpx;
}

.activity-card {
  background: #ffffff;
  border-radius: 20rpx;
  overflow: hidden;
  transition: transform 0.3s;
}

.card-shadow {
  box-shadow: 0 4rpx 16rpx rgba(0, 0, 0, 0.08);
}

.activity-img-wrap {
  position: relative;
  width: 100%;
  height: 360rpx;
}

.activity-img {
  width: 100%;
  height: 100%;
}

.activity-status-badge {
  position: absolute;
  top: 20rpx;
  right: 20rpx;
  padding: 8rpx 20rpx;
  border-radius: 30rpx;
  font-size: 24rpx;
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

.activity-info {
  padding: 24rpx;
}

.activity-header {
  margin-bottom: 16rpx;
}

.activity-title {
  font-size: 32rpx;
  font-weight: 600;
  color: #333;
}

.activity-meta {
  display: flex;
  flex-direction: column;
  gap: 12rpx;
  margin-bottom: 16rpx;
}

.meta-item {
  display: flex;
  align-items: center;
  font-size: 24rpx;
  color: #666;
}

.activity-desc {
  font-size: 26rpx;
  color: #999;
  line-height: 1.6;
  display: block;
  margin-bottom: 20rpx;
}

.activity-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.participants-info {
  font-size: 26rpx;
  color: #667eea;
  font-weight: 600;
}

.btn-signup {
  padding: 12rpx 32rpx;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: #ffffff;
  border-radius: 40rpx;
  font-size: 26rpx;
  font-weight: 600;
}

.btn-disabled {
  padding: 12rpx 32rpx;
  background: #e0e0e0;
  color: #999;
  border-radius: 40rpx;
  font-size: 26rpx;
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 120rpx 0;
  color: #999;
  font-size: 28rpx;
}
</style>
