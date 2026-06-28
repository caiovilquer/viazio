import { Link } from "react-router-dom";
import { motion } from "framer-motion";
import {
  ArrowRight,
  CalendarRange,
  Sparkles,
  SlidersHorizontal,
} from "lucide-react";
import { useMeta } from "@/api/queries";
import { Button } from "@/components/ui/button";
import { Reveal } from "@/components/shared/Reveal";
import { ExampleSearchChips } from "@/components/landing/ExampleSearchChips";
import { heroItem, staggerContainer } from "@/lib/motion";

const steps = [
  {
    icon: CalendarRange,
    title: "Diga quando você pode viajar",
    description:
      "Escolha as datas e quantos dias de férias você tem para gastar.",
  },
  {
    icon: SlidersHorizontal,
    title: "Conte o que importa pra você",
    description:
      "Clima, custo, distância ou festividades — um perfil pronto ou pesos sob medida.",
  },
  {
    icon: Sparkles,
    title: "Receba um ranking explicado",
    description:
      "Cruzamos feriados, clima, câmbio e distância e mostramos o porquê de cada nota.",
  },
];

const signals = ["Clima", "Custo de vida", "Distância & fuso", "Festividades"];

/** Motivo "rota" discreto — linha dourada fina que se desenha com paradas. */
function HeroRoute() {
  return (
    <svg
      aria-hidden
      viewBox="0 0 800 140"
      fill="none"
      className="pointer-events-none absolute left-1/2 top-1/2 -z-0 h-40 w-[min(64rem,92vw)] -translate-x-1/2 -translate-y-1/2 text-gold opacity-[0.45]"
    >
      <motion.path
        d="M30 104 C 210 36, 330 36, 470 78 C 600 116, 700 56, 778 44"
        stroke="currentColor"
        strokeOpacity="0.32"
        strokeWidth="1.4"
        strokeLinecap="round"
        initial={{ pathLength: 0, opacity: 0 }}
        animate={{ pathLength: 1, opacity: 1 }}
        transition={{ duration: 1.6, ease: [0.22, 1, 0.36, 1], delay: 0.3 }}
      />
      {[
        { cx: 30, cy: 104, r: 2.5 },
        { cx: 470, cy: 78, r: 2.5 },
        { cx: 778, cy: 44, r: 3.2 },
      ].map((n, i) => (
        <motion.circle
          key={n.cx}
          cx={n.cx}
          cy={n.cy}
          r={n.r}
          fill="currentColor"
          initial={{ scale: 0, opacity: 0 }}
          animate={{ scale: 1, opacity: 0.8 }}
          transition={{
            delay: 0.6 + i * 0.45,
            type: "spring",
            stiffness: 400,
            damping: 24,
          }}
        />
      ))}
    </svg>
  );
}

export function LandingPage() {
  const { data: meta } = useMeta();

  return (
    <div className="overflow-hidden">
      {/* ───────── Destaque principal ───────── */}
      <section className="relative px-4 pb-24 pt-20 sm:pt-28">
        <HeroRoute />

        <motion.div
          variants={staggerContainer(0.1, 0.05)}
          initial="hidden"
          animate="show"
          className="relative z-10 mx-auto max-w-4xl text-center"
        >
          <motion.p
            variants={heroItem}
            className="mb-7 flex items-center justify-center gap-3 text-[0.7rem] font-semibold uppercase tracking-[0.32em] text-gold/80"
          >
            <span>Planeje</span>
            <span className="size-1 rounded-full bg-gold/70" />
            <span>Descubra</span>
            <span className="size-1 rounded-full bg-gold/70" />
            <span>Viva</span>
          </motion.p>

          <motion.h1
            variants={heroItem}
            className="text-balance font-display text-[2.6rem] leading-[1.03] tracking-tight sm:text-6xl lg:text-[4.5rem]"
          >
            Transforme dias de folga em viagens{" "}
            <span className="text-gold-gradient">inesquecíveis</span>
          </motion.h1>

          <motion.p
            variants={heroItem}
            className="mx-auto mt-6 max-w-xl text-pretty text-base text-muted-foreground sm:text-lg"
          >
            A Viazio cruza clima, câmbio, custo de vida e festividades — e
            explica, em uma nota clara, qual destino vale o seu próximo
            feriadão.
          </motion.p>

          <motion.div
            variants={heroItem}
            className="mt-9 flex flex-col items-center justify-center gap-3 sm:flex-row"
          >
            <Button
              asChild
              size="xl"
              className="w-full rounded-full glow-coral sm:w-auto"
            >
              <Link to="/buscar">
                Planejar meu feriadão
                <ArrowRight className="size-4" />
              </Link>
            </Button>
            <Button
              asChild
              size="xl"
              variant="glass"
              className="w-full rounded-full sm:w-auto"
            >
              <Link to="/janelas">Ver melhores janelas</Link>
            </Button>
          </motion.div>

          <motion.div variants={heroItem}>
            <ExampleSearchChips />
          </motion.div>

          <motion.div
            variants={heroItem}
            className="mt-12 flex flex-wrap items-center justify-center gap-x-3 gap-y-2 text-[0.7rem] font-medium uppercase tracking-[0.18em] text-muted-foreground"
          >
            {signals.map((s, i) => (
              <span key={s} className="flex items-center gap-3">
                {i > 0 && <span className="size-1 rounded-full bg-gold/50" />}
                {s}
              </span>
            ))}
          </motion.div>
        </motion.div>
      </section>

      {/* ───────── Como funciona ───────── */}
      <section className="px-4 py-20 sm:py-28">
        <div className="mx-auto max-w-5xl">
          <Reveal className="mx-auto max-w-xl text-center">
            <p className="text-[0.7rem] font-semibold uppercase tracking-[0.28em] text-gold/80">
              Como funciona
            </p>
            <h2 className="mt-3 text-balance font-display text-3xl tracking-tight sm:text-4xl">
              Três passos entre a folga e o embarque
            </h2>
          </Reveal>

          <div className="mt-14 grid gap-5 sm:grid-cols-3">
            {steps.map((step, i) => (
              <Reveal key={step.title} delay={i * 0.08}>
                <div className="group relative h-full overflow-hidden rounded-2xl border border-hairline bg-surface/60 p-7 elevate transition-[transform,border-color] duration-300 hover:-translate-y-1 hover:border-gold/25">
                  <div className="flex items-start justify-between">
                    <span className="font-display text-4xl leading-none text-gold-gradient">
                      {String(i + 1).padStart(2, "0")}
                    </span>
                    <span className="flex size-10 items-center justify-center rounded-full border border-hairline bg-surface-2 text-muted-foreground transition-colors group-hover:text-gold">
                      <step.icon className="size-4.5" strokeWidth={1.8} />
                    </span>
                  </div>
                  <p className="mt-6 font-display text-lg tracking-tight">
                    {step.title}
                  </p>
                  <p className="mt-2 text-sm leading-relaxed text-muted-foreground">
                    {step.description}
                  </p>
                </div>
              </Reveal>
            ))}
          </div>
        </div>
      </section>

      {/* ───────── Fontes de dados ───────── */}
      {meta && (
        <section className="px-4 pb-24">
          <Reveal className="mx-auto max-w-3xl">
            <div className="rounded-3xl border border-hairline bg-surface/50 p-8 text-center elevate sm:p-10">
              <p className="font-display text-xl tracking-tight">
                Dados de fontes abertas e confiáveis
              </p>
              <p className="mx-auto mt-2 max-w-md text-sm text-muted-foreground">
                Nada de caixa-preta. Cada nota nasce de fontes públicas e
                auditáveis.
              </p>
              <div className="mt-7 flex flex-wrap justify-center gap-2">
                {meta.dataSources.map((source) => (
                  <span
                    key={source.key}
                    className="inline-flex items-center gap-1.5 rounded-full border border-hairline bg-surface-2/60 px-3.5 py-1.5 text-xs font-medium text-foreground/85"
                  >
                    <span className="size-1 rounded-full bg-gold/70" />
                    {source.label}
                  </span>
                ))}
              </div>
            </div>
          </Reveal>
        </section>
      )}

      {/* ───────── Fechamento ───────── */}
      <section className="px-4 pb-28">
        <Reveal className="mx-auto max-w-3xl text-center">
          <h2 className="text-balance font-display text-3xl tracking-tight sm:text-4xl">
            Seu próximo feriadão merece mais que um chute.
          </h2>
          <div className="mt-8">
            <Button asChild size="xl" className="rounded-full glow-coral">
              <Link to="/buscar">
                Planejar agora
                <ArrowRight className="size-4" />
              </Link>
            </Button>
          </div>
        </Reveal>
      </section>
    </div>
  );
}
