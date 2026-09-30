import { createContext, useContext, useState, useCallback } from 'react';
import * as authApi from '../api/auth';

const AuthContext = createContext(null);

export function AuthProvider({ children }) {
  const [user, setUser] = useState(() => {
    const stored = localStorage.getItem('studysync_user');
    return stored ? JSON.parse(stored) : null;
  });

  const login = useCallback(async (credentials) => {
    const data = await authApi.login(credentials);
    localStorage.setItem('studysync_token', data.token);
    localStorage.setItem('studysync_user', JSON.stringify(data));
    setUser(data);
    return data;
  }, []);

  const register = useCallback(async (payload) => {
    const data = await authApi.register(payload);
    localStorage.setItem('studysync_token', data.token);
    localStorage.setItem('studysync_user', JSON.stringify(data));
    setUser(data);
    return data;
  }, []);

  const logout = useCallback(() => {
    localStorage.removeItem('studysync_token');
    localStorage.removeItem('studysync_user');
    setUser(null);
  }, []);

  return (
    <AuthContext.Provider value={{ user, login, register, logout, isAuthenticated: !!user }}>
      {children}
    </AuthContext.Provider>
  );
}

export function useAuth() {
  const ctx = useContext(AuthContext);
  if (!ctx) throw new Error('useAuth must be used within AuthProvider');
  return ctx;
}
