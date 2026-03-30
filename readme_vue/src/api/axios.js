import axios from "axios";
import { useAuthStore } from '@/store/auth';
import router from '@/router';

const api = axios.create({
    baseURL: import.meta.env.VITE_API_BASE_URL || 'http://localhost:8202',
    timeout: 10000,
})

// 요청: AccessToken 자동 첨부
api.interceptors.request.use((config) => {
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