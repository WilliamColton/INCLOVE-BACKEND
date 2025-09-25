<template>
  <div class="auth-page">
    <el-card class="auth-card" shadow="hover">
      <h2 class="title">Welcome Back</h2>
      <el-form ref="formRef" :model="form" :rules="rules" label-position="top">
        <el-form-item label="Email" prop="email">
          <el-input v-model="form.email" placeholder="Enter email" clearable />
        </el-form-item>
        <el-form-item label="User ID (optional)" prop="userId">
          <el-input v-model="form.userId" placeholder="Enter user ID" clearable />
        </el-form-item>
        <el-form-item label="Password" prop="rawPassword">
          <el-input v-model="form.rawPassword" type="password" show-password placeholder="Enter password" />
        </el-form-item>
        <div class="actions">
          <el-button type="primary" :loading="loading" @click="onSubmit">Login</el-button>
          <el-button type="text" @click="goRegister">Create account</el-button>
        </div>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
'use strict';

import { reactive, ref } from 'vue';
import { useRouter, useRoute } from 'vue-router';
import { ElMessage } from 'element-plus';
import { login } from '../api/auth';
import { fetchCurrentUser } from '../api/profile';
import { useAuthStore } from '../store/auth';

const formRef = ref();
const loading = ref(false);
const form = reactive({
  email: '',
  userId: '',
  rawPassword: ''
});

const rules = {
  email: [
    { required: true, message: 'Email is required', trigger: 'blur' },
    { type: 'email', message: 'Please enter a valid email', trigger: ['blur', 'change'] }
  ],
  rawPassword: [
    { required: true, message: 'Password is required', trigger: 'blur' }
  ]
};

const router = useRouter();
const route = useRoute();
const authStore = useAuthStore();

const onSubmit = () => {
  if (!formRef.value) {
    return;
  }
  formRef.value.validate(async (valid) => {
    if (!valid) {
      return;
    }
    loading.value = true;
    try {
      const data = await login(form);
      if (data?.jwtToken) {
        authStore.setToken(data.jwtToken);
        authStore.setEmail(form.email);
        const current = await fetchCurrentUser();
        if (current?.userId) {
          authStore.setUserId(current.userId);
        }
        ElMessage.success('Login successful');
        const redirect = route.query.redirect;
        if (typeof redirect === 'string' && redirect) {
          router.replace(redirect);
        } else {
          router.replace({ name: 'Dashboard' });
        }
      }
    } catch (error) {
      authStore.clearAuth();
    } finally {
      loading.value = false;
    }
  });
};

const goRegister = () => {
  router.push({ name: 'Register' });
};
</script>

<style scoped>
.auth-page {
  min-height: 100%;
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 24px;
  background-color: var(--el-bg-color-page);
}

.auth-card {
  width: 420px;
}

.title {
  text-align: center;
  margin-bottom: 24px;
}

.actions {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>
