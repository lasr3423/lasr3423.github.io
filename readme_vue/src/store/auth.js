import { defineStore } from "pinia";
import { ref, computed } from 'vue';
import { authApi } from "@/api/auth";

export const useAuthStore = defineStore('auth',() => {
  // AccessToken: 메모리에만 저장 (XSS 방어)
  const accessToken  = ref(null);
  // RefreshToken: localStorage (HttpOnly 쿠키가 더 안전하나 구현 편의상)
  const refreshToken = ref(localStorage.getItem('refreshToken'));

  const isLoggedIn = computed(() => !!accessToken.value);

  async function signin(email, password) {
    const { data } = await authApi.signin({ email, password });
    setTokens(data.accessToken, data.refreshToken);
  }

  async function signout() {
    try {
      await authApi.signout()
    } finally {
      clearTokens()
    }
  }

  async function refreshAccessToken() {
    const stored = localStorage.getItem('refreshToken');
    if (!stored) throw new Error('RefreshToken 없음');

    const { data } = await authApi.refresh({ refreshToken: stored });
    setTokens(data.accessToken, data.refreshToken);
    return data.accessToken;
  }

  function setTokens(access, refresh) {
    accessToken.value = access;
    refreshToken.value = refresh;
    localStorage.setItem('refreshToken', refresh);
  }

  function clearTokens() {
    accessToken.value = null;
    refreshToken.value = null;
    localStorage.removeItem('refreshToken');
  }

  async function initialize() {
    const stored = localStorage.getItem('refreshToken');
    if (stored) {
        try { await refreshAccessToken() }
        catch { clearTokens() }
    }
  }
  return {
    accessToken, isLoggedIn,
    signin, signout, refreshAccessToken,
    setTokens, clearTokens, initialize
  };
})