import { motion } from 'framer-motion'
import { Link } from 'react-router-dom'
import { Plane, Sparkles, AlertTriangle } from 'lucide-react'
import type { TravelRecommendation } from '@/api/types'
import { ScoreRing } from '@/components/shared/ScoreRing'
import { Badge } from '@/components/ui/badge'
import { formatBrl } from '@/lib/format'

export function RecommendationCard({
  recommendation,
  rank,
  searchQuery,
}: {
  recommendation: TravelRecommendation
  rank: number
  searchQuery: string
}) {
  const { profile, exchangeToBrl, feasibility } = recommendation

  return (
    <motion.div
      layout
      initial={{ opacity: 0, y: 24 }}
      animate={{ opacity: 1, y: 0 }}
      transition={{ duration: 0.4, delay: Math.min(rank * 0.06, 0.4), ease: [0.22, 1, 0.36, 1] }}
      whileHover={{ y: -4 }}
      className="group overflow-hidden rounded-3xl border border-border bg-card shadow-sm transition-shadow hover:shadow-xl"
    >
      <Link to={`/destino/${recommendation.countryCode}?${searchQuery}`} state={{ recommendation }} className="block">
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
      </Link>
    </motion.div>
  )
}
