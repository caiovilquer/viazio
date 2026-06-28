export function scoreTone(score: number) {
  if (score >= 80) return "excellent";
  if (score >= 60) return "good";
  if (score >= 40) return "fair";
  return "poor";
}

/** Faixas quentes para visualizações de nota (ScoreRing, ClimateChart, mapa). */
export const scoreTierColor: Record<string, string> = {
  excellent: "var(--gold)",
  good: "var(--primary)",
  fair: "var(--chart-3)",
  poor: "var(--chart-5)",
};
