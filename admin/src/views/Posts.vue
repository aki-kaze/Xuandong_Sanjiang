<template>
  <div class="posts-page">
    <div class="page-header">
      <div>
        <h2 class="page-title">帖子管理</h2>
        <p class="page-sub">管理用户发布的社区帖子</p>
      </div>
    </div>

    <div class="search-bar">
      <el-input v-model="searchForm.keyword" placeholder="搜索帖子标题或内容..." clearable @clear="getPostList" @keyup.enter="handleSearch" style="width:280px">
        <template #prefix><el-icon><Search /></el-icon></template>
      </el-input>
      <el-select v-model="searchForm.status" placeholder="状态筛选" clearable @change="handleSearch" style="width:140px">
        <el-option label="待审核" value="pending" />
        <el-option label="已通过" value="approved" />
        <el-option label="已拒绝" value="rejected" />
      </el-select>
      <el-button @click="handleSearch">搜索</el-button>
      <el-button @click="resetSearch">重置</el-button>
    </div>

    <div class="table-card">
      <el-table :data="postList" v-loading="loading" stripe row-key="id">
        <el-table-column type="index" label="#" width="50" />
        <el-table-column prop="title" label="标题" min-width="180" show-overflow-tooltip />
        <el-table-column prop="userId" label="用户ID" width="90" />
        <el-table-column prop="content" label="内容" min-width="180" show-overflow-tooltip />
        <el-table-column label="状态" width="90">
          <template #default="{ row }">
            <el-tag :type="row.status==='approved'?'success':row.status==='rejected'?'danger':'warning'" size="small">
              {{ row.status==='approved'?'已通过':row.status==='rejected'?'已拒绝':'待审核' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="160" />
        <el-table-column label="操作" width="240" fixed="right">
          <template #default="{ row }">
            <div class="action-buttons">
              <el-button v-if="row.status==='pending'" size="small" class="action-view" @click="handleApprove(row.id)">通过</el-button>
              <el-button v-if="row.status==='pending'" size="small" class="action-cancel" @click="handleReject(row.id)">拒绝</el-button>
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
          @change="getPostList"
        />
      </div>
    </div>

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="580px">
      <el-form :model="formData" :rules="formRules" ref="formRef" label-width="80px">
        <el-form-item label="标题" prop="title">
          <el-input v-model="formData.title" placeholder="请输入帖子标题" />
        </el-form-item>
        <el-form-item label="用户ID" prop="userId">
          <el-input-number v-model="formData.userId" :min="1" style="width:100%" />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="formData.status" style="width:100%">
            <el-option label="待审核" value="pending" />
            <el-option label="已通过" value="approved" />
            <el-option label="已拒绝" value="rejected" />
          </el-select>
        </el-form-item>
        <el-form-item label="内容" prop="content">
          <el-input v-model="formData.content" type="textarea" :rows="5" placeholder="请输入帖子内容" />
        </el-form-item>
        <el-form-item label="图片URL">
          <el-input v-model="formData.images" placeholder="多个URL用逗号分隔" />
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
import { postApi } from '@/api'

const loading = ref(false)
const postList = ref([])
const pagination = reactive({ current: 1, size: 10, total: 0 })
const searchForm = reactive({ keyword: '', status: '' })
const dialogVisible = ref(false)
const dialogTitle = ref('编辑帖子')
const formData = reactive({})
const formRules = {
  title: [{ required: true, message: '请输入标题', trigger: 'blur' }],
  userId: [{ required: true, message: '请输入用户ID', trigger: 'blur' }],
  content: [{ required: true, message: '请输入内容', trigger: 'blur' }]
}
const formRef = ref(null)

async function getPostList() {
  loading.value = true
  try {
    const res = await postApi.getAll(pagination.current, pagination.size, searchForm.status, searchForm.keyword)
    postList.value = res.data.records
    pagination.total = res.data.total
  } catch (e) {
    ElMessage.error('获取帖子列表失败')
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  pagination.current = 1
  getPostList()
}

function resetSearch() {
  searchForm.keyword = ''
  searchForm.status = ''
  pagination.current = 1
  getPostList()
}

function handleEdit(row) {
  dialogTitle.value = '编辑帖子'
  Object.assign(formData, { ...row })
  dialogVisible.value = true
}

async function handleSave() {
  await formRef.value.validate()
  try {
    await postApi.update(formData)
    ElMessage.success('保存成功')
    dialogVisible.value = false
    getPostList()
  } catch (e) {
    ElMessage.error('保存失败')
  }
}

async function handleApprove(id) {
  try {
    await postApi.approve(id)
    ElMessage.success('已通过')
    getPostList()
  } catch (e) {
    ElMessage.error('操作失败')
  }
}

async function handleReject(id) {
  try {
    await postApi.reject(id)
    ElMessage.success('已拒绝')
    getPostList()
  } catch (e) {
    ElMessage.error('操作失败')
  }
}

async function handleDelete(id) {
  await ElMessageBox.confirm('确认删除此帖子？', '提示', { type: 'warning' })
  try {
    await postApi.delete(id)
    ElMessage.success('删除成功')
    getPostList()
  } catch (e) {
    ElMessage.error('删除失败')
  }
}

onMounted(getPostList)
</script>

<style scoped lang="scss">
@use '@/styles/admin-page.scss' as *;

// 添加通过按钮样式
.action-cancel {
  background: linear-gradient(135deg, #A33B32, #C94A40) !important;
  border-color: #A33B32 !important;
  color: white !important;
  
  &:hover {
    background: linear-gradient(135deg, #C94A40, #A33B32) !important;
    border-color: #C94A40 !important;
  }
}
</style>
