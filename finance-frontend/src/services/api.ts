import axios from 'axios';

const api = axios.create({
  baseURL: '/api',
  timeout: 10000,
  headers: {
    'Content-Type': 'application/json',
  },
});

// Request interceptor
api.interceptors.request.use(
  (config) => {
    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

// Response interceptor - unwrap the data from Result wrapper
api.interceptors.response.use(
  (response) => {
    const { data } = response;
    if (data.code === 200) {
      // Return the actual data payload directly
      response.data = data.data;
      return response;
    }
    return Promise.reject(new Error(data.message || 'Request failed'));
  },
  (error) => {
    const message = error.response?.data?.message || error.message || 'Network error';
    return Promise.reject(new Error(message));
  }
);

export default api;
