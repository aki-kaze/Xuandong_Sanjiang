<template>
	<view class="create-container">
		<view class="form-card">
			<view class="form-item">
				<text class="label">行程名称</text>
				<input class="input" v-model="form.title" placeholder="给行程起个名" maxlength="20" />
			</view>
			<view class="form-item">
				<text class="label">目的地</text>
				<input class="input" v-model="form.city" placeholder="请输入城市名" />
			</view>
			<view class="form-item">
				<text class="label">出发日期</text>
				<picker mode="date" :value="form.startDate" @change="onStartDateChange">
					<view class="picker-value">{{ form.startDate || '请选择日期' }}</view>
				</picker>
			</view>
			<view class="form-item">
				<text class="label">结束日期</text>
				<picker mode="date" :value="form.endDate" @change="onEndDateChange">
					<view class="picker-value">{{ form.endDate || '请选择日期' }}</view>
				</picker>
			</view>
			<view class="form-item">
				<text class="label">出行人数</text>
				<view class="stepper">
					<view class="step-btn" @click="decreasePeople">-</view>
					<text class="step-val">{{ form.people }}</text>
					<view class="step-btn" @click="increasePeople">+</view>
				</view>
			</view>
			<view class="form-item notes-item">
				<text class="label">行程备注</text>
				<textarea class="notes-textarea" v-model="form.notes" placeholder="添加旅行备注、特殊需求等" maxlength="200" />
			</view>
		</view>

		<!-- 选择景点 -->
		<view class="section-title">添加景点</view>
		<view class="attraction-grid">
			<view
				class="attraction-item"
				v-for="item in allAttractions"
				:key="item.id"
				:class="{ selected: selectedAttractions.includes(item.id) }"
				@click="toggleAttraction(item.id)"
			>
				<image class="thumb" :src="item.cover" mode="aspectFill"></image>
				<view class="name">{{ item.name }}</view>
				<view class="check" v-if="selectedAttractions.includes(item.id)">
					<uv-icon name="checkmark" size="14" color="#FDFCF8"></uv-icon>
				</view>
			</view>
		</view>

		<!-- 推荐路线 -->
		<view class="recommend-section" v-if="selectedAttractions.length > 0">
			<view class="section-title">推荐路线</view>
			<view class="recommend-card">
				<view class="recommend-item">
					<text class="rec-label">行程天数</text>
					<text class="rec-value">{{ tripDays }} 天</text>
				</view>
				<view class="recommend-item">
					<text class="rec-label">预估费用</text>
					<text class="rec-value">¥{{ estimatedCost }}</text>
				</view>
				<view class="recommend-item">
					<text class="rec-label">景点数量</text>
					<text class="rec-value">{{ selectedAttractions.length }} 个景点</text>
				</view>
			</view>
		</view>

		<!-- 行程预览 -->
		<view class="preview-section" v-if="isFormFilled">
			<view class="section-title">行程预览</view>
			<view class="preview-card">
				<view class="preview-title">{{ form.title || '未命名行程' }}</view>
				<view class="preview-info">
					<text class="preview-city">{{ form.city || '未设置目的地' }}</text>
					<text class="preview-date">{{ form.startDate || '待定' }} ~ {{ form.endDate || '待定' }}</text>
				</view>
				<view class="preview-people">{{ form.people }} 人出行</view>
				<view class="preview-attractions" v-if="selectedAttractions.length > 0">
					<text class="preview-attraction-tag" v-for="id in selectedAttractions.slice(0, 3)" :key="id">
						{{ getAttractionName(id) }}
					</text>
					<text class="more-tag" v-if="selectedAttractions.length > 3">+{{ selectedAttractions.length - 3 }} 更多</text>
				</view>
				<view class="preview-notes" v-if="form.notes">
					<text class="notes-label">备注</text>
					<text class="notes-content">{{ form.notes }}</text>
				</view>
			</view>
		</view>

		<view class="bottom-bar">
			<uv-button
				text="创建行程"
				:customStyle="{background: '#A33B32', color: '#FDFCF8', width: '100%'}"
				@click="createJourney"
			></uv-button>
		</view>
	</view>
</template>

<script setup>
import { ref, computed } from 'vue'
import { attractionApi, journeyApi } from '../../utils/api.js'
import { staticData } from '../../utils/static-data.js'

const allAttractions = ref([])
const selectedAttractions = ref([])

// 加载景点列表
const loadAttractions = async () => {
	try {
		const res = await attractionApi.getList(1, 50)
		allAttractions.value = res || []
	} catch (error) {
		console.error('API 加载景点失败，使用静态数据', error)
		allAttractions.value = staticData.attractions || []
	}
}

loadAttractions()

const form = ref({
	title: '',
	city: '',
	startDate: '',
	endDate: '',
	people: 1,
	notes: ''
})

// 计算行程天数
const tripDays = computed(() => {
	if (!form.value.startDate || !form.value.endDate) return 0
	const start = new Date(form.value.startDate)
	const end = new Date(form.value.endDate)
	return Math.ceil((end - start) / (1000 * 60 * 60 * 24)) + 1
})

// 预估费用
const estimatedCost = computed(() => {
	const days = tripDays.value || 1
	const people = form.value.people || 1
	const attractionCount = selectedAttractions.value.length || 1
	return (days * 300 + attractionCount * 100) * people
})

// 表单是否填写完整
const isFormFilled = computed(() => {
	return form.value.title || form.value.city || form.value.startDate || form.value.endDate || selectedAttractions.value.length > 0
})

// 根据id获取景点名称
const getAttractionName = (id) => {
	const attraction = allAttractions.value.find(a => a.id === id)
	return attraction ? attraction.name : '未知景点'
}

const onStartDateChange = (e) => {
	form.value.startDate = e.detail.value
}

const onEndDateChange = (e) => {
	form.value.endDate = e.detail.value
}

const increasePeople = () => {
	if (form.value.people < 20) form.value.people++
}

const decreasePeople = () => {
	if (form.value.people > 1) form.value.people--
}

const toggleAttraction = (id) => {
	const idx = selectedAttractions.value.indexOf(id)
	if (idx === -1) {
		selectedAttractions.value.push(id)
	} else {
		selectedAttractions.value.splice(idx, 1)
	}
}

const createJourney = async () => {
	if (!form.value.title.trim()) {
		uni.showToast({ title: '请输入行程名称', icon: 'none' })
		return
	}
	if (!form.value.city.trim()) {
		uni.showToast({ title: '请输入目的地', icon: 'none' })
		return
	}
	if (!form.value.startDate) {
		uni.showToast({ title: '请选择出发日期', icon: 'none' })
		return
	}
	if (!form.value.endDate) {
		uni.showToast({ title: '请选择结束日期', icon: 'none' })
		return
	}
	if (form.value.endDate < form.value.startDate) {
		uni.showToast({ title: '结束日期不能早于出发日期', icon: 'none' })
		return
	}

	const start = new Date(form.value.startDate)
	const end = new Date(form.value.endDate)
	const days = Math.ceil((end - start) / (1000 * 60 * 60 * 24)) + 1

	const coverImage = selectedAttractions.value.length > 0
		? allAttractions.value.find(a => a.id === selectedAttractions.value[0])?.cover
		: 'https://images.unsplash.com/photo-1599571130669-e3176b6f4cf8?w=800'

	// 保存到本地
	const journeys = uni.getStorageSync('myJourneys') || []
	const newJourney = {
		id: Date.now(),
		title: form.value.title,
		city: form.value.city,
		startDate: form.value.startDate,
		endDate: form.value.endDate,
		days,
		people: form.value.people,
		notes: form.value.notes,
		attractions: selectedAttractions.value,
		estimatedCost: estimatedCost.value,
		cover: coverImage,
		status: '未开始',
		createTime: new Date().toISOString().split('T')[0]
	}
	journeys.unshift(newJourney)
	uni.setStorageSync('myJourneys', journeys)

	// 同步保存到后端数据库
	try {
		const userInfo = uni.getStorageSync('userInfo') || {}
		await journeyApi.create({
			userId: userInfo.userId || userInfo.id || 0,
			name: form.value.title,
			cover: coverImage,
			city: form.value.city,
			days: days,
			people: form.value.people,
			startDate: form.value.startDate,
			endDate: form.value.endDate,
			status: '未开始',
			notes: form.value.notes,
			estimatedCost: estimatedCost.value,
			isCustom: true,
			difficulty: '自定义',
			stops: JSON.stringify(selectedAttractions.value.map(id => {
				const a = allAttractions.value.find(x => x.id === id)
				return a ? { name: a.name, desc: a.description || '' } : null
			}).filter(Boolean)),
			tags: '[]',
			costBreakdown: '{}',
			hotelInfo: '',
			attractionIds: JSON.stringify(selectedAttractions.value)
		})
	} catch (e) {
		console.error('同步到后端失败', e)
	}

	uni.showToast({ title: '行程创建成功', icon: 'success' })
	setTimeout(() => {
		uni.navigateBack()
	}, 1000)
}
</script>

<style lang="scss" scoped>
.create-container {
	min-height: 100vh;
	background: #FDFCF8;
	padding: 30rpx 30rpx 160rpx;
}

.form-card {
	background: #FDFCF8;
	border-radius: 16rpx;
	padding: 10rpx 30rpx;
	margin-bottom: 30rpx;
	box-shadow: 0 4rpx 20rpx rgba(163, 59, 50, 0.10);

	.form-item {
		display: flex;
		align-items: center;
		justify-content: space-between;
		padding: 30rpx 0;
		border-bottom: 1rpx solid #f0f0f0;

		&:last-child {
			border-bottom: none;
		}

		&.notes-item {
			flex-direction: column;
			align-items: flex-start;

			.notes-textarea {
				width: 100%;
				height: 150rpx;
				font-size: 28rpx;
				color: #333;
				padding: 16rpx;
				background: #FDFCF8;
				border-radius: 8rpx;
				margin-top: 16rpx;
				box-sizing: border-box;
			}
		}

		.label {
			font-size: 28rpx;
			color: #1D3B5A;
			font-weight: bold;
			width: 160rpx;
			flex-shrink: 0;
		}

		.input {
			flex: 1;
			font-size: 28rpx;
			color: #333;
			text-align: right;
		}

		.picker-value {
			font-size: 28rpx;
			color: #333;
		}

		.stepper {
			display: flex;
			align-items: center;
			gap: 30rpx;

			.step-btn {
				width: 56rpx;
				height: 56rpx;
				background: #FDFCF8;
				border-radius: 50%;
				display: flex;
				align-items: center;
				justify-content: center;
				font-size: 32rpx;
				color: #A33B32;
				font-weight: bold;
				border: 2rpx solid #A33B32;
			}

			.step-val {
				font-size: 32rpx;
				color: #333;
				font-weight: bold;
				min-width: 40rpx;
				text-align: center;
			}
		}
	}
}

.section-title {
	font-size: 28rpx;
	font-weight: bold;
	color: #1D3B5A;
	margin-bottom: 20rpx;
	padding-left: 20rpx;
	border-left: 6rpx solid #A33B32;
}

.attraction-grid {
	display: grid;
	grid-template-columns: repeat(3, 1fr);
	gap: 16rpx;
	margin-bottom: 30rpx;

	.attraction-item {
		background: #FDFCF8;
		border-radius: 12rpx;
		overflow: hidden;
		position: relative;
		border: 4rpx solid transparent;
		box-shadow: 0 4rpx 20rpx rgba(163, 59, 50, 0.10);

		&.selected {
			border-color: #A33B32;
		}

		.thumb {
			width: 100%;
			height: 160rpx;
		}

		.name {
			font-size: 22rpx;
			color: #333;
			padding: 10rpx;
			text-align: center;
			overflow: hidden;
			text-overflow: ellipsis;
			white-space: nowrap;
		}

		.check {
			position: absolute;
			top: 8rpx;
			right: 8rpx;
			width: 36rpx;
			height: 36rpx;
			background: #A33B32;
			border-radius: 50%;
			display: flex;
			align-items: center;
			justify-content: center;
		}
	}
}

.recommend-section {
	margin-bottom: 30rpx;
}

.recommend-card {
	background: #FDFCF8;
	border-radius: 16rpx;
	padding: 24rpx 30rpx;
	box-shadow: 0 4rpx 20rpx rgba(163, 59, 50, 0.10);

	.recommend-item {
		display: flex;
		justify-content: space-between;
		align-items: center;
		padding: 16rpx 0;
		border-bottom: 1rpx solid #f0f0f0;

		&:last-child {
			border-bottom: none;
		}

		.rec-label {
			font-size: 26rpx;
			color: #666;
		}

		.rec-value {
			font-size: 28rpx;
			color: #A33B32;
			font-weight: bold;
		}
	}
}

.preview-section {
	margin-bottom: 30rpx;
}

.preview-card {
	background: #FDFCF8;
	border-radius: 16rpx;
	padding: 24rpx 30rpx;
	box-shadow: 0 4rpx 20rpx rgba(163, 59, 50, 0.10);
	border-left: 6rpx solid #A33B32;

	.preview-title {
		font-size: 32rpx;
		color: #1D3B5A;
		font-weight: bold;
		margin-bottom: 16rpx;
	}

	.preview-info {
		display: flex;
		justify-content: space-between;
		margin-bottom: 12rpx;

		.preview-city {
			font-size: 26rpx;
			color: #A33B32;
		}

		.preview-date {
			font-size: 24rpx;
			color: #666;
		}
	}

	.preview-people {
		font-size: 26rpx;
		color: #D4A373;
		margin-bottom: 16rpx;
	}

	.preview-attractions {
		display: flex;
		flex-wrap: wrap;
		gap: 12rpx;
		margin-bottom: 16rpx;

		.preview-attraction-tag {
			font-size: 22rpx;
			color: #FDFCF8;
			background: #A33B32;
			padding: 6rpx 16rpx;
			border-radius: 20rpx;
		}

		.more-tag {
			font-size: 22rpx;
			color: #666;
			padding: 6rpx 16rpx;
		}
	}

	.preview-notes {
		padding-top: 16rpx;
		border-top: 1rpx dashed #f0f0f0;

		.notes-label {
			font-size: 24rpx;
			color: #1D3B5A;
			font-weight: bold;
		}

		.notes-content {
			font-size: 24rpx;
			color: #666;
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
	box-shadow: 0 -2rpx 10rpx rgba(0, 0, 0, 0.05);
}
</style>
