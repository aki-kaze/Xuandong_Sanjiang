<script setup>
import { onLaunch, onShow, onHide, onPageNotFound } from '@dcloudio/uni-app';
import { userApi } from './utils/api.js'

// 保存当前页面路径到本地存储
function saveCurrentPage() {
  try {
    const pages = getCurrentPages()
    if (pages.length > 0) {
      const currentPage = pages[pages.length - 1]
      const path = currentPage.route
      const query = currentPage.options ? '?' + Object.keys(currentPage.options).map(key => `${key}=${currentPage.options[key]}`).join('&') : ''
      uni.setStorageSync('lastPagePath', path + query)
    }
  } catch (e) {
    // 忽略错误
  }
}

// 页面变化时自动保存路径
function watchPageChange() {
  try {
    // 使用 uni.onAppRoute 监听页面路由变化
    uni.onAppRoute((res) => {
      setTimeout(() => {
        saveCurrentPage()
      }, 100)
    })
  } catch (e) {
    // 忽略错误
  }
}

onLaunch(() => {
  console.log('寻侗三江 App Launch');

  // #ifdef MP-WEIXIN
  // 屏蔽冗余日志输出，避免无关报错干扰
  console.log = () => {};
  console.error = () => {};
  console.warn = () => {};
  // #endif
  
  // 开始监听页面变化
  watchPageChange()
});

onShow(() => {
  console.log('App Show');
  // 每次显示时保存当前页面
  saveCurrentPage()
  // 检查当前登录用户是否仍有效（未被禁用）
  // 延迟执行，避免阻塞页面跳转
  setTimeout(() => {
    const token = uni.getStorageSync('token');
    const userInfo = uni.getStorageSync('userInfo');
    if (token && userInfo && userInfo.isLogin) {
      userApi.getMe({ silent: true }).catch(() => {
        // getMe 返回 401（被禁用或过期）时，auth.js 的拦截器已处理跳转
      });
    }
  }, 500);
});

onHide(() => {
  console.log('App Hide');
  // 隐藏时也保存当前页面
  saveCurrentPage()
});

onPageNotFound(() => {
  console.log('页面未找到');
});
</script>

<style lang="scss">
/* ===== 全局主题色变量 ===== */
page {
  /* 主色调 - 清新浅蓝 */
  --dong-primary: #4A90E2;
  --dong-primary-light: #6BA5F0;
  --dong-primary-dark: #357ABD;

  /* 辅助色 - 深蓝 */
  --dong-secondary: #2C3E50;
  --dong-secondary-light: #3D5A73;
  --dong-secondary-dark: #1A252F;

  /* 点缀色 - 柔和橙色 */
  --dong-accent: #FF7F50;
  --dong-accent-light: #FFA07A;
  --dong-accent-dark: #E65C3C;

  /* 背景色系 */
  --dong-paper: #F8F9FA;
  --dong-bg: #F1F4F7;

  /* 文字色系 */
  --dong-text-primary: #1A2530;
  --dong-text-secondary: #4A5568;
  --dong-text-muted: #718096;

  /* 边框 */
  --dong-border: #D1D9E0;

  /* 阴影系统 */
  --dong-shadow-sm: 0 2rpx 10rpx rgba(44, 62, 80, 0.10);
  --dong-shadow-md: 0 4rpx 20rpx rgba(44, 62, 80, 0.14);
  --dong-shadow-lg: 0 8rpx 30rpx rgba(44, 62, 80, 0.18);
  --dong-shadow-primary: 0 4rpx 20rpx rgba(74, 144, 226, 0.22);
  --dong-shadow-secondary: 0 4rpx 20rpx rgba(44, 62, 80, 0.18);

  /* 圆角系统 */
  --dong-radius-sm: 16rpx;
  --dong-radius-md: 20rpx;
  --dong-radius-lg: 24rpx;
  --dong-radius-xl: 32rpx;
  --dong-radius-2xl: 40rpx;
  --dong-radius-full: 9999rpx;

  /* 基础样式 */
  background-color: var(--dong-bg);
  font-family: -apple-system, BlinkMacSystemFont, 'PingFang SC', 'Hiragino Sans GB', 'Microsoft YaHei', sans-serif;
  font-size: 28rpx;
  line-height: 1.7;
  color: var(--dong-text-primary);
  -webkit-font-smoothing: antialiased;
}

/* ===== 通用卡片阴影 ===== */
.card-shadow {
  background: var(--dong-paper);
  border-radius: var(--dong-radius-lg);
  box-shadow: var(--dong-shadow-sm);
  overflow: hidden;
  border: 1rpx solid rgba(74, 144, 226, 0.08);
}

/* ===== Section 标题 ===== */
.section-title {
  font-size: 32rpx;
  font-weight: 700;
  color: var(--dong-secondary);
  display: flex;
  align-items: center;
  gap: 12rpx;
}

.section-title::before {
  content: '';
  display: inline-block;
  width: 8rpx;
  height: 28rpx;
  background: var(--dong-primary);
  border-radius: 4rpx;
}

/* ===== 按钮样式 ===== */
.btn-primary {
  background: var(--dong-primary);
  color: #FFFFFF;
  border-radius: var(--dong-radius-full);
  padding: 24rpx 48rpx;
  font-size: 28rpx;
  font-weight: 600;
  text-align: center;
  border: none;
  box-shadow: var(--dong-shadow-primary);
  transition: all 0.3s ease;
}

.btn-primary:active {
  transform: translateY(2rpx);
  box-shadow: var(--dong-shadow-sm);
}

.btn-accent {
  background: var(--dong-accent);
  color: #FFFFFF;
  border-radius: var(--dong-radius-full);
  padding: 24rpx 48rpx;
  font-size: 28rpx;
  font-weight: 600;
  text-align: center;
  border: none;
  box-shadow: 0 4rpx 16rpx rgba(255, 127, 80, 0.3);
  transition: all 0.3s ease;
}

.btn-accent:active {
  transform: translateY(2rpx);
}

/* ===== 标签样式 ===== */
.tag {
  display: inline-block;
  padding: 6rpx 16rpx;
  border-radius: 24rpx;
  font-size: 22rpx;
  background: rgba(74, 144, 226, 0.10);
  color: var(--dong-primary);
  font-weight: 500;
}

.tag-primary {
  background: rgba(74, 144, 226, 0.10);
  color: var(--dong-primary);
}

.tag-secondary {
  background: rgba(44, 62, 80, 0.08);
  color: var(--dong-secondary);
}

.tag-accent {
  background: rgba(255, 127, 80, 0.12);
  color: var(--dong-accent);
}

/* ===== 文字工具类 ===== */
.text-primary { color: var(--dong-text-primary); }
.text-secondary { color: var(--dong-text-secondary); }
.text-muted { color: var(--dong-text-muted); }
.text-primary-color { color: var(--dong-primary); }
.text-secondary-color { color: var(--dong-secondary); }
.text-accent-color { color: var(--dong-accent); }

/* ===== 加载状态 ===== */
.loading-container {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 60rpx 0;
  color: var(--dong-text-muted);
  font-size: 26rpx;
}

/* ===== 空状态 ===== */
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 100rpx 40rpx;
  color: var(--dong-text-muted);
}

.empty-icon {
  font-size: 100rpx;
  margin-bottom: 20rpx;
  opacity: 0.5;
}

.empty-text {
  font-size: 28rpx;
  color: var(--dong-text-muted);
}

/* ===== 动画 ===== */
@keyframes fadeIn {
  from { opacity: 0; transform: translateY(20rpx); }
  to { opacity: 1; transform: translateY(0); }
}

.animate-fade-in {
  animation: fadeIn 0.4s ease-out;
}

/* ===== 安全区域 ===== */
.safe-area-bottom {
  padding-bottom: constant(safe-area-inset-bottom);
  padding-bottom: env(safe-area-inset-bottom);
}

/* ===== 滚动条隐藏 ===== */
.hide-scrollbar::-webkit-scrollbar {
  display: none;
  width: 0;
  height: 0;
}
</style>
