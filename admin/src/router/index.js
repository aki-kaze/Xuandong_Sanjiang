import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/Login.vue'),
    meta: { title: '登录' }
  },
  {
    path: '/',
    component: () => import('@/views/Layout.vue'),
    redirect: '/dashboard',
    meta: { requiresAuth: true },
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('@/views/Dashboard.vue'),
        meta: { title: '数据概览', icon: 'DataAnalysis' }
      },

      {
        path: 'attractions',
        name: 'Attractions',
        component: () => import('@/views/Attractions.vue'),
        meta: { title: '景点管理', icon: 'Location' }
      },
      {
        path: 'ticket-orders',
        name: 'TicketOrders',
        component: () => import('@/views/TicketOrders.vue'),
        meta: { title: '门票订单', icon: 'Tickets' }
      },
      {
        path: 'food-orders',
        name: 'FoodOrders',
        component: () => import('@/views/FoodOrders.vue'),
        meta: { title: '美食订单', icon: 'Mug' }
      },
      {
        path: 'hotel-orders',
        name: 'HotelOrders',
        component: () => import('@/views/HotelOrders.vue'),
        meta: { title: '酒店订单', icon: 'House' }
      },
      {
        path: 'product-orders',
        name: 'ProductOrders',
        component: () => import('@/views/ProductOrders.vue'),
        meta: { title: '商品订单', icon: 'ShoppingCart' }
      },
      {
        path: 'experience-orders',
        name: 'ExperienceOrders',
        component: () => import('@/views/ExperienceOrders.vue'),
        meta: { title: '非遗体验订单', icon: 'Star' }
      },
      {
        path: 'order-analysis',
        name: 'OrderAnalysis',
        component: () => import('@/views/OrderAnalysis.vue'),
        meta: { title: '订单总览', icon: 'DataAnalysis' }
      },
      {
        path: 'users',
        name: 'Users',
        component: () => import('@/views/Users.vue'),
        meta: { title: '用户管理', icon: 'User' }
      },
      {
        path: 'moments',
        name: 'Moments',
        component: () => import('@/views/Moments.vue'),
        meta: { title: '社区管理', icon: 'ChatDotRound' }
      },
      {
        path: 'comments',
        name: 'Comments',
        component: () => import('@/views/Comments.vue'),
        meta: { title: '评论管理', icon: 'ChatLineSquare' }
      },
      {
        path: 'heritage',
        name: 'Heritage',
        component: () => import('@/views/Heritage.vue'),
        meta: { title: '非遗管理', icon: 'Star' }
      },
      {
        path: 'activities',
        name: 'Activities',
        component: () => import('@/views/Activities.vue'),
        meta: { title: '活动管理', icon: 'Calendar' }
      },
      {
        path: 'foods',
        name: 'Foods',
        component: () => import('@/views/Foods.vue'),
        meta: { title: '美食管理', icon: 'Mug' }
      },
      {
        path: 'products',
        name: 'Products',
        component: () => import('@/views/Products.vue'),
        meta: { title: '商品管理', icon: 'Goods' }
      },
      {
        path: 'hotels',
        name: 'Hotels',
        component: () => import('@/views/Hotels.vue'),
        meta: { title: '酒店管理', icon: 'House' }
      },
      {
        path: 'routes',
        name: 'Routes',
        component: () => import('@/views/Routes.vue'),
        meta: { title: '路线管理', icon: 'Guide' }
      },
      {
        path: 'guides',
        name: 'Guides',
        component: () => import('@/views/Guides.vue'),
        meta: { title: '攻略管理', icon: 'Notebook' }
      },
      {
        path: 'ai-knowledge',
        name: 'AiKnowledge',
        component: () => import('@/views/AiKnowledge.vue'),
        meta: { title: 'AI知识库', icon: 'ChatDotSquare' }
      },
      {
        path: 'sensitive-words',
        name: 'SensitiveWords',
        component: () => import('@/views/SensitiveWords.vue'),
        meta: { title: '敏感词管理', icon: 'Warning' }
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, from, next) => {
  document.title = to.meta.title ? `${to.meta.title} - 寻侗三江管理平台` : '寻侗三江管理平台'
  const token = localStorage.getItem('admin_token')
  if (to.meta.requiresAuth && !token) {
    next('/login')
  } else if (to.path === '/login' && token) {
    next('/dashboard')
  } else {
    next()
  }
})

export default router
