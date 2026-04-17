<template>
  <div class="bg-card border border-border rounded-lg shadow-card hover:shadow-elevated transition-shadow p-5">
    <div class="flex items-start justify-between mb-2">
      <div class="flex-1 min-w-0">
        <h3 class="font-heading font-semibold truncate">{{ project.name }}</h3>
        <p class="text-sm text-muted-foreground">v{{ project.version }}</p>
      </div>
      <span :class="project.isPublic ? 'bg-primary/10 text-primary' : 'bg-secondary text-muted-foreground'"
        class="text-xs px-2 py-0.5 rounded-full flex items-center gap-1 shrink-0 ml-2">
        <GlobeIcon v-if="project.isPublic" class="h-3 w-3" />
        <LockIcon v-else class="h-3 w-3" />
        {{ project.isPublic ? '公开' : '私密' }}
      </span>
    </div>
    <p v-if="project.description" class="text-sm text-muted-foreground line-clamp-2 mb-3">{{ project.description }}</p>
    <p class="text-xs text-muted-foreground mb-4">更新于 {{ formatDate(project.updatedAt) }}</p>
    <div class="flex gap-2">
      <a :href="`/view/${project.shareSlug}`" target="_blank"
        class="flex-1 text-center text-sm border border-border rounded-md py-1.5 hover:bg-secondary flex items-center justify-center gap-1">
        <ExternalLinkIcon class="h-3 w-3" />查看
      </a>
      <button @click="copyLink" class="border border-border rounded-md p-1.5 hover:bg-secondary">
        <CopyIcon class="h-3.5 w-3.5" />
      </button>
      <button @click="del" class="border border-border rounded-md p-1.5 hover:bg-secondary text-destructive">
        <Trash2Icon class="h-3.5 w-3.5" />
      </button>
    </div>
    <p v-if="toast" class="text-xs text-primary mt-2">{{ toast }}</p>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { Globe as GlobeIcon, Lock as LockIcon, ExternalLink as ExternalLinkIcon, Copy as CopyIcon, Trash2 as Trash2Icon } from 'lucide-vue-next'
import { projectApi } from '@/api/project'
import type { Project } from '@/types'

const props = defineProps<{ project: Project }>()
const emit = defineEmits<{ deleted: [] }>()
const toast = ref('')

function formatDate(d: string) {
  return new Date(d).toLocaleDateString('zh-CN')
}

function copyLink() {
  navigator.clipboard.writeText(`${location.origin}/view/${props.project.shareSlug}`)
  toast.value = '链接已复制'
  setTimeout(() => toast.value = '', 2000)
}

async function del() {
  if (!confirm(`确定删除「${props.project.name}」？`)) return
  await projectApi.remove(props.project.id)
  emit('deleted')
}
</script>
