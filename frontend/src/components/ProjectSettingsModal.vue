<template>
  <div class="fixed inset-0 bg-background/80 backdrop-blur-sm z-50 flex items-center justify-center p-4">
    <div class="bg-card border border-border rounded-2xl shadow-elevated w-full max-w-sm overflow-hidden animate-in fade-in zoom-in duration-200">
      <div class="p-6 border-b border-border flex justify-between items-center">
        <h2 class="text-lg font-bold">访问设置</h2>
        <button @click="$emit('close')" class="text-muted-foreground hover:text-foreground">
          <XIcon class="w-5 h-5" />
        </button>
      </div>
      
      <div class="p-6 space-y-6">
        <div class="flex items-center justify-between p-4 bg-secondary rounded-xl">
          <div>
            <p class="text-sm font-bold">公开访问</p>
            <p class="text-xs text-muted-foreground mt-0.5">{{ isPublic ? '任何人均可查看' : '仅凭密码查看' }}</p>
          </div>
          <label class="relative inline-flex items-center cursor-pointer">
            <input type="checkbox" v-model="isPublic" class="sr-only peer">
            <div class="w-11 h-6 bg-muted peer-focus:outline-none rounded-full peer peer-checked:after:translate-x-full peer-checked:after:border-white after:content-[''] after:absolute after:top-[2px] after:left-[2px] after:bg-white after:border-gray-300 after:border after:rounded-full after:h-5 after:w-5 after:transition-all peer-checked:bg-primary"></div>
          </label>
        </div>

        <div v-if="!isPublic" class="space-y-2 animate-in slide-in-from-top-2 duration-200">
          <label class="text-xs font-bold text-muted-foreground uppercase">设置访问密码</label>
          <div class="relative">
            <input 
              v-model="accessPassword" 
              :type="showPass ? 'text' : 'password'"
              placeholder="留空则保持原密码" 
              class="w-full pl-4 pr-10 py-2.5 bg-secondary border border-border rounded-xl text-sm focus:ring-1 focus:ring-primary outline-none"
            >
            <button @click="showPass = !showPass" class="absolute right-3 top-1/2 -translate-y-1/2 text-muted-foreground">
              <EyeIcon v-if="!showPass" class="w-4 h-4" />
              <EyeOffIcon v-else class="w-4 h-4" />
            </button>
          </div>
          <p class="text-[10px] text-muted-foreground italic">修改为公开访问将自动清除密码。</p>
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
          @click="save"
          :disabled="loading"
          class="flex-1 gradient-primary text-white font-bold py-2.5 rounded-xl shadow-lg shadow-primary/20 hover:opacity-90 transition-opacity text-sm disabled:opacity-50"
        >
          {{ loading ? '保存中...' : '保存设置' }}
        </button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { X as XIcon, Eye as EyeIcon, EyeOff as EyeOffIcon } from 'lucide-vue-next'
import { projectApi } from '@/api/project'
import type { Project } from '@/types'

const props = defineProps<{ project: Project }>()
const emit = defineEmits<{ close: [], updated: [] }>()

const isPublic = ref(props.project.isPublic)
const accessPassword = ref('')
const showPass = ref(false)
const loading = ref(false)

async function save() {
  loading.value = true
  try {
    await projectApi.updateSettings(props.project.id, isPublic.value, accessPassword.value)
    emit('updated')
    emit('close')
  } catch (e) {
    alert('保存失败，请稍后重试')
  } finally {
    loading.value = false
  }
}
</script>
