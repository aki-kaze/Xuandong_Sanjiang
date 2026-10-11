<template>
	<view class="ticket-detail-container">
		<!-- 订单状态 -->
		<view class="status-card">
			<view class="status-icon" :class="getStatusClass(order.status)">
				<uv-icon :name="getStatusIcon(order.status)" size="48" color="#FDFCF8"></uv-icon>
			</view>
			<text class="status-text">{{ order.status }}</text>
			<text class="status-tip" v-if="order.status === '待使用'">请在游玩当日凭二维码入园</text>
		</view>
		
		<!-- 订单信息 -->
		<view class="order-card">
			<view class="order-header">
				<text class="order-title">订单信息</text>
				<text class="order-id">订单号：{{ order.id }}</text>
			</view>
			
			<view class="order-content">
				<image class="cover" :src="order.cover" mode="aspectFill"></image>
				<view class="info">
					<text class="name">{{ order.attractionName }}</text>
					<text class="type">{{ order.type }}</text>
					<text class="quantity">数量：×{{ order.quantity }}</text>
				</view>
			</view>
			
			<view class="order-detail">
				<view class="detail-item">
					<text class="label">游玩日期</text>
					<text class="value">{{ order.date }}</text>
				</view>
				<view class="detail-item">
					<text class="label">下单时间</text>
					<text class="value">{{ order.createTime }}</text>
				</view>
				<view class="detail-item" v-if="order.contactName">
					<text class="label">联系人</text>
					<text class="value">{{ order.contactName }}</text>
				</view>
				<view class="detail-item" v-if="order.contactPhone">
					<text class="label">联系电话</text>
					<text class="value">{{ order.contactPhone }}</text>
				</view>
				<view class="detail-item">
					<text class="label">单价</text>
					<text class="value">¥{{ order.price }}</text>
				</view>
				<view class="detail-item total">
					<text class="label">实付金额</text>
					<view class="price">
						<text class="symbol">¥</text>
						<text class="value">{{ order.totalPrice }}</text>
					</view>
				</view>
			</view>
		</view>
		
		<!-- 二维码 -->
		<view class="qrcode-card" v-if="order.status === '待使用' || order.status === '已完成'">
			<view class="qrcode-title">入园凭证</view>
			<view class="qrcode-box">
				<image class="qrcode" src="https://www.xiaohongshu.com/explore/69bbae850000000023016677?xsec_token=YB2YHCgEKuzR0gi-xFW3BWeO1a90D4eQCUg-evflNpD1g%3D&xsec_source=pc_creatormng" mode="aspectFit"></image>
			</view>
			<text class="qrcode-tip">请在景区入口处出示此二维码</text>
		</view>
		
		<!-- 使用须知 -->
		<view class="notice-card">
			<view class="notice-title">使用须知</view>
			<view class="notice-list">
				<view class="notice-item" v-for="(item, index) in notices" :key="index">
					<text class="notice-number">{{ index + 1 }}.</text>
					<text class="notice-text">{{ item }}</text>
				</view>
			</view>
		</view>
		
		<!-- 底部操作栏 -->
    <view class="bottom-bar">
      <uv-button v-if="order.status === '待支付'" text="取消订单" :customStyle="{background: '#FDFCF8', color: '#666', border: '1px solid #ddd', flex: 1}" @click="cancelOrder"></uv-button>
      <uv-button v-if="order.status === '待支付'" text="立即支付" :customStyle="{background: '#1D3B5A', color: '#FDFCF8', flex: 1}" @click="payOrder"></uv-button>
      <uv-button v-if="order.status === '已完成'" text="再次购买" :customStyle="{background: '#1D3B5A', color: '#FDFCF8', width: '100%'}" @click="buyAgain"></uv-button>
      <uv-button v-if="order.status === '待使用'" text="申请退款" :customStyle="{background: '#FDFCF8', color: '#1D3B5A', border: '1px solid #1D3B5A', flex: 1}" @click="refund"></uv-button>
      <uv-button v-if="order.status === '待使用'" text="核销使用" :customStyle="{background: '#1D3B5A', color: '#FDFCF8', flex: 1}" @click="useTicket"></uv-button>
    </view>
	</view>
</template>

<script setup>
import { ref } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { orderApi, formatOrderTime } from '../../utils/api.js'

const orderId = ref('')
const order = ref({})
const isCollected = ref(false)
const notices = ref([
  '请在预约的时间段内准时到达景区',
  '入园时请出示预约时登记的有效证件',
  '如需取消订单，请提前24小时申请',
  '景区开放时间：08:00-18:00（17:30停止入园）',
  '如需帮助，请联系客服热线：400-888-8888'
])

onLoad((options) => {
	if (options.id) {
		orderId.value = options.id
		loadOrderDetail()
	}
})

const loadOrderDetail = async () => {
	try {
		const detail = await orderApi.getDetail(orderId.value)
		if (detail.createTime) {
			detail.createTime = formatOrderTime(detail.createTime)
		}
		order.value = detail
	} catch (error) {
		console.error('加载订单详情失败:', error)
	}
}

const getStatusClass = (status) => {
	const map = {
		'待使用': 'status-wait',
		'已完成': 'status-success',
		'已取消': 'status-cancelled'
	}
	return map[status] || ''
}

const getStatusIcon = (status) => {
	const map = {
		'待使用': 'ticket',
		'已完成': 'checkmark-circle',
		'已取消': 'close-circle'
	}
	return map[status] || 'ticket'
}

const toggleCollect = () => {
	isCollected.value = !isCollected.value
	uni.showToast({
		title: isCollected.value ? '收藏成功' : '取消收藏',
		icon: 'none'
	})
}

const useTicket = () => {
	uni.showModal({
		title: '核销确认',
		content: `确认核销此订单？\n\n景点：${order.value.attractionName}\n数量：${order.value.quantity}张\n订单号：${order.value.id}`,
		confirmText: '确认核销',
		confirmColor: '#A33B32',
		success: (res) => {
			if (res.confirm) {
				order.value.status = '已完成'
				const localOrders = uni.getStorageSync('orders') || []
				const orderIndex = localOrders.findIndex(o => o.id === order.value.id)
				if (orderIndex >= 0) {
					localOrders[orderIndex].status = '已完成'
					uni.setStorageSync('orders', localOrders)
				}
				uni.showToast({
					title: '核销成功',
					icon: 'success',
					duration: 2000
				})
				setTimeout(() => {
					uni.redirectTo({ url: '/pages/order/completed-order' })
				}, 1000)
			}
		}
	})
}

const refund = () => {
	uni.showModal({
		title: '提示',
		content: '确定要申请退款吗？',
		success: (res) => {
			if (res.confirm) {
				uni.showToast({
					title: '已提交退款申请',
					icon: 'success'
				})
			}
		}
	})
}

const cancelOrder = () => {
  uni.showModal({
    title: '取消订单',
    content: '确定要取消此订单吗？',
    confirmColor: '#A33B32',
    success: (res) => {
      if (res.confirm) {
        order.value.status = '已取消'
        const localOrders = uni.getStorageSync('orders') || []
        const orderIndex = localOrders.findIndex(o => o.id === order.value.id)
        if (orderIndex >= 0) {
          localOrders[orderIndex].status = '已取消'
          uni.setStorageSync('orders', localOrders)
        }
        uni.showToast({ title: '订单已取消', icon: 'success' })
      }
    }
  })
}

const payOrder = () => {
  uni.showModal({
    title: '支付确认',
    content: `确定支付此订单？\n\n订单号：${order.value.id}\n金额：¥${order.value.totalPrice}`,
    confirmText: '确认支付',
    confirmColor: '#A33B32',
    success: (res) => {
      if (res.confirm) {
        order.value.status = '待使用'
        const localOrders = uni.getStorageSync('orders') || []
        const orderIndex = localOrders.findIndex(o => o.id === order.value.id)
        if (orderIndex >= 0) {
          localOrders[orderIndex].status = '待使用'
          uni.setStorageSync('orders', localOrders)
        }
        uni.showToast({ title: '支付成功', icon: 'success' })
      }
    }
  })
}

const buyAgain = () => {
  uni.navigateTo({ url: '/pages/ticket/ticket' })
}

const contactService = () => {
  uni.showModal({
    title: '联系客服',
    content: '客服热线：400-888-8888\n工作时间：09:00-18:00',
    showCancel: false,
    confirmText: '知道了'
  })
}
</script>

<style lang="scss" scoped>
.ticket-detail-container {
	min-height: 100vh;
	background: #F8F9FA;
	padding: 20rpx 30rpx 120rpx;
}

.status-card {
	background: linear-gradient(135deg, #1D3B5A 0%, #2E5F8E 100%);
	border-radius: 20rpx;
	padding: 60rpx 30rpx;
	display: flex;
	flex-direction: column;
	align-items: center;
	gap: 20rpx;
	margin-bottom: 20rpx;
	
	.status-icon {
		width: 120rpx;
		height: 120rpx;
		border-radius: 50%;
		display: flex;
		align-items: center;
		justify-content: center;
		
		&.pending {
			background: rgba(255, 152, 0, 0.3);
		}
		
		&.using {
			background: rgba(33, 150, 243, 0.3);
		}
		
		&.completed {
			background: rgba(76, 175, 80, 0.3);
		}
	}
	
	.status-text {
		font-size: 36rpx;
		font-weight: bold;
		color: #FDFCF8;
	}
	
	.status-tip {
		font-size: 26rpx;
		color: rgba(255, 255, 255, 0.9);
	}
}

.order-card {
	background: #FDFCF8;
	border-radius: 20rpx;
	padding: 30rpx;
	margin-bottom: 20rpx;
	
	.order-header {
		display: flex;
		justify-content: space-between;
		align-items: center;
		margin-bottom: 24rpx;
		padding-bottom: 20rpx;
		border-bottom: 1rpx solid #f0f0f0;
		
		.order-title {
			font-size: 30rpx;
			font-weight: bold;
			color: #333;
		}
		
		.order-id {
			font-size: 24rpx;
			color: #999;
		}
	}
	
	.order-content {
		display: flex;
		gap: 20rpx;
		margin-bottom: 24rpx;
		padding-bottom: 24rpx;
		border-bottom: 1rpx solid #f0f0f0;
		
		.cover {
			width: 160rpx;
			height: 160rpx;
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
			}
			
			.type,
			.quantity {
				font-size: 24rpx;
				color: #999;
			}
		}
	}
	
	.order-detail {
		.detail-item {
			display: flex;
			justify-content: space-between;
			padding: 16rpx 0;
			
			.label {
				font-size: 26rpx;
				color: #999;
			}
			
			.value {
				font-size: 26rpx;
				color: #333;
			}
			
			&.total {
				border-top: 1rpx solid #f0f0f0;
				padding-top: 24rpx;
				margin-top: 12rpx;
				
				.label {
					font-size: 28rpx;
					font-weight: bold;
					color: #333;
				}
				
				.price {
					color: #FF5722;
					
					.symbol {
						font-size: 24rpx;
					}
					
					.value {
						font-size: 40rpx;
						font-weight: bold;
					}
				}
			}
		}
	}
}

.qrcode-card {
	background: #FDFCF8;
	border-radius: 20rpx;
	padding: 40rpx 30rpx;
	display: flex;
	flex-direction: column;
	align-items: center;
	margin-bottom: 20rpx;
	
	.qrcode-title {
		font-size: 30rpx;
		font-weight: bold;
		color: #333;
		margin-bottom: 30rpx;
	}
	
	.qrcode-box {
		width: 400rpx;
		height: 400rpx;
		padding: 20rpx;
		background: #F8F9FA;
		border-radius: 12rpx;
		margin-bottom: 24rpx;
		
		.qrcode {
			width: 100%;
			height: 100%;
		}
	}
	
	.qrcode-tip {
		font-size: 24rpx;
		color: #999;
		text-align: center;
	}
}

.notice-card {
	background: #FDFCF8;
	border-radius: 20rpx;
	padding: 30rpx;
	
	.notice-title {
		font-size: 30rpx;
		font-weight: bold;
		color: #333;
		margin-bottom: 20rpx;
	}
	
	.notice-list {
		.notice-item {
			display: flex;
			gap: 12rpx;
			padding: 12rpx 0;
			font-size: 26rpx;
			color: #666;
			line-height: 1.6;
			
			.notice-number {
				color: #1D3B5A;
				flex-shrink: 0;
			}
			
			.notice-text {
				flex: 1;
			}
		}
	}
}

.bottom-bar {
	position: fixed;
	bottom: 0;
	left: 0;
	right: 0;
	background: #FDFCF8;
	padding: 20rpx 30rpx;
	display: flex;
	gap: 20rpx;
	box-shadow: 0 -2rpx 10rpx rgba(0, 0, 0, 0.05);
}
</style>
