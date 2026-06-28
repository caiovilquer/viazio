import { useRef, useState, type ReactNode } from 'react'
import { Link, useLocation, useParams, useSearchParams } from 'react-router-dom'
import { motion, useReducedMotion, useScroll, useTransform } from 'framer-motion'
import { AlertTriangle, ArrowLeft, Clock, Coins, ExternalLink, Wallet } from 'lucide-react'
import { useCountry, useHolidays, useRecommendations, useTravelOverview } from '@/api/queries'
import { useDestinationImage } from '@/api/images'
import type { Region, TravelRecommendation, Exchange } from '@/api/types'
import { criteriaToRequest, searchParamsToCriteria } from '@/lib/search-params'
import { ScoreRing } from '@/components/shared/ScoreRing'
import { FavoriteButton } from '@/components/shared/FavoriteButton'
import { Flag } from '@/components/shared/Flag'
import { RouteGlyph } from '@/components/shared/Glyphs'
import { Reveal } from '@/components/shared/Reveal'
import { favoriteContextFromParams, type FavoriteContext } from '@/lib/favorites'
import { CriterionBreakdown } from '@/components/results/CriterionBreakdown'
import { ClimateChart } from '@/components/shared/ClimateChart'
import { Skeleton } from '@/components/ui/skeleton'
import { formatDateLong, formatExchange, formatExchangeParts, formatInOriginCurrency } from '@/lib/format'
import { heroItem, staggerContainer } from '@/lib/motion'
import { cn } from '@/lib/utils'

const regionPt: Record<Region, string> = {
  Africa: 'África',
  Americas: 'Américas',
  Asia: 'Ásia',
  Europe: 'Europa',
  Oceania: 'Oceania',
}

const classLabel: Record<string, string> = {
  short: 'Viagem curta',
  medium: 'Distância média',
  long: 'Viagem longa',
}

/** Wikipedia's country `imageUrl` is often just the flag — reject those. */
function isLikelyFlag(url?: string | null) {
  if (!url) return true
  const u = url.toLowerCase()
  return u.includes('flag') || u.endsWith('.svg')
}

function tzLabel(hours: number | null | undefined) {
  if (hours == null) return null
  if (hours === 0) return 'Mesmo fuso'
  return `${hours > 0 ? '+' : '−'}${Math.abs(hours)}h de fuso`
}

export function DestinationPage() {
  const { countryCode = '' } = useParams()
  const location = useLocation()
  const [params] = useSearchParams()
  const stateRecommendation = (location.state as {
    recommendation?: TravelRecommendation
    originExchangeToBrl?: Exchange | null
  } | null)?.recommendation
  const originExchangeFromState = (location.state as { originExchangeToBrl?: Exchange | null } | null)
    ?.originExchangeToBrl

  // `location.state` only survives client-side navigation (clicking a card). Opening a
  // link in a new tab, reloading, or sharing the URL gives a fresh tab with no state —
  // but the search itself (dates, origin, profile/weights) is still in the query string,
  // so re-run it scoped to just this country instead of falling back to the bare,
  // score-less overview below.
  const criteria = searchParamsToCriteria(params)
  const fallbackRequest =
    !stateRecommendation && criteria
      ? criteriaToRequest({ ...criteria, region: null, countries: [countryCode] })
      : null
  const { data: fallbackData, isLoading: loadingFallback } = useRecommendations(fallbackRequest)
  const fallbackRecommendation = fallbackData?.recommendations.find(
    (r) => r.countryCode.toLowerCase() === countryCode.toLowerCase(),
  )
  const recommendation = stateRecommendation ?? fallbackRecommendation
  const originExchangeToBrl = originExchangeFromState ?? fallbackData?.originExchangeToBrl ?? null
  const originCountryCode = fallbackData?.origin.countryCode ?? criteria?.origin.countryCode

  const { data: country, isLoading: loadingCountry } = useCountry(countryCode)
  const { data: holidays } = useHolidays(countryCode)
  const { data: overview, isLoading: loadingOverview } = useTravelOverview(
    recommendation || fallbackRequest ? undefined : countryCode,
  )

  const profile = recommendation?.profile ?? overview?.profile
  const exchange = recommendation?.exchangeToBrl ?? overview?.exchangeToBrl
  const exchangeParts = formatExchangeParts(exchange, originExchangeToBrl, originCountryCode, countryCode)
  const feasibility = recommendation?.feasibility ?? null
  const dailyCost =
    feasibility?.groundCost && feasibility.groundCost.estimatedDailyPerPerson > 0
      ? formatInOriginCurrency(
          feasibility.groundCost.estimatedDailyPerPerson,
          originExchangeToBrl,
          originCountryCode,
        )
      : null
  const totalCost =
    feasibility?.groundCost && feasibility.groundCost.estimatedDailyPerPerson > 0
      ? formatInOriginCurrency(
          feasibility.groundCost.estimatedTotal,
          originExchangeToBrl,
          originCountryCode,
        )
      : null

  const photoCity = feasibility?.destination.name ?? country?.capitals?.[0] ?? country?.name
  const { data: cityPhoto } = useDestinationImage(photoCity, 1920)
  const backendPhoto = profile && !isLikelyFlag(profile.imageUrl) ? profile.imageUrl : null
  const photoUrl = cityPhoto ?? backendPhoto ?? null

  const isLoading = loadingCountry || (!recommendation && (loadingFallback || loadingOverview))
  const backHref = params.toString() ? `/resultados?${params.toString()}` : '/buscar'
  const savedContext = favoriteContextFromParams(params)

  if (isLoading || !country) {
    return (
      <div className="pb-16">
        <Skeleton className="h-[56vh] min-h-[20rem] w-full rounded-none sm:min-h-[26rem]" />
        <div className="mx-auto max-w-4xl space-y-6 px-4 pt-10 sm:px-6">
          <Skeleton className="h-20 w-full rounded-2xl" />
          <Skeleton className="h-5 w-2/3" />
          <Skeleton className="h-28 w-full rounded-2xl" />
        </div>
      </div>
    )
  }

  const name = country.localizedName ?? country.name
  const eyebrow = regionPt[country.region] ?? country.region

  return (
    <div className="pb-16">
      <DestinationHero
        name={name}
        eyebrow={eyebrow}
        countryCode={countryCode}
        photoUrl={photoUrl}
        recommendation={recommendation}
        savedContext={savedContext}
        backHref={backHref}
      />

      {feasibility && (
        <Reveal className="relative z-20 mx-auto -mt-12 max-w-4xl px-4 sm:px-6">
          <div className="grid grid-cols-2 gap-px overflow-hidden rounded-2xl border border-hairline bg-hairline elevate-lg sm:grid-cols-4">
            <Stat
              glyph={<RouteGlyph className="size-4" />}
              label="Distância"
              value={`${Math.round(feasibility.travelEffort.distanceKm).toLocaleString('pt-BR')} km`}
              sub={classLabel[feasibility.travelEffort.classification]}
            />
            <Stat
              glyph={<Clock className="size-4" />}
              label="Tempo de voo"
              value={`${Math.round(feasibility.travelEffort.estimatedTravelHoursMin)}–${Math.round(
                feasibility.travelEffort.estimatedTravelHoursMax,
              )}h`}
              sub={tzLabel(feasibility.travelEffort.timeZoneDifferenceHours)}
            />
            <Stat
              glyph={<Wallet className="size-4" />}
              label="Custo / dia"
              value={dailyCost ? dailyCost.formatted : '—'}
              sub={
                totalCost
                  ? `≈ ${totalCost.formatted} no total${
                      dailyCost?.showFallbackNote ? ' · câmbio indisponível — valor em R$' : ''
                    }`
                  : 'Sem estimativa'
              }
            />
            <Stat
              glyph={<Coins className="size-4" />}
              label="Câmbio"
              value={exchangeParts?.amount ?? '—'}
              sub={
                exchangeParts
                  ? `${exchangeParts.unitDescription}${
                      exchangeParts.showFallbackNote ? ' · câmbio da origem indisponível' : ''
                    }`
                  : 'Sem cotação'
              }
            />
          </div>
          {feasibility.groundCost && feasibility.groundCost.estimatedDailyPerPerson > 0 && (
            <p className="mt-2.5 px-1 text-xs leading-relaxed text-muted-foreground">
              <span className="font-medium text-foreground/70">Custo/dia</span> e{' '}
              <span className="font-medium text-foreground/70">câmbio</span> são estimativas
              independentes: o custo/dia compara o nível de preços local com o do Brasil
              (paridade de poder de compra, dados do Banco Mundial)
              {originExchangeToBrl
                ? ', convertido para a moeda de origem pela cotação ao lado'
                : ''}
              , enquanto o câmbio é uma cotação de mercado em tempo real. Um pode estar
              disponível sem o outro.
              {dailyCost?.showFallbackNote && (
                <>
                  {' '}
                  Câmbio da origem indisponível — custo exibido em R$.
                </>
              )}
            </p>
          )}
        </Reveal>
      )}

      <div className="mx-auto max-w-4xl space-y-12 px-4 pt-12 sm:px-6">
        {profile?.extract && (
          <Reveal className="space-y-4">
            <p className="max-w-2xl text-pretty text-lg leading-relaxed text-foreground/85">
              {profile.extract}
            </p>
            {profile.wikipediaUrl && (
              <a
                href={profile.wikipediaUrl}
                target="_blank"
                rel="noreferrer"
                className="inline-flex items-center gap-1.5 text-sm font-medium text-gold transition-opacity hover:opacity-80"
              >
                Ler mais na Wikipédia
                <ExternalLink className="size-3.5" />
              </a>
            )}
          </Reveal>
        )}

        {recommendation && (
          <Reveal className="space-y-5">
            <SectionTitle eyebrow="Análise">Por que esse destino</SectionTitle>

            {(recommendation.highlights.length > 0 || recommendation.tradeoffs.length > 0) && (
              <div className="flex flex-wrap gap-2">
                {recommendation.highlights.map((h) => (
                  <span
                    key={h}
                    className="inline-flex items-center gap-1.5 rounded-full border border-hairline bg-surface-2/60 px-3 py-1 text-xs text-foreground/85"
                  >
                    <span className="size-1 rounded-full bg-gold/80" />
                    {h}
                  </span>
                ))}
                {recommendation.tradeoffs.map((t) => (
                  <span
                    key={t}
                    className="inline-flex items-center gap-1.5 rounded-full border border-chart-3/25 bg-chart-3/10 px-3 py-1 text-xs text-chart-3"
                  >
                    <AlertTriangle className="size-3" />
                    {t}
                  </span>
                ))}
              </div>
            )}

            <CriterionBreakdown breakdown={recommendation.breakdown} />

            <p className="text-xs text-muted-foreground">
              Baseado em {recommendation.dataQuality.availableCriteria} de{' '}
              {recommendation.dataQuality.totalCriteria} critérios com dados.
            </p>
          </Reveal>
        )}

        {recommendation?.climate && (
          <Reveal className="space-y-5">
            <SectionTitle eyebrow="Janela da viagem">Clima esperado</SectionTitle>
            <ClimateChart climate={recommendation.climate} />
          </Reveal>
        )}

        <Reveal className="space-y-5">
          <SectionTitle eyebrow="Perfil">Sobre {name}</SectionTitle>
          <dl className="grid grid-cols-1 gap-px overflow-hidden rounded-2xl border border-hairline bg-hairline sm:grid-cols-2">
            <InfoRow label="Capital" value={country.capitals.join(', ') || '—'} />
            <InfoRow label="Idiomas" value={country.languages.join(', ') || '—'} />
            <InfoRow label="Moeda" value={country.currencies.join(', ') || '—'} />
            {country.timezones.length > 0 && (
              <InfoRow label="Fuso horário" value={country.timezones.slice(0, 3).join(', ')} />
            )}
            {profile?.population != null && (
              <InfoRow
                label="População"
                value={`${profile.population.toLocaleString('pt-BR')}${
                  profile.populationYear ? ` (${profile.populationYear})` : ''
                }`}
              />
            )}
            <InfoRow label="Região" value={country.subregion || regionPt[country.region]} />
            {!feasibility && formatExchange(exchange, originExchangeToBrl, originCountryCode, countryCode) && (
              <InfoRow
                label="Câmbio"
                value={formatExchange(exchange, originExchangeToBrl, originCountryCode, countryCode) as string}
              />
            )}
          </dl>
        </Reveal>

        {holidays && holidays.length > 0 && (
          <Reveal className="space-y-5">
            <SectionTitle eyebrow="Calendário">Próximos feriados</SectionTitle>
            <ol className="relative space-y-1">
              <span aria-hidden className="absolute bottom-3 left-[6px] top-3 w-px bg-hairline" />
              {holidays.slice(0, 6).map((h) => (
                <li key={`${h.date}-${h.name}`} className="relative flex items-baseline gap-4 py-2.5 pl-7">
                  <span
                    aria-hidden
                    className="absolute left-0 top-[1.1rem] size-3.5 rounded-full border-2 border-gold/70 bg-background"
                  />
                  <div className="min-w-0 flex-1">
                    <p className="font-medium">{h.localName}</p>
                    {h.name !== h.localName && (
                      <p className="text-xs text-muted-foreground">{h.name}</p>
                    )}
                  </div>
                  <time className="shrink-0 text-sm tabular-nums text-muted-foreground">
                    {formatDateLong(h.date)}
                  </time>
                </li>
              ))}
            </ol>
          </Reveal>
        )}
      </div>
    </div>
  )
}

function DestinationHero({
  name,
  eyebrow,
  countryCode,
  photoUrl,
  recommendation,
  savedContext,
  backHref,
}: {
  name: string
  eyebrow: string
  countryCode: string
  photoUrl: string | null
  recommendation?: TravelRecommendation
  savedContext?: FavoriteContext
  backHref: string
}) {
  const reduce = useReducedMotion()
  const ref = useRef<HTMLDivElement>(null)
  const { scrollYProgress } = useScroll({ target: ref, offset: ['start start', 'end start'] })
  const y = useTransform(scrollYProgress, [0, 1], [0, 60])
  const scale = useTransform(scrollYProgress, [0, 1], [1.02, 1.1])
  const [loaded, setLoaded] = useState(false)
  const [failed, setFailed] = useState(false)
  const showPhoto = Boolean(photoUrl) && !failed

  return (
    <div ref={ref} className="relative h-[56vh] min-h-[20rem] w-full overflow-hidden sm:min-h-[26rem]">
      <motion.div
        className="absolute -inset-x-0 -top-[8%] h-[116%]"
        style={reduce ? undefined : { y, scale }}
      >
        <div className="absolute inset-0 bg-[linear-gradient(150deg,var(--surface-3),var(--surface-1))]">
          <Flag
            code={countryCode}
            className="absolute inset-0 size-full scale-150 rounded-none object-cover opacity-30 blur-3xl ring-0"
          />
          <div className="absolute inset-0 atlas-grid opacity-60 mask-fade-edges" />
          {!showPhoto && (
            <div className="absolute inset-0 flex items-center justify-center">
              <Flag
                code={countryCode}
                className="h-24 w-36 rounded-2xl object-cover shadow-2xl ring-1 ring-white/15"
              />
            </div>
          )}
        </div>
        {photoUrl && !failed && (
          <img
            src={photoUrl}
            alt={name}
            onLoad={() => setLoaded(true)}
            onError={() => setFailed(true)}
            className={cn(
              'absolute inset-0 size-full object-cover transition-opacity duration-700',
              loaded ? 'opacity-100' : 'opacity-0',
            )}
          />
        )}
      </motion.div>

      {/* scrims for legibility */}
      <div className="absolute inset-0 bg-gradient-to-t from-background via-background/35 to-background/5" />
      <div className="absolute inset-0 bg-gradient-to-br from-background/45 via-transparent to-transparent" />

      <div className="absolute inset-x-0 top-0 z-10 flex items-center justify-between p-4 sm:p-6">
        <Link
          to={backHref}
          className="inline-flex items-center gap-1.5 rounded-full border border-hairline glass px-3.5 py-2 text-sm font-medium text-foreground/90 transition-colors hover:text-foreground"
        >
          <ArrowLeft className="size-4" />
          Voltar
        </Link>
        {recommendation && <FavoriteButton recommendation={recommendation} context={savedContext} />}
      </div>

      <div className="absolute inset-x-0 bottom-0 z-10">
        <div className="mx-auto flex max-w-4xl items-end justify-between gap-4 px-4 pb-16 sm:px-6">
          <motion.div
            variants={staggerContainer(0.08)}
            initial="hidden"
            animate="show"
            className="min-w-0"
          >
            <motion.p
              variants={heroItem}
              className="mb-2 text-[0.7rem] font-semibold uppercase tracking-[0.24em] text-gold/90"
            >
              {eyebrow}
            </motion.p>
            <motion.h1
              variants={heroItem}
              className="flex items-center gap-3 text-balance font-display text-3xl leading-tight tracking-tight sm:text-5xl"
            >
              <Flag
                code={countryCode}
                className="h-7 w-11 shrink-0 rounded-md shadow-lg sm:h-9 sm:w-14"
              />
              {name}
            </motion.h1>
          </motion.div>

          {recommendation && (
            <motion.div
              variants={heroItem}
              initial="hidden"
              animate="show"
              className="shrink-0 rounded-2xl border border-hairline glass p-2 sm:p-2.5"
            >
              <ScoreRing
                score={recommendation.tripScore}
                size={72}
                strokeWidth={6}
                label="viagem"
                animate
              />
            </motion.div>
          )}
        </div>
      </div>
    </div>
  )
}

function SectionTitle({ eyebrow, children }: { eyebrow?: string; children: ReactNode }) {
  return (
    <div>
      {eyebrow && (
        <p className="mb-1 text-[0.7rem] font-semibold uppercase tracking-[0.22em] text-gold/80">
          {eyebrow}
        </p>
      )}
      <h2 className="font-display text-xl tracking-tight sm:text-2xl">{children}</h2>
    </div>
  )
}

function Stat({
  glyph,
  label,
  value,
  sub,
}: {
  glyph: ReactNode
  label: string
  value: ReactNode
  sub?: string | null
}) {
  return (
    <div className="bg-surface-1 p-4 transition-colors hover:bg-surface-2 sm:p-5">
      <div className="flex items-center gap-2 text-[0.7rem] font-medium uppercase tracking-wider text-muted-foreground">
        <span className="text-gold/85">{glyph}</span>
        {label}
      </div>
      <p className="mt-2 font-display text-lg tabular-nums sm:text-xl">{value}</p>
      {sub && <p className="mt-0.5 text-xs text-muted-foreground">{sub}</p>}
    </div>
  )
}

function InfoRow({ label, value }: { label: string; value: string }) {
  return (
    <div className="bg-surface-1 px-4 py-3.5">
      <dt className="text-[0.7rem] font-medium uppercase tracking-wider text-muted-foreground">
        {label}
      </dt>
      <dd className="mt-1 text-pretty text-sm">{value}</dd>
    </div>
  )
}
