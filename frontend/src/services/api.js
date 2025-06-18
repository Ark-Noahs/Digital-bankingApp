// src/services/api.js
import axios from "axios";

const API_BASE_URL = "http://localhost:8080/api"; // adjust if your backend runs elsewhere

export const login = (email, password) =>
  axios.post(`${API_BASE_URL}/auth/login`, { email, password });

export const getUserInfo = (token) =>
  axios.get(`${API_BASE_URL}/users/me`, {
    headers: { Authorization: `Bearer ${token}` },
  });

// Add more functions for other API endpoints as you go
