'use strict';

import http from './http';

export const login = (payload) => http.post('/auth/login', payload);
export const register = (payload) => http.post('/auth/register', payload);
export const resendRegisterEmail = () => http.post('/auth/register/email');
export const verifyRegisterCode = (payload) => http.post('/auth/register/verificationCode', payload);
export const googleLogin = (payload) => http.post('/auth/login/google', payload);
