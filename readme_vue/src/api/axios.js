import axios from "axios";
import { useAuthStore } from '@/store/auth';
import router from '@/router';

const configuredBaseURL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8202';

const api = axios.create({
    baseURL: configuredBaseURL,
    timeout: 10000,
})

// 요청: AccessToken 자동 첨부
api.interceptors.request.use((config) => {
    // Docker/Nginx 환경에서는 baseURL이 "/api" 이고,
    // 각 API 함수도 "/api/..." 경로를 넘겨 이중 "/api/api/..."가 생길 수 있다.
    // 이 경우 요청 URL 앞의 "/api" 하나만 제거해서 실제 프록시 경로를 맞춘다.
    if (configuredBaseURL.endsWith('/api') && config.url?.startsWith('/api/')) {
        config.url = config.url.replace(/^\/api/, '');
    }

    const { accessToken } = useAuthStore();
    if (accessToken) config.headers.Authorization = `Bearer ${accessToken}`;
    return config;
})

// 응답: 401 → 자동 토큰 갱신 후 원래 요청 재시도
let isRefreshing = false;
let failedQueue = [];

function processQueue(error, token = null) {
    failedQueue.forEach(p => error ? p.reject(error) : p.resolve(token));
    failedQueue = [];
}

api.interceptors.response.use(
    (res) => res,
    async (error) => {
        const original = error.config;

        if (error.response?.status === 401 && !original._retry) {
            if (isRefreshing) {
                return new Promise((resolve, reject) => failedQueue.push({ resolve, reject }))
                    .then(token => { original.headers.Authorization = `Bearer ${token}`; return api(original) });
            }
            original._retry = true;
            isRefreshing = true;

            try {
                const authStore = useAuthStore();
                const newToken = await authStore.refreshAccessToken();
                processQueue(null, newToken);
                original.headers.Authorization = `Bearer ${newToken}`;
                return api(original);
            } catch (refreshError) {
                processQueue(refreshError, null);
                useAuthStore().clearTokens();
                router.push('/signin');
                return Promise.reject(refreshError);
            } finally {
                isRefreshing = false;
            }
        }

        return Promise.reject(error);
    }
)

export default api;
