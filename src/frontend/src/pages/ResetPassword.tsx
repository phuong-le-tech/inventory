import { useState } from 'react';
import { useSearchParams, useNavigate, Link } from 'react-router-dom';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { AlertCircle, KeyRound } from 'lucide-react';
import { motion } from 'motion/react';
import { resetPasswordSchema, ResetPasswordFormData } from '../schemas/auth.schemas';
import { authApi } from '../services/authApi';
import { getApiErrorMessage } from '../utils/errorUtils';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { DotPattern } from '@/components/effects/dot-pattern';
import { BlurFade } from '@/components/effects/blur-fade';

export function ResetPassword() {
  const [serverError, setServerError] = useState('');
  const [loading, setLoading] = useState(false);
  const [searchParams] = useSearchParams();
  const navigate = useNavigate();
  const token = searchParams.get('token');

  const {
    register,
    handleSubmit,
    formState: { errors },
  } = useForm<ResetPasswordFormData>({ resolver: zodResolver(resetPasswordSchema) });

  const onSubmit = async (data: ResetPasswordFormData) => {
    if (!token) return;
    setServerError('');
    setLoading(true);
    try {
      await authApi.resetPassword(token, data.password);
      navigate('/login?reset=true', { replace: true });
    } catch (err: unknown) {
      setServerError(getApiErrorMessage(err, 'Le lien est invalide ou a expiré'));
    } finally {
      setLoading(false);
    }
  };

  if (!token) {
    return (
      <div className="min-h-screen flex items-center justify-center bg-background px-6">
        <div className="text-center">
          <h2 className="font-display text-2xl font-semibold mb-2">Lien invalide</h2>
          <p className="text-muted-foreground mb-6">Ce lien de réinitialisation est invalide.</p>
          <Button asChild>
            <Link to="/forgot-password">Demander un nouveau lien</Link>
          </Button>
        </div>
      </div>
    );
  }

  return (
    <div className="min-h-screen flex relative overflow-hidden bg-background">
      {/* Left side - decorative */}
      <div className="hidden lg:flex lg:w-[45%] relative items-center justify-center overflow-hidden">
        <DotPattern className="opacity-40" width={20} height={20} cr={1} />
        <BlurFade delay={0.1} duration={0.8} blur="20px">
          <h1 className="font-display text-[8rem] xl:text-[10rem] font-bold text-foreground/[0.04] select-none leading-none -rotate-3">
            Inven
            <br />
            tory
          </h1>
        </BlurFade>
      </div>

      {/* Right side - form */}
      <div className="flex-1 flex items-center justify-center px-6 lg:px-16">
        <motion.div
          initial={{ opacity: 0, x: 30 }}
          animate={{ opacity: 1, x: 0 }}
          transition={{ duration: 0.5, ease: [0.4, 0, 0.2, 1] }}
          className="w-full max-w-md"
        >
          <div className="mb-10">
            <BlurFade delay={0.2}>
              <div className="w-12 h-12 bg-foreground rounded-xl flex items-center justify-center mb-6">
                <KeyRound className="w-6 h-6 text-background" />
              </div>
            </BlurFade>
            <BlurFade delay={0.3}>
              <h1 className="font-display text-4xl font-semibold tracking-tight mb-2">Nouveau mot de passe</h1>
            </BlurFade>
            <BlurFade delay={0.4}>
              <p className="text-muted-foreground">Choisissez un nouveau mot de passe pour votre compte</p>
            </BlurFade>
          </div>

          <div className="rounded-2xl border bg-card p-8 shadow-float">
            <form onSubmit={handleSubmit(onSubmit)} className="space-y-5">
              {serverError && (
                <div className="bg-destructive/10 border border-destructive/20 rounded-lg px-4 py-3 text-destructive text-sm">
                  {serverError}
                  <Link to="/forgot-password" className="block mt-1 font-medium underline">
                    Demander un nouveau lien
                  </Link>
                </div>
              )}

              <div className="space-y-2">
                <Label htmlFor="password">Nouveau mot de passe</Label>
                <Input
                  id="password"
                  type="password"
                  {...register('password')}
                  className={errors.password ? 'border-destructive focus-visible:ring-destructive' : ''}
                  placeholder="Minimum 12 caractères"
                  aria-invalid={!!errors.password}
                  aria-describedby={errors.password ? 'password-error' : undefined}
                />
                {errors.password && (
                  <p id="password-error" role="alert" className="text-sm text-destructive flex items-center gap-1.5">
                    <AlertCircle className="h-4 w-4 flex-shrink-0" />
                    {errors.password.message}
                  </p>
                )}
              </div>

              <div className="space-y-2">
                <Label htmlFor="confirmPassword">Confirmer le mot de passe</Label>
                <Input
                  id="confirmPassword"
                  type="password"
                  {...register('confirmPassword')}
                  className={errors.confirmPassword ? 'border-destructive focus-visible:ring-destructive' : ''}
                  placeholder="Confirmez votre mot de passe"
                  aria-invalid={!!errors.confirmPassword}
                  aria-describedby={errors.confirmPassword ? 'confirmPassword-error' : undefined}
                />
                {errors.confirmPassword && (
                  <p id="confirmPassword-error" role="alert" className="text-sm text-destructive flex items-center gap-1.5">
                    <AlertCircle className="h-4 w-4 flex-shrink-0" />
                    {errors.confirmPassword.message}
                  </p>
                )}
              </div>

              <Button
                type="submit"
                disabled={loading}
                className="w-full h-11"
                size="lg"
              >
                {loading ? (
                  <span className="flex items-center justify-center gap-2">
                    <svg aria-hidden="true" className="animate-spin h-5 w-5" viewBox="0 0 24 24">
                      <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4" fill="none" />
                      <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z" />
                    </svg>
                    Réinitialisation...
                  </span>
                ) : (
                  'Réinitialiser le mot de passe'
                )}
              </Button>
            </form>
          </div>

          <p className="mt-6 text-center text-muted-foreground text-sm">
            <Link to="/login" className="text-foreground font-medium hover:underline">
              Retour à la connexion
            </Link>
          </p>
        </motion.div>
      </div>
    </div>
  );
}
