import type { ScoredCriterion } from "@/api/types";
import { scoreTierColor } from "@/lib/format";
import { scoreTone } from "@/lib/format";
import { cn } from "@/lib/utils";

/**
 * Decomposição visual de onde veio a nota do destino — uma barra empilhada
 * (largura do segmento = pontos contribuídos, cor = quão bem foi o critério)
 * mais legenda em linguagem simples. Feita para que ninguém precise fazer
 * peso × nota na cabeça: a barra e os números ao lado de cada ícone já
 * são essa conta, da esquerda para a direita por impacto (a API pré-ordena
 * `breakdown` por contribuição). Critérios indisponíveis ainda aparecem (apagados, "—")
 * para que um vão na barra leia "sem dado aqui", não "pontuou zero".
 */
export function ScoreComposition({
  breakdown,
  size = "md",
  showLabels = false,
  className,
}: {
  breakdown: ScoredCriterion[];
  size?: "sm" | "md";
  /** Mostra o nome de cada critério ao lado do ícone na legenda, não só os pontos. */
  showLabels?: boolean;
  className?: string;
}) {
  const available = breakdown.filter((b) => b.available && b.contribution > 0);

  return (
    <div className={cn("space-y-1.5", className)}>
      <div
        className={cn(
          "flex w-full overflow-hidden rounded-full bg-surface-3/60",
          size === "sm" ? "h-2" : "h-2.5",
        )}
        role="img"
        aria-label={`Composição da nota: ${breakdown
          .map((b) =>
            b.available
              ? `${b.label} contribuiu ${Math.round(b.contribution)} pontos`
              : `${b.label} sem dado disponível`,
          )
          .join(", ")}`}
      >
        {available.map((b) => (
          <div
            key={b.criterion}
            className="h-full transition-[width] duration-500"
            style={{
              width: `${b.contribution}%`,
              background: scoreTierColor[scoreTone(b.score)],
            }}
            title={`${b.label}: ${Math.round(b.contribution)} pts (peso ${Math.round(b.weight * 100)}% × nota ${Math.round(b.score)})`}
          />
        ))}
      </div>

      <div
        className={cn(
          "flex flex-wrap items-center gap-x-2.5 gap-y-1",
          size === "sm" ? "text-[0.65rem]" : "text-xs",
        )}
      >
        {breakdown.map((b) => (
          <span
            key={b.criterion}
            className={cn(
              "inline-flex items-center gap-1 tabular-nums",
              b.available
                ? "text-muted-foreground"
                : "text-muted-foreground/40",
            )}
            title={
              b.available
                ? `${b.label}: peso ${Math.round(b.weight * 100)}% × nota ${Math.round(b.score)} = ${Math.round(b.contribution)} pts`
                : `${b.label}: sem dado disponível, não conta na nota`
            }
          >
            <span aria-hidden className="leading-none">
              {b.icon}
            </span>
            {showLabels && <span className="hidden sm:inline">{b.label}</span>}
            {b.available ? Math.round(b.contribution) : "—"}
          </span>
        ))}
      </div>
    </div>
  );
}
