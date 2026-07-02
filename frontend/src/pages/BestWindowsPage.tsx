import { useCallback, useEffect, useState } from "react";
import { useSearchParams } from "react-router-dom";
import { CalendarSearch, Frown } from "lucide-react";
import { useBestWindows, useMeta } from "@/api/queries";
import type {
  BestWindowsQuery,
  CriterionKey,
  ProfileKey,
  Region,
  WindowSuggestion,
} from "@/api/types";
import { SearchSection } from "@/components/search/SearchSection";
import { DestinationPicker } from "@/components/search/DestinationPicker";
import { ProfilePicker } from "@/components/search/ProfilePicker";
import { WeightSliders } from "@/components/search/WeightSliders";
import { WindowCard } from "@/components/windows/WindowCard";
import { Reveal } from "@/components/shared/Reveal";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Skeleton } from "@/components/ui/skeleton";
import { todayIso } from "@/lib/dates";
import { formatDateRange, pluralize } from "@/lib/format";
import {
  buildJanelasPageHref,
  criteriaToSearchParams,
  DEFAULT_FORM_WEIGHTS,
  janelasCriteriaToFormState,
  janelasCriteriaToQuery,
  janelasCriteriaToSearchParams,
  searchParamsToJanelasCriteria,
  type JanelasCriteria,
} from "@/lib/search-params";

function readJanelasFromParams(params: URLSearchParams) {
  const criteria = searchParamsToJanelasCriteria(params);
  if (!criteria) return null;
  return {
    form: janelasCriteriaToFormState(criteria),
    query: janelasCriteriaToQuery(criteria),
  };
}

export function BestWindowsPage() {
  const { data: meta, isLoading: loadingMeta } = useMeta();
  const [searchParams, setSearchParams] = useSearchParams();
  const initial = readJanelasFromParams(searchParams);

  const [from, setFrom] = useState(() => initial?.form.from ?? todayIso());
  const [to, setTo] = useState(() => initial?.form.to ?? todayIso(330));
  const [region, setRegion] = useState<Region | null>(
    () => initial?.form.region ?? null,
  );
  const [countries, setCountries] = useState<string[]>(
    () => initial?.form.countries ?? [],
  );
  const [profile, setProfile] = useState<ProfileKey | null>(
    () => initial?.form.profile ?? "equilibrado",
  );
  const [customWeights, setCustomWeights] = useState(
    () => initial?.form.customWeights ?? false,
  );
  const [weights, setWeights] = useState<Record<CriterionKey, number>>(
    () => initial?.form.weights ?? DEFAULT_FORM_WEIGHTS,
  );
  const [minDays, setMinDays] = useState(() => initial?.form.minDays ?? 3);

  const [query, setQuery] = useState<BestWindowsQuery | null>(
    () => initial?.query ?? null,
  );
  const { data, isLoading, isError } = useBestWindows(query);

  const paramsKey = searchParams.toString();

  useEffect(() => {
    const parsed = readJanelasFromParams(new URLSearchParams(paramsKey));
    if (!parsed) {
      setQuery(null);
      return;
    }
    const { form, query: nextQuery } = parsed;
    setFrom(form.from);
    setTo(form.to);
    setRegion(form.region);
    setCountries(form.countries);
    setProfile(form.profile);
    setCustomWeights(form.customWeights);
    setWeights(form.weights);
    setMinDays(form.minDays);
    setQuery(nextQuery);
  }, [paramsKey]);

  useEffect(() => {
    if (!meta || customWeights || !profile) return;
    const preset = meta.profiles.find((p) => p.key === profile);
    if (preset) setWeights(preset.weights);
  }, [meta, customWeights, profile]);

  const janelasHref = buildJanelasPageHref(searchParams);

  const buildResultsQuery = useCallback(
    (window: Pick<WindowSuggestion, "start" | "end">) =>
      criteriaToSearchParams({
        from: window.start,
        to: window.end,
        countries: region ? [] : countries,
        region,
        profile: customWeights ? null : profile,
        weights: customWeights ? weights : {},
        travelers: 1,
        origin: { countryCode: "BR" },
      }).toString(),
    [region, countries, profile, customWeights, weights],
  );

  function handleProfileSelect(key: ProfileKey) {
    setProfile(key);
    setCustomWeights(false);
    const preset = meta?.profiles.find((p) => p.key === key);
    if (preset) setWeights(preset.weights);
  }

  const canSubmit = Boolean(from && to && (region || countries.length > 0));

  function handleSubmit() {
    if (!canSubmit) return;
    const criteria: JanelasCriteria = {
      from,
      to,
      minDays,
      countries: region ? [] : countries,
      region,
      profile: customWeights ? null : profile,
      weights: customWeights ? weights : {},
      travelers: 1,
      origin: { countryCode: "BR" },
    };
    setSearchParams(janelasCriteriaToSearchParams(criteria), { replace: true });
    setQuery(janelasCriteriaToQuery(criteria));
  }

  if (loadingMeta || !meta) {
    return (
      <div className="mx-auto max-w-3xl space-y-6 px-4 py-10">
        <Skeleton className="h-9 w-2/3 rounded-full" />
        <Skeleton className="h-40 w-full rounded-2xl" />
      </div>
    );
  }

  return (
    <div className="mx-auto max-w-3xl px-4 py-10 pb-24 lg:py-14">
      <Reveal className="max-w-2xl">
        <p className="mb-3 text-[0.7rem] font-semibold uppercase tracking-[0.22em] text-gold/80">
          Feriadões
        </p>
        <h1 className="font-display text-3xl tracking-tight sm:text-4xl">
          As melhores <span className="text-gold-gradient">janelas</span> do ano
        </h1>
        <p className="mt-3 text-muted-foreground">
          A Viazio cruza feriados, pontes e fins de semana pra achar quando
          poucos dias de férias rendem muitos dias de folga, e os melhores
          destinos pra cada janela.
        </p>
      </Reveal>

      <div className="mt-10 space-y-10">
        <SearchSection
          step={1}
          title="Período de busca"
          description="Até 400 dias de horizonte."
        >
          <div className="grid grid-cols-2 gap-3 sm:grid-cols-3">
            <div className="space-y-2">
              <Label>De</Label>
              <Input
                type="date"
                value={from}
                min={todayIso()}
                onChange={(e) => setFrom(e.target.value)}
              />
            </div>
            <div className="space-y-2">
              <Label>Até</Label>
              <Input
                type="date"
                value={to}
                min={from}
                onChange={(e) => setTo(e.target.value)}
              />
            </div>
            <div className="col-span-2 space-y-2 sm:col-span-1">
              <Label>Mín. de dias</Label>
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

        <SearchSection
          step={2}
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
          step={3}
          title="O que mais importa?"
          description="Um perfil pronto ou pesos sob medida."
        >
          <div className="space-y-5">
            <ProfilePicker
              profiles={meta.profiles}
              criteria={meta.criteria}
              value={profile}
              custom={customWeights}
              onSelect={handleProfileSelect}
              onCustom={() => {
                setCustomWeights(true);
                setProfile(null);
              }}
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
      </div>

      <Button
        size="lg"
        className="mt-9 w-full gap-2 rounded-full glow-coral"
        disabled={!canSubmit}
        onClick={handleSubmit}
      >
        <CalendarSearch className="size-4" />
        Buscar melhores janelas
      </Button>
      {!canSubmit && (
        <p className="mt-2 text-center text-xs text-muted-foreground">
          Escolha uma região ou países para começar.
        </p>
      )}

      <div className="mt-12 space-y-4">
        {isLoading &&
          Array.from({ length: 3 }).map((_, i) => (
            <Skeleton key={i} className="h-52 w-full rounded-2xl" />
          ))}

        {isError && (
          <div className="rounded-2xl border border-destructive/30 bg-destructive/5 p-6 text-center text-sm text-destructive">
            Não foi possível buscar as janelas. Tente ajustar os critérios.
          </div>
        )}

        {data && data.windows.length === 0 && (
          <div className="flex flex-col items-center gap-3 rounded-2xl border border-hairline bg-surface/50 p-12 text-center text-muted-foreground">
            <Frown className="size-9" />
            <p>
              Nenhuma janela encontrada nesse período. Tente ampliar o intervalo
              ou reduzir o mínimo de dias.
            </p>
          </div>
        )}

        {data && data.windows.length > 0 && (
          <>
            <div className="flex items-baseline justify-between gap-3 pb-1">
              <p className="text-[0.7rem] font-semibold uppercase tracking-[0.22em] text-gold/80">
                {pluralize(
                  data.windows.length,
                  "janela encontrada",
                  "janelas encontradas",
                )}
              </p>
              <p className="text-xs text-muted-foreground">
                {formatDateRange(data.from, data.to)}
              </p>
            </div>
            {data.windows.map((window, i) => (
              <WindowCard
                key={`${window.start}-${window.end}`}
                window={window}
                index={i}
                searchQuery={buildResultsQuery(window)}
                janelasHref={janelasHref}
              />
            ))}
          </>
        )}
      </div>
    </div>
  );
}
