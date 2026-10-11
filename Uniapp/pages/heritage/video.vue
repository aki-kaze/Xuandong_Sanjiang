<template>
  <view class="video-page">
    <video
      class="video-player"
      :src="videoUrl"
      :poster="posterUrl"
      autoplay
      controls
      object-fit="contain"
      @error="onVideoError"
      @loadedmetadata="onVideoLoaded"
    ></video>
    <view v-if="loading" class="loading-container">
      <view class="loading-spinner"></view>
      <text class="loading-text">视频加载中...</text>
    </view>
  </view>
</template>

<script>
export default {
  data() {
    return {
      videoUrl: '',
      posterUrl: '',
      loading: true
    }
  },
  onLoad(options) {
    if (options.url) {
      let url = decodeURIComponent(options.url)
      // 如果videoUrl是CodeBookCore播放器页面，提取其中的实际视频URL
      const mp4Match = url.match(/[?&]url=([^&]+)/)
      if (mp4Match) {
        url = decodeURIComponent(mp4Match[1])
      }
      this.videoUrl = url
    }
    if (options.poster) {
      this.posterUrl = decodeURIComponent(options.poster)
    }
  },
  methods: {
    onVideoLoaded() {
      this.loading = false
    },
    onVideoError(e) {
      this.loading = false
      uni.showToast({
        title: '视频加载失败',
        icon: 'none',
        duration: 2000
      })
    }
  }
}
</script>

<style scoped lang="scss">
.video-page {
  width: 100%;
  height: 100vh;
  position: relative;
  background-color: #000;
  display: flex;
  align-items: center;
  justify-content: center;
}

.video-player {
  width: 100%;
  height: 100%;
}

.loading-container {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  background-color: #000;
  z-index: 999;
}

.loading-spinner {
  width: 50px;
  height: 50px;
  border: 4px solid rgba(255, 255, 255, 0.2);
  border-top-color: #fff;
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

.loading-text {
  margin-top: 20px;
  color: #fff;
  font-size: 14px;
}
</style>
