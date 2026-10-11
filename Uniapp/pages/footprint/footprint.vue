<template>
  <view class="footprint-page">
    <!-- 页面头部 -->
    <view class="header-section">
      <view class="header-bg">
        <image class="header-image" src="/static/icons/footprint.png" mode="aspectFit" />
      </view>
      <view class="header-info">
        <text class="header-title">我的足迹</text>
        <text class="header-subtitle">记录您的每一次探索</text>
      </view>
      <view class="header-stats">
        <text class="stats-num">{{ totalCount }}</text>
        <text class="stats-label">次访问</text>
      </view>
    </view>

    <!-- 足迹列表 -->
    <view class="footprint-list" v-if="footprintList.length > 0">
      <view class="list-header">
        <text class="list-title">最近访问</text>
        <text class="list-clear" @tap.stop="clearFootprint">清空足迹</text>
      </view>
      
      <view class="footprint-item" v-for="(item, index) in footprintList" :key="item.id + '_' + item.type" @tap="goDetail(item)">
        <view class="item-left">
          <image v-if="item.cover" class="item-cover" :src="item.cover" mode="aspectFill" lazy-load />
          <view v-else class="item-cover-placeholder" :style="{ background: getPlaceholderColor(item.type) }">
            <image class="placeholder-icon" :src="getTypeIcon(item.type)" mode="aspectFit" />
          </view>
          <view class="item-type-tag" :style="{ background: getTypeColor(item.type) }">
            {{ getTypeName(item.type) }}
          </view>
        </view>
        <view class="item-right">
          <text class="item-name">{{ item.name }}</text>
          <view class="item-meta">
            <text class="item-location">
              <image class="meta-icon" src="/static/icons/position.png" mode="aspectFit" />
              {{ item.location || '三江侗族自治县' }}
            </text>
          </view>
          <view class="item-meta">
            <text class="item-time">
              <image class="meta-icon" src="/static/icons/time.png" mode="aspectFit" />
              {{ item.visitTime }}
            </text>
          </view>
          <view class="item-visits" v-if="item.visitCount && item.visitCount > 1">
            <text class="visits-text">访问 {{ item.visitCount }} 次</text>
          </view>
        </view>
      </view>
    </view>

    <!-- 空状态 -->
    <view class="empty-state" v-else>
      <image class="empty-image" src="/static/icons/footprint.png" mode="aspectFit" />
      <text class="empty-title">暂无足迹</text>
      <text class="empty-subtitle">快去探索三江的美景吧</text>
      <view class="empty-btn" @tap="goAttractions">去逛逛</view>
    </view>

    <!-- 底部提示 -->
    <view class="footer-tip">
      <text>足迹数据仅保存在本地，清空后无法恢复</text>
    </view>
  </view>
</template>

<script>
import { attractionApi } from '../../utils/api.js'

export default {
  data() {
    return {
      totalCount: 0,
      footprintList: [],
      defaultCovers: {
        attraction: '/static/images/attraction-default.jpg',
        food: '/static/images/food-default.jpg',
        hotel: '/static/images/hotel-default.jpg',
        shop: '/static/images/shop-default.jpg'
      }
    }
  },
  onShow() {
    this.loadFootprint()
  },
  methods: {
    // 加载足迹数据
    loadFootprint() {
      // 获取访问总次数
      this.totalCount = uni.getStorageSync('footprint') || 0

      // 获取足迹列表（从本地存储）
      const footprintData = uni.getStorageSync('footprintList') || []

      console.log('加载足迹数据，总数:', this.totalCount, '列表长度:', footprintData.length)

      // 格式化足迹数据
      this.footprintList = footprintData.map(item => {
        console.log('足迹项:', item.name, 'cover:', item.cover, 'visitCount:', item.visitCount)
        return {
          id: item.id,
          name: item.name,
          type: item.type || 'attraction',
          cover: item.cover,
          location: item.location,
          visitTime: this.formatTime(item.visitTime || item.createTime),
          visitCount: item.visitCount || 1,
          lat: item.lat,
          lng: item.lng
        }
      }).sort((a, b) => {
        // 按访问时间倒序排列
        return new Date(b.visitTime) - new Date(a.visitTime)
      })

      console.log('格式化后的足迹列表:', this.footprintList)
    },
    
    // 格式化时间
    formatTime(timestamp) {
      if (!timestamp) return '未知时间'
      
      const date = new Date(timestamp)
      const now = new Date()
      const diff = now - date
      
      // 今天
      if (diff < 24 * 60 * 60 * 1000) {
        const hour = String(date.getHours()).padStart(2, '0')
        const minute = String(date.getMinutes()).padStart(2, '0')
        return `今天 ${hour}:${minute}`
      }
      
      // 昨天
      if (diff < 48 * 60 * 60 * 1000) {
        const hour = String(date.getHours()).padStart(2, '0')
        const minute = String(date.getMinutes()).padStart(2, '0')
        return `昨天 ${hour}:${minute}`
      }
      
      // 其他日期
      const month = String(date.getMonth() + 1).padStart(2, '0')
      const day = String(date.getDate()).padStart(2, '0')
      return `${month}-${day}`
    },

    // 获取默认封面
    getDefaultCover(item) {
      const defaultCovers = {
        attraction: '/static/images/attraction-default.jpg',
        food: '/static/images/food-default.jpg',
        hotel: '/static/images/hotel-default.jpg',
        shop: '/static/images/shop-default.jpg'
      }
      return defaultCovers[item.type] || defaultCovers.attraction
    },

    // 获取类型图标
    getTypeIcon(type) {
      const icons = {
        attraction: '/static/icons/detail.png',
        hotel: '/static/icons/hotel.png',
        food: '/static/icons/food.png',
        shop: '/static/icons/shop.png'
      }
      return icons[type] || '/static/icons/detail.png'
    },

    // 获取占位符背景色
    getPlaceholderColor(type) {
      const colors = {
        attraction: '#E8F0F8',
        hotel: '#EBF5FF',
        food: '#FFF4EC',
        shop: '#FFF0F0'
      }
      return colors[type] || '#F5F5F5'
    },

    // 获取类型名称
    getTypeName(type) {
      const map = {
        attraction: '景点',
        food: '美食',
        hotel: '酒店',
        shop: '购物'
      }
      return map[type] || '景点'
    },
    
    // 获取类型颜色
    getTypeColor(type) {
      const map = {
        attraction: 'rgba(74,144,226,0.12)',
        food: 'rgba(255,153,0,0.12)',
        hotel: 'rgba(102,187,106,0.12)',
        shop: 'rgba(255,102,153,0.12)'
      }
      return map[type] || 'rgba(74,144,226,0.12)'
    },
    
    // 跳转详情页
    goDetail(item) {
      const pathMap = {
        attraction: '/pages/attraction/detail',
        food: '/pages/food/detail',
        hotel: '/pages/hotel/detail',
        shop: '/pages/shop/detail'
      }

      const path = pathMap[item.type] || pathMap.attraction
      uni.navigateTo({
        url: `${path}?id=${item.id}`
      })
    },
    
    // 清空足迹
    clearFootprint() {
      uni.showModal({
        title: '提示',
        content: '确定清空所有足迹记录？此操作不可恢复。',
        success: (res) => {
          if (res.confirm) {
            uni.setStorageSync('footprintList', [])
            uni.setStorageSync('footprint', 0)
            this.totalCount = 0
            this.footprintList = []
            uni.showToast({
              title: '已清空',
              icon: 'success'
            })
          }
        }
      })
    },
    
    // 添加足迹记录（供其他页面调用）
    addFootprint(data) {
      const footprintList = uni.getStorageSync('footprintList') || []

      // 检查是否已存在（同一个id和type）
      const existingIndex = footprintList.findIndex(item =>
        item.id === data.id && item.type === data.type
      )

      if (existingIndex !== -1) {
        // 已存在，更新访问时间和访问次数
        footprintList[existingIndex].visitTime = Date.now()
        footprintList[existingIndex].visitCount = (footprintList[existingIndex].visitCount || 1) + 1

        // 移到列表最前面
        const item = footprintList.splice(existingIndex, 1)[0]
        footprintList.unshift(item)
      } else {
        // 不存在，添加新记录
        footprintList.unshift({
          id: data.id,
          name: data.name,
          type: data.type || 'attraction',
          cover: data.cover,
          location: data.location,
          lat: data.lat,
          lng: data.lng,
          createTime: Date.now(),
          visitTime: Date.now(),
          visitCount: 1
        })

        // 更新总访问次数（只在新增时增加）
        let totalCount = uni.getStorageSync('footprint') || 0
        totalCount++
        uni.setStorageSync('footprint', totalCount)
      }

      // 只保留最近 100 条
      uni.setStorageSync('footprintList', footprintList.slice(0, 100))

      // 如果当前页面已打开，刷新数据
      this.loadFootprint()
    },
    
    // 去景点列表
    goAttractions() {
      uni.switchTab({
        url: '/pages/index/index'
      })
    }
  }
}
</script>

<style scoped lang="scss">
.footprint-page {
  min-height: 100vh;
  background: #F8FAFC;
  padding-bottom: 40rpx;
}

/* ===== 头部区域 ===== */
.header-section {
  background: linear-gradient(135deg, #1D3B5A 0%, #2E5F8E 50%, #1D3B5A 100%);
  padding: 50rpx 30rpx 80rpx;
  border-radius: 0 0 50rpx 50rpx;
  display: flex;
  flex-direction: column;
  align-items: center;
  position: relative;
  overflow: hidden;
  box-shadow: 0 8rpx 32rpx rgba(29, 59, 90, 0.3);
}

.header-bg {
  position: absolute;
  top: -30rpx;
  right: -30rpx;
  width: 240rpx;
  height: 240rpx;
  opacity: 0.08;
  transform: rotate(15deg);
}

.header-image {
  width: 100%;
  height: 100%;
}

.header-info {
  display: flex;
  flex-direction: column;
  align-items: center;
  margin-bottom: 20rpx;
  position: relative;
  z-index: 1;
}

.header-title {
  font-size: 40rpx;
  font-weight: 600;
  color: #FFFFFF;
  margin-bottom: 8rpx;
}

.header-subtitle {
  font-size: 24rpx;
  color: rgba(255, 255, 255, 0.8);
}

.header-stats {
  display: flex;
  flex-direction: column;
  align-items: center;
  position: relative;
  z-index: 1;
}

.stats-num {
  font-size: 64rpx;
  font-weight: 700;
  color: #FFFFFF;
  line-height: 1;
  text-shadow: 0 4rpx 12rpx rgba(0, 0, 0, 0.2);
}

.stats-label {
  font-size: 24rpx;
  color: rgba(255, 255, 255, 0.8);
  margin-top: 8rpx;
}

/* ===== 足迹列表 ===== */
.footprint-list {
  padding: 0 24rpx;
  margin-top: -30rpx;
}

.list-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20rpx 0;
  margin-bottom: 16rpx;
}

.list-title {
  font-size: 30rpx;
  font-weight: 600;
  color: #2C3E50;
}

.list-clear {
  font-size: 24rpx;
  color: #94A3B8;
  padding: 8rpx 16rpx;
  background: #F1F5F9;
  border-radius: 20rpx;
}

.footprint-item {
  background: #FFFFFF;
  border-radius: 24rpx;
  padding: 24rpx;
  margin-bottom: 24rpx;
  display: flex;
  gap: 20rpx;
  box-shadow: 0 6rpx 24rpx rgba(74, 144, 226, 0.1);
  transition: all 0.3s ease;
}

.footprint-item:active {
  transform: scale(0.98);
  box-shadow: 0 4rpx 16rpx rgba(74, 144, 226, 0.08);
}

.item-left {
  position: relative;
  flex-shrink: 0;
}

.item-cover {
  width: 200rpx;
  height: 200rpx;
  border-radius: 20rpx;
  background: #F1F5F9;
  box-shadow: 0 4rpx 12rpx rgba(0, 0, 0, 0.08);
}

.item-cover-placeholder {
  width: 200rpx;
  height: 200rpx;
  border-radius: 20rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 4rpx 12rpx rgba(0, 0, 0, 0.08);
}

.placeholder-icon {
  width: 80rpx;
  height: 80rpx;
  opacity: 0.3;
}

.item-type-tag {
  position: absolute;
  top: 12rpx;
  left: 12rpx;
  padding: 6rpx 12rpx;
  border-radius: 12rpx;
  font-size: 20rpx;
  color: #4A90E2;
  font-weight: 500;
}

.item-right {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  overflow: hidden;
}

.item-name {
  font-size: 30rpx;
  font-weight: 600;
  color: #2C3E50;
  margin-bottom: 8rpx;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  line-height: 1.4;
}

.item-meta {
  display: flex;
  align-items: center;
  gap: 8rpx;
  margin-bottom: 6rpx;
}

.meta-icon {
  width: 24rpx;
  height: 24rpx;
  opacity: 0.6;
}

.item-location,
.item-time {
  font-size: 22rpx;
  color: #64748B;
  display: flex;
  align-items: center;
  gap: 4rpx;
}

.item-visits {
  margin-top: 8rpx;
  display: inline-block;
}

.visits-text {
  font-size: 22rpx;
  color: #4A90E2;
  background: rgba(74, 144, 226, 0.1);
  padding: 4rpx 12rpx;
  border-radius: 12rpx;
  font-weight: 500;
}

/* ===== 空状态 ===== */
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 120rpx 30rpx;
}

.empty-image {
  width: 200rpx;
  height: 200rpx;
  margin-bottom: 40rpx;
  opacity: 0.5;
}

.empty-title {
  font-size: 32rpx;
  font-weight: 600;
  color: #94A3B8;
  margin-bottom: 16rpx;
}

.empty-subtitle {
  font-size: 26rpx;
  color: #CBD5E1;
  margin-bottom: 40rpx;
}

.empty-btn {
  background: linear-gradient(135deg, #4A90E2 0%, #2E5F8E 100%);
  color: #FFFFFF;
  font-size: 28rpx;
  padding: 24rpx 64rpx;
  border-radius: 44rpx;
  font-weight: 600;
  box-shadow: 0 8rpx 24rpx rgba(74, 144, 226, 0.35);
}

/* ===== 底部提示 ===== */
.footer-tip {
  text-align: center;
  padding: 30rpx;
  font-size: 22rpx;
  color: #94A3B8;
}
</style>
