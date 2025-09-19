<template>
  <el-config-provider>
    <el-scrollbar height="100vh">
      <el-space
        direction="vertical"
        size="large"
        style="width: 100%; max-width: 960px; margin: 0 auto; padding: 24px; box-sizing: border-box;"
        fill
      >
        <el-card shadow="never">
          <template #header>
            <el-text tag="h1" size="large">INCLOVE 双人聊天前端</el-text>
          </template>
          <el-text type="info">
            使用后端的 REST 与 WebSocket 接口完成注册、登录和双人实时聊天。
          </el-text>
        </el-card>

        <el-card shadow="hover">
          <template #header>
            <el-text tag="h2">服务端地址</el-text>
          </template>
          <el-form label-position="top">
            <el-row :gutter="16">
              <el-col :xs="24" :md="12">
                <el-form-item label="REST API 基址">
                  <el-input v-model="restBaseInput" placeholder="http://localhost:8080" clearable />
                </el-form-item>
              </el-col>
              <el-col :xs="24" :md="12">
                <el-form-item label="WebSocket 基址（可选）">
                  <el-input v-model="wsBaseInput" placeholder="默认同 REST 地址" clearable />
                </el-form-item>
              </el-col>
            </el-row>
          </el-form>
          <el-descriptions :column="1" size="small" border>
            <el-descriptions-item label="REST">{{ restBase || '未设置' }}</el-descriptions-item>
            <el-descriptions-item label="WebSocket">{{ wsUrl || '未设置' }}</el-descriptions-item>
          </el-descriptions>
        </el-card>

        <el-row v-if="!jwt" :gutter="16">
          <el-col :xs="24" :md="12">
            <el-card shadow="hover">
              <template #header>
                <el-text tag="h2">注册</el-text>
              </template>
              <el-form label-position="top" @submit.prevent="register">
                <el-form-item label="邮箱">
                  <el-input v-model="registerForm.email" type="email" placeholder="demo@example.com" clearable />
                </el-form-item>
                <el-form-item label="用户 ID">
                  <el-input v-model="registerForm.userId" placeholder="如：10001" clearable />
                </el-form-item>
                <el-form-item label="密码">
                  <el-input
                    v-model="registerForm.password"
                    type="password"
                    show-password
                    placeholder="不少于 6 位"
                  />
                </el-form-item>
                <el-form-item>
                  <el-button type="primary" native-type="submit" style="width: 100%;">注册并登录</el-button>
                </el-form-item>
              </el-form>
            </el-card>
          </el-col>
          <el-col :xs="24" :md="12">
            <el-card shadow="hover">
              <template #header>
                <el-text tag="h2">登录</el-text>
              </template>
              <el-form label-position="top" @submit.prevent="login">
                <el-form-item label="邮箱">
                  <el-input v-model="loginForm.email" type="email" placeholder="demo@example.com" clearable />
                </el-form-item>
                <el-form-item label="用户 ID">
                  <el-input v-model="loginForm.userId" placeholder="与注册一致" clearable />
                </el-form-item>
                <el-form-item label="密码">
                  <el-input v-model="loginForm.password" type="password" show-password />
                </el-form-item>
                <el-form-item>
                  <el-button type="primary" native-type="submit" style="width: 100%;">登录</el-button>
                </el-form-item>
              </el-form>
            </el-card>
          </el-col>
        </el-row>

        <el-card v-else shadow="hover">
          <template #header>
            <el-text tag="h2">账号与连接</el-text>
          </template>
          <el-descriptions :column="1" size="small" border>
            <el-descriptions-item label="连接状态">
              <el-tag :type="connectionTagType" effect="light">{{ connectionStatus }}</el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="当前用户">{{ currentUserId || '未设置' }}</el-descriptions-item>
          </el-descriptions>
          <el-space style="margin-top: 16px;" wrap>
            <el-button type="primary" @click="connectSocket" :loading="connecting">重新连接</el-button>
            <el-button type="danger" plain @click="logout">退出登录</el-button>
          </el-space>
        </el-card>

        <el-card v-if="jwt" shadow="hover">
          <template #header>
            <el-text tag="h2">建立会话</el-text>
          </template>
          <el-form label-position="top">
            <el-row :gutter="16" align="middle">
              <el-col :xs="24" :md="18">
                <el-form-item label="对方用户 ID">
                  <el-input v-model="peerUserIdInput" placeholder="例如：10002" clearable />
                </el-form-item>
              </el-col>
              <el-col :xs="24" :md="6">
                <el-form-item label=" ">
                  <el-button type="success" style="width: 100%;" @click="openConversation">开启/进入会话</el-button>
                </el-form-item>
              </el-col>
            </el-row>
          </el-form>
          <el-descriptions v-if="conversationReady" :column="1" size="small" border>
            <el-descriptions-item label="会话 ID">{{ conversationId }}</el-descriptions-item>
            <el-descriptions-item label="对方用户">{{ activePeerId }}</el-descriptions-item>
          </el-descriptions>
          <el-alert
            v-if="peerPresence"
            :title="peerPresence"
            type="success"
            :closable="false"
            show-icon
            style="margin-top: 12px;"
          />
        </el-card>

        <el-card v-if="jwt && conversationReady" shadow="hover">
          <template #header>
            <el-text tag="h2">聊天窗口</el-text>
          </template>
          <el-scrollbar ref="messageScrollbar" height="320px">
            <el-empty v-if="messages.length === 0" description="暂无消息，开始聊天吧" />
            <el-timeline v-else>
              <el-timeline-item
                v-for="msg in messages"
                :key="msg.localKey"
                :timestamp="formatTime(msg.timestamp)"
                :type="msg.fromSelf ? 'primary' : 'info'"
              >
                <el-space direction="vertical" size="small" style="width: 100%;">
                  <el-text>
                    <strong>{{ msg.fromSelf ? '我' : msg.senderId }}</strong>
                    <span v-if="msg.sid"> · #{{ msg.sid }}</span>
                  </el-text>
                  <el-text>{{ msg.content }}</el-text>
                  <el-tag v-if="msg.fromSelf" :type="msg.acked ? 'success' : 'warning'" size="small">
                    {{ msg.acked ? '已确认' : '等待确认' }}
                  </el-tag>
                </el-space>
              </el-timeline-item>
            </el-timeline>
          </el-scrollbar>
          <el-form label-position="top" @submit.prevent="sendMessage" style="margin-top: 16px;">
            <el-form-item label="输入消息">
              <el-input
                v-model="draft"
                type="textarea"
                :rows="3"
                placeholder="输入消息，回车发送，Shift+Enter 换行"
                @keydown="handleDraftKeydown"
              />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" native-type="submit" :disabled="!stompConnected">发送</el-button>
            </el-form-item>
          </el-form>
        </el-card>

        <el-card shadow="hover">
          <template #header>
            <el-text tag="h2">运行日志</el-text>
          </template>
          <el-table :data="logs" border size="small" style="width: 100%;" empty-text="暂无日志">
            <el-table-column prop="timestamp" label="时间" width="180" />
            <el-table-column label="级别" width="120">
              <template #default="{ row }">
                <el-tag :type="row.tagType" size="small">{{ row.label }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="message" label="事件" />
          </el-table>
        </el-card>
      </el-space>
    </el-scrollbar>
  </el-config-provider>
</template>

<script setup>
'use strict';

import { ref, reactive, computed, watch, onMounted, onBeforeUnmount, nextTick } from 'vue'
import axios from 'axios'
import SockJS from 'sockjs-client'
import { Client } from '@stomp/stompjs'
import { ElMessage } from 'element-plus'

const storedRest = localStorage.getItem('inclove-rest-base') || 'http://localhost:8080'
const storedWs = localStorage.getItem('inclove-ws-base') || ''
const storedToken = localStorage.getItem('inclove-token') || ''
const storedUser = localStorage.getItem('inclove-userId') || ''

const restBaseInput = ref(storedRest)
const wsBaseInput = ref(storedWs)
const registerForm = reactive({ email: '', userId: '', password: '' })
const loginForm = reactive({ email: '', userId: '', password: '' })
const jwt = ref(storedToken)
const currentUserId = ref(storedUser)
const connecting = ref(false)
const connectionStatus = ref(jwt.value ? '未连接' : '未登录')
const stompClient = ref(null)
const subscription = ref(null)
const heartbeatTimer = ref(null)
const peerUserIdInput = ref('')
const activePeerId = ref('')
const conversationId = ref('')
const messages = ref([])
const logs = ref([])
const draft = ref('')
const peerPresence = ref('')
const isTyping = ref(false)
const messageScrollbar = ref(null)
let localMessageSeed = 0

const restBase = computed(() => normalizeBase(restBaseInput.value))
const wsBase = computed(() => {
  const custom = normalizeBase(wsBaseInput.value)
  return custom || restBase.value
})
const wsUrl = computed(() => {
  const base = wsBase.value
  return base ? `${base}/ws` : ''
})
const stompConnected = computed(() => Boolean(stompClient.value && stompClient.value.connected))
const conversationReady = computed(() => Boolean(conversationId.value && activePeerId.value))
const connectionTagType = computed(() => {
  if (connectionStatus.value === '已连接') {
    return 'success'
  }
  if (connectionStatus.value.includes('异常')) {
    return 'danger'
  }
  if (connectionStatus.value.includes('连接中')) {
    return 'warning'
  }
  return 'info'
})

watch(restBaseInput, (value) => {
  localStorage.setItem('inclove-rest-base', value || '')
})

watch(wsBaseInput, (value) => {
  localStorage.setItem('inclove-ws-base', value || '')
})

watch(draft, (value) => {
  isTyping.value = value.trim().length > 0
  if (stompConnected.value && conversationReady.value) {
    sendHeartbeat(true)
  }
})

function normalizeBase(value) {
  if (!value) {
    return ''
  }
  let trimmed = value.trim()
  if (!trimmed) {
    return ''
  }
  if (!/^https?:\/\//i.test(trimmed)) {
    trimmed = `http://${trimmed}`
  }
  return trimmed.replace(/\/+$/, '')
}

function pushLog(message, severity = 'info') {
  const timestamp = new Date().toLocaleTimeString()
  const tagType = severity === 'error' ? 'danger' : severity
  const labelMap = {
    info: '信息',
    success: '成功',
    warning: '警告',
    error: '错误',
  }
  logs.value.unshift({
    id: `${Date.now()}-${Math.random().toString(16).slice(2)}`,
    message,
    severity,
    tagType,
    label: labelMap[severity] || '信息',
    timestamp,
  })
  if (logs.value.length > 160) {
    logs.value.pop()
  }
  const elMessageType = severity === 'error' ? 'error' : severity === 'warning' ? 'warning' : severity === 'success' ? 'success' : 'info'
  ElMessage({ type: elMessageType, message, grouping: true, showClose: false })
}

function ensureApiResponse(response, context = '请求') {
  const body = response?.data
  if (!body || typeof body !== 'object') {
    throw new Error(`${context}失败：响应格式无效`)
  }
  if (Object.prototype.hasOwnProperty.call(body, 'code')) {
    const numericCode = Number(body.code)
    if (!Number.isNaN(numericCode) && numericCode !== 200) {
      throw new Error(body.message || `${context}失败 (code ${body.code})`)
    }
  }
  return body
}

function authHeaders() {
  return jwt.value ? { Authorization: `Bearer ${jwt.value}` } : {}
}

async function register() {
  if (!restBase.value) {
    pushLog('请先设置有效的 REST API 地址。', 'warning')
    return
  }
  const payload = {
    email: registerForm.email.trim(),
    userId: registerForm.userId.trim(),
    rawPassword: registerForm.password,
  }
  if (!payload.email || !payload.userId || !payload.rawPassword) {
    pushLog('请完整填写注册信息。', 'warning')
    return
  }
  try {
    const res = await axios.post(`${restBase.value}/auth/register`, payload, {
      headers: { 'Content-Type': 'application/json' },
    })
    const body = ensureApiResponse(res, '注册')
    const data = body?.data
    if (data?.jwtToken) {
      afterLogin(payload.userId, data.jwtToken)
    }
    pushLog(body?.message ? `注册成功：${body.message}` : '注册成功。', 'success')
  } catch (error) {
    pushLog(`注册失败：${error?.message || error}`, 'error')
  }
}

async function login() {
  if (!restBase.value) {
    pushLog('请先设置有效的 REST API 地址。', 'warning')
    return
  }
  const payload = {
    email: loginForm.email.trim(),
    userId: loginForm.userId.trim(),
    rawPassword: loginForm.password,
  }
  if (!payload.email || !payload.userId || !payload.rawPassword) {
    pushLog('请完整填写登录信息。', 'warning')
    return
  }
  try {
    const res = await axios.post(`${restBase.value}/auth/login`, payload, {
      headers: { 'Content-Type': 'application/json' },
    })
    const body = ensureApiResponse(res, '登录')
    const data = body?.data
    afterLogin(payload.userId, data?.jwtToken)
    pushLog(body?.message ? `登录成功：${body.message}` : '登录成功。', 'success')
  } catch (error) {
    pushLog(`登录失败：${error?.message || error}`, 'error')
  }
}

function afterLogin(userId, token) {
  if (token) {
    jwt.value = token
    localStorage.setItem('inclove-token', token)
  }
  if (userId) {
    currentUserId.value = userId
    localStorage.setItem('inclove-userId', userId)
  }
  if (jwt.value) {
    connectSocket()
  }
}

async function logout() {
  await disconnectSocket()
  jwt.value = ''
  currentUserId.value = ''
  activePeerId.value = ''
  conversationId.value = ''
  messages.value = []
  peerPresence.value = ''
  localStorage.removeItem('inclove-token')
  localStorage.removeItem('inclove-userId')
  connectionStatus.value = '未登录'
  pushLog('已退出登录。', 'info')
}

async function connectSocket() {
  if (!jwt.value) {
    pushLog('尚未登录，无法建立 WebSocket。', 'warning')
    return
  }
  if (!wsUrl.value) {
    pushLog('WebSocket 端点无效，请检查地址。', 'warning')
    return
  }
  await disconnectSocket()
  connecting.value = true
  connectionStatus.value = '连接中…'

  const client = new Client({
    webSocketFactory: () => new SockJS(wsUrl.value),
    connectHeaders: { Authorization: `Bearer ${jwt.value}` },
    reconnectDelay: 5000,
    debug: () => {},
  })

  client.onConnect = () => {
    connecting.value = false
    connectionStatus.value = '已连接'
    pushLog('STOMP 已连接。', 'success')
    subscription.value = client.subscribe('/user/queue/conversations', handleIncoming)
    if (conversationReady.value) {
      startHeartbeat()
    }
  }

  client.onStompError = (frame) => {
    connecting.value = false
    connectionStatus.value = '连接异常'
    pushLog(`STOMP 错误：${frame.headers['message'] || '未知错误'}`, 'error')
  }

  client.onDisconnect = () => {
    connecting.value = false
    connectionStatus.value = '未连接'
    stopHeartbeat()
  }

  client.onWebSocketClose = () => {
    connecting.value = false
    connectionStatus.value = '未连接'
    stopHeartbeat()
  }

  client.onWebSocketError = () => {
    pushLog('WebSocket 发生错误。', 'warning')
  }

  client.activate()
  stompClient.value = client
}

async function disconnectSocket() {
  stopHeartbeat()
  if (subscription.value) {
    try {
      subscription.value.unsubscribe()
    } catch (error) {
      pushLog(`取消订阅失败：${error?.message || error}`, 'warning')
    }
    subscription.value = null
  }
  if (stompClient.value) {
    try {
      await stompClient.value.deactivate()
    } catch (error) {
      pushLog(`断开连接失败：${error?.message || error}`, 'warning')
    }
    stompClient.value = null
  }
  connecting.value = false
}

function startHeartbeat() {
  if (!conversationReady.value || !stompConnected.value) {
    return
  }
  stopHeartbeat()
  sendHeartbeat(true)
  heartbeatTimer.value = window.setInterval(() => sendHeartbeat(), 15000)
}

function stopHeartbeat() {
  if (heartbeatTimer.value) {
    clearInterval(heartbeatTimer.value)
    heartbeatTimer.value = null
  }
}

function sendHeartbeat(force = false) {
  if (!stompConnected.value) {
    return
  }
  if (!force && !conversationReady.value) {
    return
  }
  const body = {
    packageType: 'USER_STATUS',
    isOnline: true,
    isTyping: isTyping.value,
    lastActiveTime: new Date().toISOString(),
    peerId: activePeerId.value || currentUserId.value,
  }
  try {
    stompClient.value.publish({
      destination: '/app/user.heartbeat',
      body: JSON.stringify(body),
    })
  } catch (error) {
    pushLog(`发送心跳失败：${error?.message || error}`, 'warning')
  }
}

async function openConversation() {
  if (!restBase.value) {
    pushLog('请先设置 REST API 地址。', 'warning')
    return
  }
  if (!jwt.value) {
    pushLog('请先登录。', 'warning')
    return
  }
  const peerId = peerUserIdInput.value.trim()
  if (!peerId) {
    pushLog('请输入对方的用户 ID。', 'warning')
    return
  }
  try {
    const headers = authHeaders()
    await axios.post(`${restBase.value}/conversation`, null, {
      params: { peerUserId: peerId },
      headers,
    })
    const res = await axios.get(`${restBase.value}/conversation/userIdAndPeerUserId`, {
      params: { peerUserId: peerId },
      headers,
    })
    const body = ensureApiResponse(res, '获取会话')
    const data = body?.data || body
    const id = data?.id || data?.conversationId
    if (!id) {
      throw new Error('响应中未包含会话 ID')
    }
    const switching = activePeerId.value && activePeerId.value !== peerId
    conversationId.value = String(id)
    activePeerId.value = peerId
    if (switching) {
      messages.value = []
    }
    peerPresence.value = ''
    pushLog(`会话已准备就绪（ID: ${conversationId.value}）。`, 'success')
    if (stompConnected.value) {
      startHeartbeat()
    } else {
      connectSocket()
    }
  } catch (error) {
    pushLog(`开启会话失败：${error?.message || error}`, 'error')
  }
}

function addLocalMessage(message) {
  const localKey = `local-${Date.now()}-${localMessageSeed}`
  localMessageSeed += 1
  messages.value.push({
    localKey,
    sid: message.sid || null,
    senderId: message.senderId,
    recipientId: message.recipientId,
    content: message.content,
    conversationId: message.conversationId,
    timestamp: message.timestamp || new Date(),
    fromSelf: Boolean(message.fromSelf),
    acked: Boolean(message.acked),
    awaitingServerSid: Boolean(message.awaitingServerSid),
  })
  scrollToBottom()
}

function sendMessage() {
  if (!conversationReady.value) {
    pushLog('请先建立会话。', 'warning')
    return
  }
  if (!stompConnected.value) {
    pushLog('WebSocket 尚未连接。', 'warning')
    return
  }
  const text = draft.value.trim()
  if (!text) {
    return
  }
  const payload = {
    packageType: 'MESSAGE',
    recipientId: activePeerId.value,
    conversationId: conversationId.value,
    content: text,
    createdAt: new Date().toISOString(),
  }
  try {
    stompClient.value.publish({
      destination: '/app/chat.send',
      body: JSON.stringify(payload),
    })
  } catch (error) {
    pushLog(`发送失败：${error?.message || error}`, 'error')
    return
  }
  addLocalMessage({
    senderId: currentUserId.value,
    recipientId: activePeerId.value,
    content: text,
    conversationId: conversationId.value,
    fromSelf: true,
    acked: false,
    awaitingServerSid: true,
  })
  draft.value = ''
  isTyping.value = false
  startHeartbeat()
}

function handleDraftKeydown(event) {
  if (event.key === 'Enter' && !event.shiftKey) {
    event.preventDefault()
    sendMessage()
  }
}

function handleIncoming(frame) {
  try {
    const payload = JSON.parse(frame.body || '{}')
    const type = payload.packageType || (payload.content !== undefined ? 'MESSAGE' : 'ACK')
    if (type === 'MESSAGE') {
      handleIncomingMessage(payload)
    } else if (type === 'ACK') {
      markAck(payload.sid)
    } else if (type === 'USER_STATUS') {
      updatePeerStatus(payload)
    } else {
      pushLog(`收到未知类型消息：${JSON.stringify(payload)}`, 'warning')
    }
  } catch (error) {
    pushLog(`解析消息失败：${error?.message || error}`, 'error')
  }
}

function handleIncomingMessage(payload) {
  const senderId = payload.senderId ?? payload.senderID
  const recipientId = payload.recipientID ?? payload.recipientId
  const fromSelf = senderId && String(senderId) === String(currentUserId.value)
  const message = {
    localKey: `srv-${payload.sid || Date.now()}`,
    sid: payload.sid,
    senderId,
    recipientId,
    content: payload.content,
    conversationId: payload.conversationId || conversationId.value,
    timestamp: payload.createdAt ? new Date(payload.createdAt) : new Date(),
    fromSelf,
    acked: fromSelf ? false : true,
    awaitingServerSid: false,
  }
  messages.value.push(message)
  if (fromSelf) {
    attachServerSid(payload.sid)
  }
  if (recipientId && String(recipientId) === String(currentUserId.value)) {
    sendAck(payload)
  }
  scrollToBottom()
}

function attachServerSid(sid) {
  if (!sid) {
    return
  }
  const pending = messages.value.find((msg) => msg.fromSelf && (msg.awaitingServerSid || !msg.sid))
  if (pending) {
    pending.sid = sid
    pending.awaitingServerSid = false
  }
}

function sendAck(payload) {
  if (!stompConnected.value || !payload.sid) {
    return
  }
  const ack = {
    packageType: 'ACK',
    sid: payload.sid,
    conversationId: payload.conversationId || conversationId.value,
    senderID: payload.senderId ?? payload.senderID,
  }
  try {
    stompClient.value.publish({
      destination: '/app/chat.ack',
      body: JSON.stringify(ack),
    })
  } catch (error) {
    pushLog(`发送 ACK 失败：${error?.message || error}`, 'warning')
  }
}

function markAck(sid) {
  if (!sid) {
    return
  }
  const target = messages.value.find((msg) => msg.sid === sid && msg.fromSelf)
  if (target) {
    target.acked = true
    target.awaitingServerSid = false
    return
  }
  const pending = messages.value.find((msg) => msg.fromSelf && !msg.sid)
  if (pending) {
    pending.sid = sid
    pending.acked = true
    pending.awaitingServerSid = false
  }
}

function updatePeerStatus(payload) {
  if (payload.peerId && String(payload.peerId) !== String(currentUserId.value)) {
    return
  }
  if (payload.isTyping) {
    peerPresence.value = '对方正在输入…'
  } else if (payload.isOnline === false) {
    peerPresence.value = '对方离线'
  } else {
    peerPresence.value = '对方在线'
  }
}

function scrollToBottom() {
  nextTick(() => {
    if (messageScrollbar.value && typeof messageScrollbar.value.setScrollTop === 'function') {
      messageScrollbar.value.setScrollTop(Number.MAX_SAFE_INTEGER)
    }
  })
}

function formatTime(value) {
  if (!value) {
    return ''
  }
  const date = value instanceof Date ? value : new Date(value)
  return new Intl.DateTimeFormat('zh-CN', {
    hour: '2-digit',
    minute: '2-digit',
    second: '2-digit',
  }).format(date)
}

onMounted(() => {
  if (jwt.value && currentUserId.value) {
    pushLog('检测到本地凭证，尝试重连 WebSocket。', 'info')
    connectSocket()
  }
})

onBeforeUnmount(async () => {
  await disconnectSocket()
})
</script>


