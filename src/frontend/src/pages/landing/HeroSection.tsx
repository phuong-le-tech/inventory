import { Link } from "react-router-dom";
import {
  ArrowRight,
  Package,
  Layers,
  AlertTriangle,
  AlertCircle,
  Shield,
  Zap,
  CreditCard,
  Plus,
  Search,
} from "lucide-react";
import { Button } from "@/components/ui/button";
import { BlurFade } from "@/components/effects/blur-fade";
import { DotPattern } from "@/components/effects/dot-pattern";
import {
  StaggeredList,
  StaggeredItem,
} from "@/components/effects/staggered-list";
import {
  MOCK_STATS,
  MOCK_LISTS_OVERVIEW,
  MOCK_RECENT_ITEMS,
  MOCK_STATUS_CHART,
  MOCK_CATEGORY_DATA,
} from "./mockData";

const STATUS_LABELS: Record<string, string> = {
  AVAILABLE: "Disponible",
  TO_VERIFY: "À vérifier",
  NEEDS_MAINTENANCE: "Maintenance",
  DAMAGED: "Endommagé",
};

const STATUS_COLORS: Record<string, string> = {
  AVAILABLE: "bg-emerald-100 text-emerald-700",
  TO_VERIFY: "bg-amber-100 text-amber-700",
  NEEDS_MAINTENANCE: "bg-indigo-100 text-indigo-700",
  DAMAGED: "bg-red-100 text-red-700",
};

const TRUST_BADGES = [
  { icon: Zap, label: "Gratuit" },
  { icon: CreditCard, label: "Pas de carte requise" },
  { icon: Shield, label: "Données sécurisées" },
];

const statCards = [
  {
    label: "Total articles",
    value: MOCK_STATS.totalItems.toLocaleString("fr-FR"),
    icon: Package,
    iconColor: "text-green-500",
    iconBg: "bg-green-50",
  },
  {
    label: "Quantité totale",
    value: MOCK_STATS.totalQuantity.toLocaleString("fr-FR"),
    icon: Layers,
    iconColor: "text-indigo-500",
    iconBg: "bg-indigo-100",
  },
  {
    label: "À vérifier",
    value: MOCK_STATS.toVerifyCount,
    icon: AlertTriangle,
    iconColor: "text-amber-500",
    iconBg: "bg-amber-50",
  },
  {
    label: "Attention requise",
    value: MOCK_STATS.needsAttentionCount,
    icon: AlertCircle,
    iconColor: "text-red-500",
    iconBg: "bg-red-50",
  },
];

const maxBarValue = Math.max(...MOCK_STATUS_CHART.map((d) => d.count));

export function HeroSection() {
  return (
    <section aria-labelledby="hero-heading" className="relative overflow-hidden pt-20 md:pt-28 lg:pt-32 pb-20 md:pb-32">
      <DotPattern
        className="opacity-30 [mask-image:radial-gradient(ellipse_at_center,black_30%,transparent_70%)]"
        width={20}
        height={20}
        cr={1}
      />

      <div className="relative max-w-6xl mx-auto px-6">
        {/* Text content */}
        <div className="text-center">
          <BlurFade delay={0.1} duration={0.6}>
            <p className="text-sm font-medium text-brand tracking-widest uppercase mb-6">
              Gestion d'inventaire, simplifiée
            </p>
          </BlurFade>
          <BlurFade delay={0.2} duration={0.6}>
            <h1
              id="hero-heading"
              className="font-display text-4xl md:text-5xl lg:text-6xl xl:text-7xl font-bold tracking-tight max-w-4xl mx-auto leading-[1.1]"
            >
              Gérez votre inventaire{" "}
              <span className="text-brand">en toute simplicité</span>
            </h1>
          </BlurFade>
          <BlurFade delay={0.3} duration={0.6}>
            <p className="text-lg md:text-xl text-muted-foreground mt-6 max-w-2xl mx-auto leading-relaxed">
              Organisez vos articles, suivez vos niveaux de stock et gardez le
              contrôle de votre inventaire avec un tableau de bord intuitif.
            </p>
          </BlurFade>
          <BlurFade delay={0.4} duration={0.6}>
            <div className="flex flex-col sm:flex-row items-center justify-center gap-4 mt-10">
              <Button
                size="lg"
                className="h-12 px-8 text-base"
                asChild
              >
                <Link to="/signup">
                  Commencer gratuitement
                  <ArrowRight className="h-4 w-4 ml-2" aria-hidden="true" />
                </Link>
              </Button>
              <Button variant="outline" size="lg" className="h-12 px-8 text-base" asChild>
                <Link to="/login">Se connecter</Link>
              </Button>
            </div>
          </BlurFade>
          <BlurFade delay={0.5} duration={0.6}>
            <div className="flex items-center justify-center gap-6 mt-8 text-sm text-muted-foreground">
              {TRUST_BADGES.map((badge) => (
                <div key={badge.label} className="flex items-center gap-1.5">
                  <badge.icon className="h-4 w-4 text-brand" aria-hidden="true" />
                  <span>{badge.label}</span>
                </div>
              ))}
            </div>
          </BlurFade>
        </div>

        {/* Dashboard preview */}
        <BlurFade delay={0.6} duration={0.8}>
          <div className="mt-16 md:mt-20">
            <div className="rounded-2xl border bg-card overflow-hidden shadow-float">
              {/* Browser chrome */}
              <div className="flex items-center gap-2 px-4 py-3 border-b bg-muted/30" aria-hidden="true">
                <div className="flex gap-1.5">
                  <span className="w-3 h-3 rounded-full bg-red-500/50" />
                  <span className="w-3 h-3 rounded-full bg-yellow-500/50" />
                  <span className="w-3 h-3 rounded-full bg-green-500/50" />
                </div>
                <span className="text-xs text-muted-foreground ml-2">
                  Tableau de bord
                </span>
              </div>

              <div className="flex">
                {/* Sidebar mock */}
                <div className="hidden lg:flex flex-col w-[200px] border-r bg-muted/10 p-4 gap-4" aria-hidden="true">
                  <div className="flex items-center gap-2 mb-2">
                    <div className="w-7 h-7 bg-brand rounded-lg flex items-center justify-center">
                      <Package className="h-3.5 w-3.5 text-white" />
                    </div>
                    <span className="font-display text-sm font-bold">Shelfio</span>
                  </div>
                  {/* Search bar */}
                  <div className="flex items-center gap-2 px-2.5 py-2 rounded-lg bg-muted/50 border text-xs text-muted-foreground">
                    <Search className="h-3 w-3" />
                    <span>Rechercher...</span>
                    <span className="ml-auto text-[9px] px-1 py-0.5 rounded bg-background border font-mono">⌘K</span>
                  </div>
                  {/* Nav items */}
                  <div className="space-y-0.5">
                    <div className="flex items-center gap-2 px-2.5 py-2 rounded-lg bg-brand text-white text-xs font-medium">
                      <div className="w-3.5 h-3.5 rounded bg-white/20" />
                      Tableau de bord
                    </div>
                    <div className="flex items-center gap-2 px-2.5 py-2 rounded-lg text-xs text-muted-foreground">
                      <div className="w-3.5 h-3.5 rounded bg-muted" />
                      Mes Listes
                    </div>
                    <div className="flex items-center gap-2 px-2.5 py-2 rounded-lg text-xs text-muted-foreground">
                      <div className="w-3.5 h-3.5 rounded bg-muted" />
                      Paramètres
                    </div>
                  </div>
                </div>

                {/* Main content */}
                <div className="flex-1 p-5 md:p-7 space-y-6">
                  {/* Header */}
                  <div className="flex items-center justify-between">
                    <h3 className="font-display text-base md:text-lg font-bold tracking-tight text-foreground">
                      Tableau de bord
                    </h3>
                    <div className="flex items-center gap-2 px-3 py-1.5 rounded-lg bg-brand text-white text-xs font-medium">
                      <Plus className="h-3 w-3" />
                      <span className="hidden sm:inline">Ajouter un article</span>
                    </div>
                  </div>

                  {/* Stat cards */}
                  <StaggeredList
                    className="grid grid-cols-2 lg:grid-cols-4 gap-3"
                    staggerDelay={0.08}
                  >
                    {statCards.map((card, idx) => (
                      <StaggeredItem key={idx}>
                        <div className="rounded-xl bg-background/50 border p-3.5 md:p-4">
                          <div className={`w-8 h-8 rounded-[8px] flex items-center justify-center ${card.iconBg} mb-2.5`}>
                            <card.icon className={`h-4 w-4 ${card.iconColor}`} aria-hidden="true" />
                          </div>
                          <div className="font-display text-xl md:text-2xl font-extrabold tracking-tight leading-none mb-0.5">
                            {card.value}
                          </div>
                          <div className="text-[11px] font-medium text-muted-foreground">
                            {card.label}
                          </div>
                        </div>
                      </StaggeredItem>
                    ))}
                  </StaggeredList>

                  {/* Charts row */}
                  <div className="grid grid-cols-1 md:grid-cols-2 gap-3">
                    {/* Status bar chart */}
                    <div className="rounded-xl bg-background/50 border p-4">
                      <h4 className="text-sm font-bold text-foreground mb-4">
                        Articles par statut
                      </h4>
                      <div className="space-y-2.5">
                        {MOCK_STATUS_CHART.map((bar) => (
                          <div key={bar.name} className="flex items-center gap-3">
                            <span className="text-[11px] text-muted-foreground w-20 text-right flex-shrink-0">
                              {bar.name}
                            </span>
                            <div className="flex-1 h-5 bg-muted/40 rounded-md overflow-hidden">
                              <div
                                className="h-full rounded-md transition-all duration-700"
                                style={{
                                  width: `${Math.max((bar.count / maxBarValue) * 100, 4)}%`,
                                  backgroundColor: bar.fill,
                                }}
                              />
                            </div>
                            <span className="text-[11px] font-medium text-foreground w-8 text-right">
                              {bar.count}
                            </span>
                          </div>
                        ))}
                      </div>
                    </div>

                    {/* Category breakdown */}
                    <div className="rounded-xl bg-background/50 border p-4">
                      <h4 className="text-sm font-bold text-foreground mb-4">
                        Articles par catégorie
                      </h4>
                      <div className="space-y-3">
                        {MOCK_CATEGORY_DATA.map((cat) => (
                          <div key={cat.name} className="flex items-center gap-3">
                            <div
                              className="w-2.5 h-2.5 rounded-full flex-shrink-0"
                              style={{ backgroundColor: cat.color }}
                            />
                            <span className="text-xs text-foreground flex-1">{cat.name}</span>
                            <span className="text-xs font-medium text-foreground">{cat.count}</span>
                          </div>
                        ))}
                      </div>
                    </div>
                  </div>

                  {/* Tables row (desktop only) */}
                  <div className="hidden md:grid grid-cols-2 gap-3">
                    {/* Lists overview */}
                    <div className="rounded-xl bg-background/50 border p-4">
                      <div className="flex items-center justify-between mb-3">
                        <h4 className="text-sm font-bold text-foreground">
                          Aperçu des listes
                        </h4>
                        <span className="text-[11px] font-medium text-brand">Voir tout →</span>
                      </div>
                      <table className="w-full text-left">
                        <thead>
                          <tr className="text-[10px] font-semibold text-muted-foreground uppercase tracking-wider">
                            <th className="pb-2 font-semibold">Nom</th>
                            <th className="pb-2 font-semibold text-right">Articles</th>
                          </tr>
                        </thead>
                        <tbody>
                          {MOCK_LISTS_OVERVIEW.map((list, idx) => (
                            <tr key={idx} className="border-t border-border/30">
                              <td className="py-2">
                                <span className="text-xs font-medium text-foreground">
                                  {list.listName}
                                </span>
                              </td>
                              <td className="py-2 text-right">
                                <span className="text-xs text-muted-foreground">
                                  {list.itemsCount}
                                </span>
                              </td>
                            </tr>
                          ))}
                        </tbody>
                      </table>
                    </div>

                    {/* Recently updated */}
                    <div className="rounded-xl bg-background/50 border p-4">
                      <h4 className="text-sm font-bold text-foreground mb-3">
                        Récemment mis à jour
                      </h4>
                      <table className="w-full text-left">
                        <thead>
                          <tr className="text-[10px] font-semibold text-muted-foreground uppercase tracking-wider">
                            <th className="pb-2 font-semibold">Article</th>
                            <th className="pb-2 font-semibold text-right">Statut</th>
                          </tr>
                        </thead>
                        <tbody>
                          {MOCK_RECENT_ITEMS.map((item, idx) => (
                            <tr key={idx} className="border-t border-border/30">
                              <td className="py-2">
                                <span className="text-xs font-medium text-foreground">
                                  {item.name}
                                </span>
                              </td>
                              <td className="py-2 text-right">
                                <span
                                  className={`inline-flex items-center px-1.5 py-0.5 rounded-full text-[10px] font-medium ${STATUS_COLORS[item.status] || "bg-muted text-muted-foreground"}`}
                                >
                                  {STATUS_LABELS[item.status] || item.status}
                                </span>
                              </td>
                            </tr>
                          ))}
                        </tbody>
                      </table>
                    </div>
                  </div>
                </div>
              </div>

              <div className="h-16 bg-gradient-to-t from-card to-transparent -mt-16 relative z-10 pointer-events-none" />
            </div>
          </div>
        </BlurFade>
      </div>
    </section>
  );
}

export function CTASection() {
  return (
    <section aria-labelledby="cta-heading" className="relative overflow-hidden py-24 md:py-32">
      <DotPattern
        className="opacity-20 [mask-image:radial-gradient(ellipse_at_center,black_20%,transparent_70%)]"
        width={20}
        height={20}
        cr={1}
      />
      <div className="relative max-w-6xl mx-auto px-6 text-center">
        <BlurFade delay={0.1} inView>
          <h2
            id="cta-heading"
            className="font-display text-3xl md:text-4xl font-semibold tracking-tight mb-4"
          >
            Prêt à organiser votre inventaire ?
          </h2>
        </BlurFade>
        <BlurFade delay={0.2} inView>
          <p className="text-muted-foreground text-lg max-w-xl mx-auto mb-8">
            Créez votre compte gratuitement et commencez à gérer votre inventaire
            dès aujourd'hui.
          </p>
        </BlurFade>
        <BlurFade delay={0.3} inView>
          <div className="flex flex-col items-center gap-4">
            <Button
              size="lg"
              className="h-12 px-8 text-base"
              asChild
            >
              <Link to="/signup">
                Commencer gratuitement
                <ArrowRight className="h-4 w-4 ml-2" aria-hidden="true" />
              </Link>
            </Button>
            <Link
              to="/login"
              className="text-sm text-muted-foreground hover:text-foreground transition-colors"
            >
              Déjà un compte ? Se connecter
            </Link>
          </div>
        </BlurFade>
      </div>
    </section>
  );
}
