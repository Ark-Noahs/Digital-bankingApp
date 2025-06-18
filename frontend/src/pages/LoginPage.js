
import React, { useState } from "react";
import { login } from "../services/api";
import { Link } from "react-router-dom";


function LoginPage({ onLogin }) {
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [error, setError] = useState("");

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError("");
    try {
      const res = await login(email, password);
      const token = res.data.token; // adjust if your backend returns { token: ... }
      onLogin(token);
    } catch (err) {
      setError("Login failed. Check your credentials.");
    }
  };

  return (
    <div style={{ maxWidth: 400, margin: "80px auto" }}>
      <h2>Login</h2>
      <form onSubmit={handleSubmit}>
        <div>
          <input
            type="email"
            value={email}
            placeholder="Email"
            autoComplete="username"
            onChange={e => setEmail(e.target.value)}
            required
            style={{ width: "100%", marginBottom: 10 }}
          />
        </div>
        <div>
          <input
            type="password"
            value={password}
            placeholder="Password"
            autoComplete="current-password"
            onChange={e => setPassword(e.target.value)}
            required
            style={{ width: "100%", marginBottom: 10 }}
          />
        </div>
        {error && <div style={{ color: "red", marginBottom: 10 }}>{error}</div>}
        <button type="submit" style={{ width: "100%" }}>Login</button>
      </form>
        <p style={{ marginTop: 15 }}>
            Don't have an account? <Link to="/register">Register here</Link>
        </p>  
    </div>
  );
}

<p style={{ marginTop: 15 }}>
  Don't have an account? <a href="/register">Register here</a>
</p>



export default LoginPage;
