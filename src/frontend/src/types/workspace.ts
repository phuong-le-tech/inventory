export type WorkspaceRole = 'OWNER' | 'EDITOR' | 'VIEWER';
export type InvitationStatus = 'PENDING' | 'ACCEPTED' | 'DECLINED';

export interface Workspace {
  id: string;
  name: string;
  isDefault: boolean;
  role: WorkspaceRole;
  memberCount: number;
  listCount: number;
  createdAt: string;
  updatedAt: string;
}

export interface WorkspaceMember {
  id: string;
  userId: string;
  email: string;
  pictureUrl?: string;
  role: WorkspaceRole;
  joinedAt: string;
}

export interface WorkspaceInvitation {
  id: string;
  email: string;
  role: WorkspaceRole;
  status: InvitationStatus;
  workspaceName: string;
  createdAt: string;
  expiresAt: string;
}

export const WORKSPACE_ROLE_LABELS: Record<WorkspaceRole, string> = {
  OWNER: 'Propriétaire',
  EDITOR: 'Éditeur',
  VIEWER: 'Lecteur',
};
