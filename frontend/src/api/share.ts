import http from './http'

export const shareApi = {
  meta: (slug: string) => http.get(`/share/${slug}/meta`),
  verify: (slug: string, password: string) =>
    http.post(`/share/${slug}/verify`, { password }),
}
