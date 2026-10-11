<template>
	<view class="review-container">
		<!-- 评分统计 -->
		<view class="rating-summary">
			<view class="score-section">
				<text class="score">{{ avgRating }}</text>
				<view class="stars">
					<text v-for="i in 5" :key="i" class="star-icon" :class="{ active: i <= Math.round(avgRating) }">⭐</text>
				</view>
				<text class="count">{{ reviews.length }}条评价</text>
			</view>
			<!-- 评分分布 -->
			<view class="rating-bars">
				<view class="bar-row" v-for="n in 5" :key="n">
					<text class="bar-label">{{ 6 - n }}分</text>
					<view class="bar-track">
						<view class="bar-fill" :style="{ width: getBarWidth(6 - n) + '%' }"></view>
					</view>
					<text class="bar-count">{{ getRatingCount(6 - n) }}</text>
				</view>
			</view>
		</view>

		<!-- 筛选标签 -->
		<view class="filter-tags">
			<view class="filter-item" v-for="(tab, index) in filterTabs" :key="index" :class="{ active: currentFilter === index }" @tap="currentFilter = index">
				<text class="filter-text">{{ tab.name }}</text>
			</view>
		</view>

		<!-- 评价列表 -->
		<view class="review-list">
			<view class="review-item" v-for="item in displayReviews" :key="item.id">
				<view class="user-info">
					<image class="avatar" :src="item.avatar" mode="aspectFill"></image>
					<view class="info">
						<text class="name">{{ item.userName }}</text>
						<view class="stars-row">
							<text v-for="i in 5" :key="i" class="star-sm" :class="{ active: i <= item.rating }">⭐</text>
						</view>
					</view>
					<text class="time">{{ formatTime(item.createTime) }}</text>
				</view>

				<text class="content">{{ item.content }}</text>

				<view class="images" v-if="item.images && item.images.length > 0">
					<image v-for="(img, index) in item.images" :key="index" class="img" :src="img" mode="aspectFill" @tap="previewImage(index, item.images)"></image>
				</view>

				<view class="tags" v-if="item.tags && item.tags.length > 0">
					<text class="tag" v-for="tag in item.tags" :key="tag">{{ tag }}</text>
				</view>

				<view class="actions">
					<view class="action-item" @tap="likeReview(item)">
						<text class="action-icon-text">{{ item.liked ? '❤️' : '🤍' }}</text>
						<text class="action-count">{{ item.likeCount }}</text>
					</view>
				</view>
			</view>

			<view class="empty-state" v-if="displayReviews.length === 0">
				<text class="empty-icon">📝</text>
				<text class="empty-text">暂无评价，来写第一条评价吧</text>
			</view>
		</view>

		<!-- 写评价按钮 -->
		<view class="write-btn" @tap="openWriteModal">
			<text class="write-icon">✏️</text>
			<text>写评价</text>
		</view>

		<!-- 写评价弹窗 -->
		<view class="modal-mask" v-if="showWriteModal" @tap="showWriteModal = false">
			<view class="modal-box" @tap.stop>
				<view class="modal-header">
					<text class="modal-title">写评价</text>
					<text class="modal-close" @tap="showWriteModal = false">关闭</text>
				</view>
				<scroll-view class="modal-scroll" scroll-y>
					<view class="modal-body">
						<!-- 评分选择 -->
						<view class="form-row">
							<text class="form-label">评分</text>
							<view class="rating-select">
								<text
									v-for="i in 5"
									:key="i"
									class="rating-star-btn"
									:class="{ active: i <= newRating }"
									@tap="newRating = i"
								>⭐</text>
								<text class="rating-text">{{ ratingText }}</text>
							</view>
						</view>

						<!-- 标签选择 -->
						<view class="form-row">
							<text class="form-label">标签</text>
							<view class="tag-select">
								<text
									v-for="tag in availableTags"
									:key="tag"
									class="tag-option"
									:class="{ selected: newTags.includes(tag) }"
									@tap="toggleTag(tag)"
								>{{ tag }}</text>
							</view>
						</view>

						<!-- 评价内容 -->
						<view class="form-row">
							<text class="form-label">评价内容</text>
							<view class="textarea-wrap">
								<textarea
									class="form-textarea"
									v-model="newContent"
									placeholder="分享你的游玩体验，帮助其他游客..."
									maxlength="500"
									:auto-height="true"
								/>
								<text class="char-count">{{ newContent.length }}/500</text>
							</view>
						</view>

						<!-- 提交按钮 -->
						<view class="submit-btn" :class="{ disabled: !canSubmit }" @tap="submitReview">
							<text class="submit-text">发布评价</text>
						</view>
					</view>
				</scroll-view>
			</view>
		</view>
	</view>
</template>

<script setup>
import { ref, computed } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { getRandomAvatar, checkLoginStatus } from '../../utils/auth.js'
import { sensitiveWordApi } from '../../utils/api'

const attractionId = ref(0)
const currentFilter = ref(0)
const showWriteModal = ref(false)

const openWriteModal = async () => {
	const valid = await checkLoginStatus()
	if (!valid) {
		return uni.showModal({
			title: '提示',
			content: '请先登录后再发表评价',
			confirmText: '去登录',
			success: (res) => {
				if (res.confirm) {
					uni.navigateTo({ url: '/pages/login/login' })
				}
			}
		})
	}
	showWriteModal.value = true
}

const filterTabs = ref([
	{ name: '全部' },
	{ name: '好评' },
	{ name: '中评' },
	{ name: '差评' }
])

const availableTags = ref(['壮观', '值得一去', '人多', '专业讲解', '震撼', '风景优美', '交通方便', '服务周到', '性价比高', '美食推荐'])

const newRating = ref(5)
const newContent = ref('')
const newTags = ref([])

const reviews = ref([
	{
		id: 1,
		attractionId: 1,
		userName: '张三',
		avatar: getRandomAvatar(),
		rating: 5,
		content: '程阳八寨真的太壮观了！建议大家早点去，人会少一些。风雨桥的建筑技艺令人叹为观止，侗族大歌的演出更是天籁之音！',
		images: [],
		likeCount: 125,
		liked: false,
		createTime: '2026-03-15 14:30:00',
		tags: ['壮观', '值得一去']
	},
	{
		id: 2,
		attractionId: 1,
		userName: '李四',
		avatar: getRandomAvatar(),
		rating: 4,
		content: '历史文化底蕴深厚，侗族人民的智慧让人敬佩。就是节假日人太多了，建议错峰出行。',
		images: [],
		likeCount: 86,
		liked: false,
		createTime: '2026-03-10 10:15:00',
		tags: ['人多', '历史']
	},
	{
		id: 3,
		attractionId: 1,
		userName: '王五',
		avatar: getRandomAvatar(),
		rating: 5,
		content: '非常震撼的建筑，值得细细品味。讲解很专业，学到了很多关于侗族建筑和文化的知识。强烈推荐！',
		images: [],
		likeCount: 203,
		liked: false,
		createTime: '2026-03-08 16:45:00',
		tags: ['震撼', '专业讲解']
	},
	{
		id: 4,
		attractionId: 1,
		userName: '赵六',
		avatar: getRandomAvatar(),
		rating: 3,
		content: '景点本身还不错，但配套设施有待改善。停车场太小，卫生间不够干净。希望景区能改进一下。',		
		images: [],
		likeCount: 42,
		liked: false,
		createTime: '2026-03-05 09:20:00',
		tags: []
	}
])

const avgRating = computed(() => {
	if (reviews.value.length === 0) return '0.0'
	const sum = reviews.value.reduce((acc, cur) => acc + cur.rating, 0)
	return (sum / reviews.value.length).toFixed(1)
})

const displayReviews = computed(() => {
	if (currentFilter.value === 0) return reviews.value
	if (currentFilter.value === 1) return reviews.value.filter(item => item.rating >= 4)
	if (currentFilter.value === 2) return reviews.value.filter(item => item.rating === 3)
	return reviews.value.filter(item => item.rating <= 2)
})

const ratingText = computed(() => {
	const texts = ['', '很差', '较差', '一般', '好评', '非常好评']
	return texts[newRating.value] || ''
})

const canSubmit = computed(() => {
	return newRating.value > 0 && newContent.value.trim().length > 0
})

onLoad((options) => {
	if (options.id) {
		attractionId.value = parseInt(options.id)
	}
})

const getRatingCount = (rating) => {
	return reviews.value.filter(item => item.rating === rating).length
}

const getBarWidth = (rating) => {
	const count = getRatingCount(rating)
	if (reviews.value.length === 0) return 0
	return (count / reviews.value.length) * 100
}

const toggleTag = (tag) => {
	const idx = newTags.value.indexOf(tag)
	if (idx > -1) {
		newTags.value.splice(idx, 1)
	} else if (newTags.value.length < 3) {
		newTags.value.push(tag)
	} else {
		uni.showToast({ title: '最多选择3个标签', icon: 'none' })
	}
}

const formatTime = (time) => {
	if (!time) return ''
	const date = new Date(time)
	const now = new Date()
	const diff = now - date
	if (diff < 60000) return '刚刚'
	if (diff < 3600000) return Math.floor(diff / 60000) + '分钟前'
	if (diff < 86400000) return Math.floor(diff / 3600000) + '小时前'
	if (diff < 604800000) return Math.floor(diff / 86400000) + '天前'
	return date.toLocaleDateString('zh-CN')
}

const previewImage = (index, images) => {
	uni.previewImage({
		current: index,
		urls: images
	})
}

const likeReview = (item) => {
	item.liked = !item.liked
	item.likeCount += item.liked ? 1 : -1
}

const submitReview = async () => {
	if (!canSubmit.value) {
		if (!newContent.value.trim()) {
			uni.showToast({ title: '请输入评价内容', icon: 'none' })
		}
		return
	}

	// 魏证后端用户状态（是否被禁用）
	const valid = await checkLoginStatus()
	if (!valid) {
		return uni.showModal({
			title: '提示',
			content: '请先登录后再发表评价',
			confirmText: '去登录',
			success: (res) => {
				if (res.confirm) {
					uni.navigateTo({ url: '/pages/login/login' })
				}
			}
		})
	}

	// 敏感词检测 - 后端API
	try {
		const sensitiveResult = await sensitiveWordApi.check(newContent.value)
		if (sensitiveResult.hasSensitive) {
			return uni.showModal({
				title: '内容包含敏感词',
				content: `检测到敏感词：${sensitiveResult.words.join('，')}`,
				showCancel: false,
				confirmText: '我知道了'
			})
		}
	} catch (e) {
		console.error('敏感词检测失败:', e)
		uni.showToast({ title: '敏感词检测失败，请稍后重试', icon: 'none' })
		return
	}

	const userInfo = uni.getStorageSync('userInfo') || {}
	const newReview = {
		id: Date.now(),
		attractionId: attractionId.value,
		userName: userInfo.nickname || '游客',
		avatar: userInfo.avatar || getRandomAvatar(),
		rating: newRating.value,
		content: newContent.value.trim(),
		images: [],
		likeCount: 0,
		liked: false,
		createTime: new Date().toISOString(),
		tags: [...newTags.value]
	}

	reviews.value.unshift(newReview)

	// 重置表单
	newRating.value = 5
	newContent.value = ''
	newTags.value = []
	showWriteModal.value = false

	uni.showToast({ title: '评价发布成功', icon: 'success' })
}
</script>

<style lang="scss" scoped>
.review-container {
	min-height: 100vh;
	background: #F8F9FA;
	padding-bottom: 140rpx;
}

.rating-summary {
	background: #FDFCF8;
	padding: 40rpx 30rpx;

	.score-section {
		display: flex;
		flex-direction: column;
		align-items: center;
		gap: 16rpx;
		margin-bottom: 30rpx;

		.score {
			font-size: 72rpx;
			font-weight: bold;
			color: #D4A373;
		}

		.stars {
			display: flex;
			gap: 8rpx;
		}

		.count {
			font-size: 26rpx;
			color: #999;
		}
	}

	.rating-bars {
		display: flex;
		flex-direction: column;
		gap: 12rpx;

		.bar-row {
			display: flex;
			align-items: center;
			gap: 12rpx;

			.bar-label {
				font-size: 22rpx;
				color: #999;
				width: 60rpx;
				text-align: right;
			}

			.bar-track {
				flex: 1;
				height: 12rpx;
				background: #F0EDE8;
				border-radius: 6rpx;
				overflow: hidden;

				.bar-fill {
					height: 100%;
					background: linear-gradient(90deg, #D4A373, #E8C08A);
					border-radius: 6rpx;
					transition: width 0.3s;
				}
			}

			.bar-count {
				font-size: 22rpx;
				color: #999;
				width: 40rpx;
			}
		}
	}
}

.filter-tags {
	background: #FDFCF8;
	margin-top: 20rpx;
	padding: 20rpx 30rpx;
	display: flex;
	gap: 16rpx;

	.filter-item {
		padding: 10rpx 28rpx;
		border-radius: 30rpx;
		background: #F8F9FA;

		.filter-text {
			font-size: 24rpx;
			color: #666;
		}

		&.active {
			background: #A33B32;
			.filter-text {
				color: #FDFCF8;
			}
		}
	}
}

.review-list {
	padding: 20rpx 30rpx;
	display: flex;
	flex-direction: column;
	gap: 20rpx;

	.review-item {
		background: #FDFCF8;
		border-radius: 16rpx;
		padding: 30rpx;
		box-shadow: 0 2rpx 10rpx rgba(0, 0, 0, 0.05);

		.user-info {
			display: flex;
			align-items: center;
			margin-bottom: 20rpx;

			.avatar {
				width: 60rpx;
				height: 60rpx;
				border-radius: 50%;
				margin-right: 16rpx;
			}

			.info {
				flex: 1;
				display: flex;
				flex-direction: column;
				gap: 8rpx;

				.name {
					font-size: 28rpx;
					font-weight: bold;
					color: #333;
				}

				.stars-row {
					display: flex;
					gap: 2rpx;

					.star-sm {
						font-size: 18rpx;
						opacity: 0.2;
						&.active { opacity: 1; }
					}
				}
			}

			.time {
				font-size: 22rpx;
				color: #999;
			}
		}

		.content {
			font-size: 28rpx;
			color: #555;
			line-height: 1.7;
			margin-bottom: 20rpx;
		}

		.images {
			display: grid;
			grid-template-columns: repeat(3, 1fr);
			gap: 12rpx;
			margin-bottom: 20rpx;

			.img {
				width: 100%;
				height: 200rpx;
				border-radius: 50%;
			}
		}

		.tags {
			display: flex;
			gap: 12rpx;
			margin-bottom: 20rpx;
			flex-wrap: wrap;

			.tag {
				background: rgba(163,59,50,0.08);
				color: #A33B32;
				font-size: 22rpx;
				padding: 6rpx 16rpx;
				border-radius: 20rpx;
			}
		}

		.actions {
			display: flex;
			gap: 40rpx;
			padding-top: 16rpx;
			border-top: 1rpx solid #F5F0E8;

			.action-item {
				display: flex;
				align-items: center;
				gap: 8rpx;

				.action-icon-text {
					font-size: 24rpx;
				}

				.action-count {
					font-size: 24rpx;
					color: #999;
				}
			}
		}
	}
}

.empty-state {
	text-align: center;
	padding: 80rpx 0;

	.empty-icon {
		display: block;
		font-size: 80rpx;
		margin-bottom: 20rpx;
	}

	.empty-text {
		font-size: 26rpx;
		color: #999;
	}
}

.write-btn {
	position: fixed;
	bottom: 30rpx;
	right: 30rpx;
	background: linear-gradient(135deg, #A33B32, #C85A50);
	color: #FDFCF8;
	padding: 24rpx 40rpx;
	border-radius: 50rpx;
	display: flex;
	align-items: center;
	gap: 12rpx;
	font-size: 28rpx;
	font-weight: bold;
	box-shadow: 0 4rpx 20rpx rgba(163,59,50,0.4);

	.write-icon {
		font-size: 32rpx;
	}
}

/* ===== 写评价弹�?===== */
.modal-mask {
	position: fixed;
	top: 0;
	left: 0;
	right: 0;
	bottom: 0;
	background: rgba(0,0,0,0.5);
	display: flex;
	align-items: flex-end;
	z-index: 500;
}

.modal-box {
	width: 100%;
	max-height: 85vh;
	background: #FDFCF8;
	border-radius: 32rpx 32rpx 0 0;
	display: flex;
	flex-direction: column;
}

.modal-header {
	display: flex;
	justify-content: space-between;
	align-items: center;
	padding: 30rpx;
	border-bottom: 1rpx solid #F0EDE8;

	.modal-title {
		font-size: 34rpx;
		font-weight: bold;
		color: #1D3B5A;
	}

	.modal-close {
		width: 50rpx;
		height: 50rpx;
		background: #F8F9FA;
		border-radius: 50%;
		display: flex;
		align-items: center;
		justify-content: center;
		font-size: 24rpx;
		color: #999;
	}
}

.modal-scroll {
	flex: 1;
}

.modal-body {
	padding: 30rpx;

	.form-row {
		margin-bottom: 30rpx;

		.form-label {
			display: block;
			font-size: 28rpx;
			font-weight: bold;
			color: #333;
			margin-bottom: 16rpx;
		}
	}

	.rating-select {
		display: flex;
		align-items: center;
		gap: 8rpx;

		.rating-star-btn {
			font-size: 44rpx;
			opacity: 0.25;
			transition: opacity 0.2s;

			&.active {
				opacity: 1;
			}
		}

		.rating-text {
			font-size: 26rpx;
			color: #D4A373;
			margin-left: 16rpx;
			font-weight: bold;
		}
	}

	.tag-select {
		display: flex;
		flex-wrap: wrap;
		gap: 12rpx;

		.tag-option {
			font-size: 24rpx;
			color: #666;
			background: #F8F9FA;
			padding: 10rpx 24rpx;
			border-radius: 24rpx;

			&.selected {
				background: rgba(163,59,50,0.1);
				color: #A33B32;
				border: 1rpx solid #A33B32;
			}
		}
	}

	.textarea-wrap {
		position: relative;

		.form-textarea {
			width: 100%;
			min-height: 200rpx;
			background: #F8F9FA;
			border-radius: 16rpx;
			padding: 20rpx;
			font-size: 28rpx;
			color: #333;
			line-height: 1.6;
			box-sizing: border-box;
		}

		.char-count {
			position: absolute;
			bottom: 12rpx;
			right: 16rpx;
			font-size: 22rpx;
			color: #999;
		}
	}

	.submit-btn {
		background: linear-gradient(135deg, #A33B32, #C85A50);
		border-radius: 50rpx;
		padding: 28rpx;
		text-align: center;
		margin-top: 10rpx;

		.submit-text {
			font-size: 30rpx;
			color: #FDFCF8;
			font-weight: bold;
		}

		&.disabled {
			opacity: 0.5;
		}
	}
}
</style>
