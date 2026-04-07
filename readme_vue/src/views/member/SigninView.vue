<template>
  <section class="w-full max-w-md rounded-[2rem] border border-slate-200 bg-white p-8 shadow-xl shadow-slate-200/60">
    <div class="mb-8 space-y-2 text-center">
      <p class="text-sm font-semibold uppercase tracking-[0.24em] text-brand-700">Welcome back</p>
      <h1 class="text-3xl font-bold tracking-tight text-slate-900">Sign in to ReadMe</h1>
      <p class="text-sm text-slate-500">Use your email account or continue with social login.</p>
    </div>

    <form class="space-y-4" @submit.prevent="handleSignin">
      <label class="block space-y-2">
        <span class="text-sm font-medium text-slate-700">Email</span>
        <input
          v-model="email"
          class="w-full rounded-2xl border border-slate-200 bg-slate-50 px-4 py-3 text-sm outline-none transition focus:border-brand-400 focus:bg-white focus:ring-4 focus:ring-brand-100"
          type="email"
          placeholder="you@example.com"
          required
        >
      </label>

      <label class="block space-y-2">
        <span class="text-sm font-medium text-slate-700">Password</span>
        <input
          v-model="password"
          class="w-full rounded-2xl border border-slate-200 bg-slate-50 px-4 py-3 text-sm outline-none transition focus:border-brand-400 focus:bg-white focus:ring-4 focus:ring-brand-100"
          type="password"
          placeholder="Enter your password"
          required
        >
      </label>

      <p v-if="errorMsg" class="rounded-2xl border border-rose-200 bg-rose-50 px-4 py-3 text-sm text-rose-700">
        {{ errorMsg }}
      </p>

      <button
        class="w-full rounded-2xl bg-brand-800 px-4 py-3 text-sm font-semibold text-white transition hover:bg-brand-700 disabled:cursor-not-allowed disabled:bg-slate-400"
        type="submit"
        :disabled="loading"
      >
        {{ loading ? 'Signing in...' : 'Sign in' }}
      </button>
    </form>

    <div class="my-6 flex items-center gap-4 text-xs font-medium uppercase tracking-[0.2em] text-slate-400">
      <div class="h-px flex-1 bg-slate-200"></div>
      <span>or</span>
      <div class="h-px flex-1 bg-slate-200"></div>
    </div>

    <div class="grid gap-3">
      <button
        class="rounded-2xl border border-slate-200 bg-white px-4 py-3 text-sm font-semibold text-slate-700 transition hover:border-brand-200 hover:bg-brand-50"
        @click="handleGoogleLogin"
      >
        Continue with Google
      </button>
      <button
        class="rounded-2xl bg-[#FEE500] px-4 py-3 text-sm font-semibold text-slate-900 transition hover:brightness-95"
        @click="handleKakaoLogin"
      >
        Continue with Kakao
      </button>
    </div>

    <p class="mt-6 text-center text-sm text-slate-500">
      New to ReadMe?
      <router-link class="font-semibold text-brand-700 hover:text-accent-600" to="/signup">
        Create an account
      </router-link>
    </p>
  </section>
</template>

<script setup>
import { ref } from 'vue';
import { useRouter, useRoute } from 'vue-router';
import { useAuthStore } from '@/store/auth';
import { authApi } from '@/api/auth';

const router = useRouter();
const route = useRoute();
const authStore = useAuthStore();
const email = ref('');
const password = ref('');
const loading = ref(false);
const errorMsg = ref('');

async function handleSignin() {
  try {
    loading.value = true;
    errorMsg.value = '';
    await authStore.signin(email.value, password.value);
    const redirect = route.query.redirect || '/';
    router.push(redirect);
  } catch (e) {
    errorMsg.value = e.response?.data?.message || 'Sign in failed.';
  } finally {
    loading.value = false;
  }
}

function handleGoogleLogin() {
  sessionStorage.setItem('redirect', route.query.redirect || '/');
  window.location.href = 'http://localhost:8202/oauth2/authorization/google';
}

async function handleKakaoLogin() {
  try {
    sessionStorage.setItem('redirect', route.query.redirect || '/');
    const { data } = await authApi.getKakaoAuthUrl();
    window.location.href = data.authUrl;
  } catch (e) {
    errorMsg.value = 'Kakao sign in is currently unavailable.';
  }
}
</script>
