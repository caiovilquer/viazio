import { motion } from "framer-motion";
import { Crown } from "lucide-react";
import type { TravelRecommendation } from "@/api/types";
import { Flag } from "@/components/shared/Flag";
import { Reveal } from "@/components/shared/Reveal";
import { landingExampleRecommendations } from "@/lib/landing-demo";
import { formatInOriginCurrency } from "@/lib/format";
import { ease } from "@/lib/motion";
import { cn } from "@/lib/utils";

interface Metric {
  label: string;
  icon: string;
  winner: "a" | "b" | "tie";
  displayA: string;
  displayB: string;
  pctA: number;
  pctB: number;
}

function formatHours(hours: number) {
  const whole = Math.floor(hours);
  const minutes = Math.round((hours - whole) * 60);
  return minutes > 0 ? `${whole}h${String(minutes).padStart(2, "0")}` : `${whole}h`;
}

function metricRow(
  label: string,
  icon: string,
  valueA: number | null | undefined,
  valueB: number | null | undefined,
  displayA: string,
  displayB: string,
  direction: "lower" | "higher",
): Metric | null {
  if (valueA == null || valueB == null || Number.isNaN(valueA) || Number.isNaN(valueB)) {
    return null;
  }
  const winner: Metric["winner"] =
    valueA === valueB ? "tie" : direction === "lower"
      ? valueA < valueB ? "a" : "b"
      : valueA > valueB ? "a" : "b";
  const best = direction === "lower" ? Math.min(valueA, valueB) : Math.max(valueA, valueB);
  const pctOf = (v: number) =>
    direction === "lower" ? (best / v) * 100 : (v / best) * 100;
  return {
    label,
    icon,
    winner,
    displayA,
    displayB,
    pctA: pctOf(valueA),
    pctB: pctOf(valueB),
  };
}

function buildMetrics(a: TravelRecommendation, b: TravelRecommendation): Metric[] {
  const metrics: Array<Metric | null> = [];

  metrics.push(
    metricRow(
      "Distância",
      "✈️",
      a.feasibility?.travelEffort.distanceKm,
      b.feasibility?.travelEffort.distanceKm,
      a.feasibility ? `${Math.round(a.feasibility.travelEffort.distanceKm).toLocaleString("pt-BR")} km` : "—",
      b.feasibility ? `${Math.round(b.feasibility.travelEffort.distanceKm).toLocaleString("pt-BR")} km` : "—",
      "lower",
    ),
  );

  const flightA = a.feasibility?.travelEffort;
  const flightB = b.feasibility?.travelEffort;
  metrics.push(
    metricRow(
      "Tempo de voo",
      "🕒",
      flightA ? (flightA.estimatedTravelHoursMin + flightA.estimatedTravelHoursMax) / 2 : null,
      flightB ? (flightB.estimatedTravelHoursMin + flightB.estimatedTravelHoursMax) / 2 : null,
      flightA
        ? `${formatHours(flightA.estimatedTravelHoursMin)}–${formatHours(flightA.estimatedTravelHoursMax)}`
        : "—",
      flightB
        ? `${formatHours(flightB.estimatedTravelHoursMin)}–${formatHours(flightB.estimatedTravelHoursMax)}`
        : "—",
      "lower",
    ),
  );

  const costA = a.feasibility?.groundCost?.estimatedDailyPerPerson;
  const costB = b.feasibility?.groundCost?.estimatedDailyPerPerson;
  metrics.push(
    metricRow(
      "Custo terrestre / dia",
      "💰",
      costA && costA > 0 ? costA : null,
      costB && costB > 0 ? costB : null,
      costA ? formatInOriginCurrency(costA, null, "BR").formatted : "—",
      costB ? formatInOriginCurrency(costB, null, "BR").formatted : "—",
      "lower",
    ),
  );

  const weatherA = a.breakdown.find((c) => c.criterion === "weather");
  const weatherB = b.breakdown.find((c) => c.criterion === "weather");
  metrics.push(
    metricRow(
      "Clima no período",
      "☀️",
      weatherA?.available ? weatherA.score : null,
      weatherB?.available ? weatherB.score : null,
      weatherA?.available ? `${Math.round(weatherA.score)} pts` : "—",
      weatherB?.available ? `${Math.round(weatherB.score)} pts` : "—",
      "higher",
    ),
  );

  return metrics.filter((m): m is Metric => m !== null);
}

function buildVerdict(
  a: TravelRecommendation,
  b: TravelRecommendation,
  metrics: Metric[],
): string {
  const winnerIsA = a.tripScore >= b.tripScore;
  const winner = winnerIsA ? a : b;
  const loser = winnerIsA ? b : a;
  const winnerSide = winnerIsA ? "a" : "b";
  const diff = Math.round(Math.abs(a.tripScore - b.tripScore));
  const reasons = metrics
    .filter((m) => m.winner === winnerSide)
    .map((m) => m.label.toLowerCase());

  if (reasons.length === 0) {
    return `${winner.countryName} tem nota geral ${diff} ${diff === 1 ? "ponto" : "pontos"} acima de ${loser.countryName} nesse período — a diferença vem de festividades e da cobertura de dados, não dos números acima.`;
  }
  const reasonsText =
    reasons.length === 1
      ? reasons[0]
      : `${reasons.slice(0, -1).join(", ")} e ${reasons[reasons.length - 1]}`;
  return `${winner.countryName} venceu ${loser.countryName} por ${diff} ${diff === 1 ? "ponto" : "pontos"}: ${reasonsText} pesaram a favor nesse período.`;
}

export function ComparisonShowdown() {
  const [a, b] = landingExampleRecommendations;
  const metrics = buildMetrics(a, b);
  const verdict = buildVerdict(a, b, metrics);

  return (
    <div className="mx-auto max-w-2xl">
      <Reveal>
        <div className="rounded-3xl border border-hairline bg-surface/50 p-5 elevate sm:p-7">
          <div className="flex items-center justify-between gap-3 pb-5">
            <div className="flex items-center gap-2">
              <Flag code={a.countryCode} className="h-5 w-7" />
              <span className="font-display text-base tracking-tight sm:text-lg">
                {a.countryName}
              </span>
            </div>
            <span className="text-xs font-medium uppercase tracking-[0.2em] text-muted-foreground">
              vs
            </span>
            <div className="flex items-center gap-2">
              <span className="font-display text-base tracking-tight sm:text-lg">
                {b.countryName}
              </span>
              <Flag code={b.countryCode} className="h-5 w-7" />
            </div>
          </div>

          <div className="space-y-4 border-t border-hairline pt-5">
            {metrics.map((m, i) => (
              <div key={m.label}>
                <div className="mb-1.5 flex items-center justify-between gap-2 text-sm">
                  <span className="flex items-center gap-2 font-medium">
                    <span aria-hidden>{m.icon}</span>
                    {m.label}
                  </span>
                </div>
                <div className="grid grid-cols-2 gap-3">
                  <BarCell
                    display={m.displayA}
                    pct={m.pctA}
                    winner={m.winner === "a"}
                    align="left"
                    delay={i * 0.06}
                  />
                  <BarCell
                    display={m.displayB}
                    pct={m.pctB}
                    winner={m.winner === "b"}
                    align="right"
                    delay={i * 0.06}
                  />
                </div>
              </div>
            ))}
          </div>

          <p className="mt-6 border-t border-hairline pt-5 text-sm leading-relaxed text-muted-foreground">
            {verdict}
          </p>
        </div>
      </Reveal>
    </div>
  );
}

function BarCell({
  display,
  pct,
  winner,
  align,
  delay,
}: {
  display: string;
  pct: number;
  winner: boolean;
  align: "left" | "right";
  delay: number;
}) {
  return (
    <div>
      <div
        className={cn(
          "mb-1 flex items-center gap-1 text-sm tabular-nums",
          align === "right" && "justify-end",
          winner ? "font-semibold text-gold" : "text-muted-foreground",
        )}
      >
        {align === "left" && winner && <Crown className="size-3" />}
        {display}
        {align === "right" && winner && <Crown className="size-3" />}
      </div>
      <div
        className={cn(
          "h-1.5 overflow-hidden rounded-full bg-surface-3/60",
          align === "right" && "flex justify-end",
        )}
      >
        <motion.div
          className={cn("h-full rounded-full", winner ? "bg-gold" : "bg-foreground/25")}
          initial={{ width: 0 }}
          whileInView={{ width: `${pct}%` }}
          viewport={{ once: true, margin: "-30px" }}
          transition={{ duration: 0.7, delay, ease: ease.out }}
        />
      </div>
    </div>
  );
}
