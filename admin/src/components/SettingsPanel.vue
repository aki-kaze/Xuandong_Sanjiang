<template>
  <el-drawer
    v-model="visible"
    direction="rtl"
    :size="400"
    class="settings-drawer"
    :show-close="false"
  >
    <template #header>
      <div class="drawer-header">
        <div class="header-title">
          <el-icon class="header-icon"><Setting /></el-icon>
          <span>页面设置</span>
        </div>
        <el-icon class="close-btn" @click="visible = false"><Close /></el-icon>
      </div>
    </template>

    <div class="settings-panel">
      <!-- 主题模式 -->
      <div class="setting-section">
        <h3 class="section-title">
          <span class="title-accent"></span>
          主题模式
        </h3>
        <div class="section-content">
          <div class="mode-chip-group">
            <div
              class="mode-chip"
              :class="{ active: localConfig.theme === 'light' }"
              @click="handleThemeChange('light')"
            >
              <div class="chip-icon-wrap light-icon">
                <el-icon><Sunny /></el-icon>
              </div>
              <span class="chip-label">浅色模式</span>
              <span class="chip-desc">清新明亮</span>
              <div class="chip-indicator"></div>
            </div>
            <div
              class="mode-chip"
              :class="{ active: localConfig.theme === 'dark' }"
              @click="handleThemeChange('dark')"
            >
              <div class="chip-icon-wrap dark-icon">
                <el-icon><Moon /></el-icon>
              </div>
              <span class="chip-label">深色模式</span>
              <span class="chip-desc">护眼沉浸</span>
              <div class="chip-indicator"></div>
            </div>
          </div>
        </div>
      </div>

      <!-- 布局模式 -->
      <div class="setting-section">
        <h3 class="section-title">
          <span class="title-accent"></span>
          布局模式
        </h3>
        <div class="section-content">
          <div class="layout-preview-group">
            <div
              class="layout-preview-item"
              :class="{ active: localConfig.layout === 'leftMode' }"
              @click="handleLayoutChange('leftMode')"
            >
              <div class="layout-preview left-layout">
                <div class="preview-sidebar"></div>
                <div class="preview-content">
                  <div class="preview-header"></div>
                  <div class="preview-main">
                    <div class="preview-card"></div>
                    <div class="preview-card short"></div>
                  </div>
                </div>
              </div>
              <div class="layout-label">左侧布局</div>
              <div class="layout-indicator"></div>
            </div>
            <div
              class="layout-preview-item"
              :class="{ active: localConfig.layout === 'topMode' }"
              @click="handleLayoutChange('topMode')"
            >
              <div class="layout-preview top-layout">
                <div class="preview-header">
                  <div class="preview-menu-dots">
                    <span></span><span></span><span></span>
                  </div>
                </div>
                <div class="preview-main">
                  <div class="preview-card"></div>
                  <div class="preview-card short"></div>
                </div>
              </div>
              <div class="layout-label">顶部布局</div>
              <div class="layout-indicator"></div>
            </div>
          </div>
        </div>
      </div>

      <!-- 主题颜色 -->
      <div class="setting-section">
        <h3 class="section-title">
          <span class="title-accent"></span>
          主题颜色
        </h3>
        <div class="section-content">
          <div class="color-chip-group">
            <div
              v-for="color in pageConfigStore.primaryColorOptions"
              :key="color.value"
              class="color-chip"
              :class="{ active: localConfig.primaryColor === color.value }"
              @click="handleColorChange(color.value)"
            >
              <span class="chip-dot" :style="{ background: color.value }"></span>
              <span class="chip-name">{{ color.name }}</span>
              <div class="color-indicator" :style="{ background: color.value }"></div>
            </div>
          </div>
          <!-- 颜色预览条 -->
          <div class="color-preview-bar" :style="{ background: `linear-gradient(135deg, ${localConfig.primaryColor || '#A33B32'}, ${localConfig.primaryColor ? localConfig.primaryColor + '88' : '#c0504a88'})` }">
            <span class="preview-text">预览效果</span>
          </div>
        </div>
      </div>

      <!-- 标签页设置 -->
      <div class="setting-section">
        <h3 class="section-title">
          <span class="title-accent"></span>
          标签页设置
        </h3>

        <div class="setting-item">
          <div class="item-label">
            <span>显示标签页</span>
            <p class="item-desc">开启后，访问的页面会以标签页形式显示</p>
          </div>
          <el-switch
            v-model="localConfig.showTabs"
            @change="handleConfigChange('showTabs', $event)"
          />
        </div>

        <div class="setting-item" v-if="localConfig.showTabs">
          <div class="item-label">
            <span>最大标签数</span>
            <p class="item-desc">超过此数量时自动关闭最早的标签页</p>
          </div>
          <el-input-number
            v-model="localConfig.maxTabsCount"
            :min="5"
            :max="30"
            :step="5"
            size="small"
            @change="handleConfigChange('maxTabsCount', $event)"
          />
        </div>
      </div>

      <!-- 界面设置 -->
      <div class="setting-section">
        <h3 class="section-title">
          <span class="title-accent"></span>
          界面设置
        </h3>

        <div class="setting-item">
          <div class="item-label">
            <span>显示面包屑</span>
            <p class="item-desc">在顶部显示当前页面的路径</p>
          </div>
          <el-switch
            v-model="localConfig.showBreadcrumb"
            @change="handleConfigChange('showBreadcrumb', $event)"
          />
        </div>

        <div class="setting-item">
          <div class="item-label">
            <span>页面过渡动画</span>
            <p class="item-desc">切换页面时显示过渡动画效果</p>
          </div>
          <el-switch
            v-model="localConfig.showPageTransition"
            @change="handleConfigChange('showPageTransition', $event)"
          />
        </div>
      </div>

      <!-- 操作按钮 -->
      <div class="setting-actions">
        <el-button @click="handleReset" class="reset-btn">
          <el-icon><RefreshRight /></el-icon>
          恢复默认
        </el-button>
        <el-button type="primary" @click="handleSave" class="save-btn">
          <el-icon><Check /></el-icon>
          保存设置
        </el-button>
      </div>
    </div>
  </el-drawer>
</template>

<script setup>
import { ref, watch, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { Sunny, Moon, Setting, Close, RefreshRight, Check } from '@element-plus/icons-vue'
import { usePageConfigStore } from '@/stores/pageConfig'

const pageConfigStore = usePageConfigStore()

const visible = computed({
  get: () => pageConfigStore.settingsVisible,
  set: (val) => {
    if (!val) {
      pageConfigStore.closeSettings()
    }
  }
})

const localConfig = ref({ ...pageConfigStore.config })

watch(
  () => pageConfigStore.config,
  (newConfig) => {
    localConfig.value = { ...newConfig }
  },
  { deep: true }
)

const handleConfigChange = (key, value) => {
  pageConfigStore.updateConfig(key, value)
}

const handleThemeChange = (theme) => {
  localConfig.value.theme = theme
  pageConfigStore.applyTheme(theme)
  pageConfigStore.updateConfig('theme', theme)
  if (localConfig.value.primaryColor) {
    pageConfigStore.setPrimaryColor(localConfig.value.primaryColor)
  }
}

const handleLayoutChange = (layout) => {
  localConfig.value.layout = layout
  pageConfigStore.updateConfig('layout', layout)
  pageConfigStore.toggleLayout(layout)
}

const handleColorChange = (color) => {
  localConfig.value.primaryColor = color
  pageConfigStore.togglePrimaryColor(color)
  pageConfigStore.updateConfig('primaryColor', color)
}

const handleSave = () => {
  pageConfigStore.updateMultipleConfig(localConfig.value)
  pageConfigStore.saveConfig()
  ElMessage.success('设置已保存')
  pageConfigStore.closeSettings()
}

const handleReset = () => {
  pageConfigStore.resetConfig()
  localConfig.value = { ...pageConfigStore.config }
  pageConfigStore.applyTheme(localConfig.value.theme)
  if (localConfig.value.primaryColor) {
    pageConfigStore.setPrimaryColor(localConfig.value.primaryColor)
  }
  ElMessage.success('已恢复默认设置')
}
</script>

<style scoped lang="scss">
// ==================== 抽屉头部 ====================
.drawer-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  width: 100%;
  padding: 0 4px;
}

.header-title {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 18px;
  font-weight: 700;
  color: var(--dong-blue, #1D3B5A);
}

.header-icon {
  font-size: 20px;
  color: var(--dong-red, #A33B32);
}

.close-btn {
  font-size: 18px;
  color: #999;
  cursor: pointer;
  padding: 6px;
  border-radius: 8px;
  transition: all 0.2s;

  &:hover {
    color: var(--dong-red, #A33B32);
    background: rgba(163, 59, 50, 0.08);
  }
}

// ==================== 面板主体 ====================
.settings-panel {
  padding: 0 4px 20px;
}

.setting-section {
  margin-bottom: 24px;
  animation: sectionFadeIn 0.4s ease-out backwards;

  &:nth-child(1) { animation-delay: 0.05s; }
  &:nth-child(2) { animation-delay: 0.1s; }
  &:nth-child(3) { animation-delay: 0.15s; }
  &:nth-child(4) { animation-delay: 0.2s; }
  &:nth-child(5) { animation-delay: 0.25s; }

  &:last-child {
    margin-bottom: 0;
  }
}

@keyframes sectionFadeIn {
  from { opacity: 0; transform: translateX(10px); }
  to { opacity: 1; transform: translateX(0); }
}

.section-title {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 14px;
  font-weight: 700;
  color: #1D3B5A;
  margin-bottom: 16px;

  .title-accent {
    width: 4px;
    height: 16px;
    background: linear-gradient(180deg, #A33B32, #D4A373);
    border-radius: 2px;
    flex-shrink: 0;
  }
}

.section-content {
  padding-left: 14px;
}

// ==================== 主题模式芯片 ====================
.mode-chip-group {
  display: flex;
  gap: 12px;
}

.mode-chip {
  flex: 1;
  position: relative;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 6px;
  padding: 18px 14px 14px;
  border-radius: 14px;
  border: 2px solid #e8e4dc;
  background: #fafafa;
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  overflow: hidden;

  .chip-icon-wrap {
    width: 40px;
    height: 40px;
    border-radius: 12px;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 20px;
    transition: all 0.3s;

    &.light-icon {
      background: linear-gradient(135deg, #fff7e6, #ffe8b3);
      color: #e6a23c;
    }

    &.dark-icon {
      background: linear-gradient(135deg, #e8eaf6, #c5cae9);
      color: #5c6bc0;
    }
  }

  .chip-label {
    font-size: 13px;
    font-weight: 600;
    color: #333;
  }

  .chip-desc {
    font-size: 11px;
    color: #aaa;
  }

  .chip-indicator {
    position: absolute;
    bottom: 0;
    left: 50%;
    transform: translateX(-50%) scaleX(0);
    width: 60%;
    height: 3px;
    background: linear-gradient(135deg, #A33B32, #c0504a);
    border-radius: 3px 3px 0 0;
    transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  }

  &:hover {
    border-color: #A33B32;
    transform: translateY(-3px);
    box-shadow: 0 6px 16px rgba(163, 59, 50, 0.12);

    .chip-icon-wrap {
      transform: scale(1.1);
    }
  }

  &.active {
    background: linear-gradient(135deg, rgba(163, 59, 50, 0.06), rgba(163, 59, 50, 0.02));
    border-color: #A33B32;
    box-shadow: 0 4px 16px rgba(163, 59, 50, 0.15);

    .chip-label {
      color: #A33B32;
    }

    .chip-indicator {
      transform: translateX(-50%) scaleX(1);
    }
  }
}

// ==================== 布局预览 ====================
.layout-preview-group {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 12px;
}

.layout-preview-item {
  position: relative;
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  padding: 12px;
  border-radius: 14px;
  border: 2px solid #e8e4dc;
  background: #fafafa;
  overflow: hidden;

  .layout-indicator {
    position: absolute;
    bottom: 0;
    left: 50%;
    transform: translateX(-50%) scaleX(0);
    width: 50%;
    height: 3px;
    background: linear-gradient(135deg, #A33B32, #c0504a);
    border-radius: 3px 3px 0 0;
    transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  }

  &:hover {
    border-color: #A33B32;
    transform: translateY(-2px);
    box-shadow: 0 6px 16px rgba(163, 59, 50, 0.12);
  }

  &.active {
    border-color: #A33B32;
    background: linear-gradient(135deg, rgba(163, 59, 50, 0.06), rgba(163, 59, 50, 0.02));
    box-shadow: 0 4px 16px rgba(163, 59, 50, 0.15);

    .layout-indicator {
      transform: translateX(-50%) scaleX(1);
    }
  }

  .layout-preview {
    width: 100%;
    aspect-ratio: 4/3;
    border-radius: 10px;
    border: 1px solid #e8e4dc;
    background: #fff;
    padding: 6px;
    transition: all 0.3s;
    margin-bottom: 8px;
    overflow: hidden;

    &.left-layout {
      display: flex;
      gap: 4px;

      .preview-sidebar {
        width: 28%;
        background: linear-gradient(180deg, #A33B32, #8c2f27);
        border-radius: 4px;
        position: relative;
        overflow: hidden;

        &::after {
          content: '';
          position: absolute;
          top: 4px;
          left: 4px;
          right: 4px;
          height: 3px;
          background: rgba(255,255,255,0.3);
          border-radius: 2px;
          box-shadow: 0 6px 0 rgba(255,255,255,0.15), 0 12px 0 rgba(255,255,255,0.1), 0 18px 0 rgba(255,255,255,0.08);
        }
      }

      .preview-content {
        flex: 1;
        display: flex;
        flex-direction: column;
        gap: 4px;

        .preview-header {
          height: 18%;
          background: #f0f0f0;
          border-radius: 4px;
        }

        .preview-main {
          flex: 1;
          background: #fafafa;
          border-radius: 4px;
          padding: 4px;
          display: flex;
          flex-direction: column;
          gap: 3px;

          .preview-card {
            flex: 1;
            background: #fff;
            border: 1px solid #eee;
            border-radius: 3px;
            &.short { flex: 0.6; }
          }
        }
      }
    }

    &.top-layout {
      display: flex;
      flex-direction: column;
      gap: 4px;

      .preview-header {
        height: 22%;
        background: linear-gradient(90deg, #A33B32, #8c2f27);
        border-radius: 4px;
        display: flex;
        align-items: center;
        padding: 0 6px;

        .preview-menu-dots {
          display: flex;
          gap: 3px;
          span {
            width: 4px;
            height: 4px;
            border-radius: 50%;
            background: rgba(255,255,255,0.4);
            &:first-child { background: rgba(255,255,255,0.8); width: 6px; border-radius: 3px; }
          }
        }
      }

      .preview-main {
        flex: 1;
        background: #fafafa;
        border-radius: 4px;
        padding: 4px;
        display: flex;
        flex-direction: column;
        gap: 3px;

        .preview-card {
          flex: 1;
          background: #fff;
          border: 1px solid #eee;
          border-radius: 3px;
          &.short { flex: 0.6; }
        }
      }
    }
  }

  .layout-label {
    text-align: center;
    font-size: 12px;
    font-weight: 600;
    color: #666;
    transition: color 0.3s;
  }

  &.active .layout-label {
    color: #A33B32;
  }
}

// ==================== 主题颜色 ====================
.color-chip-group {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-bottom: 12px;
}

.color-chip {
  position: relative;
  min-width: 88px;
  padding: 8px 12px;
  border-radius: 10px;
  border: 2px solid #e8e4dc;
  background: #fafafa;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  font-size: 12px;
  font-weight: 500;
  color: #666;
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);

  .chip-dot {
    width: 16px;
    height: 16px;
    border-radius: 50%;
    border: 2px solid rgba(0, 0, 0, 0.08);
    transition: all 0.3s;
  }

  .chip-name { white-space: nowrap; }

  .color-indicator {
    position: absolute;
    bottom: 0;
    left: 50%;
    transform: translateX(-50%) scaleX(0);
    width: 50%;
    height: 3px;
    border-radius: 3px 3px 0 0;
    transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  }

  &:hover {
    border-color: #A33B32;
    transform: translateY(-2px);
    box-shadow: 0 4px 12px rgba(0,0,0,0.08);

    .chip-dot {
      transform: scale(1.2);
      box-shadow: 0 2px 6px rgba(0,0,0,0.2);
    }
  }

  &.active {
    background: linear-gradient(135deg, rgba(163, 59, 50, 0.06), rgba(163, 59, 50, 0.02));
    border-color: #A33B32;
    color: #A33B32;
    font-weight: 600;
    box-shadow: 0 4px 12px rgba(163, 59, 50, 0.15);

    .color-indicator {
      transform: translateX(-50%) scaleX(1);
    }
  }
}

// 颜色预览条
.color-preview-bar {
  height: 36px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: background 0.5s ease;
  box-shadow: 0 2px 8px rgba(0,0,0,0.08);

  .preview-text {
    color: #fff;
    font-size: 12px;
    font-weight: 600;
    text-shadow: 0 1px 3px rgba(0,0,0,0.2);
    letter-spacing: 2px;
  }
}

// ==================== 设置项 ====================
.setting-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 14px 0;
  border-bottom: 1px solid rgba(0,0,0,0.04);

  &:last-child {
    border-bottom: none;
  }

  .item-label {
    flex: 1;
    margin-right: 12px;

    span {
      display: block;
      font-size: 14px;
      font-weight: 500;
      color: #333;
      margin-bottom: 4px;
    }

    .item-desc {
      font-size: 12px;
      color: #aaa;
      margin: 0;
      line-height: 1.4;
    }
  }
}

// ==================== 操作按钮 ====================
.setting-actions {
  display: flex;
  gap: 12px;
  margin-top: 28px;
  padding-top: 20px;
  border-top: 2px solid #e8e4dc;

  .el-button {
    flex: 1;
    height: 44px;
    border-radius: 12px;
    font-weight: 600;
    font-size: 14px;
    transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 6px;
  }

  .reset-btn {
    background: #fafafa;
    border-color: #e8e4dc;
    color: #666;

    &:hover {
      background: #f0f0f0;
      border-color: #d0d0d0;
      color: #333;
      transform: translateY(-1px);
    }
  }

  .save-btn {
    background: linear-gradient(135deg, #A33B32, #8c2f27);
    border-color: #A33B32;
    color: white;
    box-shadow: 0 4px 12px rgba(163, 59, 50, 0.3);

    &:hover {
      background: linear-gradient(135deg, #8c2f27, #A33B32);
      transform: translateY(-2px);
      box-shadow: 0 6px 20px rgba(163, 59, 50, 0.4);
    }

    &:active {
      transform: translateY(0);
    }
  }
}

// ==================== Element Plus 覆盖 ====================
:deep(.el-drawer__header) {
  margin-bottom: 0;
  padding: 20px 24px 16px;
  border-bottom: 1px solid rgba(0,0,0,0.06);
}

:deep(.el-drawer__body) {
  padding: 20px 24px;
  overflow-y: auto;
}

:deep(.el-drawer__close) {
  display: none;
}
</style>
