<template>
  <el-container class="dashboard">
    <el-header class="dashboard-header" height="72px">
      <div class="user-info">
        <h3>Inclove Control Panel</h3>
        <p>Logged in as: <strong>{{ displayUserId }}</strong></p>
      </div>
      <div class="header-actions">
        <el-button @click="refreshUserId" :loading="loadingUser">Refresh ID</el-button>
        <el-button type="danger" @click="logout">Logout</el-button>
      </div>
    </el-header>
    <el-main>
      <el-tabs v-model="activeTab">
        <el-tab-pane label="Profile" name="profile">
          <el-form ref="profileFormRef" :model="profileForm" :rules="profileRules" label-position="top" class="section">
            <el-row :gutter="16">
              <el-col :xs="24" :sm="12">
                <el-form-item label="User Name" prop="userName">
                  <el-input v-model="profileForm.userName" placeholder="Enter your nickname" clearable />
                </el-form-item>
              </el-col>
              <el-col :xs="24" :sm="12">
                <el-form-item label="Gender" prop="gender">
                  <el-select v-model="profileForm.gender" placeholder="Select gender" clearable>
                    <el-option v-for="item in genderOptions" :key="item.value" :label="item.label" :value="item.value" />
                  </el-select>
                </el-form-item>
              </el-col>
            </el-row>
            <el-row :gutter="16">
              <el-col :xs="24" :sm="12">
                <el-form-item label="Birthday" prop="birthday">
                  <el-date-picker v-model="profileForm.birthday" type="date" placeholder="Select birthday" style="width: 100%" />
                </el-form-item>
              </el-col>
              <el-col :xs="24" :sm="12">
                <el-form-item label="Intro" prop="intro">
                  <el-input v-model="profileForm.intro" type="textarea" :autosize="{ minRows: 2, maxRows: 4 }" placeholder="Say something" />
                </el-form-item>
              </el-col>
            </el-row>
            <el-button type="primary" :loading="profileLoading" @click="saveProfile">Save Profile</el-button>
          </el-form>
        </el-tab-pane>
        <el-tab-pane label="Interests" name="interests">
          <div class="section">
            <h4>Current Interests</h4>
            <el-space wrap>
              <el-tag v-for="item in interests" :key="item">{{ item }}</el-tag>
              <el-tag v-if="interests.length === 0" type="info">No interests yet</el-tag>
            </el-space>
            <el-divider />
            <h4>Add New Interests</h4>
            <div class="input-group">
              <el-input v-model="newInterest" placeholder="Type interest and press Add" clearable @keyup.enter="addInterest" />
              <el-button type="primary" @click="addInterest">Add</el-button>
            </div>
            <el-space wrap>
              <el-tag
                v-for="(item, index) in pendingInterests"
                :key="item + index"
                closable
                @close="removePendingInterest(index)"
                type="warning"
              >
                {{ item }}
              </el-tag>
            </el-space>
            <div class="actions">
              <el-button type="success" :disabled="pendingInterests.length === 0" :loading="interestLoading" @click="saveInterests">
                Save Interests
              </el-button>
            </div>
          </div>
        </el-tab-pane>
        <el-tab-pane label="Traits" name="traits">
          <div class="section">
            <h4>Current Traits</h4>
            <el-space wrap>
              <el-tag v-for="item in traits" :key="item" type="primary">{{ item }}</el-tag>
              <el-tag v-if="traits.length === 0" type="info">No traits yet</el-tag>
            </el-space>
            <el-divider />
            <h4>Add New Traits</h4>
            <div class="input-group">
              <el-input v-model="newTrait" placeholder="Type trait and press Add" clearable @keyup.enter="addTrait" />
              <el-button type="primary" @click="addTrait">Add</el-button>
            </div>
            <el-space wrap>
              <el-tag
                v-for="(item, index) in pendingTraits"
                :key="item + index"
                closable
                @close="removePendingTrait(index)"
                type="warning"
              >
                {{ item }}
              </el-tag>
            </el-space>
            <div class="actions">
              <el-button type="success" :disabled="pendingTraits.length === 0" :loading="traitLoading" @click="saveTraits">
                Save Traits
              </el-button>
            </div>
          </div>
        </el-tab-pane>
        <el-tab-pane label="Preferences" name="preferences">
          <el-form :model="preferenceForm" label-position="top" class="section">
            <el-row :gutter="16">
              <el-col :xs="24" :sm="12">
                <el-form-item label="Preferred Age Min">
                  <el-input-number v-model="preferenceForm.preferredAgeMin" :min="18" :max="80" controls-position="right" />
                </el-form-item>
              </el-col>
              <el-col :xs="24" :sm="12">
                <el-form-item label="Preferred Age Max">
                  <el-input-number v-model="preferenceForm.preferredAgeMax" :min="18" :max="80" controls-position="right" />
                </el-form-item>
              </el-col>
            </el-row>
            <el-form-item label="Accepted Genders">
              <el-select v-model="preferenceForm.acceptedGenders" multiple placeholder="Select genders">
                <el-option v-for="item in genderOptions" :key="item.value" :label="item.label" :value="item.value" />
              </el-select>
            </el-form-item>
            <el-form-item label="Preferred Traits">
              <el-select v-model="preferenceForm.preferredTraits" multiple filterable allow-create default-first-option>
                <el-option v-for="item in traitOptions" :key="item" :label="item" :value="item" />
              </el-select>
            </el-form-item>
            <el-form-item label="Preferred Hobbies">
              <el-select v-model="preferenceForm.preferredHobbies" multiple filterable allow-create default-first-option>
                <el-option v-for="item in interestOptions" :key="item" :label="item" :value="item" />
              </el-select>
            </el-form-item>
            <el-button type="primary" :loading="preferenceLoading" @click="savePreferenceSettings">Save Preferences</el-button>
          </el-form>
        </el-tab-pane>
        <el-tab-pane label="Matches" name="matches">
          <div class="section">
            <el-button type="primary" :loading="matchLoading" @click="loadMatches">Refresh Matches</el-button>
            <el-table v-if="matches.length" :data="matches" stripe style="margin-top: 16px">
              <el-table-column prop="userName" label="User" min-width="120" />
              <el-table-column prop="gender" label="Gender" min-width="100" />
              <el-table-column prop="birthday" label="Birthday" min-width="140" />
              <el-table-column prop="intro" label="Intro" />
            </el-table>
            <el-empty v-else description="No matches yet" style="margin-top: 16px" />
          </div>
        </el-tab-pane>
        <el-tab-pane label="Chat" name="chat">
          <div class="section">
            <chat-panel />
          </div>
        </el-tab-pane>
      </el-tabs>
    </el-main>
  </el-container>
</template>

<script setup>
'use strict';

import { computed, onMounted, reactive, ref } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';
import { useAuthStore } from '../store/auth';
import { createProfile, fetchCurrentUser, fetchMatches } from '../api/profile';
import { createInterests, getInterests } from '../api/interest';
import { createTraits, getTraits } from '../api/trait';
import { createPreference } from '../api/preference';
import ChatPanel from '../components/chat/ChatPanel.vue';

const router = useRouter();
const authStore = useAuthStore();

const activeTab = ref('profile');
const loadingUser = ref(false);
const profileFormRef = ref();
const profileLoading = ref(false);
const profileForm = reactive({
  userName: '',
  gender: '',
  birthday: '',
  intro: ''
});

const profileRules = {
  userName: [{ required: true, message: 'User name is required', trigger: 'blur' }],
  gender: [{ required: true, message: 'Gender is required', trigger: 'change' }],
  birthday: [{ required: true, message: 'Birthday is required', trigger: 'change' }]
};

const genderOptions = [
  { value: 'MALE', label: 'Male' },
  { value: 'FEMALE', label: 'Female' },
  { value: 'NB', label: 'Non-binary' },
  { value: 'SECRET', label: 'Prefer not to say' }
];

const interests = ref([]);
const pendingInterests = ref([]);
const newInterest = ref('');
const interestLoading = ref(false);

const traits = ref([]);
const pendingTraits = ref([]);
const newTrait = ref('');
const traitLoading = ref(false);

const preferenceForm = reactive({
  preferredAgeMin: null,
  preferredAgeMax: null,
  acceptedGenders: [],
  preferredTraits: [],
  preferredHobbies: []
});
const preferenceLoading = ref(false);

const matches = ref([]);
const matchLoading = ref(false);

const interestOptions = computed(() => interests.value);
const traitOptions = computed(() => traits.value);

const displayUserId = computed(() => authStore.userId || authStore.email || 'Unknown');

const refreshUserId = async () => {
  try {
    loadingUser.value = true;
    const current = await fetchCurrentUser();
    if (current?.userId) {
      authStore.setUserId(current.userId);
    }
  } finally {
    loadingUser.value = false;
  }
};

const formatBirthday = (value) => {
  if (!value) {
    return '';
  }
  if (value instanceof Date) {
    return value.toISOString().slice(0, 10);
  }
  return value;
};

const saveProfile = () => {
  if (!profileFormRef.value) {
    return;
  }
  profileFormRef.value.validate(async (valid) => {
    if (!valid) {
      return;
    }
    profileLoading.value = true;
    try {
      const payload = {
        userName: profileForm.userName,
        gender: profileForm.gender,
        birthday: formatBirthday(profileForm.birthday),
        intro: profileForm.intro
      };
      await createProfile(payload);
      ElMessage.success('Profile saved');
    } finally {
      profileLoading.value = false;
    }
  });
};

const loadInterests = async () => {
  const result = await getInterests();
  interests.value = Array.isArray(result?.interestNames) ? result.interestNames : [];
};

const loadTraits = async () => {
  const result = await getTraits();
  traits.value = Array.isArray(result?.traitNames) ? result.traitNames : [];
};

const addInterest = () => {
  if (!newInterest.value.trim()) {
    return;
  }
  const value = newInterest.value.trim();
  if (!pendingInterests.value.includes(value)) {
    pendingInterests.value.push(value);
  }
  newInterest.value = '';
};

const removePendingInterest = (index) => {
  pendingInterests.value.splice(index, 1);
};

const saveInterests = async () => {
  if (pendingInterests.value.length === 0) {
    return;
  }
  interestLoading.value = true;
  try {
    await createInterests(pendingInterests.value);
    ElMessage.success('Interests updated');
    pendingInterests.value = [];
    await loadInterests();
  } finally {
    interestLoading.value = false;
  }
};

const addTrait = () => {
  if (!newTrait.value.trim()) {
    return;
  }
  const value = newTrait.value.trim();
  if (!pendingTraits.value.includes(value)) {
    pendingTraits.value.push(value);
  }
  newTrait.value = '';
};

const removePendingTrait = (index) => {
  pendingTraits.value.splice(index, 1);
};

const saveTraits = async () => {
  if (pendingTraits.value.length === 0) {
    return;
  }
  traitLoading.value = true;
  try {
    await createTraits(pendingTraits.value);
    ElMessage.success('Traits updated');
    pendingTraits.value = [];
    await loadTraits();
  } finally {
    traitLoading.value = false;
  }
};

const savePreferenceSettings = async () => {
  preferenceLoading.value = true;
  try {
    const payload = {
      preferredAgeMin: preferenceForm.preferredAgeMin ?? null,
      preferredAgeMax: preferenceForm.preferredAgeMax ?? null,
      acceptedGenders: preferenceForm.acceptedGenders,
      preferredTraits: Array.from(new Set(preferenceForm.preferredTraits)),
      preferredHobbies: Array.from(new Set(preferenceForm.preferredHobbies))
    };
    await createPreference(payload);
    ElMessage.success('Preferences saved');
  } finally {
    preferenceLoading.value = false;
  }
};

const loadMatches = async () => {
  matchLoading.value = true;
  try {
    const result = await fetchMatches();
    matches.value = Array.isArray(result) ? result : [];
  } finally {
    matchLoading.value = false;
  }
};

const logout = () => {
  authStore.clearAuth();
  router.replace({ name: 'Login' });
};

onMounted(async () => {
  if (!authStore.userId) {
    await refreshUserId();
  }
  await Promise.all([loadInterests(), loadTraits()]);
});
</script>

<style scoped>
.dashboard {
  min-height: 100%;
}

.dashboard-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 16px;
  flex-wrap: wrap;
}

.section {
  margin-top: 16px;
}

.input-group {
  display: flex;
  gap: 12px;
  max-width: 420px;
  flex-wrap: wrap;
}

.actions {
  margin-top: 16px;
}
</style>
