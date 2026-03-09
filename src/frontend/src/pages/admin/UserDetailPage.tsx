import { useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { useQuery, useQueryClient } from '@tanstack/react-query';
import { ArrowLeft, List, Package, CreditCard, Mail, KeyRound } from 'lucide-react';
import { adminApi } from '../../services/authApi';
import { queryKeys } from '../../lib/queryKeys';
import { Role } from '../../types/auth';
import { useToast } from '../../components/Toast';
import ConfirmModal from '../../components/ConfirmModal';
import { Button } from '@/components/ui/button';
import { Badge } from '@/components/ui/badge';
import { Avatar, AvatarFallback, AvatarImage } from '@/components/ui/avatar';
import { Separator } from '@/components/ui/separator';
import { Breadcrumb } from '../../components/Breadcrumb';
import { BlurFade } from '@/components/effects/blur-fade';
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '@/components/ui/select';
import { getAvatarColor } from '../../utils/avatarUtils';
import { RoleBadge } from '../../components/RoleBadge';

export function UserDetailPage() {
  const { id } = useParams();
  const navigate = useNavigate();
  const { showToast } = useToast();
  const queryClient = useQueryClient();
  const [pendingAction, setPendingAction] = useState<'disable' | 'enable' | 'delete' | null>(null);

  const { data: user, isLoading, error } = useQuery({
    queryKey: queryKeys.admin.userDetail(id!),
    queryFn: () => adminApi.getUser(id!),
    enabled: !!id,
  });

  if (isLoading) {
    return (
      <div className="max-w-4xl mx-auto animate-fade-in">
        <div className="h-5 w-48 bg-muted rounded animate-pulse mb-8" />
        <div className="flex items-start gap-6 mb-8">
          <div className="h-20 w-20 rounded-full bg-muted animate-pulse" />
          <div className="flex-1 space-y-3">
            <div className="h-8 w-64 bg-muted rounded-lg animate-pulse" />
            <div className="h-5 w-32 bg-muted rounded animate-pulse" />
          </div>
        </div>
        <div className="grid grid-cols-2 sm:grid-cols-4 gap-4 mb-8">
          {[...Array(4)].map((_, i) => (
            <div key={i} className="rounded-xl border bg-card p-5">
              <div className="h-4 w-16 bg-muted rounded animate-pulse mb-3" />
              <div className="h-8 w-12 bg-muted rounded animate-pulse" />
            </div>
          ))}
        </div>
      </div>
    );
  }

  if (error || !user) {
    return (
      <div className="max-w-4xl mx-auto">
        <Button variant="ghost" onClick={() => navigate('/admin/users')} className="mb-4">
          <ArrowLeft className="w-4 h-4 mr-2" />
          Retour
        </Button>
        <div className="text-center py-12 text-muted-foreground">
          Utilisateur introuvable
        </div>
      </div>
    );
  }

  const handleToggleStatus = async () => {
    const newEnabled = !user.enabled;
    setPendingAction(null);
    try {
      await adminApi.updateUserStatus(user.id, newEnabled);
      showToast(newEnabled ? 'Compte activé' : 'Compte désactivé', 'success');
      queryClient.invalidateQueries({ queryKey: queryKeys.admin.userDetail(user.id) });
      queryClient.invalidateQueries({ queryKey: ['admin', 'users'] });
    } catch {
      showToast('Échec de la mise à jour du statut', 'error');
    }
  };

  const handleRoleChange = async (newRole: string) => {
    try {
      await adminApi.updateUserRole(user.id, newRole as Role);
      const label = newRole === 'ADMIN' ? 'Administrateur' : newRole === 'PREMIUM_USER' ? 'Premium' : 'Utilisateur';
      showToast(`Rôle modifié en ${label}`, 'success');
      queryClient.invalidateQueries({ queryKey: queryKeys.admin.userDetail(user.id) });
      queryClient.invalidateQueries({ queryKey: ['admin', 'users'] });
    } catch {
      showToast('Échec de la mise à jour du rôle', 'error');
    }
  };

  const handleResetPassword = async () => {
    try {
      await adminApi.triggerPasswordReset(user.id);
      showToast('Email de réinitialisation envoyé', 'success');
    } catch {
      showToast("Échec de l'envoi de l'email", 'error');
    }
  };

  const handleDelete = async () => {
    setPendingAction(null);
    try {
      await adminApi.deleteUser(user.id);
      showToast('Utilisateur supprimé', 'success');
      navigate('/admin/users');
    } catch {
      showToast("Échec de la suppression", 'error');
    }
  };

  return (
    <div className="max-w-4xl mx-auto animate-fade-in">
      <Breadcrumb items={[
        { label: 'Utilisateurs', href: '/admin/users' },
        { label: user.email },
      ]} />

      {/* Profile header */}
      <BlurFade>
        <div className="flex items-start gap-6 mb-8">
          <Avatar className="h-20 w-20">
            {user.pictureUrl && <AvatarImage src={user.pictureUrl} alt="" />}
            <AvatarFallback className={`${getAvatarColor(user.email)} text-foreground text-2xl font-medium`}>
              {user.email[0].toUpperCase()}
            </AvatarFallback>
          </Avatar>
          <div className="flex-1 min-w-0">
            <h1 className="font-display text-3xl font-semibold tracking-tight mb-2 truncate">{user.email}</h1>
            <div className="flex items-center gap-3 flex-wrap">
              <RoleBadge role={user.role} />
              <Badge variant={user.enabled ? 'secondary' : 'destructive'} className={user.enabled ? 'bg-emerald-500/10 text-emerald-600 hover:bg-emerald-500/20' : ''}>
                {user.enabled ? 'Actif' : 'Désactivé'}
              </Badge>
              {user.hasGoogleAccount && (
                <Badge variant="outline">Google</Badge>
              )}
            </div>
          </div>
        </div>
      </BlurFade>

      {/* Stats cards */}
      <BlurFade delay={0.1}>
        <div className="grid grid-cols-2 sm:grid-cols-4 gap-4 mb-8">
          <div className="rounded-xl border bg-card p-5">
            <div className="flex items-center gap-2 text-muted-foreground text-sm mb-2">
              <List className="w-4 h-4" />
              Listes
            </div>
            <p className="text-2xl font-semibold">{user.listCount}</p>
          </div>
          <div className="rounded-xl border bg-card p-5">
            <div className="flex items-center gap-2 text-muted-foreground text-sm mb-2">
              <Package className="w-4 h-4" />
              Articles
            </div>
            <p className="text-2xl font-semibold">{user.itemCount}</p>
          </div>
          <div className="rounded-xl border bg-card p-5">
            <div className="flex items-center gap-2 text-muted-foreground text-sm mb-2">
              <CreditCard className="w-4 h-4" />
              Paiement
            </div>
            <p className="text-2xl font-semibold">{user.hasStripePayment ? 'Oui' : 'Non'}</p>
            {user.stripeCustomerId && (
              <p className="text-xs text-muted-foreground mt-1 truncate">{user.stripeCustomerId}</p>
            )}
          </div>
          <div className="rounded-xl border bg-card p-5">
            <div className="flex items-center gap-2 text-muted-foreground text-sm mb-2">
              <Mail className="w-4 h-4" />
              Inscrit le
            </div>
            <p className="text-lg font-semibold">{new Date(user.createdAt).toLocaleDateString('fr-FR')}</p>
          </div>
        </div>
      </BlurFade>

      <Separator className="mb-8" />

      {/* Actions */}
      <BlurFade delay={0.2}>
        <h2 className="font-display text-xl font-semibold mb-4">Actions</h2>
        <div className="space-y-4">
          {/* Role */}
          <div className="flex items-center justify-between rounded-lg border p-4">
            <div>
              <p className="font-medium">Rôle</p>
              <p className="text-sm text-muted-foreground">Modifier le rôle de l'utilisateur</p>
            </div>
            <Select value={user.role} onValueChange={handleRoleChange}>
              <SelectTrigger className="w-[180px]">
                <SelectValue />
              </SelectTrigger>
              <SelectContent>
                <SelectItem value="USER">Utilisateur</SelectItem>
                <SelectItem value="PREMIUM_USER">Premium</SelectItem>
                <SelectItem value="ADMIN">Administrateur</SelectItem>
              </SelectContent>
            </Select>
          </div>

          {/* Status toggle */}
          <div className="flex items-center justify-between rounded-lg border p-4">
            <div>
              <p className="font-medium">Statut du compte</p>
              <p className="text-sm text-muted-foreground">
                {user.enabled ? 'Le compte est actuellement actif' : 'Le compte est actuellement désactivé'}
              </p>
            </div>
            <Button
              variant={user.enabled ? 'outline' : 'default'}
              onClick={() => setPendingAction(user.enabled ? 'disable' : 'enable')}
            >
              {user.enabled ? 'Désactiver' : 'Activer'}
            </Button>
          </div>

          {/* Password reset */}
          <div className="flex items-center justify-between rounded-lg border p-4">
            <div>
              <p className="font-medium">Mot de passe</p>
              <p className="text-sm text-muted-foreground">Envoyer un email de réinitialisation du mot de passe</p>
            </div>
            <Button variant="outline" onClick={handleResetPassword}>
              <KeyRound className="w-4 h-4 mr-2" />
              Réinitialiser
            </Button>
          </div>

          {/* Delete */}
          <div className="flex items-center justify-between rounded-lg border border-destructive/20 p-4">
            <div>
              <p className="font-medium text-destructive">Supprimer le compte</p>
              <p className="text-sm text-muted-foreground">Cette action est irréversible</p>
            </div>
            <Button variant="destructive" onClick={() => setPendingAction('delete')}>
              Supprimer
            </Button>
          </div>
        </div>
      </BlurFade>

      {/* Confirm modals */}
      <ConfirmModal
        isOpen={pendingAction === 'disable' || pendingAction === 'enable'}
        title={pendingAction === 'disable' ? 'Désactiver le compte' : 'Activer le compte'}
        message={pendingAction === 'disable'
          ? "L'utilisateur ne pourra plus se connecter."
          : "L'utilisateur pourra à nouveau se connecter."
        }
        confirmLabel={pendingAction === 'disable' ? 'Désactiver' : 'Activer'}
        onConfirm={handleToggleStatus}
        onCancel={() => setPendingAction(null)}
      />

      <ConfirmModal
        isOpen={pendingAction === 'delete'}
        title="Supprimer l'utilisateur"
        message="Êtes-vous sûr de vouloir supprimer cet utilisateur ? Toutes ses données seront perdues."
        confirmLabel="Supprimer"
        onConfirm={handleDelete}
        onCancel={() => setPendingAction(null)}
      />
    </div>
  );
}
