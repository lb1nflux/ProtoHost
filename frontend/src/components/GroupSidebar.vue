<template>
  <aside class="w-64 bg-card border-r border-border flex flex-col shrink-0">
    <div class="p-6 flex items-center gap-3">
      <div class="gradient-primary rounded-lg p-1.5 text-white">
        <LayersIcon class="w-5 h-5" />
      </div>
      <span class="font-bold text-xl tracking-tight">ProtoHost</span>
    </div>

    <nav class="flex-1 px-4 space-y-1 overflow-y-auto">
      <div class="text-xs font-semibold text-muted-foreground uppercase tracking-wider px-2 mb-2 mt-2">项目库</div>
      <button 
        @click="$emit('select', undefined)"
        :class="activeGroupId === undefined ? 'bg-primary/10 text-primary' : 'text-foreground hover:bg-secondary'"
        class="w-full flex items-center justify-between px-3 py-2 rounded-md font-medium transition-colors"
      >
        <div class="flex items-center gap-3 text-sm">
          <LayoutGridIcon class="w-4 h-4" />
          全部项目
        </div>
      </button>
      <button 
        @click="$emit('select', 0)"
        :class="activeGroupId === 0 ? 'bg-primary/10 text-primary' : 'text-foreground hover:bg-secondary'"
        class="w-full flex items-center justify-between px-3 py-2 rounded-md font-medium transition-colors"
      >
        <div class="flex items-center gap-3 text-sm">
          <InboxIcon class="w-4 h-4" />
          未分组
        </div>
      </button>

      <div class="pt-6 pb-2 px-2 flex items-center justify-between">
        <span class="text-xs font-semibold text-muted-foreground uppercase tracking-wider">我的分组</span>
        <button @click="showAdd = true" class="text-muted-foreground hover:text-primary transition-colors">
          <PlusIcon class="w-4 h-4" />
        </button>
      </div>

      <div v-if="showAdd" class="px-2 mb-2">
        <input 
          v-model="newGroupName" 
          @keyup.enter="addGroup" 
          @blur="cancelAdd"
          ref="addInput"
          placeholder="填写名称并按 Enter 保存" 
          class="w-full bg-background border border-border rounded px-2 py-1 text-sm focus:ring-1 focus:ring-primary outline-none"
        />
      </div>

      <div class="space-y-1">
        <div 
          v-for="group in groups" 
          :key="group.id"
          class="group flex items-center"
        >
          <!-- 编辑模式 -->
          <div v-if="editingGroupId === group.id" class="flex-1 px-2">
            <input 
              v-model="editName" 
              @keyup.enter="saveEdit(group.id)" 
              @keyup.esc="cancelEdit"
              @blur="saveEdit(group.id)"
              ref="editInput"
              class="w-full bg-background border border-primary rounded px-2 py-1 text-sm outline-none"
            />
          </div>

          <!-- 普通显示模式 -->
          <button 
            v-else
            @click="$emit('select', group.id)"
            :class="activeGroupId === group.id ? 'bg-primary/10 text-primary' : 'text-foreground hover:bg-secondary'"
            class="flex-1 flex items-center justify-between px-3 py-2 rounded-md transition-colors text-sm"
          >
            <div class="flex items-center gap-3 truncate">
              <FolderIcon class="w-4 h-4 text-amber-500" />
              <span class="truncate">{{ group.name }}</span>
            </div>
            <span class="text-[10px] text-muted-foreground ml-2">{{ group.projectCount }}</span>
          </button>
          
          <div v-if="editingGroupId !== group.id" class="hidden group-hover:flex items-center gap-1 ml-1 pr-1">
            <button @click.stop="startEdit(group)" class="p-1 text-muted-foreground hover:text-primary">
              <Edit2Icon class="w-3 h-3" />
            </button>
            <button @click.stop="deleteGroup(group)" class="p-1 text-muted-foreground hover:text-destructive">
              <Trash2Icon class="w-3 h-3" />
            </button>
          </div>
        </div>
      </div>
    </nav>

    <div class="p-4 border-t border-border">
      <div class="flex items-center gap-3 px-3 py-2 rounded-md hover:bg-secondary cursor-pointer transition-colors group" @click="$emit('logout')">
        <div class="w-8 h-8 rounded-full bg-secondary flex items-center justify-center text-xs font-bold text-muted-foreground uppercase">
          {{ emailPrefix }}
        </div>
        <div class="flex-1 min-w-0">
          <p class="text-sm font-medium truncate">{{ auth.email }}</p>
        </div>
        <LogOutIcon class="w-4 h-4 text-muted-foreground group-hover:text-primary" />
      </div>
    </div>
  </aside>
</template>

<script setup lang="ts">
import { ref, computed, nextTick, watch } from 'vue'
import { 
  Layers as LayersIcon, 
  LayoutGrid as LayoutGridIcon, 
  Inbox as InboxIcon, 
  Plus as PlusIcon, 
  Folder as FolderIcon, 
  LogOut as LogOutIcon,
  Edit2 as Edit2Icon,
  Trash2 as Trash2Icon
} from 'lucide-vue-next'
import { useAuthStore } from '@/stores/auth'
import { groupApi } from '@/api/group'
import type { ProjectGroup } from '@/types'

const props = defineProps<{
  groups: ProjectGroup[]
  activeGroupId?: number
}>()

const emit = defineEmits<{
  select: [id: number | undefined]
  refresh: []
  logout: []
}>()

const auth = useAuthStore()
const showAdd = ref(false)
const newGroupName = ref('')
const addInput = ref<HTMLInputElement>()

// Inline edit state
const editingGroupId = ref<number | null>(null)
const editName = ref('')
const editInput = ref<HTMLInputElement>()

const emailPrefix = computed(() => auth.email?.substring(0, 2) || 'PH')

watch(showAdd, (val) => {
  if (val) {
    nextTick(() => addInput.value?.focus())
  }
})

async function addGroup() {
  if (!newGroupName.value.trim()) {
    showAdd.value = false
    return
  }
  try {
    await groupApi.create(newGroupName.value.trim())
    newGroupName.value = ''
    showAdd.value = false
    emit('refresh')
  } catch (e) {
    console.error(e)
  }
}

function cancelAdd() {
  setTimeout(() => {
    if (showAdd.value) {
      showAdd.value = false
      newGroupName.value = ''
    }
  }, 200)
}

function startEdit(group: ProjectGroup) {
  editingGroupId.value = group.id
  editName.value = group.name
  nextTick(() => editInput.value?.focus())
}

async function saveEdit(id: number) {
  if (editingGroupId.value === null) return
  
  const name = editName.value.trim()
  const original = props.groups.find(g => g.id === id)?.name
  
  if (name && name !== original) {
    try {
      await groupApi.update(id, name)
      emit('refresh')
    } catch (e) {
      console.error(e)
    }
  }
  editingGroupId.value = null
}

function cancelEdit() {
  editingGroupId.value = null
}

async function deleteGroup(group: ProjectGroup) {
  if (confirm(`确定删除分组「${group.name}」？此操作不会删除原型。`)) {
    await groupApi.remove(group.id)
    if (props.activeGroupId === group.id) {
      emit('select', undefined)
    }
    emit('refresh')
  }
}
</script>
