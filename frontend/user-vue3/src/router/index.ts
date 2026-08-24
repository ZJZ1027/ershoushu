import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/stores/user'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: '/',
      component: () => import('@/layout/AppLayout.vue'),
      children: [
        { path: '', name: 'home', meta: { auth: true }, component: () => import('@/views/Home.vue') },
        { path: 'book/:id', name: 'detail', meta: { auth: true }, component: () => import('@/views/Detail.vue') },
        { path: 'publish', name: 'publish', meta: { auth: true }, component: () => import('@/views/Publish.vue') },
        { path: 'mine', name: 'mine', meta: { auth: true }, component: () => import('@/views/Mine.vue') },
        { path: 'messages', name: 'messages', meta: { auth: true }, component: () => import('@/views/Messages.vue') },
        { path: 'orders', name: 'orders', meta: { auth: true }, component: () => import('@/views/Orders.vue') },
        { path: 'profile', name: 'profile', meta: { auth: true }, component: () => import('@/views/Profile.vue') }
      ]
    },
    { path: '/login', name: 'login', component: () => import('@/views/Login.vue') },
    { path: '/register', name: 'register', component: () => import('@/views/Register.vue') }
  ]
})

router.beforeEach((to) => {
  const user = useUserStore()
  const isGuestPage = to.name === 'login' || to.name === 'register'
  if (isGuestPage) {
    if (user.token) return { path: (to.query.redirect as string) || '/' }
    return
  }
  if (!user.token) {
    return { path: '/login', query: { redirect: to.fullPath } }
  }
})

export default router
