import { motion } from 'framer-motion'
import { Link } from 'react-router-dom'
import { Check, Plane, Sparkles, AlertTriangle } from 'lucide-react'
import type { TravelRecommendation } from '@/api/types'
import { ScoreRing } from '@/components/shared/ScoreRing'
import { FavoriteButton } from '@/components/shared/FavoriteButton'
import { Badge } from '@/components/ui/badge'
import { formatBrl } from '@/lib/format'
import { cn } from '@/lib/utils'

export function RecommendationCard({
  recommendation,
  rank,
  searchQuery,
  selectable = false,
  selected = false,
  selectDisabled = false,
  onToggleSelect,
}: {
  recommendation: TravelRecommendation
  rank: number
  searchQuery: string
  selectable?: boolean
  selected?: boolean
  selectDisabled?: boolean
  onToggleSelect?: () => void
}) {
  const { profile, exchangeToBrl, feasibility } = recommendation

  const body = (
    <>
      <div className="relative h-40 w-full overflow-hidden bg-muted sm:h-48">
        {profile.imageUrl ? (
          <img
            src={profile.imageUrl}
            alt={recommendation.countryName}
            loading="lazy"
            className="size-full object-cover transition-transform duration-500 group-hover:scale-105"
          />
        ) : (
          <div className="flex size-full items-center justify-center text-5xl">
            {profile.flagEmoji ?? '🌍'}
          </div>
        )}
        <div className="absolute inset-0 bg-gradient-to-t from-black/60 via-black/0 to-black/0" />
        <div className="absolute left-3 top-3 flex items-center gap-1.5 rounded-full bg-background/90 px-2.5 py-1 text-xs font-semibold backdrop-blur">
          <Sparkles className="size-3 text-primary" />#{rank}
        </div>
        <div className="absolute bottom-3 left-4 right-4 flex items-end justify-between text-white">
          <div>
            <p className="font-display text-xl font-semibold drop-shadow">
              {profile.flagEmoji} {recommendation.countryName}
            </p>
          </div>
        </div>
      </div>

      <div className="flex items-start justify-between gap-3 p-4">
        <div className="min-w-0 flex-1 space-y-2">
          <p className="line-clamp-2 text-sm text-muted-foreground">{recommendation.summary}</p>
          <div className="flex flex-wrap gap-1.5">
            {recommendation.highlights.slice(0, 2).map((h) => (
              <Badge key={h} variant="secondary" className="gap-1 font-normal">
                {h}
              </Badge>
            ))}
            {recommendation.tradeoffs.slice(0, 1).map((t) => (
              <Badge key={t} variant="outline" className="gap-1 font-normal text-muted-foreground">
                <AlertTriangle className="size-3" />
                {t}
              </Badge>
            ))}
          </div>
          <div className="flex flex-wrap items-center gap-3 pt-1 text-xs text-muted-foreground">
            {exchangeToBrl && (
              <span>1 {exchangeToBrl.currency} = {formatBrl(exchangeToBrl.valueInReais)}</span>
            )}
            {feasibility && (
              <span className="flex items-center gap-1">
                <Plane className="size-3" />
                {Math.round(feasibility.travelEffort.distanceKm).toLocaleString('pt-BR')} km
              </span>
            )}
          </div>
        </div>
        <ScoreRing score={recommendation.tripScore} size={56} strokeWidth={5} className="shrink-0" />
      </div>
    </>
  )

  return (
    <motion.div
      layout
      initial={{ opacity: 0, y: 24 }}
      animate={{ opacity: 1, y: 0 }}
      transition={{ duration: 0.4, delay: Math.min(rank * 0.06, 0.4), ease: [0.22, 1, 0.36, 1] }}
      whileHover={{ y: -4 }}
      className={cn(
        'group relative overflow-hidden rounded-3xl border bg-card shadow-sm transition-shadow hover:shadow-xl',
        selected ? 'border-primary ring-2 ring-primary/30' : 'border-border',
        selectable && selectDisabled && !selected && 'opacity-50',
      )}
    >
      {selectable ? (
        <motion.button
          type="button"
          whileTap={{ scale: 0.9 }}
          onClick={onToggleSelect}
          disabled={selectDisabled && !selected}
          aria-pressed={selected}
          aria-label={selected ? 'Remover da comparação' : 'Selecionar para comparar'}
          className={cn(
            'absolute right-3 top-3 z-10 flex size-7 items-center justify-center rounded-full border-2 shadow backdrop-blur transition-colors',
            selected
              ? 'border-primary bg-primary text-primary-foreground'
              : 'border-white/80 bg-background/70 text-transparent hover:border-primary/60',
          )}
        >
          <Check className="size-4" strokeWidth={3} />
        </motion.button>
      ) : (
        <FavoriteButton recommendation={recommendation} size="sm" className="absolute right-3 top-3 z-10" />
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
        <Link to={`/destino/${recommendation.countryCode}?${searchQuery}`} state={{ recommendation }} className="block">
          {body}
        </Link>
      )}
    </motion.div>
  )
}
