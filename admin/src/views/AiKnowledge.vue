<template>
  <div class="ai-page">
    <div class="deco-top-line"></div>

    <div class="page-header">
      <div class="header-left">
        <h2 class="page-title gradient-text">AI 智能助手管理</h2>
        <p class="page-sub">管理小程序AI知识库、监控对话记录、分析AI使用数据</p>
      </div>
      <div class="header-right">
        <div class="button-row">
          <el-button type="primary" plain class="btn-glow" @click="showAddDialog">
            <el-icon><Plus /></el-icon>
            新增知识条目
          </el-button>
        </div>
      </div>
    </div>

    <!-- AI 数据概览 -->
    <el-row :gutter="16" class="stat-row">
      <el-col :span="6" v-for="(stat, idx) in aiStats" :key="stat.key">
        <div class="stat-card glass-card fade-in-up" :style="{ animationDelay: (idx * 0.08) + 's' }">
          <div class="stat-icon-wrap" :style="{ background: stat.color + '15', color: stat.color }">
            <el-icon :size="22"><component :is="stat.icon" /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ stat.value }}</div>
            <div class="stat-label">{{ stat.label }}</div>
          </div>
          <div class="stat-trend" :class="stat.trendDir">{{ stat.trend }}</div>
        </div>
      </el-col>
    </el-row>

    <!-- Tab 切换 -->
    <div class="tab-bar glass-card">
      <div class="tab-group">
        <div class="tab-item" :class="{ active: activeTab === 'knowledge' }" @click="activeTab = 'knowledge'">
          <el-icon><Collection /></el-icon>
          系统提示词
        </div>
        <div class="tab-item" :class="{ active: activeTab === 'chat' }" @click="activeTab = 'chat'; loadChatLogs()">
          <el-icon><ChatDotSquare /></el-icon>
          对话监控
        </div>
        <div class="tab-item" :class="{ active: activeTab === 'analytics' }" @click="activeTab = 'analytics'">
          <el-icon><TrendCharts /></el-icon>
          数据分析
        </div>
      </div>
    </div>

    <!-- 知识库管理 -->
    <template v-if="activeTab === 'knowledge'">
      <div class="search-bar glass-card">
        <el-radio-group v-model="categoryFilter" @change="knowledgePage=1;loadKnowledge()" size="small">
          <el-radio-button value="prompt">系统提示词</el-radio-button>
        </el-radio-group>
        <el-input v-model="knowledgeSearch" placeholder="搜索知识条目" size="small" style="width:200px;margin-left:12px" clearable @keyup.enter="knowledgePage=1;loadKnowledge()">
          <template #prefix><el-icon><Search /></el-icon></template>
        </el-input>
        <div style="margin-left:auto;display:flex;gap:8px" v-if="knowledgeSelected.length">
          <el-button type="success" plain size="small" @click="handleBatchEnabled(1)">批量启用</el-button>
          <el-button type="warning" plain size="small" @click="handleBatchEnabled(0)">批量禁用</el-button>
          <el-button type="danger" plain size="small" @click="handleBatchDelete">批量删除</el-button>
        </div>
      </div>

      <div class="table-card glass-card">
        <el-table :data="knowledgeData" v-loading="knowledgeLoading" stripe @selection-change="knowledgeSelected = $event">
          <el-table-column type="selection" width="45" />
          <el-table-column label="类型" width="100">
            <template #default="{ row }">
              <el-tag type="warning" size="small" effect="dark">系统提示词</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="title" label="标题" min-width="160" />
          <el-table-column prop="content" label="内容预览" min-width="250" show-overflow-tooltip />
          <el-table-column label="状态" width="80">
            <template #default="{ row }">
              <el-switch v-model="row.enabled" :active-value="1" :inactive-value="0" size="small" @change="handleToggleEnabled(row)" />
            </template>
          </el-table-column>
          <el-table-column prop="sortOrder" label="排序" width="60" />
          <el-table-column prop="createTime" label="创建时间" width="160">
            <template #default="{ row }">
              <span style="font-size:12px;color:#999">{{ row.createTime || '-' }}</span>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="220" fixed="right">
            <template #default="{ row }">
              <div class="action-buttons">
                <div class="button-row">
                  <el-button type="primary" plain size="small" @click="handleEditKnowledge(row)">编辑</el-button>
                  <el-button type="info" plain size="small" @click="handleViewVersions(row)">版本</el-button>
                  <el-button type="danger" plain size="small" @click="handleDeleteKnowledge(row)">删除</el-button>
                </div>
              </div>
            </template>
          </el-table-column>
        </el-table>
        <div class="pagination-wrap">
          <el-pagination
            v-model:current-page="knowledgePage"
            v-model:page-size="knowledgeSize"
            :page-sizes="[10, 20, 50]"
            :total="knowledgeTotal"
            layout="total, sizes, prev, pager, next, jumper"
            @size-change="loadKnowledge"
            @current-change="loadKnowledge"
          />
        </div>
      </div>
    </template>

    <!-- 对话监控 -->
    <template v-if="activeTab === 'chat'">
      <div class="search-bar glass-card">
        <el-radio-group v-model="chatFilter" @change="chatPage=1;loadChatLogs()" size="small">
          <el-radio-button value="">全部对话</el-radio-button>
          <el-radio-button value="flagged">标记对话</el-radio-button>
          <el-radio-button value="today">今日</el-radio-button>
        </el-radio-group>
        <el-input v-model="chatSearch" placeholder="搜索对话内容" size="small" style="width:200px;margin-left:12px" clearable @keyup.enter="chatPage=1;loadChatLogs()">
          <template #prefix><el-icon><Search /></el-icon></template>
        </el-input>
      </div>

      <div class="chat-list" v-loading="chatLoading">
        <div class="chat-card glass-card fade-in-up" v-for="(chat, idx) in chatLogs" :key="chat.id" :style="{ animationDelay: (idx * 0.05) + 's' }">
          <div class="chat-header">
            <div class="chat-user" style="cursor:pointer" @click="handleViewChatDetail(chat)">
              <el-avatar :size="32" :style="{ background: chat.userColor || '#A33B32' }">{{ (chat.userName || '匿').charAt(0) }}</el-avatar>
              <div style="margin-left:10px">
                <div class="chat-username">{{ chat.userName || '匿名用户' }}</div>
                <div class="chat-time">{{ chat.createTime }}</div>
              </div>
            </div>
            <div class="chat-badges">
              <el-tag v-if="chat.flagged" size="small" type="danger" effect="dark">标记</el-tag>
              <el-tag v-if="chat.hasSensitive" size="small" type="warning">含敏感词</el-tag>
              <el-tag size="small" type="info">{{ chat.category }}</el-tag>
            </div>
          </div>
          <div class="chat-messages">
            <div class="msg-item user-msg">
              <span class="msg-label">用户</span>
              <span class="msg-text" v-html="highlightSensitive(chat.question, chat)"></span>
            </div>
            <div class="msg-item ai-msg">
              <span class="msg-label ai">AI</span>
              <span class="msg-text" v-html="highlightSensitive(chat.answer, chat)"></span>
            </div>
          </div>
          <div class="chat-footer">
            <span class="chat-metrics">响应时间: {{ chat.responseTime }}ms | Token: {{ chat.tokens || 0 }} | IP: {{ chat.ip || '-' }}</span>
            <div class="chat-actions">
              <el-button size="small" :type="chat.rating === 2 ? 'success' : 'default'" text @click="handleRateChat(chat, 2)">
                <el-icon><CircleCheck /></el-icon> 满意
              </el-button>
              <el-button size="small" :type="chat.rating === 1 ? 'danger' : 'default'" text @click="handleRateChat(chat, 1)">
                <el-icon><CircleClose /></el-icon> 不满意
              </el-button>
              <el-button size="small" text type="primary" @click="handleFlagChat(chat)">
                {{ chat.flagged ? '取消标记' : '标记' }}
              </el-button>
            </div>
          </div>
        </div>
        <el-empty v-if="!chatLogs.length && !chatLoading" description="暂无对话记录" />
      </div>
      <div class="pagination-wrap" style="margin-top:16px" v-if="chatTotal > 0">
        <el-pagination
          v-model:current-page="chatPage"
          v-model:page-size="chatSize"
          :page-sizes="[10, 20, 50]"
          :total="chatTotal"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="loadChatLogs"
          @current-change="loadChatLogs"
        />
      </div>
    </template>

    <!-- 数据分析 -->
    <template v-if="activeTab === 'analytics'">
      <!-- 时间筛选 -->
      <div class="search-bar glass-card">
        <span style="font-size:13px;color:#666;margin-right:12px">时间范围</span>
        <el-radio-group v-model="analyticsRange" @change="loadStats" size="small">
          <el-radio-button value="7">近7天</el-radio-button>
          <el-radio-button value="30">近30天</el-radio-button>
        </el-radio-group>
      </div>

      <!-- 评分统计卡片 -->
      <el-row :gutter="16" class="chart-row">
        <el-col :span="8">
          <div class="stat-card glass-card fade-in-up" style="animation-delay: 0.2s">
            <div class="stat-icon-wrap" style="background:rgba(103,194,58,0.1);color:#67C23A">
              <el-icon :size="22"><Check /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ ratingData.satisfied }}</div>
              <div class="stat-label">满意评价</div>
            </div>
          </div>
        </el-col>
        <el-col :span="8">
          <div class="stat-card glass-card fade-in-up" style="animation-delay: 0.25s">
            <div class="stat-icon-wrap" style="background:rgba(245,108,108,0.1);color:#F56C6C">
              <el-icon :size="22"><Close /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ ratingData.dissatisfied }}</div>
              <div class="stat-label">不满意评价</div>
            </div>
          </div>
        </el-col>
        <el-col :span="8">
          <div class="stat-card glass-card fade-in-up" style="animation-delay: 0.3s">
            <div class="stat-icon-wrap" style="background:rgba(64,158,255,0.1);color:#409EFF">
              <el-icon :size="22"><TrendCharts /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ ratingData.satisfactionRate }}</div>
              <div class="stat-label">满意度</div>
            </div>
          </div>
        </el-col>
      </el-row>

      <el-row :gutter="16" class="chart-row">
        <el-col :span="12">
          <div class="chart-card glass-card fade-in-up" style="animation-delay: 0.3s">
            <div class="card-header">
              <div class="card-title"><span class="title-accent"></span>AI对话趋势</div>
            </div>
            <div ref="chatTrendRef" style="height:280px"></div>
          </div>
        </el-col>
        <el-col :span="12">
          <div class="chart-card glass-card fade-in-up" style="animation-delay: 0.35s">
            <div class="card-header">
              <div class="card-title"><span class="title-accent"></span>问题分类分布</div>
            </div>
            <div ref="categoryChartRef" style="height:280px"></div>
          </div>
        </el-col>
      </el-row>
      <el-row :gutter="16" class="chart-row">
        <!-- 热门问题 Top 10 -->
        <el-col :span="24">
          <div class="chart-card glass-card fade-in-up" style="animation-delay: 0.4s">
            <div class="card-header">
              <div class="card-title"><span class="title-accent"></span>热门问题 Top 10</div>
            </div>
            <div class="hot-questions-list">
              <div class="hot-item" v-for="(item, idx) in hotQuestionsList" :key="idx">
                <span class="hot-rank" :class="'rank-' + (idx + 1)">{{ idx + 1 }}</span>
                <div class="hot-content">
                  <div class="hot-question">{{ item.question }}</div>
                  <div class="hot-meta">
                    <el-tag size="small" type="info">{{ item.category }}</el-tag>
                    <span class="hot-freq">{{ item.frequency }}次</span>
                    <span class="hot-dislike" v-if="item.dislike_count > 0">{{ item.dislike_count }}差评</span>
                  </div>
                </div>
              </div>
              <el-empty v-if="!hotQuestionsList.length" description="暂无数据" :image-size="60" />
            </div>
          </div>
        </el-col>
      </el-row>
      <el-row :gutter="16" class="chart-row">
        <!-- 知识库使用排行 -->
        <el-col :span="12">
          <div class="chart-card glass-card fade-in-up" style="animation-delay: 0.5s">
            <div class="card-header">
              <div class="card-title"><span class="title-accent"></span>知识库使用排行</div>
            </div>
            <div class="knowledge-rank">
              <div class="rank-item" v-for="(item, idx) in knowledgeRank" :key="idx" :style="{ animationDelay: (idx * 0.06) + 's' }">
                <span class="rank-num" :class="'rank-' + (idx + 1)">{{ idx + 1 }}</span>
                <span class="rank-title">{{ item.title }}</span>
                <div class="rank-bar-wrap"><div class="rank-bar" :style="{ width: item.percent + '%' }"></div></div>
                <span class="rank-count">{{ item.count }}次</span>
              </div>
              <el-empty v-if="!knowledgeRank.length" description="暂无数据" :image-size="60" />
            </div>
          </div>
        </el-col>
        <!-- 响应质量雷达图 -->
        <el-col :span="12">
          <div class="chart-card glass-card fade-in-up" style="animation-delay: 0.55s">
            <div class="card-header">
              <div class="card-title"><span class="title-accent"></span>AI响应质量评估</div>
            </div>
            <div ref="qualityChartRef" style="height:280px"></div>
          </div>
        </el-col>
      </el-row>
    </template>

    <!-- 编辑知识弹窗 -->
    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑知识条目' : '新增知识条目'" width="700px">
      <el-form :model="form" label-width="80px">
        <el-form-item label="类型">
          <el-tag type="warning" size="small" effect="dark">系统提示词</el-tag>
        </el-form-item>
        <el-form-item label="标题">
          <el-input v-model="form.title" placeholder="条目标题" />
        </el-form-item>
        <el-form-item label="内容">
          <el-input v-model="form.content" type="textarea" :rows="8" placeholder="知识内容" />
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="form.sortOrder" :min="0" />
        </el-form-item>
        <el-form-item label="状态">
          <el-switch v-model="form.enabled" :active-value="1" :inactive-value="0" />
          <span style="margin-left:8px;color:#999">{{ form.enabled === 1 ? '启用' : '禁用' }}</span>
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="button-row">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleSubmitKnowledge">确定</el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 版本历史弹窗 -->
    <el-dialog v-model="versionDialogVisible" title="知识库版本历史" width="700px">
      <div v-if="versionData" class="version-content">
        <div class="version-current">
          <div class="version-label">当前版本 (v{{ versionData.version || 0 }})</div>
          <div class="version-text">{{ versionData.content }}</div>
        </div>
        <div class="version-history" v-if="versionData.original_content">
          <div class="version-label">上一版本</div>
          <div class="version-text version-old">{{ versionData.original_content }}</div>
          <el-button type="warning" size="small" @click="handleRollback" style="margin-top:12px">
            回滚到上一版本
          </el-button>
        </div>
        <el-empty v-else description="暂无历史版本" :image-size="80" />
      </div>
      <template #footer>
        <el-button @click="versionDialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>

    <!-- 对话详情弹窗 -->
    <el-dialog v-model="chatDetailVisible" title="对话详情" width="650px">
      <div v-if="chatDetail" class="chat-detail">
        <div class="detail-meta">
          <el-descriptions :column="2" border size="small">
            <el-descriptions-item label="用户">{{ chatDetail.userName || '匿名用户' }}</el-descriptions-item>
            <el-descriptions-item label="时间">{{ chatDetail.createTime }}</el-descriptions-item>
            <el-descriptions-item label="分类">
              <el-tag size="small" type="info">{{ chatDetail.category }}</el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="评分">
              <el-tag v-if="chatDetail.rating === 2" size="small" type="success">满意</el-tag>
              <el-tag v-else-if="chatDetail.rating === 1" size="small" type="danger">不满意</el-tag>
              <el-tag v-else size="small" type="info">未评价</el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="响应时间">{{ chatDetail.responseTime }}ms</el-descriptions-item>
            <el-descriptions-item label="Token">{{ chatDetail.tokens || 0 }}</el-descriptions-item>
            <el-descriptions-item label="IP">{{ chatDetail.ip || '-' }}</el-descriptions-item>
            <el-descriptions-item label="敏感词">
              <el-tag v-if="chatDetail.hasSensitive" size="small" type="warning">{{ chatDetail.sensitiveWords || '-' }}</el-tag>
              <span v-else style="color:#999">无</span>
            </el-descriptions-item>
          </el-descriptions>
        </div>
        <div class="detail-messages" style="margin-top:16px">
          <div class="msg-item user-msg">
            <span class="msg-label">用户</span>
            <span class="msg-text" v-html="highlightSensitive(chatDetail.question, chatDetail)"></span>
          </div>
          <div class="msg-item ai-msg">
            <span class="msg-label ai">AI</span>
            <span class="msg-text" v-html="highlightSensitive(chatDetail.answer, chatDetail)"></span>
          </div>
        </div>
      </div>
      <template #footer>
        <el-button @click="chatDetailVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, nextTick, watch } from 'vue'
import * as echarts from 'echarts'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Search, ChatDotSquare, Collection, TrendCharts, Clock, User, Check, Close, CircleCheck, CircleClose } from '@element-plus/icons-vue'
import { aiKnowledgeApi, aiChatApi } from '@/api'

const activeTab = ref('knowledge')
const knowledgeLoading = ref(false)
const knowledgeData = ref([])
const categoryFilter = ref('prompt')
const knowledgeSearch = ref('')
const knowledgePage = ref(1)
const knowledgeSize = ref(10)
const knowledgeTotal = ref(0)
const knowledgeSelected = ref([])
const dialogVisible = ref(false)
const isEdit = ref(false)
const form = ref({ id: null, category: 'prompt', title: '', content: '', enabled: 1, sortOrder: 0 })

// 对话监控
const chatFilter = ref('')
const chatSearch = ref('')
const chatLogs = ref([])
const chatLoading = ref(false)
const chatPage = ref(1)
const chatSize = ref(10)
const chatTotal = ref(0)
const chatDetailVisible = ref(false)
const chatDetail = ref(null)

// AI统计
const aiStats = ref([
  { key: 'totalChats', label: '总对话数', value: '0', icon: 'ChatDotSquare', color: '#A33B32', trend: '', trendDir: '' },
  { key: 'todayChats', label: '今日对话', value: '0', icon: 'Clock', color: '#1D3B5A', trend: '', trendDir: '' },
  { key: 'avgResponse', label: '平均响应', value: '0ms', icon: 'TrendCharts', color: '#D4A373', trend: '', trendDir: '' },
  { key: 'satisfaction', label: '满意度', value: '0%', icon: 'User', color: '#67C23A', trend: '', trendDir: '' }
])

const knowledgeRank = ref([])
const hotQuestionsList = ref([])
const ratingData = ref({ satisfied: 0, dissatisfied: 0, satisfactionRate: '0%' })
const versionDialogVisible = ref(false)
const versionData = ref(null)
const currentVersionId = ref(null)

const chatTrendRef = ref()
const categoryChartRef = ref()
const qualityChartRef = ref()
const analyticsRange = ref('7')

// 监听Tab切换
watch(activeTab, async (newTab) => {
  if (newTab === 'analytics') {
    await nextTick()
    await loadStats()
  }
})

async function loadKnowledge() {
  knowledgeLoading.value = true
  try {
    const params = {}
    if (categoryFilter.value) params.category = categoryFilter.value
    if (knowledgeSearch.value) params.keyword = knowledgeSearch.value
    const res = await aiKnowledgeApi.getPage(knowledgePage.value, knowledgeSize.value, params)
    knowledgeData.value = res.data.records || []
    knowledgeTotal.value = res.data.total || 0
  } catch { ElMessage.error('获取知识库列表失败') }
  finally { knowledgeLoading.value = false }
}

async function handleToggleEnabled(row) {
  try {
    await aiKnowledgeApi.update(row)
    ElMessage.success(row.enabled === 1 ? '已启用' : '已禁用')
  } catch {
    row.enabled = row.enabled === 1 ? 0 : 1
    ElMessage.error('操作失败')
  }
}

async function handleBatchEnabled(enabled) {
  const ids = knowledgeSelected.value.map(r => r.id)
  try {
    await aiKnowledgeApi.batchEnabled(ids, enabled)
    ElMessage.success(enabled === 1 ? '批量启用成功' : '批量禁用成功')
    loadKnowledge()
  } catch { ElMessage.error('操作失败') }
}

async function handleBatchDelete() {
  await ElMessageBox.confirm(`确认删除选中的 ${knowledgeSelected.value.length} 条知识条目？`, '确认删除', { type: 'warning' })
  const ids = knowledgeSelected.value.map(r => r.id)
  try {
    await aiKnowledgeApi.batchDelete(ids)
    ElMessage.success('批量删除成功')
    loadKnowledge()
  } catch { ElMessage.error('删除失败') }
}

async function loadChatLogs() {
  chatLoading.value = true
  try {
    const params = {}
    if (chatFilter.value === 'flagged') params.flagged = 1
    if (chatFilter.value === 'today') params.today = true
    if (chatSearch.value) params.keyword = chatSearch.value
    const res = await aiChatApi.getPage(chatPage.value, chatSize.value, params)
    chatLogs.value = res.data.list || []
    chatTotal.value = res.data.total || 0
  } catch { ElMessage.error('获取对话记录失败') }
  finally { chatLoading.value = false }
}

function highlightSensitive(text, chat) {
  if (!text) return ''
  let result = text.replace(/</g, '&lt;').replace(/>/g, '&gt;')
  if (chat.hasSensitive && chat.sensitiveWords) {
    try {
      const words = typeof chat.sensitiveWords === 'string' ? JSON.parse(chat.sensitiveWords) : chat.sensitiveWords
      if (Array.isArray(words)) {
        words.forEach(w => {
          const escaped = w.replace(/[.*+?^${}()|[\]\\]/g, '\\$&')
          result = result.replace(new RegExp(escaped, 'g'), `<span class="sensitive-highlight">${w}</span>`)
        })
      }
    } catch {}
  }
  return result
}

function handleViewChatDetail(chat) {
  chatDetail.value = chat
  chatDetailVisible.value = true
}

async function handleRateChat(chat, rating) {
  try {
    await aiChatApi.rateChat(chat.id, rating)
    chat.rating = rating
    ElMessage.success(rating === 2 ? '已标记为满意' : '已标记为不满意')
  } catch { ElMessage.error('评分失败') }
}

function showAddDialog() {
  isEdit.value = false
  form.value = { id: null, category: 'prompt', title: '', content: '', enabled: 1, sortOrder: 0 }
  dialogVisible.value = true
}

function handleEditKnowledge(row) {
  isEdit.value = true
  form.value = { ...row }
  dialogVisible.value = true
}

async function handleSubmitKnowledge() {
  try {
    if (isEdit.value) await aiKnowledgeApi.update(form.value)
    else await aiKnowledgeApi.save(form.value)
    ElMessage.success(isEdit.value ? '更新成功' : '保存成功')
    dialogVisible.value = false
    loadKnowledge()
  } catch { ElMessage.error('操作失败') }
}

async function handleDeleteKnowledge(row) {
  await ElMessageBox.confirm('确认删除该知识条目？', '确认删除', { type: 'warning' })
  try {
    await aiKnowledgeApi.delete(row.id)
    ElMessage.success('删除成功')
    loadKnowledge()
  } catch { ElMessage.error('删除失败') }
}

async function handleFlagChat(chat) {
  const newFlagged = chat.flagged ? 0 : 1
  try {
    await aiChatApi.toggleFlag(chat.id, newFlagged)
    chat.flagged = newFlagged
    ElMessage.success(newFlagged ? '已标记对话' : '已取消标记')
  } catch { ElMessage.error('操作失败') }
}

async function loadStats() {
  try {
    const [analyticsRes, ratingRes, hotRes] = await Promise.all([
      aiChatApi.getAdvancedAnalytics(),
      aiChatApi.ratingStats(),
      aiChatApi.hotQuestions()
    ])
    const stats = analyticsRes.data || {}
    stats.ratingStats = ratingRes.data || {}

    // 更新统计卡片数据
    updateStatCards(stats)

    // 更新评分统计（满意、不满意、满意度）
    const r = ratingRes.data || {}
    const satisfied = r.satisfied_count || 0
    const dissatisfied = r.dissatisfied_count || 0
    const total = satisfied + dissatisfied
    ratingData.value = {
      satisfied,
      dissatisfied,
      satisfactionRate: total > 0 ? ((satisfied / total) * 100).toFixed(1) + '%' : '0%'
    }

    // 更新热门问题Top 10
    hotQuestionsList.value = (hotRes.data || []).slice(0, 10)

    // 更新知识库使用排行
    if (stats.knowledgeUsage && stats.knowledgeUsage.length > 0) {
      const maxUsage = Math.max(...stats.knowledgeUsage.map(k => k.usage_count || 0))
      knowledgeRank.value = stats.knowledgeUsage.slice(0, 10).map(k => ({
        title: k.knowledge_title || '未命名',
        count: k.usage_count || 0,
        percent: maxUsage > 0 ? Math.round((k.usage_count / maxUsage) * 100) : 0
      }))
    } else {
      knowledgeRank.value = []
    }

    // 更新图表数据
    await nextTick()
    initChartsWithData(stats)
  } catch (error) {
    console.error('加载统计数据失败:', error)
  }
}

function initChartsWithData(stats) {
  // 对话趋势
  if (chatTrendRef.value) {
    const chart = echarts.init(chatTrendRef.value)
    const dailyTrend = stats.dailyTrend || stats.dailyStats || []
    const days = dailyTrend.map(d => {
      const date = new Date(d.date)
      return `${date.getMonth() + 1}/${date.getDate()}`
    })
    const counts = dailyTrend.map(d => d.count)
    chart.setOption({
      color: ['#A33B32'],
      tooltip: { trigger: 'axis', backgroundColor: 'rgba(255,255,255,0.96)', borderColor: '#e8e4dc', textStyle: { color: '#333' }, extraCssText: 'box-shadow:0 4px 20px rgba(0,0,0,0.12);border-radius:8px' },
      grid: { top: 20, right: 20, bottom: 30, left: 40 },
      xAxis: { type: 'category', data: days.length ? days : ['暂无数据'], axisLine: { lineStyle: { color: '#e8e4dc' } }, axisLabel: { color: '#666' } },
      yAxis: { type: 'value', splitLine: { lineStyle: { color: '#f0f0f0', type: 'dashed' } }, axisLabel: { color: '#999' } },
      series: [{ name: '对话数', type: 'line', smooth: true, symbol: 'circle', symbolSize: 6, lineStyle: { width: 2.5 }, areaStyle: { color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [{ offset: 0, color: 'rgba(163,59,50,0.2)' }, { offset: 1, color: 'rgba(163,59,50,0)' }]) }, data: counts.length ? counts : [0] }]
    })
  }

  // 问题分类
  if (categoryChartRef.value) {
    const chart = echarts.init(categoryChartRef.value)
    const categoryStats = stats.categoryStats || []
    chart.setOption({
      color: ['#A33B32', '#1D3B5A', '#D4A373', '#67C23A', '#409EFF', '#E6A23C', '#909399', '#bbb'],
      tooltip: { trigger: 'item', formatter: '{b}: {c}次 ({d}%)' },
      series: [{
        type: 'pie', radius: ['40%', '70%'], center: ['50%', '50%'],
        itemStyle: { borderRadius: 5, borderColor: '#fff', borderWidth: 2 },
        label: { fontSize: 11, color: '#666' },
        data: categoryStats.length ? categoryStats.map(c => ({ name: c.category || '其他', value: c.count })) : [{ name: '暂无数据', value: 1 }]
      }]
    })
  }

  // 响应质量雷达图
  if (qualityChartRef.value) {
    const chart = echarts.init(qualityChartRef.value)
    const avgTime = stats.avgResponseTime?.avg_time || 0
    const speedScore = Math.max(0, Math.min(100, Math.round(100 - avgTime / 50)))

    // 基于真实评分数据计算满意度分数
    const r = stats.ratingStats || {}
    const satisfied = r.satisfied_count || 0
    const dissatisfied = r.dissatisfied_count || 0
    const totalRated = satisfied + dissatisfied
    const satisfactionScore = totalRated > 0 ? Math.round((satisfied / totalRated) * 100) : 0

    // 基于分类丰富度计算内容丰富度
    const categoryStats = stats.categoryStats || []
    const categoryCount = categoryStats.length
    const richnessScore = Math.min(100, 30 + categoryCount * 10)

    // 基于标记比例计算安全合规分数
    const totalCount = stats.totalCount || 1
    const flaggedCount = stats.flaggedCount || 0
    const safetyScore = Math.max(0, Math.min(100, Math.round(100 - (flaggedCount / totalCount) * 200)))

    // 基于热门问题数量和平均对话长度计算准确度
    const hotCount = (stats.hotQuestions || []).length
    const accuracyScore = Math.min(100, 40 + hotCount * 4 + (satisfactionScore / 4))
    
    // 基于分类多样性和平均响应时间计算理解力
    const understandingScore = Math.min(100, 35 + categoryCount * 8 + Math.max(0, (200 - avgTime) / 5))

    chart.setOption({
      color: ['#A33B32'],
      tooltip: { 
        trigger: 'item', 
        backgroundColor: 'rgba(255,255,255,0.96)', 
        borderColor: '#e8e4dc', 
        textStyle: { color: '#333' }, 
        extraCssText: 'box-shadow:0 4px 20px rgba(0,0,0,0.12);border-radius:8px',
        formatter: function(params) {
          let result = params.name + '<br/>'
          params.value.forEach((value, index) => {
            const indicator = params.indicator[index]
            result += indicator.name + ': ' + value + '分<br/>'
          })
          return result
        }
      },
      radar: {
        indicator: [
          { name: '响应速度', max: 100 },
          { name: '回答准确度', max: 100 },
          { name: '用户满意度', max: 100 },
          { name: '问题理解', max: 100 },
          { name: '内容丰富度', max: 100 },
          { name: '安全合规', max: 100 }
        ],
        shape: 'polygon', 
        splitNumber: 5,
        axisName: { 
          color: '#666', 
          fontSize: 12,
          fontWeight: '500'
        },
        splitArea: { 
          areaStyle: { 
            color: ['rgba(163,59,50,0.02)', 'rgba(163,59,50,0.04)', 'rgba(163,59,50,0.06)', 'rgba(163,59,50,0.08)', 'rgba(163,59,50,0.10)'] 
          } 
        },
        splitLine: { 
          lineStyle: { 
            color: 'rgba(0,0,0,0.06)' 
          } 
        },
        axisLine: { 
          lineStyle: { 
            color: 'rgba(0,0,0,0.08)' 
          } 
        }
      },
      series: [{
        type: 'radar',
        data: [
          { 
            value: [speedScore, accuracyScore, satisfactionScore, understandingScore, richnessScore, safetyScore], 
            name: '质量评分', 
            areaStyle: { 
              color: new echarts.graphic.RadialGradient(0.5, 0.5, 1, [
                { offset: 0, color: 'rgba(163,59,50,0.3)' },
                { offset: 1, color: 'rgba(163,59,50,0.1)' }
              ])
            }, 
            lineStyle: { 
              color: '#A33B32', 
              width: 3 
            }, 
            itemStyle: { 
              color: '#A33B32' 
            } 
          }
        ]
      }]
    })
  }
}

function updateStatCards(stats) {
  const totalCount = stats.totalCount || 0
  const todayCount = stats.todayCount || 0
  const avgTime = stats.avgResponseTime?.avg_time || 0

  // 使用真实评分数据计算满意度
  const r = stats.ratingStats || {}
  const satisfied = r.satisfied_count || 0
  const dissatisfied = r.dissatisfied_count || 0
  const totalRated = satisfied + dissatisfied
  const satisfaction = totalRated > 0 ? ((satisfied / totalRated) * 100) : 0

  aiStats.value = [
    { key: 'totalChats', label: '总对话数', value: totalCount.toString(), icon: 'ChatDotSquare', color: '#A33B32', trend: '', trendDir: '' },
    { key: 'todayChats', label: '今日对话', value: todayCount.toString(), icon: 'Clock', color: '#1D3B5A', trend: '', trendDir: '' },
    { key: 'avgResponse', label: '平均响应', value: Math.round(avgTime) + 'ms', icon: 'TrendCharts', color: '#D4A373', trend: '', trendDir: '' },
    { key: 'satisfaction', label: '满意度', value: (totalRated > 0 ? satisfaction.toFixed(1) : '0') + '%', icon: 'User', color: '#67C23A', trend: '', trendDir: '' }
  ]
}



async function handleViewVersions(row) {
  currentVersionId.value = row.id
  try {
    const res = await aiKnowledgeApi.getVersions(row.id)
    versionData.value = res.data || {}
    versionDialogVisible.value = true
  } catch (e) {
    ElMessage.error('获取版本历史失败')
  }
}

async function handleRollback() {
  await ElMessageBox.confirm('确认回滚到上一版本？当前内容将被覆盖。', '确认回滚', { type: 'warning' })
  try {
    await aiKnowledgeApi.rollback(currentVersionId.value)
    ElMessage.success('回滚成功')
    versionDialogVisible.value = false
    loadKnowledge()
  } catch (e) {
    ElMessage.error('回滚失败')
  }
}

onMounted(async () => {
  await loadKnowledge()
  await loadStats()
  await nextTick()
})
</script>

<style scoped lang="scss">
.deco-top-line { height: 3px; background: linear-gradient(90deg, #A33B32, #1D3B5A, #D4A373, #A33B32); background-size: 300% 100%; animation: gradientShift 4s ease infinite; border-radius: 2px; margin-bottom: 24px; }
@keyframes gradientShift { 0%, 100% { background-position: 0% 50%; } 50% { background-position: 100% 50%; } }
.gradient-text { background: linear-gradient(135deg, #1D3B5A 0%, #A33B32 100%); -webkit-background-clip: text; -webkit-text-fill-color: transparent; background-clip: text; }
.glass-card { background: rgba(255,255,255,0.85); backdrop-filter: blur(12px); -webkit-backdrop-filter: blur(12px); border: 1px solid rgba(255,255,255,0.6); border-radius: 16px; box-shadow: 0 4px 24px rgba(0,0,0,0.06), 0 1px 4px rgba(0,0,0,0.04), inset 0 1px 0 rgba(255,255,255,0.8); transition: all 0.3s cubic-bezier(0.4,0,0.2,1); &:hover { transform: translateY(-2px); box-shadow: 0 8px 32px rgba(0,0,0,0.1), 0 2px 8px rgba(0,0,0,0.06), inset 0 1px 0 rgba(255,255,255,0.8); } }
.fade-in-up { opacity: 0; animation: fadeInUp 0.6s ease-out forwards; }
@keyframes fadeInUp { from { opacity: 0; transform: translateY(20px); } to { opacity: 1; transform: translateY(0); } }

.ai-page { position: relative; }
.page-header { margin-bottom: 20px; display: flex; justify-content: space-between; align-items: center; animation: fadeInUp 0.5s ease-out; }
.header-left { flex: 1; }
.header-right { display: flex; align-items: center; gap: 12px; }
.page-title { font-size: 26px; font-weight: 800; margin-bottom: 4px; }
.page-sub { color: #999; font-size: 14px; }
.btn-glow { position: relative; overflow: hidden; }

// 统计
.stat-row { margin-bottom: 16px; }
.stat-card { padding: 16px 18px; display: flex; align-items: center; gap: 12px; position: relative; }
.stat-icon-wrap { width: 42px; height: 42px; border-radius: 12px; display: flex; align-items: center; justify-content: center; flex-shrink: 0; transition: transform 0.3s; }
.stat-card:hover .stat-icon-wrap { transform: scale(1.08) rotate(-3deg); }
.stat-info { flex: 1; }
.stat-value { font-size: 22px; font-weight: 800; color: #1D3B5A; }
.stat-label { font-size: 12px; color: #999; }
.stat-trend { font-size: 11px; font-weight: 600; padding: 2px 8px; border-radius: 8px; &.up { color: #67C23A; background: rgba(103,194,58,0.1); } }

// Tab
.tab-bar { padding: 4px; margin-bottom: 16px; display: inline-flex; }
.tab-group { display: flex; gap: 4px; }
.tab-item { padding: 10px 20px; font-size: 14px; font-weight: 500; color: #666; border-radius: 12px; cursor: pointer; transition: all 0.3s; display: flex; align-items: center; gap: 6px; &:hover { color: #1D3B5A; background: rgba(29,59,90,0.05); } &.active { color: #A33B32; background: rgba(163,59,50,0.08); font-weight: 600; } }

// 搜索
.search-bar { padding: 14px 18px; margin-bottom: 16px; display: flex; align-items: center; }

// 表格
.table-card { padding: 20px; margin-bottom: 16px; }
.pagination-wrap { margin-top: 16px; display: flex; justify-content: flex-end; }
.action-edit { background: linear-gradient(135deg, #1D3B5A, #2E5F8E) !important; border-color: #1D3B5A !important; color: white !important; &:hover { background: linear-gradient(135deg, #2E5F8E, #1D3B5A) !important; } }
.action-delete { background: linear-gradient(135deg, #A33B32, #C94A40) !important; border-color: #A33B32 !important; color: white !important; &:hover { background: linear-gradient(135deg, #C94A40, #A33B32) !important; } }

// 对话监控
.chat-list { display: flex; flex-direction: column; gap: 12px; }
.chat-card { padding: 18px; }
.chat-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 12px; }
.chat-user { display: flex; align-items: center; }
.chat-username { font-size: 14px; font-weight: 600; color: #1D3B5A; }
.chat-time { font-size: 11px; color: #bbb; }
.chat-badges { display: flex; gap: 6px; }
.chat-messages { background: #f8f9fa; border-radius: 10px; padding: 14px; margin-bottom: 12px; }
.msg-item { display: flex; gap: 10px; margin-bottom: 10px; &:last-child { margin-bottom: 0; } }
.msg-label { font-size: 11px; font-weight: 700; padding: 2px 8px; border-radius: 6px; background: #1D3B5A; color: white; flex-shrink: 0; height: 20px; line-height: 20px; &.ai { background: #A33B32; } }
.msg-text { font-size: 13px; color: #555; line-height: 1.6; white-space: pre-wrap; }
:deep(.sensitive-highlight) { background: rgba(245,108,108,0.15); color: #F56C6C; padding: 1px 4px; border-radius: 3px; font-weight: 600; }
.chat-footer { display: flex; justify-content: space-between; align-items: center; }
.chat-metrics { font-size: 11px; color: #bbb; }

// 图表
.chart-row { margin-bottom: 16px; }
.chart-card { padding: 20px; }
.card-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 14px; }
.card-title { font-size: 15px; font-weight: 700; color: #1D3B5A; display: flex; align-items: center; gap: 8px; .title-accent { width: 4px; height: 16px; background: linear-gradient(180deg, #A33B32, #D4A373); border-radius: 2px; flex-shrink: 0; } }

// 知识排行
.knowledge-rank { display: flex; flex-direction: column; gap: 12px; }
.rank-item { display: flex; align-items: center; gap: 12px; animation: fadeInUp 0.4s ease-out backwards; padding: 10px; background: #f8f9fa; border-radius: 10px; transition: all 0.3s; &:hover { background: #f0f4f8; transform: translateX(4px); } }
.rank-num { width: 28px; height: 28px; border-radius: 8px; font-size: 13px; font-weight: 800; display: flex; align-items: center; justify-content: center; background: #f0f0f0; color: #999; flex-shrink: 0; }
.rank-num.rank-1 { background: linear-gradient(135deg, #A33B32, #c0504a); color: #fff; box-shadow: 0 2px 8px rgba(163,59,50,0.25); }
.rank-num.rank-2 { background: linear-gradient(135deg, #1D3B5A, #2E5F8E); color: #fff; box-shadow: 0 2px 8px rgba(29,59,90,0.25); }
.rank-num.rank-3 { background: linear-gradient(135deg, #D4A373, #e6b989); color: #fff; box-shadow: 0 2px 8px rgba(212,163,115,0.25); }
.rank-title { font-size: 14px; color: #333; font-weight: 600; flex: 1; min-width: 0; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.rank-bar-wrap { width: 100px; height: 8px; background: rgba(0,0,0,0.06); border-radius: 4px; overflow: hidden; flex-shrink: 0; }
.rank-bar { height: 100%; border-radius: 4px; background: linear-gradient(90deg, #A33B32, #c0504a, #1D3B5A); transition: width 1s ease-out; box-shadow: 0 1px 4px rgba(163,59,50,0.2); }
.rank-count { font-size: 13px; color: #666; font-weight: 600; width: 60px; text-align: right; flex-shrink: 0; }

// 热门问题
.hot-questions-list { display: flex; flex-direction: column; gap: 12px; max-height: 420px; overflow-y: auto; padding-right: 4px; }
.hot-item { display: flex; align-items: flex-start; gap: 12px; padding: 14px; background: #f8f9fa; border-radius: 10px; border-bottom: none; transition: all 0.3s; &:hover { background: #f0f4f8; transform: translateX(4px); } &:last-child { border-bottom: none; } }
.hot-rank { width: 32px; height: 32px; border-radius: 8px; font-size: 14px; font-weight: 800; display: flex; align-items: center; justify-content: center; background: #f0f0f0; color: #999; flex-shrink: 0; margin-top: 0; &.rank-1 { background: linear-gradient(135deg, #A33B32, #c0504a); color: #fff; box-shadow: 0 2px 8px rgba(163,59,50,0.25); } &.rank-2 { background: linear-gradient(135deg, #1D3B5A, #2E5F8E); color: #fff; box-shadow: 0 2px 8px rgba(29,59,90,0.25); } &.rank-3 { background: linear-gradient(135deg, #D4A373, #e6b989); color: #fff; box-shadow: 0 2px 8px rgba(212,163,115,0.25); } }
.hot-content { flex: 1; min-width: 0; }
.hot-question { font-size: 14px; color: #333; line-height: 1.6; margin-bottom: 8px; display: -webkit-box; -webkit-line-clamp: 2; -webkit-box-orient: vertical; overflow: hidden; font-weight: 500; }
.hot-meta { display: flex; align-items: center; gap: 10px; font-size: 12px; color: #999; flex-wrap: wrap; }
.hot-freq { color: #409EFF; font-weight: 600; display: flex; align-items: center; gap: 3px; }
.hot-dislike { color: #F56C6C; font-weight: 600; display: flex; align-items: center; gap: 3px; }

// 版本历史
.version-content { }
.version-current, .version-history { margin-bottom: 16px; }
.version-label { font-size: 13px; font-weight: 600; color: #1D3B5A; margin-bottom: 8px; }
.version-text { font-size: 13px; color: #555; line-height: 1.7; padding: 12px; background: #f8f9fa; border-radius: 8px; max-height: 200px; overflow-y: auto; }
.version-old { background: #fef9e7; border: 1px dashed #E6A23C; }

// 对话详情
.chat-detail { }
.detail-meta { margin-bottom: 16px; }
.detail-messages { background: #f8f9fa; border-radius: 10px; padding: 16px; }

// 响应式
@media screen and (max-width: 1200px) { .stat-row .el-col { span: 12 !important; margin-bottom: 12px; } }
@media screen and (max-width: 768px) { .stat-row .el-col { span: 24 !important; } .chart-row .el-col { span: 24 !important; margin-bottom: 16px; } .page-header { flex-direction: column; align-items: flex-start; gap: 12px; } .tab-bar { width: 100%; overflow-x: auto; } }
</style>
