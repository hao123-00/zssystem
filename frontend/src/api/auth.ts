import request from '@/utils/request';

export interface LoginParams {
  username: string;
  password: string;
  rememberMe?: boolean;
}

export interface LoginResult {
  token: string;
  refreshToken: string;
  userId: number;
  username: string;
  realName: string;
}

export const login = (params: LoginParams) => {
  return request.post<LoginResult>('/auth/login', params);
};

export const logout = () => {
  return request.post('/auth/logout');
};

export const refreshToken = (refreshToken: string) => {
  return request.post<LoginResult>('/auth/refresh', null, {
    params: { refreshToken },
  });
};
