import { useAppSelector, useAppDispatch } from '@/store';
import { login, register, logout, fetchProfile, clearError } from '@/store/authSlice';
import type { LoginRequest, RegisterRequest } from '@/types';
import { useCallback } from 'react';

export function useAuth() {
  const dispatch = useAppDispatch();
  const { user, isAuthenticated, loading, error, token } = useAppSelector((state) => state.auth);

  const handleLogin = useCallback(
    (credentials: LoginRequest) => dispatch(login(credentials)),
    [dispatch],
  );

  const handleRegister = useCallback(
    (data: RegisterRequest) => dispatch(register(data)),
    [dispatch],
  );

  const handleLogout = useCallback(() => dispatch(logout()), [dispatch]);

  const handleFetchProfile = useCallback(() => dispatch(fetchProfile()), [dispatch]);

  const handleClearError = useCallback(() => dispatch(clearError()), [dispatch]);

  return {
    user,
    isAuthenticated,
    loading,
    error,
    token,
    login: handleLogin,
    register: handleRegister,
    logout: handleLogout,
    fetchProfile: handleFetchProfile,
    clearError: handleClearError,
  };
}

export default useAuth;
