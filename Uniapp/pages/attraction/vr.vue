<template>
	<view class="vr-container">
		<!-- VR视图区域 -->
		<view class="vr-viewer">
			<image class="vr-image" :src="currentScene.image" mode="aspectFill"></image>
			<view class="vr-overlay">
				<text class="scene-name">{{ currentScene.name }}</text>
				<text class="scene-desc">{{ currentScene.desc }}</text>
			</view>
		</view>
		
		<!-- 控制按钮 -->
		<view class="control-bar">
			<view class="control-btn" @click="toggleFullscreen">
				<uv-icon name="grid" size="24" color="#fff"></uv-icon>
				<text>全屏</text>
			</view>
			<view class="control-btn" @click="resetView">
				<uv-icon name="reload" size="24" color="#fff"></uv-icon>
				<text>复位</text>
			</view>
			<view class="control-btn" @click="toggleGyro">
				<uv-icon name="scan" size="24" color="#fff"></uv-icon>
				<text>陀螺仪</text>
			</view>
		</view>
		
		<!-- 场景切换 -->
		<view class="scene-list">
			<view class="scene-title">选择场景</view>
			<view class="scenes">
				<view class="scene-item" :class="{active: currentSceneIndex === index}" v-for="(item, index) in scenes" :key="index" @click="switchScene(index)">
					<image class="thumb" :src="item.image" mode="aspectFill"></image>
					<text class="name">{{ item.name }}</text>
				</view>
			</view>
		</view>
		
		<!-- 功能说明 -->
		<view class="tips">
			<view class="tip-item">
				<uv-icon name="info-circle" size="16" color="#2E7D32"></uv-icon>
				<text>拖动屏幕可旋转视角</text>
			</view>
			<view class="tip-item">
				<uv-icon name="info-circle" size="16" color="#2E7D32"></uv-icon>
				<text>点击热点可查看详细信息</text>
			</view>
		</view>
	</view>
</template>

<script setup>
import { ref, computed } from 'vue'

const currentSceneIndex = ref(0)

const scenes = ref([
	{
		name: '景区大门',
		desc: '景区主入口，欢迎来到这里',
		image: 'https://www.xiaohongshu.com/explore/69bbae850000000023016677?xsec_token=YB2YHCgEKuzR0gi-xFW3BWeO1a90D4eQCUg-evflNpD1g%3D&xsec_source=pc_creatormng'
	},
	{
		name: '中心广场',
		desc: '景区的中心区域，各条游览路线的交汇点',
		image: 'https://www.xiaohongshu.com/explore/69bbae850000000023016677?xsec_token=YB2YHCgEKuzR0gi-xFW3BWeO1a90D4eQCUg-evflNpD1g%3D&xsec_source=pc_creatormng'
	},
	{
		name: '主要景点',
		desc: '最具代表性的景观区域',
		image: 'https://www.xiaohongshu.com/explore/69bbae850000000023016677?xsec_token=YB2YHCgEKuzR0gi-xFW3BWeO1a90D4eQCUg-evflNpD1g%3D&xsec_source=pc_creatormng'
	},
	{
		name: '观景平台',
		desc: '最佳观赏位置，可远眺全景点',	
		image: 'https://www.xiaohongshu.com/explore/69bbae850000000023016677?xsec_token=YB2YHCgEKuzR0gi-xFW3BWeO1a90D4eQCUg-evflNpD1g%3D&xsec_source=pc_creatormng'
	}
])

const currentScene = computed(() => {
	return scenes.value[currentSceneIndex.value]
})

const switchScene = (index) => {
	currentSceneIndex.value = index
	uni.showToast({
		title: `已切换到${scenes.value[index].name}`,
		icon: 'none'
	})
}

const toggleFullscreen = () => {
	uni.showToast({
		title: '全屏功能开发中',
		icon: 'none'
	})
}

const resetView = () => {
	uni.showToast({
		title: '视角已复位',	
		icon: 'none'
	})
}

const toggleGyro = () => {
	uni.showToast({
		title: '陀螺仪功能开发中',
		icon: 'none'
	})
}
</script>

<style lang="scss" scoped>
.vr-container {
	min-height: 100vh;
	background: #000;
}

.vr-viewer {
	position: relative;
	width: 100%;
	height: 60vh;
	
	.vr-image {
		width: 100%;
		height: 100%;
	}
	
	.vr-overlay {
		position: absolute;
		bottom: 0;
		left: 0;
		right: 0;
		background: linear-gradient(to top, rgba(0, 0, 0, 0.8), transparent);
		padding: 40rpx 30rpx 30rpx;
		display: flex;
		flex-direction: column;
		gap: 12rpx;
		
		.scene-name {
			font-size: 36rpx;
			font-weight: bold;
			color: #fff;
		}
		
		.scene-desc {
			font-size: 26rpx;
			color: rgba(255, 255, 255, 0.9);
		}
	}
}

.control-bar {
	background: rgba(0, 0, 0, 0.5);
	padding: 30rpx;
	display: flex;
	justify-content: space-around;
	
	.control-btn {
		display: flex;
		flex-direction: column;
		align-items: center;
		gap: 12rpx;
		color: #fff;
		font-size: 24rpx;
	}
}

.scene-list {
	background: #1a1a1a;
	padding: 30rpx;
	
	.scene-title {
		font-size: 28rpx;
		color: #fff;
		margin-bottom: 20rpx;
	}
	
	.scenes {
		display: grid;
		grid-template-columns: repeat(4, 1fr);
		gap: 20rpx;
		
		.scene-item {
			display: flex;
			flex-direction: column;
			align-items: center;
			gap: 12rpx;
			
			.thumb {
				width: 100%;
				height: 120rpx;
				border-radius: 50%;
				border: 3rpx solid transparent;
			}
			
			.name {
				font-size: 24rpx;
				color: #999;
				text-align: center;
			}
			
			&.active {
				.thumb {
					border-color: #2E7D32;
				}
				
				.name {
					color: #2E7D32;
				}
			}
		}
	}
}

.tips {
	background: #1a1a1a;
	padding: 0 30rpx 30rpx;
	display: flex;
	flex-direction: column;
	gap: 16rpx;
	
	.tip-item {
		display: flex;
		align-items: center;
		gap: 12rpx;
		font-size: 24rpx;
		color: #999;
	}
}
</style>
