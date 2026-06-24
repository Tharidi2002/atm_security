import React, { createContext, useState, useContext, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import toast from 'react-hot-toast';
import { authService } from '../services/authService';

const AuthContext = createContext();

export const useAuth = () => useContext(AuthContext);

export const AuthProvider = ({ children }) => {
  const [user, setUser] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const token = localStorage.getItem('authToken');
    const userData = localStorage.getItem('user');
    
    if (token && userData) {
      try {
        setUser(JSON.parse(userData));
      } catch {
        localStorage.removeItem('authToken');
        localStorage.removeItem('user');
      }
    }
    setLoading(false);
  }, []);

  const login = async (username, password, navigate) => {
    try {
      setLoading(true);
      const data = await authService.login(username, password);
      
      localStorage.setItem('authToken', data.token);
      localStorage.setItem('user', JSON.stringify(data.user));
      setUser(data.user);
      
      toast.success(`Welcome ${data.user.fullName || data.user.username}!`);
      if (navigate) {
        navigate('/dashboard');
      }
      return true;
    } catch (error) {
      const message = error.response?.data?.error || 'Login failed. Please try again.';
      toast.error(message);
      return false;
    } finally {
      setLoading(false);
    }
  };

  const register = async (userData) => {
    try {
      setLoading(true);
      const data = await authService.register(userData);
      toast.success(data.message || 'Registration successful! Please login.');
      return true;
    } catch (error) {
      const message = error.response?.data?.error || 'Registration failed. Please try again.';
      toast.error(message);
      return false;
    } finally {
      setLoading(false);
    }
  };

  const logout = (navigate) => {
    authService.logout();
    setUser(null);
    toast.success('Logged out successfully');
    if (navigate) {
      navigate('/login');
    }
  };

  const value = {
    user,
    loading,
    login,
    register,
    logout,
    isAuthenticated: !!user,
  };

  return (
    <AuthContext.Provider value={value}>
      {children}
    </AuthContext.Provider>
  );
};