import axios from 'axios';
import type { LoginRequest, LoginResponse, ApiResponse } from '../types';

const api = axios.create({
  baseURL: '/api',
  timeout: 10000,
});

api.interceptors.request.use((config) => {
  const token = localStorage.getItem('token');
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

api.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      localStorage.removeItem('token');
      localStorage.removeItem('user');
      window.location.href = '/login';
    }
    return Promise.reject(error);
  }
);

export const authApi = {
  login: (data: LoginRequest) =>
    api.post<ApiResponse<LoginResponse>>('/auth/login', data).then((res) => res.data),
  getMe: () =>
    api.get<ApiResponse<any>>('/auth/me').then((res) => res.data),
};

export const shiftApi = {
  create: (data: any) =>
    api.post<ApiResponse<any>>('/shifts', data).then((res) => res.data),
  getById: (id: number) =>
    api.get<ApiResponse<any>>(`/shifts/${id}`).then((res) => res.data),
  list: (params?: any) =>
    api.get<ApiResponse<any[]>>('/shifts', { params }).then((res) => res.data),
  getMyActive: () =>
    api.get<ApiResponse<any[]>>('/shifts/my-active').then((res) => res.data),
  updateStatus: (id: number, status: string) =>
    api.put<ApiResponse<any>>(`/shifts/${id}/status`, null, { params: { status } }).then((res) => res.data),
  close: (id: number) =>
    api.post<ApiResponse<any>>(`/shifts/${id}/close`).then((res) => res.data),
};

export const applicationApi = {
  create: (data: any) =>
    api.post<ApiResponse<any>>('/applications', data).then((res) => res.data),
  getById: (id: number) =>
    api.get<ApiResponse<any>>(`/applications/${id}`).then((res) => res.data),
  list: (params?: any) =>
    api.get<ApiResponse<any[]>>('/applications', { params }).then((res) => res.data),
  needReview: () =>
    api.get<ApiResponse<any[]>>('/applications/need-review').then((res) => res.data),
  review: (data: any) =>
    api.post<ApiResponse<any>>('/applications/review', data).then((res) => res.data),
};

export const outboundApi = {
  create: (data: any) =>
    api.post<ApiResponse<any>>('/outbound', data).then((res) => res.data),
  list: (params?: any) =>
    api.get<ApiResponse<any[]>>('/outbound', { params }).then((res) => res.data),
};

export const inboundApi = {
  create: (data: any) =>
    api.post<ApiResponse<any>>('/inbound', data).then((res) => res.data),
  list: (params?: any) =>
    api.get<ApiResponse<any[]>>('/inbound', { params }).then((res) => res.data),
};

export const verificationApi = {
  create: (data: any) =>
    api.post<ApiResponse<any>>('/verification', data).then((res) => res.data),
  list: (params?: any) =>
    api.get<ApiResponse<any[]>>('/verification', { params }).then((res) => res.data),
};

export const anomalyApi = {
  create: (data: any) =>
    api.post<ApiResponse<any>>('/anomalies', data).then((res) => res.data),
  list: (params?: any) =>
    api.get<ApiResponse<any[]>>('/anomalies', { params }).then((res) => res.data),
  resolve: (id: number, result: string) =>
    api.post<ApiResponse<any>>(`/anomalies/${id}/resolve`, null, { params: { result } }).then((res) => res.data),
};

export const explosiveApi = {
  list: (params?: any) =>
    api.get<ApiResponse<any[]>>('/explosives', { params }).then((res) => res.data),
  getStock: () =>
    api.get<ApiResponse<any>>('/explosives/stock').then((res) => res.data),
};

export const workPlanApi = {
  list: () =>
    api.get<ApiResponse<any[]>>('/work-plans').then((res) => res.data),
  getById: (id: number) =>
    api.get<ApiResponse<any>>(`/work-plans/${id}`).then((res) => res.data),
};

export default api;
