<template>
  <div class="comments-page">
    <div class="deco-top-line"></div>

    <div class="page-header">
      <div class="header-left">
        <h2 class="page-title gradient-text">评论管理</h2>
        <p class="page-sub">管理社区帖子评论，支持查看、删除和审核</p>
      </div>
      <div class="header-right">
        <el-button type="primary" plain @click="loadData" :loading="loading" class="btn-glow">
          <el-icon><Refresh /></el-icon>
          刷新
        </el-button>
      </div>
    </div>

    <!-- 统计卡片 -->
    <el-row :gutter="16" class="stat-row">
      <el-col :span="6" v-for="(stat, idx) in statCards" :key="stat.key">
        <div class="stat-card glass-card fade-in-up" :style="{ animationDelay: (idx * 0.08) + 's' }">
          <div class="stat-icon-wrap" :style="{ background: stat.color + '15', color: stat.color }">
            <el-icon :size="22"><component :is="stat.icon" /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ stat.value }}</div>
            <div class="stat-label">{{ stat.label }}</div>
          </div>
        </div>
      </el-col>
    </el-row>

    <!-- 搜索筛选栏 -->
    <div class="search-bar glass-card">
      <div class="filter-group">
        <el-select v-model="statusFilter" placeholder="评论状态" clearable size="small" style="width:130px">
          <el-option label="待审核" :value="0" />
          <el-option label="已通过" :value="1" />
          <el-option label="已拒绝" :value="2" />
        </el-select>
        <el-input v-model="keyword" placeholder="搜索评论内容/用户" size="small" style="width:200px" clearable @keyup.enter="loadData">
          <template #prefix><el-icon><Search /></el-icon></template>
        </el-input>
        <el-button type="primary" plain size="small" @click="page = 1; loadData()">搜索</el-button>
      </div>
    </div>

    <!-- 评论列表 -->
    <div class="table-card glass-card">
      <el-table :data="tableData" v-loading="loading" stripe>
        <el-table-column label="#" width="60" align="center">
          <template #default="{ $index }">
            <span style="font-weight: 600; color: #1D3B5A;">{{ (page - 1) * size + $index + 1 }}</span>
          </template>
        </el-table-column>
        <el-table-column label="用户" width="150">
          <template #default="{ row }">
            <div style="display:flex;align-items:center;gap:8px">
              <el-avatar :size="32" :src="row.avatar || 'https://cdn.phototourl.com/free/2026-03-24-955d494f-d01c-4911-87ea-2e684f9a3f29.jpg'" style="flex-shrink:0">{{ row.nickname?.charAt(0) }}</el-avatar>
              <div>
                <div style="font-size:13px;font-weight:600;color:#1D3B5A">{{ row.nickname }}</div>
                <div style="font-size:11px;color:#bbb">ID: {{ row.userId }}</div>
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="评论内容" min-width="300">
          <template #default="{ row }">
            <div class="content-cell">
              <span class="content-text">{{ row.content }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="所属帖子" width="120">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="viewPost(row.postId)">
              帖子 #{{ row.postId }}
            </el-button>
          </template>
        </el-table-column>
        <el-table-column label="点赞" width="80">
          <template #default="{ row }">
            <span style="font-size:13px;color:#666">❤️ {{ row.likes || 0 }}</span>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="90">
          <template #default="{ row }">
            <el-tag v-if="row.status === 0" type="warning" size="small" effect="dark">待审核</el-tag>
            <el-tag v-else-if="row.status === 1" type="success" size="small" effect="dark">已通过</el-tag>
            <el-tag v-else type="danger" size="small" effect="dark">已拒绝</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="评论时间" width="160" />
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <div class="action-buttons">
              <template v-if="row.status === 0">
                <el-tooltip content="通过审核" placement="top">
                  <el-button size="small" circle type="success" plain @click="handleApprove(row)">
                    <el-icon><Check /></el-icon>
                  </el-button>
                </el-tooltip>
                <el-tooltip content="拒绝审核" placement="top">
                  <el-button size="small" circle type="danger" plain @click="handleReject(row)">
                    <el-icon><Close /></el-icon>
                  </el-button>
                </el-tooltip>
              </template>
              <template v-else-if="row.status === 1">
                <el-tooltip content="拒绝/隐藏" placement="top">
                  <el-button size="small" circle type="warning" plain @click="handleReject(row)">
                    <el-icon><Close /></el-icon>
                  </el-button>
                </el-tooltip>
              </template>
              <template v-else>
                <el-tooltip content="恢复审核" placement="top">
                  <el-button size="small" circle type="success" plain @click="handleApprove(row)">
                    <el-icon><Check /></el-icon>
                  </el-button>
                </el-tooltip>
              </template>
              <el-tooltip content="删除评论" placement="top">
                <el-button size="small" circle type="danger" plain @click="handleDelete(row)">
                  <el-icon><Delete /></el-icon>
                </el-button>
              </el-tooltip>
            </div>
          </template>
        </el-table-column>
      </el-table>
      <div class="pagination">
        <el-pagination v-model:current-page="page" v-model:page-size="size" :total="total" layout="total, prev, pager, next" @change="loadData" />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Refresh, Search, Delete, Check, Close, Clock } from '@element-plus/icons-vue'
import { commentApi } from '@/api'

const loading = ref(false)
const tableData = ref([])
const page = ref(1)
const size = ref(10)
const total = ref(0)
const statusFilter = ref('')
const keyword = ref('')

const statCards = ref([
  { key: 'total', label: '总评论数', value: '0', icon: 'ChatDotRound', color: '#A33B32' },
  { key: 'pending', label: '待审核', value: '0', icon: 'Clock', color: '#E6A23C' },
  { key: 'approved', label: '已通过', value: '0', icon: 'Check', color: '#67C23A' },
  { key: 'rejected', label: '已拒绝', value: '0', icon: 'Close', color: '#F56C6C' }
])

async function loadData() {
  loading.value = true
  try {
    const res = await commentApi.getAll(page.value, size.value, statusFilter.value, keyword.value)
    let data = res.data?.records || res.data?.list || []

    tableData.value = data
    total.value = res.data?.total || 0

    // 更新统计（获取所有数据）
    const allRes = await commentApi.getAll(1, 1000)
    const all = allRes.data?.records || allRes.data?.list || []
    statCards.value[0].value = String(all.length)
    statCards.value[1].value = String(all.filter(i => i.status === 0).length)
    statCards.value[2].value = String(all.filter(i => i.status === 1).length)
    statCards.value[3].value = String(all.filter(i => i.status === 2).length)
  } catch {
    ElMessage.error('获取评论列表失败')
    tableData.value = []
  } finally {
    loading.value = false
  }
}

function viewPost(postId) {
  ElMessage.info(`查看帖子 #${postId}`)
}

async function handleDelete(row) {
  await ElMessageBox.confirm(`确认删除该评论？内容：「${row.content.substring(0, 30)}...」`, '确认删除', { type: 'warning' })
  try {
    await commentApi.delete(row.id)
    ElMessage.success('删除成功')
    loadData()
  } catch {
    ElMessage.error('删除失败')
  }
}

async function handleApprove(row) {
  try {
    await commentApi.approve(row.id)
    row.status = 1
    ElMessage.success('审核通过')
    loadData()
  } catch {
    ElMessage.error('操作失败')
  }
}

async function handleReject(row) {
  try {
    await commentApi.reject(row.id)
    row.status = 2
    ElMessage.warning('已拒绝该评论')
    loadData()
  } catch {
    ElMessage.error('操作失败')
  }
}

onMounted(loadData)
</script>

<style scoped lang="scss">
.deco-top-line { height: 3px; background: linear-gradient(90deg, #A33B32, #1D3B5A, #D4A373, #A33B32); background-size: 300% 100%; animation: gradientShift 4s ease infinite; border-radius: 2px; margin-bottom: 24px; }
@keyframes gradientShift { 0%, 100% { background-position: 0% 50%; } 50% { background-position: 100% 50%; } }
.gradient-text { background: linear-gradient(135deg, #1D3B5A 0%, #A33B32 100%); -webkit-background-clip: text; -webkit-text-fill-color: transparent; background-clip: text; }
.glass-card { background: rgba(255,255,255,0.85); backdrop-filter: blur(12px); -webkit-backdrop-filter: blur(12px); border: 1px solid rgba(255,255,255,0.6); border-radius: 16px; box-shadow: 0 4px 24px rgba(0,0,0,0.06), 0 1px 4px rgba(0,0,0,0.04), inset 0 1px 0 rgba(255,255,255,0.8); transition: all 0.3s cubic-bezier(0.4,0,0.2,1); &:hover { transform: translateY(-2px); box-shadow: 0 8px 32px rgba(0,0,0,0.1), 0 2px 8px rgba(0,0,0,0.06), inset 0 1px 0 rgba(255,255,255,0.8); } }
.fade-in-up { opacity: 0; animation: fadeInUp 0.6s ease-out forwards; }
@keyframes fadeInUp { from { opacity: 0; transform: translateY(20px); } to { opacity: 1; transform: translateY(0); } }

.comments-page { position: relative; }
.page-header { margin-bottom: 20px; display: flex; justify-content: space-between; align-items: center; animation: fadeInUp 0.5s ease-out; }
.header-left { flex: 1; }
.header-right { display: flex; align-items: center; gap: 12px; }
.page-title { font-size: 26px; font-weight: 800; margin-bottom: 4px; }
.page-sub { color: #999; font-size: 14px; }

.stat-row { margin-bottom: 16px; }
.stat-card { padding: 16px 18px; display: flex; align-items: center; gap: 12px; }
.stat-icon-wrap { width: 42px; height: 42px; border-radius: 12px; display: flex; align-items: center; justify-content: center; flex-shrink: 0; transition: transform 0.3s; }
.stat-card:hover .stat-icon-wrap { transform: scale(1.08) rotate(-3deg); }
.stat-info { flex: 1; }
.stat-value { font-size: 22px; font-weight: 800; color: #1D3B5A; }
.stat-label { font-size: 12px; color: #999; }

.search-bar { padding: 14px 18px; margin-bottom: 16px; }
.filter-group { display: flex; align-items: center; gap: 10px; flex-wrap: wrap; }

.table-card { padding: 20px; margin-bottom: 16px; }
.content-cell { display: flex; flex-direction: column; gap: 4px; }
.content-text { font-size: 13px; color: #555; line-height: 1.5; display: -webkit-box; -webkit-line-clamp: 2; -webkit-box-orient: vertical; overflow: hidden; }

.action-buttons { display: flex; gap: 6px; flex-wrap: wrap; }
</style>
