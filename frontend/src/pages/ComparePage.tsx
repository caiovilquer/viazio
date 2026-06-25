import { useMemo, useState, type ReactNode } from 'react'
import { Link, useLocation, useSearchParams } from 'react-router-dom'
import { motion } from 'framer-motion'
import { ArrowLeft, Banknote, Clock, ExternalLink, Frown, Plane, ShieldCheck, Trophy, X } from 'lucide-react'
import { useMeta, useRecommendations } from '@/api/queries'
import type { TravelRecommendation } from '@/api/types'
import { criteriaToRequest, searchParamsToCriteria } from '@/lib/search-params'
import { winnerIndices } from '@/lib/compare'
import { formatBrl } from '@/lib/format'
import { ScoreRing } from '@/components/shared/ScoreRing'
import { Skeleton } from '@/components/ui/skeleton'
import { Button } from '@/components/ui/button'
import { cn } from '@/lib/utils'

export function ComparePage() {
  const [params] = useSearchParams()
  const location = useLocation()
  const { data: meta } = useMeta()

  const stateRecommendations = (location.state as { recommendations?: TravelRecommendation[] } | null)?.recommendations
  const codes = useMemo(() => params.get('codes')?.split(',').filter(Boolean) ?? [], [params])

  const criteria = useMemo(() => searchParamsToCriteria(params), [params])
  const request = !stateRecommendations && criteria ? criteriaToRequest(criteria) : null
  const { data, isLoading } = useRecommendations(request)

  const baseRecommendations = useMemo(() => {
    if (stateRecommendations) return stateRecommendations
    if (!data) return []
    return data.recommendations.filter((r) => codes.includes(r.countryCode))
  }, [stateRecommendations, data, codes])

  const [visibleCodes, setVisibleCodes] = useState(codes)
  const recommendations = baseRecommendations
    .filter((r) => visibleCodes.includes(r.countryCode))
    .sort((a, b) => b.tripScore - a.tripScore)

  const backParams = new URLSearchParams(params)
  backParams.delete('codes')
  const backHref = `/resultados?${backParams.toString()}`

  function removeDestination(code: string) {
    setVisibleCodes((current) => current.filter((c) => c !== code))
  }

  const loading = !stateRecommendations && isLoading

  if (loading) {
    return (
      <div className="mx-auto max-w-5xl px-4 py-8">
        <Skeleton className="h-8 w-48" />
        <Skeleton className="mt-6 h-96 w-full rounded-3xl" />
      </div>
    )
  }

  if (recommendations.length < 2) {
    return (
      <div className="mx-auto flex max-w-md flex-col items-center gap-4 px-4 py-20 text-center">
        <Frown className="size-10 text-muted-foreground" />
        <p className="text-muted-foreground">Selecione ao menos 2 destinos nos resultados para compará-los.</p>
        <Button asChild>
          <Link to={backHref}>Voltar para os resultados</Link>
        </Button>
      </div>
    )
  }

  const tripScores = recommendations.map((r) => r.tripScore)
  const tripWinners = winnerIndices(tripScores, 'higher')
  const distanceWinners = winnerIndices(recommendations.map((r) => r.feasibility?.travelEffort.distanceKm ?? null), 'lower')
  const flightWinners = winnerIndices(
    recommendations.map((r) => r.feasibility?.travelEffort.estimatedTravelHoursMin ?? null),
    'lower',
  )
  const costWinners = winnerIndices(
    recommendations.map((r) => r.feasibility?.groundCost.estimatedDailyPerPerson ?? null),
    'lower',
  )
  const confidenceWinners = winnerIndices(recommendations.map((r) => r.dataQuality.confidenceScore), 'higher')

  const columns = recommendations.length

  return (
    <div className="mx-auto max-w-5xl px-4 py-8 pb-16">
      <div className="mb-6 flex items-center justify-between gap-4">
        <div>
          <Link to={backHref} className="mb-2 flex items-center gap-1.5 text-sm text-muted-foreground hover:text-foreground">
            <ArrowLeft className="size-3.5" />
            Voltar aos resultados
          </Link>
          <h1 className="font-display text-2xl font-semibold sm:text-3xl">Comparando {columns} destinos</h1>
        </div>
      </div>

      <div className="overflow-x-auto rounded-3xl border border-border">
        <div
          className="grid min-w-full"
          style={{ gridTemplateColumns: `156px repeat(${columns}, minmax(180px, 1fr))` }}
        >
          {/* Header row: destination identity */}
          <div className="contents">
            <div className="sticky left-0 z-10 border-b border-border bg-card" />
            {recommendations.map((rec) => (
              <div key={rec.countryCode} className="space-y-3 border-b border-border bg-card px-3 py-4">
                <div className="flex items-start justify-between gap-2">
                  <Link
                    to={`/destino/${rec.countryCode}`}
                    state={{ recommendation: rec }}
                    className="block flex-1 overflow-hidden rounded-xl"
                  >
                    <div className="relative h-20 w-full overflow-hidden bg-muted">
                      {rec.profile.imageUrl ? (
                        <img src={rec.profile.imageUrl} alt={rec.countryName} className="size-full object-cover" />
                      ) : (
                        <div className="flex size-full items-center justify-center text-3xl">
                          {rec.profile.flagEmoji ?? '🌍'}
                        </div>
                      )}
                    </div>
                  </Link>
                  <button
                    type="button"
                    onClick={() => removeDestination(rec.countryCode)}
                    className="flex size-6 shrink-0 items-center justify-center rounded-full text-muted-foreground hover:bg-muted hover:text-foreground"
                    aria-label={`Remover ${rec.countryName} da comparação`}
                  >
                    <X className="size-3.5" />
                  </button>
                </div>
                <div className="flex items-center justify-between gap-2">
                  <p className="min-w-0 truncate font-display text-sm font-semibold">
                    {rec.profile.flagEmoji} {rec.countryName}
                  </p>
                  <ScoreRing score={rec.tripScore} size={40} strokeWidth={4} />
                </div>
                <Link
                  to={`/destino/${rec.countryCode}`}
                  state={{ recommendation: rec }}
                  className="inline-flex items-center gap-1 text-xs font-medium text-primary hover:underline"
                >
                  Ver destino completo
                  <ExternalLink className="size-3" />
                </Link>
              </div>
            ))}
          </div>

          {/* Trip score row */}
          <CompareRow label="Nota geral" icon="🏆">
            {recommendations.map((rec, i) => (
              <ScoreCell key={rec.countryCode} value={rec.tripScore} isWinner={tripWinners.has(i)} />
            ))}
          </CompareRow>

          {/* Per-criterion rows, in canonical order */}
          {meta?.criteria.map((criterion) => {
            const scores = recommendations.map(
              (rec) => rec.breakdown.find((b) => b.criterion === criterion.key)?.score ?? null,
            )
            const availability = recommendations.map(
              (rec) => rec.breakdown.find((b) => b.criterion === criterion.key)?.available ?? false,
            )
            const winners = winnerIndices(scores.map((s, i) => (availability[i] ? s : null)), 'higher')
            return (
              <CompareRow key={criterion.key} label={criterion.label} icon={criterion.icon}>
                {recommendations.map((rec, i) => {
                  const entry = rec.breakdown.find((b) => b.criterion === criterion.key)
                  if (!entry?.available) {
                    return (
                      <Cell key={rec.countryCode}>
                        <span className="text-xs text-muted-foreground">Indisponível</span>
                      </Cell>
                    )
                  }
                  return <ScoreCell key={rec.countryCode} value={entry.score} isWinner={winners.has(i)} />
                })}
              </CompareRow>
            )
          })}

          {/* Distância */}
          <CompareRow label="Distância (km)" lucideIcon={Plane}>
            {recommendations.map((rec, i) => (
              <Cell key={rec.countryCode}>
                <span className={cn('text-sm', distanceWinners.has(i) && 'font-semibold text-primary')}>
                  {rec.feasibility ? `${Math.round(rec.feasibility.travelEffort.distanceKm).toLocaleString('pt-BR')} km` : '—'}
                  {distanceWinners.has(i) && <Trophy className="ml-1 inline size-3.5" />}
                </span>
              </Cell>
            ))}
          </CompareRow>

          {/* Tempo de voo */}
          <CompareRow label="Tempo de voo" lucideIcon={Clock}>
            {recommendations.map((rec, i) => (
              <Cell key={rec.countryCode}>
                <span className={cn('text-sm', flightWinners.has(i) && 'font-semibold text-primary')}>
                  {rec.feasibility
                    ? `${Math.round(rec.feasibility.travelEffort.estimatedTravelHoursMin)}–${Math.round(rec.feasibility.travelEffort.estimatedTravelHoursMax)}h`
                    : '—'}
                  {flightWinners.has(i) && <Trophy className="ml-1 inline size-3.5" />}
                </span>
              </Cell>
            ))}
          </CompareRow>

          {/* Custo terrestre */}
          <CompareRow label="Custo terrestre/dia" lucideIcon={Banknote}>
            {recommendations.map((rec, i) => (
              <Cell key={rec.countryCode}>
                <span className={cn('text-sm', costWinners.has(i) && 'font-semibold text-primary')}>
                  {rec.feasibility ? formatBrl(rec.feasibility.groundCost.estimatedDailyPerPerson) : '—'}
                  {costWinners.has(i) && <Trophy className="ml-1 inline size-3.5" />}
                </span>
              </Cell>
            ))}
          </CompareRow>

          {/* Câmbio (informativo, sem destaque de vencedor) */}
          <CompareRow label="Câmbio" lucideIcon={Banknote}>
            {recommendations.map((rec) => (
              <Cell key={rec.countryCode}>
                <span className="text-sm text-muted-foreground">
                  {rec.exchangeToBrl ? `1 ${rec.exchangeToBrl.currency} = ${formatBrl(rec.exchangeToBrl.valueInReais)}` : '—'}
                </span>
              </Cell>
            ))}
          </CompareRow>

          {/* Confiança dos dados */}
          <CompareRow label="Confiança dos dados" lucideIcon={ShieldCheck}>
            {recommendations.map((rec, i) => (
              <Cell key={rec.countryCode}>
                <span className={cn('text-sm', confidenceWinners.has(i) && 'font-semibold text-primary')}>
                  {Math.round(rec.dataQuality.confidenceScore)}%
                  {confidenceWinners.has(i) && <Trophy className="ml-1 inline size-3.5" />}
                </span>
              </Cell>
            ))}
          </CompareRow>
        </div>
      </div>

      <p className="mt-4 text-center text-xs text-muted-foreground">
        Câmbio é informativo e não entra na nota — poder de compra já está refletido no critério de custo.
      </p>
    </div>
  )
}

function CompareRow({
  label,
  icon,
  lucideIcon: LucideIcon,
  children,
}: {
  label: string
  icon?: string
  lucideIcon?: typeof Plane
  children: ReactNode
}) {
  return (
    <div className="contents">
      <div className="sticky left-0 z-10 flex items-start gap-1.5 border-b border-border bg-background px-3 py-3 text-xs font-medium leading-snug sm:text-sm">
        {LucideIcon ? (
          <LucideIcon className="mt-0.5 size-3.5 shrink-0 text-muted-foreground" />
        ) : (
          <span aria-hidden className="shrink-0">
            {icon}
          </span>
        )}
        <span>{label}</span>
      </div>
      {children}
    </div>
  )
}

function Cell({ children }: { children: ReactNode }) {
  return <div className="flex items-center border-b border-border px-3 py-3">{children}</div>
}

function ScoreCell({ value, isWinner }: { value: number; isWinner: boolean }) {
  return (
    <motion.div initial={{ opacity: 0 }} animate={{ opacity: 1 }} className="space-y-1.5 border-b border-border px-3 py-3">
      <div className="flex items-center gap-1.5">
        <span className="tabular-nums text-sm font-semibold">{Math.round(value)}</span>
        {isWinner && <Trophy className="size-3.5 text-primary" />}
      </div>
      <div className="h-1.5 w-full max-w-24 overflow-hidden rounded-full bg-muted">
        <div
          className={cn('h-full rounded-full', isWinner ? 'bg-primary' : 'bg-muted-foreground/40')}
          style={{ width: `${Math.max(0, Math.min(100, value))}%` }}
        />
      </div>
    </motion.div>
  )
}
