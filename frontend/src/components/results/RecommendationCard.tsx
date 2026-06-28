import { useMemo, useState } from "react";
import { motion } from "framer-motion";
import { Link } from "react-router-dom";
import { Check, AlertTriangle } from "lucide-react";
import type { Exchange, TravelRecommendation } from "@/api/types";
import { useDestinationImage } from "@/api/images";
import { ScoreRing } from "@/components/shared/ScoreRing";
import { ScoreComposition } from "@/components/shared/ScoreComposition";
import { FavoriteButton } from "@/components/shared/FavoriteButton";
import { Flag } from "@/components/shared/Flag";
import { favoriteContextFromParams } from "@/lib/favorites";
import { destinationPhotoUrl } from "@/lib/destination-image";
import { formatExchange } from "@/lib/format";
import { ease } from "@/lib/motion";
import { cn } from "@/lib/utils";

function CardBanner({
  recommendation,
  photoUrl,
}: {
  recommendation: TravelRecommendation;
  photoUrl: string | null;
}) {
  const { countryName, countryCode } = recommendation;
  const [loaded, setLoaded] = useState(false);

  return (
    <div className="relative h-40 w-full overflow-hidden sm:h-44">
      {/* Camada base — lavagem da cor da bandeira + bandeira emoldurada (espaço reservado + alternativa graciosa) */}
      <div className="absolute inset-0 bg-[linear-gradient(140deg,var(--surface-3),var(--surface-1))]">
        <Flag
          code={countryCode}
          className="absolute inset-0 size-full scale-150 rounded-none object-cover opacity-35 blur-2xl ring-0"
        />
        {!photoUrl && (
          <div className="absolute inset-0 flex items-center justify-center">
            <Flag
              code={countryCode}
              className="h-[4.5rem] w-[6.75rem] rounded-lg object-cover shadow-2xl ring-1 ring-white/15 transition-transform duration-500 group-hover:scale-[1.05]"
            />
          </div>
        )}
      </div>

      {/* Foto real do destino por cima, entrando com esmaecimento */}
      {photoUrl && (
        <img
          src={photoUrl}
          alt={countryName}
          loading="lazy"
          onLoad={() => setLoaded(true)}
          className={cn(
            "absolute inset-0 size-full object-cover transition-[opacity,transform] duration-700 ease-out group-hover:scale-[1.05]",
            loaded ? "opacity-100" : "opacity-0",
          )}
        />
      )}

      <div className="absolute inset-0 bg-gradient-to-t from-background/90 via-background/25 to-transparent" />

      <div className="absolute inset-x-4 bottom-3 flex items-center gap-2.5">
        <Flag code={countryCode} className="h-5 w-7 shrink-0 shadow-sm" />
        <p className="truncate font-display text-xl font-semibold tracking-tight text-foreground drop-shadow-sm">
          {countryName}
        </p>
      </div>
    </div>
  );
}

export function RecommendationCard({
  recommendation,
  rank,
  searchQuery,
  selectable = false,
  selected = false,
  selectDisabled = false,
  showRank = true,
  emphasized = false,
  onToggleSelect,
  onHoverStart,
  onHoverEnd,
  originExchangeToBrl,
  originCountryCode,
}: {
  recommendation: TravelRecommendation;
  rank: number;
  searchQuery: string;
  originExchangeToBrl?: Exchange | null;
  originCountryCode?: string;
  selectable?: boolean;
  selected?: boolean;
  selectDisabled?: boolean;
  showRank?: boolean;
  /** Destacado de fora (ex.: passar o mouse no pin correspondente no CandidatesMap). */
  emphasized?: boolean;
  onToggleSelect?: () => void;
  onHoverStart?: () => void;
  onHoverEnd?: () => void;
}) {
  const {
    exchangeToBrl,
    feasibility,
    profile,
    countryCode: destinationCountryCode,
  } = recommendation;
  const exchangeLabel = formatExchange(
    exchangeToBrl,
    originExchangeToBrl,
    originCountryCode,
    destinationCountryCode,
  );
  const savedContext = useMemo(
    () =>
      searchQuery
        ? favoriteContextFromParams(new URLSearchParams(searchQuery))
        : undefined,
    [searchQuery],
  );
  const { data: cityPhoto } = useDestinationImage(
    feasibility?.destination.name,
  );
  const backendPhoto = destinationPhotoUrl(profile?.imageUrl);
  const photoUrl = cityPhoto ?? backendPhoto ?? null;
  // Distância já aparece no contraponto "Viagem longa: ~X km" — não repetir na linha de metadados.
  const distanceInTradeoff = recommendation.tradeoffs.some((t) =>
    t.includes("km"),
  );

  const body = (
    <>
      <CardBanner recommendation={recommendation} photoUrl={photoUrl} />

      <div className="space-y-3.5 p-5">
        <div className="flex items-start justify-between gap-3">
          <div className="flex min-w-0 flex-1 flex-wrap gap-1.5">
            {recommendation.highlights.slice(0, 2).map((h) => (
              <span
                key={h}
                className="inline-flex items-center gap-1.5 rounded-full border border-hairline bg-surface-2/60 px-2.5 py-0.5 text-xs text-foreground/85"
              >
                <span className="size-1 rounded-full bg-gold/80" />
                {h}
              </span>
            ))}
            {recommendation.tradeoffs.slice(0, 1).map((t) => (
              <span
                key={t}
                className="inline-flex items-center gap-1 rounded-full border border-chart-3/25 bg-chart-3/10 px-2.5 py-0.5 text-xs text-chart-3"
              >
                <AlertTriangle className="size-3" />
                {t}
              </span>
            ))}
            {recommendation.highlights.length === 0 &&
              recommendation.tradeoffs.length === 0 && (
                <span className="text-sm text-muted-foreground">
                  Destino tranquilo, sem grandes destaques.
                </span>
              )}
          </div>
          <ScoreRing
            score={recommendation.tripScore}
            size={48}
            strokeWidth={5}
          />
        </div>

        <ScoreComposition breakdown={recommendation.breakdown} size="sm" />

        {(exchangeLabel || (feasibility && !distanceInTradeoff)) && (
          <div className="flex flex-wrap items-center gap-x-3 gap-y-1 text-xs text-muted-foreground">
            {exchangeLabel && <span>{exchangeLabel}</span>}
            {exchangeLabel && feasibility && !distanceInTradeoff && (
              <span className="size-0.5 rounded-full bg-muted-foreground/50" />
            )}
            {feasibility && !distanceInTradeoff && (
              <span className="tabular-nums">
                {Math.round(feasibility.travelEffort.distanceKm).toLocaleString(
                  "pt-BR",
                )}{" "}
                km
              </span>
            )}
          </div>
        )}
      </div>
    </>
  );

  return (
    <motion.div
      layout
      initial={{ opacity: 0, y: 22 }}
      animate={{ opacity: 1, y: 0 }}
      transition={{
        duration: 0.4,
        delay: Math.min(rank * 0.05, 0.4),
        ease: ease.out,
      }}
      whileHover={{ y: -4 }}
      onMouseEnter={onHoverStart}
      onMouseLeave={onHoverEnd}
      className={cn(
        "group relative overflow-hidden rounded-2xl border bg-surface/70 transition-[box-shadow,border-color]",
        selected
          ? "border-gold/50 ring-1 ring-gold/30 elevate-lg"
          : emphasized
            ? "border-gold/35 ring-1 ring-gold/20 elevate-lg"
            : "border-hairline elevate hover:border-foreground/15 hover:elevate-lg",
        selectable && selectDisabled && !selected && "opacity-50",
      )}
    >
      {showRank && (
        <span className="absolute left-3 top-3 z-10 inline-flex items-center rounded-full border border-hairline bg-background/60 px-2.5 py-1 text-xs font-semibold backdrop-blur">
          <span className="text-gold">#</span>
          {rank}
        </span>
      )}

      {selectable ? (
        <motion.button
          type="button"
          whileTap={{ scale: 0.9 }}
          onClick={onToggleSelect}
          disabled={selectDisabled && !selected}
          aria-pressed={selected}
          aria-label={
            selected ? "Remover da comparação" : "Selecionar para comparar"
          }
          className={cn(
            "absolute right-3 top-3 z-10 flex size-8 items-center justify-center rounded-full border backdrop-blur transition-colors",
            selected
              ? "border-gold bg-gold text-gold-foreground"
              : "border-hairline bg-background/50 text-transparent hover:border-gold/50",
          )}
        >
          <Check className="size-4" strokeWidth={3} />
        </motion.button>
      ) : (
        <FavoriteButton
          recommendation={recommendation}
          context={savedContext}
          size="sm"
          className="absolute right-3 top-3 z-10"
        />
      )}

      {selectable ? (
        <button
          type="button"
          onClick={selectDisabled && !selected ? undefined : onToggleSelect}
          className="block w-full text-left"
        >
          {body}
        </button>
      ) : (
        <Link
          to={`/destino/${recommendation.countryCode}?${searchQuery}`}
          state={{
            recommendation,
            originExchangeToBrl: originExchangeToBrl ?? null,
          }}
          className="block"
        >
          {body}
        </Link>
      )}
    </motion.div>
  );
}
