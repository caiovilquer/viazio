import { lazy, Suspense, useEffect, useMemo, useState } from "react";
import { Link, useNavigate, useSearchParams } from "react-router-dom";
import {
  CalendarDays,
  ChevronDown,
  Columns3,
  Frown,
  Link2,
  Map,
  SlidersHorizontal,
  X,
} from "lucide-react";
import { toast } from "sonner";
import { useMeta, useRecommendations } from "@/api/queries";
import type {
  CriterionKey,
  ProfileKey,
  ProfileOption,
  TravelRecommendation,
} from "@/api/types";
import {
  criteriaToRequest,
  criteriaToSearchParams,
  searchParamsToCriteria,
} from "@/lib/search-params";
import { rescoreAll, weightsEqual } from "@/lib/rescoring";
import { describeWindow, formatDateRange, pluralize } from "@/lib/format";
import { RecommendationCard } from "@/components/results/RecommendationCard";
import { RefineBar } from "@/components/results/RefineBar";
import { CompareBar } from "@/components/results/CompareBar";
import { ScoreRing } from "@/components/shared/ScoreRing";
import { Button } from "@/components/ui/button";
import { Skeleton } from "@/components/ui/skeleton";
import { cn } from "@/lib/utils";

// d3-geo/topojson só carregam quando o usuário chega ao mapa de resultados.
const CandidatesMap = lazy(() =>
  import("@/components/results/CandidatesMap").then((m) => ({
    default: m.CandidatesMap,
  })),
);

const MAX_COMPARE = 3;

/**
 * `data.profile` é um rótulo livre resolvido ("padrão", "personalizado",
 * "economico (ajustado)"...), não um `ProfileKey` — veja o comentário do tipo. Só
 * uma correspondência exata com uma chave de preset conhecida indica perfil nomeado ativo;
 * qualquer outro valor (incluindo o sufixo "(ajustado)" após mover um slider) significa
 * pesos personalizados em vigor e nenhum chip deve ficar destacado.
 */
function resolveActiveProfile(
  profileLabel: string | null,
  profiles: ProfileOption[] | undefined,
): { activeProfile: ProfileKey | null; customWeights: boolean } {
  const match = profiles?.find((p) => p.key === profileLabel);
  return match
    ? { activeProfile: match.key, customWeights: false }
    : { activeProfile: null, customWeights: true };
}

export function ResultsPage() {
  const [params, setParams] = useSearchParams();
  const navigate = useNavigate();
  const [criteria] = useState(() => searchParamsToCriteria(params));
  const request = criteria ? criteriaToRequest(criteria) : null;
  const { data, isLoading, isError, error } = useRecommendations(request);
  const { data: meta } = useMeta();

  const [liveWeights, setLiveWeights] = useState<Record<
    CriterionKey,
    number
  > | null>(null);
  const [activeProfile, setActiveProfile] = useState<ProfileKey | null>(null);
  const [customWeights, setCustomWeights] = useState(false);

  const [compareMode, setCompareMode] = useState(false);
  const [selectedCodes, setSelectedCodes] = useState<string[]>([]);
  const [mapOpen, setMapOpen] = useState(true);

  useEffect(() => {
    // Aguarda os dois: resolver perfil nomeado vs. pesos customizados exige a lista
    // de chaves de preset em `meta`, e essa query pode concluir depois de `data`.
    if (data && meta && !liveWeights) {
      setLiveWeights(data.weights);
      const resolved = resolveActiveProfile(data.profile, meta.profiles);
      setActiveProfile(resolved.activeProfile);
      setCustomWeights(resolved.customWeights);
    }
  }, [data, liveWeights, meta]);

  // Trocar o perfil pelos chips (ou arrastar um slider de peso) só re-pontua
  // localmente os candidatos já buscados (veja `displayed` abaixo) — sem nova busca.
  // Mas a URL precisa refletir a escolha: senão recarregar, compartilhar ou
  // favoritar a página reverte silenciosamente ao perfil da busca original.
  useEffect(() => {
    if (!criteria || !liveWeights) return;
    const qs = criteriaToSearchParams({
      ...criteria,
      profile: customWeights ? null : activeProfile,
      weights: customWeights ? liveWeights : {},
    });
    setParams(qs, { replace: true });
  }, [criteria, liveWeights, activeProfile, customWeights, setParams]);

  const displayed = useMemo<TravelRecommendation[]>(() => {
    if (!data) return [];
    if (!liveWeights) return data.recommendations;
    return rescoreAll(data.recommendations, liveWeights);
  }, [data, liveWeights]);

  const dirty = Boolean(
    data && liveWeights && !weightsEqual(liveWeights, data.weights),
  );

  function handleSelectProfile(key: ProfileKey) {
    setActiveProfile(key);
    setCustomWeights(false);
    const preset = meta?.profiles.find((p) => p.key === key);
    if (preset) setLiveWeights(preset.weights);
  }

  function handleCustomWeight(criterion: CriterionKey, value: number) {
    setCustomWeights(true);
    setActiveProfile(null);
    setLiveWeights((w) => (w ? { ...w, [criterion]: value } : w));
  }

  function handleReset() {
    if (!data) return;
    setLiveWeights(data.weights);
    const resolved = resolveActiveProfile(data.profile, meta?.profiles);
    setActiveProfile(resolved.activeProfile);
    setCustomWeights(resolved.customWeights);
  }

  function toggleCompareMode() {
    setCompareMode((on) => !on);
    setSelectedCodes([]);
  }

  function toggleSelect(code: string) {
    setSelectedCodes((current) => {
      if (current.includes(code)) return current.filter((c) => c !== code);
      if (current.length >= MAX_COMPARE) {
        toast.warning(
          `Selecione no máximo ${MAX_COMPARE} destinos para comparar.`,
        );
        return current;
      }
      return [...current, code];
    });
  }

  function handleCompare() {
    if (!criteria || selectedCodes.length < 2) return;
    const selected = selectedCodes
      .map((code) => displayed.find((r) => r.countryCode === code))
      .filter((r): r is TravelRecommendation => Boolean(r));

    const qs = criteriaToSearchParams({
      ...criteria,
      profile: customWeights ? null : activeProfile,
      weights: customWeights && liveWeights ? liveWeights : {},
    });
    qs.set("codes", selectedCodes.join(","));
    navigate(`/comparar?${qs.toString()}`, {
      state: {
        recommendations: selected,
        originExchangeToBrl: data?.originExchangeToBrl ?? null,
      },
    });
  }

  function handleMapSelect(recommendation: TravelRecommendation) {
    navigate(`/destino/${recommendation.countryCode}?${params.toString()}`, {
      state: {
        recommendation,
        originExchangeToBrl: data?.originExchangeToBrl ?? null,
      },
    });
  }

  async function handleCopyLink() {
    const url = `${window.location.origin}${window.location.pathname}?${params.toString()}`;
    try {
      await navigator.clipboard.writeText(url);
      toast.success("Link copiado — compartilhe esta busca.");
    } catch {
      toast.error("Não foi possível copiar o link.");
    }
  }

  if (!criteria) {
    return (
      <div className="mx-auto flex max-w-md flex-col items-center gap-4 px-4 py-24 text-center">
        <Frown className="size-10 text-muted-foreground" />
        <p className="text-muted-foreground">
          Não encontramos critérios de busca. Vamos começar de novo?
        </p>
        <Button asChild className="rounded-full">
          <Link to="/buscar">Voltar para a busca</Link>
        </Button>
      </div>
    );
  }

  return (
    <div className="mx-auto max-w-6xl px-4 py-10 pb-28 lg:py-14">
      <div className="mb-6 flex flex-col gap-4 sm:flex-row sm:items-end sm:justify-between">
        <div>
          <p className="mb-2 text-[0.7rem] font-semibold uppercase tracking-[0.22em] text-gold/80">
            Resultados
          </p>
          <h1 className="font-display text-2xl tracking-tight sm:text-3xl">
            Seus melhores destinos
          </h1>
          <p className="mt-2 flex items-center gap-1.5 text-sm text-muted-foreground">
            <CalendarDays className="size-4" />
            {formatDateRange(criteria.from, criteria.to)}
          </p>
        </div>
        <div className="flex flex-wrap gap-2 self-start">
          <Button
            variant="outline"
            size="sm"
            className="gap-2 rounded-full"
            onClick={() => void handleCopyLink()}
          >
            <Link2 className="size-3.5" />
            Copiar link
          </Button>
          <Button
            variant="outline"
            size="sm"
            className={cn(
              "gap-2 rounded-full",
              compareMode && "border-gold/40 text-gold",
            )}
            disabled={!data || data.recommendations.length < 2}
            onClick={toggleCompareMode}
          >
            {compareMode ? (
              <X className="size-3.5" />
            ) : (
              <Columns3 className="size-3.5" />
            )}
            {compareMode ? "Cancelar" : "Comparar"}
          </Button>
          <Button
            asChild
            variant="ghost"
            size="sm"
            className="gap-2 rounded-full"
          >
            <Link to="/buscar">
              <SlidersHorizontal className="size-3.5" />
              Ajustar busca
            </Link>
          </Button>
        </div>
      </div>

      {data?.window && (
        <div className="mb-6 flex items-center gap-4 rounded-2xl border border-hairline bg-surface/50 p-4">
          <ScoreRing
            score={data.window.score}
            size={64}
            strokeWidth={6}
            label="janela"
          />
          <div className="min-w-0">
            <p className="text-sm font-medium">Qualidade da janela</p>
            <p className="mt-0.5 text-sm text-muted-foreground">
              {describeWindow(data.window)}
            </p>
          </div>
        </div>
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

      {data && displayed.length > 0 && (
        <div className="mb-6">
          <section className="overflow-hidden rounded-2xl border border-hairline bg-background">
            <button
              type="button"
              className="flex w-full items-center justify-between gap-3 bg-background px-4 py-3 text-left transition-colors hover:bg-surface-2"
              aria-expanded={mapOpen}
              aria-controls="results-map-panel"
              onClick={() => setMapOpen((open) => !open)}
            >
              <div className="flex min-w-0 items-center gap-3">
                <span className="flex size-9 shrink-0 items-center justify-center rounded-full border border-hairline bg-surface-2 text-gold">
                  <Map className="size-4" />
                </span>
                <div className="min-w-0">
                  <p className="text-sm font-medium">Mapa de destinos</p>
                  <p className="text-xs text-muted-foreground">
                    {pluralize(
                      displayed.length,
                      "candidato no mapa",
                      "candidatos no mapa",
                    )}
                    {mapOpen
                      ? " · clique para recolher"
                      : " · clique para expandir"}
                  </p>
                </div>
              </div>
              <ChevronDown
                className={cn(
                  "size-4 shrink-0 text-muted-foreground transition-transform duration-200",
                  mapOpen && "rotate-180",
                )}
              />
            </button>
            {mapOpen && (
              <div
                id="results-map-panel"
                className="border-t border-hairline bg-background"
              >
                <Suspense
                  fallback={
                    <Skeleton className="h-56 w-full rounded-none sm:h-72 lg:h-80" />
                  }
                >
                  <CandidatesMap
                    className="rounded-none border-0"
                    recommendations={displayed}
                    origin={data.origin}
                    originExchangeToBrl={data.originExchangeToBrl}
                    onSelect={handleMapSelect}
                  />
                </Suspense>
              </div>
            )}
          </section>
        </div>
      )}

      {isLoading && (
        <div className="grid gap-5 sm:grid-cols-2 lg:grid-cols-3">
          {Array.from({ length: 6 }).map((_, i) => (
            <Skeleton key={i} className="h-80 w-full rounded-2xl" />
          ))}
        </div>
      )}

      {isError && (
        <div className="rounded-2xl border border-destructive/30 bg-destructive/5 p-6 text-center text-sm text-destructive">
          {error instanceof Error
            ? error.message
            : "Não foi possível buscar recomendações."}
        </div>
      )}

      {data && data.recommendations.length === 0 && (
        <div className="flex flex-col items-center gap-3 rounded-2xl border border-hairline bg-surface/50 p-12 text-center">
          <Frown className="size-9 text-muted-foreground" />
          <p className="text-muted-foreground">
            Nenhum destino encontrado para esses critérios. Tente ampliar a
            região ou o período.
          </p>
          <Button asChild variant="outline" className="rounded-full">
            <Link to="/buscar">Ajustar busca</Link>
          </Button>
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
              originExchangeToBrl={data.originExchangeToBrl}
              originCountryCode={data.origin.countryCode}
              selectable={compareMode}
              selected={selectedCodes.includes(rec.countryCode)}
              selectDisabled={
                selectedCodes.length >= MAX_COMPARE &&
                !selectedCodes.includes(rec.countryCode)
              }
              onToggleSelect={() => toggleSelect(rec.countryCode)}
            />
          ))}
        </div>
      )}

      {data && data.skipped.length > 0 && (
        <p className="mt-8 text-center text-xs text-muted-foreground">
          {pluralize(
            data.skipped.length,
            "destino ignorado",
            "destinos ignorados",
          )}{" "}
          por falta de dados.
        </p>
      )}

      <CompareBar
        recommendations={selectedCodes
          .map((code) => displayed.find((r) => r.countryCode === code)!)
          .filter(Boolean)}
        visible={compareMode && selectedCodes.length > 0}
        maxCompare={MAX_COMPARE}
        onRemove={toggleSelect}
        onCompare={handleCompare}
      />
    </div>
  );
}
