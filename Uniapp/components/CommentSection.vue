<template>
  <view class="comment-section">
    <!-- 加载状态 -->
    <view class="loading-section" v-if="loading">
      <text class="loading-text">评论加载中...</text>
    </view>

    <!-- 评论列表 -->
    <view class="comment-list" v-else>
      <view class="comment-item" v-for="comment in comments" :key="comment.id">
        <image class="comment-avatar" :src="comment.avatar || defaultAvatar" mode="aspectFill" />
        <view class="comment-content">
          <view class="comment-header">
            <text class="comment-name">{{ comment.nickname }}</text>
            <view class="comment-rating">
              <text v-for="i in (comment.rating || 5)" :key="i">⭐</text>
            </view>
          </view>
          <text class="comment-text">{{ comment.content }}</text>
          <view class="comment-footer">
            <text class="comment-time">{{ formatTime(comment.created_at || comment.createTime) }}</text>
            <view class="comment-actions">
              <text class="comment-action" @tap="likeComment(comment)">
                <image class="icon-img-sm" :src="comment.liked ? '/static/icons/love.png' : '/static/icons/收藏.png'" mode="aspectFit" /> {{ comment.likes || 0 }}
              </text>
              <text class="comment-action" @tap="onReply(comment)" v-if="showReply">
                <image class="icon-img-sm" src="/static/icons/评论.png" mode="aspectFit" /> 回复
              </text>
              <text class="comment-action delete-action" @tap="deleteComment(comment)" v-if="canDelete(comment)">
                <image class="icon-img-sm" src="/static/icons/setting.png" mode="aspectFit" /> 删除
              </text>
            </view>
          </view>
          <!-- 回复列表 -->
          <view class="reply-list" v-if="comment.replies && comment.replies.length > 0">
            <view class="reply-item" v-for="reply in comment.replies" :key="reply.id">
              <image class="reply-avatar" :src="reply.avatar || defaultAvatar" mode="aspectFill" />
              <view class="reply-content">
                <text class="reply-name">{{ reply.nickname }}</text>
                <text class="reply-text">{{ reply.content }}</text>
                <text class="reply-time">{{ formatTime(reply.created_at || reply.createTime) }}</text>
              </view>
            </view>
          </view>
        </view>
      </view>

      <view class="comment-empty" v-if="comments.length === 0 && !loading">
        <text class="empty-icon"><image class="icon-img-lg" src="/static/icons/评论.png" mode="aspectFit" /></text>
        <text class="empty-text">暂无评论，快来发表第一条评论吧！</text>
      </view>
    </view>

    <!-- 发表评论 -->
    <view class="comment-input-section">
      <!-- 回复提示 -->
      <view class="reply-hint" v-if="replyTo">
        <text class="reply-hint-text">回复 @{{ replyTo.nickname }}</text>
        <text class="reply-cancel" @tap="cancelReply">×</text>
      </view>

      <view class="rating-selector">
        <text class="rating-label">评分：</text>
        <view class="rating-stars">
          <text
            v-for="i in 5"
            :key="i"
            class="rating-star"
            :class="{ active: i <= newRating }"
            @tap="setRating(i)"
          >⭐</text>
        </view>
      </view>

      <view class="comment-input-wrap">
        <textarea
          class="comment-input"
          v-model="newComment"
          :placeholder="replyTo ? '回复 ' + replyTo.nickname + '...' : '分享你的游玩体验...'"
          :maxlength="500"
          :auto-height="true"
          @focus="onInputFocus"
        />
        <text class="comment-count">{{ newComment.length }}/500</text>
      </view>

      <view class="submit-row">
        <view class="char-count" :class="{ 'warning': newComment.length > 450 }">
          {{ newComment.length }}/500
        </view>
        <button class="comment-submit-btn" :disabled="!newComment.trim() || submitting" @tap="submitComment">
          <text v-if="submitting">发布中...</text>
          <view v-else class="submit-btn-content">
            <text>发表评论</text>
            <text class="send-icon">➤</text>
          </view>
        </button>
      </view>
    </view>
  </view>
</template>

<script>
import { commentApi, sensitiveWordApi } from '@/utils/api'
import { checkLoginStatus } from '@/utils/auth.js'

export default {
  props: {
    attractionId: {
      type: Number,
      required: true
    },
    postId: {
      type: Number,
      default: null
    },
    showReply: {
      type: Boolean,
      default: true
    }
  },
  data() {
    return {
      comments: [],
      newComment: '',
      newRating: 5,
      currentUserId: null,
      defaultAvatar: 'https://cdn.phototourl.com/member/2026-03-24-3cd4dc5f-9c84-4b60-b074-1dba51407d7e.png',
      loading: false,
      submitting: false,
      replyTo: null,
      userInfo: null
    }
  },
  mounted() {
    this.loadUserInfo()
    this.loadComments()
  },
  methods: {
    loadUserInfo() {
      this.userInfo = uni.getStorageSync('userInfo') || null
      this.currentUserId = this.userInfo?.userId || this.userInfo?.id || uni.getStorageSync('userId') || null
    },
    async loadComments() {
      this.loading = true
      // 如果有postId，从后端加载评论
      if (this.postId) {
        try {
          const res = await commentApi.getList(this.postId, 1, 50)
          const list = res.records || res.list || []
          this.comments = list.map(item => ({
            id: item.id,
            attraction_id: this.attractionId,
            user_id: item.userId,
            nickname: item.nickname,
            avatar: item.avatar || '',
            content: item.content,
            rating: item.rating || 5,
            likes: item.likes || 0,
            liked: false,
            created_at: item.createTime,
            replies: []
          }))
        } catch (e) {
          console.error('加载评论失败', e)
          // 回退到本地存储
          this.loadLocalComments()
        } finally {
          this.loading = false
        }
      } else {
        // 没有postId时使用本地存储（兼容旧逻辑）
        this.loadLocalComments()
        this.loading = false
      }
    },
    
    loadLocalComments() {
      const all = uni.getStorageSync('comments') || {}
      this.comments = (all[this.attractionId] || []).slice().reverse()
    },

    async submitComment() {
      if (!this.newComment.trim()) {
        return uni.showToast({ title: '请输入评论内容', icon: 'none' })
      }

      if (this.submitting) return
      this.submitting = true

      // 验证后端用户状态（是否被禁用）
      const valid = await checkLoginStatus()
      if (!valid) {
        this.submitting = false
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

      // 后端敏感词检测（失败不影响发布）
      try {
        const sensitiveResult = await sensitiveWordApi.check(this.newComment)
        if (sensitiveResult.hasSensitive) {
          this.submitting = false
          return uni.showModal({
            title: '内容包含敏感词',
            content: `检测到敏感词：${sensitiveResult.words.join('、')}`,
            showCancel: false,
            confirmText: '我知道了'
          })
        }
      } catch (e) {
        console.error('后端敏感词检测失败，继续发布', e)
      }

      if (!this.currentUserId) {
        this.submitting = false
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

      // 如果有postId，提交到后端
      if (this.postId) {
        try {
          const commentData = {
            user_id: this.currentUserId,
            nickname: this.userInfo?.nickname || '用户',
            avatar: this.userInfo?.avatar || '',
            content: this.newComment,
            rating: this.newRating
          }

          // 如果是回复，添加回复目标
          if (this.replyTo) {
            commentData.replyTo = this.replyTo.id
          }

          await commentApi.create(this.postId, commentData)

          this.newComment = ''
          this.newRating = 5
          this.replyTo = null

          uni.showToast({ title: '评论发布成功', icon: 'success' })
          this.loadComments()
        } catch (e) {
          console.error('发布评论失败', e)
          uni.showToast({ title: '发布失败，请重试', icon: 'none' })
        } finally {
          this.submitting = false
        }
      } else {
        // 保存到本地存储（兼容模式）
        const all = uni.getStorageSync('comments') || {}
        if (!all[this.attractionId]) all[this.attractionId] = []
        const comment = {
          id: Date.now(),
          attraction_id: this.attractionId,
          user_id: this.currentUserId,
          nickname: this.userInfo?.nickname || uni.getStorageSync('nickname') || '游客',
          avatar: this.userInfo?.avatar || uni.getStorageSync('avatar') || '',
          content: this.newComment,
          rating: this.newRating,
          likes: 0,
          liked: false,
          created_at: new Date().toISOString(),
          replies: []
        }
        all[this.attractionId].push(comment)
        uni.setStorageSync('comments', all)
        this.newComment = ''
        this.newRating = 5
        this.replyTo = null
        this.loadComments()
        uni.showToast({ title: '评论发布成功', icon: 'success' })
        this.submitting = false
      }
    },

    onReply(comment) {
      if (!this.currentUserId) {
        return uni.showModal({
          title: '提示',
          content: '请先登录后再回复评论',
          confirmText: '去登录',
          success: (res) => {
            if (res.confirm) {
              uni.navigateTo({ url: '/pages/login/login' })
            }
          }
        })
      }
      this.replyTo = comment
    },

    cancelReply() {
      this.replyTo = null
    },

    onInputFocus() {
      // 输入框获取焦点时的处理
    },

    async deleteComment(comment) {
      uni.showModal({
        title: '确认删除',
        content: '确定要删除这条评论吗？',
        success: async (res) => {
          if (res.confirm) {
            // 尝试从后端删除
            if (this.postId && comment.id && String(comment.id).length < 15) {
              try {
                await commentApi.delete(comment.id)
                uni.showToast({ title: '删除成功', icon: 'success' })
                this.loadComments()
                return
              } catch (e) {
                console.error('后端删除失败，尝试本地删除', e)
              }
            }

            // 本地删除
            const all = uni.getStorageSync('comments') || {}
            if (all[this.attractionId]) {
              all[this.attractionId] = all[this.attractionId].filter(c => c.id !== comment.id)
              uni.setStorageSync('comments', all)
            }
            uni.showToast({ title: '删除成功', icon: 'success' })
            this.loadComments()
          }
        }
      })
    },

    async likeComment(comment) {
      if (!this.postId || !comment.id) {
        // 本地点赞
        comment.liked = !comment.liked
        comment.likes = (comment.likes || 0) + (comment.liked ? 1 : -1)
        return
      }

      try {
        await commentApi.like(comment.id)
        comment.liked = !comment.liked
        comment.likes = (comment.likes || 0) + (comment.liked ? 1 : -1)
      } catch (e) {
        console.error('点赞失败', e)
        // 本地点赞
        comment.liked = !comment.liked
        comment.likes = (comment.likes || 0) + (comment.liked ? 1 : -1)
      }
    },

    setRating(rating) {
      this.newRating = rating
    },

    canDelete(comment) {
      return this.currentUserId && comment.user_id === this.currentUserId
    },

    formatTime(time) {
      if (!time) return ''
      const date = new Date(time)
      const now = new Date()
      const diff = now - date

      if (diff < 60000) return '刚刚'
      if (diff < 3600000) return Math.floor(diff / 60000) + '分钟前'
      if (diff < 86400000) return Math.floor(diff / 3600000) + '小时前'
      if (diff < 604800000) return Math.floor(diff / 86400000) + '天前'

      return date.toLocaleDateString()
    }
  }
}
</script>

<style scoped>
/* ===== 统一图标尺寸 ===== */
.icon-img { width: 32rpx; height: 32rpx; flex-shrink: 0; }
.icon-img-sm { width: 24rpx; height: 24rpx; flex-shrink: 0; }
.icon-img-lg { width: 80rpx; height: 80rpx; }

.comment-section { background: #F8F9FA; padding: 24rpx; }

/* 评论列表 */
.comment-list { margin-bottom: 40rpx; }
.comment-item {
  display: flex;
  gap: 20rpx;
  background: #FFFFFF;
  padding: 24rpx;
  border-radius: 16rpx;
  margin-bottom: 20rpx;
  box-shadow: 0 2rpx 12rpx rgba(29,59,90,0.06);
  border: 1rpx solid #E8E8E8;
}
.comment-avatar {
  width: 72rpx;
  height: 72rpx;
  border-radius: 50%;
  flex-shrink: 0;
}
.comment-content { flex: 1; }
.comment-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12rpx;
}
.comment-name {
  font-size: 26rpx;
  font-weight: bold;
  color: #1D3B5A;
}
.comment-rating { font-size: 20rpx; }
.comment-text {
  font-size: 28rpx;
  color: #555;
  line-height: 1.6;
  display: block;
  margin-bottom: 12rpx;
}
.comment-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.comment-time {
  font-size: 22rpx;
  color: #999;
}
.comment-actions {
  display: flex;
  gap: 24rpx;
}
.comment-action {
  font-size: 22rpx;
  color: #2E5F8E;
  display: flex;
  align-items: center;
  gap: 4rpx;
}

.comment-empty {
  text-align: center;
  padding: 80rpx 0;
}
.empty-icon {
  display: block;
  font-size: 0;
  margin-bottom: 20rpx;
}
.empty-text {
  font-size: 26rpx;
  color: #999;
}

/* 发表评论 */
.comment-input-section {
  background: #FFFFFF;
  padding: 24rpx;
  border-radius: 16rpx;
  box-shadow: 0 2rpx 12rpx rgba(29,59,90,0.06);
  border: 1rpx solid #E8E8E8;
}
.rating-selector {
  display: flex;
  align-items: center;
  margin-bottom: 20rpx;
}
.rating-label {
  font-size: 26rpx;
  color: #333;
  margin-right: 16rpx;
}
.rating-stars {
  display: flex;
  gap: 8rpx;
}
.rating-star {
  font-size: 32rpx;
  opacity: 0.3;
}
.rating-star.active {
  opacity: 1;
}
.comment-input-wrap {
  position: relative;
  margin-bottom: 16rpx;
}
.comment-input {
  width: 100%;
  min-height: 120rpx;
  background: #F8F9FA;
  border-radius: 12rpx;
  padding: 16rpx;
  font-size: 26rpx;
  color: #333;
  line-height: 1.5;
  border: 1rpx solid #E8E8E8;
}
.comment-count {
  position: absolute;
  bottom: 10rpx;
  right: 10rpx;
  font-size: 20rpx;
  color: #999;
}
.comment-submit-btn {
  width: 100%;
  background: linear-gradient(135deg, #1D3B5A, #2E5F8E);
  color: #FFFFFF;
  font-size: 26rpx;
  font-weight: bold;
  border-radius: 50rpx;
  padding: 18rpx;
  border: none;
}
.comment-submit-btn[disabled] {
  opacity: 0.5;
}
.submit-btn-content {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8rpx;
}
.send-icon {
  font-size: 24rpx;
}

/* 加载状态 */
.loading-section {
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 60rpx 0;
}
.loading-text {
  font-size: 26rpx;
  color: #999;
}

/* 回复列表 */
.reply-list {
  margin-top: 16rpx;
  padding: 16rpx;
  background: #F8F9FA;
  border-radius: 12rpx;
}
.reply-item {
  display: flex;
  gap: 12rpx;
  padding: 12rpx 0;
  border-bottom: 1rpx solid rgba(29,59,90,0.08);
}
.reply-item:last-child {
  border-bottom: none;
  padding-bottom: 0;
}
.reply-avatar {
  width: 48rpx;
  height: 48rpx;
  border-radius: 50%;
  flex-shrink: 0;
}
.reply-content {
  flex: 1;
}
.reply-name {
  font-size: 24rpx;
  font-weight: bold;
  color: #1D3B5A;
}
.reply-text {
  font-size: 26rpx;
  color: #555;
  line-height: 1.5;
  display: block;
  margin-top: 4rpx;
}
.reply-time {
  font-size: 20rpx;
  color: #bbb;
  margin-top: 4rpx;
  display: block;
}

/* 回复提示 */
.reply-hint {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12rpx 16rpx;
  background: rgba(29, 59, 90, 0.08);
  border-radius: 8rpx;
  margin-bottom: 16rpx;
}
.reply-hint-text {
  font-size: 24rpx;
  color: #1D3B5A;
}
.reply-cancel {
  font-size: 32rpx;
  color: #999;
  padding: 0 8rpx;
}

/* 提交行 */
.submit-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.char-count {
  font-size: 22rpx;
  color: #999;
}
.char-count.warning {
  color: #E6A23C;
}

/* 删除按钮样式 */
.delete-action {
  color: #909399;
}
</style>
