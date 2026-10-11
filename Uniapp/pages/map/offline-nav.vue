<template>
  <view class="offline-nav-page">
    <!-- 头部 -->
    <view class="nav-header">
      <view class="back-btn" @tap="goBack">
        <text class="back-icon">
          <uv-icon name="arrow-left" size="24" color="#333"></uv-icon>
        </text>
      </view>
      <text class="header-title">离线导航</text>
      <view class="offline-badge">
        <text class="offline-dot"></text>
        <text class="offline-text">离线</text>
      </view>
    </view>

    <scroll-view class="content" scroll-y>
      <!-- 导航输入 -->
      <view class="nav-input-card">
        <view class="input-row">
          <view class="input-dot start"></view>
          <view class="input-wrap" @tap="selectPoint('start')">
            <text class="input-text" :class="{ placeholder: !startPoint.name }">
              {{ startPoint.name || '选择起点（默认当前位置）' }}
            </text>
          </view>
        </view>
        <view class="input-divider">
          <view class="divider-line"></view>
          <view class="swap-btn" @tap="swapPoints">
            <text class="swap-icon">↑</text>
            <text class="swap-icon">↓</text>
          </view>
        </view>
        <view class="input-row">
          <view class="input-dot end"></view>
          <view class="input-wrap" @tap="selectPoint('end')">
            <text class="input-text" :class="{ placeholder: !endPoint.name }">
              {{ endPoint.name || '选择目的地' }}
            </text>
          </view>
        </view>
      </view>

      <!-- 导航方式选择 -->
      <view class="mode-card" v-if="routeInfo">
        <view
          class="mode-item"
          v-for="mode in navModes"
          :key="mode.id"
          :class="{ active: activeMode === mode.id }"
          @tap="activeMode = mode.id"
        >
          <image class="mode-icon-img" :src="mode.icon" mode="aspectFit" />
          <text class="mode-label">{{ mode.label }}</text>
          <text class="mode-time">{{ mode.id === 'walk' ? routeInfo.walk.time : routeInfo.drive.time }}</text>
        </view>
      </view>

      <!-- 导航结果 -->
      <view class="result-card" v-if="routeInfo">
        <view class="result-header">
          <text class="result-title">导航概览</text>
          <text class="result-distance">{{ routeInfo.distanceText }}</text>
        </view>

        <view class="result-info-list">
          <view class="result-info-row">
            <image class="info-icon-img" src="/static/icons/explore.png" mode="aspectFit" />
            <text class="info-label">方向</text>
            <text class="info-value">{{ routeInfo.direction }}</text>
          </view>
          <view class="result-info-row">
            <image class="info-icon-img" src="/static/icons/distance.png" mode="aspectFit" />
            <text class="info-label">距离</text>
            <text class="info-value">{{ routeInfo.distanceText }}</text>
          </view>
          <view class="result-info-row" v-if="activeMode === 'walk'">
            <image class="info-icon-img" src="/static/icons/walk.png" mode="aspectFit" />
            <text class="info-label">步行</text>
            <text class="info-value">{{ routeInfo.walk.time }}</text>
          </view>
          <view class="result-info-row" v-if="activeMode === 'drive'">
            <image class="info-icon-img" src="/static/icons/drive.png" mode="aspectFit" />
            <text class="info-label">驾车</text>
            <text class="info-value">{{ routeInfo.drive.time }}</text>
          </view>
          <view class="result-info-row" v-if="activeMode === 'drive'">
            <image class="info-icon-img" src="/static/icons/cost.png" mode="aspectFit" />
            <text class="info-label">预估费用</text>
            <text class="info-value">约¥{{ routeInfo.drive.cost }}</text>
          </view>
        </view>

        <!-- 导航提示 -->
        <view class="tips-section">
          <text class="tips-title">💡 导航提示</text>
          <view class="tip-item" v-for="(tip, idx) in routeInfo.tips" :key="idx">
            <text class="tip-text">{{ tip }}</text>
          </view>
        </view>
      </view>

      <!-- 附近景点 -->
      <view class="nearby-section" v-if="nearbyList.length > 0">
        <view class="section-title-row">
          <view class="accent-bar"></view>
          <text class="section-title">附近景点</text>
        </view>
        <view class="nearby-list">
          <view class="nearby-item" v-for="item in nearbyList" :key="item.id" @tap="quickNav(item)">
            <view class="nearby-info">
              <text class="nearby-name">{{ item.name }}</text>
              <text class="nearby-dist">{{ item.distanceText }} · 步行{{ item.walkTime }}</text>
            </view>
            <image class="nav-arrow-img" src="/static/icons/explore.png" mode="aspectFit" />
          </view>
        </view>
      </view>

      <!-- 搜索面板 -->
      <view class="search-panel" v-if="showSearch">
        <view class="search-header">
          <text class="search-title">{{ searchTarget === 'start' ? '选择起点' : '选择目的地' }}</text>
          <text class="search-close" @tap="showSearch = false">关闭</text>
        </view>
        <view class="search-input-wrap">
          <input class="search-input" v-model="searchKeyword" placeholder="搜索景点名称..." @input="onSearchInput" focus />
        </view>
        <view class="search-results">
          <view class="search-item" v-for="item in searchResults" :key="item.id" @tap="confirmPoint(item)">
            <text class="search-item-name">{{ item.name }}</text>
            <text class="search-item-addr">{{ item.address }}</text>
          </view>
          <view class="search-item current-loc" @tap="useCurrentLocation" v-if="searchTarget === 'start'">
            <view class="search-item-name">
              <image class="loc-icon" src="/static/icons/position.png" mode="aspectFit" />
              <text> 使用当前位置</text>
            </view>
            <text class="search-item-addr">基于GPS定位</text>
          </view>
          <view class="search-empty" v-if="searchKeyword && searchResults.length === 0">
            <text>未找到相关景点</text>
          </view>
        </view>
      </view>
    </scroll-view>

    <!-- 底部操作 -->
    <view class="bottom-bar" v-if="routeInfo && !showSearch">
      <view class="summary-text">
        <text>{{ startPoint.name || '起点' }} → {{ endPoint.name || '终点' }}</text>
        <text class="summary-dist">{{ routeInfo.distanceText }}</text>
      </view>
      <view class="open-map-btn" @tap="openSystemMap">
        <text>打开系统地图</text>
      </view>
    </view>
  </view>
</template>

<script>
import offlineMap from '../../utils/offline-map.js'

export default {
  data() {
    return {
      startPoint: { name: '', lat: 0, lng: 0 },
      endPoint: { name: '', lat: 0, lng: 0 },
      routeInfo: null,
      activeMode: 'drive',
      navModes: [
        { id: 'walk', icon: '/static/icons/walk.png', label: '步行' },
        { id: 'drive', icon: '/static/icons/drive.png', label: '驾车' }
      ],
      nearbyList: [],
      showSearch: false,
      searchTarget: 'end',
      searchKeyword: '',
      searchResults: []
    }
  },
  onLoad(options) {
    // 如果传入了目的地参数
    if (options.toName) {
      const attraction = offlineMap.findAttraction(options.toName)
      if (attraction) {
        this.endPoint = { name: attraction.name, lat: attraction.lat, lng: attraction.lng }
        this.calcRoute()
      }
    }
    this.loadNearby()
  },
  methods: {
    goBack() {
      uni.navigateBack()
    },
    loadNearby() {
      // 获取当前位置附近景点
      try {
        uni.getLocation({
          type: 'gcj02',
          success: (res) => {
            this.startPoint = { name: '当前位置', lat: res.latitude, lng: res.longitude }
            this.nearbyList = offlineMap.getNearbyAttractions(res.latitude, res.longitude, 8)
            if (this.endPoint.name) this.calcRoute()
          },
          fail: () => {
            // 定位失败，使用默认位置
            this.startPoint = { name: '三江县城', lat: 25.7834, lng: 109.6078 }
            this.nearbyList = offlineMap.getNearbyAttractions(25.7834, 109.6078, 8)
          }
        })
      } catch (e) {
        this.startPoint = { name: '三江县城', lat: 25.7834, lng: 109.6078 }
        this.nearbyList = offlineMap.getNearbyAttractions(25.7834, 109.6078, 8)
      }
    },
    selectPoint(type) {
      this.searchTarget = type
      this.searchKeyword = ''
      this.searchResults = offlineMap.getAllAttractions().slice(0, 10)
      this.showSearch = true
    },
    onSearchInput() {
      if (!this.searchKeyword.trim()) {
        this.searchResults = offlineMap.getAllAttractions().slice(0, 10)
        return
      }
      this.searchResults = offlineMap.searchAttractions(this.searchKeyword)
    },
    confirmPoint(item) {
      const point = { name: item.name, lat: item.lat, lng: item.lng }
      if (this.searchTarget === 'start') {
        this.startPoint = point
      } else {
        this.endPoint = point
      }
      this.showSearch = false
      this.searchKeyword = ''
      this.calcRoute()
    },
    useCurrentLocation() {
      this.startPoint = { name: '当前位置', lat: 25.7834, lng: 109.6078 }
      this.showSearch = false
      this.calcRoute()
    },
    swapPoints() {
      const temp = { ...this.startPoint }
      this.startPoint = { ...this.endPoint }
      this.endPoint = temp
      this.calcRoute()
    },
    calcRoute() {
      if (!this.startPoint.lat || !this.endPoint.lat) return
      this.routeInfo = offlineMap.calcRoute(this.startPoint, this.endPoint)
    },
    quickNav(item) {
      this.endPoint = { name: item.name, lat: item.lat, lng: item.lng }
      this.calcRoute()
    },
    openSystemMap() {
      uni.openLocation({
        latitude: this.endPoint.lat,
        longitude: this.endPoint.lng,
        name: this.endPoint.name,
        fail: () => { uni.showToast({ title: '打开地图失败', icon: 'none' }) }
      })
    }
  }
}
</script>

<style scoped lang="scss">
.offline-nav-page { min-height: 100vh; background: #F8F9FA; display: flex; flex-direction: column; }

.nav-header { background: linear-gradient(135deg, #1D3B5A, #2E5F8E); padding: 60rpx 24rpx 24rpx; display: flex; align-items: center; gap: 20rpx; }
.back-btn { width: 60rpx; height: 60rpx; background: rgba(255,255,255,0.15); border-radius: 50%; display: flex; align-items: center; justify-content: center; }
.back-icon { font-size: 40rpx; color: #FDFCF8; font-weight: bold; }
.header-title { flex: 1; font-size: 34rpx; font-weight: bold; color: #FDFCF8; }
.offline-badge { display: flex; align-items: center; gap: 8rpx; background: rgba(255,255,255,0.15); padding: 8rpx 16rpx; border-radius: 20rpx; }
.offline-dot { width: 12rpx; height: 12rpx; border-radius: 50%; background: #4CAF50; }
.offline-text { font-size: 22rpx; color: #FDFCF8; }

.content { flex: 1; }

.nav-input-card { background: #FDFCF8; margin: 20rpx 24rpx; padding: 30rpx; border-radius: 20rpx; box-shadow: 0 4rpx 20rpx rgba(163,59,50,0.10); }
.input-row { display: flex; align-items: center; gap: 16rpx; }
.input-dot { width: 20rpx; height: 20rpx; border-radius: 50%; flex-shrink: 0; }
.input-dot.start { background: #4CAF50; }
.input-dot.end { background: #A33B32; }
.input-wrap { flex: 1; background: #F8F9FA; border-radius: 12rpx; padding: 20rpx; }
.input-text { font-size: 28rpx; color: #333; }
.input-text.placeholder { color: #bbb; }
.input-divider { display: flex; align-items: center; padding: 8rpx 0 8rpx 36rpx; }
.divider-line { flex: 1; height: 1rpx; background: #E0E0E0; }
.swap-btn { width: 50rpx; height: 50rpx; background: #F8F9FA; border-radius: 50%; display: flex; flex-direction: column; align-items: center; justify-content: center; margin-left: 16rpx; gap: 2rpx; }
.swap-icon { font-size: 20rpx; color: #666; line-height: 1; }

.mode-card { display: flex; gap: 16rpx; margin: 0 24rpx 20rpx; }
.mode-item { flex: 1; background: #FDFCF8; border-radius: 16rpx; padding: 20rpx; display: flex; flex-direction: column; align-items: center; gap: 8rpx; box-shadow: 0 2rpx 10rpx rgba(163,59,50,0.06); border: 2rpx solid transparent; }
.mode-item.active { border-color: #A33B32; background: #FFF8F6; }
.mode-icon { font-size: 40rpx; }
.mode-icon-img { width: 40rpx; height: 40rpx; }
.info-icon-img { width: 28rpx; height: 28rpx; }
.mode-label { font-size: 24rpx; color: #333; }
.mode-time { font-size: 22rpx; color: #A33B32; font-weight: bold; }

.result-card { background: #FDFCF8; margin: 0 24rpx 20rpx; padding: 30rpx; border-radius: 20rpx; box-shadow: 0 4rpx 20rpx rgba(163,59,50,0.10); }
.result-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 24rpx; }
.result-title { font-size: 30rpx; font-weight: bold; color: #1D3B5A; }
.result-distance { font-size: 32rpx; font-weight: bold; color: #A33B32; }

.result-info-list { display: flex; flex-direction: column; gap: 16rpx; margin-bottom: 24rpx; }
.result-info-row { display: flex; align-items: center; gap: 12rpx; }
.info-icon { font-size: 28rpx; width: 40rpx; text-align: center; }
.info-icon-img { width: 32rpx; height: 32rpx; }
.info-label { font-size: 26rpx; color: #999; width: 100rpx; }
.info-value { font-size: 26rpx; color: #333; font-weight: bold; }

.tips-section { background: rgba(212,163,115,0.1); border-radius: 14rpx; padding: 20rpx; border-left: 4rpx solid #D4A373; }
.tips-title { display: block; font-size: 26rpx; font-weight: bold; color: #8B6914; margin-bottom: 12rpx; }
.tip-item { margin-bottom: 8rpx; }
.tip-text { font-size: 24rpx; color: #666; line-height: 1.6; }

.nearby-section { margin: 0 24rpx 20rpx; }
.section-title-row { display: flex; align-items: center; gap: 14rpx; margin-bottom: 16rpx; }
.accent-bar { width: 6rpx; height: 32rpx; background: #A33B32; border-radius: 3rpx; }
.section-title { font-size: 30rpx; font-weight: bold; color: #1D3B5A; }
.nearby-list { background: #FDFCF8; border-radius: 20rpx; overflow: hidden; box-shadow: 0 4rpx 20rpx rgba(163,59,50,0.10); }
.nearby-item { display: flex; align-items: center; padding: 24rpx; border-bottom: 1rpx solid #F5F0E8; }
.nearby-item:last-child { border-bottom: none; }
.nearby-info { flex: 1; }
.nearby-name { display: block; font-size: 28rpx; font-weight: bold; color: #1D3B5A; margin-bottom: 6rpx; }
.nearby-dist { font-size: 22rpx; color: #999; }
.nav-arrow { font-size: 36rpx; }
.nav-arrow-img { width: 36rpx; height: 36rpx; }

.search-panel { position: fixed; top: 0; left: 0; right: 0; bottom: 0; background: #FDFCF8; z-index: 500; display: flex; flex-direction: column; }
.search-header { display: flex; justify-content: space-between; align-items: center; padding: 60rpx 30rpx 20rpx; background: linear-gradient(135deg, #1D3B5A, #2E5F8E); }
.search-title { font-size: 32rpx; font-weight: bold; color: #FDFCF8; }
.search-close { font-size: 36rpx; color: rgba(255,255,255,0.8); }
.search-input-wrap { padding: 20rpx 24rpx; background: #FDFCF8; }
.search-input { background: #F8F9FA; border-radius: 50rpx; padding: 18rpx 28rpx; font-size: 28rpx; }
.search-results { flex: 1; padding: 0 24rpx; overflow-y: auto; }
.search-item { display: flex; justify-content: space-between; align-items: center; padding: 24rpx 0; border-bottom: 1rpx solid #F0F0F0; }
.search-item-name { font-size: 28rpx; color: #333; font-weight: bold; display: flex; align-items: center; gap: 6rpx; }
.loc-icon { width: 26rpx; height: 26rpx; }
.search-item-addr { font-size: 22rpx; color: #999; }
.current-loc { background: rgba(76,175,80,0.05); border-radius: 12rpx; padding: 20rpx; margin: 10rpx 0; }
.search-empty { text-align: center; padding: 60rpx 0; color: #999; font-size: 28rpx; }

.bottom-bar { background: #FDFCF8; padding: 20rpx 30rpx 40rpx; display: flex; align-items: center; gap: 20rpx; box-shadow: 0 -4rpx 20rpx rgba(163,59,50,0.10); }
.summary-text { flex: 1; display: flex; flex-direction: column; gap: 4rpx; font-size: 24rpx; color: #666; }
.summary-dist { font-size: 28rpx; color: #A33B32; font-weight: bold; }
.open-map-btn { background: linear-gradient(135deg, #A33B32, #C85A50); color: #FDFCF8; font-size: 28rpx; font-weight: bold; padding: 20rpx 36rpx; border-radius: 50rpx; }
</style>
