<template>
  <CrudPage :config="config" />
</template>

<script setup>
import CrudPage from '@/components/CrudPage.vue'
import { activityApi } from '@/api'

const config = {
  api: activityApi,
  pageTitle: '活动管理',
  pageSubtitle: '管理平台旅游活动信息',
  addButtonText: '添加活动',
  nameField: 'title',
  dialogWidth: '680px',

  searchFields: [
    {
      type: 'select',
      prop: 'status',
      placeholder: '状态筛选',
      width: '140px',
      options: [
        { label: '报名中', value: '报名中' },
        { label: '进行中', value: '进行中' },
        { label: '已结束', value: '已结束' }
      ]
    }
  ],

  columns: [
    { type: 'index', prop: 'index', label: '#', width: 50 },
    { type: 'cover', prop: 'cover', label: '封面', width: 80 },
    { prop: 'title', label: '活动名称', minWidth: 140 },
    { prop: 'location', label: '地点', width: 120 },
    { prop: 'startTime', label: '开始时间', width: 130 },
    { prop: 'endTime', label: '结束时间', width: 130 },
    { type: 'participants', label: '已报名/最大', width: 110, currentProp: 'currentParticipants', maxProp: 'maxParticipants' },
    {
      type: 'tag',
      prop: 'status',
      label: '状态',
      width: 90
    },
    {
      type: 'status',
      prop: 'isPublished',
      label: '上架状态',
      width: 100
    }
  ],

  formFields: [
    { prop: 'title', label: '活动名称', type: 'input', required: true, placeholder: '请输入活动名称', span: 24 },
    { prop: 'cover', label: '封面图片', type: 'input', uploadable: true, span: 24, placeholder: '请输入封面图片 URL' },
    { prop: 'location', label: '活动地点', type: 'input', required: true, placeholder: '请输入活动地点', span: 24 },
    { prop: 'startTime', label: '开始时间', type: 'datetime', required: true, span: 12 },
    { prop: 'endTime', label: '结束时间', type: 'datetime', required: true, span: 12 },
    { prop: 'maxParticipants', label: '最大人数', type: 'number', required: true, min: 1, span: 12 },
    { prop: 'currentParticipants', label: '当前报名', type: 'number', min: 0, span: 12 },
    { prop: 'description', label: '活动描述', type: 'textarea', required: true, rows: 3, placeholder: '请输入活动描述', span: 24 },
    {
      prop: 'status',
      label: '活动状态',
      type: 'select',
      options: [
        { label: '报名中', value: '报名中' },
        { label: '进行中', value: '进行中' },
        { label: '已结束', value: '已结束' }
      ],
      default: '报名中',
      span: 24
    }
  ],

  formRules: {
    title: [{ required: true, message: '请输入活动名称', trigger: 'blur' }],
    location: [{ required: true, message: '请输入活动地点', trigger: 'blur' }],
    startTime: [{ required: true, message: '请输入开始时间', trigger: 'blur' }],
    endTime: [{ required: true, message: '请输入结束时间', trigger: 'blur' }],
    maxParticipants: [{ required: true, message: '请输入最大人数', trigger: 'blur' }],
    description: [{ required: true, message: '请输入活动描述', trigger: 'blur' }]
  },

  defaultForm: {
    title: '',
    cover: '',
    location: '',
    startTime: '',
    endTime: '',
    maxParticipants: 100,
    currentParticipants: 0,
    description: '',
    status: '报名中',
    isPublished: 0
  },

  enableToggle: true,
  statusText: {
    active: '上架',
    inactive: '下架'
  },
  actionWidth: 240,

  apiFilterMapping: (searchForm, pagination) => {
    return [pagination.page, pagination.size, searchForm.status]
  },

  dataKey: 'records'
}
</script>

<style scoped lang="scss">
@use '@/styles/buttons.scss' as *;

.deco-top-line { height: 3px; background: linear-gradient(90deg, #A33B32, #1D3B5A, #D4A373, #A33B32); background-size: 300% 100%; animation: gradientShift 4s ease infinite; border-radius: 2px; margin-bottom: 24px; }
@keyframes gradientShift { 0%, 100% { background-position: 0% 50%; } 50% { background-position: 100% 50%; } }
</style>
