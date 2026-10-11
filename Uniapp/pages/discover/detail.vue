<template>
  <view class="post-detail-page">
    <!-- 顶部导航栏 -->

    <!-- 帖子内容 -->
    <scroll-view class="content" scroll-y>
      <!-- 帖子头部 -->
      <view class="post-header">
        <view class="user-info">
          <image class="avatar" :src="post.avatar" mode="aspectFill"></image>
          <view class="user-details">
            <text class="username">{{ post.author }}</text>
            <text class="post-time">{{ formatTime(post.create_time || post.created_at) }}</text>
          </view>
        </view>
        <view class="post-content">
          <text class="content-text">{{ post.content }}</text>
        </view>
      </view>

      <!-- 帖子图片 -->
      <view class="post-images" v-if="post.images && post.images.length > 0">
        <view class="image-grid" :class="{ 'two-columns': post.images.length === 2, 'three-columns': post.images.length >= 3 }">
          <image 
            v-for="(image, index) in post.images" 
            :key="index" 
            :src="image" 
            mode="aspectFill" 
            class="post-image"
            @tap="previewImage(index)"
          ></image>
        </view>
      </view>

      <!-- 帖子标签 -->
      <view class="post-tags" v-if="post.tags && post.tags.length > 0">
        <view class="tag" v-for="(tag, index) in post.tags" :key="index">
          <text class="tag-text">{{ tag }}</text>
        </view>
      </view>

      <!-- 帖子互动 -->
      <view class="post-actions">
        <view class="action-item" @tap="toggleLike">
          <text class="action-icon">{{ post.liked ? '❤️' : '👍' }}</text>
          <text class="action-text" :class="{ 'liked': post.liked }">{{ post.likes }} 点赞</text>
        </view>
        <view class="action-item">
          <text class="action-icon">💬</text>
          <text class="action-text">{{ post.comments }} 评论</text>
        </view>
      </view>

      <!-- 评论区 -->
      <view class="comments-section">
        <view class="section-title">评论 ({{ comments.length }})</view>
        <view class="comment-item" v-for="comment in comments" :key="comment.id">
          <image class="comment-avatar" :src="comment.avatar" mode="aspectFill"></image>
          <view class="comment-content">
            <view class="comment-header">
              <text class="comment-author">{{ comment.nickname }}</text>
              <text class="comment-time">{{ formatTime(comment.create_time || comment.created_at) }}</text>
            </view>
            <text class="comment-text">{{ comment.content }}</text>
            <!-- 评论图片 -->
            <view class="comment-images" v-if="comment.images && comment.images.length > 0">
              <image 
                v-for="(image, index) in comment.images" 
                :key="index" 
                :src="image" 
                mode="aspectFill" 
                class="comment-image"
                @tap="previewCommentImage(comment.images, index)"
              ></image>
            </view>
            <view class="comment-actions">
              <text class="like-btn" :class="{ 'liked': comment.liked }" @tap="toggleCommentLike(comment)">👍 {{ comment.likes || 0 }}</text>
            </view>
          </view>
        </view>
        <view class="no-comments" v-if="comments.length === 0">
          <text>暂无评论，快来抢沙发吧！</text>
        </view>
      </view>
    </scroll-view>

    <!-- 评论输入框 -->
    <view class="comment-input-area">
      <input 
        class="comment-input" 
        v-model="commentContent" 
        placeholder="写下你的评论..."
        @confirm="submitComment"
      />
      <view class="send-btn" :class="{ active: commentContent.trim() }" @tap="submitComment">
        <text class="send-text">发送</text>
      </view>
    </view>
  </view>
</template>

<script>
import { getRandomAvatar, checkLoginStatus } from '../../utils/auth.js'
import { postApi, commentApi, userApi, sensitiveWordApi } from '../../utils/api.js'

export default {
  data() {
    return {
      post: {
        id: 0,
        author: '',
        avatar: '',
        content: '',
        images: [],
        tags: [],
        likes: 0,
        comments: 0,
        liked: false,
        create_time: ''
      },
      comments: [],
      commentContent: '',
      loading: false,
      currentUserId: null
    }
  },
  onLoad(option) {
    const postId = option.id
    if (postId) {
      this.post.id = parseInt(postId)
      this.loadPostDetail()
      this.loadComments()
      this.loadUserInfo()
    }
  },
  methods: {
    async loadUserInfo() {
      try {
        const userInfo = uni.getStorageSync('userInfo')
        if (userInfo && (userInfo.userId || userInfo.id)) {
          this.currentUserId = userInfo.userId || userInfo.id
        }
      } catch (e) {
        console.warn('获取用户信息失败', e)
      }
    },
    async loadPostDetail() {
      try {
        const res = await postApi.getDetail(this.post.id)
        if (res) {
          this.post = {
            id: res.id,
            author: res.author || res.nickname || '侗乡旅行者',
            avatar: res.avatar || getRandomAvatar(),
            content: res.content || '',
            images: this.parseJson(res.images, []),
            tags: this.parseJson(res.tags, []),
            likes: res.likes || 0,
            comments: res.comments || 0,
            liked: false,
            create_time: res.createTime || res.create_time || res.createdAt || ''
          }
        }
      } catch (e) {
        console.warn('加载帖子详情失败，使用默认数据', e)
        this.loadStaticPost(this.post.id)
      }
    },
    async loadComments() {
      this.loading = true
      try {
        const res = await commentApi.getList(this.post.id)
        if (res && res.records) {
          this.comments = res.records.map(c => ({
            id: c.id,
            nickname: c.nickname,
            avatar: c.avatar || getRandomAvatar(),
            content: c.content,
            images: [],
            likes: c.likes || 0,
            liked: false,
            created_at: c.createTime || c.create_time || c.createdAt || ''
          }))
        }
      } catch (e) {
        console.warn('加载评论失败', e)
        this.comments = []
      } finally {
        this.loading = false
      }
    },
    goBack() {
      uni.navigateBack()
    },
    toggleLike() {
      const userInfo = uni.getStorageSync('userInfo')
      if (!userInfo || !(userInfo.userId || userInfo.id)) {
        uni.navigateTo({ url: '/pages/login/login' })
        return
      }
      const newLiked = !this.post.liked
      this.post.liked = newLiked
      this.post.likes += newLiked ? 1 : -1
      postApi.like(this.post.id).catch(() => {
        this.post.liked = !newLiked
        this.post.likes += newLiked ? -1 : 1
        uni.showToast({ title: '操作失败', icon: 'none' })
      })
    },
    toggleCommentLike(comment) {
      const userInfo = uni.getStorageSync('userInfo')
      if (!userInfo || !(userInfo.userId || userInfo.id)) {
        uni.navigateTo({ url: '/pages/login/login' })
        return
      }
      const newLiked = !comment.liked
      comment.liked = newLiked
      comment.likes += newLiked ? 1 : -1
      commentApi.like(comment.id).catch(() => {
        comment.liked = !newLiked
        comment.likes += newLiked ? -1 : 1
        uni.showToast({ title: '操作失败', icon: 'none' })
      })
    },
    loadStaticPost(id) {
      const staticPosts = [
        {
          id: 1,
          author: '侗乡旅行者',
          avatar: getRandomAvatar(),
          content: '程阳八寨真的太美了！清晨的风雨桥在薄雾中若隐若现，侗族大歌的歌声在山谷间回荡，感觉时间都慢下来了。强烈推荐大家来体验！',
          images: ['https://cdn.phototourl.com/member/2026-04-16-8c087100-b848-49db-97ae-c5d0650744c4.png'],
          tags: ['程阳八寨', '风雨桥', '侗族大歌'],
          likes: 128,
          comments: 23,
          liked: false,
          create_time: '2026-03-03'
        },
        {
          id: 2,
          author: '文化探索者',
          avatar: getRandomAvatar(),
          content: '在侗族博物馆学习了侗锦编织，才知道每一块侗锦背后都有那么多故事。传承人阿姨说一块侗锦要织几个月，真的是"穿在身上的史诗"！',
          images: ['https://cdn.phototourl.com/free/2026-03-23-83a39e17-b2f9-4ff8-bced-41e9c064e9fe.jpg'],
          tags: ['侗锦', '非遗体验', '文化传承'],
          likes: 96,
          comments: 15,
          liked: false,
          create_time: '2026-03-03'
        },
        {
          id: 3,
          author: '美食达人小李',
          avatar: getRandomAvatar(),
          content: '三江的侗族油茶真的会上瘾！第一次喝觉得有点苦，但越喝越香，配上糯米饭和腌鱼，这就是侗族人的幸福早餐。回家后一直在想念这个味道！',
          images: ['https://cdn.phototourl.com/member/2026-04-16-f52eb4e0-2a5b-4ba0-a6d0-846b371d0c6c.jpg'],
          tags: ['侗族油茶', '美食', '三江特色'],
          likes: 203,
          comments: 41,
          liked: false,
          create_time: '2026-03-03'
        },
        {
          id: 4,
          author: '摄影师老王',
          avatar: getRandomAvatar(),
          content: '布央茶园的云海日出，是我拍过最美的风景之一。凌晨4 点爬起来，在寒风中等待，当第一缕阳光穿透云海照在茶园上，所有的等待都值了！',
          images: ['https://cdn.phototourl.com/free/2026-03-23-5bbeb559-132b-460d-a3f1-043d32170ed2.png'],
          tags: ['布央茶园', '云海', '摄影'],
          likes: 312,
          comments: 56,
          liked: false,
          create_time: '2026-03-03'
        }
      ]
      const staticPost = staticPosts.find(p => p.id === parseInt(id))
      if (staticPost) {
        this.post = staticPost
      }
    },
    async submitComment() {
      const content = this.commentContent.trim()
      if (!content) return

      // 验证后端用户状态（是否被禁用）
      const valid = await checkLoginStatus()
      if (!valid) {
        return uni.showModal({
          title: '提示',
          content: '请先登录后再发表评论',
          confirmText: '去登录',
          success: (res) => {
            if (res.confirm) {
              uni.navigateTo({ url: '/pages/login/login' })
            }
          }
        })
      }

      // 敏感词检测 - 仅提示，不阻止发布（后端会再次检测）
      try {
        const sensitiveResult = await sensitiveWordApi.check(content)
        if (sensitiveResult.hasSensitive) {
          console.warn('内容包含敏感词:', sensitiveResult.words)
        }
      } catch (e) {
        console.error('敏感词检测失败，继续发布', e)
      }

      try {
        const userInfo = uni.getStorageSync('userInfo') || {}
        const userId = userInfo.userId || userInfo.id
        const commentData = {
          user_id: userId,
          nickname: userInfo.nickname || '用户',
          avatar: userInfo.avatar || '',
          content: content,
          likes: 0
        }

        await commentApi.create(this.post.id, commentData)
        this.commentContent = ''
        this.post.comments++
        
        // 本地立即添加评论，让用户立即看到
        const tempComment = {
          id: Date.now(),
          nickname: userInfo.nickname || '用户',
          avatar: userInfo.avatar || getRandomAvatar(),
          content: content,
          images: [],
          likes: 0,
          liked: false,
          created_at: new Date().toISOString()
        }
        this.comments.unshift(tempComment)
        
        uni.showToast({ title: '评论发布成功', icon: 'success' })
        // 稍后重新加载评论列表，确保与后端一致
        setTimeout(() => {
          this.loadComments()
        }, 1000)
      } catch (e) {
        uni.showToast({ title: '评论失败，请重试', icon: 'none' })
      }
    },
    previewImage(index) {
      if (this.post.images && this.post.images.length > 0) {
        uni.previewImage({
          urls: this.post.images,
          current: this.post.images[index]
        })
      }
    },
    previewCommentImage(images, index) {
      if (images && images.length > 0) {
        uni.previewImage({
          urls: images,
          current: images[index]
        })
      }
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

      return date.toLocaleDateString('zh-CN', {
        year: 'numeric',
        month: '2-digit',
        day: '2-digit'
      })
    },
    parseJson(str, defaultValue) {
      try {
        return JSON.parse(str)
      } catch (error) {
        return defaultValue
      }
    }
  }
}
</script>

<style scoped lang="scss">
.post-detail-page {
  display: flex;
  flex-direction: column;
  height: 100vh;
  background: #F8F9FA;
}

/* 导航栏 */
.nav-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 88rpx;
  background: #1D3B5A;
  padding: 0 30rpx;
  position: sticky;
  top: 0;
  z-index: 100;
}

.nav-left {
  width: 80rpx;
}

.nav-back {
  font-size: 40rpx;
  color: #FDFCF8;
}

.nav-title {
  flex: 1;
  text-align: center;
  font-size: 32rpx;
  font-weight: bold;
  color: #FDFCF8;
}

.nav-right {
  width: 80rpx;
}

/* 内容区域 */
.content {
  flex: 1;
  padding: 24rpx;
}

/* 帖子头部 */
.post-header {
  background: #FFFFFF;
  border-radius: 20rpx;
  padding: 24rpx;
  margin-bottom: 20rpx;
  box-shadow: 0 2rpx 10rpx rgba(0, 0, 0, 0.05);
}

.user-info {
  display: flex;
  align-items: center;
  margin-bottom: 20rpx;
}

.avatar {
  width: 72rpx;
  height: 72rpx;
  border-radius: 50%;
  margin-right: 16rpx;
}

.user-details {
  flex: 1;
}

.username {
  display: block;
  font-size: 30rpx;
  font-weight: bold;
  color: #333333;
  margin-bottom: 4rpx;
}

.post-time {
  font-size: 22rpx;
  color: #999999;
}

.content-text {
  font-size: 28rpx;
  line-height: 1.6;
  color: #333333;
  white-space: pre-wrap;
}

/* 帖子图片 */
.post-images {
  margin: 20rpx 0;
}

.image-grid {
  display: flex;
  flex-wrap: wrap;
  gap: 12rpx;
}

.two-columns .post-image {
  width: calc(50% - 6rpx);
  aspect-ratio: 1;
  border-radius: 16rpx;
}

.three-columns .post-image {
  width: calc(33.333% - 8rpx);
  aspect-ratio: 1;
  border-radius: 16rpx;
}

.post-image {
  width: 100%;
  aspect-ratio: 16/9;
  border-radius: 16rpx;
  background: #F0F0F0;
}

/* 帖子标签 */
.post-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 12rpx;
  margin: 20rpx 0;
}

.tag {
  background: #F0EDE8;
  border-radius: 30rpx;
  padding: 8rpx 20rpx;
  border: 1rpx solid rgba(163, 59, 50, 0.15);
}

.tag-text {
  font-size: 24rpx;
  color: #A33B32;
}

/* 帖子互动 */
.post-actions {
  display: flex;
  gap: 40rpx;
  padding-top: 20rpx;
  border-top: 1rpx solid #F0F0F0;
}

.action-item {
  display: flex;
  align-items: center;
  gap: 8rpx;
}

.action-icon {
  font-size: 28rpx;
}

.action-text {
  font-size: 24rpx;
  color: #666666;
}

/* 评论区 */
.comments-section {
  background: #FFFFFF;
  border-radius: 20rpx;
  padding: 24rpx;
  box-shadow: 0 2rpx 10rpx rgba(0, 0, 0, 0.05);
}

.section-title {
  font-size: 30rpx;
  font-weight: bold;
  color: #333333;
  margin-bottom: 20rpx;
}

.comment-item {
  display: flex;
  gap: 16rpx;
  padding: 20rpx 0;
  border-bottom: 1rpx solid #F0F0F0;
}

.comment-item:last-child {
  border-bottom: none;
}

.comment-avatar {
  width: 60rpx;
  height: 60rpx;
  border-radius: 50%;
  flex-shrink: 0;
}

.comment-content {
  flex: 1;
}

.comment-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12rpx;
}

.comment-author {
  font-size: 26rpx;
  font-weight: bold;
  color: #333333;
}

.comment-time {
  font-size: 22rpx;
  color: #999999;
}

.comment-text {
  font-size: 26rpx;
  line-height: 1.5;
  color: #333333;
  margin-bottom: 12rpx;
  white-space: pre-wrap;
}

.comment-images {
  display: flex;
  gap: 12rpx;
  margin-bottom: 12rpx;
}

.comment-image {
  width: 120rpx;
  height: 120rpx;
  border-radius: 8rpx;
  background: #F0F0F0;
}

.comment-actions {
  display: flex;
  align-items: center;
}

.liked {
  color: #A33B32;
}

.like-btn.liked {
  color: #A33B32;
  font-weight: bold;
}

.no-comments {
  text-align: center;
  padding: 40rpx 0;
  color: #999999;
  font-size: 26rpx;
}

/* 评论输入框 */
.comment-input-area {
  display: flex;
  align-items: center;
  padding: 16rpx 24rpx;
  background: #FFFFFF;
  border-top: 1rpx solid #F0F0F0;
}

.comment-input {
  flex: 1;
  background: #F8F9FA;
  border-radius: 40rpx;
  padding: 16rpx 24rpx;
  font-size: 26rpx;
  margin-right: 16rpx;
}

.send-btn {
  width: 80rpx;
  height: 56rpx;
  border-radius: 28rpx;
  background: #CCCCCC;
  display: flex;
  align-items: center;
  justify-content: center;
}

.send-btn.active {
  background: linear-gradient(135deg, #1D3B5A, #2E5F8E);
}

.send-text {
  font-size: 24rpx;
  color: #FFFFFF;
  font-weight: bold;
}
</style>
