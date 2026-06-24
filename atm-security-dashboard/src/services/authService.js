import axios from '../api/axiosConfig';

export const authService = {
  login: async (username, password) => {
    const response = await axios.post('/auth/login', { username, password });
    return response.data;
  },

  register: async (userData) => {
    const response = await axios.post('/auth/register', userData);
    return response.data;
  },

  getCurrentUser: async () => {
    const response = await axios.get('/auth/me');
    return response.data;
  },

  logout: () => {
    localStorage.removeItem('authToken');
    localStorage.removeItem('user');
  },

  getToken: () => {
    return localStorage.getItem('authToken');
  },

  isAuthenticated: () => {
    return !!localStorage.getItem('authToken');
  }
};