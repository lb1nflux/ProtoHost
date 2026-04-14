import http from './http'

export const projectApi = {
  list: () => http.get('/projects'),
  upload: (form: FormData) => http.post('/projects/upload', form),
  remove: (id: number) => http.delete(`/projects/${id}`),
}
