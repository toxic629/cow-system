import { create } from 'zustand';

type AuthState = {
  token: string;
  user: string;
  setAuth: (token: string, user?: string) => void;
  clearAuth: () => void;
};

const TOKEN_KEY = 'cow_token';
const USER_KEY = 'cow_user';

export const useAuthStore = create<AuthState>((set) => ({
  token: localStorage.getItem(TOKEN_KEY) || '',
  user: localStorage.getItem(USER_KEY) || '',
  setAuth: (token, user = 'admin') => {
    localStorage.setItem(TOKEN_KEY, token);
    localStorage.setItem(USER_KEY, user);
    set({ token, user });
  },
  clearAuth: () => {
    localStorage.removeItem(TOKEN_KEY);
    localStorage.removeItem(USER_KEY);
    set({ token: '', user: '' });
  }
}));

export function getAuthToken() {
  return useAuthStore.getState().token;
}
