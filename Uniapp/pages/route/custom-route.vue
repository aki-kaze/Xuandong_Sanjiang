<template>
  <view class="custom-route-page">
    <!-- 加载状态 -->
    <view v-if="isLoading" class="loading-container">
      <view class="loading-spinner"></view>
      <text class="loading-text">加载中...</text>
    </view>

    <!-- 主体内容 -->
    <scroll-view v-else scroll-y class="content" :style="{ height: contentHeight }">
      <!-- 行程标题 -->
      <view class="section">
        <view class="section-title">行程信息</view>
        <view class="import-btn-wrapper">
          <view class="import-btn" @tap="importTodayOrders">
            <image class="import-icon" src="/static/icons/order.png" mode="aspectFit" />
            <text>导入我的订单</text>
          </view>
          <text class="import-hint">（无订单时自动添加测试数据）</text>
        </view>
        <view class="input-group">
          <input class="input" v-model="routeName" placeholder="输入行程名称（如：三江两日游）" />
        </view>
        <view class="input-group">
          <textarea class="textarea" v-model="routeDesc" placeholder="简单描述一下您的行程" />
        </view>
      </view>

      <!-- 添加景点 -->
      <view class="section">
        <view class="section-header">
          <view class="section-title">景点</view>
          <view class="add-btn" @tap="showAttractionPicker">
            <text class="add-icon">+</text> 添加
          </view>
        </view>
        <view v-if="attractions.length === 0" class="empty-hint">
          <text>暂未添加景点</text>
          <text class="hint-sub">点击上方"添加"按钮选择景点</text>
        </view>
        <view v-else>
          <view class="poi-list-wrapper">
            <view 
              v-for="(poi, index) in attractions" 
              :key="poi.id || index"
              class="poi-item-wrapper"
            >
              <view class="poi-item">
                <view class="poi-content">
                  <image class="poi-image" :src="poi.image || poi.cover" mode="aspectFill" />
                  <view class="poi-info">
                    <view class="poi-name">{{ poi.name }}</view>
                    <view class="poi-location">{{ poi.location || poi.address }}</view>
                    <view class="poi-distance" v-if="index > 0 && getAttractionDistance(index)">
                      <image class="distance-icon" src="/static/icons/walk.png" mode="aspectFit" />
                      距上一景点：{{ formatDistance(getAttractionDistance(index)) }} · {{ formatTime(getAttractionTime(index)) }}
                    </view>
                  </view>
                </view>
                <view class="poi-actions">
                  <!-- 添加到行程的下拉选择 -->
                  <picker 
                    v-if="dayCount > 0" 
                    mode="selector" 
                    :range="Array.from({length: dayCount}, (_, i) => `第${i+1}天`)"
                    @change="(e) => addPoiToDay('attraction', poi, e.detail.value)"
                  >
                    <view class="add-to-schedule-btn">
                      {{ isPoiInSchedule('attraction', poi.id) ? '已安排' : '安排' }}
                    </view>
                  </picker>
                  <view class="remove-btn" @tap.stop="removePoi('attraction', index)">×</view>
                </view>
              </view>
            </view>
          </view>
          <view class="distance-summary" v-if="attractions.length > 1">
            <image class="summary-distance-icon" src="/static/icons/walk.png" mode="aspectFit" />
            <text>景点间总距离：{{ formatDistance(getTotalAttractionsDistance()) }}</text>
          </view>
        </view>
      </view>

      <!-- 添加酒店 -->
      <view class="section">
        <view class="section-header">
          <view class="section-title">酒店</view>
          <view class="add-btn" @tap="showHotelPicker">
            <text class="add-icon">+</text> 添加
          </view>
        </view>
        <view v-if="hotels.length === 0" class="empty-hint">
          <text>暂未添加酒店</text>
          <text class="hint-sub">点击上方"添加"按钮选择酒店</text>
        </view>
        <view v-else class="poi-list-wrapper">
          <view 
            v-for="(poi, index) in hotels" 
            :key="poi.id || index"
            class="poi-item-wrapper"
          >
            <view class="poi-item">
              <view class="poi-content">
                <image class="poi-image" :src="poi.image || poi.cover" mode="aspectFill" />
                <view class="poi-info">
                  <view class="poi-name">{{ poi.name }}</view>
                  <view class="poi-location">{{ poi.location || poi.address }}</view>
                  <view class="poi-price" v-if="poi.price">¥{{ poi.price }}</view>
                  <view class="poi-distance" v-if="attractions.length > 0">
                    <image class="distance-icon" src="/static/icons/walk.png" mode="aspectFit" />
                    距最近景点：{{ formatDistance(getNearbyAttractionDistance(poi)) }}
                  </view>
                </view>
              </view>
              <view class="poi-actions">
                <!-- 添加到行程的下拉选择 -->
                <picker 
                  v-if="dayCount > 0" 
                  mode="selector" 
                  :range="Array.from({length: dayCount}, (_, i) => `第${i+1}天`)"
                  @change="(e) => addPoiToDay('hotel', poi, e.detail.value)"
                >
                  <view class="add-to-schedule-btn">
                    {{ isPoiInSchedule('hotel', poi.id) ? '已安排' : '安排' }}
                  </view>
                </picker>
                <view class="remove-btn" @tap="removePoi('hotel', index)">×</view>
              </view>
            </view>
          </view>
        </view>
      </view>

      <!-- 路线预览 -->
      <view v-if="sortedPoiList.length > 0" class="section">
        <view class="section-header">
          <view class="section-title">行程安排</view>
          <view class="sort-actions">
            <view class="day-selector">
              <text class="day-label">行程天数</text>
              <view class="day-controls">
                <view class="day-btn" @tap="changeDayCount(-1)" :class="{ disabled: dayCount <= 1 }">-</view>
                <text class="day-num">{{ dayCount }}</text>
                <view class="day-btn" @tap="changeDayCount(1)">+</view>
              </view>
            </view>
          </view>
        </view>
        
        <!-- 按天分天显示 -->
        <view class="daily-preview-list">
          <view v-for="(dayPois, dayIndex) in dailyPoiGroups" :key="dayIndex" class="day-group">
            <view class="day-header">
              <view class="day-title">
                <text class="day-index">第 {{ dayIndex + 1 }} 天</text>
                <text class="day-theme">{{ getDayTheme(dayIndex) }}</text>
              </view>
            </view>
            
            <view class="day-poi-list">
              <view 
                v-for="(poi, poiIndex) in dayPois" 
                :key="poi.id + '-' + dayIndex + '-' + poiIndex"
                class="day-poi-item"
              >
                <view class="day-poi-content">
                  <image class="day-poi-image" :src="poi.image || poi.cover" mode="aspectFill" />
                  <view class="day-poi-info">
                    <view class="day-poi-name">
                      <text :class="poi.type === 'hotel' ? 'hotel-badge' : 'attraction-badge'">
                        {{ poi.type === 'hotel' ? '酒店' : '景点' }}
                      </text>
                      <text class="day-poi-title">{{ poi.name }}</text>
                    </view>
                    <view class="day-poi-location">{{ poi.location || poi.address }}</view>
                  </view>
                  
                  <view class="day-poi-actions">
                    <!-- 切换顺序按钮 -->
                    <view 
                      v-if="poiIndex < dayPois.length - 1 || dayIndex < dayCount - 1"
                      class="move-btn swap-btn"
                      @tap.stop="swapWithNext(dayIndex, poiIndex)"
                    >
                      <image class="move-icon-img" src="/static/icons/上下.png" mode="aspectFit" />
                    </view>
                    <!-- 删除按钮 -->
                    <view class="action-btn simple-btn" @tap.stop="removeFromDay(dayIndex, poiIndex)">
                      <image class="action-icon-img" src="/static/icons/delete.png" mode="aspectFit" />
                    </view>
                  </view>
                </view>
                
                <!-- 距离信息 -->
                <view v-if="poiIndex < dayPois.length - 1" class="day-poi-connector">
                  <view class="day-connector-line"></view>
                  <view class="day-distance-info">
                    <image class="distance-icon-small" src="/static/icons/walk.png" mode="aspectFit" />
                    <text>{{ formatDistance(getDistanceBetweenPois(dayPois[poiIndex], dayPois[poiIndex + 1])) }}</text>
                  </view>
                </view>
              </view>
              
              <!-- 如果某天没有POI，显示提示 -->
              <view v-if="dayPois.length === 0" class="empty-day-poi">
                <text>点击下方景点添加到这一天</text>
              </view>
            </view>
          </view>
        </view>
      </view>

      <!-- 出行日期 -->
      <view class="section">
        <view class="section-title">出行日期</view>
        <view class="date-selector">
          <picker mode="date" :value="startDate" @change="onStartDateChange">
            <view class="date-input">
              <text class="date-label">开始日期</text>
              <text class="date-value">{{ startDate || '请选择' }}</text>
            </view>
          </picker>
          <picker mode="date" :value="endDate" @change="onEndDateChange">
            <view class="date-input">
              <text class="date-label">结束日期</text>
              <text class="date-value">{{ endDate || '请选择' }}</text>
            </view>
          </picker>
        </view>
        <view v-if="days > 0" class="days-info">共 {{ days }} 天</view>
      </view>

      <!-- 出行人数 -->
      <view class="section">
        <view class="section-title">出行人数</view>
        <view class="people-selector">
          <view class="people-btn" @tap="changePeople(-1)" :class="{ disabled: people <= 1 }">-</view>
          <view class="people-number">{{ people }}</view>
          <view class="people-btn" @tap="changePeople(1)">+</view>
        </view>
      </view>

      <!-- 交通方式 -->
      <view class="section">
        <view class="section-title">交通方式</view>
        <view class="transport-list">
          <view 
            v-for="item in transportOptions" 
            :key="item.id" 
            class="transport-item" 
            :class="{ active: transport === item.id }"
            @tap="selectTransport(item.id)"
          >
            <image class="transport-icon" :src="item.icon" mode="aspectFit" />
            <text class="transport-name">{{ item.name }}</text>
          </view>
        </view>
      </view>

      <!-- 生成路线按钮 -->
      <view class="action-section">
        <view class="generate-btn" :class="{ disabled: isGenerating }" @tap="generateRoute">
          <text v-if="!isGenerating">生成路线</text>
          <view v-else class="generating-container">
            <view class="generating-spinner"></view>
            <text>生成中...</text>
          </view>
        </view>
      </view>

      <!-- 生成结果展示 - 骨架屏 -->
      <view v-if="isGenerating" class="result-section skeleton">
        <view class="skeleton-header"></view>
        <view class="skeleton-metrics">
          <view class="skeleton-metric" v-for="i in 4" :key="i"></view>
        </view>
        <view class="skeleton-daily">
          <view class="skeleton-day" v-for="i in 2" :key="i"></view>
        </view>
      </view>

      <!-- 生成结果展示 - 真实内容 -->
      <view v-else-if="generatedRoute" class="result-section">
        <view class="result-header">
          <view class="result-title">{{ generatedRoute.name }}</view>
          <view class="result-desc">{{ generatedRoute.description }}</view>
        </view>
        
        <view class="route-metrics">
          <view class="metric">
            <image class="metric-icon" src="/static/icons/duration.png" mode="aspectFit" />
            <view class="metric-value">{{ generatedRoute.days }} 天</view>
            <view class="metric-label">行程时长</view>
          </view>
          <view class="metric">
            <image class="metric-icon" src="/static/icons/walk.png" mode="aspectFit" />
            <view class="metric-value">{{ generatedRoute.distance }} km</view>
            <view class="metric-label">总距离</view>
          </view>
          <view class="metric">
            <image class="metric-icon" src="/static/icons/difficulty.png" mode="aspectFit" />
            <view class="metric-value">轻松</view>
            <view class="metric-label">难度</view>
          </view>
          <view class="metric">
            <image class="metric-icon" src="/static/icons/cost.png" mode="aspectFit" />
            <view class="metric-value">¥{{ computedTotalCost }}</view>
            <view class="metric-label">预估费用/人</view>
          </view>
        </view>

        <view class="daily-list">
          <view v-for="(day, dayIndex) in generatedRoute.daily" :key="dayIndex" class="daily-item">
            <view class="day-header">
              <view class="day-title">Day {{ dayIndex + 1 }}</view>
              <view class="day-theme">{{ day.theme }}</view>
            </view>
            <view class="stop-list">
              <view v-for="(stop, stopIndex) in day.stops" :key="stopIndex" class="stop-item">
                <view class="stop-marker" :class="{ first: stopIndex === 0, last: stopIndex === day.stops.length - 1 }"></view>
                <view class="stop-content">
                  <view class="stop-header">
                    <image class="stop-icon" :src="getStopIcon(stop.type)" mode="aspectFit" />
                    <text class="stop-name">{{ stop.name }}</text>
                  </view>
                  <view class="stop-detail">{{ stop.detail }}</view>
                  <view class="stop-meta">
                    <view class="stop-duration">
                      <image class="duration-icon" src="/static/icons/duration.png" mode="aspectFit" />
                      <text class="duration-text">{{ stop.duration }}</text>
                    </view>
                    <view class="stop-distance" v-if="stop.distanceToNext">
                      <image class="duration-icon" src="/static/icons/walk.png" mode="aspectFit" />
                      <text class="distance-text">{{ formatDistance(stop.distanceToNext) }} · {{ formatTime(stop.timeToNext) }}</text>
                    </view>
                  </view>
                </view>
              </view>
            </view>
          </view>
        </view>

        <view class="bottom-actions">
          <view class="action-btn secondary" @tap="saveRoute" :class="{ disabled: isSaving }">
            <text v-if="!isSaving">保存路线</text>
            <text v-else>保存中...</text>
          </view>
          <view class="action-btn primary" @tap="showRouteDetail">
            <text>查看详情</text>
          </view>
        </view>
      </view>

      <view style="height: 40rpx"></view>
    </scroll-view>

    <!-- 路线详情弹窗 -->
    <view class="route-detail-modal" v-if="showRouteDetailModal" @tap="closeRouteDetail">
      <view class="route-detail-content" @tap.stop="stopPropagation">
        <view class="route-detail-header">
          <view class="detail-title">{{ generatedRoute.name }}</view>
          <view class="detail-close" @tap="closeRouteDetail">×</view>
        </view>
        
        <scroll-view scroll-y class="route-detail-scroll">
          <view class="detail-metrics">
            <view class="detail-metric">
              <image class="metric-icon" src="/static/icons/duration.png" mode="aspectFit" />
              <view class="metric-value">{{ generatedRoute.days }} 天</view>
              <view class="metric-label">行程</view>
            </view>
            <view class="detail-metric">
              <image class="metric-icon" src="/static/icons/walk.png" mode="aspectFit" />
              <view class="metric-value">{{ generatedRoute.distance }} km</view>
              <view class="metric-label">距离</view>
            </view>
            <view class="detail-metric">
              <image class="metric-icon" src="/static/icons/difficulty.png" mode="aspectFit" />
              <view class="metric-value">轻松</view>
              <view class="metric-label">难度</view>
            </view>
            <view class="detail-metric">
              <image class="metric-icon" src="/static/icons/cost.png" mode="aspectFit" />
              <view class="metric-value">¥{{ computedTotalCost }}</view>
              <view class="metric-label">费用/人</view>
            </view>
          </view>

          <view class="detail-daily-list">
            <view v-for="(day, dayIndex) in generatedRoute.daily" :key="dayIndex" class="detail-daily-item">
              <view class="detail-day-header">
                <view class="detail-day-number">Day {{ dayIndex + 1 }}</view>
                <view class="detail-day-theme">{{ day.theme }}</view>
              </view>
              <view class="detail-stop-list">
                <view v-for="(stop, stopIndex) in day.stops" :key="stopIndex">
                  <!-- 站点之间的距离连接线 -->
                  <view class="detail-stop-distance" v-if="stop.distanceToNext">
                    <view class="distance-line"></view>
                    <view class="distance-badge">
                      <image class="distance-icon" src="/static/icons/walk.png" mode="aspectFit" />
                      <text class="distance-info">{{ formatDistance(stop.distanceToNext) }} · {{ formatTime(stop.timeToNext) }}</text>
                    </view>
                    <view class="distance-line"></view>
                  </view>
                  <view class="detail-stop-item">
                    <view class="detail-stop-index">{{ stopIndex + 1 }}</view>
                    <view class="detail-stop-content">
                      <view class="detail-stop-header">
                        <image class="detail-stop-icon" :src="getStopIcon(stop.type)" mode="aspectFit" />
                        <text class="detail-stop-name">{{ stop.name }}</text>
                        <view class="detail-stop-type" :class="'type-' + stop.type">
                          {{ stop.type === 'attraction' ? '景点' : stop.type === 'hotel' ? '酒店' : '其他' }}
                        </view>
                      </view>
                      <view class="detail-stop-detail">{{ stop.detail }}</view>
                      <view class="detail-stop-meta">
                        <view class="detail-stop-duration">
                          <image class="duration-icon" src="/static/icons/duration.png" mode="aspectFit" />
                          <text class="duration-text">{{ stop.duration }}</text>
                        </view>
                      </view>
                      <view class="detail-stop-nav" v-if="stop.type === 'attraction'" @tap="navigateToStop(stop)">
                        <image class="nav-icon" src="/static/icons/compass.png" mode="aspectFit" />
                        <text>导航到这里</text>
                      </view>
                    </view>
                  </view>
                </view>
              </view>
            </view>
          </view>
        </scroll-view>

        <view class="route-detail-actions">
          <view class="route-detail-btn secondary" @tap="saveRoute" :class="{ disabled: isSaving }">
            <text v-if="!isSaving">保存路线</text>
            <text v-else>保存中...</text>
          </view>
        </view>
      </view>
    </view>

    <!-- 景点选择弹窗 -->
    <view v-if="showAttractionModal" class="modal-overlay" @tap="hidePicker">
      <view class="modal-content" @tap.stop>
        <view class="modal-header">
          <view class="modal-title">选择景点</view>
          <view class="modal-close" @tap="hidePicker">×</view>
        </view>
        <view class="search-box">
          <input class="search-input" v-model="attractionSearch" placeholder="搜索景点..." />
        </view>
        <scroll-view scroll-y class="picker-list" :scroll-into-view="scrollIntoView">
          <view 
            v-for="item in filteredAttractions" 
            :key="item.id" 
            class="picker-item"
            :class="{ selected: isPoiSelected('attraction', item.id) }"
            @tap="togglePoi('attraction', item)"
          >
            <image class="picker-image" :src="item.image || item.cover" mode="aspectFill" />
            <view class="poi-info">
              <view class="poi-name">{{ item.name }}</view>
              <view class="picker-location">{{ item.location || item.address }}</view>
              <view class="picker-price" v-if="item.price">¥{{ item.price }}</view>
            </view>
          </view>
        </scroll-view>
        <view class="modal-footer">
          <view class="confirm-btn" @tap="confirmPicker">确认选择 ({{ tempSelectedAttractions.length }})</view>
        </view>
      </view>
    </view>

    <!-- 酒店选择弹窗 -->
    <view v-if="showHotelModal" class="modal-overlay" @tap="hidePicker">
      <view class="modal-content" @tap.stop>
        <view class="modal-header">
          <view class="modal-title">选择酒店</view>
          <view class="modal-close" @tap="hidePicker">×</view>
        </view>
        <view class="search-box">
          <input class="search-input" v-model="hotelSearch" placeholder="搜索酒店..." />
        </view>
        <scroll-view scroll-y class="picker-list" :scroll-into-view="scrollIntoView">
          <view 
            v-for="item in filteredHotels" 
            :key="item.id" 
            class="picker-item"
            :class="{ selected: isPoiSelected('hotel', item.id) }"
            @tap="togglePoi('hotel', item)"
          >
            <image class="picker-image" :src="item.image || item.cover" mode="aspectFill" />
            <view class="poi-info">
              <view class="poi-name">{{ item.name }}</view>
              <view class="picker-location">{{ item.location || item.address }}</view>
              <view class="picker-price" v-if="item.price">¥{{ item.price }}</view>
            </view>
          </view>
        </scroll-view>
        <view class="modal-footer">
          <view class="confirm-btn" @tap="confirmPicker">确认选择 ({{ tempSelectedHotels.length }})</view>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, computed, watch, nextTick } from 'vue';
import { onLoad } from '@dcloudio/uni-app';
import { attractionApi, hotelApi, userRouteApi, sensitiveWordApi, ticketOrderApi, foodOrderApi, hotelOrderApi, productOrderApi, experienceOrderApi } from '@/utils/api.js';
import { checkSensitiveWordsAsync, getSensitiveWordMessage } from '@/utils/sensitive-words.js';
import { getDistance, getRouteDistance, getEstimatedTime, formatDistance, formatTime } from '@/utils/distance.js';
import { staticData } from '@/utils/static-data.js';

// ========== 状态定义 ==========
const contentHeight = ref('');
const isLoading = ref(true);
const isGenerating = ref(false);
const isSaving = ref(false);
const routeName = ref('');
const routeDesc = ref('');
const attractions = ref([]);
const hotels = ref([]);
const startDate = ref('');
const endDate = ref('');
const people = ref(1);
const transport = ref('drive');
const budget = ref('medium');
const generatedRoute = ref(null);

// 行程天数
const dayCount = ref(1);

// 按天分组的POI数据
const dailyPoiGroups = ref([]);

const showAttractionModal = ref(false);
const showHotelModal = ref(false);
const tempSelectedAttractions = ref([]);
const tempSelectedHotels = ref([]);
const attractionSearch = ref('');
const hotelSearch = ref('');
const scrollIntoView = ref('');

// 排序后的POI列表
const sortedPoiList = ref([]);

// 路线详情弹窗相关状态
const showRouteDetailModal = ref(false);

const yList = ref({});
const draggingIndex = ref(-1);

const transportOptions = ref([
  { id: 'drive', name: '自驾', icon: '/static/icons/drive.png' },
  { id: 'bus', name: '公交', icon: '/static/icons/bus.png' },
  { id: 'walk', name: '步行', icon: '/static/icons/walk.png' }
]);

const budgetOptions = ref([
  { id: 'low', range: '经济型' },
  { id: 'medium', range: '舒适型' },
  { id: 'high', range: '豪华型' }
]);

const availableAttractions = ref([]);

const availableHotels = ref([
  { id: 1, name: '三江大酒店', image: 'https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=酒店大堂%20现代装修&image_size=square', location: '三江县古宜镇', address: '三江县古宜镇', price: 280, lat: 25.8515, lng: 109.6025 },
  { id: 2, name: '侗乡国宾馆', image: 'https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=五星级酒店%20豪华房间&image_size=square', location: '三江县古宜镇', address: '三江县古宜镇', price: 480, lat: 25.8508, lng: 109.6035 },
  { id: 3, name: '程阳客栈', image: 'https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=特色民宿%20侗族风格&image_size=square', location: '三江县林溪乡', address: '三江县林溪乡', price: 180, lat: 25.8230, lng: 109.5675 },
  { id: 4, name: '丹洲民宿', image: 'https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=古城民宿%20古色古香&image_size=square', location: '三江县丹洲镇', address: '三江县丹洲镇', price: 220, lat: 25.8290, lng: 109.6875 }
]);

// ========== 计算属性 ==========
const days = computed(() => {
  if (startDate.value && endDate.value) {
    const start = new Date(startDate.value);
    const end = new Date(endDate.value);
    const diffTime = end - start;
    const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24)) + 1;
    return Math.max(1, diffDays);
  }
  return 0;
});

// 费用计算 - 与 route-detail.vue 保持一致
const getAttractionPrice = (name) => {
  if (!staticData?.attractions || !name) return 0
  let attraction = staticData.attractions.find(a => a.name === name)
  if (!attraction) {
    attraction = staticData.attractions.find(a => name.includes(a.name) || a.name.includes(name))
  }
  return attraction ? (attraction.price || 0) : 0
}

const computedTicketTotal = computed(() => {
  const attractionStops = sortedPoiList.value.filter(p => p.type === 'attraction')
  const perPerson = attractionStops.reduce((sum, stop) => sum + getAttractionPrice(stop.name), 0)
  return perPerson * people.value
})

const computedTransportTotal = computed(() => {
  let total = 0
  for (let i = 1; i < sortedPoiList.value.length; i++) {
    total += getMergedDistance(i) * 0.5 // 自驾约 ¥0.5/km
  }
  return Math.max(5, Math.round(total)) * people.value
})

const computedMealCost = computed(() => {
  const mealDays = days.value || 1
  return 50 * mealDays * people.value // 标准 ¥50/人/天
})

const computedHotelCost = computed(() => {
  const hotelPois = sortedPoiList.value.filter(p => p.type === 'hotel')
  if (hotelPois.length === 0) return 0
  const avgPrice = hotelPois.reduce((sum, h) => sum + (h.price || 200), 0) / hotelPois.length
  const hotelNights = Math.max(1, (days.value || 1) - 1)
  return Math.round(avgPrice) * hotelNights
})

const computedTotalCost = computed(() => {
  return computedTicketTotal.value + computedTransportTotal.value + computedHotelCost.value + computedMealCost.value
})

// ========== 按天管理相关方法 ==========
// 初始化按天分组
const initDailyGroups = () => {
  // 确保每天都有一个空数组
  const groups = []
  for (let i = 0; i < dayCount.value; i++) {
    groups.push([])
  }
  dailyPoiGroups.value = groups
}

// 改变天数
const changeDayCount = (delta) => {
  const newCount = dayCount.value + delta
  if (newCount < 1) return
  
  const oldCount = dayCount.value
  dayCount.value = newCount
  
  // 如果天数增加，添加空天数组
  if (newCount > oldCount) {
    for (let i = oldCount; i < newCount; i++) {
      dailyPoiGroups.value.push([])
    }
  }
  // 如果天数减少，移除最后的天数组，并把其中的POI移到前一天
  else if (newCount < oldCount) {
    const removedDay = dailyPoiGroups.value.pop()
    if (removedDay && removedDay.length > 0 && dailyPoiGroups.value.length > 0) {
      dailyPoiGroups.value[dailyPoiGroups.value.length - 1].push(...removedDay)
    }
  }
}

// 检查POI是否已在行程安排中
const isPoiInSchedule = (type, id) => {
  return dailyPoiGroups.value.some(day => 
    day.some(poi => poi.type === type && poi.id === id)
  )
}

// 添加POI到指定天数
const addPoiToDay = (type, poi, dayIndex) => {
  // 先从其他天数中移除
  for (let i = 0; i < dailyPoiGroups.value.length; i++) {
    const day = dailyPoiGroups.value[i]
    const idx = day.findIndex(p => p.type === type && p.id === poi.id)
    if (idx >= 0) {
      day.splice(idx, 1)
    }
  }
  // 添加到指定天数
  const poiWithType = { ...poi, type }
  dailyPoiGroups.value[dayIndex].push(poiWithType)
  onMoveComplete()
}

// 从天中移除（不从列表中删除）
const removeFromDay = (dayIndex, poiIndex) => {
  dailyPoiGroups.value[dayIndex].splice(poiIndex, 1)
  onMoveComplete()
}

// 切换景点顺序（当前与下一个交换）
const swapWithNext = (dayIndex, poiIndex) => {
  const day = dailyPoiGroups.value[dayIndex]
  
  if (poiIndex < day.length - 1) {
    // 在同一天内与下一个元素交换
    const temp = day[poiIndex]
    day[poiIndex] = day[poiIndex + 1]
    day[poiIndex + 1] = temp
  } else if (dayIndex < dayCount.value - 1) {
    // 是最后一天元素，移到后一天开头
    const poi = day.splice(poiIndex, 1)[0]
    dailyPoiGroups.value[dayIndex + 1].unshift(poi)
  }
  
  onMoveComplete()
}

// 获取两个POI之间的距离
const getDistanceBetweenPois = (poi1, poi2) => {
  if (!poi1 || !poi2) return 0
  const lat1 = poi1.lat || poi1.latitude
  const lng1 = poi1.lng || poi1.longitude
  const lat2 = poi2.lat || poi2.latitude
  const lng2 = poi2.lng || poi2.longitude
  if (!lat1 || !lng1 || !lat2 || !lng2) return 0
  const straightDist = getDistance(lat1, lng1, lat2, lng2)
  return getRouteDistance(straightDist, transport.value)
}



// 获取每日主题
const getDayTheme = (dayIndex) => {
  const themes = [
    '侗族文化深度游',
    '程阳桥风景区',
    '丹洲古镇探索',
    '自然山水之旅',
    '美食体验之旅',
    '休闲度假游'
  ]
  return themes[dayIndex % themes.length] || '三江深度体验'
}

const filteredAttractions = computed(() => {
  if (!attractionSearch.value) return availableAttractions.value;
  return availableAttractions.value.filter(item => 
    item.name.includes(attractionSearch.value) || 
    (item.location && item.location.includes(attractionSearch.value))
  );
});

const filteredHotels = computed(() => {
  if (!hotelSearch.value) return availableHotels.value;
  return availableHotels.value.filter(item => 
    item.name.includes(hotelSearch.value) || 
    (item.location && item.location.includes(hotelSearch.value))
  );
});

// 合并景点和酒店的列表
const mergedPoiList = computed(() => {
  const allPois = [
    ...attractions.value.map(item => ({ ...item, type: 'attraction' })),
    ...hotels.value.map(item => ({ ...item, type: 'hotel' }))
  ];
  return allPois;
});

// 监听景点和酒店列表变化，更新排序后的列表
watch([attractions, hotels], () => {
  // 如果排序后的列表为空，使用合并的列表
  if (sortedPoiList.value.length === 0 || mergedPoiList.value.length !== sortedPoiList.value.length) {
    sortedPoiList.value = [...mergedPoiList.value];
  } else {
    // 更新已有项目的数据，但保持排序
    const newList = [];
    for (const oldItem of sortedPoiList.value) {
      const newItem = mergedPoiList.value.find(item => item.id === oldItem.id && item.type === oldItem.type);
      if (newItem) {
        newList.push(newItem);
      }
    }
    // 添加新的项目
    for (const newItem of mergedPoiList.value) {
      if (!newList.find(item => item.id === newItem.id && item.type === newItem.type)) {
        newList.push(newItem);
      }
    }
    sortedPoiList.value = newList;
  }
}, { immediate: true, deep: true });

// ========== 距离计算方法 ==========
const getAttractionDistance = (index) => {
  if (index < 1 || index >= attractions.value.length) return 0
  const prev = attractions.value[index - 1]
  const curr = attractions.value[index]
  const prevLat = prev.lat || prev.latitude
  const prevLng = prev.lng || prev.longitude
  const currLat = curr.lat || curr.latitude
  const currLng = curr.lng || curr.longitude
  if (!prevLat || !prevLng || !currLat || !currLng) return 0
  const straightDist = getDistance(prevLat, prevLng, currLat, currLng)
  return getRouteDistance(straightDist, transport.value)
}

const getAttractionTime = (index) => {
  const distance = getAttractionDistance(index)
  return getEstimatedTime(distance, transport.value)
}

const getTotalAttractionsDistance = () => {
  let total = 0
  for (let i = 1; i < attractions.value.length; i++) {
    total += getAttractionDistance(i)
  }
  return Math.round(total * 10) / 10
}

const getNearbyAttractionDistance = (hotel) => {
  if (!hotel || attractions.value.length === 0) return 0
  const hLat = hotel.lat || hotel.latitude
  const hLng = hotel.lng || hotel.longitude
  if (!hLat || !hLng) {
    return 0.5 // 默认返回500米
  }
  let minDist = Infinity
  for (const attraction of attractions.value) {
    const aLat = attraction.lat || attraction.latitude
    const aLng = attraction.lng || attraction.longitude
    if (aLat && aLng) {
      const straightDist = getDistance(hLat, hLng, aLat, aLng)
      const routeDist = getRouteDistance(straightDist, transport.value)
      if (routeDist < minDist) {
        minDist = routeDist
      }
    }
  }
  return minDist === Infinity ? 0.5 : minDist
}

// 获取排序后列表中指定位置与前一个POI的距离
const getMergedDistance = (index) => {
  if (index < 1 || index >= sortedPoiList.value.length) return 0
  const prev = sortedPoiList.value[index - 1]
  const curr = sortedPoiList.value[index]
  const prevLat = prev.lat || prev.latitude
  const prevLng = prev.lng || prev.longitude
  const currLat = curr.lat || curr.latitude
  const currLng = curr.lng || curr.longitude
  if (!prevLat || !prevLng || !currLat || !currLng) return 0
  const straightDist = getDistance(prevLat, prevLng, currLat, currLng)
  return getRouteDistance(straightDist, transport.value)
}

// 获取排序后列表中指定位置与前一个POI的预估时间
const getMergedTime = (index) => {
  const distance = getMergedDistance(index)
  return getEstimatedTime(distance, transport.value)
}

// 移动时的反馈
const onMoveComplete = () => {
  uni.vibrateShort() // 轻微震动反馈
}

// 交换按钮点击 - 与前一个元素交换位置
const onSwapBtnClick = (index) => {
  // 与前一个元素交换位置
  if (index > 0) {
    swapPoi(index, index - 1)
    onMoveComplete()
  }
}

// 交换两个POI的位置
const swapPoi = (fromIndex, toIndex) => {
  if (fromIndex === toIndex) return
  const temp = sortedPoiList.value[fromIndex]
  sortedPoiList.value.splice(fromIndex, 1)
  sortedPoiList.value.splice(toIndex, 0, temp)
}

// 移到第一位
const moveToFirst = (index) => {
  if (index === 0) return
  const item = sortedPoiList.value.splice(index, 1)[0]
  sortedPoiList.value.unshift(item)
}

// 移到最后一位
const moveToLast = (index) => {
  if (index === sortedPoiList.value.length - 1) return
  const item = sortedPoiList.value.splice(index, 1)[0]
  sortedPoiList.value.push(item)
}

// 删除路线预览中的POI
const removeFromRoute = (index) => {
  uni.showModal({
    title: '确认删除',
    content: '确定要从路线中移除这个项目吗？',
    confirmText: '删除',
    confirmColor: '#EF4444',
    success: (res) => {
      if (res.confirm) {
        const poi = sortedPoiList.value[index]
        if (poi.type === 'attraction') {
          // 从景点列表中删除
          const idx = attractions.value.findIndex(a => a.id === poi.id)
          if (idx !== -1) {
            attractions.value.splice(idx, 1)
          }
        } else {
          // 从酒店列表中删除
          const idx = hotels.value.findIndex(h => h.id === poi.id)
          if (idx !== -1) {
            hotels.value.splice(idx, 1)
          }
        }
        onMoveComplete()
      }
    }
  })
}



// ========== 方法 ==========
const goBack = () => {
  uni.navigateBack();
};

const loadData = async () => {
  try {
    // 加载景点数据
    const attractionRes = await attractionApi.getList(1, 100);
    if (attractionRes && attractionRes.records) {
      availableAttractions.value = attractionRes.records.map(item => ({
        id: item.id,
        name: item.name,
        image: item.image || item.cover || '',
        location: item.location || item.address || '',
        address: item.address || item.location || '',
        cover: item.cover || item.image || '',
        price: item.price || 0,
        lat: item.lat || item.latitude || null,
        lng: item.lng || item.longitude || null
      }));
    }

    // 加载酒店数据
    const hotelRes = await hotelApi.getList(1, 100);
    if (hotelRes && hotelRes.records) {
      // 用静态数据作为坐标兜底，因为后端 hotels 表可能缺少经纬度
      const staticHotelMap = {};
      availableHotels.value.forEach(h => { staticHotelMap[h.name] = h; });

      availableHotels.value = hotelRes.records.map(item => {
        const staticMatch = staticHotelMap[item.name];
        return {
          id: item.id,
          name: item.name,
          image: item.image || item.cover || '',
          location: item.location || item.address || '',
          address: item.address || item.location || '',
          cover: item.cover || item.image || '',
          price: item.price || 0,
          lat: item.lat || item.latitude || (staticMatch ? staticMatch.lat : null),
          lng: item.lng || item.longitude || (staticMatch ? staticMatch.lng : null)
        };
      });
    }

  } catch (e) {
    console.error('加载数据失败:', e);
    uni.showToast({
      title: '数据加载失败，使用默认数据',
      icon: 'none',
      duration: 2000
    });
  } finally {
    isLoading.value = false;
  }
};

const showAttractionPicker = () => {
  tempSelectedAttractions.value = [...attractions.value];
  attractionSearch.value = '';
  showAttractionModal.value = true;
};

const showHotelPicker = () => {
  tempSelectedHotels.value = [...hotels.value];
  hotelSearch.value = '';
  showHotelModal.value = true;
};

const hidePicker = () => {
  showAttractionModal.value = false;
  showHotelModal.value = false;
};

const isPoiSelected = (type, id) => {
  const list = type === 'attraction' ? tempSelectedAttractions.value : tempSelectedHotels.value;
  return list.some(item => item.id === id);
};

const togglePoi = (type, poi) => {
  const list = type === 'attraction' ? tempSelectedAttractions.value : tempSelectedHotels.value;
  const index = list.findIndex(item => item.id === poi.id);
  if (index > -1) {
    list.splice(index, 1);
  } else {
    // 深拷贝确保数据独立
    const newItem = {
      id: poi.id,
      name: poi.name,
      image: poi.image || poi.cover || '',
      cover: poi.cover || poi.image || '',
      location: poi.location || poi.address || '',
      address: poi.address || poi.location || '',
      price: poi.price,
      lat: poi.lat || poi.latitude,
      lng: poi.lng || poi.longitude
    };
    list.push(newItem);
  }
};

const confirmPicker = () => {
  if (showAttractionModal.value) {
    // 深拷贝确保数据独立
    attractions.value = tempSelectedAttractions.value.map(item => ({...item}));
  } else if (showHotelModal.value) {
    hotels.value = tempSelectedHotels.value.map(item => ({...item}));
  }
  hidePicker();
};

const removePoi = (type, index) => {
  if (type === 'attraction') {
    attractions.value.splice(index, 1);
  } else {
    hotels.value.splice(index, 1);
  }
};

const onStartDateChange = (e) => {
  startDate.value = e.detail.value;
};

const onEndDateChange = (e) => {
  endDate.value = e.detail.value;
};

const changePeople = (delta) => {
  const newValue = people.value + delta;
  if (newValue >= 1 && newValue <= 20) {
    people.value = newValue;
  }
};

const selectTransport = (id) => {
  transport.value = id;
};

const getStopIcon = (type) => {
  const icons = {
    attraction: '/static/icons/route.png',
    hotel: '/static/icons/hotel.png',
    food: '/static/icons/route.png',
    activity: '/static/icons/difficulty.png'
  };
  return icons[type] || '/static/icons/route.png';
};

// 导入今日订单
const importTodayOrders = async () => {
  try {
    const userInfo = uni.getStorageSync('userInfo') || {}
    const userId = userInfo.userId || userInfo.id || userInfo.user_id
    
    if (!userId) {
      uni.showToast({ title: '请先登录', icon: 'none' })
      return
    }
    
    console.log('用户信息:', userInfo)
    console.log('用户ID:', userId)

    uni.showLoading({ title: '导入中...' })

    // 1. 从后端获取所有类型的订单
    let allOrders = []
    
    try {
      // 获取门票订单
      const ticketOrders = await ticketOrderApi.getByUserId(userId)
      console.log('门票订单原始数据:', ticketOrders)
      if (ticketOrders && Array.isArray(ticketOrders)) {
        allOrders = allOrders.concat(ticketOrders.map(o => ({ ...o, orderType: 'ticket' })))
      }
      
      // 获取酒店订单
      const hotelOrders = await hotelOrderApi.getByUserId(userId)
      console.log('酒店订单原始数据:', hotelOrders)
      if (hotelOrders && Array.isArray(hotelOrders)) {
        allOrders = allOrders.concat(hotelOrders.map(o => ({ ...o, orderType: 'hotel' })))
      }
      
      // 获取美食订单
      const foodOrders = await foodOrderApi.getByUserId(userId)
      console.log('美食订单原始数据:', foodOrders)
      if (foodOrders && Array.isArray(foodOrders)) {
        allOrders = allOrders.concat(foodOrders.map(o => ({ ...o, orderType: 'food' })))
      }
      
      // 获取商品订单
      const productOrders = await productOrderApi.getByUserId(userId)
      console.log('商品订单原始数据:', productOrders)
      if (productOrders && Array.isArray(productOrders)) {
        allOrders = allOrders.concat(productOrders.map(o => ({ ...o, orderType: 'product' })))
      }
      
      // 获取非遗体验订单
      const experienceOrders = await experienceOrderApi.getByUserId(userId)
      console.log('体验订单原始数据:', experienceOrders)
      if (experienceOrders && Array.isArray(experienceOrders)) {
        allOrders = allOrders.concat(experienceOrders.map(o => ({ ...o, orderType: 'experience' })))
      }
      
      console.log('后端返回的所有订单:', allOrders)
    } catch (e) {
      console.error('后端订单获取失败', e)
      uni.hideLoading()
      uni.showToast({ title: '获取订单失败', icon: 'none' })
      return
    }

    // 2. 过滤出有效的订单（已支付、待使用等状态）
    // 支持后端状态码和中文显示状态
    const validStatuses = ['paid', 'confirmed', '待使用', '已支付', 'pending_use', '已确认', '已入住', 'checked_in', '待发货', 'shipped']
    const validOrders = allOrders.filter(o => validStatuses.includes(o.status))
    
    console.log('有效订单:', validOrders)

    if (validOrders.length === 0) {
      uni.hideLoading()
      uni.showToast({ title: '没有可导入的订单', icon: 'none' })
      return
    }

    let importedCount = 0
    let unrecognizedCount = 0

    // 3. 处理各种类型的订单
    for (const order of validOrders) {
      // 获取订单名称（支持驼峰和下划线两种命名）
      let orderName = ''
      switch (order.orderType) {
        case 'ticket':
          orderName = order.ticketName || order.ticket_name || order.name || order.attractionName || order.attraction_name || ''
          break
        case 'hotel':
          orderName = order.hotelName || order.hotel_name || order.name || ''
          break
        case 'food':
          orderName = order.foodName || order.food_name || order.name || ''
          break
        case 'product':
          orderName = order.productName || order.product_name || order.name || ''
          break
        case 'experience':
          orderName = order.heritageName || order.experience_name || order.name || order.experienceName || ''
          break
        default:
          orderName = order.name || ''
      }
      
      if (!orderName) {
        console.log('跳过无名称订单:', order)
        continue
      }
      
      console.log('处理订单:', order.orderType, orderName)
      
      // 清理名称中的后缀（如"门票"、"住宿"等）
      const cleanName = orderName.replace(/门票|住宿|套餐|体验|服务/g, '')
      
      // 判断是否为酒店订单
      const isHotel = order.orderType === 'hotel' || orderName.includes('酒店') || orderName.includes('民宿') || orderName.includes('客栈')
      
      if (isHotel) {
        // 处理酒店订单
        const hotel = staticData?.hotels?.find(h => 
          h.name === orderName || 
          h.name?.includes(orderName) || 
          orderName?.includes(h.name) ||
          h.name === cleanName ||
          h.name?.includes(cleanName) ||
          cleanName.includes(h.name)
        )
        
        if (hotel) {
          const newPoi = {
            id: hotel.id || Date.now() + Math.random(),
            name: hotel.name,
            image: hotel.image || hotel.cover || '',
            cover: hotel.cover || hotel.image || '',
            location: hotel.location || hotel.address || '',
            address: hotel.address || hotel.location || '',
            price: hotel.price || order.totalAmount || order.total_amount || order.price || 0,
            lat: hotel.lat || hotel.latitude,
            lng: hotel.lng || hotel.longitude
          }
          const exists = hotels.value.some(h => h.name === newPoi.name)
          if (!exists) {
            hotels.value.push(newPoi)
            importedCount++
            console.log('添加酒店:', newPoi)
          }
        } else {
          console.log('未找到匹配酒店:', orderName, '可用酒店列表:', staticData?.hotels?.map(h => h.name))
          unrecognizedCount++
        }
      } else {
        // 处理景点/美食/商品/体验订单（作为景点导入）
        const attraction = staticData?.attractions?.find(a => 
          a.name === orderName || 
          a.name?.includes(orderName) || 
          orderName?.includes(a.name) ||
          a.name === cleanName ||
          a.name?.includes(cleanName) ||
          cleanName.includes(a.name)
        )
        
        if (attraction) {
          const newPoi = {
            id: attraction.id || Date.now() + Math.random(),
            name: attraction.name,
            image: attraction.image || attraction.cover || '',
            cover: attraction.cover || attraction.image || '',
            location: attraction.location || attraction.address || '',
            address: attraction.address || attraction.location || '',
            price: attraction.price || order.totalAmount || order.total_amount || order.price || 0,
            lat: attraction.lat || attraction.latitude,
            lng: attraction.lng || attraction.longitude
          }
          const exists = attractions.value.some(a => a.name === newPoi.name)
          if (!exists) {
            attractions.value.push(newPoi)
            importedCount++
            console.log('添加景点:', newPoi)
          }
        } else {
          console.log('未找到匹配景点:', orderName, '可用景点列表:', staticData?.attractions?.map(a => a.name))
          unrecognizedCount++
        }
      }
    }

    uni.hideLoading()

    if (importedCount > 0) {
      if (unrecognizedCount > 0) {
        uni.showToast({ title: `导入${importedCount}项，${unrecognizedCount}项无法识别`, icon: 'none', duration: 2000 })
      } else {
        uni.showToast({ title: `成功导入 ${importedCount} 项`, icon: 'success' })
      }
    } else {
      uni.showToast({ title: '订单中的项目无法识别，请检查订单名称是否与景点/酒店一致', icon: 'none', duration: 2500 })
    }

  } catch (e) {
    uni.hideLoading()
    console.error('导入订单失败', e)
    uni.showToast({ title: '导入失败', icon: 'none' })
  }
};

const onMovableChange = (e, index) => {
  draggingIndex.value = index;
  if (e.detail && e.detail.y !== undefined) {
    yList.value[index] = e.detail.y;
  }
};

const onMovableEnd = (index) => {
  if (draggingIndex.value !== -1 && yList.value[index]) {
    const y = yList.value[index];
    const itemHeight = 160; // 估算每个item的高度
    const moveIndex = Math.round(y / itemHeight);
    const newIndex = Math.max(0, Math.min(attractions.value.length - 1, index + moveIndex));
    
    if (newIndex !== index) {
      const item = attractions.value.splice(index, 1)[0];
      attractions.value.splice(newIndex, 0, item);
    }
  }
  yList.value = {};
  draggingIndex.value = -1;
};

const generateRoute = async () => {
  if (!routeName.value) {
    uni.showToast({ title: '请输入行程名称', icon: 'none' });
    return;
  }
  if (sortedPoiList.value.length === 0) {
    uni.showToast({ title: '请至少添加一个景点', icon: 'none' });
    return;
  }
  if (!startDate.value || !endDate.value) {
    uni.showToast({ title: '请选择出行日期', icon: 'none' });
    return;
  }

  isGenerating.value = true;

  try {
    const dailyList = [];
    const allStopsWithCoords = [];
    
    // 确保按天分组已初始化
    if (dailyPoiGroups.value.length === 0) {
      initDailyGroups();
    }
    
    // 使用按天分组的数据
    const actualDays = dailyPoiGroups.value.length;
    
    for (let i = 0; i < actualDays; i++) {
      const dayStops = [];
      const dayPois = dailyPoiGroups.value[i];
      
      // 每天从上午9:00开始，每个景点停留2小时，下午5:00结束
      let currentHour = 9;
      let currentMinute = 0;

      for (let j = 0; j < dayPois.length; j++) {
        const poi = dayPois[j];
        if (poi) {
          const detail = poi.type === 'hotel' 
            ? '入住休息，感受当地特色住宿'
            : '参观游览，感受侗族文化魅力';
          const duration = poi.type === 'hotel' ? '一晚' : '约 2 小时';
          
          // 生成时间显示，从9:00开始，每次加2小时
          let timeStr = '';
          if (poi.type === 'hotel') {
            timeStr = '全天';
          } else {
            // 上午时间，从9:00开始，到17:00结束
            const hour = Math.min(currentHour, 17);
            timeStr = `${String(hour).padStart(2, '0')}:${String(currentMinute).padStart(2, '0')}`;
            // 每次增加2小时
            currentHour += 2;
            // 如果超过17:00，第二天继续
            if (currentHour >= 17) {
              currentHour = 9;
            }
          }
          
          dayStops.push({
            type: poi.type,
            name: poi.name,
            cover: poi.image || poi.cover || '',
            detail: detail,
            duration: duration,
            time: timeStr,
            lat: poi.lat,
            lng: poi.lng
          });
          allStopsWithCoords.push({ lat: poi.lat, lng: poi.lng });
        }
      }

      // 计算每个站点之间的距离和耗时
      for (let k = 0; k < dayStops.length; k++) {
        if (k === 0) {
          dayStops[k].distanceToNext = null;
          dayStops[k].timeToNext = null;
        } else {
          const prev = dayStops[k - 1];
          const curr = dayStops[k];
          if (prev.lat && prev.lng && curr.lat && curr.lng) {
            const straightDist = getDistance(prev.lat, prev.lng, curr.lat, curr.lng);
            const routeDist = getRouteDistance(straightDist, transport.value);
            const timeMin = getEstimatedTime(routeDist, transport.value);
            dayStops[k].distanceToNext = routeDist;
            dayStops[k].timeToNext = timeMin;
          } else {
            dayStops[k].distanceToNext = null;
            dayStops[k].timeToNext = null;
          }
        }
      }

      if (dayStops.length > 0) {
        dailyList.push({
          theme: getDayTheme(i),
          stops: dayStops
        });
      }
    }

    if (dailyList.length === 0) {
      dailyList.push({
        theme: '侗族文化体验',
        stops: sortedPoiList.value.map(poi => ({
          type: poi.type,
          name: poi.name,
          detail: poi.type === 'hotel' ? '入住休息' : '参观游览',
          duration: poi.type === 'hotel' ? '一晚' : '约2小时',
          lat: poi.lat,
          lng: poi.lng,
          distanceToNext: null,
          timeToNext: null
        }))
      });
    }

    // 计算总距离
    let totalDistance = 0;
    for (let i = 0; i < allStopsWithCoords.length - 1; i++) {
      const from = allStopsWithCoords[i];
      const to = allStopsWithCoords[i + 1];
      if (from.lat && from.lng && to.lat && to.lng) {
        const straightDist = getDistance(from.lat, from.lng, to.lat, to.lng);
        totalDistance += getRouteDistance(straightDist, transport.value);
      }
    }
    totalDistance = Math.round(totalDistance * 10) / 10;

    generatedRoute.value = {
      name: routeName.value,
      description: routeDesc.value || '一条精心规划的三江侗族文化之旅',
      days: dailyList.length,
      distance: totalDistance || (dailyList.length * 15),
      budget: '¥' + computedTotalCost.value,
      totalCost: computedTotalCost.value,
      personCount: people.value,
      daily: dailyList
    };

    setTimeout(() => {
      showRouteDetailModal.value = true;
    }, 100);

  } catch (e) {
    console.error('生成路线失败:', e);
    uni.showToast({ title: '生成失败，请重试', icon: 'none' });
  } finally {
    isGenerating.value = false;
  }
};

// 显示路线详情弹窗
const showRouteDetail = () => {
  console.log('generatedRoute:', generatedRoute.value);
  if (generatedRoute.value) {
    console.log('打开详情弹窗');
    showRouteDetailModal.value = true;
  } else {
    uni.showToast({ title: '请先生成路线', icon: 'none' });
  }
};

// 关闭路线详情弹窗
const closeRouteDetail = () => {
  showRouteDetailModal.value = false;
};

// 阻止事件冒泡
const stopPropagation = (e) => {
  e.stopPropagation();
};

// 导航到单个景点
const navigateToStop = (stop) => {
  // 从 attractionsMap 中查找景点的经纬度
  const attraction = availableAttractions.value.find(a => a.name === stop.name);
  if (attraction && attraction.lat && attraction.lng) {
    openLocation(attraction);
  } else {
    // 尝试从 static-data 获取
    uni.showToast({ title: '正在获取位置信息', icon: 'none' });
    // 这里可以添加从后端获取景点详情的逻辑
    setTimeout(() => {
      uni.showToast({ title: '该景点暂无位置信息', icon: 'none' });
    }, 500);
  }
};

// 打开地图导航
const openLocation = (attraction) => {
  uni.showModal({
    title: '选择导航方式',
    content: `导航到：${attraction.name}`,
    confirmText: '腾讯地图',
    cancelText: '系统地图',
    success: (res) => {
      if (res.confirm) {
        // 打开腾讯地图导航
        openTencentMapNavigation(attraction);
      } else if (res.cancel) {
        // 打开系统地图
        uni.openLocation({
          latitude: parseFloat(attraction.lat),
          longitude: parseFloat(attraction.lng),
          name: attraction.name,
          address: attraction.address || attraction.location
        });
      }
    }
  });
};

// 打开腾讯地图导航
const openTencentMapNavigation = (attraction) => {
  try {
    const key = 'OB4BZ-D4W3U-B7VVO-4PJWW-6TKDJ-WPB77'
    const referer = '寻侗三江'
    
    // 获取当前位置作为起点
    uni.getLocation({
      type: 'gcj02',
      success: (res) => {
        const startPoint = {
          latitude: res.latitude,
          longitude: res.longitude,
          name: '我的位置'
        }
        const endPoint = {
          latitude: parseFloat(attraction.lat),
          longitude: parseFloat(attraction.lng),
          name: attraction.name
        }
        
        // 使用 uni.navigateTo 打开腾讯地图插件页面
        uni.navigateTo({
          url: 'plugin://route-plan/index?key=' + key +
               '&referer=' + encodeURIComponent(referer) +
               '&endPoint=' + encodeURIComponent(JSON.stringify(endPoint)) +
               '&startPoint=' + encodeURIComponent(JSON.stringify(startPoint)) +
               '&mode=driving',
          success: function() {
            console.log('导航开启成功')
          },
          fail: function(err) {
            console.error('导航开启失败:', err)
            uni.showToast({ title: '导航开启失败', icon: 'none' })
            // 降级方案：打开系统地图
            uni.openLocation({
              latitude: parseFloat(attraction.lat),
              longitude: parseFloat(attraction.lng),
              name: attraction.name,
              address: attraction.address || attraction.location
            })
          }
        })
      },
      fail: () => {
        uni.showToast({ title: '获取位置失败', icon: 'none' })
      }
    })
  } catch (e) {
    console.error('加载路线规划插件失败:', e)
    // 降级方案：直接打开位置
    uni.openLocation({
      latitude: parseFloat(attraction.lat),
      longitude: parseFloat(attraction.lng),
      name: attraction.name,
      address: attraction.address || attraction.location
    })
  }
};

// 开始完整路线导航
const startFullNavigation = () => {
  if (!generatedRoute.value || !generatedRoute.value.daily) {
    uni.showToast({ title: '路线数据为空', icon: 'none' });
    return;
  }
  
  // 获取所有景点
  const allStops = [];
  generatedRoute.value.daily.forEach(day => {
    day.stops.forEach(stop => {
      if (stop.type === 'attraction') {
        allStops.push(stop);
      }
    });
  });
  
  if (allStops.length === 0) {
    uni.showToast({ title: '路线中没有景点', icon: 'none' });
    return;
  }
  
  // 从第一个景点开始导航
  navigateToStop(allStops[0]);
};



const getBudgetText = () => {
  const texts = { low: '约500元', medium: '约1000元', high: '约2000元' };
  return texts[budget.value] || '约1000元';
};

const saveRoute = async () => {
  if (!generatedRoute.value) {
    uni.showToast({ title: '请先生成路线', icon: 'none' });
    return;
  }
  
  // 敏感词检测
  const description = generatedRoute.value.description || '';
  if (description) {
    try {
      const sensitiveCheck = await checkSensitiveWordsAsync(description);
      if (sensitiveCheck.hasSensitive) {
        uni.showModal({
          title: '提示',
          content: getSensitiveWordMessage(sensitiveCheck.sensitiveWords),
          showCancel: false,
          confirmText: '我知道了'
        });
        return;
      }
    } catch (e) {
      console.error('敏感词检测失败:', e);
      // 检测失败时，使用本地检测作为降级方案
      const localCheck = checkSensitiveWordsAsync(description);
      if (localCheck.hasSensitive) {
        uni.showModal({
          title: '提示',
          content: getSensitiveWordMessage(localCheck.sensitiveWords),
          showCancel: false,
          confirmText: '我知道了'
        });
        return;
      }
    }
  }
  
  isSaving.value = true;
  
  try {
    // 获取用户信息
    const userInfo = uni.getStorageSync('userInfo') || {};
    const userId = userInfo.userId || userInfo.id;
    
    // 构建保存数据 - user_routes 表需要的格式
    const routeToSave = {
      userId: userId || 0,
      name: generatedRoute.value.name,
      description: generatedRoute.value.description || '',
      days: generatedRoute.value.days || 1,
      distance: String(generatedRoute.value.distance || '0'),
      budget: generatedRoute.value.budget || 'medium',
      daily: JSON.stringify(generatedRoute.value.daily || []),
      attractions: JSON.stringify(attractions.value.map(a => ({id: a.id, name: a.name}))),
      hotels: JSON.stringify(hotels.value.map(h => ({id: h.id, name: h.name}))),
      startDate: startDate.value || '',
      endDate: endDate.value || '',
      people: people.value || 1,
      transport: transport.value || 'drive',
      status: 1
    };
    
    // 先保存到本地
    const localRouteData = {
      ...routeToSave,
      id: Date.now(),
      daily: generatedRoute.value.daily || [],
      attractions: attractions.value,
      hotels: hotels.value,
      createdAt: new Date().toISOString()
    };
    
    const savedRoutes = uni.getStorageSync('savedRoutes') || [];
    savedRoutes.unshift(localRouteData);
    uni.setStorageSync('savedRoutes', savedRoutes);
    
    // 保存到后端 user_routes 表
    try {
      const res = await userRouteApi.create(routeToSave);
      console.log('路线已保存到后端:', res);
      if (res && res.errorCode === 'SENSITIVE_WORDS') {
        // 后端也检测到敏感词
        uni.showModal({
          title: '提示',
          content: res.message || '描述包含敏感词，请修改后重新提交',
          showCancel: false,
          confirmText: '我知道了'
        });
        return;
      }
    } catch (e) {
      console.error('保存到后端失败:', e);
      // 检查是否是敏感词错误
      if (e && e.data && e.data.errorCode === 'SENSITIVE_WORDS') {
        uni.showModal({
          title: '提示',
          content: e.data.message || '描述包含敏感词，请修改后重新提交',
          showCancel: false,
          confirmText: '我知道了'
        });
        return;
      }
      // 后端保存失败不影响本地保存
    }
    
    uni.showToast({
      title: '保存成功',
      icon: 'success'
    });
    
    // 保存成功后返回上一页
    setTimeout(() => {
      uni.navigateBack();
    }, 1500);
    
  } catch (e) {
    console.error('保存路线失败:', e);
    uni.showToast({
      title: '保存失败',
      icon: 'none'
    });
  } finally {
    isSaving.value = false;
  }
};

const startNavigation = () => {
  if (generatedRoute.value) {
    uni.showToast({
      title: '即将开始导航',
      icon: 'success'
    });
  }
};

// ========== 生命周期 ==========
onLoad(() => {
  const systemInfo = uni.getSystemInfoSync();
  const windowHeight = systemInfo.windowHeight;
  const statusBarHeight = systemInfo.statusBarHeight || 0;
  contentHeight.value = (windowHeight - 88) + 'px';
  
  // 初始化按天分组
  initDailyGroups();
  
  loadData();
});
</script>

<style scoped>
.custom-route-page {
  min-height: 100vh;
  background: linear-gradient(180deg, #f0f7ff 0%, #ffffff 50%, #f8fafc 100%);
}

.header {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  z-index: 100;
  background: linear-gradient(135deg, #1D3B5A 0%, #2A4A75 100%);
  padding-top: var(--status-bar-height);
}

.nav-bar {
  height: 88rpx;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 32rpx;
}

.back-btn {
  width: 60rpx;
  height: 60rpx;
  display: flex;
  align-items: center;
  justify-content: center;
}

.back-icon {
  font-size: 48rpx;
  color: #fff;
  font-weight: bold;
}

.title {
  font-size: 36rpx;
  font-weight: 600;
  color: #fff;
}

.placeholder {
  width: 60rpx;
}

.loading-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 60vh;
  gap: 24rpx;
}

.loading-spinner {
  width: 60rpx;
  height: 60rpx;
  border: 4rpx solid #f0f7ff;
  border-top-color: #4A90E2;
  border-radius: 50%;
  animation: spin 1s linear infinite;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

.loading-text {
  font-size: 28rpx;
  color: #64748b;
}

.content {
  padding-top: calc(88rpx + var(--status-bar-height));
}

.section {
  margin: 24rpx 32rpx;
  background: #ffffff;
  border-radius: 24rpx;
  padding: 32rpx;
  box-shadow: 0 4rpx 20rpx rgba(29, 59, 90, 0.06);
}

.import-btn-wrapper {
  margin-bottom: 24rpx;
}

.import-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12rpx;
  background: linear-gradient(135deg, #4A90E2 0%, #2E5F8E 100%);
  border-radius: 16rpx;
  padding: 24rpx 32rpx;
  color: #fff;
  font-size: 28rpx;
  font-weight: 500;
}

.import-icon {
  width: 36rpx;
  height: 36rpx;
}

.import-hint {
  display: block;
  text-align: center;
  margin-top: 12rpx;
  font-size: 22rpx;
  color: #94A3B8;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24rpx;
}

.section-title {
  font-size: 32rpx;
  font-weight: 600;
  color: #1D3B5A;
}

.add-btn {
  display: flex;
  align-items: center;
  gap: 8rpx;
  font-size: 28rpx;
  color: #4A90E2;
  font-weight: 500;
}

.add-icon {
  font-size: 36rpx;
  font-weight: bold;
}

.input-group {
  margin-bottom: 24rpx;
}

.input-group:last-child {
  margin-bottom: 0;
}

.input {
  width: 100%;
  height: 88rpx;
  padding: 0 24rpx;
  background: #f8fafc;
  border-radius: 16rpx;
  font-size: 28rpx;
  color: #1e293b;
  box-sizing: border-box;
}

.textarea {
  width: 100%;
  min-height: 160rpx;
  padding: 24rpx;
  background: #f8fafc;
  border-radius: 16rpx;
  font-size: 28rpx;
  color: #1e293b;
  box-sizing: border-box;
}

.empty-hint {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12rpx;
  padding: 60rpx 0;
}

.empty-hint text:first-child {
  font-size: 28rpx;
  color: #94a3b8;
}

.hint-sub {
  font-size: 24rpx;
  color: #cbd5e1;
}

.movable-area {
  display: flex;
  flex-direction: column;
  gap: 16rpx;
  min-height: 400rpx;
}

.poi-list-wrapper {
  display: flex;
  flex-direction: column;
  gap: 16rpx;
}

.poi-item-wrapper {
  width: 100%;
  display: block;
}

.poi-movable {
  width: 100%;
  display: block;
}

.poi-item {
  display: flex;
  align-items: center;
  gap: 16rpx;
  padding: 24rpx 20rpx;
  background: linear-gradient(135deg, #ffffff 0%, #f8fafc 100%);
  border-radius: 20rpx;
  box-shadow: 0 4rpx 20rpx rgba(0,0,0,0.08);
  border: 1rpx solid #f0f4f8;
}

.poi-content {
  flex: 1;
  display: flex;
  align-items: center;
  gap: 16rpx;
}

.poi-image {
  width: 100rpx;
  height: 100rpx;
  border-radius: 16rpx;
  flex-shrink: 0;
  box-shadow: 0 4rpx 12rpx rgba(0,0,0,0.1);
}

.poi-info {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 6rpx;
}

.poi-name {
  font-size: 29rpx;
  font-weight: 700;
  color: #1e293b;
  line-height: 1.3;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.poi-location {
  font-size: 24rpx;
  color: #64748b;
  line-height: 1.4;
  display: -webkit-box;
  -webkit-line-clamp: 1;
  -webkit-box-orient: vertical;
  overflow: hidden;
  text-overflow: ellipsis;
}

.poi-price {
  font-size: 27rpx;
  color: #ff6b35;
  font-weight: 700;
  line-height: 1.3;
}

.poi-distance {
  font-size: 22rpx;
  color: #3b82f6;
  display: flex;
  align-items: center;
  gap: 6rpx;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.poi-distance .distance-icon {
  width: 28rpx;
  height: 28rpx;
}

.distance-summary {
  margin-top: 20rpx;
  padding: 18rpx 24rpx;
  background: linear-gradient(135deg, #eff6ff 0%, #dbeafe 100%);
  border-radius: 14rpx;
  text-align: center;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8rpx;
}

.distance-summary .summary-distance-icon {
  width: 30rpx;
  height: 30rpx;
}

.distance-summary text {
  font-size: 25rpx;
  color: #1D3B5A;
  font-weight: 500;
}

.poi-actions {
  display: flex;
  flex-direction: column;
  gap: 8rpx;
  align-items: center;
  flex-shrink: 0;
}

.add-to-schedule-btn {
  padding: 10rpx 18rpx;
  background: linear-gradient(135deg, #3b82f6 0%, #2563eb 100%);
  border-radius: 10rpx;
  color: #fff;
  font-size: 22rpx;
  font-weight: 600;
  white-space: nowrap;
  box-shadow: 0 4rpx 12rpx rgba(59, 130, 246, 0.3);
  transition: all 0.3s ease;
  text-align: center;
  min-width: 80rpx;
}

.add-to-schedule-btn:active {
  transform: scale(0.95);
  box-shadow: 0 2rpx 8rpx rgba(59, 130, 246, 0.2);
}

.remove-btn {
  width: 44rpx;
  height: 44rpx;
  background: linear-gradient(135deg, #fef2f2 0%, #fecdd3 100%);
  border-radius: 10rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #dc2626;
  font-size: 28rpx;
  font-weight: 700;
  box-shadow: 0 4rpx 12rpx rgba(220, 38, 38, 0.15);
  transition: all 0.3s ease;
}

.remove-btn:active {
  transform: scale(0.95);
  box-shadow: 0 2rpx 8rpx rgba(220, 38, 38, 0.12);
}

.date-selector {
  display: flex;
  gap: 20rpx;
}

.date-input {
  flex: 1;
  height: 112rpx;
  background: linear-gradient(135deg, #F8FAFC 0%, #F1F5F9 100%);
  border-radius: 20rpx;
  padding: 0 28rpx;
  display: flex;
  flex-direction: column;
  justify-content: center;
  box-shadow: 0 2rpx 8rpx rgba(0,0,0,0.04);
  border: 1rpx solid #E2E8F0;
}

.date-label {
  font-size: 24rpx;
  color: #64748b;
  margin-bottom: 8rpx;
  font-weight: 500;
}

.date-value {
  font-size: 30rpx;
  font-weight: 600;
  color: #1e293b;
}

.days-info {
  margin-top: 20rpx;
  text-align: center;
  font-size: 28rpx;
  color: #4A90E2;
  font-weight: 500;
}

.people-selector {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 28rpx;
  padding: 16rpx 0;
}

.people-btn {
  width: 72rpx;
  height: 72rpx;
  background: linear-gradient(135deg, #F0F7FF 0%, #E0EFFF 100%);
  border-radius: 16rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 32rpx;
  color: #4A90E2;
  font-weight: 700;
  box-shadow: 0 2rpx 8rpx rgba(74, 144, 226, 0.15);
  transition: all 0.3s ease;
}

.people-btn:active {
  transform: scale(0.95);
  box-shadow: 0 1rpx 4rpx rgba(74, 144, 226, 0.1);
}

.people-btn.disabled {
  opacity: 0.5;
  box-shadow: none;
}

.people-number {
  font-size: 36rpx;
  font-weight: 700;
  color: #1D3B5A;
  min-width: 88rpx;
  text-align: center;
}

.transport-list {
  display: flex;
  gap: 16rpx;
}

.transport-item {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12rpx;
  padding: 28rpx 16rpx;
  background: linear-gradient(135deg, #F8FAFC 0%, #F1F5F9 100%);
  border-radius: 20rpx;
  border: 2rpx solid transparent;
  box-shadow: 0 2rpx 8rpx rgba(0,0,0,0.04);
  transition: all 0.3s ease;
}

.transport-item.active {
  background: linear-gradient(135deg, #F0F7FF 0%, #E0EFFF 100%);
  border-color: #4A90E2;
  box-shadow: 0 4rpx 16rpx rgba(74, 144, 226, 0.15);
}

.transport-item:active {
  transform: scale(0.98);
}

.transport-icon {
  width: 72rpx;
  height: 72rpx;
}

.transport-name {
  font-size: 24rpx;
  color: #1e293b;
}

.budget-selector {
  display: flex;
  gap: 16rpx;
}

.budget-item {
  flex: 1;
  height: 96rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #F8FAFC 0%, #F1F5F9 100%);
  border-radius: 20rpx;
  border: 2rpx solid transparent;
  box-shadow: 0 2rpx 8rpx rgba(0,0,0,0.04);
  transition: all 0.3s ease;
}

.budget-item:active {
  transform: scale(0.98);
}

.budget-item.active {
  background: linear-gradient(135deg, #F0F7FF 0%, #E0EFFF 100%);
  border-color: #4A90E2;
  box-shadow: 0 4rpx 16rpx rgba(74, 144, 226, 0.15);
}

.budget-range {
  font-size: 28rpx;
  color: #1e293b;
  font-weight: 500;
}

.budget-item.active .budget-range {
  color: #4A90E2;
  font-weight: 700;
}

.action-section {
  margin: 40rpx 32rpx 32rpx;
}

.generate-btn {
  height: 108rpx;
  background: linear-gradient(135deg, #1D3B5A 0%, #2A4A75 100%);
  border-radius: 28rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 34rpx;
  font-weight: 700;
  color: #fff;
  box-shadow: 0 8rpx 24rpx rgba(29, 59, 90, 0.25);
  transition: all 0.3s ease;
}

.generate-btn:active {
  transform: scale(0.98);
  box-shadow: 0 4rpx 12rpx rgba(29, 59, 90, 0.2);
}

.generate-btn.disabled {
  opacity: 0.7;
}

.generating-container {
  display: flex;
  align-items: center;
  gap: 16rpx;
}

.generating-spinner {
  width: 32rpx;
  height: 32rpx;
  border: 3rpx solid rgba(255, 255, 255, 0.3);
  border-top-color: #fff;
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
}

.result-section {
  margin: 0 32rpx 32rpx;
}

.result-section.skeleton {
  border-radius: 24rpx;
  overflow: hidden;
}

.skeleton-header {
  height: 160rpx;
  background: linear-gradient(135deg, #e2e8f0 0%, #cbd5e1 100%);
  animation: shimmer 1.5s infinite;
}

.skeleton-metrics {
  display: flex;
  padding: 32rpx 0;
  background: #fff;
  gap: 16rpx;
}

.skeleton-metric {
  flex: 1;
  height: 80rpx;
  background: #e2e8f0;
  border-radius: 12rpx;
  animation: shimmer 1.5s infinite;
}

.skeleton-daily {
  background: #fff;
  padding: 24rpx;
}

.skeleton-day {
  height: 200rpx;
  background: #e2e8f0;
  border-radius: 16rpx;
  margin-bottom: 24rpx;
  animation: shimmer 1.5s infinite;
}

@keyframes shimmer {
  0% { opacity: 0.5; }
  50% { opacity: 1; }
  100% { opacity: 0.5; }
}

.result-header {
  background: linear-gradient(135deg, #1D3B5A 0%, #2A4A75 100%);
  border-radius: 24rpx 24rpx 0 0;
  padding: 40rpx 32rpx;
}

.result-title {
  font-size: 40rpx;
  font-weight: 700;
  color: #fff;
  margin-bottom: 12rpx;
}

.result-desc {
  font-size: 26rpx;
  color: rgba(255, 255, 255, 0.8);
  line-height: 1.6;
}

.route-metrics {
  display: flex;
  background: #fff;
  padding: 32rpx 0;
  border-bottom: 1rpx solid #f1f5f9;
}

.metric {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8rpx;
}

.metric-icon {
  width: 48rpx;
  height: 48rpx;
}

.metric-value {
  font-size: 28rpx;
  font-weight: 600;
  color: #1D3B5A;
}

.metric-label {
  font-size: 22rpx;
  color: #64748b;
}

.daily-list {
  background: #fff;
  padding: 32rpx 24rpx;
}

.daily-item {
  margin-bottom: 32rpx;
}

.daily-item:last-child {
  margin-bottom: 0;
}

.day-header {
  display: flex;
  align-items: center;
  gap: 16rpx;
  margin-bottom: 24rpx;
}

.day-title {
  font-size: 30rpx;
  font-weight: 600;
  color: #1D3B5A;
}

.day-theme {
  font-size: 24rpx;
  color: #4A90E2;
  padding: 6rpx 16rpx;
  background: #f0f7ff;
  border-radius: 20rpx;
}

.stop-list {
  display: flex;
  flex-direction: column;
  gap: 0;
}

.stop-item {
  display: flex;
  gap: 20rpx;
  position: relative;
  padding-left: 60rpx;
}

.stop-marker {
  position: absolute;
  left: 10rpx;
  top: 24rpx;
  width: 20rpx;
  height: 20rpx;
  background: #4A90E2;
  border-radius: 50%;
  border: 4rpx solid #e0efff;
}

.stop-marker::before {
  content: '';
  position: absolute;
  top: 16rpx;
  left: 50%;
  transform: translateX(-50%);
  width: 2rpx;
  height: 100rpx;
  background: #cbd5e1;
}

.stop-marker.first::before {
  top: 0;
  height: 80rpx;
}

.stop-marker.last::before {
  display: none;
}

.stop-content {
  flex: 1;
  background: #f8fafc;
  border-radius: 16rpx;
  padding: 24rpx;
  margin-bottom: 16rpx;
}

.stop-header {
  display: flex;
  align-items: center;
  gap: 12rpx;
  margin-bottom: 12rpx;
}

.stop-icon {
  width: 40rpx;
  height: 40rpx;
}

.stop-name {
  font-size: 28rpx;
  font-weight: 600;
  color: #1e293b;
}

.stop-detail {
  font-size: 24rpx;
  color: #64748b;
  margin-bottom: 12rpx;
  line-height: 1.5;
}

.stop-meta {
  display: flex;
  align-items: center;
  gap: 20rpx;
  flex-wrap: wrap;
}

.stop-duration {
  display: flex;
  align-items: center;
  gap: 8rpx;
}

.stop-distance {
  display: flex;
  align-items: center;
  gap: 8rpx;
}

.distance-text {
  font-size: 22rpx;
  color: #F59E0B;
  font-weight: 500;
}

.duration-icon {
  width: 28rpx;
  height: 28rpx;
}

.duration-text {
  font-size: 22rpx;
  color: #4A90E2;
}

.bottom-actions {
  display: flex;
  gap: 20rpx;
  padding: 30rpx;
  position: sticky;
  bottom: 0;
  background: #fff;
  box-shadow: 0 -2rpx 10rpx rgba(0,0,0,0.05);
}

.action-btn {
  flex: 1;
  padding: 24rpx;
  border-radius: 50rpx;
  text-align: center;
  font-size: 28rpx;
  font-weight: 600;
  transition: all 0.3s ease;
}

.action-btn.secondary {
  background: linear-gradient(135deg, #1D3B5A, #2E5F8E);
  color: #fff;
  box-shadow: 0 4rpx 16rpx rgba(29, 59, 90, 0.35);
}

.action-btn.primary {
  background: linear-gradient(135deg, #4A90E2, #357ABD);
  color: #fff;
  box-shadow: 0 4rpx 16rpx rgba(74,144,226,0.35);
}

.action-btn.disabled {
  opacity: 0.6;
}

/* ===== 路线详情弹窗 ===== */
.route-detail-modal {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(29,59,90,0.6);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 2000;
  padding: 20rpx;
}

.route-detail-content {
  width: 100%;
  max-height: 85vh;
  height: 80vh;
  background: #fff;
  border-radius: 32rpx;
  overflow: hidden;
  display: flex;
  flex-direction: column;
  box-shadow: 0 10rpx 40rpx rgba(0,0,0,0.3);
}

.route-detail-header {
  padding: 30rpx;
  background: linear-gradient(135deg, #4A90E2, #357ABD);
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-shrink: 0;
}

.detail-title {
  font-size: 34rpx;
  font-weight: 700;
  color: #fff;
}

.detail-close {
  font-size: 48rpx;
  color: rgba(255,255,255,0.9);
  width: 60rpx;
  height: 60rpx;
  display: flex;
  align-items: center;
  justify-content: center;
}

.route-detail-scroll {
  flex: 1;
  overflow-y: scroll;
  height: 100%;
}

.detail-metrics {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16rpx;
  padding: 24rpx;
  background: linear-gradient(180deg, #F0F7FF 0%, #FFFFFF 100%);
  border-bottom: 1rpx solid #E8F4FD;
}

.detail-metric {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 6rpx;
}

.detail-metric .metric-icon {
  width: 32rpx;
  height: 32rpx;
}

.detail-metric .metric-value {
  font-size: 26rpx;
  font-weight: 600;
  color: #1D3B5A;
}

.detail-metric .metric-label {
  font-size: 22rpx;
  color: #64748B;
}

.detail-daily-list {
  padding: 24rpx;
}

.detail-daily-item {
  margin-bottom: 32rpx;
}

.detail-day-header {
  display: flex;
  align-items: center;
  gap: 16rpx;
  margin-bottom: 20rpx;
  padding-bottom: 16rpx;
  border-bottom: 1rpx solid #E8F4FD;
}

.detail-day-number {
  font-size: 30rpx;
  font-weight: 700;
  color: #1D3B5A;
}

.detail-day-theme {
  font-size: 24rpx;
  color: #4A90E2;
  background: linear-gradient(135deg, #E8F4FD, #F0F7FF);
  padding: 6rpx 16rpx;
  border-radius: 20rpx;
  border: 1rpx solid #B3D9F5;
}

.detail-stop-list {
  display: flex;
  flex-direction: column;
  gap: 16rpx;
}

.detail-stop-item {
  display: flex;
  gap: 16rpx;
  padding: 20rpx;
  background: linear-gradient(135deg, #F8FBFF, #FFFFFF);
  border-radius: 16rpx;
  border: 1rpx solid #E8F4FD;
}

.detail-stop-index {
  width: 40rpx;
  height: 40rpx;
  border-radius: 50%;
  background: linear-gradient(135deg, #4A90E2, #357ABD);
  color: #fff;
  font-size: 24rpx;
  font-weight: 600;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.detail-stop-content {
  flex: 1;
}

.detail-stop-header {
  display: flex;
  align-items: center;
  gap: 10rpx;
  margin-bottom: 10rpx;
}

.detail-stop-icon {
  width: 28rpx;
  height: 28rpx;
}

.detail-stop-name {
  font-size: 28rpx;
  font-weight: 600;
  color: #1D3B5A;
  flex: 1;
}

.detail-stop-type {
  font-size: 20rpx;
  padding: 4rpx 12rpx;
  border-radius: 16rpx;
}

.detail-stop-type.type-attraction {
  background: linear-gradient(135deg, #E8F4FD, #F0F7FF);
  color: #4A90E2;
  border: 1rpx solid #B3D9F5;
}

.detail-stop-type.type-hotel {
  background: linear-gradient(135deg, #E8FDF0, #F0FFF4);
  color: #10B981;
  border: 1rpx solid #6EE7B7;
}

.detail-stop-detail {
  font-size: 24rpx;
  color: #64748B;
  margin-bottom: 10rpx;
  line-height: 1.6;
}

.detail-stop-duration {
  display: flex;
  align-items: center;
  gap: 6rpx;
  margin-bottom: 12rpx;
}

.detail-stop-duration .duration-icon {
  width: 24rpx;
  height: 24rpx;
}

.detail-stop-duration .duration-text {
  font-size: 22rpx;
  color: #94A3B8;
}

.detail-stop-meta {
  display: flex;
  align-items: center;
  gap: 16rpx;
  margin-bottom: 12rpx;
}

.detail-stop-distance {
  display: flex;
  align-items: center;
  gap: 12rpx;
  padding: 12rpx 0;
}

.distance-line {
  flex: 1;
  height: 2rpx;
  background: linear-gradient(90deg, transparent, #F59E0B, transparent);
}

.distance-badge {
  display: flex;
  align-items: center;
  gap: 8rpx;
  padding: 8rpx 20rpx;
  background: linear-gradient(135deg, #FFFBEB, #FEF3C7);
  border-radius: 24rpx;
  border: 1rpx solid #FDE68A;
  flex-shrink: 0;
}

.distance-badge .distance-icon {
  width: 24rpx;
  height: 24rpx;
}

.distance-info {
  font-size: 22rpx;
  color: #D97706;
  font-weight: 600;
  white-space: nowrap;
}

.detail-stop-nav {
  display: flex;
  align-items: center;
  gap: 8rpx;
  padding: 12rpx 16rpx;
  background: linear-gradient(135deg, #E8F4FD, #F0F7FF);
  border-radius: 24rpx;
  border: 1rpx solid #B3D9F5;
}

.detail-stop-nav .nav-icon {
  width: 24rpx;
  height: 24rpx;
}

.detail-stop-nav text {
  font-size: 22rpx;
  color: #4A90E2;
  font-weight: 600;
}

.route-detail-actions {
  display: flex;
  justify-content: center;
  padding: 20rpx 30rpx;
  border-top: 1rpx solid #E8F4FD;
  background: #fff;
  flex-shrink: 0;
}

.route-detail-btn {
  width: 60%;
  padding: 22rpx;
  border-radius: 50rpx;
  text-align: center;
  font-size: 28rpx;
  font-weight: 600;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8rpx;
}

.route-detail-btn.secondary {
  background: linear-gradient(135deg, #1D3B5A, #2E5F8E);
  color: #fff;
  box-shadow: 0 4rpx 16rpx rgba(29, 59, 90, 0.35);
}

.route-detail-btn.primary {
  background: linear-gradient(135deg, #4A90E2, #357ABD);
  color: #fff;
  box-shadow: 0 4rpx 16rpx rgba(74,144,226,0.35);
}

.nav-btn-icon {
  width: 32rpx;
  height: 32rpx;
}

.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  z-index: 1500;
  display: flex;
  align-items: center;
  justify-content: center;
}

.modal-content {
  width: 90%;
  max-height: 80vh;
  height: 70vh;
  background: #fff;
  border-radius: 24rpx;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.modal-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 32rpx;
  border-bottom: 1rpx solid #f1f5f9;
  flex-shrink: 0;
}

.modal-title {
  font-size: 32rpx;
  font-weight: 600;
  color: #1e293b;
}

.modal-close {
  width: 60rpx;
  height: 60rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 48rpx;
  color: #64748b;
  font-weight: bold;
}

.search-box {
  padding: 16rpx 32rpx;
  flex-shrink: 0;
}

.search-input {
  width: 100%;
  height: 72rpx;
  padding: 0 24rpx;
  background: #f8fafc;
  border-radius: 16rpx;
  font-size: 26rpx;
  color: #1e293b;
  box-sizing: border-box;
}

.picker-list {
  flex: 1;
  padding: 8rpx 32rpx;
  overflow-y: scroll;
}

.picker-item {
  display: flex;
  align-items: center;
  gap: 20rpx;
  padding: 20rpx;
  background: #f8fafc;
  border-radius: 16rpx;
  margin-bottom: 16rpx;
  border: 2rpx solid transparent;
}

.picker-item.selected {
  border-color: #4A90E2;
  background: #f0f7ff;
}

.picker-image {
  width: 100rpx;
  height: 100rpx;
  border-radius: 12rpx;
  flex-shrink: 0;
}

.picker-info {
  flex: 1;
  min-width: 0;
}

.picker-name {
  font-size: 28rpx;
  font-weight: 600;
  color: #1e293b;
  margin-bottom: 8rpx;
}

.picker-location {
  font-size: 24rpx;
  color: #64748b;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.picker-price {
  font-size: 26rpx;
  color: #FF7F50;
  font-weight: 600;
  margin-top: 4rpx;
}

.modal-footer {
  padding: 24rpx 32rpx 48rpx;
  border-top: 1rpx solid #f1f5f9;
  flex-shrink: 0;
}

.confirm-btn {
  height: 96rpx;
  background: linear-gradient(135deg, #4A90E2 0%, #357ABD 100%);
  border-radius: 24rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 32rpx;
  font-weight: 600;
  color: #fff;
}

/* 路线预览样式 */
.route-preview-list {
  display: flex;
  flex-direction: column;
  gap: 20rpx;
}

.route-preview-item {
  display: flex;
  flex-direction: column;
  position: relative;
  transition: transform 0.2s ease, box-shadow 0.2s ease;
}

.route-preview-item:hover {
  transform: translateY(-2rpx);
}

.route-connector {
  position: absolute;
  top: 70rpx;
  left: 22rpx;
  width: 2rpx;
  height: calc(100% - 50rpx);
  z-index: 1;
}

.connector-line {
  width: 100%;
  height: 100%;
  background: linear-gradient(180deg, #4A90E2, #B3D9F5);
  border-radius: 2rpx;
}

.route-poi-content {
  display: flex;
  align-items: center;
  gap: 16rpx;
  padding: 20rpx;
  background: linear-gradient(135deg, #F8FBFF, #FFFFFF);
  border-radius: 16rpx;
  border: 1rpx solid #E8F4FD;
  position: relative;
  z-index: 2;
}



.sort-actions {
  display: flex;
  align-items: center;
  gap: 16rpx;
}

.optimize-btn {
  display: flex;
  align-items: center;
  gap: 6rpx;
  padding: 8rpx 16rpx;
  background: linear-gradient(135deg, #4A90E2, #357ABD);
  border-radius: 24rpx;
}

.optimize-btn:active {
  transform: scale(0.95);
}

.optimize-icon {
  font-size: 20rpx;
  color: #fff;
}

.optimize-text {
  font-size: 22rpx;
  color: #fff;
  font-weight: 500;
}

.sort-hint {
  display: flex;
  align-items: center;
}

.sort-hint-text {
  font-size: 22rpx;
  color: #94A3B8;
}

.route-poi-actions {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8rpx;
  flex-shrink: 0;
}

.single-move-btn {
  padding: 8rpx;
}

.single-move-btn .action-icon-img {
  width: 36rpx;
  height: 36rpx;
}

.action-btn {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  transition: all 0.2s;
  border-radius: 8rpx;
  padding: 6rpx;
  min-width: 44rpx;
}

.action-btn:active {
  transform: scale(0.95);
}

.simple-btn {
  background: transparent;
  border: none;
  box-shadow: none;
}

.action-icon {
  font-size: 24rpx;
  line-height: 1;
}

.action-icon-img {
  width: 36rpx;
  height: 36rpx;
  flex-shrink: 0;
}

.route-poi-index {
  width: 48rpx;
  height: 48rpx;
  background: linear-gradient(135deg, #4A90E2, #357ABD);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-size: 24rpx;
  font-weight: 600;
  flex-shrink: 0;
}

.route-poi-image {
  width: 80rpx;
  height: 80rpx;
  border-radius: 12rpx;
  flex-shrink: 0;
}

.route-poi-info {
  flex: 1;
  min-width: 0;
}

.route-poi-name {
  font-size: 28rpx;
  font-weight: 600;
  color: #1e293b;
  margin-bottom: 6rpx;
  display: flex;
  align-items: center;
  gap: 8rpx;
}

.hotel-badge {
  font-size: 20rpx;
  padding: 4rpx 12rpx;
  border-radius: 16rpx;
  background: linear-gradient(135deg, #E8FDF0, #F0FFF4);
  color: #10B981;
  border: 1rpx solid #6EE7B7;
}

.attraction-badge {
  font-size: 20rpx;
  padding: 4rpx 12rpx;
  border-radius: 16rpx;
  background: linear-gradient(135deg, #E8F4FD, #F0F7FF);
  color: #4A90E2;
  border: 1rpx solid #B3D9F5;
}

.route-poi-location {
  font-size: 24rpx;
  color: #64748b;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.route-distance-info {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  gap: 6rpx;
  padding: 12rpx 20rpx;
  background: linear-gradient(135deg, #F0F7FF, #E8F4FD);
  border-radius: 0 0 16rpx 16rpx;
  margin-top: -16rpx;
  border-top: 1rpx solid #E8F4FD;
}

.route-distance-info text {
  font-size: 22rpx;
  color: #4A90E2;
  font-weight: 500;
}

.distance-icon-small {
  width: 24rpx;
  height: 24rpx;
}

/* 按天分组样式 */
.day-selector {
  display: flex;
  align-items: center;
  gap: 12rpx;
}

.day-label {
  font-size: 24rpx;
  color: #64748B;
  font-weight: 500;
}

.day-controls {
  display: flex;
  align-items: center;
  gap: 16rpx;
  background: linear-gradient(135deg, #F8FAFC 0%, #F1F5F9 100%);
  padding: 8rpx 12rpx;
  border-radius: 24rpx;
  box-shadow: 0 2rpx 8rpx rgba(0,0,0,0.05);
  border: 1rpx solid #E2E8F0;
}

.day-btn {
  width: 64rpx;
  height: 64rpx;
  border-radius: 16rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #4A90E2 0%, #357ABD 100%);
  color: #fff;
  font-size: 36rpx;
  font-weight: 700;
  box-shadow: 0 4rpx 12rpx rgba(74, 144, 226, 0.25);
  transition: all 0.3s ease;
}

.day-btn:active {
  transform: scale(0.95);
  box-shadow: 0 2rpx 6rpx rgba(74, 144, 226, 0.15);
}

.day-btn.disabled {
  background: linear-gradient(135deg, #E2E8F0 0%, #CBD5E1 100%);
  color: #94A3B8;
  box-shadow: none;
}

.day-num {
  font-size: 32rpx;
  font-weight: 700;
  color: #1D3B5A;
  min-width: 56rpx;
  text-align: center;
}

.daily-preview-list {
  display: flex;
  flex-direction: column;
  gap: 32rpx;
}

.day-group {
  display: flex;
  flex-direction: column;
  gap: 16rpx;
}

.day-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 20rpx 28rpx;
  background: linear-gradient(135deg, #F0F7FF 0%, #E8F4FD 100%);
  border-radius: 20rpx;
  border-left: 8rpx solid #4A90E2;
  box-shadow: 0 2rpx 8rpx rgba(74, 144, 226, 0.08);
}

.day-title {
  display: flex;
  flex-direction: column;
  gap: 4rpx;
}

.day-index {
  font-size: 28rpx;
  font-weight: 600;
  color: #1D3B5A;
}

.day-theme {
  font-size: 22rpx;
  color: #64748B;
}

.day-poi-list {
  display: flex;
  flex-direction: column;
  gap: 16rpx;
}

.day-poi-item {
  display: flex;
  flex-direction: column;
}

.day-poi-content {
  display: flex;
  align-items: center;
  gap: 16rpx;
  padding: 24rpx 20rpx;
  background: linear-gradient(135deg, #ffffff 0%, #f8fafc 100%);
  border-radius: 20rpx;
  border: 1rpx solid #f0f4f8;
  box-shadow: 0 4rpx 20rpx rgba(0,0,0,0.06);
  position: relative;
  z-index: 2;
}

.day-poi-image {
  width: 100rpx;
  height: 100rpx;
  border-radius: 16rpx;
  flex-shrink: 0;
  box-shadow: 0 4rpx 12rpx rgba(0,0,0,0.1);
}

.day-poi-info {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 6rpx;
}

.day-poi-name {
  font-size: 29rpx;
  font-weight: 700;
  color: #1e293b;
  display: flex;
  align-items: center;
  gap: 8rpx;
  line-height: 1.3;
  overflow: hidden;
}

.day-poi-title {
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  flex: 1;
  min-width: 0;
  font-weight: 700;
}

.day-poi-location {
  font-size: 24rpx;
  color: #64748b;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  line-height: 1.4;
}

.day-poi-actions {
  display: flex;
  flex-direction: column;
  gap: 8rpx;
  align-items: center;
}

.move-btn {
  width: 40rpx;
  height: 40rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #eff6ff 0%, #dbeafe 100%);
  border-radius: 8rpx;
  box-shadow: 0 2rpx 8rpx rgba(59, 130, 246, 0.15);
  transition: all 0.3s ease;
}

.swap-btn {
  background: linear-gradient(135deg, #eff6ff 0%, #dbeafe 100%);
}

.move-btn:active {
  transform: scale(0.95);
}

.move-icon-img {
  width: 28rpx;
  height: 28rpx;
}

.action-btn.simple-btn {
  width: 40rpx;
  height: 40rpx;
  background: linear-gradient(135deg, #fef2f2 0%, #fecdd3 100%);
  border-radius: 8rpx;
  box-shadow: 0 2rpx 8rpx rgba(220, 38, 38, 0.15);
  transition: all 0.3s ease;
}

.action-btn.simple-btn:active {
  transform: scale(0.95);
}

.action-icon-img {
  width: 22rpx;
  height: 22rpx;
}

.day-poi-connector {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8rpx;
  margin-top: -8rpx;
  margin-bottom: 8rpx;
}

.day-connector-line {
  width: 2rpx;
  height: 40rpx;
  background: linear-gradient(180deg, #4A90E2, #B3D9F5);
  border-radius: 2rpx;
}

.day-distance-info {
  display: flex;
  align-items: center;
  gap: 6rpx;
  padding: 8rpx 16rpx;
  background: linear-gradient(135deg, #F0F7FF, #E8F4FD);
  border-radius: 20rpx;
}

.day-distance-info text {
  font-size: 20rpx;
  color: #4A90E2;
  font-weight: 500;
}

.empty-day-poi {
  padding: 40rpx;
  text-align: center;
  background: #F8FAFC;
  border-radius: 16rpx;
  border: 2rpx dashed #CBD5E1;
}

.empty-day-poi text {
  font-size: 24rpx;
  color: #94A3B8;
}
</style>