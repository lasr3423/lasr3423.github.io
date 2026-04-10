<template>
  <section class="page-section">
    <div class="mx-auto max-w-md">
      <div class="surface-panel rounded-[2rem] p-10 text-center">
        <div class="mx-auto flex h-14 w-14 items-center justify-center rounded-full bg-brand-50 text-brand-700">
          ...
        </div>
        <h1 class="mt-6 text-2xl font-bold text-slate-900">로그인 처리 중입니다</h1>
        <p class="mt-3 text-sm leading-6 text-slate-500">
          인증 정보를 확인한 뒤 적절한 화면으로 이동합니다.
        </p>
      </div>
    </div>
  </section>
</template>

<script setup>
import { onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '@/store/auth'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()

function resolvePostLoginPath() {
  const redirect = sessionStorage.getItem('redirect')
  sessionStorage.removeItem('redirect')

  if (redirect) {
    return redirect
  }

  return authStore.isAdmin ? '/admin' : '/'
}

onMounted(() => {
  const { accessToken, refreshToken, error } = route.query

  if (error) {
    console.error('소셜 로그인 실패:', error)
    router.push(`/signin?error=${encodeURIComponent(error)}`)
    return
  }

  if (accessToken && refreshToken) {
    authStore.setTokens(accessToken, refreshToken)
    router.replace(resolvePostLoginPath())
  }
})
</script>
