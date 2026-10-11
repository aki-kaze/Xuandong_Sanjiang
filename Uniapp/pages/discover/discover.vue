<template>
  <view class="discover-page">
    <view class="tab-bar">
      <view class="tab-item" v-for="tab in tabs" :key="tab.id" :class="{ active: activeTab === tab.id }" @tap="switchTab(tab.id)">
        {{ tab.label }}
      </view>
    </view>

    <view class="publish-bar" v-if="activeTab === 'community'" @tap="handlePublish">
      <view class="publish-input-wrap">
        <text class="publish-placeholder">分享你的侗乡旅行故事...</text>
      </view>
      <view class="publish-btn">发布</view>
    </view>

    <view class="post-list" v-if="activeTab === 'community'">
      <view class="post-card card-shadow" v-for="item in posts" :key="item.id" @tap="viewPost(item)">
        <view class="post-header">
          <image class="post-avatar-img" :src="item.avatar" mode="aspectFill" />
          <view class="post-meta">
            <text class="post-author">{{ item.author }}</text>
            <text class="post-time">{{ item.time }}</text>
          </view>
          <view class="follow-btn" v-if="!item.followed" @tap.stop="followUser(item)">关注</view>
          <view class="follow-btn followed" v-else @tap.stop>已关注</view>
        </view>
        <text class="post-content">{{ item.content }}</text>
        <image class="post-cover-img" :src="item.coverImg" mode="aspectFill" v-if="item.coverImg" />
        <view class="post-tags">
          <text class="post-tag" v-for="tag in item.tags" :key="tag"># {{ tag }}</text>
        </view>
        <view class="post-actions">
          <view class="action-item" @tap.stop="likePost(item)">
            <image 
              class="action-icon like-icon" 
              :src="item.liked ? '/static/icons/love.png' : '/static/icons/点赞.png'" 
              mode="aspectFit" 
            />
            <text class="action-count" :style="item.liked ? 'color:#A33B32' : ''">{{ item.likes }}</text>
          </view>
          <view class="action-item" @tap.stop="viewPost(item)">
            <image class="action-icon comment-icon" src="/static/icons/评论.png" mode="aspectFit" />
            <text class="action-count">{{ item.comments }}</text>
          </view>
          <view class="action-item" @tap.stop="toggleFavorite(item)">
            <image class="action-icon collect-icon" :src="item.favorited ? '/static/icons/love.png' : '/static/icons/收藏.png'" mode="aspectFit" />
            <text class="action-count" :style="item.favorited ? 'color:#A33B32' : ''">收藏</text>
          </view>
        </view>
      </view>
    </view>

    <!-- 我的发布 -->
    <view class="myposts-container" v-if="activeTab === 'myposts'">
      <view class="myposts-tab-bar">
        <view class="myposts-tab-item" :class="{ active: myPostsType === 'posts' }" @click="myPostsType = 'posts'">动态</view>
        <view class="myposts-tab-item" :class="{ active: myPostsType === 'guides' }" @click="myPostsType = 'guides'">攻略</view>
      </view>
      
      <!-- 我的动态 -->
      <view class="post-list" v-if="myPostsType === 'posts'">
        <view v-if="myPosts.length === 0" class="empty-my-posts">
          <text class="empty-text">你还没有发布过动态</text>
        </view>
        <view class="post-card card-shadow" v-for="item in myPosts" :key="item.id">
          <view class="post-header">
            <image class="post-avatar-img" :src="item.avatar" mode="aspectFill" />
            <view class="post-meta">
              <text class="post-author">{{ item.author }}</text>
              <text class="post-time">{{ item.time }}</text>
            </view>
            <view class="status-tag" :class="'status-' + item.status">
              {{ item.statusText }}
            </view>
          </view>
          <text class="post-content">{{ item.content }}</text>
          <image class="post-cover-img" :src="item.coverImg" mode="aspectFill" v-if="item.coverImg" />
          <view class="post-tags">
            <text class="post-tag" v-for="tag in item.tags" :key="tag"># {{ tag }}</text>
          </view>
          <view class="post-actions">
            <view class="action-item">
              <image class="action-icon like-icon" :src="'/static/icons/点赞.png'" mode="aspectFit" />
              <text class="action-count">{{ item.likes }}</text>
            </view>
            <view class="action-item">
              <image class="action-icon comment-icon" src="/static/icons/评论.png" mode="aspectFit" />
              <text class="action-count">{{ item.comments }}</text>
            </view>
          </view>
        </view>
      </view>
      
      <!-- 我的攻略 -->
      <view class="guide-list" v-if="myPostsType === 'guides'">
        <view v-if="myGuides.length === 0" class="empty-my-posts">
          <text class="empty-text">你还没有发布过攻略</text>
        </view>
        <view class="guide-card card-shadow" v-for="item in myGuides" :key="item.id" @tap="viewGuide(item)">
          <view class="guide-cover-wrap">
            <image class="guide-cover-img" :src="item.cover" mode="aspectFill" />
            <text class="guide-cover-title">{{ item.title }}</text>
            <view class="status-tag" :class="'status-' + item.status">
              {{ item.statusText }}
            </view>
          </view>
          <view class="guide-info">
            <text class="guide-title">{{ item.title }}</text>
            <text class="guide-desc">{{ item.desc }}</text>
            <view class="guide-meta">
              <view class="guide-author-wrap">
                <image class="guide-avatar-img" :src="item.authorAvatar" mode="aspectFill" />
                <text class="guide-author-name">{{ item.author }}</text>
              </view>
              <view class="guide-stats">
                <text class="guide-stat"><image src="/static/icons/detail.png" mode="aspectFit" style="width:22rpx;height:22rpx;margin-right:4rpx;" /> {{ item.views }}</text>
                <text class="guide-stat"><image src="/static/icons/love.png" mode="aspectFit" style="width:22rpx;height:22rpx;margin-right:4rpx;" /> {{ item.likes }}</text>
              </view>
            </view>
            <view class="guide-tags">
              <text class="guide-tag" v-for="tag in item.tags" :key="tag">{{ tag }}</text>
            </view>
          </view>
        </view>
      </view>
    </view>

    <view class="guide-list" v-if="activeTab === 'guide'">
      <view class="publish-bar" @tap="handlePublishGuide">
        <view class="publish-input-wrap">
          <text class="publish-placeholder">分享你的旅行攻略...</text>
        </view>
        <view class="publish-btn">发布攻略</view>
      </view>
      <view class="guide-card card-shadow" v-for="item in guides" :key="item.id" @tap="viewGuide(item)">
        <view class="guide-cover-wrap">
          <image class="guide-cover-img" :src="item.cover" mode="aspectFill" />
          <text class="guide-cover-title">{{ item.title }}</text>
          <view class="guide-hot-badge" v-if="item.hot">🔥 热门</view>
        </view>
        <view class="guide-info">
          <text class="guide-title">{{ item.title }}</text>
          <text class="guide-desc">{{ item.desc }}</text>
          <view class="guide-meta">
            <view class="guide-author-wrap">
              <image class="guide-avatar-img" :src="item.authorAvatar" mode="aspectFill" />
              <text class="guide-author-name">{{ item.author }}</text>
            </view>
            <view class="guide-stats">
              <text class="guide-stat"><image src="/static/icons/detail.png" mode="aspectFit" style="width:22rpx;height:22rpx;margin-right:4rpx;" /> {{ item.views }}</text>
              <text class="guide-stat"><image src="/static/icons/love.png" mode="aspectFit" style="width:22rpx;height:22rpx;margin-right:4rpx;" /> {{ item.likes }}</text>
            </view>
          </view>
          <view class="guide-tags">
            <text class="guide-tag" v-for="tag in item.tags" :key="tag">{{ tag }}</text>
          </view>
        </view>
      </view>
    </view>

    <view class="activity-list" v-if="activeTab === 'activity'">
      <view class="activity-card card-shadow" v-for="item in activities" :key="item.id" @tap="viewActivity(item)">
        <view class="activity-header">
          <image class="activity-bg-img" :src="item.cover" mode="aspectFill" />
          <view class="activity-header-content">
            <text class="activity-title-header">{{ item.title }}</text>
            <view class="activity-status-badge" :class="item.status">{{ item.statusText }}</view>
          </view>
        </view>
        <view class="activity-body">
          <view class="activity-info-row">
            <image class="activity-info-icon" src="/static/icons/calendar.png" mode="aspectFit" />
            <view class="activity-time-content">
              <text class="activity-time-main">{{ (item.displayTime && item.displayTime.trim()) || (item.time && item.time.trim()) || '待定' }}</text>
            </view>
            <text class="activity-countdown" v-if="item.countdownText">{{ item.countdownText }}</text>
          </view>
          <view class="activity-info-row">
            <image class="activity-info-icon" src="/static/icons/position.png" mode="aspectFit" />
            <text class="activity-info-text">{{ item.location }}</text>
          </view>
          <view class="activity-info-row">
            <image class="activity-info-icon" src="/static/icons/mine.png" mode="aspectFit" />
            <text class="activity-info-text">已报名：{{ item.joinedCount }}/{{ item.maxCount }}</text>
          </view>
          <view class="activity-footer">
            <text class="activity-price">{{ item.price }}</text>
            <view class="activity-join-actions" v-if="item.joined && item.status !== 'ended'">
              <view class="joined-btn">
                <text>已报名</text>
              </view>
              <view class="cancel-join-btn" @tap.stop="handleActivityAction(item)">
                <text>取消报名</text>
              </view>
            </view>
            <view class="activity-join-btn" v-else-if="item.status !== 'ended'" @tap.stop="handleActivityAction(item)">
              <text>立即报名</text>
            </view>
            <view class="activity-disabled-btn" v-else>
              <text>活动已结束</text>
            </view>
          </view>
        </view>
      </view>
    </view>

    <!-- 发布弹窗 -->
    <view class="publish-modal" v-if="showPublish">
      <view class="publish-box">
        <view class="publish-modal-header">
          <text class="publish-modal-title">发布动态</text>
          <text class="publish-close" @tap="showPublish = false">✕</text>
        </view>
        <textarea class="publish-textarea" v-model="publishContent" placeholder="分享你在三江的旅行故事、美食体验、文化感受..." maxlength="500"></textarea>
        
        <view class="publish-image-area">
          <view class="publish-image-list">
            <view class="publish-image-item" v-for="(img, idx) in publishImages" :key="idx">
              <image class="publish-image-preview" :src="img" mode="aspectFill" />
              <view class="publish-image-delete" @tap="deleteImage(idx)">×</view>
            </view>
            <view class="publish-image-add" v-if="publishImages.length < 9" @tap="chooseImage">
              <text class="add-icon">+</text>
              <text class="add-text">添加图片</text>
            </view>
          </view>
        </view>
        
        <view class="publish-tags-select">
          <text class="publish-tags-label">添加标签</text>
          <view class="publish-tag-list">
            <view class="publish-tag-item" v-for="tag in publishTags" :key="tag" :class="{ selected: selectedTags.includes(tag) }" @tap="toggleTag(tag)">{{ tag }}</view>
          </view>
        </view>
        <view class="publish-actions">
          <view class="publish-cancel" @tap="showPublish = false">取消</view>
          <view class="publish-submit" @tap="submitPost">发布动态</view>
        </view>
      </view>
    </view>

    <!-- 攻略发布弹窗 -->
    <view class="publish-modal" v-if="showGuidePublish">
      <view class="publish-box">
        <view class="publish-modal-header">
          <text class="publish-modal-title">发布攻略</text>
          <text class="publish-close" @tap="showGuidePublish = false">✕</text>
        </view>

        <scroll-view scroll-y class="guide-form-scroll">
          <view class="guide-form-group">
            <text class="guide-form-label">攻略标题</text>
            <input class="guide-form-input" v-model="guideForm.title" placeholder="请输入攻略标题" maxlength="50" />
          </view>

          <view class="guide-form-group">
            <text class="guide-form-label">攻略简介</text>
            <textarea class="guide-form-textarea" v-model="guideForm.desc" placeholder="简要介绍这篇攻略的内容..." maxlength="200" :rows="2"></textarea>
          </view>

          <view class="guide-form-group">
            <text class="guide-form-label">封面图片</text>
            <view class="guide-cover-upload" @tap="chooseGuideCover">
              <image v-if="guideForm.cover" class="guide-cover-preview" :src="guideForm.cover" mode="aspectFill" />
              <view v-else class="guide-cover-placeholder">
                <text class="guide-cover-add">+</text>
                <text class="guide-cover-text">选择封面</text>
              </view>
            </view>
          </view>

          <view class="guide-form-group">
            <text class="guide-form-label">行程安排</text>
            <view class="guide-itinerary-list">
              <view class="guide-itin-item" v-for="(item, idx) in guideForm.itinerary" :key="idx">
                <text class="guide-itin-day">Day {{ idx + 1 }}</text>
                <input class="guide-itin-title" v-model="item.title" placeholder="标题" />
                <textarea class="guide-itin-desc" v-model="item.desc" placeholder="描述" :rows="2"></textarea>
                <text class="guide-itin-remove" @tap="removeGuideItinerary(idx)">移除</text>
              </view>
              <view class="guide-itin-add" @tap="addGuideItinerary">+ 添加行程</view>
            </view>
          </view>

          <view class="guide-form-group">
            <text class="guide-form-label">实用贴士</text>
            <view class="guide-tips-list">
              <view class="guide-tip-item" v-for="(tip, idx) in guideForm.tips" :key="idx">
                <input class="guide-tip-input" v-model="guideForm.tips[idx]" placeholder="输入贴士内容" />
                <text class="guide-tip-remove" @tap="guideForm.tips.splice(idx, 1)">×</text>
              </view>
              <view class="guide-tip-add" @tap="guideForm.tips.push('')">+ 添加贴士</view>
            </view>
          </view>

          <view class="guide-form-group">
            <text class="guide-form-label">标签（最多3个）</text>
            <view class="guide-tags-row">
              <view class="guide-tag-item" v-for="(tag, idx) in guideForm.tags" :key="idx">
                <text>{{ tag }}</text>
                <text class="guide-tag-remove" @tap="guideForm.tags.splice(idx, 1)">×</text>
              </view>
            </view>
            <view class="guide-tag-input-row">
              <input class="guide-tag-input" v-model="guideNewTag" placeholder="输入标签后回车" @confirm="addGuideTag" />
            </view>
          </view>

          <view style="height: 40rpx;"></view>
        </scroll-view>

        <view class="publish-actions">
          <view class="publish-cancel" @tap="showGuidePublish = false">取消</view>
          <view class="publish-submit" @tap="submitGuide">发布攻略</view>
        </view>
      </view>
    </view>

    <!-- 活动报名弹窗 -->
    <view class="join-modal" v-if="showJoinModal">
      <view class="join-box">
        <view class="join-header">
          <text class="join-title">活动报名</text>
          <text class="join-close" @tap="closeJoinModal">✕</text>
        </view>
        <view class="join-form">
          <view class="join-form-item">
            <text class="join-label">姓名</text>
            <input class="join-input" v-model="joinForm.name" placeholder="请输入真实姓名" />
          </view>
          <view class="join-form-item">
            <text class="join-label">手机号</text>
            <input class="join-input" v-model="joinForm.phone" placeholder="请输入手机号" type="number" />
          </view>
          <view class="join-tip">
            <text>请填写真实信息，用于活动签到</text>
          </view>
        </view>
        <view class="join-actions">
          <view class="join-cancel" @tap="closeJoinModal">取消</view>
          <view class="join-confirm" @tap="confirmJoin">确认报名</view>
        </view>
      </view>
    </view>
  </view>
</template>

<script>
import { getRandomAvatar, getUserInfo, requireLogin, checkLoginStatus } from '../../utils/auth.js'
import { favoritesUtil } from '../../utils/favorites.js'
import { followUtil } from '../../utils/follow.js'
import { postApi, sensitiveWordApi, uploadApi, activityApi, guideApi } from '../../utils/api'

export default {
  data() {
    return {
      countdownTimer: null,
      activeTab: 'community',
      tabs: [
        { id: 'community', label: '社区动态' },
        { id: 'myposts', label: '我的发布' },
        { id: 'guide', label: '旅行攻略' },
        { id: 'activity', label: '活动报名' }
      ],
      posts: [],
      myPosts: [],
      myGuides: [],
      myPostsType: 'posts',
      guides: [],
      activities: [],
      showPublish: false,
      publishContent: '',
      publishImages: [],
      publishTags: ['程阳八寨', '侗族大歌', '美食', '风雨桥', '鼓楼', '侗锦', '茶园', '古镇'],
      selectedTags: [],
      showGuidePublish: false,
      guideForm: { title: '', desc: '', cover: '', itinerary: [{ title: '', desc: '' }], tips: [''], tags: [] },
      guideNewTag: '',
      // 活动报名
      showJoinModal: false,
      currentActivity: null,
      joinForm: { name: '', phone: '' }
    }
  },
  onLoad() {
    this.loadPosts()
    this.loadActivities()
    this.loadGuides()
  },
  onShow() {
    // 检查是否有来自订单的待发布评论
    const pending = uni.getStorageSync('pendingReview')
    if (pending) {
      uni.removeStorageSync('pendingReview')
      this.publishContent = pending.content || ''
      // 尝试匹配已有标签
      if (pending.tag) {
        const matched = this.publishTags.find(t => pending.tag.includes(t))
        this.selectedTags = matched ? [matched] : []
      }
      this.showPublish = true
    }
    // 启动倒计时定时器
    this.startCountdownTimer()
  },
  onHide() {
    // 隐藏页面时停止定时器
    this.stopCountdownTimer()
  },
  onUnload() {
    // 卸载页面时停止定时器
    this.stopCountdownTimer()
  },
  methods: {
    switchTab(tabId) {
      this.activeTab = tabId
      if (tabId === 'myposts') {
        this.loadMyPosts()
        this.loadMyGuides()
      }
      if (tabId === 'activity') {
        this.loadActivities()
      }
      if (tabId === 'guide') {
        this.loadGuides()
      }
    },
    async handlePublish() {
      if (!requireLogin('发布内容')) return
      const valid = await checkLoginStatus()
      if (!valid) return
      this.showPublish = true
    },
    chooseImage() {
      const maxCount = 9 - this.publishImages.length
      uni.chooseImage({
        count: maxCount,
        sizeType: ['compressed'],
        sourceType: ['album', 'camera'],
        success: (res) => {
          this.publishImages = this.publishImages.concat(res.tempFilePaths)
        }
      })
    },
    deleteImage(idx) {
      this.publishImages.splice(idx, 1)
    },
    async loadPosts() {
      try {
        const res = await postApi.getList(1, 20)
        const list = res.records || res.list || []
        this.posts = list.map(item => this.mapPost(item))
      } catch (e) {
        console.error('加载帖子失败，使用本地数据', e)
        this.posts = this.getMockPosts()
      }
    },
    async loadMyPosts() {
      const userInfo = uni.getStorageSync('userInfo') || {}
      const userId = userInfo.userId || userInfo.id
      if (!userId) {
        this.myPosts = []
        return
      }
      try {
        const res = await postApi.getMyPosts(userId, 1, 50)
        const list = res.records || res.list || []
        this.myPosts = list.map(item => {
          const mapped = this.mapPost(item)
          if (item.status === 0) {
            mapped.statusText = '待审核'
            mapped.status = 0
          } else if (item.status === 1) {
            mapped.statusText = '已通过'
            mapped.status = 1
          } else if (item.status === 2) {
            mapped.statusText = '未通过'
            mapped.status = 2
          }
          return mapped
        })
      } catch (e) {
        console.error('加载我的帖子失败', e)
        this.myPosts = []
      }
    },
    async loadMyGuides() {
      const userInfo = uni.getStorageSync('userInfo') || {}
      const userId = userInfo.userId || userInfo.id
      if (!userId) {
        this.myGuides = []
        return
      }
      try {
        // 先加载所有攻略，然后过滤出当前用户的
        const res = await guideApi.getList(1, 100)
        const list = res.records || res.list || []
        this.myGuides = list.filter(item => {
          // 过滤出当前用户的攻略
          return item.userId === userId || item.user_id === userId
        }).map(item => {
          let cover = item.cover || ''
          if (typeof cover !== 'string' || cover === 'false' || cover === 'undefined') {
            cover = ''
          }
          const guide = {
            id: item.id,
            title: item.title || '',
            desc: item.desc || '',
            author: item.author || userInfo.nickname || '',
            authorAvatar: item.authorAvatar || item.author_avatar || userInfo.avatar || '',
            views: this.formatViews(item.views || 0),
            likes: item.likes || 0,
            hot: item.hot === 1,
            cover: cover,
            tags: this.parseTags(item.tags),
            userId: item.userId || item.user_id
          }
          // 设置审核状态 - 支持多种字段名
          const auditStatus = item.auditStatus !== undefined ? item.auditStatus : 
                             (item.status !== undefined ? item.status : 1)
          if (auditStatus === 0 || auditStatus === 'pending' || auditStatus === '待审核') {
            guide.statusText = '待审核'
            guide.status = 0
          } else if (auditStatus === 1 || auditStatus === 'approved' || auditStatus === '已通过') {
            guide.statusText = '已通过'
            guide.status = 1
          } else if (auditStatus === 2 || auditStatus === 'rejected' || auditStatus === '未通过') {
            guide.statusText = '未通过'
            guide.status = 2
          } else {
            guide.statusText = '已通过'
            guide.status = 1
          }
          return guide
        })
      } catch (e) {
        console.error('加载我的攻略失败', e)
        // 如果加载失败，尝试从本地存储读取攻略
        try {
          const localGuides = uni.getStorageSync('myGuides') || []
          this.myGuides = localGuides.filter(item => item.userId === userId)
        } catch (localErr) {
          this.myGuides = []
        }
      }
    },
    mapPost(item) {
      let images = []
      if (item.images) {
        try {
          images = typeof item.images === 'string' ? JSON.parse(item.images) : item.images
          if (!Array.isArray(images)) images = []
        } catch (e) {
          images = []
        }
      }
      images = images.map(img => typeof img === 'string' && img.startsWith('http://') ? img.replace('http://', 'https://') : img)
      // 确保所有字段是有效字符串，防止 false/undefined 导致图片加载错误
      let avatar = item.avatar || ''
      if (typeof avatar !== 'string' || avatar === 'false' || avatar === 'undefined') {
        avatar = ''
      }
      let coverImg = item.coverImg || item.cover_img || ''
      if (typeof coverImg !== 'string' || coverImg === 'false' || coverImg === 'undefined') {
        coverImg = ''
      }
      return {
        id: item.id,
        userId: item.userId || item.user_id,
        author: item.nickname || item.author,
        avatar: avatar,
        time: this.formatTime(item.createTime || item.create_time || item.createdAt),
        content: item.content,
        coverImg: coverImg,
        images: images,
        likes: item.likes || 0,
        comments: item.comments || 0,
        tags: this.parseTags(item.tags),
        liked: false,
        followed: false,
        status: item.status,
        statusText: ''
      }
    },
    getMockPosts() {
      return [
        { id: 1, author: '侗乡旅行者', avatar: 'https://cdn.phototourl.com/free/2026-03-24-9da6f2f7-f9d3-46ac-9819-695e797b76e3.jpg', time: '3天前', content: '程阳八寨真的太美了！清晨的风雨桥在薄雾中若隐若现，侗族大歌的歌声在山谷间回荡，感觉时间都慢下来了。强烈推荐大家来体验！', coverImg: 'https://cdn.phototourl.com/uploads/2026-03-19-67bbd771-fa02-4207-9437-87afc2c36bc7.jpg', likes: 128, comments: 23, tags: ['程阳八寨', '风雨桥', '侗族大歌'], liked: false, followed: false },
        { id: 2, author: '文化探索者', avatar: 'https://cdn.phototourl.com/member/2026-03-24-3cd4dc5f-9c84-4b60-b074-1dba51407d7e.png', time: '5天前', content: '在侗族博物馆学习了侗锦编织，才知道每一块侗锦背后都有那么多故事。传承人阿姨说一块侗锦要织几个月，真的是"穿在身上的史诗"！', coverImg: 'https://cdn.phototourl.com/free/2026-03-23-83a39e17-b2f9-4ff8-bced-41e9c064e9fe.jpg', likes: 96, comments: 15, tags: ['侗锦', '非遗体验', '文化传承'], liked: false, followed: false },
        { id: 3, author: '美食达人小李', avatar: 'https://cdn.phototourl.com/member/2026-03-24-f1ef810e-bb9a-439b-bc19-526382cc971b.jpg', time: '1周前', content: '三江的侗族油茶真的会上瘾！第一次喝觉得有点苦，但越喝越香，配上糯米饭和腌鱼，这就是侗族人的幸福早餐。回家后一直在想念这个味道！', coverImg: 'https://cdn.phototourl.com/member/2026-03-26-7e2a6250-6f30-490e-861b-e295b4732345.jpg', likes: 203, comments: 41, tags: ['侗族油茶', '美食', '三江特色'], liked: false, followed: false },
        { id: 4, author: '摄影师老王', avatar: 'https://cdn.phototourl.com/member/2026-03-24-1c287f60-cae7-454e-b829-926032cddd96.png', time: '2周前', content: '布央茶园的云海日出，是我拍过最美的风景之一。凌晨5点爬起来，在寒风中等待，当第一缕阳光穿透云海照在茶园上，所有的等待都值了！', coverImg: 'https://cdn.phototourl.com/free/2026-03-23-5bbeb559-132b-460d-a3f1-043d32170ed2.png', likes: 312, comments: 56, tags: ['布央茶园', '云海', '摄影'], liked: false, followed: false }
      ]
    },
    async loadGuides() {
      try {
        const res = await guideApi.getList(1, 20, 1)
        const list = res.records || res.list || []
        this.guides = list.map(item => {
          let cover = item.cover || ''
          if (typeof cover !== 'string' || cover === 'false' || cover === 'undefined') {
            cover = ''
          }
          return {
            id: item.id,
            title: item.title || '',
            desc: item.desc || '',
            author: item.author || '',
            authorAvatar: item.authorAvatar || item.author_avatar || '',
            views: this.formatViews(item.views || 0),
            likes: item.likes || 0,
            hot: item.hot === 1,
            cover: cover,
            tags: this.parseTags(item.tags),
            status: item.auditStatus !== undefined ? item.auditStatus : item.status,
            userId: item.userId
          }
        })
      } catch (e) {
        console.error('加载攻略列表失败，使用本地数据', e)
        this.guides = this.getMockGuides()
      }
    },
    getMockGuides() {
      return [
        { id: 1, title: '程阳八寨深度游攻略', desc: '从交通、住宿到景点游览，一篇文章搞定程阳八寨之旅', author: '侗乡旅行者', authorAvatar: 'https://cdn.phototourl.com/free/2026-03-24-955d494f-d01c-4911-87ea-2e684f9a3f29.jpg', views: '2.3w', likes: 1256, hot: true, cover: 'https://cdn.phototourl.com/member/2026-03-28-436b0a1e-48ea-41ca-85f4-7fd66ca349fb.jpg', tags: ['程阳八寨', '深度游', '攻略'] },
        { id: 2, title: '三江侗族非遗文化体验指南', desc: '侗族大歌、侗锦编织、油茶制作，全方位体验侗族非遗文化', author: '文化探索者', authorAvatar: 'https://cdn.phototourl.com/free/2026-03-24-9da6f2f7-f9d3-46ac-9819-695e797b76e3.jpg', views: '1.8w', likes: 986, hot: true, cover: 'https://cdn.phototourl.com/free/2026-03-23-75376aa0-ba75-4269-8839-bbe6aca040a6.jpg', tags: ['非遗文化', '体验', '侗族'] },
        { id: 3, title: '布央茶园云海日出攻略', desc: '最佳拍摄位置、最佳时间、交通住宿全攻略', author: '摄影师老王', authorAvatar: 'https://cdn.phototourl.com/member/2026-03-24-3cd4dc5f-9c84-4b60-b074-1dba51407d7e.png', views: '1.2w', likes: 756, hot: false, cover: 'https://cdn.phototourl.com/free/2026-03-23-5bbeb559-132b-460d-a3f1-043d32170ed2.png', tags: ['布央茶园', '摄影', '云海'] },
        { id: 4, title: '三江美食地图', desc: '侗族腌鱼、油茶、糯米饭...三江必吃美食全收录', author: '美食达人', authorAvatar: 'https://cdn.phototourl.com/member/2026-03-24-1c287f60-cae7-454e-b829-926032cddd96.png', views: '9800', likes: 623, hot: false, cover: 'https://cdn.phototourl.com/member/2026-04-16-f52eb4e0-2a5b-4ba0-a6d0-846b371d0c6c.jpg', tags: ['美食', '三江', '侗族'] }
      ]
    },
    formatViews(views) {
      const num = Number(views) || 0
      if (num >= 10000) return (num / 10000).toFixed(1) + 'w'
      if (num >= 1000) return (num / 1000).toFixed(1) + 'k'
      return String(num)
    },
    async loadActivities() {
      try {
        const res = await activityApi.getList(1, 20)
        const list = res.records || res.list || []
        this.activities = list.map(item => {
          // 使用后台的 startTime 和 endTime 字段
          let displayTime = ''
          if (item.startTime && item.endTime) {
            displayTime = this.formatActivityTimeRange(item.startTime, item.endTime)
          } else if (item.startTime) {
            displayTime = this.formatActivityTime(item.startTime)
          } else if (item.endTime) {
            displayTime = this.formatActivityTime(item.endTime)
          } else {
            // 兼容性处理，使用旧字段
            displayTime = this.formatActivityTime(item.activityTime || item.activity_time || item.time || '')
          }
          
          // 确保 cover 是有效 URL 字符串
          let cover = item.cover || item.coverImg || item.cover_img || ''
          if (typeof cover !== 'string' || cover === 'false' || cover === 'undefined') {
            cover = ''
          }
          
          return {
            id: item.id,
            title: item.title || '',
            time: item.startTime || item.activityTime || item.activity_time || item.time || '',
            displayTime: displayTime,
            countdownText: '',
            location: item.location || '',
            price: item.price ? `¥${item.price}/人` : '免费',
            cover: cover,
            color: '#1D3B5A',
            status: this.mapActivityStatus(item.status),
            statusText: this.mapActivityStatusText(item.status),
            joined: false,
            joinedCount: item.currentParticipants || item.current_participants || 0,
            maxCount: item.maxParticipants || item.max_participants || '不限'
          }
        })
        this.checkJoinedActivities()
        // 为每个活动计算倒计时（基于 startTime）
        this.activities.forEach(activity => {
          this.calculateActivityCountdown(activity)
        })
      } catch (e) {
        console.error('加载活动列表失败，使用本地数据', e)
        this.activities = this.getMockActivities()
        // 为模拟数据也计算倒计时
        this.activities.forEach(activity => {
          this.calculateActivityCountdown(activity)
        })
      }
    },
    getMockActivities() {
      return [
        { id: 1, title: '侗族大歌演唱体验活动', time: '2026年4月15日 14:00', displayTime: '2026年4月15日 14:00', countdownText: '', location: '程阳八寨鼓楼广场', price: '免费', cover: 'https://cdn.phototourl.com/free/2026-03-23-75376aa0-ba75-4269-8839-bbe6aca040a6.jpg', color: '#1D3B5A', status: 'ongoing', statusText: '报名中', joined: false, joinedCount: 12, maxCount: 50 },
        { id: 2, title: '侗锦编织工坊体验', time: '2026年4月20日 10:00', displayTime: '2026年4月20日 10:00', countdownText: '', location: '三江侗族生态博物馆', price: '¥68/人', cover: 'https://cdn.phototourl.com/member/2026-03-28-728dd46b-d4ad-4663-9052-6ea835a1c4d1.jpg', color: '#6B4423', status: 'ongoing', statusText: '报名中', joined: false, joinedCount: 8, maxCount: 30 },
        { id: 3, title: '布央茶园春季采茶节', time: '2026年4月30日', displayTime: '2026年4月30日', countdownText: '', location: '布央仙人山茶园', price: '¥128/人', cover: 'https://cdn.phototourl.com/free/2026-03-23-5bbeb559-132b-460d-a3f1-043d32170ed2.png', color: '#2D5A27', status: 'upcoming', statusText: '即将开始', joined: false, joinedCount: 24, maxCount: 100 },
        { id: 4, title: '三江侗族芦笙节', time: '2026年农历正月十五', displayTime: '2026年农历正月十五', countdownText: '', location: '三江县城广场', price: '免费', cover: 'https://cdn.phototourl.com/free/2026-03-23-4413c254-1f17-46a6-a102-78ffbd9c05bc.jpg', color: '#8B3252', status: 'upcoming', statusText: '即将开始', joined: false, joinedCount: 36, maxCount: 200 }
      ]
    },
    mapActivityStatus(status) {
      // 支持字符串状态和数字状态
      if (typeof status === 'string') {
        const map = { '报名中': 'ongoing', '进行中': 'ongoing', '即将开始': 'upcoming', '已结束': 'ended' }
        return map[status] || 'ongoing'
      }
      const map = { 0: 'upcoming', 1: 'ongoing', 2: 'ended' }
      return map[status] || 'ongoing'
    },
    mapActivityStatusText(status) {
      // 支持字符串状态和数字状态
      if (typeof status === 'string') {
        return status
      }
      const map = { 0: '即将开始', 1: '报名中', 2: '已结束' }
      return map[status] || '报名中'
    },
    formatDisplayTime(displayTime, time) {
      if (displayTime && displayTime.trim()) return displayTime
      if (time && time.trim()) return time
      return '待定'
    },
    // 格式化活动时间显示
    formatActivityTime(timeStr) {
      if (!timeStr) return '待定'
      // 处理常见的时间格式
      // 格式示例：2026年4月15日 14:00
      if (timeStr.includes('年') && timeStr.includes('月')) {
        return timeStr
      }
      // 格式示例：2026-04-15 14:00
      const dateMatch = timeStr.match(/(\d{4})[-/](\d{1,2})[-/](\d{1,2})(?:\s+(\d{1,2}):(\d{1,2}))?/)
      if (dateMatch) {
        const [, year, month, day, hour, minute] = dateMatch
        let result = `${year}年${month}月${day}日`
        if (hour !== undefined && minute !== undefined) {
          result += ` ${hour}:${minute.padStart(2, '0')}`
        }
        return result
      }
      return timeStr
    },
    // 格式化活动时间范围显示
    formatActivityTimeRange(startTimeStr, endTimeStr) {
      if (!startTimeStr && !endTimeStr) return '待定'
      
      const formatDate = (timeStr) => {
        if (!timeStr) return ''
        if (timeStr.includes('年') && timeStr.includes('月')) {
          return timeStr
        }
        const dateMatch = timeStr.match(/(\d{4})[-/](\d{1,2})[-/](\d{1,2})(?:\s+(\d{1,2}):(\d{1,2}))?/)
        if (dateMatch) {
          const [, year, month, day, hour, minute] = dateMatch
          let result = `${year}年${month}月${day}日`
          if (hour !== undefined && minute !== undefined) {
            result += ` ${hour}:${minute.padStart(2, '0')}`
          }
          return result
        }
        return timeStr
      }
      
      const start = formatDate(startTimeStr)
      const end = formatDate(endTimeStr)
      
      if (start && end) {
        return `${start} - ${end}`
      } else if (start) {
        return start
      } else {
        return end
      }
    },
    // 解析时间字符串为Date对象
    parseActivityTime(timeStr) {
      if (!timeStr) return null
      // 格式1：2026年4月15日 14:00
      const chineseMatch = timeStr.match(/(\d{4})年(\d{1,2})月(\d{1,2})日(?:\s+(\d{1,2}):(\d{1,2}))?/)
      if (chineseMatch) {
        const [, year, month, day, hour = 10, minute = 0] = chineseMatch
        return new Date(parseInt(year), parseInt(month) - 1, parseInt(day), parseInt(hour), parseInt(minute))
      }
      // 格式2：2026-04-15 14:00
      const standardMatch = timeStr.match(/(\d{4})[-/](\d{1,2})[-/](\d{1,2})(?:\s+(\d{1,2}):(\d{1,2}))?/)
      if (standardMatch) {
        const [, year, month, day, hour = 10, minute = 0] = standardMatch
        return new Date(parseInt(year), parseInt(month) - 1, parseInt(day), parseInt(hour), parseInt(minute))
      }
      // 尝试直接解析
      const date = new Date(timeStr)
      if (!isNaN(date.getTime())) {
        return date
      }
      return null
    },
    // 计算活动倒计时
    calculateActivityCountdown(activity) {
      const activityDate = this.parseActivityTime(activity.time)
      if (!activityDate) {
        activity.countdownText = ''
        return
      }
      
      const now = new Date()
      const diff = activityDate - now
      
      if (diff <= 0) {
        if (activity.status === 'ended') {
          activity.countdownText = '活动已结束'
        } else {
          activity.countdownText = '活动进行中'
        }
        return
      }
      
      const days = Math.floor(diff / (1000 * 60 * 60 * 24))
      const hours = Math.floor((diff % (1000 * 60 * 60 * 24)) / (1000 * 60 * 60))
      const minutes = Math.floor((diff % (1000 * 60 * 60)) / (1000 * 60))
      
      if (days > 0) {
        activity.countdownText = `距开始${days}天${hours}小时`
      } else if (hours > 0) {
        activity.countdownText = `距开始${hours}小时${minutes}分钟`
      } else if (minutes > 0) {
        activity.countdownText = `距开始${minutes}分钟`
      } else {
        activity.countdownText = '活动即将开始'
      }
    },
    // 启动倒计时定时器
    startCountdownTimer() {
      this.stopCountdownTimer()
      // 每分钟更新一次倒计时
      this.countdownTimer = setInterval(() => {
        if (this.activeTab === 'activity' && this.activities.length > 0) {
          this.activities.forEach(activity => {
            this.calculateActivityCountdown(activity)
          })
        }
      }, 60000)
    },
    // 停止倒计时定时器
    stopCountdownTimer() {
      if (this.countdownTimer) {
        clearInterval(this.countdownTimer)
        this.countdownTimer = null
      }
    },
    checkJoinedActivities() {
      const userInfo = uni.getStorageSync('userInfo') || {}
      const userId = userInfo.userId || userInfo.id
      if (!userId) return
      this.activities.forEach(item => {
        const joinedKey = `activity_joined_${item.id}_${userId}`
        item.joined = !!uni.getStorageSync(joinedKey)
      })
    },
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
    parseTags(tags) {
      if (!tags) return []
      if (Array.isArray(tags)) return tags
      try { return JSON.parse(tags) } catch { return tags.split(',').map(t => t.trim()).filter(Boolean) }
    },
    async likePost(item) {
      if (!requireLogin('点赞')) return
      const valid = await checkLoginStatus()
      if (!valid) return
      const newLiked = !item.liked
      item.liked = newLiked
      item.likes += newLiked ? 1 : -1
      postApi.like(item.id).catch(() => {
        item.liked = !newLiked
        item.likes += newLiked ? -1 : 1
        uni.showToast({ title: '操作失败', icon: 'none' })
      })
    },
    async followUser(item) {
      if (!requireLogin('关注')) return
      const valid = await checkLoginStatus()
      if (!valid) return
      followUtil.addFollow({
        userId: item.userId || item.authorId || item.id,
        nickname: item.nickname || item.author || '匿名用户',
        avatar: item.avatar || item.authorAvatar || ''
      })
      item.followed = true
    },
    sharePost(item) {
      uni.share({
        provider: 'weixin',
        type: 0,
        title: '寻侗三江 - ' + item.author + '的动态',
        content: item.content,
        success: () => { uni.showToast({ title: '分享成功', icon: 'success' }) },
        fail: () => { uni.showToast({ title: '分享取消', icon: 'none' }) }
      })
    },
    async toggleFavorite(item) {
      if (!requireLogin('收藏')) return
      const valid = await checkLoginStatus()
      if (!valid) return
      const postItem = {
        id: item.id,
        type: 'post',
        name: item.author + '的动态',
        desc: item.content,
        cover: item.coverImg,
        emoji: '📝'
      }
      favoritesUtil.toggleFavorite(postItem)
      item.favorited = !item.favorited
    },
    viewPost(item) { uni.navigateTo({ url: `/pages/discover/detail?id=${item.id}` }) },
    
    // ====================== 活动报名核心功能（完善版）======================
    async handleActivityAction(item) {
      // 活动已结束，不允许操作
      if (item.status === 'ended') {
        uni.showToast({ title: '活动已结束，无法报名或取消报名', icon: 'none' })
        return
      }

      if (item.joined) {
        uni.showModal({
          title: '取消报名',
          content: '确定要取消报名「' + item.title + '」吗？',
          confirmText: '确认取消',
          confirmColor: '#f56c6c',
          success: async (res) => {
            if (res.confirm) {
              await this.cancelActivity(item)
            }
          }
        })
      } else {
        await this.openJoinModal(item)
      }
    },

    // 打开报名弹窗
    async openJoinModal(item) {
      if (!requireLogin('报名活动')) return
      const valid = await checkLoginStatus()
      if (!valid) return

      const userInfo = uni.getStorageSync('userInfo') || {}
      this.currentActivity = item
      this.joinForm = {
        name: userInfo.name || userInfo.nickname || '',
        phone: userInfo.phone || ''
      }
      this.showJoinModal = true
    },

    // 确认报名提交
    async confirmJoin() {
      const { name, phone } = this.joinForm
      const item = this.currentActivity

      if (!name.trim()) {
        uni.showToast({ title: '请输入姓名', icon: 'none' })
        return
      }
      const phoneReg = /^\d{11}$/
      if (!phoneReg.test(phone)) {
        uni.showToast({ title: '请输入11位手机号', icon: 'none' })
        return
      }

      uni.showLoading({ title: '提交中...' })

      try {
        const userInfo = uni.getStorageSync('userInfo') || {}
        const userId = userInfo.userId || userInfo.id

        await activityApi.signup(item.id, {
          userId,
          name: name.trim(),
          phone: phone.trim()
        })

        // 本地状态更新
        item.joined = true
        item.joinedCount += 1
        const key = `activity_joined_${item.id}_${userId}`
        uni.setStorageSync(key, '1')

        uni.hideLoading()
        this.closeJoinModal()
        uni.showToast({ title: '报名成功', icon: 'success' })
      } catch (e) {
        console.error('报名失败', e)
        uni.hideLoading()
        uni.showToast({ title: '报名失败，请重试', icon: 'none' })
      }
    },

    // 取消报名
    async cancelActivity(item) {
      uni.showLoading({ title: '取消中...' })
      try {
        const userInfo = uni.getStorageSync('userInfo') || {}
        const userId = userInfo.userId || userInfo.id

        await activityApi.cancelSignup(item.id, { userId })

        // 本地更新
        item.joined = false
        item.joinedCount = Math.max(0, item.joinedCount - 1)
        const key = `activity_joined_${item.id}_${userId}`
        uni.removeStorageSync(key)

        uni.hideLoading()
        uni.showToast({ title: '已取消报名', icon: 'success' })
      } catch (e) {
        console.error('取消失败', e)
        uni.hideLoading()
        uni.showToast({ title: '取消失败', icon: 'none' })
      }
    },
    // ====================================================================

    viewActivity(item) {
      uni.navigateTo({ url: `/pages/discover/activity-detail?id=${item.id}` })
    },
    
    closeJoinModal() {
      this.showJoinModal = false
      this.currentActivity = null
      this.joinForm = { name: '', phone: '' }
    },
    viewGuide(item) {
      uni.navigateTo({ url: `/pages/discover/guide-detail?id=${item.id}` })
    },
    handlePublishGuide() {
      if (!requireLogin('发布攻略')) return
      this.guideForm = { title: '', desc: '', cover: '', itinerary: [{ title: '', desc: '' }], tips: [''], tags: [] }
      this.guideNewTag = ''
      this.showGuidePublish = true
    },
    chooseGuideCover() {
      uni.chooseImage({
        count: 1,
        sizeType: ['compressed'],
        sourceType: ['album', 'camera'],
        success: (res) => {
          this.guideForm.cover = res.tempFilePaths[0]
        }
      })
    },
    addGuideItinerary() {
      this.guideForm.itinerary.push({ title: '', desc: '' })
    },
    removeGuideItinerary(idx) {
      if (this.guideForm.itinerary.length > 1) {
        this.guideForm.itinerary.splice(idx, 1)
      }
    },
    addGuideTag() {
      const tag = this.guideNewTag.trim()
      if (tag && this.guideForm.tags.length < 3 && !this.guideForm.tags.includes(tag)) {
        this.guideForm.tags.push(tag)
      }
      this.guideNewTag = ''
    },
    async submitGuide() {
      const userInfo = uni.getStorageSync('userInfo') || {}
      const userId = userInfo.userId || userInfo.id
      if (!userId) {
        return uni.showModal({
          title: '提示',
          content: '请先登录',
          confirmText: '去登录',
          success: (res) => res.confirm && uni.navigateTo({ url: '/pages/login/login' })
        })
      }

      if (!this.guideForm.title.trim()) return uni.showToast({ title: '请输入标题', icon: 'none' })
      if (!this.guideForm.desc.trim()) return uni.showToast({ title: '请输入简介', icon: 'none' })

      try {
        const s = await sensitiveWordApi.check(this.guideForm.title + ' ' + this.guideForm.desc)
        if (s.hasSensitive) {
          return uni.showModal({ title: '包含敏感词', content: '请修改', showCancel: false })
        }
      } catch (e) {}

      uni.showLoading({ title: '发布中...' })
      let coverUrl = this.guideForm.cover
      const isServerUrl = coverUrl?.includes('/uploads/') || coverUrl?.includes('localhost:8080')
      if (coverUrl && !isServerUrl) {
        try {
          coverUrl = await uploadApi.uploadImage(coverUrl)
        } catch (e) {
          uni.hideLoading()
          return uni.showToast({ title: '封面上传失败', icon: 'none' })
        }
      }

      const data = {
        title: this.guideForm.title,
        desc: this.guideForm.desc,
        content: '',
        author: userInfo.nickname || '用户',
        authorAvatar: userInfo.avatar || '',
        cover: coverUrl || '',
        views: 0,
        tags: JSON.stringify(this.guideForm.tags),
        tips: JSON.stringify(this.guideForm.tips.filter(Boolean)),
        itinerary: JSON.stringify(this.guideForm.itinerary.filter(i => i.title)),
        hot: 0,
        status: 0,
        auditStatus: 0,
        userId: userId,
        publishTime: new Date().toISOString()
      }

      try {
        await guideApi.save(data)
        uni.hideLoading()
        this.showGuidePublish = false
        uni.showModal({ title: '提交成功', content: '等待审核', showCancel: false })
        
        // 保存到本地存储，确保"我的发布"能立即看到
        try {
          const localGuides = uni.getStorageSync('myGuides') || []
          localGuides.unshift({
            id: 'guide_' + Date.now(),
            title: this.guideForm.title,
            desc: this.guideForm.desc,
            cover: coverUrl || '',
            author: userInfo.nickname || '用户',
            authorAvatar: userInfo.avatar || '',
            views: 0,
            likes: 0,
            tags: this.guideForm.tags,
            tips: this.guideForm.tips.filter(Boolean),
            itinerary: this.guideForm.itinerary.filter(i => i.title),
            hot: 0,
            status: 0,
            auditStatus: 0,
            userId: userId,
            user_id: userId,
            publishTime: new Date().toISOString(),
            createTime: new Date().toISOString(),
            statusText: '待审核'
          })
          uni.setStorageSync('myGuides', localGuides)
        } catch (storageErr) {
          console.error('保存本地攻略失败', storageErr)
        }
        
        // 刷新页面数据
        this.loadGuides()
        if (this.activeTab === 'myposts' && this.myPostsType === 'guides') {
          this.loadMyGuides()
        }
      } catch (e) {
        // 后端保存失败，至少保存到本地
        try {
          const localGuides = uni.getStorageSync('myGuides') || []
          localGuides.unshift({
            id: 'guide_' + Date.now(),
            title: this.guideForm.title,
            desc: this.guideForm.desc,
            cover: coverUrl || '',
            author: userInfo.nickname || '用户',
            authorAvatar: userInfo.avatar || '',
            views: 0,
            likes: 0,
            tags: this.guideForm.tags,
            tips: this.guideForm.tips.filter(Boolean),
            itinerary: this.guideForm.itinerary.filter(i => i.title),
            hot: 0,
            status: 0,
            auditStatus: 0,
            userId: userId,
            user_id: userId,
            publishTime: new Date().toISOString(),
            createTime: new Date().toISOString(),
            statusText: '待审核'
          })
          uni.setStorageSync('myGuides', localGuides)
        } catch (storageErr) {
          console.error('保存本地攻略失败', storageErr)
        }
        
        uni.hideLoading()
        uni.showModal({ 
          title: '发布成功', 
          content: '已保存到本地，等待审核', 
          showCancel: false 
        })
        
        // 刷新页面数据
        this.loadGuides()
        if (this.activeTab === 'myposts' && this.myPostsType === 'guides') {
          this.loadMyGuides()
        }
      }
    },
    toggleTag(tag) {
      const i = this.selectedTags.indexOf(tag)
      if (i > -1) this.selectedTags.splice(i, 1)
      else if (this.selectedTags.length < 3) this.selectedTags.push(tag)
    },
    async submitPost() {
      if (!this.publishContent.trim()) return uni.showToast({ title: '请输入内容', icon: 'none' })
      try {
        const s = await sensitiveWordApi.check(this.publishContent)
        if (s.hasSensitive) {
          return uni.showModal({ title: '包含敏感词', content: '请修改', showCancel: false })
        }
      } catch (e) {}

      const userInfo = uni.getStorageSync('userInfo') || {}
      const userId = userInfo.userId || userInfo.id
      if (!userId) {
        return uni.showModal({
          title: '请先登录',
          confirmText: '去登录',
          success: (res) => res.confirm && uni.navigateTo({ url: '/pages/login/login' })
        })
      }

      uni.showLoading({ title: '发布中...' })
      const urls = []
      for (const img of this.publishImages) {
        try {
          urls.push(await uploadApi.uploadImage(img))
        } catch (e) {
          uni.hideLoading()
          return uni.showToast({ title: '图片上传失败', icon: 'none' })
        }
      }

      const postData = {
        user_id: userId,
        nickname: userInfo.nickname || '用户',
        avatar: userInfo.avatar || '',
        content: this.publishContent,
        cover_img: urls[0] || '',
        images: JSON.stringify(urls),
        tags: JSON.stringify(this.selectedTags)
      }

      try {
        await postApi.create(postData)
        this.publishContent = ''
        this.publishImages = []
        this.selectedTags = []
        this.showPublish = false
        uni.hideLoading()
        uni.showToast({ title: '发布成功，待审核', icon: 'success' })
        // 刷新所有标签页数据
        this.loadMyPosts()
      } catch (e) {
        uni.hideLoading()
        uni.showToast({ title: '发布失败', icon: 'none' })
      }
    }
  }
}
</script>

<style scoped lang="scss">
.discover-page { background: #F8FAFC; min-height: 100vh; padding-bottom: 40rpx; }
.card-shadow { box-shadow: 0 4rpx 20rpx rgba(74,144,226,0.12); }

.myposts-container { padding: 0; }
.myposts-tab-bar { display: flex; background: #fff; padding: 0 30rpx; border-bottom: 1rpx solid #E2E8F0; }
.myposts-tab-item { flex: 1; text-align: center; padding: 24rpx 0; font-size: 28rpx; color: #94A3B8; position: relative; }
.myposts-tab-item.active { color: #2C3E50; font-weight: 600; }
.myposts-tab-item.active::after { content: ''; position: absolute; bottom: 0; left: 50%; transform: translateX(-50%); width: 40rpx; height: 4rpx; background: #4A90E2; border-radius: 2rpx; }

.empty-my-posts { text-align: center; padding: 120rpx 0; }
.empty-text { font-size: 26rpx; color: #94A3B8; }

.status-tag { font-size: 22rpx; padding: 6rpx 16rpx; border-radius: 20rpx; }
.status-0 { color: #E6A23C; background: rgba(230,162,60,0.1); }
.status-1 { color: #67C23A; background: rgba(103,194,58,0.1); }
.status-2 { color: #F56C6C; background: rgba(245,108,108,0.1); }

.tab-bar { display: flex; background: #fff; padding: 0 30rpx; border-bottom: 1rpx solid #E2E8F0; }
.tab-item { flex: 1; text-align: center; padding: 28rpx 0; font-size: 28rpx; color: #94A3B8; position: relative; }
.tab-item.active { color: #2C3E50; font-weight: 600; }
.tab-item.active::after { content: ''; position: absolute; bottom: 0; left: 50%; transform: translateX(-50%); width: 40rpx; height: 4rpx; background: #4A90E2; border-radius: 2rpx; }

.publish-bar { margin: 20rpx 24rpx; background: #fff; border-radius: 50rpx; padding: 20rpx 24rpx; display: flex; align-items: center; gap: 16rpx; box-shadow: 0 4rpx 20rpx rgba(74,144,226,0.12); }
.publish-input-wrap { flex: 1; }
.publish-placeholder { font-size: 26rpx; color: #94A3B8; }
.publish-btn { background: linear-gradient(135deg, #1D3B5A, #2E5F8E); color: #fff; font-size: 26rpx; padding: 12rpx 30rpx; border-radius: 30rpx; font-weight: 600; box-shadow: 0 4rpx 12rpx rgba(29,59,90,0.3); }

.post-list { padding: 0 24rpx; display: flex; flex-direction: column; gap: 20rpx; }
.post-card { background: #fff; border-radius: 20rpx; padding: 24rpx; }
.post-header { display: flex; align-items: center; gap: 16rpx; margin-bottom: 16rpx; }
.post-avatar-img { width: 72rpx; height: 72rpx; border-radius: 50%; border: 2rpx solid rgba(74,144,226,0.2); flex-shrink: 0; }
.post-meta { flex: 1; }
.post-author { display: block; font-size: 28rpx; font-weight: 600; color: #2C3E50; }
.post-time { display: block; font-size: 22rpx; color: #94A3B8; }
.follow-btn { font-size: 22rpx; color: #4A90E2; border: 1rpx solid #4A90E2; padding: 6rpx 20rpx; border-radius: 20rpx; }
.follow-btn.followed { color: #94A3B8; border-color: #E2E8F0; }
.post-content { font-size: 28rpx; color: #475569; line-height: 1.7; display: block; margin-bottom: 16rpx; }
.post-cover-img { width: 100%; height: 320rpx; border-radius: 16rpx; margin-bottom: 16rpx; }
.post-tags { display: flex; flex-wrap: wrap; gap: 8rpx; margin-bottom: 16rpx; }
.post-tag { font-size: 22rpx; color: #4A90E2; background: rgba(74,144,226,0.08); padding: 4rpx 14rpx; border-radius: 20rpx; }
.post-actions { display: flex; gap: 40rpx; padding-top: 16rpx; border-top: 1rpx solid #F1F5F9; }
.action-item { display: flex; align-items: center; gap: 8rpx; }
.action-icon { width: 26rpx; height: 26rpx; }
.action-count { font-size: 24rpx; color: #94A3B8; }

.guide-list { padding: 0 24rpx; display: flex; flex-direction: column; gap: 20rpx; }
.guide-card { background: #fff; border-radius: 20rpx; overflow: hidden; }
.guide-cover-wrap { position: relative; height: 220rpx; }
.guide-cover-img { width: 100%; height: 100%; }
.guide-cover-title { position: absolute; bottom: 20rpx; left: 20rpx; right: 20rpx; font-size: 28rpx; color: #fff; font-weight: 600; text-shadow: 0 2rpx 8rpx rgba(0,0,0,0.6); }
.guide-hot-badge { position: absolute; top: 16rpx; right: 16rpx; font-size: 22rpx; color: #fff; background: #4A90E2; padding: 6rpx 16rpx; border-radius: 20rpx; }
.guide-cover-wrap .status-tag { position: absolute; top: 16rpx; left: 16rpx; z-index: 10; }
.guide-info { padding: 20rpx; }
.guide-title { font-size: 30rpx; font-weight: 600; color: #2C3E50; display: block; margin-bottom: 10rpx; }
.guide-desc { font-size: 26rpx; color: #64748B; line-height: 1.6; display: block; margin-bottom: 16rpx; }
.guide-meta { display: flex; justify-content: space-between; align-items: center; margin-bottom: 12rpx; }
.guide-author-wrap { display: flex; align-items: center; gap: 10rpx; }
.guide-avatar-img { width: 48rpx; height: 48rpx; border-radius: 50%; }
.guide-author-name { font-size: 24rpx; color: #64748B; }
.guide-stats { display: flex; gap: 20rpx; }
.guide-stat { font-size: 22rpx; color: #94A3B8; }
.guide-tags { display: flex; flex-wrap: wrap; gap: 8rpx; }
.guide-tag { font-size: 22rpx; color: #4A90E2; background: rgba(74,144,226,0.08); padding: 4rpx 14rpx; border-radius: 20rpx; }

.activity-list { padding: 0 24rpx; display: flex; flex-direction: column; gap: 20rpx; }
.activity-card { background: #fff; border-radius: 20rpx; overflow: hidden; box-shadow: 0 4rpx 20rpx rgba(74,144,226,0.12); border: 1rpx solid #E8E8E8; }
.activity-header { position: relative; height: 280rpx; overflow: hidden; }
.activity-bg-img { position: absolute; inset: 0; width: 100%; height: 100%; }
.activity-header-content { position: absolute; inset: 0; display: flex; flex-direction: column; justify-content: flex-end; padding: 20rpx; background: linear-gradient(to bottom, rgba(0,0,0,0) 0%, rgba(0,0,0,0.6) 100%); }
.activity-title-header { display: block; font-size: 30rpx; font-weight: 700; color: #fff; margin-bottom: 10rpx; text-shadow: 0 2rpx 8rpx rgba(0,0,0,0.4); }
.activity-status-badge { display: inline-flex; align-items: center; font-size: 22rpx; color: #fff; padding: 6rpx 16rpx; border-radius: 20rpx; background: rgba(255,255,255,0.25); font-weight: 600; backdrop-filter: blur(10px); align-self: flex-start; }
.activity-status-badge.upcoming { background: #FF7F50; }
.activity-body { padding: 18rpx 22rpx; }
.activity-info-row {
  display: flex;
  align-items: center;
  gap: 10rpx;
  margin-bottom: 10rpx;
}
.activity-info-icon {
  width: 32rpx;
  height: 32rpx;
  flex-shrink: 0;
}
.activity-time-content {
  flex: 1;
  display: flex;
  align-items: center;
  gap: 12rpx;
  min-width: 0;
  flex-wrap: nowrap;
}
.activity-time-main {
  font-size: 26rpx;
  color: #334155;
  font-weight: 600;
  line-height: 1.4;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  min-width: 0;
  flex: 1;
}
.activity-countdown {
  flex-shrink: 0;
  font-size: 22rpx;
  color: #4A90E2;
  font-weight: 600;
  background: linear-gradient(135deg, rgba(74,144,226,0.08) 0%, rgba(74,144,226,0.02) 100%);
  padding: 4rpx 14rpx;
  border-radius: 14rpx;
  border: 1rpx solid rgba(74,144,226,0.15);
  line-height: 1.5;
  white-space: nowrap;
}
.activity-footer { display: flex; justify-content: space-between; align-items: center; margin-top: 14rpx; padding-top: 14rpx; border-top: 1rpx solid #F0F0F0; }
.activity-price { font-size: 32rpx; color: #2E5F8E; font-weight: 700; }
.activity-join-btn { 
  background: linear-gradient(135deg, #1D3B5A, #2E5F8E); 
  color: #fff; 
  font-size: 26rpx; 
  padding: 18rpx 36rpx; 
  border-radius: 40rpx; 
  font-weight: 700; 
  box-shadow: 0 4rpx 12rpx rgba(74,144,226,0.3); 
  display: flex; 
  align-items: center; 
  justify-content: center;
  transition: all 0.3s ease;
}
.activity-join-btn:active {
  transform: scale(0.95);
  box-shadow: 0 2rpx 6rpx rgba(74,144,226,0.3);
}
.activity-join-actions { 
  display: flex; 
  gap: 10rpx; 
}
.joined-btn { 
  flex: 1; 
  background: linear-gradient(135deg, #67c23a 0%, #529b34 100%); 
  color: #fff; 
  font-size: 24rpx; 
  padding: 16rpx 20rpx; 
  border-radius: 40rpx; 
  font-weight: 600; 
  box-shadow: 0 4rpx 12rpx rgba(103,194,58,0.3); 
  display: flex; 
  align-items: center; 
  justify-content: center;
  white-space: nowrap;
  min-width: 120rpx;
  transition: all 0.3s ease;
}
.joined-btn:active {
  transform: scale(0.95);
  box-shadow: 0 2rpx 6rpx rgba(103,194,58,0.3);
}
.cancel-join-btn { 
  flex: 1; 
  background: #fff; 
  color: #f56c6c; 
  font-size: 24rpx; 
  padding: 16rpx 20rpx; 
  border-radius: 40rpx; 
  font-weight: 600; 
  border: 2rpx solid #f56c6c; 
  display: flex; 
  align-items: center; 
  justify-content: center;
  white-space: nowrap;
  min-width: 120rpx;
  transition: all 0.3s ease;
}
.cancel-join-btn:active {
  transform: scale(0.95);
  background: #FEF0F0;
}

.activity-disabled-btn { 
  background: #E2E8F0; 
  color: #94A3B8; 
  font-size: 26rpx; 
  padding: 18rpx 36rpx; 
  border-radius: 40rpx; 
  font-weight: 600; 
  box-shadow: none; 
  display: flex; 
  align-items: center; 
  justify-content: center;
  border: none;
  cursor: not-allowed;
}

.publish-modal { position: fixed; top: 0; left: 0; right: 0; bottom: 0; background: rgba(0,0,0,0.5); display: flex; align-items: flex-end; z-index: 500; }
.publish-box { width: 100%; background: #fff; border-radius: 32rpx 32rpx 0 0; padding: 30rpx; }
.publish-modal-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 24rpx; }
.publish-modal-title { font-size: 34rpx; font-weight: 600; color: #2C3E50; }
.publish-close { font-size: 36rpx; color: #94A3B8; }
.publish-textarea { width: 100%; height: 200rpx; background: #F8FAFC; border-radius: 16rpx; padding: 20rpx; font-size: 28rpx; color: #334155; box-sizing: border-box; margin-bottom: 20rpx; }
.publish-image-area { margin-bottom: 20rpx; }
.publish-image-list { display: flex; flex-wrap: wrap; gap: 16rpx; }
.publish-image-item { width: 200rpx; height: 200rpx; position: relative; }
.publish-image-preview { width: 100%; height: 100%; border-radius: 12rpx; }
.publish-image-delete { position: absolute; top: -8rpx; right: -8rpx; width: 40rpx; height: 40rpx; background: #FF5252; color: #fff; border-radius: 50%; display: flex; align-items: center; justify-content: center; font-size: 28rpx; line-height: 1; }
.publish-image-add { width: 200rpx; height: 200rpx; background: #F8FAFC; border: 2rpx dashed #CBD5E1; border-radius: 12rpx; display: flex; flex-direction: column; align-items: center; justify-content: center; gap: 8rpx; }
.add-icon { font-size: 48rpx; color: #94A3B8; line-height: 1; }
.add-text { font-size: 22rpx; color: #94A3B8; }
.publish-tags-label { font-size: 26rpx; color: #64748B; display: block; margin-bottom: 12rpx; }
.publish-tag-list { display: flex; flex-wrap: wrap; gap: 12rpx; margin-bottom: 30rpx; }
.publish-tag-item { font-size: 24rpx; color: #64748B; background: #F8FAFC; padding: 8rpx 20rpx; border-radius: 20rpx; }
.publish-tag-item.selected { background: #4A90E2; color: #fff; }
.publish-actions { display: flex; gap: 20rpx; }
.publish-cancel { flex: 1; text-align: center; padding: 24rpx; background: #F1F5F9; border-radius: 50rpx; font-size: 28rpx; color: #64748B; }
.publish-submit { flex: 2; text-align: center; padding: 24rpx; background: linear-gradient(135deg, #1D3B5A, #2E5F8E); border-radius: 50rpx; font-size: 28rpx; color: #fff; font-weight: 600; box-shadow: 0 4rpx 12rpx rgba(29,59,90,0.3); }

.status-tag { font-size: 22rpx; padding: 6rpx 16rpx; border-radius: 20rpx; font-weight: 600; flex-shrink: 0; }
.status-0 { background: #FFF7E6; color: #E6A23C; border: 1rpx solid #F5D78E; }
.status-1 { background: #F0F9EB; color: #67C23A; border: 1rpx solid #C2E7B0; }
.status-2 { background: #FEF0F0; color: #F56C6C; border: 1rpx solid #FBC4C4; }
.empty-my-posts { text-align: center; padding: 120rpx 0; }
.empty-text { font-size: 28rpx; color: #999; }

.guide-form-scroll { height: 700rpx; padding: 0 10rpx; box-sizing: border-box; }
.guide-form-group { margin-bottom: 24rpx; }
.guide-form-label { font-size: 26rpx; color: #666; display: block; margin-bottom: 10rpx; font-weight: 600; }
.guide-form-input { width: 100%; padding: 20rpx; background: #F8FAFC; border-radius: 12rpx; font-size: 28rpx; box-sizing: border-box; }
.guide-form-textarea { width: 100%; padding: 20rpx; background: #F8FAFC; border-radius: 12rpx; font-size: 28rpx; box-sizing: border-box; min-height: 120rpx; }
.guide-cover-upload { width: 100%; height: 280rpx; border-radius: 16rpx; overflow: hidden; }
.guide-cover-preview { width: 100%; height: 100%; }
.guide-cover-placeholder { width: 100%; height: 100%; background: #F8FAFC; border: 2rpx dashed #CBD5E1; border-radius: 16rpx; display: flex; flex-direction: column; align-items: center; justify-content: center; }
.guide-cover-add { font-size: 64rpx; color: #94A3B8; line-height: 1; }
.guide-cover-text { font-size: 24rpx; color: #94A3B8; margin-top: 10rpx; }
.guide-itinerary-list { display: flex; flex-direction: column; gap: 16rpx; }
.guide-itin-item { background: #F8FAFC; border-radius: 12rpx; padding: 20rpx; }
.guide-itin-day { font-size: 22rpx; color: #D4A373; font-weight: 700; display: block; margin-bottom: 10rpx; }
.guide-itin-title { width: 100%; padding: 14rpx; background: #fff; border-radius: 8rpx; font-size: 26rpx; margin-bottom: 10rpx; box-sizing: border-box; }
.guide-itin-desc { width: 100%; padding: 14rpx; background: #fff; border-radius: 8rpx; font-size: 24rpx; box-sizing: border-box; min-height: 80rpx; }
.guide-itin-remove { font-size: 22rpx; color: #F56C6C; display: block; text-align: right; margin-top: 10rpx; }
.guide-itin-add { padding: 16rpx; background: #F8FAFC; border-radius: 12rpx; text-align: center; font-size: 26rpx; color: #4A90E2; }
.guide-tips-list { display: flex; flex-direction: column; gap: 12rpx; }
.guide-tip-item { display: flex; align-items: center; gap: 12rpx; }
.guide-tip-input { flex: 1; padding: 14rpx; background: #F8FAFC; border-radius: 8rpx; font-size: 26rpx; }
.guide-tip-remove { font-size: 36rpx; color: #F56C6C; flex-shrink: 0; width: 48rpx; text-align: center; }
.guide-tip-add { padding: 16rpx; background: #F8FAFC; border-radius: 12rpx; text-align: center; font-size: 26rpx; color: #4A90E2; }
.guide-tags-row { display: flex; flex-wrap: wrap; gap: 12rpx; margin-bottom: 12rpx; }
.guide-tag-item { display: flex; align-items: center; gap: 8rpx; padding: 8rpx 16rpx; background: rgba(74,144,226,0.1); border-radius: 20rpx; font-size: 24rpx; color: #4A90E2; }
.guide-tag-remove { font-size: 28rpx; color: #94A3B8; }
.guide-tag-input-row { display: flex; gap: 12rpx; }
.guide-tag-input { flex: 1; padding: 14rpx; background: #F8FAFC; border-radius: 8rpx; font-size: 26rpx; }

/* 活动报名弹窗样式 */
.join-modal {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0,0,0,0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 999;
}
.join-box {
  width: 86%;
  background: #fff;
  border-radius: 24rpx;
  padding: 30rpx;
  box-sizing: border-box;
}
.join-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 30rpx;
}
.join-title {
  font-size: 34rpx;
  font-weight: 600;
  color: #2C3E50;
}
.join-close {
  font-size: 36rpx;
  color: #94A3B8;
}
.join-form {
  margin-bottom: 30rpx;
}
.join-form-item {
  margin-bottom: 24rpx;
}
.join-label {
  font-size: 28rpx;
  color: #64748B;
  display: block;
  margin-bottom: 12rpx;
  font-weight: 600;
}
.join-input {
  width: 100%;
  padding: 28rpx 24rpx;
  background: #F8FAFC;
  border-radius: 16rpx;
  font-size: 30rpx;
  box-sizing: border-box;
  line-height: 1.5;
  height: auto;
  min-height: 88rpx;
}
.join-tip {
  font-size: 24rpx;
  color: #94A3B8;
  text-align: center;
  margin-top: 10rpx;
}
.join-actions {
  display: flex;
  gap: 20rpx;
}
.join-cancel {
  flex: 1;
  text-align: center;
  padding: 24rpx;
  background: #F1F5F9;
  border-radius: 50rpx;
  font-size: 28rpx;
  color: #64748B;
}
.join-confirm {
  flex: 2;
  text-align: center;
  padding: 24rpx;
  background: linear-gradient(135deg, #1D3B5A, #2E5F8E);
  border-radius: 50rpx;
  font-size: 28rpx;
  color: #fff;
  font-weight: 600;
  box-shadow: 0 4rpx 12rpx rgba(29,59,90,0.3);
}
</style>