'use strict';

import http from './http';

export const createTraits = (traitNames) => http.post('/trait', { traitNames });
export const getTraits = () => http.get('/trait');
