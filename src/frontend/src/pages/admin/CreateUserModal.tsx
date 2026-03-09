import { useState, useEffect } from 'react';
import { useForm, Controller } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { AlertCircle } from 'lucide-react';
import { createUserSchema, CreateUserFormData } from '../../schemas/auth.schemas';
import { User } from '../../types/auth';
import { adminApi } from '../../services/authApi';
import { getApiErrorStatus } from '../../utils/errorUtils';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { Dialog, DialogContent, DialogHeader, DialogTitle, DialogFooter } from '@/components/ui/dialog';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select';

interface CreateUserModalProps {
  open: boolean;
  onClose: () => void;
  onCreated: (user: User) => void;
}

export function CreateUserModal({ open, onClose, onCreated }: CreateUserModalProps) {
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
