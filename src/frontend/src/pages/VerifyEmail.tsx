import { useState, useEffect } from 'react';
import { useSearchParams, Link } from 'react-router-dom';
import { motion } from 'motion/react';
import { Mail, CheckCircle, XCircle, Loader2 } from 'lucide-react';
import { authApi } from '../services/authApi';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { DotPattern } from '@/components/effects/dot-pattern';
import { BlurFade } from '@/components/effects/blur-fade';
import { getApiErrorMessage } from '@/utils/errorUtils';

type Status = 'pending' | 'verifying' | 'success' | 'error';

export function VerifyEmail() {
  const [searchParams] = useSearchParams();
  const token = searchParams.get('token');

  const [status, setStatus] = useState<Status>(token ? 'verifying' : 'pending');
  const [error, setError] = useState('');
  const [resendEmail, setResendEmail] = useState('');
  const [resendSent, setResendSent] = useState(false);
  const [resendLoading, setResendLoading] = useState(false);

  useEffect(() => {
    if (token) {
      authApi.verifyEmail(token)
        .then(() => setStatus('success'))
        .catch((err) => {
          setStatus('error');
          setError(getApiErrorMessage(err, 'Le lien de verification est invalide ou a expire.'));
        });
    }
  }, [token]);

  const handleResend = async () => {
    if (!resendEmail) return;
    setResendLoading(true);
    try {
      await authApi.resendVerification(resendEmail);
      setResendSent(true);
    } catch {
      // Always show success to prevent email enumeration
      setResendSent(true);
    } finally {
      setResendLoading(false);
    }
  };

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

      {/* Right side - content */}
      <div className="flex-1 flex items-center justify-center px-6 lg:px-16">
        <motion.div
          initial={{ opacity: 0, x: 30 }}
          animate={{ opacity: 1, x: 0 }}
          transition={{ duration: 0.5, ease: [0.4, 0, 0.2, 1] }}
          className="w-full max-w-md"
        >
          {status === 'verifying' && (
            <div className="text-center">
              <Loader2 className="h-12 w-12 animate-spin text-muted-foreground mx-auto mb-4" />
              <h2 className="font-display text-2xl font-semibold mb-2">Verification en cours...</h2>
              <p className="text-muted-foreground">Veuillez patienter pendant que nous verifions votre email.</p>
            </div>
          )}

          {status === 'success' && (
            <div className="text-center">
              <CheckCircle className="h-12 w-12 text-green-500 mx-auto mb-4" />
              <h2 className="font-display text-2xl font-semibold mb-2">Email verifie !</h2>
              <p className="text-muted-foreground mb-6">
                Votre adresse email a ete verifiee avec succes. Vous pouvez maintenant vous connecter.
              </p>
              <Button asChild className="w-full h-11" size="lg">
                <Link to="/login?verified=true">Se connecter</Link>
              </Button>
            </div>
          )}

          {status === 'error' && (
            <div className="text-center">
              <XCircle className="h-12 w-12 text-destructive mx-auto mb-4" />
              <h2 className="font-display text-2xl font-semibold mb-2">Echec de la verification</h2>
              <p className="text-muted-foreground mb-6">{error}</p>
              <div className="rounded-2xl border bg-card p-6 shadow-float text-left">
                <p className="text-sm text-muted-foreground mb-4">
                  Entrez votre email pour recevoir un nouveau lien de verification :
                </p>
                {resendSent ? (
                  <p className="text-sm text-green-600">
                    Si un compte existe avec cet email, un nouveau lien de verification a ete envoye.
                  </p>
                ) : (
                  <div className="flex gap-2">
                    <Input
                      type="email"
                      placeholder="vous@exemple.com"
                      value={resendEmail}
                      onChange={(e) => setResendEmail(e.target.value)}
                    />
                    <Button onClick={handleResend} disabled={resendLoading || !resendEmail}>
                      {resendLoading ? <Loader2 className="h-4 w-4 animate-spin" /> : 'Renvoyer'}
                    </Button>
                  </div>
                )}
              </div>
              <p className="mt-4 text-sm text-muted-foreground">
                <Link to="/login" className="text-foreground font-medium hover:underline">
                  Retour a la connexion
                </Link>
              </p>
            </div>
          )}

          {status === 'pending' && (
            <div className="text-center">
              <Mail className="h-12 w-12 text-muted-foreground mx-auto mb-4" />
              <h2 className="font-display text-2xl font-semibold mb-2">Verifiez votre email</h2>
              <p className="text-muted-foreground mb-6">
                Un email de verification a ete envoye a votre adresse. Cliquez sur le lien dans l'email pour activer votre compte.
              </p>
              <div className="rounded-2xl border bg-card p-6 shadow-float">
                <p className="text-sm text-muted-foreground mb-4">
                  Vous n'avez pas recu l'email ? Entrez votre adresse pour le renvoyer :
                </p>
                {resendSent ? (
                  <p className="text-sm text-green-600">
                    Si un compte existe avec cet email, un nouveau lien de verification a ete envoye.
                  </p>
                ) : (
                  <div className="flex gap-2">
                    <Input
                      type="email"
                      placeholder="vous@exemple.com"
                      value={resendEmail}
                      onChange={(e) => setResendEmail(e.target.value)}
                    />
                    <Button onClick={handleResend} disabled={resendLoading || !resendEmail}>
                      {resendLoading ? <Loader2 className="h-4 w-4 animate-spin" /> : 'Renvoyer'}
                    </Button>
                  </div>
                )}
              </div>
              <p className="mt-4 text-sm text-muted-foreground">
                <Link to="/login" className="text-foreground font-medium hover:underline">
                  Retour a la connexion
                </Link>
              </p>
            </div>
          )}
        </motion.div>
      </div>
    </div>
  );
}
