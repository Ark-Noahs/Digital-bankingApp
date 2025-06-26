import React, { useEffect, useState } from 'react';
  import { getUserInfo } from '../services/api';
  
  function Dashboard({ token, onLogout }) {
    const [user, setUser] = useState(null);
  
    useEffect(() => {
      const fetchUser = async () => {
        try {
          const res = await getUserInfo(token);
          setUser(res.data);
        } catch (err) {
          console.error('Failed to fetch user info');
          onLogout();
        }
      };
      fetchUser();
    }, [token, onLogout]);
  
    return (
      <div>
        <h2>Dashboard</h2>
        {user ? (
          <div>
            <p>Welcome, {user.username}!</p>
            <button onClick={onLogout}>Logout</button>
          </div>
        ) : (
          <p>Loading...</p>
        )}
      </div>
    );
  }
  
  export default Dashboard;
