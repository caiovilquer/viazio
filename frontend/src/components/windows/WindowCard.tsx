import { motion } from "framer-motion";
import { Link } from "react-router-dom";
import { ArrowRight } from "lucide-react";
import type { WindowSuggestion } from "@/api/types";
import { ScoreRing } from "@/components/shared/ScoreRing";
import { Flag } from "@/components/shared/Flag";
import { Button } from "@/components/ui/button";
import { formatDateRange, pluralize } from "@/lib/format";
import { ease } from "@/lib/motion";
import { cn } from "@/lib/utils";

export function WindowCard({
  window,
  index,
  searchQuery,
}: {
  window: WindowSuggestion;
  index: number;
  searchQuery: string;
}) {
  const freeDays = Math.max(0, window.totalDays - window.requiredLeaveDays);
  const valueLine =
    window.requiredLeaveDays <= 0
      ? `${window.totalDays} dias de folga sem gastar férias`
      : `${window.totalDays} dias de folga por ${pluralize(window.requiredLeaveDays, "dia", "dias")} de férias`;

  return (
    <motion.div
      initial={{ opacity: 0, y: 22 }}
      animate={{ opacity: 1, y: 0 }}
      transition={{
        duration: 0.4,
        delay: Math.min(index * 0.06, 0.36),
        ease: ease.out,
      }}
      whileHover={{ y: -3 }}
      className="group rounded-2xl border border-hairline bg-surface/60 p-5 elevate transition-[box-shadow,border-color] hover:border-foreground/15 hover:elevate-lg sm:p-6"
    >
      <div className="flex items-start justify-between gap-4">
        <div className="min-w-0">
          <p className="mb-1 text-[0.7rem] font-semibold uppercase tracking-[0.18em] text-gold/80">
            #{index + 1} · {formatDateRange(window.start, window.end)}
          </p>
          <h3 className="text-balance font-display text-lg tracking-tight sm:text-xl">
            {window.label}
          </h3>
          <p className="mt-1 text-sm text-foreground/80">{valueLine}</p>
        </div>
        <ScoreRing
          score={window.timingScore}
          size={56}
          strokeWidth={5}
          label="timing"
        />
      </div>

      {/* Dias de relance — dias livres (o ganho) vs dias de férias (o custo) */}
      <div className="mt-5">
        <div className="flex gap-1">
          {Array.from({ length: window.totalDays }).map((_, i) => (
            <div
              key={i}
              className={cn(
                "h-7 flex-1 rounded-md",
                i < freeDays ? "bg-gold/45" : "bg-foreground/15",
              )}
            />
          ))}
        </div>
        <div className="mt-2.5 flex flex-wrap items-center gap-x-4 gap-y-1 text-xs text-muted-foreground">
          <span className="flex items-center gap-1.5">
            <span className="size-2 rounded-sm bg-gold/70" />
            {pluralize(freeDays, "dia livre", "dias livres")}
          </span>
          {window.requiredLeaveDays > 0 && (
            <span className="flex items-center gap-1.5">
              <span className="size-2 rounded-sm bg-foreground/30" />
              {pluralize(
                window.requiredLeaveDays,
                "dia de férias",
                "dias de férias",
              )}
            </span>
          )}
          {window.bridgeDaysUsed > 0 && (
            <span>
              ·{" "}
              {pluralize(
                window.bridgeDaysUsed,
                "dia de ponte",
                "dias de ponte",
              )}
            </span>
          )}
        </div>
      </div>

      <div className="mt-5">
        <Button
          asChild
          size="sm"
          className="w-full gap-2 rounded-full sm:w-auto"
        >
          <Link to={`/resultados?${searchQuery}`}>
            Ver ranking desta janela
            <ArrowRight className="size-3.5" />
          </Link>
        </Button>
      </div>

      {window.topDestinations.length > 0 && (
        <div className="mt-5 border-t border-hairline pt-4">
          <p className="mb-2.5 text-[0.7rem] font-semibold uppercase tracking-[0.14em] text-muted-foreground">
            Top destinos nessa janela
          </p>
          <div className="flex gap-2.5 overflow-x-auto pb-1 no-scrollbar">
            {window.topDestinations.map((dest) => (
              <Link
                key={dest.countryCode}
                to={`/destino/${dest.countryCode}?${searchQuery}`}
                state={{ recommendation: dest }}
                className="flex shrink-0 items-center gap-2.5 rounded-xl border border-hairline bg-surface-2/40 px-3 py-2 transition-colors hover:border-foreground/15"
              >
                <Flag code={dest.countryCode} className="h-4 w-6 shrink-0" />
                <div className="min-w-0">
                  <p className="max-w-[8rem] truncate text-sm font-medium">
                    {dest.countryName}
                  </p>
                  <p className="text-xs tabular-nums text-gold">
                    {Math.round(dest.tripScore)} pts
                  </p>
                </div>
              </Link>
            ))}
          </div>
        </div>
      )}
    </motion.div>
  );
}
