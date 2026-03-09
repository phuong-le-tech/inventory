import { useNavigate } from 'react-router-dom';
import { ArrowLeft } from 'lucide-react';
import { motion } from 'motion/react';
import { Button } from '@/components/ui/button';

export function TermsOfService() {
  const navigate = useNavigate();

  return (
    <div className="min-h-screen bg-background">
      <div className="container max-w-prose mx-auto px-6 py-12">
        <motion.div
          initial={{ opacity: 0, y: 20 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ duration: 0.5, ease: [0.4, 0, 0.2, 1] }}
        >
          <Button
            variant="ghost"
            size="sm"
            onClick={() => navigate(-1)}
            className="mb-8 -ml-2"
          >
            <ArrowLeft className="w-4 h-4 mr-1.5" />
            Retour
          </Button>

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
