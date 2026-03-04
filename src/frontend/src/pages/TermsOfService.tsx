import { Link } from 'react-router-dom';
import { ArrowLeft } from 'lucide-react';
import { motion } from 'motion/react';
import { DotPattern } from '@/components/effects/dot-pattern';
import { BlurFade } from '@/components/effects/blur-fade';

export function TermsOfService() {
  return (
    <div className="min-h-screen flex relative overflow-hidden bg-background">
      {/* Left side - decorative */}
      <div className="hidden lg:flex lg:w-[45%] relative items-center justify-center overflow-hidden">
        <DotPattern className="opacity-40" width={20} height={20} cr={1} />
        <BlurFade delay={0.1} duration={0.8} blur="20px">
          <h1 className="font-display text-[8rem] xl:text-[10rem] font-bold text-foreground/[0.04] select-none leading-none -rotate-3">
            Ter
            <br />
            mes
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
            Conditions d'utilisation
          </h1>

          <div className="prose prose-sm text-muted-foreground space-y-6">
            <p className="text-sm text-muted-foreground/70">
              Derniere mise a jour : mars 2026
            </p>

            <section>
              <h2 className="text-lg font-semibold text-foreground mb-2">
                Utilisation acceptable
              </h2>
              <p>
                Ce service est destine a la gestion d'inventaire personnel ou
                professionnel. Vous vous engagez a ne pas utiliser le service
                pour stocker du contenu illegal, offensant ou portant atteinte
                aux droits d'autrui.
              </p>
            </section>

            <section>
              <h2 className="text-lg font-semibold text-foreground mb-2">
                Votre compte
              </h2>
              <p>
                Vous etes responsable de la securite de votre compte et de votre
                mot de passe. Nous nous reservons le droit de suspendre ou
                supprimer un compte en cas de violation de ces conditions.
              </p>
            </section>

            <section>
              <h2 className="text-lg font-semibold text-foreground mb-2">
                Propriete du contenu
              </h2>
              <p>
                Vous conservez la propriete de toutes les donnees et images que
                vous ajoutez au service. Nous ne revendiquons aucun droit sur
                votre contenu.
              </p>
            </section>

            <section>
              <h2 className="text-lg font-semibold text-foreground mb-2">
                Disponibilite du service
              </h2>
              <p>
                Le service est fourni "en l'etat" sans garantie de disponibilite.
                Nous nous efforcons de maintenir le service disponible mais ne
                pouvons garantir un fonctionnement ininterrompu.
              </p>
            </section>

            <section>
              <h2 className="text-lg font-semibold text-foreground mb-2">
                Limitation de responsabilite
              </h2>
              <p>
                Dans les limites autorisees par la loi, nous ne sommes pas
                responsables des dommages directs ou indirects resultant de
                l'utilisation ou de l'impossibilite d'utiliser le service.
              </p>
            </section>

            <section>
              <h2 className="text-lg font-semibold text-foreground mb-2">
                Modifications
              </h2>
              <p>
                Nous nous reservons le droit de modifier ces conditions a tout
                moment. Les utilisateurs seront informes des changements
                importants. L'utilisation continue du service apres modification
                vaut acceptation des nouvelles conditions.
              </p>
            </section>
          </div>
        </motion.div>
      </div>
    </div>
  );
}
