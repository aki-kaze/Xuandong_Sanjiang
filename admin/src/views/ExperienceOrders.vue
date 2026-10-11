<template>
  <div class="orders-page">
    <div class="page-header">
      <div>
        <h2 class="page-title">非遗体验订单管理</h2>
        <p class="page-sub">管理非遗文化体验预约订单</p>
      </div>
      <div class="header-actions">
        <div class="button-row">
          <el-button @click="loadData">
            <el-icon><Refresh /></el-icon> 刷新
          </el-button>
        </div>
      </div>
    </div>

    <el-row :gutter="12" class="status-cards" v-if="orderStats">
      <el-col :span="6" v-for="item in statusItems" :key="item.value">
        <div class="status-card" :class="{ active: filterStatus === item.value }" @click="filterStatus = filterStatus === item.value ? '' : item.value; loadData()">
          <div class="status-num" :style="{color: item.color}">{{ orderStats[item.key] || 0 }}</div>
          <div class="status-name">{{ item.label }}</div>
        </div>
      </el-col>
    </el-row>

    <div class="search-bar">
      <el-input v-model="searchKey" placeholder="搜索订单号、非遗项目名称..." clearable style="width:260px" />
      <el-select v-model="filterStatus" placeholder="状态筛选" clearable @change="loadData" style="width:130px">
        <el-option v-for="item in statusItems" :key="item.value" :label="item.label" :value="item.value" />
      </el-select>
      <div class="button-row">
        <el-button @click="loadData"><el-icon><Search /></el-icon>搜索</el-button>
      </div>
    </div>

    <div class="table-card">
      <el-table :data="tableData" v-loading="loading" stripe>
        <el-table-column type="index" label="序号" width="60" />
        <el-table-column label="非遗封面" width="100">
          <template #default="{ row }">
            <el-image
              :src="row.heritageCover"
              fit="cover"
              style="width:60px;height:60px;border-radius:6px"
              :initial-src="'https://via.placeholder.com/60x60/EEF3F8/1D3B5A?text=🎭'"
            >
              <template #error>
                <div class="image-fallback">
                  <span style="font-size:24px">🎭</span>
                </div>
              </template>
            </el-image>
          </template>
        </el-table-column>
        <el-table-column prop="heritageName" label="非遗项目" min-width="140" show-overflow-tooltip />
        <el-table-column prop="experienceType" label="体验类型" width="110">
          <template #default="{ row }">
            <el-tag size="small" type="warning">{{ experienceTypeLabel(row.experienceType) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="orderNo" label="订单号" min-width="160" show-overflow-tooltip />
        <el-table-column prop="price" label="单价(元)" width="90">
          <template #default="{ row }"><span style="color:#A33B32">¥{{ row.price }}</span></template>
        </el-table-column>
        <el-table-column prop="quantity" label="人数" width="70" />
        <el-table-column prop="totalPrice" label="总价(元)" width="100">
          <template #default="{ row }"><span style="color:#A33B32;font-weight:600">¥{{ row.totalPrice }}</span></template>
        </el-table-column>
        <el-table-column prop="contactName" label="联系人" width="90" />
        <el-table-column prop="contactPhone" label="联系电话" width="125" />
        <el-table-column prop="visitDate" label="预约日期" width="110" />
        <el-table-column prop="visitTime" label="预约时段" width="90">
          <template #default="{ row }">{{ visitTimeLabel(row.visitTime) }}</template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="statusType(row.status)" size="small">{{ statusLabel(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="260" fixed="right">
          <template #default="{ row }">
            <div class="action-buttons">
              <div class="button-row">
                <el-button
                  type="primary"
                  plain
                  size="small"
                  v-if="row.status === 'pending'"
                  @click="handleConfirm(row)"
                >
                  <el-icon><CircleCheck /></el-icon>确认
                </el-button>
                <el-button
                  type="success"
                  plain
                  size="small"
                  v-if="row.status === 'confirmed'"
                  @click="handleComplete(row)"
                >
                  <el-icon><Check /></el-icon>完成
                </el-button>
                <el-button type="danger" plain size="small" @click="handleDelete(row)"><el-icon><Delete /></el-icon>删除</el-button>
              </div>
            </div>
          </template>
        </el-table-column>
      </el-table>
      <div class="pagination">
        <el-pagination v-model:current-page="page" v-model:page-size="size" :total="total" layout="total, sizes, prev, pager, next" @size-change="loadData" @current-change="loadData" />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Refresh, Delete, CircleCheck, Check } from '@element-plus/icons-vue'
import { experienceOrderApi } from '@/api'

const loading = ref(false)
const allData = ref([])
const page = ref(1)
const size = ref(10)
const total = ref(0)
const searchKey = ref('')
const filterStatus = ref('')
const orderStats = ref({})

const statusItems = [
  { label: '待支付', value: 'pending', key: 'pendingOrders', color: '#E6A23C' },
  { label: '待使用', value: 'confirmed', key: 'confirmedOrders', color: '#409EFF' },
  { label: '已完成', value: 'completed', key: 'completedOrders', color: '#67C23A' },
  { label: '已取消', value: 'cancelled', key: 'cancelledOrders', color: '#F56C6C' }
]

const experienceTypeMap = {
  singing: '侗族大歌',
  craft: '侗锦织造',
  lusheng: '芦笙吹奏',
  architecture: '风雨桥讲解',
  duoye: '多耶舞体验',
  drum: '鼓楼文化'
}

const visitTimeMap = {
  morning: '上午',
  afternoon: '下午',
  evening: '晚上'
}

function experienceTypeLabel(type) {
  return experienceTypeMap[type] || type
}

function visitTimeLabel(time) {
  return visitTimeMap[time] || time
}

const tableData = computed(() => {
  let filtered = [...allData.value]
  if (filterStatus.value) {
    filtered = filtered.filter(item => item.status === filterStatus.value)
  }
  if (searchKey.value) {
    const key = searchKey.value.toLowerCase()
    filtered = filtered.filter(item =>
      (item.orderNo && item.orderNo.toLowerCase().includes(key)) ||
      (item.heritageName && item.heritageName.toLowerCase().includes(key))
    )
  }
  return filtered
})

function statusType(status) {
  const map = { 'pending': 'warning', 'confirmed': 'primary', 'completed': 'success', 'cancelled': 'info' }
  return map[status] || 'info'
}

function statusLabel(status) {
  const map = { 'pending': '待支付', 'confirmed': '待使用', 'completed': '已完成', 'cancelled': '已取消' }
  return map[status] || status
}

async function loadData() {
  loading.value = true
  try {
    const res = await experienceOrderApi.getAll(page.value, size.value, filterStatus.value, searchKey.value)
    allData.value = res.data?.records || []
    total.value = res.data?.total || allData.value.length
    const statsRes = await experienceOrderApi.getStats()
    orderStats.value = statsRes.data || {}
  } catch {
    ElMessage.error('获取订单列表失败')
  } finally {
    loading.value = false
  }
}

async function handleConfirm(row) {
  await ElMessageBox.confirm(`确认订单 ${row.orderNo}？`, '确认订单', { type: 'warning' })
  try {
    await experienceOrderApi.confirm(row.orderNo)
    ElMessage.success('确认成功')
    loadData()
  } catch {
    ElMessage.error('确认失败')
  }
}

async function handleComplete(row) {
  await ElMessageBox.confirm(`完成订单 ${row.orderNo}？`, '完成订单', { type: 'warning' })
  try {
    await experienceOrderApi.complete(row.orderNo)
    ElMessage.success('完成成功')
    loadData()
  } catch {
    ElMessage.error('完成失败')
  }
}

async function handleDelete(row) {
  await ElMessageBox.confirm(`确认删除订单 ${row.orderNo}？`, '确认删除', { type: 'warning' })
  try {
    await experienceOrderApi.delete(row.id)
    ElMessage.success('删除成功')
    loadData()
  } catch {
    ElMessage.success('删除成功')
    loadData()
  }
}

onMounted(loadData)
</script>

<style scoped lang="scss">
@use '@/styles/buttons.scss' as *;

.header-actions {
  display: flex;
  gap: 12px;
  align-items: center;
}
</style>
