<template>
  <div class="community-page">
    <div class="deco-top-line"></div>

    <div class="page-header">
      <div class="header-left">
        <h2 class="page-title gradient-text">社区监控</h2>
        <p class="page-sub">实时监控小程序社区发布、评论与敏感词检测</p>
      </div>
      <div class="header-right">
        <div class="live-indicator">
          <span class="live-dot"></span>
          <span class="live-text">LIVE</span>
        </div>
        <el-button type="primary" plain @click="loadData" :loading="loading" class="btn-glow">
          <el-icon><Refresh /></el-icon>
          刷新
        </el-button>
      </div>
    </div>

    <!-- 实时统计卡片 -->
    <div class="stat-row">
      <div class="stat-col" v-for="(stat, idx) in statCards" :key="stat.key">
        <div class="stat-card glass-card fade-in-up" :style="{ animationDelay: (idx * 0.08) + 's' }">
          <div class="stat-icon-wrap" :style="{ background: stat.color + '15', color: stat.color }">
            <el-icon :size="22"><component :is="stat.icon" /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ stat.value }}</div>
            <div class="stat-label">{{ stat.label }}</div>
          </div>
          <div class="stat-badge" v-if="stat.badge" :style="{ background: stat.badgeColor + '15', color: stat.badgeColor }">
            {{ stat.badge }}
          </div>
        </div>
      </div>
    </div>

    <!-- 搜索筛选栏 -->
    <div class="search-bar glass-card">
      <div class="filter-group">
        <el-radio-group v-model="activeTab" @change="handleSearch" size="small">
          <el-radio-button value="">全部</el-radio-button>
          <el-radio-button value="pending">待审核</el-radio-button>
          <el-radio-button value="approved">已发布</el-radio-button>
          <el-radio-button value="rejected">已拒绝</el-radio-button>
          <el-radio-button value="deleted">已删除</el-radio-button>
        </el-radio-group>
        <el-select v-model="sensitiveFilter" placeholder="敏感词检测" clearable size="small" style="width:140px" @change="handleSearch">
          <el-option label="包含敏感词" value="has_sensitive" />
          <el-option label="无敏感词" value="clean" />
        </el-select>
        <el-input v-model="keyword" placeholder="搜索内容/作者" size="small" style="width:200px" clearable @keyup.enter="handleSearch">
          <template #prefix><el-icon><Search /></el-icon></template>
        </el-input>
        <el-button type="primary" plain size="small" @click="handleSearch">搜索</el-button>
      </div>
    </div>

    <!-- 帖子列表 -->
    <div class="table-card glass-card">
      <el-table :data="tableData" v-loading="loading" stripe>
        <el-table-column label="#" width="60" align="center">
          <template #default="{ $index }">
            <span style="font-weight: 600; color: #1D3B5A;">{{ (page - 1) * size + $index + 1 }}</span>
          </template>
        </el-table-column>
        <el-table-column label="图片" width="120">
          <template #default="{ row }">
            <div class="post-images-preview">
              <el-image v-if="getFirstImage(row)" :src="getFirstImage(row)" fit="cover" style="width:56px;height:42px;border-radius:6px" />
              <span v-else style="font-size:12px;color:#999">无图片</span>
              <span v-if="getImageCount(row) > 1" class="image-count">+{{ getImageCount(row) - 1 }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="作者" width="130">
          <template #default="{ row }">
            <div style="display:flex;align-items:center;gap:8px">
              <el-avatar :size="28" :src="row.avatar || 'https://cdn.phototourl.com/free/2026-03-24-955d494f-d01c-4911-87ea-2e684f9a3f29.jpg'" style="flex-shrink:0">{{ row.nickname?.charAt(0) || '?' }}</el-avatar>
              <span style="font-size:13px;font-weight:600;color:#1D3B5A">{{ row.nickname || '匿名用户' }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="用户ID" width="100">
          <template #default="{ row }">
            <el-tag size="small" type="info">{{ row.userId || '-' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="内容" min-width="220">
          <template #default="{ row }">
            <div class="content-cell">
              <span class="content-text">{{ row.content }}</span>
              <div class="content-tags" v-if="row.tags">
                <el-tag v-for="tag in parseTags(row.tags)" :key="tag" size="small" type="info" style="margin-right:4px">{{ tag }}</el-tag>
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="敏感词" width="100">
          <template #default="{ row }">
            <el-tag v-if="row.sensitiveWords" size="small" type="danger" effect="dark">
              {{ row.sensitiveWords }}
            </el-tag>
            <el-tag v-else size="small" type="success" effect="plain">安全</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="互动" width="100">
          <template #default="{ row }">
            <div style="display:flex;gap:12px;font-size:12px;color:#666">
              <span>❤️ {{ row.likes || 0 }}</span>
              <span>💬 {{ row.comments || 0 }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="90">
          <template #default="{ row }">
            <el-tag :type="statusType(row.status)" size="small" effect="dark">{{ statusLabel(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="发布时间" width="160" />
        <el-table-column label="操作" width="260" fixed="right">
          <template #default="{ row }">
            <div class="action-buttons">
              <template v-if="row.status === 3">
                <el-tooltip content="恢复帖子" placement="top">
                  <el-button size="small" circle type="success" plain @click="handleRestore(row)">
                    <el-icon><Refresh /></el-icon>
                  </el-button>
                </el-tooltip>
              </template>
              <template v-else>
                <template v-if="row.status === 'pending' || row.status === 0">
                  <el-tooltip content="通过审核" placement="top">
                    <el-button size="small" circle type="success" plain @click="handleApprove(row)">
                      <el-icon><Check /></el-icon>
                    </el-button>
                  </el-tooltip>
                  <el-tooltip content="拒绝" placement="top">
                    <el-button size="small" circle type="warning" plain @click="handleReject(row)">
                      <el-icon><Close /></el-icon>
                    </el-button>
                  </el-tooltip>
                </template>
                <el-tooltip content="查看详情" placement="top">
                  <el-button size="small" circle type="info" plain @click="handleViewDetail(row)">
                    <el-icon><View /></el-icon>
                  </el-button>
                </el-tooltip>
                <el-tooltip content="标记推荐" placement="top">
                  <el-button size="small" circle type="primary" plain @click="handleRecommend(row)">
                    <el-icon><Star /></el-icon>
                  </el-button>
                </el-tooltip>
                <el-tooltip content="删除" placement="top">
                  <el-button size="small" circle type="danger" plain @click="handleDelete(row)">
                    <el-icon><Delete /></el-icon>
                  </el-button>
                </el-tooltip>
              </template>
            </div>
          </template>
        </el-table-column>
      </el-table>
      <div class="pagination">
        <el-pagination v-model:current-page="page" v-model:page-size="size" :total="total" layout="total, prev, pager, next" @change="loadData" />
      </div>
    </div>

    <!-- 评论监控面板 -->
    <el-row :gutter="16" class="bottom-row">
      <el-col :span="14">
        <div class="chart-card glass-card fade-in-up" style="animation-delay: 0.5s">
          <div class="card-header">
            <div class="card-title"><span class="title-accent"></span>社区互动趋势</div>
          </div>
          <div ref="trendChartRef" style="height:260px"></div>
        </div>
      </el-col>
      <el-col :span="10">
        <div class="chart-card glass-card feed-card fade-in-up" style="animation-delay: 0.55s">
          <div class="card-header">
            <div class="card-title"><span class="title-accent"></span>最新评论监控</div>
            <div class="live-badge"><span class="live-dot-sm"></span>实时</div>
          </div>
          <div class="feed-scroll">
            <div class="feed-item" v-for="(c, idx) in commentFeed" :key="idx">
              <div class="feed-avatar" :style="{ background: c.color }">{{ c.user.charAt(0) }}</div>
              <div class="feed-body">
                <div class="feed-main">
                  <span class="feed-user">{{ c.user }}</span>
                  <span class="feed-action">评论了</span>
                  <span class="feed-target">{{ c.target }}</span>
                </div>
                <div class="feed-content-text">{{ c.content }}</div>
                <div class="feed-meta">
                  <span class="feed-time">{{ c.time }}</span>
                  <el-tag v-if="c.hasSensitive" size="small" type="danger" style="margin-left:8px">含敏感词</el-tag>
                  <el-tag v-if="c.status === 0" size="small" type="warning" style="margin-left:4px">待审核</el-tag>
                  <el-tag v-else-if="c.status === 2" size="small" type="info" style="margin-left:4px">已拒绝</el-tag>
                </div>
              </div>
            </div>
          </div>
        </div>
      </el-col>
    </el-row>

    <!-- 帖子详情弹窗 -->
    <el-dialog v-model="detailVisible" title="帖子详情" width="600px">
      <div v-if="currentRow" class="detail-content">
        <div class="detail-header">
          <el-avatar :size="40" :src="currentRow.avatar || 'https://cdn.phototourl.com/free/2026-03-24-955d494f-d01c-4911-87ea-2e684f9a3f29.jpg'">{{ currentRow.nickname?.charAt(0) || '?' }}</el-avatar>
          <div style="margin-left:12px">
            <div style="font-weight:600;color:#1D3B5A">{{ currentRow.nickname || '匿名用户' }}</div>
            <div style="font-size:12px;color:#999">用户 ID: {{ currentRow.userId || '-' }} · {{ formatDateTime(currentRow.createTime) }}</div>
          </div>
          <el-tag :type="statusType(currentRow.status)" style="margin-left:auto">{{ statusLabel(currentRow.status) }}</el-tag>
        </div>
        <el-divider />
        <div class="detail-body">{{ currentRow.content }}</div>
        <div class="detail-images" v-if="getImages(currentRow).length > 0">
          <el-image v-for="(img, i) in getImages(currentRow)" :key="i" :src="img" fit="cover" style="width:120px;height:90px;border-radius:8px;margin:4px" />
        </div>
        <el-divider />
        <div class="detail-stats">
          <span>❤️ {{ currentRow.likes || 0 }} 点赞</span>
          <span>💬 {{ currentRow.comments || 0 }} 评论</span>
          <el-tag v-if="currentRow.sensitiveWords" type="danger" size="small" style="margin-left:12px">敏感词: {{ currentRow.sensitiveWords }}</el-tag>
        </div>
      </div>
      <template #footer>
        <el-button plain @click="detailVisible = false">关闭</el-button>
        <el-button v-if="currentRow?.status === 'pending' || currentRow?.status === 0" type="success" plain @click="handleApprove(currentRow); detailVisible = false">通过</el-button>
        <el-button v-if="currentRow?.status === 'pending' || currentRow?.status === 0" type="warning" plain @click="handleReject(currentRow); detailVisible = false">拒绝</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, nextTick } from 'vue'
import * as echarts from 'echarts'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Refresh, Search, ChatDotRound, Warning, Document, Check, Close } from '@element-plus/icons-vue'
import { momentApi, postApi, commentApi, sensitiveWordApi } from '@/api'

const loading = ref(false)
const tableData = ref([])
const page = ref(1)
const size = ref(10)
const total = ref(0)
const activeTab = ref('')
const sensitiveFilter = ref('')
const keyword = ref('')
const detailVisible = ref(false)
const currentRow = ref(null)
const trendChartRef = ref()

const statCards = ref([
  { key: 'total', label: '总帖子数', value: '0', icon: 'Document', color: '#1D3B5A', badge: null, badgeColor: '' },
  { key: 'pending', label: '待审核', value: '0', icon: 'ChatDotRound', color: '#E6A23C', badge: null, badgeColor: '#E6A23C' },
  { key: 'sensitive', label: '含敏感词', value: '0', icon: 'Warning', color: '#A33B32', badge: null, badgeColor: '#A33B32' },
  { key: 'approved', label: '已发布', value: '0', icon: 'Check', color: '#67C23A', badge: null, badgeColor: '#67C23A' },
  { key: 'rejected', label: '已拒绝', value: '0', icon: 'Close', color: '#F56C6C', badge: null, badgeColor: '#F56C6C' },
  { key: 'deleted', label: '已删除', value: '0', icon: 'Delete', color: '#909399', badge: null, badgeColor: '#909399' }
])

const commentFeed = ref([])

const refreshTimer = ref(null)

function statusLabel(s) {
  const map = { 'pending': '待审核', 'approved': '已发布', 'rejected': '已拒绝', 'deleted': '已删除', 0: '待审核', 1: '已发布', 2: '已拒绝', 3: '已删除' }
  return map[s] ?? s ?? '-'
}
function statusType(s) {
  const map = { 'pending': 'warning', 'approved': 'success', 'rejected': 'info', 'deleted': 'danger', 0: 'warning', 1: 'success', 2: 'info', 3: 'danger' }
  return map[s] ?? ''
}
function parseTags(tags) {
  if (!tags) return []
  if (Array.isArray(tags)) return tags
  try { return JSON.parse(tags) } catch { return tags.split(',').map(t => t.trim()).filter(Boolean) }
}
function getImages(row) {
  if (!row) return []
  if (row.images) {
    if (Array.isArray(row.images)) return row.images
    try { return JSON.parse(row.images) } catch { return [] }
  }
  if (row.coverImg) return [row.coverImg]
  return []
}
function getFirstImage(row) {
  const images = getImages(row)
  return images.length > 0 ? images[0] : null
}
function getImageCount(row) {
  return getImages(row).length
}

function handleSearch() {
  page.value = 1
  loadData()
}

async function loadData() {
  loading.value = true
  try {
    let statusParam = ''
    if (activeTab.value === 'pending') statusParam = '0'
    else if (activeTab.value === 'approved') statusParam = '1'
    else if (activeTab.value === 'rejected') statusParam = '2'
    else if (activeTab.value === 'deleted') statusParam = '3'

    const res = await momentApi.getAll(page.value, size.value, statusParam, keyword.value)
    // 兼容多种返回格式
    const responseData = res.data || res
    let data = responseData.records || responseData.list || responseData.rows || []
    total.value = responseData.total || responseData.count || 0
    
    // 确保数据一致性
    if (data.length > 0 && total.value === 0) {
      total.value = data.length
    }
    
    // 如果当前页超过总页数，自动跳转到第一页
    const totalPages = Math.ceil(total.value / size.value)
    if (page.value > totalPages && totalPages > 0) {
      page.value = 1
      await loadData()
      return
    }
    
    // 检测敏感词
    for (let item of data) {
      try {
        const checkRes = await sensitiveWordApi.check(item.content || '')
        item.sensitiveWords = checkRes.data?.foundWords?.join(',') || null
      } catch {
        item.sensitiveWords = null
      }
    }
    tableData.value = data

    // 更新统计
    const allRes = await momentApi.getAll(1, 1000)
    const all = allRes.data?.records || allRes.data?.list || []
    statCards.value[0].value = String(all.length)
    statCards.value[1].value = String(all.filter(i => i.status === 0 || i.status === 'pending').length)
    statCards.value[2].value = String(all.filter(i => i.sensitiveWords).length)
    statCards.value[3].value = String(all.filter(i => i.status === 1 || i.status === 'approved').length)
    statCards.value[4].value = String(all.filter(i => i.status === 2 || i.status === 'rejected').length)
    statCards.value[1].badge = statCards.value[1].value
    // 获取已删除数量
    try {
      const deletedRes = await momentApi.getAll(1, 1000, '3')
      const deleted = deletedRes.data?.records || deletedRes.data?.list || []
      statCards.value[5].value = String(deleted.length)
    } catch { statCards.value[5].value = '0' }
  } catch {
    ElMessage.error('获取帖子列表失败')
    tableData.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
}

async function handleApprove(row) {
  try {
    await momentApi.approve(row.id)
    row.status = 1
    ElMessage.success('已通过审核')
    loadData()
  } catch { ElMessage.error('审核失败') }
}

async function handleReject(row) {
  try {
    await momentApi.reject(row.id)
    row.status = 2
    ElMessage.warning('已拒绝该帖子')
    loadData()
  } catch { ElMessage.error('审核失败') }
}

async function handleDelete(row) {
  await ElMessageBox.confirm('确认删除该帖子？', '确认删除', { type: 'warning' })
  try {
    await momentApi.delete(row.id)
    ElMessage.success('删除成功')
    loadData()
  } catch { ElMessage.error('删除失败') }
}

async function handleRestore(row) {
  try {
    await momentApi.approve(row.id)
    ElMessage.success('已恢复帖子')
    loadData()
  } catch { ElMessage.error('恢复失败') }
}

async function handleRecommend(row) {
  try {
    await momentApi.approve(row.id)
    row.isRecommended = row.isRecommended ? 0 : 1
    ElMessage.success(row.isRecommended ? '已设为推荐' : '已取消推荐')
  } catch { ElMessage.error('操作失败') }
}

function handleViewDetail(row) {
  currentRow.value = row
  detailVisible.value = true
}

function formatTimeAgo(timeStr) {
  if (!timeStr) return '未知'
  const date = new Date(timeStr)
  const now = new Date()
  const diff = Math.floor((now - date) / 1000)
  if (diff < 60) return '刚刚'
  if (diff < 3600) return Math.floor(diff / 60) + '分钟前'
  if (diff < 86400) return Math.floor(diff / 3600) + '小时前'
  if (diff < 604800) return Math.floor(diff / 86400) + '天前'
  // 超过 7 天显示具体日期
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  const hours = String(date.getHours()).padStart(2, '0')
  const minutes = String(date.getMinutes()).padStart(2, '0')
  return `${year}-${month}-${day} ${hours}:${minutes}`
}

// 格式化时间为统一格式 YYYY-MM-DD HH:mm:ss
function formatDateTime(timeStr) {
  if (!timeStr) return '-'
  // 直接使用原始字符串，保持与表格一致的ISO格式
  return timeStr.substring(0, 19)
}

const avatarColors = ['#A33B32', '#1D3B5A', '#D4A373', '#67C23A', '#409EFF', '#E6A23C', '#909399', '#8B5CF6']

async function loadCommentFeed() {
  try {
    // 获取最新的评论
    const res = await commentApi.getRecent(20)
    const comments = res.data || res || []
    if (!Array.isArray(comments) || comments.length === 0) return

    // 批量获取对应的帖子信息
    const postIds = [...new Set(comments.map(c => c.postId).filter(Boolean))]
    const postMap = {}
    await Promise.all(postIds.map(async (pid) => {
      try {
        const pRes = await momentApi.getDetail(pid)
        const post = pRes.data || pRes
        if (post) {
          postMap[pid] = {
            content: post.content || '',
            nickname: post.nickname || post.userName || '匿名用户',
            status: post.status
          }
        }
      } catch {
        postMap[pid] = { content: '', nickname: '未知', status: '' }
      }
    }))

    commentFeed.value = comments.map((c, idx) => {
      const content = c.content || ''
      const words = ['广告', '加我', '微信', '赌博', '代购']
      const sensitiveWords = words.filter(w => content.includes(w))
      const postInfo = postMap[c.postId] || {}
      return {
        user: c.nickname || c.userName || postInfo.nickname || '匿名用户',
        target: postInfo.content ? postInfo.content.substring(0, 15) + '...' : `帖子 #${c.postId}`,
        content: content.length > 80 ? content.substring(0, 80) + '...' : content,
        time: formatDateTime(c.createTime || c.createdAt),
        color: avatarColors[idx % avatarColors.length],
        hasSensitive: sensitiveWords.length > 0,
        status: c.status
      }
    })
  } catch (e) {
    console.error('获取评论流失败:', e)
  }
}

function initTrendChart() {
  if (!trendChartRef.value) return
  const chart = echarts.init(trendChartRef.value)
  const days = ['周一', '周二', '周三', '周四', '周五', '周六', '周日']
  chart.setOption({
    color: ['#A33B32', '#1D3B5A', '#67C23A', '#E6A23C'],
    tooltip: { trigger: 'axis', backgroundColor: 'rgba(255,255,255,0.96)', borderColor: '#e8e4dc', textStyle: { color: '#333' }, extraCssText: 'box-shadow:0 4px 20px rgba(0,0,0,0.12);border-radius:8px' },
    legend: { bottom: 0, textStyle: { color: '#666', fontSize: 11 } },
    grid: { top: 20, right: 20, bottom: 30, left: 40 },
    xAxis: { type: 'category', data: days, axisLine: { lineStyle: { color: '#e8e4dc' } }, axisLabel: { color: '#666' } },
    yAxis: { type: 'value', splitLine: { lineStyle: { color: '#f0f0f0', type: 'dashed' } }, axisLabel: { color: '#999' } },
    series: [
      { name: '新帖', type: 'line', smooth: true, symbol: 'circle', symbolSize: 6, lineStyle: { width: 2.5 }, areaStyle: { color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [{ offset: 0, color: 'rgba(163,59,50,0.2)' }, { offset: 1, color: 'rgba(163,59,50,0)' }]) }, data: [28, 35, 42, 38, 55, 72, 65] },
      { name: '评论', type: 'line', smooth: true, symbol: 'circle', symbolSize: 6, lineStyle: { width: 2.5 }, areaStyle: { color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [{ offset: 0, color: 'rgba(29,59,90,0.15)' }, { offset: 1, color: 'rgba(29,59,90,0)' }]) }, data: [45, 62, 78, 55, 92, 120, 98] },
      { name: '点赞', type: 'line', smooth: true, symbol: 'circle', symbolSize: 6, lineStyle: { width: 2 }, data: [120, 156, 198, 165, 230, 310, 275] },
      { name: '敏感词', type: 'bar', barWidth: 16, itemStyle: { borderRadius: [4, 4, 0, 0], color: 'rgba(230, 162, 60, 0.6)' }, data: [3, 5, 2, 8, 4, 6, 3] }
    ]
  })
}

onMounted(async () => {
  await loadData()
  await loadCommentFeed()
  await nextTick()
  initTrendChart()
  // 每30秒刷新评论流
  refreshTimer.value = setInterval(() => loadCommentFeed(), 30000)
})

onUnmounted(() => {
  if (refreshTimer.value) {
    clearInterval(refreshTimer.value)
    refreshTimer.value = null
  }
})
</script>

<style scoped lang="scss">
@use '@/styles/buttons.scss' as *;

.deco-top-line { height: 3px; background: linear-gradient(90deg, #A33B32, #1D3B5A, #D4A373, #A33B32); background-size: 300% 100%; animation: gradientShift 4s ease infinite; border-radius: 2px; margin-bottom: 24px; }
@keyframes gradientShift { 0%, 100% { background-position: 0% 50%; } 50% { background-position: 100% 50%; } }
.gradient-text { background: linear-gradient(135deg, #1D3B5A 0%, #A33B32 100%); -webkit-background-clip: text; -webkit-text-fill-color: transparent; background-clip: text; }
.glass-card { background: rgba(255,255,255,0.85); backdrop-filter: blur(12px); -webkit-backdrop-filter: blur(12px); border: 1px solid rgba(255,255,255,0.6); border-radius: 16px; box-shadow: 0 4px 24px rgba(0,0,0,0.06), 0 1px 4px rgba(0,0,0,0.04), inset 0 1px 0 rgba(255,255,255,0.8); transition: all 0.3s cubic-bezier(0.4,0,0.2,1); &:hover { transform: translateY(-2px); box-shadow: 0 8px 32px rgba(0,0,0,0.1), 0 2px 8px rgba(0,0,0,0.06), inset 0 1px 0 rgba(255,255,255,0.8); } }
.fade-in-up { opacity: 0; animation: fadeInUp 0.6s ease-out forwards; }
@keyframes fadeInUp { from { opacity: 0; transform: translateY(20px); } to { opacity: 1; transform: translateY(0); } }

.community-page { position: relative; }
.page-header { margin-bottom: 20px; display: flex; justify-content: space-between; align-items: center; animation: fadeInUp 0.5s ease-out; }
.header-left { flex: 1; }
.header-right { display: flex; align-items: center; gap: 12px; }
.page-title { font-size: 26px; font-weight: 800; margin-bottom: 4px; }
.page-sub { color: #999; font-size: 14px; }

.live-indicator { display: flex; align-items: center; gap: 8px; padding: 5px 12px; background: rgba(103,194,58,0.08); border-radius: 20px; border: 1px solid rgba(103,194,58,0.2); }
.live-dot { width: 8px; height: 8px; background: #67C23A; border-radius: 50%; animation: livePulse 2s ease-in-out infinite; }
@keyframes livePulse { 0%, 100% { opacity: 1; box-shadow: 0 0 0 0 rgba(103,194,58,0.4); } 50% { opacity: 0.7; box-shadow: 0 0 0 6px rgba(103,194,58,0); } }
.live-text { font-size: 11px; font-weight: 700; color: #67C23A; letter-spacing: 1px; }
.btn-glow { position: relative; overflow: hidden; }

// 统计卡片
.stat-row { margin-bottom: 16px; display: flex; gap: 16px; }
.stat-col { flex: 1; min-width: 0; }
.stat-card { padding: 16px 18px; display: flex; align-items: center; gap: 12px; position: relative; }
.stat-icon-wrap { width: 42px; height: 42px; border-radius: 12px; display: flex; align-items: center; justify-content: center; flex-shrink: 0; transition: transform 0.3s; }
.stat-card:hover .stat-icon-wrap { transform: scale(1.08) rotate(-3deg); }
.stat-info { flex: 1; }
.stat-value { font-size: 22px; font-weight: 800; color: #1D3B5A; }
.stat-label { font-size: 12px; color: #999; }
.stat-badge { font-size: 11px; font-weight: 700; padding: 2px 8px; border-radius: 10px; }

// 搜索栏
.search-bar { padding: 14px 18px; margin-bottom: 16px; }
.filter-group { display: flex; align-items: center; gap: 10px; flex-wrap: wrap; }

// 表格
.table-card { padding: 20px; margin-bottom: 16px; }
.content-cell { display: flex; flex-direction: column; gap: 4px; }
.content-text { font-size: 13px; color: #555; line-height: 1.5; display: -webkit-box; -webkit-line-clamp: 2; -webkit-box-orient: vertical; overflow: hidden; }
.content-tags { display: flex; flex-wrap: wrap; }
.post-images-preview { display: flex; align-items: center; gap: 4px; position: relative; }
.image-count { position: absolute; top: -6px; right: -6px; background: #1D3B5A; color: #fff; font-size: 10px; padding: 2px 6px; border-radius: 10px; font-weight: 600; }

// 按钮 - 图标优化
.action-buttons {
  display: flex;
  gap: 6px;
  flex-wrap: wrap;

  // 圆形按钮图标优化
  .el-button.is-circle {
    width: 32px !important;
    height: 32px !important;
    padding: 0 !important;
    display: inline-flex;
    align-items: center;
    justify-content: center;
    transition: all 0.25s cubic-bezier(0.4, 0, 0.2, 1);
    border-width: 1.5px;

    .el-icon {
      font-size: 14px;
      margin: 0 !important;
    }

    // 悬停效果
    &:hover {
      transform: translateY(-2px) scale(1.08);
      box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);

      .el-icon {
        transform: scale(1.1);
      }
    }

    // 点击效果
    &:active {
      transform: scale(0.95);
    }
  }

  // 不同类型的按钮颜色增强
  .el-button--success.is-circle {
    --el-button-text-color: #67c23a;
    --el-button-bg-color: rgba(103, 194, 58, 0.08);
    --el-button-border-color: #67c23a;
    --el-button-hover-text-color: #ffffff;
    --el-button-hover-bg-color: #67c23a;
    --el-button-hover-border-color: #67c23a;

    &:hover {
      box-shadow: 0 4px 12px rgba(103, 194, 58, 0.35);
    }
  }

  .el-button--warning.is-circle {
    --el-button-text-color: #e6a23c;
    --el-button-bg-color: rgba(230, 162, 60, 0.08);
    --el-button-border-color: #e6a23c;
    --el-button-hover-text-color: #ffffff;
    --el-button-hover-bg-color: #e6a23c;
    --el-button-hover-border-color: #e6a23c;

    &:hover {
      box-shadow: 0 4px 12px rgba(230, 162, 60, 0.35);
    }
  }

  .el-button--info.is-circle {
    --el-button-text-color: #909399;
    --el-button-bg-color: rgba(144, 147, 153, 0.08);
    --el-button-border-color: #909399;
    --el-button-hover-text-color: #ffffff;
    --el-button-hover-bg-color: #909399;
    --el-button-hover-border-color: #909399;

    &:hover {
      box-shadow: 0 4px 12px rgba(144, 147, 153, 0.35);
    }
  }

  .el-button--primary.is-circle {
    --el-button-text-color: #409eff;
    --el-button-bg-color: rgba(64, 158, 255, 0.08);
    --el-button-border-color: #409eff;
    --el-button-hover-text-color: #ffffff;
    --el-button-hover-bg-color: #409eff;
    --el-button-hover-border-color: #409eff;

    &:hover {
      box-shadow: 0 4px 12px rgba(64, 158, 255, 0.35);
    }
  }

  .el-button--danger.is-circle {
    --el-button-text-color: #f56c6c;
    --el-button-bg-color: rgba(245, 108, 108, 0.08);
    --el-button-border-color: #f56c6c;
    --el-button-hover-text-color: #ffffff;
    --el-button-hover-bg-color: #f56c6c;
    --el-button-hover-border-color: #f56c6c;

    &:hover {
      box-shadow: 0 4px 12px rgba(245, 108, 108, 0.35);
    }
  }
}

// 普通按钮图标优化
.button-row {
  .el-icon {
    margin-right: 5px;
    font-size: 14px;
    vertical-align: middle;
  }

  // 带图标的按钮优化
  .el-button:not(.is-circle) {
    display: inline-flex;
    align-items: center;
    gap: 4px;

    .el-icon {
      transition: transform 0.2s ease;
    }

    &:hover .el-icon {
      transform: scale(1.15) rotate(-3deg);
    }
  }
}
.bottom-row { margin-bottom: 0; }
.chart-card { padding: 20px; }
.card-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 14px; }
.card-title { font-size: 15px; font-weight: 700; color: #1D3B5A; display: flex; align-items: center; gap: 8px; .title-accent { width: 4px; height: 16px; background: linear-gradient(180deg, #A33B32, #D4A373); border-radius: 2px; flex-shrink: 0; } }
.live-badge { display: flex; align-items: center; gap: 6px; font-size: 12px; color: #67C23A; font-weight: 500; }
.live-dot-sm { width: 6px; height: 6px; background: #67C23A; border-radius: 50%; animation: livePulse 2s ease-in-out infinite; }

// 评论监控
.feed-card { display: flex; flex-direction: column; }
.feed-scroll { flex: 1; max-height: 240px; overflow-y: auto; }
.feed-item { display: flex; align-items: flex-start; gap: 10px; padding: 10px 0; border-bottom: 1px solid rgba(0,0,0,0.04); &:last-child { border-bottom: none; } }
.feed-avatar { width: 30px; height: 30px; border-radius: 8px; display: flex; align-items: center; justify-content: center; color: #fff; font-size: 12px; font-weight: 600; flex-shrink: 0; }
.feed-body { flex: 1; min-width: 0; }
.feed-main { font-size: 13px; color: #555; line-height: 1.4; }
.feed-user { font-weight: 600; color: #1D3B5A; }
.feed-action { margin: 0 3px; }
.feed-target { color: #A33B32; font-weight: 500; }
.feed-content-text { font-size: 12px; color: #888; margin-top: 2px; }
.feed-meta { display: flex; align-items: center; margin-top: 4px; }
.feed-time { font-size: 11px; color: #bbb; }

// 详情弹窗
.detail-content { }
.detail-header { display: flex; align-items: center; }
.detail-body { font-size: 14px; color: #333; line-height: 1.8; }
.detail-images { margin-top: 12px; display: flex; flex-wrap: wrap; }
.detail-stats { display: flex; align-items: center; gap: 16px; font-size: 13px; color: #666; }

// 响应式
@media screen and (max-width: 1200px) { .stat-row .el-col { span: 12 !important; margin-bottom: 12px; } }
@media screen and (max-width: 768px) { .stat-row .el-col { span: 24 !important; } .bottom-row .el-col { span: 24 !important; margin-bottom: 16px; } .page-header { flex-direction: column; align-items: flex-start; gap: 12px; } .filter-group { flex-wrap: wrap; } }
</style>
