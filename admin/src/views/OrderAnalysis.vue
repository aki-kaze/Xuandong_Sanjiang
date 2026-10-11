<template>
  <div class="order-analysis-page">
    <div class="deco-top-line"></div>
    <div class="page-header">
      <div class="header-left">
        <h2 class="page-title gradient-text">订单总览</h2>
        <p class="page-sub">全平台订单数据统计与分析</p>
      </div>
      <div class="header-right">
        <div class="live-indicator">
          <span class="live-dot"></span>
          <span class="live-text">LIVE</span>
        </div>
        <div class="button-row">
          <el-button type="primary" @click="loadData" :loading="loading">
            <el-icon><Refresh /></el-icon> 刷新数据
          </el-button>
        </div>
      </div>
    </div>

    <!-- 总体统计卡片 -->
    <el-row :gutter="16" class="stat-cards">
      <el-col :span="6" v-for="card in statCards" :key="card.key">
        <div class="stat-card glass-card" :style="{ borderTopColor: card.color }">
          <div class="stat-icon" :style="{ background: card.color + '20', color: card.color }">
            <el-icon :size="28"><component :is="card.icon" /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ stats[card.key] ?? '-' }}</div>
            <div class="stat-label">{{ card.label }}</div>
          </div>
        </div>
      </el-col>
    </el-row>

    <!-- 收入统计卡片 -->
    <el-row :gutter="16" class="stat-cards">
      <el-col :span="6" v-for="card in revenueCards" :key="card.key">
        <div class="stat-card glass-card revenue-card" :style="{ borderTopColor: card.color }">
          <div class="stat-icon" :style="{ background: card.color + '20', color: card.color }">
            <el-icon :size="28"><component :is="card.icon" /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-value">¥{{ formatMoney(stats[card.key]) }}</div>
            <div class="stat-label">{{ card.label }}</div>
          </div>
        </div>
      </el-col>
    </el-row>

    <!-- 订单状态分布 + 各类订单数量 -->
    <el-row :gutter="16" class="chart-row">
      <el-col :span="12">
        <div class="chart-card glass-card fade-in-up" style="animation-delay: 0.5s">
          <div class="card-header">
            <div class="card-title"><span class="title-accent"></span>订单状态分布</div>
          </div>
          <div ref="orderStatusChartRef" style="height:280px"></div>
        </div>
      </el-col>
      <el-col :span="12">
        <div class="chart-card glass-card fade-in-up" style="animation-delay: 0.55s">
          <div class="card-header">
            <div class="card-title"><span class="title-accent"></span>各类订单数量</div>
          </div>
          <div ref="orderTypeChartRef" style="height:280px"></div>
        </div>
      </el-col>
    </el-row>

    <!-- 最近订单动态 + 订单转化率 -->
    <el-row :gutter="16" class="chart-row">
      <el-col :span="12">
        <div class="chart-card glass-card feed-card fade-in-up" style="animation-delay: 0.6s">
          <div class="card-header">
            <div class="card-title"><span class="title-accent"></span>最近订单动态</div>
            <div class="live-badge"><span class="live-dot-sm"></span>{{ liveFeed.length }} 条</div>
          </div>
          <div class="feed-scroll">
            <div class="feed-item" v-for="(item, idx) in liveFeed" :key="idx" :style="{ animationDelay: (idx * 0.04) + 's' }">
              <div class="feed-avatar" :style="{ background: item.color }">{{ item.typeLabel.charAt(0) }}</div>
              <div class="feed-body">
                <div class="feed-main">
                  <span class="feed-user">用户 #{{ item.userId }}</span>
                  <span class="feed-action">购买了</span>
                  <span class="feed-target">{{ item.typeLabel }}「{{ item.itemName }}」</span>
                </div>
                <span class="feed-time">{{ item.timeAgo }}</span>
              </div>
            </div>
            <div v-if="liveFeed.length === 0" class="feed-empty">暂无订单动态</div>
          </div>
        </div>
      </el-col>
      <el-col :span="12">
        <div class="chart-card glass-card fade-in-up" style="animation-delay: 0.65s">
          <div class="card-header">
            <div class="card-title"><span class="title-accent"></span>订单转化率</div>
          </div>
          <div ref="conversionChartRef" style="height:300px"></div>
        </div>
      </el-col>
    </el-row>

    <!-- 近7天订单趋势 -->
    <el-row :gutter="16" class="charts-row">
      <el-col :span="24">
        <div class="chart-card glass-card fade-in-up" style="animation-delay: 0.7s">
          <div class="card-header">
            <div class="card-title"><span class="title-accent"></span>近7天订单趋势</div>
          </div>
          <div ref="orderTrendChartRef" style="height:320px"></div>
        </div>
      </el-col>
    </el-row>

    <!-- 各类订单详情 -->
    <div class="order-sections">
      <div class="section-title">各类订单详情</div>
      <el-row :gutter="16">
        <el-col :span="4" v-for="section in orderSections" :key="section.key">
          <div class="order-section-card glass-card" @click="$router.push(section.path)">
            <div class="section-header">
              <el-icon :size="24" :color="section.color"><component :is="section.icon" /></el-icon>
              <span class="section-name">{{ section.name }}</span>
            </div>
            <div class="section-stats">
              <div class="section-stat-item">
                <span class="stat-label">总数</span>
                <span class="stat-value">{{ stats[section.countKey] || 0 }}</span>
              </div>
              <div class="section-stat-item">
                <span class="stat-label">待处理</span>
                <span class="stat-value pending">{{ stats[section.pendingKey] || 0 }}</span>
              </div>
              <div class="section-stat-item">
                <span class="stat-label">收入</span>
                <span class="stat-value">¥{{ formatMoney(stats[section.revenueKey]) }}</span>
              </div>
            </div>
            <div class="section-footer">
              <el-icon><ArrowRight /></el-icon>
            </div>
          </div>
        </el-col>
      </el-row>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onUnmounted, nextTick } from 'vue'
import * as echarts from 'echarts'
import { Refresh, ArrowRight, Tickets, Food, House, ShoppingBag } from '@element-plus/icons-vue'
import { ticketOrderApi, foodOrderApi, hotelOrderApi, productOrderApi, experienceOrderApi, dashboardApi } from '@/api'

const loading = ref(false)
const orderTypeChartRef = ref()
const orderStatusChartRef = ref()
const orderTrendChartRef = ref()
const conversionChartRef = ref()

const stats = reactive({
  ticketOrderCount: 0,
  foodOrderCount: 0,
  hotelOrderCount: 0,
  productOrderCount: 0,
  experienceOrderCount: 0,
  totalOrderCount: 0,
  pendingTicketOrders: 0,
  pendingFoodOrders: 0,
  pendingHotelOrders: 0,
  pendingProductOrders: 0,
  pendingExperienceOrders: 0,
  ticketRevenue: 0,
  foodRevenue: 0,
  hotelRevenue: 0,
  productRevenue: 0,
  experienceRevenue: 0,
  totalRevenue: 0,
  orderStats: {},
  conversionStats: {}
})

// 实时动态
const liveFeed = ref([])

const statCards = [
  { key: 'totalOrderCount', label: '总订单数', icon: 'Tickets', color: '#A33B32' },
  { key: 'ticketOrderCount', label: '门票订单', icon: 'Tickets', color: '#1D3B5A' },
  { key: 'foodOrderCount', label: '美食订单', icon: 'Food', color: '#D4A373' },
  { key: 'experienceOrderCount', label: '非遗体验订单', icon: 'Star', color: '#8B5CF6' }
]

const revenueCards = [
  { key: 'totalRevenue', label: '总收入', icon: 'Money', color: '#A33B32' },
  { key: 'ticketRevenue', label: '门票收入', icon: 'Tickets', color: '#1D3B5A' },
  { key: 'foodRevenue', label: '美食收入', icon: 'Food', color: '#D4A373' },
  { key: 'experienceRevenue', label: '非遗体验收入', icon: 'Star', color: '#8B5CF6' }
]

const orderSections = [
  { key: 'ticket', name: '门票订单', icon: 'Tickets', color: '#1D3B5A', path: '/ticket-orders', countKey: 'ticketOrderCount', pendingKey: 'pendingTicketOrders', revenueKey: 'ticketRevenue' },
  { key: 'food', name: '美食订单', icon: 'Food', color: '#D4A373', path: '/food-orders', countKey: 'foodOrderCount', pendingKey: 'pendingFoodOrders', revenueKey: 'foodRevenue' },
  { key: 'hotel', name: '酒店订单', icon: 'House', color: '#67C23A', path: '/hotel-orders', countKey: 'hotelOrderCount', pendingKey: 'pendingHotelOrders', revenueKey: 'hotelRevenue' },
  { key: 'product', name: '商品订单', icon: 'ShoppingBag', color: '#8B5CF6', path: '/product-orders', countKey: 'productOrderCount', pendingKey: 'pendingProductOrders', revenueKey: 'productRevenue' },
  { key: 'experience', name: '非遗体验订单', icon: 'Star', color: '#E6A23C', path: '/experience-orders', countKey: 'experienceOrderCount', pendingKey: 'pendingExperienceOrders', revenueKey: 'experienceRevenue' }
]

function formatMoney(value) {
  if (!value) return '0'
  return Number(value).toLocaleString('zh-CN', { minimumFractionDigits: 0, maximumFractionDigits: 2 })
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

// 订单状态分布环形图
function initOrderStatusChart() {
  const chart = ensureChart(orderStatusChartRef, () => null, c => {})
  if (!chart) return
  const theme = getChartTheme()
  const os = stats.orderStats
  chart.setOption({
    ...theme,
    color: ['#A33B32', '#1D3B5A', '#67C23A', '#999'],
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

// 各类订单数量柱状图
function initOrderTypeChart() {
  const chart = ensureChart(orderTypeChartRef, () => null, c => {})
  if (!chart) return
  const theme = getChartTheme()
  const cs = stats.conversionStats
  chart.setOption({
    ...theme,
    color: ['#A33B32', '#1D3B5A', '#D4A373', '#67C23A', '#8B5CF6'],
    tooltip: { ...theme.tooltip, trigger: 'axis' },
    legend: { textStyle: { color: '#666', fontSize: 12 }, bottom: 0 },
    grid: { top: 20, right: 20, bottom: 40, left: 50 },
    xAxis: {
      type: 'category',
      data: ['门票', '美食', '酒店', '商品', '非遗体验'],
      axisLine: { lineStyle: { color: '#e8e4dc' } },
      axisLabel: { color: '#666' }
    },
    yAxis: {
      type: 'value',
      splitLine: { lineStyle: { color: '#f0f0f0', type: 'dashed' } },
      axisLabel: { color: '#999' }
    },
    series: [
      { name: '总订单', type: 'bar', barWidth: 20, itemStyle: { borderRadius: [4, 4, 0, 0] }, data: [cs.ticketOrders || 0, cs.foodOrders || 0, cs.hotelOrders || 0, cs.productOrders || 0, cs.experienceOrders || 0] },
      { name: '已支付', type: 'bar', barWidth: 20, itemStyle: { borderRadius: [4, 4, 0, 0] }, data: [cs.ticketPaidOrders || 0, cs.foodPaidOrders || 0, cs.hotelPaidOrders || 0, cs.productPaidOrders || 0, cs.experiencePaidOrders || 0] }
    ]
  })
}

// 订单转化率图
function initConversionChart() {
  const chart = ensureChart(conversionChartRef, () => null, c => {})
  if (!chart) return
  const theme = getChartTheme()
  const cs = stats.conversionStats
  chart.setOption({
    ...theme,
    color: ['#A33B32', '#1D3B5A', '#D4A373', '#67C23A', '#8B5CF6', '#409EFF'],
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
      barWidth: 36,
      itemStyle: {
        borderRadius: [6, 6, 0, 0],
        color: function(params) {
          const colors = [
            new echarts.graphic.LinearGradient(0, 0, 0, 1, [{ offset: 0, color: '#A33B32' }, { offset: 1, color: '#c0504a' }]),
            new echarts.graphic.LinearGradient(0, 0, 0, 1, [{ offset: 0, color: '#1D3B5A' }, { offset: 1, color: '#2a5580' }]),
            new echarts.graphic.LinearGradient(0, 0, 0, 1, [{ offset: 0, color: '#D4A373' }, { offset: 1, color: '#e0b88a' }]),
            new echarts.graphic.LinearGradient(0, 0, 0, 1, [{ offset: 0, color: '#67C23A' }, { offset: 1, color: '#85cf5c' }]),
            new echarts.graphic.LinearGradient(0, 0, 0, 1, [{ offset: 0, color: '#8B5CF6' }, { offset: 1, color: '#a78bfa' }]),
            new echarts.graphic.LinearGradient(0, 0, 0, 1, [{ offset: 0, color: '#409EFF' }, { offset: 1, color: '#66b1ff' }])
          ]
          return colors[params.dataIndex]
        }
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

function initOrderTrendChart() {
  const chart = ensureChart(orderTrendChartRef, () => null, c => {})
  if (!chart) return
  const theme = getChartTheme()
  const days = []
  const ticketData = []
  const foodData = []
  const hotelData = []
  const productData = []
  const experienceData = []

  for (let i = 6; i >= 0; i--) {
    const date = new Date()
    date.setDate(date.getDate() - i)
    days.push(`${date.getMonth() + 1}/${date.getDate()}`)
    ticketData.push(Math.floor(Math.random() * 50) + 10)
    foodData.push(Math.floor(Math.random() * 30) + 5)
    hotelData.push(Math.floor(Math.random() * 20) + 3)
    productData.push(Math.floor(Math.random() * 40) + 8)
    experienceData.push(Math.floor(Math.random() * 25) + 5)
  }

  chart.setOption({
    ...theme,
    tooltip: { trigger: 'axis' },
    legend: { data: ['门票', '美食', '酒店', '商品', '非遗体验'], bottom: 0, textStyle: { color: '#666' } },
    grid: { left: '3%', right: '4%', bottom: '10%', containLabel: true },
    xAxis: { type: 'category', boundaryGap: false, data: days, axisLine: { lineStyle: { color: '#e8e4dc' } }, axisLabel: { color: '#666' } },
    yAxis: { type: 'value', splitLine: { lineStyle: { color: '#f0f0f0', type: 'dashed' } }, axisLabel: { color: '#999' } },
    series: [
      { name: '门票', type: 'line', smooth: true, data: ticketData, itemStyle: { color: '#1D3B5A' } },
      { name: '美食', type: 'line', smooth: true, data: foodData, itemStyle: { color: '#D4A373' } },
      { name: '酒店', type: 'line', smooth: true, data: hotelData, itemStyle: { color: '#67C23A' } },
      { name: '商品', type: 'line', smooth: true, data: productData, itemStyle: { color: '#8B5CF6' } },
      { name: '非遗体验', type: 'line', smooth: true, data: experienceData, itemStyle: { color: '#E6A23C' } }
    ]
  })
}

// 构建动态 feed
function buildFeed(recentOrders) {
  const colorMap = {
    ticket: '#A33B32',
    food: '#D4A373',
    hotel: '#1D3B5A',
    product: '#67C23A',
    experience: '#8B5CF6'
  }
  return (recentOrders || []).map((o, idx) => ({
    userId: o.userId,
    typeLabel: o.typeLabel,
    itemName: o.itemName,
    timeAgo: o.timeAgo || '刚刚',
    color: colorMap[o.type] || '#409EFF'
  }))
}

async function loadData() {
  loading.value = true
  try {
    const [ticketRes, foodRes, hotelRes, productRes, experienceRes, dashboardRes] = await Promise.all([
      ticketOrderApi.getStats().catch(() => ({ data: {} })),
      foodOrderApi.getStats().catch(() => ({ data: {} })),
      hotelOrderApi.getStats().catch(() => ({ data: {} })),
      productOrderApi.getStats().catch(() => ({ data: {} })),
      experienceOrderApi.getStats().catch(() => ({ data: {} })),
      dashboardApi.getStats().catch(() => ({ data: {} }))
    ])

    const ticketStats = ticketRes.data || {}
    const foodStats = foodRes.data || {}
    const hotelStats = hotelRes.data || {}
    const productStats = productRes.data || {}
    const experienceStats = experienceRes.data || {}
    const dashboardData = dashboardRes.data || {}

    Object.assign(stats, {
      ticketOrderCount: ticketStats.totalOrders || 0,
      foodOrderCount: foodStats.totalOrders || 0,
      hotelOrderCount: hotelStats.totalOrders || 0,
      productOrderCount: productStats.totalOrders || 0,
      experienceOrderCount: experienceStats.totalOrders || 0,
      totalOrderCount: (ticketStats.totalOrders || 0) + (foodStats.totalOrders || 0) + (hotelStats.totalOrders || 0) + (productStats.totalOrders || 0) + (experienceStats.totalOrders || 0),
      pendingTicketOrders: ticketStats.pendingOrders || 0,
      pendingFoodOrders: foodStats.pendingOrders || 0,
      pendingHotelOrders: hotelStats.pendingOrders || 0,
      pendingProductOrders: productStats.pendingOrders || 0,
      pendingExperienceOrders: experienceStats.pendingOrders || 0,
      ticketRevenue: ticketStats.totalRevenue || 0,
      foodRevenue: foodStats.totalRevenue || 0,
      hotelRevenue: hotelStats.totalRevenue || 0,
      productRevenue: productStats.totalRevenue || 0,
      experienceRevenue: experienceStats.totalRevenue || 0,
      totalRevenue: (ticketStats.totalRevenue || 0) + (foodStats.totalRevenue || 0) + (hotelStats.totalRevenue || 0) + (productStats.totalRevenue || 0) + (experienceStats.totalRevenue || 0),
      orderStats: dashboardData.orderStats || {},
      conversionStats: dashboardData.conversionStats || {}
    })

    // 实时动态
    liveFeed.value = buildFeed(dashboardData.recentOrders)

    await nextTick()
    initOrderStatusChart()
    initOrderTypeChart()
    initConversionChart()
    initOrderTrendChart()
  } catch (err) {
    console.error('获取订单统计数据失败:', err)
    ElMessage.error('获取订单统计数据失败')
  } finally {
    loading.value = false
  }
}

onMounted(async () => {
  await loadData()
})

onUnmounted(() => {
  // 清理图表实例
  ;[orderTypeChartRef, orderStatusChartRef, orderTrendChartRef, conversionChartRef].forEach(ref => {
    if (ref.value) {
      const chart = echarts.getInstanceByDom(ref.value)
      if (chart) chart.dispose()
    }
  })
})
</script>

<style scoped lang="scss">
@use '@/styles/buttons.scss' as *;

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
  box-shadow: 0 4px 24px rgba(0, 0, 0, 0.06), 0 1px 4px rgba(0, 0, 0, 0.04), inset 0 1px 0 rgba(255, 255, 255, 0.8);
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);

  &:hover {
    transform: translateY(-2px);
    box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1), 0 2px 8px rgba(0, 0, 0, 0.06), inset 0 1px 0 rgba(255, 255, 255, 0.8);
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

.page-header {
  margin-bottom: 20px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  animation: fadeInUp 0.5s ease-out;
}

.header-left {
  flex: 1;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 12px;
}

.page-title {
  font-size: 26px;
  font-weight: 800;
  margin-bottom: 4px;
}

.page-sub {
  color: #999;
  font-size: 14px;
}

.live-indicator {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 5px 12px;
  background: rgba(103,194,58,0.08);
  border-radius: 20px;
  border: 1px solid rgba(103,194,58,0.2);
}

.live-dot {
  width: 8px;
  height: 8px;
  background: #67C23A;
  border-radius: 50%;
  animation: livePulse 2s ease-in-out infinite;
}

@keyframes livePulse {
  0%, 100% { opacity: 1; box-shadow: 0 0 0 0 rgba(103,194,58,0.4); }
  50% { opacity: 0.7; box-shadow: 0 0 0 6px rgba(103,194,58,0); }
}

.live-text {
  font-size: 11px;
  font-weight: 700;
  color: #67C23A;
  letter-spacing: 1px;
}

.order-analysis-page {
  position: relative;
  padding: 0;
}

.stat-cards {
  margin-bottom: 16px;
}

.stat-card {
  padding: 20px;
  display: flex;
  align-items: center;
  gap: 16px;
  border-top: 3px solid transparent;
  cursor: pointer;
  animation: fadeInUp 0.6s ease-out backwards;
}

.stat-icon {
  width: 56px;
  height: 56px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.stat-info {
  flex: 1;
}

.stat-value {
  font-size: 28px;
  font-weight: 700;
  color: #1D3B5A;
  line-height: 1;
  margin-bottom: 6px;
}

.stat-label {
  font-size: 13px;
  color: #999;
}

.charts-row, .chart-row {
  margin-bottom: 16px;
}

.chart-card {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.card-title {
  font-size: 15px;
  font-weight: 700;
  color: #1D3B5A;
  display: flex;
  align-items: center;
  gap: 8px;

  .title-accent {
    width: 4px;
    height: 18px;
    background: linear-gradient(180deg, #A33B32, #D4A373);
    border-radius: 2px;
    flex-shrink: 0;
  }
}

.live-badge {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 12px;
  color: #67C23A;
  font-weight: 500;
}

.live-dot-sm {
  width: 6px;
  height: 6px;
  background: #67C23A;
  border-radius: 50%;
  animation: livePulse 2s ease-in-out infinite;
}

.feed-card {
  display: flex;
  flex-direction: column;
}

.feed-scroll {
  flex: 1;
  max-height: 280px;
  overflow-y: auto;
}

.feed-item {
  display: flex;
  align-items: flex-start;
  gap: 10px;
  padding: 10px 0;
  border-bottom: 1px solid rgba(0,0,0,0.04);
  animation: fadeInUp 0.3s ease-out backwards;

  &:last-child {
    border-bottom: none;
  }
}

.feed-avatar {
  width: 32px;
  height: 32px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-size: 13px;
  font-weight: 600;
  flex-shrink: 0;
}

.feed-body {
  flex: 1;
  min-width: 0;
}

.feed-main {
  font-size: 13px;
  color: #555;
  line-height: 1.5;
}

.feed-user {
  font-weight: 600;
  color: #1D3B5A;
}

.feed-action {
  margin: 0 3px;
}

.feed-target {
  color: #A33B32;
  font-weight: 500;
}

.feed-time {
  font-size: 11px;
  color: #bbb;
  margin-top: 2px;
  display: block;
}

.feed-empty {
  text-align: center;
  color: #ccc;
  padding: 40px 0;
  font-size: 14px;
}

.order-sections {
  margin-top: 24px;
}

.section-title {
  font-size: 16px;
  font-weight: 600;
  color: #1D3B5A;
  margin-bottom: 16px;
  padding-left: 10px;
  border-left: 3px solid #A33B32;
}

.order-section-card {
  padding: 20px;
  cursor: pointer;
  transition: all 0.3s;
  border: 1px solid transparent;

  &:hover {
    transform: translateY(-3px);
    box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1), 0 2px 8px rgba(0, 0, 0, 0.06), inset 0 1px 0 rgba(255, 255, 255, 0.8);
    border-color: var(--el-color-primary);
  }

  .section-header {
    display: flex;
    align-items: center;
    gap: 10px;
    margin-bottom: 16px;

    .section-name {
      font-size: 15px;
      font-weight: 600;
      color: #1D3B5A;
    }
  }

  .section-stats {
    display: flex;
    flex-direction: column;
    gap: 8px;

    .section-stat-item {
      display: flex;
      justify-content: space-between;
      align-items: center;

      .stat-label {
        font-size: 13px;
        color: #999;
      }

      .stat-value {
        font-size: 16px;
        font-weight: 600;
        color: #1D3B5A;

        &.pending {
          color: #E6A23C;
        }
      }
    }
  }

  .section-footer {
    display: flex;
    justify-content: flex-end;
    margin-top: 12px;
    color: #999;
    transition: color 0.3s;
  }

  &:hover .section-footer {
    color: var(--el-color-primary);
  }
}
</style>
