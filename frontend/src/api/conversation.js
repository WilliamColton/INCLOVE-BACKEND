'use strict';

import http from './http';

export const openConversation = (peerUserId) => http.post('/conversation', null, { params: { peerUserId } });
export const findConversationByUsers = (peerUserId) => http.get('/conversation/userIdAndPeerUserId', { params: { peerUserId } });
export const findConversationById = (conversationId) => http.get('/conversation/id', { params: { conversationId } });
