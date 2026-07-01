import { AlertTriangle, CircleDashed } from "lucide-react";
import { cn } from "@/lib/utils";

/**
 * O motor gera um "tradeoff" tanto para um critério real que pesou contra
 * (ex.: "Viagem longa: ~3.000 km") quanto para um critério que simplesmente
 * não tem dado (ex.: "Custo de vida: dado indisponível") — sintaticamente são
 * a mesma coisa, mas semanticamente bem diferentes: o primeiro é um alerta de
 * verdade, o segundo é só uma lacuna de dado. Tratar os dois com o mesmo selo
 * vermelho assusta o usuário por algo que nem é um problema.
 */
function isUnavailableTradeoff(tradeoff: string): boolean {
  return tradeoff.endsWith("dado indisponível");
}

/**
 * Selos de destaques (bons pontos) e contrapontos de um destino — usado tanto
 * nos cards de resultado quanto no exemplo da landing, para os dois ficarem
 * consistentes (e para corrigir o estilo em um só lugar).
 */
export function DestinationTags({
  highlights,
  tradeoffs,
  maxHighlights = 2,
  maxTradeoffs = 1,
  className,
}: {
  highlights: string[];
  tradeoffs: string[];
  maxHighlights?: number;
  maxTradeoffs?: number;
  className?: string;
}) {
  const shownHighlights = highlights.slice(0, maxHighlights);
  const shownTradeoffs = tradeoffs.slice(0, maxTradeoffs);

  return (
    <div className={cn("flex flex-wrap gap-1.5", className)}>
      {shownHighlights.map((h) => (
        <span
          key={h}
          className="inline-flex items-center gap-1.5 rounded-full border border-hairline bg-surface-2/60 px-2.5 py-0.5 text-xs text-foreground/85"
        >
          <span className="size-1 shrink-0 rounded-full bg-gold/80" />
          {h}
        </span>
      ))}
      {shownTradeoffs.map((t) =>
        isUnavailableTradeoff(t) ? (
          <span
            key={t}
            className="inline-flex items-center gap-1.5 rounded-full border border-hairline bg-surface-2/40 px-2.5 py-0.5 text-xs text-muted-foreground/70"
          >
            <CircleDashed className="size-3 shrink-0" />
            {t}
          </span>
        ) : (
          <span
            key={t}
            className="inline-flex items-center gap-1.5 rounded-full border border-chart-3/25 bg-chart-3/10 px-2.5 py-0.5 text-xs text-chart-3"
          >
            <AlertTriangle className="size-3 shrink-0" />
            {t}
          </span>
        ),
      )}
      {highlights.length === 0 && tradeoffs.length === 0 && (
        <span className="text-sm text-muted-foreground">
          Destino tranquilo, sem grandes destaques.
        </span>
      )}
    </div>
  );
}
