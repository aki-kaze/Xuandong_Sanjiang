<template>
  <el-container class="layout-container" :class="{ 'dark-theme': isDark }">
    <!-- ========== 左侧布局模式 ========== -->
    <template v-if="currentLayout === 'leftMode'">
      <!-- 侧边栏 -->
      <el-aside :width="isCollapsed ? '64px' : '240px'" class="layout-aside">
        <!-- 品牌区 - 渐变背景 -->
        <div class="sidebar-header" :class="{ collapsed: isCollapsed }">
          <div class="brand" v-if="!isCollapsed">
            <div class="brand-icon-wrap">
              <span class="brand-icon">侗</span>
              <div class="brand-icon-glow"></div>
            </div>
            <div class="brand-text">
              <div class="brand-name">寻侗三江</div>
              <div class="brand-sub">管理平台 v2.0</div>
            </div>
          </div>
          <div class="brand brand-collapsed" v-else>
            <div class="brand-icon-wrap">
              <span class="brand-icon">侗</span>
            </div>
          </div>
        </div>

        <!-- 菜单区 -->
        <el-scrollbar class="menu-scrollbar">
          <el-menu
            :default-active="activeRoute"
            :collapse="isCollapsed"
            router
            class="sidebar-menu"
            :background-color="'transparent'"
            :text-color="isDark ? '#b0b0b0' : '#5a6a7a'"
            :active-text-color="'#A33B32'"
            :default-openeds="orderSubMenuOpen"
          >
            <template v-for="(item, index) in menuItems" :key="item.path || item.title">
              <el-sub-menu v-if="item.children" :index="item.title" class="menu-group">
                <template #title>
                  <el-icon><component :is="item.icon" /></el-icon>
                  <span>{{ item.title }}</span>
                </template>
                <el-menu-item
                  v-for="child in item.children"
                  :key="child.path"
                  :index="child.path"
                  class="menu-item sub-item"
                >
                  <el-icon><component :is="child.icon" /></el-icon>
                  <template #title>{{ child.title }}</template>
                </el-menu-item>
              </el-sub-menu>
              <el-menu-item
                v-else
                :index="item.path"
                class="menu-item"
              >
                <el-icon><component :is="item.icon" /></el-icon>
                <template #title>{{ item.title }}</template>
              </el-menu-item>
            </template>
          </el-menu>
        </el-scrollbar>

        <!-- 侧边栏底部 - 时间/天气小部件 -->
        <div class="sidebar-footer">
          <div class="sidebar-widget" v-if="!isCollapsed">
            <div class="widget-time">{{ currentTime }}</div>
            <div class="widget-date">{{ currentDate }}</div>
          </div>
        </div>
      </el-aside>

      <!-- 主内容区 -->
      <el-container class="layout-main">
        <!-- 顶部导航 -->
        <el-header class="layout-header">
          <div class="header-left">
            <el-icon class="collapse-btn" @click="isCollapsed = !isCollapsed">
              <Fold v-if="!isCollapsed" />
              <Expand v-else />
            </el-icon>
            <el-breadcrumb v-if="pageConfigStore.config.showBreadcrumb" separator="/">
              <el-breadcrumb-item>首页</el-breadcrumb-item>
              <el-breadcrumb-item>{{ currentTitle }}</el-breadcrumb-item>
            </el-breadcrumb>
          </div>
          <div class="header-right">
            <el-badge :value="totalPendingCount" :hidden="totalPendingCount === 0" class="badge-item">
              <el-tooltip content="查看待审核内容" placement="bottom">
                <el-dropdown trigger="click" @command="(cmd) => cmd === 'post' ? $router.push('/moments') : $router.push('/guides')">
                  <el-icon class="header-icon bell-icon">
                    <Bell />
                  </el-icon>
                  <template #dropdown>
                    <el-dropdown-menu>
                      <el-dropdown-item v-if="pendingPostCount > 0" command="post">
                        <span>待审核动态</span>
                        <span style="margin-left: 10px; color: #f56c6c; font-weight: bold;">{{ pendingPostCount }}</span>
                      </el-dropdown-item>
                      <el-dropdown-item v-if="pendingGuideCount > 0" command="guide">
                        <span>待审核攻略</span>
                        <span style="margin-left: 10px; color: #f56c6c; font-weight: bold;">{{ pendingGuideCount }}</span>
                      </el-dropdown-item>
                      <el-dropdown-item v-if="totalPendingCount === 0" disabled>暂无待审核内容</el-dropdown-item>
                    </el-dropdown-menu>
                  </template>
                </el-dropdown>
              </el-tooltip>
            </el-badge>

            <el-tooltip content="页面设置" placement="bottom">
              <el-icon class="header-icon" @click="pageConfigStore.openSettings()">
                <Setting />
              </el-icon>
            </el-tooltip>

            <el-dropdown @command="handleCommand">
              <span class="user-info">
                <el-avatar :size="36" :src="adminInfo.avatar" class="avatar" @click="handleAvatarClick">
                  {{ (adminInfo.nickname || adminInfo.name)?.charAt(0) || 'A' }}
                </el-avatar>
                <span class="username">{{ adminInfo.nickname || adminInfo.name || '管理员' }}</span>
                <el-icon><ArrowDown /></el-icon>
              </span>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="avatar" icon="UserFilled">更换头像</el-dropdown-item>
                  <el-dropdown-item command="logout" icon="SwitchButton">退出登录</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </div>
        </el-header>

        <!-- 标签页 -->
        <TabsView v-if="pageConfigStore.config.showTabs" />

        <!-- 内容区 -->
        <el-main class="layout-content">
          <template v-if="loading">
            <el-skeleton :rows="5" animated></el-skeleton>
          </template>
          <template v-else>
            <transition :name="pageConfigStore.config.showPageTransition ? 'fade' : ''" mode="out-in">
              <router-view />
            </transition>
          </template>
        </el-main>
      </el-container>
    </template>

    <!-- ========== 顶部布局模式 ========== -->
    <template v-else>
      <el-container class="top-layout-container">
        <!-- 顶部导航栏 -->
        <el-header class="top-header">
          <div class="top-header-left">
            <!-- 品牌 Logo -->
            <div class="brand top-brand">
              <div class="brand-icon-wrap small">
                <span class="brand-icon">侗</span>
              </div>
              <div class="brand-text">
                <div class="brand-name">寻侗三江</div>
                <div class="brand-sub">管理平台</div>
              </div>
            </div>

            <!-- 顶部主菜单 -->
            <div class="top-menu-wrapper">
              <el-menu
                :default-active="activeRoute"
                mode="horizontal"
                router
                class="top-menu"
                :background-color="'transparent'"
                :text-color="isDark ? '#b0b0b0' : '#5a6a7a'"
                :active-text-color="'#A33B32'"
              >
                <el-menu-item
                  v-for="item in topMenuItems"
                  :key="item.path"
                  :index="item.path"
                >
                  <el-icon><component :is="item.icon" /></el-icon>
                  <span>{{ item.title }}</span>
                </el-menu-item>
              </el-menu>
            </div>
          </div>

          <!-- 右侧功能区 -->
          <div class="top-header-right">
            <el-tooltip :content="isDark ? '切换为浅色模式' : '切换为深色模式'" placement="bottom">
              <el-icon class="theme-btn" @click="toggleTheme">
                <component :is="isDark ? 'Sunny' : 'Moon'" />
              </el-icon>
            </el-tooltip>

            <el-badge :value="totalPendingCount" :hidden="totalPendingCount === 0" class="badge-item">
              <el-tooltip content="查看待审核内容" placement="bottom">
                <el-dropdown trigger="click" @command="(cmd) => cmd === 'post' ? $router.push('/moments') : $router.push('/guides')">
                  <el-icon class="header-icon">
                    <Bell />
                  </el-icon>
                  <template #dropdown>
                    <el-dropdown-menu>
                      <el-dropdown-item v-if="pendingPostCount > 0" command="post">
                        <span>待审核动态</span>
                        <span style="margin-left: 10px; color: #f56c6c; font-weight: bold;">{{ pendingPostCount }}</span>
                      </el-dropdown-item>
                      <el-dropdown-item v-if="pendingGuideCount > 0" command="guide">
                        <span>待审核攻略</span>
                        <span style="margin-left: 10px; color: #f56c6c; font-weight: bold;">{{ pendingGuideCount }}</span>
                      </el-dropdown-item>
                      <el-dropdown-item v-if="totalPendingCount === 0" disabled>暂无待审核内容</el-dropdown-item>
                    </el-dropdown-menu>
                  </template>
                </el-dropdown>
              </el-tooltip>
            </el-badge>

            <el-tooltip content="页面设置" placement="bottom">
              <el-icon class="header-icon" @click="pageConfigStore.openSettings()">
                <Setting />
              </el-icon>
            </el-tooltip>

            <el-dropdown @command="handleCommand">
              <span class="user-info">
                <el-avatar :size="36" :src="adminInfo.avatar" class="avatar">
                  {{ (adminInfo.nickname || adminInfo.name)?.charAt(0) || 'A' }}
                </el-avatar>
                <span class="username">{{ adminInfo.nickname || adminInfo.name || '管理员' }}</span>
                <el-icon><ArrowDown /></el-icon>
              </span>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="avatar" icon="UserFilled">更换头像</el-dropdown-item>
                  <el-dropdown-item command="logout" icon="SwitchButton">退出登录</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </div>
        </el-header>

        <!-- 标签页 -->
        <TabsView v-if="pageConfigStore.config.showTabs" />

        <!-- 内容区 -->
        <el-main class="top-layout-content">
          <template v-if="loading">
            <el-skeleton :rows="5" animated></el-skeleton>
          </template>
          <template v-else>
            <transition :name="pageConfigStore.config.showPageTransition ? 'fade' : ''" mode="out-in">
              <router-view />
            </transition>
          </template>
        </el-main>
      </el-container>
    </template>

    <!-- 设置面板 -->
    <SettingsPanel />

    <!-- 头像上传弹窗 -->
    <el-dialog
      v-model="avatarDialogVisible"
      title="更换头像"
      width="560px"
      :close-on-click-modal="false"
      class="avatar-change-dialog"
    >
      <div class="avatar-dialog-content">
        <!-- 当前头像预览 -->
        <div class="current-avatar-section">
          <div class="avatar-preview-wrapper">
            <el-avatar :size="100" :src="newAvatar || adminInfo.avatar" class="avatar-preview">
              {{ (adminInfo.nickname || adminInfo.name)?.charAt(0) || 'A' }}
            </el-avatar>
          </div>
          <p class="preview-label">预览效果</p>
        </div>

        <!-- 选项卡切换 -->
        <el-tabs v-model="avatarTab" class="avatar-tabs">
          <!-- URL 输入 -->
          <el-tab-pane label="自定义 URL" name="url">
            <div class="avatar-url-input">
              <el-input
                v-model="newAvatar"
                placeholder="请输入头像图片 URL"
                clearable
                @input="handleUrlInput"
              >
                <template #prefix>
                  <el-icon><Link /></el-icon>
                </template>
              </el-input>
              <p class="input-tip">支持 JPG、PNG、GIF 格式的图片链接</p>
            </div>
          </el-tab-pane>

          <!-- 本地上传 -->
          <el-tab-pane label="本地上传" name="upload">
            <div class="avatar-upload">
              <el-upload
                ref="uploadRef"
                class="upload-wrapper"
                drag
                :auto-upload="false"
                :on-change="handleFileChange"
                :on-remove="handleFileRemove"
                :limit="1"
                accept="image/*"
              >
                <el-icon class="el-icon--upload"><UploadFilled /></el-icon>
                <div class="el-upload__text">
                  将文件拖到此处，或<em>点击上传</em>
                </div>
                <template #tip>
                  <div class="el-upload__tip">
                    支持 jpg/png/gif 文件，大小不超过 2MB
                  </div>
                </template>
              </el-upload>
              <div v-if="uploadPreview" class="upload-preview">
                <el-avatar :size="80" :src="uploadPreview" class="preview-img">
                  {{ getAvatarInitial(uploadPreview) }}
                </el-avatar>
                <p>已选择</p>
              </div>
            </div>
          </el-tab-pane>
        </el-tabs>
      </div>

      <template #footer>
        <span class="dialog-footer">
          <div class="button-row">
            <el-button @click="avatarDialogVisible = false">取消</el-button>
            <el-button @click="resetAvatar">恢复默认</el-button>
            <el-button type="primary" @click="handleAvatarSave" :loading="saving">保存</el-button>
          </div>
        </span>
      </template>
    </el-dialog>
  </el-container>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessageBox, ElMessage, ElNotification } from 'element-plus'
import { useAuthStore } from '@/stores/auth'
import { usePageConfigStore } from '@/stores/pageConfig'
import { momentApi, guideApi } from '@/api'
import { Fold, Expand, Sunny, Moon, Bell, ArrowDown, Setting, Link, UploadFilled } from '@element-plus/icons-vue'
import TabsView from '@/components/TabsView.vue'
import SettingsPanel from '@/components/SettingsPanel.vue'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()
const pageConfigStore = usePageConfigStore()
const isCollapsed = ref(false)
const pendingPostCount = ref(0)
const pendingGuideCount = ref(0)
const isDark = ref(false)
const loading = ref(false)
const avatarDialogVisible = ref(false)
const notificationDropdownVisible = ref(false)

// 时间显示
const currentTime = ref('')
const currentDate = ref('')
let timeTimer = null
let refreshTimer = null

function updateTime() {
  const now = new Date()
  currentTime.value = now.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit', second: '2-digit' })
  currentDate.value = now.toLocaleDateString('zh-CN', { month: 'long', day: 'numeric', weekday: 'long' })
}

const currentLayout = computed(() => pageConfigStore.config.layout || 'leftMode')

const adminInfo = computed(() => authStore.adminInfo)
const totalPendingCount = computed(() => pendingPostCount.value + pendingGuideCount.value)
const menuItems = [
  { path: '/dashboard', title: '数据概览', icon: 'DataAnalysis' },
  { path: '/attractions', title: '景点管理', icon: 'Location' },
  { path: '/foods', title: '美食管理', icon: 'Mug' },
  { path: '/products', title: '商品管理', icon: 'Goods' },
  { path: '/hotels', title: '酒店管理', icon: 'House' },
  { path: '/routes', title: '路线与行程', icon: 'Guide' },
  { path: '/guides', title: '攻略管理', icon: 'Notebook' },
  {
    title: '订单管理',
    icon: 'List',
    children: [
      { path: '/order-analysis', title: '订单总览', icon: 'DataAnalysis' },
      { path: '/ticket-orders', title: '门票订单', icon: 'Tickets' },
      { path: '/food-orders', title: '美食订单', icon: 'Mug' },
      { path: '/hotel-orders', title: '酒店订单', icon: 'House' },
      { path: '/product-orders', title: '商品订单', icon: 'ShoppingCart' },
      { path: '/experience-orders', title: '非遗体验订单', icon: 'Star' }
    ]
  },
  { path: '/users', title: '用户管理', icon: 'User' },
  { path: '/moments', title: '社区管理', icon: 'ChatDotRound' },
  { path: '/heritage', title: '非遗管理', icon: 'Star' },
  { path: '/activities', title: '活动管理', icon: 'Calendar' },
  { path: '/ai-knowledge', title: 'AI 知识库', icon: 'ChatDotSquare' },
  { path: '/sensitive-words', title: '敏感词管理', icon: 'Warning' }
]

const topMenuItems = [
  { path: '/dashboard', title: '数据概览', icon: 'DataAnalysis' },
  { path: '/attractions', title: '景点管理', icon: 'Location' },
  { path: '/foods', title: '美食管理', icon: 'Mug' },
  { path: '/products', title: '商品管理', icon: 'Goods' },
  { path: '/hotels', title: '酒店管理', icon: 'House' },
  { path: '/order-analysis', title: '订单管理', icon: 'List' },
  { path: '/users', title: '用户管理', icon: 'User' }
]

const orderPaths = ['/order-analysis', '/ticket-orders', '/food-orders', '/hotel-orders', '/product-orders', '/experience-orders']
const activeRoute = computed(() => '/' + route.path.split('/')[1])
const orderSubMenuOpen = computed(() =>
  orderPaths.includes(activeRoute.value) ? ['订单管理'] : []
)
const currentTitle = computed(() => {
  const path = activeRoute.value
  for (const item of menuItems) {
    if (item.path === path) return item.title
    if (item.children) {
      const child = item.children.find(c => c.path === path)
      if (child) return child.title
    }
  }
  return ''
})

async function handleCommand(cmd) {
  if (cmd === 'logout') {
    await ElMessageBox.confirm('确认退出登录？', '提示', { type: 'warning' })
    authStore.logout()
    router.push('/login')
  } else if (cmd === 'avatar') {
    openAvatarDialog()
  }
}

function handleAvatarClick() {
  openAvatarDialog()
}

// ========== 头像相关功能 ==========
const newAvatar = ref('')
const avatarTab = ref('url')
const uploadRef = ref(null)
const uploadPreview = ref('')
const saving = ref(false)

// 默认头像
const defaultAvatar = 'https://cdn.phototourl.com/free/2026-03-24-f37e50c9-e052-4485-8e58-128c75c91fc4.jpg'

// 打开头像对话框
function openAvatarDialog() {
  newAvatar.value = adminInfo.value.avatar || ''
  avatarTab.value = 'url'
  uploadPreview.value = ''
  uploadRef.value?.clearFiles()
  avatarDialogVisible.value = true
}

// 获取头像首字母
function getAvatarInitial(avatar) {
  if (!avatar) return 'A'
  return (adminInfo.value.nickname || adminInfo.value.name)?.charAt(0) || 'A'
}

// URL 输入处理
function handleUrlInput() {
  avatarTab.value = 'url'
}

// 文件改变处理
function handleFileChange(file) {
  if (file && file.raw) {
    // 检查文件大小
    if (file.raw.size > 2 * 1024 * 1024) {
      ElMessage.error('图片大小不能超过 2MB')
      uploadRef.value?.clearFiles()
      return
    }
    // 检查文件类型
    const validTypes = ['image/jpeg', 'image/png', 'image/gif']
    if (!validTypes.includes(file.raw.type)) {
      ElMessage.error('只支持 JPG、PNG、GIF 格式图片')
      uploadRef.value?.clearFiles()
      return
    }
    // 生成预览
    const reader = new FileReader()
    reader.onload = (e) => {
      uploadPreview.value = e.target.result
      newAvatar.value = e.target.result
    }
    reader.readAsDataURL(file.raw)
  }
}

// 移除文件
function handleFileRemove() {
  uploadPreview.value = ''
  newAvatar.value = adminInfo.value.avatar || ''
}

// 重置头像
function resetAvatar() {
  newAvatar.value = defaultAvatar
  ElMessage.success('已恢复默认头像')
}

// 保存头像
async function handleAvatarSave() {
  if (!newAvatar.value) {
    ElMessage.warning('请选择或输入头像地址')
    return
  }

  saving.value = true
  try {
    authStore.setAvatar(newAvatar.value)
    ElMessage.success('头像更新成功')
    avatarDialogVisible.value = false
  } catch (error) {
    console.error('更新头像失败:', error)
    ElMessage.error('更新头像失败')
  } finally {
    saving.value = false
  }
}

function toggleTheme() {
  pageConfigStore.toggleTheme()
  isDark.value = pageConfigStore.config.theme === 'dark'
}

watch(
  () => route.path,
  () => {
    loading.value = true
    setTimeout(() => {
      loading.value = false
    }, 300)
  }
)

async function loadPendingCounts() {
  try {
    const [momentRes, guideRes] = await Promise.all([
      momentApi.countPending(),
      guideApi.countPending()
    ])
    const newPostCount = momentRes.data || 0
    const newGuideCount = guideRes.data || 0
    
    // 如果数量增加，显示通知
    if (newPostCount > pendingPostCount.value || newGuideCount > pendingGuideCount.value) {
      if (newPostCount > pendingPostCount.value) {
        ElNotification({
          title: '新的动态待审核',
          message: `有 ${newPostCount - pendingPostCount.value} 条新动态需要审核`,
          type: 'warning',
          duration: 5000,
          position: 'top-right',
          onClick: () => router.push('/moments')
        })
      }
      if (newGuideCount > pendingGuideCount.value) {
        ElNotification({
          title: '新的攻略待审核',
          message: `有 ${newGuideCount - pendingGuideCount.value} 篇新攻略需要审核`,
          type: 'warning',
          duration: 5000,
          position: 'top-right',
          onClick: () => router.push('/guides')
        })
      }
    }
    
    pendingPostCount.value = newPostCount
    pendingGuideCount.value = newGuideCount
  } catch (err) {
    console.error('加载待审核数量失败:', err)
  }
}

onMounted(async () => {
  isDark.value = pageConfigStore.config.theme === 'dark'
  updateTime()
  timeTimer = setInterval(updateTime, 1000)
  await loadPendingCounts()
  // 每 30 秒刷新一次待审核数量
  refreshTimer = setInterval(() => {
    loadPendingCounts()
  }, 30000)
})

onUnmounted(() => {
  if (timeTimer) {
    clearInterval(timeTimer)
    timeTimer = null
  }
  if (refreshTimer) {
    clearInterval(refreshTimer)
    refreshTimer = null
  }
})
</script>

<style scoped lang="scss">
@use '@/styles/responsive.scss' as *;

.layout-container {
  height: 100vh;
  overflow: hidden;
  transition: background-color 0.3s, color 0.3s;
}

.layout-container.dark-theme {
  background-color: #121212;
  color: #e0e0e0;
}

// =================== 侧边栏 ===================
.layout-aside {
  background: linear-gradient(180deg, #ffffff 0%, #faf8f5 100%);
  transition: width 0.3s cubic-bezier(0.4, 0, 0.2, 1), background-color 0.3s;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  box-shadow: 2px 0 16px rgba(29, 59, 90, 0.08);
  border-right: 1px solid rgba(232, 228, 220, 0.6);
}

.layout-container.dark-theme .layout-aside {
  background: linear-gradient(180deg, #1a1a1a 0%, #141414 100%);
  box-shadow: 2px 0 12px rgba(0, 0, 0, 0.3);
  border-right-color: rgba(255, 255, 255, 0.05);
}

// =================== 品牌区 ===================
.sidebar-header {
  height: 68px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  background: linear-gradient(135deg, rgba(163, 59, 50, 0.06) 0%, rgba(29, 59, 90, 0.04) 100%);
  border-bottom: 1px solid rgba(232, 228, 220, 0.5);
  transition: all 0.3s;

  &.collapsed {
    padding: 0;
  }
}

.layout-container.dark-theme .sidebar-header {
  background: linear-gradient(135deg, rgba(163, 59, 50, 0.12) 0%, rgba(29, 59, 90, 0.08) 100%);
  border-bottom-color: rgba(255, 255, 255, 0.05);
}

.brand {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 0 12px;
  transition: all 0.3s;
  cursor: pointer;

  &:hover {
    transform: translateX(3px);
  }

  &.brand-collapsed {
    justify-content: center;
    padding: 0;
  }
}

.brand-icon-wrap {
  position: relative;
  width: 38px;
  height: 38px;
  flex-shrink: 0;

  &.small {
    width: 32px;
    height: 32px;

    .brand-icon {
      width: 32px;
      height: 32px;
      font-size: 15px;
      border-radius: 8px;
    }
  }
}

.brand-icon {
  width: 38px;
  height: 38px;
  border-radius: 10px;
  background: linear-gradient(135deg, #A33B32 0%, #c0504a 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 18px;
  font-weight: 800;
  position: relative;
  z-index: 1;
  box-shadow: 0 3px 12px rgba(163, 59, 50, 0.3);
  transition: all 0.3s;
}

.brand-icon-glow {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  width: 52px;
  height: 52px;
  background: radial-gradient(circle, rgba(163, 59, 50, 0.2), transparent 70%);
  border-radius: 50%;
  animation: iconGlow 3s ease-in-out infinite;
}

@keyframes iconGlow {
  0%, 100% { transform: translate(-50%, -50%) scale(1); opacity: 0.5; }
  50% { transform: translate(-50%, -50%) scale(1.2); opacity: 0.8; }
}

.brand:hover .brand-icon {
  transform: scale(1.05) rotate(-3deg);
  box-shadow: 0 5px 16px rgba(163, 59, 50, 0.4);
}

.brand-name {
  color: #1D3B5A;
  font-size: 16px;
  font-weight: 800;
  white-space: nowrap;
  letter-spacing: 0.5px;
}

.layout-container.dark-theme .brand-name {
  color: #e0e0e0;
}

.brand-sub {
  color: #aaa;
  font-size: 11px;
  white-space: nowrap;
  letter-spacing: 1px;
}

// =================== 菜单样式 ===================
.menu-scrollbar {
  flex: 1;
  overflow: hidden;
}

.sidebar-menu {
  border-right: none !important;
  padding: 8px 0;
}

.menu-item-inner {
  display: flex;
  align-items: center;
  gap: 10px;
  transition: all 0.3s;
  animation: slideInLeft 0.4s ease-out backwards;
}

@keyframes slideInLeft {
  from {
    opacity: 0;
    transform: translateX(-12px);
  }
  to {
    opacity: 1;
    transform: translateX(0);
  }
}

.sub-inner {
  padding-left: 8px;
}

:deep(.el-menu-item),
:deep(.el-sub-menu__title) {
  height: 46px !important;
  line-height: 46px !important;
  margin: 2px 8px;
  border-radius: 10px !important;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1) !important;
  border: 1px solid transparent !important;
}

:deep(.el-menu-item.is-active) {
  background: linear-gradient(135deg, rgba(163, 59, 50, 0.12), rgba(163, 59, 50, 0.06)) !important;
  color: #A33B32 !important;
  border-color: rgba(163, 59, 50, 0.15) !important;
  font-weight: 600;

  &::before {
    content: '';
    position: absolute;
    left: 0;
    top: 50%;
    transform: translateY(-50%);
    width: 3px;
    height: 20px;
    background: linear-gradient(180deg, #A33B32, #c0504a);
    border-radius: 0 3px 3px 0;
  }
}

:deep(.el-menu-item:hover) {
  background: rgba(29, 59, 90, 0.06) !important;
  color: #1D3B5A !important;
  transform: translateX(2px);
}

:deep(.el-sub-menu__title:hover) {
  background: rgba(29, 59, 90, 0.06) !important;
  color: #1D3B5A !important;
}

:deep(.el-menu--collapse) {
  width: 64px;

  .menu-item-inner {
    justify-content: center;
    padding: 0;
  }
}

// =================== 侧边栏底部 ===================
.sidebar-footer {
  flex-shrink: 0;
  border-top: 1px solid rgba(232, 228, 220, 0.5);
  transition: border-top-color 0.3s;
}

.layout-container.dark-theme .sidebar-footer {
  border-top-color: rgba(255, 255, 255, 0.05);
}

.sidebar-widget {
  padding: 12px 16px;
  text-align: center;
  border-bottom: 1px solid rgba(232, 228, 220, 0.3);
}

.layout-container.dark-theme .sidebar-widget {
  border-bottom-color: rgba(255, 255, 255, 0.03);
}

.widget-time {
  font-size: 18px;
  font-weight: 700;
  color: #1D3B5A;
  font-variant-numeric: tabular-nums;
  letter-spacing: 1px;
}

.layout-container.dark-theme .widget-time {
  color: #e0e0e0;
}

.widget-date {
  font-size: 11px;
  color: #aaa;
  margin-top: 2px;
}

.sidebar-actions {
  height: 48px;
  display: flex;
  align-items: center;
  justify-content: space-around;
}

.theme-btn,
.collapse-btn {
  color: #5a6a7a;
  font-size: 20px;
  cursor: pointer;
  transition: all 0.3s;
  padding: 8px;
  border-radius: 8px;
}

.theme-btn:hover,
.collapse-btn:hover {
  color: #A33B32;
  background: rgba(163, 59, 50, 0.08);
  transform: scale(1.1);
}

// =================== 通用头部样式 ===================
.layout-main {
  display: flex;
  flex-direction: column;
  overflow: hidden;
  transition: background-color 0.3s;
}

.layout-header {
  height: 60px !important;
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: rgba(255, 255, 255, 0.9);
  backdrop-filter: blur(12px);
  -webkit-backdrop-filter: blur(12px);
  box-shadow: 0 1px 8px rgba(0, 21, 41, 0.06);
  padding: 0 24px;
  border-bottom: 1px solid rgba(232, 228, 220, 0.4);
  z-index: 100;

  .header-left {
    display: flex;
    align-items: center;
    gap: 16px;

    .collapse-btn {
      font-size: 30px;
      cursor: pointer;
      color: #666;
      padding: 6px;
      border-radius: 6px;
      transition: all 0.2s;

      &:hover {
        color: #A33B32;
        background: rgba(163, 59, 50, 0.06);
      }
    }
  }

  .header-right {
    display: flex;
    align-items: center;
    gap: 16px;

    .header-icon {
      font-size: 34px;
      cursor: pointer;
      color: #666;
      transition: all 0.3s;
      padding: 8px;
      border-radius: 10px;

      &:hover {
        color: #A33B32;
        background: rgba(163, 59, 50, 0.08);
        transform: scale(1.08);
      }
    }

    .bell-icon {
      animation: none;

      &:hover {
        animation: bellShake 0.5s ease;
      }
    }

    .badge-item {
      display: flex;
      align-items: center;
    }

    .user-info {
      display: flex;
      align-items: center;
      gap: 8px;
      cursor: pointer;
      padding: 6px 14px;
      border-radius: 10px;
      transition: all 0.3s;
      border: 1px solid transparent;

      &:hover {
        background: rgba(29, 59, 90, 0.04);
        border-color: rgba(29, 59, 90, 0.08);
        transform: translateY(-1px);
      }

      .username {
        color: #333;
        font-size: 13px;
        font-weight: 500;
      }

      .avatar {
        transition: all 0.3s;
        cursor: pointer;
        border: 2px solid transparent;

        &:hover {
          transform: scale(1.08);
          border-color: #A33B32;
          box-shadow: 0 2px 8px rgba(163, 59, 50, 0.3);
        }
      }
    }
  }
}

@keyframes bellShake {
  0%, 100% { transform: rotate(0); }
  20% { transform: rotate(12deg); }
  40% { transform: rotate(-10deg); }
  60% { transform: rotate(6deg); }
  80% { transform: rotate(-4deg); }
}

.layout-container.dark-theme .layout-header {
  background: rgba(30, 30, 30, 0.9);
  box-shadow: 0 1px 8px rgba(0, 0, 0, 0.3);
  border-bottom-color: rgba(255, 255, 255, 0.05);

  .username {
    color: #e0e0e0;
  }
  .header-icon {
    color: #b0b0b0;
  }
  .user-info:hover {
    background: rgba(255,255,255,0.05);
    border-color: rgba(255,255,255,0.08);
  }
}

// =================== 顶部布局 ===================
.top-layout-container {
  display: flex;
  flex-direction: column;
  width: 100%;
  height: 100%;
  overflow: hidden;
}

.top-header {
  height: 60px !important;
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: rgba(255, 255, 255, 0.9);
  backdrop-filter: blur(12px);
  box-shadow: 0 1px 8px rgba(0, 21, 41, 0.06);
  padding: 0 24px;
  border-bottom: 1px solid rgba(232, 228, 220, 0.4);
  transition: all 0.3s;
  flex-shrink: 0;
  z-index: 100;
  position: relative;
  width: 100%;
}

.layout-container.dark-theme .top-header {
  background: rgba(30, 30, 30, 0.9);
  border-bottom-color: rgba(255, 255, 255, 0.05);
  box-shadow: 0 1px 8px rgba(0, 0, 0, 0.3);
}

.top-header-left {
  display: flex;
  align-items: center;
  gap: 24px;
  height: 100%;
  flex: 1;
  min-width: 0;
}

.top-brand {
  flex-shrink: 0;

  .brand-name {
    font-size: 14px;
  }

  .brand-sub {
    font-size: 10px;
  }
}

.top-menu-wrapper {
  flex: 1;
  height: 100%;
  display: flex;
  align-items: center;
  overflow: hidden;
}

.top-menu {
  border-bottom: none !important;
  background: transparent !important;
  width: 100%;
  height: 100%;
  overflow-x: auto;
  overflow-y: hidden;

  :deep(.el-menu-item) {
    height: 60px;
    line-height: 60px;
    border-bottom: 3px solid transparent !important;
    background: transparent !important;
    padding: 0 16px;
    margin: 0;
    display: inline-flex;
    align-items: center;
    gap: 6px;
    transition: all 0.3s;
    flex-shrink: 0;
    border-radius: 0 !important;

    &.is-active {
      border-bottom-color: #A33B32 !important;
      background: rgba(163, 59, 50, 0.05) !important;
      color: #A33B32 !important;
    }

    &:hover {
      background: rgba(29, 59, 90, 0.05) !important;
    }
  }

  :deep(.el-menu--horizontal) {
    border: none;
  }
}

.top-header-right {
  display: flex;
  align-items: center;
  gap: 16px;
  flex-shrink: 0;

  .header-icon, .theme-btn {
    font-size: 24px;
    cursor: pointer;
    color: #666;
    transition: all 0.3s;
    padding: 8px;
    border-radius: 10px;

    &:hover {
      color: #A33B32;
      background: rgba(163, 59, 50, 0.08);
    }
  }

  .badge-item {
    display: flex;
    align-items: center;
  }

  .user-info {
    display: flex;
    align-items: center;
    gap: 8px;
    cursor: pointer;
    padding: 6px 14px;
    border-radius: 10px;
    transition: all 0.3s;

    &:hover {
      background: rgba(29, 59, 90, 0.04);
    }

    .username {
      color: #333;
      font-size: 13px;
    }

    .avatar {
      transition: all 0.3s;
      cursor: pointer;

      &:hover {
        transform: scale(1.08);
      }
    }
  }
}

.layout-container.dark-theme .top-header-right {
  .header-icon, .theme-btn {
    color: #b0b0b0;
  }
  .user-info {
    .username {
      color: #e0e0e0;
    }
    &:hover {
      background: rgba(255,255,255,0.05);
    }
  }
}

// =================== 内容区 ===================
.top-layout-content {
  flex: 1;
  width: 100%;
  overflow-y: auto;
  padding: 20px;
  background: #f5f5f5;
  transition: background-color 0.3s;
  box-sizing: border-box;
}

.layout-container.dark-theme .top-layout-content {
  background: #121212;
}

.layout-content {
  background: #f5f5f5;
  overflow-y: auto;
  padding: 20px;
  transition: background-color 0.3s;
}

.layout-container.dark-theme .layout-content {
  background: #121212;
}

/* 页面切换动画 */
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.3s, transform 0.3s;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
  transform: translateY(10px);
}

/* 隐藏滚动条 */
:deep(.el-scrollbar__bar) {
  display: none !important;
}

// =================== 头像更换弹窗样式 ===================
.avatar-change-dialog {
  .avatar-dialog-content {
    padding: 8px 0;
  }

  .current-avatar-section {
    text-align: center;
    margin-bottom: 24px;
    padding: 20px;
    background: linear-gradient(135deg, rgba(163, 59, 50, 0.04) 0%, rgba(29, 59, 90, 0.03) 100%);
    border-radius: 12px;
    border: 1px solid rgba(163, 59, 50, 0.1);
  }

  .avatar-preview-wrapper {
    display: inline-block;
    position: relative;

    .avatar-preview {
      border: 3px solid #fff;
      box-shadow: 0 4px 16px rgba(163, 59, 50, 0.2);
      transition: all 0.3s;

      &:hover {
        transform: scale(1.05);
        box-shadow: 0 6px 20px rgba(163, 59, 50, 0.3);
      }
    }
  }

  .preview-label {
    margin-top: 12px;
    font-size: 13px;
    color: #999;
    font-weight: 500;
  }

  .avatar-tabs {
    :deep(.el-tabs__header) {
      margin-bottom: 16px;
    }

    :deep(.el-tabs__item) {
      font-size: 14px;
      padding: 0 20px;
      height: 44px;
      line-height: 44px;

      &.is-active {
        font-weight: 600;
      }
    }
  }

  // 头像库
  .avatar-gallery {
    display: grid;
    grid-template-columns: repeat(4, 1fr);
    gap: 12px;
    max-height: 320px;
    overflow-y: auto;
    padding: 4px;

    .avatar-item {
      position: relative;
      cursor: pointer;
      border-radius: 12px;
      padding: 8px;
      transition: all 0.3s;
      background: #f8f9fa;
      border: 2px solid transparent;

      &:hover {
        background: #fff;
        box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
        transform: translateY(-2px);
      }

      &.active {
        border-color: #A33B32;
        background: rgba(163, 59, 50, 0.05);
        box-shadow: 0 4px 12px rgba(163, 59, 50, 0.15);

        &::after {
          content: '✓';
          position: absolute;
          top: 4px;
          right: 4px;
          width: 20px;
          height: 20px;
          background: #A33B32;
          color: #fff;
          border-radius: 50%;
          font-size: 12px;
          display: flex;
          align-items: center;
          justify-content: center;
        }
      }

      .gallery-avatar {
        display: block;
        margin: 0 auto;
      }
    }
  }

  // URL 输入
  .avatar-url-input {
    padding: 8px;

    .el-input {
      margin-bottom: 12px;
    }

    .input-tip {
      font-size: 12px;
      color: #999;
      margin-top: 8px;
      line-height: 1.5;
    }
  }

  // 本地上传
  .avatar-upload {
    display: flex;
    flex-direction: column;
    align-items: center;
    padding: 16px;

    .upload-wrapper {
      width: 100%;
      max-width: 400px;

      :deep(.el-upload) {
        width: 100%;
      }

      :deep(.el-upload-dragger) {
        padding: 32px 24px;
        border-radius: 12px;
        border: 2px dashed #dcdfe6;
        transition: all 0.3s;

        &:hover {
          border-color: #A33B32;
          background: rgba(163, 59, 50, 0.02);
        }
      }

      .el-icon--upload {
        font-size: 48px;
        color: #A33B32;
        margin-bottom: 16px;
      }

      .el-upload__text {
        font-size: 14px;
        color: #666;

        em {
          color: #A33B32;
          font-style: normal;
          font-weight: 500;
        }
      }

      .el-upload__tip {
        font-size: 12px;
        color: #999;
        margin-top: 8px;
      }
    }

    .upload-preview {
      margin-top: 20px;
      text-align: center;

      .preview-img {
        border: 3px solid #fff;
        box-shadow: 0 4px 16px rgba(0, 0, 0, 0.1);
      }

      p {
        margin-top: 12px;
        font-size: 13px;
        color: #666;
        font-weight: 500;
      }
    }
  }

  .button-row {
    display: flex;
    gap: 12px;
    justify-content: center;
  }
}

// =================== 响应式适配 ===================
@include mobile {
  .layout-header, .top-header {
    height: 56px !important;
    padding: 0 15px;
  }

  .top-brand .brand-text {
    display: none;
  }

  .top-menu :deep(.el-menu-item) {
    padding: 0 8px;
    span {
      display: none;
    }
  }

  .layout-content, .top-layout-content {
    padding: 12px;
  }

  .top-header-right, .header-right {
    gap: 8px;
    .username {
      display: none;
    }
  }

  .sidebar-widget {
    display: none;
  }


}

@include tablet {
  .layout-aside {
    width: 200px !important;
  }
  .top-menu :deep(.el-menu-item) {
    padding: 0 12px;
  }
}
</style>
