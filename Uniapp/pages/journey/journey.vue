<template>
	<view class="journey-container">
		<!-- 顶部统计 -->
		<view class="stats-card">
			<view class="stat-item">
				<text class="number">{{ journeys.length }}</text>
				<text class="label">总行程</text>
			</view>
			<view class="stat-item">
				<text class="number">{{ ongoingCount }}</text>
				<text class="label">进行中</text>
			</view>
			<view class="stat-item">
				<text class="number">{{ completedCount }}</text>
				<text class="label">已完成</text>
			</view>
		</view>

		<!-- 创建行程按钮 -->
		<view class="create-btn" @click="createJourney">
			<uv-icon name="plus" size="18" color="#FDFCF8"></uv-icon>
			<text>创建新行程</text>
		</view>

		<!-- 行程列表 -->
		<view class="journey-list">
			<view class="journey-item" v-for="item in journeys" :key="item.id" @click="goJourneyDetail(item.id)">
				<image class="cover" :src="getJourneyCover(item)" mode="aspectFill"></image>
				<view class="journey-info">
					<view class="journey-header">
						<text class="title">{{ item.title }}</text>
						<view class="status" :class="getStatusClass(item.status)">
							{{ item.status }}
						</view>
					</view>
					<view class="journey-meta">
						<view class="meta-item">
							<uv-icon name="map" size="14" color="#999"></uv-icon>
							<text>{{ item.city }}</text>
						</view>
						<view class="meta-item">
							<uv-icon name="clock" size="14" color="#999"></uv-icon>
							<text>{{ item.days }}天</text>
						</view>
						<view class="meta-item">
							<uv-icon name="calendar" size="14" color="#999"></uv-icon>
							<text>{{ item.startDate }}</text>
						</view>
					</view>
				</view>
			</view>
		</view>

		<!-- 空状态 -->
		<view class="empty-state" v-if="journeys.length === 0">
			<uv-icon name="empty-list" size="80" color="#ccc"></uv-icon>
			<text class="empty-text">暂无行程</text>
			<text class="empty-tip">点击上方按钮创建您的第一个行程</text>
		</view>
	</view>
</template>

<script setup>
import { ref, computed } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { attractionApi } from '../../utils/api.js'
import { staticData } from '../../utils/static-data.js'

const journeys = ref([])
const attractionsCache = ref([])

// 加载景点数据用于获取封面
const loadAttractions = async () => {
	try {
		const res = await attractionApi.getList(1, 50)
		attractionsCache.value = res || []
	} catch (e) {
		attractionsCache.value = staticData.attractions || []
	}
}

onShow(() => {
	const local = uni.getStorageSync('myJourneys') || []
	journeys.value = local
	loadAttractions()
})

// 获取行程封面：优先用已有cover，否则用第一个景点的图片
const getJourneyCover = (item) => {
	if (item.cover && !item.cover.includes('unsplash')) return item.cover
	// 尝试从景点数据获取第一个景点的图片
	const attractionIds = item.attractions || []
	if (attractionIds.length > 0 && attractionsCache.value.length > 0) {
		const firstAttraction = attractionsCache.value.find(a => a.id === attractionIds[0])
		if (firstAttraction && firstAttraction.cover) {
			return firstAttraction.cover
		}
	}
	// 默认封面
	return 'https://images.unsplash.com/photo-1599571130669-e3176b6f4cf8?w=800'
}

const ongoingCount = computed(() => {
	return journeys.value.filter(item => item.status === '进行中').length
})

const completedCount = computed(() => {
	return journeys.value.filter(item => item.status === '已完成').length
})

const getStatusClass = (status) => {
	if (status === '进行中') return 'ongoing'
	if (status === '已完成') return 'completed'
	return 'pending'
}

const createJourney = () => {
	uni.navigateTo({ url: '/pages/journey/create' })
}

const goJourneyDetail = (id) => {
	uni.navigateTo({
		url: `/pages/journey/detail?id=${id}`
	})
}
</script>

<style lang="scss" scoped>
.journey-container {
	min-height: 100vh;
	background: #F8F9FA;
	padding: 30rpx;
}

.stats-card {
	background: linear-gradient(135deg, #1D3B5A 0%, #2E5F8E 100%);
	border-radius: 20rpx;
	padding: 40rpx;
	display: flex;
	justify-content: space-around;
	margin-bottom: 30rpx;
	box-shadow: 0 8rpx 20rpx rgba(29, 59, 90, 0.2);

	.stat-item {
		display: flex;
		flex-direction: column;
		align-items: center;
		gap: 12rpx;

		.number {
			font-size: 48rpx;
			font-weight: bold;
			color: #FDFCF8;
		}

		.label {
			font-size: 24rpx;
			color: rgba(255, 255, 255, 0.9);
		}
	}
}

.create-btn {
	background: #D4A373;
	color: #FDFCF8;
	border-radius: 50rpx;
	padding: 24rpx;
	display: flex;
	align-items: center;
	justify-content: center;
	gap: 12rpx;
	font-size: 28rpx;
	margin-bottom: 30rpx;
	box-shadow: 0 4rpx 12rpx rgba(29, 59, 90, 0.3);
}

.journey-list {
	display: flex;
	flex-direction: column;
	gap: 20rpx;

	.journey-item {
		background: #FDFCF8;
		border-radius: 16rpx;
		overflow: hidden;
		box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.05);

		.cover {
			width: 100%;
			height: 320rpx;
		}

		.journey-info {
			padding: 24rpx;

			.journey-header {
				display: flex;
				justify-content: space-between;
				align-items: center;
				margin-bottom: 16rpx;

				.title {
					font-size: 32rpx;
					font-weight: bold;
					color: #333;
					flex: 1;
				}

				.status {
					padding: 6rpx 16rpx;
					border-radius: 20rpx;
					font-size: 22rpx;

					&.ongoing {
						background: #E3F2FD;
						color: #2196F3;
					}

					&.completed {
						background: #E8F5E9;
						color: #4CAF50;
					}

					&.pending {
						background: #FFF3E0;
						color: #FF9800;
					}
				}
			}

			.journey-meta {
				display: flex;
				flex-wrap: wrap;
				gap: 24rpx;

				.meta-item {
					display: flex;
					align-items: center;
					gap: 8rpx;
					font-size: 24rpx;
					color: #999;
				}
			}
		}
	}
}

.empty-state {
	display: flex;
	flex-direction: column;
	align-items: center;
	justify-content: center;
	padding: 120rpx 0;

	.empty-text {
		font-size: 28rpx;
		color: #999;
		margin-top: 30rpx;
	}

	.empty-tip {
		font-size: 24rpx;
		color: #ccc;
		margin-top: 12rpx;
	}
}
</style>
