export interface Item {
  id: string;
  name: string;
  category: string;
  status: string;
  imageBase64?: string;
  contentType?: string;
  createdAt: string;
  updatedAt: string;
}

export interface ItemFormData {
  name: string;
  category: string;
  status: string;
}

export interface DashboardStats {
  totalItems: number;
  countByStatus: Record<string, number>;
  countByCategory: Record<string, number>;
}

export const STATUS_OPTIONS = ['In Stock', 'Low Stock', 'Out of Stock'] as const;
