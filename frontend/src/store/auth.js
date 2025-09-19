'use strict';

import { defineStore } from 'pinia';

const TOKEN_KEY = 'inclove_token';
const USER_ID_KEY = 'inclove_user_id';

export const useAuthStore = defineStore('auth', {
  state: () => ({
    token: window.localStorage.getItem(TOKEN_KEY) || '',
    userId: window.localStorage.getItem(USER_ID_KEY) || '',
    email: ''
  }),
  getters: {
    isAuthenticated: (state) => Boolean(state.token)
  },
  actions: {
    setToken(token) {
      this.token = token;
      if (token) {
        window.localStorage.setItem(TOKEN_KEY, token);
      } else {
        window.localStorage.removeItem(TOKEN_KEY);
      }
    },
    setUserId(userId) {
      this.userId = userId;
      if (userId) {
        window.localStorage.setItem(USER_ID_KEY, userId);
      } else {
        window.localStorage.removeItem(USER_ID_KEY);
      }
    },
    setEmail(email) {
      this.email = email;
    },
    clearAuth() {
      this.setToken('');
      this.setUserId('');
      this.setEmail('');
    }
  }
});
