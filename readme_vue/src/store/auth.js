import { defineStore } from "pinia";
import { ref, computed } from 'vue';
import { authApi } from "@/api/auth";

export const useAuthStore = defineStore('auth', () => {
  // AccessToken: 메모리에만 저장 (XSS 방어)
  const accessToken  = ref(null);
  // RefreshToken: localStorage
  const refreshToken = ref(localStorage.getItem('refreshToken'));
  // 역할: JWT payload의 role 클레임에서 파싱 ("USER" / "MANAGER" / "ADMIN")
  const userRole = ref(null);

  const isLoggedIn = computed(() => !!accessToken.value);
  // MANAGER 또는 ADMIN이면 관리자로 취급
  const isAdmin = computed(() =>
    userRole.value === 'ADMIN' || userRole.value === 'MANAGER'
  );

  // JWT payload를 디코딩해서 role 추출
  // 서명 검증은 서버가 담당 — 여기서는 UI 표시 목적으로만 사용
  function parseRole(token) {
    if (!token) return null;
    try {
      const payload = JSON.parse(atob(token.split('.')[1]));
      return payload.role || null;
    } catch {
      return null;
    }
  }

  async function signin(email, password) {
    const { data } = await authApi.signin({ email, password });
    setTokens(data.accessToken, data.refreshToken);
  }

  async function signout() {
    try {
      await authApi.signout();
    } finally {
      clearTokens();
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
    userRole.value = parseRole(access); // AccessToken에서 role 파싱
    localStorage.setItem('refreshToken', refresh);
  }

  function clearTokens() {
    accessToken.value = null;
    refreshToken.value = null;
    userRole.value = null;
    localStorage.removeItem('refreshToken');
  }

  async function initialize() {
    const stored = localStorage.getItem('refreshToken');
    if (stored) {
      try { await refreshAccessToken(); }
      catch { clearTokens(); }
    }
  }

  return {
    accessToken, isLoggedIn, userRole, isAdmin,
    signin, signout, refreshAccessToken,
    setTokens, clearTokens, initialize,
  };
});
