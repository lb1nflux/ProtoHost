import http from './http'
import type { ProjectGroup } from '@/types'

export const groupApi = {
  list: () => http.get<ProjectGroup[]>('/groups'),
  create: (name: string) => http.post<ProjectGroup>('/groups', { name }),
  update: (id: number, name: string) => http.put(`/groups/${id}`, { name }),
  remove: (id: number) => http.delete(`/groups/${id}`),
  sort: (ids: number[]) => http.put('/groups/sort', ids),
}
