import { defineStore } from 'pinia'
import { ref } from 'vue'
import { authApi } from '@/api/auth'

export const useAuthStore = defineStore('auth', () => {
  const token = ref(localStorage.getItem('token') || '')
  const userId = ref(Number(localStorage.getItem('userId')) || 0)
  const email = ref(localStorage.getItem('email') || '')

  async function login(emailVal: string, password: string) {
    const { data } = await authApi.login(emailVal, password)
    setAuth(data)
  }

  async function register(emailVal: string, password: string) {
    const { data } = await authApi.register(emailVal, password)
    setAuth(data)
  }

  function setAuth(data: { token: string; userId: number; email: string }) {
    token.value = data.token
    userId.value = data.userId
    email.value = data.email
    localStorage.setItem('token', data.token)
    localStorage.setItem('userId', String(data.userId))
    localStorage.setItem('email', data.email)
  }

  function logout() {
    token.value = ''
    userId.value = 0
    email.value = ''
    localStorage.clear()
  }

  return { token, userId, email, login, register, logout }
})
