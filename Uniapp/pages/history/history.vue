<template>
	<view class="history-container">
		<!-- 操作栏 -->
		<view class="action-bar">
			<text class="total">共{{ historyList.length }}条浏览记录</text>
			<text class="clear-btn" @click="clearAll">清空</text>
		</view>
		
		<!-- 历史记录列表 -->
		<view class="history-list">
			<view class="history-item" v-for="item in historyList" :key="item.id" @click="goDetail(item)">
				<image class="cover" :src="item.data.image || item.data.cover" mode="aspectFill"></image>
				<view class="info">
					<view class="name">{{ item.data.name || item.data.title }}</view>
					<view class="time">{{ item.viewTime }}</view>
				</view>
				<view class="delete-btn" @click.stop="deleteItem(item.id)">
					<uv-icon name="close" size="18" color="#999"></uv-icon>
				</view>
			</view>
		</view>
		
		<!-- 空状态 -->
		<view class="empty-state" v-if="historyList.length === 0">
			<uv-icon name="empty-history" size="80" color="#ccc"></uv-icon>
			<text class="empty-text">暂无浏览记录</text>
		</view>
	</view>
</template>

<script setup>
import { ref } from 'vue'

const historyList = ref([])

const goDetail = (item) => {
	if (item.type === 'attraction') {
		uni.navigateTo({
			url: `/pages/attraction/detail?id=${item.data.id}`
		})
	} else if (item.type === 'guide') {
		uni.navigateTo({
			url: `/pages/guide/detail?id=${item.data.id}`
		})
	}
}

const deleteItem = (id) => {
	const index = historyList.value.findIndex(item => item.id === id)
	if (index > -1) {
		historyList.value.splice(index, 1)
		uni.showToast({
			title: '已删除',
			icon: 'success'
		})
	}
}

const clearAll = () => {
	if (historyList.value.length === 0) {
		return
	}
	
	uni.showModal({
		title: '提示',
		content: '确定要清空所有浏览记录吗？',
		success: (res) => {
			if (res.confirm) {
				historyList.value = []
				uni.showToast({
					title: '已清空',
					icon: 'success'
				})
			}
		}
	})
}
</script>

<style lang="scss" scoped>
.history-container {
	min-height: 100vh;
	background: #F8F9FA;
}

.action-bar {
	background: #FDFCF8;
	padding: 24rpx 30rpx;
	display: flex;
	justify-content: space-between;
	align-items: center;
	
	.total {
		font-size: 26rpx;
		color: #666;
	}
	
	.clear-btn {
		font-size: 26rpx;
		color: #FF5722;
	}
}

.history-list {
	padding: 20rpx 30rpx;
	display: flex;
	flex-direction: column;
	gap: 20rpx;
	
	.history-item {
		background: #FDFCF8;
		border-radius: 16rpx;
		padding: 24rpx;
		display: flex;
		align-items: center;
		gap: 20rpx;
		
		.cover {
			width: 120rpx;
			height: 120rpx;
			border-radius: 12rpx;
			flex-shrink: 0;
		}
		
		.info {
			flex: 1;
			display: flex;
			flex-direction: column;
			gap: 12rpx;
			
			.name {
				font-size: 28rpx;
				font-weight: bold;
				color: #333;
				overflow: hidden;
				text-overflow: ellipsis;
				white-space: nowrap;
			}
			
			.time {
				font-size: 24rpx;
				color: #999;
			}
		}
		
		.delete-btn {
			padding: 12rpx;
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
}
</style>
