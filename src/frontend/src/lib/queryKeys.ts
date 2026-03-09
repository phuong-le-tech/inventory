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
    userDetail: (id: string) => ['admin', 'users', 'detail', id] as const,
    lists: (params: object) => ['admin', 'lists', params] as const,
    listDetail: (id: string) => ['admin', 'lists', 'detail', id] as const,
    stats: () => ['admin', 'stats'] as const,
    items: (params: object) => ['admin', 'items', params] as const,
  },
};
