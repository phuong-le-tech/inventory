import { useState, useCallback } from 'react';
import { useNavigate } from 'react-router-dom';
import { Search, Layers, X } from 'lucide-react';
import { motion } from 'motion/react';
import { adminApi } from '../../services/authApi';
import { useQuery } from '@tanstack/react-query';
import { queryKeys } from '../../lib/queryKeys';
import { Button } from '@/components/ui/button';
import { Pagination } from '@/components/Pagination';
import { Input } from '@/components/ui/input';
import { Badge } from '@/components/ui/badge';
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from '@/components/ui/table';
import { BlurFade } from '@/components/effects/blur-fade';
import { useDebounce } from '../../hooks/useDebounce';

export function AdminListsPage() {
  const navigate = useNavigate();
  const [page, setPage] = useState(0);
  const [searchInput, setSearchInput] = useState('');
  const debouncedSearch = useDebounce(searchInput, 300);

  const handleSearchChange = useCallback((value: string) => {
    setSearchInput(value);
    setPage(0);
  }, []);

  const hasActiveFilters = !!debouncedSearch;

  const clearFilters = () => {
    setSearchInput('');
    setPage(0);
  };

  const params = {
    page,
    size: 20,
    ...(debouncedSearch && { search: debouncedSearch }),
  };

  const { data, isLoading: loading } = useQuery({
    queryKey: queryKeys.admin.lists(params),
    queryFn: ({ signal }) => adminApi.getLists(params, signal),
  });

  const lists = data?.content ?? [];
  const totalPages = data?.totalPages ?? 0;
  const totalElements = data?.totalElements ?? 0;

  if (loading) {
    return (
      <div className="animate-fade-in">
        <div className="mb-8">
          <div className="h-10 w-48 bg-muted rounded-lg animate-pulse mb-2" />
          <div className="h-5 w-72 bg-muted rounded-lg animate-pulse" />
        </div>
        <div className="flex gap-3 mb-6">
          <div className="h-10 flex-1 max-w-sm bg-muted rounded-lg animate-pulse" />
        </div>
        <div className="rounded-xl border bg-card">
          <div className="border-b px-6 py-3 flex gap-4">
            <div className="h-4 w-32 bg-muted rounded animate-pulse" />
            <div className="h-4 w-20 bg-muted rounded animate-pulse" />
            <div className="h-4 w-32 bg-muted rounded animate-pulse" />
            <div className="h-4 w-16 bg-muted rounded animate-pulse" />
            <div className="h-4 w-20 bg-muted rounded animate-pulse" />
          </div>
          {[...Array(5)].map((_, i) => (
            <div key={i} className="flex items-center gap-4 px-6 py-5 border-b last:border-0">
              <div className="h-4 w-40 bg-muted rounded animate-pulse" />
              <div className="h-6 w-20 bg-muted rounded-full animate-pulse ml-4" />
              <div className="h-4 w-36 bg-muted rounded animate-pulse ml-4" />
              <div className="h-4 w-10 bg-muted rounded animate-pulse ml-auto" />
              <div className="h-4 w-24 bg-muted rounded animate-pulse ml-4" />
            </div>
          ))}
        </div>
      </div>
    );
  }

  return (
    <div className="max-w-7xl mx-auto animate-fade-in">
      <div className="mb-8">
        <BlurFade>
          <h1 className="font-display text-4xl font-semibold tracking-tight mb-2">Contenu</h1>
        </BlurFade>
        <BlurFade delay={0.1}>
          <p className="text-muted-foreground">
            Parcourir les listes et articles de tous les utilisateurs
            {totalElements > 0 && ` — ${totalElements} liste${totalElements !== 1 ? 's' : ''} au total`}
          </p>
        </BlurFade>
      </div>

      {/* Search */}
      <div className="flex flex-col sm:flex-row gap-3 mb-6">
        <div className="relative flex-1 max-w-sm">
          <Search className="absolute left-3 top-1/2 -translate-y-1/2 h-4 w-4 text-muted-foreground" />
          <Input
            placeholder="Rechercher par nom..."
            value={searchInput}
            onChange={(e) => handleSearchChange(e.target.value)}
            className="pl-9"
          />
        </div>
        {hasActiveFilters && (
          <Button variant="ghost" size="sm" onClick={clearFilters} className="h-10 px-3">
            <X className="w-4 h-4 mr-1" />
            Effacer
          </Button>
        )}
      </div>

      <div className="rounded-xl border bg-card">
        <Table>
          <TableHeader>
            <TableRow>
              <TableHead>Nom</TableHead>
              <TableHead>Catégorie</TableHead>
              <TableHead>Propriétaire</TableHead>
              <TableHead className="text-right">Articles</TableHead>
              <TableHead>Créé le</TableHead>
            </TableRow>
          </TableHeader>
          <TableBody>
            {lists.map((list, index) => (
              <motion.tr
                key={list.id}
                initial={{ opacity: 0, y: 10 }}
                animate={{ opacity: 1, y: 0 }}
                transition={{ delay: index * 0.05, duration: 0.3 }}
                className="border-b transition-colors hover:bg-muted/50 cursor-pointer"
                onClick={() => navigate(`/admin/lists/${list.id}`, { state: { ownerEmail: list.ownerEmail } })}
              >
                <TableCell className="py-5 font-medium">{list.name}</TableCell>
                <TableCell>
                  {list.category ? (
                    <Badge variant="secondary">{list.category}</Badge>
                  ) : (
                    <span className="text-muted-foreground">—</span>
                  )}
                </TableCell>
                <TableCell className="text-muted-foreground">{list.ownerEmail}</TableCell>
                <TableCell className="text-right tabular-nums">{list.itemCount}</TableCell>
                <TableCell className="text-muted-foreground">
                  {new Date(list.createdAt).toLocaleDateString('fr-FR')}
                </TableCell>
              </motion.tr>
            ))}
          </TableBody>
        </Table>

        {lists.length === 0 && (
          <div className="text-center py-12 text-muted-foreground">
            <Layers className="h-10 w-10 mx-auto mb-3 text-muted-foreground/40" />
            <p>{hasActiveFilters ? 'Aucun résultat pour cette recherche' : 'Aucune liste trouvée'}</p>
          </div>
        )}
      </div>

      <Pagination page={page} totalPages={totalPages} onPageChange={setPage} />
    </div>
  );
}
