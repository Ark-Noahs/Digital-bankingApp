
import React, { useEffect, useState } from "react";
import { getUserInfo } from "../services/api";

function Dashboard({ onLogout }) {
  const [user, setUser] = useState(null);

  useEffect(() => {
    const token = localStorage.getItem("token");
    if (!token) return;
    getUserInfo(token)
      .then((res) => setUser(res.data))
      .catch((err) => {
        // Invalid/expired token, force logout
        onLogout();
      });
  }, [onLogout]);

  if (!user) {
    return <div>Loading...</div>;
  }

  return (
    <div style={{ maxWidth: 600, margin: "80px auto" }}>
      <h2>Welcome, {user.name || user.email}!</h2>
      <p>User ID: {user.id}</p>
      <button onClick={onLogout}>Logout</button>
      {/* You can show account info, transactions, etc. here */}
    </div>
  );
}

export default Dashboard;