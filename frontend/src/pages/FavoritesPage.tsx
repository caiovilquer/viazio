import { Link, useNavigate } from "react-router-dom";
import { AnimatePresence, motion } from "framer-motion";
import { CalendarDays, Columns3, Heart, Search } from "lucide-react";
import { useMeta } from "@/api/queries";
import { useFavorites, type FavoriteEntry } from "@/lib/favorites";
import type { ApiMetaResponse } from "@/api/types";
import { RecommendationCard } from "@/components/results/RecommendationCard";
import { Reveal } from "@/components/shared/Reveal";
import { Button } from "@/components/ui/button";
import { formatDate, formatDateRange } from "@/lib/format";
import { spring } from "@/lib/motion";

function savedCaption(entry: FavoriteEntry, meta?: ApiMetaResponse): string {
  const c = entry.context;
  if (c?.from && c?.to) {
    const range = formatDateRange(c.from, c.to);
    const prof = c.profile
      ? meta?.profiles.find((p) => p.key === c.profile)?.label
      : c.weights && Object.keys(c.weights).length > 0
        ? "Personalizado"
        : undefined;
    return prof ? `${range} · ${prof}` : range;
  }
  return `Salvo em ${formatDate(entry.savedAt.slice(0, 10))}`;
}

export function FavoritesPage() {
  const favorites = useFavorites();
  const { data: meta } = useMeta();
  const navigate = useNavigate();

  function compareSaved() {
    const recs = favorites.map((f) => f.recommendation);
    const codes = recs.map((r) => r.countryCode).join(",");
    navigate(`/comparar?codes=${codes}`, {
      state: { recommendations: recs, saved: favorites },
    });
  }

  return (
    <div className="mx-auto max-w-6xl px-4 py-10 pb-24 lg:py-14">
      <Reveal className="mb-8 flex flex-wrap items-end justify-between gap-4">
        <div>
          <p className="mb-3 text-[0.7rem] font-semibold uppercase tracking-[0.22em] text-gold/80">
            Salvos
          </p>
          <h1 className="font-display text-3xl tracking-tight sm:text-4xl">
            Seus destinos salvos
          </h1>
          <p className="mt-2 text-sm text-muted-foreground">
            {favorites.length === 0
              ? "Toque no coração de um destino para guardá-lo aqui."
              : `${favorites.length} destino${favorites.length === 1 ? "" : "s"} guardado${favorites.length === 1 ? "" : "s"} para depois.`}
          </p>
        </div>
        {favorites.length >= 2 && (
          <Button
            variant="outline"
            className="gap-2 rounded-full"
            onClick={compareSaved}
          >
            <Columns3 className="size-4" />
            Comparar salvos
          </Button>
        )}
      </Reveal>

      {favorites.length === 0 ? (
        <motion.div
          initial={{ opacity: 0, y: 12 }}
          animate={{ opacity: 1, y: 0 }}
          className="flex flex-col items-center gap-5 rounded-3xl border border-hairline bg-surface/40 px-6 py-16 text-center"
        >
          <span className="flex size-16 items-center justify-center rounded-full border border-gold/30 bg-gold/5 text-gold">
            <Heart className="size-7" />
          </span>
          <div className="max-w-sm space-y-1.5">
            <p className="font-display text-lg">Nada guardado ainda</p>
            <p className="text-sm text-muted-foreground">
              Conforme você explora, salve os destinos que te chamarem. Eles
              ficam aqui prontos para comparar depois.
            </p>
          </div>
          <div className="flex flex-wrap justify-center gap-3">
            <Button asChild className="rounded-full glow-coral">
              <Link to="/buscar">
                <Search className="size-4" />
                Buscar destinos
              </Link>
            </Button>
            <Button asChild variant="outline" className="rounded-full">
              <Link to="/janelas">Ver melhores janelas</Link>
            </Button>
          </div>
        </motion.div>
      ) : (
        <div className="grid gap-x-5 gap-y-6 sm:grid-cols-2 lg:grid-cols-3">
          <AnimatePresence mode="popLayout">
            {favorites.map((entry, i) => (
              <motion.div
                key={entry.countryCode}
                layout
                exit={{ opacity: 0, scale: 0.92 }}
                transition={spring.soft}
              >
                <RecommendationCard
                  recommendation={entry.recommendation}
                  rank={i + 1}
                  searchQuery=""
                  showRank={false}
                />
                <p className="mt-2 flex items-center gap-1.5 px-1 text-xs text-muted-foreground">
                  <CalendarDays className="size-3.5 shrink-0 text-gold/70" />
                  {savedCaption(entry, meta)}
                </p>
              </motion.div>
            ))}
          </AnimatePresence>
        </div>
      )}
    </div>
  );
}
