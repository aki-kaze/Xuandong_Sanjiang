<template>
  <CrudPage :config="config" />
</template>

<script setup>
import CrudPage from '@/components/CrudPage.vue'
import { attractionApi } from '@/api'

const config = {
  api: attractionApi,
  pageTitle: '景点管理',
  pageSubtitle: '管理三江旅游景点信息',
  addButtonText: '添加景点',
  nameField: 'name',
  dialogWidth: '680px',
  actionWidth: 220,
  enableToggle: true,
  statusField: 'status',

  searchFields: [
    {
      type: 'input',
      prop: 'keyword',
      placeholder: '搜索景点名称或地址...',
      width: '280px'
    },
    {
      type: 'select',
      prop: 'category',
      placeholder: '分类筛选',
      width: '140px',
      options: [
        { label: '自然风光', value: '自然风光' },
        { label: '古镇村落', value: '古镇村落' },
        { label: '侗族文化', value: '侗族文化' },
        { label: '非遗体验', value: '非遗体验' },
        { label: '生态农业', value: '生态农业' }
      ]
    }
  ],

  columns: [
    { type: 'index', prop: 'index', label: '#', width: 60 },
    { type: 'cover', prop: 'cover', label: '封面', width: 90 },
    { prop: 'name', label: '景点名称', minWidth: 150 },
    {
      type: 'tag',
      prop: 'category',
      label: '分类',
      width: 110,
      bgColor: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)'
    },
    {
      type: 'number',
      prop: 'rating',
      label: '评分',
      width: 80
    },
    {
      type: 'currency',
      prop: 'price',
      label: '票价',
      width: 100
    },
    { prop: 'address', label: '地址', minWidth: 180 },
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
    { prop: 'name', label: '景点名称', type: 'input', required: true, placeholder: '请输入景点名称', span: 12 },
    {
      prop: 'category',
      label: '分类',
      type: 'select',
      required: true,
      placeholder: '请选择分类',
      span: 12,
      options: [
        { label: '自然风光', value: '自然风光' },
        { label: '古镇村落', value: '古镇村落' },
        { label: '侗族文化', value: '侗族文化' },
        { label: '非遗体验', value: '非遗体验' },
        { label: '生态农业', value: '生态农业' }
      ]
    },
    { prop: 'rating', label: '评分', type: 'number', required: true, min: 0, max: 5, step: 0.1, precision: 1, span: 12 },
    { prop: 'price', label: '票价', type: 'number', required: true, min: 0, precision: 2, span: 12 },
    { prop: 'cover', label: '封面图片', type: 'input', uploadable: true, span: 24, placeholder: '请输入封面图片URL' },
    { prop: 'address', label: '地址', type: 'input', required: true, placeholder: '请输入详细地址', span: 12 },
    { prop: 'phone', label: '联系电话', type: 'input', placeholder: '请输入联系电话', span: 12 },
    { prop: 'openTime', label: '开放时间', type: 'input', placeholder: '如：08:00-18:00', span: 12 },
    { prop: 'tags', label: '标签', type: 'input', placeholder: '逗号分隔，如：山水,古镇', span: 12 },
    { prop: 'description', label: '景点描述', type: 'textarea', required: true, rows: 3, placeholder: '请输入景点描述', span: 24 },
    { prop: 'tips', label: '游玩提示', type: 'textarea', rows: 2, placeholder: '请输入游玩提示', span: 24 },
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
    name: [{ required: true, message: '请输入景点名称', trigger: 'blur' }],
    category: [{ required: true, message: '请选择分类', trigger: 'change' }],
    description: [{ required: true, message: '请输入描述', trigger: 'blur' }],
    address: [{ required: true, message: '请输入地址', trigger: 'blur' }]
  },

  defaultForm: {
    name: '',
    category: '',
    rating: 4.5,
    price: 0,
    cover: '',
    address: '',
    phone: '',
    openTime: '',
    tags: '',
    description: '',
    tips: '',
    status: 1
  },

  apiFilterMapping: (searchForm, pagination) => {
    return [pagination.page, pagination.size, searchForm.category, searchForm.keyword]
  },

  dataKey: 'records'
}
</script>
