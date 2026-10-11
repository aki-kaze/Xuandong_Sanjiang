<template>
  <view class="setting-page">
    <view class="setting-section">
      <text class="section-title">账号设置</text>
      <view class="setting-list">
        <view class="setting-item" @tap="editProfile">
          <text class="item-label">个人信息</text>
          <view class="item-right">
            <text class="item-value">{{ userInfo.nickname || '未设置' }}</text>
            <text class="item-arrow">></text>
          </view>
        </view>
        <view class="setting-item" @tap="showChangePasswordModal">
          <text class="item-label">修改密码</text>
          <text class="item-arrow">></text>
        </view>
        <view class="setting-item" @tap="bindPhone">
          <text class="item-label">手机绑定</text>
          <view class="item-right">
            <text class="item-value">{{ phoneDisplay }}</text>
            <text class="item-arrow">></text>
          </view>
        </view>
      </view>
    </view>

    <view class="setting-section">
      <text class="section-title">通用设置</text>
      <view class="setting-list">
        <view class="setting-item">
          <text class="item-label">消息通知</text>
          <switch :checked="notificationEnabled" @change="toggleNotification" color="#2E5F8E" />
        </view>
        <view class="setting-item" @tap="clearCache">
          <text class="item-label">清除缓存</text>
          <view class="item-right">
            <text class="item-value">{{ cacheSize }}</text>
            <text class="item-arrow">></text>
          </view>
        </view>
        <view class="setting-item" @tap="checkUpdate">
          <text class="item-label">检查更新</text>
          <view class="item-right">
            <text class="item-value">v1.0.0</text>
            <text class="item-arrow">></text>
          </view>
        </view>
      </view>
    </view>

    <view class="setting-section">
      <text class="section-title">关于</text>
      <view class="setting-list">
        <view class="setting-item" @tap="showAbout">
          <text class="item-label">关于寻侗三江</text>
          <text class="item-arrow">></text>
        </view>
        <view class="setting-item" @tap="showPrivacy">
          <text class="item-label">隐私政策</text>
          <text class="item-arrow">></text>
        </view>
        <view class="setting-item" @tap="showAgreement">
          <text class="item-label">用户协议</text>
          <text class="item-arrow">></text>
        </view>
      </view>
    </view>

    <view class="logout-wrap" v-if="isLogin">
      <view class="logout-btn" @tap="logout">退出登录</view>
    </view>

    <!-- 个人信息修改弹窗 -->
    <view class="modal-mask" v-if="showProfileModal" @tap="closeProfileModal">
      <view class="modal-box" @tap.stop>
        <view class="modal-header">
          <text class="modal-title">修改个人信息</text>
          <text class="modal-close" @tap="closeProfileModal">x</text>
        </view>
        <view class="modal-body">
          <!-- 头像选择 -->
          <view class="avatar-section">
            <text class="avatar-label">头像</text>
            <view class="avatar-picker" @tap="chooseAvatar">
              <image class="avatar-preview" :src="editAvatar || defaultAvatar" mode="aspectFill"></image>
              <text class="avatar-change">点击更换</text>
            </view>
          </view>
          <view class="form-row">
            <text class="form-label">昵称</text>
            <input class="modal-input" v-model="editNickname" placeholder="请输入昵称" maxlength="20" />
          </view>
          <text class="modal-hint">{{ editNickname.length }}/20</text>
          <view class="form-row">
            <text class="form-label">姓名</text>
            <input class="modal-input" v-model="editName" placeholder="请输入真实姓名" maxlength="20" />
          </view>
          <view class="form-row">
            <text class="form-label">性别</text>
            <view class="gender-select">
              <view class="gender-option" :class="{ active: editGender === 0 }" @tap="editGender = 0">
                <text class="gender-icon-text">♂</text>
                <text>男</text>
              </view>
              <view class="gender-option" :class="{ active: editGender === 1 }" @tap="editGender = 1">
                <text class="gender-icon-text">♀</text>
                <text>女</text>
              </view>
            </view>
          </view>
        </view>
        <view class="modal-actions">
          <view class="modal-cancel" @tap="closeProfileModal">取消</view>
          <view class="modal-confirm" @tap="saveProfile">保存</view>
        </view>
      </view>
    </view>

    <!-- 修改密码弹窗 -->
    <view class="modal-mask" v-if="showPasswordModal" @tap="showPasswordModal = false">
      <view class="modal-box" @tap.stop>
        <view class="modal-header">
          <text class="modal-title">修改密码</text>
          <text class="modal-close" @tap="showPasswordModal = false">x</text>
        </view>
        <view class="modal-body">
          <view class="form-row">
            <text class="form-label">原密码</text>
            <input class="modal-input" v-model="oldPassword" type="password" placeholder="请输入原密码" maxlength="20" />
          </view>
          <view class="form-row">
            <text class="form-label">新密码</text>
            <input class="modal-input" v-model="newPassword" type="password" placeholder="请输入新密码（至少6位）" maxlength="20" />
          </view>
          <view class="form-row">
            <text class="form-label">确认密码</text>
            <input class="modal-input" v-model="confirmPassword" type="password" placeholder="请再次输入新密码" maxlength="20" />
          </view>
        </view>
        <view class="modal-actions">
          <view class="modal-cancel" @tap="showPasswordModal = false">取消</view>
          <view class="modal-confirm" @tap="submitChangePassword">确认修改</view>
        </view>
      </view>
    </view>

    <!-- 关于我们弹窗 -->
    <view class="modal-mask" v-if="showAboutModal" @tap="showAboutModal = false">
      <view class="modal-box about-box" @tap.stop>
        <view class="modal-header">
          <text class="modal-title">关于寻侗三江</text>
          <text class="modal-close" @tap="showAboutModal = false">x</text>
        </view>
        <scroll-view class="modal-scroll" scroll-y>
          <view class="about-content">
            <view class="about-logo">
              <text class="about-logo-text">寻侗三江</text>
              <text class="about-version">v1.0.0</text>
            </view>
            <text class="about-desc">
              寻侗三江是一款专注于广西三江侗族自治县文化旅游的导览小程序，为用户提供景点导览、非遗文化、美食推荐、住宿预订、路线规划等服务</text>
            <text class="about-section-title">主要功能</text>
            <text class="about-feature">景点导览：程阳八寨、布央茶园、丹洲古镇等</text>
            <text class="about-feature">非遗文化：侗族大歌、侗锦编织、油茶制作</text>
            <text class="about-feature">美食推荐：侗族油茶、酸鱼酸肉、糯米饭</text>
            <text class="about-feature">住宿预订：特色民宿、星级酒店</text>
            <text class="about-feature">路线规划：智能定制旅游路线</text>
            <text class="about-feature">AI助手：智能问答与行程规划</text>
            <text class="about-feature">社区互动：分享旅行故事与攻略</text>
            <text class="about-section-title">联系我们</text>
            <text class="about-feature">官方邮箱：contact@xundong.com</text>
            <text class="about-feature">客服电话：00-XXX-XXXX</text>
            <text class="about-copyright">© 2026 寻侗三江团队 版权所有</text>
          </view>
        </scroll-view>
      </view>
    </view>

    <!-- 隐私政策弹窗 -->
    <view class="modal-mask" v-if="showPrivacyModal" @tap="showPrivacyModal = false">
      <view class="modal-box about-box" @tap.stop>
        <view class="modal-header">
          <text class="modal-title">隐私政策</text>
          <text class="modal-close" @tap="showPrivacyModal = false">x</text>
        </view>
        <scroll-view class="modal-scroll" scroll-y>
          <view class="about-content">
            <text class="about-section-title">一、信息收集</text>
            <text class="about-feature">我们可能收集您的手机号码、头像、昵称等基本信息，用于账号注册与登录</text>
            <text class="about-section-title">二、信息使用</text>
            <text class="about-feature">收集的信息仅用于提供更好的服务体验，包括个性化推荐、行程规划等</text>
            <text class="about-section-title">三、信息保存</text>
            <text class="about-feature">我们采用业界标准的安全措施保护您的个人信息，不会向第三方共享您的个人信息</text>
            <text class="about-section-title">四、权限说明</text>
            <text class="about-feature">位置权限：用于地图导航和周边推荐</text>
            <text class="about-feature">相机权限：用于拍摄和上传图片</text>
            <text class="about-feature">存储权限：用于缓存数据以提升体验</text>
            <text class="about-section-title">五、联系我们</text>
            <text class="about-feature">如对隐私政策有任何疑问，请联系我们：contact@xundong.com</text>
            <text class="about-copyright">最后更新：2026年1月1日</text>
          </view>
        </scroll-view>
      </view>
    </view>

    <!-- 用户协议弹窗 -->
    <view class="modal-mask" v-if="showAgreementModal" @tap="showAgreementModal = false">
      <view class="modal-box about-box" @tap.stop>
        <view class="modal-header">
          <text class="modal-title">用户协议</text>
          <text class="modal-close" @tap="showAgreementModal = false">x</text>
        </view>
        <scroll-view class="modal-scroll" scroll-y>
          <view class="about-content">
            <text class="about-section-title">一、服务说明</text>
            <text class="about-feature">寻侗三江为用户提供三江侗族自治县的文化旅游信息、预订服务和社区互动功能</text>
            <text class="about-section-title">二、用户责任</text>
            <text class="about-feature">用户应妥善保管账号信息，不得发布违法违规内容，不得恶意攻击或骚扰其他用户</text>
            <text class="about-section-title">三、内容规范</text>
            <text class="about-feature">社区发布的内容应遵守国家法律法规，不得包含政治敏感、色情暴力、广告营销等违规信息</text>
            <text class="about-section-title">四、知识产权</text>
            <text class="about-feature">应用内的文字、图片、视频等内容的知识产权归寻侗三江团队所有，未经授权不得转载、复制、修改或使用</text>
            <text class="about-section-title">五、免责声明</text>
            <text class="about-feature">用户因自身操作导致的损失，本平台不承担责任。预订服务以实际商家信息为准</text>
            <text class="about-copyright">最后更新：2026年1月1日</text>
          </view>
        </scroll-view>
      </view>
    </view>
  </view>
</template>

<script>
import { getUserInfo, setUserInfo } from '../../utils/auth.js'
import { userApi } from '../../utils/api.js'

export default {
  data() {
    return {
      notificationEnabled: true,
      cacheSize: '计算中...',
      isLogin: false,
      phoneDisplay: '未绑定',
      userInfo: { nickname: '未设置' },

      // 个人信息编辑
      showProfileModal: false,
      editNickname: '',
      editName: '',
      editGender: 0,
      editAvatar: '',
      defaultAvatar: 'https://cdn.phototourl.com/free/2026-03-24-955d494f-d01c-4911-87ea-2e684f9a3f29.jpg',
      avatarPool: [
        'https://cdn.phototourl.com/free/2026-03-24-955d494f-d01c-4911-87ea-2e684f9a3f29.jpg',
        'https://cdn.phototourl.com/free/2026-03-24-9da6f2f7-f9d3-46ac-9819-695e797b76e3.jpg',
        'https://cdn.phototourl.com/member/2026-03-24-3cd4dc5f-9c84-4b60-b074-1dba51407d7e.png',
        'https://cdn.phototourl.com/member/2026-03-24-1c287f60-cae7-454e-b829-926032cddd96.png',
        'https://cdn.phototourl.com/member/2026-03-24-f1ef810e-bb9a-439b-bc19-526382cc971b.jpg'
      ],

      // 修改密码
      showPasswordModal: false,
      oldPassword: '',
      newPassword: '',
      confirmPassword: '',

      // 关于弹窗
      showAboutModal: false,
      showPrivacyModal: false,
      showAgreementModal: false
    }
  },
  onShow() {
    this.loadUserInfo()
    this.calcCacheSize()
  },
  methods: {
    loadUserInfo() {
      const userInfo = getUserInfo()
      this.isLogin = !!userInfo.isLogin
      this.userInfo = userInfo
      if (userInfo.phone && typeof userInfo.phone === 'string') {
        this.phoneDisplay = userInfo.phone.slice(0, 3) + '****' + userInfo.phone.slice(7)
      }
    },
    calcCacheSize() {
      try {
        const info = uni.storageInfoSync()
        const sizeKB = info.currentSize || 0
        if (sizeKB < 1024) {
          this.cacheSize = sizeKB + 'KB'
        } else {
          this.cacheSize = (sizeKB / 1024).toFixed(1) + 'MB'
        }
      } catch (e) {
        this.cacheSize = '0KB'
      }
    },

    // ===== 个人信息编辑 =====
    editProfile() {
      if (!this.isLogin) {
        uni.showToast({ title: '请先登录', icon: 'none' })
        setTimeout(() => {
          uni.navigateTo({ url: '/pages/login/login' })
        }, 1000)
        return
      }
      const userInfo = getUserInfo()
      this.editNickname = userInfo.nickname || ''
      this.editName = userInfo.name || ''
      // 性别字段处理：0=男，1=女，其他值默认为 0
      this.editGender = (userInfo.gender === 0 || userInfo.gender === 1) ? userInfo.gender : 0
      this.editAvatar = userInfo.avatar || ''
      this.showProfileModal = true
    },
    closeProfileModal() {
      this.showProfileModal = false
    },
    chooseAvatar() {
      uni.showActionSheet({
        itemList: ['从相册选择', '随机头像'],
        success: (res) => {
          if (res.tapIndex === 0) {
            uni.chooseImage({
              count: 1,
              sizeType: ['compressed'],
              sourceType: ['album'],
              success: (chooseRes) => {
                this.editAvatar = chooseRes.tempFilePaths[0]
              }
            })
          } else {
            this.editAvatar = this.avatarPool[Math.floor(Math.random() * this.avatarPool.length)]
            uni.showToast({ title: '已更换随机头像', icon: 'none' })
          }
        }
      })
    },
    async saveProfile() {
      const name = this.editNickname.trim()
      if (!name) {
        uni.showToast({ title: '昵称不能为空', icon: 'none' })
        return
      }

      const userInfo = getUserInfo()
      const userId = userInfo.id || userInfo.userId

      if (!userId) {
        uni.showToast({ title: '用户信息异常，请重新登录', icon: 'none' })

        setTimeout(() => {
          uni.redirectTo({ url: '/pages/login/login' })
        }, 1500)
        return
      }

      uni.showLoading({ title: '保存中...' })
      try {
        const updateData = {
          id: userId,
          nickname: name,
          name: this.editName.trim() || null,
          gender: this.editGender,
          avatar: this.editAvatar || this.defaultAvatar,
          phone: userInfo.phone || null  // 保留原有手机号
        }

        // 调用后端 API 更新用户信息
        await userApi.update(updateData)

        // 更新本地存储 - 完整更新 userInfo 对象（保留 phone 等原有字段）
        const updatedUserInfo = {
          ...userInfo,
          id: userId,
          userId: userId,
          nickname: name,
          name: updateData.name,
          gender: this.editGender,
          avatar: updateData.avatar
        }
        // 确保 phone 字段被保留（如果原来有的话）
        if (userInfo.phone) {
          updatedUserInfo.phone = userInfo.phone
        }
        setUserInfo(updatedUserInfo)

        // 同步更新当前页面的 userInfo
        this.userInfo = updatedUserInfo

        uni.hideLoading()
        this.showProfileModal = false
        uni.showToast({ title: '个人信息已更新', icon: 'success' })
      } catch (e) {
        uni.hideLoading()
        console.error('保存个人信息失败:', e)
        uni.showToast({ title: '保存失败，请重试', icon: 'none' })
      }
    },

    // ===== 修改密码 =====
    showChangePasswordModal() {
      if (!this.isLogin) {
        uni.showToast({ title: '请先登录', icon: 'none' })
        setTimeout(() => {
          uni.navigateTo({ url: '/pages/login/login' })
        }, 1000)
        return
      }
      const userInfo = getUserInfo()
      if (userInfo.loginType === 'guest') {
        uni.showToast({ title: '游客无法修改密码，请先注册账号', icon: 'none' })
        return
      }
      this.oldPassword = ''
      this.newPassword = ''
      this.confirmPassword = ''
      this.showPasswordModal = true
    },
    async submitChangePassword() {
      if (!this.oldPassword) {
        uni.showToast({ title: '请输入原密码', icon: 'none' })
        return
      }
      if (!this.newPassword) {
        uni.showToast({ title: '请输入新密码', icon: 'none' })
        return
      }
      if (this.newPassword.length < 6) {
        uni.showToast({ title: '新密码长度不能少于6位', icon: 'none' })
        return
      }
      if (this.newPassword !== this.confirmPassword) {
        uni.showToast({ title: '两次输入的密码不一致', icon: 'none' })
        return
      }

      uni.showLoading({ title: '修改中...' })
      try {
        await userApi.changePassword({
          oldPassword: this.oldPassword,
          newPassword: this.newPassword,
          confirmPassword: this.confirmPassword
        })

        uni.hideLoading()
        this.showPasswordModal = false
        this.oldPassword = ''
        this.newPassword = ''
        this.confirmPassword = ''
        // 修改密码成功后刷新用户信息
        this.loadUserInfo()
        uni.showToast({ title: '密码修改成功', icon: 'success' })
      } catch (e) {
        uni.hideLoading()
        console.error('修改密码失败:', e)
        const errorMsg = e.message || e.errMsg || '修改失败，请重试'
        uni.showToast({ title: errorMsg, icon: 'none', duration: 2000 })
      }
    },

    // ===== 其他功能 =====
    bindPhone() {
      if (!this.isLogin) {
        uni.showToast({ title: '请先登录', icon: 'none' })
        setTimeout(() => {
          uni.navigateTo({ url: '/pages/login/login' })
        }, 1000)
        return
      }
      
      const userInfo = getUserInfo()
      if (userInfo.phone) {
        // 已绑定手机号，显示解绑选项
        uni.showModal({
          title: '手机绑定',
          content: `当前已绑定手机号：${userInfo.phone.slice(0, 3) + '****' + userInfo.phone.slice(7)}，是否要更换手机号？`,
          confirmText: '更换',
          cancelText: '保留',
          success: (res) => {
            if (res.confirm) {
              this.showPhoneBindModal()
            }
          }
        })
      } else {
        // 未绑定手机号，显示绑定选项
        this.showPhoneBindModal()
      }
    },
    showPhoneBindModal() {
      uni.showModal({
        title: '手机绑定',
        content: '该功能需要后端支持，目前暂不可用',
        showCancel: false
      })
    },
    toggleNotification(e) {
      this.notificationEnabled = e.detail.value
      uni.showToast({ title: e.detail.value ? '已开启通知' : '已关闭通知', icon: 'none' })
    },
    clearCache() {
      uni.showModal({
        title: '清除缓存',
        content: '确定要清除缓存吗？这将清除本地缓存数据',
        success: (res) => {
          if (res.confirm) {
            uni.showLoading({ title: '清除缓存中...' })
            setTimeout(() => {
              try {
                const token = uni.getStorageSync('token')
                const userInfo = uni.getStorageSync('userInfo')
                const notificationEnabled = this.notificationEnabled

                uni.clearStorageSync()

                if (token) uni.setStorageSync('token', token)
                if (userInfo) uni.setStorageSync('userInfo', userInfo)
                this.notificationEnabled = notificationEnabled
              } catch (e) {
                console.error('清除缓存失败:', e)
              }
              this.cacheSize = '0KB'
              uni.hideLoading()
              uni.showToast({ title: '缓存已清除', icon: 'success' })
            }, 500)
          }
        }
      })
    },
    checkUpdate() {
      uni.showLoading({ title: '检查中...' })
      setTimeout(() => {
        uni.hideLoading()
        uni.showToast({ title: '已是最新版本', icon: 'none' })
      }, 1000)
    },
    showAbout() {
      this.showAboutModal = true
    },
    showPrivacy() {
      this.showPrivacyModal = true
    },
    showAgreement() {
      this.showAgreementModal = true
    },
    logout() {
      uni.showModal({
        title: '退出登录',
        content: '确定要退出登录吗？',
        success: (res) => {
          if (res.confirm) {
            uni.removeStorageSync('userInfo')
            uni.removeStorageSync('token')
            uni.showToast({ title: '已退出登录', icon: 'success' })
            setTimeout(() => { uni.reLaunch({ url: '/pages/index/index' }) }, 800)
          }
        }
      })
    }
  }
}
</script>

<style scoped lang="scss">
.setting-page { min-height: 100vh; background: #F8FAFC; padding-bottom: 40rpx; }

.setting-section { margin-top: 20rpx; }
.section-title { display: block; padding: 20rpx 30rpx 10rpx; font-size: 24rpx; color: #94A3B8; }
.setting-list { background: #FFFFFF; }
.setting-item { display: flex; justify-content: space-between; align-items: center; padding: 28rpx 30rpx; border-bottom: 1rpx solid #E8E8E8; }
.setting-item:last-child { border-bottom: none; }
.item-label { font-size: 28rpx; color: #1D3B5A; }
.item-right { display: flex; align-items: center; gap: 10rpx; }
.item-value { font-size: 26rpx; color: #64748B; }
.item-arrow { font-size: 32rpx; color: #94A3B8; }

.logout-wrap { padding: 60rpx 30rpx 20rpx; }
.logout-btn { background: #FFFFFF; color: #2E5F8E; font-size: 30rpx; font-weight: bold; text-align: center; padding: 28rpx; border-radius: 50rpx; border: 2rpx solid #2E5F8E; }

/* ===== 弹窗 ===== */
.modal-mask { position: fixed; top: 0; left: 0; right: 0; bottom: 0; background: rgba(0,0,0,0.5); display: flex; align-items: center; justify-content: center; z-index: 999; padding: 0 40rpx; }
.modal-box { width: 100%; max-width: 600rpx; background: #FFFFFF; border-radius: 24rpx; overflow: hidden; box-shadow: 0 10rpx 40rpx rgba(29,59,90,0.15); }
.about-box { max-height: 80vh; display: flex; flex-direction: column; }
.modal-header { display: flex; justify-content: space-between; align-items: center; padding: 30rpx; border-bottom: 1rpx solid #E2E8F0; }
.modal-title { font-size: 32rpx; font-weight: bold; color: #1D3B5A; }
.modal-close { width: 50rpx; height: 50rpx; background: #F8FAFC; border-radius: 50%; display: flex; align-items: center; justify-content: center; font-size: 24rpx; color: #94A3B8; }
.modal-body { padding: 30rpx; }
.modal-input { width: 100%; height: 88rpx; background: #F8FAFC; border-radius: 14rpx; padding: 0 24rpx; font-size: 28rpx; color: #1D3B5A; box-sizing: border-box; border: 1rpx solid #E2E8F0; }
.modal-hint { display: block; text-align: right; font-size: 22rpx; color: #94A3B8; margin-top: 10rpx; }

/* ===== 头像选择 ===== */
.avatar-section { display: flex; align-items: center; justify-content: center; flex-direction: column; margin-bottom: 30rpx; }
.avatar-label { font-size: 24rpx; color: #94A3B8; margin-bottom: 16rpx; }
.avatar-picker { display: flex; flex-direction: column; align-items: center; gap: 12rpx; }
.avatar-preview { width: 120rpx; height: 120rpx; border-radius: 50%; border: 4rpx solid rgba(46,95,142,0.2); }
.avatar-change { font-size: 22rpx; color: #2E5F8E; }
.form-row { display: flex; align-items: center; gap: 16rpx; margin-bottom: 16rpx; }
.form-label { font-size: 26rpx; color: #1D3B5A; width: 100rpx; flex-shrink: 0; }
.form-row .modal-input { flex: 1; }
.modal-actions { display: flex; gap: 16rpx; padding: 0 30rpx 30rpx; }
.modal-cancel { flex: 1; text-align: center; padding: 24rpx; background: #F8FAFC; border-radius: 50rpx; font-size: 28rpx; color: #64748B; }
.modal-confirm { flex: 1; text-align: center; padding: 24rpx; background: linear-gradient(135deg, #1D3B5A, #2E5F8E); border-radius: 50rpx; font-size: 28rpx; color: #FFFFFF; font-weight: bold; }

/* ===== 性别选择 ===== */
.gender-select { flex: 1; display: flex; gap: 16rpx; }
.gender-option { flex: 1; display: flex; flex-direction: column; align-items: center; gap: 8rpx; padding: 16rpx 0; background: #F8FAFC; border-radius: 14rpx; border: 2rpx solid transparent; transition: all 0.3s; }
.gender-option.active { background: rgba(46,95,142,0.1); border-color: #2E5F8E; }
.gender-icon-text { font-size: 36rpx; color: #94A3B8; }
.gender-option.active .gender-icon-text { color: #2E5F8E; }
.gender-option text { font-size: 24rpx; color: #64748B; }
.gender-option.active text { color: #2E5F8E; font-weight: 600; }

/* ===== 关于内容 ===== */
.modal-scroll { flex: 1; }
.about-content { padding: 30rpx; }
.about-logo { text-align: center; margin-bottom: 30rpx; }
.about-logo-text { display: block; font-size: 40rpx; font-weight: bold; color: #1D3B5A; margin-bottom: 8rpx; }
.about-version { font-size: 24rpx; color: #94A3B8; }
.about-desc { display: block; font-size: 26rpx; color: #64748B; line-height: 1.7; margin-bottom: 30rpx; text-align: center; }
.about-section-title { display: block; font-size: 28rpx; font-weight: bold; color: #1D3B5A; margin: 24rpx 0 12rpx; }
.about-feature { display: block; font-size: 24rpx; color: #64748B; line-height: 1.8; margin-bottom: 8rpx; }
.about-copyright { display: block; text-align: center; font-size: 22rpx; color: #CBD5E1; margin-top: 30rpx; padding-top: 20rpx; border-top: 1rpx solid #E2E8F0; }
</style>
