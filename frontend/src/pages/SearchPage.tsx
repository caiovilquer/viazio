import { useMemo, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { motion } from 'framer-motion'
import { MapPin, Wallet, ArrowRight } from 'lucide-react'
import { useMeta } from '@/api/queries'
import type { CriterionKey, ProfileKey, Region } from '@/api/types'
import { criteriaToSearchParams, type SearchCriteria } from '@/lib/search-params'
import { SearchSection } from '@/components/search/SearchSection'
import { DestinationPicker } from '@/components/search/DestinationPicker'
import { ProfilePicker } from '@/components/search/ProfilePicker'
import { WeightSliders } from '@/components/search/WeightSliders'
import { TravelersStepper } from '@/components/search/TravelersStepper'
import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import { Label } from '@/components/ui/label'
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select'
import { Skeleton } from '@/components/ui/skeleton'

function todayIso(offsetDays = 0) {
  const d = new Date()
  d.setDate(d.getDate() + offsetDays)
  return d.toISOString().slice(0, 10)
}

export function SearchPage() {
  const { data: meta, isLoading } = useMeta()
  const navigate = useNavigate()

  const [from, setFrom] = useState(todayIso(14))
  const [to, setTo] = useState(todayIso(18))
  const [originCountry, setOriginCountry] = useState('BR')
  const [originCity, setOriginCity] = useState<string | undefined>(undefined)
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
  const [travelers, setTravelers] = useState(1)
  const [maxBudget, setMaxBudget] = useState('')

  const originCountryOption = meta?.countries.find((c) => c.code === originCountry)

  const canSubmit = Boolean(from && to && (region || countries.length > 0))

  const dayCount = useMemo(() => {
    const start = new Date(`${from}T00:00:00`)
    const end = new Date(`${to}T00:00:00`)
    return Math.max(0, Math.round((end.getTime() - start.getTime()) / 86_400_000) + 1)
  }, [from, to])

  function handleProfileSelect(key: ProfileKey) {
    setProfile(key)
    setCustomWeights(false)
    const preset = meta?.profiles.find((p) => p.key === key)
    if (preset) setWeights(preset.weights)
  }

  function handleCustom() {
    setCustomWeights(true)
    setProfile(null)
  }

  function handleSubmit() {
    if (!canSubmit) return
    const criteria: SearchCriteria = {
      from,
      to,
      countries: region ? [] : countries,
      region,
      profile: customWeights ? null : profile,
      weights: customWeights ? weights : {},
      travelers,
      origin: { countryCode: originCountry, city: originCity },
      maxGroundBudgetBrl: maxBudget ? Number(maxBudget) : undefined,
    }
    navigate(`/resultados?${criteriaToSearchParams(criteria).toString()}`)
  }

  if (isLoading || !meta) {
    return (
      <div className="mx-auto max-w-2xl space-y-6 px-4 py-10">
        <Skeleton className="h-8 w-2/3" />
        <Skeleton className="h-40 w-full rounded-2xl" />
        <Skeleton className="h-40 w-full rounded-2xl" />
      </div>
    )
  }

  return (
    <div className="mx-auto max-w-2xl px-4 py-8 sm:py-12">
      <motion.h1
        initial={{ opacity: 0, y: 10 }}
        animate={{ opacity: 1, y: 0 }}
        className="mb-8 font-display text-3xl font-semibold tracking-tight sm:text-4xl"
      >
        Monte seu próximo <span className="text-primary">feriadão</span>
      </motion.h1>

      <div className="space-y-10">
        <SearchSection step={1} title="De onde você parte?" description="Usamos isso para calcular distância e câmbio.">
          <div className="grid grid-cols-2 gap-3">
            <div className="space-y-1.5">
              <Label>País de origem</Label>
              <Select
                value={originCountry}
                onValueChange={(v) => {
                  setOriginCountry(v)
                  setOriginCity(undefined)
                }}
              >
                <SelectTrigger className="w-full">
                  <SelectValue placeholder="País" />
                </SelectTrigger>
                <SelectContent>
                  {meta.countries.map((c) => (
                    <SelectItem key={c.code} value={c.code}>
                      {c.flagEmoji} {c.name}
                    </SelectItem>
                  ))}
                </SelectContent>
              </Select>
            </div>
            <div className="space-y-1.5">
              <Label>Cidade</Label>
              <Select value={originCity ?? originCountryOption?.defaultCity} onValueChange={setOriginCity}>
                <SelectTrigger className="w-full">
                  <SelectValue placeholder="Cidade" />
                </SelectTrigger>
                <SelectContent>
                  {originCountryOption?.cities.map((city) => (
                    <SelectItem key={city.name} value={city.name}>
                      {city.name}
                    </SelectItem>
                  ))}
                </SelectContent>
              </Select>
            </div>
          </div>
        </SearchSection>

        <SearchSection step={2} title="Quando você quer viajar?" description={`${dayCount} dia${dayCount === 1 ? '' : 's'} de viagem`}>
          <div className="grid grid-cols-2 gap-3">
            <div className="space-y-1.5">
              <Label>Ida</Label>
              <Input type="date" value={from} min={todayIso()} onChange={(e) => setFrom(e.target.value)} />
            </div>
            <div className="space-y-1.5">
              <Label>Volta</Label>
              <Input type="date" value={to} min={from} onChange={(e) => setTo(e.target.value)} />
            </div>
          </div>
        </SearchSection>

        <SearchSection step={3} title="Para onde?" description="Escolha uma região inteira ou países específicos.">
          <DestinationPicker
            regions={meta.regions}
            countries={meta.countries}
            region={region}
            selectedCountries={countries}
            onRegionChange={setRegion}
            onCountriesChange={setCountries}
          />
        </SearchSection>

        <SearchSection step={4} title="O que mais importa pra você?" description="Escolha um perfil ou personalize os pesos.">
          <div className="space-y-5">
            <ProfilePicker
              profiles={meta.profiles}
              value={profile}
              custom={customWeights}
              onSelect={handleProfileSelect}
              onCustom={handleCustom}
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

        <SearchSection step={5} title="Detalhes finais" description="Opcional, mas ajuda a refinar os resultados.">
          <div className="space-y-3">
            <TravelersStepper value={travelers} max={meta.limits.maximumTravelers} onChange={setTravelers} />
            <div className="flex items-center gap-3 rounded-2xl border border-border bg-card px-4 py-3">
              <Wallet className="size-4 shrink-0 text-muted-foreground" />
              <Input
                type="number"
                placeholder="Orçamento terrestre máximo (BRL, opcional)"
                value={maxBudget}
                onChange={(e) => setMaxBudget(e.target.value)}
                className="border-0 p-0 shadow-none focus-visible:ring-0"
                min={0}
              />
            </div>
          </div>
        </SearchSection>
      </div>

      <motion.div
        initial={{ opacity: 0, y: 20 }}
        animate={{ opacity: 1, y: 0 }}
        transition={{ delay: 0.3 }}
        className="sticky bottom-20 mt-10 md:bottom-6"
      >
        <Button
          size="lg"
          className="w-full gap-2 rounded-full text-base shadow-lg shadow-primary/20"
          disabled={!canSubmit}
          onClick={handleSubmit}
        >
          <MapPin className="size-4" />
          Encontrar meus destinos
          <ArrowRight className="size-4" />
        </Button>
      </motion.div>
    </div>
  )
}
