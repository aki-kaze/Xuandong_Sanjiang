import { defineStore } from 'pinia'
import { ref } from 'vue'
import { authApi } from '@/api'

export const useAuthStore = defineStore('auth', () => {
  const token = ref(localStorage.getItem('admin_token') || '')
  const adminInfo = ref(JSON.parse(localStorage.getItem('admin_info') || '{}'))

  async function login(credentials) {
    try {
      const res = await authApi.login(credentials)
      token.value = res.data.token
      adminInfo.value = res.data.userInfo
      localStorage.setItem('admin_token', token.value)
      localStorage.setItem('admin_info', JSON.stringify(adminInfo.value))
      return res
    } catch (error) {
      throw error
    }
  }

  function logout() {
    token.value = ''
    adminInfo.value = {}
    localStorage.removeItem('admin_token')
    localStorage.removeItem('admin_info')
  }

  function setAvatar(avatar) {
    adminInfo.value.avatar = avatar
    localStorage.setItem('admin_info', JSON.stringify(adminInfo.value))
  }

  return { token, adminInfo, login, logout, setAvatar }
})
