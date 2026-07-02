import type { DataSourceInfo } from "@/api/types";
import { Reveal } from "@/components/shared/Reveal";
import { cn } from "@/lib/utils";

const modeLabel: Record<DataSourceInfo["mode"], string> = {
  STATIC: "Catálogo fixo",
  LIVE: "Consulta em tempo real",
  LIVE_AND_HISTORICAL: "Tempo real + histórico",
  LIVE_CACHED: "Tempo real, cache diário",
};

const modeDotClass: Record<DataSourceInfo["mode"], string> = {
  STATIC: "bg-muted-foreground/50",
  LIVE: "bg-gold",
  LIVE_AND_HISTORICAL: "bg-gold",
  LIVE_CACHED: "bg-primary",
};

export function DataSourcesPanel({
  sources,
}: {
  sources: DataSourceInfo[];
}) {
  return (
    <Reveal className="mx-auto max-w-3xl">
      <div className="overflow-hidden rounded-3xl border border-hairline bg-surface/50 elevate">
        <div className="border-b border-hairline px-5 py-4 sm:px-7">
          <p className="font-display text-lg tracking-tight">
            Nenhuma nota nasce de achismo
          </p>
          <p className="mt-1 text-sm text-muted-foreground">
            Cada critério aponta para uma fonte pública específica, sem
            caixa-preta, sem selo decorativo.
          </p>
        </div>
        <div className="grid gap-px bg-hairline sm:grid-cols-2">
          {sources.map((source) => (
            <div
              key={source.key}
              className="flex items-start gap-3 bg-surface/50 px-5 py-4 sm:px-7"
            >
              <span
                aria-hidden
                className={cn("mt-1.5 size-1.5 shrink-0 rounded-full", modeDotClass[source.mode])}
              />
              <div className="min-w-0">
                <div className="flex flex-wrap items-baseline gap-x-2">
                  <p className="text-sm font-medium text-foreground/90">
                    {source.label}
                  </p>
                  <span className="text-[0.65rem] uppercase tracking-wide text-muted-foreground/70">
                    {modeLabel[source.mode]}
                  </span>
                </div>
                <p className="mt-0.5 text-xs leading-relaxed text-muted-foreground">
                  {source.purpose}
                </p>
              </div>
            </div>
          ))}
        </div>
      </div>
    </Reveal>
  );
}
