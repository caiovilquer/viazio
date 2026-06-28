import { useMemo, useState } from "react";
import { motion } from "framer-motion";
import { Check, Search, X } from "lucide-react";
import { Input } from "@/components/ui/input";
import { Flag } from "@/components/shared/Flag";
import type { CountryOption, Region, RegionOption } from "@/api/types";
import { spring } from "@/lib/motion";
import { cn } from "@/lib/utils";

const modes = [
  { key: "region", label: "Por região" },
  { key: "countries", label: "Países específicos" },
] as const;

export function DestinationPicker({
  regions,
  countries,
  region,
  selectedCountries,
  onRegionChange,
  onCountriesChange,
}: {
  regions: RegionOption[];
  countries: CountryOption[];
  region: Region | null;
  selectedCountries: string[];
  onRegionChange: (region: Region | null) => void;
  onCountriesChange: (countries: string[]) => void;
}) {
  const [mode, setMode] = useState<"region" | "countries">(
    region ? "region" : "countries",
  );
  const [query, setQuery] = useState("");

  const filtered = useMemo(() => {
    if (!query.trim()) return countries.slice(0, 24);
    const q = query.trim().toLowerCase();
    return countries
      .filter((c) => c.name.toLowerCase().includes(q))
      .slice(0, 24);
  }, [countries, query]);

  function switchMode(next: "region" | "countries") {
    setMode(next);
    if (next === "region") onCountriesChange([]);
    else onRegionChange(null);
  }

  function toggleCountry(code: string) {
    if (selectedCountries.includes(code)) {
      onCountriesChange(selectedCountries.filter((c) => c !== code));
    } else if (selectedCountries.length < 50) {
      onCountriesChange([...selectedCountries, code]);
    }
  }

  return (
    <div className="space-y-4">
      {/* controle segmentado */}
      <div className="grid grid-cols-2 gap-1 rounded-xl border border-hairline bg-surface-2/50 p-1">
        {modes.map((m) => {
          const active = mode === m.key;
          return (
            <button
              key={m.key}
              type="button"
              onClick={() => switchMode(m.key)}
              className="relative rounded-lg px-3 py-2 text-sm font-medium"
            >
              {active && (
                <motion.span
                  layoutId="dest-mode"
                  transition={spring.snappy}
                  className="absolute inset-0 rounded-lg bg-surface-3 elevate"
                />
              )}
              <span
                className={cn(
                  "relative z-10",
                  active ? "text-foreground" : "text-muted-foreground",
                )}
              >
                {m.label}
              </span>
            </button>
          );
        })}
      </div>

      {mode === "region" ? (
        <div className="grid grid-cols-2 gap-2.5 sm:grid-cols-3">
          {regions.map((r) => {
            const active = region === r.key;
            return (
              <motion.button
                key={r.key}
                type="button"
                whileTap={{ scale: 0.97 }}
                onClick={() => onRegionChange(active ? null : r.key)}
                className={cn(
                  "flex items-center justify-between rounded-xl border p-4 text-left text-sm font-medium transition-[background-color,border-color]",
                  active
                    ? "border-gold/40 bg-surface-3 text-foreground"
                    : "border-hairline bg-surface/50 text-foreground/85 hover:border-foreground/15 hover:bg-surface-2",
                )}
              >
                {r.label}
                {active && (
                  <Check className="size-4 text-gold" strokeWidth={2.5} />
                )}
              </motion.button>
            );
          })}
        </div>
      ) : (
        <div className="space-y-3">
          <div className="relative">
            <Search className="pointer-events-none absolute left-3.5 top-1/2 size-4 -translate-y-1/2 text-muted-foreground" />
            <Input
              placeholder="Buscar país..."
              value={query}
              onChange={(e) => setQuery(e.target.value)}
              className="pl-10"
            />
          </div>

          {selectedCountries.length > 0 && (
            <div className="flex flex-wrap gap-2">
              {selectedCountries.map((code) => {
                const country = countries.find((c) => c.code === code);
                return (
                  <span
                    key={code}
                    className="inline-flex items-center gap-1.5 rounded-full border border-gold/30 bg-gold/10 py-1 pl-2 pr-1 text-xs font-medium text-foreground"
                  >
                    <Flag code={code} className="h-3 w-4" />
                    {country?.name ?? code}
                    <button
                      type="button"
                      onClick={() => toggleCountry(code)}
                      aria-label={`Remover ${country?.name ?? code}`}
                      className="flex size-4 items-center justify-center rounded-full text-muted-foreground transition-colors hover:bg-foreground/10 hover:text-foreground"
                    >
                      <X className="size-3" />
                    </button>
                  </span>
                );
              })}
            </div>
          )}

          <div className="grid max-h-64 grid-cols-1 gap-1 overflow-y-auto rounded-xl border border-hairline bg-surface/40 p-1.5 sm:grid-cols-2">
            {filtered.map((country) => {
              const isSelected = selectedCountries.includes(country.code);
              return (
                <button
                  key={country.code}
                  type="button"
                  onClick={() => toggleCountry(country.code)}
                  className={cn(
                    "flex items-center justify-between gap-2 rounded-lg px-3 py-2 text-left text-sm transition-colors",
                    isSelected
                      ? "bg-surface-3 text-foreground"
                      : "text-foreground/85 hover:bg-surface-2",
                  )}
                >
                  <span className="flex min-w-0 items-center gap-2">
                    <Flag code={country.code} className="h-3.5 w-5 shrink-0" />
                    <span className="truncate">{country.name}</span>
                  </span>
                  {isSelected && (
                    <Check
                      className="size-4 shrink-0 text-gold"
                      strokeWidth={2.5}
                    />
                  )}
                </button>
              );
            })}
            {filtered.length === 0 && (
              <p className="col-span-2 px-3 py-6 text-center text-sm text-muted-foreground">
                Nenhum país encontrado.
              </p>
            )}
          </div>
        </div>
      )}
    </div>
  );
}
