import { useState } from 'react'
import { motion } from 'framer-motion'
import { CalendarSearch, Frown } from 'lucide-react'
import { useMeta } from '@/api/queries'
import { useBestWindows } from '@/api/queries'
import type { BestWindowsQuery, CriterionKey, ProfileKey, Region } from '@/api/types'
import { SearchSection } from '@/components/search/SearchSection'
import { DestinationPicker } from '@/components/search/DestinationPicker'
import { ProfilePicker } from '@/components/search/ProfilePicker'
import { WeightSliders } from '@/components/search/WeightSliders'
import { WindowCard } from '@/components/windows/WindowCard'
import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import { Label } from '@/components/ui/label'
import { Skeleton } from '@/components/ui/skeleton'

function todayIso(offsetDays = 0) {
  const d = new Date()
  d.setDate(d.getDate() + offsetDays)
  return d.toISOString().slice(0, 10)
}

export function BestWindowsPage() {
  const { data: meta, isLoading: loadingMeta } = useMeta()

  const [from, setFrom] = useState(todayIso())
  const [to, setTo] = useState(todayIso(330))
  const [region, setRegion] = useState<Region | null>(null)
  const [countries, setCountries] = useState<string[]>([])
  const [profile, setProfile] = useState<ProfileKey | null>('equilibrado')
  const [customWeights, setCustomWeights] = useState(false)
  const [weights, setWeights] = useState<Record<CriterionKey, number>>({
    weather: 0.25,
    cost: 0.25,
    distance: 0.25,
    festivities: 0.25,
  })
  const [minDays, setMinDays] = useState(3)

  const [query, setQuery] = useState<BestWindowsQuery | null>(null)
  const { data, isLoading, isError } = useBestWindows(query)

  function handleProfileSelect(key: ProfileKey) {
    setProfile(key)
    setCustomWeights(false)
    const preset = meta?.profiles.find((p) => p.key === key)
    if (preset) setWeights(preset.weights)
  }

  const canSubmit = Boolean(from && to && (region || countries.length > 0))

  function handleSubmit() {
    if (!canSubmit) return
    setQuery({
      from,
      to,
      minDays,
      topWindows: 8,
      destinationsPerWindow: 3,
      region: region ?? undefined,
      countries: region ? undefined : countries,
      profile: customWeights ? undefined : (profile ?? undefined),
      weights: customWeights ? weights : undefined,
      originCountry: 'BR',
    })
  }

  if (loadingMeta || !meta) {
    return (
      <div className="mx-auto max-w-2xl space-y-6 px-4 py-10">
        <Skeleton className="h-8 w-2/3" />
        <Skeleton className="h-40 w-full rounded-2xl" />
      </div>
    )
  }

  return (
    <div className="mx-auto max-w-3xl px-4 py-8 sm:py-12">
      <motion.h1
        initial={{ opacity: 0, y: 10 }}
        animate={{ opacity: 1, y: 0 }}
        className="mb-2 font-display text-3xl font-semibold tracking-tight sm:text-4xl"
      >
        Melhores <span className="text-primary">janelas</span> do ano
      </motion.h1>
      <p className="mb-8 text-muted-foreground">
        Descubra os feriadões mais aproveitáveis dos próximos meses, combinando feriados e fins de semana.
      </p>

      <div className="space-y-8">
        <SearchSection step={1} title="Período de busca" description="Até 400 dias de horizonte.">
          <div className="grid grid-cols-2 gap-3 sm:grid-cols-3">
            <div className="space-y-1.5">
              <Label>De</Label>
              <Input type="date" value={from} min={todayIso()} onChange={(e) => setFrom(e.target.value)} />
            </div>
            <div className="space-y-1.5">
              <Label>Até</Label>
              <Input type="date" value={to} min={from} onChange={(e) => setTo(e.target.value)} />
            </div>
            <div className="col-span-2 space-y-1.5 sm:col-span-1">
              <Label>Mín. dias</Label>
              <Input
                type="number"
                min={3}
                max={30}
                value={minDays}
                onChange={(e) => setMinDays(Number(e.target.value))}
              />
            </div>
          </div>
        </SearchSection>

        <SearchSection step={2} title="Para onde?">
          <DestinationPicker
            regions={meta.regions}
            countries={meta.countries}
            region={region}
            selectedCountries={countries}
            onRegionChange={setRegion}
            onCountriesChange={setCountries}
          />
        </SearchSection>

        <SearchSection step={3} title="Prioridades">
          <div className="space-y-5">
            <ProfilePicker
              profiles={meta.profiles}
              value={profile}
              custom={customWeights}
              onSelect={handleProfileSelect}
              onCustom={() => {
                setCustomWeights(true)
                setProfile(null)
              }}
            />
            {customWeights && (
              <WeightSliders
                criteria={meta.criteria}
                weights={weights}
                onChange={(criterion, value) => setWeights((w) => ({ ...w, [criterion]: value }))}
              />
            )}
          </div>
        </SearchSection>
      </div>

      <Button
        size="lg"
        className="mt-8 w-full gap-2 rounded-full text-base shadow-lg shadow-primary/20"
        disabled={!canSubmit}
        onClick={handleSubmit}
      >
        <CalendarSearch className="size-4" />
        Buscar melhores janelas
      </Button>

      <div className="mt-10 space-y-4">
        {isLoading && (
          <div className="space-y-4">
            {Array.from({ length: 3 }).map((_, i) => (
              <Skeleton key={i} className="h-40 w-full rounded-3xl" />
            ))}
          </div>
        )}

        {isError && (
          <div className="rounded-2xl border border-destructive/30 bg-destructive/5 p-6 text-center text-sm text-destructive">
            Não foi possível buscar as janelas. Tente ajustar os critérios.
          </div>
        )}

        {data && data.windows.length === 0 && (
          <div className="flex flex-col items-center gap-3 rounded-2xl border border-border bg-card p-10 text-center text-muted-foreground">
            <Frown className="size-8" />
            Nenhuma janela encontrada para esse período.
          </div>
        )}

        {data?.windows.map((window, i) => (
          <WindowCard key={`${window.start}-${window.end}`} window={window} index={i} searchQuery="" />
        ))}
      </div>
    </div>
  )
}
