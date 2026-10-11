<template>
	<view class="nearby-container">
		<!-- 定位信息 -->
		<view class="location-info">
			<view class="location-box">
				<uv-icon name="map" size="20" color="#1D3B5A"></uv-icon>
				<text class="location-text">当前位置：{{ currentLocation }}</text>
			</view>
			<uv-button text="重新定位" size="small" :customStyle="{background: '#1D3B5A', color: '#FDFCF8', padding: '0 20rpx'}" @click="relocate"></uv-button>
		</view>
		
		<!-- 距离筛选 -->
		<view class="distance-filter">
			<view class="filter-item" :class="{active: currentDistance === item.value}" v-for="item in distanceOptions" :key="item.value" @click="selectDistance(item.value)">
				{{ item.label }}
			</view>
		</view>
		
		<!-- 附近景点列表 -->
		<view class="attraction-list">
			<view class="attraction-item" v-for="item in nearbyAttractions" :key="item.id" @click="goDetail(item.id)">
				<image class="cover" :src="item.image" mode="aspectFill"></image>
				<view class="info">
					<view class="name">{{ item.name }}</view>
					<view class="desc">{{ item.desc }}</view>
					<view class="meta">
						<view class="distance">
							<uv-icon name="map" size="14" color="#1D3B5A"></uv-icon>
							<text>距离{{ item.distance }}</text>
						</view>
						<view class="rating">
							<uv-icon name="star-fill" size="14" color="#D4A373"></uv-icon>
							<text>{{ item.rating }}</text>
						</view>
						<view class="price">
							<text class="symbol">¥</text>
							<text class="value">{{ item.price }}</text>
						</view>
					</view>
				</view>
			</view>
		</view>
	</view>
</template>

<script setup>
import { ref, computed } from 'vue'
import { attractionApi } from '../../utils/api.js'

const currentLocation = ref('广西柳州三江侗族自治州')
const currentDistance = ref('all')
const nearbyAttractions = ref([])

const distanceOptions = ref([
	{ label: '全部', value: 'all' },
	{ label: '1km 内', value: '1' },
	{ label: '3km 内', value: '3' },
	{ label: '5km 内', value: '5' }
])

// 加载附近景点
const loadAttractions = async () => {
	try {
		const res = await attractionApi.getList(1, 20)
		nearbyAttractions.value = (res || []).map(item => ({
			...item,
			distance: '1.2km',
			desc: item.description?.substring(0, 50) + '...' || '',
			image: item.cover
		}))
	} catch (error) {
		console.error('加载景点失败:', error)
	}
}

loadAttractions()

const selectDistance = (value) => {
	currentDistance.value = value
}

const relocate = () => {
	uni.showLoading({
		title: '定位中...'
	})
	setTimeout(() => {
		uni.hideLoading()
		uni.showToast({
			title: '定位成功',
			icon: 'success'
		})
	}, 1000)
}

const goDetail = (id) => {
	uni.navigateTo({
		url: `/pages/attraction/detail?id=${id}`
	})
}
</script>

<style lang="scss" scoped>
.nearby-container {
	min-height: 100vh;
	background: #F8F9FA;
}

.location-info {
	background: #FDFCF8;
	padding: 30rpx;
	display: flex;
	justify-content: space-between;
	align-items: center;
	
	.location-box {
		display: flex;
		align-items: center;
		gap: 12rpx;
		
		.location-text {
			font-size: 28rpx;
			color: #333;
		}
	}
}

.distance-filter {
	background: #FDFCF8;
	margin-top: 20rpx;
	padding: 20rpx 30rpx;
	display: flex;
	gap: 20rpx;
	
	.filter-item {
		padding: 12rpx 30rpx;
		border-radius: 30rpx;
		font-size: 26rpx;
		color: #666;
		background: #F8F9FA;
		
		&.active {
			background: #E8F0F8;
			color: #1D3B5A;
		}
	}
}

.attraction-list {
	padding: 20rpx 30rpx;
	display: flex;
	flex-direction: column;
	gap: 20rpx;
	
	.attraction-item {
		background: #FDFCF8;
		border-radius: 16rpx;
		overflow: hidden;
		display: flex;
		
		.cover {
			width: 240rpx;
			height: 240rpx;
			flex-shrink: 0;
		}
		
		.info {
			flex: 1;
			padding: 20rpx;
			display: flex;
			flex-direction: column;
			
			.name {
				font-size: 30rpx;
				font-weight: bold;
				color: #333;
				margin-bottom: 12rpx;
			}
			
			.desc {
				font-size: 24rpx;
				color: #999;
				line-height: 1.5;
				margin-bottom: auto;
				overflow: hidden;
				text-overflow: ellipsis;
				display: -webkit-box;
				-webkit-line-clamp: 2;
				-webkit-box-orient: vertical;
			}
			
			.meta {
				display: flex;
				justify-content: space-between;
				align-items: center;
				margin-top: 12rpx;
				
				.distance,
				.rating {
					display: flex;
					align-items: center;
					gap: 4rpx;
					font-size: 24rpx;
					color: #666;
				}
				
				.price {
					color: #FF5722;
					
					.symbol {
						font-size: 20rpx;
					}
					
					.value {
						font-size: 28rpx;
						font-weight: bold;
					}
				}
			}
		}
	}
}
</style>
