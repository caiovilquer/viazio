import { motion } from "framer-motion";
import { Link } from "react-router-dom";
import { ArrowUpRight } from "lucide-react";
import {
  demoFilterChips,
  demoWindow,
  landingExampleHref,
  landingExampleRecommendations,
  landingExampleWindow,
} from "@/lib/landing-demo";
import { Flag } from "@/components/shared/Flag";
import { ScoreRing } from "@/components/shared/ScoreRing";
import { ScoreComposition } from "@/components/shared/ScoreComposition";
import { formatDateRange } from "@/lib/format";
import { staggerContainer, heroItem } from "@/lib/motion";

/**
 * Visual central do hero — um "boarding pass" de decisão, não uma ilustração
 * solta. Cabeçalho com a janela + filtros aplicados, talão perfurado, corpo
 * com o ranking de um exemplo real (mesmos números de uma busca real para
 * essas datas — ver `lib/landing-demo.ts` — só não recalculados a cada
 * carregamento da home).
 */
export function HeroProductPreview() {
  const totalDays = landingExampleWindow.totalDays;
  const freeDays = landingExampleWindow.freeDays;
  const top = landingExampleRecommendations.slice(0, 3);

  return (
    <motion.div
      variants={heroItem}
      className="relative mx-auto mt-12 max-w-xl"
    >
      <Link
        to={landingExampleHref()}
        aria-label="Ver o ranking completo deste exemplo"
        className="group block overflow-hidden rounded-[1.75rem] border border-hairline bg-surface/80 text-left elevate-lg backdrop-blur-sm transition-[border-color,transform] duration-300 hover:-translate-y-1 hover:border-gold/30"
      >
        {/* Cabeçalho do talão */}
        <div className="relative px-5 pb-5 pt-5 sm:px-6">
          <div className="flex items-start justify-between gap-3">
            <div>
              <p className="text-[0.65rem] font-semibold uppercase tracking-[0.2em] text-gold/80">
                Exemplo ao vivo · saindo do {demoWindow.originLabel}
              </p>
              <p className="mt-1.5 font-display text-lg tracking-tight sm:text-xl">
                {formatDateRange(demoWindow.from, demoWindow.to)} ·{" "}
                {demoWindow.holidayName}
              </p>
            </div>
            <span className="mt-0.5 flex shrink-0 items-center gap-1 rounded-full border border-hairline bg-surface-2/60 px-2.5 py-1 text-[0.65rem] font-medium text-muted-foreground">
              {totalDays} dias
            </span>
          </div>

          {/* Barra de dias livres × dias de férias — mesmo motivo do WindowCard */}
          <div className="mt-3.5 flex gap-1">
            {Array.from({ length: totalDays }).map((_, i) => (
              <div
                key={i}
                className={
                  i < freeDays
                    ? "h-1.5 flex-1 rounded-full bg-gold/60"
                    : "h-1.5 flex-1 rounded-full bg-foreground/15"
                }
              />
            ))}
          </div>

          <div className="mt-4 flex flex-wrap gap-1.5">
            {demoFilterChips.map((chip) => (
              <span
                key={chip}
                className="rounded-full border border-hairline bg-surface-2/50 px-2.5 py-1 text-[0.7rem] font-medium text-foreground/80"
              >
                {chip}
              </span>
            ))}
          </div>
        </div>

        {/* Talão perfurado — divisória de ticket */}
        <div className="relative h-0 border-t border-dashed border-hairline">
          <span
            aria-hidden
            className="absolute -left-3 -top-3 size-6 rounded-full bg-background"
          />
          <span
            aria-hidden
            className="absolute -right-3 -top-3 size-6 rounded-full bg-background"
          />
        </div>

        {/* Corpo — ranking ao vivo */}
        <motion.div
          variants={staggerContainer(0.08, 0.1)}
          initial="hidden"
          whileInView="show"
          viewport={{ once: true, margin: "-60px" }}
          className="space-y-1 px-3 py-3 sm:px-4"
        >
          {top.map((dest, i) => (
            <motion.div
              key={dest.countryCode}
              variants={heroItem}
              className="flex items-center gap-3 rounded-2xl px-2.5 py-2.5 transition-colors group-hover:bg-surface-2/30 sm:gap-4"
            >
              <span className="w-4 shrink-0 text-center font-display text-sm text-muted-foreground">
                {i + 1}
              </span>
              <Flag
                code={dest.countryCode}
                className="h-5 w-7 shrink-0 rounded-[4px]"
              />
              <div className="min-w-0 flex-1">
                <p className="truncate text-sm font-medium tracking-tight sm:text-[0.95rem]">
                  {dest.countryName}
                </p>
                <ScoreComposition
                  breakdown={dest.breakdown}
                  size="sm"
                  className="mt-1"
                />
              </div>
              <ScoreRing score={dest.tripScore} size={40} strokeWidth={4} />
            </motion.div>
          ))}
        </motion.div>

        <div className="flex items-center justify-between gap-2 border-t border-hairline px-5 py-3.5 text-sm font-medium text-gold sm:px-6">
          Ver o ranking completo deste feriado
          <ArrowUpRight className="size-4 transition-transform duration-300 group-hover:translate-x-0.5 group-hover:-translate-y-0.5" />
        </div>
      </Link>
    </motion.div>
  );
}
