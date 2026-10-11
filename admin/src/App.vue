<template>
  <router-view />
</template>

<script setup>
</script>

<style>
/* ==================== 全局 CSS 变量系统 ==================== */
:root {
  /* 品牌色 */
  --dong-red: #A33B32;
  --dong-red-light: #c0504a;
  --dong-red-dark: #8c2f27;
  --dong-blue: #1D3B5A;
  --dong-blue-light: #2E5F8E;
  --dong-wood: #D4A373;
  --dong-paper: #FDFCF8;

  /* 中性色 */
  --text-primary: #1D3B5A;
  --text-regular: #333;
  --text-secondary: #666;
  --text-muted: #999;
  --text-placeholder: #bbb;
  --border-light: #e8e4dc;
  --bg-page: #f5f5f5;
  --bg-card: rgba(255, 255, 255, 0.85);

  /* 阴影 */
  --card-shadow: 0 4px 24px rgba(0, 0, 0, 0.06);
  --card-shadow-hover: 0 8px 32px rgba(0, 0, 0, 0.1);
  --btn-shadow: 0 4px 12px rgba(163, 59, 50, 0.3);

  /* 圆角 */
  --radius-sm: 8px;
  --radius-md: 12px;
  --radius-lg: 16px;
  --radius-xl: 24px;

  /* 动画 */
  --transition-fast: 0.2s cubic-bezier(0.4, 0, 0.2, 1);
  --transition-normal: 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  --transition-slow: 0.5s cubic-bezier(0.4, 0, 0.2, 1);

  /* 玻璃拟态 */
  --glass-bg: rgba(255, 255, 255, 0.85);
  --glass-border: rgba(255, 255, 255, 0.6);
  --glass-blur: 12px;
  --glass-shadow: 0 4px 24px rgba(0, 0, 0, 0.06), 0 1px 4px rgba(0, 0, 0, 0.04), inset 0 1px 0 rgba(255, 255, 255, 0.8);

  /* 安全区域 */
  --safe-area-inset-top: env(safe-area-inset-top, 0px);
  --safe-area-inset-right: env(safe-area-inset-right, 0px);
  --safe-area-inset-bottom: env(safe-area-inset-bottom, 0px);
  --safe-area-inset-left: env(safe-area-inset-left, 0px);
}

/* ==================== 全局重置 ==================== */
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

html, body {
  width: 100%;
  height: 100%;
  font-family: -apple-system, BlinkMacSystemFont, 'PingFang SC', 'Hiragino Sans GB',
    'Microsoft YaHei', 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
  background: var(--bg-page);
  color: var(--text-regular);
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
}

/* ==================== 滚动条美化 ==================== */
::-webkit-scrollbar {
  width: 6px;
  height: 6px;
}
::-webkit-scrollbar-thumb {
  background: linear-gradient(180deg, rgba(163,59,50,0.2), rgba(29,59,90,0.15));
  border-radius: 3px;
  transition: background var(--transition-fast);
}
::-webkit-scrollbar-thumb:hover {
  background: linear-gradient(180deg, rgba(163,59,50,0.35), rgba(29,59,90,0.25));
}
::-webkit-scrollbar-track {
  background: transparent;
}
html {
  scrollbar-width: thin;
  scrollbar-color: rgba(163,59,50,0.2) transparent;
  -ms-overflow-style: none;
}

/* ==================== 全局 Element Plus 覆盖 ==================== */
/* 按钮样式已移至 styles/buttons.scss，使用 Element Plus 官方 plain 样式 */

/* 分页 */
.el-pagination.is-background .el-pager li.is-active {
  background: linear-gradient(135deg, var(--dong-red), var(--dong-red-light)) !important;
  color: #fff !important;
  border-radius: var(--radius-sm);
}
.el-pagination.is-background .el-pager li:hover {
  color: var(--dong-red);
}

/* Tabs */
.el-tabs__item.is-active {
  color: var(--dong-red);
}
.el-tabs__active-bar {
  background: linear-gradient(90deg, var(--dong-red), var(--dong-red-light));
  height: 3px;
  border-radius: 2px;
}

/* Radio */
.el-radio__input.is-checked .el-radio__inner {
  border-color: var(--dong-red) !important;
  background: var(--dong-red) !important;
}

/* Checkbox */
.el-checkbox__input.is-checked .el-checkbox__inner {
  background-color: var(--dong-red) !important;
  border-color: var(--dong-red) !important;
}

/* Switch */
.el-switch.is-checked .el-switch__core {
  border-color: var(--dong-red) !important;
  background-color: var(--dong-red) !important;
}

/* Dialog 圆角 */
.el-dialog {
  border-radius: var(--radius-xl) !important;
}

/* Input focus */
.el-input__wrapper.is-focus {
  box-shadow: 0 0 0 1px var(--dong-red) inset !important;
}

/* Select */
.el-select .el-input.is-focus .el-input__wrapper {
  box-shadow: 0 0 0 1px var(--dong-red) inset !important;
}

/* ==================== 全局工具类 ==================== */
/* 玻璃卡片 */
.glass-card {
  background: var(--glass-bg);
  backdrop-filter: blur(var(--glass-blur));
  -webkit-backdrop-filter: blur(var(--glass-blur));
  border: 1px solid var(--glass-border);
  border-radius: var(--radius-lg);
  box-shadow: var(--glass-shadow);
  transition: all var(--transition-normal);
}
.glass-card:hover {
  transform: translateY(-2px);
  box-shadow: var(--card-shadow-hover), inset 0 1px 0 rgba(255,255,255,0.8);
}

/* 渐变文字 */
.gradient-text {
  background: linear-gradient(135deg, var(--dong-blue) 0%, var(--dong-red) 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

/* 淡入动画 */
.fade-in-up {
  opacity: 0;
  animation: globalFadeInUp 0.6s ease-out forwards;
}
@keyframes globalFadeInUp {
  from { opacity: 0; transform: translateY(20px); }
  to { opacity: 1; transform: translateY(0); }
}

/* ==================== 暗黑模式 ==================== */
html.dark {
  --bg-page: #121212;
  --glass-bg: rgba(30, 30, 30, 0.85);
  --glass-border: rgba(255, 255, 255, 0.08);
  --glass-shadow: 0 4px 24px rgba(0, 0, 0, 0.3), 0 1px 4px rgba(0, 0, 0, 0.2), inset 0 1px 0 rgba(255, 255, 255, 0.05);
  --text-primary: #e0e0e0;
  --text-regular: #d0d0d0;
  --text-secondary: #b0b0b0;
  --text-muted: #888;
  --border-light: rgba(255,255,255,0.08);

  ::-webkit-scrollbar-thumb {
    background: rgba(255,255,255,0.15);
  }

  .layout-content {
    background: var(--bg-page);
  }

  .el-card {
    --el-card-border-color: var(--border-light);
    background: var(--glass-bg);
  }

  .el-dialog {
    --el-dialog-bg-color: #1e1e1e;
  }

  .el-drawer {
    --el-drawer-bg-color: #1e1e1e;
  }

  .el-table {
    --el-table-border-color: var(--border-light);
    --el-table-header-bg-color: #1a1a1a;
    --el-table-row-hover-bg-color: #222;
    --el-table-current-row-bg-color: #252525;
  }

  #nprogress .bar {
    background: var(--dong-red) !important;
  }

  .context-menu {
    background-color: #1e1e1e;
    border: 1px solid var(--border-light);
    .context-menu-item {
      color: var(--text-regular);
      &:hover {
        background-color: #252525;
        color: var(--dong-red);
      }
      &.disabled {
        color: var(--text-muted);
      }
    }
  }

  .tabs-view {
    background-color: #1a1a1a;
    border-bottom-color: var(--border-light);
    :deep(.el-tabs__item) {
      background-color: #222;
      border-color: var(--border-light);
      color: var(--text-secondary);
      &:hover {
        background-color: #252525;
        color: var(--dong-red);
        border-color: var(--dong-red);
      }
      &.is-active {
        background-color: #1a1a1a;
        color: var(--dong-red);
        border-color: var(--dong-red);
        border-bottom-color: #1a1a1a;
      }
    }
    .tabs-actions :deep(.el-button) {
      background-color: #222;
      border-color: var(--border-light);
      color: var(--text-secondary);
      &:hover {
        background-color: rgba(163,59,50,0.15);
        border-color: var(--dong-red);
        color: var(--dong-red);
      }
    }
  }

  .settings-panel {
    .section-title { color: var(--text-primary); }
    .setting-item {
      border-bottom-color: var(--border-light);
      .item-label span { color: var(--text-primary); }
    }
    .setting-actions { border-top-color: var(--border-light); }
  }

  :deep(.el-drawer__header) {
    border-bottom-color: var(--border-light);
  }
}
</style>
