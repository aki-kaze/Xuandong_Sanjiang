<template>
  <view class="map-page">
    <map
      class="map-view"
      :latitude="mapCenter.lat"
      :longitude="mapCenter.lng"
      :markers="markers"
      :scale="mapScale"
      :show-location="hasLocationAuth"
      @markertap="onMarkerTap"
      @callouttap="onCalloutTap"
      @tap="onMapTap"
    ></map>

    <view class="map-top">
      <view class="map-search" @tap="toggleSearch">
        <text class="map-search-icon"><image src="/static/icons/search.png" mode="aspectFit" style="width:30rpx;height:30rpx;" /></text>
        <text class="map-search-text">{{ searchKeyword || '搜索三江景点、地址...' }}</text>
      </view>
      <view class="map-filter">
        <view
          class="filter-btn"
          v-for="f in filters"
          :key="f.id"
          :class="{ active: activeFilter === f.id }"
          @tap="setFilter(f.id)"
        >{{ f.label }}</view>
      </view>
    </view>

    <view class="search-panel" v-if="showSearch">
      <view class="search-input-wrap">
        <input
          class="search-input"
          v-model="searchKeyword"
          placeholder="搜索景点、地址..."
          focus
          @input="onSearchInput"
        />
        <text class="clear-btn" @tap="clearSearch">清除</text>
      </view>
      <view class="search-results">
        <view
          class="search-result-item"
          v-for="item in searchResults"
          :key="item.id"
          @tap="goToMarker(item)"
        >
          <image class="result-icon" src="/static/icons/position.png" mode="aspectFit" />
          <view class="result-info">
            <text class="result-name">{{ item.name }}</text>
            <text class="result-type">{{ item.typeLabel }}</text>
          </view>
        </view>
        <view class="search-empty" v-if="searchKeyword && searchResults.length === 0">
          <text>未找到相关地点</text>
        </view>
      </view>
    </view>

    <view class="bottom-panel" v-if="selectedMarker">
      <view class="panel-drag-bar"></view>
      <view class="panel-content">
        <view class="panel-header">
          <view class="panel-image-wrap" v-if="selectedMarker.cover || selectedMarker.image">
            <image class="panel-image" :src="selectedMarker.cover || selectedMarker.image" mode="aspectFill" />
          </view>
          <view class="panel-icon-wrap" v-else>
            <image class="panel-icon" :src="getMarkerIcon(selectedMarker.type)" mode="aspectFit" />
          </view>
          <view class="panel-info">
            <text class="panel-name">{{ selectedMarker.name }}</text>
            <text class="panel-type">{{ selectedMarker.typeLabel }}</text>
            <text class="panel-address" v-if="selectedMarker.heritage">非遗：{{ selectedMarker.heritage }}</text>
          </view>
          <view class="panel-close" @tap.stop="closePanel">✕</view>
        </view>

        <!-- 景点操作 -->
        <view class="panel-actions" v-if="selectedMarker.type === 'attraction'">
          <view class="action-btn" @tap="startNavigation(selectedMarker)">
            <image class="action-icon" src="/static/icons/compass.png" mode="aspectFit" />
            <text class="action-label">导航</text>
          </view>
          <view class="action-btn" @tap="goAttractionDetail(selectedMarker)">
            <image class="action-icon" src="/static/icons/detail.png" mode="aspectFit" />
            <text class="action-label">详情</text>
          </view>
          <view class="action-btn" @tap="goTicket">
            <image class="action-icon" src="/static/icons/ticket.png" mode="aspectFit" />
            <text class="action-label">购票</text>
          </view>
          <view class="action-btn" @tap="goStampCard">
            <image class="action-icon" src="/static/icons/love.png" mode="aspectFit" />
            <text class="action-label">集卡</text>
          </view>
        </view>
        <!-- 非遗集卡点操作 -->
        <view class="panel-actions" v-if="selectedMarker.type === 'stamp'">
          <view class="action-btn" @tap="startNavigation(selectedMarker)">
            <image class="action-icon" src="/static/icons/compass.png" mode="aspectFit" />
            <text class="action-label">导航</text>
          </view>
          <view class="action-btn" @tap="goStampCard">
            <image class="action-icon" src="/static/icons/love.png" mode="aspectFit" />
            <text class="action-label">护照</text>
          </view>
        </view>
        <!-- 美食店操作 -->
        <view class="panel-actions" v-if="selectedMarker.type === 'food'">
          <view class="action-btn" @tap="startNavigation(selectedMarker)">
            <image class="action-icon" src="/static/icons/compass.png" mode="aspectFit" />
            <text class="action-label">导航</text>
          </view>
          <view class="action-btn" @tap="goFoodDetail(selectedMarker)">
            <image class="action-icon" src="/static/icons/detail.png" mode="aspectFit" />
            <text class="action-label">美食详情</text>
          </view>
        </view>
        <!-- 通用导航（未指定类型或临时标记） -->
        <view class="panel-actions" v-if="!selectedMarker.type || !['attraction', 'stamp', 'food'].includes(selectedMarker.type)">
          <view class="action-btn" @tap="startNavigation(selectedMarker)">
            <image class="action-icon" src="/static/icons/compass.png" mode="aspectFit" />
            <text class="action-label">导航</text>
          </view>
        </view>
        <!-- 打卡集章按钮（非遗集卡点） -->
        <view class="checkin-section" v-if="selectedMarker.heritageId">
          <view
            class="checkin-btn"
            :class="{ collected: isStampCollected(selectedMarker.heritageId) }"
            @tap="checkinStamp(selectedMarker)"
          >
            <image class="checkin-btn-icon-img" v-if="isStampCollected(selectedMarker.heritageId)" src="/static/icons/点赞.png" mode="aspectFit" />
            <image class="checkin-btn-icon-img" v-else src="/static/icons/position.png" mode="aspectFit" />
            <text class="checkin-btn-text">{{ isStampCollected(selectedMarker.heritageId) ? '已打卡集章' : '打卡集章' }}</text>
          </view>
        </view>
      </view>
    </view>

    <view class="fab-group" v-if="!selectedMarker && !showSearch">
      <view class="route-fab" @tap="goRoutePlan">
        <image class="route-fab-icon" src="/static/icons/景区地图.png" mode="aspectFit" />
        <text class="route-fab-text">路线规划</text>
      </view>
    </view>

    <view class="locate-btn" @tap="locateMe">
      <image class="locate-icon" src="/static/icons/position.png" mode="aspectFit" />
    </view>
  </view>
</template>

<script>
import staticAttractions from '../../data/attractions.json'
// import staticFoods from '../../data/foods.json'  // 已注释，不加载美食数据
import { collectStamp, showStampToast, getCollectedStamps } from '../../utils/stamps.js'

export default {
  data() {
    return {
      mapCenter: { lat: 25.7834, lng: 109.6078 },
      mapScale: 12,
      activeFilter: 'all',
      filters: [
        { id: 'all', label: '全部' },
        { id: 'attraction', label: '景点' },
        { id: 'stamp', label: '集卡' }
      ],
      allMarkers: [],
      selectedMarker: null,
      showSearch: false,
      searchKeyword: '',
      searchResults: [],
      collectedStamps: [],
      hasLocationAuth: false
    }
  },
  computed: {
    markers() {
      if (this.activeFilter === 'all') return this.allMarkers
      if (this.activeFilter === 'stamp') {
        return this.allMarkers.filter(m => m.heritageId)
      }
      return this.allMarkers.filter(m => m.type === this.activeFilter)
    }
  },
  onLoad(options) {
    this.checkLocationAuth()
    this.loadAttractions()
    this.refreshStamps()
    // 从护照页面跳转过来，自动切换到集卡筛选并选中对应非遗项目
    if (options && options.filter) {
      this.activeFilter = options.filter
    }
    if (options && options.heritageId) {
      const targetId = parseInt(options.heritageId)
      this.$nextTick(() => {
        const marker = this.allMarkers.find(m => m.heritageId === targetId)
        if (marker) {
          this.selectedMarker = marker
          this.mapCenter = { lat: marker.lat, lng: marker.lng }
          this.mapScale = 15
        }
      })
    }
    // 从美食详情页、景点详情、购票页面跳转过来，定位到目标位置
    if (options && (options.targetId || (options.targetLat && options.targetLng) || (options.lat && options.lng))) {
      const targetId = parseInt(options.targetId) || 0
      const targetLat = parseFloat(options.targetLat || options.lat)
      const targetLng = parseFloat(options.targetLng || options.lng)
      const targetName = decodeURIComponent(options.targetName || options.name || '')
      const targetAddress = decodeURIComponent(options.targetAddress || '')
      const targetCover = options.targetCover ? decodeURIComponent(options.targetCover) : ''
      const targetType = decodeURIComponent(options.targetType || 'attraction')

      this.$nextTick(() => {
        let marker = null

        // 如果是美食类型，直接创建临时标记，避免匹配到景点
        if (targetType === 'food') {
          const tempMarker = {
            id: Date.now(),
            type: 'food',
            lat: targetLat,
            lng: targetLng,
            latitude: targetLat,
            longitude: targetLng,
            title: targetName,
            name: targetName,
            address: targetAddress,
            cover: targetCover,
            image: targetCover,
            typeLabel: '美食店',
            width: 30,
            height: 30,
            alpha: 0,
            callout: {
              content: targetName,
              color: '#B45309',
              fontSize: 13,
              borderRadius: 8,
              bgColor: '#FFFBEB',
              padding: 8,
              display: 'ALWAYS',
              borderWidth: 1,
              borderColor: '#F59E0B'
            }
          }
          this.allMarkers.push(tempMarker)
          this.selectedMarker = tempMarker
          this.mapCenter = { lat: targetLat, lng: targetLng }
          this.mapScale = 15
          return
        }

        // 景点/购票：先按 id，再按名称/坐标
        marker = this.allMarkers.find(m => m.id === targetId)
        if (!marker) {
          marker = this.allMarkers.find(m => {
            if (m.type !== 'attraction') return false
            const nameMatch = m.name.includes(targetName.replace('景区门票', '').replace('套票', '').replace('茶园', ''))
            const latClose = Math.abs(m.lat - targetLat) < 0.01
            const lngClose = Math.abs(m.lng - targetLng) < 0.01
            return nameMatch || (latClose && lngClose)
          })
        }

        if (marker) {
          this.selectedMarker = marker
          this.mapCenter = { lat: marker.lat, lng: marker.lng }
          this.mapScale = 15
        } else if (targetLat && targetLng) {
          // 静态数据中没有此地点，创建临时标记
          const typeLabelMap = {
            'food': '美食店',
            'attraction': '景点',
            'stamp': '非遗集卡点'
          }
          const tempMarker = {
            id: Date.now(),
            type: targetType,
            lat: targetLat,
            lng: targetLng,
            latitude: targetLat,
            longitude: targetLng,
            title: targetName,
            name: targetName,
            address: targetAddress,
            cover: targetCover,
            image: targetCover,
            typeLabel: typeLabelMap[targetType] || '地点',
            width: 30,
            height: 30,
            alpha: 0,
            callout: {
              content: targetName,
              color: '#1D3B5A',
              fontSize: 13,
              borderRadius: 8,
              bgColor: '#FFFFFF',
              padding: 8,
              display: 'ALWAYS',
              borderWidth: 1,
              borderColor: '#1D3B5A'
            }
          }
          this.allMarkers.push(tempMarker)
          this.selectedMarker = tempMarker
          this.mapCenter = { lat: targetLat, lng: targetLng }
          this.mapScale = 15
        }
      })
    }
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
    getMarkerIcon(type) {
      const iconMap = {
        'food': '/static/icons/food.png',
        'attraction': '/static/icons/position.png',
        'stamp': '/static/icons/feiyi.png',
        'hotel': '/static/icons/hotel.png'
      }
      return iconMap[type] || '/static/icons/position.png'
    },
    loadAttractions() {
      // 非遗项目对应的地图坐标（地址、图片链接）
      const heritageLocations = [
        { heritageId: 1, name: '程阳八寨鼓楼广场', lat: 25.90128, lng: 109.6487, heritage: '侗族大歌', image: 'https://cdn.phototourl.com/free/2026-03-23-75376aa0-ba75-4269-8839-bbe6aca040a6.jpg' },
        { heritageId: 2, name: '程阳永济风雨桥', lat: 25.90328, lng: 109.6467, heritage: '侗族风雨桥', image: 'https://cdn.phototourl.com/free/2026-03-23-656b106c-9ce1-4ef1-93b3-e205cd8267f4.png' },
        { heritageId: 3, name: '三江鼓楼', lat: 25.7834, lng: 109.6078, heritage: '侗族鼓楼', image: 'https://cdn.phototourl.com/free/2026-03-23-4df6b38e-a4a7-4898-a867-eababd51019a.jpg' },
        { heritageId: 4, name: '三江侗族生态博物馆', lat: 25.7812, lng: 109.6045, heritage: '侗锦织造技艺', image: 'https://cdn.phototourl.com/free/2026-03-23-83a39e17-b2f9-4ff8-bced-41e9c064e9fe.jpg' },
        { heritageId: 5, name: '程阳八寨芦笙广场', lat: 25.8234, lng: 109.5678, heritage: '侗族芦笙节', image: 'https://cdn.phototourl.com/free/2026-03-23-4413c254-1f17-46a6-a102-78ffbd9c05bc.jpg' },
        { heritageId: 6, name: '程阳八寨多耶广场', lat: 25.8234, lng: 109.5678, heritage: '侗族多耶', image: 'https://cdn.phototourl.com/free/2026-03-23-32c0ee5b-709a-4f2c-9098-e9a4478e20c3.png' }
      ]

      // 使用默认标记，隐藏图标只显示气泡
      const heritageMarkers = heritageLocations.map(h => ({
        id: 1000 + h.heritageId,
        heritageId: h.heritageId,
        type: 'stamp',
        lat: h.lat,
        lng: h.lng,
        latitude: h.lat,
        longitude: h.lng,
        title: h.name,
        name: h.name,
        heritage: h.heritage,
        typeLabel: '非遗集卡点',
        cover: h.image,
        image: h.image,
        width: 30,
        height: 30,
        alpha: 0,
        callout: {
          content: h.name,
          color: '#1D3B5A',
          fontSize: 13,
          borderRadius: 8,
          bgColor: '#FDFCF8',
          padding: 8,
          display: 'ALWAYS',
          borderWidth: 1,
          borderColor: '#2E5F8E'
        }
      }))

      const attractionMarkers = staticAttractions.map(m => ({
        ...m,
        id: parseInt(m.id) || 0,
        type: 'attraction',
        lat: parseFloat(m.lat),
        lng: parseFloat(m.lng),
        latitude: parseFloat(m.lat),
        longitude: parseFloat(m.lng),
        title: m.name,
        name: m.name,
        typeLabel: '景点',
        cover: m.cover || m.images?.[0] || '',
        width: 30,
        height: 30,
        alpha: 0,
        callout: {
          content: m.name,
          color: '#1D3B5A',
          fontSize: 13,
          borderRadius: 8,
          bgColor: '#FFFFFF',
          padding: 8,
          display: 'ALWAYS',
          borderWidth: 1,
          borderColor: '#1D3B5A'
        }
      }))

      // 美食标记点 - 已注释，不在地图上显示
      // const foodMarkers = staticFoods.map(f => ({
      //   id: 2000 + f.id,
      //   type: 'food',
      //   lat: parseFloat(f.latitude),
      //   lng: parseFloat(f.longitude),
      //   ...
      // }))

      this.allMarkers = [...heritageMarkers, ...attractionMarkers]
      console.log('[map] markers 总数:', this.allMarkers.length)
      console.log('[map] 景点标记数量:', attractionMarkers.length)
      console.log('[map] 非遗标记数量:', heritageMarkers.length)
      if (this.allMarkers.length > 0) {
        console.log('[map] 第一个 marker 完整数据:', JSON.stringify(this.allMarkers[0], null, 2))
      }
    },
    setFilter(id) {
      this.activeFilter = id
      this.selectedMarker = null
    },
    onMarkerTap(e) {
      console.log('[map] markertap 事件触发:', JSON.stringify(e))
      console.log('[map] markerId:', e.markerId)
      const id = e.markerId
      this.selectedMarker = this.allMarkers.find(m => m.id === id) || null
      console.log('[map] 找到的 marker:', this.selectedMarker ? this.selectedMarker.name : 'null')
      if (this.selectedMarker) {
        this.mapCenter = { lat: this.selectedMarker.lat, lng: this.selectedMarker.lng }
      }
    },
    onCalloutTap(e) {
      console.log('[map] callouttap 事件触发:', JSON.stringify(e))
      const id = e.markerId
      this.selectedMarker = this.allMarkers.find(m => m.id === id) || null
      console.log('[map] callout 找到的 marker:', this.selectedMarker ? this.selectedMarker.name : 'null')
      if (this.selectedMarker) {
        this.mapCenter = { lat: this.selectedMarker.lat, lng: this.selectedMarker.lng }
      }
    },
    onMapTap() {
      if (!this.showSearch) this.selectedMarker = null
    },
    closePanel() { this.selectedMarker = null },
    toggleSearch() { this.showSearch = !this.showSearch; if (!this.showSearch) { this.searchKeyword = ''; this.searchResults = [] } },
    clearSearch() { this.searchKeyword = ''; this.searchResults = []; this.showSearch = false },
    onSearchInput() {
      const kw = this.searchKeyword.trim()
      if (!kw) { this.searchResults = []; return }
      this.searchResults = this.allMarkers.filter(m => m.name.includes(kw))
    },
    goToMarker(item) {
      this.mapCenter = { lat: item.lat, lng: item.lng }
      this.mapScale = 15
      this.selectedMarker = item
      this.showSearch = false
      this.searchKeyword = ''
    },
    startNavigation(marker) {
      uni.openLocation({
        latitude: marker.lat,
        longitude: marker.lng,
        name: marker.name,
        address: marker.address || '',
        success: () => {},
        fail: () => { uni.showToast({ title: '打开地图失败', icon: 'none' }) }
      })
    },
    goAttractionDetail(marker) {
      if (marker.id) {
        uni.navigateTo({ url: '/pages/attraction/detail?id=' + marker.id })
      } else {
        uni.showToast({ title: '暂无详情', icon: 'none' })
      }
    },
    goFoodDetail(marker) {
      uni.showToast({ title: '请从美食列表页访问详情', icon: 'none' })
    },
    goTicket() { uni.navigateTo({ url: '/pages/ticket/ticket' }) },
    goStampCard() {
      uni.navigateTo({ url: '/pages/heritage/passport' })
    },
    goRoutePlan() { uni.navigateTo({ url: '/pages/route/route' }) },
    refreshStamps() {
      this.collectedStamps = getCollectedStamps()
    },
    isStampCollected(heritageId) {
      return this.collectedStamps.some(s => s.id === heritageId)
    },
    checkinStamp(marker) {
      if (!marker.heritageId) return
      if (this.isStampCollected(marker.heritageId)) {
        uni.showToast({ title: '已集过此印章', icon: 'none' })
        return
      }
      const result = collectStamp(marker.heritageId, 'location')
      if (result.isNew) {
        this.refreshStamps()
        showStampToast(result.stamp, result.allCollected)
      }
    },
    locateMe() {
      uni.showLoading({ title: '定位中...' })

      uni.getLocation({
        type: 'gcj02',
        success: (res) => {
          uni.hideLoading()

          this.mapCenter = { lat: res.latitude, lng: res.longitude }
          this.mapScale = 15

          uni.showToast({
            title: '定位成功',
            icon: 'success',
            duration: 1500
          })

          this.getNearbyAttractions(res.latitude, res.longitude)
        },
        fail: (err) => {
          uni.hideLoading()
          console.error('定位失败:', err)

          uni.showModal({
            title: '定位失败',
            content: '请检查是否已授权位置权限，或手动搜索目的地。',
            showCancel: false
          })
        }
      })
    },

    getNearbyAttractions(lat, lng) {
      uni.showToast({
        title: `附近${staticAttractions.length}个景点`,
        icon: 'none',
        duration: 2000
      })
    }
  }
}
</script>

<style scoped lang="scss">
.map-page { position: relative; width: 100%; height: 100vh; overflow: hidden; }
.map-view { width: 100%; height: 100vh; }

.map-top { position: absolute; top: 0; left: 0; right: 0; padding: 20rpx 24rpx 0; z-index: 100; }
.map-search { background: #FDFCF8; border-radius: 50rpx; padding: 18rpx 28rpx; display: flex; align-items: center; gap: 12rpx; box-shadow: 0 4rpx 20rpx rgba(163,59,50,0.15); margin-bottom: 16rpx; }
.map-search-icon { font-size: 30rpx; }
.map-search-text { font-size: 28rpx; color: #999; flex: 1; }
.map-filter { display: flex; gap: 12rpx; overflow-x: auto; padding-bottom: 4rpx; }
.filter-btn { background: #FDFCF8; border-radius: 30rpx; padding: 10rpx 24rpx; font-size: 24rpx; color: #666; white-space: nowrap; box-shadow: 0 2rpx 10rpx rgba(163,59,50,0.06); }
.filter-btn.active { background: #1D3B5A; color: #FDFCF8; }

.search-panel { position: absolute; top: 0; left: 0; right: 0; bottom: 0; background: #FDFCF8; z-index: 200; padding: 20rpx; }
.search-input-wrap { display: flex; align-items: center; gap: 16rpx; background: #F8F9FA; border-radius: 50rpx; padding: 16rpx 24rpx; margin-bottom: 20rpx; }
.search-input { flex: 1; font-size: 28rpx; }
.search-result-item { display: flex; align-items: center; gap: 20rpx; padding: 24rpx 0; border-bottom: 1rpx solid #F0F0F0; }
.result-icon { width: 40rpx; height: 40rpx; }
.result-name { display: block; font-size: 28rpx; color: #333; font-weight: bold; }
.result-type { display: block; font-size: 22rpx; color: #999; }
.search-empty { text-align: center; color: #999; padding: 60rpx 0; font-size: 28rpx; }

.bottom-panel { position: absolute; bottom: 0; left: 0; right: 0; background: #FDFCF8; border-radius: 32rpx 32rpx 0 0; padding: 0 0 40rpx; box-shadow: 0 -4rpx 20rpx rgba(163,59,50,0.10); z-index: 100; }
.panel-drag-bar { width: 80rpx; height: 8rpx; background: #E0E0E0; border-radius: 4rpx; margin: 16rpx auto; }
.panel-content { padding: 0 30rpx; }
.panel-header { display: flex; align-items: center; gap: 20rpx; margin-bottom: 30rpx; }
.panel-icon-wrap { width: 80rpx; height: 80rpx; background: #E8F4FD; border-radius: 20rpx; display: flex; align-items: center; justify-content: center; flex-shrink: 0; }
.panel-icon { width: 44rpx; height: 44rpx; }
.panel-image-wrap { width: 120rpx; height: 90rpx; border-radius: 16rpx; overflow: hidden; flex-shrink: 0; box-shadow: 0 2rpx 12rpx rgba(0,0,0,0.1); }
.panel-image { width: 100%; height: 100%; }
.panel-info { flex: 1; min-width: 0; }
.panel-name { display: block; font-size: 32rpx; font-weight: bold; color: #1D3B5A; }
.panel-type { display: block; font-size: 24rpx; color: #999; }
.panel-address { display: block; font-size: 22rpx; color: #666; margin-top: 4rpx; }
.panel-close {
  width: 60rpx;
  height: 60rpx;
  border-radius: 50%;
  background: #F8F9FA;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 28rpx;
  color: #666;
  margin: -10rpx -10rpx -10rpx 0;
}
.panel-actions { display: flex; justify-content: space-around; }
.action-btn { display: flex; flex-direction: column; align-items: center; gap: 10rpx; padding: 16rpx 24rpx; }
.action-icon { font-size: 44rpx; }
.action-icon,
.route-fab-icon,
.offline-fab-icon,
.locate-icon,
.nav-opt-icon {
  width: 40rpx;
  height: 40rpx;
}
.action-label { font-size: 24rpx; color: #333; }

.checkin-section { padding: 20rpx 0 0; }
.checkin-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10rpx;
  background: linear-gradient(135deg, #FF7F50 0%, #E8633A 100%);
  border-radius: 50rpx;
  padding: 18rpx 0;
  box-shadow: 0 4rpx 16rpx rgba(255,127,80,0.3);
}
.checkin-btn.collected {
  background: linear-gradient(135deg, #4A90E2 0%, #2E5F8E 100%);
  box-shadow: 0 4rpx 16rpx rgba(74,144,226,0.3);
}
.checkin-btn-icon-img { width: 32rpx; height: 32rpx; }
.checkin-btn-text { font-size: 26rpx; font-weight: 600; color: #FFFFFF; }

.fab-group { position: absolute; bottom: 60rpx; left: 30rpx; display: flex; flex-direction: column; gap: 20rpx; z-index: 100; }

.route-fab { background: #1D3B5A; border-radius: 50rpx; padding: 20rpx 30rpx; display: flex; align-items: center; gap: 10rpx; box-shadow: 0 4rpx 20rpx rgba(163,59,50,0.15); }
.route-fab-icon { font-size: 32rpx; }
.route-fab-text { font-size: 26rpx; color: #FDFCF8; }

.locate-btn { position: absolute; bottom: 60rpx; right: 30rpx; width: 88rpx; height: 88rpx; background: #FDFCF8; border-radius: 50%; display: flex; align-items: center; justify-content: center; font-size: 40rpx; box-shadow: 0 4rpx 20rpx rgba(163,59,50,0.10); z-index: 100; }
</style>


