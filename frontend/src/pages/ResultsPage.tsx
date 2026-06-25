import { useMemo } from 'react'
import { Link, useSearchParams } from 'react-router-dom'
import { motion } from 'framer-motion'
import { CalendarDays, Frown, SlidersHorizontal, Sparkles } from 'lucide-react'
import { useRecommendations } from '@/api/queries'
import { criteriaToRequest, searchParamsToCriteria } from '@/lib/search-params'
import { formatDateRange } from '@/lib/format'
import { RecommendationCard } from '@/components/results/RecommendationCard'
import { Button } from '@/components/ui/button'
import { Skeleton } from '@/components/ui/skeleton'

export function ResultsPage() {
  const [params] = useSearchParams()
  const criteria = useMemo(() => searchParamsToCriteria(params), [params])
  const request = criteria ? criteriaToRequest(criteria) : null
  const { data, isLoading, isError, error } = useRecommendations(request)

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
    <div className="mx-auto max-w-5xl px-4 py-8">
      <div className="mb-6 flex flex-col gap-4 sm:flex-row sm:items-end sm:justify-between">
        <div>
          <h1 className="font-display text-2xl font-semibold sm:text-3xl">Seus melhores destinos</h1>
          <p className="mt-1 flex items-center gap-1.5 text-sm text-muted-foreground">
            <CalendarDays className="size-4" />
            {formatDateRange(criteria.from, criteria.to)}
          </p>
        </div>
        <Button asChild variant="outline" size="sm" className="gap-2 self-start rounded-full">
          <Link to="/buscar">
            <SlidersHorizontal className="size-3.5" />
            Ajustar busca
          </Link>
        </Button>
      </div>

      {data?.window && (
        <motion.div
          initial={{ opacity: 0, y: 10 }}
          animate={{ opacity: 1, y: 0 }}
          className="mb-8 rounded-2xl border border-border bg-card p-4"
        >
          <div className="flex items-center gap-2 text-sm font-medium">
            <Sparkles className="size-4 text-primary" />
            Qualidade da janela: {Math.round(data.window.score)}/100
          </div>
          <p className="mt-1 text-sm text-muted-foreground">{data.window.explanation}</p>
        </motion.div>
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

      {data && data.recommendations.length > 0 && (
        <div className="grid gap-5 sm:grid-cols-2 lg:grid-cols-3">
          {data.recommendations.map((rec, i) => (
            <RecommendationCard
              key={rec.countryCode}
              recommendation={rec}
              rank={i + 1}
              searchQuery={params.toString()}
            />
          ))}
        </div>
      )}

      {data && data.skipped.length > 0 && (
        <p className="mt-8 text-center text-xs text-muted-foreground">
          {data.skipped.length} destino(s) ignorado(s) por falta de dados.
        </p>
      )}
    </div>
  )
}
