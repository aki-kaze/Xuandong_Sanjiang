<template>
  <CrudPage :config="config" />
</template>

<script setup>
import CrudPage from '@/components/CrudPage.vue'
import { productApi } from '@/api'

const config = {
  api: productApi,
  pageTitle: '商品管理',
  pageSubtitle: '管理侗乡特产商品信息',
  addButtonText: '添加商品',
  nameField: 'name',
  actionWidth: 220,
  enableToggle: true,
  statusField: 'status',

  searchFields: [
    {
      type: 'input',
      prop: 'keyword',
      placeholder: '搜索商品名称...',
      width: '280px'
    },
    {
      type: 'select',
      prop: 'category',
      placeholder: '分类筛选',
      width: '140px',
      options: [
        { label: '茶叶', value: '茶叶' },
        { label: '腌制', value: '腌制' },
        { label: '工艺品', value: '工艺品' },
        { label: '酒水', value: '酒水' }
      ]
    }
  ],

  columns: [
    { type: 'index', prop: 'index', label: '#', width: 60 },
    { type: 'cover', prop: 'cover', label: '封面', width: 90 },
    { prop: 'name', label: '商品名称', minWidth: 150 },
    {
      type: 'tag',
      prop: 'category',
      label: '分类',
      width: 110,
      bgColor: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)'
    },
    {
      type: 'currency',
      prop: 'price',
      label: '价格',
      width: 100
    },
    { type: 'tag', prop: 'hot', label: '热销', width: 80 },
    { prop: 'sales', label: '销量', width: 80 },
    {
      type: 'status',
      prop: 'status',
      label: '状态',
      width: 90
    },
    { prop: 'desc', label: '描述', minWidth: 150 },
    {
      type: 'datetime',
      prop: 'createdAt',
      label: '创建时间',
      width: 170
    }
  ],

  formFields: [
    { prop: 'name', label: '商品名称', type: 'input', required: true, placeholder: '请输入商品名称' },
    {
      prop: 'category',
      label: '分类',
      type: 'select',
      required: true,
      placeholder: '请选择分类',
      options: [
        { label: '茶叶', value: '茶叶' },
        { label: '腌制', value: '腌制' },
        { label: '工艺品', value: '工艺品' },
        { label: '酒水', value: '酒水' }
      ]
    },
    { prop: 'price', label: '价格', type: 'number', required: true, min: 0, step: 0.01 },
    { prop: 'desc', label: '简短描述', type: 'input', required: true, placeholder: '请输入简短描述' },
    { prop: 'fullDesc', label: '详细描述', type: 'textarea', required: true, rows: 3, placeholder: '请输入详细描述' },
    { prop: 'tags', label: '标签', type: 'input', placeholder: '多个标签用逗号分隔' },
    { prop: 'cover', label: '封面图片', type: 'input', uploadable: true, placeholder: '请输入封面图片URL' },
    { prop: 'sales', label: '销量', type: 'number', min: 0, defaultValue: 0 },
    { prop: 'hot', label: '是否热销', type: 'switch', defaultValue: false },
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
    name: [{ required: true, message: '请输入商品名称', trigger: 'blur' }],
    category: [{ required: true, message: '请选择分类', trigger: 'change' }],
    price: [{ required: true, message: '请输入价格', trigger: 'blur' }],
    desc: [{ required: true, message: '请输入简短描述', trigger: 'blur' }],
    fullDesc: [{ required: true, message: '请输入详细描述', trigger: 'blur' }]
  },

  defaultForm: {
    name: '',
    category: '',
    price: 0,
    desc: '',
    fullDesc: '',
    tags: '',
    cover: '',
    sales: 0,
    hot: 0,
    status: 1
  },

  apiFilterMapping: (searchForm, pagination) => {
    return [pagination.page, pagination.size, searchForm.category, searchForm.keyword]
  },

  dataKey: 'records'
}
</script>
