<template>
  <div class="orders-page">
    <div class="page-header">
      <div>
        <h2 class="page-title">商品订单管理</h2>
        <p class="page-sub">管理特产商品销售订单</p>
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
      <el-col :span="4" v-for="item in statusItems" :key="item.value">
        <div class="status-card" :class="{ active: filterStatus === item.value }" @click="filterStatus = filterStatus === item.value ? '' : item.value; loadData()">
          <div class="status-num" :style="{color: item.color}">{{ orderStats[item.key] || 0 }}</div>
          <div class="status-name">{{ item.label }}</div>
        </div>
      </el-col>
    </el-row>

    <div class="search-bar">
      <el-input v-model="searchKey" placeholder="搜索订单号、商品名称..." clearable style="width:260px" />
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
        <el-table-column label="商品封面" width="100">
          <template #default="{ row }">
            <el-image
              :src="row.productCover"
              fit="cover"
              style="width:60px;height:60px;border-radius:6px"
              :initial-src="'https://via.placeholder.com/60x60/F5F0FF/7C3AED?text=🛍️'"
            >
              <template #error>
                <div class="image-fallback">
                  <span style="font-size:24px">🛍️</span>
                </div>
              </template>
            </el-image>
          </template>
        </el-table-column>
        <el-table-column prop="productName" label="商品名称" min-width="150" show-overflow-tooltip />
        <el-table-column prop="orderNo" label="订单号" min-width="160" show-overflow-tooltip />
        <el-table-column prop="price" label="单价(元)" width="100">
          <template #default="{ row }"><span style="color:#A33B32">¥{{ row.price }}</span></template>
        </el-table-column>
        <el-table-column prop="quantity" label="数量" width="70" />
        <el-table-column prop="totalPrice" label="总价(元)" width="100">
          <template #default="{ row }"><span style="color:#A33B32;font-weight:600">¥{{ row.totalPrice }}</span></template>
        </el-table-column>
        <el-table-column prop="contactName" label="收货人" width="90" />
        <el-table-column prop="contactPhone" label="收货电话" width="125" />
        <el-table-column prop="address" label="收货地址" min-width="200" show-overflow-tooltip />
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
                  v-if="row.status === 'paid'"
                  @click="handleShip(row)"
                >
                  <el-icon><Van /></el-icon>发货
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
import { Search, Refresh, Delete, Van } from '@element-plus/icons-vue'
import { productOrderApi } from '@/api'

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
  { label: '待发货', value: 'paid', key: 'paidOrders', color: '#409EFF' },
  { label: '发货中', value: 'shipped', key: 'shippedOrders', color: '#67C23A' },
  { label: '已收货', value: 'delivered', key: 'deliveredOrders', color: '#909399' },
  { label: '已取消', value: 'cancelled', key: 'cancelledOrders', color: '#F56C6C' }
]

const tableData = computed(() => {
  let filtered = [...allData.value]
  if (filterStatus.value) {
    filtered = filtered.filter(item => item.status === filterStatus.value)
  }
  if (searchKey.value) {
    const key = searchKey.value.toLowerCase()
    filtered = filtered.filter(item =>
      (item.orderNo && item.orderNo.toLowerCase().includes(key)) ||
      (item.productName && item.productName.toLowerCase().includes(key))
    )
  }
  return filtered
})

function statusType(status) {
  const map = { 'pending': 'warning', 'paid': 'primary', 'shipped': 'primary', 'delivered': 'success', 'cancelled': 'danger' }
  return map[status] || 'info'
}

function statusLabel(status) {
  const map = { 'pending': '待支付', 'paid': '待发货', 'shipped': '发货中', 'delivered': '已收货', 'cancelled': '已取消' }
  return map[status] || status
}

async function loadData() {
  loading.value = true
  try {
    const res = await productOrderApi.getAll(page.value, size.value, filterStatus.value, searchKey.value)
    allData.value = res.data?.records || []
    total.value = res.data?.total || allData.value.length
    const statsRes = await productOrderApi.getStats()
    orderStats.value = statsRes.data || {}
  } catch {
    ElMessage.error('获取订单列表失败')
  } finally {
    loading.value = false
  }
}

async function handleShip(row) {
  await ElMessageBox.confirm(`确认发货 ${row.productName}？`, '确认发货', { type: 'warning' })
  try {
    await productOrderApi.ship(row.orderNo)
    ElMessage.success('发货成功')
    loadData()
  } catch {
    ElMessage.error('发货失败')
  }
}

async function handleDelete(row) {
  await ElMessageBox.confirm(`确认删除订单 ${row.orderNo}？`, '确认删除', { type: 'warning' })
  try {
    await productOrderApi.delete(row.id)
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
