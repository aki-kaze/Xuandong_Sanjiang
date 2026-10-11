<template>
  <div class="tabs-view">
    <el-tabs
      v-model="activeTab"
      type="card"
      @tab-click="handleTabClick"
      @tab-remove="handleTabRemove"
    >
      <el-tab-pane
        v-for="tab in tabs"
        :key="tab.path"
        :label="tab.title"
        :name="tab.path"
        :closable="tab.closable"
      >
        <template #label>
          <span
            class="tab-label"
            @contextmenu.prevent.stop="handleContextMenu($event, tab)"
          >
            {{ tab.title }}
          </span>
        </template>
      </el-tab-pane>
    </el-tabs>

    <!-- 标签页操作按钮 -->
    <div class="tabs-actions">
      <el-dropdown @command="handleCommand" trigger="click">
        <el-button size="small" circle>
          <el-icon><ArrowDown /></el-icon>
        </el-button>
        <template #dropdown>
          <el-dropdown-menu>
            <el-dropdown-item command="refresh">
              <el-icon><Refresh /></el-icon>
              刷新当前
            </el-dropdown-item>
            <el-dropdown-item command="closeOthers">
              <el-icon><Close /></el-icon>
              关闭其他
            </el-dropdown-item>
            <el-dropdown-item command="closeLeft">
              <el-icon><Back /></el-icon>
              关闭左侧
            </el-dropdown-item>
            <el-dropdown-item command="closeRight">
              <el-icon><Right /></el-icon>
              关闭右侧
            </el-dropdown-item>
            <el-dropdown-item command="closeAll" divided>
              <el-icon><CloseBold /></el-icon>
              关闭所有
            </el-dropdown-item>
          </el-dropdown-menu>
        </template>
      </el-dropdown>
    </div>

    <!-- 右键菜单 -->
    <teleport to="body">
      <div
        v-show="contextMenuVisible"
        class="context-menu"
        :style="{ left: contextMenuPosition.x + 'px', top: contextMenuPosition.y + 'px' }"
      >
        <div class="context-menu-item" @click="handleContextCommand('refresh')">
          <el-icon><Refresh /></el-icon>
          <span>刷新</span>
        </div>
        <div
          class="context-menu-item"
          :class="{ disabled: !selectedTab?.closable }"
          @click="handleContextCommand('close')"
        >
          <el-icon><Close /></el-icon>
          <span>关闭</span>
        </div>
        <div class="context-menu-item" @click="handleContextCommand('closeOthers')">
          <el-icon><Close /></el-icon>
          <span>关闭其他</span>
        </div>
        <div class="context-menu-item" @click="handleContextCommand('closeLeft')">
          <el-icon><Back /></el-icon>
          <span>关闭左侧</span>
        </div>
        <div class="context-menu-item" @click="handleContextCommand('closeRight')">
          <el-icon><Right /></el-icon>
          <span>关闭右侧</span>
        </div>
      </div>
    </teleport>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ArrowDown, Refresh, Close, CloseBold, Back, Right } from '@element-plus/icons-vue'

const router = useRouter()
const route = useRoute()

const contextMenuVisible = ref(false)
const contextMenuPosition = ref({ x: 0, y: 0 })
const selectedTab = ref(null)

// 简单的 tabs 状态管理（内联，不依赖独立 store）
const tabs = ref([
  { path: '/dashboard', title: '数据概览', closable: false }
])

const activeTab = computed({
  get: () => route.path,
  set: () => {}
})

// 根据路由自动添加标签
const addTab = (path, title) => {
  const exists = tabs.value.find(t => t.path === path)
  if (!exists) {
    tabs.value.push({ path, title, closable: path !== '/dashboard' })
  }
}

const handleTabClick = (tab) => {
  const path = tab.paneName
  if (path !== route.path) {
    router.push(path)
  }
}

const handleTabRemove = (path) => {
  const index = tabs.value.findIndex(t => t.path === path)
  if (index > -1) {
    tabs.value.splice(index, 1)
  }
  if (path === route.path) {
    const lastTab = tabs.value[tabs.value.length - 1]
    if (lastTab) {
      router.push(lastTab.path)
    }
  }
}

const handleContextMenu = (event, tab) => {
  event.preventDefault()
  event.stopPropagation()
  selectedTab.value = tab
  contextMenuPosition.value = { x: event.clientX, y: event.clientY }
  contextMenuVisible.value = true
}

const handleClickOutside = () => {
  contextMenuVisible.value = false
}

const handleContextCommand = (command) => {
  if (!selectedTab.value) return
  const tabPath = selectedTab.value.path

  switch (command) {
    case 'refresh':
      router.go(0)
      break
    case 'close':
      if (selectedTab.value.closable) handleTabRemove(tabPath)
      break
    case 'closeOthers':
      tabs.value = tabs.value.filter(t => t.path === tabPath)
      if (route.path !== tabPath) router.push(tabPath)
      break
    case 'closeLeft':
      tabs.value = tabs.value.filter(t => t.path === tabPath || tabs.value.findIndex(x => x.path === t.path) >= tabs.value.findIndex(x => x.path === tabPath))
      break
    case 'closeRight':
      tabs.value = tabs.value.filter(t => t.path === tabPath || tabs.value.findIndex(x => x.path === t.path) <= tabs.value.findIndex(x => x.path === tabPath))
      break
  }
  contextMenuVisible.value = false
}

const handleCommand = (command) => {
  const currentPath = route.path
  switch (command) {
    case 'refresh':
      router.go(0)
      break
    case 'closeOthers':
      tabs.value = tabs.value.filter(t => t.path === currentPath)
      break
    case 'closeAll':
      tabs.value = [{ path: '/dashboard', title: '数据概览', closable: false }]
      router.push('/dashboard')
      break
  }
}

// 监听路由变化，自动添加标签
import { watch } from 'vue'
watch(
  () => route.path,
  (path) => {
    if (route.meta?.title) {
      addTab(path, route.meta.title)
    }
  },
  { immediate: true }
)

onMounted(() => {
  document.addEventListener('click', handleClickOutside)
  document.addEventListener('contextmenu', handleClickOutside)
})

onUnmounted(() => {
  document.removeEventListener('click', handleClickOutside)
  document.removeEventListener('contextmenu', handleClickOutside)
})
</script>

<style scoped lang="scss">
.tabs-view {
  position: relative;
  display: flex;
  align-items: center;
  background-color: var(--el-bg-color);
  border-bottom: 1px solid var(--el-border-color-lighter);
  padding: 0 10px;
  gap: 8px;

  :deep(.el-tabs) {
    flex: 1;
    min-width: 0;
    overflow: hidden;

    .el-tabs__header {
      margin: 0;
      border-bottom: none;
      padding: 6px 0 0 0;
    }

    .el-tabs__nav-wrap {
      &::after {
        display: none;
      }
    }

    .el-tabs__nav-scroll {
      overflow-x: auto;
      overflow-y: hidden;

      &::-webkit-scrollbar {
        height: 4px;
      }

      &::-webkit-scrollbar-thumb {
        background-color: var(--el-border-color);
        border-radius: 3px;
      }

      &::-webkit-scrollbar-track {
        background-color: transparent;
      }
    }

    .el-tabs__nav {
      border: none;
      display: inline-flex;
      flex-wrap: nowrap;
    }

    .el-tabs__item {
      height: 34px;
      line-height: 34px;
      padding: 0 16px;
      font-size: 13px;
      color: var(--el-text-color-regular);
      border: 1px solid var(--el-border-color-light);
      border-radius: 3px 3px 0 0;
      margin-right: 4px;
      background-color: var(--el-fill-color-lighter);
      transition: all 0.3s;
      position: relative;
      top: 1px;

      &:hover {
        color: var(--el-color-primary);
        background-color: var(--el-bg-color);
        border-color: var(--el-color-primary);
      }

      &.is-active {
        color: var(--el-color-primary);
        background-color: var(--el-bg-color);
        border-color: var(--el-color-primary);
        border-bottom-color: var(--el-bg-color);
      }

      .tab-label {
        display: flex;
        align-items: center;
        gap: 4px;
      }
    }
  }
}

.tabs-actions {
  flex-shrink: 0;
  padding: 6px 0 0 0;

  :deep(.el-button) {
    background-color: var(--el-fill-color-lighter);
    border-color: var(--el-border-color);

    &:hover {
      background-color: var(--el-color-primary-light-9);
      border-color: var(--el-color-primary);
      color: var(--el-color-primary);
    }
  }
}

.context-menu {
  position: fixed;
  z-index: 9999;
  background-color: var(--el-bg-color-overlay);
  border: 1px solid var(--el-border-color-lighter);
  border-radius: 4px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  padding: 5px 0;
  min-width: 120px;

  .context-menu-item {
    display: flex;
    align-items: center;
    gap: 8px;
    padding: 8px 16px;
    font-size: 14px;
    color: var(--el-text-color-regular);
    cursor: pointer;
    transition: all 0.2s;

    &:hover {
      background-color: var(--el-color-primary-light-9);
      color: var(--el-color-primary);
    }

    &.disabled {
      color: var(--el-text-color-disabled);
      cursor: not-allowed;

      &:hover {
        background-color: transparent;
        color: var(--el-text-color-disabled);
      }
    }
  }
}
</style>
