<template>
  <!-- 整个首页的根容器 -->
  <view class="index-page">

    <!-- ====================== 1. 顶部轮播 Banner ====================== -->
    <view class="hero-banner">
      <!-- 轮播组件：自动播放、循环、指示器 -->
      <swiper class="banner-swiper" autoplay circular interval="4000" indicator-dots
        indicator-color="rgba(255,255,255,0.4)" indicator-active-color="#A33B32">
        <!-- v-for 循环渲染轮播图数据 -->
        <swiper-item v-for="(item, idx) in banners" :key="idx">
          <view class="banner-item">
            <!-- 轮播背景图 -->
            <image class="banner-bg" :src="item.img" mode="aspectFill" />
            <!-- 轮播文字内容 -->
            <view class="banner-content">
              <text class="banner-eyebrow">{{ item.eyebrow }}</text>
              <text class="banner-title">{{ item.title }}</text>
              <text class="banner-sub">{{ item.sub }}</text>
            </view>
          </view>
        </swiper-item>
      </swiper>

      <!-- 搜索栏：悬浮在轮播图底部 -->
      <view class="search-bar" @tap="goSearch">
        <text class="search-icon">
          <image class="icon-img" src="/static/icons/search.png" mode="aspectFit" />
        </text>
        <text class="search-placeholder">搜索景点、美食、非遗活动...</text>
      </view>
    </view>

    <!-- ====================== 2. 快捷功能入口（8个图标） ====================== -->
    <view class="quick-nav card-shadow">
      <!-- 循环渲染 8 个功能按钮 -->
      <view class="quick-item" v-for="item in quickNavs" :key="item.id" @tap="goPage(item.path)">
        <view class="quick-icon" :style="{ background: item.bg }">
          <image v-if="item.iconImg" :src="item.iconImg" class="quick-icon-img" mode="aspectFit" />
          <text v-else>{{ item.icon }}</text>
        </view>
        <text class="quick-label">{{ item.label }}</text>
      </view>
    </view>

    <!-- ====================== 3. 非遗文化展示模块 ====================== -->
    <view class="section heritage-section">
      <view class="section-header">
        <view class="section-title-wrap">
          <view class="section-accent-bar"></view>
          <text class="section-title-text">绝美非遗</text>
        </view>
        <view class="section-header-right">
          <view class="heritage-map-link" @tap="goHeritageMap">
            <image class="heritage-map-icon" src="/static/icons/景区地图.png" mode="aspectFit" />
            <text>非遗地图</text>
          </view>
          <text class="section-more" @tap="goHeritage">更多></text>
        </view>
      </view>

      <!-- 非遗大卡片轮播 -->
      <swiper class="heritage-hero-swiper" autoplay circular interval="5000" indicator-dots>
        <swiper-item v-for="item in heritageItems.slice(0, 3)" :key="item.id" @tap="goHeritageDetail(item)">
          <view class="heritage-hero-card">
            <image class="heritage-hero-img" :src="item.cover" mode="aspectFill" />
            <view class="heritage-hero-level" :style="{ background: item.color }">
              <text>{{ item.level }}</text>
            </view>
            <view class="heritage-hero-info">
              <text class="heritage-hero-name">{{ item.name }}</text>
              <text class="heritage-hero-desc">{{ item.description ? item.description.slice(0, 60) + '...' : '' }}</text>
            </view>
          </view>
        </swiper-item>
      </swiper>

      <!-- 非遗小卡片横向滚动 -->
      <scroll-view scroll-x class="heritage-scroll">
        <view class="heritage-list">
          <view class="heritage-card" v-for="item in heritageItems" :key="item.id" @tap="goHeritageDetail(item)">
            <view class="heritage-img-wrap">
              <image class="heritage-img" :src="item.cover" mode="aspectFill" />
              <text class="heritage-level-badge">{{ item.level }}</text>
            </view>
            <view class="heritage-info">
              <text class="heritage-name">{{ item.name }}</text>
            </view>
          </view>
        </view>
      </scroll-view>
    </view>

    <!-- ====================== 4. 热门景点列表 ====================== -->
    <view class="section">
      <view class="section-header">
        <view class="section-title-wrap">
          <view class="section-accent-bar"></view>
          <text class="section-title-text">热门景点</text>
        </view>
        <text class="section-more" @tap="goAttractionList">更多></text>
      </view>
      <view class="attraction-list">
        <!-- 只显示前3个景点 -->
        <view class="attraction-card card-shadow" v-for="item in attractions.slice(0,3)" :key="item.id" @tap="goAttractionDetail(item)">
          <view class="attraction-img-wrap">
            <image class="attraction-img" :src="item.cover" mode="aspectFill" />
            <view class="attraction-price-tag">
              <text>{{ item.price === 0 ? '免费' : '¥' + item.price }}</text>
            </view>
          </view>
          <view class="attraction-info">
            <text class="attraction-name">{{ item.name }}</text>
            <view class="attraction-meta">
              <text class="attraction-rating">⭐{{ item.rating }}</text>
              <text class="tag">{{ item.category }}</text>
            </view>
            <text class="attraction-addr">
              <image class="addr-icon" src="/static/icons/position.png" mode="aspectFit" /> {{ item.address }}
            </text>
            <view class="attraction-tags-row">
              <text class="tag tag-red" v-for="tag in (item.tags || []).slice(0,2)" :key="tag">{{ tag }}</text>
            </view>
          </view>
        </view>
      </view>
    </view>

    <!-- ====================== 5. 精选旅游路线 ====================== -->
    <view class="section">
      <view class="section-header">
        <view class="section-title-wrap">
          <view class="section-accent-bar"></view>
          <text class="section-title-text">精选路线</text>
        </view>
        <text class="section-more" @tap="goRoute">更多></text>
      </view>
      <scroll-view scroll-x class="route-scroll">
        <view class="route-list">
          <view class="route-card card-shadow" v-for="item in routes" :key="item.id" @tap="goRouteDetail(item)">
            <view class="route-img-wrap">
              <image class="route-img" :src="item.cover" mode="aspectFill" />
              <text class="route-duration">{{ item.duration }}</text>
              <text class="route-difficulty-badge">{{ item.difficulty }}</text>
            </view>
            <view class="route-body">
              <text class="route-name">{{ item.name }}</text>
              <view class="route-tags">
                <text class="tag tag-wood" v-for="tag in (item.tags || [])" :key="tag">{{ tag }}</text>
              </view>
              <view class="route-footer">
                <text class="route-price">¥{{ item.price }}/人起</text>
                <text class="route-btn" @tap.stop="goRouteTicket(item)">立即预订</text>
              </view>
            </view>
          </view>
        </view>
      </scroll-view>
    </view>

    <!-- ====================== 6. 侗乡美食 ====================== -->
    <view class="section">
      <view class="section-header">
        <view class="section-title-wrap">
          <view class="section-accent-bar"></view>
          <text class="section-title-text">侗乡美食</text>
        </view>
        <text class="section-more" @tap="goFood">更多></text>
      </view>
      <scroll-view scroll-x class="food-scroll">
        <view class="food-list">
          <view class="food-card card-shadow" v-for="item in foods.slice(0,5)" :key="item.id" @tap="goFoodDetail(item)">
            <view class="food-img-wrap">
              <image class="food-img" :src="item.cover" mode="aspectFill" />
              <view class="food-rating-badge">⭐{{ item.rating }}</view>
            </view>
            <view class="food-body">
              <text class="food-name">{{ item.name }}</text>
              <view class="food-meta">
                <text class="food-price">{{ item.price }}</text>
                <text class="food-detail-link">查看详情 ›</text>
              </view>
            </view>
          </view>
        </view>
      </scroll-view>
    </view>

    <!-- ====================== 7. 社区动态（寨友圈） ====================== -->
    <view class="section">
      <view class="section-header">
        <view class="section-title-wrap">
          <view class="section-accent-bar"></view>
          <text class="section-title-text">寨友圈</text>
        </view>
        <text class="section-more" @tap="goCommunity">更多></text>
      </view>
      <view class="post-list">
        <view class="post-card card-shadow" v-for="item in (communityPosts || []).slice(0,2)" :key="item.id" @tap="goPostDetail(item)">
          <view class="post-header">
            <image class="post-avatar-img" :src="item.avatar" mode="aspectFill" />
            <view class="post-meta">
              <text class="post-author">{{ item.author }}</text>
              <text class="post-time">{{ item.time }}</text>
            </view>
          </view>
          <text class="post-content">{{ item.content }}</text>
          <image class="post-cover-img" :src="item.coverImg" mode="aspectFill" v-if="item.coverImg" />
          <view class="post-tags">
            <text class="tag tag-wood" v-for="tag in (item.tags || [])" :key="tag"># {{ tag }}</text>
          </view>
          <view class="post-actions">
            <!-- 点赞功能 -->
            <view class="post-action" @tap.stop="likePost(item)">
              <image class="action-icon" :src="item.liked ? '/static/icons/love.png' : '/static/icons/点赞.png'" mode="aspectFit" />
              <text :class="{ 'action-liked': item.liked }">{{ item.likes }}</text>
            </view>
            <!-- 评论跳转 -->
            <view class="post-action" @tap.stop="goPostDetail(item)">
              <image class="action-icon" src="/static/icons/评论.png" mode="aspectFit" />
              <text>{{ item.comments }}</text>
            </view>
          </view>
        </view>
      </view>
    </view>

    <!-- ====================== 8. 底部AI故事入口 ====================== -->
    <view class="story-banner section">
      <view class="story-content">
        <text class="story-label">活态故事</text>
        <text class="story-text">"每当月圆之夜，寨子里的长者会在鼓楼下，为孩子们讲述千年侗族的传奇故事..."</text>
        <view class="story-btn" @tap="goAI">探索更多> </view>
      </view>
    </view>

    <!-- 底部留白 -->
    <view style="height: 40rpx;"></view>
  </view>
</template>

<script>
// 导入接口请求工具
import { postApi, attractionApi, heritageApi, foodApi, routeApi } from '../../utils/api.js'
// 导入随机头像工具
import { getRandomAvatar } from '../../utils/auth.js'
// 导入静态兜底数据（接口失败时使用）
import { staticData } from '../../utils/static-data.js'
// 导入性能优化工具（如果存在）
let getCache, setCache, preloadData
try {
  const perf = require('../../utils/performance.js')
  getCache = perf.getCache
  setCache = perf.setCache
  preloadData = perf.preloadData
} catch (e) {
  console.log('性能优化工具未找到，使用基础模式')
  getCache = () => null
  setCache = () => {}
  preloadData = (k, fn) => fn()
}

export default {
  // 页面数据定义
  data() {
    return {
      // 轮播图数据
      banners: [
        { title: '寻侗三江', eyebrow: '千年侗寨 · 非遗共生', sub: '探索侗族千年文化与自然之美', img: 'https://cdn.phototourl.com/free/2026-03-23-023da30f-8b5e-4893-9987-add47194d84d.jpg' },
        { title: '程阳八寨', eyebrow: '世界最美侗族古村落', sub: '风雨桥 · 鼓楼 · 侗族大歌', img: 'https://cdn.phototourl.com/uploads/2026-03-19-67bbd771-fa02-4207-9437-87afc2c36bc7.jpg' },
        { title: '侗族大歌', eyebrow: '人类非物质文化遗产名录', sub: '天籁之音 世代相传', img: 'https://cdn.phototourl.com/free/2026-03-23-75376aa0-ba75-4269-8839-bbe6aca040a6.jpg' },
        { title: '侗乡美食', eyebrow: '品味千年侗族风味', sub: '腌鱼 · 油茶 · 五色糯米饭', img: 'https://cdn.phototourl.com/free/2026-03-23-656b106c-9ce1-4ef1-93b3-e205cd8267f4.png' }
      ],
      // 8 个快捷功能入口数据
      quickNavs: [
        { id: 1, iconImg: '/static/icons/map.png', label: '地图导航', path: '/pages/map/map', bg: 'rgba(74,144,226,0.12)' },
        { id: 2, iconImg: '/static/icons/ticket.png', label: '购票预订', path: '/pages/ticket/ticket', bg: 'rgba(74,144,226,0.12)' },
        { id: 3, iconImg: '/static/icons/ai_index.png', label: 'AI 助手', path: '/pages/ai/ai', bg: 'rgba(74,144,226,0.12)' },
        { id: 4, iconImg: '/static/icons/hotel.png', label: '住宿', path: '/pages/hotel/hotel', bg: 'rgba(74,144,226,0.12)' },
        { id: 5, iconImg: '/static/icons/food.png', label: '美食', path: '/pages/food/food', bg: 'rgba(74,144,226,0.12)' },
        { id: 6, iconImg: '/static/icons/shop.png', label: '特产', path: '/pages/shop/shop', bg: 'rgba(74,144,226,0.12)' },
        { id: 7, iconImg: '/static/icons/discover_index.png', label: '社区', path: '/pages/discover/discover', bg: 'rgba(74,144,226,0.12)' },
        { id: 8, iconImg: '/static/icons/route.png', label: '路线', path: '/pages/route/route', bg: 'rgba(74,144,226,0.12)' }
      ],
      // 页面渲染的列表数据
      heritageItems: [],
      attractions: [],
      foods: [],
      routes: [],
      communityPosts: [],
      // 加载状态
      loading: true
    }
  },

  // 页面加载时执行：使用缓存和预加载优化
  onLoad() {
    // 优先使用缓存数据，实现秒开
    this.loadFromCache()
    // 同时从网络加载最新数据
    this.loadAllData()
  },

  // 页面显示时执行（下拉刷新后会触发）
  onShow() {
    // 检查缓存是否过期，如果过期则刷新
    this.checkAndRefresh()
  },

  // 方法集合
  methods: {
    // ============== 缓存相关方法 ==============
    // 从缓存加载数据（实现秒开）
    loadFromCache() {
      try {
        const heritageCache = getCache('heritage')
        const attractionsCache = getCache('attractions')
        const foodsCache = getCache('foods')
        const routesCache = getCache('routes')
        const postsCache = getCache('posts')
        
        if (heritageCache && heritageCache.length > 0) {
          this.heritageItems = heritageCache
          console.log('使用非遗缓存数据')
        }
        if (attractionsCache && attractionsCache.length > 0) {
          this.attractions = attractionsCache
          console.log('使用景点缓存数据')
        }
        if (foodsCache && foodsCache.length > 0) {
          this.foods = foodsCache
          console.log('使用美食缓存数据')
        }
        if (routesCache && routesCache.length > 0) {
          this.routes = routesCache
          console.log('使用路线缓存数据')
        }
        if (postsCache && postsCache.length > 0) {
          this.communityPosts = postsCache
          console.log('使用社区缓存数据')
        }
        
        // 如果所有数据都有缓存，标记为已加载
        if (this.heritageItems.length > 0 || this.attractions.length > 0) {
          this.loading = false
        }
      } catch (e) {
        console.log('加载缓存失败:', e.message)
      }
    },
    
    // 检查并刷新数据
    checkAndRefresh() {
      try {
        const lastRefresh = uni.getStorageSync('lastRefresh')
        const now = Date.now()
        const cacheTime = 5 * 60 * 1000 // 5 分钟缓存
        
        if (!lastRefresh || now - lastRefresh > cacheTime) {
          console.log('缓存已过期，刷新数据')
          this.loadAllData()
          uni.setStorageSync('lastRefresh', now)
        }
      } catch (e) {
        console.log('检查缓存失败:', e.message)
      }
    },
    
    // 并行加载所有数据（优化为并发请求）
    async loadAllData() {
      try {
        // 使用 Promise.all 并行加载，提高加载速度
        await Promise.all([
          this.loadHeritage(),
          this.loadAttractions(),
          this.loadRoutes(),
          this.loadFoods(),
          this.loadCommunityPosts()
        ])
        this.loading = false
        console.log('所有数据加载完成')
      } catch (error) {
        console.error('数据加载失败:', error)
        this.loading = false
      }
    },
    
    // ============== 1. 加载非遗数据 ==============
    async loadHeritage() {
      try {
        const res = await heritageApi.getList(1, 10)
        const list = res.records || res.list || res || []
        if (Array.isArray(list) && list.length > 0) {
          this.heritageItems = list.map(item => {
            try { if (typeof item.tags === 'string') item.tags = JSON.parse(item.tags) } catch (e) { item.tags = [] }
            return item
          })
          // 保存到缓存
          setCache('heritage', this.heritageItems)
        } else {
          this.heritageItems = staticData.heritage || []
        }
      } catch (error) {
        console.error('加载非遗失败:', error)
        this.heritageItems = staticData.heritage || []
      }
    },

    // ============== 2. 加载景点数据 ==============
    async loadAttractions() {
      try {
        const res = await attractionApi.getList(1, 10)
        if (!res || !Array.isArray(res) || res.length === 0) {
          this.attractions = staticData.attractions || []
          return
        }
        this.attractions = res.map(item => {
          try { if (typeof item.tags === 'string') item.tags = JSON.parse(item.tags) } catch (e) { item.tags = [] }
          return item
        })
        // 保存到缓存
        setCache('attractions', this.attractions)
      } catch (error) {
        console.error('加载景点失败:', error)
        this.attractions = staticData.attractions || []
      }
    },

    // ============== 3. 加载路线数据 ==============
    async loadRoutes() {
      try {
        const res = await routeApi.getList(1, 10)
        const list = res.records || res.list || res || []
        if (Array.isArray(list) && list.length > 0) {
          this.routes = list.map(item => {
            try {
              if (typeof item.stops === 'string') item.stops = JSON.parse(item.stops)
              if (typeof item.tags === 'string') item.tags = JSON.parse(item.tags)
            } catch (e) { item.tags = [] }
            return item
          })
          // 保存到缓存
          setCache('routes', this.routes)
        } else {
          this.routes = staticData.routes || []
        }
      } catch (error) {
        console.error('加载路线失败:', error)
        this.routes = staticData.routes || []
      }
    },

    // ============== 4. 加载美食数据 ==============
    async loadFoods() {
      try {
        const res = await foodApi.getList(1, 10)
        const list = res.records || res.list || res || []
        if (Array.isArray(list) && list.length > 0) {
          this.foods = list.map(item => {
            try { if (typeof item.tags === 'string') item.tags = JSON.parse(item.tags) } catch (e) { item.tags = [] }
            return item
          })
          // 保存到缓存
          setCache('foods', this.foods)
        } else {
          this.foods = staticData.foods || []
        }
      } catch (error) {
        console.error('加载美食失败:', error)
        this.foods = staticData.foods || []
      }
    },

    // ============== 5. 加载社区帖子 ==============
    async loadCommunityPosts() {
      try {
        const res = await postApi.getList(1, 2)
        const list = res.records || res.list || []
        if (Array.isArray(list) && list.length > 0) {
          this.communityPosts = list.map(item => ({
            id: item.id,
            author: item.nickname || item.author || '侗乡旅行者',
            avatar: item.avatar || getRandomAvatar(),
            time: this.formatTime(item.createTime || item.create_time || item.createdAt),
            content: item.content,
            coverImg: item.coverImg || item.cover_img || '',
            likes: item.likes || 0,
            comments: item.comments || 0,
            tags: this.parseTags(item.tags),
            liked: false,
            followed: false
          }))
          // 保存到缓存
          setCache('posts', this.communityPosts)
        } else {
          this.loadStaticPosts()
        }
      } catch (error) {
        console.error('加载社区帖子失败:', error)
        this.loadStaticPosts()
      }
    },

    // 静态社区帖子（接口失败兜底）
    loadStaticPosts() {
      this.communityPosts = [
        { id: 1, author: '侗乡旅行者', avatar: getRandomAvatar(), time: '2 小时前', content: '今天终于来到了心心念念的程阳八寨！风雨桥真的太震撼了，侗族人民的智慧让人敬佩。晚上还听了侗族大歌，天籁之音啊！强烈推荐大家来体验！', coverImg: 'https://cdn.phototourl.com/member/2026-04-16-8c087100-b848-49db-97ae-c5d0650744c4.png', likes: 328, comments: 45, tags: ['程阳八寨', '风雨桥', '侗族大歌'], liked: false, followed: false },
        { id: 2, author: '美食探索者', avatar: getRandomAvatar(), time: '5 小时前', content: '三江的美食真的太对我胃口了！酸鱼酸肉酸爽开胃，油茶香甜可口，还有五色糯米饭，好看又好吃！来了三天胖了三斤，但是值了！', coverImg: 'https://cdn.phototourl.com/member/2026-04-16-61885197-72c5-41b1-a26c-7eeb40c75b43.jpg', likes: 256, comments: 32, tags: ['美食', '酸鱼', '油茶'], liked: false, followed: false },
      ]
    },

    // 时间格式化
    formatTime(time) {
      if (!time) return ''
      const date = new Date(time)
      const now = new Date()
      const diff = now - date
      if (diff < 60000) return '刚刚'
      if (diff < 3600000) return Math.floor(diff / 60000) + ' 分钟前'
      if (diff < 86400000) return Math.floor(diff / 3600000) + ' 小时前'
      if (diff < 604800000) return Math.floor(diff / 86400000) + ' 天前'
      return date.toLocaleDateString()
    },

    // 标签解析
    parseTags(tags) {
      if (!tags) return []
      if (Array.isArray(tags)) return tags
      try { return JSON.parse(tags) } catch { return tags.split(',').map(t => t.trim()).filter(Boolean) }
    },

    // ============== 页面跳转方法 ==============
    goSearch() { uni.navigateTo({ url: '/pages/search/search' }) },
    goPage(path) {
      const tabBarPages = ['/pages/index/index','/pages/heritage/heritage','/pages/ai/ai','/pages/discover/discover','/pages/mine/mine']
      if (tabBarPages.includes(path)) { uni.switchTab({ url: path }) } 
      else { uni.navigateTo({ url: path }) }
    },
    goAttractionList() { uni.navigateTo({ url: '/pages/attraction/list' }) },
    goAttractionDetail(item) { uni.navigateTo({ url: '/pages/attraction/detail?id=' + item.id }) },
    goHeritage() { uni.switchTab({ url: '/pages/heritage/heritage' }) },
    goHeritageDetail(item) { uni.switchTab({ url: '/pages/heritage/heritage' }) },
    goHeritageMap() { uni.navigateTo({ url: '/pages/heritage/heritage-map' }) },
    goRoute() { uni.navigateTo({ url: '/pages/route/route' }) },
    goRouteDetail(item) { uni.navigateTo({ url: '/pages/route/route-detail?id=' + item.id }) },
    goRouteTicket(item) {
      const routeData = encodeURIComponent(JSON.stringify(item))
      uni.navigateTo({ url: `/pages/ticket/ticket?tab=route&route=${routeData}` })
    },
    goFood() { uni.navigateTo({ url: '/pages/food/food' }) },
    goFoodDetail(item) { uni.navigateTo({ url: '/pages/food/detail?id=' + item.id }) },
    goCommunity() { uni.switchTab({ url: '/pages/discover/discover' }) },
    goPostDetail(item) { uni.navigateTo({ url: `/pages/discover/detail?id=${item.id}` }) },
    goAI() { uni.switchTab({ url: '/pages/ai/ai' }) },

    // 点赞功能
    likePost(item) {
      item.liked = !item.liked
      item.likes += item.liked ? 1 : -1
    }
  }
}
</script>

<style scoped lang="scss">
/* ===== 页面基础样式 ===== */
.index-page {
  background: var(--dong-bg);
  min-height: 100vh;
  padding-bottom: 20rpx;
}

/* ===== 公共卡片阴影 ===== */
.card-shadow {
  background: var(--dong-paper);
  border-radius: var(--dong-radius-lg);
  box-shadow: var(--dong-shadow-md);
  overflow: hidden;
}

/* ===== Section 通用样式 ===== */
.section { 
  margin: 32rpx 24rpx 0; 
}

.section-header { 
  display: flex; 
  align-items: center; 
  justify-content: space-between; 
  margin-bottom: 24rpx; 
}

.section-title-wrap { 
  display: flex; 
  align-items: center; 
  gap: 14rpx; 
}

.section-accent-bar {
  width: 8rpx;
  height: 28rpx;
  background: var(--dong-primary);
  border-radius: 4rpx;
}

.section-title-text {
  font-size: 32rpx;
  font-weight: 700;
  color: var(--dong-secondary);
}

.section-more {
  font-size: 26rpx;
  color: var(--dong-primary);
}

/* ===== 其余样式保持原样，已完整保留 ===== */
/* （样式代码较长，已完整保留，不影响使用） */

/* ===== Tags ===== */
.tag {
  display: inline-block;
  padding: 6rpx 16rpx;
  border-radius: 24rpx;
  font-size: 22rpx;
  background: rgba(74, 144, 226, 0.10);
  color: var(--dong-primary);
  margin-right: 8rpx;
  font-weight: 500;
}

.tag-primary {
  background: rgba(74, 144, 226, 0.10);
  color: var(--dong-primary);
}

.tag-accent {
  background: rgba(255, 127, 80, 0.12);
  color: var(--dong-accent);
}

/* ===== Hero Banner ===== */
.hero-banner { 
  position: relative; 
}

.banner-swiper { 
  height: 480rpx; 
}

.banner-item { 
  position: relative; 
  width: 100%; 
  height: 100%; 
}

.banner-bg { 
  width: 100%; 
  height: 100%; 
}

.banner-content { 
  position: absolute; 
  bottom: 0; 
  left: 0; 
  right: 0; 
  display: flex; 
  flex-direction: column; 
  padding: 40rpx 48rpx; 
}

.banner-eyebrow { 
  font-size: 22rpx; 
  color: #FF7F50; 
  letter-spacing: 0.3em; 
  font-weight: 600; 
  margin-bottom: 10rpx; 
  text-shadow: 0 2rpx 8rpx rgba(0,0,0,0.5);
}

.banner-title { 
  font-size: 56rpx; 
  font-weight: bold; 
  color: #FFFFFF; 
  display: block; 
  margin-bottom: 10rpx; 
  letter-spacing: 0.1em;
  text-shadow: 0 2rpx 12rpx rgba(0,0,0,0.6);
}

.banner-sub { 
  font-size: 26rpx; 
  color: #FFFFFF; 
  display: block; 
  text-shadow: 0 2rpx 8rpx rgba(0,0,0,0.5);
}

.search-bar {
  position: absolute;
  bottom: -28rpx;
  left: 30rpx;
  right: 30rpx;
  background: var(--dong-paper);
  border-radius: 50rpx;
  padding: 22rpx 32rpx;
  display: flex;
  align-items: center;
  box-shadow: var(--dong-shadow-md);
  z-index: 10;
}

.search-icon {
  font-size: 0;
  margin-right: 12rpx;
  display: flex;
  align-items: center;
}

.search-placeholder {
  color: var(--dong-text-muted);
  font-size: 28rpx;
}

/* ===== 快捷导航 ===== */
.quick-nav {
  margin: 50rpx 24rpx 0;
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 24rpx;
  padding: 36rpx 24rpx;
  background: var(--dong-paper);
  border-radius: 24rpx;
  box-shadow: var(--dong-shadow-md);
}

.quick-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12rpx;
}

.quick-icon {
  width: 96rpx;
  height: 96rpx;
  border-radius: 24rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 44rpx;
}

.quick-icon-img { 
  width: 52rpx; 
  height: 52rpx; 
}

.quick-label {
  font-size: 24rpx;
  color: var(--dong-text-secondary);
  font-weight: 500;
}

/* ===== 非遗文化 ===== */
.heritage-section {
  overflow: hidden;
}
.section-header-right {
  display: flex;
  align-items: center;
  gap: 16rpx;
}
.heritage-map-link {
  font-size: 22rpx;
  color: #A33B32;
  background: rgba(163, 59, 50, 0.08);
  padding: 6rpx 16rpx;
  border-radius: 20rpx;
  display: flex;
  align-items: center;
  gap: 6rpx;
}

.heritage-map-icon {
  width: 28rpx;
  height: 28rpx;
}

/* 非遗大卡片轮播 */
.heritage-hero-swiper {
  height: 320rpx;
  margin: 0 24rpx 20rpx;
  border-radius: 20rpx;
  overflow: hidden;
}
.heritage-hero-card {
  position: relative;
  width: 100%;
  height: 100%;
  border-radius: 20rpx;
  overflow: hidden;
}
.heritage-hero-img {
  width: 100%;
  height: 100%;
}
.heritage-hero-level {
  position: absolute;
  top: 20rpx;
  left: 20rpx;
  padding: 6rpx 16rpx;
  border-radius: 16rpx;
}
.heritage-hero-level text {
  font-size: 20rpx;
  color: #fff;
  font-weight: bold;
}
.heritage-hero-info { 
  position: absolute;
  bottom: 20rpx;
  left: 20rpx;
  right: 20rpx;
}
.heritage-hero-name {
  display: block;
  font-size: 36rpx;
  font-weight: bold;
  color: #FFFFFF;
  margin-bottom: 8rpx;
  letter-spacing: 0.06em;
  text-shadow: 0 2rpx 8rpx rgba(0,0,0,0.6);
}
.heritage-hero-desc {
  display: block;
  font-size: 24rpx;
  color: #FFFFFF;
  line-height: 1.5;
  text-shadow: 0 2rpx 6rpx rgba(0,0,0,0.5);
}

.heritage-scroll {
  white-space: nowrap;
}

.heritage-list { 
  display: inline-flex; 
  gap: 20rpx; 
  padding: 4rpx 4rpx 10rpx; 
}

.heritage-card {
  display: inline-flex;
  flex-direction: column;
  width: 160rpx;
  background: var(--dong-paper);
  border-radius: 20rpx;
  overflow: hidden;
  box-shadow: var(--dong-shadow-sm);
}

.heritage-img-wrap { 
  position: relative; 
  width: 160rpx; 
  height: 140rpx; 
}

.heritage-img { 
  width: 100%; 
  height: 100%; 
}

.heritage-level-badge {
  position: absolute;
  bottom: 10rpx;
  left: 10rpx;
  font-size: 18rpx;
  color: #FFFFFF;
  font-weight: bold;
  background: rgba(44, 62, 80, 0.7);
  padding: 3rpx 10rpx;
  border-radius: 20rpx;
  text-shadow: 0 1rpx 4rpx rgba(0,0,0,0.5);
}

.heritage-info { 
  padding: 14rpx 14rpx 16rpx; 
  display: flex; 
  flex-direction: column; 
  align-items: center; 
  gap: 6rpx; 
}

.heritage-name {
  font-size: 24rpx;
  color: var(--dong-blue);
  font-weight: bold;
  text-align: center;
}

/* ===== 热门景点 ===== */
.attraction-list { 
  display: flex; 
  flex-direction: column; 
  gap: 20rpx; 
}

.attraction-card {
  background: var(--dong-paper);
  border-radius: var(--dong-radius-lg);
  overflow: hidden;
  display: flex;
  box-shadow: var(--dong-shadow-sm);
}

.attraction-img-wrap { 
  position: relative; 
  width: 220rpx; 
  flex-shrink: 0; 
}

.attraction-img { 
  width: 220rpx; 
  height: 200rpx; 
}

.attraction-price-tag { 
  position: absolute; 
  bottom: 10rpx; 
  left: 10rpx; 
  background: rgba(74, 144, 226, 0.9); 
  color: #FFFFFF; 
  font-size: 22rpx; 
  padding: 6rpx 16rpx; 
  border-radius: 24rpx;
  font-weight: 600;
  backdrop-filter: blur(4rpx);
}

.attraction-info { 
  flex: 1; 
  padding: 20rpx; 
}

.attraction-name {
  font-size: 30rpx;
  font-weight: bold;
  color: var(--dong-blue);
  display: block;
  margin-bottom: 10rpx;
}

.attraction-meta { 
  display: flex; 
  align-items: center; 
  gap: 12rpx; 
  margin-bottom: 10rpx; 
}

.attraction-rating {
  font-size: 24rpx;
  color: var(--dong-accent);
  font-weight: 600;
}

.attraction-addr {
  font-size: 22rpx;
  color: var(--dong-text-muted);
  display: block;
  margin-bottom: 10rpx;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  display: flex;
  align-items: center;
}

.addr-icon {
  width: 24rpx;
  height: 24rpx;
  margin-right: 4rpx;
}

.attraction-tags-row { 
  display: flex; 
  flex-wrap: wrap; 
  gap: 8rpx; 
}

/* ===== 精选路线 ===== */
.route-scroll { 
  white-space: nowrap; 
}

.route-list { 
  display: inline-flex; 
  gap: 24rpx; 
  padding: 4rpx 4rpx 10rpx; 
}

.route-card {
  display: inline-block;
  width: 320rpx;
  background: var(--dong-paper);
  border-radius: var(--dong-radius-lg);
  overflow: hidden;
  vertical-align: top;
  box-shadow: var(--dong-shadow-sm);
}

.route-img-wrap { 
  position: relative; 
  width: 100%; 
  height: 240rpx; 
}

.route-img { 
  width: 100%; 
  height: 100%; 
}

.route-duration {
  position: absolute;
  bottom: 16rpx;
  left: 20rpx;
  font-size: 28rpx;
  font-weight: bold;
  color: #FFFFFF;
  text-shadow: 0 2rpx 6rpx rgba(0,0,0,0.6);
}

.route-difficulty-badge {
  position: absolute;
  top: 16rpx;
  right: 20rpx;
  font-size: 22rpx;
  color: #FFFFFF;
  background: rgba(255, 127, 80, 0.85);
  padding: 4rpx 14rpx;
  border-radius: 20rpx;
  text-shadow: 0 1rpx 4rpx rgba(0,0,0,0.4);
}

.route-body { 
  padding: 20rpx;
  display: flex;
  flex-direction: column;
  height: 220rpx;
}

.route-name {
  font-size: 28rpx;
  font-weight: bold;
  color: var(--dong-blue);
  display: block;
  margin-bottom: 12rpx;
  white-space: normal;
  flex-shrink: 0;
}

.route-tags { 
  display: flex; 
  flex-wrap: wrap; 
  gap: 8rpx; 
  margin-bottom: 16rpx;
  flex-shrink: 0;
}

.route-footer { 
  display: flex; 
  justify-content: space-between; 
  align-items: center;
  margin-top: auto;
  flex-shrink: 0;
}

.route-price {
  font-size: 28rpx;
  color: var(--dong-primary);
  font-weight: 700;
}

.route-btn {
  font-size: 22rpx;
  color: #FFFFFF;
  background: var(--dong-primary);
  padding: 10rpx 22rpx;
  border-radius: 24rpx;
  font-weight: 600;
  box-shadow: 0 4rpx 12rpx rgba(74, 144, 226, 0.3);
}

/* ===== 特色美食 ===== */
.food-scroll {
  white-space: nowrap;
}

.food-list {
  display: inline-flex;
  gap: 20rpx;
  padding: 4rpx 4rpx 10rpx;
}

.food-card {
  display: inline-flex;
  flex-direction: column;
  width: 220rpx;
  background: var(--dong-paper);
  border-radius: var(--dong-radius-lg);
  overflow: hidden;
  box-shadow: var(--dong-shadow-sm);
}

.food-img-wrap {
  position: relative;
  width: 220rpx;
  height: 180rpx;
}

.food-img {
  width: 220rpx;
  height: 180rpx;
}

.food-rating-badge {
  position: absolute;
  top: 12rpx;
  right: 12rpx;
  background: rgba(255, 255, 255, 0.92);
  color: #D4A373;
  font-size: 20rpx;
  padding: 4rpx 12rpx;
  border-radius: 20rpx;
  font-weight: 600;
  box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.12);
}

.food-body {
  padding: 14rpx 16rpx 18rpx;
}

.food-name {
  font-size: 26rpx;
  color: var(--dong-blue);
  font-weight: bold;
  display: block;
  margin-bottom: 10rpx;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.food-meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.food-price {
  font-size: 24rpx;
  color: var(--dong-accent);
  font-weight: 600;
}

.food-detail-link {
  font-size: 20rpx;
  color: var(--dong-primary);
  font-weight: 500;
}

/* ===== 社区帖子 ===== */
.post-list { 
  display: flex; 
  flex-direction: column; 
  gap: 20rpx; 
}

.post-card {
  padding: 24rpx;
  background: var(--dong-paper);
  border-radius: var(--dong-radius-lg);
  box-shadow: var(--dong-shadow-sm);
}

.post-header { 
  display: flex; 
  align-items: center; 
  gap: 16rpx; 
  margin-bottom: 16rpx; 
}

.post-avatar-img {
  width: 72rpx;
  height: 72rpx;
  border-radius: 50%;
  border: 2rpx solid rgba(74, 144, 226, 0.2);
}

.post-meta { 
  flex: 1; 
}

.post-author {
  display: block;
  font-size: 28rpx;
  font-weight: 600;
  color: var(--dong-secondary);
}

.post-time {
  display: block;
  font-size: 22rpx;
  color: var(--dong-text-muted);
}

.post-content { 
  font-size: 26rpx; 
  color: #555; 
  line-height: 1.7; 
  display: block; 
  margin-bottom: 16rpx; 
}

.post-cover-img { 
  width: 100%; 
  height: 300rpx; 
  border-radius: 12rpx; 
  margin-bottom: 16rpx; 
}

.post-tags { 
  display: flex; 
  flex-wrap: wrap; 
  gap: 8rpx; 
  margin-bottom: 16rpx; 
}

.post-actions {
  display: flex;
  gap: 40rpx;
  border-top: 1rpx solid var(--dong-border);
  padding-top: 16rpx;
}

.post-action {
  font-size: 24rpx;
  color: var(--dong-text-muted);
  display: flex;
  align-items: center;
  gap: 8rpx;
}

.action-icon {
  width: 26rpx;
  height: 26rpx;
}

.action-liked {
  color: #A33B32;
  font-weight: 600;
}

/* ===== 活态故事 Banner ===== */
.story-banner {
  background: linear-gradient(135deg, #4A90E2 0%, #2C3E50 100%);
  border-radius: 32rpx;
  padding: 40rpx 36rpx;
  overflow: hidden;
  position: relative;
  box-shadow: 0 8rpx 30rpx rgba(74, 144, 226, 0.3);
}

.story-banner::before {
  content: '';
  position: absolute;
  top: -50%;
  right: -30%;
  width: 300rpx;
  height: 300rpx;
  background: radial-gradient(circle, rgba(255,255,255,0.15) 0%, transparent 70%);
  border-radius: 50%;
}

.story-content { 
  position: relative; 
  z-index: 1; 
}

.story-label {
  display: block;
  font-size: 22rpx;
  color: #FF7F50;
  letter-spacing: 0.3em;
  margin-bottom: 16rpx;
  font-weight: 700;
}

.story-text { 
  display: block; 
  font-size: 30rpx; 
  color: #FFFFFF; 
  line-height: 1.8; 
  margin-bottom: 28rpx; 
  font-weight: 500;
  text-shadow: 0 2rpx 8rpx rgba(0,0,0,0.2);
}

.story-btn {
  display: inline-flex;
  align-items: center;
  gap: 8rpx;
  background: #FFFFFF;
  color: #4A90E2;
  font-size: 26rpx;
  padding: 16rpx 36rpx;
  border-radius: 9999rpx;
  font-weight: 700;
  box-shadow: 0 4rpx 16rpx rgba(255, 255, 255, 0.25);
  transition: transform 0.3s ease;
}

.story-btn:active {
  transform: scale(0.95);
}

.empty-section {
  text-align: center;
  padding: 40rpx 0;
  font-size: 26rpx;
  color: #999;
}
</style>