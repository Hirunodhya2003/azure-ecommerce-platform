import { createContext, useContext, useState, useCallback, useMemo } from 'react';
import { authApi } from '../api/services.js';

const AuthContext = createContext(null);

const readUser = () => {
  try {
    return JSON.parse(localStorage.getItem('user'));
  } catch {
    return null;
  }
};

export function AuthProvider({ children }) {
  const [user, setUser] = useState(readUser);
  const [token, setToken] = useState(() => localStorage.getItem('token'));

  const login = useCallback(async (email, password) => {
    const data = await authApi.login({ email, password });
    const { token: jwt, ...profile } = data; // {id,name,email,role,active,token}
    localStorage.setItem('token', jwt);
    localStorage.setItem('user', JSON.stringify(profile));
    setToken(jwt);
    setUser(profile);
    return profile;
  }, []);

  const register = useCallback((payload) => authApi.register(payload), []);

  const logout = useCallback(() => {
    localStorage.removeItem('token');
    localStorage.removeItem('user');
    setToken(null);
    setUser(null);
  }, []);

  const value = useMemo(
    () => ({ user, token, isAuthenticated: !!token, isAdmin: user?.role === 'ADMIN', login, register, logout }),
    [user, token, login, register, logout]
  );
  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export const useAuth = () => useContext(AuthContext);
