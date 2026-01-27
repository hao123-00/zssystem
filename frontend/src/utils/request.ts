import axios, { InternalAxiosRequestConfig } from 'axios';
import { getToken, removeToken } from './auth';

const request = axios.create({
  baseURL: '/api',
  timeout: 10000,
});

// 请求拦截器
request.interceptors.request.use(
  (config: InternalAxiosRequestConfig) => {
    const token = getToken();
    if (token && config.headers) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

// 响应拦截器
request.interceptors.response.use(
  (response) => {
    // 如果是blob响应（文件下载），直接返回
    if (response.data instanceof Blob) {
      return response;
    }
    
    // 普通JSON响应，解析数据
    const { code, message, data } = response.data;
    if (code === 200) {
      return data;
    } else {
      return Promise.reject(new Error(message || '请求失败'));
    }
  },
  (error) => {
    if (error.response?.status === 401) {
      removeToken();
      window.location.href = '/login';
    }
    return Promise.reject(error);
  }
);

export default request;
