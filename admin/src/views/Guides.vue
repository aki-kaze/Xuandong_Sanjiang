<template>
  <div class="guides-page">
    <div class="deco-top-line"></div>

    <div class="page-header">
      <div class="header-left">
        <h2 class="page-title gradient-text">攻略管理</h2>
        <p class="page-sub">管理旅行攻略内容，关联小程序旅行攻略模块</p>
      </div>
      <div class="header-right">
        <el-button type="primary" plain class="btn-glow" @click="handleAdd">
          <el-icon><Plus /></el-icon>
          添加攻略
        </el-button>
      </div>
    </div>

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

    <div class="search-bar glass-card">
      <div class="filter-group">
        <el-radio-group v-model="activeTab" @change="handleSearch" size="small">
          <el-radio-button value="">全部</el-radio-button>
          <el-radio-button value="pending">待审核</el-radio-button>
          <el-radio-button value="approved">已发布</el-radio-button>
          <el-radio-button value="rejected">已拒绝</el-radio-button>
        </el-radio-group>
        <el-input v-model="searchKeyword" placeholder="搜索攻略标题/作者" size="small" style="width:200px" clearable @keyup.enter="handleSearch">
          <template #prefix><el-icon><Search /></el-icon></template>
        </el-input>
        <el-button type="primary" plain size="small" @click="handleSearch">搜索</el-button>
      </div>
    </div>

    <div class="table-card glass-card">
      <el-table :data="filteredList" v-loading="loading" stripe>
        <el-table-column label="#" width="60" align="center">
          <template #default="{ $index }">
            <span style="font-weight: 600; color: #1D3B5A;">{{ $index + 1 }}</span>
          </template>
        </el-table-column>
        <el-table-column label="封面" width="80">
          <template #default="{ row }">
            <el-image v-if="getGuideCover(row)" :src="getGuideCover(row)" fit="cover" style="width:56px;height:42px;border-radius:6px" />
            <span v-else style="font-size:12px;color:#999">无封面</span>
          </template>
        </el-table-column>
        <el-table-column prop="title" label="攻略标题" min-width="180">
          <template #default="{ row }">
            <span style="font-weight:600;color:#1D3B5A">{{ row.title }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="author" label="作者" width="100" />
        <el-table-column label="浏览量" width="80">
          <template #default="{ row }">
            <span>{{ formatViews(row.views) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="标签" min-width="150">
          <template #default="{ row }">
            <el-tag v-for="tag in parseJsonField(row.tags, [])" :key="tag" size="small" style="margin-right:4px;margin-bottom:2px">{{ tag }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="热门" width="70" align="center">
          <template #default="{ row }">
            <el-tag :type="row.hot === 1 ? 'danger' : 'info'" size="small">{{ row.hot === 1 ? '是' : '否' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="审核状态" width="90" align="center">
          <template #default="{ row }">
            <el-tag :type="row.auditStatus === 1 ? 'success' : row.auditStatus === 2 ? 'danger' : 'warning'" size="small">
              {{ row.auditStatus === 1 ? '已通过' : row.auditStatus === 2 ? '已拒绝' : '待审核' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="发布时间" width="160">
          <template #default="{ row }">
            <span>{{ formatTime(row.publishTime || row.publish_time || row.createTime || row.create_time) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="280" fixed="right">
          <template #default="{ row }">
            <div class="action-buttons">
              <el-tooltip content="编辑攻略" placement="top">
                <el-button size="small" circle type="primary" plain @click="handleEdit(row)">
                  <el-icon><Edit /></el-icon>
                </el-button>
              </el-tooltip>
              <template v-if="row.auditStatus !== 1">
                <el-tooltip content="通过审核" placement="top">
                  <el-button size="small" circle type="success" plain @click="auditGuide(row, 1)">
                    <el-icon><Check /></el-icon>
                  </el-button>
                </el-tooltip>
              </template>
              <template v-if="row.auditStatus !== 2">
                <el-tooltip content="拒绝" placement="top">
                  <el-button size="small" circle type="warning" plain @click="auditGuide(row, 2)">
                    <el-icon><Close /></el-icon>
                  </el-button>
                </el-tooltip>
              </template>
              <el-tooltip content="删除攻略" placement="top">
                <el-button size="small" circle type="danger" plain @click="handleDelete(row.id)">
                  <el-icon><Delete /></el-icon>
                </el-button>
              </el-tooltip>
            </div>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-bar">
        <el-pagination
          v-model:current-page="currentPage"
          :page-size="pageSize"
          :total="total"
          layout="total, prev, pager, next"
          @current-change="loadData"
        />
      </div>
    </div>

    <!-- 编辑弹窗 -->
    <el-dialog v-model="showEdit" :title="editingRow ? '编辑攻略' : '添加攻略'" width="720px" destroy-on-close>
      <el-form :model="form" label-width="90px">
        <el-form-item label="攻略标题">
          <el-input v-model="form.title" placeholder="请输入攻略标题" />
        </el-form-item>
        <el-form-item label="攻略简介">
          <el-input v-model="form.desc" type="textarea" :rows="2" placeholder="请输入攻略简介" />
        </el-form-item>
        <el-form-item label="封面图片">
          <el-input v-model="form.cover" placeholder="请输入封面图片URL" />
        </el-form-item>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="作者名称">
              <el-input v-model="form.author" placeholder="作者名称" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="作者头像">
              <el-input v-model="form.authorAvatar" placeholder="作者头像URL" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="是否热门">
              <el-switch v-model="form.hot" :active-value="1" :inactive-value="0" active-text="热门" inactive-text="普通" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="状态">
              <el-switch v-model="form.status" :active-value="1" :inactive-value="0" active-text="上架" inactive-text="下架" />
            </el-form-item>
          </el-col>
        </el-row>

        <!-- 标签管理 -->
        <el-form-item label="标签">
          <div style="display:flex;flex-wrap:wrap;gap:8px;margin-bottom:8px">
            <el-tag v-for="(tag, idx) in formTags" :key="idx" closable @close="formTags.splice(idx, 1)">{{ tag }}</el-tag>
          </div>
          <div style="display:flex;gap:8px">
            <el-input v-model="newTag" placeholder="输入标签" style="width:160px" @keyup.enter="addTag" />
            <el-button size="small" @click="addTag">添加标签</el-button>
          </div>
        </el-form-item>

        <!-- 行程安排 -->
        <el-form-item label="行程安排">
          <div class="itinerary-list">
            <div class="itinerary-item" v-for="(item, idx) in formItinerary" :key="idx">
              <span class="itinerary-day">Day {{ idx + 1 }}</span>
              <el-input v-model="item.title" placeholder="标题" style="width:200px;margin-right:8px" />
              <el-input v-model="item.desc" placeholder="描述" style="flex:1;margin-right:8px" />
              <el-button size="small" type="danger" link @click="formItinerary.splice(idx, 1)">移除</el-button>
            </div>
            <el-button size="small" @click="formItinerary.push({ title: '', desc: '' })">+ 添加行程</el-button>
          </div>
        </el-form-item>

        <!-- 实用贴士 -->
        <el-form-item label="实用贴士">
          <div class="tips-list">
            <div class="tip-item" v-for="(tip, idx) in formTips" :key="idx">
              <el-input v-model="formTips[idx]" placeholder="输入贴士内容" style="flex:1;margin-right:8px" />
              <el-button size="small" type="danger" link @click="formTips.splice(idx, 1)">移除</el-button>
            </div>
            <el-button size="small" @click="formTips.push('')">+ 添加贴士</el-button>
          </div>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showEdit = false">取消</el-button>
        <el-button type="primary" @click="handleSave" :loading="saving">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Search, Edit, Delete, Check, Close, Star, View, Document, ChatDotRound, Warning } from '@element-plus/icons-vue'
import { guideApi } from '@/api'

const loading = ref(false)
const saving = ref(false)
const list = ref([])
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(10)
const searchKeyword = ref('')
const activeTab = ref('')
const showEdit = ref(false)
const editingRow = ref(null)

const form = ref({
  title: '', desc: '', content: '', author: '', authorAvatar: '',
  cover: '', hot: 0, status: 1
})
const formTags = ref([])
const formItinerary = ref([])
const formTips = ref([])
const newTag = ref('')

const statCards = ref([
  { key: 'total', label: '总攻略数', value: '0', icon: 'Document', color: '#1D3B5A', badge: null, badgeColor: '' },
  { key: 'pending', label: '待审核', value: '0', icon: 'ChatDotRound', color: '#E6A23C', badge: null, badgeColor: '#E6A23C' },
  { key: 'approved', label: '已发布', value: '0', icon: 'Check', color: '#67C23A', badge: null, badgeColor: '#67C23A' },
  { key: 'rejected', label: '已拒绝', value: '0', icon: 'Close', color: '#F56C6C', badge: null, badgeColor: '#F56C6C' }
])

const filteredList = computed(() => list.value)

function parseJsonField(val, defaultVal = []) {
  if (!val) return defaultVal
  if (Array.isArray(val)) return val
  try { return JSON.parse(val) } catch { return defaultVal }
}

function getGuideCover(row) {
  if (!row) return null
  if (row.cover) {
    if (Array.isArray(row.cover)) return row.cover.length > 0 ? row.cover[0] : null
    if (typeof row.cover === 'string') {
      try {
        const parsed = JSON.parse(row.cover)
        if (Array.isArray(parsed) && parsed.length > 0) return parsed[0]
      } catch { /* not JSON */ }
      return row.cover || null
    }
  }
  return null
}

function formatViews(views) {
  const num = Number(views) || 0
  if (num >= 10000) return (num / 10000).toFixed(1) + 'w'
  if (num >= 1000) return (num / 1000).toFixed(1) + 'k'
  return String(num)
}

function formatTime(time) {
  if (!time) return '-'
  if (typeof time === 'string' && time.includes('年')) return time
  const d = new Date(time)
  if (isNaN(d.getTime())) return '-'
  return `${d.getFullYear()}-${String(d.getMonth()+1).padStart(2,'0')}-${String(d.getDate()).padStart(2,'0')} ${String(d.getHours()).padStart(2,'0')}:${String(d.getMinutes()).padStart(2,'0')}`
}

function handleSearch() {
  currentPage.value = 1
  loadData()
}

async function loadData() {
  loading.value = true
  try {
    let auditStatusParam = ''
    if (activeTab.value === 'pending') auditStatusParam = '0'
    else if (activeTab.value === 'approved') auditStatusParam = '1'
    else if (activeTab.value === 'rejected') auditStatusParam = '2'

    const res = await guideApi.getPage(currentPage.value, pageSize.value, auditStatusParam, searchKeyword.value)
    // 兼容多种返回格式
    const responseData = res.data || res
    list.value = responseData.records || responseData.list || responseData.rows || []
    total.value = responseData.total || responseData.count || 0

    // 确保数据一致性：如果返回的数据条数与总数不一致，以实际返回的条数为准更新总数
    if (list.value.length > 0 && total.value === 0) {
      total.value = list.value.length
    }

    // 如果当前页超过总页数，自动跳转到第一页
    const totalPages = Math.ceil(total.value / pageSize.value)
    if (currentPage.value > totalPages && totalPages > 0) {
      currentPage.value = 1
      await loadData()
      return
    }

    // 更新统计 — 通过后端接口分别查询各审核状态的数量
    const [totalRes, pendingRes, approvedRes, rejectedRes] = await Promise.all([
      guideApi.getPage(1, 1, '', ''),
      guideApi.getPage(1, 1, '0', ''),
      guideApi.getPage(1, 1, '1', ''),
      guideApi.getPage(1, 1, '2', '')
    ])
    const getTotal = (r) => ((r.data || r).total || 0)
    statCards.value[0].value = String(getTotal(totalRes))
    statCards.value[1].value = String(getTotal(pendingRes))
    statCards.value[2].value = String(getTotal(approvedRes))
    statCards.value[3].value = String(getTotal(rejectedRes))
    statCards.value[1].badge = statCards.value[1].value
  } catch (e) {
    console.error('加载攻略列表失败', e)
    list.value = []
    total.value = 0
  }
  loading.value = false
}

// 审核攻略
async function auditGuide(row, auditStatus) {
  const action = auditStatus === 1 ? '通过' : '拒绝'
  await ElMessageBox.confirm(`确定${action}该攻略吗？`, '提示', { type: 'warning' })
  try {
    const updateData = {
      id: row.id,
      auditStatus: auditStatus,
      status: auditStatus === 1 ? 1 : 0, // 通过则上架，拒绝则下架
      title: row.title,
      desc: row.desc,
      content: row.content,
      author: row.author,
      authorAvatar: row.authorAvatar,
      cover: row.cover,
      views: row.views,
      tags: row.tags,
      tips: row.tips,
      itinerary: row.itinerary,
      hot: row.hot,
      publishTime: row.publishTime
    }
    await guideApi.update(updateData)
    ElMessage.success(`${action}成功`)
    loadData()
  } catch (e) {
    console.error('审核失败', e)
    ElMessage.error(`${action}失败`)
  }
}

function handleAdd() {
  editingRow.value = null
  form.value = { title: '', desc: '', content: '', author: '', authorAvatar: '', cover: '', hot: 0, status: 1 }
  formTags.value = []
  formItinerary.value = [{ title: '', desc: '' }]
  formTips.value = ['']
  showEdit.value = true
}

function handleEdit(row) {
  editingRow.value = row
  form.value = {
    id: row.id,
    title: row.title || '',
    desc: row.desc || '',
    content: row.content || '',
    author: row.author || '',
    authorAvatar: row.authorAvatar || row.author_avatar || '',
    cover: row.cover || '',
    hot: row.hot || 0,
    status: row.status !== undefined ? row.status : 1
  }
  formTags.value = parseJsonField(row.tags, [])
  formItinerary.value = parseJsonField(row.itinerary, [{ title: '', desc: '' }])
  formTips.value = parseJsonField(row.tips, [''])
  showEdit.value = true
}

function addTag() {
  const tag = newTag.value.trim()
  if (tag && !formTags.value.includes(tag)) {
    formTags.value.push(tag)
  }
  newTag.value = ''
}

async function handleSave() {
  if (!form.value.title) {
    ElMessage.warning('请输入攻略标题')
    return
  }
  saving.value = true
  const data = {
    ...form.value,
    tags: JSON.stringify(formTags.value),
    itinerary: JSON.stringify(formItinerary.value.filter(i => i.title)),
    tips: JSON.stringify(formTips.value.filter(t => t))
  }
  try {
    if (editingRow.value) {
      await guideApi.update(data)
      ElMessage.success('更新成功')
    } else {
      await guideApi.save(data)
      ElMessage.success('添加成功')
    }
    showEdit.value = false
    loadData()
  } catch (e) {
    console.error('保存失败', e)
    ElMessage.error('保存失败')
  }
  saving.value = false
}

async function handleDelete(id) {
  try {
    await guideApi.delete(id)
    ElMessage.success('删除成功')
    loadData()
  } catch (e) {
    ElMessage.error('删除失败')
  }
}

onMounted(() => loadData())
</script>

<style scoped lang="scss">
@use '@/styles/buttons.scss' as *;

.guides-page { padding: 20px; }
.deco-top-line { height: 3px; background: linear-gradient(90deg, #1D3B5A, #2E5F8E, #D4A373); border-radius: 2px; margin-bottom: 20px; }
.page-header { display: flex; justify-content: space-between; align-items: flex-start; margin-bottom: 20px; }
.page-title { font-size: 22px; margin: 0 0 4px; }
.gradient-text { background: linear-gradient(135deg, #1D3B5A, #2E5F8E); -webkit-background-clip: text; -webkit-text-fill-color: transparent; }
.page-sub { font-size: 13px; color: #999; margin: 0; }
.glass-card { background: #fff; border-radius: 12px; padding: 16px; margin-bottom: 16px; box-shadow: 0 2px 12px rgba(0,0,0,0.06); }
.search-bar { padding: 14px 18px; margin-bottom: 16px; }
.filter-group { display: flex; align-items: center; gap: 10px; flex-wrap: wrap; }
.table-card { padding: 0; overflow: hidden; }
.table-card :deep(.el-table) { --el-table-header-bg-color: #F5F7FA; }
.pagination-bar { display: flex; justify-content: flex-end; padding: 16px; }

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

.fade-in-up { opacity: 0; animation: fadeInUp 0.6s ease-out forwards; }
@keyframes fadeInUp { from { opacity: 0; transform: translateY(20px); } to { opacity: 1; transform: translateY(0); } }

.btn-glow { position: relative; overflow: hidden; }
.itinerary-list { width: 100%; }
.itinerary-item { display: flex; align-items: center; margin-bottom: 8px; }
.itinerary-day { font-size: 12px; color: #D4A373; font-weight: 700; margin-right: 8px; white-space: nowrap; }
.tips-list { width: 100%; }
.tip-item { display: flex; align-items: center; margin-bottom: 8px; }

// 按钮样式 - 与社区管理一致
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
</style>
