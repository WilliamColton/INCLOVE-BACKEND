'use strict';

import axios from 'axios';
import { ElMessage } from 'element-plus';
import router from '../router';
import pinia from '../store';
import { useAuthStore } from '../store/auth';

const http = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080',
  timeout: 15000
});

http.interceptors.request.use(
  (config) => {
    const authStore = useAuthStore(pinia);
    if (authStore.token) {
      config.headers.Authorization = `Bearer ${authStore.token}`;
    }
    return config;
  },
  (error) => Promise.reject(error)
);

http.interceptors.response.use(
  (response) => {
    const payload = response.data;
    if (payload && Object.prototype.hasOwnProperty.call(payload, 'code')) {
      if (payload.code === 200) {
        return payload.data ?? null;
      }
      if (payload.code === 10000) {
        const authStore = useAuthStore(pinia);
        authStore.clearAuth();
        if (router.currentRoute.value.name !== 'Login') {
          router.replace({ name: 'Login' });
        }
      }
      ElMessage.error(payload.message || 'Request failed');
      return Promise.reject(new Error(payload.message || 'Request failed'));
    }
    return payload;
  },
  (error) => {
    const message = error.response?.data?.message || error.message || 'Network error';
    ElMessage.error(message);
    return Promise.reject(error);
  }
);

export default http;
