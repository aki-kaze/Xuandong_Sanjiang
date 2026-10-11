<template>
  <CrudPage :config="config" />
</template>

<script setup>
import CrudPage from '@/components/CrudPage.vue'
import { hotelApi } from '@/api'

const config = {
  api: hotelApi,
  pageTitle: '酒店管理',
  pageSubtitle: '管理三江旅游住宿酒店信息',
  addButtonText: '添加酒店',
  nameField: 'name',
  actionWidth: 220,
  enableToggle: true,
  statusField: 'status',

  searchFields: [
    {
      type: 'input',
      prop: 'keyword',
      placeholder: '搜索酒店名称或地址...',
      width: '280px'
    },
    {
      type: 'select',
      prop: 'type',
      placeholder: '类型筛选',
      width: '140px',
      options: [
        { label: '星级酒店', value: '星级酒店' },
        { label: '民宿', value: '民宿' },
        { label: '快捷酒店', value: '快捷酒店' }
      ]
    }
  ],

  columns: [
    { type: 'index', prop: 'index', label: '#', width: 60 },
    { type: 'cover', prop: 'cover', label: '封面', width: 90 },
    { prop: 'name', label: '酒店名称', minWidth: 150 },
    {
      type: 'tag',
      prop: 'type',
      label: '类型',
      width: 100,
      bgColor: 'linear-gradient(135deg, #f093fb 0%, #f5576c 100%)'
    },
    {
      prop: 'rating',
      label: '评分',
      width: 80,
      type: 'number'
    },
    {
      prop: 'price',
      label: '价格',
      width: 100,
      type: 'currency'
    },
    { prop: 'address', label: '地址', minWidth: 180 },
    {
      prop: 'status',
      label: '状态',
      width: 90,
      type: 'status'
    },
    {
      prop: 'createdAt',
      label: '创建时间',
      width: 170,
      type: 'datetime'
    }
  ],

  formFields: [
    { prop: 'name', label: '酒店名称', type: 'input', required: true, placeholder: '请输入酒店名称' },
    {
      prop: 'type',
      label: '类型',
      type: 'select',
      required: true,
      placeholder: '请选择类型',
      options: [
        { label: '星级酒店', value: '星级酒店' },
        { label: '民宿', value: '民宿' },
        { label: '快捷酒店', value: '快捷酒店' }
      ]
    },
    { prop: 'price', label: '价格', type: 'number', required: true, min: 0, step: 0.01 },
    { prop: 'address', label: '地址', type: 'input', required: true, placeholder: '请输入地址' },
    { prop: 'description', label: '描述', type: 'textarea', rows: 3, placeholder: '请输入酒店描述' },
    { prop: 'cover', label: '封面URL', type: 'input', uploadable: true, span: 24, placeholder: '请输入封面图片URL' },
    { prop: 'tags', label: '标签', type: 'input', placeholder: '多个标签用逗号分隔' },
    { prop: 'facilities', label: '设施', type: 'textarea', rows: 2, placeholder: '请输入酒店设施' },
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
    name: [{ required: true, message: '请输入酒店名称', trigger: 'blur' }],
    type: [{ required: true, message: '请选择类型', trigger: 'change' }],
    price: [{ required: true, message: '请输入价格', trigger: 'blur' }],
    address: [{ required: true, message: '请输入地址', trigger: 'blur' }]
  },

  defaultForm: {
    name: '',
    type: '',
    price: 0,
    address: '',
    description: '',
    cover: '',
    tags: '',
    facilities: '',
    status: 1
  },

  apiFilterMapping: (searchForm, pagination) => {
    return [pagination.page, pagination.size, searchForm.type, searchForm.keyword]
  },

  dataKey: 'records'
}
</script>
