<template>
  <view class="activity-detail-page">
    <view class="header-banner">
      <image class="banner-bg-img" :src="activity.cover" mode="aspectFill" />
      <view class="banner-overlay"></view>
      <view class="banner-content">
        <view class="banner-top">
        </view>
        <view class="banner-bottom">
          <text class="banner-title">{{ activity.title }}</text>
          <view class="status-badge" :class="activity.status">{{ activity.statusText }}</view>
        </view>
      </view>
    </view>

    <scroll-view class="content" scroll-y>
      <view class="info-card">
        <view class="info-row">
          <view class="info-icon-wrap" style="background: rgba(74,144,226,0.12)">
            <image class="info-icon-img" src="/static/icons/calendar.png" mode="aspectFit" />
          </view>
          <view class="info-text">
            <text class="info-label">活动时间</text>
            <text class="info-value">{{ activity.displayTime }}</text>
            <view class="countdown-wrap" v-if="activity.countdownText">
              <text class="countdown-icon">⏱</text>
              <text class="countdown-text">{{ activity.countdownText }}</text>
            </view>
          </view>
        </view>
        <view class="info-row">
          <view class="info-icon-wrap" style="background: rgba(74,144,226,0.12)">
            <image class="info-icon-img" src="/static/icons/position.png" mode="aspectFit" />
          </view>
          <view class="info-text">
            <text class="info-label">活动地点</text>
            <text class="info-value">{{ activity.location }}</text>
          </view>
          <view class="nav-btn" @tap="openMap">导航</view>
        </view>
        <view class="info-row">
          <view class="info-icon-wrap" style="background: rgba(74,144,226,0.12)">
            <image class="info-icon-img" src="/static/icons/ticket.png" mode="aspectFit" />
          </view>
          <view class="info-text">
            <text class="info-label">活动费用</text>
            <text class="info-value price">{{ activity.price }}</text>
          </view>
        </view>
        <view class="info-row">
          <view class="info-icon-wrap" style="background: rgba(74,144,226,0.12)">
            <image class="info-icon-img" src="/static/icons/discover.png" mode="aspectFit" />
          </view>
          <view class="info-text">
            <text class="info-label">已报名</text>
            <text class="info-value">{{ activity.joinedCount || 0 }}/{{ activity.maxCount || '不限' }}人</text>
          </view>
        </view>
      </view>

      <view class="section-card">
        <view class="section-header">
          <view class="section-accent-bar"></view>
          <view class="section-icon-wrap" style="background: rgba(74,144,226,0.12)">
            <image class="section-icon-img" src="/static/icons/detail.png" mode="aspectFit" />
          </view>
          <text class="section-title">活动介绍</text>
        </view>
        <text class="section-text">{{ activity.desc }}</text>
      </view>

      <view class="section-card" v-if="activity.highlights && activity.highlights.length">
        <view class="section-header">
          <view class="section-accent-bar"></view>
          <view class="section-icon-wrap" style="background: rgba(74,144,226,0.12)">
            <image class="section-icon-img" src="/static/icons/hot.png" mode="aspectFit" />
          </view>
          <text class="section-title">活动亮点</text>
        </view>
        <view class="highlights-list">
          <view class="highlight-item" v-for="(item, index) in activity.highlights" :key="index">
            <view class="highlight-icon-wrap">
              <image class="highlight-icon-img" src="/static/icons/hot.png" mode="aspectFit" />
            </view>
            <text class="highlight-text">{{ item }}</text>
          </view>
        </view>
      </view>

      <view class="section-card">
        <view class="section-header">
          <view class="section-accent-bar"></view>
          <view class="section-icon-wrap" style="background: rgba(74,144,226,0.12)">
            <image class="section-icon-img" src="/static/icons/target.png" mode="aspectFit" />
          </view>
          <text class="section-title">报名须知</text>
        </view>
        <view class="notice-list">
          <view class="notice-item">
            <view class="notice-dot"></view>
            <text class="notice-text">请提前15分钟到达活动地点签到</text>
          </view>
          <view class="notice-item">
            <view class="notice-dot"></view>
            <text class="notice-text">报名成功后如需取消，请提前24小时联系客服</text>
          </view>
          <view class="notice-item">
            <view class="notice-dot"></view>
            <text class="notice-text">活动当天请携带报名时填写的手机号</text>
          </view>
          <view class="notice-item">
            <view class="notice-dot"></view>
            <text class="notice-text">如遇恶劣天气，活动将延期举办</text>
          </view>
        </view>
      </view>

      <view class="section-card">
        <view class="section-header">
          <view class="section-accent-bar"></view>
          <view class="section-icon-wrap" style="background: rgba(74,144,226,0.12)">
            <image class="section-icon-img" src="/static/icons/setting.png" mode="aspectFit" />
          </view>
          <text class="section-title">主办方</text>
        </view>
        <view class="organizer">
          <view class="organizer-icon-wrap">
            <image class="organizer-icon-img" src="/static/icons/discover_index.png" mode="aspectFit" />
          </view>
          <view class="organizer-info">
            <text class="organizer-name">{{ activity.organizer || '三江侗族自治县文化和旅游局' }}</text>
            <text class="organizer-desc">专注三江文化旅游推广</text>
          </view>
        </view>
      </view>

      <view style="height: 180rpx;"></view>
    </scroll-view>

    <view class="bottom-bar">
      <view class="bottom-left">
        <view class="bottom-btn" @tap="shareActivity">
          <view class="bottom-btn-icon-wrap">
            <image class="bottom-btn-icon-img" src="/static/icons/share.png" mode="aspectFit" />
          </view>
          <text class="bottom-btn-label">分享</text>
        </view>
      </view>
      <view class="join-actions" v-if="activity.joined">
        <view class="joined-btn">
          <text class="joined-text">已报名</text>
        </view>
        <view class="cancel-join-btn" @tap="handleCancelJoin">
          <text>取消报名</text>
        </view>
      </view>
      <view class="join-btn" v-else @tap="handleJoinAction">
        <text>立即报名</text>
      </view>
    </view>

    <!-- 报名信息填写弹窗 -->
    <view class="modal-mask" v-if="showJoinModal" @tap="showJoinModal = false">
      <view class="modal-box" @tap.stop>
        <view class="modal-header">
          <text class="modal-title">活动报名信息</text>
          <text class="modal-close" @tap="showJoinModal = false">✕</text>
        </view>
        <view class="modal-body">
          <view class="activity-info">
            <text class="activity-title">{{ activity.title }}</text>
            <text class="activity-time">{{ activity.displayTime }}</text>
            <text class="activity-location">{{ activity.location }}</text>
          </view>
          <view class="form-section">
            <view class="form-item">
              <text class="form-label">姓名 <text class="required">*</text></text>
              <input
                class="form-input"
                v-model="joinForm.name"
                placeholder="请输入您的姓名"
                maxlength="20"
              />
            </view>
            <view class="form-item">
              <text class="form-label">手机号码 <text class="required">*</text></text>
              <input
                class="form-input"
                v-model="joinForm.phone"
                placeholder="请输入您的手机号码"
                type="number"
                maxlength="11"
              />
            </view>
          </view>
          <view class="form-tip">
            <text class="tip-icon">ℹ️</text>
            <text class="tip-text">已登录用户信息将自动填充，请确保信息准确以便联系</text>
          </view>
        </view>
        <view class="modal-footer">
          <view class="cancel-btn" @tap="showJoinModal = false">取消</view>
          <view class="confirm-btn" @tap="confirmJoin">确认报名</view>
        </view>
      </view>
    </view>
  </view>
</template>

<script>
import { activityApi } from '../../utils/api.js'

export default {
  data() {
    return {
      activityId: null,
      loading: false,
      showJoinModal: false,
      joinForm: {
        name: '',
        phone: ''
      },
      activity: {
        id: null,
        title: '',
        time: '',
        displayTime: '',
        countdownText: '',
        location: '',
        price: '',
        cover: '',
        status: 'ongoing',
        statusText: '报名中',
        joined: false,
        joinedCount: 0,
        maxCount: '不限',
        desc: '',
        highlights: [],
        organizer: '三江侗族自治县文化和旅游局'
      },
      countdownTimer: null
    }
  },
  onLoad(options) {
    if (options.id) {
      this.activityId = parseInt(options.id)
      this.loadActivity()
    }
  },
  onUnload() {
    // 页面卸载时清除定时器
    this.stopCountdown()
  },
  onHide() {
    // 页面隐藏时暂停定时器
    this.stopCountdown()
  },
  onShow() {
    // 页面显示时重新启动倒计时
    if (this.activity.time) {
      this.startCountdown(this.activity.time)
    }
  },
  methods: {
    goBack() {
      uni.navigateBack()
    },
    async loadActivity() {
      this.loading = true
      try {
        const data = await activityApi.getDetail(this.activityId)
        const timeStr = data.activityTime || data.activity_time || data.time || ''
        this.activity = {
          id: data.id,
          title: data.title || '',
          time: timeStr,
          displayTime: this.formatTime(timeStr),
          countdownText: '',
          location: data.location || '',
          lat: data.lat || data.latitude || null,
          lng: data.lng || data.longitude || null,
          price: data.price ? `¥${data.price}/人` : '免费',
          cover: data.cover || data.coverImg || data.cover_img || '',
          status: this.mapStatus(data.status),
          statusText: this.mapStatusText(data.status),
          joined: false,
          joinedCount: data.currentParticipants || data.current_participants || 0,
          maxCount: data.maxParticipants || data.max_participants || '不限',
          desc: data.description || data.desc || '',
          highlights: this.parseHighlights(data.highlights),
          organizer: data.organizer || '三江侗族自治县文化和旅游局'
        }
        // 开始倒计时
        this.startCountdown(timeStr)
        // 检查用户是否已报名
        this.checkJoined()
      } catch (e) {
        console.error('加载活动详情失败，使用本地数据', e)
        this.loadLocalActivity()
      } finally {
        this.loading = false
      }
    },
    loadLocalActivity() {
      const activities = [
        {
          id: 1,
          title: '侗族大歌演唱体验活动',
          time: '2026年4月15日 14:00',
          displayTime: '2026年4月15日 14:00',
          location: '程阳八寨鼓楼广场',
          price: '免费',
          cover: 'https://cdn.phototourl.com/free/2026-03-23-75376aa0-ba75-4269-8839-bbe6aca040a6.jpg',
          status: 'ongoing',
          statusText: '报名中',
          joined: false,
          joinedCount: 45,
          maxCount: 100,
          desc: '侗族大歌是侗族人民在长期生产生活中创造的一种多声部、无指挥、无伴奏的民间合唱形式，被誉为"天籁之音"。',
          highlights: [
            '侗族大歌传承人亲临现场教学',
            '学习经典侗族大歌《蝉之歌》',
            '体验多声部无伴奏合唱的魅力',
            '与侗族歌手互动交流',
            '免费获得侗族文化纪念品'
          ],
          organizer: '三江侗族自治县文化和旅游局'
        },
        {
          id: 2,
          title: '侗锦编织工坊体验',
          time: '2026年4月20日 10:00',
          displayTime: '2026年4月20日 10:00',
          location: '三江侗族生态博物馆',
          price: '¥68/人',
          cover: 'https://cdn.phototourl.com/free/2026-03-23-83a39e17-b2f9-4ff8-bced-41e9c064e9fe.jpg',
          status: 'ongoing',
          statusText: '报名中',
          joined: false,
          joinedCount: 28,
          maxCount: 50,
          desc: '侗锦是侗族人民的传统手工艺术品，以其精美的图案和精湛的技艺闻名于世。',
          highlights: [
            '国家级非遗传承人亲自指导',
            '学习传统侗锦编织技艺',
            '亲手编织属于自己的侗锦',
            '作品可带走留念',
            '赠送编织工具套装'
          ],
          organizer: '三江侗族生态博物馆'
        },
        {
          id: 3,
          title: '布央茶园春季采茶节',
          time: '2026年4月30日',
          displayTime: '2026年4月30日',
          location: '布央仙人山茶园',
          price: '¥128/人',
          cover: 'https://cdn.phototourl.com/member/2026-03-26-3af2a741-df2c-4b80-be6d-4996ec3f7719.jpg',
          status: 'upcoming',
          statusText: '即将开始',
          joined: false,
          joinedCount: 76,
          maxCount: 200,
          desc: '布央茶园是广西最大的有机茶园之一，被誉为"中国早春第一茶"。',
          highlights: [
            '万亩有机茶园参观',
            '专业茶农指导采茶',
            '体验传统制茶工艺',
            '观看精彩茶艺表演',
            '免费品尝布央早春茶'
          ],
          organizer: '布央仙人山茶园景区'
        },
        {
          id: 4,
          title: '三江侗族芦笙节',
          time: '2026年农历正月十五',
          displayTime: '2026年农历正月十五',
          location: '三江县城广场',
          price: '免费',
          cover: 'https://cdn.phototourl.com/uploads/2026-03-19-67bbd771-fa02-4207-9437-87afc2c36bc7.jpg',
          status: 'upcoming',
          statusText: '即将开始',
          joined: false,
          joinedCount: 0,
          maxCount: '不限',
          desc: '芦笙节是侗族最隆重的传统节日之一，每年农历正月十五举行。',
          highlights: [
            '千人芦笙合奏表演',
            '侗族盛装巡游',
            '品尝侗族百家宴',
            '篝火晚会狂欢',
            '传统体育竞技比赛'
          ],
          organizer: '三江侗族自治县人民政府'
        }
      ]
      const activity = activities.find(a => a.id === this.activityId)
      if (activity) {
        this.activity = { ...this.activity, ...activity }
        // 开始倒计时（添加安全检查）
        if (this.startCountdown) {
          this.startCountdown(activity.time)
        }
      }
    },
    mapStatus(status) {
      const map = { 0: 'upcoming', 1: 'ongoing', 2: 'ended' }
      return map[status] || 'ongoing'
    },
    mapStatusText(status) {
      const map = { 0: '即将开始', 1: '报名中', 2: '已结束' }
      return map[status] || '报名中'
    },
    parseHighlights(highlights) {
      if (!highlights) return []
      if (Array.isArray(highlights)) return highlights
      try { return JSON.parse(highlights) } catch { return [] }
    },
    // 格式化时间显示
    formatTime(timeStr) {
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
    // 解析时间字符串为Date对象
    parseTime(timeStr) {
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
    // 开始倒计时
    startCountdown(timeStr) {
      // 清除之前的定时器
      this.stopCountdown()
      
      const activityDate = this.parseTime(timeStr)
      if (!activityDate) {
        this.activity.countdownText = ''
        return
      }
      
      const now = new Date()
      const diff = activityDate - now
      
      if (diff <= 0) {
        // 活动已经开始或结束
        if (this.activity.status === 'ended') {
          this.activity.countdownText = '活动已结束'
        } else {
          this.activity.countdownText = '活动进行中'
        }
        return
      }
      
      // 更新倒计时显示
      this.updateCountdown(diff)
      
      // 开始定时器每秒更新
      this.countdownTimer = setInterval(() => {
        const newDiff = activityDate - new Date()
        if (newDiff <= 0) {
          this.stopCountdown()
          this.activity.countdownText = '活动已开始'
        } else {
          this.updateCountdown(newDiff)
        }
      }, 1000)
    },
    // 更新倒计时显示
    updateCountdown(diff) {
      const days = Math.floor(diff / (1000 * 60 * 60 * 24))
      const hours = Math.floor((diff % (1000 * 60 * 60 * 24)) / (1000 * 60 * 60))
      const minutes = Math.floor((diff % (1000 * 60 * 60)) / (1000 * 60))
      const seconds = Math.floor((diff % (1000 * 60)) / 1000)
      
      if (days > 0) {
        this.activity.countdownText = `距离开始还有 ${days}天${hours}小时`
      } else if (hours > 0) {
        this.activity.countdownText = `距离开始还有 ${hours}小时${minutes}分钟`
      } else if (minutes > 0) {
        this.activity.countdownText = `距离开始还有 ${minutes}分钟`
      } else {
        this.activity.countdownText = `距离开始还有 ${seconds}秒`
      }
    },
    // 停止倒计时
    stopCountdown() {
      if (this.countdownTimer) {
        clearInterval(this.countdownTimer)
        this.countdownTimer = null
      }
    },
    checkJoined() {
      const userInfo = uni.getStorageSync('userInfo') || {}
      const userId = userInfo.userId || userInfo.id
      if (!userId) return
      const joinedKey = `activity_joined_${this.activityId}_${userId}`
      this.activity.joined = !!uni.getStorageSync(joinedKey)
    },
    openMap() {
      const lat = parseFloat(this.activity.lat || this.activity.latitude)
      const lng = parseFloat(this.activity.lng || this.activity.longitude)
      if (!lat || !lng) { uni.showToast({ title: '暂无位置信息', icon: 'none' }); return }
      uni.openLocation({
        latitude: lat,
        longitude: lng,
        name: this.activity.location || this.activity.title,
        address: this.activity.location || '三江侗族自治县',
        scale: 16,
        fail: () => { uni.showToast({ title: '打开地图失败', icon: 'none' }) }
      })
    },
    shareActivity() {
      uni.share({
        provider: 'weixin',
        type: 0,
        title: '寻侗三江 - ' + this.activity.title,
        content: this.activity.desc,
        success: () => { uni.showToast({ title: '分享成功', icon: 'success' }) },
        fail: () => { uni.showToast({ title: '分享取消', icon: 'none' }) }
      })
    },
    async handleJoinAction() {
      if (this.activity.joined) {
        return
      }
      
      // 检查用户是否已登录
      const userInfo = uni.getStorageSync('userInfo') || {}
      const isLogin = userInfo.isLogin || false
      
      if (!isLogin) {
        uni.showModal({
          title: '提示',
          content: '请先登录后再报名活动',
          confirmText: '去登录',
          confirmColor: '#4A90E2',
          success: (res) => {
            if (res.confirm) {
              uni.navigateTo({ url: '/pages/login/login' })
            }
          }
        })
        return
      }
      
      // 显示报名信息填写弹窗，自动填充已登录用户的信息
      this.joinForm = {
        name: userInfo.name || userInfo.nickname || '',
        phone: userInfo.phone || ''
      }
      
      this.showJoinModal = true
    },
    
    async handleCancelJoin() {
      // 显示取消报名确认
      uni.showModal({
        title: '取消报名',
        content: '确定要取消报名"' + this.activity.title + '"吗？\n\n取消后名额将释放给其他用户。',
        confirmText: '取消报名',
        confirmColor: '#f56c6c',
        success: async (res) => {
          if (res.confirm) {
            await this.cancelJoin()
          }
        }
      })
    },
    
    async cancelJoin() {
      try {
        const userInfo = uni.getStorageSync('userInfo') || {}
        const userId = userInfo.userId || userInfo.id
        
        // 调用后端取消报名接口
        await activityApi.cancelSignup(this.activityId, {
          userId: userId
        })
        
        // 更新报名状态
        this.activity.joined = false
        this.activity.joinedCount = Math.max(0, (this.activity.joinedCount || 0) - 1)
        
        // 清除本地报名状态
        if (userId) {
          const joinedKey = `activity_joined_${this.activityId}_${userId}`
          uni.removeStorageSync(joinedKey)
        }
        
        uni.showToast({ title: '已取消报名', icon: 'success' })
      } catch (e) {
        console.error('取消报名失败，降级到本地', e)
        // 降级：只更新本地状态
        this.activity.joined = false
        this.activity.joinedCount = Math.max(0, (this.activity.joinedCount || 0) - 1)
        
        const userInfo = uni.getStorageSync('userInfo') || {}
        const userId = userInfo.userId || userInfo.id
        if (userId) {
          const joinedKey = `activity_joined_${this.activityId}_${userId}`
          uni.removeStorageSync(joinedKey)
        }
        
        uni.showToast({ title: '已取消报名', icon: 'success' })
      }
    },
    
    async confirmJoin() {
      // 验证表单
      if (!this.joinForm.name || !this.joinForm.name.trim()) {
        uni.showToast({ title: '请输入姓名', icon: 'none' })
        return
      }
      if (!this.joinForm.phone || !this.joinForm.phone.trim()) {
        uni.showToast({ title: '请输入手机号码', icon: 'none' })
        return
      }
      // 验证手机号格式
      const phoneReg = /^\d{11}$/
      if (!phoneReg.test(this.joinForm.phone)) {
        uni.showToast({ title: '请输入11位手机号', icon: 'none' })
        return
      }
      
      try {
        const userInfo = uni.getStorageSync('userInfo') || {}
        const userId = userInfo.userId || userInfo.id
        
        // 调用后端报名接口
        await activityApi.signup(this.activityId, {
          name: this.joinForm.name.trim(),
          phone: this.joinForm.phone.trim(),
          userId: userId
        })
        
        // 更新报名状态
        this.activity.joined = true
        this.activity.joinedCount = (this.activity.joinedCount || 0) + 1
        this.showJoinModal = false
        
        // 保存报名状态到本地
        if (userId) {
          const joinedKey = `activity_joined_${this.activityId}_${userId}`
          uni.setStorageSync(joinedKey, '1')
        }
        
        uni.showToast({ title: '报名成功', icon: 'success' })
      } catch (e) {
        console.error('报名失败，降级到本地', e)
        // 降级：只更新本地状态
        this.activity.joined = true
        this.activity.joinedCount = (this.activity.joinedCount || 0) + 1
        this.showJoinModal = false
        
        const userInfo = uni.getStorageSync('userInfo') || {}
        const userId = userInfo.userId || userInfo.id
        if (userId) {
          const joinedKey = `activity_joined_${this.activityId}_${userId}`
          uni.setStorageSync(joinedKey, '1')
        }
        
        uni.showToast({ title: '报名成功', icon: 'success' })
      }
    }
  }
}
</script>

<style scoped lang="scss">
.activity-detail-page {
  min-height: 100vh;
  background: #F8F9FA;
}

.header-banner {
  height: 480rpx;
  position: relative;
}

.banner-bg-img {
  position: absolute;
  width: 100%;
  height: 100%;
}

.banner-overlay {
  position: absolute;
  inset: 0;
  background: linear-gradient(to bottom, rgba(0,0,0,0.2) 0%, rgba(0,0,0,0.5) 100%);
}

.banner-content {
  position: absolute;
  inset: 0;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  padding: 30rpx;
}

.banner-top {
  display: flex;
  justify-content: flex-start;
}

.back-btn {
  width: 72rpx;
  height: 72rpx;
  background: rgba(255, 255, 255, 0.25);
  backdrop-filter: blur(10rpx);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
}

.back-icon {
  font-size: 36rpx;
  color: #FFFFFF;
  font-weight: bold;
}

.banner-bottom {
  display: flex;
  flex-direction: column;
  gap: 16rpx;
}

.banner-title {
  font-size: 38rpx;
  font-weight: bold;
  color: #FFFFFF;
  line-height: 1.4;
  text-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.3);
}

.status-badge {
  display: inline-block;
  font-size: 22rpx;
  color: #FFFFFF;
  background: rgba(255,255,255,0.25);
  padding: 8rpx 22rpx;
  border-radius: 24rpx;
  align-self: flex-start;
  font-weight: 500;
}

.status-badge.ongoing {
  background: #4A90E2;
}

.status-badge.upcoming {
  background: #FF7F50;
}

.content {
  margin-top: -30rpx;
  position: relative;
  z-index: 1;
}

.info-card {
  background: #FFFFFF;
  margin: 0 24rpx 20rpx;
  padding: 24rpx;
  border-radius: 20rpx;
  box-shadow: 0 4rpx 16rpx rgba(0, 0, 0, 0.06);
  border: 1rpx solid #E8E8E8;
}

.info-row {
  display: flex;
  align-items: center;
  gap: 16rpx;
  padding: 16rpx 0;
  border-bottom: 1rpx solid #F0F0F0;
}

.info-row:last-child {
  border-bottom: none;
}

.info-icon-wrap {
  width: 56rpx;
  height: 56rpx;
  border-radius: 14rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.info-icon-img {
  width: 32rpx;
  height: 32rpx;
}

.info-text {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 4rpx;
}

.info-label {
  font-size: 20rpx;
  color: #999;
}

.info-value {
  font-size: 26rpx;
  color: #333;
}

.info-value.price {
  color: #4A90E2;
  font-weight: bold;
  font-size: 28rpx;
}

/* 倒计时样式 */
.countdown-wrap {
  display: flex;
  align-items: center;
  gap: 6rpx;
  margin-top: 8rpx;
  padding: 8rpx 16rpx;
  background: linear-gradient(135deg, rgba(74,144,226,0.08) 0%, rgba(74,144,226,0.02) 100%);
  border-radius: 20rpx;
  border: 1rpx solid rgba(74,144,226,0.15);
  align-self: flex-start;
}

.countdown-icon {
  font-size: 24rpx;
  line-height: 1;
}

.countdown-text {
  font-size: 22rpx;
  color: #4A90E2;
  font-weight: 600;
  line-height: 1.4;
}

.nav-btn {
  font-size: 22rpx;
  color: #4A90E2;
  background: rgba(74,144,226,0.1);
  padding: 8rpx 20rpx;
  border-radius: 20rpx;
  font-weight: 500;
}

.section-card {
  background: #FFFFFF;
  margin: 0 24rpx 20rpx;
  padding: 24rpx;
  border-radius: 20rpx;
  box-shadow: 0 4rpx 16rpx rgba(0, 0, 0, 0.06);
  border: 1rpx solid #E8E8E8;
}

.section-header {
  display: flex;
  align-items: center;
  gap: 12rpx;
  margin-bottom: 16rpx;
}

.section-accent-bar {
  width: 6rpx;
  height: 32rpx;
  background: #4A90E2;
  border-radius: 3rpx;
}

.section-icon-wrap {
  width: 56rpx;
  height: 56rpx;
  border-radius: 14rpx;
  display: flex;
  align-items: center;
  justify-content: center;
}

.section-icon-img {
  width: 32rpx;
  height: 32rpx;
}

.section-title {
  font-size: 28rpx;
  font-weight: bold;
  color: #2C3E50;
}

.section-text {
  font-size: 26rpx;
  color: #555;
  line-height: 1.7;
}

.highlights-list {
  display: flex;
  flex-direction: column;
  gap: 14rpx;
}

.highlight-item {
  display: flex;
  align-items: flex-start;
  gap: 12rpx;
}

.highlight-icon-wrap {
  flex-shrink: 0;
  width: 44rpx;
  height: 44rpx;
  background: #4A90E2;
  border-radius: 12rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-top: 2rpx;
}

.highlight-icon-img {
  width: 24rpx;
  height: 24rpx;
}

.highlight-text {
  flex: 1;
  font-size: 24rpx;
  color: #555;
  line-height: 1.6;
}

.notice-list {
  display: flex;
  flex-direction: column;
  gap: 12rpx;
}

.notice-item {
  display: flex;
  align-items: flex-start;
  gap: 12rpx;
}

.notice-dot {
  width: 10rpx;
  height: 10rpx;
  background: #4A90E2;
  border-radius: 50%;
  flex-shrink: 0;
  margin-top: 8rpx;
}

.notice-text {
  flex: 1;
  font-size: 24rpx;
  color: #666;
  line-height: 1.6;
}

.organizer {
  display: flex;
  align-items: center;
  gap: 20rpx;
}

.organizer-icon-wrap {
  width: 80rpx;
  height: 80rpx;
  background: rgba(74,144,226,0.12);
  border-radius: 20rpx;
  display: flex;
  align-items: center;
  justify-content: center;
}

.organizer-icon-img {
  width: 48rpx;
  height: 48rpx;
}

.organizer-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 4rpx;
}

.organizer-name {
  font-size: 28rpx;
  font-weight: bold;
  color: #2C3E50;
}

.organizer-desc {
  font-size: 22rpx;
  color: #999;
}

.bottom-bar {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  background: #FFFFFF;
  padding: 20rpx 30rpx 40rpx;
  display: flex;
  align-items: center;
  gap: 20rpx;
  box-shadow: 0 -4rpx 20rpx rgba(0, 0, 0, 0.08);
}

.bottom-left {
  display: flex;
  gap: 24rpx;
}

.bottom-btn {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4rpx;
}

.bottom-btn-icon-wrap {
  width: 56rpx;
  height: 56rpx;
  background: #F8F9FA;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
}

.bottom-btn-icon-img {
  width: 28rpx;
  height: 28rpx;
}

.bottom-btn-label {
  font-size: 18rpx;
  color: #666;
}

.join-btn {
  flex: 1;
  background: linear-gradient(135deg, #4A90E2 0%, #2E5F8E 100%);
  color: #FFFFFF;
  font-size: 28rpx;
  font-weight: bold;
  text-align: center;
  padding: 24rpx;
  border-radius: 50rpx;
  box-shadow: 0 4rpx 16rpx rgba(74, 144, 226, 0.3);
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.3s ease;
}

.join-btn:active {
  transform: scale(0.95);
  box-shadow: 0 2rpx 8rpx rgba(74, 144, 226, 0.3);
}

// 已报名状态的双按钮布局
.join-actions {
  flex: 1;
  display: flex;
  gap: 12rpx;
}

.joined-btn {
  flex: 1;
  background: linear-gradient(135deg, #67c23a 0%, #529b34 100%);
  color: #FFFFFF;
  font-size: 26rpx;
  font-weight: 600;
  text-align: center;
  padding: 20rpx 16rpx;
  border-radius: 50rpx;
  box-shadow: 0 4rpx 16rpx rgba(103, 194, 58, 0.3);
  display: flex;
  align-items: center;
  justify-content: center;
  white-space: nowrap;
  min-width: 120rpx;
  transition: all 0.3s ease;
}

.joined-btn:active {
  transform: scale(0.95);
  box-shadow: 0 2rpx 8rpx rgba(103, 194, 58, 0.3);
}

.joined-text {
  color: #FFFFFF;
}

.cancel-join-btn {
  flex: 1;
  background: #fff;
  color: #f56c6c;
  font-size: 26rpx;
  font-weight: 600;
  text-align: center;
  padding: 20rpx 16rpx;
  border-radius: 50rpx;
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

/* ===== 报名信息弹窗样式 ===== */
.modal-mask {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
  padding: 0 40rpx;
}

.modal-box {
  width: 100%;
  max-width: 640rpx;
  background: #FDFCF8;
  border-radius: 24rpx;
  overflow: hidden;
  display: flex;
  flex-direction: column;
  max-height: 85vh;
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 30rpx;
  border-bottom: 1rpx solid #F0EDE8;
}

.modal-title {
  font-size: 32rpx;
  font-weight: bold;
  color: #1D3B5A;
}

.modal-close {
  width: 50rpx;
  height: 50rpx;
  background: #F8F9FA;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24rpx;
  color: #999;
}

.modal-body {
  padding: 30rpx;
  overflow-y: auto;
  flex: 1;
}

.activity-info {
  background: linear-gradient(135deg, rgba(74,144,226,0.08) 0%, rgba(74,144,226,0.02) 100%);
  padding: 24rpx;
  border-radius: 16rpx;
  margin-bottom: 24rpx;
  border-left: 4rpx solid #4A90E2;
}

.activity-title {
  display: block;
  font-size: 28rpx;
  font-weight: bold;
  color: #1D3B5A;
  margin-bottom: 12rpx;
}

.activity-time,
.activity-location {
  display: block;
  font-size: 22rpx;
  color: #666;
  margin-bottom: 6rpx;
  line-height: 1.5;
}

.form-section {
  margin-bottom: 20rpx;
}

.form-item {
  margin-bottom: 24rpx;
}

.form-item:last-child {
  margin-bottom: 0;
}

.form-label {
  display: block;
  font-size: 28rpx;
  color: #333;
  margin-bottom: 12rpx;
  font-weight: 600;
}

.required {
  color: #f56c6c;
}

.form-input {
  width: 100%;
  min-height: 88rpx;
  height: auto;
  background: #F8F9FA;
  border-radius: 12rpx;
  padding: 28rpx 20rpx;
  font-size: 30rpx;
  color: #333;
  border: 2rpx solid transparent;
  transition: all 0.2s ease;
  line-height: 1.5;
  box-sizing: border-box;
}

.form-input:focus {
  background: #FFFFFF;
  border-color: #4A90E2;
}

.form-tip {
  display: flex;
  align-items: center;
  gap: 8rpx;
  background: rgba(255,191,0,0.08);
  padding: 16rpx 20rpx;
  border-radius: 12rpx;
  margin-top: 20rpx;
}

.tip-icon {
  font-size: 28rpx;
  flex-shrink: 0;
}

.tip-text {
  flex: 1;
  font-size: 24rpx;
  color: #999;
  line-height: 1.5;
}

.modal-footer {
  display: flex;
  padding: 20rpx 30rpx 30rpx;
  border-top: 1rpx solid #F0EDE8;
  gap: 20rpx;
}

.cancel-btn,
.confirm-btn {
  flex: 1;
  text-align: center;
  padding: 24rpx;
  border-radius: 50rpx;
  font-size: 28rpx;
  font-weight: bold;
}

.cancel-btn {
  background: #F5F5F5;
  color: #666;
}

.confirm-btn {
  background: linear-gradient(135deg, #4A90E2 0%, #2E5F8E 100%);
  color: #FFFFFF;
  box-shadow: 0 4rpx 16rpx rgba(74, 144, 226, 0.3);
}
</style>
