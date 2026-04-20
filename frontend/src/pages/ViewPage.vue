<template>
  <div v-if="loading" class="min-h-screen flex items-center justify-center bg-background">
    <div class="animate-pulse text-muted-foreground">加载中...</div>
  </div>

  <div v-else-if="error" class="min-h-screen flex items-center justify-center bg-background">
    <div class="bg-card border border-border rounded-lg shadow-elevated p-12 text-center max-w-md w-full mx-4">
      <LayersIcon class="h-12 w-12 text-muted-foreground mx-auto mb-4" />
      <p class="font-heading font-semibold text-lg">{{ error }}</p>
    </div>
  </div>

  <!-- 密码验证 -->
  <div v-else-if="needsPassword" class="min-h-screen flex items-center justify-center bg-background px-4">
    <div class="bg-card border border-border rounded-lg shadow-elevated p-6 max-w-md w-full">
      <div class="text-center mb-6">
        <div class="mx-auto mb-3 rounded-xl bg-secondary p-3 w-fit">
          <LockIcon class="h-6 w-6 text-muted-foreground" />
        </div>
        <h2 class="font-heading font-semibold text-lg">{{ meta?.name }}</h2>
        <p class="text-sm text-muted-foreground mt-1">此原型需要密码才能查看</p>
      </div>
      <form @submit.prevent="verifyPassword" class="space-y-4">
        <input v-model="passwordInput" type="password" placeholder="请输入访问密码" autofocus
          autocomplete="new-password"
          class="w-full px-3 py-2 rounded-md border border-border bg-background text-sm focus:outline-none focus:ring-2 focus:ring-primary/50" />
        <p v-if="pwError" class="text-sm text-destructive">{{ pwError }}</p>
        <button type="submit" :disabled="verifying"
          class="w-full gradient-primary text-white font-medium py-2 rounded-md text-sm disabled:opacity-60">
          查看原型
        </button>
      </form>
    </div>
  </div>

  <!-- 原型 iframe -->
  <div v-else :class="fullscreen ? 'fixed inset-0 z-50' : 'h-screen'" class="flex flex-col bg-background">
    <header class="border-b border-border bg-card shrink-0 h-12 flex items-center px-4 justify-between">
      <div class="flex items-center gap-2">
        <div class="gradient-primary rounded p-1"><LayersIcon class="h-3.5 w-3.5 text-white" /></div>
        <span class="font-heading font-semibold text-sm">{{ meta?.name }}</span>
        <span class="text-xs text-muted-foreground">v{{ meta?.version }}</span>
      </div>
      <button @click="fullscreen = !fullscreen" class="p-1.5 rounded hover:bg-secondary">
        <MaximizeIcon v-if="!fullscreen" class="h-4 w-4" />
        <MinimizeIcon v-else class="h-4 w-4" />
      </button>
    </header>
    <div class="flex-1">
      <iframe ref="iframeRef" :src="iframeSrc" class="w-full h-full border-0" :title="meta?.name"
        sandbox="allow-scripts allow-forms allow-popups allow-same-origin"
        @load="onIframeLoad" />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { Layers as LayersIcon, Lock as LockIcon, Maximize as MaximizeIcon, Minimize as MinimizeIcon } from 'lucide-vue-next'
import { shareApi } from '@/api/share'

const route = useRoute()
const slug = route.params.slug as string

const loading = ref(true)
const error = ref('')
const needsPassword = ref(false)
const meta = ref<{ name: string; version: string; isPublic: boolean; entryFile: string } | null>(null)
const passwordInput = ref('')
const pwError = ref('')
const verifying = ref(false)
const iframeSrc = ref('')
const fullscreen = ref(false)
const iframeRef = ref<HTMLIFrameElement | null>(null)
let currentViewToken = ''

function buildSrc(token?: string) {
  const base = `/api/share/${slug}/files/${meta.value!.entryFile}`
  return token ? `${base}?viewToken=${token}` : base
}

function onIframeLoad() {
  if (!currentViewToken || !iframeRef.value) return
  try {
    const iframeUrl = iframeRef.value.contentWindow?.location.href
    if (iframeUrl && iframeUrl.indexOf('viewToken') < 0 && iframeUrl.indexOf('/api/share/') >= 0) {
      const sep = iframeUrl.indexOf('?') >= 0 ? '&' : '?'
      iframeSrc.value = iframeUrl + sep + 'viewToken=' + currentViewToken
    }
  } catch {
    // cross-origin, ignore
  }
}

onMounted(async () => {
  try {
    const { data } = await shareApi.meta(slug)
    meta.value = data
    if (data.isPublic) {
      iframeSrc.value = buildSrc()
    } else {
      const saved = sessionStorage.getItem(`vt_${slug}`)
      if (saved) { currentViewToken = saved; iframeSrc.value = buildSrc(saved) }
      else { needsPassword.value = true }
    }
  } catch {
    error.value = '项目不存在或已被删除'
  } finally {
    loading.value = false
  }
})

async function verifyPassword() {
  pwError.value = ''
  verifying.value = true
  try {
    const { data } = await shareApi.verify(slug, passwordInput.value)
    sessionStorage.setItem(`vt_${slug}`, data.viewToken)
    currentViewToken = data.viewToken
    iframeSrc.value = buildSrc(data.viewToken)
    needsPassword.value = false
  } catch {
    pwError.value = '密码错误'
  } finally {
    verifying.value = false
  }
}
</script>
