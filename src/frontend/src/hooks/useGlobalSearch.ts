import { useState, useEffect, useRef } from 'react';
import { useDebounce } from './useDebounce';
import { itemsApi, listsApi } from '../services/api';
import type { Item, ItemList } from '../types/item';

interface GlobalSearchResult {
  items: Item[];
  lists: ItemList[];
  isLoading: boolean;
}

export function useGlobalSearch(query: string): GlobalSearchResult {
  const [items, setItems] = useState<Item[]>([]);
  const [lists, setLists] = useState<ItemList[]>([]);
  const [isLoading, setIsLoading] = useState(false);
  const abortRef = useRef<AbortController | null>(null);
  const debouncedQuery = useDebounce(query.trim(), 300);

  useEffect(() => {
    if (!debouncedQuery) {
      setItems([]);
      setLists([]);
      setIsLoading(false);
      return;
    }

    abortRef.current?.abort();
    const controller = new AbortController();
    abortRef.current = controller;

    setIsLoading(true);

    Promise.all([
      itemsApi.getAll({ search: debouncedQuery, size: 5 }, controller.signal),
      listsApi.getAll({ search: debouncedQuery, size: 5 }, controller.signal),
    ])
      .then(([itemsRes, listsRes]) => {
        setItems(itemsRes.content);
        setLists(listsRes.content);
        setIsLoading(false);
      })
      .catch((err) => {
        if (err.name !== 'AbortError' && err.name !== 'CanceledError') {
          setItems([]);
          setLists([]);
          setIsLoading(false);
        }
      });

    return () => {
      controller.abort();
    };
  }, [debouncedQuery]);

  return { items, lists, isLoading };
}
