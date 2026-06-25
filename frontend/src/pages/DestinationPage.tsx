import { Link, useLocation, useParams, useSearchParams } from 'react-router-dom'
import { motion } from 'framer-motion'
import { ArrowLeft, Clock, ExternalLink, MapPin, Plane, Wallet } from 'lucide-react'
import { useCountry, useHolidays, useTravelOverview } from '@/api/queries'
import type { TravelRecommendation } from '@/api/types'
import { ScoreRing } from '@/components/shared/ScoreRing'
import { FavoriteButton } from '@/components/shared/FavoriteButton'
import { CriterionBreakdown } from '@/components/results/CriterionBreakdown'
import { Badge } from '@/components/ui/badge'
import { Skeleton } from '@/components/ui/skeleton'
import { formatBrl, formatDateLong } from '@/lib/format'

export function DestinationPage() {
  const { countryCode = '' } = useParams()
  const location = useLocation()
  const [params] = useSearchParams()
  const recommendation = (location.state as { recommendation?: TravelRecommendation } | null)?.recommendation

  const { data: country, isLoading: loadingCountry } = useCountry(countryCode)
  const { data: holidays } = useHolidays(countryCode)
  const { data: overview, isLoading: loadingOverview } = useTravelOverview(
    recommendation ? undefined : countryCode,
  )

  const profile = recommendation?.profile ?? overview?.profile
  const exchange = recommendation?.exchangeToBrl ?? overview?.exchangeToBrl
  const isLoading = loadingCountry || (!recommendation && loadingOverview)

  const backHref = params.toString() ? `/resultados?${params.toString()}` : '/buscar'

  if (isLoading || !country) {
    return (
      <div className="mx-auto max-w-3xl px-4 py-8">
        <Skeleton className="h-64 w-full rounded-3xl" />
        <Skeleton className="mt-6 h-8 w-1/2" />
        <Skeleton className="mt-3 h-24 w-full" />
      </div>
    )
  }

  return (
    <div className="mx-auto max-w-3xl pb-16">
      <div className="relative h-64 w-full overflow-hidden bg-muted sm:h-80">
        {profile?.imageUrl ? (
          <img src={profile.imageUrl} alt={country.name} className="size-full object-cover" />
        ) : (
          <div className="flex size-full items-center justify-center text-7xl">{profile?.flagEmoji ?? '🌍'}</div>
        )}
        <div className="absolute inset-0 bg-gradient-to-t from-background via-background/10 to-transparent" />
        <Link
          to={backHref}
          className="absolute left-4 top-4 flex items-center gap-1.5 rounded-full bg-background/90 px-3 py-1.5 text-sm font-medium shadow backdrop-blur"
        >
          <ArrowLeft className="size-4" />
          Voltar
        </Link>
      </div>

      <div className="-mt-10 space-y-8 px-4">
        <motion.div
          initial={{ opacity: 0, y: 16 }}
          animate={{ opacity: 1, y: 0 }}
          className="flex items-end justify-between rounded-3xl border border-border bg-card p-5 shadow-sm"
        >
          <div>
            <h1 className="font-display text-2xl font-semibold sm:text-3xl">
              {profile?.flagEmoji} {country.localizedName ?? country.name}
            </h1>
            <p className="text-sm text-muted-foreground">{country.subregion}</p>
          </div>
          {recommendation && (
            <div className="flex items-center gap-3">
              <FavoriteButton recommendation={recommendation} />
              <ScoreRing score={recommendation.tripScore} size={64} label="score" />
            </div>
          )}
        </motion.div>

        {profile?.extract && <p className="text-balance leading-relaxed text-muted-foreground">{profile.extract}</p>}

        {profile?.wikipediaUrl && (
          <a
            href={profile.wikipediaUrl}
            target="_blank"
            rel="noreferrer"
            className="inline-flex items-center gap-1.5 text-sm font-medium text-primary hover:underline"
          >
            Saiba mais na Wikipédia
            <ExternalLink className="size-3.5" />
          </a>
        )}

        {recommendation && (
          <section className="space-y-3">
            <h2 className="font-display text-lg font-semibold">Por que esse destino?</h2>
            <CriterionBreakdown breakdown={recommendation.breakdown} />
          </section>
        )}

        {recommendation?.feasibility && (
          <section className="grid grid-cols-2 gap-3 sm:grid-cols-4">
            <Fact icon={Plane} label="Distância" value={`${Math.round(recommendation.feasibility.travelEffort.distanceKm).toLocaleString('pt-BR')} km`} />
            <Fact
              icon={Clock}
              label="Tempo de voo"
              value={`${Math.round(recommendation.feasibility.travelEffort.estimatedTravelHoursMin)}–${Math.round(recommendation.feasibility.travelEffort.estimatedTravelHoursMax)}h`}
            />
            <Fact
              icon={Wallet}
              label="Custo terrestre/dia"
              value={formatBrl(recommendation.feasibility.groundCost.estimatedDailyPerPerson)}
            />
            <Fact icon={MapPin} label="Câmbio" value={exchange ? `1 ${exchange.currency} = ${formatBrl(exchange.valueInReais)}` : '—'} />
          </section>
        )}

        <section className="space-y-2">
          <h2 className="font-display text-lg font-semibold">Sobre {country.localizedName ?? country.name}</h2>
          <div className="flex flex-wrap gap-2 text-sm">
            <Badge variant="outline">Capital: {country.capitals.join(', ') || '—'}</Badge>
            <Badge variant="outline">Idiomas: {country.languages.join(', ') || '—'}</Badge>
            <Badge variant="outline">Moeda: {country.currencies.join(', ') || '—'}</Badge>
          </div>
        </section>

        {holidays && holidays.length > 0 && (
          <section className="space-y-3">
            <h2 className="font-display text-lg font-semibold">Próximos feriados</h2>
            <div className="space-y-2">
              {holidays.slice(0, 6).map((h) => (
                <div key={`${h.date}-${h.name}`} className="flex items-center justify-between rounded-xl border border-border px-4 py-2.5 text-sm">
                  <span className="font-medium">{h.localName}</span>
                  <span className="text-muted-foreground">{formatDateLong(h.date)}</span>
                </div>
              ))}
            </div>
          </section>
        )}
      </div>
    </div>
  )
}

function Fact({ icon: Icon, label, value }: { icon: typeof Plane; label: string; value: string }) {
  return (
    <div className="flex flex-col gap-1 rounded-2xl border border-border bg-card p-3">
      <Icon className="size-4 text-muted-foreground" />
      <span className="text-sm font-semibold">{value}</span>
      <span className="text-xs text-muted-foreground">{label}</span>
    </div>
  )
}
