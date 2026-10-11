<template>
  <view class="guide-detail-page">
    <view class="header-banner">
      <image class="banner-img" :src="guide.cover" mode="aspectFill" />
      <view class="banner-overlay"></view>
      <view class="banner-content">
        <view class="banner-tags">
          <text class="tag" v-for="tag in guide.tags" :key="tag">{{ tag }}</text>
        </view>
        <text class="banner-title">{{ guide.title }}</text>
        <view class="banner-meta">
          <view class="meta-item">
            <image class="meta-icon-img" src="/static/icons/position.png" mode="aspectFit" />
            <text class="meta-text">{{ guide.views }}</text>
          </view>
          <view class="meta-item">
            <image class="meta-icon-img" src="/static/icons/love.png" mode="aspectFit" />
            <text class="meta-text">{{ guide.likes }}</text>
          </view>
          <view class="meta-item">
            <image class="meta-icon-img" src="/static/icons/calendar.png" mode="aspectFit" />
            <text class="meta-text">{{ guide.publishTime || '2026-03-10' }}</text>
          </view>
        </view>
      </view>
    </view>

    <scroll-view class="content" scroll-y>
      <view class="author-card">
        <image class="author-avatar" :src="guide.authorAvatar" mode="aspectFill" />
        <view class="author-info">
          <text class="author-name">{{ guide.author }}</text>
          <text class="author-title">旅行达人</text>
        </view>
        <view class="follow-btn" :class="{ followed: isFollowed }" @tap="toggleFollow">
          <text>{{ isFollowed ? '已关注' : '关注' }}</text>
        </view>
      </view>

      <view class="section-card">
        <view class="section-header">
          <view class="section-accent-bar"></view>
          <view class="section-icon-wrap" style="background: rgba(29,59,90,0.1)">
            <image class="section-icon-img" src="/static/icons/detail.png" mode="aspectFit" />
          </view>
          <text class="section-title">攻略简介</text>
        </view>
        <text class="section-text">{{ guide.desc }}</text>
      </view>

      <view class="section-card">
        <view class="section-header">
          <view class="section-accent-bar"></view>
          <view class="section-icon-wrap" style="background: rgba(212,163,115,0.15)">
            <image class="section-icon-img" src="/static/icons/calendar.png" mode="aspectFit" />
          </view>
          <text class="section-title">行程安排</text>
        </view>
        <view class="timeline">
          <view class="timeline-item" v-for="(item, idx) in itinerary" :key="idx">
            <view class="timeline-left">
              <view class="timeline-dot" :class="{ active: idx === 0 }"></view>
              <view class="timeline-line" v-if="idx < itinerary.length - 1"></view>
            </view>
            <view class="timeline-content">
              <view class="timeline-header">
                <text class="day-label">Day {{ idx + 1 }}</text>
                <text class="timeline-title">{{ item.title }}</text>
              </view>
              <text class="timeline-desc">{{ item.desc }}</text>
            </view>
          </view>
        </view>
      </view>

      <view class="section-card">
        <view class="section-header">
          <view class="section-accent-bar"></view>
          <view class="section-icon-wrap" style="background: rgba(163,59,50,0.08)">
            <image class="section-icon-img" src="/static/icons/target.png" mode="aspectFit" />
          </view>
          <text class="section-title">实用贴士</text>
        </view>
        <view class="tips-grid">
          <view class="tip-card" v-for="(tip, idx) in tips" :key="idx">
            <view class="tip-icon-wrap">
              <image class="tip-icon-img" src="/static/icons/hot.png" mode="aspectFit" />
            </view>
            <text class="tip-text">{{ tip }}</text>
          </view>
        </view>
      </view>

      <view class="section-card">
        <view class="section-header">
          <view class="section-accent-bar"></view>
          <view class="section-icon-wrap" style="background: rgba(45,90,39,0.08)">
            <image class="section-icon-img" src="/static/icons/position.png" mode="aspectFit" />
          </view>
          <text class="section-title">相关景点</text>
        </view>
        <scroll-view class="spots-scroll" scroll-x>
          <view class="spots-list">
            <view class="spot-item" v-for="(spot, idx) in relatedSpots" :key="idx" @tap="goSpotDetail(spot.id)">
              <image class="spot-img" :src="spot.cover" mode="aspectFill" />
              <text class="spot-name">{{ spot.name }}</text>
            </view>
          </view>
        </scroll-view>
      </view>

      <view style="height: 180rpx;"></view>
    </scroll-view>

    <view class="footer-bar">
      <view class="footer-action" @tap="toggleFavorite">
        <view class="action-icon-wrap" :class="{ active: isFavorited }">
          <text class="action-icon">{{ isFavorited ? '❤️' : '🤍' }}</text>
        </view>
        <text class="action-text">{{ isFavorited ? '已收藏' : '收藏' }}</text>
      </view>
    </view>
  </view>
</template>

<script>
import { staticData } from '../../utils/static-data.js'
import { guideApi } from '../../utils/api.js'
import { followUtil } from '../../utils/follow.js'
import { favoritesUtil } from '../../utils/favorites.js'

export default {
  data() {
    return {
      guideId: null,
      guide: {
        id: 1,
        title: '程阳八寨深度游攻略',
        desc: '从交通、住宿到景点游览，一篇文章搞定程阳八寨之旅。程阳八寨由八个侗族村寨组成，有着独特的侗族文化和建筑风格。',
        author: '侗乡旅行者',
        authorAvatar: 'https://cdn.phototourl.com/free/2026-03-23-3644ba83-1411-49c2-b85b-d3c88e7f2ba5.jpg',
        views: '2.3w',
        likes: 1256,
        publishTime: '2026-03-10',
        cover: 'https://cdn.phototourl.com/uploads/2026-03-19-67bbd771-fa02-4207-9437-87afc2c36bc7.jpg',
        tags: ['程阳八寨', '深度游', '攻略']
      },
      isFavorited: false,
      isFollowed: false,
      itinerary: [
        { title: '到达程阳八寨', desc: '从三江县城出发，乘坐公交车或出租车到达程阳八寨景区入口，购买门票后进入景区。' },
        { title: '游览风雨桥群', desc: '依次参观程阳风雨桥、普济桥等著名风雨桥，感受侗族建筑的魅力。' },
        { title: '漫步侗族村寨', desc: '走进各个侗族村寨，参观鼓楼、民居，体验侗族人民的日常生活。' },
        { title: '品尝侗族美食', desc: '在村寨里的农家乐品尝酸鱼酸肉、油茶等侗族特色美食。' }
      ],
      tips: ['建议游玩时间：1-2天', '最佳旅游季节：春秋两季', '尊重当地少数民族风俗习惯', '准备好舒适的鞋子，需要走较多路'],
      relatedSpots: []
    }
  },
  onLoad(option) {
    this.guideId = option.id
    if (this.guideId) {
      this.loadGuideDetail(this.guideId)
    }
    this.loadRelatedSpots()
  },
  methods: {
    goBack() {
      uni.navigateBack()
    },
    async loadGuideDetail(id) {
      try {
        const guide = await guideApi.getDetail(id)
        // 验证攻略是否审核通过
        const auditStatus = guide.auditStatus !== undefined ? guide.auditStatus : guide.status
        if (auditStatus !== 1) {
          uni.showModal({
            title: '提示',
            content: '该攻略正在审核中或已被删除',
            showCancel: false,
            success: () => {
              uni.navigateBack()
            }
          })
          return
        }
        const authorId = guide.authorId || guide.author_id || guide.userId || 0
        this.guide = {
          id: guide.id,
          title: guide.title || '',
          desc: guide.desc || '',
          author: guide.author || '',
          authorId: authorId,
          authorAvatar: guide.authorAvatar || guide.author_avatar || '',
          views: guide.views || 0,
          likes: guide.likes || 0,
          publishTime: guide.publishTime || guide.publish_time || '',
          cover: guide.cover || '',
          tags: this.parseTags(guide.tags)
        }
        this.itinerary = this.parseJsonField(guide.itinerary)
        this.tips = this.parseJsonField(guide.tips)
        // 用 authorId 检查关注状态，如果没有 authorId 则用昵称生成稳定 ID
        const followId = this.getFollowId()
        this.isFollowed = followUtil.isFollowing(followId)
        this.isFavorited = favoritesUtil.isFavorite(this.guide.id, 'guide')
      } catch (e) {
        console.error('加载攻略详情失败，使用本地数据', e)
        this.loadGuideDetailLocal(id)
      }
    },
    loadGuideDetailLocal(id) {
      const guides = [
        {
          id: 1, title: '程阳八寨深度游攻略', desc: '从交通、住宿到景点游览，一篇文章搞定程阳八寨之旅。程阳八寨由八个侗族村寨组成，有着独特的侗族文化和建筑风格。', author: '侗乡旅行者', authorAvatar: 'https://cdn.phototourl.com/free/2026-03-24-955d494f-d01c-4911-87ea-2e684f9a3f29.jpg', views: '2.3w', likes: 1256, cover: 'https://cdn.phototourl.com/member/2026-03-28-436b0a1e-48ea-41ca-85f4-7fd66ca349fb.jpg', tags: ['程阳八寨', '深度游', '攻略'], publishTime: '2026-03-10', itinerary: [{ title: '到达程阳八寨', desc: '从三江县城出发，乘坐公交车或出租车到达程阳八寨景区入口，购买门票后进入景区。' }, { title: '游览风雨桥群', desc: '依次参观程阳风雨桥、普济桥等著名风雨桥，感受侗族建筑的魅力。' }, { title: '漫步侗族村寨', desc: '走进各个侗族村寨，参观鼓楼、民居，体验侗族人民的日常生活。' }, { title: '品尝侗族美食', desc: '在村寨里的农家乐品尝酸鱼酸肉、油茶等侗族特色美食。' }], tips: ['建议游玩时间：1-2天', '最佳旅游季节：春秋两季', '尊重当地少数民族风俗习惯', '准备好舒适的鞋子，需要走较多路']
        },
        { id: 2, title: '三江侗族非遗文化体验指南', desc: '侗族大歌、侗锦编织、油茶制作，全方位体验侗族非遗文化。来到三江，一定要体验这些传承千年的文化瑰宝。', author: '文化探索者', authorAvatar: 'https://cdn.phototourl.com/free/2026-03-24-9da6f2f7-f9d3-46ac-9819-695e797b76e3.jpg', views: '1.8w', likes: 986, cover: 'https://cdn.phototourl.com/free/2026-03-23-75376aa0-ba75-4269-8839-bbe6aca040a6.jpg', tags: ['非遗文化', '体验', '侗族'], publishTime: '2026-03-08', itinerary: [{ title: '侗族大歌表演', desc: '在鼓楼观看侗族大歌表演，感受多声部无伴奏合唱的魅力。' }, { title: '侗锦编织体验', desc: '跟随侗族阿妈学习侗锦编织，了解这一国家级非遗技艺。' }, { title: '油茶制作体验', desc: '学习制作侗族油茶，体验打油茶的全过程。' }, { title: '参观生态博物馆', desc: '参观三江侗族生态博物馆，全面了解侗族文化。' }], tips: ['提前了解非遗文化背景，体验会更有意义', '体验项目可能需要预约', '可以购买一些非遗产品作为纪念', '尊重传承人的劳动成果']
        },
        { id: 3, title: '布央茶园云海日出攻略', desc: '最佳拍摄位置、最佳时间、交通住宿全攻略。布央茶园的云海日出是三江必看的美景之一。', author: '摄影师老王', authorAvatar: 'https://cdn.phototourl.com/member/2026-03-24-3cd4dc5f-9c84-4b60-b074-1dba51407d7e.png', views: '1.2w', likes: 756, cover: 'https://cdn.phototourl.com/free/2026-03-23-5bbeb559-132b-460d-a3f1-043d32170ed2.png', tags: ['布央茶园', '摄影', '云海'], publishTime: '2026-03-05', itinerary: [{ title: '到达布央茶园', desc: '建议下午到达，入住茶园民宿，体验茶园风光。' }, { title: '傍晚拍摄', desc: '傍晚时分可以拍摄茶园日落，光线非常美。' }, { title: '早起拍日出', desc: '早上5点左右起床，前往最佳拍摄点等待云海日出。' }, { title: '采茶体验', desc: '上午可以体验采茶，了解茶叶制作过程。' }], tips: ['最佳拍摄季节：春季和秋季', '一定要住一晚茶园民宿', '带上三脚架和广角镜头', '注意保暖，清晨气温较低']
        },
        { id: 4, title: '三江美食地图', desc: '侗族腌鱼、油茶、糯米饭...三江必吃美食全收录。让你在三江的每一天都吃得开心！', author: '美食达人', authorAvatar: 'https://cdn.phototourl.com/member/2026-03-24-1c287f60-cae7-454e-b829-926032cddd96.png', views: '9800', likes: 623, cover: 'https://cdn.phototourl.com/member/2026-04-16-f52eb4e0-2a5b-4ba0-a6d0-846b371d0c6c.jpg', tags: ['美食', '三江', '侗族'], publishTime: '2026-03-01', itinerary: [{ title: '早餐吃油茶', desc: '早上一定要来一碗侗族油茶，配上各种小吃。' }, { title: '午餐吃酸鱼酸肉', desc: '侗族特色美食，酸辣开胃，不容错过。' }, { title: '下午茶吃糯米饭', desc: '五色糯米饭好看又好吃，还可以做伴手礼。' }, { title: '晚餐吃百家宴', desc: '如果赶上节日，可以体验侗族百家宴。' }], tips: ['酸鱼酸肉可能有些人吃不惯，可以先少量尝试', '油茶可以免费续碗', '糯米饭建议趁热吃', '可以带一些特产回去']
        }
      ]
      const guide = guides.find(g => g.id === parseInt(id))
      if (guide) {
        this.guide = guide
        this.itinerary = guide.itinerary
        this.tips = guide.tips
        const followId = this.getFollowId()
        this.isFollowed = followUtil.isFollowing(followId)
        this.isFavorited = favoritesUtil.isFavorite(this.guide.id, 'guide')
      }
    },
    parseTags(tags) {
      if (!tags) return []
      if (Array.isArray(tags)) return tags
      try { return JSON.parse(tags) } catch { return tags.split(',').map(t => t.trim()).filter(Boolean) }
    },
    parseJsonField(val) {
      if (!val) return []
      if (Array.isArray(val)) return val
      try { return JSON.parse(val) } catch { return [] }
    },
    loadRelatedSpots() {
      this.relatedSpots = staticData.attractions.slice(0, 4).map(a => ({
        id: a.id,
        name: a.name,
        cover: a.cover
      }))
    },
    toggleFavorite() {
      if (this.isFavorited) {
        favoritesUtil.removeFavorite(this.guide.id, 'guide')
        this.isFavorited = false
        this.guide.likes--
      } else {
        favoritesUtil.addFavorite({
          id: this.guide.id,
          type: 'guide',
          name: this.guide.title,
          desc: this.guide.desc,
          cover: this.guide.cover,
          icon: '/static/icons/detail.png'
        })
        this.isFavorited = true
        this.guide.likes++
      }
    },
    toggleFollow() {
      const followId = this.getFollowId()
      if (!followId) {
        uni.showToast({ title: '无法获取作者信息', icon: 'none' })
        return
      }
      const userInfo = {
        userId: followId,
        nickname: this.guide.author,
        avatar: this.guide.authorAvatar
      }
      if (this.isFollowed) {
        followUtil.removeFollow(followId)
        this.isFollowed = false
      } else {
        followUtil.addFollow(userInfo)
        this.isFollowed = true
      }
    },
    getFollowId() {
      // 优先使用真实的 userId，如果没有则用昵称生成稳定的关注 ID
      if (this.guide.authorId && this.guide.authorId !== 0) {
        return this.guide.authorId
      }
      if (this.guide.author) {
        return 'guide_author_' + this.guide.author
      }
      return null
    },
    goSpotDetail(id) {
      uni.navigateTo({ url: '/pages/attraction/detail?id=' + id })
    }
  }
}
</script>

<style scoped lang="scss">
.guide-detail-page {
  min-height: 100vh;
  background: #F8F9FA;
}

.header-banner {
  position: relative;
  height: 540rpx;
}

.banner-img {
  width: 100%;
  height: 100%;
}

.banner-overlay {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  height: 320rpx;
  background: linear-gradient(transparent, rgba(29, 59, 90, 0.9));
}

.banner-content {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  padding: 30rpx;
}

.banner-tags {
  display: flex;
  gap: 12rpx;
  margin-bottom: 16rpx;
}

.tag {
  font-size: 20rpx;
  color: #FDFCF8;
  background: linear-gradient(135deg, #D4A373, #D4B873);
  padding: 6rpx 18rpx;
  border-radius: 20rpx;
  font-weight: 500;
  box-shadow: 0 4rpx 12rpx rgba(200, 169, 81, 0.3);
}

.banner-title {
  display: block;
  font-size: 40rpx;
  font-weight: bold;
  color: #FDFCF8;
  margin-bottom: 20rpx;
  line-height: 1.4;
  text-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.3);
}

.banner-meta {
  display: flex;
  gap: 24rpx;
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 8rpx;
}

.meta-icon {
  font-size: 24rpx;
}

.meta-icon-img {
  width: 28rpx;
  height: 28rpx;
}

.meta-text {
  font-size: 22rpx;
  color: rgba(255, 255, 255, 0.9);
}

.back-btn {
  position: absolute;
  top: 70rpx;
  left: 20rpx;
  width: 76rpx;
  height: 76rpx;
  background: rgba(0, 0, 0, 0.35);
  backdrop-filter: blur(10rpx);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.3s;
}

.back-icon {
  font-size: 48rpx;
  color: #FDFCF8;
  font-weight: bold;
  line-height: 1;
}

.content {
  margin-top: -50rpx;
  position: relative;
  z-index: 1;
}

.author-card {
  display: flex;
  align-items: center;
  gap: 20rpx;
  background: #FDFCF8;
  margin: 0 24rpx;
  padding: 28rpx;
  border-radius: 24rpx;
  box-shadow: 0 8rpx 24rpx rgba(29, 59, 90, 0.08);
}

.author-avatar {
  width: 96rpx;
  height: 96rpx;
  border-radius: 50%;
  border: 3rpx solid rgba(212, 163, 115, 0.3);
}

.author-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 6rpx;
}

.author-name {
  font-size: 30rpx;
  font-weight: bold;
  color: #1D3B5A;
}

.author-title {
  font-size: 22rpx;
  color: #999;
}

.follow-btn {
  padding: 14rpx 32rpx;
  background: linear-gradient(135deg, #1D3B5A, #2E5F8E);
  border-radius: 30rpx;
  transition: all 0.3s;
}

.follow-btn.followed {
  background: #F8F9FA;
}

.follow-btn text {
  font-size: 24rpx;
  color: #FDFCF8;
  font-weight: bold;
}

.follow-btn.followed text {
  color: #1D3B5A;
}

.section-card {
  background: #FDFCF8;
  margin: 0 24rpx 24rpx;
  padding: 32rpx;
  border-radius: 24rpx;
  box-shadow: 0 8rpx 24rpx rgba(29, 59, 90, 0.06);
}

.section-header {
  display: flex;
  align-items: center;
  gap: 16rpx;
  margin-bottom: 24rpx;
}

.section-accent-bar {
  width: 6rpx;
  height: 36rpx;
  background: linear-gradient(180deg, #1D3B5A, #2E5F8E);
  border-radius: 3rpx;
}

.section-icon-wrap {
  width: 64rpx;
  height: 64rpx;
  border-radius: 18rpx;
  display: flex;
  align-items: center;
  justify-content: center;
}

.section-icon {
  font-size: 32rpx;
}

.section-icon-img {
  width: 36rpx;
  height: 36rpx;
}

.section-title {
  font-size: 32rpx;
  font-weight: bold;
  color: #1D3B5A;
}

.section-text {
  font-size: 28rpx;
  color: #555;
  line-height: 1.8;
}

.timeline {
  display: flex;
  flex-direction: column;
}

.timeline-item {
  display: flex;
  gap: 20rpx;
}

.timeline-left {
  display: flex;
  flex-direction: column;
  align-items: center;
  width: 36rpx;
}

.timeline-dot {
  width: 28rpx;
  height: 28rpx;
  border-radius: 50%;
  background: #ddd;
  flex-shrink: 0;
  border: 4rpx solid #F8F9FA;
}

.timeline-dot.active {
  background: linear-gradient(135deg, #D4A373, #D4B873);
  box-shadow: 0 6rpx 16rpx rgba(200, 169, 81, 0.4);
}

.timeline-line {
  flex: 1;
  width: 3rpx;
  background: linear-gradient(180deg, #D4A373, #E8D4B8);
  margin: 8rpx 0;
}

.timeline-content {
  flex: 1;
  padding-bottom: 36rpx;
}

.timeline-header {
  display: flex;
  align-items: center;
  gap: 14rpx;
  margin-bottom: 14rpx;
}

.day-label {
  font-size: 20rpx;
  color: #D4A373;
  background: rgba(200, 169, 81, 0.15);
  padding: 6rpx 14rpx;
  border-radius: 14rpx;
  font-weight: bold;
}

.timeline-title {
  font-size: 28rpx;
  font-weight: bold;
  color: #1D3B5A;
}

.timeline-desc {
  font-size: 26rpx;
  color: #666;
  line-height: 1.7;
}

.tips-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 18rpx;
}

.tip-card {
  display: flex;
  align-items: flex-start;
  gap: 14rpx;
  padding: 24rpx;
  background: linear-gradient(135deg, #F8F9FA, #FAF8F5);
  border-radius: 18rpx;
  border: 1rpx solid rgba(200, 169, 81, 0.2);
  transition: all 0.3s;
}

.tip-icon-wrap {
  flex-shrink: 0;
  width: 52rpx;
  height: 52rpx;
  background: linear-gradient(135deg, #D4A373, #D4B873);
  border-radius: 14rpx;
  display: flex;
  align-items: center;
  justify-content: center;
}

.tip-icon {
  font-size: 24rpx;
}

.tip-icon-img {
  width: 32rpx;
  height: 32rpx;
}

.tip-text {
  flex: 1;
  font-size: 24rpx;
  color: #555;
  line-height: 1.6;
}

.spots-scroll {
  white-space: nowrap;
}

.spots-list {
  display: flex;
  gap: 20rpx;
}

.spot-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 14rpx;
}

.spot-img {
  width: 200rpx;
  height: 160rpx;
  border-radius: 50%;
  box-shadow: 0 6rpx 16rpx rgba(0, 0, 0, 0.1);
}

.spot-name {
  font-size: 24rpx;
  color: #1D3B5A;
  font-weight: 500;
}

.footer-bar {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  z-index: 10;
  display: flex;
  padding: 24rpx 30rpx;
  padding-bottom: calc(24rpx + var(--window-bottom));
  background: #FDFCF8;
  box-shadow: 0 -8rpx 24rpx rgba(29, 59, 90, 0.08);
}

.footer-action {
  flex: 1;
  display: flex;
  flex-direction: row;
  align-items: center;
  justify-content: center;
  gap: 12rpx;
  padding: 20rpx;
  background: #F8F9FA;
  border-radius: 40rpx;
  transition: all 0.3s;
}

.footer-action:active {
  transform: scale(0.96);
}

.action-icon-wrap {
  width: 60rpx;
  height: 60rpx;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.3s;
}

.action-icon-wrap.active {
  background: linear-gradient(135deg, #1D3B5A, #2E5F8E);
}

.action-icon {
  font-size: 32rpx;
}

.action-text {
  font-size: 26rpx;
  color: #1D3B5A;
  font-weight: 600;
}

.footer-action .action-icon-wrap.active + .action-text {
  color: #1D3B5A;
}
</style>
