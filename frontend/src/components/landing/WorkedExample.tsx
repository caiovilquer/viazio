import { motion } from "framer-motion";
import { Link } from "react-router-dom";
import { AlertTriangle, ArrowRight } from "lucide-react";
import { Flag } from "@/components/shared/Flag";
import { Reveal } from "@/components/shared/Reveal";
import { ScoreRing } from "@/components/shared/ScoreRing";
import { ScoreComposition } from "@/components/shared/ScoreComposition";
import { Button } from "@/components/ui/button";
import {
  demoWindow,
  landingExampleHref,
  landingExampleOriginExchangeToBrl,
  landingExampleRecommendations,
  landingExampleWindow,
} from "@/lib/landing-demo";
import { describeWindow, formatDateRange, formatExchange } from "@/lib/format";
import { ease } from "@/lib/motion";

export function WorkedExample() {
  const top = landingExampleRecommendations.slice(0, 3);

  return (
    <div className="mx-auto max-w-3xl">
      <Reveal className="rounded-3xl border border-hairline bg-surface/40 px-5 py-5 sm:px-7 sm:py-6">
        <div className="flex flex-wrap items-center justify-between gap-3">
          <div>
            <p className="text-[0.7rem] font-semibold uppercase tracking-[0.2em] text-gold/80">
              {formatDateRange(demoWindow.from, demoWindow.to)} ·{" "}
              {demoWindow.holidayName}
            </p>
            <p className="mt-1 text-sm text-foreground/80">
              {describeWindow(landingExampleWindow)} Saindo do{" "}
              {demoWindow.originLabel}.
            </p>
          </div>
          <Button
            asChild
            variant="outline"
            size="sm"
            className="shrink-0 gap-1.5 rounded-full"
          >
            <Link to={landingExampleHref()}>
              Rodar com minhas datas
              <ArrowRight className="size-3.5" />
            </Link>
          </Button>
        </div>
      </Reveal>

      <div className="mt-4 space-y-3">
        {top.map((dest, i) => {
          const exchangeLabel = formatExchange(
            dest.exchangeToBrl,
            landingExampleOriginExchangeToBrl,
            "BR",
            dest.countryCode,
          );
          const distanceKm = dest.feasibility?.travelEffort.distanceKm;
          return (
            <motion.div
              key={dest.countryCode}
              initial={{ opacity: 0, y: 18 }}
              whileInView={{ opacity: 1, y: 0 }}
              viewport={{ once: true, margin: "-60px" }}
              transition={{ duration: 0.4, delay: i * 0.07, ease: ease.out }}
            >
              <Link
                to={landingExampleHref(dest.countryCode)}
                className="group block overflow-hidden rounded-2xl border border-hairline bg-surface/60 p-4 elevate transition-[border-color,transform] duration-300 hover:-translate-y-0.5 hover:border-gold/25 sm:p-5"
              >
                <div className="flex items-start gap-3.5 sm:gap-4">
                  <span className="mt-1 inline-flex size-7 shrink-0 items-center justify-center rounded-full border border-hairline bg-surface-2 font-display text-xs text-muted-foreground">
                    {i + 1}
                  </span>
                  <Flag
                    code={dest.countryCode}
                    className="mt-1 h-6 w-9 shrink-0 rounded-[5px]"
                  />
                  <div className="min-w-0 flex-1">
                    <div className="flex items-baseline justify-between gap-3">
                      <p className="font-display text-lg tracking-tight">
                        {dest.countryName}
                      </p>
                      {(distanceKm != null || exchangeLabel) && (
                        <span className="hidden text-xs text-muted-foreground sm:inline">
                          {distanceKm != null &&
                            `${Math.round(distanceKm).toLocaleString("pt-BR")} km`}
                          {distanceKm != null && exchangeLabel && " · "}
                          {exchangeLabel}
                        </span>
                      )}
                    </div>

                    <div className="mt-2 flex flex-wrap gap-1.5">
                      {dest.highlights.slice(0, 2).map((h) => (
                        <span
                          key={h}
                          className="inline-flex items-center gap-1.5 rounded-full border border-hairline bg-surface-2/60 px-2.5 py-0.5 text-xs text-foreground/85"
                        >
                          <span className="size-1 rounded-full bg-gold/80" />
                          {h}
                        </span>
                      ))}
                      {dest.tradeoffs.slice(0, 1).map((t) => (
                        <span
                          key={t}
                          className="inline-flex items-center gap-1 rounded-full border border-chart-3/25 bg-chart-3/10 px-2.5 py-0.5 text-xs text-chart-3"
                        >
                          <AlertTriangle className="size-3" />
                          {t}
                        </span>
                      ))}
                    </div>

                    <ScoreComposition
                      breakdown={dest.breakdown}
                      size="sm"
                      showLabels
                      className="mt-3"
                    />
                  </div>
                  <ScoreRing
                    score={dest.tripScore}
                    size={48}
                    strokeWidth={5}
                    className="mt-1 shrink-0"
                  />
                </div>
              </Link>
            </motion.div>
          );
        })}
      </div>
    </div>
  );
}
