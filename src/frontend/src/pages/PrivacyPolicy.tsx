import { useNavigate } from 'react-router-dom';
import { ArrowLeft } from 'lucide-react';
import { motion } from 'motion/react';
import { Button } from '@/components/ui/button';

export function PrivacyPolicy() {
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
