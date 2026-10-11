<template>
  <div class="dashboard">
    <!-- 顶部装饰线 -->
    <div class="deco-top-line"></div>

    <div class="page-header">
      <div class="header-left">
        <h2 class="page-title gradient-text">数据概览</h2>
        <p class="page-sub">实时掌握平台运营数据</p>
      </div>
      <div class="header-right">
        <el-select v-model="refreshInterval" @change="startAutoRefresh" style="width:120px;margin-right:12px">
          <el-option label="关闭自动刷新" :value="0" />
          <el-option label="每5秒刷新" :value="5000" />
          <el-option label="每10秒刷新" :value="10000" />
          <el-option label="每30秒刷新" :value="30000" />
          <el-option label="每1分钟刷新" :value="60000" />
        </el-select>
        <div class="button-row">
          <el-button type="primary" @click="loadData" :loading="loading">
            <el-icon><Refresh /></el-icon>
            刷新数据
          </el-button>
        </div>
      </div>
    </div>

    <!-- 统计卡片 - 玻璃拟态风格 -->
    <el-row :gutter="16" class="stat-cards">
      <el-col :span="6" v-if="sectionLoading.stats" v-for="n in 4" :key="'skeleton-top-' + n">
        <SkeletonCard />
      </el-col>
      <el-col :span="6" v-else v-for="(card, index) in statCards.slice(0, 4)" :key="card.key">
        <div
          class="stat-card glass-card"
          :class="'stat-card-' + index"
          :style="{ '--card-accent': card.color, animationDelay: (index * 0.1) + 's' }"
        >
          <div class="stat-card-bg">
            <div class="stat-card-circle circle-1"></div>
            <div class="stat-card-circle circle-2"></div>
          </div>
          <div class="stat-card-content">
            <div class="stat-icon-wrap" :style="{ background: card.color + '18', color: card.color }">
              <el-icon :size="26"><component :is="card.icon" /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">
                <span class="number-counter">{{ formatCardValue(card.key) }}</span>
              </div>
              <div class="stat-label">{{ card.label }}</div>
            </div>
          </div>
        </div>
      </el-col>
    </el-row>

    <!-- 第二行统计卡片 -->
    <el-row :gutter="16" class="stat-cards miniapp-stats">
      <el-col :span="6" v-if="sectionLoading.stats" v-for="n in 4" :key="'skeleton-mini-' + n">
        <SkeletonCard />
      </el-col>
      <el-col :span="6" v-else v-for="(card, index) in statCards.slice(4)" :key="card.key">
        <div
          class="stat-card glass-card miniapp-card"
          :style="{ '--card-accent': card.color, animationDelay: ((index + 4) * 0.1) + 's' }"
        >
          <div class="stat-card-bg">
            <div class="stat-card-circle circle-1"></div>
            <div class="stat-card-circle circle-2"></div>
          </div>
          <div class="stat-card-content">
            <div class="stat-icon-wrap" :style="{ background: card.color + '18', color: card.color }">
              <el-icon :size="24"><component :is="card.icon" /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value small">
                <span class="number-counter">{{ formatCardValue(card.key) }}</span>
              </div>
              <div class="stat-label">{{ card.label }}</div>
            </div>
          </div>
        </div>
      </el-col>
    </el-row>

    <!-- 各类订单收入对比 + 热门景点排行 -->
    <el-row :gutter="16" class="chart-row">
      <el-col :span="14">
        <div class="chart-card glass-card fade-in-up" style="animation-delay: 0.4s">
          <div class="card-header">
            <div class="card-title"><span class="title-accent"></span>各类订单收入对比</div>
            <div class="chart-legend">
              <span class="legend-item" v-for="item in revenueLegend" :key="item.name">
                <span class="legend-dot" :style="{ background: item.color }"></span>{{ item.name }}
              </span>
            </div>
          </div>
          <div ref="revenueChartRef" style="height:300px"></div>
        </div>
      </el-col>
      <el-col :span="10">
        <div class="chart-card glass-card fade-in-up" style="animation-delay: 0.5s">
          <div class="card-header">
            <div class="card-title"><span class="title-accent"></span>热门景点排行</div>
          </div>
          <div class="page-rank-list">
            <div class="rank-item" v-for="(item, idx) in attractionRank" :key="item.name" :style="{ animationDelay: (0.6 + idx * 0.05) + 's' }">
              <span class="rank-num" :class="'rank-' + (idx + 1)">{{ idx + 1 }}</span>
              <span class="rank-name">{{ item.name }}</span>
              <div class="rank-bar-wrap">
                <div class="rank-bar" :style="{ width: item.percent + '%', background: rankColors[idx] }"></div>
              </div>
              <span class="rank-value">{{ item.rating }}</span>
            </div>
            <div v-if="attractionRank.length === 0" class="feed-empty">暂无景点数据</div>
          </div>
        </div>
      </el-col>
    </el-row>

    <!-- 图表区域 -->
    <el-row :gutter="20" class="charts-row">
      <el-col :span="12">
        <div class="chart-card glass-card fade-in-up" style="animation-delay: 0.6s">
          <div class="card-header">
            <div class="card-title">
              <span class="title-accent"></span>
              订单状态分布
            </div>
          </div>
          <div ref="orderChartRef" style="height:280px"></div>
        </div>
      </el-col>
      <el-col :span="12">
        <div class="chart-card glass-card fade-in-up" style="animation-delay: 0.7s">
          <div class="card-header">
            <div class="card-title">
              <span class="title-accent"></span>
              各资源订单转化率
            </div>
          </div>
          <div ref="conversionChartRef" style="height:280px"></div>
        </div>
      </el-col>
    </el-row>

    <!-- 快捷操作 + 实时动态 -->
    <el-row :gutter="20" class="bottom-row">
      <el-col :span="14">
        <div class="quick-actions glass-card fade-in-up" style="animation-delay: 1s">
          <div class="card-header">
            <div class="card-title">
              <span class="title-accent"></span>
              快捷操作
            </div>
          </div>
          <el-row :gutter="12" style="margin-top:16px">
            <el-col :span="4" v-for="item in quickActions" :key="item.path">
              <div class="action-item" @click="$router.push(item.path)" :style="{ '--action-color': item.color }">
                <div class="action-icon-wrap">
                  <el-icon :size="22" :color="item.color"><component :is="item.icon" /></el-icon>
                </div>
                <span class="action-label">{{ item.label }}</span>
              </div>
            </el-col>
          </el-row>
        </div>
      </el-col>
      <el-col :span="10">
        <div class="activity-feed glass-card fade-in-up" style="animation-delay: 1.1s">
          <div class="card-header">
            <div class="card-title">
              <span class="title-accent"></span>
              实时动态
            </div>
            <el-badge :value="pendingCount" :hidden="pendingCount === 0" class="badge-item">
              <span class="live-dot"></span>
              LIVE
            </el-badge>
          </div>
          <div class="feed-list">
            <div
              class="feed-item"
              v-for="(item, idx) in feedItems"
              :key="idx"
              :style="{ animationDelay: (idx * 0.08) + 's' }"
            >
              <div class="feed-dot" :style="{ background: item.color }"></div>
              <div class="feed-content">
                <span class="feed-text">{{ item.text }}</span>
                <span class="feed-time">{{ item.time }}</span>
              </div>
            </div>
            <div v-if="feedItems.length === 0" class="feed-empty">暂无动态</div>
          </div>
        </div>
      </el-col>
    </el-row>

    <!-- 待审核提示 -->
    <el-alert
      v-if="stats.pendingPosts > 0"
      :title="`有 ${stats.pendingPosts} 篇帖子待审核`"
      type="warning"
      show-icon
      class="pending-alert glass-card"
      @click="$router.push('/moments')"
      style="cursor:pointer;margin-top:20px"
    />
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onUnmounted, nextTick } from 'vue'
import * as echarts from 'echarts'
import { Refresh } from '@element-plus/icons-vue'
import { dashboardApi, attractionApi } from '@/api'
import SkeletonCard from '@/components/SkeletonCard.vue'

const stats = reactive({
  attractions: 0,
  orders: 0,
  users: 0,
  posts: 0,
  heritages: 0,
  pendingPosts: 0,
  todayOrders: 0,
  todayUsers: 0,
  totalRevenue: 0,
  ticketRevenue: 0,
  foodRevenue: 0,
  hotelRevenue: 0,
  productRevenue: 0,
  experienceRevenue: 0,
  orderStats: {},
  conversionStats: {},
  recentOrders: []
})

const loading = ref(false)
const sectionLoading = reactive({ stats: true, charts: true })
const refreshInterval = ref(10000)
const pendingCount = ref(0)
let autoRefreshTimer = null

// 实时动态
const feedItems = ref([])

// 景点排行
const attractionRank = ref([])

// 图例
const revenueLegend = [
  { name: '门票', color: '#A33B32' },
  { name: '美食', color: '#1D3B5A' },
  { name: '酒店', color: '#D4A373' },
  { name: '商品', color: '#67C23A' },
  { name: '非遗体验', color: '#8B5CF6' }
]

const rankColors = ['#A33B32', '#1D3B5A', '#D4A373', '#67C23A', '#409EFF', '#E6A23C', '#909399', '#bbb']

// 统计卡片配置
const statCards = [
  { key: 'orders', label: '订单总数', icon: 'Tickets', color: '#A33B32' },
  { key: 'users', label: '注册用户', icon: 'User', color: '#1D3B5A' },
  { key: 'todayOrders', label: '今日订单', icon: 'TrendCharts', color: '#D4A373' },
  { key: 'todayUsers', label: '今日新增用户', icon: 'UserFilled', color: '#67C23A' },
  { key: 'totalRevenue', label: '总收入', icon: 'Money', color: '#409EFF' },
  { key: 'attractions', label: '景点数量', icon: 'Location', color: '#E6A23C' },
  { key: 'posts', label: '社区帖子', icon: 'ChatDotRound', color: '#909399' },
  { key: 'pendingPosts', label: '待审核帖子', icon: 'Warning', color: '#A33B32' }
]

const quickActions = [
  { path: '/attractions', label: '添加景点', icon: 'Plus', color: '#A33B32' },
  { path: '/ticket-orders', label: '查看订单', icon: 'Tickets', color: '#1D3B5A' },
  { path: '/moments', label: '审核帖子', icon: 'Check', color: '#E6A23C' },
  { path: '/heritage', label: '非遗管理', icon: 'Star', color: '#67C23A' },
  { path: '/activities', label: '活动管理', icon: 'Calendar', color: '#409EFF' },
  { path: '/users', label: '用户管理', icon: 'User', color: '#909399' }
]

// 格式化卡片值
function formatCardValue(key) {
  if (key === 'totalRevenue') {
    return '¥' + Number(stats.totalRevenue || 0).toLocaleString('zh-CN', { minimumFractionDigits: 0, maximumFractionDigits: 0 })
  }
  return (stats[key] ?? 0).toLocaleString()
}

// 图表引用
const orderChartRef = ref()
const conversionChartRef = ref()
const revenueChartRef = ref()

// 图表实例缓存
let orderChart = null
let conversionChart = null
let revenueChart = null

const chartColors = {
  red: '#A33B32',
  blue: '#1D3B5A',
  wood: '#D4A373',
  green: '#67C23A',
  accent: '#409EFF',
  warn: '#E6A23C'
}

function getChartTheme() {
  return {
    textStyle: { fontFamily: '-apple-system, BlinkMacSystemFont, "PingFang SC", sans-serif' },
    tooltip: {
      backgroundColor: 'rgba(255,255,255,0.96)',
      borderColor: '#e8e4dc',
      textStyle: { color: '#333' },
      extraCssText: 'box-shadow: 0 4px 20px rgba(0,0,0,0.12); border-radius: 8px; backdrop-filter: blur(10px);'
    }
  }
}

function ensureChart(ref, getter, setter) {
  if (!ref.value) return null
  let chart = getter()
  if (!chart) {
    chart = echarts.init(ref.value)
    setter(chart)
  }
  return chart
}

// 收入对比柱状图
function initRevenueChart(data) {
  const chart = ensureChart(revenueChartRef, () => revenueChart, v => revenueChart = v)
  if (!chart) return
  const theme = getChartTheme()
  chart.setOption({
    ...theme,
    color: ['#A33B32', '#1D3B5A', '#D4A373', '#67C23A', '#8B5CF6'],
    grid: { top: 30, right: 20, bottom: 30, left: 60 },
    xAxis: {
      type: 'category',
      data: ['门票', '美食', '酒店', '商品', '非遗体验'],
      axisLine: { lineStyle: { color: '#e8e4dc' } },
      axisLabel: { color: '#666' }
    },
    yAxis: {
      type: 'value',
      axisLabel: { formatter: '¥{value}', color: '#999' },
      splitLine: { lineStyle: { color: '#f0f0f0', type: 'dashed' } }
    },
    tooltip: { ...theme.tooltip, trigger: 'axis', formatter: '{b}: ¥{c}' },
    series: [{
      type: 'bar',
      barWidth: 40,
      itemStyle: {
        borderRadius: [6, 6, 0, 0],
        color: function(params) {
          const colors = [
            new echarts.graphic.LinearGradient(0, 0, 0, 1, [{ offset: 0, color: '#A33B32' }, { offset: 1, color: '#c0504a' }]),
            new echarts.graphic.LinearGradient(0, 0, 0, 1, [{ offset: 0, color: '#1D3B5A' }, { offset: 1, color: '#2a5580' }]),
            new echarts.graphic.LinearGradient(0, 0, 0, 1, [{ offset: 0, color: '#D4A373' }, { offset: 1, color: '#e0b88a' }]),
            new echarts.graphic.LinearGradient(0, 0, 0, 1, [{ offset: 0, color: '#67C23A' }, { offset: 1, color: '#85cf5c' }]),
            new echarts.graphic.LinearGradient(0, 0, 0, 1, [{ offset: 0, color: '#8B5CF6' }, { offset: 1, color: '#a78bfa' }])
          ]
          return colors[params.dataIndex]
        }
      },
      emphasis: {
        itemStyle: { shadowBlur: 10, shadowColor: 'rgba(0,0,0,0.2)' }
      },
      data: [data.ticket, data.food, data.hotel, data.product, data.experience],
      label: { show: true, position: 'top', formatter: '¥{c}', color: '#666', fontSize: 12 }
    }]
  })
}

function initOrderChart() {
  const chart = ensureChart(orderChartRef, () => orderChart, v => orderChart = v)
  if (!chart) return
  const theme = getChartTheme()
  const os = stats.orderStats
  chart.setOption({
    ...theme,
    color: [chartColors.red, chartColors.blue, chartColors.green, '#999'],
    tooltip: { ...theme.tooltip, trigger: 'item', formatter: '{b}: {c} ({d}%)' },
    legend: { textStyle: { color: '#666', fontSize: 12 }, bottom: 0 },
    series: [{
      type: 'pie',
      radius: ['42%', '72%'],
      center: ['50%', '45%'],
      itemStyle: { borderRadius: 6, borderColor: '#fff', borderWidth: 3 },
      emphasis: {
        itemStyle: { shadowBlur: 20, shadowColor: 'rgba(0,0,0,0.15)' },
        label: { fontSize: 14, fontWeight: 'bold' }
      },
      label: { fontSize: 12, color: '#666' },
      data: [
        { name: '待支付', value: os['待支付'] || 0 },
        { name: '待使用', value: os['待使用'] || 0 },
        { name: '已完成', value: os['已完成'] || 0 },
        { name: '已取消', value: os['已取消'] || 0 }
      ]
    }]
  })
}

function initConversionChart() {
  const chart = ensureChart(conversionChartRef, () => conversionChart, v => conversionChart = v)
  if (!chart) return
  const theme = getChartTheme()
  const cs = stats.conversionStats
  chart.setOption({
    ...theme,
    color: [chartColors.red, chartColors.blue, chartColors.wood, chartColors.green, '#8B5CF6'],
    tooltip: { ...theme.tooltip, trigger: 'axis', formatter: '{b}: {c}%' },
    xAxis: {
      type: 'category',
      data: ['门票', '美食', '酒店', '商品', '非遗体验', '总体'],
      axisLine: { lineStyle: { color: '#e8e4dc' } },
      axisLabel: { color: '#666' }
    },
    yAxis: {
      type: 'value',
      max: 100,
      axisLabel: { formatter: '{value}%', color: '#999' },
      splitLine: { lineStyle: { color: '#f0f0f0', type: 'dashed' } }
    },
    series: [{
      type: 'bar',
      barWidth: 40,
      itemStyle: {
        borderRadius: [6, 6, 0, 0],
        color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
          { offset: 0, color: '#A33B32' },
          { offset: 1, color: '#D4A373' }
        ])
      },
      data: [
        cs.ticketConversionRate || 0,
        cs.foodConversionRate || 0,
        cs.hotelConversionRate || 0,
        cs.productConversionRate || 0,
        cs.experienceConversionRate || 0,
        cs.overallConversionRate || 0
      ],
      label: { show: true, position: 'top', formatter: '{c}%', color: '#666', fontSize: 12 }
    }]
  })
}

// 将最近订单转为动态 feed
function buildFeedItems(recentOrders) {
  const colorMap = {
    ticket: '#A33B32',
    food: '#D4A373',
    hotel: '#1D3B5A',
    product: '#67C23A',
    experience: '#8B5CF6'
  }
  return (recentOrders || []).map((o, idx) => ({
    text: `用户 #${o.userId} 购买了${o.typeLabel}「${o.itemName}」`,
    time: o.timeAgo || '刚刚',
    color: colorMap[o.type] || '#409EFF'
  }))
}

async function loadData() {
  loading.value = true
  sectionLoading.stats = true
  sectionLoading.charts = true
  try {
    const res = await dashboardApi.getStats()
    const data = res.data || {}

    stats.attractions = data.attractions ?? 0
    stats.orders = data.orders ?? 0
    stats.users = data.users ?? 0
    stats.posts = data.posts ?? 0
    stats.heritages = data.heritages ?? 0
    stats.pendingPosts = data.pendingPosts ?? 0
    stats.todayOrders = data.todayOrders ?? 0
    stats.todayUsers = data.todayUsers ?? 0
    stats.totalRevenue = data.totalRevenue ?? 0
    stats.ticketRevenue = data.ticketRevenue ?? 0
    stats.foodRevenue = data.foodRevenue ?? 0
    stats.hotelRevenue = data.hotelRevenue ?? 0
    stats.productRevenue = data.productRevenue ?? 0
    stats.experienceRevenue = data.experienceRevenue ?? 0
    stats.experienceOrders = data.conversionStats?.experienceOrders || 0
    stats.orderStats = data.orderStats ?? {}
    stats.conversionStats = data.conversionStats ?? {}
    stats.recentOrders = data.recentOrders ?? []

    pendingCount.value = stats.pendingPosts
    feedItems.value = buildFeedItems(stats.recentOrders)

    sectionLoading.stats = false
    await nextTick()

    // 加载景点排行
    try {
      const attrRes = await attractionApi.getAll(1, 50)
      const list = attrRes.data?.records || attrRes.data?.list || attrRes.data || []
      const sorted = Array.isArray(list) ? list.filter(a => a.rating).sort((a, b) => (b.rating || 0) - (a.rating || 0)).slice(0, 8) : []
      const maxRating = sorted.length > 0 ? (sorted[0].rating || 1) : 1
      attractionRank.value = sorted.map(a => ({
        name: a.name,
        rating: a.rating?.toFixed(1) || '-',
        percent: Math.round(((a.rating || 0) / maxRating) * 100)
      }))
    } catch (err) {
      console.error('获取景点数据失败:', err)
    }

    setTimeout(() => {
        sectionLoading.charts = false
        nextTick(() => {
          initRevenueChart({
            ticket: stats.ticketRevenue,
            food: stats.foodRevenue,
            hotel: stats.hotelRevenue,
            product: stats.productRevenue,
            experience: stats.experienceRevenue
          })
          initOrderChart()
          initConversionChart()
        })
      }, 300)
  } catch (err) {
    console.error('获取统计数据失败:', err)
    sectionLoading.stats = false
    sectionLoading.charts = false
  } finally {
    loading.value = false
  }
}

function startAutoRefresh() {
  if (autoRefreshTimer) {
    clearInterval(autoRefreshTimer)
    autoRefreshTimer = null
  }
  if (refreshInterval.value > 0) {
    autoRefreshTimer = setInterval(() => {
      loadData()
    }, refreshInterval.value)
  }
}

onMounted(async () => {
  await loadData()
  startAutoRefresh()
})

onUnmounted(() => {
  if (autoRefreshTimer) {
    clearInterval(autoRefreshTimer)
    autoRefreshTimer = null
  }
})
</script>

<style scoped lang="scss">
// ==================== 顶部装饰线 ====================
.deco-top-line {
  height: 3px;
  background: linear-gradient(90deg, #A33B32, #1D3B5A, #D4A373, #A33B32);
  background-size: 300% 100%;
  animation: gradientShift 4s ease infinite;
  border-radius: 2px;
  margin-bottom: 24px;
}

@keyframes gradientShift {
  0%, 100% { background-position: 0% 50%; }
  50% { background-position: 100% 50%; }
}

.gradient-text {
  background: linear-gradient(135deg, #1D3B5A 0%, #A33B32 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.glass-card {
  background: rgba(255, 255, 255, 0.85);
  backdrop-filter: blur(12px);
  -webkit-backdrop-filter: blur(12px);
  border: 1px solid rgba(255, 255, 255, 0.6);
  border-radius: 16px;
  box-shadow:
    0 4px 24px rgba(0, 0, 0, 0.06),
    0 1px 4px rgba(0, 0, 0, 0.04),
    inset 0 1px 0 rgba(255, 255, 255, 0.8);
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);

  &:hover {
    transform: translateY(-2px);
    box-shadow:
      0 8px 32px rgba(0, 0, 0, 0.1),
      0 2px 8px rgba(0, 0, 0, 0.06),
      inset 0 1px 0 rgba(255, 255, 255, 0.8);
  }
}

.fade-in-up {
  opacity: 0;
  animation: fadeInUp 0.6s ease-out forwards;
}

@keyframes fadeInUp {
  from { opacity: 0; transform: translateY(20px); }
  to { opacity: 1; transform: translateY(0); }
}

.dashboard { position: relative; }

.page-header {
  margin-bottom: 24px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  animation: fadeInUp 0.5s ease-out;
}

.header-left { flex: 1; }
.header-right { display: flex; align-items: center; }

.page-title {
  font-size: 26px;
  font-weight: 800;
  margin-bottom: 4px;
  letter-spacing: -0.5px;
}

.page-sub { color: #999; font-size: 14px; }

// ==================== 统计卡片 ====================
.stat-cards { margin-bottom: 24px; }

.stat-card {
  position: relative;
  overflow: hidden;
  padding: 24px;
  cursor: pointer;
  animation: fadeInUp 0.6s ease-out backwards;

  .stat-card-bg {
    position: absolute;
    top: 0; left: 0; right: 0; bottom: 0;
    pointer-events: none;

    .stat-card-circle {
      position: absolute;
      border-radius: 50%;
      opacity: 0.06;

      &.circle-1 {
        width: 120px; height: 120px;
        background: var(--card-accent);
        top: -30px; right: -20px;
      }

      &.circle-2 {
        width: 80px; height: 80px;
        background: var(--card-accent);
        bottom: -20px; left: -10px;
      }
    }
  }

  .stat-card-content {
    position: relative;
    z-index: 1;
    display: flex;
    align-items: center;
    gap: 16px;
  }

  .stat-icon-wrap {
    width: 56px; height: 56px;
    border-radius: 14px;
    display: flex;
    align-items: center;
    justify-content: center;
    flex-shrink: 0;
    transition: transform 0.3s;
  }

  &:hover .stat-icon-wrap { transform: scale(1.08) rotate(-3deg); }

  .stat-value {
    font-size: 32px;
    font-weight: 800;
    color: #1D3B5A;
    line-height: 1;
    margin-bottom: 4px;
    letter-spacing: -1px;

    &.small { font-size: 26px; }
  }

  .number-counter { display: inline-block; }

  .stat-label { font-size: 13px; color: #999; font-weight: 500; }
}

// ==================== 图表卡片 ====================
.charts-row, .chart-row { margin-bottom: 20px; }

.chart-card, .quick-actions { padding: 24px; }

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.chart-legend {
  display: flex;
  gap: 14px;
  .legend-item {
    display: flex;
    align-items: center;
    gap: 5px;
    font-size: 11px;
    color: #666;
  }
  .legend-dot {
    width: 8px;
    height: 8px;
    border-radius: 50%;
  }
}

// 排行
.page-rank-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.rank-item {
  display: flex;
  align-items: center;
  gap: 10px;
  animation: fadeInUp 0.4s ease-out backwards;
}

.rank-num {
  width: 22px;
  height: 22px;
  border-radius: 6px;
  font-size: 11px;
  font-weight: 700;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f0f0f0;
  color: #999;
  flex-shrink: 0;
}

.rank-num.rank-1 {
  background: #A33B32;
  color: #fff;
}

.rank-num.rank-2 {
  background: #1D3B5A;
  color: #fff;
}

.rank-num.rank-3 {
  background: #D4A373;
  color: #fff;
}

.rank-name {
  font-size: 13px;
  color: #555;
  width: 70px;
  flex-shrink: 0;
}

.rank-bar-wrap {
  flex: 1;
  height: 6px;
  background: rgba(0,0,0,0.04);
  border-radius: 3px;
  overflow: hidden;
}

.rank-bar {
  height: 100%;
  border-radius: 3px;
  transition: width 1s ease-out;
}

.rank-value {
  font-size: 12px;
  color: #999;
  width: 40px;
  text-align: right;
  flex-shrink: 0;
}

.card-title {
  font-size: 16px;
  font-weight: 700;
  color: #1D3B5A;
  display: flex;
  align-items: center;
  gap: 8px;

  .title-accent {
    width: 4px; height: 18px;
    background: linear-gradient(180deg, #A33B32, #D4A373);
    border-radius: 2px;
    flex-shrink: 0;
  }
}

// ==================== 快捷操作 ====================
.action-item {
  background: rgba(248, 249, 251, 0.8);
  border-radius: 14px;
  padding: 18px 10px;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 10px;
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  border: 1px solid transparent;

  .action-icon-wrap {
    width: 44px; height: 44px;
    border-radius: 12px;
    display: flex;
    align-items: center;
    justify-content: center;
    transition: all 0.3s;
  }

  .action-label {
    font-size: 13px;
    color: #666;
    font-weight: 500;
    transition: color 0.3s;
  }

  &:hover {
    background: white;
    border-color: var(--action-color);
    transform: translateY(-4px);
    box-shadow: 0 8px 24px rgba(0, 0, 0, 0.08);

    .action-icon-wrap { transform: scale(1.1); }
    .action-label { color: var(--action-color); }
  }
}

// ==================== 实时动态 ====================
.activity-feed { padding: 24px; }

.live-dot {
  display: inline-block;
  width: 8px; height: 8px;
  background: #67C23A;
  border-radius: 50%;
  margin-right: 6px;
  animation: livePulse 2s ease-in-out infinite;
}

@keyframes livePulse {
  0%, 100% { opacity: 1; box-shadow: 0 0 0 0 rgba(103, 194, 58, 0.4); }
  50% { opacity: 0.7; box-shadow: 0 0 0 6px rgba(103, 194, 58, 0); }
}

.feed-list {
  max-height: 240px;
  overflow-y: auto;
}

.feed-item {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  padding: 12px 0;
  border-bottom: 1px solid rgba(0, 0, 0, 0.04);
  animation: fadeInUp 0.4s ease-out backwards;

  &:last-child { border-bottom: none; }

  .feed-dot {
    width: 8px; height: 8px;
    border-radius: 50%;
    margin-top: 6px;
    flex-shrink: 0;
  }

  .feed-content {
    flex: 1;
    display: flex;
    justify-content: space-between;
    align-items: flex-start;
    gap: 8px;
  }

  .feed-text {
    font-size: 13px;
    color: #555;
    line-height: 1.5;
  }

  .feed-time {
    font-size: 11px;
    color: #bbb;
    white-space: nowrap;
    flex-shrink: 0;
  }
}

.feed-empty {
  text-align: center;
  color: #ccc;
  padding: 40px 0;
  font-size: 14px;
}

.bottom-row { margin-bottom: 0; }

// ==================== 小程序指标卡片 ====================
.miniapp-stats {
  margin-top: -4px;
  margin-bottom: 20px;
}

.miniapp-card {
  .stat-icon-wrap {
    width: 48px; height: 48px;
    border-radius: 12px;
  }

  .stat-value {
    font-size: 26px;
    letter-spacing: -0.5px;
  }
}

// ==================== 响应式 ====================
@media screen and (max-width: 1200px) {
  .stat-cards .el-col {
    span: 12 !important;
    margin-bottom: 12px;
  }
}

@media screen and (max-width: 768px) {
  .stat-cards .el-col { span: 24 !important; }
  .charts-row .el-col, .bottom-row .el-col {
    span: 24 !important;
    margin-bottom: 16px;
  }
  .page-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }
  .stat-card .stat-value { font-size: 26px; }
}
</style>
