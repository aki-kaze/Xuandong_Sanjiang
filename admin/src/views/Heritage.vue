<template>
  <CrudPage :config="config" />
</template>

<script setup>
import CrudPage from '@/components/CrudPage.vue'
import { heritageApi } from '@/api'

const config = {
  api: heritageApi,
  pageTitle: '非遗管理',
  pageSubtitle: '管理三江侗族非物质文化遗产项目',
  addButtonText: '添加非遗项目',
  nameField: 'name',
  dialogWidth: '600px',
  actionWidth: 220,
  enableToggle: true,
  statusField: 'status',

  searchFields: [
    {
      type: 'input',
      prop: 'keyword',
      placeholder: '搜索非遗名称...',
      width: '280px'
    },
    {
      type: 'select',
      prop: 'level',
      placeholder: '级别筛选',
      width: '140px',
      options: [
        { label: '世界非遗', value: '世界非遗' },
        { label: '国家非遗', value: '国家非遗' },
        { label: '省级非遗', value: '省级非遗' }
      ]
    }
  ],

  columns: [
    { type: 'index', prop: 'index', label: '#', width: 60 },
    { type: 'cover', prop: 'cover', label: '封面', width: 90 },
    { prop: 'name', label: '项目名称', minWidth: 160 },
    {
      type: 'tag',
      prop: 'level',
      label: '级别',
      width: 110,
      bgColor: 'linear-gradient(135deg, #f093fb 0%, #f5576c 100%)'
    },
    { prop: 'description', label: '简介', minWidth: 200 },
    {
      type: 'status',
      prop: 'status',
      label: '状态',
      width: 90
    },
    {
      type: 'datetime',
      prop: 'createdAt',
      label: '创建时间',
      width: 170
    }
  ],

  formFields: [
    { prop: 'name', label: '项目名称', type: 'input', required: true, placeholder: '请输入项目名称', span: 12 },
    {
      prop: 'level',
      label: '级别',
      type: 'select',
      required: true,
      placeholder: '请选择级别',
      span: 12,
      options: [
        { label: '世界非遗', value: '世界非遗' },
        { label: '国家非遗', value: '国家非遗' },
        { label: '省级非遗', value: '省级非遗' }
      ]
    },
    { prop: 'icon', label: '图标', type: 'input', placeholder: 'Emoji 图标，如 🎵', span: 12 },
    { prop: 'cover', label: '封面图片', type: 'input', uploadable: true, span: 24, placeholder: '请输入封面图片URL' },
    { prop: 'color', label: '主题色', type: 'color-picker', span: 12 },
    { prop: 'description', label: '简介', type: 'textarea', required: true, rows: 2, placeholder: '请输入简介', span: 24 },
    { prop: 'content', label: '详细内容', type: 'textarea', rows: 4, placeholder: '请输入详细内容', span: 24 },
    {
      prop: 'status',
      label: '状态',
      type: 'radio',
      options: [
        { label: '上架', value: 1 },
        { label: '下架', value: 0 }
      ],
      default: 1,
      span: 24
    }
  ],

  formRules: {
    name: [{ required: true, message: '请输入项目名称', trigger: 'blur' }],
    level: [{ required: true, message: '请选择级别', trigger: 'change' }],
    description: [{ required: true, message: '请输入简介', trigger: 'blur' }]
  },

  defaultForm: {
    name: '',
    icon: '',
    level: '',
    color: '#A33B32',
    category: '',
    description: '',
    cover: '',
    content: '',
    status: 1
  },

  apiFilterMapping: (searchForm, pagination) => {
    return [pagination.page, pagination.size, searchForm.level, searchForm.keyword, true]
  },

  dataKey: 'records'
}
</script>
