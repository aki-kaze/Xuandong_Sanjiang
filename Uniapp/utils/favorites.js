// 收藏工具函数
export const favoritesUtil = {
  // 获取所有收藏
  getAllFavorites() {
    try {
      return uni.getStorageSync('favorites') || []
    } catch (e) {
      return []
    }
  },

  // 检查是否已收藏
  isFavorite(id, type) {
    const favorites = this.getAllFavorites()
    return favorites.some(f => String(f.id) === String(id) && f.type === type)
  },

  // 添加收藏
  addFavorite(item) {
    const favorites = this.getAllFavorites()
    if (!this.isFavorite(item.id, item.type)) {
      favorites.push({
        id: item.id,
        type: item.type,
        name: item.name,
        desc: item.desc || item.description || '',
        price: item.price || 0,
        cover: item.cover || item.image || '',
        emoji: item.emoji || '🏞️',
        icon: item.icon || '',
        createTime: Date.now()
      })
      uni.setStorageSync('favorites', favorites)
      uni.showToast({ title: '收藏成功', icon: 'success' })
      return true
    }
    return false
  },

  // 取消收藏
  removeFavorite(id, type) {
    let favorites = this.getAllFavorites()
    const originalLength = favorites.length
    favorites = favorites.filter(f => !(String(f.id) === String(id) && f.type === type))
    if (favorites.length < originalLength) {
      uni.setStorageSync('favorites', favorites)
      uni.showToast({ title: '已取消收藏', icon: 'success' })
      return true
    }
    return false
  },

  // 切换收藏状态
  toggleFavorite(item) {
    if (this.isFavorite(item.id, item.type)) {
      return this.removeFavorite(item.id, item.type)
    } else {
      return this.addFavorite(item)
    }
  }
}
