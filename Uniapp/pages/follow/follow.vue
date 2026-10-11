<template>
  <view class="follow-page">
    <view class="follow-list" v-if="follows.length > 0">
      <view class="follow-card" v-for="item in follows" :key="item.userId">
        <view class="user-section">
          <image class="user-avatar" :src="item.avatar || 'https://cdn.phototourl.com/free/2026-03-24-955d494f-d01c-4911-87ea-2e684f9a3f29.jpg'" mode="aspectFill" />
          <view class="user-info">
            <text class="user-name">{{ item.nickname }}</text>
            <text class="follow-time">关注于 {{ formatTime(item.createTime) }}</text>
          </view>
          <view class="unfollow-btn" @tap="handleUnfollow(item)">
            <text>已关注</text>
          </view>
        </view>

        <view class="posts-section" v-if="item.posts && item.posts.length > 0">
          <view class="section-label">
            <text class="label-icon">📝</text>
            <text class="label-text">TA的动态</text>
          </view>
          <view class="post-item" v-for="post in item.posts" :key="post.id" @tap="goPostDetail(post.id)">
            <view class="post-content">
              <text class="post-title">{{ post.content || post.title || '无标题' }}</text>
              <text class="post-time">{{ post.createTime || '' }}</text>
            </view>
            <image v-if="post.images" class="post-cover" :src="parseCover(post.images)" mode="aspectFill" />
          </view>
        </view>

        <view class="posts-section" v-if="item.guides && item.guides.length > 0">
          <view class="section-label">
            <text class="label-icon">📋</text>
            <text class="label-text">TA的攻略</text>
          </view>
          <view class="post-item" v-for="guide in item.guides" :key="guide.id" @tap="goGuideDetail(guide.id)">
            <view class="post-content">
              <text class="post-title">{{ guide.title }}</text>
              <text class="post-time">{{ guide.publishTime || '' }}</text>
            </view>
            <image v-if="guide.cover" class="post-cover" :src="guide.cover" mode="aspectFill" />
          </view>
        </view>

        <view class="no-posts" v-if="(!item.posts || item.posts.length === 0) && (!item.guides || item.guides.length === 0)">
          <text class="no-posts-text">暂无动态</text>
        </view>
      </view>
    </view>

    <view class="empty-state" v-else>
      <text class="empty-icon">👤</text>
      <text class="empty-title">暂无关注</text>
      <text class="empty-sub">去发现有趣的博主，关注他们吧</text>
      <view class="empty-btn" @tap="goExplore">去发现</view>
    </view>
  </view>
</template>

<script>
import { followUtil } from '../../utils/follow.js'
import { postApi, guideApi } from '../../utils/api.js'

export default {
  data() {
    return {
      follows: []
    }
  },
  onShow() {
    this.loadFollows()
  },
  methods: {
    async loadFollows() {
      const list = followUtil.getAllFollows()
      if (!list || list.length === 0) {
        this.follows = []
        return
      }
      const enriched = []
      for (const user of list) {
        try {
          const posts = await this.loadUserPosts(user.userId, user.nickname)
          const guides = await this.loadUserGuides(user.nickname)
          enriched.push({ ...user, posts, guides })
        } catch (e) {
          enriched.push({ ...user, posts: [], guides: [] })
        }
      }
      this.follows = enriched
    },
    async loadUserPosts(userId, nickname) {
      if (!nickname) return []
      let list = []
      // 优先尝试从社区列表中按昵称过滤
      try {
        const communityRes = await postApi.getList(1, 50)
        const communityList = (communityRes && communityRes.records) || (communityRes && communityRes.list) || []
        list = communityList.filter(p => (p.nickname || p.author) === nickname)
      } catch (e) {}
      
      // 如果社区列表没找到，尝试使用 getMyPosts（但要确保 userId 是有效的数字）
      if (list.length === 0 && userId) {
        try {
          const userIdNum = Number(userId)
          if (!isNaN(userIdNum) && userIdNum > 0) {
            const res = await postApi.getMyPosts(userIdNum, 1, 50)
            list = (res && res.records) || (res && res.list) || []
          }
        } catch (e) {}
      }
      
      return list.filter(p => Number(p.status) === 1).slice(0, 3)
    },
    async loadUserGuides(nickname) {
      if (!nickname) return []
      try {
        const res = await guideApi.getList(1, 50, 1)
        const list = (res && res.records) || (res && res.list) || []
        return list.filter(g => {
          // 只显示审核通过的攻略
          const auditStatus = g.auditStatus !== undefined ? g.auditStatus : g.status
          return g.author === nickname && auditStatus === 1
        }).slice(0, 3)
      } catch (e) {
        return []
      }
    },
    parseCover(images) {
      if (!images) return ''
      let url = ''
      if (typeof images === 'string' && images.startsWith('[')) {
        try {
          const arr = JSON.parse(images)
          url = arr[0] || ''
        } catch (e) {
          url = images
        }
      } else {
        url = images
      }
      return url
    },
    formatTime(timestamp) {
      if (!timestamp) return ''
      const date = new Date(timestamp)
      const m = date.getMonth() + 1
      const d = date.getDate()
      return `${m}月${d}日`
    },
    handleUnfollow(item) {
      uni.showModal({
        title: '取消关注',
        content: `确定取消关注 ${item.nickname} 吗？`,
        success: (res) => {
          if (res.confirm) {
            followUtil.removeFollow(item.userId)
            this.loadFollows()
          }
        }
      })
    },
    goPostDetail(id) {
      uni.navigateTo({ url: '/pages/discover/detail?id=' + id })
    },
    goGuideDetail(id) {
      uni.navigateTo({ url: '/pages/discover/guide-detail?id=' + id })
    },
    goExplore() {
      uni.switchTab({ url: '/pages/discover/discover' })
    }
  }
}
</script>

<style scoped lang="scss">
.follow-page {
  min-height: 100vh;
  background: #F8F9FA;
  padding: 24rpx;
}

.follow-card {
  background: #FDFCF8;
  border-radius: 24rpx;
  padding: 28rpx;
  margin-bottom: 24rpx;
  box-shadow: 0 8rpx 24rpx rgba(29, 59, 90, 0.06);
}

.user-section {
  display: flex;
  align-items: center;
  gap: 20rpx;
  margin-bottom: 20rpx;
}

.user-avatar {
  width: 96rpx;
  height: 96rpx;
  border-radius: 50%;
  border: 3rpx solid rgba(212, 163, 115, 0.3);
  flex-shrink: 0;
}

.user-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 6rpx;
}

.user-name {
  font-size: 30rpx;
  font-weight: bold;
  color: #1D3B5A;
}

.follow-time {
  font-size: 22rpx;
  color: #999;
}

.unfollow-btn {
  padding: 12rpx 28rpx;
  background: #F8F9FA;
  border: 1rpx solid #ddd;
  border-radius: 30rpx;
  flex-shrink: 0;
}

.unfollow-btn text {
  font-size: 24rpx;
  color: #666;
  font-weight: 500;
}

.posts-section {
  border-top: 1rpx solid rgba(29, 59, 90, 0.06);
  padding-top: 20rpx;
}

.section-label {
  display: flex;
  align-items: center;
  gap: 10rpx;
  margin-bottom: 16rpx;
}

.label-icon {
  font-size: 28rpx;
}

.label-text {
  font-size: 26rpx;
  font-weight: 600;
  color: #1D3B5A;
}

.post-item {
  display: flex;
  align-items: center;
  gap: 16rpx;
  padding: 16rpx 0;
  border-bottom: 1rpx solid rgba(29, 59, 90, 0.04);
}

.post-item:last-child {
  border-bottom: none;
}

.post-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 8rpx;
}

.post-title {
  font-size: 26rpx;
  color: #333;
  line-height: 1.5;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.post-time {
  font-size: 22rpx;
  color: #bbb;
}

.post-cover {
  width: 100rpx;
  height: 80rpx;
  border-radius: 12rpx;
  flex-shrink: 0;
}

.no-posts {
  padding: 20rpx 0;
  text-align: center;
}

.no-posts-text {
  font-size: 24rpx;
  color: #ccc;
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding-top: 200rpx;
}

.empty-icon {
  font-size: 80rpx;
  margin-bottom: 24rpx;
}

.empty-title {
  font-size: 32rpx;
  font-weight: bold;
  color: #1D3B5A;
  margin-bottom: 12rpx;
}

.empty-sub {
  font-size: 26rpx;
  color: #999;
  margin-bottom: 40rpx;
}

.empty-btn {
  padding: 20rpx 60rpx;
  background: linear-gradient(135deg, #1D3B5A, #2E5F8E);
  border-radius: 40rpx;
}

.empty-btn text {
  font-size: 28rpx;
  color: #FDFCF8;
  font-weight: bold;
}
</style>
