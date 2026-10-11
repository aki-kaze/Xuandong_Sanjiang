<template>
  <view class="list-page">
    <!-- 搜索-->
    <view class="search-bar" @tap="goSearch">
      <image class="search-icon" src="/static/icons/search.png" mode="aspectFit" />
      <text class="search-placeholder">搜索三江景点...</text>
    </view>

    <!-- 分类筛选-->
    <view class="filter-bar">
      <view class="filter-item" v-for="f in dynamicFilters" :key="f.id" :class="{ active: activeFilter === f.id }" @tap="activeFilter = f.id">
        {{ f.label }}
      </view>
    </view>

    <!-- 排序 -->
    <view class="sort-bar">
      <view class="sort-item" v-for="s in sorts" :key="s.id" :class="{ active: activeSort === s.id }" @tap="activeSort = s.id">
        {{ s.label }}
      </view>
    </view>

    <!-- 景点列表 -->
    <view class="attraction-list">
      <view class="attraction-card card-shadow" v-for="item in sortedAttractions" :key="item.id" @tap="goDetail(item.id)">
        <view class="attraction-img-wrap">
          <image class="attraction-img" :src="item.cover" mode="aspectFill" />
          <view class="attraction-price-badge">{{ item.price === 0 ? '免费' : '¥' + item.price }}</view>
        </view>
        <view class="attraction-info">
          <view class="attraction-header">
            <text class="attraction-name">{{ item.name }}</text>
            <text class="attraction-rating">⭐{{ item.rating }}</text>
          </view>
          <view class="attraction-tags">
            <text class="a-tag" v-for="tag in (item.tags || []).slice(0,2)" :key="tag">{{ tag }}</text>
          </view>
          <text class="attraction-desc">{{ (item.description || item.desc || '').slice(0, 50) }}...</text>
          <view class="attraction-footer">
            <text class="attraction-addr"><image src="/static/icons/position.png" mode="aspectFit" style="width:20rpx;height:20rpx;margin-right:4rpx;" /> {{ (item.address || '').slice(0, 15) }}...</text>
            <view class="attraction-btns">
              <view class="btn-map" @tap.stop="goMap(item)">导航</view>
              <view class="btn-ticket" @tap.stop="goTicket(item.id)">购票</view>
            </view>
          </view>
        </view>
      </view>
    </view>

    <view style="height: 40rpx;"></view>
  </view>
</template>

<script>
import { attractionApi } from '../../utils/api.js'
import { staticData } from '../../utils/static-data.js'

export default {
  data() {
    return {
      attractions: [],
      loading: true,
      activeFilter: 'all',
      activeSort: 'default',
      filters: [
        { id: 'all', label: '全部' },
        { id: '古村落', label: '古村落' },
        { id: '古建筑', label: '古建筑' },
        { id: '博物馆', label: '博物馆' },
        { id: '古镇', label: '古镇' },
        { id: '自然风光', label: '自然风光' },
        { id: '文化体验', label: '文化体验' }
      ],
      sorts: [
        { id: 'default', label: '综合' },
        { id: 'rating', label: '评分' },
        { id: 'priceAsc', label: '价格低到高' },
        { id: 'priceDesc', label: '价格高到低' }
      ]
    }
  },
  computed: {
    dynamicFilters() {
      const categorySet = new Set()
      this.attractions.forEach(a => {
        if (a.category) categorySet.add(a.category)
      })
      const dynamic = [...categorySet].map(c => ({ id: c, label: c }))
      return [{ id: 'all', label: '全部' }, ...dynamic]
    },
    filteredAttractions() {
      if (this.activeFilter === 'all') return this.attractions
      return this.attractions.filter(a => a.category === this.activeFilter)
    },
    sortedAttractions() {
      const list = [...this.filteredAttractions]
      if (this.activeSort === 'rating') return list.sort((a, b) => b.rating - a.rating)
      if (this.activeSort === 'priceAsc') return list.sort((a, b) => a.price - b.price)
      if (this.activeSort === 'priceDesc') return list.sort((a, b) => b.price - a.price)
      return list
    }
  },
  onLoad(options) {
    this.loadAttractions()
    // 如果带有 action=ticket 参数，提示用户选择景点
    if (options.action === 'ticket') {
      uni.showToast({ title: '请选择景点', icon: 'none' })
    }
  },
  methods: {
    async loadAttractions() {
      uni.showLoading({ title: '加载中...' })
      this.loading = true
      try {
        const res = await attractionApi.getList(1, 100)
        const dataList = res.records || res.list || res || []
        this.attractions = dataList.map(item => {
          // 解析 JSON 字符串字段
          try {
            if (typeof item.tags === 'string') {
              item.tags = JSON.parse(item.tags)
            }
          } catch (e) {
            console.error('解析数据失败:', e)
            // 提供默认值
            if (!item.tags) item.tags = []
          }
          return item
        })
      } catch (error) {
        console.error('加载景点失败:', error)
        // 使用静态数据兜底
        this.attractions = staticData.attractions || []
      } finally {
        this.loading = false
        uni.hideLoading()
      }
    },
    goSearch() { uni.navigateTo({ url: '/pages/search/search' }) },
    goDetail(id) { uni.navigateTo({ url: '/pages/attraction/detail?id=' + id }) },
    goMap(item) {
      uni.navigateTo({
        url: '/pages/map/map?latitude=' + item.latitude + '&longitude=' + item.longitude + '&name=' + encodeURIComponent(item.name)
      })
    },
    goTicket(id) { uni.navigateTo({ url: '/pages/attraction/detail?id=' + id + '&action=ticket' }) }
  }
}
</script>

<style scoped lang="scss">
.list-page { background: #F8F9FA; min-height: 100vh; }
.card-shadow { box-shadow: 0 4rpx 20rpx rgba(29,59,90,0.08); }

.search-bar { margin: 16rpx 24rpx; background: #FFFFFF; border-radius: 50rpx; padding: 20rpx 30rpx; display: flex; align-items: center; gap: 12rpx; box-shadow: 0 4rpx 20rpx rgba(29,59,90,0.08); border: 1rpx solid #E8E8E8; }
.search-icon { width: 28rpx; height: 28rpx; }
.search-placeholder { font-size: 26rpx; color: #bbb; }

.filter-bar { display: flex; background: #FFFFFF; padding: 0 16rpx; overflow-x: auto; border-bottom: 1rpx solid #E8E8E8; }
.filter-item { white-space: nowrap; padding: 20rpx 20rpx; font-size: 26rpx; color: #666; position: relative; flex-shrink: 0; }
.filter-item.active { color: #1D3B5A; font-weight: bold; }
.filter-item.active::after { content: ''; position: absolute; bottom: 0; left: 50%; transform: translateX(-50%); width: 30rpx; height: 4rpx; background: #1D3B5A; border-radius: 2rpx; }

.sort-bar { display: flex; background: #FFFFFF; padding: 12rpx 24rpx; gap: 20rpx; border-bottom: 1rpx solid #E8E8E8; }
.sort-item { font-size: 24rpx; color: #999; padding: 6rpx 16rpx; border-radius: 20rpx; }
.sort-item.active { color: #2E5F8E; background: rgba(29,59,90,0.08); font-weight: bold; }

.attraction-list { padding: 20rpx 24rpx; display: flex; flex-direction: column; gap: 20rpx; }
.attraction-card { background: #FFFFFF; border-radius: 20rpx; overflow: hidden; display: flex; border: 1rpx solid #E8E8E8; }
.attraction-img-wrap { position: relative; width: 220rpx; flex-shrink: 0; height: 220rpx; }
.attraction-img { width: 100%; height: 100%; }
.attraction-price-badge { position: absolute; bottom: 10rpx; left: 10rpx; background: linear-gradient(135deg, #1D3B5A, #2E5F8E); color: #FFFFFF; font-size: 20rpx; padding: 4rpx 14rpx; border-radius: 20rpx; }
.attraction-info { flex: 1; padding: 20rpx; display: flex; flex-direction: column; gap: 8rpx; }
.attraction-header { display: flex; justify-content: space-between; align-items: center; }
.attraction-name { font-size: 28rpx; font-weight: bold; color: #1D3B5A; flex: 1; }
.attraction-rating { font-size: 22rpx; color: #2E5F8E; font-weight: bold; }
.attraction-tags { display: flex; flex-wrap: wrap; gap: 8rpx; }
.a-tag { font-size: 20rpx; color: #1D3B5A; background: rgba(29,59,90,0.08); padding: 4rpx 12rpx; border-radius: 20rpx; }
.attraction-desc { font-size: 22rpx; color: #999; line-height: 1.5; flex: 1; }
.attraction-footer { display: flex; justify-content: space-between; align-items: center; }
.attraction-addr { font-size: 20rpx; color: #bbb; flex: 1; }
.attraction-btns { display: flex; gap: 10rpx; }
.btn-map { font-size: 20rpx; color: #1D3B5A; border: 1rpx solid #1D3B5A; padding: 6rpx 16rpx; border-radius: 20rpx; }
.btn-ticket { font-size: 20rpx; color: #FFFFFF; background: linear-gradient(135deg, #1D3B5A, #2E5F8E); padding: 6rpx 16rpx; border-radius: 20rpx; }
</style>
