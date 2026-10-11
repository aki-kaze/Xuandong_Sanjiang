<template>
  <view class="shop-page">

    <!-- 顶部 Banner -->
    <view class="shop-banner">
      <image class="shop-banner-icon" src="https://cdn.phototourl.com/member/2026-03-27-e9378a61-c9de-4444-82a2-566a0bfaac69.jpg" mode="aspectFill" />
      <view class="shop-banner-info">
        <text class="shop-banner-title">侗乡特产商城</text>
        <text class="shop-banner-sub">正宗三江特产 · 产地直发</text>
      </view>
    </view>

    <!-- 分类筛选栏 -->
    <view class="category-bar">
      <view
        class="cat-item"
        v-for="cat in categories"
        :key="cat.id"
        :class="{ active: activeCategory === cat.id }"
        @tap="activeCategory = cat.id"
      >
        <text class="cat-label">{{ cat.label }}</text>
      </view>
    </view>

    <!-- 商品列表 -->
    <view class="product-grid">
      <view class="product-card" v-for="item in filteredProducts" :key="item.id" @tap="showProductDetail(item)">
        <image class="product-img" :src="item.cover" mode="aspectFill" />
        <view class="product-badge" v-if="item.hot">热销</view>
        <view class="product-info">
          <text class="product-name">{{ item.name }}</text>
          <text class="product-desc">{{ item.desc }}</text>
          <view class="product-footer">
            <text class="product-price">¥{{ item.price }}</text>
            <view class="product-add-btn" @tap.stop="addToCart(item)">+</view>
          </view>
          <text class="product-sales">已售 {{ item.sales }}</text>
        </view>
      </view>
    </view>

    <!-- 购买弹窗 -->
    <view class="pay-modal" v-if="showPayModal">
      <view class="pay-box">
        <text class="pay-title">确认订单</text>
        <view class="pay-info">
          <view class="pay-product">
            <image class="pay-product-img" :src="payProduct.cover" mode="aspectFill" />
            <view class="pay-product-info">
              <text class="pay-product-name">{{ payProduct.name }}</text>
              <text class="pay-product-price">¥{{ payProduct.price }} × {{ payQuantity }}</text>
            </view>
          </view>
          <view class="pay-form">
            <view class="pay-form-item">
              <text class="pay-form-label">收货姓名</text>
              <input class="pay-input" v-model="payName" placeholder="请输入收货人姓名" />
            </view>
            <view class="pay-form-item">
              <text class="pay-form-label">手机号码</text>
              <input class="pay-input" v-model="payPhone" placeholder="请输入手机号" type="number" maxlength="11" />
            </view>
            <view class="pay-form-item">
              <text class="pay-form-label">收货地址</text>
              <input class="pay-input" v-model="payAddress" placeholder="请输入详细收货地址" />
            </view>
          </view>
          <view class="pay-total-row">
            <text class="pay-total-label">合计</text>
            <text class="pay-total-price">¥{{ payProduct.price * payQuantity }}</text>
          </view>
        </view>
        <view class="pay-actions">
          <view class="pay-cancel" @tap="showPayModal = false">取消</view>
          <view class="pay-confirm" @tap="confirmPay">确认支付 ¥{{ payProduct.price * payQuantity }}</view>
        </view>
      </view>
    </view>

    <view style="height: 160rpx;"></view>

    <!-- 购物车悬浮按钮 -->
    <view class="cart-fab" @tap="showCart = true" v-if="cartItems.length > 0">
      <image class="cart-fab-icon" src="/static/icons/cart.png" mode="aspectFit" />
      <text class="cart-fab-count">{{ totalCartCount }}</text>
      <text class="cart-fab-price">¥{{ totalCartPrice }}</text>
      <view class="cart-fab-btn">去结算</view>
    </view>

    <!-- 商品详情弹窗 -->
    <view class="product-modal" v-if="showModal">
      <view class="product-modal-box">
        <view class="product-modal-img-wrap">
          <image class="product-modal-img" :src="selectedProduct.cover" mode="aspectFill" />
          <text class="product-modal-close" @tap="showModal = false">×</text>
        </view>
        <view class="product-modal-content">
          <view class="product-modal-header">
            <text class="product-modal-name">{{ selectedProduct.name }}</text>
            <text class="product-modal-price">¥{{ selectedProduct.price }}</text>
          </view>
          <text class="product-modal-full-desc">{{ selectedProduct.fullDesc }}</text>
          <view class="product-modal-tags">
            <text class="p-tag" v-for="tag in selectedProduct.tags" :key="tag">{{ tag }}</text>
          </view>
          <view class="product-modal-qty">
            <text class="qty-label">购买数量</text>
            <view class="qty-ctrl">
              <view class="qty-btn" @tap="modalQty > 1 && modalQty--">−</view>
              <text class="qty-num">{{ modalQty }}</text>
              <view class="qty-btn" @tap="modalQty++">+</view>
            </view>
          </view>
          <view class="product-modal-actions">
            <view class="modal-cart-btn" @tap="addToCartFromModal">加入购物车</view>
            <view class="modal-buy-btn" @tap="buyNow">立即购买</view>
          </view>
        </view>
      </view>
    </view>

    <!-- 购物车弹窗 -->
    <view class="cart-modal" v-if="showCart">
      <view class="cart-box">
        <view class="cart-header">
          <text class="cart-title">购物车</text>    
          <text @tap="showCart = false">×</text>
        </view>
        <view class="cart-list">
          <view class="cart-item" v-for="item in cartItems" :key="item.id">
            <image class="cart-item-img" :src="item.cover" mode="aspectFill" />
            <view class="cart-item-info">
              <text class="cart-item-name">{{ item.name }}</text>
              <text class="cart-item-price">¥{{ item.price }}</text>
            </view>
            <view class="cart-item-qty">
              <view class="qty-btn-sm" @tap="decreaseCart(item)">−</view>
              <text class="qty-num-sm">{{ item.qty }}</text>
              <view class="qty-btn-sm" @tap="item.qty++">+</view>
            </view>
          </view>
        </view>
        <view class="cart-footer">
          <view class="cart-total">
            <text class="cart-total-label">合计</text>
            <text class="cart-total-price">¥{{ totalCartPrice }}</text>
          </view>
          <view class="cart-checkout-btn" @tap="checkout">去结算</view>
        </view>
      </view>
    </view>
  </view>
</template>

<script>
import { productOrderApi, productApi, formatOrderTime } from '../../utils/api.js'

export default {
  data() {
    return {
      activeCategory: 'all',
      categories: [
        { id: 'all', icon: '🛍️', label: '全部' },
        { id: '茶叶', icon: '🍃', label: '茶叶' },
        { id: '腌制', icon: '🐟', label: '腌制' },
        { id: '工艺品', icon: '🧵', label: '工艺品' },
        { id: '酒水', icon: '🍶', label: '酒水' }
      ],
      products: [],
      productColors: ['#E8F8E8', '#FFF0E8', '#F0F0FF', '#FFF8E8', '#FFE8F0', '#E8F0FF'],
      showModal: false,
      selectedProduct: {},
      modalQty: 1,
      cartItems: [],
      showCart: false,
      showPayModal: false,
      payProduct: {},
      payQuantity: 1,
      payName: '',
      payPhone: '',
      payAddress: ''
    }
  },
  computed: {
    filteredProducts() {
      if (this.activeCategory === 'all') return this.products
      return this.products.filter(p => p.category === this.activeCategory)
    },
    totalCartCount() {
      return this.cartItems.reduce((sum, item) => sum + item.qty, 0)
    },
    totalCartPrice() {
      return this.cartItems.reduce((sum, item) => sum + item.price * item.qty, 0)
    }
  },
  onLoad(options) {
    this.loadProducts()
    if (options && options.id) {
      setTimeout(() => {
        const product = this.products.find(p => p.id === parseInt(options.id))
        if (product) {
          this.showProductDetail(product)
        }
      }, 500)
    }
  },
  methods: {
    async loadProducts() {
      try {
        const res = await productApi.getList(1, 100)
        const records = res.records || res.list || []
        this.products = records.map(item => {
          let tags = []
          if (item.tags) {
            if (typeof item.tags === 'string') {
              tags = item.tags.split(',').filter(t => t.trim())
            } else if (Array.isArray(item.tags)) {
              tags = item.tags
            }
          }
          let sales = item.sales || 0
          let salesStr = sales.toString()
          if (sales >= 1000) {
            salesStr = (sales / 1000).toFixed(1) + 'k'
          }
          return {
            id: item.id,
            name: item.name,
            category: item.category,
            price: item.price,
            cover: item.cover || 'https://via.placeholder.com/200x200',
            desc: item.desc || '',
            fullDesc: item.fullDesc || '',
            tags: tags,
            sales: salesStr,
            hot: item.hot || false,
            status: item.status || 1
          }
        })
      } catch (e) {
        console.error('加载商品失败:', e)
        this.products = [
          { id: 1, name: '三江布央高山绿茶', category: '茶叶', price: 88, cover: 'https://cdn.phototourl.com/member/2026-03-27-3e619480-e5d2-47ab-b7a9-2e1d481fb09e.jpg', hot: true, sales: '2.3k', desc: '海拔 800 米高山云雾茶', fullDesc: '三江布央仙人山高山绿茶，产自海拔 800-1000 米的云雾茶园，茶叶品质优良，香气清幽，滋味醇厚，是广西著名的优质绿茶', tags: ['高山茶', '绿茶', '有机'] },
          { id: 2, name: '侗族传统腌鱼', category: '腌制', price: 68, cover: 'https://cdn.phototourl.com/member/2026-03-27-648429d3-f1f1-4fd0-b36a-3961ff8f151b.jpg', hot: true, sales: '1.8k', desc: '百年传统工艺腌制', fullDesc: '侗族腌鱼采用百年传统工艺，将新鲜鱼类用糯米、辣椒、盐等腌制而成，存放数年不坏，味道鲜美独特，是侗族人待客的最高礼遇', tags: ['非遗美食', '传统腌制', '特产'] },
          { id: 3, name: '侗锦手工织品', category: '工艺品', price: 258, cover: 'https://cdn.phototourl.com/member/2026-03-27-cc0f380f-d346-4ca0-b7b7-a19c82eab849.jpg', hot: false, sales: '856', desc: '非遗传承人手工织造', fullDesc: '侗锦是侗族妇女用传统织机手工织造的彩色织锦，图案精美，色彩鲜艳，每块侗锦需要数月时间织造，是珍贵的非遗工艺品', tags: ['非遗工艺', '手工', '收藏'] },
          { id: 4, name: '侗族腌肉礼盒', category: '腌制', price: 128, cover: 'https://cdn.phototourl.com/member/2026-03-27-bf0a0153-1f6e-4c22-9384-03301c156cec.jpg', tags: ['传统腌制', '年货', '礼盒'] },
          { id: 5, name: '三江糯米酒', category: '酒水', price: 58, cover: 'https://cdn.phototourl.com/member/2026-03-28-19d2c7dd-559d-42b8-90ee-f0812910a882.jpg', hot: false, sales: '678', desc: '侗族传统糯米酿造', fullDesc: '三江糯米酒采用当地优质糯米酿造，口感醇厚，甜而不腻，是侗族人招待贵客的传统饮品，也是馈赠亲友的佳品', tags: ['糯米酒', '传统酿造', '特产'] },
          { id: 6, name: '侗族鼓楼摆件', category: '工艺品', price: 168, cover: 'https://cdn.phototourl.com/member/2026-03-27-e9378a61-c9de-4444-82a2-566a0bfaac69.jpg', hot: false, sales: '423', desc: '精美鼓楼工艺摆件', fullDesc: '以三江鼓楼为原型制作的精美工艺摆件，采用优质木材雕刻而成，造型精美，是了解侗族建筑文化的最佳纪念品', tags: ['工艺品', '摆件', '纪念品'] }
        ]
      }
    },
    showProductDetail(item) { this.selectedProduct = item; this.modalQty = 1; this.showModal = true },
    addToCart(item) {
      const existing = this.cartItems.find(c => c.id === item.id)
      if (existing) existing.qty++
      else this.cartItems.push({ ...item, qty: 1 })
      uni.showToast({ title: '已加入购物车', icon: 'success' })
    },
    addToCartFromModal() {
      const existing = this.cartItems.find(c => c.id === this.selectedProduct.id)
      if (existing) existing.qty += this.modalQty
      else this.cartItems.push({ ...this.selectedProduct, qty: this.modalQty })
      this.showModal = false
      uni.showToast({ title: '已加入购物车', icon: 'success' })
    },
    async buyNow() {
      this.showModal = false
      this.payProduct = this.selectedProduct
      this.payQuantity = this.modalQty
      this.initPayUserInfo()
      this.showPayModal = true
    },
    async createProductOrder(product, quantity, contactName, contactPhone, address) {
      const userInfo = uni.getStorageSync('userInfo') || {}
      const totalPrice = product.price * quantity

      const orderData = {
        orderNo: 'SP' + Date.now(),
        userId: userInfo.userId || 0,
        productId: product.id,
        productName: product.name,
        productCover: product.cover || '',
        quantity: quantity,
        price: product.price,
        totalPrice: totalPrice,
        contactName: contactName || userInfo.name || '',
        contactPhone: contactPhone || userInfo.phone || '',
        address: address || userInfo.address || '',
        remark: '特产商品订单'
      }

      try {
        const res = await productOrderApi.create(orderData)
        const orderNo = (res && res.order) ? res.order.orderNo : orderData.orderNo

        // 不自动调用支付，让用户在订单页面手动支付
        uni.showModal({
          title: '下单成功',
          content: '订单号：' + orderNo + '\n请前往订单页面完成支付',
          showCancel: false,
          success: () => {
            uni.navigateTo({ url: '/pages/order/product-order?status=待支付' })
          }
        })
      } catch (e) {
        console.error('创建订单失败:', e)
        const userInfo = uni.getStorageSync('userInfo') || {}
        const fallbackOrder = {
          id: 'SP' + Date.now(),
          orderType: 'product',
          userId: userInfo.userId || userInfo.id || 0,
          productName: product.name + ' × ' + quantity,
          ticketName: product.name + ' × ' + quantity,
          cover: product.cover,
          productCover: product.cover,
          price: product.price,
          quantity: quantity,
          totalPrice: totalPrice,
          date: new Date().toISOString().slice(0,10),
          status: '待支付',
          createTime: formatOrderTime(new Date()),
          contactName: contactName || userInfo.name || '',
          contactPhone: contactPhone || userInfo.phone || '',
          address: address || userInfo.address || ''
        }
        
        const orders = uni.getStorageSync('orders') || []
        orders.unshift(fallbackOrder)
        uni.setStorageSync('orders', orders)
        
        uni.showModal({
          title: '下单成功',
          content: '订单号：' + fallbackOrder.id + '\n请前往订单页面完成支付',
          showCancel: false,
          success: () => {
            uni.navigateTo({ url: '/pages/order/product-order?status=待支付' })
          }
        })
      }
    },
    decreaseCart(item) {
      if (item.qty > 1) item.qty--
      else this.cartItems = this.cartItems.filter(c => c.id !== item.id)
    },
    async checkout() {
      this.showCart = false
      if (this.cartItems.length === 0) return
      this.payProduct = this.cartItems[0]
      this.payQuantity = this.cartItems[0].qty
      this.initPayUserInfo()
      this.showPayModal = true
    },
    initPayUserInfo() {
      const userInfo = uni.getStorageSync('userInfo') || {}
      this.payName = userInfo.name || userInfo.nickname || ''
      this.payPhone = userInfo.phone || ''
      this.payAddress = userInfo.address || ''
    },
    async confirmPay() {
      if (!this.payName.trim()) { uni.showToast({ title: '请填写收货人姓名', icon: 'none' }); return }
      if (!this.payPhone || this.payPhone.length !== 11) { uni.showToast({ title: '请填写正确手机号', icon: 'none' }); return }
      if (!this.payAddress.trim()) { uni.showToast({ title: '请填写收货地址', icon: 'none' }); return }

      this.showPayModal = false

      if (this.cartItems.length > 0) {
        for (const item of this.cartItems) {
          await this.createProductOrder(item, item.qty, this.payName, this.payPhone, this.payAddress)
        }
        this.cartItems = []
      } else {
        await this.createProductOrder(this.payProduct, this.payQuantity, this.payName, this.payPhone, this.payAddress)
      }
    }
  }
}
</script>

<style scoped lang="scss">
.shop-page { background: #F8F9FA; min-height: 100vh; }

.shop-banner { background: linear-gradient(135deg, #1D3B5A, #2E5F8E); padding: 30rpx; display: flex; align-items: center; gap: 20rpx; }
.shop-banner-icon { width: 80rpx; height: 80rpx; border-radius: 50%; border: 2rpx solid rgba(255,255,255,0.3); }
.shop-banner-title { display: block; font-size: 36rpx; font-weight: bold; color: #FFFFFF; margin-bottom: 6rpx; }
.shop-banner-sub { display: block; font-size: 24rpx; color: rgba(255,255,255,0.85); }

.category-bar { display: flex; background: #FFFFFF; padding: 16rpx 20rpx; overflow-x: auto; border-bottom: 1rpx solid #E8E8E8; gap: 12rpx; }
.cat-item { display: flex; align-items: center; padding: 12rpx 28rpx; flex-shrink: 0; border-radius: 40rpx; background: #F8FAFC; }
.cat-item.active .cat-label { color: #FFFFFF; font-weight: bold; }
.cat-item.active { background: linear-gradient(135deg, #1D3B5A, #2E5F8E); }
.cat-label { font-size: 26rpx; color: #64748B; }

.product-grid { padding: 20rpx 24rpx; display: grid; grid-template-columns: repeat(2, 1fr); gap: 20rpx; }
.product-card { background: #FFFFFF; border-radius: 16rpx; overflow: hidden; box-shadow: 0 4rpx 20rpx rgba(29,59,90,0.10); position: relative; border: 1rpx solid #E8E8E8; }
.product-img { width: 100%; height: 240rpx; }
.product-badge { position: absolute; top: 12rpx; right: 12rpx; background: linear-gradient(135deg, #1D3B5A, #2E5F8E); color: #FFFFFF; font-size: 20rpx; padding: 4rpx 12rpx; border-radius: 20rpx; z-index: 1; }
.product-info { padding: 16rpx; }
.product-name { display: block; font-size: 26rpx; font-weight: bold; color: #1D3B5A; margin-bottom: 6rpx; }
.product-desc { display: block; font-size: 22rpx; color: #888; margin-bottom: 10rpx; }
.product-footer { display: flex; justify-content: space-between; align-items: center; margin-bottom: 6rpx; }
.product-price { font-size: 32rpx; font-weight: bold; color: #2E5F8E; }
.product-add-btn { width: 52rpx; height: 52rpx; background: linear-gradient(135deg, #1D3B5A, #2E5F8E); color: #FFFFFF; border-radius: 50%; display: flex; align-items: center; justify-content: center; font-size: 36rpx; }
.product-sales { font-size: 20rpx; color: #bbb; }

.cart-fab { position: fixed; bottom: 60rpx; left: 30rpx; right: 30rpx; background: linear-gradient(135deg, #1D3B5A, #2E5F8E); border-radius: 40rpx; padding: 14rpx 22rpx; display: flex; align-items: center; gap: 12rpx; z-index: 100; box-shadow: 0 4rpx 20rpx rgba(29,59,90,0.25); }
.cart-fab-icon { width: 40rpx; height: 40rpx; }
.cart-fab-count { background: #FFFFFF; color: #1D3B5A; font-size: 16rpx; width: 28rpx; height: 28rpx; border-radius: 50%; display: flex; align-items: center; justify-content: center; font-weight: bold; }
.cart-fab-price { flex: 1; font-size: 22rpx; color: #FFFFFF; font-weight: bold; }
.cart-fab-btn { background: #FFFFFF; color: #1D3B5A; font-size: 20rpx; font-weight: bold; padding: 8rpx 20rpx; border-radius: 24rpx; }

.product-modal { position: fixed; top: 0; left: 0; right: 0; bottom: 0; background: rgba(0,0,0,0.5); display: flex; align-items: flex-end; z-index: 500; }
.product-modal-box { width: 100%; background: #FFFFFF; border-radius: 32rpx 32rpx 0 0; overflow: hidden; }
.product-modal-img-wrap { height: 320rpx; position: relative; }
.product-modal-img { width: 100%; height: 100%; }
.product-modal-close { position: absolute; top: 20rpx; right: 24rpx; font-size: 36rpx; color: rgba(255,255,255,0.9); background: rgba(0,0,0,0.5); width: 60rpx; height: 60rpx; border-radius: 50%; display: flex; align-items: center; justify-content: center; }
.product-modal-content { padding: 24rpx 30rpx 50rpx; }
.product-modal-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16rpx; }
.product-modal-name { font-size: 34rpx; font-weight: bold; color: #1D3B5A; }
.product-modal-price { font-size: 36rpx; font-weight: bold; color: #2E5F8E; }
.product-modal-full-desc { display: block; font-size: 26rpx; color: #555; line-height: 1.8; margin-bottom: 16rpx; }
.product-modal-tags { display: flex; flex-wrap: wrap; gap: 10rpx; margin-bottom: 24rpx; }
.p-tag { font-size: 22rpx; color: #1D3B5A; background: rgba(29,59,90,0.10); padding: 6rpx 16rpx; border-radius: 20rpx; }
.product-modal-qty { display: flex; justify-content: space-between; align-items: center; margin-bottom: 24rpx; }
.qty-label { font-size: 28rpx; color: #333; }
.qty-ctrl { display: flex; align-items: center; gap: 24rpx; }
.qty-btn { width: 56rpx; height: 56rpx; border-radius: 50%; border: 1rpx solid #1D3B5A; display: flex; align-items: center; justify-content: center; font-size: 28rpx; color: #1D3B5A; }
.qty-num { font-size: 32rpx; font-weight: bold; color: #333; min-width: 40rpx; text-align: center; }
.product-modal-actions { display: flex; gap: 20rpx; }
.modal-cart-btn { flex: 1; text-align: center; padding: 24rpx; background: #F8F9FA; border-radius: 50rpx; font-size: 28rpx; color: #1D3B5A; font-weight: bold; border: 1rpx solid #1D3B5A; }
.modal-buy-btn { flex: 1; text-align: center; padding: 24rpx; background: linear-gradient(135deg, #1D3B5A, #2E5F8E); border-radius: 50rpx; font-size: 28rpx; color: #FFFFFF; font-weight: bold; }

.cart-modal { position: fixed; top: 0; left: 0; right: 0; bottom: 0; background: rgba(0,0,0,0.5); display: flex; align-items: flex-end; z-index: 600; }
.cart-box { width: 100%; background: #FFFFFF; border-radius: 32rpx 32rpx 0 0; padding: 30rpx; max-height: 70vh; display: flex; flex-direction: column; }
.cart-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 24rpx; font-size: 32rpx; font-weight: bold; color: #1D3B5A; }
.cart-list { flex: 1; overflow-y: auto; }
.cart-item { display: flex; align-items: center; gap: 16rpx; padding: 16rpx 0; border-bottom: 1rpx solid #E8E8E8; }
.cart-item-img { width: 80rpx; height: 80rpx; border-radius: 50%; flex-shrink: 0; }
.cart-item-info { flex: 1; }
.cart-item-name { display: block; font-size: 26rpx; font-weight: bold; color: #333; margin-bottom: 4rpx; }
.cart-item-price { display: block; font-size: 24rpx; color: #2E5F8E; }
.cart-item-qty { display: flex; align-items: center; gap: 16rpx; }
.qty-btn-sm { width: 44rpx; height: 44rpx; border-radius: 50%; border: 1rpx solid #1D3B5A; display: flex; align-items: center; justify-content: center; font-size: 24rpx; color: #1D3B5A; }
.qty-num-sm { font-size: 26rpx; font-weight: bold; color: #333; min-width: 30rpx; text-align: center; }
.cart-footer { display: flex; justify-content: space-between; align-items: center; padding-top: 20rpx; border-top: 1rpx solid #E8E8E8; }
.cart-total-label { display: block; font-size: 22rpx; color: #666; }
.cart-total-price { display: block; font-size: 40rpx; font-weight: bold; color: #2E5F8E; }
.cart-checkout-btn { background: linear-gradient(135deg, #1D3B5A, #2E5F8E); color: #FFFFFF; font-size: 28rpx; font-weight: bold; padding: 20rpx 48rpx; border-radius: 50rpx; }

.pay-modal { position: fixed; top: 0; left: 0; right: 0; bottom: 0; background: rgba(0,0,0,0.5); display: flex; align-items: flex-end; z-index: 700; }
.pay-box { width: 100%; background: #FFFFFF; border-radius: 32rpx 32rpx 0 0; padding: 30rpx 30rpx 60rpx; }
.pay-title { display: block; font-size: 34rpx; font-weight: 600; color: #1D3B5A; text-align: center; margin-bottom: 24rpx; }
.pay-info { }
.pay-product { display: flex; gap: 16rpx; padding: 20rpx; background: #F8FAFC; border-radius: 16rpx; margin-bottom: 20rpx; }
.pay-product-img { width: 120rpx; height: 120rpx; border-radius: 12rpx; flex-shrink: 0; }
.pay-product-info { flex: 1; display: flex; flex-direction: column; justify-content: center; gap: 8rpx; }
.pay-product-name { font-size: 28rpx; font-weight: 600; color: #1D3B5A; }
.pay-product-price { font-size: 24rpx; color: #888; }
.pay-form { background: #F8FAFC; border-radius: 16rpx; padding: 0 20rpx; margin-bottom: 20rpx; }
.pay-form-item { display: flex; align-items: center; justify-content: space-between; padding: 20rpx 0; border-bottom: 1rpx solid #E2E8F0; }
.pay-form-item:last-child { border-bottom: none; }
.pay-form-label { font-size: 26rpx; color: #64748B; flex-shrink: 0; margin-right: 16rpx; }
.pay-input { flex: 1; font-size: 26rpx; color: #334155; text-align: right; }
.pay-total-row { display: flex; justify-content: space-between; align-items: center; padding: 16rpx 0; }
.pay-total-label { font-size: 28rpx; color: #334155; font-weight: 600; }
.pay-total-price { font-size: 40rpx; font-weight: 600; color: #2E5F8E; }
.pay-actions { display: flex; gap: 20rpx; margin-top: 20rpx; }
.pay-cancel { flex: 1; text-align: center; padding: 24rpx; background: #F1F5F9; border-radius: 50rpx; font-size: 28rpx; color: #64748B; }
.pay-confirm { flex: 2; text-align: center; padding: 24rpx; background: linear-gradient(135deg, #1D3B5A, #2E5F8E); border-radius: 50rpx; font-size: 28rpx; color: #FFFFFF; font-weight: 600; }
</style>
