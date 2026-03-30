<template>
    <div class="oauth-callback">
        <p>로그인 처리 중...</p>
    </div>
</template>

<script setup>
import { onMounted } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { useAuthStore } from '@/store/auth';

const route = useRoute();
const router = useRouter();
const authStore = useAuthStore();

onMounted(() => {
    const { accessToken, refreshToken, error } = route.query;

    if (error) {
        console.error('소셜 로그인 실패 :', error)
        router.push('/signin?error='+encodeURIComponent(error))
        return
    }

  if (accessToken && refreshToken) {
    authStore.setTokens(accessToken, refreshToken)
    // 이전 페이지 또는 메인으로 이동
    const redirect = sessionStorage.getItem('redirect') || '/'
    sessionStorage.removeItem('redirect')
    router.replace(redirect)
  }
})
</script>

<style lang="scss" scoped>

</style>