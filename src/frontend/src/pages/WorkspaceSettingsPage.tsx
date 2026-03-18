import { useState, useEffect } from 'react';
import { useParams, Navigate } from 'react-router-dom';
import { useQuery, useQueryClient } from '@tanstack/react-query';
import { Trash2, UserPlus, Save } from 'lucide-react';
import { workspaceApi } from '../services/workspaceApi';
import type { WorkspaceRole } from '../types/workspace';
import { WORKSPACE_ROLE_LABELS } from '../types/workspace';
import { useWorkspace } from '../contexts/WorkspaceContext';
import { useToast } from '../components/Toast';
import ConfirmModal from '../components/ConfirmModal';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { Badge } from '@/components/ui/badge';
import { Breadcrumb } from '../components/Breadcrumb';
import { BlurFade } from '@/components/effects/blur-fade';
import { queryKeys } from '../lib/queryKeys';

export default function WorkspaceSettingsPage() {
  const { id } = useParams<{ id: string }>();
  const { showToast } = useToast();
  const { refreshWorkspaces } = useWorkspace();
  const queryClient = useQueryClient();

  const [name, setName] = useState('');
  const [inviteEmail, setInviteEmail] = useState('');
  const [inviteRole, setInviteRole] = useState<WorkspaceRole>('EDITOR');
  const [saving, setSaving] = useState(false);
  const [inviting, setInviting] = useState(false);
  const [removingUserId, setRemovingUserId] = useState<string | null>(null);

  const { data: workspace, isLoading } = useQuery({
    queryKey: queryKeys.workspaces.detail(id!),
    queryFn: () => workspaceApi.getById(id!),
    enabled: !!id,
  });

  const { data: members = [], refetch: refetchMembers } = useQuery({
    queryKey: queryKeys.workspaces.members(id!),
    queryFn: () => workspaceApi.getMembers(id!),
    enabled: !!id,
  });

  // Sync name state when workspace data loads
  useEffect(() => {
    if (workspace) setName(workspace.name);
  }, [workspace]);

  const handleSaveName = async () => {
    if (!id || !name.trim()) return;
    setSaving(true);
    try {
      await workspaceApi.update(id, { name: name.trim() });
      await refreshWorkspaces();
      queryClient.invalidateQueries({ queryKey: queryKeys.workspaces.detail(id) });
      showToast('Nom mis à jour', 'success');
    } catch {
      showToast('Erreur lors de la mise à jour', 'error');
    } finally {
      setSaving(false);
    }
  };

  const handleInvite = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!id || !inviteEmail.trim()) return;
    setInviting(true);
    try {
      await workspaceApi.inviteMember(id, { email: inviteEmail.trim(), role: inviteRole });
      setInviteEmail('');
      showToast('Invitation envoyée', 'success');
    } catch {
      showToast("Erreur lors de l'envoi de l'invitation", 'error');
    } finally {
      setInviting(false);
    }
  };

  const handleRemoveMember = async () => {
    if (!id || !removingUserId) return;
    try {
      await workspaceApi.removeMember(id, removingUserId);
      await refetchMembers();
      showToast('Membre retiré', 'success');
    } catch {
      showToast('Erreur lors du retrait du membre', 'error');
    } finally {
      setRemovingUserId(null);
    }
  };

  const handleRoleChange = async (userId: string, role: WorkspaceRole) => {
    if (!id) return;
    try {
      await workspaceApi.updateMemberRole(id, userId, { role });
      await refetchMembers();
      showToast('Rôle mis à jour', 'success');
    } catch {
      showToast('Erreur lors de la mise à jour du rôle', 'error');
    }
  };

  if (isLoading) {
    return <div className="animate-pulse h-64 bg-muted rounded-lg" />;
  }

  if (!workspace) {
    return <Navigate to="/workspaces" replace />;
  }

  // Only OWNER can access workspace settings
  if (workspace.role !== 'OWNER') {
    return <Navigate to="/workspaces" replace />;
  }

  return (
    <>
      <Breadcrumb items={[
        { label: 'Espaces de travail', href: '/workspaces' },
        { label: workspace.name },
      ]} />

      <h1 className="text-2xl font-bold mb-6">Paramètres de l'espace</h1>

      {/* Name edit */}
      <BlurFade delay={0.1}>
        <div className="mb-8 p-4 border rounded-lg bg-card">
          <Label htmlFor="ws-name">Nom</Label>
          <div className="flex gap-2 mt-1">
            <Input
              id="ws-name"
              value={name}
              onChange={(e) => setName(e.target.value)}
              maxLength={100}
            />
            <Button onClick={handleSaveName} disabled={saving}>
              <Save className="h-4 w-4 mr-2" />
              Enregistrer
            </Button>
          </div>
        </div>
      </BlurFade>

      {/* Invite form */}
      <BlurFade delay={0.2}>
        <div className="mb-8 p-4 border rounded-lg bg-card">
          <h2 className="text-lg font-semibold mb-3">Inviter un membre</h2>
          <form onSubmit={handleInvite} className="flex gap-2 items-end">
            <div className="flex-1">
              <Label htmlFor="invite-email">Email</Label>
              <Input
                id="invite-email"
                type="email"
                value={inviteEmail}
                onChange={(e) => setInviteEmail(e.target.value)}
                placeholder="email@exemple.com"
              />
            </div>
            <div>
              <Label htmlFor="invite-role">Rôle</Label>
              <select
                id="invite-role"
                value={inviteRole}
                onChange={(e) => setInviteRole(e.target.value as WorkspaceRole)}
                className="flex h-9 w-full rounded-md border border-input bg-transparent px-3 py-1 text-sm"
              >
                <option value="EDITOR">{WORKSPACE_ROLE_LABELS.EDITOR}</option>
                <option value="VIEWER">{WORKSPACE_ROLE_LABELS.VIEWER}</option>
              </select>
            </div>
            <Button type="submit" disabled={inviting || !inviteEmail.trim()}>
              <UserPlus className="h-4 w-4 mr-2" />
              Inviter
            </Button>
          </form>
        </div>
      </BlurFade>

      {/* Members list */}
      <BlurFade delay={0.3}>
        <div className="p-4 border rounded-lg bg-card">
          <h2 className="text-lg font-semibold mb-3">
            Membres ({members.length})
          </h2>
          <div className="space-y-3">
            {members.map((member) => (
              <div
                key={member.id}
                className="flex items-center justify-between py-2 border-b last:border-0"
              >
                <div className="flex items-center gap-3">
                  {member.pictureUrl ? (
                    <img
                      src={member.pictureUrl}
                      alt=""
                      className="h-8 w-8 rounded-full"
                    />
                  ) : (
                    <div className="h-8 w-8 rounded-full bg-muted flex items-center justify-center text-xs font-medium">
                      {member.email[0].toUpperCase()}
                    </div>
                  )}
                  <div>
                    <p className="text-sm font-medium">{member.email}</p>
                    <Badge variant="outline" className="text-[11px]">
                      {WORKSPACE_ROLE_LABELS[member.role]}
                    </Badge>
                  </div>
                </div>
                {member.role !== 'OWNER' && (
                  <div className="flex items-center gap-2">
                    <select
                      value={member.role}
                      onChange={(e) =>
                        handleRoleChange(member.userId, e.target.value as WorkspaceRole)
                      }
                      className="text-xs border rounded px-2 py-1"
                    >
                      <option value="EDITOR">{WORKSPACE_ROLE_LABELS.EDITOR}</option>
                      <option value="VIEWER">{WORKSPACE_ROLE_LABELS.VIEWER}</option>
                    </select>
                    <button
                      onClick={() => setRemovingUserId(member.userId)}
                      className="p-1 text-muted-foreground hover:text-destructive transition-colors"
                      title="Retirer"
                      aria-label="Retirer le membre"
                    >
                      <Trash2 className="h-4 w-4" />
                    </button>
                  </div>
                )}
              </div>
            ))}
          </div>
        </div>
      </BlurFade>

      <ConfirmModal
        isOpen={!!removingUserId}
        onCancel={() => setRemovingUserId(null)}
        onConfirm={handleRemoveMember}
        title="Retirer le membre"
        message="Ce membre n'aura plus accès aux listes de cet espace."
        confirmLabel="Retirer"
      />
    </>
  );
}
