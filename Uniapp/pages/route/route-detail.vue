<template>
  <view class="route-detail-page">
    <!-- 顶部封面 -->
    <view class="detail-header">
      <image class="detail-header-bg" :src="getRouteCover()" mode="aspectFill" />
      <view class="detail-header-overlay"></view>
      
      <view class="detail-header-content">
        <text class="detail-title">{{ route.name || '暂无路线名称' }}</text>
        <view class="detail-tags" v-if="route.tags && route.tags.length">
          <text class="detail-tag" v-for="tag in route.tags" :key="tag">{{ tag }}</text>
        </view>
      </view>
    </view>

    <scroll-view scroll-y class="content">
      <!-- 概览卡片 -->
      <view class="overview-card">
        <view class="overview-metrics">
          <view class="metric">
            <image class="metric-icon-img" src="/static/icons/duration.png" mode="aspectFit" />
            <text class="metric-val">{{ route.duration || '0天' }}</text>
            <text class="metric-key">行程时长</text>
          </view>
          <view class="metric">
            <image class="metric-icon-img" src="/static/icons/distance.png" mode="aspectFit" />
            <text class="metric-val">{{ route.distance || '0km' }}</text>
            <text class="metric-key">总距离</text>
          </view>
          <view class="metric">
            <image class="metric-icon-img" src="/static/icons/difficulty.png" mode="aspectFit" />
            <text class="metric-val">{{ route.difficulty || '普通' }}</text>
            <text class="metric-key">难度</text>
          </view>
          <view class="metric">
            <image class="metric-icon-img" src="/static/icons/cost.png" mode="aspectFit" />
            <text class="metric-val">¥{{ totalCost }}/人</text>
            <text class="metric-key">预估费用</text>
          </view>
        </view>
      </view>

      <!-- Tab 切换 -->
      <view class="tab-bar">
        <view
          class="tab-item"
          v-for="tab in tabs"
          :key="tab.id"
          :class="{ active: activeTab === tab.id }"
          @tap="activeTab = tab.id"
        >
          <image class="tab-icon-img" :src="tab.icon" mode="aspectFit" />
          <text class="tab-label">{{ tab.label }}</text>
        </view>
      </view>

      <!-- 行程安排 -->
      <view class="tab-content" v-if="activeTab === 'itinerary'">
        <view class="stops-timeline">
          <view class="stop-card" v-for="(stop, idx) in route.stops" :key="idx">
            <view class="stop-time-col">
              <text class="stop-time-text">{{ stop.time || '00:00' }}</text>
              <view class="stop-dot-line">
                <view class="stop-dot" :class="{ first: idx === 0, last: idx === route.stops.length - 1 }"></view>
                <view class="stop-connector" v-if="idx < route.stops.length - 1"></view>
              </view>
            </view>
            <view class="stop-body">
              <text class="stop-name">{{ stop.name || '未知景点' }}</text>
              <text class="stop-duration" v-if="stop.duration">
                <image class="duration-icon" src="/static/icons/duration.png" mode="aspectFit" />
                {{ stop.duration }}
              </text>
              <text class="stop-desc">{{ stop.desc || '暂无介绍' }}</text>

              <!-- 交通方式-->
              <view class="transport-section" v-if="idx > 0">
                <view class="section-label">
                  <image class="label-icon-img" src="/static/icons/drive.png" mode="aspectFit" />
                  <text class="label-text">交通方式</text>
                </view>
                <view class="transport-options">
                  <view
                    class="transport-opt"
                    v-for="t in transportOptions"
                    :key="t.id"
                    :class="{ selected: stopTransport[idx] === t.id }"
                    @tap="selectTransport(idx, t.id)"
                  >
                    <image class="transport-icon-img" :src="t.icon" mode="aspectFit" />
                    <text class="transport-name">{{ t.name }}</text>
                    <text class="transport-time">{{ t.getTime(route.stops[idx-1], stop) }}</text>
                    <text class="transport-cost" v-if="t.getCost(route.stops[idx-1], stop) > 0">¥{{ t.getCost(route.stops[idx-1], stop) }}</text>
                  </view>
                </view>
              </view>

              <!-- 景点导航按钮 -->
              <view class="nav-btn" @tap="navigateTo(stop)">
                <image class="nav-icon" src="/static/icons/explore.png" mode="aspectFit" />
                <text>导航到此景点</text>
              </view>
            </view>
          </view>
          <view class="empty-tip" v-if="!route.stops || route.stops.length === 0">
            <text>暂无行程安排</text>
          </view>
        </view>
      </view>

      <!-- 住宿推荐 -->
      <view class="tab-content" v-if="activeTab === 'hotel'">
        <view class="hotel-list">
          <view class="hotel-card" v-for="hotel in nearbyHotels" :key="hotel.id" @tap="selectHotel(hotel)">
            <image class="hotel-img" :src="hotel.cover" mode="aspectFill" />
            <view class="hotel-info">
              <text class="hotel-name">{{ hotel.name }}</text>
              <text class="hotel-type">{{ hotel.type }}</text>
              <view class="hotel-rating">
                <text class="rating-stars">⭐{{ hotel.rating }}</text>
                <text class="rating-count">({{ hotel.reviewCount }}条评论)</text>
              </view>
              <view class="hotel-tags" v-if="hotel.tags && hotel.tags.length">
                <text class="hotel-tag" v-for="tag in hotel.tags" :key="tag">{{ tag }}</text>
              </view>
              <view class="hotel-price-row">
                <text class="hotel-price">¥{{ hotel.price }}</text>
                <text class="hotel-price-unit">/晚</text>
                <view class="hotel-book-btn" @tap.stop="bookHotel(hotel)">立即预订</view>
              </view>
            </view>
          </view>
          <view class="empty-tip" v-if="nearbyHotels.length === 0">
            <text>暂无推荐住宿</text>
          </view>
        </view>
      </view>

      <!-- 费用计算 -->
      <view class="tab-content" v-if="activeTab === 'cost'">
        <view class="cost-card">
          <text class="cost-title">费用明细</text>

          <!-- 出行人数 -->
          <view class="cost-section">
            <view class="cost-section-title">
              <image class="cost-section-icon" src="/static/icons/mine1.png" mode="aspectFit" />
              <text>出行人数</text>
            </view>
            <view class="night-selector">
              <text>人数</text>
              <view class="night-btn" @tap="personCount > 1 && personCount--">-</view>
              <text class="night-num">{{ personCount }}人</text>
              <view class="night-btn" @tap="personCount < 20 && personCount++">+</view>
            </view>
          </view>

          <!-- 门票费用 -->
          <view class="cost-section">
            <view class="cost-section-title">
              <image class="cost-section-icon" src="/static/icons/ticket.png" mode="aspectFit" />
              <text>景点门票（{{ personCount }}人）</text>
            </view>
            <view class="cost-item" v-for="(stop, idx) in route.stops || []" :key="'ticket-'+idx">
              <text class="cost-item-name">{{ stop.name }}</text>
              <text class="cost-item-val">¥{{ getAttractionPrice(stop.name) }} × {{ personCount }}</text>
            </view>
            <view class="cost-subtotal">
              <text>门票小计</text>
              <text class="cost-subtotal-val">¥{{ ticketTotal }}</text>
            </view>
          </view>

          <!-- 交通费 -->
          <view class="cost-section">
            <view class="cost-section-title">
              <image class="cost-section-icon" src="/static/icons/drive.png" mode="aspectFit" />
              <text>交通费（{{ personCount }}人）</text>
            </view>
            <view class="cost-item" v-for="(stop, idx) in route.stops || []" :key="'trans-'+idx" v-if="idx > 0">
              <text class="cost-item-name">{{ route.stops[idx-1].name }} → {{ stop.name }}（{{ getDistance(route.stops[idx-1], stop) }}km）</text>
              <text class="cost-item-val">¥{{ getTransportCost(idx) || 0 }}</text>
            </view>
            <view class="cost-subtotal">
              <text>交通小计</text>
              <text class="cost-subtotal-val">¥{{ transportTotal }}</text>
            </view>
          </view>

          <!-- 住宿费用 -->
          <view class="cost-section" v-if="selectedHotel">
            <view class="cost-section-title">
              <image class="cost-section-icon" src="/static/icons/hotel.png" mode="aspectFit" />
              <text>住宿费用</text>
            </view>
            <view class="cost-item">
              <text class="cost-item-name">{{ selectedHotel.name }}</text>
              <text class="cost-item-val">¥{{ selectedHotel.price }}/晚 × {{ hotelNights }}晚</text>
            </view>
            <view class="night-selector">
              <text>住宿天数</text>
              <view class="night-btn" @tap="hotelNights > 1 && hotelNights--">-</view>
              <text class="night-num">{{ hotelNights }}</text>
              <view class="night-btn" @tap="hotelNights++">+</view>
            </view>
            <view class="cost-subtotal">
              <text>住宿小计</text>
              <text class="cost-subtotal-val">¥{{ hotelCost }}</text>
            </view>
          </view>
          <view class="cost-section empty-hotel" v-else @tap="activeTab = 'hotel'">
            <view class="cost-section-title">
              <image class="cost-section-icon" src="/static/icons/hotel.png" mode="aspectFit" />
              <text>住宿费用</text>
            </view>
            <text class="add-hotel-tip">+ 点击选择住宿</text>
          </view>

          <!-- 餐饮费用 -->
          <view class="cost-section">
            <view class="cost-section-title">
              <image class="cost-section-icon" src="/static/icons/food.png" mode="aspectFit" />
              <text>餐饮费用（预估）</text>
            </view>
            <view class="cost-item">
              <text class="cost-item-name">人均 {{ mealLevel === 'budget' ? '40' : mealLevel === 'comfort' ? '80' : '50' }}元/天 × {{ mealDays }}天 × {{ personCount }}人</text>
              <text class="cost-item-val">¥{{ mealCost }}</text>
            </view>
            <view class="meal-selector">
              <text>用餐标准</text>
              <view class="meal-options">
                <view
                  class="meal-opt"
                  v-for="m in mealOptions"
                  :key="m.id"
                  :class="{ selected: mealLevel === m.id }"
                  @tap="mealLevel = m.id"
                >{{ m.label }}</view>
              </view>
            </view>
          </view>

          <!-- 总计 -->
          <view class="cost-total">
            <text class="total-label">预估总费用</text>
            <text class="total-value">¥{{ totalCost }}</text>
            <text class="total-note">* {{ personCount }}人合计，人均约 ¥{{ personCount > 0 ? Math.round(totalCost / personCount) : 0 }}，仅供参考</text>
          </view>
        </view>
      </view>

      <view style="height: 180rpx;"></view>
    </scroll-view>

    <!-- 底部操作按钮 -->
    <view class="footer">
      <view class="footer-left">
        <text class="footer-cost">预估 ¥{{ totalCost }}</text>
        <text class="footer-note">/{{ personCount }}人</text>
      </view>
      <view class="footer-btns">
        <view class="footer-btn-outline" @tap="openMapNav">
          <image class="footer-btn-icon" src="/static/icons/map.png" mode="aspectFit" />
          <text>地图导航</text>
        </view>
        <view class="footer-btn-fill" @tap="bookRoute">
          <image class="footer-btn-icon" src="/static/icons/ticket.png" mode="aspectFit" />
          <text>立即预订</text>
        </view>
      </view>
    </view>
  </view>
</template>

<script>
import { staticData, getAttractionById } from '../../utils/static-data.js'
import { routeApi } from '../../utils/api.js'

export default {
  data() {
    return {
      route: {},
      activeTab: 'itinerary',
      tabs: [
        { id: 'itinerary', label: '行程', icon: '/static/icons/route.png' },
        { id: 'hotel', label: '住宿', icon: '/static/icons/hotel.png' },
        { id: 'cost', label: '费用', icon: '/static/icons/cost.png' }
      ],
      transportOptions: [
        {
          id: 'driving',
          name: '自驾',
          icon: '/static/icons/drive.png',
          getTime: (from, to) => {
            const km = this.getDistance(from, to)
            if (km <= 0) return '~10分钟'
            const mins = Math.round(km / 30 * 60)
            return mins < 60 ? `~${mins}分钟` : `~${(mins / 60).toFixed(1)}小时`
          },
          getCost: (from, to) => {
            const km = this.getDistance(from, to)
            return Math.max(5, Math.round(km * 0.5))
          }
        },
        {
          id: 'bus',
          name: '公交',
          icon: '/static/icons/bus.png',
          getTime: (from, to) => {
            const km = this.getDistance(from, to)
            if (km <= 0) return '~15分钟'
            const mins = Math.round(km / 15 * 60)
            return mins < 60 ? `~${mins}分钟` : `~${(mins / 60).toFixed(1)}小时`
          },
          getCost: (from, to) => 2
        },
        {
          id: 'walking',
          name: '步行',
          icon: '/static/icons/walk.png',
          getTime: (from, to) => {
            const km = this.getDistance(from, to)
            if (km <= 0) return '~10分钟'
            const mins = Math.round(km / 5 * 60)
            return mins < 60 ? `~${mins}分钟` : `~${(mins / 60).toFixed(1)}小时`
          },
          getCost: (from, to) => 0
        }
      ],
      stopTransport: {},
      selectedHotel: null,
      hotelNights: 1,
      personCount: 1,
      mealLevel: 'normal',
      mealOptions: [
        { id: 'budget', label: '经济 ¥40/人/天' },
        { id: 'normal', label: '标准 ¥50/人/天' },
        { id: 'comfort', label: '舒适 ¥80/人/天' }
      ]
    }
  },
  computed: {
    nearbyHotels() {
      return staticData?.hotels?.filter(h => h.status === 1) || []
    },
    ticketTotal() {
      if (!this.route?.stops) return 0
      const perPerson = this.route.stops.reduce((sum, stop) => {
        return sum + this.getAttractionPrice(stop.name)
      }, 0)
      return perPerson * this.personCount
    },
    transportTotal() {
      let total = 0
      if (!this.route?.stops) return 0
      for (let i = 1; i < this.route.stops.length; i++) {
        total += this.getTransportCost(i)
      }
      return total * this.personCount
    },
    hotelCost() {
      return this.selectedHotel ? this.selectedHotel.price * this.hotelNights : 0
    },
    mealDays() {
      if (!this.route?.duration) return 1
      const num = parseInt(this.route.duration) || 1
      return num > 0 ? num : 1
    },
    mealCost() {
      const prices = { budget: 40, normal: 50, comfort: 80 }
      return (prices[this.mealLevel] || 50) * this.mealDays * this.personCount
    },
    totalCost() {
      return this.ticketTotal + this.transportTotal + this.hotelCost + this.mealCost
    }
  },
  onLoad(options) {
    if (options.routeData) {
      try {
        this.route = JSON.parse(decodeURIComponent(options.routeData)) || {}
        // 如果是自定义路线（有 daily 字段），需要转换为 stops
        if (this.route.daily && Array.isArray(this.route.daily)) {
          // 从 daily 中提取所有 stops
          const allStops = []
          this.route.daily.forEach(day => {
            if (day.stops && Array.isArray(day.stops)) {
              day.stops.forEach(stop => {
                allStops.push({
                  name: stop.name,
                  time: stop.time || '全天',
                  duration: stop.duration,
                  desc: stop.detail,
                  type: stop.type
                })
              })
            }
          })
          this.route.stops = allStops
        } else if (this.route.stops && Array.isArray(this.route.stops)) {
          // 如果只有 stops，补充时间信息
          let currentHour = 9
          let currentMinute = 0
          this.route.stops.forEach((stop, index) => {
            if (!stop.time || stop.time === '00:00' || stop.time === '全天') {
              if (stop.type !== 'hotel') {
                const hour = Math.min(currentHour, 17)
                stop.time = `${String(hour).padStart(2, '0')}:${String(currentMinute).padStart(2, '0')}`
                currentHour += 2
                if (currentHour >= 17) {
                  currentHour = 9
                }
              }
            }
          })
        }
      } catch (e) {
        console.error('解析路线数据失败:', e)
        uni.showToast({ title: '加载路线失败', icon: 'none' })
        this.route = {}
      }
    } else if (options.id) {
      this.loadRouteById(parseInt(options.id))
    }
    // 初始化交通方式
    if (this.route.stops) {
      this.route.stops.forEach((stop, idx) => {
        if (idx > 0) {
          this.$set(this.stopTransport, idx, 'driving')
        }
      })
    }
    // 自动选择默认住宿（中等价位）
    if (!this.selectedHotel && this.nearbyHotels.length > 0) {
      const sorted = [...this.nearbyHotels].sort((a, b) => a.price - b.price)
      const mid = Math.floor(sorted.length / 2)
      this.selectHotel(sorted[mid] || sorted[0])
    }
  },
  methods: {
    async loadRouteById(id) {
      try {
        const data = await routeApi.getDetail(id)
        // 处理JSON字符串字段
        if (typeof data.stops === 'string') {
          try { data.stops = JSON.parse(data.stops) } catch { data.stops = [] }
        }
        if (typeof data.tags === 'string') {
          try { data.tags = JSON.parse(data.tags) } catch { data.tags = [] }
        }
        // 补充时间信息
        if (data.stops && Array.isArray(data.stops)) {
          let currentHour = 9
          let currentMinute = 0
          data.stops.forEach((stop, index) => {
            if (!stop.time || stop.time === '00:00') {
              if (stop.type !== 'hotel') {
                const hour = Math.min(currentHour, 17)
                stop.time = `${String(hour).padStart(2, '0')}:${String(currentMinute).padStart(2, '0')}`
                currentHour += 2
                if (currentHour >= 17) {
                  currentHour = 9
                }
              }
            }
          })
        }
        this.route = data
        // 初始化交通方式
        if (this.route.stops) {
          this.route.stops.forEach((stop, idx) => {
            if (idx > 0) {
              this.$set(this.stopTransport, idx, 'driving')
            }
          })
        }
      } catch (e) {
        console.error('加载路线详情失败:', e)
        uni.showToast({ title: '加载路线失败', icon: 'none' })
      }
    },
    getRouteCover() {
      if (this.route.cover) return this.route.cover
      return 'https://cdn.phototourl.com/free/2026-03-24-63babdc5-28dd-4c8b-acfe-0a49893a3277.png'
    },
    goBack() {
      uni.navigateBack()
    },
    selectTransport(stopIdx, transportId) {
      this.$set(this.stopTransport, stopIdx, transportId)
    },
    getAttractionPrice(name) {
      if (!staticData?.attractions || !name) return 0
      let attraction = staticData.attractions.find(a => a.name === name)
      if (!attraction) {
        attraction = staticData.attractions.find(a => name.includes(a.name) || a.name.includes(name))
      }
      return attraction ? (attraction.price || 0) : 0
    },
    getAttractionByName(name) {
      if (!staticData?.attractions || !name) return null
      let attraction = staticData.attractions.find(a => a.name === name)
      if (!attraction) {
        attraction = staticData.attractions.find(a => name.includes(a.name) || a.name.includes(name))
      }
      return attraction || null
    },
    getDistance(from, to) {
      if (!from || !to) return 0
      const fromAttr = this.getAttractionByName(from.name || from)
      const toAttr = this.getAttractionByName(to.name || to)
      if (!fromAttr || !toAttr) return 5
      const lat1 = parseFloat(fromAttr.lat || fromAttr.latitude) || 0
      const lng1 = parseFloat(fromAttr.lng || fromAttr.longitude) || 0
      const lat2 = parseFloat(toAttr.lat || toAttr.latitude) || 0
      const lng2 = parseFloat(toAttr.lng || toAttr.longitude) || 0
      if (!lat1 || !lng1 || !lat2 || !lng2) return 5
      const R = 6371
      const dLat = (lat2 - lat1) * Math.PI / 180
      const dLng = (lng2 - lng1) * Math.PI / 180
      const a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
        Math.cos(lat1 * Math.PI / 180) * Math.cos(lat2 * Math.PI / 180) *
        Math.sin(dLng / 2) * Math.sin(dLng / 2)
      const c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
      return Math.round(R * c * 1.3 * 10) / 10
    },
    getTransportCost(stopIdx) {
      if (!this.route?.stops || stopIdx <= 0) return 0
      const transportId = this.stopTransport[stopIdx] || 'driving'
      const option = this.transportOptions.find(t => t.id === transportId)
      return option ? option.getCost(this.route.stops[stopIdx - 1], this.route.stops[stopIdx]) : 0
    },
    selectHotel(hotel) {
      if (!hotel) return
      this.selectedHotel = hotel
      this.hotelNights = 1
      uni.showToast({
        title: `已选择 ${hotel.name}`,
        icon: 'none'
      })
    },
    navigateTo(stop) {
      if (!stop || !stop.name) {
        uni.showToast({ title: '景点信息异常', icon: 'none' })
        return
      }
      const attraction = staticData?.attractions?.find(a => a.name === stop.name)
      if (attraction) {
        const lat = parseFloat(attraction.lat || attraction.latitude)
        const lng = parseFloat(attraction.lng || attraction.longitude)
        if (lat && lng) {
          uni.openLocation({
            latitude: lat,
            longitude: lng,
            name: attraction.name,
            address: attraction.address || '三江侗族自治县',
            scale: 16,
            fail: () => {
              uni.showToast({ title: '打开地图失败', icon: 'none' })
            }
          })
          return
        }
      }
      uni.showToast({ title: '未找到景点位置', icon: 'none' })
    },
    openMapNav() {
      try {
        if (!this.route.stops || this.route.stops.length === 0) {
          uni.showToast({ title: '路线站点为空', icon: 'none' })
          return
        }
        // 导航到第一个景点
        const firstStop = this.route.stops[0]
        const firstAttr = staticData?.attractions?.find(a => a.name === firstStop.name)

        if (firstAttr && (firstAttr.lat || firstAttr.latitude)) {
          uni.openLocation({
            latitude: parseFloat(firstAttr.lat || firstAttr.latitude),
            longitude: parseFloat(firstAttr.lng || firstAttr.longitude),
            name: firstAttr.name,
            address: firstAttr.address || '三江侗族自治县',
            scale: 16,
            fail: () => {
              uni.showToast({ title: '打开地图失败', icon: 'none' })
            }
          })
        } else {
          // 未找到坐标，尝试获取当前位置导航
          uni.getLocation({
            type: 'gcj02',
            success: (res) => {
              uni.openLocation({
                latitude: res.latitude,
                longitude: res.longitude,
                name: '我的位置',
                scale: 16,
                fail: () => {
                  uni.showToast({ title: '打开地图失败', icon: 'none' })
                }
              })
            },
            fail: () => {
              uni.showToast({ title: '暂无位置信息', icon: 'none' })
            }
          })
        }
      } catch (e) {
        console.error('导航失败:', e)
        uni.showToast({ title: '导航失败', icon: 'none' })
      }
    },
    bookHotel(hotel) {
      if (!hotel?.id) return
      uni.navigateTo({ url: `/pages/hotel/detail?id=${hotel.id}` })
    },
    bookRoute() {
      const routeData = encodeURIComponent(JSON.stringify(this.route))
      uni.navigateTo({ url: `/pages/ticket/ticket?tab=route&route=${routeData}` })
    }
  }
}
</script>

<style scoped lang="scss">
.route-detail-page {
  min-height: 100vh;
  background: #F8FAFC;
  display: flex;
  flex-direction: column;
}

/* 顶部封面 */
.detail-header {
  height: 400rpx;
  position: relative;
  overflow: hidden;
}
.detail-header-bg {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  z-index: 0;
}
.detail-header-overlay {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: linear-gradient(to bottom, rgba(0,0,0,0.15) 0%, rgba(0,0,0,0.7) 100%);
  z-index: 1;
}
.detail-header-nav {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  padding-top: var(--status-bar-height);
  z-index: 10;
}
.back-btn {
  width: 80rpx;
  height: 80rpx;
  display: flex;
  align-items: center;
  justify-content: center;
}
.back-icon {
  width: 44rpx;
  height: 44rpx;
  filter: drop-shadow(0 2rpx 8rpx rgba(0,0,0,0.3));
}
.detail-header-content {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  padding: 30rpx;
  z-index: 2;
}
.detail-title {
  font-size: 42rpx;
  font-weight: 700;
  color: #FFFFFF;
  display: block;
  text-shadow: 0 2rpx 8rpx rgba(0,0,0,0.5);
  margin-bottom: 12rpx;
}
.detail-tags {
  display: flex;
  gap: 10rpx;
  flex-wrap: wrap;
}
.detail-tag {
  font-size: 22rpx;
  color: rgba(255,255,255,0.95);
  background: rgba(255,255,255,0.25);
  padding: 6rpx 16rpx;
  border-radius: 24rpx;
  backdrop-filter: blur(4rpx);
}

.content {
  flex: 1;
}

/* 概览卡片 */
.overview-card {
  background: #FFFFFF;
  margin: -20rpx 24rpx 24rpx;
  border-radius: 20rpx;
  padding: 36rpx 20rpx;
  box-shadow: 0 4rpx 24rpx rgba(74,144,226,0.1);
  position: relative;
  z-index: 5;
  box-sizing: border-box;
}
.overview-metrics {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 10rpx;
  align-items: start;
  justify-items: center;
}
.metric {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8rpx;
  min-height: 140rpx;
  justify-content: flex-start;
  width: 100%;
  box-sizing: border-box;
}
.metric-icon-img {
  width: 44rpx;
  height: 44rpx;
  margin-bottom: 4rpx;
  object-fit: contain;
}
.metric-val {
  font-size: 28rpx;
  font-weight: 700;
  color: #1D3B5A;
  line-height: 1.2;
  white-space: nowrap;
}
.metric-key {
  font-size: 22rpx;
  color: #94A3B8;
  line-height: 1.2;
  white-space: nowrap;
}

/* Tab 导航 */
.tab-bar {
  display: flex;
  background: #FFFFFF;
  margin: 0 24rpx 16rpx;
  border-radius: 16rpx;
  overflow: hidden;
  box-shadow: 0 2rpx 12rpx rgba(74,144,226,0.06);
}
.tab-item {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 20rpx 0;
  gap: 6rpx;
  border-bottom: 4rpx solid transparent;
  transition: all 0.3s ease;
}
.tab-item.active {
  border-bottom-color: #4A90E2;
  background: linear-gradient(to bottom, rgba(74,144,226,0.05), transparent);
}
.tab-icon-img {
  width: 36rpx;
  height: 36rpx;
}
.transport-icon-img {
  width: 36rpx;
  height: 36rpx;
}
.tab-label {
  font-size: 24rpx;
  color: #94A3B8;
  font-weight: 500;
}
.tab-item.active .tab-label {
  color: #4A90E2;
  font-weight: 700;
}

.tab-content {
  background: #FFFFFF;
  margin: 0 24rpx 16rpx;
  padding: 28rpx 24rpx;
  border-radius: 16rpx;
  box-shadow: 0 2rpx 12rpx rgba(74,144,226,0.06);
}

/* 行程时间线 */
.stops-timeline {
  display: flex;
  flex-direction: column;
  gap: 0;
}
.stop-card {
  display: flex;
  gap: 20rpx;
}
.stop-time-col {
  display: flex;
  flex-direction: column;
  align-items: center;
  width: 120rpx;
  flex-shrink: 0;
}
.stop-time-text {
  font-size: 24rpx;
  color: #4A90E2;
  font-weight: 700;
  margin-bottom: 8rpx;
}
.stop-dot-line {
  display: flex;
  flex-direction: column;
  align-items: center;
}
.stop-dot {
  width: 20rpx;
  height: 20rpx;
  border-radius: 50%;
  background: #E2E8F0;
  border: 3rpx solid #FFFFFF;
  box-shadow: 0 0 0 2rpx #E2E8F0;
}
.stop-dot.first {
  background: #4A90E2;
  box-shadow: 0 0 0 2rpx #4A90E2;
}
.stop-dot.last {
  background: #2E5F8E;
  box-shadow: 0 0 0 2rpx #2E5F8E;
}
.stop-connector {
  width: 3rpx;
  flex: 1;
  min-height: 60rpx;
  background: #E2E8F0;
  margin: 8rpx 0;
}
.stop-body {
  flex: 1;
  padding-bottom: 30rpx;
}
.stop-name {
  font-size: 30rpx;
  font-weight: 700;
  color: #1D3B5A;
  display: block;
  margin-bottom: 8rpx;
}
.stop-duration {
  font-size: 22rpx;
  color: #94A3B8;
  display: block;
  margin: 4rpx 0;
  display: flex;
  align-items: center;
  gap: 6rpx;
}
.duration-icon {
  width: 26rpx;
  height: 26rpx;
}
.stop-desc {
  font-size: 24rpx;
  color: #64748B;
  line-height: 1.7;
  display: block;
  margin-bottom: 16rpx;
}

/* 交通方式 */
.transport-section {
  margin-top: 16rpx;
  padding-top: 16rpx;
  border-top: 1rpx solid #F1F5F9;
}
.section-label {
  display: flex;
  align-items: center;
  gap: 8rpx;
  margin-bottom: 12rpx;
}
.label-icon-img {
  width: 28rpx;
  height: 28rpx;
}
.label-text {
  font-size: 24rpx;
  color: #64748B;
  font-weight: 600;
}
.transport-options {
  display: flex;
  gap: 12rpx;
}
.transport-opt {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 16rpx 12rpx;
  background: #F8FAFC;
  border-radius: 16rpx;
  border: 2rpx solid transparent;
  gap: 4rpx;
  transition: all 0.3s ease;
}
.transport-opt.selected {
  border-color: #4A90E2;
  background: rgba(74,144,226,0.08);
}
.transport-name {
  font-size: 22rpx;
  color: #1D3B5A;
  font-weight: 600;
}
.transport-time {
  font-size: 20rpx;
  color: #94A3B8;
}
.transport-cost {
  font-size: 22rpx;
  color: #4A90E2;
  font-weight: 700;
}

.nav-btn {
  margin-top: 16rpx;
  padding: 14rpx 24rpx;
  background: linear-gradient(135deg, #4A90E2, #2E5F8E);
  border-radius: 50rpx;
  text-align: center;
  font-size: 24rpx;
  color: #FFFFFF;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8rpx;
  font-weight: 600;
}
.nav-icon {
  width: 28rpx;
  height: 28rpx;
}

/* 住宿 */
.hotel-list {
  display: flex;
  flex-direction: column;
  gap: 20rpx;
}
.hotel-card {
  display: flex;
  gap: 16rpx;
  padding: 16rpx;
  background: #F8FAFC;
  border-radius: 16rpx;
  border: 2rpx solid transparent;
  transition: all 0.3s ease;
}
.hotel-card:active {
  transform: scale(0.98);
}
.hotel-img {
  width: 220rpx;
  height: 160rpx;
  border-radius: 12rpx;
  flex-shrink: 0;
}
.hotel-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 6rpx;
}
.hotel-name {
  font-size: 28rpx;
  font-weight: 700;
  color: #1D3B5A;
}
.hotel-type {
  font-size: 22rpx;
  color: #94A3B8;
}
.hotel-rating {
  display: flex;
  align-items: center;
  gap: 8rpx;
}
.rating-stars {
  font-size: 22rpx;
  color: #F59E0B;
}
.rating-count {
  font-size: 20rpx;
  color: #94A3B8;
}
.hotel-tags {
  display: flex;
  gap: 8rpx;
  flex-wrap: wrap;
}
.hotel-tag {
  font-size: 20rpx;
  color: #4A90E2;
  background: rgba(74,144,226,0.1);
  padding: 4rpx 12rpx;
  border-radius: 16rpx;
}
.hotel-price-row {
  display: flex;
  align-items: center;
  gap: 8rpx;
  margin-top: auto;
}
.hotel-price {
  font-size: 32rpx;
  font-weight: 700;
  color: #4A90E2;
}
.hotel-price-unit {
  font-size: 20rpx;
  color: #94A3B8;
}
.hotel-book-btn {
  margin-left: auto;
  padding: 10rpx 24rpx;
  background: linear-gradient(135deg, #4A90E2, #2E5F8E);
  color: #FFFFFF;
  border-radius: 40rpx;
  font-size: 22rpx;
  font-weight: 600;
}

.empty-tip {
  text-align: center;
  padding: 60rpx 0;
  color: #94A3B8;
  font-size: 28rpx;
}

/* 费用 */
.cost-card {
  padding: 0;
}
.cost-title {
  font-size: 30rpx;
  font-weight: 700;
  color: #1D3B5A;
  display: block;
  margin-bottom: 24rpx;
}
.cost-section {
  padding: 20rpx 0;
  border-bottom: 1rpx solid #F1F5F9;
}
.cost-section:last-of-type {
  border-bottom: none;
}
.cost-section-title {
  font-size: 26rpx;
  font-weight: 700;
  color: #1D3B5A;
  display: flex;
  align-items: center;
  margin-bottom: 16rpx;
}
.cost-section-icon {
  width: 32rpx;
  height: 32rpx;
  margin-right: 6rpx;
}
.cost-item {
  display: flex;
  justify-content: space-between;
  padding: 10rpx 0;
}
.cost-item-name {
  font-size: 24rpx;
  color: #64748B;
}
.cost-item-val {
  font-size: 24rpx;
  color: #1D3B5A;
  font-weight: 600;
}
.cost-subtotal {
  display: flex;
  justify-content: space-between;
  padding: 14rpx 0 0;
  border-top: 1rpx dashed #E2E8F0;
  margin-top: 10rpx;
  font-size: 24rpx;
  color: #4A90E2;
  font-weight: 700;
}
.cost-subtotal-val {
  color: #4A90E2;
}

.night-selector, .meal-selector {
  display: flex;
  align-items: center;
  gap: 12rpx;
  padding: 14rpx 0;
  font-size: 24rpx;
  color: #64748B;
}
.night-btn {
  width: 52rpx;
  height: 52rpx;
  border-radius: 50%;
  background: #F1F5F9;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 30rpx;
  color: #4A90E2;
  font-weight: 700;
}
.night-num {
  font-size: 28rpx;
  font-weight: 700;
  color: #1D3B5A;
  min-width: 60rpx;
  text-align: center;
}
.meal-options {
  display: flex;
  gap: 12rpx;
}
.meal-opt {
  padding: 8rpx 18rpx;
  background: #F1F5F9;
  border-radius: 24rpx;
  font-size: 22rpx;
  color: #64748B;
  font-weight: 500;
  transition: all 0.3s ease;
}
.meal-opt.selected {
  background: linear-gradient(135deg, #4A90E2, #2E5F8E);
  color: #FFFFFF;
  font-weight: 600;
}

.empty-hotel {
  cursor: pointer;
}
.add-hotel-tip {
  font-size: 24rpx;
  color: #4A90E2;
  font-weight: 600;
}

.cost-total {
  padding: 32rpx 0 0;
  text-align: center;
}
.total-label {
  font-size: 26rpx;
  color: #94A3B8;
  display: block;
}
.total-value {
  font-size: 56rpx;
  font-weight: 700;
  color: #4A90E2;
  display: block;
  margin: 8rpx 0;
  background: linear-gradient(135deg, #4A90E2, #2E5F8E);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
}
.total-note {
  font-size: 20rpx;
  color: #CBD5E1;
  display: block;
}

/* 底部 */
.footer {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  background: #FFFFFF;
  padding: 20rpx 24rpx;
  padding-bottom: calc(20rpx + var(--window-bottom));
  display: flex;
  align-items: center;
  justify-content: space-between;
  box-shadow: 0 -4rpx 24rpx rgba(74,144,226,0.1);
  border-top: 1rpx solid #F1F5F9;
}
.footer-left {
  display: flex;
  align-items: baseline;
  gap: 4rpx;
}
.footer-cost {
  font-size: 40rpx;
  font-weight: 700;
  color: #4A90E2;
}
.footer-note {
  font-size: 22rpx;
  color: #94A3B8;
}
.footer-btns {
  display: flex;
  gap: 16rpx;
}
.footer-btn-outline {
  padding: 18rpx 28rpx;
  border: 2rpx solid #4A90E2;
  border-radius: 50rpx;
  font-size: 26rpx;
  color: #4A90E2;
  font-weight: 600;
  display: flex;
  align-items: center;
  gap: 6rpx;
}
.footer-btn-fill {
  padding: 18rpx 28rpx;
  background: linear-gradient(135deg, #4A90E2, #2E5F8E);
  border-radius: 50rpx;
  font-size: 26rpx;
  color: #FFFFFF;
  font-weight: 600;
  display: flex;
  align-items: center;
  gap: 6rpx;
}
.footer-btn-icon {
  width: 28rpx;
  height: 28rpx;
}
</style>