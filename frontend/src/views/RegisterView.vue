<template>
  <div class="auth-page">
    <el-card class="auth-card" shadow="hover">
      <h2 class="title">Create Your Account</h2>
      <el-steps :active="activeStep" finish-status="success" align-center>
        <el-step title="Register" />
        <el-step title="Verify" />
      </el-steps>
      <div v-if="activeStep === 0" class="step-content">
        <el-form ref="registerFormRef" :model="registerForm" :rules="registerRules" label-position="top">
          <el-form-item label="Email" prop="email">
            <el-input v-model="registerForm.email" placeholder="Enter email" clearable />
          </el-form-item>
          <el-form-item label="User ID" prop="userId">
            <el-input v-model="registerForm.userId" placeholder="Choose a user ID" clearable />
          </el-form-item>
          <el-form-item label="Password" prop="rawPassword">
            <el-input v-model="registerForm.rawPassword" type="password" show-password placeholder="Enter password" />
          </el-form-item>
          <el-form-item label="Confirm Password" prop="confirmPassword">
            <el-input v-model="registerForm.confirmPassword" type="password" show-password placeholder="Confirm password" />
          </el-form-item>
          <div class="actions">
            <el-button type="primary" :loading="registerLoading" @click="submitRegister">Register</el-button>
            <el-button type="text" @click="goLogin">Back to login</el-button>
          </div>
        </el-form>
      </div>
      <div v-else class="step-content">
        <p class="info">A verification code has been sent to {{ registerForm.email }}.</p>
        <el-form ref="verifyFormRef" :model="verifyForm" :rules="verifyRules" label-position="top">
          <el-form-item label="Verification Code" prop="verificationCode">
            <el-input v-model="verifyForm.verificationCode" placeholder="Enter verification code" clearable />
          </el-form-item>
          <div class="actions">
            <el-button :loading="resendLoading" @click="resendCode">Resend Email</el-button>
            <el-button type="primary" :loading="verifyLoading" @click="submitVerification">Verify</el-button>
          </div>
        </el-form>
      </div>
    </el-card>
  </div>
</template>

<script setup>
'use strict';

import { computed, reactive, ref } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';
import { register, resendRegisterEmail, verifyRegisterCode } from '../api/auth';
import { fetchCurrentUser } from '../api/profile';
import { useAuthStore } from '../store/auth';

const activeStep = ref(0);
const registerFormRef = ref();
const verifyFormRef = ref();
const registerLoading = ref(false);
const verifyLoading = ref(false);
const resendLoading = ref(false);

const registerForm = reactive({
  email: '',
  userId: '',
  rawPassword: '',
  confirmPassword: ''
});

const registerRules = {
  email: [
    { required: true, message: 'Email is required', trigger: 'blur' },
    { type: 'email', message: 'Please enter a valid email', trigger: ['blur', 'change'] }
  ],
  userId: [
    { required: true, message: 'User ID is required', trigger: 'blur' }
  ],
  rawPassword: [
    { required: true, message: 'Password is required', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: 'Confirm your password', trigger: 'blur' },
    {
      validator: (_rule, value, callback) => {
        if (value !== registerForm.rawPassword) {
          callback(new Error('Passwords do not match'));
        } else {
          callback();
        }
      },
      trigger: ['blur', 'change']
    }
  ]
};

const verifyForm = reactive({
  verificationCode: ''
});

const verifyRules = {
  verificationCode: [
    { required: true, message: 'Verification code is required', trigger: 'blur' }
  ]
};

const router = useRouter();
const authStore = useAuthStore();

const canResend = computed(() => Boolean(registerForm.email));

const submitRegister = () => {
  if (!registerFormRef.value) {
    return;
  }
  registerFormRef.value.validate(async (valid) => {
    if (!valid) {
      return;
    }
    registerLoading.value = true;
    try {
      const payload = {
        email: registerForm.email,
        userId: registerForm.userId,
        rawPassword: registerForm.rawPassword
      };
      const response = await register(payload);
      if (response?.jwtToken) {
        authStore.setToken(response.jwtToken);
        authStore.setEmail(registerForm.email);
        const current = await fetchCurrentUser();
        if (current?.userId) {
          authStore.setUserId(current.userId);
        }
        ElMessage.success('Registration successful, please verify your email');
        activeStep.value = 1;
      }
    } finally {
      registerLoading.value = false;
    }
  });
};

const resendCode = async () => {
  if (!canResend.value) {
    ElMessage.warning('Please complete the registration form first');
    return;
  }
  try {
    resendLoading.value = true;
    await resendRegisterEmail();
    ElMessage.success('Verification email sent again');
  } finally {
    resendLoading.value = false;
  }
};

const submitVerification = () => {
  if (!verifyFormRef.value) {
    return;
  }
  verifyFormRef.value.validate(async (valid) => {
    if (!valid) {
      return;
    }
    verifyLoading.value = true;
    try {
      await verifyRegisterCode({ verificationCode: verifyForm.verificationCode });
      ElMessage.success('Verification successful');
      router.replace({ name: 'Dashboard' });
    } finally {
      verifyLoading.value = false;
    }
  });
};

const goLogin = () => {
  router.push({ name: 'Login' });
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
  width: 480px;
}

.title {
  text-align: center;
  margin-bottom: 24px;
}

.step-content {
  margin-top: 24px;
}

.info {
  margin-bottom: 16px;
}

.actions {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>
