<template>
  <div class="not-found">
    <el-empty description="Page Not Found">
      <el-button type="primary" @click="goHome">Back to Home</el-button>
    </el-empty>
  </div>
</template>

<script setup>
'use strict';

import { useRouter } from 'vue-router';

const router = useRouter();

const goHome = () => {
  router.push({ name: 'Login' });
};
</script>

<style scoped>
.not-found {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100%;
}
</style>
