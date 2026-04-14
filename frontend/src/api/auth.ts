import http from './http'

export const authApi = {
  register: (email: string, password: string) =>
    http.post('/auth/register', { email, password }),
  login: (email: string, password: string) =>
    http.post('/auth/login', { email, password }),
}
