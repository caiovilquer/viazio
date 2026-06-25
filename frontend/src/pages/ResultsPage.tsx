import { useEffect, useMemo, useState } from 'react'
import { Link, useNavigate, useSearchParams } from 'react-router-dom'
import { motion } from 'framer-motion'
import { CalendarDays, Columns3, Frown, SlidersHorizontal, Sparkles, X } from 'lucide-react'
import { toast } from 'sonner'
import { useMeta, useRecommendations } from '@/api/queries'
import type { CriterionKey, ProfileKey, TravelRecommendation } from '@/api/types'
import { criteriaToRequest, criteriaToSearchParams, searchParamsToCriteria } from '@/lib/search-params'
import { rescoreAll, weightsEqual } from '@/lib/rescoring'
import { formatDateRange } from '@/lib/format'
import { RecommendationCard } from '@/components/results/RecommendationCard'
import { RefineBar } from '@/components/results/RefineBar'
import { CompareBar } from '@/components/results/CompareBar'
import { Button } from '@/components/ui/button'
import { Skeleton } from '@/components/ui/skeleton'

const MAX_COMPARE = 3

export function ResultsPage() {
  const [params] = useSearchParams()
  const navigate = useNavigate()
  const [criteria] = useState(() => searchParamsToCriteria(params))
  const request = criteria ? criteriaToRequest(criteria) : null
  const { data, isLoading, isError, error } = useRecommendations(request)
  const { data: meta } = useMeta()

  const [liveWeights, setLiveWeights] = useState<Record<CriterionKey, number> | null>(null)
  const [activeProfile, setActiveProfile] = useState<ProfileKey | null>(null)
  const [customWeights, setCustomWeights] = useState(false)

  const [compareMode, setCompareMode] = useState(false)
  const [selectedCodes, setSelectedCodes] = useState<string[]>([])

  useEffect(() => {
    if (data && !liveWeights) {
      setLiveWeights(data.weights)
      setActiveProfile(data.profile)
      setCustomWeights(data.profile === null)
    }
  }, [data, liveWeights])

  const displayed = useMemo<TravelRecommendation[]>(() => {
    if (!data) return []
    if (!liveWeights) return data.recommendations
    return rescoreAll(data.recommendations, liveWeights)
  }, [data, liveWeights])

  const dirty = Boolean(data && liveWeights && !weightsEqual(liveWeights, data.weights))

  function handleSelectProfile(key: ProfileKey) {
    setActiveProfile(key)
    setCustomWeights(false)
    const preset = meta?.profiles.find((p) => p.key === key)
    if (preset) setLiveWeights(preset.weights)
  }

  function handleCustomWeight(criterion: CriterionKey, value: number) {
    setCustomWeights(true)
    setActiveProfile(null)
    setLiveWeights((w) => (w ? { ...w, [criterion]: value } : w))
  }

  function handleReset() {
    if (!data) return
    setLiveWeights(data.weights)
    setActiveProfile(data.profile)
    setCustomWeights(data.profile === null)
  }

  function toggleCompareMode() {
    setCompareMode((on) => !on)
    setSelectedCodes([])
  }

  function toggleSelect(code: string) {
    setSelectedCodes((current) => {
      if (current.includes(code)) return current.filter((c) => c !== code)
      if (current.length >= MAX_COMPARE) {
        toast.warning(`Selecione no máximo ${MAX_COMPARE} destinos para comparar.`)
        return current
      }
      return [...current, code]
    })
  }

  function handleCompare() {
    if (!criteria || selectedCodes.length < 2) return
    const selected = selectedCodes
      .map((code) => displayed.find((r) => r.countryCode === code))
      .filter((r): r is TravelRecommendation => Boolean(r))

    const qs = criteriaToSearchParams({
      ...criteria,
      profile: customWeights ? null : activeProfile,
      weights: customWeights && liveWeights ? liveWeights : {},
    })
    qs.set('codes', selectedCodes.join(','))
    navigate(`/comparar?${qs.toString()}`, { state: { recommendations: selected } })
  }

  if (!criteria) {
    return (
      <div className="mx-auto flex max-w-md flex-col items-center gap-4 px-4 py-20 text-center">
        <Frown className="size-10 text-muted-foreground" />
        <p className="text-muted-foreground">Não encontramos critérios de busca. Vamos começar de novo?</p>
        <Button asChild>
          <Link to="/buscar">Voltar para a busca</Link>
        </Button>
      </div>
    )
  }

  return (
    <div className="mx-auto max-w-5xl px-4 py-8 pb-28">
      <div className="mb-4 flex flex-col gap-4 sm:flex-row sm:items-end sm:justify-between">
        <div>
          <h1 className="font-display text-2xl font-semibold sm:text-3xl">Seus melhores destinos</h1>
          <p className="mt-1 flex items-center gap-1.5 text-sm text-muted-foreground">
            <CalendarDays className="size-4" />
            {formatDateRange(criteria.from, criteria.to)}
          </p>
        </div>
        <div className="flex gap-2 self-start">
          <Button
            variant={compareMode ? 'default' : 'outline'}
            size="sm"
            className="gap-2 rounded-full"
            disabled={!data || data.recommendations.length < 2}
            onClick={toggleCompareMode}
          >
            {compareMode ? <X className="size-3.5" /> : <Columns3 className="size-3.5" />}
            {compareMode ? 'Cancelar' : 'Comparar'}
          </Button>
          <Button asChild variant="outline" size="sm" className="gap-2 rounded-full">
            <Link to="/buscar">
              <SlidersHorizontal className="size-3.5" />
              Ajustar busca
            </Link>
          </Button>
        </div>
      </div>

      {data?.window && (
        <motion.div
          initial={{ opacity: 0, y: 10 }}
          animate={{ opacity: 1, y: 0 }}
          className="mb-6 rounded-2xl border border-border bg-card p-4"
        >
          <div className="flex items-center gap-2 text-sm font-medium">
            <Sparkles className="size-4 text-primary" />
            Qualidade da janela: {Math.round(data.window.score)}/100
          </div>
          <p className="mt-1 text-sm text-muted-foreground">{data.window.explanation}</p>
        </motion.div>
      )}

      {meta && liveWeights && data && (
        <RefineBar
          profiles={meta.profiles}
          criteria={meta.criteria}
          weights={liveWeights}
          activeProfile={activeProfile}
          custom={customWeights}
          dirty={dirty}
          onSelectProfile={handleSelectProfile}
          onCustomWeight={handleCustomWeight}
          onReset={handleReset}
        />
      )}

      {isLoading && (
        <div className="grid gap-5 sm:grid-cols-2 lg:grid-cols-3">
          {Array.from({ length: 6 }).map((_, i) => (
            <Skeleton key={i} className="h-80 w-full rounded-3xl" />
          ))}
        </div>
      )}

      {isError && (
        <div className="rounded-2xl border border-destructive/30 bg-destructive/5 p-6 text-center text-sm text-destructive">
          {error instanceof Error ? error.message : 'Não foi possível buscar recomendações.'}
        </div>
      )}

      {data && data.recommendations.length === 0 && (
        <div className="rounded-2xl border border-border bg-card p-10 text-center text-muted-foreground">
          Nenhum destino encontrado para esses critérios. Tente ampliar a região ou o período.
        </div>
      )}

      {data && displayed.length > 0 && (
        <div className="grid gap-5 sm:grid-cols-2 lg:grid-cols-3">
          {displayed.map((rec, i) => (
            <RecommendationCard
              key={rec.countryCode}
              recommendation={rec}
              rank={i + 1}
              searchQuery={params.toString()}
              selectable={compareMode}
              selected={selectedCodes.includes(rec.countryCode)}
              selectDisabled={selectedCodes.length >= MAX_COMPARE && !selectedCodes.includes(rec.countryCode)}
              onToggleSelect={() => toggleSelect(rec.countryCode)}
            />
          ))}
        </div>
      )}

      {data && data.skipped.length > 0 && (
        <p className="mt-8 text-center text-xs text-muted-foreground">
          {data.skipped.length} destino(s) ignorado(s) por falta de dados.
        </p>
      )}

      <CompareBar
        recommendations={selectedCodes.map((code) => displayed.find((r) => r.countryCode === code)!).filter(Boolean)}
        visible={compareMode && selectedCodes.length > 0}
        maxCompare={MAX_COMPARE}
        onRemove={toggleSelect}
        onCompare={handleCompare}
      />
    </div>
  )
}
