<template>
	<view class="audio-container">
		<!-- 当前播放 -->
		<view class="player-card">
			<image class="cover" :src="currentAudio.cover" mode="aspectFill"></image>
			<view class="player-info">
				<text class="title">{{ currentAudio.title }}</text>
				<text class="subtitle">{{ currentAudio.subtitle }}</text>
				
				<!-- 进度条 -->
				<view class="progress-section">
					<text class="time">{{ formatTime(currentTime) }}</text>
					<view class="progress-bar">
						<view class="progress" :style="{width: progressPercent + '%'}"></view>
					</view>
					<text class="time">{{ formatTime(duration) }}</text>
				</view>
				
				<!-- 播放控制 -->
				<view class="controls">
					<view class="control-btn" @click="previous">
						<uv-icon name="skip-back-left" size="28" color="#333"></uv-icon>
					</view>
					<view class="play-btn" @click="togglePlay">
						<uv-icon :name="isPlaying ? 'pause-circle-fill' : 'play-circle-fill'" size="52" color="#D4A373"></uv-icon>
					</view>
					<view class="control-btn" @click="next">
						<uv-icon name="skip-forward-right" size="28" color="#333"></uv-icon>
					</view>
				</view>
			</view>
		</view>
		
		<!-- 播放列表 -->
		<view class="playlist-section">
			<view class="section-header">
				<text class="title">播放列表</text>
				<text class="count">共{{ audioList.length }}个讲解点</text>
			</view>
			
			<view class="playlist">
				<view class="audio-item" :class="{active: currentIndex === index}" v-for="(item, index) in audioList" :key="index" @click="selectAudio(index)">
					<view class="item-left">
						<text class="number">{{ index + 1 }}</text>
						<image class="thumb" :src="item.cover" mode="aspectFill"></image>
						<view class="info">
							<text class="name">{{ item.title }}</text>
							<text class="desc">{{ item.subtitle }}</text>
						</view>
					</view>
					<view class="item-right">
						<text class="duration">{{ formatTime(item.duration) }}</text>
						<uv-icon v-if="currentIndex === index && isPlaying" name="volume-fill" size="18" color="#D4A373"></uv-icon>
					</view>
				</view>
			</view>
		</view>
	</view>
</template>

<script setup>
import { ref, computed } from 'vue'

const currentIndex = ref(0)
const isPlaying = ref(false)
const currentTime = ref(0)
const duration = ref(180)

const audioList = ref([
	{
		title: '景区概况',
		subtitle: '景区的整体介绍和历史背景',
		cover: 'https://www.xiaohongshu.com/explore/69bbae850000000023016677?xsec_token=YB2YHCgEKuzR0gi-xFW3BWeO1a90D4eQCUg-evflNpD1g%3D&xsec_source=pc_creatormng',
		duration: 180
	},
	{
		title: '主要建筑',
		subtitle: '核心建筑群的详细讲解',
		cover: 'https://www.xiaohongshu.com/explore/69bbae850000000023016677?xsec_token=YB2YHCgEKuzR0gi-xFW3BWeO1a90D4eQCUg-evflNpD1g%3D&xsec_source=pc_creatormng',
		duration: 240
	},
	{
		title: '文化故事',
		subtitle: '与景点相关的历史典故',
		cover: 'https://www.xiaohongshu.com/explore/69bbae850000000023016677?xsec_token=YB2YHCgEKuzR0gi-xFW3BWeO1a90D4eQCUg-evflNpD1g%3D&xsec_source=pc_creatormng',
		duration: 200
	},
	{
		title: '建筑艺术',
		subtitle: '建筑风格和艺术特色',
		cover: 'https://www.xiaohongshu.com/explore/69bbae850000000023016677?xsec_token=YB2YHCgEKuzR0gi-xFW3BWeO1a90D4eQCUg-evflNpD1g%3D&xsec_source=pc_creatormng',
		duration: 220
	},
	{
		title: '游览建议',
		subtitle: '最佳游览路线和注意事项',
		cover: 'https://www.xiaohongshu.com/explore/69bbae850000000023016677?xsec_token=YB2YHCgEKuzR0gi-xFW3BWeO1a90D4eQCUg-evflNpD1g%3D&xsec_source=pc_creatormng',
		duration: 150
	}
])

const currentAudio = computed(() => {
	return audioList.value[currentIndex.value]
})

const progressPercent = computed(() => {
	return (currentTime.value / duration.value) * 100
})

const togglePlay = () => {
	isPlaying.value = !isPlaying.value
	if (isPlaying.value) {
		// 模拟播放进度
		const timer = setInterval(() => {
			if (currentTime.value < duration.value) {
				currentTime.value++
			} else {
				clearInterval(timer)
				isPlaying.value = false
				currentTime.value = 0
			}
		}, 1000)
	}
}

const previous = () => {
	if (currentIndex.value > 0) {
		currentIndex.value--
		currentTime.value = 0
		duration.value = audioList.value[currentIndex.value].duration
		uni.showToast({
			title: '上一个讲解点',
			icon: 'none'
		})
	}
}

const next = () => {
	if (currentIndex.value < audioList.value.length - 1) {
		currentIndex.value++
		currentTime.value = 0
		duration.value = audioList.value[currentIndex.value].duration
		uni.showToast({
			title: '下一个讲解点',
			icon: 'none'
		})
	}
}

const selectAudio = (index) => {
	currentIndex.value = index
	currentTime.value = 0
	duration.value = audioList.value[index].duration
	isPlaying.value = false
}

const formatTime = (seconds) => {
	const min = Math.floor(seconds / 60)
	const sec = seconds % 60
	return `${min.toString().padStart(2, '0')}:${sec.toString().padStart(2, '0')}`
}
</script>

<style lang="scss" scoped>
.audio-container {
	min-height: 100vh;
	background: #F8F9FA;
	padding-bottom: 30rpx;
}

.player-card {
	background: linear-gradient(135deg, #1D3B5A 0%, #2E5F8E 100%);
	padding: 40rpx 30rpx;
	
	.cover {
		width: 100%;
		height: 400rpx;
		border-radius: 20rpx;
		margin-bottom: 30rpx;
	}
	
	.player-info {
		.title {
			font-size: 36rpx;
			font-weight: bold;
			color: #FDFCF8;
			display: block;
			margin-bottom: 12rpx;
		}
		
		.subtitle {
			font-size: 26rpx;
			color: rgba(255, 255, 255, 0.9);
			display: block;
			margin-bottom: 40rpx;
		}
		
		.progress-section {
			display: flex;
			align-items: center;
			gap: 16rpx;
			margin-bottom: 40rpx;
			
			.time {
				font-size: 24rpx;
				color: rgba(255, 255, 255, 0.9);
			}
			
			.progress-bar {
				flex: 1;
				height: 6rpx;
				background: rgba(255, 255, 255, 0.3);
				border-radius: 6rpx;
				overflow: hidden;
				
				.progress {
					height: 100%;
					background: #FDFCF8;
					border-radius: 6rpx;
					transition: width 0.3s;
				}
			}
		}
		
		.controls {
			display: flex;
			justify-content: center;
			align-items: center;
			gap: 80rpx;
			
			.control-btn {
				width: 80rpx;
				height: 80rpx;
				background: rgba(255, 255, 255, 0.3);
				border-radius: 50%;
				display: flex;
				align-items: center;
				justify-content: center;
			}
			
			.play-btn {
				width: 100rpx;
				height: 100rpx;
				background: #FDFCF8;
				border-radius: 50%;
				display: flex;
				align-items: center;
				justify-content: center;
			}
		}
	}
}

.playlist-section {
	margin: 20rpx 30rpx;
	background: #FDFCF8;
	border-radius: 20rpx;
	padding: 30rpx;
	
	.section-header {
		display: flex;
		justify-content: space-between;
		align-items: center;
		margin-bottom: 24rpx;
		
		.title {
			font-size: 30rpx;
			font-weight: bold;
			color: #333;
		}
		
		.count {
			font-size: 24rpx;
			color: #999;
		}
	}
	
	.playlist {
		display: flex;
		flex-direction: column;
		gap: 20rpx;
		
		.audio-item {
			display: flex;
			justify-content: space-between;
			align-items: center;
			padding: 20rpx;
			border-radius: 12rpx;
			background: #F8F9FA;
			
			&.active {
				background: #E8F0F8;
			}
			
			.item-left {
				flex: 1;
				display: flex;
				align-items: center;
				gap: 16rpx;
				
				.number {
					width: 40rpx;
					font-size: 28rpx;
					font-weight: bold;
					color: #999;
				}
				
				.thumb {
					width: 80rpx;
					height: 80rpx;
					border-radius: 12rpx;
				}
				
				.info {
					flex: 1;
					display: flex;
					flex-direction: column;
					gap: 8rpx;
					
					.name {
						font-size: 28rpx;
						color: #333;
						font-weight: bold;
					}
					
					.desc {
						font-size: 24rpx;
						color: #999;
					}
				}
			}
			
			.item-right {
				display: flex;
				align-items: center;
				gap: 16rpx;
				
				.duration {
					font-size: 24rpx;
					color: #999;
				}
			}
		}
	}
}
</style>
