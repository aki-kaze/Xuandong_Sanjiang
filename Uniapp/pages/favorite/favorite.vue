<template>
  <view class="favorite-page">
    <view class="tab-bar">
      <scroll-view scroll-x class="tab-scroll">
        <view class="tab-inner">
          <view class="tab-item" :class="{ active: activeTab === tab.key }" v-for="tab in tabs" :key="tab.key" @tap="activeTab = tab.key">
            <text class="tab-label">{{ tab.label }}</text>
            <text class="tab-count" v-if="getCount(tab.key) > 0">{{ getCount(tab.key) }}</text>
          </view>
        </view>
      </scroll-view>
    </view>

    <view class="fav-list" v-if="displayList.length > 0">
      <view class="fav-card" v-for="item in displayList" :key="item.id + '-' + item.type" @tap="goDetail(item)">
        <view class="fav-cover-wrap">
          <image v-if="item.cover" class="fav-cover" :src="item.cover" mode="aspectFill" />
          <view v-else class="fav-cover-placeholder" :style="{ background: placeholderColor(item.type) }">
            <image class="fav-cover-icon" :src="getTypeIcon(item.type)" mode="aspectFit" />
          </view>
        </view>
        <view class="fav-info">
          <view class="fav-top">
            <text class="fav-name">{{ item.name }}</text>
            <view class="fav-type-tag" :style="{ background: tagColor(item.type) }">
              <text class="fav-type-text">{{ typeLabel(item.type) }}</text>
            </view>
          </view>
          <text class="fav-desc" v-if="item.desc">{{ item.desc }}</text>
          <view class="fav-bottom">
            <text class="fav-price" v-if="item.price && item.price > 0">¥{{ item.price }}</text>
            <text class="fav-price free" v-else-if="item.type === 'attraction'">免费</text>
            <text class="fav-time">{{ formatTime(item.createTime) }}</text>
            <view class="fav-del-btn" @tap.stop="removeItem(item)">
              <text class="del-text">取消收藏</text>
            </view>
          </view>
        </view>
      </view>
    </view>

    <view class="empty-state" v-else>
      <text class="empty-icon">💝</text>
      <text class="empty-title">暂无{{ typeLabel(activeTab) }}</text>
      <text class="empty-sub">去发现更多精彩内容吧</text>
      <view class="empty-btn" @tap="goExplore">去探索</view>
    </view>
  </view>
</template>

<script>
import { favoritesUtil } from '../../utils/favorites.js'

export default {
  data() {
    return {
      activeTab: 'attraction',
      favorites: [],
      tabs: [
        { key: 'attraction', label: '景点' },
        { key: 'hotel', label: '酒店' },
        { key: 'food', label: '美食' },
        { key: 'guide', label: '攻略' },
        { key: 'post', label: '动态' },
        { key: 'shop', label: '商品' }
      ]
    }
  },
  computed: {
    displayList() {
      return this.favorites.filter(f => f.type === this.activeTab)
    }
  },
  onShow() {
    this.favorites = favoritesUtil.getAllFavorites()
  },
  methods: {
    getCount(type) {
      return this.favorites.filter(f => f.type === type).length
    },
    getTypeIcon(type) {
      const icons = {
        attraction: '/static/icons/detail.png',
        hotel: '/static/icons/hotel.png',
        food: '/static/icons/food.png',
        guide: '/static/icons/detail.png',
        post: '/static/icons/discover.png',
        shop: '/static/icons/shop.png'
      }
      return icons[type] || '/static/icons/love.png'
    },
    typeLabel(type) {
      const labels = {
        attraction: '景点',
        hotel: '酒店',
        food: '美食',
        guide: '攻略',
        post: '动态',
        shop: '商品'
      }
      return labels[type] || '收藏'
    },
    tagColor(type) {
      const colors = {
        attraction: 'rgba(29,59,90,0.1)',
        hotel: 'rgba(74,144,226,0.12)',
        food: 'rgba(232,115,74,0.12)',
        guide: 'rgba(212,163,115,0.15)',
        post: 'rgba(103,194,58,0.12)',
        shop: 'rgba(163,59,50,0.1)'
      }
      return colors[type] || 'rgba(0,0,0,0.05)'
    },
    placeholderColor(type) {
      const colors = {
        attraction: '#E8F0F8',
        hotel: '#EBF5FF',
        food: '#FFF4EC',
        guide: '#FDF6E3',
        post: '#F0FFF0',
        shop: '#FFF0F0'
      }
      return colors[type] || '#F5F5F5'
    },
    formatTime(timestamp) {
      if (!timestamp) return ''
      const now = Date.now()
      const diff = now - timestamp
      if (diff < 60000) return '刚刚'
      if (diff < 3600000) return Math.floor(diff / 60000) + '分钟前'
      if (diff < 86400000) return Math.floor(diff / 3600000) + '小时前'
      if (diff < 2592000000) return Math.floor(diff / 86400000) + '天前'
      const d = new Date(timestamp)
      return (d.getMonth() + 1) + '月' + d.getDate() + '日'
    },
    goDetail(item) {
      if (item.type === 'attraction') {
        uni.navigateTo({ url: '/pages/attraction/detail?id=' + item.id })
      } else if (item.type === 'hotel') {
        uni.navigateTo({ url: '/pages/hotel/detail?id=' + item.id })
      } else if (item.type === 'food') {
        uni.navigateTo({ url: '/pages/food/detail?id=' + item.id })
      } else if (item.type === 'guide') {
        uni.navigateTo({ url: '/pages/discover/guide-detail?id=' + item.id })
      } else if (item.type === 'post') {
        uni.navigateTo({ url: '/pages/discover/detail?id=' + item.id })
      } else {
        uni.navigateTo({ url: '/pages/shop/shop' })
      }
    },
    removeItem(item) {
      uni.showModal({
        title: '取消收藏',
        content: '确定取消收藏"' + item.name + '"吗？',
        success: (res) => {
          if (res.confirm) {
            favoritesUtil.removeFavorite(item.id, item.type)
            this.favorites = favoritesUtil.getAllFavorites()
          }
        }
      })
    },
    goExplore() { uni.switchTab({ url: '/pages/index/index' }) }
  }
}
</script>

<style scoped lang="scss">
.favorite-page {
  min-height: 100vh;
  background: #F8F9FA;
}

/* ===== Tab 栏 ===== */
.tab-bar {
  background: #FDFCF8;
  border-bottom: 1rpx solid rgba(0,0,0,0.04);
}
.tab-scroll {
  white-space: nowrap;
}
.tab-inner {
  display: flex;
  padding: 0 12rpx;
}
.tab-item {
  display: flex;
  align-items: center;
  gap: 8rpx;
  padding: 28rpx 24rpx;
  font-size: 28rpx;
  color: #999;
  position: relative;
  flex-shrink: 0;
}
.tab-item.active {
  color: #1D3B5A;
  font-weight: bold;
}
.tab-item.active::after {
  content: '';
  position: absolute;
  bottom: 0;
  left: 50%;
  transform: translateX(-50%);
  width: 36rpx;
  height: 6rpx;
  background: linear-gradient(135deg, #D4A373, #D4B873);
  border-radius: 3rpx;
}
.tab-count {
  font-size: 20rpx;
  color: #fff;
  background: #A33B32;
  min-width: 32rpx;
  height: 32rpx;
  line-height: 32rpx;
  text-align: center;
  border-radius: 16rpx;
  padding: 0 8rpx;
}
.tab-item.active .tab-count {
  background: #1D3B5A;
}

/* ===== 收藏列表 ===== */
.fav-list {
  padding: 20rpx 24rpx;
  display: flex;
  flex-direction: column;
  gap: 20rpx;
}

.fav-card {
  background: #FDFCF8;
  border-radius: 20rpx;
  overflow: hidden;
  display: flex;
  box-shadow: 0 4rpx 16rpx rgba(29,59,90,0.06);
  transition: transform 0.2s;
}
.fav-card:active {
  transform: scale(0.98);
}

/* 封面图 */
.fav-cover-wrap {
  width: 220rpx;
  height: 220rpx;
  flex-shrink: 0;
  overflow: hidden;
}
.fav-cover {
  width: 100%;
  height: 100%;
}
.fav-cover-placeholder {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
}
.fav-cover-icon {
  width: 64rpx;
  height: 64rpx;
  opacity: 0.5;
}

/* 信息区 */
.fav-info {
  flex: 1;
  padding: 22rpx 24rpx;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  min-width: 0;
}

.fav-top {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 12rpx;
  margin-bottom: 10rpx;
}

.fav-name {
  font-size: 30rpx;
  font-weight: bold;
  color: #1D3B5A;
  flex: 1;
  min-width: 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.fav-type-tag {
  flex-shrink: 0;
  padding: 4rpx 14rpx;
  border-radius: 8rpx;
}
.fav-type-text {
  font-size: 20rpx;
  color: #1D3B5A;
  font-weight: 500;
}

.fav-desc {
  font-size: 24rpx;
  color: #999;
  line-height: 1.6;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  margin-bottom: 12rpx;
}

.fav-bottom {
  display: flex;
  align-items: center;
  gap: 16rpx;
}

.fav-price {
  font-size: 30rpx;
  font-weight: bold;
  color: #E8734A;
}
.fav-price.free {
  font-size: 24rpx;
  color: #67C23A;
  font-weight: 500;
}

.fav-time {
  font-size: 22rpx;
  color: #ccc;
  flex: 1;
}

.fav-del-btn {
  padding: 8rpx 20rpx;
  border: 1rpx solid #eee;
  border-radius: 24rpx;
  flex-shrink: 0;
}
.fav-del-btn:active {
  background: #f5f5f5;
}
.del-text {
  font-size: 22rpx;
  color: #999;
}

/* ===== 空状态 ===== */
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 160rpx 0;
}
.empty-icon {
  font-size: 80rpx;
  margin-bottom: 24rpx;
}
.empty-title {
  font-size: 32rpx;
  color: #1D3B5A;
  font-weight: bold;
  margin-bottom: 12rpx;
}
.empty-sub {
  font-size: 26rpx;
  color: #999;
  margin-bottom: 48rpx;
}
.empty-btn {
  background: linear-gradient(135deg, #1D3B5A, #2E5F8E);
  color: #FDFCF8;
  font-size: 28rpx;
  padding: 22rpx 64rpx;
  border-radius: 50rpx;
  font-weight: 500;
}
</style>
