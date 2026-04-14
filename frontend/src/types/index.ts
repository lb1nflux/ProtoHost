export interface Project {
  id: number
  name: string
  version: string
  description?: string
  isPublic: boolean
  shareSlug: string
  entryFile: string
  createdAt: string
  updatedAt: string
}
