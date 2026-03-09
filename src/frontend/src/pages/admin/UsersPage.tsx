import { useState, useEffect } from 'react';
import { useForm, Controller } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { Plus, Trash2, Shield, User as UserIcon, AlertCircle } from 'lucide-react';
import { motion } from 'motion/react';
import { createUserSchema, CreateUserFormData } from '../../schemas/auth.schemas';
import { User } from '../../types/auth';
import { adminApi } from '../../services/authApi';
import { useQuery, useQueryClient } from '@tanstack/react-query';
import { queryKeys } from '../../lib/queryKeys';
import { useToast } from '../../components/Toast';
import ConfirmModal from '../../components/ConfirmModal';
import { getApiErrorStatus } from '../../utils/errorUtils';
import { Button } from '@/components/ui/button';
import { Pagination } from '@/components/Pagination';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { Badge } from '@/components/ui/badge';
import { Avatar, AvatarFallback, AvatarImage } from '@/components/ui/avatar';
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from '@/components/ui/table';
import { Tooltip, TooltipContent, TooltipProvider, TooltipTrigger } from '@/components/ui/tooltip';
import { Dialog, DialogContent, DialogHeader, DialogTitle, DialogFooter } from '@/components/ui/dialog';
import { BlurFade } from '@/components/effects/blur-fade';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select';

const PASTEL_COLORS = [
  'bg-brand',
  'bg-status-verify',
  'bg-status-pending',
  'bg-status-ready',
  'bg-status-prepare',
];

function getAvatarColor(email: string): string {
  let hash = 0;
  for (let i = 0; i < email.length; i++) {
    hash = email.charCodeAt(i) + ((hash << 5) - hash);
  }
  return PASTEL_COLORS[Math.abs(hash) % PASTEL_COLORS.length];
}

export function UsersPage() {
  const [showCreateModal, setShowCreateModal] = useState(false);
  const [pendingDeleteId, setPendingDeleteId] = useState<string | null>(null);
  const [page, setPage] = useState(0);
  const { showToast } = useToast();
  const queryClient = useQueryClient();

  const params = { page, size: 20 };
  const { data, isLoading: loading } = useQuery({
    queryKey: queryKeys.admin.users(params),
    queryFn: () => adminApi.getUsers(params),
  });

  const users = data?.content ?? [];
  const totalPages = data?.totalPages ?? 0;
  const totalElements = data?.totalElements ?? 0;

  const handleDeleteConfirm = async () => {
    if (!pendingDeleteId) return;
    const id = pendingDeleteId;
    setPendingDeleteId(null);
    try {
      await adminApi.deleteUser(id);
      showToast('Utilisateur supprimé', 'success');
      if (users.length === 1 && page > 0) {
        setPage(p => p - 1);
      }
      queryClient.invalidateQueries({ queryKey: ['admin', 'users'] });
    } catch {
      showToast("Échec de la suppression de l'utilisateur", 'error');
    }
  };

  const handleToggleRole = async (user: User) => {
    const newRole = user.role === 'ADMIN' ? 'USER' : 'ADMIN';
    try {
      await adminApi.updateUserRole(user.id, newRole);
      const label = newRole === 'ADMIN' ? 'Administrateur' : 'Utilisateur';
      showToast(`Rôle modifié en ${label}`, 'success');
      queryClient.invalidateQueries({ queryKey: ['admin', 'users'] });
    } catch {
      showToast('Échec de la mise à jour du rôle', 'error');
    }
  };

  if (loading) {
    return (
      <div className="animate-fade-in">
        <div className="flex items-center justify-between mb-8">
          <div>
            <div className="h-10 w-48 bg-muted rounded-lg animate-pulse mb-2" />
            <div className="h-5 w-72 bg-muted rounded-lg animate-pulse" />
          </div>
          <div className="h-10 w-44 bg-muted rounded-lg animate-pulse" />
        </div>
        <div className="rounded-xl border bg-card">
          <div className="border-b px-6 py-3 flex gap-4">
            <div className="h-4 w-32 bg-muted rounded animate-pulse" />
            <div className="h-4 w-16 bg-muted rounded animate-pulse" />
            <div className="h-4 w-20 bg-muted rounded animate-pulse" />
            <div className="h-4 w-16 bg-muted rounded animate-pulse ml-auto" />
          </div>
          {[...Array(5)].map((_, i) => (
            <div key={i} className="flex items-center gap-4 px-6 py-5 border-b last:border-0">
              <div className="h-10 w-10 rounded-full bg-muted animate-pulse" />
              <div className="h-4 w-48 bg-muted rounded animate-pulse" />
              <div className="h-6 w-28 bg-muted rounded-full animate-pulse ml-8" />
              <div className="h-4 w-24 bg-muted rounded animate-pulse ml-auto" />
              <div className="flex gap-1 ml-4">
                <div className="h-8 w-8 bg-muted rounded animate-pulse" />
                <div className="h-8 w-8 bg-muted rounded animate-pulse" />
              </div>
            </div>
          ))}
        </div>
      </div>
    );
  }

  return (
    <TooltipProvider>
      <div className="max-w-7xl mx-auto animate-fade-in">
        <div className="flex items-center justify-between mb-8">
          <div>
            <BlurFade>
              <h1 className="font-display text-4xl font-semibold tracking-tight mb-2">Utilisateurs</h1>
            </BlurFade>
            <BlurFade delay={0.1}>
              <p className="text-muted-foreground">
                Gérer les comptes utilisateurs et les permissions
                {totalElements > 0 && ` -- ${totalElements} au total`}
              </p>
            </BlurFade>
          </div>
          <Button onClick={() => setShowCreateModal(true)}>
            <Plus className="w-4 h-4 mr-2" />
            Ajouter un utilisateur
          </Button>
        </div>

        <div className="rounded-xl border bg-card">
          <Table>
            <TableHeader>
              <TableRow>
                <TableHead>Utilisateur</TableHead>
                <TableHead>Rôle</TableHead>
                <TableHead>Créé le</TableHead>
                <TableHead className="text-right">Actions</TableHead>
              </TableRow>
            </TableHeader>
            <TableBody>
              {users.map((user, index) => (
                <motion.tr
                  key={user.id}
                  initial={{ opacity: 0, y: 10 }}
                  animate={{ opacity: 1, y: 0 }}
                  transition={{ delay: index * 0.05, duration: 0.3 }}
                  className="border-b transition-colors hover:bg-muted/50"
                >
                  <TableCell className="py-5">
                    <div className="flex items-center gap-3">
                      <Avatar className="h-10 w-10">
                        {user.pictureUrl && <AvatarImage src={user.pictureUrl} alt="" />}
                        <AvatarFallback className={`${getAvatarColor(user.email)} text-foreground text-sm font-medium`}>
                          {user.email[0].toUpperCase()}
                        </AvatarFallback>
                      </Avatar>
                      <span className="font-medium">{user.email}</span>
                    </div>
                  </TableCell>
                  <TableCell>
                    <Badge variant={user.role === 'ADMIN' ? 'default' : 'secondary'}>
                      {user.role === 'ADMIN' ? (
                        <><Shield className="w-3 h-3 mr-1" />Administrateur</>
                      ) : (
                        <><UserIcon className="w-3 h-3 mr-1" />Utilisateur</>
                      )}
                    </Badge>
                  </TableCell>
                  <TableCell className="text-muted-foreground">
                    {new Date(user.createdAt).toLocaleDateString('fr-FR')}
                  </TableCell>
                  <TableCell>
                    <div className="flex items-center justify-end gap-1">
                      <Tooltip>
                        <TooltipTrigger asChild>
                          <Button
                            variant="ghost"
                            size="icon"
                            className="h-8 w-8"
                            onClick={() => handleToggleRole(user)}
                          >
                            <Shield className="w-4 h-4" />
                          </Button>
                        </TooltipTrigger>
                        <TooltipContent>
                          {user.role === 'ADMIN' ? 'Rétrograder en utilisateur' : 'Promouvoir en administrateur'}
                        </TooltipContent>
                      </Tooltip>
                      <Tooltip>
                        <TooltipTrigger asChild>
                          <Button
                            variant="ghost"
                            size="icon"
                            className="h-8 w-8 text-muted-foreground hover:text-destructive"
                            onClick={() => setPendingDeleteId(user.id)}
                          >
                            <Trash2 className="w-4 h-4" />
                          </Button>
                        </TooltipTrigger>
                        <TooltipContent>Supprimer l'utilisateur</TooltipContent>
                      </Tooltip>
                    </div>
                  </TableCell>
                </motion.tr>
              ))}
            </TableBody>
          </Table>

          {users.length === 0 && (
            <div className="text-center py-12 text-muted-foreground">
              Aucun utilisateur trouvé
            </div>
          )}
        </div>

        <Pagination page={page} totalPages={totalPages} onPageChange={setPage} />

        <ConfirmModal
          isOpen={pendingDeleteId !== null}
          title="Supprimer l'utilisateur"
          message="Êtes-vous sûr de vouloir supprimer cet utilisateur ? Cette action est irréversible."
          confirmLabel="Supprimer"
          onConfirm={handleDeleteConfirm}
          onCancel={() => setPendingDeleteId(null)}
        />

        <CreateUserModal
          open={showCreateModal}
          onClose={() => setShowCreateModal(false)}
          onCreated={() => {
            setShowCreateModal(false);
            showToast('Utilisateur créé avec succès', 'success');
            queryClient.invalidateQueries({ queryKey: ['admin', 'users'] });
          }}
        />
      </div>
    </TooltipProvider>
  );
}

function CreateUserModal({ open, onClose, onCreated }: { open: boolean; onClose: () => void; onCreated: (user: User) => void }) {
  const [loading, setLoading] = useState(false);
  const [serverError, setServerError] = useState('');

  const {
    register,
    handleSubmit,
    control,
    formState: { errors },
    reset,
  } = useForm<CreateUserFormData>({
    resolver: zodResolver(createUserSchema),
    defaultValues: { role: 'USER' },
  });

  useEffect(() => {
    if (open) {
      reset({ email: '', password: '', role: 'USER' });
      setServerError('');
    }
  }, [open, reset]);

  const onSubmit = async (data: CreateUserFormData) => {
    setLoading(true);
    setServerError('');
    try {
      const user = await adminApi.createUser(data);
      onCreated(user);
    } catch (err: unknown) {
      const status = getApiErrorStatus(err);
      setServerError(
        status === 409
          ? 'Un utilisateur avec cet email existe déjà'
          : "Échec de la création de l'utilisateur"
      );
    } finally {
      setLoading(false);
    }
  };

  return (
    <Dialog open={open} onOpenChange={(o) => !o && onClose()}>
      <DialogContent>
        <DialogHeader>
          <DialogTitle>Créer un utilisateur</DialogTitle>
        </DialogHeader>

        <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">
          {serverError && (
            <div className="bg-destructive/10 border border-destructive/20 rounded-lg px-4 py-3 text-destructive text-sm">
              {serverError}
            </div>
          )}

          <div className="space-y-2">
            <Label htmlFor="create-user-email">Email</Label>
            <Input
              id="create-user-email"
              type="email"
              {...register('email')}
              className={errors.email ? 'border-destructive' : ''}
              placeholder="utilisateur@exemple.com"
              aria-invalid={!!errors.email}
              aria-describedby={errors.email ? 'create-user-email-error' : undefined}
            />
            {errors.email && (
              <p id="create-user-email-error" role="alert" className="text-sm text-destructive flex items-center gap-1.5">
                <AlertCircle className="h-4 w-4 flex-shrink-0" />
                {errors.email.message}
              </p>
            )}
          </div>

          <div className="space-y-2">
            <Label htmlFor="create-user-password">Mot de passe</Label>
            <Input
              id="create-user-password"
              type="password"
              {...register('password')}
              className={errors.password ? 'border-destructive' : ''}
              placeholder="Minimum 6 caractères"
              aria-invalid={!!errors.password}
              aria-describedby={errors.password ? 'create-user-password-error' : undefined}
            />
            {errors.password && (
              <p id="create-user-password-error" role="alert" className="text-sm text-destructive flex items-center gap-1.5">
                <AlertCircle className="h-4 w-4 flex-shrink-0" />
                {errors.password.message}
              </p>
            )}
          </div>

          <div className="space-y-2">
            <Label htmlFor="create-user-role">Rôle</Label>
            <Controller
              name="role"
              control={control}
              render={({ field }) => (
                <Select value={field.value} onValueChange={field.onChange}>
                  <SelectTrigger id="create-user-role">
                    <SelectValue />
                  </SelectTrigger>
                  <SelectContent>
                    <SelectItem value="USER">Utilisateur</SelectItem>
                    <SelectItem value="ADMIN">Administrateur</SelectItem>
                  </SelectContent>
                </Select>
              )}
            />
          </div>

          <DialogFooter className="gap-2 sm:gap-0 pt-4">
            <Button type="button" variant="outline" onClick={onClose}>
              Annuler
            </Button>
            <Button type="submit" disabled={loading}>
              {loading ? 'Création...' : 'Créer'}
            </Button>
          </DialogFooter>
        </form>
      </DialogContent>
    </Dialog>
  );
}
