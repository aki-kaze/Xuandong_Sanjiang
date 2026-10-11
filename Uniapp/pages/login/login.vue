<template>
  <view class="login-page">
    <view class="login-bg-decoration">
      <view class="circle circle-1"></view>
      <view class="circle circle-2"></view>
      <view class="circle circle-3"></view>
    </view>

    <view class="login-header">
      <view class="logo-wrapper">
        <view class="logo">
          <text class="logo-icon">🏯</text>
        </view>
        <view class="brand-info">
          <text class="brand-name">寻侗三江</text>
          <text class="brand-tagline">探索侗族千年文化</text>
        </view>
      </view>
    </view>

    <view class="login-card">
      <view class="card-header">
        <text class="card-title">欢迎回来</text>
        <text class="card-subtitle">登录后享受更多服务</text>
      </view>

      <view class="input-group">
        <view class="input-item">
          <view class="input-wrapper">
            <image class="input-icon" src="/static/icons/iPhone.png" mode="aspectFit" />
            <input
              class="input-field"
              v-model="phone"
              placeholder="请输入账号/手机号"
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
              placeholder="请输入密码"
            />
            <view class="password-toggle" @tap="showPassword = !showPassword">
              <text>{{ showPassword ? '👁' : '👁‍🗨' }}</text>
            </view>
          </view>
        </view>
      </view>

      <view class="login-actions">
        <button class="login-btn" @tap="handleLogin" :disabled="isLoading">
          <text class="btn-text">{{ isLoading ? '登录中...' : '登录' }}</text>
        </button>
      </view>

      <view class="card-footer">
        <view class="footer-left" @tap="agreed = !agreed">
          <view class="checkbox" :class="{ checked: agreed }">
            <text v-if="agreed" class="check-mark">✓</text>
          </view>
          <text class="agree-text">我已阅读并同意</text>
        </view>
        <view class="footer-links">
          <text class="link" @tap.stop="showAgreement('user')">《用户协议》</text>
          <text class="link" @tap.stop="showAgreement('privacy')">《隐私政策》</text>
        </view>
      </view>

      <view class="register-prompt">
        <text class="prompt-text">还没有账号？</text>
        <text class="register-link" @tap="goRegister">立即注册</text>
      </view>
    </view>

    <view class="guest-mode">
      <text class="guest-tip">游客模式：只能查看信息，登录后可收藏、下单、发布</text>
    </view>

    <!-- 用户协议弹窗 -->
    <view class="modal-mask" v-if="showUserAgreementModal" @tap="showUserAgreementModal = false">
      <view class="modal-box about-box" @tap.stop>
        <view class="modal-header">
          <text class="modal-title">用户协议</text>
          <text class="modal-close" @tap="showUserAgreementModal = false">✕</text>
        </view>
        <scroll-view class="modal-scroll" scroll-y>
          <view class="about-content">
            <text class="about-section-title">寻侗三江小程序用户协议</text>
            <text class="about-date">更新日期：2026年4月1日 生效日期：2026年4月1日</text>

            <text class="about-section-title">一、总则</text>
            <text class="about-feature">1.1 寻侗三江小程序（以下简称"本小程序"）是由寻侗三江团队（以下简称"我们"）开发运营的文化旅游服务产品。</text>
            <text class="about-feature">1.2 本协议是用户（以下简称"您"）与我们之间关于使用本小程序服务所订立的协议。</text>
            <text class="about-feature">1.3 您在使用本小程序前，请仔细阅读并遵守本协议。请您务必审慎阅读、充分理解各条款内容，特别是免除或者限制责任的条款。</text>
            <text class="about-feature">1.4 您注册、登录或使用本小程序服务，即表示您已阅读并同意接受本协议的全部内容。</text>

            <text class="about-section-title">二、服务说明</text>
            <text class="about-feature">2.1 寻侗三江为用户提供广西三江侗族自治县的文化旅游信息展示、景点导览、门票预订、酒店预订、美食推荐、路线规划、AI智能助手及社区互动等功能。</text>
            <text class="about-feature">2.2 我们有权根据法律法规及经营需要，变更、中断或终止部分或全部的免费服务，并在合理范围内提前通知用户。</text>
            <text class="about-feature">2.3 本小程序提供的预订服务（门票、酒店等）仅为信息中介，具体服务由第三方商家提供，实际服务以商家为准。</text>

            <text class="about-section-title">三、用户注册与账户管理</text>
            <text class="about-feature">3.1 您可以通过手机号注册的方式创建账户。</text>
            <text class="about-feature">3.2 您应提供及时、详尽及准确的个人资料，并不断更新注册资料，符合及时、详尽准确的要求。</text>
            <text class="about-feature">3.3 您的账户信息和密码由您自行保管，因您保管不善可能导致账号被他人使用（包括但不限于遭受黑客攻击、账号被盗等），责任由您自行承担。</text>
            <text class="about-feature">3.4 如发现任何未经授权使用您账户的情况，应立即通知我们。</text>

            <text class="about-section-title">四、用户行为规范</text>
            <text class="about-feature">4.1 您在使用本小程序服务时，必须遵守中华人民共和国相关法律法规的规定，不得利用本小程序服务制作、复制、发布、传播含有下列内容的信息：</text>
            <text class="about-feature">（一）反对宪法所确定的基本原则的；</text>
            <text class="about-feature">（二）危害国家安全，泄露国家秘密，颠覆国家政权，破坏国家统一的；</text>
            <text class="about-feature">（三）损害国家荣誉和利益的；</text>
            <text class="about-feature">（四）煽动民族仇恨、民族歧视，破坏民族团结的；</text>
            <text class="about-feature">（五）破坏国家宗教政策，宣扬邪教和封建迷信的；</text>
            <text class="about-feature">（六）散布谣言，扰乱社会秩序，破坏社会稳定的；</text>
            <text class="about-feature">（七）散布淫秽、色情、赌博、暴力、凶杀、恐怖或者教唆犯罪的；</text>
            <text class="about-feature">（八）侮辱或者诽谤他人，侵害他人合法权益的；</text>
            <text class="about-feature">（九）含有法律、行政法规禁止的其他内容的。</text>
            <text class="about-feature">4.2 您不得利用本小程序服务进行任何可能对互联网正常运转造成不利影响的行为，不得利用本小程序服务传输任何骚扰性的、中伤他人的、辱骂性的、恐吓性的、庸俗淫秽的或其他任何非法的信息资料。</text>
            <text class="about-feature">4.3 您不得利用本小程序服务从事任何违法违规的活动，包括但不限于发布虚假信息、恶意刷单、恶意差评等。</text>

            <text class="about-section-title">五、知识产权声明</text>
            <text class="about-feature">5.1 本小程序所展示的所有内容，包括但不限于文字、图片、音频、视频、软件、程序、版面设计、数据等，均受著作权法、商标法、专利法及相关知识产权法律保护。</text>
            <text class="about-feature">5.2 未经我们书面许可，您不得以任何方式复制、转载、修改、传播本小程序的任何内容。</text>
            <text class="about-feature">5.3 用户在本小程序社区发布的原创内容，著作权归用户所有，但授予我们免费、非独占、可再许可的使用权。</text>

            <text class="about-section-title">六、免责声明</text>
            <text class="about-feature">6.1 本小程序提供的旅游信息仅供参考，我们不对信息的准确性、完整性、及时性作任何保证。</text>
            <text class="about-feature">6.2 因不可抗力或本小程序不能控制的原因导致的服务中断或其他缺陷，我们不承担任何责任。</text>
            <text class="about-feature">6.3 用户因自身操作（包括但不限于误操作、泄露账号密码等）导致的损失，本平台不承担责任。</text>
            <text class="about-feature">6.4 预订服务（门票、酒店等）的实际履行由第三方商家负责，因第三方商家原因导致的纠纷，我们有义务协助用户解决，但不承担直接赔偿责任。</text>

            <text class="about-section-title">七、隐私保护</text>
            <text class="about-feature">7.1 我们重视用户隐私保护，具体的隐私信息收集和使用规则，请参见《隐私政策》。</text>
            <text class="about-feature">7.2 未经您的同意，我们不会向第三方共享您的个人信息，法律法规另有规定或政府机关依法要求除外。</text>

            <text class="about-section-title">八、协议的修改</text>
            <text class="about-feature">8.1 根据国家法律法规变化及网站运营需要，我们有权对本协议条款不时地进行修改。</text>
            <text class="about-feature">8.2 修改后的协议一旦被张贴在本小程序上即生效，并代替原来的协议。用户可随时登录查阅最新协议。</text>
            <text class="about-feature">8.3 如您不同意修改后的协议，请停止使用本小程序服务。您继续使用本小程序服务将被视为接受修改后的协议。</text>

            <text class="about-section-title">九、服务终止</text>
            <text class="about-feature">9.1 您有权随时停止使用本小程序服务。</text>
            <text class="about-feature">9.2 如您违反本协议的任何条款，我们有权单方面终止向您提供服务，而无须给予事先通知。</text>

            <text class="about-section-title">十、争议解决</text>
            <text class="about-feature">10.1 本协议的成立、生效、履行、解释及纠纷解决，适用中华人民共和国大陆地区法律。</text>
            <text class="about-feature">10.2 若您与我们发生任何争议，双方应友好协商解决；协商不成的，您同意将争议提交至我们所在地有管辖权的人民法院诉讼解决。</text>

            <text class="about-section-title">十一、联系我们</text>
            <text class="about-feature">如您对本协议有任何疑问或建议，请通过以下方式联系我们：</text>
            <text class="about-feature">邮箱：contact@xundong.com</text>
            <text class="about-copyright">© 2026 寻侗三江团队 版权所有</text>
          </view>
        </scroll-view>
        <view class="modal-footer">
          <view class="modal-footer-btn" @tap="agreeAndClose('user')">同意并关闭</view>
        </view>
      </view>
    </view>

    <!-- 隐私政策弹窗 -->
    <view class="modal-mask" v-if="showPrivacyModal" @tap="showPrivacyModal = false">
      <view class="modal-box about-box" @tap.stop>
        <view class="modal-header">
          <text class="modal-title">隐私政策</text>
          <text class="modal-close" @tap="showPrivacyModal = false">✕</text>
        </view>
        <scroll-view class="modal-scroll" scroll-y>
          <view class="about-content">
            <text class="about-section-title">寻侗三江小程序隐私政策</text>
            <text class="about-date">更新日期：2026年4月1日 生效日期：2026年4月1日</text>

            <text class="about-section-title">一、引言</text>
            <text class="about-feature">1.1 寻侗三江团队（以下简称"我们"）非常重视用户隐私保护。本隐私政策适用于寻侗三江小程序的所有服务。</text>
            <text class="about-feature">1.2 在您使用我们的服务前，请仔细阅读本隐私政策。如您不同意本政策的任何内容，请立即停止使用我们的服务。</text>
            <text class="about-feature">1.3 当您使用我们的服务时，即表示您同意我们按照本隐私政策收集、使用、存储和分享您的相关信息。</text>

            <text class="about-section-title">二、我们收集的信息</text>
            <text class="about-feature">2.1 您主动提供的信息：</text>
            <text class="about-feature">- 账户信息：手机号码、微信昵称、微信头像</text>
            <text class="about-feature">- 个人信息：昵称、头像</text>
            <text class="about-feature">- 订单信息：联系人姓名、手机号、预订信息</text>
            <text class="about-feature">- 社区内容：您发布的帖子、评论、分享内容</text>
            <text class="about-feature">2.2 我们自动收集的信息：</text>
            <text class="about-feature">- 设备信息：设备型号、操作系统版本、唯一设备标识</text>
            <text class="about-feature">- 日志信息：访问时间、访问时长、浏览记录、搜索记录</text>
            <text class="about-feature">- 位置信息：在您授权后，我们会收集您的位置信息以提供地图导航和周边推荐服务</text>
            <text class="about-feature">2.3 来自第三方的信息：</text>
            <text class="about-feature">- 暂无第三方信息收集</text>

            <text class="about-section-title">三、我们如何使用信息</text>
            <text class="about-feature">3.1 我们收集的信息将用于以下目的：</text>
            <text class="about-feature">- 向您提供、维护和改进我们的服务</text>
            <text class="about-feature">- 处理您的订单和交易</text>
            <text class="about-feature">- 向您发送服务通知和订单状态更新</text>
            <text class="about-feature">- 为您提供个性化的旅游推荐</text>
            <text class="about-feature">- 保障我们的服务安全，预防、检测和调查欺诈活动</text>
            <text class="about-feature">- 与您沟通，回复您的咨询和反馈</text>
            <text class="about-feature">3.2 我们不会将您的个人信息用于与本隐私政策所述目的无关的用途。</text>

            <text class="about-section-title">四、信息的共享与披露</text>
            <text class="about-feature">4.1 我们不会向第三方共享您的个人信息，但以下情况除外：</text>
            <text class="about-feature">- 在获取您明确同意的情况下</text>
            <text class="about-feature">- 为完成预订服务（门票、酒店等），我们需要将必要的联系信息提供给相关服务提供商</text>
            <text class="about-feature">- 根据法律法规或政府机关的强制性要求</text>
            <text class="about-feature">- 在紧急情况下为保护用户或公众的人身安全</text>
            <text class="about-feature">4.2 我们不会出售您的个人信息。</text>

            <text class="about-section-title">五、信息的存储与保护</text>
            <text class="about-feature">5.1 我们会在实现本政策所述目的所需的最短时间内保留您的个人信息。</text>
            <text class="about-feature">5.2 我们采用业界标准的安全措施保护您的个人信息，包括数据加密传输、安全存储等技术手段。</text>
            <text class="about-feature">5.3 尽管我们采取了合理的安全措施，但请您理解，互联网传输和存储无法保证百分之百的安全。</text>

            <text class="about-section-title">六、您的权利</text>
            <text class="about-feature">6.1 您有权访问、更正、删除您的个人信息。您可以通过以下方式行使您的权利：</text>
            <text class="about-feature">- 在"我的"-"设置"中查看和修改个人信息</text>
            <text class="about-feature">- 通过联系客服请求删除账户</text>
            <text class="about-feature">6.2 您有权撤回您的授权同意。撤回同意后，我们将不再处理相应的个人信息，但不影响此前基于您的授权而进行的数据处理。</text>
            <text class="about-feature">6.3 您有权注销您的账户。账户注销后，我们将删除您的个人信息或对其进行匿名化处理。</text>

            <text class="about-section-title">七、未成年人保护</text>
            <text class="about-feature">7.1 我们非常重视对未成年人个人信息的保护。若您为未成年人，请在法定监护人的陪同下阅读本政策，并在获得法定监护人同意后使用我们的服务。</text>
            <text class="about-feature">7.2 如我们发现在未获得法定监护人同意的情况下收集了未成年人的个人信息，我们会尽快删除相关信息。</text>

            <text class="about-section-title">八、权限说明</text>
            <text class="about-feature">8.1 本小程序可能会申请以下权限：</text>
            <text class="about-feature">- 位置权限：用于地图导航和周边推荐（可选）</text>
            <text class="about-feature">- 相机权限：用于拍摄和上传图片（可选）</text>
            <text class="about-feature">- 相册权限：用于选择和上传图片（可选）</text>
            <text class="about-feature">- 存储权限：用于缓存数据以提升体验（可选）</text>
            <text class="about-feature">8.2 这些权限均为可选授权，您可以在设备系统设置中随时关闭。</text>

            <text class="about-section-title">九、隐私政策的更新</text>
            <text class="about-feature">9.1 我们可能会不时更新本隐私政策。更新后的政策将在本页面发布，并标注更新日期。</text>
            <text class="about-feature">9.2 继续使用我们的服务即表示您同意受更新后的隐私政策约束。</text>

            <text class="about-section-title">十、联系我们</text>
            <text class="about-feature">如您对本隐私政策有任何疑问、意见或投诉，请通过以下方式联系我们：</text>
            <text class="about-feature">邮箱：contact@xundong.com</text>
            <text class="about-feature">我们将在收到您的反馈后尽快回复。</text>
            <text class="about-copyright">© 2026 寻侗三江团队 版权所有</text>
          </view>
        </scroll-view>
        <view class="modal-footer">
          <view class="modal-footer-btn" @tap="agreeAndClose('privacy')">同意并关闭</view>
        </view>
      </view>
    </view>
  </view>
</template>

<script>
import auth from '../../utils/auth.js'
import { userApi } from '../../utils/api.js'

export default {
  data() {
    return {
      phone: '',
      password: '',
      showPassword: false,
      agreed: false,
      isLoading: false,
      showUserAgreementModal: false,
      showPrivacyModal: false
    }
  },
  onLoad(options) {
    // 检查是否从注册页跳转过来，如果是则自动填充账号密码
    if (options.fromRegister === 'true') {
      this.autoFillFromRegister()
    }
  },
  methods: {
    // 自动填充注册信息
    autoFillFromRegister() {
      const registerInfo = uni.getStorageSync('registerInfo')
      if (registerInfo) {
        console.log('从注册信息自动填充:', registerInfo)
        this.phone = registerInfo.phone || ''
        this.password = registerInfo.password || ''
        // 自动勾选协议
        this.agreed = true
        // 提示用户
        uni.showToast({
          title: '已自动填充注册信息',
          icon: 'success',
          duration: 1500
        })
      }
    },

    // 显示协议弹窗
    showAgreement(type) {
      if (type === 'user') {
        this.showUserAgreementModal = true
      } else {
        this.showPrivacyModal = true
      }
    },

    // 同意并关闭弹窗
    agreeAndClose(type) {
      this.agreed = true
      if (type === 'user') {
        this.showUserAgreementModal = false
      } else {
        this.showPrivacyModal = false
      }
      uni.showToast({ title: '已同意协议', icon: 'success' })
    },

    // 账号密码登录
    async handleLogin() {
      if (!this.agreed) {
        uni.showToast({ title: '请先同意用户协议和隐私政策', icon: 'none' })
        return
      }

      if (!this.phone) {
        uni.showToast({ title: '请输入账号', icon: 'none' })
        return
      }

      if (!this.password) {
        uni.showToast({ title: '请输入密码', icon: 'none' })
        return
      }

      this.isLoading = true

      try {
        let result = null
        try {
          result = await userApi.login({
            phone: this.phone,
            password: this.password
          })
        } catch (apiError) {
          console.error('登录API调用失败:', apiError)
          // 检查是否是后端返回的业务错误（如账号被禁用）
          const errorMsg = apiError?.message || apiError?.msg || apiError?.data?.message || ''
          if (errorMsg) {
            // 如果有具体错误信息，显示后端返回的错误
            uni.showToast({ title: errorMsg, icon: 'none', duration: 2000 })
          } else {
            // 如果没有错误信息，说明是网络问题
            uni.showToast({ title: '网络连接失败，请稍后重试', icon: 'none' })
          }
          this.isLoading = false
          return
        }

        console.log('登录响应:', result)

        // 确保token存在
        const token = result.token || result.accessToken || result.data?.token
        const userId = result.id || result.userId || result.data?.id

        if (!token) {
          console.error('登录响应中没有token:', result)
          uni.showToast({ title: '登录失败，未获取到token', icon: 'none' })
          this.isLoading = false
          return
        }

        auth.setUserInfo({
          isLogin: true,
          phone: result.phone || this.phone,
          name: result.name || '',
          nickname: result.nickname || result.name || '侗乡旅行者',
          avatar: result.avatar || '',
          userId: userId,
          token: token,
          loginType: 'password',
          loginTime: new Date().toISOString()
        })

        // 额外确保token被保存
        uni.setStorageSync('token', token)
        console.log('登录成功，token已保存:', token)

        // 登录成功后清除注册信息（可选）
        uni.removeStorageSync('registerInfo')

        uni.showToast({ title: '登录成功', icon: 'success' })
        setTimeout(() => { uni.switchTab({ url: '/pages/index/index' }) }, 800)
      } catch (error) {
        console.error('登录失败:', error)
        // 如果后端返回了具体错误信息（如"账号已禁用"），不覆盖
        const msg = error?.message || ''
        if (!msg.includes('禁用')) {
          uni.showToast({ title: '账号或密码错误', icon: 'none' })
        }
      } finally {
        this.isLoading = false
      }
    },

    // 游客模式
    async guestLogin() {
      try {
        uni.showLoading({ title: '进入游客模式...' })

        let result = null
        try {
          // 调用后端获取游客token
          result = await userApi.guestLogin()
        } catch (apiError) {
          console.warn('后端接口未连接，使用本地游客模式:', apiError)
          // 降级：使用本地游客模式
          result = {
            token: 'guest_local_' + Date.now(),
            isGuest: true,
            nickname: '游客'
          }
        }

        auth.setUserInfo({
          isLogin: false,
          name: '游客',
          nickname: '游客',
          loginType: 'guest',
          token: result.token,
          loginTime: new Date().toISOString()
        })

        uni.hideLoading()
        uni.showToast({
          title: '游客模式：只能查看信息',
          icon: 'none',
          duration: 2000
        })

        setTimeout(() => {
          uni.switchTab({ url: '/pages/index/index' })
        }, 1000)

      } catch (error) {
        uni.hideLoading()
        console.error('游客登录失败:', error)
        uni.showToast({
          title: '进入游客模式失败',
          icon: 'none'
        })
      }
    },

    // 跳转到注册页
    goRegister() {
      uni.redirectTo({ url: '/pages/register/register' })
    }
  }
}
</script>

<style scoped lang="scss">
.login-page {
  min-height: 100vh;
  background: linear-gradient(135deg, #1D3B5A 0%, #2E5F8E 50%, #3D7AB5 100%);
  position: relative;
  overflow: hidden;
}

.login-bg-decoration {
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
  width: 400rpx;
  height: 400rpx;
  background: #FDFCF8;
  top: -100rpx;
  right: -100rpx;
}

.circle-2 {
  width: 300rpx;
  height: 300rpx;
  background: #D4A373;
  bottom: 10%;
  left: -80rpx;
}

.circle-3 {
  width: 200rpx;
  height: 200rpx;
  background: #FDFCF8;
  top: 40%;
  right: -50rpx;
}

.login-header {
  padding: 80rpx 60rpx 40rpx;
  position: relative;
  z-index: 1;
}

.logo-wrapper {
  display: flex;
  align-items: center;
  gap: 24rpx;
}

.logo {
  width: 120rpx;
  height: 120rpx;
  background: linear-gradient(135deg, #FDFCF8 0%, #f0f0f0 100%);
  border-radius: 30rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 8rpx 32rpx rgba(0, 0, 0, 0.15);
}

.logo-icon {
  font-size: 60rpx;
}

.brand-info {
  display: flex;
  flex-direction: column;
  gap: 8rpx;
}

.brand-name {
  font-size: 44rpx;
  font-weight: bold;
  color: #FDFCF8;
  text-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.2);
}

.brand-tagline {
  font-size: 26rpx;
  color: rgba(255, 255, 255, 0.8);
}

.login-card {
  background: #FDFCF8;
  border-radius: 40rpx 40rpx 0 0;
  padding: 50rpx 40rpx;
  position: relative;
  z-index: 1;
  min-height: 60vh;
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
  margin-bottom: 40rpx;
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

.login-actions {
  margin-bottom: 30rpx;
}

.login-btn {
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

.login-btn[disabled] {
  background: #ccc;
  box-shadow: none;
}

.login-btn .btn-text {
  font-size: 32rpx;
  font-weight: bold;
  color: #FDFCF8;
}

.card-footer {
  display: flex;
  flex-wrap: wrap;
  justify-content: center;
  align-items: center;
  gap: 8rpx;
  margin-bottom: 30rpx;
}

.footer-left {
  display: flex;
  align-items: center;
  gap: 8rpx;
}

.checkbox {
  width: 32rpx;
  height: 32rpx;
  border: 2rpx solid #ddd;
  border-radius: 6rpx;
  display: flex;
  align-items: center;
  justify-content: center;
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

.register-prompt {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 8rpx;
}

.prompt-text {
  font-size: 26rpx;
  color: #999;
}

.register-link {
  font-size: 26rpx;
  color: #D4A373;
  font-weight: bold;
}

.guest-mode {
  text-align: center;
  padding: 30rpx;
}

.guest-tip {
  font-size: 20rpx;
  color: rgba(255, 255, 255, 0.5);
  display: block;
}

/* ===== 协议弹窗样式 ===== */
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
  z-index: 999;
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
}

.about-box {
  max-height: 85vh;
  display: flex;
  flex-direction: column;
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 30rpx;
  border-bottom: 1rpx solid #F0EDE8;
  flex-shrink: 0;
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

.modal-scroll {
  flex: 1;
  min-height: 0;
  overflow: hidden;
}

.about-content {
  padding: 30rpx;
}

.about-section-title {
  display: block;
  font-size: 28rpx;
  font-weight: bold;
  color: #1D3B5A;
  margin: 24rpx 0 12rpx;
}

.about-section-title:first-child {
  margin-top: 0;
  text-align: center;
  font-size: 32rpx;
}

.about-date {
  display: block;
  font-size: 22rpx;
  color: #999;
  text-align: center;
  margin-bottom: 20rpx;
}

.about-feature {
  display: block;
  font-size: 24rpx;
  color: #666;
  line-height: 1.8;
  margin-bottom: 8rpx;
}

.about-copyright {
  display: block;
  text-align: center;
  font-size: 22rpx;
  color: #ccc;
  margin-top: 30rpx;
  padding-top: 20rpx;
  border-top: 1rpx solid #F0EDE8;
}

.modal-footer {
  padding: 20rpx 30rpx 30rpx;
  border-top: 1rpx solid #F0EDE8;
  flex-shrink: 0;
  position: relative;
  z-index: 10;
}

.modal-footer-btn {
  width: 100%;
  text-align: center;
  padding: 24rpx;
  background: linear-gradient(135deg, #1D3B5A, #2E5F8E);
  border-radius: 50rpx;
  font-size: 28rpx;
  color: #FDFCF8;
  font-weight: bold;
  position: relative;
  z-index: 10;
}
</style>
