<template>
  <div class="skeleton-card glass-card">
    <div class="skeleton-content">
      <div class="skeleton-icon shimmer"></div>
      <div class="skeleton-info">
        <div class="skeleton-value shimmer"></div>
        <div class="skeleton-label shimmer"></div>
      </div>
    </div>
  </div>
</template>

<script setup>
</script>

<style lang="scss" scoped>
.skeleton-card {
  padding: 24px;
  min-height: 100px;
}

.skeleton-content {
  display: flex;
  align-items: center;
  gap: 16px;
}

.skeleton-icon {
  width: 56px;
  height: 56px;
  border-radius: 14px;
  background: #f0f0f0;
  flex-shrink: 0;
}

.skeleton-info {
  flex: 1;
}

.skeleton-value {
  width: 80px;
  height: 28px;
  border-radius: 6px;
  background: #f0f0f0;
  margin-bottom: 8px;
}

.skeleton-label {
  width: 60px;
  height: 14px;
  border-radius: 4px;
  background: #f0f0f0;
}

.shimmer {
  position: relative;
  overflow: hidden;

  &::after {
    content: '';
    position: absolute;
    top: 0;
    left: -100%;
    width: 100%;
    height: 100%;
    background: linear-gradient(
      90deg,
      transparent,
      rgba(255, 255, 255, 0.5),
      transparent
    );
    animation: shimmer 1.5s infinite;
  }
}

@keyframes shimmer {
  0% { left: -100%; }
  100% { left: 100%; }
}
</style>