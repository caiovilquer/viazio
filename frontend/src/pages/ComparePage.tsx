import { useMemo, useState, type ReactNode } from "react";
import { Link, useLocation, useSearchParams } from "react-router-dom";
import { AnimatePresence, motion, useReducedMotion } from "framer-motion";
import {
  ArrowLeft,
  Clock,
  Coins,
  Crown,
  ExternalLink,
  Frown,
  Info,
  RefreshCw,
  ShieldCheck,
  Wallet,
  X,
} from "lucide-react";
import { useMeta, useRecommendations } from "@/api/queries";
import type {
  CriterionOption,
  Exchange,
  ProfileKey,
  RecommendationSearchRequest,
  TravelRecommendation,
} from "@/api/types";
import { criteriaToRequest, searchParamsToCriteria } from "@/lib/search-params";
import { winnerIndices, type WinnerDirection } from "@/lib/compare";
import { type FavoriteEntry } from "@/lib/favorites";
import { todayIso } from "@/lib/dates";
import { destinationPhotoUrl } from "@/lib/destination-image";
import {
  formatDateRange,
  formatExchange,
  formatInOriginCurrency,
} from "@/lib/format";
import { ScoreRing } from "@/components/shared/ScoreRing";
import { ScoreComposition } from "@/components/shared/ScoreComposition";
import { Flag } from "@/components/shared/Flag";
import { RouteGlyph } from "@/components/shared/Glyphs";
import { Reveal } from "@/components/shared/Reveal";
import { useDestinationImage } from "@/api/images";
import { Skeleton } from "@/components/ui/skeleton";
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
import { ease, spring } from "@/lib/motion";
import { cn } from "@/lib/utils";

const clamp = (n: number) => Math.max(0, Math.min(100, n));

interface MetricCell {
  code: string;
  name: string;
  available: boolean;
  display: string;
  widthPct: number;
  isWinner: boolean;
  /** Por que o destino pontuou assim — expõe o raciocínio do próprio motor
   *  em vez de deixar um número solto sem explicação. */
  caption?: string;
}

interface Metric {
  key: string;
  label: string;
  hint: string;
  emoji?: string;
  icon?: ReactNode;
  /** Peso (0–1) aplicado a este critério no perfil ativo, se for um critério pontuado. */
  weight?: number;
  cells: MetricCell[];
}

/** Monta uma linha de métrica comparável: vencedor via motor compartilhado, larguras das barras
 *  absolutas (notas 0–100) ou relativas ao melhor (magnitudes brutas). */
function buildMetric(
  recs: TravelRecommendation[],
  opts: {
    key: string;
    label: string;
    hint: string;
    emoji?: string;
    icon?: ReactNode;
    direction: WinnerDirection;
    scale: "absolute" | "relative";
    weight?: number;
    valueOf: (rec: TravelRecommendation) => number | null;
    displayOf: (rec: TravelRecommendation) => string;
    captionOf?: (rec: TravelRecommendation) => string | undefined;
  },
): Metric {
  const values = recs.map(opts.valueOf);
  const winners = winnerIndices(values, opts.direction);
  const valid = values.filter(
    (v): v is number => v != null && !Number.isNaN(v),
  );
  const best =
    opts.direction === "lower" ? Math.min(...valid) : Math.max(...valid);

  const cells = recs.map((rec, i) => {
    const v = values[i];
    const available = v != null && !Number.isNaN(v);
    let widthPct = 0;
    if (available) {
      if (opts.scale === "absolute") widthPct = clamp(v);
      else if (valid.length)
        widthPct =
          opts.direction === "lower"
            ? clamp((best / v) * 100)
            : clamp((v / best) * 100);
    }
    return {
      code: rec.countryCode,
      name: rec.countryName,
      available,
      display: available ? opts.displayOf(rec) : "—",
      widthPct,
      isWinner: winners.has(i),
      caption: available ? opts.captionOf?.(rec) : undefined,
    };
  });

  return {
    key: opts.key,
    label: opts.label,
    hint: opts.hint,
    emoji: opts.emoji,
    icon: opts.icon,
    weight: opts.weight,
    cells,
  };
}

function uniqueWinner(
  recs: TravelRecommendation[],
  values: Array<number | null>,
  dir: WinnerDirection,
) {
  const w = winnerIndices(values, dir);
  if (w.size !== 1) return null;
  return recs[[...w][0]];
}

export function ComparePage() {
  const [params] = useSearchParams();
  const location = useLocation();
  const { data: meta } = useMeta();
  const reduce = useReducedMotion();

  const navState = location.state as {
    recommendations?: TravelRecommendation[];
    saved?: FavoriteEntry[];
    originExchangeToBrl?: Exchange | null;
  } | null;
  const stateRecommendations = navState?.recommendations;
  const saved = navState?.saved;
  const originExchangeFromState = navState?.originExchangeToBrl;
  const fromSaved = Boolean(saved && saved.length > 0);

  const codes = useMemo(
    () => params.get("codes")?.split(",").filter(Boolean) ?? [],
    [params],
  );
  const criteria = useMemo(() => searchParamsToCriteria(params), [params]);

  // Destinos salvos podem ter sido guardados em janelas diferentes — detectar isso para
  // avisar e oferecer re-pontuar todos numa janela comum.
  const savedPeriods = new Set(
    (saved ?? []).map((s) =>
      s.context?.from && s.context?.to
        ? `${s.context.from}|${s.context.to}`
        : "unknown",
    ),
  );
  const periodsDiffer =
    fromSaved &&
    saved!.length >= 2 &&
    (savedPeriods.size > 1 || savedPeriods.has("unknown"));
  const firstCtx = saved?.find(
    (s) => s.context?.from && s.context?.to,
  )?.context;

  const [recompute, setRecompute] = useState<{
    from: string;
    to: string;
    profile: ProfileKey | null;
  } | null>(null);
  const [panelOpen, setPanelOpen] = useState(false);
  const [panelFrom, setPanelFrom] = useState(firstCtx?.from ?? todayIso());
  const [panelTo, setPanelTo] = useState(firstCtx?.to ?? todayIso(7));
  const [panelProfile, setPanelProfile] = useState<ProfileKey | null>(
    firstCtx?.profile ?? "equilibrado",
  );

  const commonRequest: RecommendationSearchRequest | null = recompute
    ? {
        from: recompute.from,
        to: recompute.to,
        countries: codes,
        profile: recompute.profile ?? undefined,
        travelers: firstCtx?.travelers ?? 1,
        origin: {
          countryCode: firstCtx?.originCountry ?? "BR",
          city: firstCtx?.originCity,
        },
      }
    : null;

  const request =
    commonRequest ??
    (!stateRecommendations && criteria ? criteriaToRequest(criteria) : null);
  const { data, isLoading } = useRecommendations(request);

  const recomputed =
    commonRequest && data
      ? data.recommendations.filter((r) => codes.includes(r.countryCode))
      : null;
  const recomputeApplied = Boolean(recomputed && recomputed.length >= 2);
  const recomputeLoading = Boolean(commonRequest && isLoading && !data);
  const recomputeFailed = Boolean(
    commonRequest && data && (recomputed?.length ?? 0) < 2,
  );

  const baseRecommendations: TravelRecommendation[] = recomputeApplied
    ? recomputed!
    : stateRecommendations
      ? stateRecommendations
      : data && !commonRequest
        ? data.recommendations.filter((r) => codes.includes(r.countryCode))
        : [];

  const originExchangeToBrl =
    data?.originExchangeToBrl ?? originExchangeFromState ?? null;
  const originCountryCode = data?.origin.countryCode ?? firstCtx?.originCountry;
  const showCostFallbackNote =
    originCountryCode != null &&
    originCountryCode !== "BR" &&
    !(originExchangeToBrl && originExchangeToBrl.valueInReais > 0);

  const [visibleCodes, setVisibleCodes] = useState(codes);
  const [focusedCode, setFocusedCode] = useState<string | null>(null);

  const recommendations = baseRecommendations
    .filter((r) => visibleCodes.includes(r.countryCode))
    .sort((a, b) => b.tripScore - a.tripScore);

  const backParams = new URLSearchParams(params);
  backParams.delete("codes");
  const backHref = fromSaved
    ? "/salvos"
    : `/resultados?${backParams.toString()}`;
  const backLabel = fromSaved ? "Voltar aos salvos" : "Voltar aos resultados";

  function removeDestination(code: string) {
    setVisibleCodes((current) => current.filter((c) => c !== code));
    setFocusedCode((f) => (f === code ? null : f));
  }
  function toggleFocus(code: string) {
    setFocusedCode((f) => (f === code ? null : code));
  }

  const loading = !stateRecommendations && isLoading;

  if (loading) {
    return (
      <div className="mx-auto max-w-5xl px-4 py-10">
        <Skeleton className="h-8 w-48 rounded-full" />
        <div className="mt-6 grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
          {Array.from({ length: 3 }).map((_, i) => (
            <Skeleton key={i} className="h-56 w-full rounded-2xl" />
          ))}
        </div>
        <Skeleton className="mt-6 h-80 w-full rounded-2xl" />
      </div>
    );
  }

  if (recommendations.length < 2) {
    return (
      <div className="mx-auto flex max-w-md flex-col items-center gap-4 px-4 py-24 text-center">
        <Frown className="size-10 text-muted-foreground" />
        <p className="text-muted-foreground">
          Selecione ao menos 2 destinos nos resultados para compará-los.
        </p>
        <Button asChild className="rounded-full">
          <Link to={backHref}>Voltar para os resultados</Link>
        </Button>
      </div>
    );
  }

  const criteriaList: CriterionOption[] = meta?.criteria ?? [];

  // Prêmios (síntese em linguagem simples): vencedor geral + vencedor claro em cada dimensão de pontuação.
  const overallWinner = uniqueWinner(
    recommendations,
    recommendations.map((r) => r.tripScore),
    "higher",
  );
  const criterionAwards = criteriaList
    .map((c) => ({
      title: `Melhor em ${c.label}`,
      emoji: c.icon,
      rec: uniqueWinner(
        recommendations,
        recommendations.map((r) => {
          const e = r.breakdown.find((b) => b.criterion === c.key);
          return e?.available ? e.score : null;
        }),
        "higher",
      ),
    }))
    .filter(
      (a): a is { title: string; emoji: string; rec: TravelRecommendation } =>
        a.rec != null,
    );

  // Linhas de "corrida" de métricas, em dois grupos: dimensões pontuadas com peso (0–100)
  // e os números brutos da viagem por trás delas.
  const overallMetric = buildMetric(recommendations, {
    key: "overall",
    label: "Nota geral da viagem",
    hint: "maior é melhor",
    icon: <Crown className="size-4" />,
    direction: "higher",
    scale: "absolute",
    valueOf: (r) => r.tripScore,
    displayOf: (r) => String(Math.round(r.tripScore)),
  });

  const criterionMetrics: Metric[] = criteriaList.map((c) =>
    buildMetric(recommendations, {
      key: c.key,
      label: c.label,
      hint: "0–100 · maior é melhor",
      emoji: c.icon,
      direction: "higher",
      scale: "absolute",
      weight: recommendations
        .find((r) => r.breakdown.find((b) => b.criterion === c.key)?.available)
        ?.breakdown.find((b) => b.criterion === c.key)?.weight,
      valueOf: (r) => {
        const e = r.breakdown.find((b) => b.criterion === c.key);
        return e?.available ? e.score : null;
      },
      displayOf: (r) =>
        String(
          Math.round(
            r.breakdown.find((b) => b.criterion === c.key)?.score ?? 0,
          ),
        ),
      captionOf: (r) =>
        r.breakdown.find((b) => b.criterion === c.key)?.justification,
    }),
  );

  const tripMetrics: Metric[] = [
    buildMetric(recommendations, {
      key: "distance",
      label: "Distância",
      hint: "menor é melhor",
      icon: <RouteGlyph className="size-4" />,
      direction: "lower",
      scale: "relative",
      valueOf: (r) => r.feasibility?.travelEffort.distanceKm ?? null,
      displayOf: (r) =>
        `${Math.round(r.feasibility!.travelEffort.distanceKm).toLocaleString("pt-BR")} km`,
    }),
    buildMetric(recommendations, {
      key: "flight",
      label: "Tempo de voo",
      hint: "menor é melhor",
      icon: <Clock className="size-4" />,
      direction: "lower",
      scale: "relative",
      valueOf: (r) =>
        r.feasibility?.travelEffort.estimatedTravelHoursMin ?? null,
      displayOf: (r) =>
        `${Math.round(r.feasibility!.travelEffort.estimatedTravelHoursMin)}–${Math.round(
          r.feasibility!.travelEffort.estimatedTravelHoursMax,
        )}h`,
    }),
    buildMetric(recommendations, {
      key: "cost",
      label: "Custo terrestre / dia",
      hint: "menor é melhor",
      icon: <Wallet className="size-4" />,
      direction: "lower",
      scale: "relative",
      // Estimativa 0/BAIXA (ex.: Venezuela) é dado ausente, não "de graça" — não deixar vencer.
      valueOf: (r) => {
        const daily = r.feasibility?.groundCost?.estimatedDailyPerPerson;
        return daily && daily > 0 ? daily : null;
      },
      displayOf: (r) =>
        formatInOriginCurrency(
          r.feasibility!.groundCost!.estimatedDailyPerPerson,
          originExchangeToBrl,
          originCountryCode,
        ).formatted,
    }),
    buildMetric(recommendations, {
      key: "confidence",
      label: "Confiança dos dados",
      hint: "maior é melhor",
      icon: <ShieldCheck className="size-4" />,
      direction: "higher",
      scale: "absolute",
      valueOf: (r) => r.dataQuality.confidenceScore,
      displayOf: (r) => `${Math.round(r.dataQuality.confidenceScore)}%`,
    }),
  ];

  return (
    <div className="mx-auto max-w-5xl px-4 py-10 pb-20 sm:px-6">
      <div className="mb-6">
        <Link
          to={backHref}
          className="mb-2 inline-flex items-center gap-1.5 text-sm text-muted-foreground transition-colors hover:text-foreground"
        >
          <ArrowLeft className="size-3.5" />
          {backLabel}
        </Link>
        <h1 className="font-display text-2xl tracking-tight sm:text-3xl">
          Comparando {recommendations.length} destinos
        </h1>
        <p className="mt-1 text-sm text-muted-foreground">
          {focusedCode
            ? "Destacando um destino. Toque novamente para limpar."
            : "Toque num destino para destacá-lo em cada métrica."}
        </p>
      </div>

      {fromSaved && (periodsDiffer || recompute) && (
        <Reveal className="mb-6">
          <div className="rounded-2xl border border-gold/25 bg-gold/[0.06] p-4 sm:p-5">
            <div className="flex flex-wrap items-start justify-between gap-3">
              <div className="flex gap-2.5">
                <span className="mt-0.5 shrink-0 text-gold">
                  <Info className="size-4" />
                </span>
                <div className="text-sm">
                  {recomputeApplied ? (
                    <p>
                      Comparando todos na janela{" "}
                      <span className="font-medium text-foreground">
                        {formatDateRange(recompute!.from, recompute!.to)}
                      </span>
                      , agora numa base comum.
                    </p>
                  ) : recomputeLoading ? (
                    <p className="text-muted-foreground">
                      Recompondo os destinos na mesma janela…
                    </p>
                  ) : recomputeFailed ? (
                    <p className="text-muted-foreground">
                      Não foi possível recompor todos nessa janela. Mostrando os
                      dados salvos.
                    </p>
                  ) : (
                    <p className="text-muted-foreground">
                      Estes destinos foram salvos para{" "}
                      <span className="font-medium text-foreground">
                        datas diferentes
                      </span>
                      . Distância, voo e custo/dia são comparáveis, mas{" "}
                      <span className="text-foreground">
                        nota geral, clima e festividades
                      </span>{" "}
                      dependem do período de cada um.
                    </p>
                  )}
                </div>
              </div>
              <div className="flex shrink-0 gap-2">
                {recomputeApplied ? (
                  <Button
                    variant="ghost"
                    size="sm"
                    className="rounded-full"
                    onClick={() => {
                      setRecompute(null);
                      setPanelOpen(false);
                    }}
                  >
                    Usar dados salvos
                  </Button>
                ) : (
                  <Button
                    variant="outline"
                    size="sm"
                    className="gap-1.5 rounded-full"
                    disabled={recomputeLoading}
                    onClick={() => setPanelOpen((o) => !o)}
                  >
                    <RefreshCw
                      className={cn(
                        "size-3.5",
                        recomputeLoading && "animate-spin",
                      )}
                    />
                    Recomparar numa janela comum
                  </Button>
                )}
              </div>
            </div>

            {panelOpen && !recomputeApplied && (
              <div className="mt-4 grid gap-3 border-t border-gold/20 pt-4 sm:grid-cols-[1fr_1fr_1fr_auto] sm:items-end">
                <div className="space-y-1.5">
                  <Label>De</Label>
                  <Input
                    type="date"
                    value={panelFrom}
                    onChange={(e) => setPanelFrom(e.target.value)}
                  />
                </div>
                <div className="space-y-1.5">
                  <Label>Até</Label>
                  <Input
                    type="date"
                    value={panelTo}
                    min={panelFrom}
                    onChange={(e) => setPanelTo(e.target.value)}
                  />
                </div>
                <div className="space-y-1.5">
                  <Label>Perfil</Label>
                  <Select
                    value={panelProfile ?? undefined}
                    onValueChange={(v) => setPanelProfile(v as ProfileKey)}
                  >
                    <SelectTrigger className="w-full">
                      <SelectValue placeholder="Perfil" />
                    </SelectTrigger>
                    <SelectContent>
                      {meta?.profiles.map((p) => (
                        <SelectItem key={p.key} value={p.key}>
                          {p.label}
                        </SelectItem>
                      ))}
                    </SelectContent>
                  </Select>
                </div>
                <Button
                  className="rounded-full glow-coral"
                  disabled={!panelFrom || !panelTo}
                  onClick={() => {
                    setRecompute({
                      from: panelFrom,
                      to: panelTo,
                      profile: panelProfile,
                    });
                    setPanelOpen(false);
                  }}
                >
                  Aplicar
                </Button>
              </div>
            )}
          </div>
        </Reveal>
      )}

      {/* Síntese — quem vence o quê, em linguagem simples */}
      <Reveal className="mb-7">
        <div className="rounded-2xl border border-hairline bg-surface/40 p-4 sm:p-5">
          {overallWinner && (
            <div className="mb-3 flex flex-wrap items-center gap-2.5 border-b border-hairline pb-3">
              <span className="inline-flex items-center gap-1.5 rounded-full bg-gold/15 px-2.5 py-1 text-[0.7rem] font-semibold uppercase tracking-wide text-gold">
                <Crown className="size-3.5" />
                Melhor no geral
              </span>
              <span className="flex items-center gap-2 font-display text-lg">
                <Flag code={overallWinner.countryCode} className="h-4 w-6" />
                {overallWinner.countryName}
              </span>
            </div>
          )}
          <div className="flex flex-wrap gap-2">
            {criterionAwards.map((a) => (
              <span
                key={a.title}
                className="inline-flex items-center gap-1.5 rounded-full border border-hairline bg-surface-2/50 px-3 py-1 text-xs"
              >
                <span aria-hidden>{a.emoji}</span>
                <span className="text-muted-foreground">{a.title}:</span>
                <Flag code={a.rec.countryCode} className="h-3 w-4" />
                <span className="font-medium">{a.rec.countryName}</span>
              </span>
            ))}
          </div>
        </div>
      </Reveal>

      {/* Cards de destino */}
      <div className="mb-8 grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
        <AnimatePresence mode="popLayout">
          {recommendations.map((rec) => (
            <CompareCard
              key={rec.countryCode}
              rec={rec}
              originExchangeToBrl={originExchangeToBrl}
              isOverallWinner={overallWinner?.countryCode === rec.countryCode}
              focused={focusedCode === rec.countryCode}
              dimmed={Boolean(focusedCode) && focusedCode !== rec.countryCode}
              onFocus={() => toggleFocus(rec.countryCode)}
              onRemove={() => removeDestination(rec.countryCode)}
            />
          ))}
        </AnimatePresence>
      </div>

      {/* Corridas de métricas */}
      <div className="space-y-3">
        <MetricRow
          metric={overallMetric}
          focusedCode={focusedCode}
          onFocus={toggleFocus}
          reduce={Boolean(reduce)}
        />

        <GroupLabel
          title="Critérios de pontuação"
          caption="Quanto cada destino pontua (0–100) em cada critério ponderado da busca."
        />
        {criterionMetrics.map((m) => (
          <MetricRow
            key={m.key}
            metric={m}
            focusedCode={focusedCode}
            onFocus={toggleFocus}
            reduce={Boolean(reduce)}
          />
        ))}

        <GroupLabel
          title="Números da viagem"
          caption="Os valores brutos por trás da pontuação."
        />
        {tripMetrics.map((m) => (
          <MetricRow
            key={m.key}
            metric={m}
            focusedCode={focusedCode}
            onFocus={toggleFocus}
            reduce={Boolean(reduce)}
          />
        ))}

        {/* Câmbio — apenas informativo, sem vencedor */}
        <Reveal>
          <div className="rounded-2xl border border-hairline bg-surface/40 p-4 sm:p-5">
            <div className="mb-3.5 flex items-center justify-between gap-2">
              <span className="flex items-center gap-2 text-sm font-medium sm:text-base">
                <span className="text-gold/85">
                  <Coins className="size-4" />
                </span>
                Câmbio
              </span>
              <span className="text-[0.7rem] uppercase tracking-wider text-muted-foreground">
                informativo
              </span>
            </div>
            <div className="grid gap-2 sm:grid-cols-2 lg:grid-cols-3">
              {recommendations.map((rec) => (
                <div
                  key={rec.countryCode}
                  className={cn(
                    "rounded-xl border border-hairline bg-surface-2/40 px-3 py-2.5 transition-opacity",
                    focusedCode &&
                      focusedCode !== rec.countryCode &&
                      "opacity-40",
                  )}
                >
                  <span className="flex min-w-0 items-center gap-1.5 text-sm">
                    <Flag code={rec.countryCode} className="h-3 w-4 shrink-0" />
                    <span className="truncate">{rec.countryName}</span>
                  </span>
                  <span className="mt-0.5 block tabular-nums text-xs text-muted-foreground">
                    {formatExchange(
                      rec.exchangeToBrl,
                      originExchangeToBrl,
                      originCountryCode,
                      rec.countryCode,
                    ) ?? "Sem cotação"}
                  </span>
                </div>
              ))}
            </div>
          </div>
        </Reveal>
      </div>

      <p className="mt-5 text-center text-xs text-muted-foreground">
        Câmbio é informativo e não entra na nota: o poder de compra já está
        refletido no critério de custo.
        <br />
        Custo terrestre/dia é uma estimativa por PPP (paridade de poder de
        compra, Banco Mundial), independente da cotação de câmbio: um pode estar
        disponível sem o outro.
        {showCostFallbackNote && (
          <>
            <br />
            Câmbio da origem indisponível. Custo exibido em R$.
          </>
        )}
      </p>
    </div>
  );
}

function GroupLabel({ title, caption }: { title: string; caption: string }) {
  return (
    <div className="px-1 pb-1 pt-4">
      <p className="text-[0.7rem] font-semibold uppercase tracking-[0.2em] text-gold/80">
        {title}
      </p>
      <p className="mt-0.5 text-xs text-muted-foreground">{caption}</p>
    </div>
  );
}

function MetricRow({
  metric,
  focusedCode,
  onFocus,
  reduce,
}: {
  metric: Metric;
  focusedCode: string | null;
  onFocus: (code: string) => void;
  reduce: boolean;
}) {
  return (
    <Reveal>
      <div className="rounded-2xl border border-hairline bg-surface/40 p-4 sm:p-5">
        <div className="mb-3.5 flex items-center justify-between gap-2">
          <span className="flex items-center gap-2 text-sm font-medium sm:text-base">
            {metric.emoji ? (
              <span aria-hidden className="text-base leading-none">
                {metric.emoji}
              </span>
            ) : (
              <span className="text-gold/85">{metric.icon}</span>
            )}
            {metric.label}
            {metric.weight != null && (
              <span className="rounded-full border border-hairline px-1.5 py-px text-[0.65rem] font-normal tracking-wide text-muted-foreground">
                peso {Math.round(metric.weight * 100)}%
              </span>
            )}
          </span>
          <span className="text-[0.7rem] uppercase tracking-wider text-muted-foreground">
            {metric.hint}
          </span>
        </div>
        <div className="space-y-3">
          {metric.cells.map((cell) => (
            <BarLine
              key={cell.code}
              cell={cell}
              dimmed={Boolean(focusedCode) && focusedCode !== cell.code}
              onFocus={() => onFocus(cell.code)}
              reduce={reduce}
            />
          ))}
        </div>
      </div>
    </Reveal>
  );
}

function BarLine({
  cell,
  dimmed,
  onFocus,
  reduce,
}: {
  cell: MetricCell;
  dimmed: boolean;
  onFocus: () => void;
  reduce: boolean;
}) {
  const fill = cn(
    "h-full rounded-full",
    cell.isWinner ? "bg-gold" : "bg-foreground/25",
  );
  return (
    <button
      type="button"
      onClick={onFocus}
      className={cn(
        "block w-full text-left transition-opacity focus-visible:outline-none",
        dimmed && "opacity-40",
      )}
    >
      <div className="mb-1 flex items-center justify-between gap-2 text-sm">
        <span className="flex min-w-0 items-center gap-1.5">
          <Flag code={cell.code} className="h-3 w-4 shrink-0" />
          <span className="truncate">{cell.name}</span>
        </span>
        <span
          className={cn(
            "flex shrink-0 items-center gap-1 tabular-nums",
            cell.isWinner ? "font-semibold text-gold" : "text-muted-foreground",
          )}
        >
          {cell.display}
          {cell.isWinner && <Crown className="size-3" />}
        </span>
      </div>
      <div className="h-2 overflow-hidden rounded-full bg-surface-3/50">
        {reduce ? (
          <div className={fill} style={{ width: `${cell.widthPct}%` }} />
        ) : (
          <motion.div
            className={fill}
            initial={{ width: 0 }}
            whileInView={{ width: `${cell.widthPct}%` }}
            viewport={{ once: true, margin: "-30px" }}
            transition={{ duration: 0.7, ease: ease.out }}
          />
        )}
      </div>
      {cell.caption && (
        <p className="mt-1 text-xs leading-relaxed text-muted-foreground">
          {cell.caption}
        </p>
      )}
    </button>
  );
}

function CompareCard({
  rec,
  originExchangeToBrl,
  isOverallWinner,
  focused,
  dimmed,
  onFocus,
  onRemove,
}: {
  rec: TravelRecommendation;
  originExchangeToBrl: Exchange | null;
  isOverallWinner: boolean;
  focused: boolean;
  dimmed: boolean;
  onFocus: () => void;
  onRemove: () => void;
}) {
  const photoCity = rec.feasibility?.destination.name ?? rec.countryName;
  const { data: cityPhoto } = useDestinationImage(photoCity, 1280);
  const backendPhoto = destinationPhotoUrl(rec.profile?.imageUrl);
  const photoUrl = cityPhoto ?? backendPhoto ?? null;
  const [loaded, setLoaded] = useState(false);
  const [failed, setFailed] = useState(false);
  const showPhoto = Boolean(photoUrl) && !failed;

  return (
    <motion.div
      layout
      initial={{ opacity: 0, y: 16 }}
      animate={{ opacity: dimmed ? 0.55 : 1, y: 0 }}
      exit={{ opacity: 0, scale: 0.95 }}
      transition={spring.soft}
      className={cn(
        "group relative overflow-hidden rounded-2xl border bg-surface/70",
        isOverallWinner
          ? "border-gold/45 elevate-lg"
          : "border-hairline elevate",
        focused && "ring-2 ring-gold/50",
      )}
    >
      {isOverallWinner && (
        <span className="absolute left-3 top-3 z-10 inline-flex items-center gap-1 rounded-full border border-gold/40 bg-background/70 px-2.5 py-1 text-[0.62rem] font-semibold uppercase tracking-wide text-gold backdrop-blur">
          <Crown className="size-3" />
          Melhor no geral
        </span>
      )}
      <button
        type="button"
        onClick={onRemove}
        aria-label={`Remover ${rec.countryName} da comparação`}
        className="absolute right-3 top-3 z-10 flex size-7 items-center justify-center rounded-full border border-hairline bg-background/60 text-foreground/80 backdrop-blur transition-colors hover:text-foreground"
      >
        <X className="size-3.5" />
      </button>

      <button
        type="button"
        onClick={onFocus}
        aria-pressed={focused}
        aria-label={
          focused
            ? `Parar de destacar ${rec.countryName}`
            : `Destacar ${rec.countryName}`
        }
        className="block w-full text-left"
      >
        <div className="relative h-32 w-full overflow-hidden">
          <div className="absolute inset-0 bg-[linear-gradient(150deg,var(--surface-3),var(--surface-1))]">
            <Flag
              code={rec.countryCode}
              className="absolute inset-0 size-full scale-150 rounded-none object-cover opacity-30 blur-2xl ring-0"
            />
            <div className="absolute inset-0 atlas-grid opacity-50 mask-fade-edges" />
            {!showPhoto && (
              <div className="absolute inset-0 flex items-center justify-center">
                <Flag
                  code={rec.countryCode}
                  className="h-12 w-20 rounded-lg object-cover shadow-xl ring-1 ring-white/15"
                />
              </div>
            )}
          </div>
          {photoUrl && !failed && (
            <img
              src={photoUrl}
              alt={rec.countryName}
              loading="lazy"
              onLoad={() => setLoaded(true)}
              onError={() => setFailed(true)}
              className={cn(
                "absolute inset-0 size-full object-cover transition-[opacity,transform] duration-700 group-hover:scale-[1.04]",
                loaded ? "opacity-100" : "opacity-0",
              )}
            />
          )}
          <div className="absolute inset-0 bg-gradient-to-t from-background/90 via-background/20 to-transparent" />
          <div className="absolute inset-x-3 bottom-2.5 flex items-center gap-2">
            <Flag code={rec.countryCode} className="h-4 w-6 shrink-0 shadow" />
            <p className="truncate font-display text-base font-semibold tracking-tight drop-shadow-sm">
              {rec.countryName}
            </p>
          </div>
        </div>
      </button>

      <div className="space-y-3 p-4">
        <div className="flex items-center justify-between gap-3">
          <div className="flex items-center gap-3">
            <ScoreRing
              score={rec.tripScore}
              size={48}
              strokeWidth={5}
              animate
            />
            <div className="leading-tight">
              <p className="text-sm font-medium">Nota geral</p>
              <p className="text-xs text-muted-foreground">da viagem</p>
            </div>
          </div>
          <Link
            to={`/destino/${rec.countryCode}`}
            state={{ recommendation: rec, originExchangeToBrl }}
            className="inline-flex shrink-0 items-center gap-1 text-xs font-medium text-gold transition-opacity hover:opacity-80"
          >
            Ver destino
            <ExternalLink className="size-3" />
          </Link>
        </div>

        <div className="border-t border-hairline pt-3">
          <p className="mb-1.5 text-[0.65rem] uppercase tracking-wider text-muted-foreground/70">
            Como a nota foi composta
          </p>
          <ScoreComposition breakdown={rec.breakdown} size="md" showLabels />
        </div>
      </div>
    </motion.div>
  );
}
