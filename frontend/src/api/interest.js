'use strict';

import http from './http';

export const createInterests = (interestNames) => http.post('/interest', { interestNames });
export const getInterests = () => http.get('/interest');
