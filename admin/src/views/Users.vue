<template>
  <CrudPage :config="config" />
</template>

<script setup>
import CrudPage from '@/components/CrudPage.vue'
import { userApi } from '@/api'

const config = {
  api: userApi,
  pageTitle: '用户管理',
  pageSubtitle: '管理平台注册用户',
  addButtonText: '添加用户',
  nameField: 'nickname',
  dialogWidth: '620px',
  actionWidth: 240,
  enableToggle: true,
  statusField: 'status',
  statusText: {
    active: '启用',
    inactive: '禁用'
  },

  searchFields: [
    {
      type: 'input',
      prop: 'keyword',
      placeholder: '搜索用户昵称/手机号...',
      width: '280px'
    }
  ],

  columns: [
    { type: 'index', prop: 'index', label: '#', width: 50 },
    {
      type: 'avatar',
      prop: 'avatar',
      label: '头像',
      width: 70
    },
    { prop: 'nickname', label: '昵称', width: 120 },
    { prop: 'name', label: '姓名', width: 80 },
    { prop: 'phone', label: '手机号', width: 160 },
    {
      type: 'tag',
      prop: 'genderText',
      label: '性别',
      width: 70,
      bgColor: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)'
    },
    {
      type: 'status',
      prop: 'status',
      label: '状态',
      width: 70
    },
    {
      type: 'datetime',
      prop: 'createdAt',
      label: '注册时间',
      width: 200
    },
    {
      type: 'datetime',
      prop: 'updatedAt',
      label: '更新时间',
      width: 200
    }
  ],

  formFields: [
    { prop: 'nickname', label: '昵称', type: 'input', required: true, placeholder: '请输入用户昵称', span: 12 },
    { prop: 'name', label: '姓名', type: 'input', placeholder: '请输入姓名', span: 12 },
    { prop: 'phone', label: '手机号', type: 'input', placeholder: '请输入手机号', span: 12 },
    { prop: 'password', label: '密码', type: 'password', placeholder: '请输入密码', span: 12 },
    {
      prop: 'gender',
      label: '性别',
      type: 'radio',
      options: [
        { label: '男', value: 0 },
        { label: '女', value: 1 }
      ],
      default: 0,
      span: 12
    },
    {
      prop: 'status',
      label: '状态',
      type: 'radio',
      options: [
        { label: '正常', value: 1 },
        { label: '禁用', value: 0 }
      ],
      default: 1,
      span: 12
    },
    { prop: 'avatar', label: '头像', type: 'avatar-with-input', placeholder: '请输入头像图片URL', span: 24 }
  ],

  formRules: {
    nickname: [{ required: true, message: '请输入用户昵称', trigger: 'blur' }]
  },

  defaultForm: {
    nickname: '',
    name: '',
    phone: '',
    gender: 0,
    status: 1,
    avatar: ''
  },

  apiFilterMapping: (searchForm, pagination) => {
    return [pagination.page, pagination.size, searchForm.keyword]
  },

  // 预处理行数据：将 gender 数字转为文本
  transformRow: (row) => {
    return {
      ...row,
      status: typeof row.status === 'number' ? row.status : (row.status === null ? 1 : Number(row.status)),
      genderText: ['男', '女'][row.gender] || '男'
    }
  },

  dataKey: 'records'
}
</script>
