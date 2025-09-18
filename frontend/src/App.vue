<template>
  <div class="app-shell">
    <header class="hero">
      <h1>INCLOVE 双人聊天前端</h1>
      <p>使用后端提供的 REST 与 WebSocket 接口完成注册、登录及双人实时聊天。</p>
    </header>

    <section class="panel">
      <h2>服务端地址</h2>
      <div class="field-grid">
        <label>
          <span>REST API 基址</span>
          <input v-model="restBaseInput" type="text" placeholder="http://localhost:8080" />
        </label>
        <label>
          <span>WebSocket 基址（可选）</span>
          <input v-model="wsBaseInput" type="text" placeholder="默认同 REST 地址" />
        </label>
      </div>
      <p class="endpoint-line">
        REST：<strong>{{ restBase || '未设置' }}</strong>
        <span class="divider">·</span>
        WebSocket：<strong>{{ wsUrl || '未设置' }}</strong>
      </p>
    </section>

    <section v-if="!jwt" class="panel-grid">
      <form class="panel" @submit.prevent="register">
        <h2>注册</h2>
        <p class="panel-hint">填写邮箱、用户 ID 与密码，立即创建账号并登录。</p>
        <label>
          <span>邮箱</span>
          <input v-model="registerForm.email" type="email" placeholder="demo@example.com" required />
        </label>
        <label>
          <span>用户 ID</span>
          <input v-model="registerForm.userId" type="text" placeholder="如：10001" required />
        </label>
        <label>
          <span>密码</span>
          <input v-model="registerForm.password" type="password" placeholder="不少于 6 位" required />
        </label>
        <button type="submit">注册并登录</button>
      </form>

      <form class="panel" @submit.prevent="login">
        <h2>登录</h2>
        <p class="panel-hint">已注册用户直接登录，系统会保存 JWT 并尝试自动重连 WebSocket。</p>
        <label>
          <span>邮箱</span>
          <input v-model="loginForm.email" type="email" placeholder="demo@example.com" required />
        </label>
        <label>
          <span>用户 ID</span>
          <input v-model="loginForm.userId" type="text" placeholder="与注册一致" required />
        </label>
        <label>
          <span>密码</span>
          <input v-model="loginForm.password" type="password" required />
        </label>
        <button type="submit">登录</button>
      </form>
    </section>

    <section v-else class="panel">
      <h2>账号与连接</h2>
      <div class="status-line">
        <span :class="['status-dot', stompConnected ? 'online' : 'offline']"></span>
        <span>{{ connectionStatus }}</span>
      </div>
      <div class="account-line">
        <span>当前用户：<strong>{{ currentUserId || '未设置' }}</strong></span>
        <div class="account-actions">
          <button type="button" class="secondary" @click="logout">退出登录</button>
          <button type="button" @click="connectSocket" :disabled="connecting">重新连接</button>
        </div>
      </div>
    </section>

    <section v-if="jwt" class="panel">
      <h2>建立会话</h2>
      <p class="panel-hint">输入对方用户 ID，系统将调用 /conversation 接口创建或查询会话。</p>
      <div class="field-inline">
        <label>
          <span>对方用户 ID</span>
          <input v-model="peerUserIdInput" type="text" placeholder="例如：10002" />
        </label>
        <button type="button" @click="openConversation">开启 / 进入会话</button>
      </div>
      <p v-if="conversationReady" class="conversation-line">
        会话 ID：<strong>{{ conversationId }}</strong>
        <span class="divider">·</span>
        对方：<strong>{{ activePeerId }}</strong>
      </p>
      <p v-if="peerPresence" class="presence">{{ peerPresence }}</p>
    </section>

    <section v-if="jwt && conversationReady" class="panel chat-panel">
      <h2>聊天窗口</h2>
      <div class="messages" ref="messagePane">
        <div v-if="messages.length === 0" class="empty">暂无消息，开始聊天吧～</div>
        <div
          v-for="msg in messages"
          :key="msg.localKey"
          :class="['message', msg.fromSelf ? 'self' : 'peer']"
        >
          <div class="bubble">{{ msg.content }}</div>
          <div class="meta">
            <span>{{ msg.fromSelf ? '我' : msg.senderId }}</span>
            <span v-if="msg.sid">#{{ msg.sid }}</span>
            <span>{{ formatTime(msg.timestamp) }}</span>
            <span
              v-if="msg.fromSelf"
              :class="['ack', msg.acked ? 'delivered' : 'pending']"
            >
              {{ msg.acked ? '已送达' : '等待确认' }}
            </span>
          </div>
        </div>
      </div>
      <div class="composer">
        <label class="visually-hidden" for="chat-draft">输入消息</label>
        <textarea
          id="chat-draft"
          v-model="draft"
          rows="3"
          placeholder="输入消息，回车发送，Shift+Enter 换行"
          @keydown.enter.exact.prevent="sendMessage"
        ></textarea>
        <div class="composer-actions">
          <button type="button" @click="sendMessage" :disabled="!stompConnected">发送</button>
        </div>
      </div>
    </section>

    <section class="panel">
      <h2>运行日志</h2>
      <ul class="log-list">
        <li v-for="(entry, idx) in logs" :key="idx">{{ entry }}</li>
      </ul>
    </section>
  </div>
</template>

<script setup>
import { ref, reactive, computed, watch, onMounted, onBeforeUnmount, nextTick } from 'vue'
import axios from 'axios'
import SockJS from 'sockjs-client'
import { Client } from '@stomp/stompjs'

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
const messageCounter = ref(0)
const peerPresence = ref('')
const isTyping = ref(false)
const messagePane = ref(null)

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

watch(restBaseInput, (val) => {
  localStorage.setItem('inclove-rest-base', val || '')
})
watch(wsBaseInput, (val) => {
  localStorage.setItem('inclove-ws-base', val || '')
})
watch(draft, (val) => {
  isTyping.value = val.trim().length > 0
  if (stompConnected.value && conversationReady.value) {
    sendHeartbeat(true)
  }
})

function normalizeBase(value) {
  if (!value) return ''
  let trimmed = value.trim()
  if (!trimmed) return ''
  if (!/^https?:\/\//i.test(trimmed)) {
    trimmed = `http://${trimmed}`
  }
  return trimmed.replace(/\/+$/, '')
}

function log(message) {
  const entry = `${new Date().toLocaleTimeString()} ${message}`
  logs.value.unshift(entry)
  if (logs.value.length > 120) {
    logs.value.pop()
  }
  console.log(message)
}

function handleHttpError(context, error) {
  console.error(context, error)
  const detail =
    error?.response?.data?.message || error?.response?.data || error?.message || '未知错误'
  log(`${context}：${detail}`)
}

function authHeaders() {
  return jwt.value ? { Authorization: `Bearer ${jwt.value}` } : {}
}

async function register() {
  if (!restBase.value) {
    log('请先设置有效的 REST API 地址。')
    return
  }
  const payload = {
    email: registerForm.email.trim(),
    userId: registerForm.userId.trim(),
    rawPassword: registerForm.password,
  }
  if (!payload.email || !payload.userId || !payload.rawPassword) {
    log('请完整填写注册信息。')
    return
  }
  try {
    const res = await axios.post(`${restBase.value}/auth/register`, payload, {
      headers: { 'Content-Type': 'application/json' },
    })
    log('注册成功，自动完成登录。')
    if (res.data?.jwtToken) {
      afterLogin(payload.userId, res.data.jwtToken)
    }
  } catch (err) {
    handleHttpError('注册失败', err)
  }
}

async function login() {
  if (!restBase.value) {
    log('请先设置有效的 REST API 地址。')
    return
  }
  const payload = {
    email: loginForm.email.trim(),
    userId: loginForm.userId.trim(),
    rawPassword: loginForm.password,
  }
  if (!payload.email || !payload.userId || !payload.rawPassword) {
    log('请完整填写登录信息。')
    return
  }
  try {
    const res = await axios.post(`${restBase.value}/auth/login`, payload, {
      headers: { 'Content-Type': 'application/json' },
    })
    afterLogin(payload.userId, res.data?.jwtToken)
    log('登录成功。')
  } catch (err) {
    handleHttpError('登录失败', err)
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
  log('已退出登录。')
}

async function connectSocket() {
  if (!jwt.value) {
    log('尚未登录，无法建立 WebSocket。')
    return
  }
  if (!wsUrl.value) {
    log('WebSocket 端点无效，请检查地址。')
    return
  }

  await disconnectSocket()

  connecting.value = true
  connectionStatus.value = '连接中…'

  const client = new Client({
    webSocketFactory: () => new SockJS(wsUrl.value),
    connectHeaders: { Authorization: `Bearer ${jwt.value}` },
    reconnectDelay: 5000,
    debug: (msg) => console.debug('[STOMP]', msg),
  })

  client.onConnect = () => {
    connecting.value = false
    connectionStatus.value = '已连接'
    log('STOMP 已连接。')
    subscription.value = client.subscribe('/user/queue/conversations', handleIncoming)
    if (conversationReady.value) {
      startHeartbeat()
    }
  }

  client.onStompError = (frame) => {
    connecting.value = false
    connectionStatus.value = '连接异常'
    log(`STOMP 错误：${frame.headers['message'] || '未知错误'}`)
  }

  client.onDisconnect = () => {
    connecting.value = false
    connectionStatus.value = '已断开'
    stopHeartbeat()
  }

  client.onWebSocketClose = () => {
    connecting.value = false
    connectionStatus.value = '连接断开'
    stopHeartbeat()
  }

  client.onWebSocketError = () => {
    log('WebSocket 发生错误。')
  }

  client.activate()
  stompClient.value = client
}

async function disconnectSocket() {
  stopHeartbeat()
  if (subscription.value) {
    try {
      subscription.value.unsubscribe()
    } catch (err) {
      console.debug(err)
    }
    subscription.value = null
  }
  if (stompClient.value) {
    try {
      await stompClient.value.deactivate()
    } catch (err) {
      console.debug(err)
    }
    stompClient.value = null
  }
  connecting.value = false
  if (jwt.value) {
    connectionStatus.value = '未连接'
  }
}

function startHeartbeat() {
  if (!conversationReady.value || !stompConnected.value) return
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
  if (!stompConnected.value) return
  if (!force && !conversationReady.value) return
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
  } catch (err) {
    console.debug('发送心跳失败', err)
  }
}

async function openConversation() {
  if (!restBase.value) {
    log('请先设置 REST API 地址。')
    return
  }
  if (!jwt.value) {
    log('请先登录。')
    return
  }
  const peerId = peerUserIdInput.value.trim()
  if (!peerId) {
    log('请输入对方的用户 ID。')
    return
  }
  try {
    const headers = authHeaders()
    await axios.post(
      `${restBase.value}/conversation`,
      null,
      { params: { peerUserId: peerId }, headers }
    )
    const res = await axios.get(`${restBase.value}/conversation/userIdAndPeerUserId`, {
      params: { peerUserId: peerId },
      headers,
    })
    const convoId = res.data?.id || res.data?.conversationId || ''
    if (!convoId) {
      log('未找到会话，请确认双方已经注册。')
      return
    }
    const switching = activePeerId.value && activePeerId.value !== peerId
    conversationId.value = String(convoId)
    activePeerId.value = peerId
    if (switching) {
      messages.value = []
      peerPresence.value = ''
    }
    log(`会话已准备就绪（ID: ${conversationId.value}）。`)
    if (stompConnected.value) {
      startHeartbeat()
    } else {
      connectSocket()
    }
  } catch (err) {
    handleHttpError('开启会话失败', err)
  }
}

function addLocalMessage(msg) {
  const localKey = msg.localKey || `local-${Date.now()}-${messageCounter.value++}`
  const record = {
    localKey,
    sid: msg.sid || null,
    senderId: msg.senderId,
    recipientId: msg.recipientId,
    conversationId: msg.conversationId,
    content: msg.content,
    timestamp: msg.timestamp || new Date(),
    fromSelf: Boolean(msg.fromSelf),
    acked: Boolean(msg.acked),
    awaitingServerSid: Boolean(msg.awaitingServerSid),
  }
  messages.value.push(record)
  scrollToBottom()
}

function sendMessage() {
  if (!conversationReady.value) {
    log('请先建立会话。')
    return
  }
  if (!stompConnected.value) {
    log('WebSocket 尚未连接。')
    return
  }
  const text = draft.value.trim()
  if (!text) return

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
  } catch (err) {
    log('发送失败：' + (err?.message || err))
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

function handleIncoming(frame) {
  try {
    const payload = JSON.parse(frame.body || '{}')
    const type = payload.packageType || (payload.content !== undefined ? 'MESSAGE' : 'ACK')
    if (type === 'MESSAGE') {
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
    } else if (type === 'ACK') {
      markAck(payload.sid)
    } else if (type === 'USER_STATUS') {
      updatePeerStatus(payload)
    } else {
      log('收到未知类型消息：' + JSON.stringify(payload))
    }
  } catch (error) {
    console.error('解析消息失败', error, frame.body)
    log('解析消息失败：' + (error?.message || error))
  }
}

function attachServerSid(sid) {
  if (!sid) return
  const pending = messages.value.find(
    (msg) => msg.fromSelf && (msg.awaitingServerSid || !msg.sid)
  )
  if (pending) {
    pending.sid = sid
    pending.awaitingServerSid = false
  }
}

function sendAck(payload) {
  if (!stompConnected.value) return
  if (!payload.sid) return
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
  } catch (err) {
    console.debug('发送 ACK 失败', err)
  }
}

function markAck(sid) {
  if (!sid) return
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

function formatTime(value) {
  if (!value) return ''
  const date = value instanceof Date ? value : new Date(value)
  return new Intl.DateTimeFormat('zh-CN', {
    hour: '2-digit',
    minute: '2-digit',
    second: '2-digit',
  }).format(date)
}

function scrollToBottom() {
  nextTick(() => {
    const pane = messagePane.value
    if (pane) {
      pane.scrollTop = pane.scrollHeight
    }
  })
}

onMounted(() => {
  if (jwt.value && currentUserId.value) {
    log('检测到本地凭证，尝试重连 WebSocket。')
    connectSocket()
  }
})

onBeforeUnmount(async () => {
  await disconnectSocket()
})
</script>

<style scoped>
.app-shell {
  max-width: 1080px;
  margin: 0 auto;
  padding: 32px 20px 48px;
}

.hero {
  margin-bottom: 24px;
}

.hero h1 {
  margin: 0 0 8px;
  font-size: 2rem;
  font-weight: 700;
}

.hero p {
  margin: 0;
  color: #4b5563;
}

.panel {
  background: #ffffff;
  border: 1px solid #e5e7eb;
  border-radius: 16px;
  padding: 24px;
  box-shadow: 0 10px 30px rgba(15, 23, 42, 0.08);
  margin-bottom: 20px;
}

.panel h2 {
  margin: 0 0 12px;
  font-size: 1.25rem;
}

.panel-hint {
  margin: 0 0 16px;
  font-size: 0.92rem;
  color: #6b7280;
}

.panel-grid {
  display: grid;
  gap: 20px;
  grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
  margin-bottom: 20px;
}

.field-grid {
  display: grid;
  gap: 16px;
  grid-template-columns: repeat(auto-fit, minmax(240px, 1fr));
}

label {
  display: flex;
  flex-direction: column;
  gap: 8px;
  font-size: 0.95rem;
  color: #374151;
  font-weight: 600;
}

input,
textarea {
  padding: 10px 12px;
  border-radius: 10px;
  border: 1px solid #cbd5f5;
  transition: border-color 0.2s ease, box-shadow 0.2s ease;
  background: #fff;
}

input:focus,
textarea:focus {
  outline: none;
  border-color: #2563eb;
  box-shadow: 0 0 0 3px rgba(37, 99, 235, 0.15);
}

button {
  padding: 10px 22px;
  border-radius: 999px;
  border: none;
  background: linear-gradient(135deg, #2563eb, #1d4ed8);
  color: #fff;
  font-weight: 600;
  cursor: pointer;
  transition: transform 0.15s ease, box-shadow 0.15s ease;
}

button:hover {
  transform: translateY(-1px);
  box-shadow: 0 12px 26px rgba(37, 99, 235, 0.25);
}

button:disabled {
  background: #94a3b8;
  cursor: not-allowed;
  box-shadow: none;
  transform: none;
}

button.secondary {
  background: #f3f4f6;
  color: #1f2937;
  box-shadow: none;
}

button.secondary:hover {
  background: #e5e7eb;
  transform: none;
}

.status-line {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 0.95rem;
  margin-bottom: 16px;
}

.status-dot {
  width: 12px;
  height: 12px;
  border-radius: 50%;
  background: #d1d5db;
}

.status-dot.online {
  background: #22c55e;
  box-shadow: 0 0 12px rgba(34, 197, 94, 0.55);
}

.status-dot.offline {
  background: #ef4444;
  box-shadow: 0 0 12px rgba(239, 68, 68, 0.45);
}

.account-line {
  display: flex;
  flex-wrap: wrap;
  gap: 16px;
  align-items: center;
  justify-content: space-between;
}

.account-actions {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
}

.field-inline {
  display: flex;
  flex-wrap: wrap;
  gap: 16px;
  align-items: flex-end;
}

.field-inline label {
  flex: 1 1 240px;
}

.presence {
  margin-top: 12px;
  font-size: 0.95rem;
  color: #2563eb;
  font-weight: 600;
}

.conversation-line {
  margin-top: 12px;
  font-size: 0.95rem;
  color: #374151;
}

.messages {
  max-height: 360px;
  overflow-y: auto;
  display: flex;
  flex-direction: column;
  gap: 14px;
  padding-right: 6px;
}

.message {
  display: flex;
  flex-direction: column;
  gap: 6px;
  max-width: 75%;
}

.message.self {
  margin-left: auto;
  align-items: flex-end;
}

.message.peer {
  margin-right: auto;
  align-items: flex-start;
}

.bubble {
  padding: 10px 14px;
  border-radius: 16px;
  line-height: 1.45;
  word-break: break-word;
  background: #e5e7eb;
  color: #111827;
  box-shadow: 0 10px 24px rgba(15, 23, 42, 0.1);
}

.message.self .bubble {
  background: linear-gradient(135deg, #2563eb, #1d4ed8);
  color: #fff;
  border-bottom-right-radius: 4px;
}

.message.peer .bubble {
  border-bottom-left-radius: 4px;
}

.meta {
  font-size: 0.78rem;
  color: #6b7280;
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  align-items: center;
}

.ack.delivered {
  color: #15803d;
}

.ack.pending {
  color: #ea580c;
}

.empty {
  text-align: center;
  color: #94a3b8;
  font-size: 0.9rem;
  padding: 48px 0;
}

.composer {
  margin-top: 16px;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

textarea {
  resize: vertical;
  min-height: 96px;
}

.composer-actions {
  display: flex;
  justify-content: flex-end;
}

.log-list {
  list-style: none;
  margin: 0;
  padding: 0;
  max-height: 220px;
  overflow-y: auto;
  display: flex;
  flex-direction: column;
  gap: 6px;
  font-size: 0.85rem;
  color: #4b5563;
}

.log-list li {
  padding: 4px 0;
  border-bottom: 1px dashed #e5e7eb;
}

.log-list li:last-child {
  border-bottom: none;
}

.endpoint-line {
  font-size: 0.9rem;
  color: #4b5563;
  margin-top: 16px;
}

.divider {
  margin: 0 6px;
  opacity: 0.5;
}

.visually-hidden {
  position: absolute;
  width: 1px;
  height: 1px;
  padding: 0;
  margin: -1px;
  overflow: hidden;
  clip: rect(0, 0, 0, 0);
  white-space: nowrap;
  border: 0;
}

@media (max-width: 640px) {
  .panel {
    padding: 20px;
  }

  button {
    width: 100%;
  }

  .account-actions {
    width: 100%;
    flex-direction: column;
    align-items: stretch;
  }

  .field-inline {
    flex-direction: column;
    align-items: stretch;
  }
}
</style>
