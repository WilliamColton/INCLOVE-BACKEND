'use strict';

import http from './http';

export const createPreference = (payload) => http.post('/preference', payload);
