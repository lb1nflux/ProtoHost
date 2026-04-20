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
        <h2 class="text-xl font-heading font-semibold text-center">找回密码</h2>
        <p class="text-muted-foreground text-sm text-center">验证码将发送至您的注册邮箱</p>
        
        <form @submit.prevent="submit" class="space-y-4">
          <div class="space-y-1">
            <label class="text-sm font-medium">注册邮箱</label>
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
            <label class="text-sm font-medium">新密码</label>
            <input v-model="password" type="password" required minlength="6" placeholder="输入新密码"
              class="w-full px-3 py-2 rounded-md border border-border bg-background text-sm focus:outline-none focus:ring-2 focus:ring-primary/50" />
          </div>
          <div class="space-y-1">
            <label class="text-sm font-medium">确认新密码</label>
            <input v-model="confirmPassword" type="password" required minlength="6" placeholder="再次输入新密码"
              class="w-full px-3 py-2 rounded-md border border-border bg-background text-sm focus:outline-none focus:ring-2 focus:ring-primary/50" />
          </div>
          <p v-if="error" class="text-sm text-destructive">{{ error }}</p>
          <p v-if="success" class="text-sm text-green-600">{{ success }}</p>
          
          <button type="submit" :disabled="loading"
            class="w-full gradient-primary text-white font-medium py-2 rounded-md text-sm disabled:opacity-60">
            {{ loading ? '处理中...' : '重置密码' }}
          </button>
        </form>
        
        <p class="text-center text-sm">
          <router-link to="/login" class="text-primary hover:underline">返回登录</router-link>
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

const router = useRouter()
const email = ref('')
const code = ref('')
const password = ref('')
const confirmPassword = ref('')
const loading = ref(false)
const sendingCode = ref(false)
const countdown = ref(0)
const error = ref('')
const success = ref('')
let timer: any = null

async function sendCode() {
  if (!email.value) return
  error.value = ''
  sendingCode.value = true
  try {
    await authApi.sendResetCode(email.value)
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
  if (password.value !== confirmPassword.value) {
    error.value = '两次输入的密码不一致'
    return
  }
  
  error.value = ''
  success.value = ''
  loading.value = true
  try {
    await authApi.resetPassword({
      email: email.value,
      password: password.value,
      code: code.value
    })
    success.value = '密码重置成功，正在跳转登录...'
    setTimeout(() => {
      router.push('/login')
    }, 2000)
  } catch (e: any) {
    error.value = e || '重置失败'
  } finally {
    loading.value = false
  }
}

onUnmounted(() => {
  if (timer) clearInterval(timer)
})
</script>
