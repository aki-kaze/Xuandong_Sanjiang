<template>
  <view class="detail-page">
    <!-- 顶部图片区 -->
    <view class="hero-section">
      <image class="hero-img" :src="attraction.cover" mode="aspectFill" @error="handleImageError" />
      <view class="hero-overlay">
        <view class="hero-top-actions">
          <view class="hero-btn" @tap="toggleCollect"><image :src="isCollected ? '/static/icons/love.png' : '/static/icons/收藏.png'" class="icon-img" mode="aspectFit" /></view>
          <view class="hero-btn" @tap="shareAttraction">
            <image src="/static/icons/share.png" mode="aspectFit" style="width:28rpx;height:28rpx;" />
          </view>
        </view>
        <view class="hero-bottom-info">
          <text class="hero-category-badge">{{ attraction.category }}</text>
        </view>
      </view>
    </view>

    <!-- 基本信息 -->
    <view class="info-card card-shadow">
      <view class="info-header">
        <text class="info-name">{{ attraction.name }}</text>
        <view class="info-price-tag">
          <text class="info-price">{{ attraction.price === 0 ? '免费' : '¥' + attraction.price }}</text>
        </view>
      </view>
      <view class="info-rating">
        <text class="rating-star">⭐</text>
        <text class="rating-score">{{ attraction.rating }}</text>
        <text class="rating-label">综合评分</text>
        <view class="rating-sep"></view>
        <text class="rating-category">{{ attraction.category }}</text>
      </view>
      <view class="info-tags">
        <text class="info-tag" v-for="tag in attraction.tags" :key="tag">{{ tag }}</text>
      </view>
    </view>

    <!-- 实用信息 -->
    <view class="practical-card card-shadow">
      <view class="card-title-row">
        <view class="card-accent-bar"></view>
        <text class="card-title">实用信息</text>
      </view>
      <view class="practical-item">
        <text class="practical-icon"><image class="icon-img" src="/static/icons/position.png" mode="aspectFit" /></text>
        <view class="practical-content">
          <text class="practical-label">地址</text>
          <text class="practical-value">{{ attraction.address }}</text>
        </view>
        <view class="practical-action-btn" @tap="openMap">导航</view>
      </view>
      <view class="practical-item">
        <text class="practical-icon"><image class="icon-img" src="/static/icons/time.png" mode="aspectFit" /></text>
        <view class="practical-content">
          <text class="practical-label">开放时间</text>
          <text class="practical-value">{{ attraction.openTime }}</text>
        </view>
      </view>
      <view class="practical-item" v-if="attraction.phone">
        <text class="practical-icon"><image class="icon-img" src="/static/icons/phone.png" mode="aspectFit" /></text>
        <view class="practical-content">
          <text class="practical-label">联系电话</text>
          <text class="practical-value">{{ attraction.phone }}</text>
        </view>
        <view class="practical-action-btn" @tap="callPhone">拨打</view>
      </view>
    </view>

    <!-- 快捷功能 -->
    <view class="action-card card-shadow">
      <view class="action-item" @tap="goMap">
        <view class="action-icon-wrap">
          <image src="/static/icons/map.png" class="action-icon-img" mode="aspectFit" />
        </view>
        <text class="action-label">地图导航</text>
      </view>
      <view class="action-item" @tap="goAI">
        <view class="action-icon-wrap">
          <image src="/static/icons/ai_index.png" class="action-icon-img" mode="aspectFit" />
        </view>
        <text class="action-label">AI问答</text>
      </view>
      <view class="action-item" @tap="goRoute">
        <view class="action-icon-wrap">
          <image src="/static/icons/route.png" class="action-icon-img" mode="aspectFit" />
        </view>
        <text class="action-label">路线规划</text>
      </view>
      <view class="action-item" @tap="openTicketModal">
        <view class="action-icon-wrap">
          <image src="/static/icons/ticket.png" class="action-icon-img" mode="aspectFit" />
        </view>
        <text class="action-label">购票预订</text>
      </view>
    </view>

    <!-- 景点介绍 -->
    <view class="desc-card card-shadow">
      <view class="card-title-row">
        <view class="card-accent-bar"></view>
        <text class="card-title">景点介绍</text>
      </view>
      <text class="desc-text">{{ attraction.desc }}</text>
      <view class="tips-box" v-if="attraction.tips">
        <text class="tips-title"><image class="icon-img" src="/static/icons/detail.png" mode="aspectFit" /> 旅游贴士</text>
        <text class="tips-text">{{ attraction.tips }}</text>
      </view>
    </view>

    <!-- 文旅产品推荐 -->
    <view class="shop-card card-shadow">
      <view class="card-title-row">
        <view class="card-accent-bar"></view>
        <text class="card-title">文旅产品推荐</text>
      </view>
      <scroll-view scroll-x class="shop-scroll">
        <view class="shop-list">
          <view class="shop-item" v-for="item in shopItems" :key="item.id">
            <image class="shop-img" :src="item.img" mode="aspectFill" />
            <text class="shop-name">{{ item.name }}</text>
            <text class="shop-price">¥{{ item.price }}</text>
          </view>
        </view>
      </scroll-view>
    </view>

    <!-- 游客评价 -->
    <view class="review-card card-shadow">
      <view class="review-header">
        <view class="card-title-row">
          <view class="card-accent-bar"></view>
          <text class="card-title">游客评价</text>
        </view>
      </view>

      <!-- 使用评论组件 -->
      <CommentSection :attractionId="attractionId" />
    </view>

    <!-- 周边景点 -->
    <view class="nearby-card card-shadow">
      <view class="card-title-row">
        <view class="card-accent-bar"></view>
        <text class="card-title">周边景点</text>
      </view>
      <scroll-view scroll-x>
        <view class="nearby-list">
          <view class="nearby-item" v-for="item in nearbyAttractions" :key="item.id" @tap="goDetail(item.id)">
            <image class="nearby-img" :src="item.cover" mode="aspectFill" />
            <view class="nearby-info">
              <text class="nearby-name">{{ item.name }}</text>
              <text class="nearby-price">{{ item.price === 0 ? '免费' : '¥' + item.price }}</text>
            </view>
          </view>
        </view>
      </scroll-view>
    </view>

    <view style="height: 160rpx;"></view>

    <!-- 底部操作栏 -->
    <view class="bottom-bar">
      <view class="bottom-left">
        <view class="bottom-btn" @tap="toggleCollect">
          <image :src="isCollected ? '/static/icons/love.png' : '/static/icons/收藏.png'" class="icon-img" mode="aspectFit" />
          <text class="bottom-btn-label">收藏</text>
        </view>
        <view class="bottom-btn" @tap="goMap">
          <image src="/static/icons/map.png" class="icon-img" mode="aspectFit" />
          <text class="bottom-btn-label">导航</text>
        </view>
      </view>
      <view class="bottom-book" @tap="openTicketModal">立即购票</view>
    </view>

    <!-- 购票弹窗 -->
    <view class="pay-modal" v-if="showTicketModal" @tap="closeModal">
      <view class="pay-box" @tap.stop>
        <view class="pay-header">
          <text class="pay-title">预订门票</text>
          <text class="pay-close" @tap.stop="closeModal">✕</text>
        </view>

        <!-- 订单确认 -->
        <view>
          <view class="ticket-info-card">
            <text class="ticket-info-name">{{ selectedTicket.name }}</text>
            <view class="ticket-info-price">
              <text class="price-symbol">¥</text>
              <text class="price-value">{{ selectedTicket.price }}</text>
            </view>
          </view>

          <view class="pay-info">
            <view class="pay-row">
              <text class="pay-key">数量</text>
              <view class="pay-qty-ctrl">
                <view class="qty-btn" :class="{ disabled: quantity <= 1 }" @tap="quantity > 1 && quantity--">－</view>
                <text class="qty-num">{{ quantity }}</text>
                <view class="qty-btn" :class="{ disabled: quantity >= 5 }" @tap="quantity < 5 && quantity++">＋</view>
              </view>
            </view>
            <view class="pay-row">
              <text class="pay-key">游玩日期</text>
              <picker mode="date" :value="selectedDate" @change="e => selectedDate = e.detail.value">
                <view class="pay-picker">
                  <text class="pay-picker-text">{{ selectedDate }}</text>
                  <text class="pay-picker-arrow">›</text>
                </view>
              </picker>
            </view>
            <view class="pay-row">
              <text class="pay-key">联系人</text>
              <input class="pay-input" v-model="contactName" placeholder="请输入姓名" />
            </view>
            <view class="pay-row">
              <text class="pay-key">手机号</text>
              <input class="pay-input" v-model="contactPhone" placeholder="请输入手机号" type="number" maxlength="11" />
            </view>
            <view class="pay-row total-row">
              <text class="pay-key">合计</text>
              <text class="pay-total">¥{{ selectedTicket.price * quantity }}</text>
            </view>
          </view>

          <view class="pay-actions">
            <view class="pay-back-btn" @tap.stop="closeModal">取消</view>
            <view class="pay-btn" @tap.stop="confirmPay">
              <text class="pay-btn-text">确认支付 ¥{{ selectedTicket.price * quantity }}</text>
            </view>
          </view>
        </view>
      </view>
    </view>
  </view>
</template>

<script>
import { attractionApi, ticketOrderApi, formatOrderTime } from '../../utils/api.js'
import { staticData } from '../../utils/static-data.js'
import CommentSection from '@/components/CommentSection.vue'
import { getRandomAvatar, requireLogin, checkLoginStatus } from '../../utils/auth.js'
import { favoritesUtil } from '../../utils/favorites.js'

export default {
  components: {
    CommentSection
  },
  data() {
    return {
      attractionId: 1,
      attraction: {},
      isCollected: false,
      reviews: [
      { id: 1, name: '旅行达人', avatar: getRandomAvatar(), time: '2026-02-10', stars: 5, content: '程阳八寨真的太美了！侗族大歌的演出让我感动落泪，强烈推荐！' },
      { id: 2, name: '文化爱好者', avatar: getRandomAvatar(), time: '2026-01-28', stars: 5, content: '风雨桥的建筑技艺令人叹为观止，不用一钉一铆却如此坚固，侗族人民的智慧令人敬佩。' },
      { id: 3, name: '摄影师小张', avatar: getRandomAvatar(), time: '2026-01-15', stars: 4, content: '清晨的薄雾中，风雨桥倒映在水中，美得像一幅画。建议早起拍照，光线最好。' }
    ],
      shopItems: [
        { id: 1, name: '精选手绣侗锦', price: 299, img: 'https://cdn.phototourl.com/free/2026-03-23-83a39e17-b2f9-4ff8-bced-41e9c064e9fe.jpg' },
        { id: 2, name: '木构模型积木', price: 158, img: 'https://cdn.phototourl.com/free/2026-03-23-4df6b38e-a4a7-4898-a867-eababd51019a.jpg' },
        { id: 3, name: '三江布央茶叶', price: 88, img: 'https://cdn.phototourl.com/free/2026-03-23-5bbeb559-132b-460d-a3f1-043d32170ed2.png' }
      ],
      showTicketModal: false,
      tickets: staticData.tickets,
      selectedTicket: { name: '门票', price: 0 },
      selectedDate: new Date().toISOString().slice(0, 10),
      quantity: 1,
      contactName: '',
      contactPhone: ''
    }
  },
  computed: {
    nearbyAttractions() {
      return staticData.attractions.filter(a => a.id !== this.attractionId).slice(0, 4)
    }
  },
  onLoad(options) {
    if (options.id) this.attractionId = parseInt(options.id)
    this.loadAttraction()

    // 自动填充用户姓名和电话
    const userInfo = uni.getStorageSync('userInfo') || {}
    if (userInfo.name) {
      this.contactName = userInfo.name
    } else if (userInfo.nickname) {
      this.contactName = userInfo.nickname
    }
    if (userInfo.phone) {
      this.contactPhone = userInfo.phone
    }
    // 如果带有 action=ticket 参数，自动打开购票弹窗
    if (options.action === 'ticket') {
      this.$nextTick(() => {
        setTimeout(() => {
          this.openTicketModal()
        }, 500)
      })
    }
  },
  onShow() {
    this.checkIsCollected()
  },
  methods: {
    checkIsCollected() {
      this.isCollected = favoritesUtil.isFavorite(this.attractionId, 'attraction')
    },
    async loadAttraction() {
      try {
        const res = await attractionApi.getDetail(this.attractionId)
        if (res && res.id) {
          this.attraction = this.normalizeAttraction(res)
        } else {
          this.loadStaticAttraction()
        }
      } catch (error) {
        console.error('加载景点详情失败，使用静态数据:', error)
        this.loadStaticAttraction()
      }

      // 数据加载完成后记录足迹
      this.$nextTick(() => {
        this.addFootprint()
      })
    },
    normalizeAttraction(data) {
      // 统一后端API和静态数据的字段名差异
      if (data.tags && typeof data.tags === 'string') {
        try { data.tags = JSON.parse(data.tags) } catch (e) { data.tags = [] }
      }
      // 后端返回的是 openTime，但模板用 openTime — 这个一致没问题
      // 确保 lat/lng 是数字
      if (data.lat) data.lat = parseFloat(data.lat)
      if (data.lng) data.lng = parseFloat(data.lng)
      // desc 字段兼容：后端用 description，模板用 desc
      if (!data.desc && data.description) data.desc = data.description
      return data
    },
    loadStaticAttraction() {
      const attraction = staticData.attractions.find(a => a.id === this.attractionId)
      if (attraction) {
        this.attraction = this.normalizeAttraction({ ...attraction })
      } else {
        uni.showToast({ title: '未找到景点信息', icon: 'none' })
      }
    },
    async toggleCollect() {
      if (!requireLogin('收藏景点')) return
      const valid = await checkLoginStatus()
      if (!valid) return
      const item = {
        id: this.attractionId,
        type: 'attraction',
        name: this.attraction.name,
        desc: this.attraction.desc || this.attraction.description,
        price: this.attraction.price,
        cover: this.attraction.cover,
        emoji: '🏞️'
      }
      favoritesUtil.toggleFavorite(item)
      this.checkIsCollected()
    },
    shareAttraction() { uni.showToast({ title: '点击右上角分享', icon: 'none' }) },
    openMap() {
      const lat = parseFloat(this.attraction.lat || this.attraction.latitude)
      const lng = parseFloat(this.attraction.lng || this.attraction.longitude)
      if (!lat || !lng) { 
        uni.showToast({ title: '暂无位置信息', icon: 'none' })
        return 
      }
      
      // 跳转到内部地图页面，携带目标位置信息
      uni.navigateTo({
        url: '/pages/map/map?targetId=' + this.attractionId + '&targetLat=' + lat + '&targetLng=' + lng + '&targetName=' + encodeURIComponent(this.attraction.name) + '&targetAddress=' + encodeURIComponent(this.attraction.address || '') + '&targetCover=' + encodeURIComponent(this.attraction.cover || '') + '&targetType=attraction'
      })
    },
    callPhone() {
      if (this.attraction.phone) {
        uni.makePhoneCall({
          phoneNumber: this.attraction.phone,
          fail: (err) => {
            if (err.errMsg && !err.errMsg.includes('cancel')) {
              console.error('拨号失败', err)
              uni.showToast({ title: '拨号失败', icon: 'none' })
            }
          }
        })
      }
    },
    goMap() {
      const lat = parseFloat(this.attraction.lat || this.attraction.latitude)
      const lng = parseFloat(this.attraction.lng || this.attraction.longitude)
      
      // 跳转到内部地图页面，携带目标位置信息
      uni.navigateTo({
        url: '/pages/map/map?targetId=' + this.attractionId + '&targetLat=' + lat + '&targetLng=' + lng + '&targetName=' + encodeURIComponent(this.attraction.name) + '&targetAddress=' + encodeURIComponent(this.attraction.address || '') + '&targetCover=' + encodeURIComponent(this.attraction.cover || '') + '&targetType=attraction'
      })
    },
    // 添加足迹记录
    addFootprint() {
      // 确保景点数据已加载
      if (!this.attraction || !this.attraction.name) {
        console.log('景点数据未加载，跳过足迹记录')
        return
      }

      console.log('准备记录足迹，景点信息:', {
        id: this.attractionId,
        name: this.attraction.name,
        cover: this.attraction.cover
      })

      const footprintList = uni.getStorageSync('footprintList') || []

      // 检查是否已存在（同一个id和type）
      const existingIndex = footprintList.findIndex(item =>
        item.id === this.attractionId && item.type === 'attraction'
      )

      if (existingIndex !== -1) {
        // 已存在，更新访问时间和访问次数
        footprintList[existingIndex].visitTime = Date.now()
        footprintList[existingIndex].visitCount = (footprintList[existingIndex].visitCount || 1) + 1

        // 更新封面和位置信息（如果有的话）
        if (this.attraction.cover) {
          footprintList[existingIndex].cover = this.attraction.cover
        }
        if (this.attraction.address) {
          footprintList[existingIndex].location = this.attraction.address
        }

        // 移到列表最前面
        const item = footprintList.splice(existingIndex, 1)[0]
        footprintList.unshift(item)

        console.log('更新已有足迹，访问次数:', footprintList[0].visitCount)
      } else {
        // 不存在，添加新记录
        const newFootprint = {
          id: this.attractionId,
          name: this.attraction.name,
          type: 'attraction',
          cover: this.attraction.cover || '',
          location: this.attraction.address || '',
          lat: parseFloat(this.attraction.lat || this.attraction.latitude || 0),
          lng: parseFloat(this.attraction.lng || this.attraction.longitude || 0),
          createTime: Date.now(),
          visitTime: Date.now(),
          visitCount: 1
        }

        footprintList.unshift(newFootprint)

        // 更新总访问次数（只在新增时增加）
        let totalCount = uni.getStorageSync('footprint') || 0
        totalCount++
        uni.setStorageSync('footprint', totalCount)

        console.log('添加新足迹，总数:', totalCount, '封面:', newFootprint.cover)
      }

      // 只保留最近 100 条
      uni.setStorageSync('footprintList', footprintList.slice(0, 100))

      console.log('足迹记录成功')
    },
    goAI() { uni.switchTab({ url: '/pages/ai/ai' }) },
    goRoute() { uni.navigateTo({ url: '/pages/route/route' }) },
    openTicketModal() {
      this.quantity = 1
      this.contactName = ''
      this.contactPhone = ''
      this.selectedDate = new Date().toISOString().slice(0, 10)
      this.selectedTicket = {
        name: this.attraction.name + '门票',
        price: this.attraction.price || 0
      }
      // 自动填充用户信息
      const userInfo = uni.getStorageSync('userInfo') || {}
      if (userInfo.name) {
        this.contactName = userInfo.name
      }
      if (userInfo.phone) {
        this.contactPhone = userInfo.phone
      }
      if (userInfo.nickname && !userInfo.name) {
        this.contactName = userInfo.nickname
      }
      this.showTicketModal = true
    },
    closeModal() {
      this.showTicketModal = false
    },
    async confirmPay() {
      if (!this.contactName.trim()) {
        uni.showToast({ title: '请填写联系人姓名', icon: 'none' })
        return
      }
      if (!this.contactPhone || String(this.contactPhone).length !== 11) {
        uni.showToast({ title: '请填写正确的手机号', icon: 'none' })
        return
      }
      const userInfo = uni.getStorageSync('userInfo') || {}
      const userId = userInfo.userId || userInfo.id || 0
      const orderData = {
        orderNo: 'SJ' + Date.now(),
        ticketId: this.attraction.id,
        ticketName: this.selectedTicket.name,
        ticketCover: this.attraction.cover || '',
        ticketType: '景区门票',
        price: this.selectedTicket.price,
        quantity: this.quantity,
        totalPrice: this.selectedTicket.price * this.quantity,
        visitDate: this.selectedDate,
        contactName: this.contactName,
        contactPhone: String(this.contactPhone),
        remark: '',
        userId: userId
      }
      this.closeModal()
      try {
        const res = await ticketOrderApi.create(orderData)
        const orderNo = (res && res.order) ? res.order.orderNo : orderData.orderNo
        uni.showModal({
          title: '预订成功！',
          content: '订单号：' + orderNo + '\n请在"我的订单"中查看',
          showCancel: false,
          success: () => { uni.navigateTo({ url: '/pages/order/ticket-order?status=待支付' }) }
        })
      } catch (e) {
        console.error('创建订单失败:', e)
        uni.showModal({
          title: '预订失败',
          content: '网络异常，订单创建失败，请稍后重试',
          showCancel: false
        })
      }
    },
    goDetail(id) { uni.navigateTo({ url: '/pages/attraction/detail?id=' + id }) },
    handleImageError(e) {
      console.warn('图片加载失败:', e.detail.errMsg)
      // 可以设置默认图片
      this.attraction.cover = '/static/icons/position.png'
    }
  }
}
</script>

<style scoped lang="scss">
.detail-page { background: #F8F9FA; min-height: 100vh; }
.card-shadow { box-shadow: 0 4rpx 20rpx rgba(29,59,90,0.08); }

/* ===== 卡片标题 ===== */
.card-title-row { display: flex; align-items: center; gap: 14rpx; margin-bottom: 24rpx; }
.card-accent-bar { width: 6rpx; height: 32rpx; background: linear-gradient(135deg, #1D3B5A, #2E5F8E); border-radius: 3rpx; }
.card-title { font-size: 30rpx; font-weight: bold; color: #1D3B5A; }

/* ===== Hero ===== */
.hero-section { position: relative; height: 500rpx; }
.hero-img { width: 100%; height: 100%; }
.hero-overlay { position: absolute; inset: 0; background: linear-gradient(transparent 50%, rgba(29,59,90,0.6)); }
.hero-top-actions { position: absolute; top: 30rpx; right: 30rpx; display: flex; gap: 16rpx; }
.hero-btn { width: 72rpx; height: 72rpx; background: rgba(255,255,255,0.85); border-radius: 50%; display: flex; align-items: center; justify-content: center; }
.hero-bottom-info { position: absolute; bottom: 24rpx; left: 30rpx; }
.hero-category-badge { font-size: 24rpx; color: #FFFFFF; background: rgba(29,59,90,0.7); padding: 8rpx 20rpx; border-radius: 20rpx; font-weight: bold; }

/* ===== 基本信息卡片 ===== */
.info-card { background: #FFFFFF; margin: 0 0 20rpx; padding: 30rpx 24rpx; border: 1rpx solid #E8E8E8; }
.info-header { display: flex; justify-content: space-between; align-items: flex-start; margin-bottom: 16rpx; }
.info-name { font-size: 40rpx; font-weight: bold; color: #1D3B5A; flex: 1; }
.info-price-tag { background: linear-gradient(135deg, #1D3B5A, #2E5F8E); border-radius: 20rpx; padding: 8rpx 20rpx; }
.info-price { font-size: 28rpx; color: #FFFFFF; font-weight: bold; }
.info-rating { display: flex; align-items: center; gap: 10rpx; margin-bottom: 16rpx; }
.rating-star { font-size: 28rpx; }
.rating-score { font-size: 32rpx; font-weight: bold; color: #2E5F8E; }
.rating-label { font-size: 24rpx; color: #888; }
.rating-sep { width: 1rpx; height: 28rpx; background: #ddd; margin: 0 6rpx; }
.rating-category { font-size: 24rpx; color: #1D3B5A; background: rgba(29,59,90,0.08); padding: 4rpx 14rpx; border-radius: 20rpx; }
.info-tags { display: flex; flex-wrap: wrap; gap: 10rpx; }
.info-tag { font-size: 22rpx; color: #1D3B5A; background: rgba(29,59,90,0.08); padding: 6rpx 16rpx; border-radius: 20rpx; }

/* ===== 实用信息 ===== */
.practical-card { background: #FFFFFF; margin: 0 0 20rpx; padding: 24rpx; border: 1rpx solid #E8E8E8; }
.practical-item { display: flex; align-items: center; gap: 16rpx; padding: 16rpx 0; border-bottom: 1rpx solid #F0F0F0; }
.practical-item:last-child { border-bottom: none; }
.practical-icon { flex-shrink: 0; display: flex; align-items: center; justify-content: center; }
.practical-content { flex: 1; }
.practical-label { display: block; font-size: 22rpx; color: #888; margin-bottom: 4rpx; }
.practical-value { display: block; font-size: 26rpx; color: #333; }
.practical-action-btn { font-size: 24rpx; color: #1D3B5A; background: rgba(29,59,90,0.08); padding: 8rpx 20rpx; border-radius: 20rpx; border: 1rpx solid #1D3B5A; }

/* ===== 快捷功能 ===== */
.action-card { background: #FFFFFF; margin: 0 0 20rpx; padding: 24rpx; display: grid; grid-template-columns: repeat(4, 1fr); gap: 20rpx; border: 1rpx solid #E8E8E8; }
.action-item { display: flex; flex-direction: column; align-items: center; gap: 10rpx; }
.action-icon-wrap { width: 88rpx; height: 88rpx; border-radius: 24rpx; display: flex; align-items: center; justify-content: center; background: rgba(74,144,226,0.12); }
.action-icon-img { width: 52rpx; height: 52rpx; }
.action-label { font-size: 22rpx; color: #444; }

/* ===== 景点介绍 ===== */
.desc-card { background: #FFFFFF; margin: 0 0 20rpx; padding: 24rpx; border: 1rpx solid #E8E8E8; }
.desc-text { font-size: 28rpx; color: #555; line-height: 1.8; display: block; margin-bottom: 20rpx; }
.tips-box { background: rgba(29,59,90,0.06); border-radius: 14rpx; padding: 20rpx; border-left: 4rpx solid #1D3B5A; }
.tips-title { display: flex; align-items: center; font-size: 26rpx; font-weight: bold; color: #1D3B5A; margin-bottom: 10rpx; }
.tips-text { font-size: 26rpx; color: #666; line-height: 1.6; }

/* ===== 文旅商品 ===== */
.shop-card { background: #FFFFFF; margin: 0 0 20rpx; padding: 24rpx; border: 1rpx solid #E8E8E8; }
.shop-scroll { white-space: nowrap; }
.shop-list { display: inline-flex; gap: 20rpx; }
.shop-item { display: inline-flex; flex-direction: column; width: 180rpx; }
.shop-img { width: 180rpx; height: 160rpx; border-radius: 16rpx; margin-bottom: 10rpx; }
.shop-name { font-size: 24rpx; color: #1D3B5A; font-weight: bold; margin-bottom: 6rpx; white-space: normal; }
.shop-price { font-size: 26rpx; color: #2E5F8E; font-weight: bold; }

/* ===== 游客评价 ===== */
.review-card { background: #FFFFFF; margin: 0 0 20rpx; padding: 24rpx; border: 1rpx solid #E8E8E8; }
.review-header { display: flex; justify-content: space-between; align-items: flex-start; }
.review-score { font-size: 32rpx; font-weight: bold; color: #2E5F8E; }
.review-more { font-size: 24rpx; color: #2E5F8E; }

/* ===== 周边景点 ===== */
.nearby-card { background: #FFFFFF; margin: 0 0 20rpx; padding: 24rpx; border: 1rpx solid #E8E8E8; }
.nearby-list { display: inline-flex; gap: 20rpx; padding: 4rpx; }
.nearby-item { display: inline-flex; flex-direction: column; width: 200rpx; }
.nearby-img { width: 200rpx; height: 150rpx; border-radius: 16rpx; margin-bottom: 10rpx; }
.nearby-info { padding: 0 4rpx; }
.nearby-name { font-size: 24rpx; color: #1D3B5A; font-weight: bold; display: block; margin-bottom: 4rpx; white-space: normal; }
.nearby-price { font-size: 22rpx; color: #2E5F8E; }

/* ===== 底部操作栏 ===== */
.bottom-bar { position: fixed; bottom: 0; left: 0; right: 0; background: #FFFFFF; padding: 20rpx 30rpx 40rpx; display: flex; align-items: center; gap: 20rpx; box-shadow: 0 -4rpx 20rpx rgba(29,59,90,0.10); }
.bottom-left { display: flex; gap: 30rpx; }
.bottom-btn { display: flex; flex-direction: column; align-items: center; gap: 6rpx; }
.icon-img { width: 44rpx; height: 44rpx; }
.bottom-btn-label { font-size: 20rpx; color: #666; }
.bottom-book { flex: 1; background: linear-gradient(135deg, #1D3B5A, #2E5F8E); color: #FFFFFF; font-size: 30rpx; font-weight: bold; text-align: center; padding: 24rpx; border-radius: 50rpx; }

/* ===== 购票弹窗 ===== */
.pay-modal { position: fixed; top: 0; left: 0; right: 0; bottom: 0; background: rgba(0,0,0,0.5); display: flex; align-items: flex-end; z-index: 500; }
.pay-box { width: 100%; background: #FFFFFF; border-radius: 32rpx 32rpx 0 0; padding: 30rpx 30rpx 60rpx; max-height: 85vh; overflow-y: auto; }
.pay-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 24rpx; }
.pay-title { font-size: 34rpx; font-weight: bold; color: #1D3B5A; }
.pay-close { width: 60rpx; height: 60rpx; background: #F8F9FA; border-radius: 50%; display: flex; align-items: center; justify-content: center; font-size: 28rpx; color: #666; }

.ticket-info-card { background: linear-gradient(135deg, #1D3B5A, #2E5F8E); border-radius: 20rpx; padding: 24rpx; margin-bottom: 24rpx; display: flex; justify-content: space-between; align-items: center; }
.ticket-info-name { font-size: 30rpx; font-weight: bold; color: #FFFFFF; flex: 1; }
.ticket-info-price { display: flex; align-items: baseline; }
.price-symbol { font-size: 24rpx; color: #FFD700; }
.price-value { font-size: 44rpx; font-weight: bold; color: #FFD700; }

.pay-info { display: flex; flex-direction: column; gap: 20rpx; margin-bottom: 24rpx; }
.pay-row { display: flex; justify-content: space-between; align-items: center; }
.pay-key { font-size: 26rpx; color: #666; min-width: 120rpx; }
.pay-qty-ctrl { display: flex; align-items: center; gap: 20rpx; }
.qty-btn { width: 50rpx; height: 50rpx; border-radius: 50%; border: 2rpx solid #1D3B5A; display: flex; align-items: center; justify-content: center; font-size: 28rpx; color: #1D3B5A; }
.qty-btn.disabled { border-color: #ddd; color: #ddd; }
.qty-num { font-size: 32rpx; font-weight: bold; color: #1D3B5A; min-width: 50rpx; text-align: center; }
.pay-picker { display: flex; align-items: center; gap: 8rpx; padding: 12rpx 20rpx; background: #F8F9FA; border-radius: 12rpx; }
.pay-picker-text { font-size: 26rpx; color: #333; }
.pay-picker-arrow { font-size: 28rpx; color: #888; }
.pay-input { flex: 1; padding: 12rpx 20rpx; background: #F8F9FA; border-radius: 12rpx; font-size: 26rpx; color: #333; }
.total-row { border-top: 2rpx solid #E8E8E8; padding-top: 20rpx; margin-top: 10rpx; }
.pay-total { font-size: 40rpx; font-weight: bold; color: #2E5F8E; }

.pay-actions { display: flex; gap: 16rpx; }
.pay-back-btn { flex: 0 0 160rpx; background: #F8F9FA; border-radius: 50rpx; padding: 28rpx; text-align: center; font-size: 28rpx; color: #666; }
.pay-btn { flex: 1; background: linear-gradient(135deg, #1D3B5A, #2E5F8E); border-radius: 50rpx; padding: 28rpx; text-align: center; }
.pay-btn-text { font-size: 30rpx; font-weight: bold; color: #FFFFFF; }
</style>
