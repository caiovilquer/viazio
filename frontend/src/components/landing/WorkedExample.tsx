import { motion } from "framer-motion";
import { Link } from "react-router-dom";
import { ArrowRight } from "lucide-react";
import { Flag } from "@/components/shared/Flag";
import { Reveal } from "@/components/shared/Reveal";
import { ScoreRing } from "@/components/shared/ScoreRing";
import { ScoreComposition } from "@/components/shared/ScoreComposition";
import { DestinationTags } from "@/components/shared/DestinationTags";
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
                <div className="flex items-start gap-3 sm:gap-4">
                  <span className="mt-1 inline-flex size-7 shrink-0 items-center justify-center rounded-full border border-hairline bg-surface-2 font-display text-xs text-muted-foreground">
                    {i + 1}
                  </span>
                  <Flag
                    code={dest.countryCode}
                    className="mt-1 h-6 w-9 shrink-0 rounded-[5px]"
                  />
                  <div className="min-w-0 flex-1">
                    <div className="flex items-start justify-between gap-3">
                      <div className="min-w-0">
                        <p className="truncate font-display text-lg tracking-tight">
                          {dest.countryName}
                        </p>
                        {(distanceKm != null || exchangeLabel) && (
                          <p className="mt-0.5 text-xs text-muted-foreground">
                            {distanceKm != null &&
                              `${Math.round(distanceKm).toLocaleString("pt-BR")} km`}
                            {distanceKm != null && exchangeLabel && " · "}
                            {exchangeLabel}
                          </p>
                        )}
                      </div>
                      <ScoreRing
                        score={dest.tripScore}
                        size={44}
                        strokeWidth={5}
                        className="shrink-0 sm:hidden"
                      />
                    </div>

                    <DestinationTags
                      highlights={dest.highlights}
                      tradeoffs={dest.tradeoffs}
                      className="mt-2.5"
                    />

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
                    className="mt-1 hidden shrink-0 sm:flex"
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
