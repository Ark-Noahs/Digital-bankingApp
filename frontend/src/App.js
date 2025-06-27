import React, { useState } from 'react';
import { Routes, Route, Navigate } from 'react-router-dom';
import LoginPage from './components/LoginPage';
import RegisterPage from './components/RegisterPage';
import Dashboard from './components/Dashboard';
import ForgotPasswordPage from './components/ForgotPasswordPage';

function App() {
  //store JWT token from local storage if it exists
  const [token, setToken] = useState(localStorage.getItem('token'));

  //called after successful login....
  const handleLogin = (newToken) => {
    localStorage.setItem('token', newToken); // persist token
    setToken(newToken);                      // update state
  };

  //called when user logs out
  const handleLogout = () => {
    localStorage.removeItem('token'); // clear token
    setToken(null);                   // update state
  };

  return (
    <Routes>
      {/*public route --> login page */}
      <Route
        path="/login"
        element={<LoginPage onLogin={handleLogin} />}
      />

      {/*public route --> register page */}
      <Route
        path="/register"
        element={<RegisterPage />}
      />

      {/*public Route --> forgot password */}
      <Route
        path="/forgot-password"
        element={<ForgotPasswordPage />}
      />

      {/*protected route --> Dashboard */}
      <Route
        path="/dashboard"
        element={
          token
            ? <Dashboard token={token} onLogout={handleLogout} />
            : <Navigate to="/login" replace /> // redirect to login if no token
        }
      />

      {/*catch-all route redirects to login */}
      <Route path="*" element={<Navigate to="/login" replace />} />
    </Routes>
  );
}

export default App;
