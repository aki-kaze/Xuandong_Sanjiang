<template>
  <view class="route-page">
    <!-- 自定义路线入口（放在最上方） -->
    <view class="custom-route-card" @tap="goCustomRoute">
      <view class="custom-left">
        <image class="icon-img-lg" src="/static/icons/detail.png" mode="aspectFit" />
        <view class="custom-info">
          <text class="custom-title">自定义路线规划</text>
          <text class="custom-sub">自己规划路线，定制专属旅游路线</text>
        </view>
      </view>
      <text class="custom-arrow">></text>
    </view>

    <!-- 保存的自定义路线列表 -->
    <view class="custom-routes-section" v-if="customRoutes.length > 0">
      <view class="section-title">
        <text>我的路线</text>
      </view>
      <view class="route-list">
        <view class="route-card custom-card" v-for="item in customRoutes" :key="item.id" @tap="showDetail(item)">
          <view class="route-cover custom-cover">
            <image class="route-cover-bg" v-if="item.cover" :src="item.cover" mode="aspectFill" />
            <view class="route-cover-overlay" v-if="item.cover"></view>
            <view class="route-cover-info">
              <text class="route-cover-name">{{ item.name }}</text>
              <view class="route-cover-tags">
                <text class="cover-tag" v-for="tag in item.tags" :key="tag">{{ tag }}</text>
              </view>
            </view>
          </view>
          <view class="route-info">
            <view class="route-meta-row">
              <view class="route-meta-item">
                <image class="icon-img" src="/static/icons/duration.png" mode="aspectFit" />
                <text class="meta-text">{{ item.duration }}</text>
              </view>
              <view class="route-meta-item">
                <image class="icon-img" src="/static/icons/difficulty.png" mode="aspectFit" />
                <text class="meta-text">{{ item.difficulty }}</text>
              </view>
              <view class="route-meta-item">
                <image class="icon-img" src="/static/icons/position.png" mode="aspectFit" />
                <text class="meta-text">{{ item.stops.length }}个景点</text>
              </view>
            </view>
            <view class="stops-preview">
              <view class="stop-dot" v-for="(stop, idx) in item.stops.slice(0,4)" :key="idx">
                <view class="dot" :class="{ first: idx === 0, last: idx === item.stops.length - 1 }"></view>
                <view class="dot-line" v-if="idx < 3"></view>
                <text class="stop-name">{{ stop.name.length > 4 ? stop.name.slice(0,4)+'...' : stop.name }}</text>
              </view>
              <text class="stops-more" v-if="item.stops.length > 4">+{{ item.stops.length - 4 }}</text>
            </view>
            <view class="route-footer">
              <text class="route-price" v-if="item.price > 0">¥{{ item.price }}</text>
              <view class="delete-btn" @tap.stop="deleteCustomRoute(item.id)">删除</view>
            </view>
          </view>
        </view>
      </view>
    </view>

    <!-- 推荐路线标题 -->
    <view class="section-title">
      <text>推荐路线</text>
    </view>

    <!-- 路线筛选 -->
    <view class="filter-bar">
      <view
        class="filter-item"
        v-for="f in filters"
        :key="f.id"
        :class="{ active: activeFilter === f.id }"
        @tap="handleFilterChange(f.id)"
      >{{ f.label }}</view>
    </view>

    <!-- 推荐路线列表 -->
    <view class="route-list">
      <view class="empty-tip" v-if="filteredRoutes.length === 0">
        <text>暂无路线数据</text>
        <text style="font-size: 20rpx; color: #94A3B8; display: block; margin-top: 8rpx;">
          当前筛选: {{ activeFilter }} | 总数据: {{ routes.length }}
        </text>
      </view>
      <view class="route-card" v-for="item in filteredRoutes" :key="item.id" @tap="showDetail(item)">
        <view class="route-cover">
          <image class="route-cover-bg" :src="item.cover" mode="aspectFill" />
          <view class="route-cover-overlay"></view>
          <view class="route-cover-info">
            <text class="route-cover-name">{{ item.name }}</text>
            <view class="route-cover-tags">
              <text class="cover-tag" v-for="tag in item.tags" :key="tag">{{ tag }}</text>
            </view>
          </view>
        </view>
        <view class="route-info">
          <view class="route-meta-row">
            <view class="route-meta-item">
              <image class="icon-img" src="/static/icons/duration.png" mode="aspectFit" />
              <text class="meta-text">{{ item.duration }}</text>
            </view>
            <view class="route-meta-item">
              <image class="icon-img" src="/static/icons/distance.png" mode="aspectFit" />
              <text class="meta-text">{{ item.distance }}</text>
            </view>
            <view class="route-meta-item">
              <image class="icon-img" src="/static/icons/difficulty.png" mode="aspectFit" />
              <text class="meta-text">{{ item.difficulty }}</text>
            </view>
          </view>
          <view class="stops-preview">
            <view class="stop-dot" v-for="(stop, idx) in item.stops.slice(0,4)" :key="idx">
              <view class="dot" :class="{ first: idx === 0, last: idx === item.stops.length - 1 }"></view>
              <view class="dot-line" v-if="idx < 3"></view>
              <text class="stop-name">{{ stop.name.length > 4 ? stop.name.slice(0,4)+'...' : stop.name }}</text>
            </view>
            <text class="stops-more" v-if="item.stops.length > 4">+{{ item.stops.length - 4 }}</text>
          </view>
          <view class="route-footer">
            <text class="route-price">¥{{ item.price }}/人起</text>
            <view class="route-btns">
              <view class="route-btn-fill" @tap.stop="goTicket(item)">立即预订</view>
            </view>
          </view>
        </view>
      </view>
    </view>

    <view style="height: 40rpx;"></view>

    <view class="detail-modal" v-if="showModal">
      <view class="detail-box">
        <view class="detail-header" :style="{ background: selectedRoute.isCustom ? '#FF7F50' : coverColors[selectedRoute.id % coverColors.length] }">
          <text class="detail-title">{{ selectedRoute.name }}</text>
          <text class="detail-close" @tap="showModal = false">关闭</text>
        </view>
        <scroll-view scroll-y class="detail-scroll">
          <view class="detail-meta">
            <view class="detail-meta-item">
              <text class="detail-meta-val">{{ selectedRoute.duration }}</text>
              <text class="detail-meta-key">行程时长</text>
            </view>
            <view class="detail-meta-item" v-if="!selectedRoute.isCustom">
              <text class="detail-meta-val">{{ selectedRoute.distance }}</text>
              <text class="detail-meta-key">总距离</text>
            </view>
            <view class="detail-meta-item">
              <text class="detail-meta-val">{{ selectedRoute.difficulty }}</text>
              <text class="detail-meta-key">难度</text>
            </view>
            <view class="detail-meta-item" v-if="!selectedRoute.isCustom">
              <text class="detail-meta-val" style="color:#A33B32">¥{{ selectedRoute.price }}</text>
              <text class="detail-meta-key">参考价格</text>
            </view>
          </view>
          <view class="detail-stops">
            <text class="detail-stops-title">行程安排</text>
            <view class="stop-item" v-for="(stop, idx) in selectedRoute.stops" :key="idx">
              <view class="stop-left">
                <view class="stop-num">{{ idx + 1 }}</view>
                <view class="stop-line" v-if="idx < selectedRoute.stops.length - 1"></view>
              </view>
              <view class="stop-content">
                <view class="stop-header">
                  <text class="stop-name-full">{{ stop.name }}</text>
                  <text class="stop-time">{{ stop.time }}</text>
                </view>
                <view class="stop-meta-row">
                  <text class="stop-duration" v-if="stop.duration">
                    <image class="icon-img-sm" src="/static/icons/duration.png" mode="aspectFit" />
                    {{ stop.duration }}
                  </text>
                  <text class="stop-transport" v-if="stop.transport">
                    {{ getTransportIcon(stop.transport) }} {{ getTransportLabel(stop.transport) }}
                  </text>
                  <text class="stop-ticket" v-if="stop.ticketPrice !== undefined && stop.ticketPrice !== null">
                    <image class="icon-img-sm" src="/static/icons/ticket.png" mode="aspectFit" />
                    {{ stop.ticketPrice > 0 ? '¥' + stop.ticketPrice : '免费' }}
                  </text>
                </view>
                <text class="stop-desc">{{ stop.desc }}</text>
              </view>
            </view>
          </view>
        </scroll-view>
        <view class="detail-actions" v-if="!selectedRoute.isCustom">
          <view class="detail-btn-book" @tap="goTicket(selectedRoute)"><image class="icon-img" src="/static/icons/ticket.png" mode="aspectFit" /> 立即预订</view>
        </view>
        <view class="detail-actions" v-else>
          <view class="detail-btn-nav" @tap="showModal = false">关闭</view>
        </view>
      </view>
    </view>
  </view>
</template>

<script>
import { staticData } from '../../utils/static-data.js'
import { routeApi, userRouteApi } from '../../utils/api.js'
import staticAttractions from '../../data/attractions.json'

export default {
  data() {
    return {
      routes: [],
      customRoutes: [],
      activeFilter: 'all',
      filters: [
        { id: 'all', label: '全部路线' },
        { id: '1天', label: '一日游' },
        { id: '2天', label: '两日游' },
        { id: '3天', label: '三日游' }
      ],
      coverColors: ['linear-gradient(135deg, #4A90E2, #357ABD)', 'linear-gradient(135deg, #FF7F50, #E65C3C)', 'linear-gradient(135deg, #6BA5F0, #4A90E2)'],
      showModal: false,
      selectedRoute: {},
      attractionsMap: {},
      loading: false,
      dataSource: 'static'
    }
  },
  computed: {
    filteredRoutes() {
      if (this.activeFilter === 'all') {
        return this.routes
      }
      return this.routes.filter(r => String(r.duration || '') === String(this.activeFilter))
    }
  },
  onLoad() {
    this.initAttractionsMap()
    this.loadRoutes()
    this.loadCustomRoutes()
  },
  onShow() {
    // 每次页面显示时重新加载路线数据，确保与后端同步
    this.loadRoutes()
    this.loadCustomRoutes()
  },
  methods: {
    handleFilterChange(filterId) {
      this.activeFilter = filterId
    },
    async loadRoutes() {
      this.loading = true
      try {
        const result = await routeApi.getList(1, 50)
        const list = result.records || result.list || result || []
        // 优先使用后端数据，即使为空也使用后端返回的结果
        this.routes = list.map(item => {
          if (typeof item.stops === 'string') {
            try { item.stops = JSON.parse(item.stops) } catch (e) { item.stops = [] }
          }
          if (typeof item.tags === 'string') {
            try { item.tags = JSON.parse(item.tags) } catch (e) { item.tags = [] }
          }
          // 统一 duration 格式为 "N 天"
          item.duration = this.normalizeDuration(item.duration, item.days)
          return item
        })
        this.dataSource = 'api'
        console.log('路线数据加载成功（后端 API）:', this.routes.length + '条')
      } catch (error) {
        // 只有当 API 调用真正失败时（网络错误、服务器错误等），才使用静态数据
        console.error('API 调用失败，使用静态数据:', error.message || error)
        this.loadStaticData()
      } finally {
        this.loading = false
      }
    },
    loadStaticData() {
      this.routes = (staticData.routes || []).map(item => {
        if (typeof item.tags === 'string') {
          try { item.tags = JSON.parse(item.tags) } catch (e) { item.tags = [] }
        }
        item.duration = this.normalizeDuration(item.duration, item.days)
        return item
      })
      this.dataSource = 'static'
      console.log('路线数据加载成功（静态数据）')
    },
    normalizeDuration(duration, days) {
      // 从 duration 或 days 字段提取天数，统一为 "N天" 格式
      if (duration && /^\d+天$/.test(duration)) return duration
      const num = days || (duration ? parseInt(duration) : 0)
      return num ? num + '天' : '1天'
    },
    initAttractionsMap() {
      this.attractionsMap = {}
      staticAttractions.forEach(a => {
        this.attractionsMap[a.name] = a
      })
    },
    async loadCustomRoutes() {
      const userInfo = uni.getStorageSync('userInfo') || {}
      const userId = userInfo.userId || userInfo.id

      let apiRoutes = []

      if (userId) {
        try {
          const res = await userRouteApi.getMyRoutes(userId)
          const list = Array.isArray(res) ? res : (res.records || res.list || [])
          apiRoutes = list.map(item => this.mapUserRouteToRoute(item))
        } catch (e) {
          console.warn('从后端加载自定义路线失败，使用本地数据', e)
        }
      }

      // 从本地存储加载（兼容 savedRoutes 和 customRoutes 两个键）
      let localRoutes = []
      try {
        const saved = uni.getStorageSync('savedRoutes') || []
        const custom = uni.getStorageSync('customRoutes') || []
        const allLocal = [...custom, ...saved]
        localRoutes = allLocal.map(item => {
          // 如果已经是最终渲染格式（有 duration 和 stops 数组），直接使用
          if (item.duration && Array.isArray(item.stops)) {
            return {
              id: item.id,
              name: item.name || '未命名路线',
              duration: item.duration,
              difficulty: item.difficulty || '自定义',
              price: item.price || 0,
              distance: item.distance || '-',
              tags: Array.isArray(item.tags) ? item.tags : [],
              isCustom: true,
              stops: item.stops,
              cover: item.cover || '',
              userId: item.userId
            }
          }
          // 否则走转换逻辑（自定义路线规划保存的格式）
          return this.mapUserRouteToRoute(item)
        })
      } catch (e) {
        console.log('No local custom routes', e)
      }

      // 合并：本地数据 + 后端数据，去重（按 name）
      const merged = [...localRoutes]
      const mergedNames = new Set(localRoutes.map(r => r.name))
      apiRoutes.forEach(r => {
        if (!mergedNames.has(r.name)) {
          merged.push(r)
        }
      })
      this.customRoutes = merged
    },
    mapUserRouteToRoute(item) {
      // 解析 daily 字段（user_routes 表使用 daily 存储每日行程）
      let stops = []
      let tags = []
      let dailyData = []
      
      // 先尝试从 stops 字段解析
      try {
        stops = typeof item.stops === 'string' ? JSON.parse(item.stops) : (item.stops || [])
      } catch { stops = [] }
      
      // 如果 stops 为空，尝试从 daily 解析
      if (stops.length === 0 && item.daily) {
        try {
          dailyData = typeof item.daily === 'string' ? JSON.parse(item.daily) : item.daily
          stops = dailyData.flatMap(day => day.stops || [])
        } catch { stops = [] }
      }

      try { tags = typeof item.tags === 'string' ? JSON.parse(item.tags) : (item.tags || []) } catch { tags = [] }

      // 解析 attractions 字段用于获取封面
      let attractions = []
      try {
        attractions = typeof item.attractions === 'string' ? JSON.parse(item.attractions) : (item.attractions || [])
      } catch { attractions = [] }

      // 获取封面 - 优先从 daily 中的第一个景点获取
      let cover = item.cover || ''
      
      // 方法 1：从 daily 数据获取第一个景点的 cover
      if (!cover && dailyData.length > 0) {
        const firstDay = dailyData[0]
        if (firstDay.stops && firstDay.stops.length > 0) {
          const firstStop = firstDay.stops[0]
          // 尝试从 stop.cover 获取
          if (firstStop.cover) {
            cover = firstStop.cover
          }
        }
      }
      
      // 方法 2：从 stops 获取第一个景点的 cover
      if (!cover && stops.length > 0) {
        const firstStop = stops[0]
        if (firstStop.cover) {
          cover = firstStop.cover
        }
        // 尝试从 attractionsMap 获取
        if (!cover && firstStop.name && this.attractionsMap[firstStop.name]) {
          cover = this.attractionsMap[firstStop.name].cover || ''
        }
      }

      // 方法 3：从 attractions 获取
      if (!cover && attractions.length > 0) {
        const firstAttr = this.attractionsMap[attractions[0].name]
        if (firstAttr) {
          cover = firstAttr.cover || ''
        }
      }

      return {
        id: item.id,
        name: item.name || '未命名路线',
        duration: item.days ? `${item.days} 天` : '1 天',
        difficulty: item.difficulty || '自定义',
        price: item.budget ? this.parseBudget(item.budget) : 0,
        distance: item.distance || '-',
        tags: tags,
        isCustom: true,
        stops: stops,
        cover: cover,
        userId: item.userId
      }
    },
    parseBudget(budget) {
      if (typeof budget === 'string') {
        const match = budget.match(/\d+/)
        return match ? parseInt(match[0]) : 0
      }
      return budget || 0
    },
    async deleteCustomRoute(id) {
      uni.showModal({
        title: '提示',
        content: '确定删除这条路线吗？删除后无法恢复',
        success: async (res) => {
          if (res.confirm) {
            try {
              // 从两个本地存储键中删除
              const saved = uni.getStorageSync('savedRoutes') || []
              uni.setStorageSync('savedRoutes', saved.filter(r => r.id !== id))
              const custom = uni.getStorageSync('customRoutes') || []
              uni.setStorageSync('customRoutes', custom.filter(r => r.id !== id))
              // 只有后端自增 id（数字较小）才调后端删除，时间戳 id 是本地数据
              if (id && id < 1000000000000) {
                try { await userRouteApi.delete(id) } catch (e) {}
              }
              await this.loadCustomRoutes()
              uni.showToast({ title: '删除成功', icon: 'success' })
            } catch (e) {
              console.error('删除失败:', e)
              uni.showToast({ title: '删除失败，请重试', icon: 'none' })
            }
          }
        }
      })
    },
    showDetail(item) {
      // 自定义路线（isCustom=true）通过 routeData 传递数据
      if (item.isCustom) {
        const routeData = encodeURIComponent(JSON.stringify(item))
        uni.navigateTo({ url: `/pages/route/route-detail?routeData=${routeData}` })
      } else if (item.id && this.dataSource === 'api') {
        // 推荐路线从 API 加载
        uni.navigateTo({ url: `/pages/route/route-detail?id=${item.id}` })
      } else {
        // 其他情况通过 routeData 传递
        const routeData = encodeURIComponent(JSON.stringify(item))
        uni.navigateTo({ url: `/pages/route/route-detail?routeData=${routeData}` })
      }
    },
    goMap() { uni.switchTab({ url: '/pages/map/map' }) },
    goAI() { uni.switchTab({ url: '/pages/ai/ai' }) },
    goCustomRoute() { uni.navigateTo({ url: '/pages/route/custom-route' }) },
    goTicket(route) {
      const routeData = encodeURIComponent(JSON.stringify(route))
      uni.navigateTo({ url: `/pages/ticket/ticket?tab=route&route=${routeData}` })
    },
    getTransportIcon(transport) {
      const icons = {
        walking: '🚶',
        bus: '🚌',
        driving: '🚗',
        cablecar: '🚡'
      }
      return icons[transport] || '🚶'
    },
    getTransportLabel(transport) {
      const labels = {
        walking: '步行',
        bus: '公交',
        driving: '自驾',
        cablecar: '缆车'
      }
      return labels[transport] || '步行'
    },
    goCustomRoute() { uni.navigateTo({ url: '/pages/route/custom-route' }) },
    goTicket(item) {
      this.showModal = false
      const routeData = encodeURIComponent(JSON.stringify(item))
      uni.navigateTo({ url: `/pages/ticket/ticket?tab=route&route=${routeData}` })
    },
    openTencentMapNav(route) {
      try {
        if (!route.stops || route.stops.length === 0) {
          uni.showToast({ title: '路线站点为空', icon: 'none' })
          return
        }

        const firstStop = route.stops[0]
        const lastStop = route.stops[route.stops.length - 1]

        let startPoint = null
        let endPoint = null

        if (firstStop && this.attractionsMap[firstStop.name]) {
          const a = this.attractionsMap[firstStop.name]
          startPoint = {
            latitude: parseFloat(a.lat),
            longitude: parseFloat(a.lng),
            name: a.name
          }
        }

        if (lastStop && this.attractionsMap[lastStop.name]) {
          const a = this.attractionsMap[lastStop.name]
          endPoint = {
            latitude: parseFloat(a.lat),
            longitude: parseFloat(a.lng),
            name: a.name
          }
        }

        if (!endPoint) {
          uni.showToast({ title: '未找到景点位置信息', icon: 'none' })  
          return
        }

        if (!startPoint) {
          uni.getLocation({
            type: 'gcj02',
            success: (res) => {
              startPoint = {
                latitude: res.latitude,
                longitude: res.longitude,
                name: '我的位置'
              }
              this.openRoutePlanPlugin(startPoint, endPoint)
            },
            fail: () => {
              startPoint = {
                latitude: 25.7834,
                longitude: 109.6078,
                name: '三江鼓楼'
              }
              this.openRoutePlanPlugin(startPoint, endPoint)
            }
          })
        } else {
          this.openRoutePlanPlugin(startPoint, endPoint)
        }

      } catch (e) {
        console.error('打开路线规划失败:', e)
        uni.showToast({ title: '打开路线规划失败', icon: 'none' })
      }
    },
    openRoutePlanPlugin(startPoint, endPoint) {
      try {
        const plugin = requirePlugin('route-plan')
        const key = 'OB4BZ-D4W3U-B7VVO-4PJWW-6TKDJ-WPB77'
        const referer = '寻侗三江'

        plugin.open({
          key: key,
          referer: referer,
          endPoint: endPoint,
          startPoint: startPoint,
          mode: 'driving',
          success: function(res) {
            console.log('路线规划成功:', res)
          },
          fail: function(err) {
            console.error('路线规划失败:', err)
            uni.showToast({ title: '路线规划失败', icon: 'none' })
          }
        })
      } catch (e) {
        console.error('加载路线规划插件失败:', e)
        
        uni.openLocation({
          latitude: endPoint.latitude,
          longitude: endPoint.longitude,
          name: endPoint.name,
          success: () => {},
          fail: () => {
            uni.showToast({ title: '打开地图失败', icon: 'none' })
          }
        })
      }
    }
  }
}
</script>

<style scoped lang="scss">
/* ===== 统一图标尺寸 ===== */
.icon-img { width: 32rpx; height: 32rpx; flex-shrink: 0; }
.icon-img-lg { width: 52rpx; height: 52rpx; flex-shrink: 0; }
.icon-img-sm { width: 24rpx; height: 24rpx; flex-shrink: 0; }

.route-page { background: #F8FAFC; min-height: 100vh; padding-bottom: 40rpx; }

.section-title {
  padding: 20rpx 24rpx 10rpx;
}
.section-title text {
  font-size: 30rpx;
  font-weight: 600;
  color: #2C3E50;
}

.custom-route-card {
  margin: 20rpx 24rpx;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 20rpx;
  padding: 32rpx 30rpx;
  display: flex;
  align-items: center;
  justify-content: space-between;
  box-shadow: 0 8rpx 32rpx rgba(102,126,234,0.35);
  position: relative;
  overflow: hidden;
}
.custom-route-card::before {
  content: '';
  position: absolute;
  top: -50%;
  right: -20%;
  width: 200rpx;
  height: 200rpx;
  background: rgba(255,255,255,0.1);
  border-radius: 50%;
}
.custom-route-card::after {
  content: '';
  position: absolute;
  bottom: -30%;
  left: -10%;
  width: 150rpx;
  height: 150rpx;
  background: rgba(255,255,255,0.08);
  border-radius: 50%;
}
.custom-left { display: flex; align-items: center; gap: 20rpx; position: relative; z-index: 1; }
.custom-icon { font-size: 0; }
.icon-img-lg { filter: brightness(0) invert(1); }
.custom-info { display: flex; flex-direction: column; }
.custom-title { display: block; font-size: 32rpx; font-weight: 700; color: #FFFFFF; margin-bottom: 8rpx; text-shadow: 0 2rpx 8rpx rgba(0,0,0,0.2); }
.custom-sub { display: block; font-size: 24rpx; color: rgba(255,255,255,0.9); }
.custom-arrow { font-size: 48rpx; color: rgba(255,255,255,0.8); position: relative; z-index: 1; font-weight: 300; }

.filter-bar { display: flex; gap: 16rpx; padding: 10rpx 24rpx 20rpx; background: #FFFFFF; overflow-x: auto; }
.filter-item { white-space: nowrap; padding: 16rpx 32rpx; border-radius: 30rpx; font-size: 26rpx; color: #64748B; background: #F1F5F9; transition: all 0.3s ease; }
.filter-item.active { background: linear-gradient(135deg, #4A90E2, #357ABD); color: #FFFFFF; }

.route-list { padding: 0 24rpx 20rpx; display: flex; flex-direction: column; gap: 24rpx; }
.route-card { background: #FFFFFF; border-radius: 20rpx; overflow: hidden; box-shadow: 0 4rpx 20rpx rgba(74,144,226,0.12); }
.route-cover { height: 240rpx; display: flex; align-items: flex-end; padding: 24rpx; position: relative; overflow: hidden; }
.custom-cover { background: linear-gradient(135deg, #4A90E2, #357ABD); }
.route-cover-bg { position: absolute; top: 0; left: 0; width: 100%; height: 100%; z-index: 0; }
.route-cover-overlay { position: absolute; top: 0; left: 0; width: 100%; height: 100%; background: linear-gradient(to bottom, rgba(0,0,0,0.1) 0%, rgba(0,0,0,0.6) 100%); z-index: 1; }
.route-cover-info { position: relative; z-index: 2; }
.route-cover-name { display: block; font-size: 36rpx; font-weight: 700; color: #FFFFFF; margin-bottom: 10rpx; text-shadow: 0 2rpx 8rpx rgba(0,0,0,0.5); }
.route-cover-tags { display: flex; gap: 10rpx; flex-wrap: wrap; }
.cover-tag { font-size: 22rpx; color: rgba(255,255,255,0.95); background: rgba(255,255,255,0.25); padding: 6rpx 16rpx; border-radius: 24rpx; backdrop-filter: blur(4rpx); }
.route-info { padding: 24rpx; }
.route-meta-row { display: flex; gap: 30rpx; margin-bottom: 20rpx; }
.route-meta-item { display: flex; align-items: center; gap: 8rpx; }
.meta-text { font-size: 24rpx; color: #64748B; }

.stops-preview { display: flex; align-items: center; margin-bottom: 20rpx; overflow: hidden; }
.stop-dot { display: flex; flex-direction: column; align-items: center; position: relative; }
.dot { width: 16rpx; height: 16rpx; border-radius: 50%; background: #FF7F50; margin-bottom: 8rpx; }
.dot.first { background: #4A90E2; }
.dot.last { background: #4A90E2; }
.dot-line { width: 60rpx; height: 2rpx; background: #E2E8F0; position: absolute; top: 7rpx; left: 16rpx; }
.stop-name { font-size: 20rpx; color: #64748B; text-align: center; }
.stops-more { font-size: 22rpx; color: #FF7F50; margin-left: 10rpx; }

.route-footer { display: flex; justify-content: space-between; align-items: center; }
.route-price { font-size: 32rpx; font-weight: 600; color: #4A90E2; }
.route-btns { display: flex; gap: 16rpx; }
.route-btn-outline { font-size: 24rpx; color: #1D3B5A; border: 1rpx solid #1D3B5A; padding: 12rpx 24rpx; border-radius: 30rpx; display: flex; align-items: center; gap: 6rpx; }
.route-btn-fill { font-size: 24rpx; color: #FFFFFF; background: linear-gradient(135deg, #1D3B5A, #2E5F8E); padding: 12rpx 24rpx; border-radius: 30rpx; box-shadow: 0 4rpx 12rpx rgba(29,59,90,0.3); }

.delete-btn {
  padding: 12rpx 32rpx;
  background: #F1F5F9;
  border-radius: 30rpx;
  font-size: 24rpx;
  color: #4A90E2;
}

.empty-tip {
  text-align: center;
  padding: 80rpx 24rpx;
  color: #94A3B8;
  font-size: 26rpx;
}

.detail-modal { position: fixed; top: 0; left: 0; right: 0; bottom: 0; background: rgba(0,0,0,0.5); display: flex; align-items: flex-end; z-index: 500; }
.detail-box { width: 100%; height: 85vh; background: #FFFFFF; border-radius: 32rpx 32rpx 0 0; display: flex; flex-direction: column; overflow: hidden; }
.detail-header { padding: 30rpx; display: flex; justify-content: space-between; align-items: center; background: linear-gradient(135deg, #1D3B5A, #2E5F8E); }
.detail-title { font-size: 34rpx; font-weight: 600; color: #FFFFFF; }
.detail-close { font-size: 36rpx; color: rgba(255,255,255,0.8); }
.detail-scroll { flex: 1; }
.detail-meta { display: grid; grid-template-columns: repeat(4, 1fr); padding: 24rpx; border-bottom: 1rpx solid #F1F5F9; }
.detail-meta-item { display: flex; flex-direction: column; align-items: center; gap: 6rpx; }
.detail-meta-val { font-size: 28rpx; font-weight: 600; color: #2C3E50; }
.detail-meta-key { font-size: 22rpx; color: #94A3B8; }
.detail-stops { padding: 24rpx; }
.detail-stops-title { display: block; font-size: 28rpx; font-weight: 600; color: #2C3E50; margin-bottom: 24rpx; }
.stop-item { display: flex; gap: 20rpx; margin-bottom: 24rpx; }
.stop-left { display: flex; flex-direction: column; align-items: center; }
.stop-num { width: 48rpx; height: 48rpx; border-radius: 50%; background: linear-gradient(135deg, #1D3B5A, #2E5F8E); color: #FFFFFF; display: flex; align-items: center; justify-content: center; font-size: 22rpx; font-weight: 600; flex-shrink: 0; }
.stop-line { flex: 1; width: 2rpx; background: #E2E8F0; margin: 8rpx 0; min-height: 40rpx; }
.stop-content { flex: 1; padding-bottom: 10rpx; }
.stop-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 8rpx; }
.stop-name-full { font-size: 28rpx; font-weight: 600; color: #334155; }
.stop-time { font-size: 22rpx; color: #FF7F50; }
.stop-duration { display: flex; align-items: center; gap: 4rpx; font-size: 22rpx; color: #94A3B8; margin-bottom: 6rpx; }
.stop-desc { display: block; font-size: 24rpx; color: #64748B; line-height: 1.6; }
.detail-actions { display: flex; padding: 20rpx 30rpx 40rpx; border-top: 1rpx solid #F1F5F9; }
.detail-btn-book { flex: 1; text-align: center; padding: 24rpx; background: #4A90E2; border-radius: 50rpx; font-size: 28rpx; color: #FFFFFF; font-weight: 600; display: flex; align-items: center; justify-content: center; gap: 8rpx; }
</style>
