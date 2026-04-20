<template>
  <div class="bg-card border border-border rounded-xl shadow-card hover:shadow-elevated transition-all p-5 group">
    <div class="flex items-start justify-between mb-3">
      <div class="flex-1 min-w-0">
        <h3 class="font-heading font-bold text-lg truncate group-hover:text-primary transition-colors">{{ project.name }}</h3>
        <p class="text-[10px] text-muted-foreground uppercase font-semibold tracking-tighter opacity-50">{{ project.shareSlug }}</p>
      </div>
      <div class="flex items-center gap-2">
        <span :class="project.isPublic ? 'bg-emerald-50 text-emerald-600' : 'bg-secondary text-muted-foreground'"
          class="text-[10px] px-2 py-0.5 rounded font-bold flex items-center gap-1 shrink-0">
          <GlobeIcon v-if="project.isPublic" class="h-3 w-3" />
          <LockIcon v-else class="h-3 w-3" />
          {{ project.isPublic ? '公开' : '私密' }}
        </span>
        
        <div class="relative">
          <button @click.stop="showMenu = !showMenu" class="p-1 hover:bg-secondary rounded text-muted-foreground">
            <MoreVerticalIcon class="h-4 w-4" />
          </button>
          
          <div v-if="showMenu" v-click-outside="() => showMenu = false" class="absolute right-0 mt-2 w-48 bg-card border border-border rounded-lg shadow-elevated z-10 py-1">
            <button @click="openUpdate" class="w-full text-left px-4 py-2 text-xs hover:bg-secondary flex items-center gap-2">
              <UploadCloudIcon class="h-3.5 w-3.5" />更新版本
            </button>
            <button @click="openSettings" class="w-full text-left px-4 py-2 text-xs hover:bg-secondary flex items-center gap-2">
              <SettingsIcon class="h-3.5 w-3.5" />访问设置
            </button>
            <button @click="openVersions" class="w-full text-left px-4 py-2 text-xs hover:bg-secondary flex items-center gap-2 border-t border-border mt-1 pt-1">
              <HistoryIcon class="h-3.5 w-3.5" />版本记录
            </button>
            <button @click="openMove" class="w-full text-left px-4 py-2 text-xs hover:bg-secondary flex items-center gap-2">
              <FolderIcon class="h-3.5 w-3.5" />移动至分组
            </button>

            <button @click="del" class="w-full text-left px-4 py-2 text-xs hover:bg-secondary text-destructive flex items-center gap-2 border-t border-border mt-1">
              <Trash2Icon class="h-3.5 w-3.5" />删除项目
            </button>
          </div>
        </div>
      </div>
    </div>

    <p v-if="project.description" class="text-sm text-muted-foreground line-clamp-2 mb-4 h-10">{{ project.description }}</p>
    <div v-else class="h-10 mb-4"></div>

    <!-- 统计信息 -->
    <div class="grid grid-cols-2 gap-y-3 mb-5 border-y border-border/50 py-3">
      <div class="flex items-center gap-2 text-muted-foreground">
        <ClockIcon class="w-3.5 h-3.5 opacity-60" />
        <span class="text-[11px]">{{ formatTime(project.lastUpdatedAt || project.updatedAt) }}更新</span>
      </div>
      <div class="flex items-center gap-2 text-muted-foreground">
        <HardDriveIcon class="w-3.5 h-3.5 opacity-60" />
        <span class="text-[11px]">{{ formatSize(project.fileSize) }}</span>
      </div>
      <div class="flex items-center gap-2 text-muted-foreground">
        <EyeIcon class="w-3.5 h-3.5 opacity-60" />
        <span class="text-[11px]">{{ project.viewCount }} 次浏览</span>
      </div>
      <div class="flex items-center gap-2 text-primary">
        <GitBranchIcon class="w-3.5 h-3.5 opacity-80" />
        <span class="text-[11px] font-bold">{{ formatVersion(project.version) }}</span>
      </div>
    </div>

    <div class="flex gap-2">
      <a :href="`/view/${project.shareSlug}`" target="_blank"
        class="flex-1 text-center text-xs font-bold bg-foreground text-background rounded-lg py-2 hover:opacity-90 transition-opacity flex items-center justify-center gap-1.5 shadow-sm">
        <ExternalLinkIcon class="h-3.5 w-3.5" />查看原型
      </a>
      <button @click="copyLink" class="border border-border rounded-lg p-2 hover:bg-secondary transition-colors" title="复制链接">
        <CopyIcon class="h-4 w-4" />
      </button>
    </div>
    
    <p v-if="toast" class="text-[10px] text-primary mt-2 text-center font-medium animate-pulse">{{ toast }}</p>

    <ProjectUpdateModal 
      v-if="showUpdateModal" 
      :project="project" 
      @close="showUpdateModal = false" 
      @updated="$emit('updated')"
    />

    <ProjectSettingsModal
      v-if="showSettingsModal"
      :project="project"
      @close="showSettingsModal = false"
      @updated="$emit('updated')"
    />

    <ProjectVersionsModal
      v-if="showVersionsModal"
      :project="project"
      @close="showVersionsModal = false"
    />

    <ProjectMoveModal
      v-if="showMoveModal"
      :project="project"
      :groups="groups"
      @close="showMoveModal = false"
      @updated="$emit('updated')"
    />
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { 
  Globe as GlobeIcon, 
  Lock as LockIcon, 
  ExternalLink as ExternalLinkIcon, 
  Copy as CopyIcon, 
  Trash2 as Trash2Icon,
  Clock as ClockIcon,
  HardDrive as HardDriveIcon,
  Eye as EyeIcon,
  GitBranch as GitBranchIcon,
  MoreVertical as MoreVerticalIcon,
  UploadCloud as UploadCloudIcon,
  Inbox as InboxIcon,
  Folder as FolderIcon,
  Settings as SettingsIcon,
  History as HistoryIcon
} from 'lucide-vue-next'
import { projectApi } from '@/api/project'
import ProjectUpdateModal from './ProjectUpdateModal.vue'
import ProjectSettingsModal from './ProjectSettingsModal.vue'
import ProjectVersionsModal from './ProjectVersionsModal.vue'
import ProjectMoveModal from './ProjectMoveModal.vue'
import type { Project, ProjectGroup } from '@/types'

const props = defineProps<{ 
  project: Project
  groups: ProjectGroup[]
}>()
const emit = defineEmits<{ deleted: [], updated: [] }>()

const toast = ref('')
const showMenu = ref(false)
const showUpdateModal = ref(false)
const showSettingsModal = ref(false)
const showVersionsModal = ref(false)
const showMoveModal = ref(false)

function formatVersion(v: string) {
  if (!v) return ''
  return /^\d/.test(v) ? `v${v}` : v
}

function formatTime(d: string) {
  const date = new Date(d)
  const now = new Date()
  const diff = now.getTime() - date.getTime()
  
  if (diff < 60000) return '刚刚'
  if (diff < 3600000) return Math.floor(diff / 60000) + '分钟前'
  if (diff < 86400000) return Math.floor(diff / 3600000) + '小时前'
  
  return date.toLocaleDateString('zh-CN', { month: 'short', day: 'numeric' })
}

function formatSize(bytes: number) {
  if (!bytes) return '0 B'
  const k = 1024
  const sizes = ['B', 'KB', 'MB', 'GB']
  const i = Math.floor(Math.log(bytes) / Math.log(k))
  return parseFloat((bytes / Math.pow(k, i)).toFixed(1)) + ' ' + sizes[i]
}

async function copyLink() {
  const url = `${location.origin}/view/${props.project.shareSlug}`

  try {
    if (navigator.clipboard?.writeText) {
      await navigator.clipboard.writeText(url)
    } else {
      const input = document.createElement('textarea')
      input.value = url
      input.setAttribute('readonly', '')
      input.style.position = 'absolute'
      input.style.left = '-9999px'
      document.body.appendChild(input)
      input.select()
      document.execCommand('copy')
      document.body.removeChild(input)
    }

    toast.value = '链接已复制到剪贴板'
  } catch {
    toast.value = '复制失败，请手动复制链接'
  }

  setTimeout(() => toast.value = '', 2000)
}

function openUpdate() {
  showMenu.value = false
  showUpdateModal.value = true
}

function openSettings() {
  showMenu.value = false
  showSettingsModal.value = true
}

function openVersions() {
  showMenu.value = false
  showVersionsModal.value = true
}

function openMove() {
  showMenu.value = false
  showMoveModal.value = true
}

async function del() {
  showMenu.value = false
  if (!confirm(`确定删除「${props.project.name}」？此操作将永久移除所有版本。`)) return
  await projectApi.remove(props.project.id)
  emit('deleted')
}

// Simple click outside directive
const vClickOutside = {
  mounted(el: any, binding: any) {
    el.clickOutsideEvent = (event: Event) => {
      if (!(el === event.target || el.contains(event.target))) {
        binding.value(event)
      }
    }
    document.addEventListener('click', el.clickOutsideEvent)
  },
  unmounted(el: any) {
    document.removeEventListener('click', el.clickOutsideEvent)
  }
}
</script>
