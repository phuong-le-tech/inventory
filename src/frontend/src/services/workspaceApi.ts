import http from './http';
import type {
  Workspace,
  WorkspaceMember,
  WorkspaceInvitation,
  WorkspaceRole,
} from '../types/workspace';

export const workspaceApi = {
  getAll: async (signal?: AbortSignal): Promise<Workspace[]> => {
    const response = await http.get<Workspace[]>('/workspaces', { signal });
    return response.data;
  },

  getById: async (id: string, signal?: AbortSignal): Promise<Workspace> => {
    const response = await http.get<Workspace>(`/workspaces/${id}`, { signal });
    return response.data;
  },

  create: async (data: { name: string }): Promise<Workspace> => {
    const response = await http.post<Workspace>('/workspaces', data);
    return response.data;
  },

  update: async (id: string, data: { name: string }): Promise<Workspace> => {
    const response = await http.patch<Workspace>(`/workspaces/${id}`, data);
    return response.data;
  },

  delete: async (id: string): Promise<void> => {
    await http.delete(`/workspaces/${id}`);
  },

  getMembers: async (id: string, signal?: AbortSignal): Promise<WorkspaceMember[]> => {
    const response = await http.get<WorkspaceMember[]>(`/workspaces/${id}/members`, { signal });
    return response.data;
  },

  inviteMember: async (id: string, data: { email: string; role: WorkspaceRole }): Promise<void> => {
    await http.post(`/workspaces/${id}/invitations`, data);
  },

  removeMember: async (workspaceId: string, userId: string): Promise<void> => {
    await http.delete(`/workspaces/${workspaceId}/members/${userId}`);
  },

  updateMemberRole: async (
    workspaceId: string,
    userId: string,
    data: { role: WorkspaceRole },
  ): Promise<void> => {
    await http.patch(`/workspaces/${workspaceId}/members/${userId}/role`, data);
  },

  acceptInvitationById: async (invitationId: string): Promise<void> => {
    await http.post(`/workspaces/invitations/id/${invitationId}/accept`);
  },

  declineInvitationById: async (invitationId: string): Promise<void> => {
    await http.post(`/workspaces/invitations/id/${invitationId}/decline`);
  },

  getPendingInvitations: async (signal?: AbortSignal): Promise<WorkspaceInvitation[]> => {
    const response = await http.get<WorkspaceInvitation[]>('/workspaces/invitations/pending', {
      signal,
    });
    return response.data;
  },
};
