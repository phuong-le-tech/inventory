import { useState, useEffect, useRef, useCallback } from 'react';
import { useNavigate } from 'react-router-dom';
import { Search, List, Package, Clock, X } from 'lucide-react';
import * as DialogPrimitive from '@radix-ui/react-dialog';
import { cn } from '@/lib/utils';
import { useGlobalSearch } from '../hooks/useGlobalSearch';
import { Skeleton } from './ui/skeleton';

const RECENT_SEARCHES_KEY = 'shelfio_recent_searches';
const MAX_RECENT = 5;

function getRecentSearches(): string[] {
  try {
    const stored = localStorage.getItem(RECENT_SEARCHES_KEY);
    const parsed = stored ? JSON.parse(stored) : [];
    return Array.isArray(parsed)
      ? parsed.filter((s): s is string => typeof s === 'string')
      : [];
  } catch {
    return [];
  }
}

function saveRecentSearch(query: string) {
  const recent = getRecentSearches().filter((s) => s !== query);
  recent.unshift(query);
  localStorage.setItem(
    RECENT_SEARCHES_KEY,
    JSON.stringify(recent.slice(0, MAX_RECENT))
  );
}

function clearRecentSearches() {
  localStorage.removeItem(RECENT_SEARCHES_KEY);
}

interface GlobalSearchProps {
  open: boolean;
  onOpenChange: (open: boolean) => void;
}

export function GlobalSearch({ open, onOpenChange }: GlobalSearchProps) {
  const [query, setQuery] = useState('');
  const [recentSearches, setRecentSearches] = useState<string[]>([]);
  const [selectedIndex, setSelectedIndex] = useState(-1);
  const { items, lists, isLoading } = useGlobalSearch(query);
  const inputRef = useRef<HTMLInputElement>(null);
  const navigate = useNavigate();

  const hasQuery = query.trim().length > 0;
  const hasResults = lists.length > 0 || items.length > 0;
  const allResults = [
    ...lists.map((l) => ({ type: 'list' as const, id: l.id, data: l })),
    ...items.map((i) => ({ type: 'item' as const, id: i.id, data: i })),
  ];

  useEffect(() => {
    if (open) {
      setQuery('');
      setSelectedIndex(-1);
      setRecentSearches(getRecentSearches());
    }
  }, [open]);

  useEffect(() => {
    setSelectedIndex(-1);
  }, [lists, items]);

  useEffect(() => {
    if (isLoading) setSelectedIndex(-1);
  }, [isLoading]);

  const handleSelect = useCallback(
    (type: 'list' | 'item', id: string, itemListId?: string) => {
      if (hasQuery) {
        saveRecentSearch(query.trim());
      }
      onOpenChange(false);
      if (type === 'list') {
        navigate(`/lists/${id}`);
      } else {
        navigate(`/lists/${itemListId}`);
      }
    },
    [navigate, onOpenChange, query, hasQuery]
  );

  const handleRecentClick = (search: string) => {
    setQuery(search);
  };

  const handleClearRecent = () => {
    clearRecentSearches();
    setRecentSearches([]);
  };

  const handleKeyDown = (e: React.KeyboardEvent) => {
    if (e.key === 'ArrowDown') {
      e.preventDefault();
      setSelectedIndex((prev) =>
        prev < allResults.length - 1 ? prev + 1 : 0
      );
    } else if (e.key === 'ArrowUp') {
      e.preventDefault();
      setSelectedIndex((prev) =>
        prev > 0 ? prev - 1 : allResults.length - 1
      );
    } else if (e.key === 'Enter' && selectedIndex >= 0) {
      e.preventDefault();
      const selected = allResults[selectedIndex];
      if (selected) {
        handleSelect(
          selected.type,
          selected.id,
          selected.type === 'item' ? selected.data.itemListId : undefined
        );
      }
    }
  };

  return (
    <DialogPrimitive.Root open={open} onOpenChange={onOpenChange}>
      <DialogPrimitive.Portal>
        <DialogPrimitive.Overlay className="fixed inset-0 z-50 bg-black/40 backdrop-blur-sm data-[state=open]:animate-in data-[state=closed]:animate-out data-[state=closed]:fade-out-0 data-[state=open]:fade-in-0" />
        <DialogPrimitive.Content
          className="fixed left-[50%] top-[20%] z-50 w-full max-w-lg translate-x-[-50%] border bg-background shadow-float sm:rounded-xl overflow-hidden data-[state=open]:animate-in data-[state=closed]:animate-out data-[state=closed]:fade-out-0 data-[state=open]:fade-in-0 data-[state=closed]:zoom-out-95 data-[state=open]:zoom-in-95 data-[state=closed]:slide-out-to-left-1/2 data-[state=open]:slide-in-from-left-1/2"
          onKeyDown={handleKeyDown}
          onOpenAutoFocus={(e) => {
            e.preventDefault();
            inputRef.current?.focus();
          }}
        >
          <DialogPrimitive.Title className="sr-only">
            Recherche globale
          </DialogPrimitive.Title>
          {/* Search input */}
          <div className="flex items-center gap-3 px-4 border-b">
            <Search className="h-4 w-4 text-muted-foreground flex-shrink-0" />
            <input
              ref={inputRef}
              type="text"
              value={query}
              onChange={(e) => setQuery(e.target.value)}
              placeholder="Rechercher des listes et articles..."
              className="flex-1 py-3.5 text-sm bg-transparent outline-none placeholder:text-muted-foreground"
            />
            {query && (
              <button
                type="button"
                aria-label="Effacer la recherche"
                onClick={() => setQuery('')}
                className="p-1 text-muted-foreground hover:text-foreground transition-colors"
              >
                <X className="h-3.5 w-3.5" />
              </button>
            )}
          </div>

          {/* Results area */}
          <div className="max-h-[320px] overflow-y-auto">
            {/* Loading state */}
            {isLoading && hasQuery && (
              <div className="p-3 space-y-2">
                {[...Array(3)].map((_, i) => (
                  <Skeleton key={i} className="h-10 w-full" />
                ))}
              </div>
            )}

            {/* Results */}
            {!isLoading && hasQuery && hasResults && (
              <div className="py-1.5">
                {lists.length > 0 && (
                  <div>
                    <div className="px-4 py-1.5 text-[11px] font-semibold tracking-wide text-muted-foreground uppercase">
                      Listes
                    </div>
                    {lists.map((list, i) => (
                        <button
                          key={list.id}
                          role="option"
                          aria-selected={selectedIndex === i}
                          onClick={() => handleSelect('list', list.id)}
                          className={cn(
                            'flex items-center gap-3 w-full px-4 py-2.5 text-sm text-left transition-colors',
                            selectedIndex === i
                              ? 'bg-accent text-accent-foreground'
                              : 'hover:bg-muted/50'
                          )}
                        >
                          <List className="h-4 w-4 text-muted-foreground flex-shrink-0" />
                          <div className="min-w-0 flex-1">
                            <div className="font-medium truncate">
                              {list.name}
                            </div>
                            {list.category && (
                              <div className="text-xs text-muted-foreground truncate">
                                {list.category}
                              </div>
                            )}
                          </div>
                        </button>
                    ))}
                  </div>
                )}

                {items.length > 0 && (
                  <div>
                    <div className="px-4 py-1.5 text-[11px] font-semibold tracking-wide text-muted-foreground uppercase">
                      Articles
                    </div>
                    {items.map((item, i) => (
                        <button
                          key={item.id}
                          role="option"
                          aria-selected={selectedIndex === lists.length + i}
                          onClick={() =>
                            handleSelect('item', item.id, item.itemListId)
                          }
                          className={cn(
                            'flex items-center gap-3 w-full px-4 py-2.5 text-sm text-left transition-colors',
                            selectedIndex === lists.length + i
                              ? 'bg-accent text-accent-foreground'
                              : 'hover:bg-muted/50'
                          )}
                        >
                          <Package className="h-4 w-4 text-muted-foreground flex-shrink-0" />
                          <div className="min-w-0 flex-1">
                            <div className="font-medium truncate">
                              {item.name}
                            </div>
                            {item.barcode && (
                              <div className="text-xs text-muted-foreground truncate">
                                {item.barcode}
                              </div>
                            )}
                          </div>
                        </button>
                    ))}
                  </div>
                )}
              </div>
            )}

            {/* No results */}
            {!isLoading && hasQuery && !hasResults && (
              <div className="px-4 py-8 text-center text-sm text-muted-foreground">
                Aucun resultat pour &laquo;{query.trim()}&raquo;
              </div>
            )}

            {/* Recent searches (when no query) */}
            {!hasQuery && recentSearches.length > 0 && (
              <div className="py-1.5">
                <div className="flex items-center justify-between px-4 py-1.5">
                  <span className="text-[11px] font-semibold tracking-wide text-muted-foreground uppercase">
                    Recherches recentes
                  </span>
                  <button
                    onClick={handleClearRecent}
                    className="text-[11px] text-muted-foreground hover:text-foreground transition-colors"
                  >
                    Effacer
                  </button>
                </div>
                {recentSearches.map((search) => (
                  <button
                    key={search}
                    onClick={() => handleRecentClick(search)}
                    className="flex items-center gap-3 w-full px-4 py-2.5 text-sm text-left hover:bg-muted/50 transition-colors"
                  >
                    <Clock className="h-4 w-4 text-muted-foreground flex-shrink-0" />
                    <span className="truncate">{search}</span>
                  </button>
                ))}
              </div>
            )}

            {/* Empty state (no query, no recent) */}
            {!hasQuery && recentSearches.length === 0 && (
              <div className="px-4 py-8 text-center text-sm text-muted-foreground">
                Commencez a taper pour rechercher...
              </div>
            )}
          </div>

          {/* Footer with keyboard hint */}
          <div className="flex items-center gap-4 px-4 py-2 border-t text-[11px] text-muted-foreground">
            <span>
              <kbd className="px-1.5 py-0.5 rounded bg-muted font-mono text-[10px]">
                &uarr;&darr;
              </kbd>{' '}
              naviguer
            </span>
            <span>
              <kbd className="px-1.5 py-0.5 rounded bg-muted font-mono text-[10px]">
                &crarr;
              </kbd>{' '}
              ouvrir
            </span>
            <span>
              <kbd className="px-1.5 py-0.5 rounded bg-muted font-mono text-[10px]">
                esc
              </kbd>{' '}
              fermer
            </span>
          </div>
        </DialogPrimitive.Content>
      </DialogPrimitive.Portal>
    </DialogPrimitive.Root>
  );
}

const IS_MAC = typeof navigator !== 'undefined' && /Mac|iPhone|iPad/.test(navigator.userAgent);

export function SearchTrigger({
  onClick,
}: {
  onClick: () => void;
}) {
  return (
    <button
      onClick={onClick}
      className="flex items-center gap-2 w-full max-w-xs px-3 py-2 text-sm text-muted-foreground bg-muted/50 border rounded-lg hover:bg-muted/80 transition-colors"
    >
      <Search className="h-4 w-4" />
      <span className="flex-1 text-left">Rechercher...</span>
      <kbd className="hidden sm:inline-flex px-1.5 py-0.5 rounded bg-background border text-[10px] font-mono">
        {IS_MAC ? '⌘' : 'Ctrl'}K
      </kbd>
    </button>
  );
}
