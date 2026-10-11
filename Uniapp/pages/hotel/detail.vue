<template>
	<view class="hotel-detail-container">
		<!-- 图片轮播 -->
		<view class="banner">
			<swiper class="swiper" :indicator-dots="true" :autoplay="true" :interval="3000" :duration="500">
				<swiper-item v-for="(img, index) in hotel.images" :key="index">
					<image class="banner-img" :src="img" mode="aspectFill"></image>
				</swiper-item>
			</swiper>
			<view class="collect-btn" @click="toggleCollect">
				<text class="collect-icon">{{ isCollected ? '❤️' : '🤍' }}</text>
			</view>
		</view>

		<!-- 基本信息 -->
		<view class="info-card">
			<view class="title">{{ hotel.name }}</view>
			<view class="type-badge">{{ hotel.type }}</view>
			<view class="rating-row">
				<view class="rating">
					<text class="star-icon">⭐</text>
					<text class="score">{{ hotel.rating }}</text>
					<text class="count">{{ hotel.reviewCount }}条评价</text>
				</view>
			</view>
			<view class="address-row">
				<image class="address-icon" src="/static/icons/position.png" mode="aspectFit" />
				<text>{{ hotel.address }}</text>
				<text class="distance">距您{{ hotel.distance }}</text>
				<text class="view-location-btn" @tap="viewLocation">查看位置</text>
			</view>
		</view>

		<!-- 设施服务 -->
		<view class="facilities-section">
			<view class="section-title">设施服务</view>
			<view class="facilities-list">
				<view class="facility-item" v-for="facility in hotel.facilities" :key="facility">
					<text class="icon">✓</text>
					<text>{{ facility }}</text>
				</view>
			</view>
		</view>

		<!-- 酒店介绍 -->
		<view class="intro-section">
			<view class="section-title">酒店介绍</view>
			<view class="intro-content">{{ hotel.desc }}</view>
		</view>

		<!-- 房型选择 -->
		<view class="room-section">
			<view class="section-title">房型选择</view>
			<view class="room-list">
				<view class="room-item" v-for="room in roomTypes" :key="room.id">
					<image class="room-img" :src="room.image" mode="aspectFill"></image>
					<view class="room-info">
						<text class="room-name">{{ room.name }}</text>
						<text class="room-desc">{{ room.desc }}</text>
						<view class="room-bottom">
							<view class="price">
								<text class="symbol">¥</text>
								<text class="value">{{ room.price }}</text>
								<text class="unit">/晚</text>
							</view>
							<view class="book-btn" @click="bookRoom(room)">预订</view>
						</view>
					</view>
				</view>
			</view>
		</view>

		<!-- 入住政策 -->
		<view class="policy-section">
			<view class="section-title">入住政策</view>
			<view class="policy-list">
				<view class="policy-item">
					<text class="policy-label">入住时间</text>
					<text class="policy-value">14:00 之后</text>
				</view>
				<view class="policy-item">
					<text class="policy-label">退房时间</text>
					<text class="policy-value">12:00 之前</text>
				</view>
				<view class="policy-item">
					<text class="policy-label">儿童政策</text>
					<text class="policy-value">允许携带儿童入住</text>
				</view>
				<view class="policy-item">
					<text class="policy-label">宠物政策</text>
					<text class="policy-value">不允许携带宠物</text>
				</view>
			</view>
		</view>

		<!-- 底部操作栏 -->
		<view class="bottom-bar">
			<view class="price-info">
				<text class="label">最低</text>
				<view class="price">
					<text class="symbol">¥</text>
					<text class="value">{{ hotel.price }}</text>
					<text class="unit">/晚起</text>
				</view>
			</view>
			<view class="book-now-btn" @click="bookNow">立即预订</view>
		</view>
	</view>
</template>

<script>
import { staticData } from '../../utils/static-data.js'
import { hotelApi, hotelOrderApi, formatOrderTime } from '../../utils/api.js'
import { favoritesUtil } from '../../utils/favorites.js'

export default {
  data() {
    return {
      hotelId: 1,
      isCollected: false,
      hotel: {},
      roomTypes: [],
      checkInDate: '',
      checkOutDate: ''
    }
  },
  onLoad(options) {
    if (options.id) {
      this.hotelId = parseInt(options.id)
      this.loadHotelDetail()
      this.initDates()
    }
  },
  onShow() {
    this.checkIsCollected()
  },
  methods: {
    checkIsCollected() {
      this.isCollected = favoritesUtil.isFavorite(this.hotelId, 'hotel')
    },
    initDates() {
      const today = new Date()
      this.checkInDate = today.toISOString().slice(0, 10)
      const tomorrow = new Date(today)
      tomorrow.setDate(tomorrow.getDate() + 1)
      this.checkOutDate = tomorrow.toISOString().slice(0, 10)
    },
    async loadHotelDetail() {
      try {
        // 优先从后端 API 获取
        const res = await hotelApi.getById(this.hotelId)
        if (res && res.data) {
          this.hotel = res.data
          this.roomTypes = res.data.roomTypes || []
          return
        }
      } catch (e) {
        console.log('从 API 加载酒店失败，使用静态数据', e)
      }
      
      // API 失败时使用静态数据
      const hotel = staticData.hotels.find(h => h.id === this.hotelId)
      if (hotel) {
        this.hotel = hotel
        this.roomTypes = hotel.roomTypes || []
      } else {
        this.hotel = {
          id: 1,
          name: '三江侗乡大酒店',
          type: '高档型',
          rating: 4.8,
          reviewCount: 256,
          address: '广西柳州三江侗族自治县古宜镇侗乡大道',
          distance: '1.2km',
          price: 380,
          latitude: 25.7834,
          longitude: 109.6078,
          image: 'https://cdn.phototourl.com/member/2026-03-27-41f55c65-23ae-4fb8-940c-9413cda51f87.jpg',
          images: [
            'https://cdn.phototourl.com/free/2026-03-23-023da30f-8b5e-4893-9987-add47194d84d.jpg',
            'https://cdn.phototourl.com/free/2026-03-23-5bbeb559-132b-460d-a3f1-043d32170ed2.png',
            'https://cdn.phototourl.com/free/2026-03-23-75376aa0-ba75-4269-8839-bbe6aca040a6.jpg'
          ],
          facilities: ['免费 WiFi', '停车场', '24 小时前台', '餐厅', '健身房', '商务中心', '会议室', '洗衣服务'],
          desc: '三江侗乡大酒店是一家集住宿、餐饮、娱乐为一体的现代化酒店，位于三江县城中心，交通便利，周边配套设施完善。酒店设计融合侗族文化元素，环境优雅舒适，服务周到热情。'
        }
        this.roomTypes = [
          {
            id: 1,
            name: '豪华大床房',
            desc: '面积 35㎡，大床 2m×2m',
            image: 'https://cdn.phototourl.com/free/2026-03-23-023da30f-8b5e-4893-9987-add47194d84d.jpg',
            price: 380
          },
          {
            id: 2,
            name: '豪华双床房',
            desc: '面积 35㎡，双床 1.2m×2m',
            image: 'https://cdn.phototourl.com/free/2026-03-23-5bbeb559-132b-460d-a3f1-043d32170ed2.png',
            price: 380
          },
          {
            id: 3,
            name: '行政套房',
            desc: '面积 60㎡，大床 2.2m×2m',
            image: 'https://cdn.phototourl.com/free/2026-03-23-75376aa0-ba75-4269-8839-bbe6aca040a6.jpg',
            price: 680
          }
        ]
      }
    },
    toggleCollect() {
      const item = {
        id: this.hotelId,
        type: 'hotel',
        name: this.hotel.name,
        desc: this.hotel.desc || this.hotel.description,
        price: this.hotel.price,
        cover: this.hotel.images && this.hotel.images[0] ? this.hotel.images[0] : '',
        emoji: '/static/icons/hotel.png'
      }
      favoritesUtil.toggleFavorite(item)
      this.checkIsCollected()
    },
    viewLocation() {
      const lat = parseFloat(this.hotel.latitude || this.hotel.lat)
      const lng = parseFloat(this.hotel.longitude || this.hotel.lng)
      if (!lat || !lng) { uni.showToast({ title: '暂无位置信息', icon: 'none' }); return }
      uni.openLocation({
        latitude: lat,
        longitude: lng,
        name: this.hotel.name,
        address: this.hotel.address || '三江侗族自治县',
        scale: 16,
        fail: () => { uni.showToast({ title: '打开地图失败', icon: 'none' }) }
      })
    },
    bookRoom(room) {
      uni.showModal({
        title: '预订确认',
        content: `确定预订 ${room.name} 吗？\n价格：¥${room.price}/晚`,
        success: (res) => {
          if (res.confirm) {
            this.createOrder(room)
          }
        }
      })
    },
    bookNow() {
      uni.showModal({
        title: '预订确认',
        content: `确定预订 ${this.hotel.name} 吗？\n最低价格：¥${this.hotel.price}/晚`,
        success: (res) => {
          if (res.confirm) {
            this.createOrder(null)
          }
        }
      })
    },
    async createOrder(room) {
      const userInfo = uni.getStorageSync('userInfo') || {}
      const nights = 1
      const roomQuantity = 1
      const price = room ? room.price : this.hotel.price
      const totalPrice = price * nights * roomQuantity
      const orderNo = 'HT' + Date.now()

      const orderData = {
        orderNo: orderNo,
        userId: userInfo.userId || 0,
        hotelId: this.hotelId,
        hotelName: this.hotel.name,
        hotelCover: this.hotel.images && this.hotel.images.length > 0 ? this.hotel.images[0] : (room ? room.image : ''),
        roomType: room ? room.name : '标准房',
        quantity: roomQuantity,
        price: price,
        totalPrice: totalPrice,
        checkInDate: this.checkInDate,
        checkOutDate: this.checkOutDate,
        contactName: userInfo.name || userInfo.nickname || '',
        contactPhone: userInfo.phone || userInfo.phone || '',
        remark: '酒店预订'
      }

      try {
        const res = await hotelOrderApi.create(orderData)
        const createdOrder = (res && res.data) ? res.data : (res && res.order) ? res.order : orderData
        
        uni.showToast({ title: '预订成功', icon: 'success' })
        setTimeout(() => {
          uni.navigateTo({ url: '/pages/order/hotel-order?status=待支付' })
        }, 1500)
      } catch (e) {
        console.error('创建订单失败:', e)
        const fallbackOrder = {
          id: orderNo,
          orderNo: orderNo,
          orderType: 'hotel',
          type: 'hotel',
          ticketName: room ? room.name : this.hotel.name,
          hotelName: this.hotel.name,
          hotelCover: this.hotel.images && this.hotel.images.length > 0 ? this.hotel.images[0] : (room ? room.image : ''),
          cover: this.hotel.images && this.hotel.images.length > 0 ? this.hotel.images[0] : (room ? room.image : ''),
          price: price,
          quantity: roomQuantity,
          totalPrice: totalPrice,
          date: this.checkInDate,
          checkInDate: this.checkInDate,
          checkOutDate: this.checkOutDate,
          nights: nights,
          address: this.hotel.address,
          status: '待支付',
          createTime: formatOrderTime(new Date()),
          createdAt: new Date(),
          contactName: userInfo.name || userInfo.nickname || '',
          contactPhone: userInfo.phone || ''
        }

        const orders = uni.getStorageSync('orders') || []
        orders.unshift(fallbackOrder)
        uni.setStorageSync('orders', orders)

        uni.showToast({ title: '预订成功', icon: 'success' })
        setTimeout(() => {
          uni.navigateTo({ url: '/pages/order/hotel-order?status=待支付' })
        }, 1500)
      }
    }
  }
}
</script>

<style lang="scss" scoped>
.hotel-detail-container {
	min-height: 100vh;
	background: #F8F9FA;
	padding-bottom: 120rpx;
}

.banner {
	position: relative;

	.swiper {
		height: 500rpx;
	}

	.banner-img {
		width: 100%;
		height: 100%;
	}

	.collect-btn {
		position: absolute;
		top: 30rpx;
		right: 30rpx;
		width: 68rpx;
		height: 68rpx;
		background: rgba(255, 255, 255, 0.9);
		border-radius: 50%;
		display: flex;
		align-items: center;
		justify-content: center;
		box-shadow: 0 4rpx 12rpx rgba(0, 0, 0, 0.15);

		.collect-icon {
			font-size: 36rpx;
		}
	}
}

.info-card {
	background: #FDFCF8;
	padding: 30rpx;
	margin: 20rpx;
	border-radius: 16rpx;
	box-shadow: 0 4rpx 20rpx rgba(0, 0, 0, 0.05);
	position: relative;

	.title {
		font-size: 36rpx;
		font-weight: bold;
		color: #333;
		margin-bottom: 12rpx;
		padding-right: 120rpx;
	}

	.type-badge {
		position: absolute;
		top: 30rpx;
		right: 30rpx;
		background: #FFF3E0;
		color: #FF9800;
		padding: 6rpx 16rpx;
		border-radius: 8rpx;
		font-size: 24rpx;
	}

	.rating-row {
		display: flex;
		align-items: center;
		margin-bottom: 16rpx;

		.rating {
			display: flex;
			align-items: center;
			gap: 8rpx;
			font-size: 26rpx;

			.star-icon {
				font-size: 24rpx;
			}

			.score {
				color: #FF9800;
				font-weight: bold;
				font-size: 30rpx;
			}

			.count {
				color: #999;
			}
		}
	}

	.address-row {
		display: flex;
		align-items: center;
		gap: 8rpx;
		font-size: 26rpx;
		color: #666;

		.address-icon {
			width: 28rpx;
			height: 28rpx;
		}

		.distance {
			color: #1D3B5A;
		}

		.view-location-btn {
			margin-left: auto;
			color: #4A90E2;
			font-size: 24rpx;
			padding: 6rpx 16rpx;
			background: rgba(74,144,226,0.1);
			border-radius: 20rpx;
		}
	}
}

.facilities-section,
.intro-section,
.room-section,
.policy-section {
	background: #FDFCF8;
	margin: 20rpx;
	padding: 30rpx;
	border-radius: 16rpx;
	box-shadow: 0 4rpx 20rpx rgba(0, 0, 0, 0.05);
}

.section-title {
	font-size: 30rpx;
	font-weight: bold;
	color: #333;
	margin-bottom: 20rpx;
}

.facilities-list {
	display: grid;
	grid-template-columns: repeat(2, 1fr);
	gap: 20rpx;

	.facility-item {
		display: flex;
		align-items: center;
		gap: 12rpx;
		font-size: 26rpx;
		color: #666;

		.icon {
			font-size: 28rpx;
		}
	}
}

.intro-content {
	font-size: 28rpx;
	color: #666;
	line-height: 1.8;
}

.room-list {
	display: flex;
	flex-direction: column;
	gap: 20rpx;

	.room-item {
		display: flex;
		gap: 20rpx;
		background: #F8F9FA;
		border-radius: 12rpx;
		overflow: hidden;

		.room-img {
			width: 200rpx;
			height: 200rpx;
			flex-shrink: 0;
		}

		.room-info {
			flex: 1;
			padding: 20rpx;
			display: flex;
			flex-direction: column;

			.room-name {
				font-size: 28rpx;
				font-weight: bold;
				color: #333;
				margin-bottom: 8rpx;
			}

			.room-desc {
				font-size: 24rpx;
				color: #999;
				margin-bottom: auto;
			}

			.room-bottom {
				display: flex;
				justify-content: space-between;
				align-items: center;
				margin-top: 16rpx;

				.price {
					color: #FF5722;

					.symbol {
						font-size: 20rpx;
					}

					.value {
						font-size: 32rpx;
						font-weight: bold;
					}

					.unit {
						font-size: 20rpx;
					}
				}

				.book-btn {
					background: #1D3B5A;
					color: #FDFCF8;
					padding: 12rpx 30rpx;
					border-radius: 20rpx;
					font-size: 24rpx;
				}
			}
		}
	}
}

.policy-list {
	.policy-item {
		display: flex;
		justify-content: space-between;
		padding: 20rpx 0;
		border-bottom: 1rpx solid #f0f0f0;

		&:last-child {
			border-bottom: none;
		}

		.policy-label {
			font-size: 26rpx;
			color: #999;
		}

		.policy-value {
			font-size: 26rpx;
			color: #333;
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
	padding-bottom: calc(20rpx + var(--window-bottom));
	display: flex;
	gap: 20rpx;
	align-items: center;
	box-shadow: 0 -2rpx 10rpx rgba(0, 0, 0, 0.05);

	.price-info {
		display: flex;
		flex-direction: column;
		gap: 4rpx;

		.label {
			font-size: 24rpx;
			color: #999;
		}

		.price {
			color: #FF5722;

			.symbol {
				font-size: 20rpx;
			}

			.value {
				font-size: 32rpx;
				font-weight: bold;
			}

			.unit {
				font-size: 20rpx;
			}
		}
	}

	.book-now-btn {
		flex: 1;
		background: linear-gradient(135deg, #1D3B5A, #2E5F8E);
		color: #FDFCF8;
		padding: 24rpx;
		border-radius: 50rpx;
		font-size: 30rpx;
		font-weight: bold;
		text-align: center;
	}
}
</style>
