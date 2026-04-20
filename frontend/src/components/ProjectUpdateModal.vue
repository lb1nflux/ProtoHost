<template>
  <div class="fixed inset-0 bg-background/80 backdrop-blur-sm z-50 flex items-center justify-center p-4">
    <div class="bg-card border border-border rounded-2xl shadow-elevated w-full max-w-md overflow-hidden animate-in fade-in zoom-in duration-200">
      <div class="p-6 border-b border-border flex justify-between items-center">
        <h2 class="text-lg font-bold">更新版本：{{ project.name }}</h2>
        <button @click="$emit('close')" class="text-muted-foreground hover:text-foreground">
          <XIcon class="w-5 h-5" />
        </button>
      </div>
      
      <div class="p-6 space-y-5">
        <!-- 模式切换 -->
        <div class="grid grid-cols-2 gap-2 p-1 bg-secondary rounded-lg">
          <button 
            @click="handleModeChange(false)"
            :class="!isReplace ? 'bg-card text-primary shadow-sm' : 'text-muted-foreground hover:text-foreground'"
            class="px-3 py-1.5 rounded-md text-sm font-semibold transition-all"
          >
            上传新版本
          </button>
          <button 
            @click="handleModeChange(true)"
            :class="isReplace ? 'bg-card text-primary shadow-sm' : 'text-muted-foreground hover:text-foreground'"
            class="px-3 py-1.5 rounded-md text-sm font-semibold transition-all"
          >
            覆盖当前版
          </button>
        </div>

        <!-- 上传区 -->
        <div 
          @click="fileInput?.click()"
          class="border-2 border-dashed border-border rounded-xl p-8 flex flex-col items-center justify-center gap-2 hover:border-primary/50 transition-colors bg-secondary/30 cursor-pointer"
        >
          <input type="file" ref="fileInput" @change="handleFile" class="hidden" accept=".zip" />
          <UploadCloudIcon class="w-10 h-10 text-primary/50" />
          <p class="text-sm font-medium">{{ fileName || '点击选择新 ZIP 文件' }}</p>
          <p class="text-xs text-muted-foreground italic">支持 .zip 格式 (Max 100MB)</p>
        </div>

        <!-- 版本信息 -->
        <div class="space-y-4">
          <div v-if="!isReplace">
            <label class="block text-xs font-bold text-muted-foreground uppercase mb-1">新版本号 *</label>
            <input 
              v-model="version" 
              type="text" 
              required
              placeholder="例如 1.3" 
              class="w-full px-4 py-2 bg-secondary/50 border border-border rounded-lg text-sm focus:ring-1 focus:ring-primary outline-none"
            >
          </div>
          <div>
            <label class="block text-xs font-bold text-muted-foreground uppercase mb-1">更新日志</label>
            <textarea 
              v-model="changelog"
              placeholder="描述本次更新的内容..." 
              rows="3" 
              class="w-full px-4 py-2 bg-secondary/50 border border-border rounded-lg text-sm focus:ring-1 focus:ring-primary outline-none"
            ></textarea>
          </div>
        </div>

        <!-- 覆盖提示 -->
        <div v-if="isReplace" class="bg-amber-50 border border-amber-100 rounded-lg p-3 flex gap-3">
          <AlertTriangleIcon class="w-5 h-5 text-amber-500 shrink-0" />
          <p class="text-[11px] text-amber-700 leading-relaxed">
            <b>覆盖警告：</b>系统将物理删除服务器上的当前版本文件并替换。该操作不可逆，但能节省空间。
          </p>
        </div>
      </div>

      <div class="p-6 bg-secondary/30 flex gap-3">
        <button 
          @click="$emit('close')" 
          class="flex-1 bg-card border border-border text-foreground font-bold py-2.5 rounded-xl hover:bg-secondary transition-colors text-sm"
        >
          取消
        </button>
        <button 
          @click="submit"
          :disabled="loading || !file"
          class="flex-1 gradient-primary text-white font-bold py-2.5 rounded-xl shadow-lg shadow-primary/20 hover:opacity-90 transition-opacity text-sm disabled:opacity-50"
        >
          {{ loading ? '上传中...' : '开始上传' }}
        </button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { X as XIcon, UploadCloud as UploadCloudIcon, AlertTriangle as AlertTriangleIcon } from 'lucide-vue-next'
import { projectApi } from '@/api/project'
import type { Project } from '@/types'

const props = defineProps<{ project: Project }>()
const emit = defineEmits<{ close: [], updated: [] }>()

const fileInput = ref<HTMLInputElement>()
const file = ref<File | null>(null)
const fileName = ref('')
const isReplace = ref(false)
const version = ref('')
const changelog = ref('')
const loading = ref(false)

// Cache for original changelogs
const versionMap = new Map<string, string>()

async function fetchCurrentChangelog() {
  try {
    const { data } = await projectApi.versions(props.project.id)
    // Find the current version's changelog
    const current = data.find((v: any) => v.versionNumber === props.project.version)
    if (current && current.changelog) {
      changelog.value = current.changelog
    }
  } catch (e) {
    console.error('Failed to fetch changelog', e)
  }
}

function handleModeChange(replace: boolean) {
  isReplace.value = replace
  if (replace) {
    fetchCurrentChangelog()
  } else {
    changelog.value = ''
  }
}

function handleFile(e: Event) {
  const target = e.target as HTMLInputElement
  if (target.files && target.files[0]) {
    file.value = target.files[0]
    fileName.value = file.value.name
  }
}

async function submit() {
  if (!file.value) return
  if (!isReplace.value && !version.value.trim()) {
    alert('请输入新版本号')
    return
  }
  loading.value = true
  try {
    const formData = new FormData()
    formData.append('file', file.value)
    formData.append('projectId', props.project.id.toString())
    formData.append('name', props.project.name)
    formData.append('version', version.value || props.project.version)
    formData.append('isReplace', isReplace.value.toString())
    formData.append('changelog', changelog.value)
    formData.append('isPublic', props.project.isPublic.toString())
    
    await projectApi.upload(formData)
    emit('updated')
    emit('close')
  } catch (e) {
    alert('上传失败，请重试')
  } finally {
    loading.value = false
  }
}
</script>
