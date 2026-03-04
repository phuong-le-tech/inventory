import { Link } from 'react-router-dom';
import { ArrowLeft } from 'lucide-react';
import { motion } from 'motion/react';
import { DotPattern } from '@/components/effects/dot-pattern';
import { BlurFade } from '@/components/effects/blur-fade';

export function PrivacyPolicy() {
  return (
    <div className="min-h-screen flex relative overflow-hidden bg-background">
      {/* Left side - decorative */}
      <div className="hidden lg:flex lg:w-[45%] relative items-center justify-center overflow-hidden">
        <DotPattern className="opacity-40" width={20} height={20} cr={1} />
        <BlurFade delay={0.1} duration={0.8} blur="20px">
          <h1 className="font-display text-[8rem] xl:text-[10rem] font-bold text-foreground/[0.04] select-none leading-none -rotate-3">
            Priva
            <br />
            cy
          </h1>
        </BlurFade>
      </div>

      {/* Right side - content */}
      <div className="flex-1 flex items-start justify-center px-6 lg:px-16 py-12 overflow-y-auto">
        <motion.div
          initial={{ opacity: 0, x: 30 }}
          animate={{ opacity: 1, x: 0 }}
          transition={{ duration: 0.5, ease: [0.4, 0, 0.2, 1] }}
          className="w-full max-w-lg"
        >
          <Link
            to="/signup"
            className="inline-flex items-center gap-1.5 text-sm text-muted-foreground hover:text-foreground transition-colors mb-8"
          >
            <ArrowLeft className="w-4 h-4" />
            Retour
          </Link>

          <h1 className="font-display text-3xl font-semibold tracking-tight mb-8">
            Politique de confidentialite
          </h1>

          <div className="prose prose-sm text-muted-foreground space-y-6">
            <p className="text-sm text-muted-foreground/70">
              Derniere mise a jour : mars 2026
            </p>

            <section>
              <h2 className="text-lg font-semibold text-foreground mb-2">
                Donnees collectees
              </h2>
              <p>Nous collectons les donnees suivantes :</p>
              <ul className="list-disc pl-5 space-y-1 mt-2">
                <li>Adresse email (pour l'authentification)</li>
                <li>Mot de passe (stocke sous forme de hachage securise BCrypt)</li>
                <li>Images que vous ajoutez a vos objets</li>
                <li>
                  Si vous utilisez Google pour vous connecter : votre nom, email et
                  photo de profil fournis par Google
                </li>
              </ul>
            </section>

            <section>
              <h2 className="text-lg font-semibold text-foreground mb-2">
                Stockage des donnees
              </h2>
              <p>
                Vos donnees sont stockees dans une base de donnees PostgreSQL
                securisee. Les images sont stockees directement dans la base de
                donnees. Les mots de passe ne sont jamais stockes en clair.
              </p>
            </section>

            <section>
              <h2 className="text-lg font-semibold text-foreground mb-2">
                Utilisation des cookies
              </h2>
              <p>
                Nous utilisons un cookie HttpOnly securise pour maintenir votre
                session d'authentification. Ce cookie expire apres 24 heures.
                Aucun cookie de suivi ou publicitaire n'est utilise.
              </p>
            </section>

            <section>
              <h2 className="text-lg font-semibold text-foreground mb-2">
                Services tiers
              </h2>
              <p>
                Si vous choisissez de vous connecter avec Google, nous utilisons
                l'API Google OAuth pour verifier votre identite. Aucune autre
                donnee n'est partagee avec des tiers.
              </p>
            </section>

            <section>
              <h2 className="text-lg font-semibold text-foreground mb-2">
                Vos droits
              </h2>
              <p>Vous avez le droit de :</p>
              <ul className="list-disc pl-5 space-y-1 mt-2">
                <li>Acceder a vos donnees personnelles</li>
                <li>Supprimer votre compte et toutes les donnees associees</li>
                <li>Exporter vos donnees sur demande</li>
              </ul>
            </section>

            <section>
              <h2 className="text-lg font-semibold text-foreground mb-2">
                Contact
              </h2>
              <p>
                Pour toute question concernant vos donnees personnelles, vous
                pouvez nous contacter par email.
              </p>
            </section>
          </div>
        </motion.div>
      </div>
    </div>
  );
}
