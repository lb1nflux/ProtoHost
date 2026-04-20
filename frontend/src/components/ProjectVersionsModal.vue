<template>
  <div class="fixed inset-0 bg-background/80 backdrop-blur-sm z-50 flex items-center justify-center p-4">
    <div class="bg-card border border-border rounded-2xl shadow-elevated w-full max-w-lg overflow-hidden animate-in fade-in zoom-in duration-200">
      <div class="p-6 border-b border-border flex justify-between items-center">
        <div>
          <h2 class="text-lg font-bold">版本记录</h2>
          <p class="text-xs text-muted-foreground mt-0.5">{{ project.name }}</p>
        </div>
        <button @click="$emit('close')" class="text-muted-foreground hover:text-foreground">
          <XIcon class="w-5 h-5" />
        </button>
      </div>
      
      <div class="max-h-[60vh] overflow-y-auto p-4">
        <div v-if="loading" class="space-y-3 py-10 text-center">
          <div class="animate-spin h-6 w-6 border-2 border-primary border-t-transparent rounded-full mx-auto"></div>
          <p class="text-xs text-muted-foreground">正在获取历史版本...</p>
        </div>
        
        <div v-else-if="versions.length === 0" class="py-20 text-center">
          <HistoryIcon class="h-10 w-10 text-muted-foreground mx-auto mb-2 opacity-20" />
          <p class="text-sm text-muted-foreground">暂无历史版本</p>
        </div>

        <div v-else class="space-y-2">
          <div 
            v-for="v in versions" 
            :key="v.id"
            class="flex items-center justify-between p-4 bg-secondary/30 border border-border/50 rounded-xl hover:bg-secondary/50 transition-colors group"
          >
            <div class="flex items-center gap-4">
              <div class="h-10 w-10 rounded-full bg-primary/10 flex items-center justify-center text-primary">
                <GitBranchIcon class="h-5 w-5" />
              </div>
              <div>
                <div class="flex items-center gap-2">
                  <span class="font-bold text-sm">{{ formatVersion(v.versionNumber) }}</span>
                  <span v-if="v.versionNumber === project.version" class="bg-primary/20 text-primary text-[10px] px-1.5 py-0.5 rounded font-bold uppercase">当前</span>
                </div>
                <p class="text-[11px] text-muted-foreground mt-0.5">{{ formatDate(v.createdAt) }}</p>
                <p v-if="v.changelog" class="text-xs text-foreground/70 mt-1 italic line-clamp-1">"{{ v.changelog }}"</p>
              </div>
            </div>
            
            <div class="flex items-center gap-2">
              <span class="text-[10px] text-muted-foreground opacity-0 group-hover:opacity-100 transition-opacity mr-2">{{ formatSize(v.fileSize) }}</span>
              <button 
                @click="download(v)"
                class="p-2.5 bg-card border border-border rounded-lg text-muted-foreground hover:text-primary hover:border-primary/50 transition-all shadow-sm"
                title="下载 ZIP"
              >
                <DownloadIcon class="h-4 w-4" />
              </button>
            </div>
          </div>
        </div>
      </div>

      <div class="p-4 bg-secondary/30 border-t border-border flex justify-end">
        <button 
          @click="$emit('close')" 
          class="px-6 py-2 bg-card border border-border text-foreground font-bold rounded-xl hover:bg-secondary transition-colors text-sm shadow-sm"
        >
          关闭
        </button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { X as XIcon, GitBranch as GitBranchIcon, Download as DownloadIcon, History as HistoryIcon } from 'lucide-vue-next'
import { projectApi } from '@/api/project'
import type { Project } from '@/types'

const props = defineProps<{ project: Project }>()
defineEmits<{ close: [] }>()

const versions = ref<any[]>([])
const loading = ref(true)

function formatVersion(v: string) {
  if (!v) return ''
  return /^\d/.test(v) ? `v${v}` : v
}

async function fetchVersions() {
  try {
    const { data } = await projectApi.versions(props.project.id)
    versions.value = data
  } finally {
    loading.value = false
  }
}

function formatDate(d: string) {
  return new Date(d).toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}

function formatSize(bytes: number) {
  if (!bytes) return '0 B'
  const k = 1024
  const sizes = ['B', 'KB', 'MB', 'GB']
  const i = Math.floor(Math.log(bytes) / Math.log(k))
  return parseFloat((bytes / Math.pow(k, i)).toFixed(1)) + ' ' + sizes[i]
}

function download(v: any) {
  const filename = `${props.project.name}_v${v.versionNumber}.zip`
  projectApi.download(v.id, filename)
}

onMounted(fetchVersions)
</script>
