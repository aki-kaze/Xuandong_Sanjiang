<template>
  <scroll-view class="heritage-page" scroll-y @scroll="onPageScroll">

    <!-- 页面顶部 Hero -->
    <view class="page-hero">
      <image class="hero-bg" src="https://cdn.phototourl.com/free/2026-03-23-75376aa0-ba75-4269-8839-bbe6aca040a6.jpg" mode="aspectFill" />
      <text class="hero-eyebrow">三江侗族自治县</text>
      <text class="hero-title">非遗文化</text>
      <text class="hero-sub">传承千年 · 活态共生</text>
      <view class="hero-stats">
        <view class="stat-item">
          <text class="stat-num">1</text>
          <text class="stat-label">世界非遗</text>
        </view>
        <view class="stat-divider"></view>
        <view class="stat-item">
          <text class="stat-num">3</text>
          <text class="stat-label">国家非遗</text>
        </view>
        <view class="stat-divider"></view>
        <view class="stat-item">
          <text class="stat-num">2</text>
          <text class="stat-label">省级非遗</text>
        </view>
      </view>
    </view>

    <!-- 地图入口按钮 -->
    <view class="heritage-map-link" @tap="goHeritageMap">
      <image class="heritage-map-icon" src="/static/icons/景区地图.png" mode="aspectFit" />
       <text class="motto-text">非遗地图</text>
    </view>

    <!-- 非遗护照入口 -->
    <view class="passport-link" @tap="goPassport">
      <image class="passport-link-icon" src="/static/icons/feiyi.png" mode="aspectFit" />
      <view class="passport-link-info">
        <text class="passport-link-title">非遗护照</text>
        <text class="passport-link-sub">已集 {{ collectedStamps.length }}/6 枚印章</text>
      </view>
      <text class="passport-link-arrow">></text>
    </view>

    <!-- 分类筛选-->
    <view class="filter-bar">
      <view
        class="filter-item"
        v-for="f in filters"
        :key="f.id"
        :class="{ active: activeFilter === f.id }"
        @tap="setFilter(f.id)"
      >
        <text class="filter-dot" :style="{ background: f.color }"></text>
        <text class="filter-label">{{ f.label }}</text>
      </view>
    </view>

    <!-- 非遗项目列表 -->
    <view class="heritage-list">
      <view
        class="heritage-card"
        v-for="item in filteredItems"
        :key="item.id"
      >
        <!-- 封面图区 -->
        <view class="card-cover-wrap" @tap="toggleExpand(item)">
          <image class="card-cover" :src="item.cover" mode="aspectFill" />
          <view class="card-level-badge" :style="{ background: item.color }">
            <text class="badge-text">{{ item.level }}</text>
          </view>
          <view class="card-cover-info">
            <text class="card-name-on-img">{{ item.name }}</text>
          </view>
        </view>

        <!-- 摘要 -->
        <view class="card-body" @tap="toggleExpand(item)">
          <text class="card-desc">{{ item.desc }}</text>
          <view class="expand-row">
            <text class="expand-text">{{ expandedId === item.id ? '收起详情' : '展开详情' }}</text>
            <text class="expand-arrow" :class="{ rotated: expandedId === item.id }">▼</text>
          </view>
        </view>

        <!-- ===== 展开详情 ===== -->
        <view class="card-detail" v-if="expandedId === item.id">

          <!-- 详情标签 -->
          <view class="detail-tabs">
            <view
              class="detail-tab"
              v-for="tab in detailTabs"
              :key="tab.id"
              :class="{ active: activeTab === tab.id }"
              @tap="activeTab = tab.id"
            >
              <image class="icon-img" :src="tab.icon" mode="aspectFit" />
              <text class="tab-label">{{ tab.label }}</text>
            </view>
          </view>

          <!-- Tab: 历史背景 -->
          <view class="tab-panel" v-if="activeTab === 'history'">
            <text class="panel-text">{{ item.detail }}</text>
            <view class="timeline" v-if="item.timeline && item.timeline.length">
              <text class="timeline-title">历史沿革</text>
              <view class="timeline-item" v-for="(t, idx) in item.timeline" :key="idx">
                <view class="timeline-left">
                  <view class="timeline-dot" :style="{ background: item.color }"></view>
                  <view class="timeline-line" v-if="idx < item.timeline.length - 1"></view>
                </view>
                <view class="timeline-right">
                  <text class="timeline-year" :style="{ color: item.color }">{{ t.year }}</text>
                  <text class="timeline-event">{{ t.event }}</text>
                </view>
              </view>
            </view>
          </view>

          <!-- Tab: 技艺传承 -->
          <view class="tab-panel" v-if="activeTab === 'skill'">
            <text class="panel-text">{{ item.skillDesc }}</text>
            <view class="inheritor-card" v-if="item.inheritor">
              <view class="inheritor-header">
                <view class="inheritor-avatar" :style="{ background: item.color + '22' }">
                  <image class="icon-img" src="/static/icons/mine1.png" mode="aspectFit" />
                </view>
                <view class="inheritor-meta">
                  <text class="inheritor-name">{{ item.inheritor.name }}</text>
                  <view class="inheritor-title-badge">
                    <text class="inheritor-title-text">{{ item.inheritor.title }}</text>
                  </view>
                </view>
              </view>
              <text class="inheritor-story">{{ item.inheritor.story }}</text>
            </view>
          </view>

          <!-- Tab: 活态共生 -->
          <view class="tab-panel" v-if="activeTab === 'story'">
            <view class="story-block">
              <view class="story-quote-mark">"</view>
              <text class="story-quote-text">{{ item.story }}</text>
              <view class="story-footer">
                <view class="story-line"></view>
                <text class="story-author">{{ item.storyAuthor }}</text>
              </view>
            </view>
            <view class="story-living-tips">
              <text class="tips-title"><image class="tips-title-icon" src="/static/icons/position.png" mode="aspectFit" /> 亲身体验</text>
              <text class="tips-text">前往程阳八寨，在鼓楼夜晚听一曲天籁大歌；走进林溪镇，拜访传承人，感受千年技艺的温度。</text> 
              <view class="tips-nav-btn" @tap.stop="navigateToHeritage(item)">
                <text class="tips-nav-text"><image class="tips-nav-icon" src="/static/icons/compass.png" mode="aspectFit" /> 导航前往</text>
              </view>
            </view>
          </view>

          <!-- 沉浸式音频讲解 -->
          <view class="media-section">
            <text class="media-section-title"><image class="icon-img" src="/static/icons/ai.png" mode="aspectFit" /> 沉浸式音频讲解</text>

            <!-- 音频播放按钮 -->
            <view class="audio-player" @tap="togglePlay(item)">
              <view class="audio-btn" :class="{ playing: playingId === item.id }" :style="playingId === item.id ? { background: item.color } : {}">
                <image class="audio-btn-icon-img" src="/static/icons/pause.png" mode="aspectFit" v-if="playingId === item.id" />
                <image class="audio-btn-icon-img" src="/static/icons/play.png" mode="aspectFit" v-else />
              </view>
              <view class="audio-info">
                <text class="audio-name">{{ item.name }} · 文化音频讲解</text>
                <text class="audio-duration">时长：约3分钟</text>
              </view>
              <view class="audio-wave" v-if="playingId === item.id">
                <view class="wave-bar" v-for="i in 5" :key="i" :style="{ animationDelay: (i * 0.1) + 's' }"></view>
              </view>
              <text class="audio-status" v-else>点击播放</text>
            </view>

            <!-- 视频讲解 --> 
            <view class="video-card" @tap="watchVideo(item)">
              <image class="video-thumb" :src="item.cover" mode="aspectFill" />
              <view class="video-mask">
                <view class="video-play-circle">
                  <image class="icon-img" src="/static/icons/play.png" mode="aspectFit" />
                </view>
                <view class="video-label-wrap">
                  <text class="video-label">传承人亲身体验 · 深度讲解</text>
                  <text class="video-sub">点击观看视频</text>
                </view>
              </view>
            </view>
          </view>

          <!-- 体验预约入口 -->
          <view class="experience-section">
            <text class="experience-title"><image class="icon-img" src="/static/icons/ticket.png" mode="aspectFit" /> 亲身体验</text>
            <view class="experience-card" @tap="bookExperience(item)">
              <view class="exp-left">
                <view class="exp-icon-wrap" :style="{ background: item.color + '15' }">
                  <image class="icon-img" src="/static/icons/ticket.png" mode="aspectFit" />
                </view>
                <view class="exp-info">
                  <text class="exp-name">{{ item.name }}体验</text>
                  <text class="exp-desc">传承人亲自指导，深度体验非遗技艺</text>
                  <view class="exp-meta">
                    <text class="exp-duration"><image class="icon-img" src="/static/icons/duration.png" mode="aspectFit" /> 约2小时</text>
                    <text class="exp-people"><image class="icon-img" src="/static/icons/all.png" mode="aspectFit" /> 2-8人</text>
                  </view>
                </view>
              </view>
              <view class="exp-right">
                <text class="exp-price" v-if="getExpPrice(item) > 0">¥{{ getExpPrice(item) }}</text>
                <text class="exp-price free" v-else>免费</text>
                <text class="exp-price-unit" v-if="getExpPrice(item) > 0">/人</text>
                <view class="exp-book-btn">预约</view>
              </view>
            </view>
            <!-- 体验项目预览 -->
            <view class="exp-types-preview">
              <view class="exp-type-tag" v-for="tag in getExpTags(item)" :key="tag">
                <text class="exp-type-tag-text">{{ tag }}</text>
              </view>
            </view>
          </view>

          <!-- 领章按钮 -->
          <view class="stamp-claim-section">
            <view
              class="stamp-claim-btn"
              :class="{ collected: isStampCollected(item.id) }"
              :style="isStampCollected(item.id) ? { background: item.color } : {}"
              @tap.stop="claimStamp(item)"
            >
              <text class="stamp-claim-icon" v-if="isStampCollected(item.id)">✅</text>
              <image class="stamp-claim-icon" src="/static/icons/feiyi.png" mode="aspectFit" v-else></image>
              <text class="stamp-claim-text">{{ isStampCollected(item.id) ? '已集章' : '领取印章' }}</text>
            </view>
          </view>

          <!-- 用户评价 -->
          <view class="comments-section">
            <view class="comments-header">
              <text class="comments-title"><image class="icon-img" src="/static/icons/评论.png" mode="aspectFit" /> 游客评价</text>
              <text class="comments-count">({{ getComments(item).length }}条评价)</text>
            </view>
            <view class="comment-item" v-for="comment in getComments(item)" :key="comment.id">
              <view class="comment-user">
                <image class="comment-avatar" :src="comment.avatar" mode="aspectFill" />
                <view class="comment-user-info">
                  <text class="comment-name">{{ comment.name }}</text>
                  <text class="comment-time">{{ comment.time }}</text>
                </view>
                <view class="comment-rating">
                  <text v-for="s in 5" :key="s" :style="{ color: s <= comment.rating ? '#D4A373' : '#E0E0E0' }">★</text>
                </view>
              </view>
              <text class="comment-text">{{ comment.text }}</text>
            </view>
            <view class="comment-more" v-if="getComments(item).length > 0">
              <text>查看全部评价 ({{ getComments(item).length }})</text>
            </view>
            <view class="comment-empty" v-else>
              <text>暂无评价，来成为第一个体验者吧</text>
            </view>
          </view>

          <!-- 相关文旅产品推荐 -->
          <view class="products-section" v-if="getRelatedProducts(item).length">
            <view class="products-header">
              <text class="products-title"><image class="icon-img" src="/static/icons/shop.png" mode="aspectFit" /> 相关文旅体验</text>
              <text class="products-sub">精选产品，开启非遗之活态共生</text>
            </view>
            <scroll-view scroll-x class="products-scroll">
              <view class="products-list">
                <view
                  class="product-card"
                  v-for="p in getRelatedProducts(item)"
                  :key="p.id"
                  @tap="goTicket(p)"
                >
                  <view class="product-img-wrap">
                    <image class="product-img" :src="p.cover" mode="aspectFill" />
                    <view class="product-discount-tag" v-if="p.originalPrice > p.price">
                      <text>{{ Math.round(p.price / p.originalPrice * 10) }}折</text>
                    </view>
                  </view>
                  <view class="product-info">
                    <text class="product-name">{{ p.name }}</text>
                    <view class="product-price-row">
                      <text class="product-price">¥{{ p.price }}</text>
                      <text class="product-original">¥{{ p.originalPrice }}</text>
                    </view>
                    <view class="product-btn" @tap.stop="goTicket(p)">立即预订</view>
                  </view>
                </view>
              </view>
            </scroll-view>
          </view>

        </view>
      </view>
    </view>

    <view style="height: 60rpx;"></view>
  </scroll-view>
</template>

<script>
import { staticData } from '../../utils/static-data.js'
import { heritageApi, attractionApi } from '../../utils/api.js'
import { collectStamp, showStampToast, getCollectedStamps } from '../../utils/stamps.js'

export default {
  data() {
    return {
      heritageItems: [],
      tickets: [],
      activeFilter: 'all',
      filters: [
        { id: 'all', label: '全部', color: '#4A90E2' },
        { id: '世界非遗', label: '世界非遗', color: '#4A90E2' },
        { id: '国家非遗', label: '国家非遗', color: '#2C3E50' },
        { id: '省级非遗', label: '省级非遗', color: '#FF7F50' }
      ],
      expandedId: null,
      activeTab: 'history',
      playingId: null,
      audioCtx: null,
      innerAudioContext: null,
      collectedStamps: [],
      detailTabs: [
        { id: 'history', icon: '/static/icons/detail.png', label: '历史背景', isPng: true },
        { id: 'skill', icon: '/static/icons/ai.png', label: '技艺传承', isPng: true },
        { id: 'story', icon: '/static/icons/heritage.png', label: '活态共生', isPng: true }
      ],
      loading: false,
      dataSource: 'static',
      // 模拟用户评价数据
      commentsData: {
        1: [
          { id: 1, name: '小雨', avatar: 'https://cdn.phototourl.com/free/2026-03-24-955d494f-d01c-4911-87ea-2e684f9a3f29.jpg', time: '2026-03-20', rating: 5, text: '在程阳八寨鼓楼前听了一曲侗族大歌，真的被震撼到了！那种多声部的和声，完全不需要指挥和伴奏，太不可思议了！' },
          { id: 2, name: '旅行者阿鑫', avatar: 'https://cdn.phototourl.com/free/2026-03-24-9da6f2f7-f9d3-46ac-9819-695e797b76e3.jpg', time: '2026-03-18', rating: 5, text: '传承人吴炳亮老师亲自教我们唱侗歌，虽然唱得不好，但那种沉浸感真的很棒。强烈推荐！' },
          { id: 3, name: '文化探索', avatar: 'https://cdn.phototourl.com/member/2026-03-24-3cd4dc5f-9c84-4b60-b074-1dba51407d7e.png', time: '2026-03-15', rating: 4, text: '侗族大歌不愧是世界非遗，歌声如同天籁。不过建议提前预约体验课，不然可能排不上队。' }
        ],
        2: [
          { id: 4, name: '建筑爱好', avatar: 'https://cdn.phototourl.com/member/2026-03-24-1c287f60-cae7-454e-b829-926032cddd96.png', time: '2026-03-22', rating: 5, text: '程阳永济桥太壮观了！不用一颗钉子，全靠榫卯结构连接，古人的智慧令人叹服！' },
          { id: 5, name: '摄影爱好', avatar: 'https://cdn.phototourl.com/member/2026-03-24-f1ef810e-bb9a-439b-bc19-526382cc971b.jpg', time: '2026-03-19', rating: 5, text: '风雨桥的飞檐翘角太美了，日落时分拍照效果绝佳。建议傍晚去拍摄。' }
        ],
        3: [
          { id: 6, name: '带娃旅行', avatar: 'https://cdn.phototourl.com/free/2026-03-24-955d494f-d01c-4911-87ea-2e684f9a3f29.jpg', time: '2026-03-21', rating: 5, text: '三江鼓楼27层高耸入云，小朋友看得目瞪口呆。鼓楼广场晚上还有侗族大歌表演，非常值得一看！' }
        ],
        4: [
          { id: 7, name: '手工艺爱好', avatar: 'https://cdn.phototourl.com/free/2026-03-24-9da6f2f7-f9d3-46ac-9819-695e797b76e3.jpg', time: '2026-03-17', rating: 5, text: '在博物馆体验了侗锦织造，粟田梅老师亲自指导，虽然织了一下午只完成一小块，但成就感满满！' }
        ]
      }
    }
  },
  computed: {
    filteredItems() {
      if (this.activeFilter === 'all') return this.heritageItems
      return this.heritageItems.filter(item => item.level === this.activeFilter)
    }
  },
  onLoad() {
    this.refreshStamps()
    this.loadHeritageData()
  },
  methods: {
    async loadHeritageData(level = '') {
      this.loading = true
      try {
        // 优先尝试从后端API获取非遗数据
        const heritageResult = await heritageApi.getList(1, 50, level)
        const heritageList = heritageResult.records || heritageResult.list || heritageResult || []
        if (Array.isArray(heritageList) && heritageList.length > 0) {
          // 从静态数据中获取audioUrl/videoUrl用于补充API数据
          const staticMap = {}
          ;(staticData.heritage || []).forEach(s => { staticMap[s.id] = s })
          this.heritageItems = heritageList.map(item => {
            // 处理JSON字符串字段
            if (typeof item.timeline === 'string') {
              try { item.timeline = JSON.parse(item.timeline) } catch (e) { item.timeline = [] }
            }
            if (typeof item.inheritor === 'string') {
              try { item.inheritor = JSON.parse(item.inheritor) } catch (e) { item.inheritor = null }
            }
            if (typeof item.relatedTicketIds === 'string') {
              try { item.relatedTicketIds = JSON.parse(item.relatedTicketIds) } catch (e) { item.relatedTicketIds = [] }
            }
            // 补充静态数据中的audioUrl/videoUrl（后端数据库暂无这两个字段）
            const staticItem = staticMap[item.id]
            if (staticItem) {
              if (!item.audioUrl) item.audioUrl = staticItem.audioUrl || ''
              if (!item.videoUrl) item.videoUrl = staticItem.videoUrl || ''
            }
            return item
          })
          this.dataSource = 'api'
          console.log('非遗数据加载成功（后端API）')
        } else {
          throw new Error('API返回数据为空')
        }

        // 尝试从后端API获取门票数据
        try {
          const ticketResult = await attractionApi.getList(1, 50)
          const ticketList = ticketResult.records || ticketResult.list || ticketResult || []
          if (Array.isArray(ticketList) && ticketList.length > 0) {
            this.tickets = ticketList.map(t => ({
              ...t,
              price: t.ticket_price || t.price || 0,
              original_price: t.ticket_price || t.price || 0,
              type: t.category || '景点门票',
              includes: '["景区门票"]',
              includesArray: ['景区门票']
            }))
          } else {
            this.tickets = staticData.tickets || []
          }
        } catch (e) {
          this.tickets = staticData.tickets || []
        }
      } catch (error) {
        // 后端不可用，降级到静态数据
        console.warn('API调用失败，使用静态数据:', error.message || error)
        this.loadStaticData()
      } finally {
        this.loading = false
      }
    },
    loadStaticData() {
      this.heritageItems = staticData.heritage || []
      this.tickets = staticData.tickets || []
      this.dataSource = 'static'
      console.log('非遗数据加载成功（静态数据）')
    },
    setFilter(id) {
      this.activeFilter = id
      this.expandedId = null
      this.stopPlay()
      // 重新加载数据，传入分类参数
      this.loadHeritageData(id === 'all' ? '' : id)
    },
    toggleExpand(item) {
      if (this.expandedId === item.id) {
        this.expandedId = null
        this.stopPlay()
      } else {
        this.expandedId = item.id
        this.activeTab = 'history'
        this.stopPlay()
      }
    },
    togglePlay(item) {
      if (this.playingId === item.id) {
        this.stopPlay()
      } else {
        this.stopPlay()
        this.playingId = item.id

        // 创建音频上下文
        if (!this.innerAudioContext) {
          this.innerAudioContext = uni.createInnerAudioContext()
        }

        // 使用文字转语音播报非遗介绍
        this.playTextToSpeech(item)
      }
    },

    // 文字转语音播报
    playTextToSpeech(item) {
      // 如果有真实音频URL，使用音频文件
      if (item.audioUrl) {
        this.innerAudioContext.src = item.audioUrl
        this.innerAudioContext.play()

        this.innerAudioContext.onPlay(() => {
          console.log('开始播放:', item.audioUrl)
        })

        this.innerAudioContext.onEnded(() => {
          this.playingId = null
          uni.showToast({ title: '播放完毕', icon: 'success' })
        })

        this.innerAudioContext.onError((err) => {
          console.error('播放失败:', err)
          this.playingId = null
          uni.showToast({ title: '播放失败', icon: 'none' })
        })
      } else {
        // 构建播报文本
        const text = `${item.name}是${item.level}非遗，${item.desc}`

        // 在小程序中使用插件或第三方服务实现TTS
        // 这里使用模拟方式
        uni.showToast({
          title: '正在播放语音讲解',
          icon: 'none',
          duration: 2000
        })

        // 模拟播放（实际项目中应接入真实的TTS服务或音频文件）
        this._playTimer = setTimeout(() => {
          if (this.playingId === item.id) {
            this.playingId = null
            uni.showToast({ title: '播放完毕', icon: 'success', duration: 1500 })
          }
        }, 8000) // 模拟8秒播放时间
      }
    },

    stopPlay() {
      this.playingId = null

      if (this._playTimer) {
        clearTimeout(this._playTimer)
        this._playTimer = null
      }

      if (this.innerAudioContext) {
        try {
          this.innerAudioContext.stop()
        } catch (e) {
          console.error('停止音频失败:', e)
        }
      }
    },
    watchVideo(item) {
      if (item.videoUrl) {
        const posterParam = item.cover ? '&poster=' + encodeURIComponent(item.cover) : ''
        uni.navigateTo({
          url: '/pages/heritage/video?url=' + encodeURIComponent(item.videoUrl) + posterParam
        })
      } else {
        uni.showModal({
          title: '视频讲解',
          content: `即将播放「${item.name}」》传承人深度讲解视频`,
          confirmText: '开始观看',
          cancelText: '取消',
          success: (res) => {
            if (res.confirm) {
              uni.showToast({ title: '视频加载中...', icon: 'loading', duration: 1500 })
            }
          }
        })
      }
    },
    getRelatedProducts(item) {
      if (!item.relatedTicketIds || !item.relatedTicketIds.length) return []
      return this.tickets.filter(t => item.relatedTicketIds.includes(t.id))
    },
    goTicket(product) {
      uni.navigateTo({ url: '/pages/ticket/ticket?id=' + product.id })
    },
    navigateToHeritage(item) {
      const lat = parseFloat(item.latitude || item.lat)
      const lng = parseFloat(item.longitude || item.lng)
      if (!lat || !lng) { uni.showToast({ title: '暂无位置信息', icon: 'none' }); return }
      uni.openLocation({
        latitude: lat,
        longitude: lng,
        name: item.name,
        address: item.address || '三江侗族自治县',
        scale: 16,
        fail: () => { uni.showToast({ title: '打开地图失败', icon: 'none' }) }
      })
    },
    goHeritageMap() {
      uni.navigateTo({ url: '/pages/heritage/heritage-map' })
    },
    goPassport() {
      uni.navigateTo({ url: '/pages/heritage/passport' })
    },
    getComments(item) {
      return this.commentsData[item.id] || []
    },
    bookExperience(item) {
      const cover = encodeURIComponent(item.cover || '')
      const name = encodeURIComponent(item.name)
      uni.navigateTo({
        url: `/pages/heritage/experience-book?id=${item.id}&name=${name}&cover=${cover}`
      })
    },
    getExpTags(item) {
      const tagMap = {
        1: ['侗族大歌学唱', '多声部合唱', '传承人指导'],
        2: ['风雨桥建筑', '榫卯结构讲解', '建造工艺'],
        3: ['鼓楼文化', '登楼体验', '品油茶'],
        4: ['侗锦织造', '织机操作', '图案设计'],
        5: ['芦笙吹奏', '乐器体验', '节庆舞蹈'],
        6: ['多耶舞蹈', '集体歌舞', '互动体验']
      }
      return tagMap[item.id] || ['非遗体验', '传承人指导']
    },
    getExpPrice(item) {
      const priceMap = { 1: 88, 2: 0, 3: 10, 4: 68, 5: 78, 6: 58 }
      return priceMap[item.id] !== undefined ? priceMap[item.id] : 88
    },
    refreshStamps() {
      this.collectedStamps = getCollectedStamps()
    },
    isStampCollected(heritageId) {
      return this.collectedStamps.some(s => s.id === heritageId)
    },
    claimStamp(item) {
      const result = collectStamp(item.id, 'booking')
      if (result.isNew) {
        this.refreshStamps()
        showStampToast(result.stamp, result.allCollected)
      } else if (this.isStampCollected(item.id)) {
        uni.showToast({ title: '已集过此印章', icon: 'none' })
      }
    },
    onPageScroll() {}
  },
  onUnload() {
    this.stopPlay()
    if (this.innerAudioContext) {
      this.innerAudioContext.destroy()
    }
  }
}
</script>

<style scoped lang="scss">
/* ===== 统一图标尺寸 ===== */
.icon-img { width: 32rpx; height: 32rpx; flex-shrink: 0; }

/* ===== 页面基础 ===== */
.heritage-page {
  background: #F8FAFC;
  min-height: 100vh;
}

/* ===== Hero Banner ===== */
.page-hero {
  position: relative;
  height: 480rpx;
  overflow: hidden;
  padding: 0 40rpx 40rpx;
  display: flex;
  flex-direction: column;
  justify-content: flex-end;
}
.hero-bg {
  position: absolute;
  inset: 0;
  width: 100%;
  height: 100%;
  z-index: 0;
}
.hero-eyebrow {
  position: relative;
  z-index: 1;
  font-size: 22rpx;
  color: #FF7F50;
  letter-spacing: 0.25em;
  font-weight: 600;
  margin-bottom: 10rpx;
  text-shadow: 0 2rpx 8rpx rgba(0,0,0,0.6);
}
.hero-title {
  position: relative;
  z-index: 1;
  font-size: 72rpx;
  font-weight: 600;
  color: #FFFFFF;
  letter-spacing: 0.12em;
  display: block;
  margin-bottom: 8rpx;
  text-shadow: 0 2rpx 12rpx rgba(0,0,0,0.7);
}
.hero-sub {
  position: relative;
  z-index: 1;
  font-size: 26rpx;
  color: #FFFFFF;
  display: block;
  margin-bottom: 32rpx;
  text-shadow: 0 2rpx 8rpx rgba(0,0,0,0.6);
}
.hero-stats {
  position: relative;
  z-index: 1;
  display: flex;
  align-items: center;
  gap: 0;
  background: rgba(44, 62, 80, 0.7);
  border-radius: 20rpx;
  padding: 20rpx 0;
}
.stat-item {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4rpx;
}
.stat-num {
  font-size: 44rpx;
  font-weight: 600;
  color: #FF7F50;
  line-height: 1;
}
.stat-label {
  font-size: 22rpx;
  color: rgba(255, 255, 255, 0.85);
}
.stat-divider {
  width: 1rpx;
  height: 48rpx;
  background: rgba(255, 255, 255, 0.25);
}

/* ===== 标语===== */
.motto-bar {
  margin: 24rpx 24rpx 0;
  background: linear-gradient(135deg, #4A90E2 0%, #2C3E50 100%);
  border-radius: 20rpx;
  padding: 24rpx 32rpx;
  display: flex;
  align-items: center;
  gap: 16rpx;
  box-shadow: 0 6rpx 20rpx rgba(74, 144, 226, 0.25);
  position: relative;
  overflow: hidden;
}
.motto-bar::before {
  content: '';
  position: absolute;
  top: -40%;
  right: -20%;
  width: 200rpx;
  height: 200rpx;
  background: radial-gradient(circle, rgba(255,255,255,0.15) 0%, transparent 60%);
  border-radius: 50%;
}
.motto-icon {
  font-size: 36rpx;
  flex-shrink: 0;
  position: relative;
  z-index: 1;
}
.motto-text {
  font-size: 26rpx;
  color: #1D3B5A;
  line-height: 1.6;
  flex: 1;
  font-weight: 600;
  position: relative;
  z-index: 1;
}

/* ===== 分类筛===== */
.filter-bar {
  display: flex;
  gap: 12rpx;
  padding: 24rpx 24rpx 0;
  overflow-x: auto;
}
.filter-item {
  display: flex;
  align-items: center;
  gap: 8rpx;
  background: #FFFFFF;
  border-radius: 50rpx;
  padding: 14rpx 28rpx;
  white-space: nowrap;
  box-shadow: 0 4rpx 16rpx rgba(74, 144, 226, 0.15);
  border: 2rpx solid transparent;
  transition: all 0.3s ease;
}
.filter-item.active {
  background: linear-gradient(135deg, #4A90E2 0%, #2C3E50 100%);
  border-color: #4A90E2;
  box-shadow: 0 6rpx 24rpx rgba(74, 144, 226, 0.35);
}
.filter-dot {
  width: 12rpx;
  height: 12rpx;
  border-radius: 50%;
}
.filter-label {
  font-size: 26rpx;
  color: #64748B;
  font-weight: 600;
}
.filter-item.active .filter-label {
  color: #FFFFFF;
}

/* ===== 非遗卡片列表 ===== */
.heritage-list {
  padding: 24rpx 24rpx 0;
  display: flex;
  flex-direction: column;
  gap: 24rpx;
}

/* ===== 卡片 ===== */
.heritage-card {
  background: #FFFFFF;
  border-radius: 28rpx;
  overflow: hidden;
  box-shadow: 0 8rpx 30rpx rgba(74, 144, 226, 0.18);
  border: 1rpx solid rgba(74, 144, 226, 0.08);
  transition: transform 0.3s ease, box-shadow 0.3s ease;
}

.heritage-card:active {
  transform: scale(0.98);
  box-shadow: 0 4rpx 16rpx rgba(74, 144, 226, 0.12);
}

/* 封面图区 */
.card-cover-wrap {
  position: relative;
  height: 280rpx;
}
.card-cover {
  width: 100%;
  height: 100%;
}
.card-level-badge {
  position: absolute;
  top: 20rpx;
  right: 20rpx;
  border-radius: 50rpx;
  padding: 8rpx 24rpx;
  box-shadow: 0 4rpx 12rpx rgba(0,0,0,0.2);
  backdrop-filter: blur(8px);
}
.badge-text {
  font-size: 22rpx;
  color: #FFFFFF;
  font-weight: 700;
  letter-spacing: 0.05em;
}
.card-cover-info {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  padding: 24rpx 32rpx 28rpx;
  display: flex;
  align-items: center;
  gap: 16rpx;
  background: linear-gradient(to top, rgba(0,0,0,0.6) 0%, transparent 100%);
}
.card-icon-big {
  font-size: 56rpx;
}
.card-name-on-img {
  font-size: 42rpx;
  font-weight: 700;
  color: #FFFFFF;
  letter-spacing: 0.06em;
  text-shadow: 0 2rpx 12rpx rgba(0,0,0,0.5);
}

/* 卡片摘要 */
.card-body {
  padding: 28rpx 32rpx 24rpx;
}
.card-desc {
  font-size: 28rpx;
  color: #475569;
  line-height: 1.8;
  display: block;
  margin-bottom: 20rpx;
  font-weight: 500;
}
.expand-row {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8rpx;
  padding: 16rpx 0 8rpx;
  border-top: 2rpx solid #F8FAFC;
}
.expand-text {
  font-size: 28rpx;
  color: #4A90E2;
  font-weight: 600;
}
.expand-arrow {
  font-size: 24rpx;
  color: #4A90E2;
  transition: transform 0.3s;
  display: inline-block;
}
.expand-arrow.rotated {
  transform: rotate(180deg);
}

/* ===== 展开详情区域 ===== */
.card-detail {
  border-top: 2rpx solid #F1F5F9;
}

/* 详情标签 */
.detail-tabs {
  display: flex;
  background: #F1F5F9;
}
.detail-tab {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 6rpx;
  padding: 20rpx 0;
  position: relative;
}
.detail-tab.active {
  background: #FFFFFF;
}
.detail-tab.active::after {
  content: '';
  position: absolute;
  bottom: 0;
  left: 20%;
  right: 20%;
  height: 4rpx;
  background: #4A90E2;
  border-radius: 2rpx;
}
.tab-label {
  font-size: 22rpx;
  color: #94A3B8;
}
.detail-tab.active .tab-label {
  color: #4A90E2;
  font-weight: 600;
}

/* Tab 内容面板 */
.tab-panel {
  padding: 28rpx;
}
.panel-text {
  font-size: 27rpx;
  color: #475569;
  line-height: 1.85;
  display: block;
  margin-bottom: 28rpx;
}

/* 历史时间线 */
.timeline-title {
  display: block;
  font-size: 26rpx;
  font-weight: 600;
  color: #2C3E50;
  margin-bottom: 20rpx;
}
.timeline-item {
  display: flex;
  gap: 20rpx;
  margin-bottom: 4rpx;
}
.timeline-left {
  display: flex;
  flex-direction: column;
  align-items: center;
  width: 24rpx;
}
.timeline-dot {
  width: 18rpx;
  height: 18rpx;
  border-radius: 50%;
  flex-shrink: 0;
  margin-top: 6rpx;
}
.timeline-line {
  width: 2rpx;
  flex: 1;
  background: #E2E8F0;
  margin: 6rpx 0;
  min-height: 30rpx;
}
.timeline-right {
  flex: 1;
  padding-bottom: 24rpx;
}
.timeline-year {
  display: block;
  font-size: 24rpx;
  font-weight: 600;
  margin-bottom: 4rpx;
}
.timeline-event {
  display: block;
  font-size: 25rpx;
  color: #64748B;
  line-height: 1.6;
}

/* 技艺传承 - 传承人卡片 */
.inheritor-card {
  background: #F8FAFC;
  border-radius: 16rpx;
  padding: 24rpx;
  margin-top: 8rpx;
}
.inheritor-header {
  display: flex;
  align-items: center;
  gap: 20rpx;
  margin-bottom: 16rpx;
}
.inheritor-avatar {
  width: 88rpx;
  height: 88rpx;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}
.inheritor-meta {
  flex: 1;
}
.inheritor-name {
  display: block;
  font-size: 30rpx;
  font-weight: 600;
  color: #2C3E50;
  margin-bottom: 8rpx;
}
.inheritor-title-badge {
  display: inline-block;
  background: rgba(74, 144, 226, 0.12);
  border-radius: 20rpx;
  padding: 4rpx 16rpx;
}
.inheritor-title-text {
  font-size: 22rpx;
  color: #4A90E2;
  font-weight: 500;
}
.inheritor-story {
  font-size: 25rpx;
  color: #64748B;
  line-height: 1.75;
  display: block;
}

/* 活态故事 */
.story-block {
  background: #2C3E50;
  border-radius: 16rpx;
  padding: 32rpx 28rpx 28rpx;
  margin-bottom: 24rpx;
  position: relative;
  overflow: hidden;
}
.story-block::before {
  content: '';
  position: absolute;
  right: -40rpx;
  top: -40rpx;
  width: 160rpx;
  height: 160rpx;
  background: rgba(255, 127, 80, 0.12);
  border-radius: 50%;
}
.story-quote-mark {
  font-size: 96rpx;
  color: #FF7F50;
  line-height: 0.6;
  font-family: Georgia, serif;
  margin-bottom: 12rpx;
  display: block;
}
.story-quote-text {
  font-size: 27rpx;
  color: rgba(255, 255, 255, 0.92);
  line-height: 1.85;
  font-style: normal;
  display: block;
  margin-bottom: 24rpx;
  position: relative;
  z-index: 1;
}
.story-footer {
  display: flex;
  align-items: center;
  gap: 16rpx;
}
.story-line {
  width: 40rpx;
  height: 2rpx;
  background: #FF7F50;
}
.story-author {
  font-size: 23rpx;
  color: #FF7F50;
}
.story-living-tips {
  background: rgba(74, 144, 226, 0.08);
  border-radius: 12rpx;
  padding: 20rpx 24rpx;
  border-left: 4rpx solid #4A90E2;
}
.tips-title {
  display: block;
  font-size: 26rpx;
  font-weight: 600;
  color: #4A90E2;
  margin-bottom: 10rpx;
  display: flex;
  align-items: center;
}

.tips-title-icon,
.tips-nav-icon {
  width: 28rpx;
  height: 28rpx;
  margin-right: 6rpx;
}
.tips-text {
  font-size: 25rpx;
  color: #64748B;
  line-height: 1.7;
  display: block;
  margin-bottom: 16rpx;
}
.tips-nav-btn {
  background: #4A90E2;
  border-radius: 30rpx;
  padding: 14rpx 32rpx;
  display: inline-flex;
  align-items: center;
}
.tips-nav-text {
  font-size: 26rpx;
  color: #FFFFFF;
  font-weight: 600;
}

/* ===== 沉浸式音频/视频 ===== */
.media-section {
  padding: 0 28rpx 24rpx;
}
.media-section-title {
  display: flex;
  align-items: center;
  font-size: 28rpx;
  font-weight: 600;
  color: #2C3E50;
  margin-bottom: 20rpx;
}

/* 音频播放器 */
.audio-player {
  display: flex;
  align-items: center;
  gap: 20rpx;
  background: #F8FAFC;
  border-radius: 16rpx;
  padding: 20rpx 24rpx;
  margin-bottom: 20rpx;
  border: 1rpx solid #E2E8F0;
}
.audio-btn {
  width: 72rpx;
  height: 72rpx;
  border-radius: 50%;
  background: #2C3E50;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  box-shadow: 0 4rpx 12rpx rgba(44, 62, 80, 0.25);
}
.audio-btn.playing {
  box-shadow: 0 4rpx 16rpx rgba(74, 144, 226, 0.35);
}
.audio-btn-icon {
  font-size: 28rpx;
  color: #FFFFFF;
  padding-left: 4rpx;
}
.audio-btn-icon-img {
  width: 32rpx;
  height: 32rpx;
}
.audio-btn.playing .audio-btn-icon {
  padding-left: 0;
}
.audio-info {
  flex: 1;
}
.audio-name {
  display: block;
  font-size: 26rpx;
  font-weight: 500;
  color: #2C3E50;
  margin-bottom: 6rpx;
}
.audio-duration {
  display: block;
  font-size: 22rpx;
  color: #94A3B8;
}
.audio-status {
  font-size: 22rpx;
  color: #CBD5E1;
}
.audio-wave {
  display: flex;
  align-items: center;
  gap: 5rpx;
  height: 40rpx;
}
.wave-bar {
  width: 6rpx;
  background: #4A90E2;
  border-radius: 3rpx;
  animation: wave 0.8s ease-in-out infinite alternate;
}
.wave-bar:nth-child(1) { height: 16rpx; animation-delay: 0s; }
.wave-bar:nth-child(2) { height: 30rpx; animation-delay: 0.1s; }
.wave-bar:nth-child(3) { height: 38rpx; animation-delay: 0.2s; }
.wave-bar:nth-child(4) { height: 26rpx; animation-delay: 0.3s; }
.wave-bar:nth-child(5) { height: 18rpx; animation-delay: 0.4s; }
@keyframes wave {
  from { transform: scaleY(0.5); }
  to { transform: scaleY(1); }
}

/* 视频卡片 */
.video-card {
  position: relative;
  height: 200rpx;
  border-radius: 16rpx;
  overflow: hidden;
}
.video-thumb {
  width: 100%;
  height: 100%;
}
.video-mask {
  position: absolute;
  inset: 0;
  background: rgba(10, 20, 40, 0.55);
  display: flex;
  align-items: center;
  gap: 24rpx;
  padding: 0 36rpx;
}
.video-play-circle {
  width: 88rpx;
  height: 88rpx;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.25);
  border: 3rpx solid rgba(255, 255, 255, 0.6);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  backdrop-filter: blur(4px);
}
.video-label-wrap {
  flex: 1;
}
.video-label {
  display: block;
  font-size: 28rpx;
  font-weight: 600;
  color: #FFFFFF;
  margin-bottom: 6rpx;
}
.video-sub {
  display: block;
  font-size: 22rpx;
  color: rgba(255, 255, 255, 0.75);
}

/* ===== 文旅产品推荐 ===== */
.products-section {
  padding: 0 28rpx 28rpx;
}
.products-header {
  margin-bottom: 20rpx;
}
.products-title {
  display: flex;
  align-items: center;
  font-size: 28rpx;
  font-weight: 600;
  color: #2C3E50;
  margin-bottom: 6rpx;
}
.products-sub {
  font-size: 22rpx;
  color: #94A3B8;
}
.products-scroll {
  white-space: nowrap;
}
.products-list {
  display: inline-flex;
  gap: 20rpx;
  padding: 4rpx 4rpx 8rpx;
}
.product-card {
  display: inline-flex;
  flex-direction: column;
  width: 280rpx;
  background: #F8FAFC;
  border-radius: 16rpx;
  overflow: hidden;
  box-shadow: 0 2rpx 12rpx rgba(74, 144, 226, 0.10);
  vertical-align: top;
}
.product-img-wrap {
  position: relative;
  height: 160rpx;
}
.product-img {
  width: 100%;
  height: 100%;
}
.product-discount-tag {
  position: absolute;
  top: 10rpx;
  left: 10rpx;
  background: #4A90E2;
  color: #FFFFFF;
  font-size: 20rpx;
  font-weight: 600;
  padding: 3rpx 12rpx;
  border-radius: 20rpx;
}
.product-info {
  padding: 16rpx 18rpx 18rpx;
}
.product-name {
  display: block;
  font-size: 24rpx;
  font-weight: 600;
  color: #2C3E50;
  margin-bottom: 10rpx;
  white-space: normal;
  line-height: 1.4;
}
.product-price-row {
  display: flex;
  align-items: baseline;
  gap: 8rpx;
  margin-bottom: 14rpx;
}
.product-price {
  font-size: 30rpx;
  font-weight: 600;
  color: #4A90E2;
}
.product-original {
  font-size: 22rpx;
  color: #CBD5E1;
  text-decoration: line-through;
}
.product-btn {
  background: #4A90E2;
  color: #FFFFFF;
  font-size: 22rpx;
  text-align: center;
  padding: 12rpx 0;
  border-radius: 20rpx;
  font-weight: 500;
}

/* ===== 地图入口 ===== */
.heritage-map-link {
  margin: 24rpx 24rpx 0;
  background: #FFFFFF;
  border-radius: 16rpx;
  padding: 20rpx 28rpx;
  display: flex;
  align-items: center;
  gap: 16rpx;
  box-shadow: 0 4rpx 20rpx rgba(74,144,226,0.12);
}
.map-entry-icon { font-size: 40rpx; }
.map-entry-text { font-size: 28rpx; color: #1D3B5A; font-weight: 600; }

/* ===== 体验预约 ===== */
.experience-section {
  padding: 0 28rpx 24rpx;
}
.experience-title {
  display: flex;
  align-items: center;
  font-size: 28rpx;
  font-weight: 600;
  color: #2C3E50;
  margin-bottom: 16rpx;
}
.experience-card {
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: #FFFFFF;
  border: 2rpx solid #F1F5F9;
  border-radius: 16rpx;
  padding: 20rpx;
}
.exp-left { display: flex; gap: 16rpx; flex: 1; }
.exp-icon-wrap {
  width: 72rpx;
  height: 72rpx;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}
.exp-info { flex: 1; display: flex; flex-direction: column; gap: 4rpx; }
.exp-name { font-size: 26rpx; font-weight: 600; color: #2C3E50; }
.exp-desc { font-size: 22rpx; color: #64748B; }
.exp-meta { display: flex; gap: 16rpx; }
.exp-duration, .exp-people { font-size: 20rpx; color: #94A3B8; display: flex; align-items: center; gap: 4rpx; }
.exp-right { display: flex; flex-direction: column; align-items: flex-end; gap: 4rpx; }
.exp-price { font-size: 32rpx; font-weight: 600; color: #4A90E2; }
.exp-price.free { color: #22C55E; }
.exp-price-unit { font-size: 20rpx; color: #94A3B8; }
.exp-book-btn {
  padding: 8rpx 24rpx;
  background: #4A90E2;
  color: #fff;
  border-radius: 20rpx;
  font-size: 22rpx;
  font-weight: 600;
}

/* 体验类型标签预览 */
.exp-types-preview {
  display: flex;
  flex-wrap: wrap;
  gap: 12rpx;
  margin-top: 16rpx;
}
.exp-type-tag {
  background: rgba(74,144,226,0.08);
  border-radius: 20rpx;
  padding: 6rpx 18rpx;
}
.exp-type-tag-text {
  font-size: 22rpx;
  color: #4A90E2;
  font-weight: 500;
}



.heritage-map-icon {
  width: 32rpx;
  height: 32rpx;
}
/* ===== 用户评价 ===== */
.comments-section {
  padding: 0 28rpx 24rpx;
}
.comments-header {
  display: flex;
  align-items: center;
  gap: 8rpx;
  margin-bottom: 16rpx;
}
.comments-title { font-size: 28rpx; font-weight: 600; color: #2C3E50; display: flex; align-items: center; }
.comments-count { font-size: 22rpx; color: #94A3B8; }
.comment-item {
  padding: 16rpx 0;
  border-bottom: 1rpx solid #F1F5F9;
}
.comment-user {
  display: flex;
  align-items: center;
  gap: 12rpx;
  margin-bottom: 10rpx;
}
.comment-avatar {
  width: 56rpx;
  height: 56rpx;
  border-radius: 50%;
  flex-shrink: 0;
}
.comment-user-info { flex: 1; }
.comment-name { display: block; font-size: 24rpx; font-weight: 600; color: #334155; }
.comment-time { display: block; font-size: 20rpx; color: #94A3B8; }
.comment-rating { display: flex; gap: 2rpx; font-size: 22rpx; }
.comment-text { font-size: 24rpx; color: #64748B; line-height: 1.6; display: block; }
.comment-more {
  text-align: center;
  padding: 16rpx 0;
  font-size: 24rpx;
  color: #4A90E2;
}
.comment-empty {
  text-align: center;
  padding: 30rpx 0;
  font-size: 24rpx;
  color: #94A3B8;
}

/* ===== 非遗护照入口 ===== */
.passport-link {
  margin: 16rpx 24rpx 0;
  background: #FFFFFF;
  border: 2rpx solid #F0F7FF;
  border-radius: 16rpx;
  padding: 20rpx 28rpx;
  display: flex;
  align-items: center;
  gap: 16rpx;
  box-shadow: 0 4rpx 20rpx rgba(74, 144, 226, 0.10);
}
.passport-link-icon {
  font-size: 40rpx;
  flex-shrink: 0;
  width: 60rpx;
  height: 60rpx;
}
.passport-link-info {
  flex: 1;
}
.passport-link-title {
  font-size: 28rpx;
  font-weight: 600;
  color: #1D3B5A;
  display: block;
}
.passport-link-sub {
  font-size: 22rpx;
  color: #64748B;
  display: block;
}
.passport-link-arrow {
  font-size: 32rpx;
  color: #4A90E2;
}

/* ===== 领章按钮 ===== */
.stamp-claim-section {
  padding: 0 28rpx 24rpx;
}
.stamp-claim-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10rpx;
  background: linear-gradient(135deg, #FF7F50 0%, #E8633A 100%);
  border-radius: 50rpx;
  padding: 18rpx 0;
  box-shadow: 0 4rpx 16rpx rgba(255,127,80,0.3);
}
.stamp-claim-btn.collected {
  box-shadow: 0 4rpx 16rpx rgba(0,0,0,0.1);
}
.stamp-claim-icon {
  font-size: 28rpx;
  width: 36rpx;
  height: 36rpx;
}
.stamp-claim-text {
  font-size: 26rpx;
  font-weight: 600;
  color: #FFFFFF;
}
.stamp-claim-btn.collected .stamp-claim-text {
  color: rgba(255,255,255,0.9);
}
</style>
