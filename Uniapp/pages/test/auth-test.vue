<template>
  <view class="test-page">
    <view class="test-header">
      <text class="test-title">游客/用户登录测试</text>
    </view>

    <view class="test-section">
      <text class="section-title">当前状态</text>
      <view class="status-card">
        <text class="status-text">登录状态 {{ userInfo.isLogin ? '已登录' : '未登录' }}</text>
        <text class="status-text">用户类型：{{ userInfo.loginType || '未知' }}</text>
        <text class="status-text">昵称：{{ userInfo.nickname || '游客' }}</text>
        <text class="status-text">Token: {{ currentToken ? currentToken.substring(0, 20) + '...' : '无' }}</text>
      </view>
    </view>

    <view class="test-section">
      <text class="section-title">功能测试</text>
      <view class="test-buttons">
        <button class="test-btn" @tap="testGuestLogin">游客登录</button>
        <button class="test-btn" @tap="testUserLogin">用户登录</button>
        <button class="test-btn" @tap="testApiCall">测试 API 调用</button>
        <button class="test-btn" @tap="testPermission">测试权限检查</button>
        <button class="test-btn danger" @tap="testLogout">退出登录</button>
      </view>
    </view>

    <view class="test-section">
      <text class="section-title">测试结果</text>
      <scroll-view class="result-scroll" scroll-y>
        <view class="result-content">
          <text class="result-text" v-for="(log, index) in logs" :key="index">{{ log }}</text>
        </view>
      </scroll-view>
    </view>
  </view>
</template>

<script>
import auth, { isGuestMode, isLoggedIn, requireLogin, userApi } from '../../utils/auth.js'

export default {
  data() {
    return {
      userInfo: {},
      currentToken: '',
      logs: []
    }
  },
  onShow() {
    this.refreshStatus()
  },
  methods: {
    refreshStatus() {
      this.userInfo = auth.getUserInfo()
      this.currentToken = uni.getStorageSync('token') || ''
    },

    addLog(message) {
      const time = new Date().toLocaleTimeString()
      this.logs.unshift(`[${time}] ${message}`)
      if (this.logs.length > 20) {
        this.logs.pop()
      }
    },

    async testGuestLogin() {
      this.addLog('开始游客登录测试...')
      try {
        const result = await userApi.guestLogin()
        this.addLog(`游客登录成功：${JSON.stringify(result)}`)

        auth.setUserInfo({
          isLogin: false,
          nickname: '游客',
          loginType: 'guest',
          token: result.token,
          loginTime: new Date().toISOString()
        })

        this.refreshStatus()
        this.addLog('游客登录状态已更新')
      } catch (error) {
        this.addLog(`游客登录失败：${error.message}`)
        // 降级到本地游客模式
        auth.setUserInfo({
          isLogin: false,
          nickname: '游客',
          loginType: 'guest',
          token: 'guest_local_' + Date.now(),
          loginTime: new Date().toISOString()
        })
        this.refreshStatus()
        this.addLog('已切换到本地游客模式')
      }
    },

    async testUserLogin() {
      this.addLog('开始用户登录测试...')
      try {
        // 使用模拟数据登录
        const result = await userApi.login({
          phone: '13800000000',
          password: '123456'
        })

        auth.setUserInfo({
          isLogin: true,
          phone: result.phone,
          nickname: result.nickname || '侗乡旅行者',
          avatar: result.avatar || '',
          userId: result.id,
          token: result.token,
          loginType: 'password',
          loginTime: new Date().toISOString()
        })

        this.refreshStatus()
        this.addLog('用户登录成功')
      } catch (error) {
        this.addLog(`用户登录失败：${error.message}`)
      }
    },

    async testApiCall() {
      this.addLog('开始API调用测试...')
      try {
        const result = await userApi.getMe()
        this.addLog(`API调用成功: ${JSON.stringify(result)}`)
      } catch (error) {
        this.addLog(`API调用失败: ${error.message}`)
      }
    },

    testPermission() {
      this.addLog('开始权限检查测试...')

      const guest = isGuestMode()
      const logged = isLoggedIn()

      this.addLog(`isGuestMode(): ${guest}`)
      this.addLog(`isLoggedIn(): ${logged}`)

      // 测试 requireLogin
      const canOperate = requireLogin('测试操作')
      this.addLog(`requireLogin('测试操作'): ${canOperate}`)
    },

    testLogout() {
      auth.logout()
      this.refreshStatus()
      this.addLog('已退出登录')
    }
  }
}
</script>

<style scoped lang="scss">
.test-page {
  min-height: 100vh;
  background: #F8FAFC;
  padding: 20rpx;
}

.test-header {
  text-align: center;
  padding: 40rpx 0;
}

.test-title {
  font-size: 36rpx;
  font-weight: bold;
  color: #1D3B5A;
}

.test-section {
  background: #FFFFFF;
  border-radius: 16rpx;
  padding: 24rpx;
  margin-bottom: 20rpx;
}

.section-title {
  font-size: 30rpx;
  font-weight: 600;
  color: #2C3E50;
  display: block;
  margin-bottom: 16rpx;
}

.status-card {
  background: #F8FAFC;
  border-radius: 12rpx;
  padding: 20rpx;
}

.status-text {
  display: block;
  font-size: 26rpx;
  color: #475569;
  margin-bottom: 8rpx;
}

.test-buttons {
  display: flex;
  flex-direction: column;
  gap: 16rpx;
}

.test-btn {
  background: linear-gradient(135deg, #1D3B5A 0%, #2E5F8E 100%);
  color: #FFFFFF;
  border-radius: 12rpx;
  font-size: 28rpx;
  padding: 20rpx;
  border: none;

  &.danger {
    background: linear-gradient(135deg, #DC3545 0%, #C82333 100%);
  }
}

.result-scroll {
  height: 300rpx;
  background: #F8FAFC;
  border-radius: 12rpx;
  padding: 16rpx;
}

.result-content {
  display: flex;
  flex-direction: column;
  gap: 8rpx;
}

.result-text {
  font-size: 24rpx;
  color: #475569;
  word-break: break-all;
}
</style>