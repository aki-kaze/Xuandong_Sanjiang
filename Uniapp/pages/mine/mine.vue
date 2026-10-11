<template>
  <view class="mine-page">
    <!-- 用户信息卡片 -->
    <view class="user-card">
      <view class="user-info" @tap="goLogin">
        <image v-if="userInfo.isLogin && userInfo.avatar" class="user-avatar-img" :src="userInfo.avatar" mode="aspectFill" @tap.stop="changeAvatar" />
        <view v-else class="user-avatar" @tap.stop="goLogin">{{ userInfo.isLogin ? userInfo.nickname.charAt(0) : (userInfo.loginType === 'guest' ? '游' : '登') }}</view>
        <view class="user-detail">
          <text class="user-name">{{ userInfo.isLogin ? userInfo.nickname : (userInfo.loginType === 'guest' ? '游客模式' : '点击登录') }}</text>
          <view class="user-badge">{{ userInfo.isLogin ? '三江荣誉寨民' : (userInfo.loginType === 'guest' ? '登录享受完整服务' : '登录享受更多服务') }}</view>
        </view>
  
      </view>
      <view class="user-stats">
        <view class="stat-item" @tap="goPage('/pages/favorite/favorite')">
          <text class="stat-num">{{ stats.favorite }}</text>
          <text class="stat-label">收藏</text>
        </view>
        <view class="stat-divider"></view>
        <view class="stat-item" @tap="goPage('/pages/order/order')">
          <text class="stat-num">{{ stats.order }}</text>
          <text class="stat-label">订单</text>
        </view>
        <view class="stat-divider"></view>
        <view class="stat-item" @tap="goPage('/pages/footprint/footprint')">
          <text class="stat-num">{{ stats.footprint }}</text>
          <text class="stat-label">足迹</text>
        </view>
        <view class="stat-divider"></view>
        <view class="stat-item" @tap="goPage('/pages/heritage/passport')">
          <text class="stat-num">{{ stats.stamps }}</text>
          <text class="stat-label">印章</text>
        </view>
      </view>
    </view>

    <!-- 游客模式提示 -->
    <view class="guest-banner" v-if="userInfo.loginType === 'guest'" @tap="goLogin">
      <text class="guest-banner-text">🔒 登录后可收藏、下单、发布内容</text>
      <text class="guest-banner-btn">立即登录</text>
    </view>

    <!-- 我的订单 -->
    <view class="section-card card-shadow">
      <view class="section-header">
        <view class="section-title-wrap">
          <view class="section-accent-bar"></view>
          <text class="section-title">我的订单</text>
        </view>
        <text class="section-more" @tap="goPage('/pages/order/order')">全部订单></text>
      </view>
      <view class="order-grid">
        <view class="order-item" v-for="item in orderTypes" :key="item.id" @tap="goOrderList(item.status)">
          <view class="order-icon-wrap" :style="{ background: item.bg }">
            <image :src="item.icon" class="order-icon" mode="aspectFit"></image>
          </view>
          <text class="order-label">{{ item.label }}</text>
          <view class="order-badge" v-if="item.badge > 0">{{ item.badge }}</view>
        </view>
      </view>
    </view>

    <!-- 功能菜单 -->
    <view class="section-card card-shadow">
      <view class="menu-item" v-for="item in menuItems" :key="item.id" @tap="goPage(item.path)">
        <view class="menu-left">
          <view class="menu-icon-wrap" :style="{ background: item.bg }">
            <image :src="item.icon" class="menu-icon" mode="aspectFit"></image>
          </view>
          <text class="menu-label">{{ item.label }}</text>
        </view>
        <text class="menu-arrow">></text>
      </view>
    </view>

    <!-- 三江旅游服务 -->
    <view class="section-card card-shadow">
      <view class="section-header" style="margin-bottom:24rpx">
        <view class="section-title-wrap">
          <view class="section-accent-bar"></view>
          <text class="section-title">三江旅游服务</text>
        </view>
      </view>
      <view class="service-grid">
        <view class="service-item" v-for="item in services" :key="item.id" @tap="goPage(item)">
          <view class="service-icon-wrap" :style="{ background: item.bg }">
            <image :src="item.icon" class="service-icon" mode="aspectFit"></image>
          </view>
          <text class="service-label">{{ item.label }}</text>
        </view>
      </view>
    </view>

    <!-- 退出登录 -->
    <view class="logout-btn" v-if="userInfo.isLogin" @tap="logout">退出登录</view>

    <!-- 版权信息 -->
    <view class="footer-info">
      <text class="footer-text">寻侗三江 · 千年侗寨非遗共生</text>
    </view>

    <view style="height: 40rpx;"></view>
  </view>
</template>

<script>
import auth, { getRandomAvatar, isGuestMode, requireLogin, checkLoginStatus } from '../../utils/auth.js'
import { favoritesUtil } from '../../utils/favorites.js'
import { userApi } from '../../utils/api.js'
import { getStampCount } from '../../utils/stamps.js'
import config from '../../utils/config.js'
import { loadAllUserOrders, getLocalOrders, getCurrentUserId } from '../../utils/order-utils.js'

export default {
  data() {
    return {
      userInfo: { isLogin: false, nickname: '游客' },
      stats: { favorite: 0, order: 0, footprint: 0, stamps: 0 },
      orderTypes: [
        { id: 1, icon: '/static/icons/daizhifu.png', label: '待支付', status: '待支付', bg: 'rgba(74,144,226,0.12)', badge: 0 },
        { id: 2, icon: '/static/icons/daiyong.png', label: '待使用', status: '待使用', bg: 'rgba(74,144,226,0.12)', badge: 0 },
        { id: 3, icon: '/static/icons/yiwancheng.png', label: '已完成', status: '已完成', bg: 'rgba(74,144,226,0.12)', badge: 0 },
        { id: 4, icon: '/static/icons/all.png', label: '全部', status: '', bg: 'rgba(74,144,226,0.12)', badge: 0 }
      ],
      menuItems: [
        { id: 1, icon: '/static/icons/love.png', label: '我的收藏', path: '/pages/favorite/favorite', bg: 'rgba(74,144,226,0.12)' },
        { id: 2, icon: '/static/icons/我的关注.png', label: '我的关注', path: '/pages/follow/follow', bg: 'rgba(74,144,226,0.12)' },
        { id: 3, icon: '/static/icons/shop.png', label: '侗乡特产', path: '/pages/shop/shop', bg: 'rgba(74,144,226,0.12)' },
        { id: 4, icon: '/static/icons/setting.png', label: '设置', path: '/pages/setting/setting', bg: 'rgba(74,144,226,0.12)' }
      ],
      services: [
        { id: 1, icon: '/static/icons/map.png', label: '地图导航', path: '/pages/map/map', type: 'navigate', bg: 'rgba(74,144,226,0.12)' },
        { id: 2, icon: '/static/icons/ai.png', label: 'AI 助手', path: '/pages/ai/ai', type: 'tab', bg: 'rgba(74,144,226,0.12)' },
        { id: 3, icon: '/static/icons/ticket.png', label: '购票预订', path: '/pages/ticket/ticket', type: 'navigate', bg: 'rgba(74,144,226,0.12)' },
        { id: 4, icon: '/static/icons/hotel.png', label: '住宿预订', path: '/pages/hotel/hotel', type: 'navigate', bg: 'rgba(74,144,226,0.12)' },
        { id: 5, icon: '/static/icons/route.png', label: '路线规划', path: '/pages/route/route', type: 'navigate', bg: 'rgba(74,144,226,0.12)'},
        { id: 6, icon: '/static/icons/food.png', label: '侗乡美食', path: '/pages/food/food', type: 'navigate', bg: 'rgba(74,144,226,0.12)' }
      ]
    }
  },
  onShow() {
    this.userInfo = auth.getUserInfo()
    this.stats.favorite = favoritesUtil.getAllFavorites().length
    this.stats.footprint = uni.getStorageSync('footprint') || 0
    this.stats.stamps = getStampCount()
    this.loadOrderStats()
  },
  methods: {
    async loadOrderStats() {
      try {
        const userId = getCurrentUserId()
        let orders = []
        if (userId > 0) {
          orders = await loadAllUserOrders(userId)
        } else {
          orders = getLocalOrders()
        }
        this.stats.order = orders.length
        this.updateOrderBadges(orders)
      } catch (e) {
        console.error('加载订单统计失败:', e)
        // 降级使用本地数据
        const orders = getLocalOrders()
        this.stats.order = orders.length
        this.updateOrderBadges(orders)
      }
    },
    updateOrderBadges(orders) {
      // 统计各状态订单数量
      let pendingPay = 0
      let pendingUse = 0
      let completed = 0
      
      orders.forEach(order => {
        const status = order.status || ''
        if (status.includes('待支付')) {
          pendingPay++
        } else if (status.includes('待使用') || status.includes('已支付')) {
          pendingUse++
        } else if (status.includes('已完成') || status.includes('已使用')) {
          completed++
        }
      })
      
      // 更新角标显示
      this.orderTypes[0].badge = pendingPay
      this.orderTypes[1].badge = pendingUse
      this.orderTypes[2].badge = completed
      this.orderTypes[3].badge = orders.length
      
      console.log('订单统计更新:', { pendingPay, pendingUse, completed, total: orders.length })
    },
    goLogin() {
      if (!this.userInfo.isLogin) {
        uni.navigateTo({ url: '/pages/login/login' })
      }
    },
    async goPage(item) {
      const path = typeof item === 'string' ? item : item.path
      const type = typeof item === 'object' ? item.type : 'navigate'
      
      // 检查是否需要登录
      const needLoginPaths = ['/pages/favorite/favorite', '/pages/follow/follow', '/pages/order/order', '/pages/shop/shop']
      if (needLoginPaths.includes(path)) {
        if (!requireLogin('此功能需要登录')) return
        const valid = await checkLoginStatus()
        if (!valid) return
      }
      
      // 根据类型选择跳转方式
      if (type === 'tab') {
        uni.switchTab({ url: path })
      } else {
        uni.navigateTo({ url: path })
      }
    },
    async goOrderList(status) {
      if (!requireLogin('查看订单')) return
      const valid = await checkLoginStatus()
      if (!valid) return
      if (status === '待支付') {
        uni.navigateTo({ url: '/pages/order/pending-order' })
      } else if (status === '待使用') {
        uni.navigateTo({ url: '/pages/order/pending-use' })
      } else if (status === '已完成') {
        uni.navigateTo({ url: '/pages/order/completed-order' })
      } else {
        uni.navigateTo({ url: '/pages/order/order' })
      }
    },
    logout() {
      uni.showModal({
        title: '提示',
        content: '确定退出登录？',
        success: (res) => {
          if (res.confirm) {
            auth.logout()
            this.userInfo = auth.getUserInfo()
            uni.showToast({ title: '已退出登录', icon: 'success' })
          }
        }
      })
    },
    changeAvatar() {
      if (!this.userInfo.isLogin) {
        uni.navigateTo({ url: '/pages/login/login' })
        return
      }
      uni.chooseImage({
        count: 1,
        sizeType: ['compressed'],
        sourceType: ['album', 'camera'],
        success: async (res) => {
          const tempFilePath = res.tempFilePaths[0]
          uni.showLoading({ title: '上传中...' })
          try {
            // 上传图片到服务器
            const uploadRes = await new Promise((resolve, reject) => {
              uni.uploadFile({
                url: config.baseURL + '/upload/avatar',
                filePath: tempFilePath,
                name: 'file',
                header: {
                  'Authorization': uni.getStorageSync('token') ? `Bearer ${uni.getStorageSync('token')}` : ''
                },
                success: (r) => {
                  try { resolve(JSON.parse(r.data)) } catch (e) { reject(r) }
                },
                fail: reject
              })
            })

            if (uploadRes.code === 200 || uploadRes.data) {
              const avatarUrl = uploadRes.data || uploadRes.url
              // 更新用户头像
              await userApi.update({ id: this.userInfo.userId || this.userInfo.id, avatar: avatarUrl })
              // 更新本地存储
              this.userInfo.avatar = avatarUrl
              uni.setStorageSync('userInfo', this.userInfo)
              uni.showToast({ title: '头像已更新', icon: 'success' })
            } else {
              throw new Error(uploadRes.msg || '上传失败')
            }
          } catch (e) {
            console.error('头像上传失败', e)
            // 降级：使用本地随机头像
            const newAvatar = getRandomAvatar()
            try {
              await userApi.update({ id: this.userInfo.userId || this.userInfo.id, avatar: newAvatar })
              this.userInfo.avatar = newAvatar
              uni.setStorageSync('userInfo', this.userInfo)
              uni.showToast({ title: '头像已更新', icon: 'success' })
            } catch (e2) {
              uni.showToast({ title: '更新失败，请重试', icon: 'none' })
            }
          } finally {
            uni.hideLoading()
          }
        }
      })
    }
  }
}
</script>

<style scoped lang="scss">
.mine-page { background: #F8FAFC; min-height: 100vh; }
.card-shadow { box-shadow: 0 4rpx 20rpx rgba(74,144,226,0.12); }

/* ===== 用户卡片 ===== */
.user-card {
  background: linear-gradient(135deg, #1D3B5A 0%, #2E5F8E 100%);
  padding: 60rpx 30rpx 0;
  border-radius: 0 0 40rpx 40rpx;
}
.user-info { display: flex; align-items: center; gap: 24rpx; margin-bottom: 40rpx; }
.user-avatar {
  width: 120rpx; height: 120rpx; border-radius: 50%;
  background: rgba(255,255,255,0.2); display: flex; align-items: center;
  justify-content: center; font-size: 48rpx; font-weight: 600;
  color: #FFFFFF; border: 4rpx solid rgba(255,255,255,0.4);
  box-shadow: 0 4rpx 20rpx rgba(0,0,0,0.2);
  overflow: hidden;
}
.user-avatar-img {
  width: 120rpx; height: 120rpx; border-radius: 50%;
  border: 4rpx solid rgba(255,255,255,0.4);
  box-shadow: 0 4rpx 20rpx rgba(0,0,0,0.2);
}
.user-detail { flex: 1; }
.user-name { display: block; font-size: 36rpx; font-weight: 600; color: #FFFFFF; margin-bottom: 10rpx; }
.user-badge { display: inline-block; font-size: 22rpx; color: rgba(255,255,255,0.85); background: rgba(255,255,255,0.15); padding: 4rpx 18rpx; border-radius: 20rpx; }
.user-arrow { font-size: 40rpx; color: rgba(255,255,255,0.6); }

.user-stats { background: rgba(255,255,255,0.15); border-radius: 20rpx 20rpx 0 0; padding: 30rpx; display: flex; justify-content: space-around; align-items: center; }
.stat-item { display: flex; flex-direction: column; align-items: center; gap: 8rpx; }
.stat-num { font-size: 40rpx; font-weight: 600; color: #FFFFFF; }
.stat-label { font-size: 24rpx; color: rgba(255,255,255,0.75); }
.stat-divider { width: 1rpx; height: 60rpx; background: rgba(255,255,255,0.3); }

/* ===== Section Card ===== */
.section-card { background: #FFFFFF; margin: 20rpx 24rpx; border-radius: 20rpx; padding: 24rpx; }
.section-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 24rpx; }
.section-title-wrap { display: flex; align-items: center; gap: 14rpx; }
.section-accent-bar { width: 6rpx; height: 32rpx; background: #4A90E2; border-radius: 3rpx; }
.section-title { font-size: 30rpx; font-weight: 600; color: #2C3E50; }
.section-more { font-size: 24rpx; color: #4A90E2; }

/* ===== 订单Grid ===== */
.order-grid { display: grid; grid-template-columns: repeat(4, 1fr); gap: 20rpx; }
.order-item { display: flex; flex-direction: column; align-items: center; gap: 12rpx; position: relative; }
.order-icon-wrap { width: 88rpx; height: 88rpx; border-radius: 24rpx; display: flex; align-items: center; justify-content: center; }
.order-icon { width: 48rpx; height: 48rpx; }
.order-label { font-size: 24rpx; color: #475569; }
.order-badge { position: absolute; top: -8rpx; right: 10rpx; background: #4A90E2; color: #FFFFFF; font-size: 18rpx; width: 32rpx; height: 32rpx; border-radius: 50%; display: flex; align-items: center; justify-content: center; }

/* ===== 菜单Item ===== */
.menu-item { display: flex; align-items: center; justify-content: space-between; padding: 24rpx 0; border-bottom: 1rpx solid #F1F5F9; }
.menu-item:last-child { border-bottom: none; }
.menu-left { display: flex; align-items: center; gap: 20rpx; }
.menu-icon-wrap { width: 72rpx; height: 72rpx; border-radius: 18rpx; display: flex; align-items: center; justify-content: center; }
.menu-icon { width: 40rpx; height: 40rpx; }
.menu-label { font-size: 28rpx; color: #334155; }
.menu-arrow { font-size: 36rpx; color: #CBD5E1; }

/* ===== 服务Grid ===== */
.service-grid { display: grid; grid-template-columns: repeat(3, 1fr); gap: 20rpx; }
.service-item { display: flex; flex-direction: column; align-items: center; gap: 12rpx; }
.service-icon-wrap { width: 88rpx; height: 88rpx; border-radius: 24rpx; display: flex; align-items: center; justify-content: center; }
.service-icon { width: 48rpx; height: 48rpx; }
.service-label { font-size: 24rpx; color: #475569; }

/* ===== 游客提示 ===== */
.guest-banner {
  background: linear-gradient(135deg, #FFF7E6 0%, #FFF2E6 100%);
  margin: 20rpx 24rpx;
  border-radius: 16rpx;
  padding: 24rpx;
  display: flex;
  justify-content: space-between;
  align-items: center;
  border: 2rpx solid #FFD591;
}

.guest-banner-text {
  font-size: 26rpx;
  color: #D46B08;
  flex: 1;
}

.guest-banner-btn {
  background: linear-gradient(135deg, #1D3B5A 0%, #2E5F8E 100%);
  color: #FFFFFF;
  font-size: 24rpx;
  padding: 12rpx 24rpx;
  border-radius: 20rpx;
  font-weight: 600;
}

/* ===== 核销入口 ===== */
.verify-section { padding: 10rpx 0; }
.verify-input-wrap { display: flex; gap: 16rpx; align-items: center; }
.verify-input { flex: 1; height: 72rpx; background: #F1F5F9; border-radius: 16rpx; padding: 0 20rpx; font-size: 30rpx; letter-spacing: 8rpx; text-align: center; }
.verify-btn { background: #4A90E2; color: #FFFFFF; font-size: 28rpx; padding: 16rpx 36rpx; border-radius: 16rpx; font-weight: 600; }
.service-icon { width: 48rpx; height: 48rpx; }
.service-label { font-size: 24rpx; color: #334155; }

/* ===== 退出、版权 ===== */
.logout-btn { margin: 20rpx 24rpx; background: #FFFFFF; border-radius: 50rpx; padding: 28rpx; text-align: center; font-size: 28rpx; color: #4A90E2; box-shadow: 0 4rpx 20rpx rgba(74,144,226,0.12); }
.footer-info { text-align: center; padding: 20rpx; }
.footer-text { font-size: 22rpx; color: #94A3B8; letter-spacing: 0.1em; }
</style>