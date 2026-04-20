import http from './http'

export const authApi = {
  sendRegisterCode: (email: string) =>
    http.post(`/auth/send-register-code?email=${encodeURIComponent(email)}`),
  sendResetCode: (email: string) =>
    http.post(`/auth/send-reset-code?email=${encodeURIComponent(email)}`),
  register: (email: string, password: string, code: string) =>
    http.post('/auth/register', { email, password, code }),
  login: (email: string, password: string) =>
    http.post('/auth/login', { email, password }),
  resetPassword: (data: any) =>
    http.post('/auth/reset-password', data),
}
