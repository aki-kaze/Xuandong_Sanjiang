<template>
  <div class="login-container" ref="loginContainer">
    <!-- Canvas 粒子背景 -->
    <canvas ref="particleCanvas" class="particle-canvas"></canvas>

    <!-- 动态背景 -->
    <div class="bg-effects">
      <div class="bg-circle circle-1"></div>
      <div class="bg-circle circle-2"></div>
      <div class="bg-circle circle-3"></div>
      <div class="bg-circle circle-4"></div>
      <div class="bg-gradient"></div>
    </div>

    <!-- 装饰性图案 -->
    <div class="decorations">
      <div class="deco-card deco-1">
        <span class="deco-icon">🏔️</span>
        <span class="deco-text">程阳古镇</span>
      </div>
      <div class="deco-card deco-2">
        <span class="deco-icon">🎭</span>
        <span class="deco-text">侗族大歌</span>
      </div>
      <div class="deco-card deco-3">
        <span class="deco-icon">🌉</span>
        <span class="deco-text">程阳风雨桥</span>
      </div>
    </div>

    <!-- 登录注册卡片 -->
    <div class="login-card" ref="loginCard">
      <div class="card-inner">
        <!-- 左侧品牌区 -->
        <div class="brand-section">
          <div class="brand-content">
            <div class="brand-logo">
              <span class="logo-text">侗</span>
              <div class="logo-glow"></div>
              <div class="logo-rings">
                <div class="ring ring-1"></div>
                <div class="ring ring-2"></div>
              </div>
            </div>
            <h1 class="brand-title">
              <span v-for="(char, idx) in brandTitleChars" :key="idx" class="typing-char" :style="{ animationDelay: (idx * 0.15) + 's' }">{{ char }}</span>
            </h1>
            <p class="brand-subtitle">旅游管理平台</p>
            <div class="brand-desc">
              探索三江之美<br/>
              感受侗族风情
            </div>
            <div class="brand-features">
              <div class="feature-viewport">
                <div class="feature-scroll">
                  <div class="feature-item" v-for="(feat, idx) in allFeatures" :key="idx">
                    <span>{{ feat }}</span>
                  </div>
                  <!-- 复制一份用于无缝衔接 -->
                  <div class="feature-item" v-for="(feat, idx) in allFeatures" :key="'dup-' + idx">
                    <span>{{ feat }}</span>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- 右侧表单区 -->
        <div class="form-section">
          <div class="form-header">
            <h2>{{ isLogin ? '欢迎回来' : '创建账号' }}</h2>
            <p>{{ isLogin ? '请登录您的账号继续管理' : '注册新账号开始使用平台' }}</p>
          </div>

          <!-- 切换按钮 -->
          <div class="mode-switch">
            <div class="switch-bg" :class="{ right: !isLogin }"></div>
            <button
              class="switch-btn"
              :class="{ active: isLogin }"
              @click="isLogin = true"
              type="button"
            >
              登录
            </button>
            <button
              class="switch-btn"
              :class="{ active: !isLogin }"
              @click="isLogin = false"
              type="button"
            >
              注册
            </button>
          </div>

          <!-- 登录表单 -->
          <Transition name="form-fade" mode="out-in">
            <el-form
              v-if="isLogin"
              key="login"
              ref="loginFormRef"
              :model="loginForm"
              :rules="loginRules"
              class="login-form"
              @submit.prevent="handleLogin"
            >
              <el-form-item prop="username" class="form-item-animate">
                <el-input
                  v-model="loginForm.username"
                  placeholder="请输入管理员账号"
                  size="large"
                  :prefix-icon="User"
                />
              </el-form-item>

              <el-form-item prop="password" class="form-item-animate" style="animation-delay: 0.1s">
                <el-input
                  v-model="loginForm.password"
                  type="password"
                  placeholder="请输入密码"
                  size="large"
                  show-password
                  :prefix-icon="Lock"
                  @keyup.enter="handleLogin"
                />
              </el-form-item>

              <el-form-item class="form-item-animate" style="animation-delay: 0.2s">
                <div class="button-row">
                  <el-button
                    type="primary"
                    size="large"
                    :loading="loading"
                    class="login-btn"
                    @click="handleLogin"
                  >
                    <span v-if="!loading">登 录</span>
                    <span v-else>登录中...</span>
                  </el-button>
                </div>
              </el-form-item>
            </el-form>

            <!-- 注册表单 -->
            <el-form
              v-else
              key="register"
              ref="registerFormRef"
              :model="registerForm"
              :rules="registerRules"
              class="login-form"
              @submit.prevent="handleRegister"
            >
              <el-form-item prop="username" class="form-item-animate">
                <el-input
                  v-model="registerForm.username"
                  placeholder="请输入用户名"
                  size="large"
                  :prefix-icon="User"
                />
              </el-form-item>

              <el-form-item prop="password" class="form-item-animate" style="animation-delay: 0.1s">
                <el-input
                  v-model="registerForm.password"
                  type="password"
                  placeholder="请输入密码"
                  size="large"
                  show-password
                  :prefix-icon="Lock"
                />
              </el-form-item>

              <el-form-item prop="confirmPassword" class="form-item-animate" style="animation-delay: 0.2s">
                <el-input
                  v-model="registerForm.confirmPassword"
                  type="password"
                  placeholder="请确认密码"
                  size="large"
                  show-password
                  :prefix-icon="Lock"
                />
              </el-form-item>

              <el-form-item prop="nickname" class="form-item-animate" style="animation-delay: 0.3s">
                <el-input
                  v-model="registerForm.nickname"
                  placeholder="请输入昵称（可选）"
                  size="large"
                  :prefix-icon="UserFilled"
                />
              </el-form-item>

              <el-form-item class="form-item-animate" style="animation-delay: 0.4s">
                <div class="button-row">
                  <el-button
                    type="primary"
                    size="large"
                    :loading="loading"
                    class="login-btn"
                    @click="handleRegister"
                  >
                    <span v-if="!loading">注 册</span>
                    <span v-else>注册中...</span>
                  </el-button>
                </div>
              </el-form-item>
            </el-form>
          </Transition>

          <p class="hint" v-if="isLogin">
            默认账号：admin / 密码：admin123
          </p>
        </div>
      </div>
    </div>

    <!-- 底部版权 -->
    <div class="footer">
      <p>© 2026 寻侗三江旅游管理平台</p>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { User, Lock, UserFilled } from '@element-plus/icons-vue'
import { useAuthStore } from '@/stores/auth'
import { authApi } from '@/api'

const router = useRouter()
const authStore = useAuthStore()
const loginFormRef = ref()
const registerFormRef = ref()
const loading = ref(false)
const isLogin = ref(true)
const loginContainer = ref()
const loginCard = ref()
const particleCanvas = ref()

// 品牌标题打字效果
const brandTitleChars = '寻侗三江'.split('')

// 特色功能卡片 - 6个四字卡片，电影字幕滚动
const allFeatures = ['景点导览', '酒店预订', '美食推荐', '特产购买', '智能助手', '路线规划']

// 粒子系统
let particles = []
let animationId = null

function initParticles() {
  const canvas = particleCanvas.value
  if (!canvas) return
  const ctx = canvas.getContext('2d')

  function resize() {
    canvas.width = window.innerWidth
    canvas.height = window.innerHeight
  }
  resize()
  window.addEventListener('resize', resize)

  // 创建粒子
  const colors = ['rgba(163,59,50,0.15)', 'rgba(29,59,90,0.12)', 'rgba(212,163,115,0.1)']
  for (let i = 0; i < 50; i++) {
    particles.push({
      x: Math.random() * canvas.width,
      y: Math.random() * canvas.height,
      radius: Math.random() * 3 + 1,
      vx: (Math.random() - 0.5) * 0.5,
      vy: (Math.random() - 0.5) * 0.5,
      color: colors[Math.floor(Math.random() * colors.length)]
    })
  }

  function animate() {
    ctx.clearRect(0, 0, canvas.width, canvas.height)

    particles.forEach(p => {
      p.x += p.vx
      p.y += p.vy

      if (p.x < 0 || p.x > canvas.width) p.vx *= -1
      if (p.y < 0 || p.y > canvas.height) p.vy *= -1

      ctx.beginPath()
      ctx.arc(p.x, p.y, p.radius, 0, Math.PI * 2)
      ctx.fillStyle = p.color
      ctx.fill()
    })

    // 连线
    for (let i = 0; i < particles.length; i++) {
      for (let j = i + 1; j < particles.length; j++) {
        const dx = particles[i].x - particles[j].x
        const dy = particles[i].y - particles[j].y
        const dist = Math.sqrt(dx * dx + dy * dy)
        if (dist < 120) {
          ctx.beginPath()
          ctx.moveTo(particles[i].x, particles[i].y)
          ctx.lineTo(particles[j].x, particles[j].y)
          ctx.strokeStyle = `rgba(163,59,50,${0.06 * (1 - dist / 120)})`
          ctx.lineWidth = 0.5
          ctx.stroke()
        }
      }
    }

    animationId = requestAnimationFrame(animate)
  }
  animate()
}

const loginForm = reactive({
  username: '',
  password: ''
})

const registerForm = reactive({
  username: '',
  password: '',
  confirmPassword: '',
  nickname: ''
})

const validateConfirmPassword = (rule, value, callback) => {
  if (value === '') {
    callback(new Error('请再次输入密码'))
  } else if (value !== registerForm.password) {
    callback(new Error('两次输入密码不一致'))
  } else {
    callback()
  }
}

const loginRules = {
  username: [{ required: true, message: '请输入账号', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

const registerRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '用户名长度在 3 到 20 个字符', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度在 6 到 20 个字符', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, validator: validateConfirmPassword, trigger: 'blur' }
  ]
}

async function handleLogin() {
  await loginFormRef.value.validate()
  loading.value = true
  try {
    await authStore.login(loginForm)
    ElMessage.success('登录成功')
    router.push('/dashboard')
  } catch (error) {
    ElMessage.error(error.message || '登录失败')
  } finally {
    loading.value = false
  }
}

async function handleRegister() {
  await registerFormRef.value.validate()
  loading.value = true
  try {
    await authApi.register({
      username: registerForm.username,
      password: registerForm.password,
      nickname: registerForm.nickname || registerForm.username
    })
    ElMessage.success('注册成功，请登录')
    isLogin.value = true
    registerForm.username = ''
    registerForm.password = ''
    registerForm.confirmPassword = ''
    registerForm.nickname = ''
  } catch (error) {
    ElMessage.error(error.message || '注册失败')
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  initParticles()
})

onUnmounted(() => {
  if (animationId) {
    cancelAnimationFrame(animationId)
    animationId = null
  }
})
</script>

<style scoped lang="scss">
// ==================== 变量定义 ====================
$primary-color: #A33B32;
$primary-dark: #8c2f27;
$primary-light: #c0504a;
$blue-color: #1D3B5A;
$text-dark: #1D3B5A;
$text-light: #666;
$text-muted: #999;

// ==================== 容器样式 ====================
.login-container {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #f8f9fa 0%, #e9ecef 50%, #dee2e6 100%);
  position: relative;
  overflow: hidden;
  padding: 20px;
}

// ==================== 粒子画布 ====================
.particle-canvas {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  pointer-events: none;
  z-index: 1;
}

// ==================== 背景特效 ====================
.bg-effects {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  overflow: hidden;
  pointer-events: none;
}

.bg-gradient {
  position: absolute;
  top: -50%;
  right: -30%;
  width: 80%;
  height: 150%;
  background: linear-gradient(
    135deg,
    rgba($primary-color, 0.08) 0%,
    rgba($primary-light, 0.05) 30%,
    rgba($blue-color, 0.03) 60%,
    transparent 100%
  );
  border-radius: 50%;
  animation: float 20s ease-in-out infinite;
}

.bg-circle {
  position: absolute;
  border-radius: 50%;
  opacity: 0.6;

  &.circle-1 {
    width: 400px;
    height: 400px;
    background: radial-gradient(circle, rgba($primary-color, 0.1), transparent 70%);
    top: -100px;
    right: -100px;
    animation: float 15s ease-in-out infinite;
  }

  &.circle-2 {
    width: 300px;
    height: 300px;
    background: radial-gradient(circle, rgba($blue-color, 0.08), transparent 70%);
    bottom: -50px;
    left: -50px;
    animation: float 18s ease-in-out infinite reverse;
  }

  &.circle-3 {
    width: 200px;
    height: 200px;
    background: radial-gradient(circle, rgba($primary-light, 0.12), transparent 70%);
    top: 50%;
    left: 10%;
    animation: float 12s ease-in-out infinite;
  }

  &.circle-4 {
    width: 150px;
    height: 150px;
    background: radial-gradient(circle, rgba($blue-color, 0.1), transparent 70%);
    bottom: 20%;
    right: 15%;
    animation: float 10s ease-in-out infinite reverse;
  }
}

@keyframes float {
  0%, 100% {
    transform: translate(0, 0) rotate(0deg);
  }
  25% {
    transform: translate(20px, -30px) rotate(5deg);
  }
  50% {
    transform: translate(-10px, 20px) rotate(-3deg);
  }
  75% {
    transform: translate(30px, 10px) rotate(3deg);
  }
}

// ==================== 装饰卡片 ====================
.decorations {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  pointer-events: none;
  overflow: hidden;
  z-index: 2;
}

.deco-card {
  position: absolute;
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px 20px;
  background: rgba(255, 255, 255, 0.85);
  backdrop-filter: blur(12px);
  -webkit-backdrop-filter: blur(12px);
  border-radius: 50px;
  box-shadow: 0 4px 24px rgba(0, 0, 0, 0.08);
  border: 1px solid rgba(255, 255, 255, 0.6);
  opacity: 0.8;

  .deco-icon {
    font-size: 20px;
  }

  .deco-text {
    font-size: 13px;
    color: $text-dark;
    font-weight: 500;
    white-space: nowrap;
  }

  &.deco-1 {
    top: 15%;
    left: 5%;
  }

  &.deco-2 {
    top: 25%;
    right: 8%;
  }

  &.deco-3 {
    bottom: 25%;
    left: 8%;
  }
}

// ==================== 登录卡片 ====================
.login-card {
  width: 100%;
  max-width: 900px;
  position: relative;
  z-index: 10;
  animation: cardAppear 0.8s ease-out;
}

@keyframes cardAppear {
  0% {
    opacity: 0;
    transform: translateY(30px) scale(0.98);
  }
  100% {
    opacity: 1;
    transform: translateY(0) scale(1);
  }
}

.card-inner {
  display: flex;
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  border-radius: 24px;
  box-shadow:
    0 25px 50px rgba(0, 0, 0, 0.1),
    0 10px 20px rgba(0, 0, 0, 0.05),
    inset 0 1px 0 rgba(255, 255, 255, 0.8);
  overflow: hidden;
  border: 1px solid rgba(255, 255, 255, 0.8);
}

// ==================== 左侧品牌区 ====================
.brand-section {
  flex: 1;
  background: linear-gradient(
    135deg,
    $primary-color 0%,
    $primary-dark 50%,
    $primary-dark 100%
  );
  padding: 48px 40px;
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  overflow: hidden;

  &::before {
    content: '';
    position: absolute;
    top: -50%;
    left: -50%;
    width: 200%;
    height: 200%;
    background: radial-gradient(
      circle,
      rgba(255, 255, 255, 0.1) 0%,
      transparent 50%
    );
    animation: brandGlow 8s ease-in-out infinite;
  }

  &::after {
    content: '';
    position: absolute;
    bottom: 0;
    left: 0;
    right: 0;
    height: 40%;
    background: linear-gradient(
      to top,
      rgba(0, 0, 0, 0.2),
      transparent
    );
  }
}

@keyframes brandGlow {
  0%, 100% {
    transform: translate(0, 0);
  }
  50% {
    transform: translate(10%, 10%);
  }
}

.brand-content {
  position: relative;
  z-index: 1;
  text-align: center;
  color: white;
}

.brand-logo {
  width: 80px;
  height: 80px;
  margin: 0 auto 24px;
  position: relative;

  .logo-text {
    display: flex;
    align-items: center;
    justify-content: center;
    width: 100%;
    height: 100%;
    background: rgba(255, 255, 255, 0.2);
    border-radius: 20px;
    font-size: 40px;
    font-weight: bold;
    backdrop-filter: blur(10px);
    border: 2px solid rgba(255, 255, 255, 0.3);
    position: relative;
    z-index: 1;
  }

  .logo-glow {
    position: absolute;
    top: 50%;
    left: 50%;
    transform: translate(-50%, -50%);
    width: 120px;
    height: 120px;
    background: radial-gradient(
      circle,
      rgba(255, 255, 255, 0.3),
      transparent 60%
    );
    animation: pulse 2s ease-in-out infinite;
  }

  .logo-rings {
    position: absolute;
    top: 50%;
    left: 50%;
    transform: translate(-50%, -50%);
    width: 100%;
    height: 100%;

    .ring {
      position: absolute;
      top: 50%;
      left: 50%;
      transform: translate(-50%, -50%);
      border: 1px solid rgba(255, 255, 255, 0.15);
      border-radius: 50%;
      animation: ringExpand 3s ease-out infinite;

      &.ring-1 {
        animation-delay: 0s;
      }

      &.ring-2 {
        animation-delay: 1.5s;
      }
    }
  }
}

@keyframes pulse {
  0%, 100% {
    transform: translate(-50%, -50%) scale(1);
    opacity: 0.5;
  }
  50% {
    transform: translate(-50%, -50%) scale(1.2);
    opacity: 0.8;
  }
}

@keyframes ringExpand {
  0% {
    width: 80px;
    height: 80px;
    opacity: 0.4;
  }
  100% {
    width: 160px;
    height: 160px;
    opacity: 0;
  }
}

// 打字效果
.typing-char {
  display: inline-block;
  opacity: 0;
  animation: typingChar 0.3s ease-out forwards;
}

@keyframes typingChar {
  from {
    opacity: 0;
    transform: translateY(8px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.brand-title {
  font-size: 32px;
  font-weight: 700;
  margin: 0 0 8px;
  text-shadow: 0 2px 10px rgba(0, 0, 0, 0.2);
  letter-spacing: 2px;
}

.brand-subtitle {
  font-size: 14px;
  opacity: 0.9;
  margin: 0 0 32px;
  letter-spacing: 4px;
}

.brand-desc {
  font-size: 16px;
  line-height: 1.8;
  margin-bottom: 32px;
  opacity: 0.9;
}

.brand-features {
  .feature-viewport {
    overflow: hidden;
    height: 88px;
  }

  .feature-scroll {
    display: flex;
    flex-direction: column;
    gap: 10px;
    animation: scrollUp 15s linear infinite;
  }

  .feature-item {
    display: flex;
    align-items: center;
    justify-content: center;
    padding: 10px 20px;
    background: rgba(255, 255, 255, 0.15);
    border-radius: 10px;
    backdrop-filter: blur(10px);
    border: 1px solid rgba(255, 255, 255, 0.1);
    transition: all 0.3s ease;
    flex-shrink: 0;

    span {
      font-size: 14px;
      font-weight: 500;
    }
  }
}

@keyframes scrollUp {
  0% {
    transform: translateY(0);
  }
  100% {
    transform: translateY(calc(-50% - 5px)); /* -50% covers first set; -5px accounts for half the gap */
  }
}

// ==================== 右侧表单区 ====================
.form-section {
  flex: 1;
  padding: 48px 40px;
  display: flex;
  flex-direction: column;
  justify-content: center;
  min-width: 0;
}

.form-header {
  text-align: center;
  margin-bottom: 32px;

  h2 {
    font-size: 26px;
    font-weight: 700;
    color: $text-dark;
    margin: 0 0 8px;
  }

  p {
    font-size: 14px;
    color: $text-muted;
    margin: 0;
  }
}

// ==================== 切换按钮 ====================
.mode-switch {
  display: flex;
  position: relative;
  background: #f5f5f5;
  border-radius: 12px;
  padding: 4px;
  margin-bottom: 32px;
  overflow: hidden;

  .switch-bg {
    position: absolute;
    top: 4px;
    left: 4px;
    width: calc(50% - 4px);
    height: calc(100% - 8px);
    background: linear-gradient(135deg, $primary-color, $primary-light);
    border-radius: 8px;
    transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
    box-shadow: 0 2px 8px rgba($primary-color, 0.3);

    &.right {
      transform: translateX(100%);
    }
  }

  .switch-btn {
    flex: 1;
    padding: 12px 20px;
    border: none;
    background: transparent;
    font-size: 15px;
    font-weight: 500;
    color: $text-light;
    cursor: pointer;
    position: relative;
    z-index: 1;
    transition: color 0.3s ease;

    &.active {
      color: white;
    }

    &:hover:not(.active) {
      color: $text-dark;
    }
  }
}

// ==================== 表单样式 ====================
.form-item-animate {
  animation: itemSlideIn 0.5s ease-out backwards;
}

@keyframes itemSlideIn {
  0% {
    opacity: 0;
    transform: translateX(-20px);
  }
  100% {
    opacity: 1;
    transform: translateX(0);
  }
}

.login-form {
  margin-bottom: 0;

  :deep(.el-input__wrapper) {
    padding: 4px 12px;
    border-radius: 10px;
    background: #f8f9fa;
    border: 2px solid transparent;
    transition: all 0.3s ease;
    box-shadow: none;

    &:hover {
      background: #f0f1f3;
    }

    &.is-focus {
      background: white;
      border-color: $primary-color;
      box-shadow: 0 0 0 3px rgba($primary-color, 0.08);
    }
  }

  :deep(.el-input__prefix) {
    color: $text-muted;
    transition: color 0.3s;

    &:hover {
      color: $primary-color;
    }
  }
}

.login-btn {
  width: 100%;
  height: 52px;
  border-radius: 12px;
  background: linear-gradient(135deg, $primary-color 0%, $primary-dark 100%) !important;
  border: none;
  font-size: 16px;
  font-weight: 600;
  letter-spacing: 4px;
  color: white;
  transition: all 0.3s ease;
  position: relative;
  overflow: hidden;

  &::before {
    content: '';
    position: absolute;
    top: 0;
    left: -100%;
    width: 100%;
    height: 100%;
    background: linear-gradient(
      90deg,
      transparent,
      rgba(255, 255, 255, 0.2),
      transparent
    );
    transition: left 0.5s ease;
  }

  &:hover {
    transform: translateY(-2px);
    box-shadow: 0 8px 25px rgba($primary-color, 0.35);

    &::before {
      left: 100%;
    }
  }

  &:active {
    transform: translateY(0);
  }
}

// ==================== 表单过渡动画 ====================
.form-fade-enter-active,
.form-fade-leave-active {
  transition: all 0.3s ease;
}

.form-fade-enter-from {
  opacity: 0;
  transform: translateX(20px);
}

.form-fade-leave-to {
  opacity: 0;
  transform: translateX(-20px);
}

// ==================== 提示文字 ====================
.hint {
  text-align: center;
  color: $text-muted;
  font-size: 13px;
  margin-top: 24px;
}

// ==================== 底部版权 ====================
.footer {
  position: absolute;
  bottom: 20px;
  left: 0;
  right: 0;
  text-align: center;

  p {
    color: $text-muted;
    font-size: 12px;
    margin: 0;
  }
}

// ==================== 响应式设计 ====================
@media screen and (max-width: 900px) {
  .card-inner {
    flex-direction: column;
    max-width: 480px;
  }

  .brand-section {
    padding: 32px 24px;

    .brand-logo {
      width: 60px;
      height: 60px;
      margin-bottom: 16px;

      .logo-text {
        font-size: 28px;
        border-radius: 14px;
      }
    }

    .brand-title {
      font-size: 24px;
    }

    .brand-subtitle {
      font-size: 12px;
      margin-bottom: 20px;
    }

    .brand-desc {
      font-size: 14px;
      margin-bottom: 20px;
    }

    .brand-features {
      .feature-viewport {
        height: 72px;
      }

      .feature-item {
        padding: 8px 14px;

        span {
          font-size: 12px;
        }
      }
    }
  }

  .form-section {
    padding: 32px 24px;
  }

  .deco-card {
    display: none;
  }
}

@media screen and (max-width: 640px) {
  .login-container {
    padding: 16px;
    align-items: flex-start;
    padding-top: 40px;
  }

  .login-card {
    margin-top: 0;
  }

  .card-inner {
    border-radius: 20px;
  }

  .brand-section {
    padding: 24px 20px;

    .brand-logo {
      width: 50px;
      height: 50px;
      margin-bottom: 12px;

      .logo-text {
        font-size: 24px;
        border-radius: 12px;
      }

      .logo-glow {
        width: 80px;
        height: 80px;
      }
    }

    .brand-title {
      font-size: 22px;
    }

    .brand-subtitle {
      font-size: 11px;
      margin-bottom: 16px;
    }

    .brand-desc {
      font-size: 13px;
      margin-bottom: 16px;
    }
  }

  .form-section {
    padding: 28px 20px;
  }

  .form-header {
    margin-bottom: 24px;

    h2 {
      font-size: 22px;
    }

    p {
      font-size: 13px;
    }
  }

  .mode-switch {
    margin-bottom: 24px;
  }

  .login-btn {
    height: 48px;
    font-size: 15px;
  }

  .footer {
    display: none;
  }
}

@media screen and (max-width: 480px) {
  .login-container {
    padding: 12px;
    padding-top: 20px;
  }

  .brand-section {
    padding: 20px 16px;
  }

  .form-section {
    padding: 24px 16px;
  }

  .form-header {
    h2 {
      font-size: 20px;
    }
  }
}

@media screen and (max-height: 600px) and (orientation: landscape) {
  .login-container {
    align-items: center;
    padding-top: 20px;
  }

  .login-card {
    max-width: 700px;
  }

  .card-inner {
    flex-direction: row;
  }

  .brand-section {
    padding: 24px;

    .brand-logo {
      width: 50px;
      height: 50px;
      margin-bottom: 12px;

      .logo-text {
        font-size: 24px;
      }
    }

    .brand-title {
      font-size: 20px;
    }

    .brand-subtitle {
      display: none;
    }

    .brand-desc {
      display: none;
    }

    .brand-features {
      .feature-viewport {
        height: 80px;
      }

      .feature-item {
        padding: 6px 10px;

        span {
          font-size: 11px;
        }
      }
    }
  }

  .form-section {
    padding: 24px;
  }

  .form-header {
    margin-bottom: 16px;

    h2 {
      font-size: 18px;
    }

    p {
      font-size: 12px;
    }
  }

  .mode-switch {
    margin-bottom: 16px;
  }
}
</style>
