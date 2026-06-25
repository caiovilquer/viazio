import { useMemo, useState } from 'react'
import { motion } from 'framer-motion'
import { Search, X } from 'lucide-react'
import { Tabs, TabsList, TabsTrigger } from '@/components/ui/tabs'
import { Input } from '@/components/ui/input'
import { Badge } from '@/components/ui/badge'
import type { CountryOption, Region, RegionOption } from '@/api/types'
import { cn } from '@/lib/utils'

export function DestinationPicker({
  regions,
  countries,
  region,
  selectedCountries,
  onRegionChange,
  onCountriesChange,
}: {
  regions: RegionOption[]
  countries: CountryOption[]
  region: Region | null
  selectedCountries: string[]
  onRegionChange: (region: Region | null) => void
  onCountriesChange: (countries: string[]) => void
}) {
  const [mode, setMode] = useState<'region' | 'countries'>(region ? 'region' : 'countries')
  const [query, setQuery] = useState('')

  const filtered = useMemo(() => {
    if (!query.trim()) return countries.slice(0, 24)
    const q = query.trim().toLowerCase()
    return countries.filter((c) => c.name.toLowerCase().includes(q)).slice(0, 24)
  }, [countries, query])

  function toggleCountry(code: string) {
    if (selectedCountries.includes(code)) {
      onCountriesChange(selectedCountries.filter((c) => c !== code))
    } else if (selectedCountries.length < 50) {
      onCountriesChange([...selectedCountries, code])
    }
  }

  return (
    <div className="space-y-4">
      <Tabs
        value={mode}
        onValueChange={(v) => {
          const next = v as 'region' | 'countries'
          setMode(next)
          if (next === 'region') onCountriesChange([])
          else onRegionChange(null)
        }}
      >
        <TabsList className="w-full">
          <TabsTrigger value="region" className="flex-1">
            Por região
          </TabsTrigger>
          <TabsTrigger value="countries" className="flex-1">
            Países específicos
          </TabsTrigger>
        </TabsList>
      </Tabs>

      {mode === 'region' ? (
        <div className="grid grid-cols-2 gap-3 sm:grid-cols-3">
          {regions.map((r) => {
            const isActive = region === r.key
            return (
              <motion.button
                key={r.key}
                type="button"
                whileTap={{ scale: 0.96 }}
                onClick={() => onRegionChange(isActive ? null : r.key)}
                className={cn(
                  'rounded-2xl border p-4 text-left text-sm font-medium transition-colors',
                  isActive
                    ? 'border-primary bg-primary/8 ring-2 ring-primary/30'
                    : 'border-border bg-card hover:border-primary/40',
                )}
              >
                {r.label}
              </motion.button>
            )
          })}
        </div>
      ) : (
        <div className="space-y-3">
          <div className="relative">
            <Search className="absolute left-3 top-1/2 size-4 -translate-y-1/2 text-muted-foreground" />
            <Input
              placeholder="Buscar país..."
              value={query}
              onChange={(e) => setQuery(e.target.value)}
              className="pl-9"
            />
          </div>

          {selectedCountries.length > 0 && (
            <div className="flex flex-wrap gap-2">
              {selectedCountries.map((code) => {
                const country = countries.find((c) => c.code === code)
                return (
                  <Badge key={code} variant="secondary" className="gap-1 pr-1">
                    {country ? `${country.flagEmoji} ${country.name}` : code}
                    <button
                      type="button"
                      onClick={() => toggleCountry(code)}
                      className="ml-1 rounded-full p-0.5 hover:bg-foreground/10"
                    >
                      <X className="size-3" />
                    </button>
                  </Badge>
                )
              })}
            </div>
          )}

          <div className="grid max-h-64 grid-cols-1 gap-1 overflow-y-auto rounded-xl border border-border p-1 sm:grid-cols-2">
            {filtered.map((country) => {
              const isSelected = selectedCountries.includes(country.code)
              return (
                <button
                  key={country.code}
                  type="button"
                  onClick={() => toggleCountry(country.code)}
                  className={cn(
                    'flex items-center gap-2 rounded-lg px-3 py-2 text-left text-sm transition-colors',
                    isSelected ? 'bg-primary/10 text-primary' : 'hover:bg-secondary',
                  )}
                >
                  <span>{country.flagEmoji}</span>
                  <span className="truncate">{country.name}</span>
                </button>
              )
            })}
            {filtered.length === 0 && (
              <p className="col-span-2 px-3 py-4 text-center text-sm text-muted-foreground">
                Nenhum país encontrado.
              </p>
            )}
          </div>
        </div>
      )}
    </div>
  )
}
