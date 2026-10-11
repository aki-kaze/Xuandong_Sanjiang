<template>
  <view class="heritage-map-page">
    <!-- 地图区域 -->
    <map
      class="map-view"
      :latitude="centerLat"
      :longitude="centerLng"
      :markers="mapMarkers"
      :scale="13"
      :show-location="hasLocationAuth"
      @markertap="onMarkerTap"
      @callouttap="onCalloutTap"
    ></map>

    <!-- 底部非遗列表 -->
    <view class="bottom-panel" :class="{ expanded: panelExpanded }">
      <view class="panel-handle" @tap="panelExpanded = !panelExpanded">
        <view class="handle-bar"></view>
        <text class="handle-text">{{ panelExpanded ? '收起' : '展开列表' }}</text>
      </view>

      <scroll-view scroll-y class="heritage-scroll" :style="{ height: panelExpanded ? '60vh' : '200rpx' }">
        <view class="heritage-map-list">
          <view
            class="hml-item"
            v-for="item in heritageItems"
            :key="item.id"
            :class="{ active: selectedId === item.id }"
            @tap="focusHeritage(item)"
          >
            <view class="hml-icon" :style="{ background: item.color + '22' }">
              <image v-if="item.icon.startsWith('http')" class="hml-img" :src="item.icon" mode="aspectFill" />
              <text v-else class="hml-emoji">{{ item.icon }}</text>
            </view>
            <view class="hml-info">
              <text class="hml-name">{{ item.name }}</text>
              <text class="hml-level" :style="{ color: item.color }">{{ item.level }}</text>
              <text class="hml-addr" v-if="item.location">{{ item.location.address }}</text>
            </view>
            <view class="hml-actions">
              <view class="hml-nav-btn" @tap.stop="navigateTo(item)">
                <image class="hml-nav-icon" src="/static/icons/compass.png" mode="aspectFit" />
                <text class="hml-action-label">导航</text>
              </view>
              <view class="hml-detail-btn" @tap.stop="goDetail(item)">
                <image class="hml-detail-icon" src="/static/icons/detail.png" mode="aspectFit" />
                <text class="hml-action-label">详情</text>
              </view>
            </view>
          </view>
        </view>
      </scroll-view>
    </view>

    <!-- 选中非遗信息区域 -->
    <view class="selected-card" v-if="selectedHeritage" @tap="goDetail(selectedHeritage)">
      <image class="sc-img" :src="selectedHeritage.cover" mode="aspectFill" />
      <view class="sc-info">
        <view class="sc-header">
          <text class="sc-name">{{ selectedHeritage.name }}</text>
          <view class="sc-level-badge" :style="{ background: selectedHeritage.color }">
            <text>{{ selectedHeritage.level }}</text>
          </view>
        </view>
        <text class="sc-desc" v-if="selectedHeritage.location">{{ selectedHeritage.location.desc }}</text>
        <view class="sc-footer">
          <text class="sc-tap">点击查看详情 →</text>
        </view>
      </view>
      <text class="sc-close" @tap.stop="selectedHeritage = null">→</text>
    </view>
  </view>
</template>

<script>
import { staticData } from '../../utils/static-data.js'

export default {
  data() {
    return {
      centerLat: 25.8234,
      centerLng: 109.6078,
      heritageItems: [],
      mapMarkers: [],
      selectedId: null,
      selectedHeritage: null,
      panelExpanded: false,
      hasLocationAuth: false
    }
  },
  onLoad() {
    this.checkLocationAuth()
    this.loadHeritageData()
  },
  methods: {
    checkLocationAuth() {
      uni.getSetting({
        success: (res) => {
          if (res.authSetting['scope.userLocation']) {
            this.hasLocationAuth = true
          }
        }
      })
    },
    loadHeritageData() {
      // 为每个非遗项目添加体验地点坐标和地址描述
      const locations = {
        1: { lat: 25.8234, lng: 109.5678, address: '程阳八寨鼓楼广场', desc: '侗族大歌传习地，可体验大歌演出' },
        2: { lat: 25.8256, lng: 109.5701, address: '程阳永济风雨桥', desc: '风雨桥建造技艺展示，榫卯结构体验' },
        3: { lat: 25.7834, lng: 109.6078, address: '三江鼓楼广场', desc: '世界最高侗族鼓楼，建筑技艺展示' },
        4: { lat: 25.7812, lng: 109.6045, address: '三江侗族生态博物馆', desc: '侗锦织造技艺体验，可亲手织锦' },
        5: { lat: 25.8234, lng: 109.5678, address: '程阳八寨芦笙节', desc: '芦笙节举办地，可体验芦笙演奏' },
        6: { lat: 25.8234, lng: 109.5678, address: '程阳八寨多耶广场', desc: '多耶歌舞体验，围圈共舞' }
      }

      this.heritageItems = staticData.heritage.filter(h => h.status === 1).map(h => ({
        ...h,
        location: locations[h.id] || { lat: 25.8234, lng: 109.6078, address: '三江侗族自治县', desc: '' }
      }))

      this.mapMarkers = this.heritageItems.map((h, idx) => ({
        id: h.id,
        latitude: h.location.lat,
        longitude: h.location.lng,
        title: h.name,
        iconPath: '/static/icons/transparent.png',
        width: 1,
        height: 1,
        callout: {
          content: h.name,
          color: '#1D3B5A',
          fontSize: 14,
          borderRadius: 8,
          bgColor: '#FDFCF8',
          padding: 8,
          display: 'ALWAYS',
          borderWidth: 1,
          borderColor: h.color || '#A33B32'
        }
      }))
    },
    onMarkerTap(e) {
      console.log('[heritage-map] markertap:', JSON.stringify(e))
      const markerId = e.detail.markerId
      const heritage = this.heritageItems.find(h => h.id === markerId)
      if (heritage) {
        this.selectedId = heritage.id
        this.selectedHeritage = heritage
        this.centerLat = heritage.location.lat
        this.centerLng = heritage.location.lng
      }
    },
    onCalloutTap(e) {
      console.log('[heritage-map] callouttap:', JSON.stringify(e))
      const markerId = e.detail.markerId
      const heritage = this.heritageItems.find(h => h.id === markerId)
      if (heritage) {
        this.selectedId = heritage.id
        this.selectedHeritage = heritage
        this.centerLat = heritage.location.lat
        this.centerLng = heritage.location.lng
      }
    },
    focusHeritage(item) {
      this.selectedId = item.id
      this.selectedHeritage = item
      this.centerLat = item.location.lat
      this.centerLng = item.location.lng
      this.panelExpanded = false
    },
    navigateTo(item) {
      uni.openLocation({
        latitude: item.location.lat,
        longitude: item.location.lng,
        name: item.name,
        address: item.location.address,
        fail: () => { uni.showToast({ title: '打开地图失败', icon: 'none' }) }
      })
    },
    goDetail(item) {
      uni.switchTab({
        url: `/pages/heritage/heritage?id=${item.id}`
      })
    },
    resetMap() {
      this.centerLat = 25.8234
      this.centerLng = 109.6078
      this.selectedId = null
      this.selectedHeritage = null
    },
    goBack() {
      uni.navigateBack()
    }
  }
}
</script>

<style scoped lang="scss">
.heritage-map-page {
  position: relative;
  width: 100%;
  height: 100vh;
  background: #F8F9FA;
}

.map-view {
  width: 100%;
  height: 100%;
}

/* 底部面板 */
.bottom-panel {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  background: #FDFCF8;
  border-radius: 32rpx 32rpx 0 0;
  box-shadow: 0 -4rpx 24rpx rgba(0,0,0,0.12);
  z-index: 100;
  transition: height 0.3s;
}
.panel-handle {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 16rpx 0 8rpx;
  gap: 8rpx;
}
.handle-bar { width: 80rpx; height: 6rpx; background: #D0D0D0; border-radius: 3rpx; }
.handle-text { font-size: 22rpx; color: #999; }

.panel-header {
  display: none;
}
.panel-title { font-size: 30rpx; font-weight: bold; color: #1D3B5A; display: flex; align-items: center; gap: 8rpx; }
.panel-title-icon,
.hml-nav-icon { width: 28rpx; height: 28rpx; }
.panel-count { font-size: 24rpx; color: #999; }

.heritage-scroll {
  padding: 0 24rpx 40rpx;
}

.heritage-map-list {
  display: flex;
  flex-direction: column;
  gap: 16rpx;
}

.hml-item {
  display: flex;
  align-items: center;
  gap: 16rpx;
  padding: 20rpx;
  background: #F8F9FA;
  border-radius: 16rpx;
  border: 2rpx solid transparent;
}
.hml-item.active {
  border-color: #A33B32;
  background: rgba(163, 59, 50, 0.06);
}
.hml-icon {
  width: 72rpx;
  height: 72rpx;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}
.hml-emoji { font-size: 36rpx; }
.hml-img { width: 48rpx; height: 48rpx; border-radius: 50%; }
.hml-info { flex: 1; display: flex; flex-direction: column; gap: 4rpx; }
.hml-name { font-size: 28rpx; font-weight: bold; color: #1D3B5A; }
.hml-level { font-size: 22rpx; font-weight: 500; }
.hml-addr { font-size: 22rpx; color: #999; }
.hml-actions { display: flex; flex-direction: row; gap: 12rpx; align-items: center; justify-content: flex-end; }
.hml-action-label { font-size: 20rpx; color: #666; }
.hml-nav-btn {
  display: flex;
  align-items: center;
  gap: 6rpx;
  padding: 8rpx 20rpx;
  background: #1D3B5A;
  color: #FDFCF8;
  border-radius: 20rpx;
  font-size: 22rpx;
}
.hml-nav-icon { width: 28rpx; height: 28rpx; }
.hml-detail-btn {
  display: flex;
  align-items: center;
  gap: 6rpx;
  padding: 8rpx 20rpx;
  border: 2rpx solid #A33B32;
  color: #A33B32;
  background: rgba(163, 59, 50, 0.08);
  border-radius: 20rpx;
  font-size: 22rpx;
}
.hml-detail-icon { width: 28rpx; height: 28rpx; }

/* 选中信息卡片 */
.selected-card {
  position: fixed;
  bottom: 240rpx;
  left: 24rpx;
  right: 24rpx;
  background: #FDFCF8;
  border-radius: 20rpx;
  overflow: hidden;
  box-shadow: 0 4rpx 24rpx rgba(0,0,0,0.15);
  z-index: 150;
  display: flex;
  gap: 0;
}
.sc-img {
  width: 200rpx;
  height: 200rpx;
  flex-shrink: 0;
}
.sc-info {
  flex: 1;
  padding: 20rpx 24rpx;
  display: flex;
  flex-direction: column;
  gap: 8rpx;
}
.sc-header { display: flex; align-items: center; gap: 12rpx; }
.sc-name { font-size: 30rpx; font-weight: bold; color: #1D3B5A; flex: 1; }
.sc-level-badge {
  padding: 4rpx 12rpx;
  border-radius: 16rpx;
  font-size: 20rpx;
  color: #FDFCF8;
}
.sc-desc { font-size: 24rpx; color: #666; line-height: 1.5; }
.sc-footer { margin-top: auto; }
.sc-tap { font-size: 22rpx; color: #A33B32; }
.sc-close {
  position: absolute;
  top: 12rpx;
  right: 16rpx;
  font-size: 28rpx;
  color: #999;
  padding: 8rpx;
}
</style>
