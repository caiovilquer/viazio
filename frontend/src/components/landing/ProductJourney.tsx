import { useRef, useState, type ReactNode } from "react";
import {
  AnimatePresence,
  motion,
  useMotionValueEvent,
  useReducedMotion,
  useScroll,
  useTransform,
  type MotionValue,
} from "framer-motion";
import { Check } from "lucide-react";
import { Flag } from "@/components/shared/Flag";
import { ScoreRing } from "@/components/shared/ScoreRing";
import {
  demoFilterChips,
  demoWindow,
  landingExampleRecommendations,
} from "@/lib/landing-demo";
import { formatExchange } from "@/lib/format";
import { ease } from "@/lib/motion";
import { cn } from "@/lib/utils";

/**
 * A demonstração central da landing: um mock fixo do produto que muda de
 * estado conforme o usuário desce a página, com o roteiro do lado esquerdo
 * amarrado ao mesmo scroll. Não é um carrossel automático nem um vídeo — é o
 * scroll do próprio visitante controlando o "replay" de uma busca real
 * (mesmos números congelados de `lib/landing-demo.ts`).
 *
 * Desktop: coluna esquerda em fluxo normal + coluna direita `sticky`, presa
 * enquanto a coluna esquerda (4× a altura da viewport) passa por baixo — só
 * CSS sticky, sem cálculo manual de posição. `useScroll` no container só
 * decide qual dos 4 estados o mock mostra.
 * Mobile: cada etapa vira um cartão normal, sem sticky nem scroll-jacking.
 */

const winner = landingExampleRecommendations[0];
const runnerUp = landingExampleRecommendations[1];
const third = landingExampleRecommendations[2];

const weather = winner.breakdown.find((b) => b.criterion === "weather");
const cost = winner.breakdown.find((b) => b.criterion === "cost");
const distance = winner.breakdown.find((b) => b.criterion === "distance");
const festivities = winner.breakdown.find((b) => b.criterion === "festivities");
const flight = winner.feasibility?.travelEffort;
const exchangeQuote = formatExchange(
  winner.exchangeToBrl,
  null,
  "BR",
  winner.countryCode,
);
const rainPct = winner.climate
  ? Math.round(winner.climate.rainyDayProbability * 100)
  : null;

/**
 * Pontos de opacidade (entrada 0–1 do scroll → saída 0.32–1) para o bloco de
 * texto `index` de `total`. O primeiro passo não tem "antes" (v não passa de
 * 0) e o último não tem "depois" (v não passa de 1) — por isso eles não
 * desvanecem nessa ponta, só na que existe. Os pontos ficam sempre dentro de
 * [0, 1] e estritamente crescentes por construção (sem precisar de clamp),
 * exigência da Web Animations API que o framer-motion usa para acelerar
 * transforms ligados ao scroll.
 */
function stepOpacityStops(
  index: number,
  total: number,
): { input: number[]; output: number[] } {
  if (total <= 1) return { input: [0, 1], output: [1, 1] };

  const center = index / (total - 1);
  const gap = 1 / (total - 1);
  const fadeHalf = gap * 0.42;
  const plateauHalf = fadeHalf * 0.35;

  if (index === 0) {
    return { input: [0, plateauHalf, fadeHalf], output: [1, 1, 0.32] };
  }
  if (index === total - 1) {
    return { input: [1 - fadeHalf, 1 - plateauHalf, 1], output: [0.32, 1, 1] };
  }
  return {
    input: [
      center - fadeHalf,
      center - plateauHalf,
      center + plateauHalf,
      center + fadeHalf,
    ],
    output: [0.32, 1, 1, 0.32],
  };
}

/** Interpolação linear "manual" — usada com a forma de função do `useTransform`
 * (em vez da forma de listas de offsets) para os valores de opacidade ligados
 * ao scroll. Isso evita o caminho de aceleração via Web Animations API do
 * framer-motion, que nos testes ficou fora de sincronia com o MotionValue
 * real bem no primeiro trecho da curva (o pedaço achatado em 1 → 1). */
function interpolateStops(v: number, input: number[], output: number[]): number {
  const clamped = Math.min(input[input.length - 1], Math.max(input[0], v));
  for (let i = 0; i < input.length - 1; i++) {
    if (clamped <= input[i + 1]) {
      const span = input[i + 1] - input[i];
      const t = span === 0 ? 0 : (clamped - input[i]) / span;
      return output[i] + (output[i + 1] - output[i]) * t;
    }
  }
  return output[output.length - 1];
}

function formatHours(hours: number) {
  const whole = Math.floor(hours);
  const minutes = Math.round((hours - whole) * 60);
  return minutes > 0 ? `${whole}h${String(minutes).padStart(2, "0")}` : `${whole}h`;
}

interface Step {
  id: string;
  kicker: string;
  title: string;
  description: string;
}

const steps: Step[] = [
  {
    id: "select",
    kicker: "1 · Você decide o essencial",
    title: "Feriado, origem, duração e orçamento",
    description:
      "Escolha o feriado, de onde você sai, quantos dias tem livres e até quanto quer gastar. O resto é o motor que calcula.",
  },
  {
    id: "analyze",
    kicker: "2 · O motor cruza os dados",
    title: "Clima, custo, câmbio, voo, eventos e chance de chuva",
    description:
      "Cada destino é avaliado nos mesmos seis sinais, na mesma janela de datas exata da sua viagem, não uma impressão geral.",
  },
  {
    id: "rank",
    kicker: "3 · O ranking aparece",
    title: "Cada destino, com nota final explicada",
    description:
      "Os destinos saem ordenados pela nota de viagem: soma ponderada dos critérios acima, sempre visível, nunca uma caixa preta.",
  },
  {
    id: "explain",
    kicker: "4 · Por que esse venceu",
    title: `Por que ${winner.countryName} ficou em 1º lugar`,
    description:
      "Cada nota vem com a justificativa por trás: o que pesou a favor, o que pesou contra, e de onde vieram os pontos.",
  },
];

/* ─────────────────────── Mock do produto ─────────────────────── */

function MockShell({ activeStep, children }: { activeStep: number; children: ReactNode }) {
  return (
    <div className="overflow-hidden rounded-[1.75rem] border border-hairline bg-surface/80 elevate-lg backdrop-blur-sm">
      <div className="flex items-center justify-between gap-3 border-b border-hairline bg-surface-2/40 px-5 py-3.5 sm:px-6">
        <p className="text-[0.65rem] font-semibold uppercase tracking-[0.2em] text-gold/80">
          Simulação ao vivo
        </p>
        <div className="flex items-center gap-1.5">
          {steps.map((s, i) => (
            <span
              key={s.id}
              className={cn(
                "h-1.5 w-5 rounded-full transition-colors duration-500",
                i <= activeStep ? "bg-gold/70" : "bg-foreground/12",
              )}
            />
          ))}
        </div>
      </div>

      <div className="relative h-[27rem] px-5 py-5 sm:h-[26rem] sm:px-6 sm:py-6">
        <AnimatePresence mode="wait">
          <motion.div
            key={steps[activeStep].id}
            initial={{ opacity: 0, y: 16 }}
            animate={{ opacity: 1, y: 0 }}
            exit={{ opacity: 0, y: -12 }}
            transition={{ duration: 0.38, ease: ease.out }}
            className="absolute inset-0 px-5 py-5 sm:px-6 sm:py-6"
          >
            {children}
          </motion.div>
        </AnimatePresence>
      </div>
    </div>
  );
}

function FieldRow({ label, value, delay }: { label: string; value: string; delay: number }) {
  return (
    <motion.div
      initial={{ opacity: 0, x: -8 }}
      animate={{ opacity: 1, x: 0 }}
      transition={{ duration: 0.32, delay, ease: ease.out }}
      className="flex items-center justify-between gap-3 border-b border-hairline/70 py-3 first:pt-0 last:border-b-0"
    >
      <span className="text-xs uppercase tracking-[0.14em] text-muted-foreground">
        {label}
      </span>
      <span className="flex items-center gap-1.5 text-sm font-medium tracking-tight">
        {value}
        <span className="flex size-4 items-center justify-center rounded-full bg-gold/15 text-gold">
          <Check className="size-2.5" strokeWidth={3} />
        </span>
      </span>
    </motion.div>
  );
}

function SelectPanel() {
  return (
    <div className="flex h-full flex-col">
      <div>
        <FieldRow label="Feriado" value={demoWindow.holidayName} delay={0.02} />
        <FieldRow label="Saindo de" value={demoWindow.originLabel} delay={0.1} />
        <FieldRow label="Duração" value={`${demoWindow.totalDays} dias`} delay={0.18} />
        <FieldRow label="Orçamento" value="Até R$ 3.500" delay={0.26} />
      </div>
      <motion.div
        initial={{ opacity: 0 }}
        animate={{ opacity: 1 }}
        transition={{ delay: 0.4, duration: 0.3 }}
        className="mt-auto flex flex-wrap gap-1.5 pt-4"
      >
        {demoFilterChips.map((chip) => (
          <span
            key={chip}
            className="rounded-full border border-hairline bg-surface-2/50 px-2.5 py-1 text-[0.7rem] font-medium text-foreground/80"
          >
            {chip}
          </span>
        ))}
      </motion.div>
    </div>
  );
}

function AnalysisBar({
  icon,
  label,
  score,
  caption,
  delay,
}: {
  icon: string;
  label: string;
  score: number | null;
  caption: string;
  delay: number;
}) {
  return (
    <motion.div
      initial={{ opacity: 0, y: 6 }}
      animate={{ opacity: 1, y: 0 }}
      transition={{ duration: 0.3, delay, ease: ease.out }}
      className="py-2 first:pt-0"
    >
      <div className="mb-1 flex items-center justify-between gap-2 text-xs">
        <span className="flex items-center gap-1.5 font-medium text-foreground/85">
          <span aria-hidden>{icon}</span>
          {label}
        </span>
        <span className="text-muted-foreground">{caption}</span>
      </div>
      <div className="h-1.5 overflow-hidden rounded-full bg-surface-3/60">
        {score != null ? (
          <motion.div
            className="h-full rounded-full bg-gold/70"
            initial={{ width: 0 }}
            animate={{ width: `${score}%` }}
            transition={{ duration: 0.6, delay: delay + 0.1, ease: ease.out }}
          />
        ) : (
          <div className="h-full w-full rounded-full bg-foreground/[0.06]" />
        )}
      </div>
    </motion.div>
  );
}

function AnalysisPanel() {
  return (
    <div className="space-y-0.5">
      <AnalysisBar
        icon="☀️"
        label="Clima"
        score={weather?.score ?? null}
        caption="~16°C, seco"
        delay={0}
      />
      <AnalysisBar
        icon="💰"
        label="Custo de vida"
        score={cost?.available ? cost.score : null}
        caption={cost?.available ? `${Math.round(cost.score)} pts` : "sem dado"}
        delay={0.07}
      />
      <AnalysisBar
        icon="💱"
        label="Câmbio"
        score={null}
        caption={exchangeQuote ?? "—"}
        delay={0.14}
      />
      <AnalysisBar
        icon="✈️"
        label="Voo"
        score={distance?.score ?? null}
        caption={
          flight
            ? `${formatHours(flight.estimatedTravelHoursMin)}–${formatHours(flight.estimatedTravelHoursMax)}`
            : "—"
        }
        delay={0.21}
      />
      <AnalysisBar
        icon="🎊"
        label="Eventos"
        score={festivities?.score ?? null}
        caption="Columbus Day"
        delay={0.28}
      />
      <AnalysisBar
        icon="🌧️"
        label="Chance de chuva"
        score={null}
        caption={rainPct != null ? `${rainPct}%` : "—"}
        delay={0.35}
      />
    </div>
  );
}

function RankRow({
  rank,
  dest,
  delay,
}: {
  rank: number;
  dest: (typeof landingExampleRecommendations)[number];
  delay: number;
}) {
  return (
    <motion.div
      initial={{ opacity: 0, y: 10 }}
      animate={{ opacity: 1, y: 0 }}
      transition={{ duration: 0.32, delay, ease: ease.out }}
      className={cn(
        "flex items-center gap-3 rounded-2xl px-2.5 py-2.5",
        rank === 1 && "bg-gold/[0.07]",
      )}
    >
      <span className="w-4 shrink-0 text-center font-display text-sm text-muted-foreground">
        {rank}
      </span>
      <Flag code={dest.countryCode} className="h-5 w-7 shrink-0 rounded-[4px]" />
      <span className="min-w-0 flex-1 truncate text-sm font-medium tracking-tight">
        {dest.countryName}
      </span>
      <ScoreRing score={dest.tripScore} size={38} strokeWidth={4} />
    </motion.div>
  );
}

function RankPanel() {
  return (
    <div className="flex h-full flex-col justify-center gap-1.5">
      <RankRow rank={1} dest={winner} delay={0} />
      <RankRow rank={2} dest={runnerUp} delay={0.08} />
      <RankRow rank={3} dest={third} delay={0.16} />
    </div>
  );
}

function ExplainPanel() {
  const topReasons = winner.breakdown.filter((b) => b.available).slice(0, 2);
  const gap = Math.round(winner.tripScore - runnerUp.tripScore);
  return (
    <div className="flex h-full flex-col">
      <div className="flex items-center gap-3.5">
        <ScoreRing score={winner.tripScore} size={52} strokeWidth={5} animate />
        <div className="min-w-0">
          <div className="flex items-center gap-2">
            <Flag code={winner.countryCode} className="h-5 w-7 shrink-0 rounded-[4px]" />
            <p className="truncate font-display text-lg tracking-tight">
              {winner.countryName}
            </p>
          </div>
          <p className="mt-0.5 text-xs text-muted-foreground">
            {gap > 0
              ? `venceu ${runnerUp.countryName} por ${gap} ${gap === 1 ? "ponto" : "pontos"}`
              : `venceu ${runnerUp.countryName} por uma margem mínima`}
          </p>
        </div>
      </div>

      <motion.div
        initial={{ opacity: 0 }}
        animate={{ opacity: 1 }}
        transition={{ delay: 0.15, duration: 0.3 }}
        className="mt-3.5 flex flex-wrap gap-1.5"
      >
        {winner.highlights.slice(0, 3).map((h) => (
          <span
            key={h}
            className="inline-flex items-center gap-1.5 rounded-full border border-hairline bg-surface-2/60 px-2.5 py-0.5 text-xs text-foreground/85"
          >
            <span className="size-1 rounded-full bg-gold/80" />
            {h}
          </span>
        ))}
      </motion.div>

      <div className="mt-4 space-y-2.5 border-t border-hairline pt-3.5">
        {topReasons.map((b, i) => (
          <motion.div
            key={b.criterion}
            initial={{ opacity: 0, y: 6 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ duration: 0.3, delay: 0.24 + i * 0.08, ease: ease.out }}
            className="flex gap-2.5 text-sm"
          >
            <span aria-hidden className="mt-0.5 shrink-0">
              {b.icon}
            </span>
            <p className="leading-snug text-muted-foreground">
              <span className="font-medium text-foreground/85">{b.label}: </span>
              {b.justification}
            </p>
          </motion.div>
        ))}
      </div>
    </div>
  );
}

const panels = [SelectPanel, AnalysisPanel, RankPanel, ExplainPanel];

/* ─────────────────────── Texto (coluna esquerda) ─────────────────────── */

function StepText({
  step,
  index,
  progress,
  total,
}: {
  step: Step;
  index: number;
  progress: MotionValue<number>;
  total: number;
}) {
  // Cada bloco de texto tem 100vh e fica centralizado (`justify-center`) —
  // então, à medida que o container (altura ≈ total × 100vh) rola, o bloco i
  // fica exatamente centralizado na viewport quando o progresso do scroll é
  // i/(total-1), não i/total (isso combina com o arredondamento usado em
  // `ProductJourney` para trocar o mock — os dois têm que concordar).
  const { input, output } = stepOpacityStops(index, total);
  const opacity = useTransform(progress, (v) => interpolateStops(v, input, output));
  const scale = useTransform(opacity, (v) => 0.985 + ((v - 0.32) / 0.68) * 0.015);

  return (
    <motion.div
      style={{ opacity, scale }}
      className="flex min-h-[100vh] flex-col justify-center py-16"
    >
      <p className="text-[0.7rem] font-semibold uppercase tracking-[0.28em] text-gold/80">
        {step.kicker}
      </p>
      <h3 className="mt-3 max-w-md text-balance font-display text-2xl tracking-tight sm:text-[1.7rem]">
        {step.title}
      </h3>
      <p className="mt-3 max-w-sm text-pretty text-sm leading-relaxed text-muted-foreground sm:text-base">
        {step.description}
      </p>
    </motion.div>
  );
}

/* ─────────────────────── Composição ─────────────────────── */

export function ProductJourney() {
  const reduce = useReducedMotion();
  const containerRef = useRef<HTMLDivElement>(null);
  const [activeStep, setActiveStep] = useState(0);

  const { scrollYProgress } = useScroll({
    target: containerRef,
    offset: ["start start", "end end"],
  });

  useMotionValueEvent(scrollYProgress, "change", (v) => {
    // Mesmo modelo de centralização do `StepText`: o bloco i está centralizado
    // em v = i/(total-1), então o mock troca exatamente na metade do caminho
    // entre um centro e o próximo — é isso que `Math.round` dá de graça.
    const total = steps.length;
    const idx =
      total > 1
        ? Math.min(total - 1, Math.max(0, Math.round(v * (total - 1))))
        : 0;
    setActiveStep((prev) => (prev === idx ? prev : idx));
  });

  const ActivePanel = panels[activeStep];

  return (
    <>
      {/* Desktop — sticky scroll real */}
      <div ref={containerRef} className="relative hidden px-4 lg:block">
        <div className="mx-auto grid max-w-6xl gap-x-16 lg:grid-cols-[1.05fr_1fr]">
          <div>
            {steps.map((step, i) => (
              <StepText
                key={step.id}
                step={step}
                index={i}
                progress={scrollYProgress}
                total={steps.length}
              />
            ))}
          </div>
          <div className="relative">
            <div className="sticky top-28 py-16">
              <MockShell activeStep={activeStep}>
                <ActivePanel />
              </MockShell>
            </div>
          </div>
        </div>
      </div>

      {/* Mobile — fallback simples, sem scroll-jacking */}
      <div className="space-y-14 px-4 lg:hidden">
        {steps.map((step, i) => {
          const Panel = panels[i];
          return (
            <motion.div
              key={step.id}
              initial={reduce ? undefined : { opacity: 0, y: 18 }}
              whileInView={reduce ? undefined : { opacity: 1, y: 0 }}
              viewport={{ once: true, margin: "-60px" }}
              transition={{ duration: 0.45, ease: ease.out }}
              className="mx-auto max-w-md"
            >
              <p className="text-[0.7rem] font-semibold uppercase tracking-[0.28em] text-gold/80">
                {step.kicker}
              </p>
              <h3 className="mt-2.5 text-balance font-display text-xl tracking-tight sm:text-2xl">
                {step.title}
              </h3>
              <p className="mt-2.5 text-pretty text-sm leading-relaxed text-muted-foreground">
                {step.description}
              </p>
              <div className="mt-5">
                <MockShell activeStep={i}>
                  <Panel />
                </MockShell>
              </div>
            </motion.div>
          );
        })}
      </div>
    </>
  );
}
