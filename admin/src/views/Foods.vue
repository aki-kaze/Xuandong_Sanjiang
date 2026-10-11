<template>
  <CrudPage :config="config" />
</template>

<script setup>
import CrudPage from '@/components/CrudPage.vue'
import { foodApi } from '@/api'

const config = {
  api: foodApi,
  pageTitle: '美食管理',
  pageSubtitle: '管理三江特色美食信息',
  addButtonText: '添加美食',
  nameField: 'name',
  actionWidth: 220,
  enableToggle: true,
  statusField: 'status',

  searchFields: [
    {
      type: 'input',
      prop: 'keyword',
      placeholder: '搜索美食名称...',
      width: '280px'
    },
    {
      type: 'select',
      prop: 'category',
      placeholder: '分类筛选',
      width: '140px',
      options: [
        { label: '特色小吃', value: '特色小吃' },
        { label: '正餐', value: '正餐' },
        { label: '饮品', value: '饮品' }
      ]
    }
  ],

  columns: [
    { type: 'index', prop: 'index', label: '#', width: 60 },
    { type: 'cover', prop: 'cover', label: '封面', width: 90 },
    { prop: 'name', label: '美食名称', minWidth: 150 },
    {
      type: 'tag',
      prop: 'category',
      label: '分类',
      width: 110,
      bgColor: 'linear-gradient(135deg, #f093fb 0%, #f5576c 100%)'
    },
    {
      type: 'currency',
      prop: 'price',
      label: '价格',
      width: 100
    },
    { prop: 'description', label: '描述', minWidth: 180 },
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
    { prop: 'name', label: '美食名称', type: 'input', required: true, placeholder: '请输入美食名称' },
    {
      prop: 'category',
      label: '分类',
      type: 'select',
      required: true,
      placeholder: '请选择分类',
      options: [
        { label: '特色小吃', value: '特色小吃' },
        { label: '正餐', value: '正餐' },
        { label: '饮品', value: '饮品' }
      ]
    },
    { prop: 'price', label: '价格', type: 'number', required: true, min: 0, step: 0.01 },
    { prop: 'description', label: '描述', type: 'textarea', required: true, rows: 3, placeholder: '请输入描述' },
    { prop: 'cover', label: '封面图片', type: 'input', uploadable: true, placeholder: '请输入封面图片URL' },
    {
      prop: 'status',
      label: '状态',
      type: 'radio',
      options: [
        { label: '上架', value: 1 },
        { label: '下架', value: 0 }
      ],
      default: 1
    }
  ],

  formRules: {
    name: [{ required: true, message: '请输入美食名称', trigger: 'blur' }],
    category: [{ required: true, message: '请选择分类', trigger: 'change' }],
    price: [{ required: true, message: '请输入价格', trigger: 'blur' }],
    description: [{ required: true, message: '请输入描述', trigger: 'blur' }]
  },

  defaultForm: {
    name: '',
    category: '',
    price: 0,
    description: '',
    cover: '',
    status: 1
  },

  apiFilterMapping: (searchForm, pagination) => {
    return [pagination.page, pagination.size, searchForm.category, searchForm.keyword]
  },

  dataKey: 'records'
}
</script>
