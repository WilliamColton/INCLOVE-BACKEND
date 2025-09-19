'use strict';

import http from './http';

export const createProfile = (payload) => http.post('/profile', payload);
export const fetchMatches = () => http.get('/profile');
export const fetchCurrentUser = () => http.get('/profile/me');
