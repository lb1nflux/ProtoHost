<template>
  <div class="min-h-screen bg-background">
    <header class="border-b border-border bg-card">
      <div class="container mx-auto flex items-center justify-between h-16 px-4">
        <div class="flex items-center gap-3">
          <div class="gradient-primary rounded-lg p-1.5">
            <LayersIcon class="h-5 w-5 text-white" />
          </div>
          <span class="font-heading font-bold text-lg">ProtoHost</span>
        </div>
        <div class="flex items-center gap-3">
          <span class="text-sm text-muted-foreground hidden sm:block">{{ auth.email }}</span>
          <button @click="logout" class="p-2 rounded-md hover:bg-secondary">
            <LogOutIcon class="h-4 w-4" />
          </button>
        </div>
      </div>
    </header>

    <main class="container mx-auto px-4 py-8 max-w-5xl">
      <div class="flex items-center justify-between mb-8">
        <div>
          <h1 class="text-2xl font-heading font-bold">我的原型</h1>
          <p class="text-muted-foreground text-sm mt-1">管理和分享你的 Axure 原型</p>
        </div>
        <router-link to="/upload"
          class="gradient-primary text-white text-sm font-medium px-4 py-2 rounded-md flex items-center gap-2">
          <PlusIcon class="h-4 w-4" />上传原型
        </router-link>
      </div>

      <div v-if="loading" class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
        <div v-for="i in 3" :key="i" class="bg-card border border-border rounded-lg p-5 animate-pulse h-40" />
      </div>

      <div v-else-if="projects.length === 0"
        class="bg-card border border-border rounded-lg shadow-card flex flex-col items-center justify-center py-16 text-center">
        <LayersIcon class="h-12 w-12 text-muted-foreground mb-4" />
        <h3 class="font-heading font-semibold text-lg mb-2">还没有原型</h3>
        <p class="text-muted-foreground text-sm mb-6">上传你的第一个 Axure 原型开始使用</p>
        <router-link to="/upload"
          class="gradient-primary text-white text-sm font-medium px-4 py-2 rounded-md flex items-center gap-2">
          <PlusIcon class="h-4 w-4" />上传原型
        </router-link>
      </div>

      <div v-else class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
        <ProjectCard v-for="p in projects" :key="p.id" :project="p" @deleted="fetchProjects" />
      </div>
    </main>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { Layers as LayersIcon, Plus as PlusIcon, LogOut as LogOutIcon } from 'lucide-vue-next'
import { useAuthStore } from '@/stores/auth'
import { projectApi } from '@/api/project'
import ProjectCard from '@/components/ProjectCard.vue'
import type { Project } from '@/types'

const auth = useAuthStore()
const router = useRouter()
const projects = ref<Project[]>([])
const loading = ref(true)

async function fetchProjects() {
  loading.value = true
  try {
    const { data } = await projectApi.list()
    projects.value = data
  } finally {
    loading.value = false
  }
}

function logout() {
  auth.logout()
  router.push('/login')
}

onMounted(fetchProjects)
</script>
