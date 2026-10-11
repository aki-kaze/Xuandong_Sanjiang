<template>
  <div class="sensitive-page">
    <div class="deco-top-line"></div>

    <div class="page-header">
      <div class="header-left">
        <h2 class="page-title gradient-text">敏感词管理</h2>
        <p class="page-sub">管理社区内容敏感词过滤规则，实时检测与统计分析</p>
      </div>
      <div class="header-right">
        <div class="button-row">
          <el-button plain @click="showBatchDialog = true">
            <el-icon><Upload /></el-icon>
            批量导入
          </el-button>
          <el-button type="primary" plain class="btn-glow" @click="handleAdd">
            <el-icon><Plus /></el-icon>
            新增敏感词
          </el-button>
        </div>
      </div>
    </div>

    <!-- 统计卡片 -->
    <el-row :gutter="16" class="stat-row">
      <el-col :span="6" v-for="(stat, idx) in statsCards" :key="stat.key">
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

    <!-- 搜索筛选 -->
    <div class="search-bar glass-card">
      <el-select v-model="categoryFilter" placeholder="分类筛选" clearable size="small" style="width:130px">
        <el-option label="政治" value="政治" />
        <el-option label="色情" value="色情" />
        <el-option label="暴力" value="暴力" />
        <el-option label="广告" value="广告" />
        <el-option label="其他" value="其他" />
      </el-select>
      <el-select v-model="levelFilter" placeholder="严重程度" clearable size="small" style="width:130px;margin-left:10px">
        <el-option label="轻度" :value="1" />
        <el-option label="中度" :value="2" />
        <el-option label="严重" :value="3" />
      </el-select>
      <el-input v-model="searchWord" placeholder="搜索敏感词" size="small" style="width:180px;margin-left:10px" clearable @keyup.enter="filterData">
        <template #prefix><el-icon><Search /></el-icon></template>
      </el-input>
      <div class="button-row">
        <el-button size="small" @click="filterData" style="margin-left:8px">搜索</el-button>
        <el-button size="small" @click="showTestDialog = true" type="warning">
          <el-icon><View /></el-icon>
          实时检测
        </el-button>
      </div>
    </div>

    <!-- 敏感词表格 -->
    <div class="table-card glass-card">
      <el-table :data="paginatedData" v-loading="loading" stripe>
        <el-table-column label="#" width="60" align="center">
          <template #default="{ $index }">
            <span style="font-weight: 600; color: #1D3B5A;">{{ (page - 1) * pageSize + $index + 1 }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="word" label="敏感词" min-width="100">
          <template #default="{ row }">
            <span style="font-weight:600;color:#A33B32">{{ row.word }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="category" label="分类" width="100">
          <template #default="{ row }">
            <el-tag size="small" :type="categoryType(row.category)" effect="dark">{{ row.category }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="level" label="严重程度" width="200">
          <template #default="{ row }">
            <div style="display:flex;align-items:center;gap:6px">
              <div class="level-bar" :style="{ width: (row.level * 30) + 'px', background: levelColor(row.level) }"></div>
              <el-tag size="small" :type="levelType(row.level)">{{ levelLabel(row.level) }}</el-tag>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="命中统计" width="100">
          <template #default="{ row }">
            <span style="font-size:13px;color:#666">{{ row.hitCount || 0 }} 次</span>
          </template>
        </el-table-column>
        <el-table-column label="最近命中" width="160">
          <template #default="{ row }">
            <span style="font-size:12px;color:#999">{{ row.lastHit || '-' }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="创建时间" width="160">
          <template #default="{ row }">
            <span style="font-size:12px;color:#999">{{ row.createdAt || '-' }}</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="160" fixed="right">
          <template #default="{ row }">
            <div class="action-buttons">
              <div class="button-row">
                <el-button type="primary" plain size="small" @click="handleEdit(row)">编辑</el-button>
                <el-button type="danger" plain size="small" @click="handleDelete(row)">删除</el-button>
              </div>
            </div>
          </template>
        </el-table-column>
      </el-table>
      <div class="pagination">
        <el-pagination v-model:current-page="page" v-model:page-size="pageSize" :total="totalCount" layout="total, prev, pager, next" />
      </div>
    </div>

    <!-- 分类统计 -->
    <el-row :gutter="16" class="bottom-row">
      <el-col :span="12">
        <div class="chart-card glass-card fade-in-up" style="animation-delay: 0.5s">
          <div class="card-header">
            <div class="card-title"><span class="title-accent"></span>分类分布</div>
          </div>
          <div ref="categoryChartRef" style="height:260px"></div>
        </div>
      </el-col>
      <el-col :span="12">
        <div class="chart-card glass-card fade-in-up" style="animation-delay: 0.55s">
          <div class="card-header">
            <div class="card-title"><span class="title-accent"></span>命中趋势（近7天）</div>
          </div>
          <div ref="hitTrendRef" style="height:260px"></div>
        </div>
      </el-col>
    </el-row>

    <!-- 新增/编辑弹窗 -->
    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑敏感词' : '新增敏感词'" width="500px">
      <el-form :model="form" label-width="80px">
        <el-form-item label="敏感词">
          <el-input v-model="form.word" placeholder="输入敏感词" />
        </el-form-item>
        <el-form-item label="分类">
          <el-select v-model="form.category" placeholder="选择分类">
            <el-option label="政治" value="政治" />
            <el-option label="色情" value="色情" />
            <el-option label="暴力" value="暴力" />
            <el-option label="广告" value="广告" />
            <el-option label="其他" value="其他" />
          </el-select>
        </el-form-item>
        <el-form-item label="严重程度">
          <el-select v-model="form.level" placeholder="选择严重程度">
            <el-option label="轻度" :value="1" />
            <el-option label="中度" :value="2" />
            <el-option label="严重" :value="3" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="button-row">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleSubmit">确定</el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 批量导入弹窗 -->
    <el-dialog v-model="showBatchDialog" title="批量导入敏感词" width="550px">
      <el-alert title="每行输入一个敏感词，格式：敏感词,分类,严重程度（如：广告词,广告,2）" type="info" :closable="false" style="margin-bottom:16px" />
      <el-input v-model="batchText" type="textarea" :rows="10" placeholder="广告,广告,2&#10;赌博,其他,3&#10;代购,广告,1" />
      <template #footer>
        <div class="button-row">
          <el-button @click="showBatchDialog = false">取消</el-button>
          <el-button type="primary" @click="handleBatchImport">导入</el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 实时检测弹窗 -->
    <el-dialog v-model="showTestDialog" title="实时敏感词检测" width="550px">
      <el-input v-model="testText" type="textarea" :rows="6" placeholder="输入要检测的文本内容..." />
      <div class="test-result" v-if="testResult">
        <div class="result-header">
          <el-tag :type="testResult.hasSensitive ? 'danger' : 'success'" size="large" effect="dark">
            {{ testResult.hasSensitive ? '包含敏感词' : '内容安全' }}
          </el-tag>
        </div>
        <div class="result-detail" v-if="testResult.hasSensitive">
          <div class="found-words">
            <span class="found-label">检测到的敏感词：</span>
            <el-tag v-for="(w, idx) in testResult.words" :key="idx" type="danger" size="small" style="margin:2px">
              {{ w }}
            </el-tag>
          </div>
          <div class="result-highlight" v-html="testResult.highlighted"></div>
        </div>
      </div>
      <template #footer>
        <div class="button-row">
          <el-button @click="showTestDialog = false">关闭</el-button>
          <el-button type="primary" @click="handleTestCheck">检测</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, nextTick, watch } from 'vue'
import * as echarts from 'echarts'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Search, Upload, View } from '@element-plus/icons-vue'
import { sensitiveWordApi } from '@/api'

const loading = ref(false)
const tableData = ref([])
const page = ref(1)
const pageSize = ref(10)
const categoryFilter = ref('')
const levelFilter = ref('')
const searchWord = ref('')
const dialogVisible = ref(false)
const isEdit = ref(false)
const form = ref({ id: null, word: '', category: '其他', level: 1 })
const showBatchDialog = ref(false)
const batchText = ref('')
const showTestDialog = ref(false)
const testText = ref('')
const testResult = ref(null)
const categoryChartRef = ref()
const hitTrendRef = ref()

const statsCards = ref([
  { key: 'total', label: '敏感词总数', value: '0', icon: 'Warning', color: '#A33B32' },
  { key: 'today', label: '今日命中', value: '0', icon: 'View', color: '#E6A23C' },
  { key: 'blocked', label: '已拦截', value: '0', icon: 'CircleClose', color: '#1D3B5A' },
  { key: 'categories', label: '分类数', value: '0', icon: 'Folder', color: '#67C23A' }
])

const filteredData = computed(() => {
  let data = tableData.value
  if (categoryFilter.value) data = data.filter(i => i.category === categoryFilter.value)
  if (levelFilter.value) data = data.filter(i => i.level === levelFilter.value)
  if (searchWord.value) data = data.filter(i => i.word.includes(searchWord.value))
  return data
})

const paginatedData = computed(() => {
  const start = (page.value - 1) * pageSize.value
  const end = start + pageSize.value
  return filteredData.value.slice(start, end)
})

const totalCount = computed(() => filteredData.value.length)

// 监听筛选条件变化，重置页码
watch([categoryFilter, levelFilter, searchWord], () => {
  page.value = 1
})

function categoryType(cat) {
  const map = { '政治': 'danger', '色情': 'danger', '暴力': 'warning', '广告': 'info', '其他': '' }
  return map[cat] || ''
}
function levelType(level) {
  const map = { 1: 'info', 2: 'warning', 3: 'danger' }
  return map[level] || ''
}
function levelLabel(level) {
  const map = { 1: '轻度', 2: '中度', 3: '严重' }
  return map[level] || level
}
function levelColor(level) {
  const map = { 1: '#409EFF', 2: '#E6A23C', 3: '#A33B32' }
  return map[level] || '#999'
}

async function loadData() {
  loading.value = true
  try {
    const res = await sensitiveWordApi.getAll()
    tableData.value = res.data || []
    statsCards.value[0].value = String(tableData.value.length)
    // 统计分类数
    const categories = new Set(tableData.value.map(i => i.category))
    statsCards.value[3].value = String(categories.size)
  } catch { ElMessage.error('获取敏感词列表失败') }
  finally { loading.value = false }
}

async function loadStats() {
  try {
    const res = await sensitiveWordApi.getStats()
    statsCards.value[1].value = String(res.data.todayHits || 0)
    statsCards.value[2].value = String(res.data.totalHits || 0)
  } catch {}
}

function filterData() {
  // computed 自动过滤
}

function handleAdd() {
  isEdit.value = false
  form.value = { id: null, word: '', category: '其他', level: 1 }
  dialogVisible.value = true
}

function handleEdit(row) {
  isEdit.value = true
  form.value = { ...row }
  dialogVisible.value = true
}

async function handleSubmit() {
  try {
    if (isEdit.value) await sensitiveWordApi.update(form.value)
    else await sensitiveWordApi.save(form.value)
    ElMessage.success(isEdit.value ? '更新成功' : '保存成功')
    dialogVisible.value = false
    loadData()
  } catch { ElMessage.error('操作失败') }
}

async function handleDelete(row) {
  await ElMessageBox.confirm('确认删除该敏感词？', '确认删除', { type: 'warning' })
  try {
    await sensitiveWordApi.delete(row.id)
    ElMessage.success('删除成功')
    loadData()
  } catch { ElMessage.error('删除失败') }
}

async function handleBatchImport() {
  if (!batchText.value.trim()) { ElMessage.warning('请输入要导入的敏感词'); return }
  const lines = batchText.value.trim().split('\n')
  let count = 0
  for (const line of lines) {
    const parts = line.split(',')
    if (parts.length >= 1) {
      const word = parts[0].trim()
      const category = parts[1]?.trim() || '其他'
      const level = parseInt(parts[2]?.trim()) || 1
      if (word) {
        try {
          await sensitiveWordApi.save({ word, category, level })
          count++
        } catch {}
      }
    }
  }
  ElMessage.success(`成功导入 ${count} 个敏感词`)
  showBatchDialog.value = false
  batchText.value = ''
  loadData()
}

async function handleTestCheck() {
  if (!testText.value.trim()) { ElMessage.warning('请输入要检测的文本'); return }
  try {
    const res = await sensitiveWordApi.check(testText.value)
    testResult.value = {
      hasSensitive: res.data.hasSensitive,
      words: res.data.words || [],
      highlighted: testText.value
    }
    if (res.data.hasSensitive) {
      let highlighted = testText.value
      for (const word of res.data.words) {
        highlighted = highlighted.replace(new RegExp(word, 'g'), `<span style="background:#ffebee;color:#A33B32;padding:2px 4px;border-radius:4px;font-weight:600">${word}</span>`)
      }
      testResult.value.highlighted = highlighted
    }
  } catch (e) {
    ElMessage.error('检测失败')
  }
}

function initCharts() {
  if (categoryChartRef.value) {
    const chart = echarts.init(categoryChartRef.value)
    const catData = {}
    tableData.value.forEach(w => { catData[w.category] = (catData[w.category] || 0) + 1 })
    chart.setOption({
      color: ['#A33B32', '#1D3B5A', '#D4A373', '#67C23A', '#409EFF'],
      tooltip: { trigger: 'item', formatter: '{b}: {c}个 ({d}%)' },
      series: [{
        type: 'pie', radius: ['40%', '70%'], center: ['50%', '50%'],
        itemStyle: { borderRadius: 5, borderColor: '#fff', borderWidth: 2 },
        label: { fontSize: 12, color: '#666' },
        data: Object.entries(catData).map(([name, value]) => ({ name, value }))
      }]
    })
  }

  if (hitTrendRef.value) {
    const chart = echarts.init(hitTrendRef.value)
    chart.setOption({
      color: ['#A33B32'],
      tooltip: { trigger: 'axis', backgroundColor: 'rgba(255,255,255,0.96)', borderColor: '#e8e4dc', textStyle: { color: '#333' }, extraCssText: 'box-shadow:0 4px 20px rgba(0,0,0,0.12);border-radius:8px' },
      grid: { top: 20, right: 20, bottom: 30, left: 40 },
      xAxis: { type: 'category', data: ['周一', '周二', '周三', '周四', '周五', '周六', '周日'], axisLine: { lineStyle: { color: '#e8e4dc' } }, axisLabel: { color: '#666' } },
      yAxis: { type: 'value', splitLine: { lineStyle: { color: '#f0f0f0', type: 'dashed' } }, axisLabel: { color: '#999' } },
      series: [{
        type: 'bar', barWidth: 28,
        itemStyle: {
          borderRadius: [6, 6, 0, 0],
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: '#A33B32' },
            { offset: 1, color: 'rgba(163,59,50,0.3)' }
          ])
        },
        data: [35, 48, 42, 55, 38, 62, 42],
        label: { show: true, position: 'top', color: '#666', fontSize: 12 }
      }]
    })
  }
}

onMounted(async () => {
  await loadData()
  await loadStats()
  await nextTick()
  initCharts()
})
</script>

<style scoped lang="scss">
.deco-top-line { height: 3px; background: linear-gradient(90deg, #A33B32, #1D3B5A, #D4A373, #A33B32); background-size: 300% 100%; animation: gradientShift 4s ease infinite; border-radius: 2px; margin-bottom: 24px; }
@keyframes gradientShift { 0%, 100% { background-position: 0% 50%; } 50% { background-position: 100% 50%; } }
.gradient-text { background: linear-gradient(135deg, #1D3B5A 0%, #A33B32 100%); -webkit-background-clip: text; -webkit-text-fill-color: transparent; background-clip: text; }
.glass-card { background: rgba(255,255,255,0.85); backdrop-filter: blur(12px); -webkit-backdrop-filter: blur(12px); border: 1px solid rgba(255,255,255,0.6); border-radius: 16px; box-shadow: 0 4px 24px rgba(0,0,0,0.06), 0 1px 4px rgba(0,0,0,0.04), inset 0 1px 0 rgba(255,255,255,0.8); transition: all 0.3s cubic-bezier(0.4,0,0.2,1); &:hover { transform: translateY(-2px); box-shadow: 0 8px 32px rgba(0,0,0,0.1), 0 2px 8px rgba(0,0,0,0.06), inset 0 1px 0 rgba(255,255,255,0.8); } }
.fade-in-up { opacity: 0; animation: fadeInUp 0.6s ease-out forwards; }
@keyframes fadeInUp { from { opacity: 0; transform: translateY(20px); } to { opacity: 1; transform: translateY(0); } }

.sensitive-page { position: relative; }
.page-header { margin-bottom: 20px; display: flex; justify-content: space-between; align-items: center; animation: fadeInUp 0.5s ease-out; }
.header-left { flex: 1; }
.header-right { display: flex; align-items: center; gap: 10px; }
.page-title { font-size: 26px; font-weight: 800; margin-bottom: 4px; }
.page-sub { color: #999; font-size: 14px; }
.btn-glow { position: relative; overflow: hidden; }
.btn-secondary { background: #fafafa; border-color: #e8e4dc; color: #666; &:hover { background: #f0f0f0; border-color: #d0d0d0; color: #333; } }

// 统计
.stat-row { margin-bottom: 16px; }
.stat-card { padding: 16px 18px; display: flex; align-items: center; gap: 12px; }
.stat-icon-wrap { width: 42px; height: 42px; border-radius: 12px; display: flex; align-items: center; justify-content: center; flex-shrink: 0; transition: transform 0.3s; }
.stat-card:hover .stat-icon-wrap { transform: scale(1.08) rotate(-3deg); }
.stat-info { flex: 1; }
.stat-value { font-size: 22px; font-weight: 800; color: #1D3B5A; }
.stat-label { font-size: 12px; color: #999; }

// 搜索
.search-bar { padding: 14px 18px; margin-bottom: 16px; display: flex; align-items: center; }

// 表格
.table-card { padding: 20px; margin-bottom: 16px; }
.level-bar { height: 6px; border-radius: 3px; flex-shrink: 0; }
.action-edit { background: linear-gradient(135deg, #1D3B5A, #2E5F8E) !important; border-color: #1D3B5A !important; color: white !important; &:hover { background: linear-gradient(135deg, #2E5F8E, #1D3B5A) !important; } }
.action-delete { background: linear-gradient(135deg, #A33B32, #C94A40) !important; border-color: #A33B32 !important; color: white !important; &:hover { background: linear-gradient(135deg, #C94A40, #A33B32) !important; } }

// 图表
.bottom-row { margin-bottom: 0; }
.chart-card { padding: 20px; }
.card-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 14px; }
.card-title { font-size: 15px; font-weight: 700; color: #1D3B5A; display: flex; align-items: center; gap: 8px; .title-accent { width: 4px; height: 16px; background: linear-gradient(180deg, #A33B32, #D4A373); border-radius: 2px; flex-shrink: 0; } }

// 检测结果
.test-result { margin-top: 16px; padding: 16px; background: #f8f9fa; border-radius: 12px; }
.result-header { margin-bottom: 12px; }
.found-words { margin-bottom: 12px; }
.found-label { font-size: 13px; color: #666; font-weight: 500; }
.result-highlight { font-size: 14px; color: #333; line-height: 1.8; padding: 12px; background: white; border-radius: 8px; border: 1px solid #eee; }

// 响应式
@media screen and (max-width: 1200px) { .stat-row .el-col { span: 12 !important; margin-bottom: 12px; } }
@media screen and (max-width: 768px) { .stat-row .el-col { span: 24 !important; } .bottom-row .el-col { span: 24 !important; margin-bottom: 16px; } .page-header { flex-direction: column; align-items: flex-start; gap: 12px; } .search-bar { flex-wrap: wrap; gap: 8px; } }
</style>
