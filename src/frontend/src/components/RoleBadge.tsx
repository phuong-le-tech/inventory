import { Shield, Crown, User as UserIcon } from 'lucide-react';
import { Badge } from '@/components/ui/badge';
import { Role } from '../types/auth';

export function RoleBadge({ role }: { role: Role }) {
  switch (role) {
    case 'ADMIN':
      return <Badge variant="default"><Shield className="w-3 h-3 mr-1" />Administrateur</Badge>;
    case 'PREMIUM_USER':
      return <Badge variant="default" className="bg-amber-500/90 hover:bg-amber-500"><Crown className="w-3 h-3 mr-1" />Premium</Badge>;
    default:
      return <Badge variant="secondary"><UserIcon className="w-3 h-3 mr-1" />Utilisateur</Badge>;
  }
}
