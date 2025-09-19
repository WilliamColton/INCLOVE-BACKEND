<template>
  <el-card shadow="never">
    <template #header>
      <div class="chat-header">
        <span>Conversation</span>
        <el-tag :type="connectionStatus === 'connected' ? 'success' : 'info'">{{ connectionStatus }}</el-tag>
      </div>
    </template>
    <el-form label-position="top" class="chat-form">
      <el-form-item label="Peer User ID">
        <el-input v-model="peerUserId" placeholder="Enter target user ID" clearable />
      </el-form-item>
      <el-form-item label="Conversation ID">
        <el-input v-model="conversationId" placeholder="Conversation ID" readonly />
      </el-form-item>
      <div class="button-group">
        <el-button :loading="conversationLoading" @click="initializeConversation">Open Conversation</el-button>
        <el-button type="primary" :loading="connecting" @click="connect">Connect</el-button>
        <el-button type="danger" :disabled="!stompClient" @click="disconnect">Disconnect</el-button>
      </div>
    </el-form>
    <el-divider />
    <el-form label-position="top" class="chat-form">
      <el-form-item label="Message">
        <el-input
          v-model="messageContent"
          placeholder="Type message"
          type="textarea"
          :autosize="{ minRows: 2, maxRows: 4 }"
        />
      </el-form-item>
      <div class="button-group">
        <el-button type="primary" :disabled="!canSend" :loading="sendLoading" @click="sendMessage">Send</el-button>
        <el-button :disabled="!canSend" @click="sendHeartbeat">Send Heartbeat</el-button>
      </div>
    </el-form>
    <el-divider />
    <h4>Messages</h4>
    <el-empty v-if="messages.length === 0" description="No messages" />
    <el-timeline v-else>
      <el-timeline-item
        v-for="item in messages"
        :key="item.localId"
        :type="item.direction === 'outgoing' ? 'primary' : 'success'"
        :timestamp="item.timestamp"
        placement="top"
      >
        <el-card :body-style="{ padding: '12px' }">
          <p>{{ item.content }}</p>
          <small>
            {{ item.direction === 'outgoing' ? 'To' : 'From' }}: {{ item.direction === 'outgoing' ? item.recipientId : item.senderId }}
            <span v-if="item.status"> · {{ item.status }}</span>
          </small>
        </el-card>
      </el-timeline-item>
    </el-timeline>
    <el-divider />
    <h4>Status Updates</h4>
    <el-empty v-if="statusLog.length === 0" description="No status updates" />
    <el-timeline v-else>
      <el-timeline-item v-for="item in statusLog" :key="item.id" type="info" :timestamp="item.timestamp">
        <span>{{ item.message }}</span>
      </el-timeline-item>
    </el-timeline>
  </el-card>
</template>

<script setup>
'use strict';

import { computed, onBeforeUnmount, reactive, ref } from 'vue';
import { ElMessage } from 'element-plus';
import SockJS from 'sockjs-client/dist/sockjs';
import { Client } from '@stomp/stompjs';
import { findConversationByUsers, openConversation } from '../../api/conversation';
import { useAuthStore } from '../../store/auth';

const authStore = useAuthStore();

const peerUserId = ref('');
const conversationId = ref('');
const messageContent = ref('');
const messages = reactive([]);
const statusLog = reactive([]);
const pendingQueue = reactive([]);
const stompClient = ref();
const subscription = ref();
const connecting = ref(false);
const sendLoading = ref(false);
const conversationLoading = ref(false);
const connectionStatus = ref('disconnected');
let heartbeatTimer;
const HEARTBEAT_INTERVAL_MS = 5000;
let localMessageCounter = 0;

const wsUrl = import.meta.env.VITE_WS_BASE_URL || 'http://localhost:8080/ws';

const canSend = computed(
  () => Boolean(stompClient.value && connectionStatus.value === 'connected' && peerUserId.value)
);

const pushStatus = (message) => {
  statusLog.push({ id: `${Date.now()}-${statusLog.length}`, message, timestamp: new Date().toLocaleString() });
};

const stopHeartbeatTimer = () => {
  if (heartbeatTimer) {
    clearInterval(heartbeatTimer);
    heartbeatTimer = undefined;
  }
};

const initializeConversation = async () => {
  if (!peerUserId.value) {
    ElMessage.warning('Please enter the peer user ID');
    return;
  }
  try {
    conversationLoading.value = true;
    await openConversation(peerUserId.value.trim());
    const result = await findConversationByUsers(peerUserId.value.trim());
    if (result?.id) {
      conversationId.value = result.id;
      ElMessage.success('Conversation ready');
    } else {
      ElMessage.warning('Conversation not found');
    }
  } finally {
    conversationLoading.value = false;
  }
};

const connect = () => {
  if (!authStore.token) {
    ElMessage.error('Please login again');
    return;
  }
  if (!peerUserId.value) {
    ElMessage.warning('Set peer user ID first');
    return;
  }
  if (stompClient.value) {
    disconnect();
  }
  connecting.value = true;
  connectionStatus.value = 'connecting';
  const client = new Client({
    webSocketFactory: () => new SockJS(wsUrl),
    connectHeaders: {
      Authorization: `Bearer ${authStore.token}`
    },
    debug: () => {},
    reconnectDelay: 0
  });

  client.onConnect = () => {
    connecting.value = false;
    connectionStatus.value = 'connected';
    subscription.value = client.subscribe('/user/queue/conversations', (frame) => {
      if (!frame?.body) {
        return;
      }
      try {
        const payload = JSON.parse(frame.body);
        handleIncoming(payload);
      } catch (error) {
        pushStatus(`Invalid payload: ${frame.body}`);
      }
    });
    pushStatus('Connected to chat server');
    sendHeartbeat();
    startHeartbeatTimer();
  };

  client.onDisconnect = () => {
    connectionStatus.value = 'disconnected';
    stopHeartbeatTimer();
    pushStatus('Disconnected from chat server');
  };

  client.onStompError = (frame) => {
    connecting.value = false;
    connectionStatus.value = 'error';
    stopHeartbeatTimer();
    pushStatus(`Broker error: ${frame?.headers['message'] || ''}`);
  };

  client.onWebSocketError = (event) => {
    connecting.value = false;
    connectionStatus.value = 'error';
    stopHeartbeatTimer();
    pushStatus(`WebSocket error: ${event?.type || 'unknown'}`);
  };

  client.activate();
  stompClient.value = client;
};

const disconnect = () => {
  stopHeartbeatTimer();
  if (subscription.value) {
    subscription.value.unsubscribe();
    subscription.value = undefined;
  }
  if (stompClient.value) {
    stompClient.value.deactivate();
    stompClient.value = undefined;
  }
  connectionStatus.value = 'disconnected';
  pushStatus('Connection closed');
};

const handleIncoming = (payload) => {
  if (!payload?.packageType) {
    return;
  }
  if (payload.packageType === 'MESSAGE') {
    const item = {
      localId: `${payload.sid}-${Date.now()}`,
      sid: payload.sid,
      content: payload.content,
      senderId: payload.senderId,
      recipientId: payload.recipientID,
      conversationId: payload.conversationId,
      direction: payload.senderId === authStore.userId ? 'outgoing' : 'incoming',
      status: payload.senderId === authStore.userId ? 'delivered' : 'received',
      timestamp: new Date().toLocaleString()
    };
    messages.push(item);
    if (item.direction === 'incoming') {
      sendAck(item);
    }
  } else if (payload.packageType === 'ACK') {
    const pending = pendingQueue.shift();
    if (pending) {
      pending.sid = payload.sid;
      pending.status = 'delivered';
      pending.timestamp = new Date().toLocaleString();
    }
  } else if (payload.packageType === 'USER_STATUS') {
    const onlineText = payload.isOnline ? 'online' : 'offline';
    pushStatus(`${payload.peerId || 'Peer'} is ${onlineText}${payload.isTyping ? ' and typing' : ''}`);
  }
};

const sendMessage = async () => {
  if (!canSend.value) {
    return;
  }
  if (!conversationId.value) {
    ElMessage.warning('Open conversation first');
    return;
  }
  if (!messageContent.value.trim()) {
    ElMessage.warning('Please enter a message');
    return;
  }
  if (!stompClient.value || !stompClient.value.connected) {
    ElMessage.warning('Connect first');
    return;
  }
  sendLoading.value = true;
  try {
    const now = new Date();
    const formatted = now.toISOString().slice(0, 19);
    const tempMessage = {
      localId: `out-${localMessageCounter}`,
      sid: '',
      content: messageContent.value,
      senderId: authStore.userId,
      recipientId: peerUserId.value,
      conversationId: conversationId.value,
      direction: 'outgoing',
      status: 'pending',
      timestamp: now.toLocaleString()
    };
    localMessageCounter += 1;
    messages.push(tempMessage);
    pendingQueue.push(tempMessage);
    stompClient.value.publish({
      destination: '/app/chat.send',
      body: JSON.stringify({
        packageType: 'MESSAGE',
        recipientId: peerUserId.value,
        conversationId: conversationId.value,
        content: messageContent.value,
        createdAt: formatted
      })
    });
    messageContent.value = '';
  } finally {
    sendLoading.value = false;
  }
};

const sendAck = (message) => {
  if (!stompClient.value || !stompClient.value.connected) {
    return;
  }
  stompClient.value.publish({
    destination: '/app/chat.ack',
    body: JSON.stringify({
      sid: message.sid,
      conversationId: message.conversationId,
      senderID: message.senderId
    })
  });
};

const sendHeartbeat = ({ silent = false } = {}) => {
  if (!stompClient.value || !stompClient.value.connected || !peerUserId.value) {
    if (!silent) {
      ElMessage.warning('Connect before sending heartbeat');
    }
    return;
  }
  const now = new Date().toISOString().slice(0, 19);
  stompClient.value.publish({
    destination: '/app/user.heartbeat',
    body: JSON.stringify({
      isOnline: true,
      isTyping: false,
      lastActiveTime: now,
      peerId: peerUserId.value
    })
  });
  if (!silent) {
    pushStatus('Heartbeat sent');
  }
};

const startHeartbeatTimer = () => {
  stopHeartbeatTimer();
  heartbeatTimer = setInterval(() => {
    sendHeartbeat({ silent: true });
  }, HEARTBEAT_INTERVAL_MS);
};

onBeforeUnmount(() => {
  disconnect();
});
</script>

<style scoped>
.chat-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.chat-form {
  margin-bottom: 16px;
}

.button-group {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
}
</style>
