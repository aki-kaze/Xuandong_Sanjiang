<template>
  <view class="search-page">
    <!-- 搜索栏 -->
    <view class="search-bar">
      <image class="icon-img" src="/static/icons/search.png" mode="aspectFit" />
      <input
        class="search-input"
        v-model="keyword"
        placeholder="搜索三江景点、美食、文化.."
        focus
        @input="onInput"
        @confirm="doSearch"
        confirm-type="search"
      />
      <text class="search-clear" v-if="keyword" @tap="clearKeyword">清除</text>
    </view>

    <!-- 热门搜索 & 历史 -->
    <view class="suggest-section" v-if="!keyword">
      <view class="suggest-block">
        <text class="suggest-title"><image class="icon-img" src="/static/icons/hot.png" mode="aspectFit" /> 热门搜索</text>
        <view class="suggest-tags">
          <view class="suggest-tag" v-for="item in hotKeywords" :key="item" @tap="selectKeyword(item)">{{ item }}</view>
        </view>
      </view>
      <view class="suggest-block" v-if="historyKeywords.length > 0">
        <view class="suggest-title-row">
          <text class="suggest-title"><image class="icon-img" src="/static/icons/time.png" mode="aspectFit" /> 历史搜索</text>
          <text class="suggest-clear" @tap="clearHistory">清除</text>
        </view>
        <view class="suggest-tags">
          <view class="suggest-tag history" v-for="item in historyKeywords" :key="item" @tap="selectKeyword(item)">{{ item }}</view>
        </view>
      </view>
    </view>

    <!-- 搜索结果 -->
    <view class="result-section" v-else>
      <!-- 分类Tab -->
      <view class="result-tabs">
        <view
          class="result-tab"
          v-for="tab in tabs"
          :key="tab.key"
          :class="{ active: activeTab === tab.key }"
          @tap="activeTab = tab.key"
        >{{ tab.label }}({{ getCount(tab.key) }})</view>
      </view>

      <!-- 景点结果 -->
      <view class="result-list" v-if="activeTab === 'attraction'">
        <view class="result-empty" v-if="attractionResults.length === 0">
          <text class="empty-icon"><image class="icon-img-lg" src="/static/icons/search.png" mode="aspectFit" /></text>
          <text class="empty-text">未找到相关景点</text>
        </view>
        <view class="result-item" v-for="item in attractionResults" :key="item.id" @tap="goAttractionDetail(item.id)">
          <view class="result-thumb">
            <image class="result-thumb-img" :src="fixUrl(item.cover)" mode="aspectFill" />
          </view>
          <view class="result-info">
            <text class="result-name">{{ item.name }}</text>
            <view class="result-tags">
              <text class="r-tag" v-for="tag in (item.tags || []).slice(0,2)" :key="tag">{{ tag }}</text>
            </view>
            <view class="result-bottom">
              <text class="result-addr"><image class="icon-img-sm" src="/static/icons/position.png" mode="aspectFit" /> 三江</text>
              <text class="result-price">{{ item.price === 0 ? '免费' : '¥' + item.price }}</text>
            </view>
          </view>
        </view>
      </view>

      <!-- 美食结果 -->
      <view class="result-list" v-if="activeTab === 'food'">
        <view class="result-empty" v-if="foodResults.length === 0">
          <image class="empty-icon-img" src="/static/icons/food.png" mode="aspectFit" />
          <text class="empty-text">未找到相关美食</text>
        </view>
        <view class="result-item" v-for="item in foodResults" :key="item.id" @tap="goFood(item)">
          <view class="result-thumb">
            <image class="result-thumb-img" :src="fixUrl(item.cover)" mode="aspectFill" />
          </view>
          <view class="result-info">
            <text class="result-name">{{ item.name }}</text>
            <text class="result-desc">{{ (item.description || '').slice(0,40) }}...</text>
            <view class="result-bottom">
              <text class="result-addr">{{ item.category }}</text>
              <text class="result-price">{{ item.price }}</text>
            </view>
          </view>
        </view>
      </view>

      <!-- 文化结果 -->
      <view class="result-list" v-if="activeTab === 'culture'">
        <view class="result-empty" v-if="cultureResults.length === 0">
          <text class="empty-icon">🎵</text>
          <text class="empty-text">未找到相关文化内容</text>
        </view>
        <view class="result-item" v-for="item in cultureResults" :key="item.id" @tap="goHeritage(item)">
          <view class="result-thumb">
            <image class="result-thumb-img" :src="fixUrl(item.cover || item.icon)" mode="aspectFill" />
          </view>
          <view class="result-info">
            <text class="result-name">{{ item.name }}</text>
            <text class="result-desc">{{ (item.description || '').slice(0,40) }}...</text>
            <view class="result-bottom">
              <text class="result-addr">{{ item.level }}</text>
            </view>
          </view>
        </view>
      </view>
    </view>
  </view>
</template>

<script>
import { staticData } from '../../utils/static-data.js'
import { fixImageUrl } from '../../utils/api.js'

export default {
  data() {
    return {
      keyword: '',
      activeTab: 'attraction',
      tabs: [
        { key: 'attraction', label: '景点' },
        { key: 'food', label: '美食' },
        { key: 'culture', label: '文化' }
      ],
      hotKeywords: ['程阳八寨', '风雨桥', '侗族大歌', '鼓楼', '侗锦', '布央茶园', '丹洲古镇', '腌鱼'],
      historyKeywords: uni.getStorageSync('searchHistory') || [],
      searchResults: {
        attraction: staticData.attractions,
        food: staticData.foods,
        culture: staticData.heritage
      },
    }
  },
  computed: {
    attractionResults() {
      if (!this.keyword) return []
      const kw = this.keyword
      return this.searchResults.attraction.filter(a => a.name.includes(kw) || (a.tags && a.tags.some(t => t.includes(kw))) || (a.description && a.description.includes(kw)))
    },
    foodResults() {
      if (!this.keyword) return []
      const kw = this.keyword
      return this.searchResults.food.filter(f => f.name.includes(kw) || (f.tags && f.tags.some(t => t.includes(kw))) || (f.description && f.description.includes(kw)))
    },
    cultureResults() {
      if (!this.keyword) return []
      const kw = this.keyword
      return this.searchResults.culture.filter(h => h.name.includes(kw) || (h.description && h.description.includes(kw)))
    }
  },
  methods: {
    fixUrl(url) {
      return fixImageUrl(url)
    },
    getCount(key) {
      return { attraction: this.attractionResults.length, food: this.foodResults.length, culture: this.cultureResults.length }[key] || 0
    },
    onInput() {},
    doSearch() {
      if (!this.keyword.trim()) return
      this.saveHistory(this.keyword.trim())
    },
    selectKeyword(kw) { this.keyword = kw; this.saveHistory(kw) },
    saveHistory(kw) {
      const list = uni.getStorageSync('searchHistory') || []
      const filtered = list.filter(i => i !== kw)
      filtered.unshift(kw)
      const newList = filtered.slice(0, 8)
      uni.setStorageSync('searchHistory', newList)
      this.historyKeywords = newList
    },
    clearKeyword() { this.keyword = '' },
    clearHistory() { uni.setStorageSync('searchHistory', []); this.historyKeywords = [] },
    goAttractionDetail(id) { uni.navigateTo({ url: '/pages/attraction/detail?id=' + id }) },
    goFood(item) { uni.navigateTo({ url: '/pages/food/detail?id=' + item.id }) },
    goHeritage(item) { uni.switchTab({ url: '/pages/heritage/heritage' }) }
  },
  onLoad() {
  }
}
</script>

<style scoped lang="scss">
/* ===== 统一图标尺寸 ===== */
.icon-img { width: 32rpx; height: 32rpx; flex-shrink: 0; }
.icon-img-lg { width: 80rpx; height: 80rpx; }
.icon-img-sm { width: 24rpx; height: 24rpx; flex-shrink: 0; }

.search-page { background: #F8F9FA; min-height: 100vh; }

.search-bar { display: flex; align-items: center; gap: 12rpx; background: #FDFCF8; padding: 16rpx 24rpx; border-bottom: 1rpx solid #F0F0F0; }
.search-input { flex: 1; font-size: 28rpx; color: #333; }
.search-clear { font-size: 28rpx; color: #bbb; padding: 8rpx; }

.suggest-section { padding: 24rpx; }
.suggest-block { margin-bottom: 30rpx; }
.suggest-title { display: flex; align-items: center; font-size: 28rpx; font-weight: bold; color: #1D3B5A; margin-bottom: 16rpx; }
.suggest-title-row { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16rpx; }
.suggest-clear { font-size: 24rpx; color: #999; }
.suggest-tags { display: flex; flex-wrap: wrap; gap: 16rpx; }
.suggest-tag { background: #FDFCF8; color: #333; font-size: 26rpx; padding: 12rpx 24rpx; border-radius: 30rpx; box-shadow: 0 2rpx 10rpx rgba(163,59,50,0.06); }
.suggest-tag.history { background: #F8F9FA; color: #666; }

.result-tabs { display: flex; background: #FDFCF8; border-bottom: 1rpx solid #F0F0F0; }
.result-tab { flex: 1; text-align: center; padding: 24rpx 0; font-size: 26rpx; color: #999; position: relative; }
.result-tab.active { color: #1D3B5A; font-weight: bold; }
.result-tab.active::after { content: ''; position: absolute; bottom: 0; left: 50%; transform: translateX(-50%); width: 40rpx; height: 4rpx; background: #D4A373; border-radius: 2rpx; }

.result-list { padding: 16rpx 24rpx; display: flex; flex-direction: column; gap: 16rpx; }
.result-empty { display: flex; flex-direction: column; align-items: center; padding: 80rpx 0; }
.empty-icon { font-size: 0; margin-bottom: 16rpx; }
.empty-icon-img { width: 60rpx; height: 60rpx; margin-bottom: 16rpx; }
.empty-text { font-size: 28rpx; color: #999; }
.result-item { background: #FDFCF8; border-radius: 12rpx; overflow: hidden; display: flex; box-shadow: 0 2rpx 10rpx rgba(163,59,50,0.06); }
.result-thumb { width: 180rpx; height: 140rpx; flex-shrink: 0; overflow: hidden; background: #F0F0F0; }
.result-thumb-img { width: 100%; height: 100%; }
.result-info { flex: 1; padding: 16rpx 20rpx; display: flex; flex-direction: column; gap: 6rpx; }
.result-name { font-size: 28rpx; font-weight: bold; color: #1D3B5A; }
.result-tags { display: flex; gap: 8rpx; }
.r-tag { font-size: 20rpx; color: #D4A373; background: #FDF6E3; padding: 4rpx 12rpx; border-radius: 20rpx; }
.result-desc { font-size: 22rpx; color: #999; line-height: 1.5; }
.result-bottom { display: flex; justify-content: space-between; align-items: center; }
.result-addr { font-size: 22rpx; color: #999; display: flex; align-items: center; gap: 4rpx; }
.result-price { font-size: 24rpx; color: #E8734A; font-weight: bold; }
</style>
