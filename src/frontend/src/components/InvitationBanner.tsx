import { useState } from 'react';
import { Mail, Check, X } from 'lucide-react';
import { useWorkspace } from '../contexts/WorkspaceContext';
import { workspaceApi } from '../services/workspaceApi';
import { useToast } from './Toast';

export function InvitationBanner() {
  const { pendingInvitations, refreshInvitations, refreshWorkspaces } = useWorkspace();
  const { showToast } = useToast();
  const [processing, setProcessing] = useState<string | null>(null);

  if (pendingInvitations.length === 0) return null;

  const handleAccept = async (invitationId: string) => {
    setProcessing(invitationId);
    try {
      await workspaceApi.acceptInvitationById(invitationId);
      await Promise.all([refreshInvitations(), refreshWorkspaces()]);
    } catch {
      showToast("Erreur lors de l'acceptation de l'invitation", 'error');
    } finally {
      setProcessing(null);
    }
  };

  const handleDecline = async (invitationId: string) => {
    setProcessing(invitationId);
    try {
      await workspaceApi.declineInvitationById(invitationId);
      await refreshInvitations();
    } catch {
      showToast("Erreur lors du refus de l'invitation", 'error');
    } finally {
      setProcessing(null);
    }
  };

  return (
    <div className="space-y-2 mb-4">
      {pendingInvitations.map((invitation) => (
        <div
          key={invitation.id}
          className="flex items-center gap-3 px-4 py-3 bg-brand-light rounded-lg border border-brand/20"
        >
          <Mail className="h-5 w-5 text-brand flex-shrink-0" />
          <p className="text-sm flex-1">
            Vous avez été invité(e) à rejoindre{' '}
            <strong>{invitation.workspaceName}</strong>
          </p>
          <div className="flex gap-2">
            <button
              onClick={() => handleAccept(invitation.id)}
              disabled={processing === invitation.id}
              className="flex items-center gap-1 px-3 py-1.5 text-xs font-medium bg-brand text-white rounded-md hover:opacity-90 transition-opacity disabled:opacity-50"
            >
              <Check className="h-3.5 w-3.5" />
              Accepter
            </button>
            <button
              onClick={() => handleDecline(invitation.id)}
              disabled={processing === invitation.id}
              className="flex items-center gap-1 px-3 py-1.5 text-xs font-medium bg-muted text-muted-foreground rounded-md hover:bg-muted/80 transition-colors disabled:opacity-50"
            >
              <X className="h-3.5 w-3.5" />
              Refuser
            </button>
          </div>
        </div>
      ))}
    </div>
  );
}
