<template>
  <div class="min-h-screen flex items-center justify-center bg-background px-4">
    <div class="w-full max-w-md space-y-8">
      <div class="flex flex-col items-center gap-3">
        <div class="gradient-primary rounded-xl p-3">
          <LayersIcon class="h-8 w-8 text-white" />
        </div>
        <h1 class="text-3xl font-bold font-heading text-foreground">ProtoHost</h1>
        <p class="text-muted-foreground text-sm">原型托管，一键分享</p>
      </div>
      <div class="bg-card border border-border rounded-lg shadow-elevated p-6 space-y-4">
        <h2 class="text-xl font-heading font-semibold text-center">注册账号</h2>
        <form @submit.prevent="submit" class="space-y-4">
          <div class="space-y-1">
            <label class="text-sm font-medium">邮箱</label>
            <div class="flex gap-2">
              <input v-model="email" type="email" required placeholder="your@email.com"
                class="flex-1 px-3 py-2 rounded-md border border-border bg-background text-sm focus:outline-none focus:ring-2 focus:ring-primary/50" />
              <button type="button" :disabled="countdown > 0 || !email || sendingCode" @click="sendCode"
                class="px-3 py-2 bg-primary text-white text-xs font-medium rounded-md disabled:bg-muted disabled:text-muted-foreground transition-all">
                {{ countdown > 0 ? `${countdown}s` : '发送验证码' }}
              </button>
            </div>
          </div>
          <div class="space-y-1">
            <label class="text-sm font-medium">验证码</label>
            <input v-model="code" type="text" required maxlength="6" placeholder="输入 6 位验证码"
              class="w-full px-3 py-2 rounded-md border border-border bg-background text-sm text-center font-mono tracking-widest focus:outline-none focus:ring-2 focus:ring-primary/50" />
          </div>
          <div class="space-y-1">
            <label class="text-sm font-medium">设置密码</label>
            <input v-model="password" type="password" required minlength="6" placeholder="••••••••"
              class="w-full px-3 py-2 rounded-md border border-border bg-background text-sm focus:outline-none focus:ring-2 focus:ring-primary/50" />
          </div>
          <p v-if="error" class="text-sm text-destructive">{{ error }}</p>
          <button type="submit" :disabled="loading"
            class="w-full gradient-primary text-white font-medium py-2 rounded-md text-sm disabled:opacity-60">
            {{ loading ? '处理中...' : '注册' }}
          </button>
        </form>
        <p class="text-center text-sm">
          <router-link to="/login" class="text-primary hover:underline">已有账号？去登录</router-link>
        </p>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { Layers as LayersIcon } from 'lucide-vue-next'
import { authApi } from '@/api/auth'
import { useAuthStore } from '@/stores/auth'

const router = useRouter()
const authStore = useAuthStore()
const email = ref('')
const password = ref('')
const code = ref('')
const loading = ref(false)
const sendingCode = ref(false)
const countdown = ref(0)
const error = ref('')
let timer: any = null

async function sendCode() {
  if (!email.value) return
  error.value = ''
  sendingCode.value = true
  try {
    await authApi.sendRegisterCode(email.value)
    countdown.value = 60
    timer = setInterval(() => {
      countdown.value--
      if (countdown.value <= 0) clearInterval(timer)
    }, 1000)
  } catch (e: any) {
    error.value = e || '验证码发送失败'
  } finally {
    sendingCode.value = false
  }
}

async function submit() {
  error.value = ''
  loading.value = true
  try {
    await authStore.register(email.value, password.value, code.value)
    router.push('/')
  } catch (e: any) {
    error.value = e || '注册失败'
  } finally {
    loading.value = false
  }
}

onUnmounted(() => {
  if (timer) clearInterval(timer)
})
</script>
