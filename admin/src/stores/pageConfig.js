/**
 * 页面配置状态管理
 */
import { defineStore } from 'pinia'
import { ref, watch } from 'vue'

export const usePageConfigStore = defineStore('pageConfig', () => {
  const LOCAL_STORAGE_KEY = 'admin-page-config'

  const defaultConfig = {
    showTabs: true,
    maxTabsCount: 20,
    autoHideTabsOnMobile: true,
    theme: 'light',
    layout: 'leftMode',
    primaryColor: '#A33B32',
    sidebarCollapsed: false,
    showBreadcrumb: true,
    showPageTransition: true,
    keepAlive: true
  }

  const primaryColorOptions = [
    { value: '#A33B32', name: '朱砂红' },
    { value: '#1D3B5A', name: '靛青' },
    { value: '#D4A373', name: '木构色' },
    { value: '#8B5CF6', name: '紫色' },
    { value: '#10B981', name: '绿色' },
    { value: '#EF4444', name: '红色' },
    { value: '#1677FF', name: '蓝色' },
  ]

  const config = ref({ ...defaultConfig })
  const settingsVisible = ref(false)

  const loadConfig = () => {
    try {
      const saved = localStorage.getItem(LOCAL_STORAGE_KEY)
      if (saved) {
        const parsed = JSON.parse(saved)
        config.value = { ...defaultConfig, ...parsed }
      } else {
        if (window.matchMedia('(prefers-color-scheme: dark)').matches) {
          config.value.theme = 'dark'
        }
      }
    } catch (error) {
      console.error('加载页面配置失败:', error)
    }
    config.value.layout = 'leftMode'
  }

  const saveConfig = () => {
    try {
      localStorage.setItem(LOCAL_STORAGE_KEY, JSON.stringify(config.value))
    } catch (error) {
      console.error('保存页面配置失败:', error)
    }
  }

  const updateConfig = (key, value) => {
    config.value[key] = value
  }

  const updateMultipleConfig = (updates) => {
    Object.assign(config.value, updates)
  }

  const resetConfig = () => {
    config.value = { ...defaultConfig }
  }

  const openSettings = () => {
    settingsVisible.value = true
  }

  const closeSettings = () => {
    settingsVisible.value = false
  }

  const applyTheme = (theme) => {
    document.documentElement.setAttribute('data-theme', theme)
    if (theme === 'dark') {
      document.documentElement.classList.add('dark')
    } else {
      document.documentElement.classList.remove('dark')
    }
  }

  const toggleTheme = () => {
    const newTheme = config.value.theme === 'light' ? 'dark' : 'light'
    config.value.theme = newTheme
    applyTheme(newTheme)
  }

  const setPrimaryColor = (color) => {
    const root = document.documentElement
    const isDark = config.value.theme === 'dark'

    const mixLightTarget = isDark ? '#141414' : '#ffffff'
    const mixDarkTarget = isDark ? '#ffffff' : '#000000'

    root.style.setProperty('--el-color-primary', color)
    root.style.setProperty('--el-color-primary-light-3', `color-mix(in srgb, ${color} 70%, ${mixLightTarget})`)
    root.style.setProperty('--el-color-primary-light-5', `color-mix(in srgb, ${color} 50%, ${mixLightTarget})`)
    root.style.setProperty('--el-color-primary-light-7', `color-mix(in srgb, ${color} 30%, ${mixLightTarget})`)
    root.style.setProperty('--el-color-primary-light-8', `color-mix(in srgb, ${color} 20%, ${mixLightTarget})`)
    root.style.setProperty('--el-color-primary-light-9', `color-mix(in srgb, ${color} 10%, ${mixLightTarget})`)
    root.style.setProperty('--el-color-primary-dark-2', `color-mix(in srgb, ${color} 80%, ${mixDarkTarget})`)
  }

  const togglePrimaryColor = (colorValue) => {
    config.value.primaryColor = colorValue
    setPrimaryColor(colorValue)
  }

  const toggleLayout = (layout) => {
    config.value.layout = layout
    saveConfig()
  }

  const applyLayout = () => {
    return config.value.layout
  }

  watch(
    () => config.value,
    () => {
      saveConfig()
    },
    { deep: true }
  )

  loadConfig()
  applyTheme(config.value.theme)
  if (config.value.primaryColor) {
    setPrimaryColor(config.value.primaryColor)
  }

  return {
    config,
    settingsVisible,
    primaryColorOptions,
    updateConfig,
    updateMultipleConfig,
    resetConfig,
    openSettings,
    closeSettings,
    loadConfig,
    saveConfig,
    applyTheme,
    toggleTheme,
    setPrimaryColor,
    togglePrimaryColor,
    toggleLayout,
    applyLayout
  }
})
