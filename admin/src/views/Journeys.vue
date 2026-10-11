<template>
  <div class="journeys-page">
    <div class="page-header">
      <div>
        <h2 class="page-title">行程管理</h2>
        <p class="page-sub">管理用户创建的旅游行程</p>
      </div>
    </div>

    <div class="search-bar">
      <el-input v-model="searchForm.keyword" placeholder="搜索行程名称..." clearable @clear="getJourneyList" @keyup.enter="handleSearch" style="width:280px">
        <template #prefix><el-icon><Search /></el-icon></template>
      </el-input>
      <el-input v-model="searchForm.userId" placeholder="用户ID" clearable @clear="getJourneyList" style="width:140px" />
      <el-button @click="handleSearch">搜索</el-button>
      <el-button @click="resetSearch">重置</el-button>
    </div>

    <div class="table-card">
      <el-table :data="journeyList" v-loading="loading" stripe row-key="id">
        <el-table-column type="index" label="#" width="50" />
        <el-table-column prop="name" label="行程名称" min-width="160" />
        <el-table-column prop="userId" label="用户ID" width="90" />
        <el-table-column prop="startDate" label="开始日期" width="120" />
        <el-table-column prop="endDate" label="结束日期" width="120" />
        <el-table-column prop="description" label="描述" min-width="160" show-overflow-tooltip />
        <el-table-column prop="createTime" label="创建时间" width="160" />
        <el-table-column label="操作" width="130" fixed="right">
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
          @change="getJourneyList"
        />
      </div>
    </div>

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="560px">
      <el-form :model="formData" :rules="formRules" ref="formRef" label-width="90px">
        <el-form-item label="行程名称" prop="name">
          <el-input v-model="formData.name" placeholder="请输入行程名称" />
        </el-form-item>
        <el-form-item label="用户ID" prop="userId">
          <el-input-number v-model="formData.userId" :min="1" style="width:100%" />
        </el-form-item>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="开始日期">
              <el-date-picker v-model="formData.startDate" type="date" placeholder="选择开始日期" style="width:100%" value-format="YYYY-MM-DD" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="结束日期">
              <el-date-picker v-model="formData.endDate" type="date" placeholder="选择结束日期" style="width:100%" value-format="YYYY-MM-DD" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="描述">
          <el-input v-model="formData.description" type="textarea" :rows="3" placeholder="请输入行程描述" />
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
import { Search } from '@element-plus/icons-vue'
import { journeyApi } from '@/api'

const loading = ref(false)
const journeyList = ref([])
const pagination = reactive({ current: 1, size: 10, total: 0 })
const searchForm = reactive({ keyword: '', userId: '' })
const dialogVisible = ref(false)
const dialogTitle = ref('编辑行程')
const formData = reactive({})
const formRules = {
  name: [{ required: true, message: '请输入行程名称', trigger: 'blur' }],
  userId: [{ required: true, message: '请输入用户ID', trigger: 'blur' }]
}
const formRef = ref(null)

async function getJourneyList() {
  loading.value = true
  try {
    const res = await journeyApi.getAll(pagination.current, pagination.size, searchForm.keyword, searchForm.userId)
    journeyList.value = res.data.records
    pagination.total = res.data.total
  } catch (e) {
    ElMessage.error('获取行程列表失败')
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  pagination.current = 1
  getJourneyList()
}

function resetSearch() {
  searchForm.keyword = ''
  searchForm.userId = ''
  pagination.current = 1
  getJourneyList()
}

function handleEdit(row) {
  dialogTitle.value = '编辑行程'
  Object.assign(formData, { ...row })
  dialogVisible.value = true
}

async function handleSave() {
  await formRef.value.validate()
  try {
    await journeyApi.update(formData)
    ElMessage.success('保存成功')
    dialogVisible.value = false
    getJourneyList()
  } catch (e) {
    ElMessage.error('保存失败')
  }
}

async function handleDelete(id) {
  await ElMessageBox.confirm('确认删除此行程？', '提示', { type: 'warning' })
  try {
    await journeyApi.delete(id)
    ElMessage.success('删除成功')
    getJourneyList()
  } catch (e) {
    ElMessage.error('删除失败')
  }
}

onMounted(getJourneyList)
</script>

<style scoped lang="scss">
@use '@/styles/admin-page.scss' as *;
</style>
