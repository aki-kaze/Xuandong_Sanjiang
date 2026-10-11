/**
 * uni 一键登录工具类
 * 基于 uni-app 官方文档实现
 * 支持中国移动、中国联通、中国电信三大运营商网关认证
 */

// 检查是否支持一键登录
export function checkUniverifyAvailable() {
  return new Promise((resolve, reject) => {
    // #ifdef APP-PLUS
    uni.getProvider({
      service: 'oauth',
      success: (res) => {
        if (res.provider && res.provider.includes('univerify')) {
          resolve(true)
        } else {
          resolve(false)
        }
      },
      fail: (err) => {
        console.error('获取服务列表失败:', err)
        resolve(false)
      }
    })
    // #endif
    
    // #ifndef APP-PLUS
    resolve(false)
    // #endif
  })
}

// 预登录（可选，可提升登录速度）
export function preLogin() {
  return new Promise((resolve, reject) => {
    // #ifdef APP-PLUS
    uni.preLogin({
      provider: 'univerify',
      success: (res) => {
        console.log('预登录成功:', res)
        resolve(res)
      },
      fail: (err) => {
        console.log('预登录失败（可能不支持一键登录）:', err)
        reject(err)
      }
    })
    // #endif
    
    // #ifndef APP-PLUS
    reject(new Error('当前环境不支持一键登录'))
    // #endif
  })
}

// 一键登录
export function univerifyLogin() {
  return new Promise((resolve, reject) => {
    // #ifdef APP-PLUS
    uni.login({
      provider: 'univerify',
      success: (loginRes) => {
        console.log('一键登录成功:', loginRes)
        // loginRes.authResult 包含 access_token 等信息
        resolve(loginRes)
      },
      fail: (err) => {
        console.error('一键登录失败:', err)
        reject(err)
      }
    })
    // #endif
    
    // #ifndef APP-PLUS
    reject(new Error('当前环境不支持一键登录'))
    // #endif
  })
}

// 获取用户手机号（需要在 uniCloud 云函数中通过 access_token 换取）
// 注意：小程序端无法直接获取手机号，需要通过 uniCloud 云函数
export async function getPhoneNumber(loginResult) {
  // #ifdef APP-PLUS
  if (loginResult && loginResult.authResult) {
    // 这里应该调用 uniCloud 云函数来换取手机号
    // 由于项目未配置 uniCloud，返回模拟数据
    console.log('access_token:', loginResult.authResult.access_token)
    
    // TODO: 实际项目中应该调用 uniCloud 云函数
    // const cloudResult = await uniCloud.callFunction({
    //   name: 'get-phone-number',
    //   data: {
    //     access_token: loginResult.authResult.access_token,
    //     appid: '__UNI__YOUR_APPID'
    //   }
    // })
    
    // 返回模拟手机号（实际应从云函数获取）
    return {
      phoneNumber: '138****8888',
      access_token: loginResult.authResult.access_token
    }
  }
  // #endif
  
  throw new Error('无法获取手机号')
}

// 退出登录（清除运营商登录状态）
export function logout() {
  return new Promise((resolve, reject) => {
    // #ifdef APP-PLUS
    uni.univerifyManager && uni.univerifyManager.logout({
      success: (res) => {
        console.log('退出登录成功:', res)
        resolve(res)
      },
      fail: (err) => {
        console.error('退出登录失败:', err)
        reject(err)
      }
    })
    // #endif
    
    // #ifndef APP-PLUS
    resolve()
    // #endif
  })
}

export default {
  checkUniverifyAvailable,
  preLogin,
  univerifyLogin,
  getPhoneNumber,
  logout
}
