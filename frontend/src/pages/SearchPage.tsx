import { useEffect, useMemo, useState } from "react";
import { useNavigate, useSearchParams } from "react-router-dom";
import { ArrowRight } from "lucide-react";
import { useMeta } from "@/api/queries";
import type { CriterionKey, ProfileKey, Region } from "@/api/types";
import {
  criteriaToFormState,
  criteriaToSearchParams,
  DEFAULT_FORM_WEIGHTS,
  searchParamsToCriteria,
  type SearchCriteria,
} from "@/lib/search-params";
import { todayIso } from "@/lib/dates";
import { formatBrl, formatDateRange } from "@/lib/format";
import { SearchSection } from "@/components/search/SearchSection";
import { DestinationPicker } from "@/components/search/DestinationPicker";
import { ProfilePicker } from "@/components/search/ProfilePicker";
import { WeightSliders } from "@/components/search/WeightSliders";
import { TravelersStepper } from "@/components/search/TravelersStepper";
import { PlanSummary } from "@/components/search/PlanSummary";
import { Flag } from "@/components/shared/Flag";
import { Reveal } from "@/components/shared/Reveal";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";
import { Skeleton } from "@/components/ui/skeleton";

function readFormFromParams(params: URLSearchParams) {
  const criteria = searchParamsToCriteria(params);
  return criteria ? criteriaToFormState(criteria) : null;
}

export function SearchPage() {
  const { data: meta, isLoading } = useMeta();
  const navigate = useNavigate();
  const [searchParams] = useSearchParams();

  const [from, setFrom] = useState(
    () => readFormFromParams(searchParams)?.from ?? todayIso(14),
  );
  const [to, setTo] = useState(
    () => readFormFromParams(searchParams)?.to ?? todayIso(18),
  );
  const [originCountry, setOriginCountry] = useState(
    () => readFormFromParams(searchParams)?.originCountry ?? "BR",
  );
  const [originCity, setOriginCity] = useState<string | undefined>(
    () => readFormFromParams(searchParams)?.originCity,
  );
  const [region, setRegion] = useState<Region | null>(
    () => readFormFromParams(searchParams)?.region ?? null,
  );
  const [countries, setCountries] = useState<string[]>(
    () => readFormFromParams(searchParams)?.countries ?? [],
  );
  const [profile, setProfile] = useState<ProfileKey | null>(
    () => readFormFromParams(searchParams)?.profile ?? "equilibrado",
  );
  const [customWeights, setCustomWeights] = useState(
    () => readFormFromParams(searchParams)?.customWeights ?? false,
  );
  const [weights, setWeights] = useState<Record<CriterionKey, number>>(
    () => readFormFromParams(searchParams)?.weights ?? DEFAULT_FORM_WEIGHTS,
  );
  const [travelers, setTravelers] = useState(
    () => readFormFromParams(searchParams)?.travelers ?? 1,
  );
  const [maxBudget, setMaxBudget] = useState(
    () => readFormFromParams(searchParams)?.maxBudget ?? "",
  );

  const paramsKey = searchParams.toString();

  useEffect(() => {
    const form = readFormFromParams(new URLSearchParams(paramsKey));
    if (!form) return;
    setFrom(form.from);
    setTo(form.to);
    setOriginCountry(form.originCountry);
    setOriginCity(form.originCity);
    setRegion(form.region);
    setCountries(form.countries);
    setProfile(form.profile);
    setCustomWeights(form.customWeights);
    setWeights(form.weights);
    setTravelers(form.travelers);
    setMaxBudget(form.maxBudget);
  }, [paramsKey]);

  useEffect(() => {
    if (!meta || customWeights || !profile) return;
    const preset = meta.profiles.find((p) => p.key === profile);
    if (preset) setWeights(preset.weights);
  }, [meta, customWeights, profile]);

  const originCountryOption = meta?.countries.find(
    (c) => c.code === originCountry,
  );

  const canSubmit = Boolean(from && to && (region || countries.length > 0));

  const dayCount = useMemo(() => {
    const start = new Date(`${from}T00:00:00`);
    const end = new Date(`${to}T00:00:00`);
    return Math.max(
      0,
      Math.round((end.getTime() - start.getTime()) / 86_400_000) + 1,
    );
  }, [from, to]);

  function handleProfileSelect(key: ProfileKey) {
    setProfile(key);
    setCustomWeights(false);
    const preset = meta?.profiles.find((p) => p.key === key);
    if (preset) setWeights(preset.weights);
  }

  function handleCustom() {
    setCustomWeights(true);
    setProfile(null);
  }

  function handleSubmit() {
    if (!canSubmit) return;
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
    };
    navigate(`/resultados?${criteriaToSearchParams(criteria).toString()}`);
  }

  // ── Rótulos do resumo ao vivo ──
  const budgetNum = maxBudget ? Number(maxBudget) : NaN;
  const summary = {
    originCode: originCountry,
    originLabel: originCountryOption?.name ?? originCountry,
    destinationLabel: region
      ? (meta?.regions.find((r) => r.key === region)?.label ?? region)
      : countries.length > 0
        ? `${countries.length} ${countries.length === 1 ? "país" : "países"}`
        : null,
    dateRangeLabel: formatDateRange(from, to),
    profileLabel: customWeights
      ? "Personalizado"
      : (meta?.profiles.find((p) => p.key === profile)?.label ?? "—"),
    budgetLabel:
      Number.isFinite(budgetNum) && budgetNum > 0
        ? formatBrl(budgetNum)
        : undefined,
  };

  if (isLoading || !meta) {
    return (
      <div className="mx-auto max-w-6xl px-4 py-10 lg:py-14">
        <Skeleton className="h-9 w-64" />
        <div className="mt-10 grid gap-10 lg:grid-cols-[minmax(0,1fr)_21rem] lg:gap-12">
          <div className="space-y-8">
            {Array.from({ length: 4 }).map((_, i) => (
              <Skeleton key={i} className="h-32 w-full rounded-2xl" />
            ))}
          </div>
          <Skeleton className="hidden h-80 w-full rounded-2xl lg:block" />
        </div>
      </div>
    );
  }

  return (
    <div className="mx-auto max-w-6xl px-4 py-10 lg:py-14">
      <Reveal className="max-w-2xl">
        <p className="mb-3 text-[0.7rem] font-semibold uppercase tracking-[0.22em] text-gold/80">
          Planejar
        </p>
        <h1 className="font-display text-3xl tracking-tight sm:text-4xl">
          Monte seu próximo <span className="text-gold-gradient">feriadão</span>
        </h1>
        <p className="mt-3 text-muted-foreground">
          Cinco passos rápidos — e a Viazio cruza os dados pra explicar cada
          destino.
        </p>
      </Reveal>

      <div className="mt-10 grid gap-10 lg:grid-cols-[minmax(0,1fr)_21rem] lg:gap-12">
        {/* ── Coluna do formulário ── */}
        <div className="space-y-12">
          <SearchSection
            step={1}
            title="De onde você parte?"
            description="Para calcular distância e câmbio."
          >
            <div className="grid grid-cols-2 gap-3">
              <div className="space-y-2">
                <Label>País de origem</Label>
                <Select
                  value={originCountry}
                  onValueChange={(v) => {
                    setOriginCountry(v);
                    setOriginCity(undefined);
                  }}
                >
                  <SelectTrigger className="w-full">
                    <SelectValue placeholder="País" />
                  </SelectTrigger>
                  <SelectContent>
                    {meta.countries.map((c) => (
                      <SelectItem key={c.code} value={c.code}>
                        <Flag code={c.code} className="h-3.5 w-5" /> {c.name}
                      </SelectItem>
                    ))}
                  </SelectContent>
                </Select>
              </div>
              {/* <div className="space-y-2">
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
              </div> */}
            </div>
          </SearchSection>

          <SearchSection
            step={2}
            title="Quando você quer viajar?"
            description={`${dayCount} dia${dayCount === 1 ? "" : "s"} de viagem`}
          >
            <div className="grid grid-cols-2 gap-3">
              <div className="space-y-2">
                <Label>Ida</Label>
                <Input
                  type="date"
                  value={from}
                  min={todayIso()}
                  onChange={(e) => setFrom(e.target.value)}
                />
              </div>
              <div className="space-y-2">
                <Label>Volta</Label>
                <Input
                  type="date"
                  value={to}
                  min={from}
                  onChange={(e) => setTo(e.target.value)}
                />
              </div>
            </div>
          </SearchSection>

          <SearchSection
            step={3}
            title="Para onde?"
            description="Uma região inteira ou países específicos."
          >
            <DestinationPicker
              regions={meta.regions}
              countries={meta.countries}
              region={region}
              selectedCountries={countries}
              onRegionChange={setRegion}
              onCountriesChange={setCountries}
            />
          </SearchSection>

          <SearchSection
            step={4}
            title="O que mais importa pra você?"
            description="Um perfil pronto ou pesos sob medida."
          >
            <div className="space-y-5">
              <ProfilePicker
                profiles={meta.profiles}
                criteria={meta.criteria}
                value={profile}
                custom={customWeights}
                onSelect={handleProfileSelect}
                onCustom={handleCustom}
              />
              {customWeights && (
                <WeightSliders
                  criteria={meta.criteria}
                  weights={weights}
                  onChange={(criterion, value) =>
                    setWeights((w) => ({ ...w, [criterion]: value }))
                  }
                />
              )}
            </div>
          </SearchSection>

          <SearchSection
            step={5}
            title="Detalhes finais"
            description="Opcional, mas ajuda a refinar."
          >
            <div className="space-y-3">
              <TravelersStepper
                value={travelers}
                max={meta.limits.maximumTravelers}
                onChange={setTravelers}
              />
              <div className="relative">
                <span className="pointer-events-none absolute left-3.5 top-1/2 -translate-y-1/2 text-sm text-muted-foreground">
                  R$
                </span>
                <Input
                  type="number"
                  placeholder="Orçamento terrestre máximo (opcional)"
                  value={maxBudget}
                  onChange={(e) => setMaxBudget(e.target.value)}
                  className="pl-10"
                  min={0}
                />
              </div>
            </div>
          </SearchSection>
        </div>

        {/* ── Trilho de resumo fixo (desktop) ── */}
        <aside className="hidden lg:block">
          <div className="sticky top-24">
            <PlanSummary
              {...summary}
              dayCount={dayCount}
              travelers={travelers}
              canSubmit={canSubmit}
              onSubmit={handleSubmit}
            />
          </div>
        </aside>
      </div>

      {/* ── CTA fixo (mobile) ── */}
      <div className="sticky bottom-20 z-30 mt-10 lg:hidden">
        <div className="rounded-2xl border border-hairline glass p-3 elevate-lg">
          <div className="mb-2 flex items-center justify-between px-1 text-xs text-muted-foreground">
            <span className="truncate">
              {summary.destinationLabel
                ? `${summary.destinationLabel} · ${dayCount} dias`
                : "Escolha um destino"}
            </span>
            <span className="shrink-0">{summary.profileLabel}</span>
          </div>
          <Button
            size="lg"
            className="w-full rounded-full glow-coral"
            disabled={!canSubmit}
            onClick={handleSubmit}
          >
            Encontrar meus destinos
            <ArrowRight className="size-4" />
          </Button>
        </div>
      </div>
    </div>
  );
}
