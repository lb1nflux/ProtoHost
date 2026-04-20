import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    { path: '/login', component: () => import('@/pages/LoginPage.vue') },
    { path: '/register', component: () => import('@/pages/RegisterPage.vue') },
    { path: '/forgot-password', component: () => import('@/pages/ForgotPasswordPage.vue') },
    { path: '/', component: () => import('@/pages/DashboardPage.vue'), meta: { auth: true } },
    { path: '/upload', component: () => import('@/pages/UploadPage.vue'), meta: { auth: true } },
    { path: '/view/:slug', component: () => import('@/pages/ViewPage.vue') },
  ],
})

router.beforeEach(to => {
  const auth = useAuthStore()
  if (to.meta.auth && !auth.token) return '/login'
})

export default router
