import React, { useState } from "react";
import { BrowserRouter as Router, Route, Routes, Navigate } from "react-router-dom";
import LoginPage from "./pages/LoginPage";
import Dashboard from "./pages/Dashboard";

// Helper: Checks if token exists in localStorage
function isAuthenticated() {
  return !!localStorage.getItem("token");
}

function App() {
  // This is a trick to "refresh" the app when user logs in/out
  const [auth, setAuth] = useState(isAuthenticated());

  // Login success handler
  const handleLogin = (token) => {
    localStorage.setItem("token", token);
    setAuth(true);
  };

  // Logout handler
  const handleLogout = () => {
    localStorage.removeItem("token");
    setAuth(false);
  };

  return (
    <Router>
      <Routes>
        <Route
          path="/login"
          element={
            isAuthenticated() ? (
              <Navigate to="/dashboard" />
            ) : (
              <LoginPage onLogin={handleLogin} />
            )
          }
        />
        <Route
          path="/dashboard"
          element={
            isAuthenticated() ? (
              <Dashboard onLogout={handleLogout} />
            ) : (
              <Navigate to="/login" />
            )
          }
        />
        <Route
          path="/"
          element={
            <Navigate to={isAuthenticated() ? "/dashboard" : "/login"} />
          }
        />
      </Routes>
    </Router>
  );
}

export default App;
