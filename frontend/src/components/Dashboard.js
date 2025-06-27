

import React, { useEffect, useState } from 'react';
  import { getUserAccounts } from "../services/api";
  
  function Dashboard() {
    const [accounts, setAccounts] = useState([]);
    const [loading, setLoading] = useState(true);

    const token = localStorage.getItem("token");
  
    useEffect(() => {
      async function fetchAccounts() {
        try {
            const res = await getUserAccounts(token);
            setAccounts(res.data);
        } catch (err) {
            console.error("ERROR fetching accounts", err);
        } finally {
            setLoading(false);
        }
      }

      fetchAccounts();
    }, [token]);
    if (loading) return <p>Loading........</p>
  
    return (
      <div style={{ display: "flex", height: "100vh" }}>
      <div style={{ width: "250px", borderRight: "1px solid #ccc", padding: 20 }}>
        <h3>Your Accounts</h3>
        {accounts.length === 0 ? (
          <p>You don't have any accounts yet.</p>
        ) : (
          <ul>
            {accounts.map(acc => (
              <li key={acc.id}>{acc.name} (${acc.balance})</li>
            ))}
          </ul>
        )}
        <button>Create Account</button>
      </div>
      <div style={{ flex: 1, padding: 20 }}>
        <h2>Dashboard</h2>
        <button>Send Money</button>
      </div>
    </div>
  );
}

export default Dashboard;
