<template>
  <div class="fixed inset-0 bg-background/80 backdrop-blur-sm z-50 flex items-center justify-center p-4">
    <div class="bg-card border border-border rounded-2xl shadow-elevated w-full max-w-sm overflow-hidden animate-in fade-in zoom-in duration-200">
      <div class="p-6 border-b border-border flex justify-between items-center">
        <div>
          <h2 class="text-lg font-bold">移动至分组</h2>
          <p class="text-xs text-muted-foreground mt-0.5">选择目标位置</p>
        </div>
        <button @click="$emit('close')" class="text-muted-foreground hover:text-foreground">
          <XIcon class="w-5 h-5" />
        </button>
      </div>
      
      <div class="max-h-[50vh] overflow-y-auto p-2">
        <div class="space-y-1">
          <!-- 未分组选项 -->
          <button 
            @click="selectedGroupId = undefined"
            :class="selectedGroupId === undefined ? 'bg-primary/10 text-primary border-primary/20' : 'hover:bg-secondary border-transparent'"
            class="w-full flex items-center gap-3 px-4 py-3 rounded-xl border text-sm font-medium transition-all group"
          >
            <div class="h-8 w-8 rounded-lg bg-secondary flex items-center justify-center group-hover:bg-card transition-colors">
              <InboxIcon class="h-4 w-4" />
            </div>
            <span>未分组</span>
            <CheckIcon v-if="selectedGroupId === undefined" class="h-4 w-4 ml-auto" />
          </button>

          <!-- 自定义分组列表 -->
          <button 
            v-for="g in groups" 
            :key="g.id"
            @click="selectedGroupId = g.id"
            :class="selectedGroupId === g.id ? 'bg-primary/10 text-primary border-primary/20' : 'hover:bg-secondary border-transparent'"
            class="w-full flex items-center gap-3 px-4 py-3 rounded-xl border text-sm font-medium transition-all group"
          >
            <div class="h-8 w-8 rounded-lg bg-amber-50 flex items-center justify-center group-hover:bg-card transition-colors">
              <FolderIcon class="h-4 w-4 text-amber-500" />
            </div>
            <span class="truncate">{{ g.name }}</span>
            <CheckIcon v-if="selectedGroupId === g.id" class="h-4 w-4 ml-auto" />
          </button>
        </div>
      </div>

      <div class="p-4 bg-secondary/30 flex gap-3">
        <button 
          @click="$emit('close')" 
          class="flex-1 bg-card border border-border text-foreground font-bold py-2.5 rounded-xl hover:bg-secondary transition-colors text-sm shadow-sm"
        >
          取消
        </button>
        <button 
          @click="confirmMove"
          :disabled="loading || selectedGroupId === (project.groupId || undefined)"
          class="flex-1 gradient-primary text-white font-bold py-2.5 rounded-xl shadow-lg shadow-primary/20 hover:opacity-90 transition-opacity text-sm disabled:opacity-50"
        >
          {{ loading ? '移动中...' : '确认移动' }}
        </button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { X as XIcon, Inbox as InboxIcon, Folder as FolderIcon, Check as CheckIcon } from 'lucide-vue-next'
import { projectApi } from '@/api/project'
import type { Project, ProjectGroup } from '@/types'

const props = defineProps<{ 
  project: Project
  groups: ProjectGroup[]
}>()

const emit = defineEmits<{ close: [], updated: [] }>()

const selectedGroupId = ref<number | undefined>(props.project.groupId || undefined)
const loading = ref(false)

async function confirmMove() {
  loading.value = true
  try {
    await projectApi.move(props.project.id, selectedGroupId.value ?? null)
    emit('updated')
    emit('close')
  } catch (e) {
    alert('移动失败')
  } finally {
    loading.value = false
  }
}
</script>
