<template>
	<view class="journey-detail-container">
		<!-- 封面图片 -->
		<image class="cover" :src="getJourneyCover()" mode="aspectFill"></image>

		<!-- 行程基本信息 -->
		<view class="info-card">
			<view class="title">{{ journey.title }}</view>
			<view class="meta">
				<view class="meta-item">
					<uv-icon name="map" size="16" color="#D4A373"></uv-icon>
					<text>{{ journey.city }}</text>
				</view>
				<view class="meta-item">
					<uv-icon name="clock" size="16" color="#D4A373"></uv-icon>
					<text>{{ journey.days }}天</text>
				</view>
				<view class="meta-item">
					<uv-icon name="calendar" size="16" color="#D4A373"></uv-icon>
					<text>{{ journey.startDate }} - {{ journey.endDate }}</text>
				</view>
				<view class="meta-item" v-if="journey.estimatedCost">
					<uv-icon name="red-packet" size="16" color="#D4A373"></uv-icon>
					<text>预估 ¥{{ journey.estimatedCost }}</text>
				</view>
			</view>
			<view class="status-badge" :class="getStatusClass(journey.status)">
				{{ journey.status }}
			</view>
		</view>

		<!-- 行程安排 -->
		<view class="schedule-section">
			<view class="section-title">行程安排</view>
			<view class="day-list">
				<view class="day-item" v-for="(day, index) in scheduleData" :key="index">
					<view class="day-header">
						<view class="day-number">Day {{ index + 1 }}</view>
						<text class="day-date">{{ getDate(journey.startDate, index) }}</text>
					</view>
					<view class="day-content">
						<view class="activity" v-for="(activity, idx) in day.activities" :key="idx">
							<view class="time-dot">
								<view class="dot"></view>
								<view class="line" v-if="idx < day.activities.length - 1"></view>
							</view>
							<view class="activity-info">
								<view class="time">{{ activity.time }}</view>
								<view class="activity-name">{{ activity.name }}</view>
								<view class="activity-desc">{{ activity.desc }}</view>
								<view class="activity-price" v-if="activity.price > 0">
									<text class="price-symbol">¥</text>
									<text class="price-value">{{ activity.price }}</text>
									<text class="price-label">/人</text>
								</view>
							</view>
						</view>
					</view>
				</view>
			</view>
		</view>

		<!-- 包含景点 -->
		<view class="attraction-section" v-if="attractionList.length > 0">
			<view class="section-title">包含景点</view>
			<view class="attraction-list">
				<view class="attraction-item" v-for="item in attractionList" :key="item.id" @click="goAttraction(item.id)">
					<image class="thumb" :src="item.cover" mode="aspectFill"></image>
					<view class="info">
						<text class="name">{{ item.name }}</text>
						<view class="price" v-if="item.price > 0">
							<text class="symbol">¥</text>
							<text class="value">{{ item.price }}</text>
						</view>
						<text class="free-tag" v-else>免费</text>
					</view>
				</view>
			</view>
		</view>

		<!-- 注意事项 -->
		<view class="tips-section">
			<view class="section-title">注意事项</view>
			<view class="tips-list">
				<view class="tip-item" v-for="(tip, index) in tips" :key="index">
					<uv-icon name="info-circle" size="16" color="#D4A373"></uv-icon>
					<text>{{ tip }}</text>
				</view>
			</view>
		</view>

		<!-- 费用明细 -->
		<view class="cost-section" v-if="costBreakdown.total > 0">
			<view class="section-title">费用明细</view>
			<view class="cost-card">
				<view class="cost-row" v-if="costBreakdown.ticket > 0">
					<text class="cost-label">景点门票</text>
					<text class="cost-value">¥{{ costBreakdown.ticket }}</text>
				</view>
				<view class="cost-row" v-if="costBreakdown.hotel > 0">
					<text class="cost-label">住宿费用</text>
					<text class="cost-value">¥{{ costBreakdown.hotel }}</text>
				</view>
				<view class="cost-row" v-if="costBreakdown.meal > 0">
					<text class="cost-label">餐饮费用</text>
					<text class="cost-value">¥{{ costBreakdown.meal }}</text>
				</view>
				<view class="cost-row cost-total">
					<text class="cost-label">合计</text>
					<text class="cost-value">¥{{ costBreakdown.total }}</text>
				</view>
			</view>
		</view>

		<!-- 底部操作栏 -->
		<view class="bottom-bar">
			<uv-button text="编辑行程" :customStyle="{background: '#FDFCF8', color: '#D4A373', border: '1px solid #D4A373', flex: 1}" @click="editJourney"></uv-button>
			<uv-button text="分享行程" :customStyle="{background: '#D4A373', color: '#FDFCF8', flex: 1}" @click="shareJourney"></uv-button>
		</view>
	</view>
</template>

<script setup>
import { ref, computed } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { attractionApi, journeyApi } from '../../utils/api.js'
import { staticData } from '../../utils/static-data.js'

const journeyId = ref(null)
const journey = ref({})
const attractionList = ref([])
const attractionsMap = ref({})

// 加载景点数据用于封面和信息展示
const loadAttractionsData = async () => {
	try {
		const res = await attractionApi.getList(1, 100)
		const list = res || []
		list.forEach(a => { attractionsMap.value[a.id] = a })
	} catch (e) {
		// 降级到静态数据
		;(staticData.attractions || []).forEach(a => { attractionsMap.value[a.id] = a })
	}
}

// 获取行程封面
const getJourneyCover = () => {
	if (journey.value.cover && !journey.value.cover.includes('unsplash')) {
		return journey.value.cover
	}
	// 尝试从景点数据获取
	const attractionIds = journey.value.attractions || []
	if (typeof attractionIds === 'string') {
		try { JSON.parse(attractionIds) } catch {}
	}
	const ids = Array.isArray(attractionIds) ? attractionIds : []
	if (ids.length > 0) {
		const first = attractionsMap.value[ids[0]]
		if (first) return first.cover || ''
	}
	return 'https://images.unsplash.com/photo-1599571130669-e3176b6f4cf8?w=800'
}

// 动态生成日程安排
const scheduleData = computed(() => {
	const days = parseInt(journey.value.days) || 1
	const ids = parseAttractionIds()
	const attractions = ids.map(id => attractionsMap.value[id]).filter(Boolean)

	// 如果有景点数据，根据景点数量分配到各天
	if (attractions.length > 0) {
		const result = []
		const perDay = Math.ceil(attractions.length / days)
		for (let d = 0; d < days; d++) {
			const dayAttractions = attractions.slice(d * perDay, (d + 1) * perDay)
			const activities = []

			// 早餐
			activities.push({ time: '08:00', name: '酒店早餐', desc: '享用当地特色早餐', price: 0 })

			// 景点游览
			dayAttractions.forEach((attr, idx) => {
				const time = 9 + idx * 2
				const timeStr = `${String(time).padStart(2, '0')}:00`
				activities.push({
					time: timeStr,
					name: attr.name,
					desc: attr.description || attr.detail || attr.address || '探索这个美丽的景点',
					price: attr.price || 0
				})
				// 午餐插入
				if (idx === Math.floor(dayAttractions.length / 2)) {
					activities.push({ time: '12:00', name: '午餐时间', desc: '品尝当地特色美食', price: 0 })
				}
			})

			// 晚餐
			activities.push({ time: '18:00', name: '晚餐', desc: '品尝侗族特色美食', price: 0 })

			result.push({ activities })
		}
		return result
	}

	// 无景点数据时的默认日程
	return Array.from({ length: days }, (_, d) => ({
		activities: [
			{ time: '08:00', name: '酒店早餐', desc: '享用当地特色早餐', price: 0 },
			{ time: '09:30', name: '出发游览', desc: '前往景点游览', price: 0 },
			{ time: '12:00', name: '午餐', desc: '品尝当地特色美食', price: 0 },
			{ time: '14:00', name: '下午行程', desc: '继续游览', price: 0 },
			{ time: '18:00', name: '晚餐', desc: '享用侗族美食', price: 0 }
		]
	}))
})

// 动态生成注意事项
const tips = computed(() => {
	const result = []
	const ids = parseAttractionIds()
	const attractions = ids.map(id => attractionsMap.value[id]).filter(Boolean)

	if (attractions.length > 0) {
		// 根据景点生成注意事项
		const hasPaidAttractions = attractions.some(a => a.price > 0)
		if (hasPaidAttractions) {
			result.push('部分景点需要购票，建议提前在线预订可享受优惠')
		}
		result.push('建议穿着舒适的鞋子，部分景点需要步行较长距离')
		result.push('注意景点开放时间，合理安排行程')
	} else {
		result.push('请提前预订景点门票，避免现场排队')
		result.push('建议穿着舒适的鞋子，部分景点需要步行较长距离')
		result.push('注意景点开放时间，合理安排行程')
	}

	result.push('携带防晒用品和充足的饮用水')
	result.push('保管好个人财物，注意人身安全')
	result.push('尊重当地侗族风俗习惯')

	return result
})

// 费用明细
const costBreakdown = computed(() => {
	const ids = parseAttractionIds()
	const attractions = ids.map(id => attractionsMap.value[id]).filter(Boolean)
	const ticket = attractions.reduce((sum, a) => sum + (a.price || 0), 0)
	const days = parseInt(journey.value.days) || 1
	const hotel = journey.value.estimatedCost ? Math.round(journey.value.estimatedCost * 0.4) : 0
	const meal = days * 100
	return {
		ticket,
		hotel,
		meal,
		total: journey.value.estimatedCost || (ticket + hotel + meal)
	}
})

function parseAttractionIds() {
	const raw = journey.value.attractions || journey.value.attractionIds || []
	if (Array.isArray(raw)) return raw
	if (typeof raw === 'string') {
		try { return JSON.parse(raw) } catch { return [] }
	}
	return []
}

onLoad(async (options) => {
	await loadAttractionsData()
	if (options.id) {
		journeyId.value = parseInt(options.id)
		await loadJourneyDetail()
	}
})

const loadJourneyDetail = async () => {
	// 先尝试从后端加载
	try {
		const data = await journeyApi.getDetail(journeyId.value)
		journey.value = data || {}
		// 解析 attractionIds
		const ids = parseAttractionIds()
		// 构建景点列表
		attractionList.value = ids.map(id => attractionsMap.value[id]).filter(Boolean)
		return
	} catch (e) {
		console.warn('从后端加载行程失败，尝试本地数据', e)
	}

	// 降级到本地存储
	const local = uni.getStorageSync('myJourneys') || []
	journey.value = local.find(j => j.id === journeyId.value) || {}
	const ids = parseAttractionIds()
	attractionList.value = ids.map(id => attractionsMap.value[id]).filter(Boolean)
}

const getStatusClass = (status) => {
	if (status === '进行中') return 'ongoing'
	if (status === '已完成') return 'completed'
	return 'pending'
}

const getDate = (startDate, dayIndex) => {
	if (!startDate) return ''
	const date = new Date(startDate)
	date.setDate(date.getDate() + dayIndex)
	return `${date.getMonth() + 1}月${date.getDate()}日`
}

const goAttraction = (id) => {
	uni.navigateTo({
		url: `/pages/attraction/detail?id=${id}`
	})
}

const editJourney = () => {
	uni.navigateTo({ url: `/pages/journey/create?id=${journeyId.value}` })
}

const shareJourney = () => {
	uni.showToast({ title: '点击右上角分享', icon: 'none' })
}
</script>

<style lang="scss" scoped>
.journey-detail-container {
	min-height: 100vh;
	background: #F8F9FA;
	padding-bottom: 120rpx;
}

.cover {
	width: 100%;
	height: 400rpx;
}

.info-card {
	background: #FDFCF8;
	padding: 30rpx;
	position: relative;

	.title {
		font-size: 36rpx;
		font-weight: bold;
		color: #333;
		margin-bottom: 20rpx;
		padding-right: 120rpx;
	}

	.meta {
		display: flex;
		flex-wrap: wrap;
		gap: 24rpx;

		.meta-item {
			display: flex;
			align-items: center;
			gap: 8rpx;
			font-size: 26rpx;
			color: #666;
		}
	}

	.status-badge {
		position: absolute;
		top: 30rpx;
		right: 30rpx;
		padding: 8rpx 20rpx;
		border-radius: 20rpx;
		font-size: 24rpx;

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

.schedule-section,
.attraction-section,
.tips-section,
.cost-section {
	background: #FDFCF8;
	margin-top: 20rpx;
	padding: 30rpx;
}

.section-title {
	font-size: 30rpx;
	font-weight: bold;
	color: #333;
	margin-bottom: 24rpx;
}

.day-list {
	.day-item {
		margin-bottom: 40rpx;

		&:last-child {
			margin-bottom: 0;
		}

		.day-header {
			display: flex;
			align-items: center;
			gap: 16rpx;
			margin-bottom: 20rpx;

			.day-number {
				background: #D4A373;
				color: #FDFCF8;
				padding: 8rpx 20rpx;
				border-radius: 20rpx;
				font-size: 24rpx;
				font-weight: bold;
			}

			.day-date {
				font-size: 26rpx;
				color: #666;
			}
		}

		.day-content {
			padding-left: 20rpx;

			.activity {
				display: flex;
				gap: 20rpx;
				margin-bottom: 30rpx;

				&:last-child {
					margin-bottom: 0;
				}

				.time-dot {
					position: relative;
					display: flex;
					flex-direction: column;
					align-items: center;

					.dot {
						width: 16rpx;
						height: 16rpx;
						background: #D4A373;
						border-radius: 50%;
						flex-shrink: 0;
					}

					.line {
						width: 2rpx;
						flex: 1;
						background: #E0E0E0;
						margin-top: 8rpx;
					}
				}

				.activity-info {
					flex: 1;
					padding-bottom: 20rpx;

					.time {
						font-size: 24rpx;
						color: #D4A373;
						margin-bottom: 8rpx;
					}

					.activity-name {
						font-size: 28rpx;
						font-weight: bold;
						color: #333;
						margin-bottom: 8rpx;
					}

					.activity-desc {
						font-size: 24rpx;
						color: #999;
						line-height: 1.5;
					}

					.activity-price {
						margin-top: 8rpx;
						display: inline-flex;
						align-items: baseline;
						background: #FFF3E0;
						padding: 4rpx 12rpx;
						border-radius: 12rpx;

						.price-symbol {
							font-size: 20rpx;
							color: #A33B32;
						}

						.price-value {
							font-size: 26rpx;
							color: #A33B32;
							font-weight: bold;
						}

						.price-label {
							font-size: 18rpx;
							color: #999;
						}
					}
				}
			}
		}
	}
}

.attraction-list {
	display: grid;
	grid-template-columns: repeat(2, 1fr);
	gap: 20rpx;

	.attraction-item {
		background: #F8F9FA;
		border-radius: 12rpx;
		overflow: hidden;

		.thumb {
			width: 100%;
			height: 200rpx;
		}

		.info {
			padding: 16rpx;
			display: flex;
			justify-content: space-between;
			align-items: center;

			.name {
				font-size: 26rpx;
				color: #333;
				flex: 1;
				overflow: hidden;
				text-overflow: ellipsis;
				white-space: nowrap;
			}

			.price {
				color: #FF5722;
				margin-left: 12rpx;

				.symbol {
					font-size: 20rpx;
				}

				.value {
					font-size: 24rpx;
					font-weight: bold;
				}
			}

			.free-tag {
				font-size: 22rpx;
				color: #4CAF50;
				font-weight: bold;
			}
		}
	}
}

.tips-list {
	.tip-item {
		display: flex;
		align-items: flex-start;
		gap: 12rpx;
		padding: 16rpx 0;
		border-bottom: 1rpx solid #f0f0f0;
		font-size: 26rpx;
		color: #666;
		line-height: 1.6;

		&:last-child {
			border-bottom: none;
		}
	}
}

.cost-card {
	background: #FAFAFA;
	border-radius: 12rpx;
	padding: 20rpx;

	.cost-row {
		display: flex;
		justify-content: space-between;
		padding: 10rpx 0;

		.cost-label {
			font-size: 26rpx;
			color: #666;
		}

		.cost-value {
			font-size: 26rpx;
			color: #333;
			font-weight: 600;
		}
	}

	.cost-total {
		border-top: 1rpx dashed #DCDFE6;
		margin-top: 10rpx;
		padding-top: 16rpx;

		.cost-label {
			font-weight: 600;
			color: #1D3B5A;
		}

		.cost-value {
			font-size: 32rpx;
			color: #A33B32;
			font-weight: 700;
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
