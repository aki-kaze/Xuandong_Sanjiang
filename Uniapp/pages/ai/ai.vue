<template>
  <view class="ai-page">
    <!-- 顶部标题 Header -->
    <view class="ai-header">
      <text class="ai-sessions-btn" @tap="toggleSessionList">历史</text>
      <view class="ai-avatar-wrap">
        <image class="ai-avatar-icon" src="/static/icons/ai_robot.png" mode="aspectFit" />
      </view>
      <view class="ai-header-info">
        <text class="ai-name">侗乡 AI 游览管家</text>
        <text class="ai-status">● 在线 · 为您生成专属三江行程</text>
      </view>
      <text class="ai-clear" @tap="clearChat">清空</text>
    </view>

    <!-- 会话列表侧边栏 -->
    <view class="session-drawer" v-if="showSessionList">
      <view class="session-drawer-mask" @tap="showSessionList = false"></view>
      <view class="session-drawer-content">
        <view class="session-drawer-header">
          <text class="drawer-title">聊天记录</text>
          <view class="drawer-actions">
            <text class="new-chat-btn" @tap="createNewSession">+ 新对话</text>
            <text class="drawer-close" @tap="showSessionList = false">×</text>
          </view>
        </view>
        <scroll-view class="session-list" scroll-y>
          <view class="session-item"
                v-for="session in sessions"
                :key="session.id"
                :class="{ active: currentSessionId === session.id }"
                @tap="selectSession(session)">
            <view class="session-item-top">
              <text class="session-title">{{ session.title }}</text>
              <text class="session-delete" @tap.stop="deleteSession(session)">×</text>
            </view>
            <text class="session-preview" v-if="session.lastMessage">{{ session.lastMessage }}</text>
            <text class="session-meta">{{ formatSessionTime(session.updateTime || session.createTime) }} · {{ session.messageCount }}条消息</text>
          </view>
          <view class="empty-sessions" v-if="sessions.length === 0">
            <text class="empty-sessions-text">暂无聊天记录</text>
            <text class="empty-sessions-sub">开始对话后会自动保存</text>
          </view>
        </scroll-view>
      </view>
    </view>

    <!-- 聊天消息列表 -->
    <scroll-view class="chat-list" scroll-y :scroll-top="scrollTop" :scroll-into-view="scrollIntoViewId" :scroll-with-animation="true" id="chatScroll">
      <view class="chat-messages">
        <!-- 欢迎消息 -->
        <view class="msg-row msg-ai" id="welcome-msg">
          <image class="msg-avatar ai-avatar" src="/static/icons/ai_robot.png" mode="aspectFit" />
          <view class="msg-bubble ai-bubble">
            <text class="msg-text">您好！我是侗乡 AI 游览管家，专注于三江侗族自治县旅游服务。我可以帮您：</text>
            <view class="quick-questions-grid">
              <view class="quick-q-card" v-for="(q, qi) in quickQuestions" :key="q" @tap="sendQuickQuestion(q)">
                <view class="quick-q-icon-wrap" :style="{ background: tagStyles[qi % tagStyles.length].bg }">
                  <image class="quick-q-icon" :src="tagStyles[qi % tagStyles.length].icon" mode="aspectFit" />
                </view>
                <text class="quick-q-text">{{ q }}</text>
                <text class="quick-q-arrow">›</text>
              </view>
            </view>
            <text class="msg-text msg-text-bottom">请问有什么可以帮您？</text>
          </view>
        </view>

        <!-- 对话消息 -->
        <view class="msg-row" :class="msg.role === 'user' ? 'msg-user' : 'msg-ai'" v-for="(msg, idx) in messages" :key="idx" :id="'msg-' + idx">
          <view class="msg-avatar" :class="msg.role === 'user' ? 'user-avatar' : 'ai-avatar'">
            <text v-if="msg.role === 'user'">我</text>
            <image v-else src="/static/icons/ai_robot.png" mode="aspectFit" />
          </view>
          <view class="msg-bubble" :class="msg.role === 'user' ? 'user-bubble' : 'ai-bubble'">
            <text class="msg-text" v-if="!msg.loading && !msg.toolResult && !msg.itinerary">{{ msg.content }}</text>
            
            <!-- 工具结果展示 -->
            <view v-if="msg.toolResult" class="tool-result">
              <!-- 景点卡片 -->
              <view v-if="msg.toolType === 'attraction'" class="attraction-cards">
                <view class="attraction-card" v-for="item in msg.toolData" :key="item.id" @tap="goAttractionDetail(item)">
                  <image class="card-cover" :src="item.cover" mode="aspectFill" />
                  <view class="card-info">
                    <text class="card-name">{{ item.name }}</text>
                    <view class="card-meta">
                      <text class="card-rating">⭐ {{ item.rating }}</text>
                      <text class="card-price" v-if="item.price > 0">¥{{ item.price }}</text>
                      <text class="card-price" v-else>免费</text>
                    </view>
                    <text class="card-desc">{{ item.description.slice(0, 50) }}...</text>
                  </view>
                </view>
              </view>
              
              <!-- 美食卡片 -->
              <view v-if="msg.toolType === 'food'" class="food-cards">
                <view class="food-card" v-for="item in msg.toolData" :key="item.id" @tap="goFoodDetail(item)">
                  <image class="food-cover" :src="item.cover" mode="aspectFill" />
                  <view class="food-info">
                    <text class="food-name">{{ item.name }}</text>
                    <view class="food-meta">
                      <text class="food-rating">⭐ {{ item.rating }}</text>
                      <text class="food-price">{{ item.price }}</text>
                    </view>
                    <text class="food-desc">{{ item.description.slice(0, 40) }}...</text>
                  </view>
                </view>
              </view>
              
              <!-- 路线卡片 -->
              <view v-if="msg.toolType === 'route'" class="route-cards">
                <view class="route-card" v-for="item in msg.toolData" :key="item.id" @tap="goRouteDetail(item)">
                  <view class="route-header" :style="{ background: routeColors[item.id % routeColors.length] }">
                    <text class="route-name">{{ item.name }}</text>
                    <view class="route-tags">
                      <text class="route-tag" v-for="tag in item.tags" :key="tag">{{ tag }}</text>
                    </view>
                  </view>
                  <view class="route-content">
                    <view class="route-stops">
                      <view class="stop-dot" v-for="(stop, idx) in item.stops.slice(0, 4)" :key="idx">
                        <text class="stop-dot-circle"></text>
                        <text class="stop-dot-line" v-if="idx < 3"></text>
                      </view>
                    </view>
                    <view class="route-info-row">
                      <text class="info-item"><image class="icon-img" src="/static/icons/duration.png" mode="aspectFit" /> {{ item.duration }}</text>
                      <text class="info-item"><image class="icon-img" src="/static/icons/distance.png" mode="aspectFit" /> {{ item.distance }}</text>
                      <text class="info-item"><image class="icon-img" src="/static/icons/difficulty.png" mode="aspectFit" /> {{ item.difficulty }}</text>
                    </view>
                  </view>
                </view>
              </view>

              <!-- 非遗文化跳转按钮 -->
              <view v-if="msg.toolType === 'heritage'" class="action-buttons">
                <view class="action-btn" @tap="goToHeritage"><image class="icon-img" src="/static/icons/heritage.png" mode="aspectFit" /> 前往非遗页面</view>
              </view>

              <!-- 购票信息跳转按钮 -->
              <view v-if="msg.toolType === 'ticket'" class="action-buttons">
                <view class="action-btn" @tap="goToTickets"><image class="icon-img" src="/static/icons/ticket.png" mode="aspectFit" /> 前往购票页面</view>
              </view>

              <!-- 酒店住宿跳转按钮（新增） -->
              <view v-if="msg.toolType === 'hotel'" class="action-buttons">
                <view class="action-btn" @tap="goToHotels"><image class="icon-img" src="/static/icons/hotel.png" mode="aspectFit" /> 前往酒店页面</view>
              </view>
            </view>

            <!-- MCP 智能行程规划（核心新增） -->
            <view v-if="msg.itinerary" class="itinerary-result">
              <view class="itinerary-title">✅ {{ msg.itinerary.title }}</view>
              <view class="itinerary-days" v-for="(day, d) in msg.itinerary.days" :key="d">
                <view class="day-box">
                  <view class="day-title">第 {{ day.day }} 天 | {{ day.date }}</view>
                  <view class="day-plans">
                    <view class="plan-item" v-for="(item, i) in day.plans" :key="i">
                      <view class="plan-dot"></view>
                      <view class="plan-body">
                        <view class="plan-header">
                          <text class="plan-time">{{ item.time }}</text>
                          <text class="plan-title">{{ item.title }}</text>
                        </view>
                        <text class="plan-desc" v-if="item.desc">{{ item.desc }}</text>
                        <view class="plan-traffic" v-if="item.traffic">
                          <image class="traffic-icon-img" src="/static/icons/drive.png" mode="aspectFit" />
                          <text>{{ item.traffic }}</text>
                        </view>
                      </view>
                    </view>
                  </view>
                </view>
              </view>
              <view class="save-itinerary-btn" @tap="saveItinerary(msg.itinerary)">
                <text class="save-btn-text">保存到我的路线</text>
              </view>
            </view>
            
            <view class="typing-indicator" v-if="msg.loading">
              <view class="dot"></view>
              <view class="dot"></view>
              <view class="dot"></view>
            </view>

            <!-- 满意度评价按钮（仅 AI 消息且非 loading） -->
            <view class="rating-bar" v-if="msg.role === 'ai' && !msg.loading">
              <template v-if="!msg.rating">
                <text class="rating-label">这个回答有帮助吗？</text>
                <view class="rating-btns">
                  <view class="rating-btn like" @tap="rateMessage(idx, 2)">
                    <text>👍 满意</text>
                  </view>
                  <view class="rating-btn dislike" @tap="rateMessage(idx, 1)">
                    <text>👎 不满意</text>
                  </view>
                </view>
              </template>
              <text class="rating-done" v-else-if="msg.rating === 2">✓ 感谢您的反馈！</text>
              <text class="rating-done" v-else-if="msg.rating === 1">✓ 已收到反馈，我们会改进</text>
            </view>
          </view>
        </view>
      </view>
    </scroll-view>

    <!-- 快捷功能按钮（对话中） -->
    <view class="quick-actions" v-if="messages.length > 0">
      <view class="quick-action-btn" @tap="goToAttractions">
        <image class="action-icon" src="/static/icons/search.png" mode="aspectFit" />
        <text class="action-text">搜索景点</text>
      </view>
      <view class="quick-action-btn" @tap="goToFoods">
        <image class="action-icon" src="/static/icons/food.png" mode="aspectFit" />
        <text class="action-text">推荐美食</text>
      </view>
      <view class="quick-action-btn" @tap="goToRoutes">
        <image class="action-icon" src="/static/icons/route.png" mode="aspectFit" />
        <text class="action-text">路线规划</text>
      </view>
      <view class="quick-action-btn" @tap="goToHeritage">
        <image class="action-icon" src="/static/icons/heritage.png" mode="aspectFit" />
        <text class="action-text">非遗文化</text>
      </view>
      <view class="quick-action-btn" @tap="goToHotels">
        <image class="action-icon" src="/static/icons/hotel.png" mode="aspectFit" />
        <text class="action-text">住宿推荐</text>
      </view>
      <view class="quick-action-btn" @tap="goToTickets">
        <image class="action-icon" src="/static/icons/ticket.png" mode="aspectFit" />
        <text class="action-text">购票信息</text>
      </view>
    </view>

    <!-- 输入区域 -->
    <view class="input-area">
      <view class="input-wrap">
        <text class="mic-icon"><image class="icon-img" src="/static/icons/send.png" mode="aspectFit" /></text>
        <input
          class="chat-input"
          v-model="inputText"
          placeholder="点击输入提问..."
          :adjust-position="true"
          @confirm="sendMessage"
          confirm-type="send"
        />
        <view class="send-btn" :class="{ active: inputText.trim() }" @tap="sendMessage">
          <text class="send-icon">➤</text>
        </view>
      </view>
    </view>
  </view>
</template>

<script>
import { staticData } from '../../utils/static-data.js'
import { aiApi } from '../../utils/api.js'
import config from '../../utils/config.js'

export default {
  data() {
    return {
      messages: [],
      inputText: '',
      scrollTop: 0,
      scrollIntoViewId: '',
      isLoading: false,
      currentSessionId: null,
      sessions: [],
      showSessionList: false,
      quickQuestions: [
        '三江有什么好玩的？',
        '三江有什么特色美食？',
        '帮我规划三江两日游',
        '今天三江的天气怎么样？',
        '推荐三江的酒店住宿',
        '三江非遗文化介绍'
      ],
      tagStyles: [
        { bg: 'linear-gradient(135deg, rgba(29,59,90,0.12), rgba(46,95,142,0.08))', icon: '/static/icons/search.png' },
        { bg: 'linear-gradient(135deg, rgba(230,81,0,0.10), rgba(255,152,0,0.06))', icon: '/static/icons/food.png' },
        { bg: 'linear-gradient(135deg, rgba(45,80,22,0.10), rgba(74,140,42,0.06))', icon: '/static/icons/route.png' },
        { bg: 'linear-gradient(135deg, rgba(123,63,0,0.10), rgba(212,163,115,0.06))', icon: '/static/icons/map.png' },
        { bg: 'linear-gradient(135deg, rgba(29,59,90,0.10), rgba(46,95,142,0.06))', icon: '/static/icons/hotel.png' },
        { bg: 'linear-gradient(135deg, rgba(123,63,0,0.10), rgba(180,120,60,0.06))', icon: '/static/icons/heritage.png' }
      ],
      routeColors: ['linear-gradient(135deg,#1D3B5A,#2E5F8E)', 'linear-gradient(135deg,#2D5016,#4A8C2A)', 'linear-gradient(135deg,#7B3F00,#D4A373)'],
      responseCache: new Map(),
      cacheConfig: {
        maxAge: 5 * 60 * 1000,  // 5分钟过期
        maxSize: 50              // 最大缓存条数
      },
      lastRequestTime: 0,
      isRequesting: false,  // 防止重复请求
      requestRetryCount: 0,  // 重试计数
      maxRetries: 3,  // 最大重试次数
      userPreferences: {
        interests: [],
        visitedPlaces: [],
        dietaryRestrictions: []
      }
    }
  },
  onLoad() {
    this.loadSessions()
  },
  onShow() {
    this.loadSessions()
  },
  methods: {
    sendQuickQuestion(q) {
      this.inputText = q
      this.sendMessage()
    },
    async sendMessage() {
      const text = this.inputText.trim()
      if (!text || this.isLoading || this.isRequesting) return

      // 优化防抖：500ms
      const now = Date.now()
      if (now - this.lastRequestTime < 500) {
        return
      }
      this.lastRequestTime = now
      this.isRequesting = true
      this.requestRetryCount = 0

      this.messages.push({ role: 'user', content: text })
      this.inputText = ''
      this.scrollToBottom()
      this.cleanupOldMessages()

      // 确保会话存在（用于保存消息）
      await this.ensureSession(text)

      // 优先使用本地 MCP 工具匹配（响应快、数据准确）
      const localResult = this.runMCPTools(text)
      if (localResult) {
        this.messages.push(localResult)
        // 保存用户消息和AI回复到后端
        this.saveUserMessage(text)
        this.saveToolResult(localResult)
        this.isLoading = false
        this.isRequesting = false
        this.scrollToBottom()
        return
      }

      this.isLoading = true
      const loadingIdx = this.messages.length
      this.messages.push({ role: 'ai', content: '', loading: true })

      try {
        await this.callAIAPIWithRetry(text, loadingIdx)
        // callAIAPIWithRetry 内部已经处理了错误，这里不需要再处理
      } catch (error) {
        // 理论上不会到这里，因为 callAIAPIWithRetry 已经不再抛出错误
        console.error('AI 请求异常:', error)
      } finally {
        this.isLoading = false
        this.isRequesting = false
      }
    },

    // ======================
    // MCP 旅游规划工具核心（简化版 - 仅保留跳转功能）
    // ======================
    runMCPTools(text) {
      const t = text.toLowerCase()

      // 1. 购票信息（需要跳转页面）
      if (this.isTicketQuery(t)) {
        const result = {
          role: 'ai',
          content: '为您提供三江景点购票信息，点击下方按钮前往购票页面👇',
          toolResult: true,
          toolType: 'ticket',
          loading: false
        }
        this.saveToolResult(result)
        return result
      }

      // 2. 酒店住宿（需要跳转页面）
      if (this.isHotelQuery(t)) {
        const result = {
          role: 'ai',
          content: '为您推荐三江住宿推荐\n\n🏨 **三江县城**：交通便利，酒店齐全，价格 100-300 元/晚\n🏡 **程阳八寨**：推荐侗族特色民宿，体验当地文化，80-200 元/晚\n🌿 **布央茶园周边**：生态民宿，空气清新，可体验采茶\n\n点击下方按钮跳转查看更多住宿详情👇',
          toolResult: true,
          toolType: 'hotel',
          loading: false
        }
        this.saveToolResult(result)
        return result
      }

      // 其他所有问题（包括交通、天气等）都交给 AI 回答
      return null
    },

    // 增强版：智能筛选景点
    filterAttractions(text) {
      let list = [...staticData.attractions]
      
      if (text.includes('必去') || text.includes('推荐') || text.includes('好玩')) {
        return list.slice(0, 5)
      }
      if (text.includes('古镇') || text.includes('程阳')) {
        return list.filter(a => a.name.includes('程阳') || a.category === '古镇村落')
      }
      if (text.includes('茶园') || text.includes('布央')) {
        return list.filter(a => a.name.includes('布央') || a.category === '生态农业')
      }
      if (text.includes('自然') || text.includes('山水')) {
        return list.filter(a => a.category === '自然风光')
      }
      
      return list.slice(0, 5)
    },

    // 增强版：智能筛选美食
    filterFoods(text) {
      let list = [...staticData.foods]
      
      if (text.includes('酸鱼') || text.includes('酸肉')) {
        return list.filter(f => f.name.includes('酸') || f.name.includes('鱼') || f.name.includes('肉'))
      }
      if (text.includes('油茶')) {
        return list.filter(f => f.name.includes('油茶'))
      }
      if (text.includes('糯米饭') || text.includes('糯')) {
        return list.filter(f => f.name.includes('糯'))
      }
      if (text.includes('推荐') || text.includes('特色')) {
        return list
      }
      
      return list
    },

    // 新增：酒店查询
    isHotelQuery(text) {
      const kw = [
        '三江住宿', '三江酒店', '住哪里', '民宿', '客栈', '住什么', '住宿推荐',
        '三江住哪里', '三江住宿推荐', '三江酒店推荐', '三江民宿推荐', '三江客栈推荐',
        '三江住宿攻略', '三江住宿指南', '三江住宿价格', '三江住宿多少钱', '三江住宿贵吗',
        '三江住宿便宜', '三江住宿哪家好', '三江住宿哪家好', '三江住宿推荐', '三江住宿攻略',
        '三江住宿指南', '三江住宿价格', '三江住宿多少钱', '三江住宿贵吗', '三江住宿便宜'
      ]
      return kw.some(k => text.includes(k))
    },

    isItineraryQuery(text) {
      const kw = [
        '三江一日游', '三江两日游', '三江三日游', '三江行程', '三江怎么玩', '三江游玩路线', '帮我规划', '一日游', '两日游',
        '三江旅游攻略', '三江旅游路线', '三江游玩攻略', '三江游玩路线', '三江行程规划', '三江行程推荐',
        '三江怎么玩', '三江怎么安排', '三江旅游安排', '三江旅游计划', '三江游玩计划', '三江游玩安排',
        '三江旅游指南', '三江旅游建议', '三江旅游建议', '三江旅游推荐', '三江旅游推荐', '三江旅游推荐路线'
      ]
      return kw.some(k => text.includes(k))
    },

    isHeritageQuery(text) {
      const kw = [
        '三江非遗', '侗族非遗', '侗族大歌', '三江风雨桥', '三江鼓楼',
        '侗族文化', '侗族传统', '侗族习俗', '侗族历史', '侗族介绍',
        '三江文化', '三江传统', '三江习俗', '三江历史', '三江介绍',
        '三江非遗文化', '三江非遗项目', '三江非遗传承', '三江非遗传承人',
        '侗族大歌介绍', '侗族大歌历史', '侗族大歌传承', '侗族大歌表演',
        '三江风雨桥介绍', '三江风雨桥历史', '三江风雨桥建筑', '三江风雨桥特色',
        '三江鼓楼介绍', '三江鼓楼历史', '三江鼓楼建筑', '三江鼓楼特色'
      ]
      return kw.some(k => text.includes(k))
    },

    isTicketQuery(text) {
      const kw = [
        '三江门票', '景区购票', '三江买票', '三江票价',
        '三江门票价格', '三江门票多少钱', '三江门票贵吗', '三江门票便宜',
        '三江门票优惠', '三江门票折扣', '三江门票团购', '三江门票预订',
        '三江门票购买', '三江门票怎么买', '三江门票哪里买', '三江门票网上购买',
        '三江景区门票', '三江景点门票', '三江景点购票', '三江景区购票'
      ]
      return kw.some(k => text.includes(k))
    },

    isTrafficQuery(text) {
      const kw = [
        '去三江', '三江交通', '怎么去三江', '三江导航', '到三江', '怎么坐车', '坐车', '交通',
        '三江怎么去', '三江怎么到', '三江交通方式', '三江交通指南', '三江交通攻略',
        '三江公交', '三江大巴', '三江高铁', '三江火车', '三江飞机', '三江自驾',
        '三江打车', '三江出租车', '三江班车', '三江客车', '三江驾车',
        '三江路线', '三江导航', '三江地图', '三江怎么走', '三江怎么到达'
      ]
      return kw.some(k => text.includes(k))
    },

    isWeatherQuery(text) {
      const kw = [
        '三江天气', '三江温度', '三江下雨', '三江气候',
        '三江天气怎么样', '三江气温', '三江气候如何', '三江天气预报',
        '三江今天天气', '三江明天天气', '三江后天天气', '三江周末天气',
        '三江天气情况', '三江天气好吗', '三江天气如何', '三江天气适合旅游吗',
        '三江需要带伞吗', '三江需要穿什么', '三江穿衣指南', '三江穿衣建议',
        '三江的天气', '天气怎样', '天气如何'
      ]
      return kw.some(k => text.includes(k))
    },

    // 生成智能行程（MCP 核心能力增强版）
    generateItinerary(text) {
      const day1 = [
        { time: '09:00', title: '程阳八寨', desc: '观赏世界最长风雨桥，体验侗族鼓楼文化', traffic: '县城打车25分钟' },
        { time: '12:00', title: '侗族午餐', desc: '品尝酸鱼、酸肉、油茶', traffic: '' },
        { time: '14:00', title: '侗族大歌表演', desc: '世界级非物质文化遗产', traffic: '' },
        { time: '17:00', title: '返回县城', desc: '结束行程', traffic: '' }
      ]

      const day2 = [
        { time: '09:30', title: '布央仙人山', desc: '万亩茶园，云海观景', traffic: '公交50分钟' },
        { time: '12:30', title: '茶园餐厅', desc: '茶餐特色宴', traffic: '' },
        { time: '14:30', title: '丹洲古镇', desc: '明代古县城，四面环水', traffic: '自驾40分钟' }
      ]

      const day3 = [
        { time: '09:00', title: '月也侗寨', desc: '侗族文化体验村', traffic: '县城打车15分钟' },
        { time: '12:00', title: '特色午餐', desc: '侗族风味美食', traffic: '' },
        { time: '14:00', title: '非遗体验馆', desc: '体验侗锦织造、蜡染', traffic: '' },
        { time: '16:00', title: '自由活动', desc: '购买特产，准备返程', traffic: '' }
      ]

      const day4 = [
        { time: '08:30', title: '深度体验程阳八寨', desc: '走进村寨，探访侗族人家', traffic: '' },
        { time: '11:30', title: '农家午餐', desc: '品尝地道农家菜', traffic: '' },
        { time: '13:30', title: '侗族手工艺体验', desc: '学习制作银饰、侗锦', traffic: '' },
        { time: '16:00', title: '返程准备', desc: '整理行李，结束愉快的三江之旅', traffic: '' }
      ]

      if (text.includes('一日')) {
        return {
          role: 'ai',
          itinerary: {
            title: '三江精品一日游',
            days: [{ day: 1, date: '当日', plans: day1 }]
          }
        }
      }

      if (text.includes('两日')) {
        return {
          role: 'ai',
          itinerary: {
            title: '三江深度两日游',
            days: [
              { day: 1, date: '第一天', plans: day1 },
              { day: 2, date: '第二天', plans: day2 }
            ]
          }
        }
      }

      if (text.includes('三日')) {
        return {
          role: 'ai',
          itinerary: {
            title: '三江完整三日游',
            days: [
              { day: 1, date: '第一天', plans: day1 },
              { day: 2, date: '第二天', plans: day2 },
              { day: 3, date: '第三天', plans: day3 }
            ]
          }
        }
      }

      if (text.includes('四日')) {
        return {
          role: 'ai',
          itinerary: {
            title: '三江深度四日游',
            days: [
              { day: 1, date: '第一天', plans: day1 },
              { day: 2, date: '第二天', plans: day2 },
              { day: 3, date: '第三天', plans: day3 },
              { day: 4, date: '第四天', plans: day4 }
            ]
          }
        }
      }

      return {
        role: 'ai', content: '已为您规划推荐路线👇',
        toolResult: true, toolType: 'route', toolData: staticData.routes.slice(0, 3)
      }
    },

    // 优化历史消息处理
    optimizeHistory() {
      return this.messages
        .filter(m => m.content && !m.loading)
        .slice(-5)  // 减少到最近5条消息
        .map(m => ({
          role: m.role === 'user' ? 'user' : 'assistant',
          content: this.truncateMessage(m.content, 200) // 限制每条消息长度
        }))
    },

    // 截断消息
    truncateMessage(text, maxLength) {
      if (!text || text.length <= maxLength) return text
      return text.substring(0, maxLength) + '...'
    },

    // 清理旧消息
    cleanupOldMessages() {
      if (this.messages.length > 100) {
        this.messages = this.messages.slice(-50)
      }
    },

    // 缓存管理方法
    getCacheKey(text, historyLength) {
      // 简单的字符串哈希
      const str = `${text}_${historyLength}`
      let hash = 0
      for (let i = 0; i < str.length; i++) {
        const char = str.charCodeAt(i)
        hash = ((hash << 5) - hash) + char
        hash = hash & hash
      }
      return hash.toString()
    },

    getFromCache(key) {
      const cached = this.responseCache.get(key)
      if (cached && Date.now() - cached.timestamp < this.cacheConfig.maxAge) {
        console.log('缓存命中:', key)
        return cached.data
      }
      if (cached) {
        this.responseCache.delete(key)  // 过期删除
      }
      return null
    },

    setCache(key, data) {
      // 如果缓存满了，删除最旧的
      if (this.responseCache.size >= this.cacheConfig.maxSize) {
        const firstKey = this.responseCache.keys().next().value
        this.responseCache.delete(firstKey)
      }
      this.responseCache.set(key, {
        data,
        timestamp: Date.now()
      })
    },

    // 带重试的 API 调用
    async callAIAPIWithRetry(text, loadingIdx, retryCount = 0) {
      try {
        await this.callAIAPI(text, loadingIdx)
      } catch (error) {
        if (retryCount < this.maxRetries) {
          // 指数退避：1s, 2s, 4s
          const delay = Math.pow(2, retryCount) * 1000
          console.log(`AI API 请求失败，${delay}ms 后重试 (${retryCount + 1}/${this.maxRetries})`)
          await new Promise(resolve => setTimeout(resolve, delay))
          return await this.callAIAPIWithRetry(text, loadingIdx, retryCount + 1)
        }
        // 重试次数用尽后，不再抛出错误（callAIAPI 已经处理了）
        console.log('AI API 重试失败，已使用本地回复')
      }
    },
    isAttractionQuery(text) {
      const keywords = [
        '三江景点', '景点推荐', '必去景点', '有哪些景点', '游玩', '景区', '古镇推荐', '茶园', '鼓楼', '风雨桥', '程阳八寨', '布央', '丹洲', '月也侗寨',
        '三江旅游', '三江哪里好玩', '三江有什么好玩的', '三江好玩的地方', '三江必去', '三江值得去', '三江旅游攻略', '三江旅游指南',
        '旅游景点', '值得去的地方', '推荐几个景点', '哪里好玩', '三江介绍', '三江概况', '三江简介', '三江特色', '三江有什么特色',
        '三江好玩', '三江旅游推荐', '三江必玩', '三江必打卡', '三江打卡'
      ]
      return keywords.some(k => text.includes(k))
    },
    isFoodQuery(text) {
      const keywords = [
        '三江美食', '三江吃什么', '侗族美食', '三江餐馆', '油茶', '酸鱼', '酸肉', '侗族糯米饭',
        '特色美食', '美食推荐', '有什么好吃的', '好吃的', '特色菜', '当地美食', '推荐餐厅', '吃什么',
        '美食攻略', '三江特色菜', '三江餐厅', '三江小吃', '三江特产', '侗族特色菜', '三江吃什么好',
        '三江美食推荐', '三江必吃', '三江必吃美食', '三江美食攻略', '三江特色小吃', '三江特产美食',
        '三江有什么美食', '三江有什么特色'
      ]
      return keywords.some(k => text.includes(k))
    },
    searchAttractions(text) {
      return staticData.attractions.filter(a => {
        const name = a.name.toLowerCase()
        const desc = a.description.toLowerCase()
        const tags = a.tags ? a.tags.join(' ').toLowerCase() : ''
        return name.includes(text) || desc.includes(text) || tags.includes(text)
      })
    },

    callAIAPI(text, loadingIdx) {
      const userInfo = uni.getStorageSync('userInfo') || {}
      const userId = userInfo.userId
      const userName = userInfo.nickname || '匿名用户'

      // 使用优化后的历史消息
      const recentHistory = this.optimizeHistory()

      // 暂时禁用缓存，确保每次都调用AI回答
      // const cacheKey = this.getCacheKey(text, recentHistory.length)
      // const cachedResponse = this.getFromCache(cacheKey)
      // if (cachedResponse) {
      //   // 缓存命中，直接返回
      //   const msgObj = {
      //     role: 'ai',
      //     content: cachedResponse.reply,
      //     loading: false,
      //     chatId: null,
      //     rating: null
      //   }
      //   if (cachedResponse.toolCall) {
      //     msgObj.toolResult = true
      //     msgObj.toolType = cachedResponse.toolType
      //     msgObj.toolData = cachedResponse.toolData
      //   }
      //   if (cachedResponse.itinerary) {
      //     msgObj.itinerary = cachedResponse.itinerary
      //   }
      //   this.messages.splice(loadingIdx, 1, msgObj)
      //   this.isLoading = false
      //   this.isRequesting = false
      //   this.scrollToBottom()
      //   return Promise.resolve()
      // }

      // 调用后端 AI API
      return aiApi.chat(text, recentHistory, this.currentSessionId)
        .then(data => {
          // 暂时禁用缓存
          // this.setCache(cacheKey, data)

          const msgObj = {
            role: 'ai',
            content: data.reply,
            loading: false,
            chatId: data.chat_id || null,
            rating: null
          }

          // 处理后端 MCP 工具返回的结果
          if (data.toolCall) {
            msgObj.toolResult = true
            msgObj.toolType = data.toolType
            msgObj.toolData = data.toolData
          }
          // 暂时忽略后端返回的 itinerary，让AI直接回答
          // if (data.itinerary) {
          //   msgObj.itinerary = data.itinerary
          // }

          this.messages.splice(loadingIdx, 1, msgObj)
          // 更新会话 ID（新会话时后端返回）
          if (data.session_id && !this.currentSessionId) {
            this.currentSessionId = data.session_id
            this.loadSessions()
          }
          this.isLoading = false
          this.isRequesting = false
          this.scrollToBottom()
        })
        .catch(error => {
          // API 调用失败时使用本地回复（不抛出错误，避免上层捕获）
          console.log('AI API 调用失败，使用本地回复:', error.message)
          this.messages.splice(loadingIdx, 1, {
            role: 'ai',
            content: this.generateLocalReply(text),
            loading: false
          })
          this.isLoading = false
          this.isRequesting = false
          this.scrollToBottom()
          // 不再抛出错误，让上层认为请求成功完成
        })
    },

    // 保存 MCP 工具结果到后端
    async saveToolResult(msgObj) {
      if (!this.currentSessionId) {
        await this.ensureSession(msgObj.content || '工具查询')
      }
      if (!this.currentSessionId) return
      const token = uni.getStorageSync('token')
      if (!token) return

      const userInfo = uni.getStorageSync('userInfo') || {}
      uni.request({
        url: config.baseURL + '/api/ai-chat/save',
        method: 'POST',
        header: {
          'Content-Type': 'application/json',
          'Authorization': `Bearer ${token}`
        },
        data: {
          session_id: this.currentSessionId,
          userId: userInfo.userId || 0,
          userName: userInfo.nickname || '匿名用户',
          question: 'MCP工具查询',
          answer: msgObj.content || '',
          category: '工具调用',
          response_time: 0
        }
      }).catch(err => {
        console.error('保存工具结果失败:', err)
      })
    },

    // 保存用户消息到后端会话（用于 MCP 工具触发的场景）
    async saveUserMessage(content) {
      if (!this.currentSessionId) {
        await this.ensureSession(content)
      }
      if (!this.currentSessionId) return

      const token = uni.getStorageSync('token')
      if (!token) return

      const userInfo = uni.getStorageSync('userInfo') || {}
      const userId = userInfo.userId
      const userName = userInfo.nickname || '匿名用户'

      uni.request({
        url: config.baseURL + '/api/ai-chat/save',
        method: 'POST',
        header: {
          'Content-Type': 'application/json',
          'Authorization': `Bearer ${token}`
        },
        data: {
          session_id: this.currentSessionId,
          userId: userId,
          userName: userName,
          question: content,
          answer: 'MCP 工具调用',
          category: '工具调用',
          response_time: 0
        }
      }).catch(err => {
        console.error('保存用户消息失败:', err)
      })
    },

    // 确保会话存在，不存在则创建
    async ensureSession(firstMessage) {
      if (this.currentSessionId) return this.currentSessionId
      const token = uni.getStorageSync('token')
      if (!token) return null

      const userInfo = uni.getStorageSync('userInfo') || {}
      const title = firstMessage.substring(0, 20) + (firstMessage.length > 20 ? '...' : '')
      try {
        const res = await aiApi.createSession({
          title,
          userId: userInfo.userId || 0,
          userName: userInfo.nickname || '匿名用户'
        })
        if (res && res.id) {
          this.currentSessionId = res.id
          this.loadSessions()
          return this.currentSessionId
        }
      } catch (err) {
        console.error('创建会话失败:', err)
      }
      return null
    },

    // ===== 会话管理 =====
    loadSessions() {
      const token = uni.getStorageSync('token')
      if (!token) return

      aiApi.getSessions(1, 50)
        .then(res => {
          // 后端返回 { list: [...], total, page, size }
          const list = res.list || res || []
          this.sessions = list.map(s => ({
            id: s.id,
            title: s.title || '新对话',
            lastMessage: s.lastMessage || s.last_message || '',
            messageCount: s.messageCount || s.message_count || 0,
            createTime: s.createTime || s.create_time || '',
            updateTime: s.updateTime || s.update_time || ''
          }))
        })
        .catch(err => {
          console.error('加载会话列表失败:', err)
        })
    },

    selectSession(session) {
      this.currentSessionId = session.id
      this.showSessionList = false
      this.loadSessionMessages(session.id)
    },

    loadSessionMessages(sessionId) {
      this.messages = []
      this.isLoading = true

      aiApi.getSessionMessages(sessionId)
        .then(data => {
          const list = data || data.list || []
          this.messages = list.map(msg => {
            // 后端返回 role: "assistant"，前端统一用 "ai"
            const role = (msg.role === 'assistant') ? 'ai' : msg.role
            const hasTool = !!(msg.toolResult || msg.tool_type || msg.itinerary)
            let toolData = msg.toolResult || null
            let toolType = msg.toolType || msg.tool_type || null
            let itinerary = msg.itinerary || null

            // 尝试解析 JSON 字符串格式的 toolResult 和 itinerary
            if (typeof toolData === 'string') {
              try { toolData = JSON.parse(toolData) } catch (e) {}
            }
            if (typeof itinerary === 'string') {
              try { itinerary = JSON.parse(itinerary) } catch (e) {}
            }

            return {
              role,
              content: msg.content || '',
              toolResult: hasTool,
              toolType,
              toolData,
              itinerary,
              loading: false,
              chatId: msg.id || msg.chatId || null,
              rating: msg.rating || null
            }
          })
          this.isLoading = false
          this.scrollToBottom()
        })
        .catch(err => {
          console.error('加载消息失败:', err)
          this.isLoading = false
        })
    },

    createNewSession() {
      this.currentSessionId = null
      this.messages = []
      this.showSessionList = false
      this.scrollToBottom()
    },

    deleteSession(session) {
      uni.showModal({
        title: '删除会话',
        content: `确定删除「${session.title}」？`,
        success: (res) => {
          if (!res.confirm) return
          const token = uni.getStorageSync('token')
          if (token) {
            aiApi.deleteSession(session.id).then(() => {
              // 如果删除的是当前会话，清空聊天
              if (this.currentSessionId === session.id) {
                this.currentSessionId = null
                this.messages = []
              }
              this.loadSessions()
              uni.showToast({ title: '已删除', icon: 'success' })
            }).catch(err => {
              console.error('删除会话失败:', err)
              uni.showToast({ title: '删除失败', icon: 'none' })
            })
          }
        }
      })
    },

    toggleSessionList() {
      this.showSessionList = !this.showSessionList
      if (this.showSessionList) {
        this.loadSessions()
      }
    },

    formatSessionTime(timeStr) {
      if (!timeStr) return ''
      const date = new Date(timeStr)
      const now = new Date()
      const diff = now - date

      if (diff < 3600000) return Math.floor(diff / 60000) + '分钟前'
      if (diff < 86400000) return Math.floor(diff / 3600000) + '小时前'
      if (diff < 604800000) return Math.floor(diff / 86400000) + '天前'
      return (date.getMonth() + 1) + '/' + date.getDate()
    },

    generateLocalReply(text) {
      const t = text
      // 其他城市/地区问题
      const otherCityMatch = t.match(/([\u4e00-\u9fa5]{2,4})(有什么好玩|怎么玩|旅游|景点|攻略)/)
      if (otherCityMatch && !t.includes('三江') && !t.includes('侗')) {
        const city = otherCityMatch[1]
        return `关于${city}的旅游信息，我作为三江侗族专属导览助手了解有限。\
\
不过如果您想了解**广西三江侗族自治县**的旅游，我可以为您详细介绍：\
\
🏛️ **必游景点**：程阳八寨、布央茶园、丹洲古城、侗族鼓楼\
🍜 **特色美食**：侗族油茶、腌鱼腌肉、糯米饭、三江茶叶\
🎵 **非遗文化**：侗族大歌（UNESCO非遗）、侗锦织造技艺\
🗺️ **推荐行程**：两日游可涵盖主要景点\
\
请问您对三江哪方面感兴趣？😊`
      }
      // 打招呼
      if (/^(你好|您好|hi|hello|嗨|在吗|在不|早上好|下午好|晚上好|晚安|早安|午安)/.test(t)) {
        return '您好！我是侗乡AI游览管家 😊\
\
我专注于广西三江侗族自治县的旅游服务，可以帮您：\
• 推荐景点和美食\
• 规划一日游/两日游/三日游行程\
• 介绍侗族非遗文化\
• 提供购票和交通信息\
• 查询天气和住宿推荐\
\
请问有什么可以帮您？'
      }
      // 感谢
      if (/谢谢|感谢|太好了|不错|很好|棒|谢谢啦|多谢|感谢感谢/.test(t)) {
        return '不客气！希望您在三江玩得开心 😊 如有其他问题随时问我~'
      }
      // 再见
      if (/再见|拜拜|bye|goodbye|下次见|回见|先这样/.test(t)) {
        return '再见！祝您旅途愉快，期待下次为您服务 👋'
      }
      // 天气
      if (/天气|气温|下雨|温度|穿什么|气候|天气预报|今天天气|明天天气|后天天气/.test(t)) {
        return '三江属亚热带季风气候，四季宜人：\
\
🌸 **春季（3-5月）**：15-25°C，雨水较多，建议带雨具\
☀️ **夏季（6-8月）**：25-35°C，适合避暑，山区清凉\
🍂 **秋季（9-11月）**：15-28°C，天高气爽，最佳旅游季节\
❄️ **冬季（12-2月）**：5-15°C，偶有霜冻\
\
出发前建议查看最新天气预报，三江山区变化较快。'
      }
      // 交通
      if (/怎么去|交通|高铁|火车|飞机|自驾|大巴|怎么到|公交|打车|出租车|班车/.test(t)) {
        return '前往三江侗族自治县的交通方式：\
\
🚄 **高铁**：三江南站，可从桂林、柳州乘高铁直达，约1-2小时\
🚌 **大巴**：桂林、柳州均有班车，票价约50-80元\
🚗 **自驾**：从桂林出发约2.5小时，沿G321国道或高速\
✈️ **飞机**：最近机场为桂林两江国际机场，再转高铁或大巴\
\
建议提前在12306或携程购票预订 🎫'
      }
      // 住宿
      if (/住哪|酒店|民宿|住宿|客栈|住什么|住宿推荐|住宿攻略|住宿价格/.test(t)) {
        return '三江推荐住宿区域：\
\
🏨 **三江县城**：交通便利，酒店齐全，价格100-300元/晚\
🏡 **程阳八寨**：推荐侗族特色民宿，体验当地文化，80-200元/晚\
🌿 **布央茶园周边**：生态民宿，空气清新，可体验采茶\
\
旺季（五一、国庆、春节）建议提前一周预订~'
      }
      // 费用/预算
      if (/多少钱|费用|预算|价格|贵不贵|便宜|花费|消费|多少钱|门票价格/.test(t)) {
        return '三江旅游参考费用：\
\
🎫 **景区门票**：程阳八寨约88元，其他景点30-60元不等\
🍜 **餐饮**：人均50-100元，特色农家菜性价比高\
🏨 **住宿**：经济型80-150元，品质酒店200-400元\
🚗 **交通**：当地包车一天约200-300元\
\
💰 两日游人均预算约600-1200元（不含往返交通）'
      }
      // 最佳时间
      if (/什么时候|最佳时间|几月|哪个季节|旅游旺季|什么时候去|什么时间去|最佳旅游/.test(t)) {
        return '三江最佳旅游时间：\
\
🌟 **最佳季节：9月-11月**（秋高气爽，侗族节庆多）\
🌸 **春季3-5月**：茶园新绿，油菜花盛开\
🎆 **节庆推荐**：\
  • 三月三（侗族歌节）\
  • 萨玛节（侗族祭祀节日）\
  • 冬节（12月，侗族大歌表演）\
\
如有特定节假日计划，建议提前15天预订。'
      }
      // 美食推荐
      if (/美食|吃什么|好吃的|特色菜|餐厅|餐馆|小吃|特产|油茶|酸鱼|酸肉|糯米饭/.test(t)) {
        return '三江侗族特色美食推荐：\
\
🍵 **侗族油茶**：三江特色饮品，微苦回甘，提神醒脑\
🐟 **酸鱼酸肉**：侗族传统腌制品，酸香可口，风味独特\
🍚 **侗族糯米饭**：软糯香甜，配腊肉更佳\
🍵 **三江茶叶**：布央仙人山茶园出品，清香怡人\
\
推荐品尝地点：程阳八寨农家乐、县城特色餐馆'
      }
      // 景点推荐
      if (/景点|好玩|旅游|景区|古镇|茶园|鼓楼|风雨桥|程阳|布央|丹洲|月也/.test(t)) {
        return '三江必游景点推荐：\
\
🏛️ **程阳八寨**：世界最长风雨桥，侗族鼓楼文化，国家4A景区\
🍵 **布央仙人山**：万亩茶园，云海观景，生态茶园体验\
🏰 **丹洲古镇**：明代古县城，四面环水，历史文化名镇\
🎵 **月也侗寨**：侗族文化体验村，非遗传承基地\
\
建议游玩时间：2-3天可涵盖主要景点'
      }
      // 非遗文化
      if (/非遗|文化|传统|习俗|历史|侗族大歌|风雨桥|鼓楼|侗锦|蜡染|银饰/.test(t)) {
        return '三江侗族非遗文化介绍：\
\
🎵 **侗族大歌**：UNESCO人类非物质文化遗产，多声部无伴奏合唱\
🌉 **风雨桥建造技艺**：国家级非遗，木结构建筑奇迹\
🏛️ **侗族鼓楼**：侗族标志性建筑，全木结构无钉无铆\
🧵 **侗锦织造**：传统手工织锦，图案精美，色彩鲜艳\
\
推荐体验：侗族大歌表演、侗锦织造体验、风雨桥参观'
      }
      // 行程规划
      if (/行程|规划|路线|一日游|两日游|三日游|怎么玩|怎么安排|旅游计划/.test(t)) {
        return '三江旅游行程推荐：\
\
🗺️ **一日游**：程阳八寨 → 侗族午餐 → 侗族大歌表演 → 返回县城\
🗺️ **两日游**：\
  Day1: 程阳八寨 → 侗族午餐 → 侗族大歌\
  Day2: 布央仙人山 → 茶园午餐 → 丹洲古镇\
🗺️ **三日游**：\
  Day1-2: 同上\
  Day3: 月也侗寨 → 非遗体验馆 → 自由活动\
\
您可以告诉我具体游玩天数，我为您生成详细行程！'
      }
      // 门票信息
      if (/门票|购票|买票|票价|门票价格|门票优惠|门票预订/.test(t)) {
        return '三江景区门票信息：\
\
🎫 **程阳八寨**：88元/人（含风雨桥、鼓楼、村寨）\
🎫 **布央仙人山**：60元/人（含茶园、云海观景台）\
🎫 **丹洲古镇**：免费开放\
🎫 **月也侗寨**：50元/人（含文化体验）\
\
💡 优惠：学生证半价，60岁以上老人免票\
🎟️ 建议提前网上购票，避免排队'
      }
      // 默认通用回复
      return '您好！我是侗乡AI游览管家 😊\
\
我专注于三江侗族自治县旅游服务，可以帮您：\
• 推荐景点和美食\
• 规划旅游行程\
• 介绍非遗文化\
• 提供交通和住宿信息\
• 查询天气和门票价格\
\
请问有什么可以帮您？您可以尝试问：\
• 三江有哪些必去景点？\
• 推荐三江美食\
• 帮我规划三江两日游\
• 三江天气怎么样？'
    },

    goAttractionDetail(item) {
      uni.navigateTo({ url: '/pages/attraction/detail?id=' + item.id })
    },
    goFoodDetail(item) {
      uni.navigateTo({ url: '/pages/food/detail?id=' + item.id })
    },
    goRouteDetail(item) {
      uni.navigateTo({ url: '/pages/route/route?id=' + item.id })
    },
    goToAttractions() {
      uni.navigateTo({ url: '/pages/attraction/list' })
    },
    goToFoods() {
      uni.navigateTo({ url: '/pages/food/food' })
    },
    goToRoutes() {
      uni.navigateTo({ url: '/pages/route/route' })
    },
    goToHeritage() {
      uni.switchTab({ url: '/pages/heritage/heritage' })
    },
    goToTickets() {
      uni.navigateTo({ url: '/pages/ticket/ticket' })
    },
    goToHotels() {
      uni.navigateTo({ url: '/pages/hotel/hotel' })
    },
    clearChat() {
      uni.showModal({
        title: '提示',
        content: this.currentSessionId ? '确定删除当前会话？' : '确定清空聊天记录？',
        success: (res) => {
          if (!res.confirm) return
          if (this.currentSessionId) {
            const token = uni.getStorageSync('token')
            if (token) {
              aiApi.deleteSession(this.currentSessionId).then(() => {
                this.loadSessions()
              }).catch(err => {
                console.error('删除会话失败:', err)
              })
            }
          }
          this.currentSessionId = null
          this.messages = []
        }
      })
    },
    rateMessage(idx, rating) {
      const msg = this.messages[idx]
      if (!msg) return
      
      // 如果有 chatId，提交到后端
      if (msg.chatId) {
        const token = uni.getStorageSync('token')
        uni.request({
          url: config.baseURL + '/api/ai-chat/' + msg.chatId + '/rate',
          method: 'PUT',
          data: { rating },
          header: {
            'Content-Type': 'application/json',
            'Authorization': token ? `Bearer ${token}` : ''
          },
          success: () => {
            msg.rating = rating
            uni.showToast({ title: '感谢反馈', icon: 'success' })
          },
          fail: () => {
            uni.showToast({ title: '评价失败', icon: 'none' })
          }
        })
      } else {
        // 本地 MCP 工具回答，只记录本地评价
        msg.rating = rating
        uni.showToast({ title: '感谢反馈', icon: 'success' })
      }
    },
    saveItinerary(itinerary) {
      // 构建每日行程数据
      const daily = itinerary.days.map(day => ({
        day: day.day,
        date: day.date,
        stops: day.plans.map(plan => ({
          name: plan.title,
          desc: plan.desc || '',
          time: plan.time,
          duration: '',
          transport: plan.traffic || ''
        }))
      }))

      // 收集所有景点名
      const allStops = daily.flatMap(d => d.stops)

      const startDate = new Date().toISOString().split('T')[0]
      const endDateObj = new Date()
      endDateObj.setDate(endDateObj.getDate() + itinerary.days.length - 1)
      const endDate = endDateObj.toISOString().split('T')[0]

      const userInfo = uni.getStorageSync('userInfo') || {}
      const userId = userInfo.userId || userInfo.id

      // 直接保存为"我的路线"最终渲染格式
      const routeData = {
        id: Date.now(),
        userId: userId || 0,
        name: itinerary.title,
        duration: itinerary.days.length + ' 天',
        difficulty: '适中',
        price: 0,
        distance: '-',
        tags: ['AI推荐'],
        isCustom: true,
        stops: allStops,
        cover: '',
        // 额外保存原始数据用于详情页
        daily: daily,
        startDate: startDate,
        endDate: endDate,
        days: itinerary.days.length,
        people: 1,
        transport: 'drive'
      }

      const customRoutes = uni.getStorageSync('customRoutes') || []
      customRoutes.unshift(routeData)
      uni.setStorageSync('customRoutes', customRoutes)
      uni.showModal({
        title: '保存成功',
        content: '已保存到我的路线，是否前往查看？',
        confirmText: '去看看',
        cancelText: '留在这里',
        success: (res) => {
          if (res.confirm) {
            uni.navigateTo({ url: '/pages/route/route' })
          }
        }
      })
    },
    scrollToBottom() {
      // 使用 $nextTick 确保 DOM 更新后再滚动
      this.$nextTick(() => {
        // 计算最后一条消息的 id
        const lastMsgId = 'msg-' + (this.messages.length - 1)
        this.scrollIntoViewId = lastMsgId
        
        // 同时设置 scrollTop 作为双重保险
        this.scrollTop = 999999
        
        // 再次确保滚动到底部
        setTimeout(() => {
          this.scrollTop = 999999
          
          // 第三次确保（应对特殊情况）
          setTimeout(() => {
            this.scrollTop = 999999
          }, 50)
        }, 50)
      })
    }
  }
}
</script>

<style scoped lang="scss">
.ai-page { display: flex; flex-direction: column; height: 100vh; background: #F8F9FA; }
.ai-header {
  background: linear-gradient(135deg, #1D3B5A 0%, #2E5F8E 100%);
  padding: 24rpx 30rpx 40rpx;
  display: flex;
  align-items: center;
  gap: 20rpx;
  border-radius: 0 0 40rpx 40rpx;
  animation: slideDown 0.5s ease-out;
}
@keyframes slideDown {
  from { transform: translateY(-20px); opacity: 0; }
  to { transform: translateY(0); opacity: 1; }
}
.ai-avatar-wrap { width: 84rpx; height: 84rpx; background: rgba(255,255,255,0.2); border-radius: 20rpx; display: flex; align-items: center; justify-content: center; animation: pulse 2s infinite; }
@keyframes pulse {
  0%, 100% { transform: scale(1); }
  50% { transform: scale(1.05); }
}
.ai-avatar-icon { width: 56rpx; height: 56rpx; }
.ai-header-info { flex: 1; }
.ai-name { display: block; font-size: 32rpx; font-weight: bold; color: #FFFFFF; }
.ai-status { display: block; font-size: 22rpx; color: rgba(255,255,255,0.85); margin-top: 6rpx; }
.ai-clear { font-size: 26rpx; color: rgba(255,255,255,0.65); padding: 10rpx 20rpx; border-radius: 20rpx; transition: all 0.3s; }
.ai-clear:active { background: rgba(255,255,255,0.15); }

.ai-sessions-btn {
  font-size: 26rpx;
  color: rgba(255,255,255,0.8);
  padding: 10rpx 20rpx;
  border-radius: 20rpx;
  border: 1rpx solid rgba(255,255,255,0.3);
  transition: all 0.3s;
}
.ai-sessions-btn:active { background: rgba(255,255,255,0.15); }

/* 会话侧边栏 */
.session-drawer {
  position: fixed;
  top: 0; left: 0; right: 0; bottom: 0;
  z-index: 1000;
}
.session-drawer-mask {
  position: absolute;
  top: 0; left: 0; right: 0; bottom: 0;
  background: rgba(0,0,0,0.4);
}
.session-drawer-content {
  position: absolute;
  top: 0; left: 0; bottom: 0;
  width: 520rpx;
  background: #FFFFFF;
  display: flex;
  flex-direction: column;
  animation: slideInLeft 0.3s ease-out;
  box-shadow: 4rpx 0 20rpx rgba(29,59,90,0.10);
}
@keyframes slideInLeft {
  from { transform: translateX(-100%); }
  to { transform: translateX(0); }
}
.session-drawer-header {
  padding: 40rpx 30rpx 20rpx;
  display: flex;
  justify-content: space-between;
  align-items: center;
  border-bottom: 1rpx solid #E8E8E8;
}
.drawer-title {
  font-size: 34rpx;
  font-weight: bold;
  color: #1D3B5A;
}
.drawer-actions {
  display: flex;
  align-items: center;
  gap: 20rpx;
}
.new-chat-btn {
  font-size: 26rpx;
  color: #2E5F8E;
  padding: 8rpx 20rpx;
  border-radius: 20rpx;
  border: 1rpx solid #2E5F8E;
}
.new-chat-btn:active { background: rgba(29,59,90,0.08); }
.drawer-close {
  font-size: 40rpx;
  color: #999;
  padding: 4rpx 12rpx;
}
.session-list {
  flex: 1;
  padding: 10rpx 0;
}
.session-item {
  padding: 24rpx 30rpx;
  border-bottom: 1rpx solid #F1F5F9;
  transition: all 0.3s;
}
.session-item:active { background: #F8F9FA; }
.session-item.active {
  background: rgba(29,59,90,0.06);
  border-left: 6rpx solid #1D3B5A;
}
.session-item-top {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 6rpx;
}
.session-title {
  font-size: 28rpx;
  color: #1D3B5A;
  font-weight: 500;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  flex: 1;
  margin-right: 12rpx;
}
.session-delete {
  font-size: 32rpx;
  color: #B0BEC5;
  padding: 4rpx 12rpx;
  flex-shrink: 0;
}
.session-delete:active { color: #E53935; }
.session-preview {
  display: block;
  font-size: 24rpx;
  color: #999;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  margin-bottom: 6rpx;
  line-height: 1.4;
}
.session-meta {
  font-size: 22rpx;
  color: #B0BEC5;
}
.empty-sessions {
  padding: 100rpx 30rpx;
  display: flex;
  flex-direction: column;
  align-items: center;
}
.empty-sessions-text {
  font-size: 28rpx;
  color: #999;
  margin-bottom: 12rpx;
}
.empty-sessions-sub {
  font-size: 24rpx;
  color: #B0BEC5;
}

.ai-tools-list { display: flex; flex-wrap: wrap; gap: 12rpx; margin: 16rpx 0; }
.tool-item { padding: 8rpx 16rpx; background: rgba(29,59,90,0.1); border-radius: 20rpx; font-size: 24rpx; color: #1D3B5A; transition: all 0.3s; display: flex; align-items: center; gap: 6rpx; }

.icon-img { width: 32rpx; height: 32rpx; flex-shrink: 0; }
.tool-item.clickable { cursor: pointer; }
.tool-item.clickable:active { background: rgba(29,59,90,0.2); transform: scale(0.98); }

/* 欢迎消息内的推荐问题 - 两列卡片网格 */
.quick-questions-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16rpx;
  margin: 20rpx 0;
}

.quick-q-card {
  display: flex;
  align-items: center;
  gap: 12rpx;
  padding: 18rpx 16rpx;
  background: #F8FAFC;
  border-radius: 16rpx;
  border: 1rpx solid rgba(29,59,90,0.08);
  transition: all 0.2s;
  overflow: hidden;
}

.quick-q-card:active {
  background: rgba(29,59,90,0.06);
  transform: scale(0.97);
  border-color: rgba(29,59,90,0.18);
}

.quick-q-icon-wrap {
  width: 56rpx;
  height: 56rpx;
  border-radius: 14rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.quick-q-icon {
  width: 32rpx;
  height: 32rpx;
}

.quick-q-text {
  flex: 1;
  font-size: 24rpx;
  color: #1D3B5A;
  font-weight: 500;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  line-height: 1.4;
}

.quick-q-arrow {
  font-size: 28rpx;
  color: #B0BEC5;
  flex-shrink: 0;
  font-weight: 300;
}

.msg-text-bottom {
  margin-top: 8rpx;
}

.quick-questions { padding: 24rpx; animation: fadeIn 0.5s ease-out; }
@keyframes fadeIn {
  from { opacity: 0; transform: translateY(10px); }
  to { opacity: 1; transform: translateY(0); }
}
.quick-intro-card { background: #FFFFFF; border-radius: 20rpx; padding: 24rpx; box-shadow: 0 4rpx 20rpx rgba(29,59,90,0.08); border-left: 6rpx solid #1D3B5A; }
.quick-intro-text { display: block; font-size: 26rpx; color: #999; margin-bottom: 20rpx; }
.quick-list { display: flex; flex-wrap: wrap; gap: 16rpx; }
.quick-q { background: #F8F9FA; border-radius: 30rpx; padding: 14rpx 24rpx; font-size: 26rpx; color: #1D3B5A; border: 1rpx solid rgba(29,59,90,0.15); transition: all 0.3s; }
.quick-q:active { background: rgba(29,59,90,0.08); transform: scale(0.98); }

.chat-list { flex: 1; overflow: hidden; }
.chat-messages { padding: 20rpx 24rpx; }
.msg-row { display: flex; align-items: flex-start; gap: 16rpx; margin-bottom: 30rpx; animation: messageSlide 0.3s ease-out; }
@keyframes messageSlide {
  from { opacity: 0; transform: translateY(10px); }
  to { opacity: 1; transform: translateY(0); }
}
.msg-user { flex-direction: row-reverse; }
.msg-avatar { width: 72rpx; height: 72rpx; border-radius: 50%; display: flex; align-items: center; justify-content: center; font-size: 28rpx; font-weight: bold; flex-shrink: 0; }
.msg-avatar image { width: 52rpx; height: 52rpx; }
.ai-avatar { background: linear-gradient(135deg, #1D3B5A, #2E5F8E); font-size: 36rpx; }
.user-avatar { background: linear-gradient(135deg, #1D3B5A, #2E5F8E); color: #FFFFFF; }
.msg-bubble { max-width: 75%; padding: 20rpx 24rpx; border-radius: 20rpx; transition: transform 0.2s; }
.msg-bubble:active { transform: scale(0.99); }
.ai-bubble { background: #FFFFFF; border-radius: 4rpx 20rpx 20rpx 20rpx; box-shadow: 0 4rpx 20rpx rgba(29,59,90,0.08); border: 1rpx solid #E8E8E8; }
.user-bubble { background: linear-gradient(135deg, #1D3B5A, #2E5F8E); border-radius: 20rpx 4rpx 20rpx 20rpx; }
.msg-text { font-size: 28rpx; line-height: 1.7; white-space: pre-wrap; }
.ai-bubble .msg-text { color: #333; }
.user-bubble .msg-text { color: #FFFFFF; }

.typing-indicator { display: flex; align-items: center; gap: 8rpx; padding: 8rpx 0; }
.dot { width: 12rpx; height: 12rpx; background: #1D3B5A; border-radius: 50%; animation: bounce 1.2s infinite; }
.dot:nth-child(2) { animation-delay: 0.2s; }
.dot:nth-child(3) { animation-delay: 0.4s; }
@keyframes bounce { 0%, 60%, 100% { transform: translateY(0); } 30% { transform: translateY(-12rpx); } }

// 满意度评价
.rating-bar { margin-top: 16rpx; padding-top: 12rpx; border-top: 1rpx solid rgba(0,0,0,0.06); }
.rating-label { font-size: 22rpx; color: #999; display: block; margin-bottom: 8rpx; }
.rating-btns { display: flex; gap: 16rpx; }
.rating-btn { padding: 8rpx 24rpx; border-radius: 24rpx; font-size: 22rpx; &.like { background: rgba(103,194,58,0.1); color: #67C23A; border: 1rpx solid rgba(103,194,58,0.3); } &.dislike { background: rgba(245,108,108,0.1); color: #F56C6C; border: 1rpx solid rgba(245,108,108,0.3); } }
.rating-done { font-size: 22rpx; color: #999; }

.tool-result { margin-top: 12rpx; }
.attraction-cards { display: flex; flex-direction: column; gap: 16rpx; }
.attraction-card { display: flex; gap: 16rpx; background: #F8F9FA; border-radius: 16rpx; padding: 12rpx; transition: all 0.3s; border: 1rpx solid #E8E8E8; }
.attraction-card:active { transform: scale(0.98); background: #E8F4FD; }
.card-cover { width: 160rpx; height: 120rpx; border-radius: 16rpx; flex-shrink: 0; }
.card-info { flex: 1; display: flex; flex-direction: column; gap: 8rpx; }
.card-name { font-size: 28rpx; font-weight: bold; color: #333; }
.card-meta { display: flex; gap: 16rpx; }
.card-rating { font-size: 24rpx; color: #2E5F8E; }
.card-price { font-size: 24rpx; color: #2E5F8E; font-weight: bold; }
.card-desc { font-size: 22rpx; color: #999; line-height: 1.5; }

.food-cards { display: flex; flex-direction: column; gap: 16rpx; }
.food-card { display: flex; gap: 16rpx; background: #F8F9FA; border-radius: 16rpx; padding: 12rpx; transition: all 0.3s; border: 1rpx solid #E8E8E8; }
.food-card:active { transform: scale(0.98); background: #E8F4FD; }
.food-cover { width: 140rpx; height: 100rpx; border-radius: 16rpx; flex-shrink: 0; }
.food-info { flex: 1; display: flex; flex-direction: column; gap: 8rpx; }
.food-name { font-size: 28rpx; font-weight: bold; color: #333; }
.food-meta { display: flex; gap: 16rpx; }
.food-rating { font-size: 24rpx; color: #2E5F8E; }
.food-price { font-size: 24rpx; color: #2E5F8E; font-weight: bold; }
.food-desc { font-size: 22rpx; color: #999; line-height: 1.5; }

.route-cards { display: flex; flex-direction: column; gap: 16rpx; }
.route-card { background: #F8F9FA; border-radius: 16rpx; overflow: hidden; transition: all 0.3s; border: 1rpx solid #E8E8E8; }
.route-card:active { transform: scale(0.98); }
.route-header { padding: 20rpx; }
.route-name { display: block; font-size: 30rpx; font-weight: bold; color: #fff; margin-bottom: 10rpx; }
.route-tags { display: flex; gap: 10rpx; }
.route-tag { font-size: 22rpx; color: rgba(255,255,255,0.9); background: rgba(255,255,255,0.2); padding: 4rpx 14rpx; border-radius: 20rpx; }
.route-content { padding: 16rpx 20rpx; }
.route-stops { display: flex; align-items: center; margin-bottom: 16rpx; }
.stop-dot { display: flex; align-items: center; position: relative; }
.stop-dot-circle { width: 16rpx; height: 16rpx; border-radius: 50%; background: #1D3B5A; }
.stop-dot-line { width: 60rpx; height: 2rpx; background: #E8E8E8; }
.route-info-row { display: flex; gap: 30rpx; }
.info-item { font-size: 24rpx; color: #666; display: flex; align-items: center; gap: 4rpx; }

/* 天气样式 */

/* 行程规划样式（MCP 核心） */
.itinerary-result { margin-top: 20rpx; }
.itinerary-title { font-size: 28rpx; font-weight: bold; color: #1D3B5A; margin-bottom: 16rpx; }
.day-box { margin-bottom: 20rpx; background: #F8F9FA; border-radius: 16rpx; padding: 20rpx; border: 1rpx solid #E8E8E8; }
.day-box:active { background: #E8F4FD; }
.day-title { font-size: 26rpx; font-weight: bold; color: #2E5F8E; margin-bottom: 12rpx; padding-bottom: 8rpx; border-bottom: 1rpx solid #E8E8E8; }
.day-plans { display: flex; flex-direction: column; gap: 16rpx; }
.plan-item { display: flex; align-items: flex-start; gap: 12rpx; }
.plan-dot { width: 12rpx; height: 12rpx; border-radius: 50%; background: #2E5F8E; flex-shrink: 0; margin-top: 8rpx; }
.plan-body { flex: 1; min-width: 0; }
.plan-header { display: flex; align-items: center; gap: 10rpx; margin-bottom: 4rpx; }
.plan-time { font-size: 22rpx; color: #999; flex-shrink: 0; background: #E8F4FD; padding: 2rpx 8rpx; border-radius: 6rpx; }
.plan-title { font-size: 26rpx; font-weight: bold; color: #333; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.plan-desc { font-size: 22rpx; color: #888; display: block; margin-bottom: 2rpx; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.plan-traffic { font-size: 20rpx; color: #1D3B5A; display: flex; align-items: center; gap: 4rpx; }
.traffic-icon-img { width: 20rpx; height: 20rpx; flex-shrink: 0; }

.action-buttons { margin-top: 16rpx; }
.action-btn {
  padding: 16rpx 24rpx;
  background: linear-gradient(135deg, #1D3B5A, #2E5F8E);
  color: #FFFFFF;
  border-radius: 30rpx;
  text-align: center;
  font-size: 26rpx;
  transition: all 0.3s;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8rpx;
}
.action-btn:active { transform: scale(0.98); opacity: 0.9; }

.save-itinerary-btn {
  margin-top: 16rpx;
  background: linear-gradient(135deg, #1D3B5A, #2E5F8E);
  border-radius: 30rpx;
  padding: 16rpx;
  text-align: center;
}
.save-btn-text {
  font-size: 26rpx;
  color: #FFFFFF;
  font-weight: bold;
}

/* 快捷功能按钮 */
.quick-actions {
  background: #FFFFFF;
  border-top: 1rpx solid #E8E8E8;
  padding: 16rpx 24rpx;
  display: flex;
  justify-content: space-around;
  align-items: center;
}

.quick-action-btn {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 6rpx;
  padding: 8rpx 12rpx;
  transition: all 0.3s;
}

.quick-action-btn:active {
  transform: scale(0.95);
}

.action-icon {
  width: 40rpx;
  height: 40rpx;
}

.action-text {
  font-size: 20rpx;
  color: #1D3B5A;
  font-weight: 500;
}

.input-area { background: #FFFFFF; padding: 16rpx 24rpx 40rpx; border-top: 1rpx solid #E8E8E8; }
.input-wrap { display: flex; align-items: center; gap: 16rpx; background: #F8F9FA; border-radius: 50rpx; padding: 12rpx 12rpx 12rpx 24rpx; border: 1rpx solid rgba(29,59,90,0.1); }
.mic-icon { padding: 8rpx; border-radius: 50%; transition: all 0.3s; display: flex; align-items: center; justify-content: center; }
.mic-icon:active { background: rgba(29,59,90,0.1); }
.chat-input { flex: 1; font-size: 28rpx; color: #333; }
.send-btn { width: 72rpx; height: 72rpx; border-radius: 50%; background: #ccc; display: flex; align-items: center; justify-content: center; transition: all 0.3s; }
.send-btn.active { background: linear-gradient(135deg, #1D3B5A, #2E5F8E); }
.send-btn:active { transform: scale(0.95); }
.send-icon { font-size: 30rpx; color: #FFFFFF; }
</style>