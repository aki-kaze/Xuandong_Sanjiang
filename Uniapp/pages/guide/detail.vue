<template>
	<view class="guide-detail-container">
		<!-- 封面 -->
		<image class="cover" :src="guide.cover" mode="aspectFill"></image>
		
		<!-- 标题信息 -->
		<view class="header-section">
			<view class="title">{{ guide.title }}</view>
			<view class="meta">
				<view class="author-info">
					<image class="avatar" :src="guide.avatar" mode="aspectFill"></image>
					<text class="name">{{ guide.author }}</text>
					<text class="time">{{ guide.createTime }}</text>
				</view>
				<view class="stats">
					<view class="stat-item">
						<uv-icon name="eye" size="14" color="#999"></uv-icon>
						<text>{{ formatNumber(guide.viewCount) }}</text>
					</view>
					<view class="stat-item">
						<uv-icon name="heart" size="14" color="#999"></uv-icon>
						<text>{{ formatNumber(guide.likeCount) }}</text>
					</view>
				</view>
			</view>
			
			<view class="tags">
				<text class="tag" v-for="tag in guide.tags" :key="tag">{{ tag }}</text>
			</view>
		</view>
		
		<!-- 内容 -->
		<view class="content-section">
			<view class="content-text">{{ guide.desc }}</view>
			<view class="content-text">
				这是一份详细的旅游攻略，包含了景点介绍、交通指南、美食推荐、住宿建议等实用信息。
				
				【Day 1】
				早上：抵达目的地，办理入站手续
				中午：品尝当地特色美食
				下午：游览第一个景点
				晚上：夜游老街，体验夜市文化
				
				【Day 2】
				全天游览主要景点，建议早起避开人流高峰。记得带好防晒用品和充足的水。
				
				【Day 3】
				上午：参观博物馆
				下午：自由活动，购买特产
				晚上：返回到入站点
				
				【实用tips】
				1. 提前预订门票可以享受优惠
				2. 建议穿舒适的鞋子
				3. 注意景区开放时的交通流量
			</view>
		</view>
		
		<!-- 底部操作 -->
		<view class="action-bar">
			<view class="action-item" @click="toggleLike">
				<uv-icon :name="isLiked ? 'heart-fill' : 'heart'" size="24" :color="isLiked ? '#FF5722' : '#999'"></uv-icon>
				<text>{{ isLiked ? '已点赞' : '点赞' }}</text>
			</view>
			<view class="action-item" @click="toggleCollect">
				<uv-icon :name="isCollected ? 'star-fill' : 'star'" size="24" :color="isCollected ? '#FF9800' : '#999'"></uv-icon>
				<text>{{ isCollected ? '已收藏' : '收藏' }}</text>
			</view>
			<view class="action-item" @click="share">
				<uv-icon name="share" size="24" color="#999"></uv-icon>
				<text>分享</text>
			</view>
		</view>
	</view>
</template>

<script setup>
import { ref } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { postApi } from '../../utils/api.js'

const guideId = ref(0)
const isLiked = ref(false)
const isCollected = ref(false)
const guide = ref({})

onLoad((options) => {
	if (options.id) {
		guideId.value = parseInt(options.id)
		loadGuideDetail()
	}
})

const loadGuideDetail = async () => {
	try {
		const detail = await postApi.getDetail(guideId.value)
		guide.value = detail
	} catch (error) {
		console.error('加载攻略详情失败:', error)
	}
}

const formatNumber = (num) => {
	if (num >= 10000) {
		return (num / 10000).toFixed(1) + 'w'
	}
	return num
}

const toggleLike = () => {
	isLiked.value = !isLiked.value
	uni.showToast({
		title: isLiked.value ? '点赞成功' : '取消点赞',
		icon: 'none'
	})
}

const toggleCollect = () => {
	isCollected.value = !isCollected.value
	uni.showToast({
		title: isCollected.value ? '收藏成功' : '取消收藏',
		icon: 'none'
	})
}

const share = () => {
	uni.showToast({
		title: '分享功能开发中',
		icon: 'none'
	})
}
</script>

<style lang="scss" scoped>
.guide-detail-container {
	min-height: 100vh;
	background: #F8F9FA;
	padding-bottom: 120rpx;
}

.cover {
	width: 100%;
	height: 400rpx;
}

.header-section {
	background: #FDFCF8;
	padding: 30rpx;
	
	.title {
		font-size: 36rpx;
		font-weight: bold;
		color: #333;
		line-height: 1.5;
		margin-bottom: 20rpx;
	}
	
	.meta {
		display: flex;
		justify-content: space-between;
		align-items: center;
		margin-bottom: 20rpx;
		
		.author-info {
			display: flex;
			align-items: center;
			gap: 12rpx;
			
			.avatar {
				width: 50rpx;
				height: 50rpx;
				border-radius: 50%;
			}
			
			.name {
				font-size: 26rpx;
				color: #333;
			}
			
			.time {
				font-size: 24rpx;
				color: #999;
			}
		}
		
		.stats {
			display: flex;
			gap: 24rpx;
			
			.stat-item {
				display: flex;
				align-items: center;
				gap: 8rpx;
				font-size: 24rpx;
				color: #999;
			}
		}
	}
	
	.tags {
		display: flex;
		gap: 12rpx;
		flex-wrap: wrap;
		
		.tag {
			background: #E8F5E9;
			color: #2E7D32;
			font-size: 22rpx;
			padding: 6rpx 16rpx;
			border-radius: 8rpx;
		}
	}
}

.content-section {
	background: #FDFCF8;
	margin-top: 20rpx;
	padding: 30rpx;
	
	.content-text {
		font-size: 28rpx;
		color: #666;
		line-height: 2;
		margin-bottom: 30rpx;
		white-space: pre-line;
	}
}

.action-bar {
	position: fixed;
	bottom: 0;
	left: 0;
	right: 0;
	background: #FDFCF8;
	padding: 20rpx 30rpx;
	display: flex;
	justify-content: space-around;
	box-shadow: 0 -2rpx 10rpx rgba(0, 0, 0, 0.05);
	
	.action-item {
		display: flex;
		flex-direction: column;
		align-items: center;
		gap: 8rpx;
		font-size: 24rpx;
		color: #999;
	}
}
</style>
