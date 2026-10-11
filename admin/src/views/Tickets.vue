<template>
  <div class="tickets-page">
    <div class="page-header">
      <div>
        <h2 class="page-title">门票管理</h2>
        <p class="page-sub">管理三江景区门票信息</p>
      </div>
      <el-button type="primary" @click="handleAdd" class="add-btn">
        <el-icon><Plus /></el-icon> 添加门票
      </el-button>
    </div>

    <div class="search-bar">
      <el-input v-model="searchForm.keyword" placeholder="搜索门票名称..." clearable @clear="getTicketList" @keyup.enter="handleSearch" style="width:280px">
        <template #prefix><el-icon><Search /></el-icon></template>
      </el-input>
      <el-select v-model="searchForm.type" placeholder="类型筛选" clearable @change="handleSearch" style="width:140px">
        <el-option label="景点门票" value="景点门票" />
        <el-option label="活动门票" value="活动门票" />
        <el-option label="演出门票" value="演出门票" />
      </el-select>
      <el-button @click="handleSearch">搜索</el-button>
      <el-button @click="resetSearch">重置</el-button>
    </div>

    <div class="table-card">
      <el-table :data="ticketList" v-loading="loading" stripe row-key="id">
        <el-table-column type="index" label="#" width="50" />
        <el-table-column label="封面" width="80">
          <template #default="{ row }">
            <el-image :src="row.cover" fit="cover" style="width:50px;height:40px;border-radius:4px" />
          </template>
        </el-table-column>
        <el-table-column prop="name" label="门票名称" min-width="150" />
        <el-table-column prop="type" label="类型" width="110">
          <template #default="{ row }">
            <el-tag size="small" type="warning">{{ row.type }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="price" label="现价(元)" width="100" />
        <el-table-column prop="originalPrice" label="原价(元)" width="100" />
        <el-table-column prop="validDays" label="有效天数" width="100" />
        <el-table-column label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'" size="small">{{ row.status === 1 ? '上架' : '下架' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="160" fixed="right">
          <template #default="{ row }">
            <div class="action-buttons">
              <el-button size="small" class="action-edit" @click="handleEdit(row)">编辑</el-button>
              <el-button size="small" class="action-delete" @click="handleDelete(row.id)">删除</el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>
      <div class="pagination">
        <el-pagination
          v-model:current-page="pagination.current"
          v-model:page-size="pagination.size"
          :total="pagination.total"
          layout="total, prev, pager, next"
          @change="getTicketList"
        />
      </div>
    </div>

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="580px">
      <el-form :model="formData" :rules="formRules" ref="formRef" label-width="90px">
        <el-form-item label="门票名称" prop="name">
          <el-input v-model="formData.name" placeholder="请输入门票名称" />
        </el-form-item>
        <el-form-item label="类型" prop="type">
          <el-select v-model="formData.type" placeholder="请选择类型" style="width:100%">
            <el-option label="景点门票" value="景点门票" />
            <el-option label="活动门票" value="活动门票" />
            <el-option label="演出门票" value="演出门票" />
          </el-select>
        </el-form-item>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="价格(元)" prop="price">
              <el-input-number v-model="formData.price" :min="0" style="width:100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="原价(元)">
              <el-input-number v-model="formData.originalPrice" :min="0" style="width:100%" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="有效天数">
          <el-input-number v-model="formData.validDays" :min="1" style="width:100%" />
        </el-form-item>
        <el-form-item label="封面URL">
          <el-input v-model="formData.cover" placeholder="请输入封面图片URL" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="formData.description" type="textarea" :rows="2" placeholder="请输入门票描述" />
        </el-form-item>
        <el-form-item label="包含内容">
          <el-input v-model="formData.includes" type="textarea" :rows="2" placeholder="请输入包含内容" />
        </el-form-item>
        <el-form-item label="注意事项">
          <el-input v-model="formData.notice" type="textarea" :rows="2" placeholder="请输入注意事项" />
        </el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="formData.status">
            <el-radio :value="1">上架</el-radio>
            <el-radio :value="0">下架</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSave" class="submit-btn">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Search } from '@element-plus/icons-vue'
import { ticketApi } from '@/api'

const loading = ref(false)
const ticketList = ref([])
const pagination = reactive({ current: 1, size: 10, total: 0 })
const searchForm = reactive({ keyword: '', type: '' })
const dialogVisible = ref(false)
const dialogTitle = ref('添加门票')
const formData = reactive({})
const formRules = {
  name: [{ required: true, message: '请输入门票名称', trigger: 'blur' }],
  type: [{ required: true, message: '请选择类型', trigger: 'change' }],
  price: [{ required: true, message: '请输入价格', trigger: 'blur' }]
}
const formRef = ref(null)

async function getTicketList() {
  loading.value = true
  try {
    const res = await ticketApi.getAll(pagination.current, pagination.size, searchForm.type, searchForm.keyword)
    ticketList.value = res.data.records
    pagination.total = res.data.total
  } catch (e) {
    ElMessage.error('获取门票列表失败')
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  pagination.current = 1
  getTicketList()
}

function resetSearch() {
  searchForm.keyword = ''
  searchForm.type = ''
  pagination.current = 1
  getTicketList()
}

function handleAdd() {
  dialogTitle.value = '添加门票'
  Object.assign(formData, { id: '', name: '', type: '', price: 0, originalPrice: 0, validDays: 1, cover: '', description: '', includes: '', notice: '', status: 1 })
  dialogVisible.value = true
}

function handleEdit(row) {
  dialogTitle.value = '编辑门票'
  Object.assign(formData, { ...row })
  dialogVisible.value = true
}

async function handleSave() {
  await formRef.value.validate()
  try {
    if (formData.id) {
      await ticketApi.update(formData)
    } else {
      await ticketApi.save(formData)
    }
    ElMessage.success('保存成功')
    dialogVisible.value = false
    getTicketList()
  } catch (e) {
    ElMessage.error('保存失败')
  }
}

async function handleDelete(id) {
  await ElMessageBox.confirm('确认删除此门票？', '提示', { type: 'warning' })
  try {
    await ticketApi.delete(id)
    ElMessage.success('删除成功')
    getTicketList()
  } catch (e) {
    ElMessage.error('删除失败')
  }
}

onMounted(getTicketList)
</script>

<style scoped lang="scss">
@use '@/styles/admin-page.scss' as *;
</style>
