import axios from 'axios';
import {
  Item,
  ItemFormData,
  DashboardStats,
  PageResponse,
  ItemSearchParams,
  ItemList,
  ItemListWithItems,
  ItemListFormData,
  ItemListSearchParams
} from '../types/item';

const api = axios.create({
  baseURL: '/api/v1',
  withCredentials: true,
});

// Unwrap Google JSON Style Guide response envelope and redirect on 401
api.interceptors.response.use(
  (response) => {
    if (response.data && typeof response.data === 'object' && 'data' in response.data) {
      response.data = response.data.data;
    }
    return response;
  },
  (error) => {
    if (error.response?.status === 401 && !window.location.pathname.includes('/login')) {
      window.location.href = '/login';
    }
    return Promise.reject(error);
  }
);

export const listsApi = {
  getAll: async (params: ItemListSearchParams = {}): Promise<PageResponse<ItemList>> => {
    const response = await api.get<PageResponse<ItemList>>('/lists', { params });
    return response.data;
  },

  getById: async (id: string): Promise<ItemListWithItems> => {
    const response = await api.get<ItemListWithItems>(`/lists/${id}`);
    return response.data;
  },

  create: async (data: ItemListFormData): Promise<ItemList> => {
    const response = await api.post<ItemList>('/lists', data);
    return response.data;
  },

  update: async (id: string, data: ItemListFormData): Promise<ItemList> => {
    const response = await api.patch<ItemList>(`/lists/${id}`, data);
    return response.data;
  },

  delete: async (id: string): Promise<void> => {
    await api.delete(`/lists/${id}`);
  },
};

export const itemsApi = {
  getAll: async (params: ItemSearchParams = {}): Promise<PageResponse<Item>> => {
    const response = await api.get<PageResponse<Item>>('/items', { params });
    return response.data;
  },

  getById: async (id: string): Promise<Item> => {
    const response = await api.get<Item>(`/items/${id}`);
    return response.data;
  },

  create: async (data: ItemFormData, image?: File): Promise<Item> => {
    const formData = new FormData();
    formData.append('data', JSON.stringify(data));
    if (image) {
      formData.append('image', image);
    }
    const response = await api.post<Item>('/items', formData);
    return response.data;
  },

  update: async (id: string, data: ItemFormData, image?: File): Promise<Item> => {
    const formData = new FormData();
    formData.append('data', JSON.stringify(data));
    if (image) {
      formData.append('image', image);
    }
    const response = await api.patch<Item>(`/items/${id}`, formData);
    return response.data;
  },

  delete: async (id: string): Promise<void> => {
    await api.delete(`/items/${id}`);
  },
};

export const dashboardApi = {
  getStats: async (): Promise<DashboardStats> => {
    const response = await api.get<DashboardStats>('/items/stats');
    return response.data;
  },
};
