import { useRef } from "react";
import { Link } from "react-router-dom";
import { motion } from "framer-motion";
import { ArrowRight } from "lucide-react";
import { useMeta } from "@/api/queries";
import { Button } from "@/components/ui/button";
import { Reveal } from "@/components/shared/Reveal";
import { HeroProductPreview } from "@/components/landing/HeroProductPreview";
import { ProductJourney } from "@/components/landing/ProductJourney";
import { RouteProgress } from "@/components/landing/RouteProgress";
import { WorkedExample } from "@/components/landing/WorkedExample";
import { ComparisonShowdown } from "@/components/landing/ComparisonShowdown";
import { DataSourcesPanel } from "@/components/landing/DataSourcesPanel";
import { dataSourcesFallback, landingExampleRecommendations } from "@/lib/landing-demo";
import { heroItem, staggerContainer } from "@/lib/motion";

const criteriaSignals = [
  { icon: "☀️", label: "Clima" },
  { icon: "💰", label: "Custo de vida" },
  { icon: "✈️", label: "Distância" },
  { icon: "🎊", label: "Festividades" },
];

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

function SectionHeading({
  kicker,
  title,
  description,
}: {
  kicker: string;
  title: string;
  description?: string;
}) {
  return (
    <Reveal className="mx-auto max-w-xl text-center">
      <p className="text-[0.7rem] font-semibold uppercase tracking-[0.28em] text-gold/80">
        {kicker}
      </p>
      <h2 className="mt-3 text-balance font-display text-3xl tracking-tight sm:text-4xl">
        {title}
      </h2>
      {description && (
        <p className="mx-auto mt-3 max-w-md text-pretty text-sm text-muted-foreground sm:text-base">
          {description}
        </p>
      )}
    </Reveal>
  );
}

export function LandingPage() {
  const { data: meta } = useMeta();
  const dataSources = meta?.dataSources ?? dataSourcesFallback;
  const [comparisonA, comparisonB] = landingExampleRecommendations;
  const pageRef = useRef<HTMLDivElement>(null);

  return (
    <div ref={pageRef} className="relative">
      <RouteProgress containerRef={pageRef} />

      {/* ───────── Hero — produto, não slogan ─────────
          overflow-hidden fica só aqui (não no container inteiro): a seção de
          demonstração mais abaixo depende de `position: sticky`, que um
          ancestral com overflow não-visible quebraria. */}
      <section className="relative overflow-hidden px-4 pb-20 pt-20 sm:pt-28">
        <HeroRoute />

        <motion.div
          variants={staggerContainer(0.1, 0.05)}
          initial="hidden"
          animate="show"
          className="relative z-10 mx-auto max-w-3xl text-center"
        >
          <motion.h1
            variants={heroItem}
            className="text-balance font-display text-[2.5rem] leading-[1.05] tracking-tight sm:text-5xl lg:text-[3.6rem]"
          >
            Escolha um feriado.{" "}
            <span className="text-gold-gradient">
              Veja para onde ele realmente compensa ir.
            </span>
          </motion.h1>

          <motion.p
            variants={heroItem}
            className="mx-auto mt-5 max-w-lg text-pretty text-base text-muted-foreground sm:text-lg"
          >
            O Viazio cruza clima, custo de vida, distância e festividades de
            cada destino na sua janela de folga e devolve uma nota explicada
            para cada um, não uma lista de sugestões soltas.
          </motion.p>

          <motion.div
            variants={heroItem}
            className="mt-8 flex flex-col items-center justify-center gap-3 sm:flex-row"
          >
            <Button
              asChild
              size="xl"
              className="w-full rounded-full glow-coral sm:w-auto"
            >
              <Link to="/buscar">
                Simular meu feriado
                <ArrowRight className="size-4" />
              </Link>
            </Button>
            <Button
              asChild
              size="xl"
              variant="glass"
              className="w-full rounded-full sm:w-auto"
            >
              <Link to="/janelas">Ver melhores janelas do ano</Link>
            </Button>
          </motion.div>
        </motion.div>

        <HeroProductPreview />

        <motion.div
          variants={heroItem}
          initial="hidden"
          animate="show"
          className="relative z-10 mx-auto mt-9 flex max-w-xl flex-wrap items-center justify-center gap-x-4 gap-y-2 text-xs text-muted-foreground"
        >
          {criteriaSignals.map((s, i) => (
            <span key={s.label} className="flex items-center gap-2">
              {i > 0 && <span className="size-1 rounded-full bg-gold/50" />}
              <span aria-hidden>{s.icon}</span>
              {s.label}
            </span>
          ))}
        </motion.div>
      </section>

      {/* ───────── Demonstração — o produto de verdade, amarrado ao scroll ───────── */}
      <section className="relative py-16 sm:py-24">
        <ProductJourney />
      </section>

      {/* ───────── Exemplo real de recomendação ───────── */}
      <section className="px-4 py-16 sm:py-24">
        <SectionHeading
          kicker="Exemplo real"
          title="Feriado de 4 dias, saindo do Brasil"
          description="12 de outubro, mesma busca que você faria. Para essas datas, estes destinos ficam com notas bem diferentes."
        />
        <div className="mt-10">
          <WorkedExample />
        </div>
      </section>

      {/* ───────── Comparação direta ───────── */}
      <section className="px-4 py-16 sm:py-24">
        <SectionHeading
          kicker="Comparação direta"
          title={`Por que ${comparisonA.countryName} venceu ${comparisonB.countryName}`}
          description="Mesma janela de viagem, critério por critério, para você ver de onde vem a diferença, não só o resultado final."
        />
        <div className="mt-10">
          <ComparisonShowdown />
        </div>
      </section>

      {/* ───────── Fontes de dados ───────── */}
      <section className="px-4 py-16 sm:py-20">
        <SectionHeading kicker="Fontes de dados" title="De onde vêm os números" />
        <div className="mt-10">
          <DataSourcesPanel sources={dataSources} />
        </div>
      </section>

      {/* ───────── Fechamento ───────── */}
      <section className="px-4 pb-28 pt-4">
        <Reveal className="mx-auto max-w-2xl text-center">
          <h2 className="text-balance font-display text-3xl tracking-tight sm:text-4xl">
            Descubra o melhor destino para o seu próximo feriado.
          </h2>
          <p className="mx-auto mt-3 max-w-md text-sm text-muted-foreground">
            Informe suas datas, de onde você sai e o que importa pra você. O
            ranking sai em segundos, com a nota explicada.
          </p>
          <div className="mt-8">
            <Button asChild size="xl" className="rounded-full glow-coral">
              <Link to="/buscar">
                Simular meu feriado
                <ArrowRight className="size-4" />
              </Link>
            </Button>
          </div>
        </Reveal>
      </section>
    </div>
  );
}
