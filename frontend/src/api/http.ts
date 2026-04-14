import axios from 'axios'
import { useAuthStore } from '@/stores/auth'

const http = axios.create({ baseURL: '/api' })

http.interceptors.request.use(config => {
  const token = localStorage.getItem('token')
  if (token) config.headers.Authorization = `Bearer ${token}`
  return config
})

http.interceptors.response.use(
  r => r,
  err => {
    if (err.response?.status === 401) {
      localStorage.removeItem('token')
      location.href = '/login'
    }
    return Promise.reject(err.response?.data?.message || '请求失败')
  }
)

export default http
