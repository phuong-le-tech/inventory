import { Link } from 'react-router-dom';
import { Button } from '@/components/ui/button';

export default function NotFound() {
  return (
    <div className="flex flex-col items-center justify-center min-h-[60vh] text-center px-4">
      <h1 className="font-display text-6xl font-bold text-foreground/10 mb-4">404</h1>
      <h2 className="font-display text-2xl font-semibold mb-2">Page introuvable</h2>
      <p className="text-muted-foreground mb-8 max-w-md">
        La page que vous recherchez n'existe pas ou a été déplacée.
      </p>
      <Button asChild>
        <Link to="/dashboard">Retour au tableau de bord</Link>
      </Button>
    </div>
  );
}
