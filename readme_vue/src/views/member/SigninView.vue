<template>
  <div class="signin-page">
    <h2>로그인</h2>

    <!-- 일반 로그인 -->
    <form @submit.prevent="handleSignin">
      <input v-model="email"    type="email"    placeholder="이메일" required />
      <input v-model="password" type="password" placeholder="비밀번호" required />
      <button type="submit" :disabled="loading">로그인</button>
    </form>

    <p v-if="errorMsg" class="error">{{ errorMsg }}</p>

    <div class="divider">또는</div>

    <!-- 구글 로그인: Spring Security OAuth2 자동 처리 URL -->
    <a href="http://localhost:8202/oauth2/authorization/google" class="btn-social btn-google">
      구글로 로그인
    </a>

    <!-- 카카오 로그인: 서버에서 URL 받아서 이동 -->
    <button @click="handleKakaoLogin" class="btn-social btn-kakao">
      카카오로 로그인
    </button>
  </div>
</template>

<script setup>
import { ref } from 'vue';
import { useRouter } from 'vue-router';
import { useAuthStore } from '@/store/auth';
import { authApi } from '@/api/auth';

const router    = useRouter()
const authStore = useAuthStore()
const email     = ref('')
const password  = ref('')
const loading   = ref(false)
const errorMsg  = ref('')

async function handleSignin() {
  try {
    loading.value  = true
    errorMsg.value = ''
    await authStore.signin(email.value, password.value)
    router.push('/')
  } catch (e) {
    errorMsg.value = e.response?.data?.message || '로그인에 실패했습니다.'
  } finally {
    loading.value = false
  }
}

async function handleKakaoLogin() {
  try {
    const { data } = await authApi.getKakaoAuthUrl()
    window.location.href = data.authUrl
  } catch (e) {
    errorMsg.value = '카카오 로그인 연결에 실패했습니다.'
  }
}

</script>