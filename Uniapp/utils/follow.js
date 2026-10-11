// 关注工具函数 - 基于 localStorage 持久化
// 数据结构：{ userId, nickname, avatar, createTime }

export const followUtil = {
  // 获取所有关注列表
  getAllFollows() {
    try {
      return uni.getStorageSync('user_follows') || []
    } catch (e) {
      return []
    }
  },

  // 检查是否已关注某用户
  isFollowing(userId) {
    const follows = this.getAllFollows()
    return follows.some(f => f.userId === userId)
  },

  // 添加关注
  addFollow(userInfo) {
    const follows = this.getAllFollows()
    if (!this.isFollowing(userInfo.userId)) {
      follows.push({
        userId: userInfo.userId,
        nickname: userInfo.nickname || '匿名用户',
        avatar: userInfo.avatar || '',
        createTime: Date.now()
      })
      uni.setStorageSync('user_follows', follows)
      uni.showToast({ title: '关注成功', icon: 'success' })
      return true
    }
    uni.showToast({ title: '已关注该用户', icon: 'none' })
    return false
  },

  // 取消关注
  removeFollow(userId) {
    let follows = this.getAllFollows()
    const originalLength = follows.length
    follows = follows.filter(f => f.userId !== userId)
    if (follows.length < originalLength) {
      uni.setStorageSync('user_follows', follows)
      uni.showToast({ title: '已取消关注', icon: 'success' })
      return true
    }
    return false
  },

  // 切换关注状态
  toggleFollow(userInfo) {
    if (this.isFollowing(userInfo.userId)) {
      return this.removeFollow(userInfo.userId)
    } else {
      return this.addFollow(userInfo)
    }
  },

  // 获取关注数量
  getFollowCount() {
    return this.getAllFollows().length
  }
}
