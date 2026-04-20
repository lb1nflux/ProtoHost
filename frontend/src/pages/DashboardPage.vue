<template>
  <div class="flex h-screen bg-background overflow-hidden">
    <!-- 侧边栏 -->
    <GroupSidebar 
      :groups="groups" 
      :active-group-id="activeGroupId"
      @select="selectGroup"
      @refresh="fetchGroups"
      @logout="logout"
    />

    <!-- 主内容区 -->
    <main class="flex-1 flex flex-col min-w-0">
      <header class="h-16 border-b border-border bg-card flex items-center justify-between px-8">
        <div class="flex items-center gap-3">
          <h1 class="text-xl font-bold font-heading">{{ currentGroupName }}</h1>
        </div>
        <div class="flex items-center gap-4">
          <router-link to="/upload"
            class="gradient-primary text-white text-sm font-medium px-4 py-2 rounded-md flex items-center gap-2 shadow-lg shadow-primary/20 hover:opacity-90 transition-opacity">
            <PlusIcon class="h-4 w-4" />上传原型
          </router-link>
        </div>
      </header>

      <div class="flex-1 overflow-y-auto p-8">
        <div v-if="loading" class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
          <div v-for="i in 3" :key="i" class="bg-card border border-border rounded-xl p-5 animate-pulse h-48" />
        </div>

        <div v-else-if="projects.length === 0"
          class="bg-card border border-border rounded-xl shadow-card flex flex-col items-center justify-center py-20 text-center">
          <LayersIcon class="h-16 w-16 text-muted-foreground mb-4 opacity-20" />
          <h3 class="font-heading font-semibold text-lg mb-2">还没有原型</h3>
          <p class="text-muted-foreground text-sm mb-8 max-w-xs">上传你的第一个 Axure 原型到此分组开始使用</p>
          <router-link to="/upload"
            class="gradient-primary text-white text-sm font-medium px-6 py-2.5 rounded-lg flex items-center gap-2">
            <PlusIcon class="h-4 w-4" />上传原型
          </router-link>
        </div>

        <div v-else class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
          <ProjectCard 
            v-for="p in projects" 
            :key="p.id" 
            :project="p" 
            @deleted="refresh" 
            @updated="refresh"
            :groups="groups"
          />
        </div>
      </div>
    </main>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { Layers as LayersIcon, Plus as PlusIcon } from 'lucide-vue-next'
import { useAuthStore } from '@/stores/auth'
import { projectApi } from '@/api/project'
import { groupApi } from '@/api/group'
import ProjectCard from '@/components/ProjectCard.vue'
import GroupSidebar from '@/components/GroupSidebar.vue'
import type { Project, ProjectGroup } from '@/types'

const auth = useAuthStore()
const router = useRouter()
const projects = ref<Project[]>([])
const groups = ref<ProjectGroup[]>([])
const loading = ref(true)
const activeGroupId = ref<number | undefined>(undefined)

const currentGroupName = computed(() => {
  if (activeGroupId.value === undefined) return '全部项目'
  if (activeGroupId.value === 0) return '未分组'
  return groups.value.find(g => g.id === activeGroupId.value)?.name || '未知分组'
})

async function fetchGroups() {
  const { data } = await groupApi.list()
  groups.value = data
}

async function fetchProjects() {
  loading.value = true
  try {
    const { data } = await projectApi.list(activeGroupId.value)
    projects.value = data
  } finally {
    loading.value = false
  }
}

function selectGroup(id: number | undefined) {
  activeGroupId.value = id
  fetchProjects()
}

function refresh() {
  fetchProjects()
  fetchGroups()
}

function logout() {
  auth.logout()
  router.push('/login')
}

onMounted(async () => {
  await Promise.all([fetchGroups(), fetchProjects()])
})
</script>
