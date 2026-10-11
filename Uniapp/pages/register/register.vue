<template>
  <view class="register-page">
    <view class="register-bg-decoration">
      <view class="circle circle-1"></view>
      <view class="circle circle-2"></view>
    </view>


    <view class="register-card">
      <view class="card-header">
        <text class="card-title">创建账号</text>
        <text class="card-subtitle">注册后享受更多专属服务</text>
      </view>

      <view class="input-group">
        <view class="input-item">
          <view class="input-wrapper">
            <image class="input-icon" src="/static/icons/mine1.png" mode="aspectFit" />
            <input
              class="input-field"
              v-model="nickname"
              placeholder="请输入昵称"
              maxlength="20"
            />
          </view>
        </view>

        <view class="input-item">
          <view class="input-wrapper">
            <image class="input-icon" src="/static/icons/mine1.png" mode="aspectFit" />
            <input
              class="input-field"
              v-model="name"
              placeholder="请输入真实姓名"
              maxlength="20"
            />
          </view>
        </view>

        <view class="input-item">
          <view class="input-wrapper">
            <image class="input-icon" src="/static/icons/iPhone.png" mode="aspectFit" />
            <input 
              class="input-field" 
              v-model="phone" 
              placeholder="请输入手机号" 
              maxlength="11"
            />
          </view>
        </view>

        <view class="input-item">
          <view class="input-wrapper">
            <image class="input-icon" src="/static/icons/password.png" mode="aspectFit" />
            <input 
              class="input-field" 
              v-model="password" 
              :password="!showPassword" 
              placeholder="请设置密码（6-20位）" 
            />
            <view class="password-toggle" @tap="showPassword = !showPassword">
              <text>{{ showPassword ? '👁' : '👁‍🗨' }}</text>
            </view>
          </view>
        </view>

        <view class="input-item">
          <view class="input-wrapper">
            <image class="input-icon" src="/static/icons/password.png" mode="aspectFit" />
            <input 
              class="input-field" 
              v-model="confirmPassword" 
              :password="!showConfirmPassword" 
              placeholder="请再次输入密码" 
            />
            <view class="password-toggle" @tap="showConfirmPassword = !showConfirmPassword">
              <text>{{ showConfirmPassword ? '👁' : '👁‍🗨' }}</text>
            </view>
          </view>
        </view>
      </view>

      <view class="agreement-section" @tap="agreed = !agreed">
        <view class="checkbox" :class="{ checked: agreed }">
          <text v-if="agreed" class="check-mark">✓</text>
        </view>
        <text class="agree-text">我已阅读并同意</text>
        <text class="link" @tap.stop="showAgreement('user')">《用户协议》</text>
        <text class="link" @tap.stop="showAgreement('privacy')">《隐私政策》</text>
      </view>

      <view class="register-actions">
        <button class="register-btn" @tap="handleRegister" :disabled="isLoading">
          <text class="btn-text">{{ isLoading ? '注册中...' : '立即注册' }}</text>
        </button>
      </view>

      <view class="login-prompt">
        <text class="prompt-text">已有账号？</text>
        <text class="login-link" @tap="goLogin">立即登录</text>
      </view>
    </view>

    <view class="benefits-section">
      <text class="benefits-title">注册专享</text>
      <view class="benefits-list">
        <view class="benefit-item">
          <image class="benefit-icon-img" src="/static/icons/ticket.png" mode="aspectFit" />
          <text class="benefit-text">专属优惠券</text>
        </view>
        <view class="benefit-item">
          <image class="benefit-icon-img" src="/static/icons/cost.png" mode="aspectFit" />
          <text class="benefit-text">会员积分</text>
        </view>
        <view class="benefit-item">
          <image class="benefit-icon-img" src="/static/icons/route.png" mode="aspectFit" />
          <text class="benefit-text">订单管理</text>
        </view>
      </view>
    </view>
  </view>
</template>

<script>
import { userApi } from '../../utils/api.js'

export default {
  data() {
    return {
      nickname: '',
      name: '',
      phone: '',
      password: '',
      confirmPassword: '',
      agreed: false,
      showPassword: false,
      showConfirmPassword: false,
      isLoading: false
    }
  },
  methods: {
    goBack() {
      uni.navigateBack()
    },

    async handleRegister() {
      if (!this.nickname) {
        uni.showToast({ title: '请输入昵称', icon: 'none' })
        return
      }
      if (!this.phone || this.phone.length !== 11) {
        uni.showToast({ title: '请输入正确的手机号', icon: 'none' })
        return
      }
      if (!this.password || this.password.length < 6) {
        uni.showToast({ title: '密码不能少于6位', icon: 'none' })
        return
      }
      if (this.password !== this.confirmPassword) {
        uni.showToast({ title: '两次密码不一致', icon: 'none' })
        return
      }
      if (!this.agreed) {
        uni.showToast({ title: '请先同意用户协议', icon: 'none' })
        return
      }

      this.isLoading = true

      try {
        const result = await userApi.register({
          phone: this.phone,
          password: this.password,
          nickname: this.nickname,
          name: this.name
        })

        console.log('注册成功，后端返回:', result)

        // 保存注册信息到本地存储，供登录页自动填充使用
        uni.setStorageSync('registerInfo', {
          phone: this.phone,
          password: this.password,
          nickname: this.nickname,
          name: this.name
        })

        uni.showToast({ title: '注册成功，请登录', icon: 'success' })
        setTimeout(() => {
          // 跳转到登录页
          uni.redirectTo({ url: '/pages/login/login?fromRegister=true' })
        }, 800)
      } catch (error) {
        console.error('注册失败:', error)
      } finally {
        this.isLoading = false
      }
    },

    goLogin() {
      uni.redirectTo({ url: '/pages/login/login' })
    },

    showAgreement(type) {
      uni.showModal({
        title: type === 'user' ? '用户协议' : '隐私政策',
        content: type === 'user' ? '这里是用户协议内容...' : '这里是隐私政策内容...',
        showCancel: false
      })
    }
  }
}
</script>

<style scoped lang="scss">
.register-page {
  min-height: 100vh;
  background: linear-gradient(135deg, #1D3B5A 0%, #2E5F8E 50%, #3D7AB5 100%);
  position: relative;
  overflow: hidden;
}

.register-bg-decoration {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  overflow: hidden;
}

.circle {
  position: absolute;
  border-radius: 50%;
  opacity: 0.1;
}

.circle-1 {
  width: 300rpx;
  height: 300rpx;
  background: #D4A373;
  top: -80rpx;
  left: -80rpx;
}

.circle-2 {
  width: 200rpx;
  height: 200rpx;
  background: #FDFCF8;
  bottom: 20%;
  right: -60rpx;
}

.register-header {
  padding: 60rpx 30rpx 20rpx;
  position: relative;
  z-index: 1;
}

.back-btn {
  width: 80rpx;
  height: 80rpx;
  display: flex;
  align-items: center;
  justify-content: center;
}

.back-icon {
  font-size: 60rpx;
  color: #FDFCF8;
  font-weight: bold;
}

.register-card {
  background: #FDFCF8;
  border-radius: 40rpx 40rpx 0 0;
  padding: 40rpx;
  position: relative;
  z-index: 1;
  min-height: 65vh;
}

.card-header {
  text-align: center;
  margin-bottom: 40rpx;
}

.card-title {
  display: block;
  font-size: 40rpx;
  font-weight: bold;
  color: #1D3B5A;
  margin-bottom: 12rpx;
}

.card-subtitle {
  display: block;
  font-size: 26rpx;
  color: #999;
}

.input-group {
  display: flex;
  flex-direction: column;
  gap: 24rpx;
  margin-bottom: 30rpx;
}

.input-item {
  position: relative;
}

.input-wrapper {
  display: flex;
  align-items: center;
  background: #F8F9FA;
  border-radius: 16rpx;
  padding: 0 24rpx;
  height: 100rpx;
  gap: 16rpx;
  border: 2rpx solid transparent;
  transition: all 0.3s ease;
}

.input-wrapper:focus-within {
  border-color: #1D3B5A;
  background: #FDFCF8;
}

.input-icon {
  width: 40rpx;
  height: 40rpx;
  flex-shrink: 0;
}

.input-field {
  flex: 1;
  font-size: 28rpx;
  color: #333;
}

.password-toggle {
  padding: 10rpx;
}

.password-toggle text {
  font-size: 32rpx;
}

.agreement-section {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 8rpx;
  margin-bottom: 30rpx;
  padding: 0 10rpx;
}

.checkbox {
  width: 32rpx;
  height: 32rpx;
  border: 2rpx solid #ddd;
  border-radius: 6rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.checkbox.checked {
  background: #1D3B5A;
  border-color: #1D3B5A;
}

.check-mark {
  color: #FDFCF8;
  font-size: 20rpx;
}

.agree-text {
  font-size: 22rpx;
  color: #999;
}

.link {
  font-size: 22rpx;
  color: #1D3B5A;
}

.register-actions {
  margin-bottom: 30rpx;
}

.register-btn {
  width: 100%;
  background: linear-gradient(135deg, #1D3B5A 0%, #2E5F8E 100%);
  border-radius: 50rpx;
  padding: 30rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  border: none;
  box-shadow: 0 8rpx 24rpx rgba(30, 58, 95, 0.3);
}

.register-btn[disabled] {
  background: #ccc;
  box-shadow: none;
}

.register-btn .btn-text {
  font-size: 32rpx;
  font-weight: bold;
  color: #FDFCF8;
}

.login-prompt {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 8rpx;
}

.prompt-text {
  font-size: 26rpx;
  color: #999;
}

.login-link {
  font-size: 26rpx;
  color: #D4A373;
  font-weight: bold;
}

.benefits-section {
  background: rgba(255, 255, 255, 0.1);
  padding: 30rpx 40rpx;
  margin-top: -20rpx;
}

.benefits-title {
  display: block;
  text-align: center;
  font-size: 26rpx;
  color: rgba(255, 255, 255, 0.8);
  margin-bottom: 20rpx;
}

.benefits-list {
  display: flex;
  justify-content: space-around;
}

.benefit-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8rpx;
}

.benefit-icon {
  font-size: 36rpx;
}

.benefit-icon-img {
  width: 36rpx;
  height: 36rpx;
}

.benefit-text {
  font-size: 22rpx;
  color: rgba(255, 255, 255, 0.8);
}
</style>



