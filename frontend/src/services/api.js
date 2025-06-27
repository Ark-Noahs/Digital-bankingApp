

import axios from 'axios';

const API = axios.create({
  baseURL: 'http://localhost:8080/api'
});

export const login = (email, password) => API.post('/users/login', { email, password });
export const register = (email, password, username) => API.post('/users/register', { email, password, username });
export const getUserInfo = (token) => API.get('/users/me', {
  headers: { Authorization: `Bearer ${token}` }
});
export const getUserAccounts = (token) =>       //call to get the users account
    axios.get("http://localhost:8080/api/accounts", {
        headers: {Authorization: 'Bearer ${token}' } ,
    });