export const queryKeys = {
  dashboard: {
    all: ['dashboard', 'stats'] as const,
    stats: () => ['dashboard', 'stats'] as const,
  },
  lists: {
    all: ['lists'] as const,
    list: (params: object) => ['lists', 'list', params] as const,
    detail: (id: string) => ['lists', 'detail', id] as const,
  },
  items: {
    all: ['items'] as const,
    list: (params: object) => ['items', 'list', params] as const,
    detail: (id: string) => ['items', 'detail', id] as const,
  },
  admin: {
    users: (params: object) => ['admin', 'users', params] as const,
  },
};
