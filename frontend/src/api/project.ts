import http from './http'
import type { Project } from '@/types'

export const projectApi = {
  list: (groupId?: number) => http.get<Project[]>('/projects', { params: { groupId } }),
  upload: (form: FormData) => http.post<Project>('/projects/upload', form, {
    headers: { 'Content-Type': 'multipart/form-data' }
  }),
  remove: (id: number) => http.delete(`/projects/${id}`),
  move: (id: number, groupId: number | null) => http.put(`/projects/${id}/group`, null, { params: { groupId: groupId ?? 0 } }),
  updateSettings: (id: number, isPublic: boolean, accessPassword?: string) => 
    http.put(`/projects/${id}/settings`, null, { params: { isPublic, accessPassword } }),
  versions: (id: number) => http.get<any[]>(`/projects/${id}/versions`),
  download: async (versionId: number, filename: string = 'prototype.zip') => {
    const response = await http.get(`/projects/versions/${versionId}/download`, {
      responseType: 'blob'
    })
    const url = window.URL.createObjectURL(new Blob([response.data]))
    const link = document.createElement('a')
    link.href = url
    link.setAttribute('download', filename)
    document.body.appendChild(link)
    link.click()
    link.remove()
    window.URL.revokeObjectURL(url)
  }
}
