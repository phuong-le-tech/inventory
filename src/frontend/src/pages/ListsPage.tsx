import { useState, useMemo } from 'react';
import { Link } from 'react-router-dom';
import { Plus, Pencil, Trash2, FolderOpen, Crown, Search } from 'lucide-react';
import { motion, AnimatePresence } from 'motion/react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { listsApi } from '../services/api';
import { useAuth } from '../contexts/AuthContext';
import { SkeletonCard, SkeletonText, Skeleton } from '../components/Skeleton';
import { useToast } from '../components/Toast';
import ConfirmModal from '../components/ConfirmModal';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Pagination } from '@/components/Pagination';
import { Badge } from '@/components/ui/badge';
import { BlurFade } from '@/components/effects/blur-fade';
import { SpotlightCard } from '@/components/effects/spotlight-card';
import { StaggeredList, StaggeredItem } from '@/components/effects/staggered-list';
import { queryKeys } from '../lib/queryKeys';

// Must match backend ItemListServiceImpl.MAX_FREE_LISTS
const FREE_LIST_LIMIT = 5;

export default function ListsPage() {
  const [pendingDeleteId, setPendingDeleteId] = useState<string | null>(null);
  const [page, setPage] = useState(0);
  const [searchQuery, setSearchQuery] = useState('');
  const { showToast } = useToast();
  const { isPremium } = useAuth();
  const queryClient = useQueryClient();

  const params = { page, size: 9, sortBy: 'createdAt', sortDir: 'desc' as const };
  const { data, isLoading: loading } = useQuery({
    queryKey: queryKeys.lists.list(params),
    queryFn: () => listsApi.getAll(params),
  });

  const lists = data?.content ?? [];
  const totalPages = data?.totalPages ?? 0;
  const totalElements = data?.totalElements ?? 0;

  const filteredLists = useMemo(() => {
    if (!searchQuery.trim()) return lists;
    const query = searchQuery.toLowerCase();
    return lists.filter((list) => {
      return (
        list.name.toLowerCase().includes(query) ||
        list.category?.toLowerCase().includes(query) ||
        list.description?.toLowerCase().includes(query)
      );
    });
  }, [lists, searchQuery]);

  const deleteMutation = useMutation({
    mutationFn: (id: string) => listsApi.delete(id),
    onSuccess: () => {
      showToast('Liste supprimée avec succès', 'success');
      queryClient.invalidateQueries({ queryKey: queryKeys.lists.all });
      queryClient.invalidateQueries({ queryKey: queryKeys.dashboard.all });
    },
    onError: () => {
      showToast('Échec de la suppression de la liste', 'error');
    },
  });

  const handleDeleteConfirm = () => {
    if (!pendingDeleteId) return;
    const id = pendingDeleteId;
    setPendingDeleteId(null);
    deleteMutation.mutate(id);
  };

  if (loading && lists.length === 0) {
    return (
      <div>
        <div className="flex justify-between items-center mb-8">
          <div className="space-y-2">
            <SkeletonText className="w-32 h-10" />
            <SkeletonText className="w-24 h-4" />
          </div>
          <Skeleton className="h-10 w-36 rounded-lg" />
        </div>
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
          {[...Array(6)].map((_, i) => (
            <SkeletonCard key={i} />
          ))}
        </div>
      </div>
    );
  }

  return (
    <div className="max-w-7xl mx-auto">
      <div className="flex justify-between items-start mb-8">
        <div>
          <BlurFade delay={0.1}>
            <h1 className="font-display text-4xl font-semibold tracking-tight">Mes Listes</h1>
          </BlurFade>
          <BlurFade delay={0.2}>
            <p className="text-muted-foreground mt-1">
              {searchQuery ? `${filteredLists.length} résultat${filteredLists.length > 1 ? 's' : ''}` : `${totalElements} listes au total`}
            </p>
          </BlurFade>
        </div>
        <BlurFade delay={0.2}>
          {!isPremium && totalElements >= FREE_LIST_LIMIT ? (
            <Button asChild>
              <Link to="/upgrade">
                <Crown className="h-4 w-4 mr-2" />
                Passer en Premium
              </Link>
            </Button>
          ) : (
            <Button asChild>
              <Link to="/lists/new">
                <Plus className="h-4 w-4 mr-2" />
                Nouvelle liste
              </Link>
            </Button>
          )}
        </BlurFade>
      </div>

      {/* Search input */}
      <BlurFade delay={0.3}>
        <div className="relative mb-6">
          <Search className="absolute left-3 top-1/2 -translate-y-1/2 h-4 w-4 text-muted-foreground pointer-events-none" />
          <Input
            type="search"
            placeholder="Rechercher par nom, catégorie ou description..."
            value={searchQuery}
            onChange={(e) => setSearchQuery(e.target.value)}
            className="pl-10"
          />
        </div>
      </BlurFade>

      {/* Upgrade banners for free users */}
      {!isPremium && totalElements === FREE_LIST_LIMIT - 1 && (
        <BlurFade delay={0.3}>
          <div className="rounded-xl border border-brand/30 bg-brand-light/50 p-4 mb-2 flex items-center justify-between">
            <p className="text-sm text-foreground">
              <span className="font-medium">1 liste restante</span> sur votre plan gratuit
            </p>
            <Button variant="outline" size="sm" asChild>
              <Link to="/upgrade">Voir les offres</Link>
            </Button>
          </div>
        </BlurFade>
      )}
      {!isPremium && totalElements >= FREE_LIST_LIMIT && (
        <BlurFade delay={0.3}>
          <div className="rounded-xl border-2 border-brand bg-brand-light/50 p-4 mb-2 flex items-center justify-between">
            <div>
              <p className="text-sm font-medium text-foreground">Limite atteinte</p>
              <p className="text-sm text-muted-foreground">
                Passez en Premium pour des listes illimitées — 2€ (paiement unique)
              </p>
            </div>
            <Button size="sm" asChild>
              <Link to="/upgrade">
                <Crown className="h-3.5 w-3.5 mr-1.5" />
                Passer en Premium
              </Link>
            </Button>
          </div>
        </BlurFade>
      )}

      <StaggeredList className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
        <AnimatePresence>
          {filteredLists.map((list) => (
            <StaggeredItem key={list.id}>
              <SpotlightCard className="group rounded-2xl border bg-card shadow-card transition-all duration-300 hover:shadow-elevated overflow-hidden">
                <Link to={`/lists/${list.id}`} className="block p-6">
                  <div className="flex items-start justify-between mb-4">
                    <div className="w-12 h-12 rounded-xl bg-brand-light flex items-center justify-center">
                      <span className="font-display text-xl font-bold text-foreground">
                        {list.name[0]?.toUpperCase()}
                      </span>
                    </div>
                    {list.category && (
                      <Badge variant="secondary">{list.category}</Badge>
                    )}
                  </div>
                  <h3 className="font-display text-lg font-semibold tracking-tight mb-1 group-hover:text-brand-dark transition-colors">
                    {list.name}
                  </h3>
                  {list.description && (
                    <p className="text-muted-foreground text-sm line-clamp-2 mb-3">{list.description}</p>
                  )}
                  <p className="text-muted-foreground text-sm">
                    <span className="font-semibold text-foreground">{list.itemCount || 0}</span> articles
                  </p>
                </Link>

                <motion.div
                  initial={{ opacity: 0, y: 10 }}
                  whileInView={{ opacity: 1, y: 0 }}
                  className="px-6 pb-6"
                >
                  <div className="flex gap-2">
                    <Button variant="outline" size="sm" className="w-full flex-1" asChild>
                      <Link to={`/lists/${list.id}/edit`}>
                        <Pencil className="h-3.5 w-3.5 mr-1.5" />
                        Modifier
                      </Link>
                    </Button>
                    <Button
                      variant="outline"
                      size="icon"
                      className="h-9 w-9 text-muted-foreground hover:text-destructive hover:border-destructive"
                      onClick={(e) => {
                        e.preventDefault();
                        setPendingDeleteId(list.id);
                      }}
                      disabled={deleteMutation.isPending && deleteMutation.variables === list.id}
                      aria-label={`Supprimer ${list.name}`}
                    >
                      <Trash2 className={`h-3.5 w-3.5 ${deleteMutation.isPending && deleteMutation.variables === list.id ? 'animate-pulse' : ''}`} />
                    </Button>
                  </div>
                </motion.div>
              </SpotlightCard>
            </StaggeredItem>
          ))}
        </AnimatePresence>
      </StaggeredList>

      {filteredLists.length === 0 && !loading && (
        <div className="text-center py-24 animate-fade-in relative">
          <div className="text-[12rem] font-display font-bold text-muted/30 leading-none select-none">0</div>
          <div className="-mt-16 relative z-10">
            {searchQuery ? (
              <>
                <Search className="h-12 w-12 mx-auto mb-3 text-muted-foreground/50" />
                <p className="text-lg text-muted-foreground mb-4">Aucun résultat pour "{searchQuery}"</p>
                <Button variant="outline" onClick={() => setSearchQuery('')}>
                  Effacer la recherche
                </Button>
              </>
            ) : (
              <>
                <FolderOpen className="h-12 w-12 mx-auto mb-3 text-muted-foreground/50" />
                <p className="text-lg text-muted-foreground mb-4">Aucune liste trouvée.</p>
                <Button asChild>
                  <Link to="/lists/new">Créer votre première liste</Link>
                </Button>
              </>
            )}
          </div>
        </div>
      )}

      <Pagination page={page} totalPages={totalPages} onPageChange={setPage} />

      <ConfirmModal
        isOpen={pendingDeleteId !== null}
        title="Supprimer la liste"
        message="Êtes-vous sûr de vouloir supprimer cette liste et tous ses articles ? Cette action est irréversible."
        confirmLabel="Supprimer"
        onConfirm={handleDeleteConfirm}
        onCancel={() => setPendingDeleteId(null)}
      />
    </div>
  );
}
