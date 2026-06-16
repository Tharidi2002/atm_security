import React, { createContext, useContext, useEffect, useState } from 'react';
import { authApi } from '../services/api';
import type { AuthResponse, UserProfile } from '../types';

interface AuthContextType {
  user: UserProfile | null;
  isAuthenticated: boolean;
  loading: boolean;
  login: (username: string, password: string) => Promise<AuthResponse>;
  register: (payload: {
    username: string;
    email: string;
    password: string;
    fullName: string;
    bankId: number;
  }) => Promise<AuthResponse>;
  logout: () => Promise<void>;
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

export const AuthProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const [user, setUser] = useState<UserProfile | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const token = localStorage.getItem('accessToken');
    if (token) {
      authApi
        .profile()
        .then((res) => setUser(res.data.data))
        .catch(() => localStorage.clear())
        .finally(() => setLoading(false));
    } else {
      setLoading(false);
    }
  }, []);

  const persistAuth = (data: AuthResponse) => {
    localStorage.setItem('accessToken', data.accessToken);
    localStorage.setItem('refreshToken', data.refreshToken);
    setUser(data.user);
    return data;
  };

  const login = async (username: string, password: string) => {
    const res = await authApi.login(username, password);
    return persistAuth(res.data.data);
  };

  const register = async (payload: {
    username: string;
    email: string;
    password: string;
    fullName: string;
    bankId: number;
  }) => {
    const res = await authApi.register(payload);
    return persistAuth(res.data.data);
  };

  const logout = async () => {
    const refreshToken = localStorage.getItem('refreshToken');
    if (refreshToken) {
      try {
        await authApi.logout(refreshToken);
      } catch {
        /* ignore */
      }
    }
    localStorage.clear();
    setUser(null);
  };

  return (
    <AuthContext.Provider
      value={{
        user,
        isAuthenticated: !!user,
        loading,
        login,
        register,
        logout,
      }}
    >
      {children}
    </AuthContext.Provider>
  );
};

export const useAuth = () => {
  const ctx = useContext(AuthContext);
  if (!ctx) throw new Error('useAuth must be used within AuthProvider');
  return ctx;
};
