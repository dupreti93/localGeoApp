import { createContext, useState, useEffect } from 'react';
import axios from 'axios';

export const AuthContext = createContext();

export const AuthProvider = ({ children }) => {
  const [user, setUser] = useState(null);
  const [token, setToken] = useState(localStorage.getItem('token'));
  const [loading, setLoading] = useState(true); // Add loading state

  useEffect(() => {
    const fetchUser = async () => {
      if (token) {
        try {
          const payload = JSON.parse(atob(token.split('.')[1]));
          const userId = payload.sub; // Extract userId from token
          const res = await axios.get(`http://18.224.30.8:8080/api/users/${userId}`, {
            headers: { Authorization: `Bearer ${token}` },
          });
          setUser({ ...res.data, userId }); // Ensure userId is preserved
          setLoading(false);
        } catch (err) {
          console.error('Error fetching user:', err.response?.status, err.message);
          setUser({ userId, displayName: 'User' });
          setLoading(false);
        }
      } else {
        setLoading(false);
      }
    };

    fetchUser();
  }, [token]);

  const login = async (username, password) => {
    try {
      const res = await axios.post(
        'http://18.224.30.8:8080/api/auth/login',
        { username, password },
        {
          headers: {
            'Content-Type': 'application/json',
            'Accept': 'application/json',
          },
        }
      );
      console.log('Login successful, token:', res.data);
      setToken(res.data);
      localStorage.setItem('token', res.data);
    } catch (err) {
      console.error('Login error:', err.response?.status, err.response?.data, err.message);
      throw err;
    }
  };

  const register = async (username, password, displayName, bio) => {
    try {
      await axios.post(
        'http://18.224.30.8:8080/api/auth/register',
        { username, password, displayName, bio },
        {
          headers: {
            'Content-Type': 'application/json',
            'Accept': 'application/json',
          },
        }
      );
    } catch (err) {
      console.error('Register error:', err.response?.status, err.response?.data, err.message);
      throw err;
    }
  };

  const logout = () => {
    console.log('Logging out');
    setToken(null);
    setUser(null);
    localStorage.removeItem('token');
  };

  return (
    <AuthContext.Provider value={{ user, token, login, register, logout, loading }}>
      {children}
    </AuthContext.Provider>
  );
};