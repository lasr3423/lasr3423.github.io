<template>
  <div class="signin-page">
    <h2>로그인</h2>

    <!-- 일반 로그인 -->
    <form @submit.prevent="handleSignin">
      <input v-model="email" type="email" placeholder="이메일" required /><br>
      <input v-model="password" type="password" placeholder="비밀번호" required /><br>
      <button type="submit" :disabled="loading">{{ loading ? '처리 중...' : '로그인' }}</button><br>
    </form>

    <p v-if="errorMsg" class="error">{{ errorMsg }}</p><br>

    <div class="divider">또는</div><br>

    <!-- 구글 로그인 -->
    <button @click="handleGoogleLogin" class="btn-social btn-google">
      구글로 로그인
    </button>
    <!-- 카카오 로그인 -->
    <button @click="handleKakaoLogin" class="btn-social btn-kakao">
      카카오로 로그인
    </button>
    <br>
    <p>아직 회원이 아니신가요? <router-link to="/signup">회원가입</router-link></p><br>

  </div>
</template>

<script setup>
import { ref } from 'vue';
import { useRouter, useRoute } from 'vue-router';
import { useAuthStore } from '@/store/auth';
import { authApi } from '@/api/auth';

const router = useRouter()
const route = useRoute()
const authStore = useAuthStore()
const email = ref('')
const password = ref('')
const loading = ref(false)
const errorMsg = ref('')

async function handleSignin() {
  try {
    loading.value = true
    errorMsg.value = ''
    await authStore.signin(email.value, password.value)
    // 로그인 성공 후 원래 가려던 페이지로 이동, 없으면 홈으로
    const redirect = route.query.redirect || '/'
    router.push(redirect)
  } catch (e) {
    errorMsg.value = e.response?.data?.message || '로그인에 실패했습니다.'
  } finally {
    loading.value = false
  }
}

function handleGoogleLogin() {
  // OAuth 콜백 후 돌아올 페이지 저장
  sessionStorage.setItem('redirect', route.query.redirect || '/')
  window.location.href = 'http://localhost:8202/oauth2/authorization/google'
}

async function handleKakaoLogin() {
  try {
    // OAuth 콜백 후 돌아올 페이지 저장
    sessionStorage.setItem('redirect', route.query.redirect || '/')
    const { data } = await authApi.getKakaoAuthUrl()
    window.location.href = data.authUrl
  } catch (e) {
    errorMsg.value = '카카오 로그인 연결에 실패했습니다.'
  }
}
</script>
