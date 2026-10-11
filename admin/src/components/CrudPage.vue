<template>
  <div class="crud-page">
    <div class="page-header">
      <div class="header-left">
        <h2 class="page-title">{{ config.pageTitle }}</h2>
        <p class="page-subtitle">{{ config.pageSubtitle }}</p>
      </div>
      <div class="header-actions">
        <el-button type="primary" plain @click="handleAdd">
          <el-icon><Plus /></el-icon>
          {{ config.addButtonText }}
        </el-button>
      </div>
    </div>

    <div class="search-bar">
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item
          v-for="field in config.searchFields"
          :key="field.prop"
        >
          <el-input
            v-if="field.type === 'input'"
            v-model="searchForm[field.prop]"
            :placeholder="field.placeholder || field.label"
            clearable
            :style="{ width: field.width || '200px' }"
            @keyup.enter="handleSearch"
          />
          <el-select
            v-else-if="field.type === 'select'"
            v-model="searchForm[field.prop]"
            :placeholder="field.placeholder || field.label"
            clearable
            :style="{ width: field.width || '200px' }"
          >
            <el-option
              v-for="opt in field.options"
              :key="opt.value"
              :label="opt.label"
              :value="opt.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" plain @click="handleSearch">
            <el-icon><Search /></el-icon>
            搜索
          </el-button>
          <el-button plain @click="handleReset">
            <el-icon><Refresh /></el-icon>
            重置
          </el-button>
        </el-form-item>
      </el-form>
    </div>

    <div class="table-container">
      <el-table
        :data="tableData"
        v-loading="loading"
        stripe
        class="data-table"
      >
        <el-table-column
          v-for="col in config.columns"
          :key="col.prop"
          :prop="col.prop"
          :label="col.label"
          :width="col.width"
          :min-width="col.minWidth"
          :align="col.align || 'center'"
          show-overflow-tooltip
        >
          <template #default="{ $index, row }" v-if="col.type">
            <template v-if="col.type === 'index'">
              <span style="font-weight: 600; color: #1D3B5A;">{{ (pagination.page - 1) * pagination.size + $index + 1 }}</span>
            </template>

            <template v-else-if="col.type === 'image'">
              <el-image
                v-if="row[col.prop]"
                :src="row[col.prop]"
                :preview-src-list="[row[col.prop]]"
                fit="cover"
                style="width: 56px; height: 56px; border-radius: 50%;"
              />
              <span v-else>-</span>
            </template>

            <template v-else-if="col.type === 'avatar'">
              <el-image
                v-if="row[col.prop]"
                :src="row[col.prop]"
                :preview-src-list="[row[col.prop]]"
                fit="cover"
                style="width: 56px; height: 56px; border-radius: 50%;"
              />
              <span v-else>-</span>
            </template>

            <template v-else-if="col.type === 'cover'">
              <el-image
                v-if="row[col.prop]"
                :src="row[col.prop]"
                :preview-src-list="[row[col.prop]]"
                fit="cover"
                style="width: 80px; height: 60px; border-radius: 6px;"
              />
              <span v-else>-</span>
            </template>

            <template v-else-if="col.type === 'tag'">
              <el-tag v-if="typeof row[col.prop] === 'boolean' || row[col.prop] === 0 || row[col.prop] === 1" size="small" :type="row[col.prop] === true || row[col.prop] === 1 ? 'success' : 'info'">
                {{ row[col.prop] === true || row[col.prop] === 1 ? '是' : '否' }}
              </el-tag>
              <el-tag v-else size="small">{{ row[col.prop] }}</el-tag>
            </template>

            <template v-else-if="col.type === 'status'">
              <el-tag :type="row[col.prop] === 1 ? 'success' : 'danger'" size="small">
                {{ row[col.prop] === 1 ? (config.statusText?.active || '上架') : (config.statusText?.inactive || '下架') }}
              </el-tag>
            </template>

            <template v-else-if="col.type === 'datetime'">
              {{ formatDateTime(row[col.prop]) }}
            </template>

            <template v-else-if="col.type === 'date'">
              {{ formatDate(row[col.prop]) }}
            </template>

            <template v-else-if="col.type === 'currency'">
              ¥{{ formatNumber(row[col.prop]) }}
            </template>

            <template v-else-if="col.type === 'number'">
              {{ formatNumber(row[col.prop]) }}
            </template>

            <template v-else-if="col.type === 'participants'">
              {{ row[col.currentProp] || 0 }}/{{ row[col.maxProp] || 0 }}
            </template>
          </template>
        </el-table-column>

        <el-table-column label="操作" :width="config.actionWidth || 200" align="center" fixed="right">
          <template #default="{ row }">
            <div class="action-buttons">
              <el-button
                v-if="config.enableToggle === true"
                :type="getStatusValue(row) === 1 ? 'warning' : 'success'"
                plain
                size="small"
                @click="handleToggleStatus(row)"
                class="action-btn"
              >
                {{ getStatusValue(row) === 1 ? (config.statusText?.inactive || '下架') : (config.statusText?.active || '上架') }}
              </el-button>
              <el-button
                type="primary"
                plain
                size="small"
                @click="handleEdit(row)"
                class="action-btn"
              >
                编辑
              </el-button>
              <el-button
                type="danger"
                plain
                size="small"
                @click="handleDelete(row)"
                class="action-btn"
              >
                删除
              </el-button>
            </div>
          </template>
        </el-table-column>

        <template #empty>
          <div class="empty-state">
            <p>暂无数据</p>
          </div>
        </template>
      </el-table>
    </div>

    <div class="pagination-wrapper">
      <span class="pagination-info">
        共 {{ pagination.total }} 条记录
      </span>
      <el-pagination
        v-model:current-page="pagination.page"
        v-model:page-size="pagination.size"
        :page-sizes="[10, 20, 50, 100]"
        :total="pagination.total"
        layout="sizes, prev, pager, next, jumper"
        background
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
      />
    </div>

    <el-dialog
      v-model="dialogVisible"
      :title="isEdit ? '编辑' : '新增'"
      :width="config.dialogWidth || '620px'"
      :close-on-click-modal="false"
      class="custom-dialog"
      @close="handleDialogClose"
    >
      <div class="dialog-header-decoration"></div>
      <el-form
        ref="formRef"
        :model="form"
        :rules="config.formRules"
        label-width="120px"
        class="custom-form"
      >
        <el-row :gutter="24">
          <el-col
            v-for="field in config.formFields"
            :key="field.prop"
            :span="field.span || 24"
          >
            <el-form-item
              :label="field.label"
              :prop="field.prop"
              :rules="field.rules"
            >
              <template v-if="field.type === 'input'">
                <!-- 普通输入框 -->
                <el-input
                  v-if="!field.uploadable"
                  v-model="form[field.prop]"
                  :placeholder="field.placeholder"
                  clearable
                  class="custom-input"
                />
                <!-- 图片输入框（带上传功能） -->
                <div v-else class="image-upload-wrapper">
                  <el-tabs v-model="imageTab[field.prop]" class="image-tabs">
                    <!-- URL 输入 -->
                    <el-tab-pane label="自定义 URL" name="url">
                      <div class="url-input-section">
                        <el-input
                          v-model="form[field.prop]"
                          :placeholder="field.placeholder || '请输入图片 URL'"
                          clearable
                          @input="handleImageUrlInput(field.prop)"
                          class="custom-input"
                        />
                        <div v-if="form[field.prop]" class="image-preview-small">
                          <el-image
                            :src="form[field.prop]"
                            fit="cover"
                            class="preview-image-small"
                          >
                            <template #error>
                              <div class="image-error-small">
                                <el-icon><Picture /></el-icon>
                              </div>
                            </template>
                          </el-image>
                        </div>
                      </div>
                    </el-tab-pane>

                    <!-- 本地上传 -->
                    <el-tab-pane label="本地上传" name="upload">
                      <div class="upload-section">
                        <el-upload
                          :ref="(el) => uploadRef[field.prop] = el"
                          class="image-uploader"
                          drag
                          :auto-upload="false"
                          :show-file-list="false"
                          :on-change="(file) => handleImageUpload(field.prop, file)"
                          accept="image/jpeg,image/png,image/gif,image/webp"
                        >
                          <div v-if="uploadPreview[field.prop]" class="upload-preview">
                            <el-image
                              :src="uploadPreview[field.prop]"
                              fit="cover"
                              class="upload-preview-image"
                            />
                            <div class="upload-preview-overlay" @click.stop="clearUploadPreview(field.prop)">
                              <el-icon class="upload-delete-icon"><Close /></el-icon>
                            </div>
                          </div>
                          <div v-else class="upload-placeholder">
                            <el-icon class="upload-icon"><Upload /></el-icon>
                            <div class="upload-text">将文件拖到此处，或<em>点击上传</em></div>
                            <div class="upload-tip">支持 jpg/png/gif/webp 文件，大小不超过 2MB</div>
                          </div>
                        </el-upload>
                      </div>
                    </el-tab-pane>
                  </el-tabs>
                </div>
              </template>
              <div v-else-if="field.type === 'password'" class="password-input-wrapper">
                <el-input
                  v-model="form[field.prop]"
                  :type="passwordVisible[field.prop] ? 'text' : 'password'"
                  :placeholder="field.placeholder"
                  clearable
                  class="custom-input"
                >
                  <template #suffix>
                    <el-icon
                      class="password-toggle-icon"
                      @click="togglePasswordVisibility(field.prop)"
                    >
                      <component :is="passwordVisible[field.prop] ? 'View' : 'Hide'" />
                    </el-icon>
                  </template>
                </el-input>
              </div>
              <el-input-number
                v-else-if="field.type === 'number'"
                v-model="form[field.prop]"
                :min="field.min"
                :max="field.max"
                :step="field.step"
                :precision="field.precision"
                class="custom-input-number"
              />
              <el-input
                v-else-if="field.type === 'textarea'"
                v-model="form[field.prop]"
                type="textarea"
                :rows="field.rows || 3"
                :placeholder="field.placeholder"
                class="custom-textarea"
              />
              <el-select
                v-else-if="field.type === 'select'"
                v-model="form[field.prop]"
                :placeholder="field.placeholder"
                clearable
                class="custom-select"
              >
                <el-option
                  v-for="opt in field.options"
                  :key="opt.value"
                  :label="opt.label"
                  :value="opt.value"
                />
              </el-select>
              <el-radio-group v-else-if="field.type === 'radio'" v-model="form[field.prop]" class="custom-radio-group">
                <el-radio
                  v-for="opt in field.options"
                  :key="opt.value"
                  :value="opt.value"
                >
                  {{ opt.label }}
                </el-radio>
              </el-radio-group>
              <el-switch
                v-else-if="field.type === 'switch'"
                v-model="form[field.prop]"
                :active-value="1"
                :inactive-value="0"
              />
              <el-date-picker
                v-else-if="field.type === 'datetime'"
                v-model="form[field.prop]"
                type="datetime"
                placeholder="选择日期时间"
                format="YYYY-MM-DD HH:mm:ss"
                value-format="YYYY-MM-DD HH:mm:ss"
                class="custom-date-picker"
              />
              <el-date-picker
                v-else-if="field.type === 'date'"
                v-model="form[field.prop]"
                type="date"
                placeholder="选择日期"
                format="YYYY-MM-DD"
                value-format="YYYY-MM-DD"
                class="custom-date-picker"
              />
              <el-color-picker
                v-else-if="field.type === 'color-picker'"
                v-model="form[field.prop]"
                :predefine="field.predefine"
                show-alpha
              />
              <div v-else-if="field.type === 'avatar-with-input'" class="avatar-input-wrapper">
                <div class="avatar-preview">
                  <el-image
                    v-if="form[field.prop]"
                    :src="form[field.prop]"
                    fit="cover"
                    class="avatar-image"
                  >
                    <template #error>
                      <div class="image-error">
                        <el-icon><Picture /></el-icon>
                        <span>加载失败</span>
                      </div>
                    </template>
                  </el-image>
                  <div v-else class="avatar-placeholder">
                    <el-icon><User /></el-icon>
                    <span>暂无头像</span>
                  </div>
                </div>
                <el-input
                  v-model="form[field.prop]"
                  :placeholder="field.placeholder"
                  clearable
                  class="avatar-url-input"
                />
              </div>
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>

      <template #footer>
        <div class="dialog-footer">
          <el-button plain @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleSubmit" :loading="submitLoading" class="submit-btn">
            {{ isEdit ? '保存修改' : '确认添加' }}
          </el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed, nextTick } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Search, Refresh, View, Hide, Picture, User, Upload, Close } from '@element-plus/icons-vue'
import request, { uploadRequest } from '@/api'

const props = defineProps({
  config: {
    type: Object,
    required: true
  }
})

const loading = ref(false)
const submitLoading = ref(false)
const dialogVisible = ref(false)
const isEdit = ref(false)
const tableData = ref([])
const searchForm = reactive({})
const form = reactive({})
const formRef = ref()
const pagination = reactive({
  page: 1,
  size: 10,
  total: 0
})
const passwordVisible = reactive({})

// 图片上传相关
const imageTab = reactive({}) // 每个图片字段的当前 Tab：'url' | 'upload'
const uploadPreview = reactive({}) // 每个图片字段的上传预览
const uploadRef = reactive({}) // 每个图片字段的上传组件引用

// 打开对话框时初始化图片上传状态
const initImageUploadState = () => {
  if (props.config.formFields) {
    props.config.formFields.forEach(field => {
      if (field.type === 'image' || (field.type === 'input' && field.uploadable)) {
        const prop = field.prop
        imageTab[prop] = form[prop] ? 'url' : 'url'
        uploadPreview[prop] = ''
      }
    })
  }
}

// 处理图片 URL 输入
const handleImageUrlInput = (prop) => {
  imageTab[prop] = 'url'
}

// 处理图片上传
const handleImageUpload = async (prop, file) => {
  const isImage = file.raw.type.startsWith('image/')
  const isLt2M = file.raw.size / 1024 / 1024 < 2

  if (!isImage) {
    ElMessage.error('只能上传图片文件!')
    return false
  }
  if (!isLt2M) {
    ElMessage.error('图片大小不能超过 2MB!')
    return false
  }

  // 显示加载提示
  const loadingMsg = ElMessage({
    message: '正在上传...',
    type: 'info',
    duration: 0
  })

  try {
    // 构建 FormData
    const formData = new FormData()
    formData.append('file', file.raw)

    // 使用 uploadRequest 调用后端上传接口（不使用 /api 前缀）
    const response = await uploadRequest.post('/upload', formData)

    if (response.code === 200 && response.data) {
      const fileUrl = response.data
      form[prop] = fileUrl
      uploadPreview[prop] = fileUrl
      imageTab[prop] = 'upload'
      loadingMsg.close()
      ElMessage.success('上传成功!')
    } else {
      loadingMsg.close()
      ElMessage.error(response.message || '上传失败')
    }
  } catch (error) {
    loadingMsg.close()
    console.error('图片上传失败:', error)
    ElMessage.error('上传失败，请重试')
  }

  return false // 阻止自动上传
}

// 清除上传预览
const clearUploadPreview = (prop) => {
  form[prop] = ''
  uploadPreview[prop] = ''
  imageTab[prop] = 'url'
  if (uploadRef[prop]) {
    uploadRef[prop].clearFiles()
  }
}

const initSearchForm = () => {
  if (props.config.searchFields) {
    props.config.searchFields.forEach(field => {
      searchForm[field.prop] = field.default || ''
    })
  }
}

const initForm = () => {
  Object.keys(form).forEach(key => delete form[key])
  if (props.config.defaultForm) {
    Object.assign(form, JSON.parse(JSON.stringify(props.config.defaultForm)))
  } else if (props.config.formFields) {
    props.config.formFields.forEach(field => {
      form[field.prop] = field.default ?? (field.type === 'number' ? 0 : '')
    })
  }
}

const loadData = async () => {
  loading.value = true
  try {
    let apiParams
    if (props.config.apiFilterMapping) {
      apiParams = props.config.apiFilterMapping(searchForm, pagination)
    } else {
      apiParams = [pagination.page, pagination.size]
    }

    const response = await props.config.api.getPage(...apiParams)
    const data = response.data || response
    const dataKey = props.config.dataKey || 'records'

    tableData.value = (data[dataKey] || data.records || data.list || []).map(row => {
      if (props.config.transformRow) {
        return props.config.transformRow(row)
      }
      return row
    })
    pagination.total = data.total || 0
  } catch (error) {
    console.error('加载数据失败:', error)
    ElMessage.error('加载数据失败: ' + (error.message || '未知错误'))
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  pagination.page = 1
  loadData()
}

const handleReset = () => {
  initSearchForm()
  pagination.page = 1
  nextTick(() => loadData())
}

const handleAdd = () => {
  isEdit.value = false
  initForm()
  initImageUploadState()
  dialogVisible.value = true
}

const handleEdit = (row) => {
  isEdit.value = true
  Object.assign(form, JSON.parse(JSON.stringify(row)))
  // 不清空密码字段，让管理员可以看到和修改密码
  initImageUploadState()
  dialogVisible.value = true
}

const togglePasswordVisibility = (prop) => {
  passwordVisible[prop] = !passwordVisible[prop]
}

const getStatusValue = (row) => {
  const statusField = props.config.statusField || 'isPublished'
  return row[statusField]
}

const handleToggleStatus = async (row) => {
  const statusField = props.config.statusField || 'isPublished'
  const currentStatus = row[statusField]
  const newStatus = currentStatus === 1 ? 0 : 1
  const action = newStatus === 1 ? (props.config.statusText?.active || '上架') : (props.config.statusText?.inactive || '下架')

  try {
    // 如果API提供了专门的publish/unpublish方法，使用它们
    if (newStatus === 1 && props.config.api.publish) {
      await props.config.api.publish(row.id)
    } else if (newStatus === 0 && props.config.api.unpublish) {
      await props.config.api.unpublish(row.id)
    } else {
      // 否则使用原来的update方法
      const updateData = { ...row }
      updateData[statusField] = newStatus
      await props.config.api.update(updateData)
    }
    ElMessage.success(`${action}成功！`)
    loadData()
  } catch (error) {
    console.error(`${action}失败:`, error)
    ElMessage.error(`${action}失败`)
  }
}

const handleDelete = (row) => {
  const name = row[props.config.nameField || 'name'] || row.id
  ElMessageBox.confirm(
    `确定要删除"${name}"吗？`,
    '确认删除',
    {
      confirmButtonText: '确认删除',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(async () => {
    try {
      await props.config.api.delete(row.id)
      ElMessage.success('删除成功！')
      loadData()
    } catch (error) {
      console.error('删除失败:', error)
      ElMessage.error('删除失败')
    }
  }).catch(() => {})
}

const handleSubmit = async () => {
  if (!formRef.value) return

  try {
    await formRef.value.validate()
    submitLoading.value = true

    // 准备提交的数据
    const submitData = { ...form }

    // 如果是编辑模式且密码为空，则删除密码字段（表示不修改密码）
    if (isEdit.value && !submitData.password) {
      delete submitData.password
    }

    if (isEdit.value) {
      await props.config.api.update(submitData)
      ElMessage.success('更新成功！')
    } else {
      await props.config.api.save(submitData)
      ElMessage.success('新增成功！')
    }

    dialogVisible.value = false
    nextTick(() => loadData())
  } catch (error) {
    console.error('提交失败:', error)
    if (error !== false) {
      ElMessage.error('操作失败')
    }
  } finally {
    submitLoading.value = false
  }
}

const handleSizeChange = () => {
  pagination.page = 1
  loadData()
}

const handleCurrentChange = () => {
  loadData()
}

const handleDialogClose = () => {
  if (formRef.value) {
    formRef.value.resetFields()
  }
}

const formatDateTime = (date) => {
  if (!date) return '-'
  try {
    const d = new Date(date)
    if (isNaN(d.getTime())) return '-'
    const year = d.getFullYear()
    const month = String(d.getMonth() + 1).padStart(2, '0')
    const day = String(d.getDate()).padStart(2, '0')
    const hours = String(d.getHours()).padStart(2, '0')
    const minutes = String(d.getMinutes()).padStart(2, '0')
    const seconds = String(d.getSeconds()).padStart(2, '0')
    return `${year}-${month}-${day} ${hours}:${minutes}:${seconds}`
  } catch (e) {
    return '-'
  }
}

const formatDate = (date) => {
  if (!date) return '-'
  try {
    const d = new Date(date)
    if (isNaN(d.getTime())) return '-'
    const year = d.getFullYear()
    const month = String(d.getMonth() + 1).padStart(2, '0')
    const day = String(d.getDate()).padStart(2, '0')
    return `${year}-${month}-${day}`
  } catch (e) {
    return '-'
  }
}

const formatNumber = (num) => {
  if (num === null || num === undefined) return '-'
  return Number(num).toLocaleString('zh-CN')
}

onMounted(() => {
  initSearchForm()
  initForm()
  nextTick(() => loadData())
})
</script>

<style lang="scss" scoped>
@use '@/styles/buttons.scss' as *;

.crud-page {
  padding: 20px;
}

.deco-top-line { height: 3px; background: linear-gradient(90deg, #A33B32, #1D3B5A, #D4A373, #A33B32); background-size: 300% 100%; animation: gradientShift 4s ease infinite; border-radius: 2px; margin-bottom: 24px; }
@keyframes gradientShift { 0%, 100% { background-position: 0% 50%; } 50% { background-position: 100% 50%; } }
.glass-card { background: rgba(255,255,255,0.85); backdrop-filter: blur(12px); -webkit-backdrop-filter: blur(12px); border: 1px solid rgba(255,255,255,0.6); border-radius: 16px; box-shadow: 0 4px 24px rgba(0,0,0,0.06), 0 1px 4px rgba(0,0,0,0.04), inset 0 1px 0 rgba(255,255,255,0.8); transition: all 0.3s cubic-bezier(0.4,0,0.2,1); &:hover { transform: translateY(-2px); box-shadow: 0 8px 32px rgba(0,0,0,0.1), 0 2px 8px rgba(0,0,0,0.06), inset 0 1px 0 rgba(255,255,255,0.8); } }

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  animation: fadeInUp 0.5s ease-out;

  .header-left {
    .page-title {
      margin: 0 0 4px 0;
      font-size: 24px;
      font-weight: 800;
      color: #1D3B5A;
    }

    .page-subtitle {
      margin: 0;
      font-size: 14px;
      color: #999;
    }
  }
}

.search-bar {
  margin-bottom: 20px;
  padding: 16px;
  @extend .glass-card;
}

.table-container {
  @extend .glass-card;
  padding: 20px;

  .data-table {
    width: 100%;
  }

  .empty-state {
    padding: 40px 20px;
    text-align: center;
    color: #909399;
  }
}

.pagination-wrapper {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 20px;
  padding: 16px;
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05);

  .pagination-info {
    color: #606266;
    font-size: 14px;
  }
}

// 弹窗样式优化
.custom-dialog {
  .dialog-header-decoration {
    height: 4px;
    background: linear-gradient(90deg, #A33B32, #1D3B5A, #D4A373);
    background-size: 200% 100%;
    animation: gradientFlow 3s ease infinite;
    border-radius: 2px;
    margin: -20px -20px 20px -20px;
  }

  @keyframes gradientFlow {
    0%, 100% { background-position: 0% 50%; }
    50% { background-position: 100% 50%; }
  }

  .custom-form {
    .custom-input,
    .custom-input-number,
    .custom-textarea,
    .custom-select,
    .custom-date-picker {
      .el-input__wrapper,
      .el-textarea__inner {
        border-radius: 8px;
        transition: all 0.3s;
        box-shadow: 0 1px 4px rgba(0, 0, 0, 0.04);

        &:hover {
          box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
        }

        &:focus {
          box-shadow: 0 2px 12px rgba(163, 59, 50, 0.15);
        }
      }
    }

    .custom-radio-group {
      display: flex;
      gap: 16px;
      flex-wrap: wrap;

      .el-radio {
        margin-right: 0;
        padding: 8px 16px;
        background: rgba(255, 255, 255, 0.8);
        border: 1px solid rgba(0, 0, 0, 0.06);
        border-radius: 8px;
        transition: all 0.3s;

        &:hover {
          background: rgba(163, 59, 50, 0.05);
          border-color: rgba(163, 59, 50, 0.3);
        }

        .el-radio__input.is-checked + .el-radio__label {
          color: #A33B32;
          font-weight: 600;
        }
      }
    }
  }

  .dialog-footer {
    display: flex;
    justify-content: flex-end;
    gap: 12px;
    padding-top: 16px;
    border-top: 1px solid rgba(0, 0, 0, 0.04);

    .submit-btn {
      min-width: 100px;
      position: relative;
      overflow: hidden;

      &::before {
        content: '';
        position: absolute;
        top: 0;
        left: -100%;
        width: 100%;
        height: 100%;
        background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.2), transparent);
        transition: left 0.5s;
      }

      &:hover::before {
        left: 100%;
      }
    }
  }
}

@keyframes fadeInUp {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

// 操作按钮样式优化
.action-buttons {
  display: flex;
  gap: 8px;
  justify-content: center;
  align-items: center;
  flex-wrap: wrap;

  .action-btn {
    margin: 0 !important;
    min-width: 60px;
    font-size: 13px;
    padding: 5px 12px;
    border-radius: 6px;
    transition: all 0.3s ease;

    &:hover {
      transform: translateY(-2px);
      box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
    }

    &:active {
      transform: translateY(0);
    }
  }
}

// 密码输入框样式
.password-input-wrapper {
  width: 100%;

  .password-toggle-icon {
    cursor: pointer;
    font-size: 18px;
    color: #909399;
    transition: color 0.3s;

    &:hover {
      color: #1D3B5A;
    }
  }
}

// 头像输入框样式
.avatar-input-wrapper {
  display: flex;
  align-items: center;
  gap: 20px;
  width: 100%;

  .avatar-preview {
    flex-shrink: 0;
    width: 100px;
    height: 100px;
    border-radius: 50%;
    overflow: hidden;
    border: 2px solid #E8E8E8;
    background: #F8F9FA;
    display: flex;
    align-items: center;
    justify-content: center;

    .avatar-image {
      width: 100%;
      height: 100%;
      border-radius: 50%;
    }

    .avatar-placeholder,
    .image-error {
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;
      color: #C0C4CC;
      font-size: 14px;

      .el-icon {
        font-size: 32px;
        margin-bottom: 4px;
      }

      span {
        font-size: 12px;
      }
    }
  }

  .avatar-url-input {
    flex: 1;
  }
}

// 日期时间选择器样式
.custom-date-picker {
  width: 100%;

  :deep(.el-input__wrapper) {
    border-radius: 8px;
    box-shadow: 0 0 0 1px #dcdfe6 inset;
    transition: all 0.3s;

    &:hover {
      box-shadow: 0 0 0 1px #A33B32 inset;
    }

    &.is-focus {
      box-shadow: 0 0 0 1px #A33B32 inset;
    }
  }

  :deep(.el-input__inner) {
    font-size: 14px;
  }
}

// 图片上传组件样式
.image-upload-wrapper {
  width: 100%;

  .image-tabs {
    :deep(.el-tabs__header) {
      margin-bottom: 16px;
    }

    :deep(.el-tabs__item) {
      font-size: 14px;
      font-weight: 500;
      color: #606266;
      transition: all 0.3s;

      &.is-active {
        color: #A33B32;
        font-weight: 600;
      }

      &:hover {
        color: #A33B32;
      }
    }

    :deep(.el-tabs__active-bar) {
      background-color: #A33B32;
    }
  }

  // URL 输入部分
  .url-input-section {
    display: flex;
    flex-direction: column;
    gap: 12px;

    .image-preview-small {
      width: 100%;
      max-width: 200px;
      border-radius: 8px;
      overflow: hidden;
      border: 1px solid #e4e7ed;

      .preview-image-small {
        width: 100%;
        height: 120px;
        display: block;
      }

      .image-error-small {
        width: 100%;
        height: 120px;
        display: flex;
        align-items: center;
        justify-content: center;
        background: #f5f7fa;
        color: #c0c4cc;
        font-size: 24px;
      }
    }
  }

  // 上传部分
  .upload-section {
    .image-uploader {
      :deep(.el-upload) {
        width: 100%;
      }

      :deep(.el-upload-dragger) {
        width: 100%;
        height: 180px;
        border: 2px dashed #dcdfe6;
        border-radius: 12px;
        background: #fafafa;
        transition: all 0.3s;
        display: flex;
        align-items: center;
        justify-content: center;

        &:hover {
          border-color: #A33B32;
          background: rgba(163, 59, 50, 0.02);
        }
      }

      .upload-placeholder {
        display: flex;
        flex-direction: column;
        align-items: center;
        justify-content: center;
        gap: 12px;

        .upload-icon {
          font-size: 48px;
          color: #A33B32;
        }

        .upload-text {
          font-size: 14px;
          color: #606266;

          em {
            color: #A33B32;
            font-style: normal;
            font-weight: 500;
          }
        }

        .upload-tip {
          font-size: 12px;
          color: #909399;
        }
      }

      .upload-preview {
        position: relative;
        width: 100%;
        height: 100%;
        display: flex;
        align-items: center;
        justify-content: center;

        .upload-preview-image {
          max-width: 100%;
          max-height: 160px;
          border-radius: 8px;
        }

        .upload-preview-overlay {
          position: absolute;
          top: 0;
          left: 0;
          right: 0;
          bottom: 0;
          background: rgba(0, 0, 0, 0.5);
          display: flex;
          align-items: center;
          justify-content: center;
          opacity: 0;
          transition: opacity 0.3s;
          border-radius: 8px;
          cursor: pointer;

          &:hover {
            opacity: 1;
          }

          .upload-delete-icon {
            font-size: 32px;
            color: #fff;
          }
        }
      }
    }
  }
}
</style>
