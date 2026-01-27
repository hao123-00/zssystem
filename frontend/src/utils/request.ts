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
  async (error) => {
    if (error.response?.status === 401) {
      removeToken();
      window.location.href = '/login';
    }
    
    // 处理下载接口的错误响应
    if (error.config?.responseType === 'blob' && error.response?.data) {
      try {
        // 如果响应是Blob，尝试读取文本
        if (error.response.data instanceof Blob) {
          const text = await error.response.data.text();
          let errorMessage = '下载失败';
          try {
            // 尝试解析为JSON
            const json = JSON.parse(text);
            errorMessage = json.message || json.error || errorMessage;
          } catch {
            // 如果不是JSON，直接使用文本
            errorMessage = text || errorMessage;
          }
          return Promise.reject(new Error(errorMessage));
        } else if (typeof error.response.data === 'string') {
          // 如果已经是字符串，尝试解析为JSON
          try {
            const json = JSON.parse(error.response.data);
            return Promise.reject(new Error(json.message || json.error || '下载失败'));
          } catch {
            return Promise.reject(new Error(error.response.data || '下载失败'));
          }
        } else if (error.response.data && typeof error.response.data === 'object') {
          // 如果已经是对象，直接提取message
          return Promise.reject(new Error(error.response.data.message || error.response.data.error || '下载失败'));
        }
      } catch (e) {
        console.error('解析下载错误响应失败:', e);
        return Promise.reject(new Error('下载失败: ' + (error.message || '未知错误')));
      }
    }
    
    // 处理普通JSON错误响应
    if (error.response?.data) {
      const errorData = error.response.data;
      if (typeof errorData === 'object' && errorData.message) {
        return Promise.reject(new Error(errorData.message));
      } else if (typeof errorData === 'string') {
        return Promise.reject(new Error(errorData));
      }
    }
    
    return Promise.reject(error);
  }
);

export default request;
