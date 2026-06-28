import type { CriterionKey, ScoredCriterion, TravelRecommendation } from '@/api/types'

// Mirrors TravelRecommendationEngine.buildRecommendation (backend) so weight changes
// can be previewed instantly client-side, without refetching already-fetched candidates.
const DESTINATION_SHARE = 0.8
const WINDOW_SHARE = 0.2
const MIN_CONFIDENCE_MULTIPLIER = 0.75

function round1(value: number) {
  return Math.round(value * 10) / 10
}

// round1 is 1 decimal place — fine for 0–100 scores, far too coarse for 0–1 weights:
// 0.25 would round to 0.3 (round-half-up at the 0.05 boundary), and four criteria at
// 0.25 each would all individually round up, displaying "peso 30%" four times and
// summing to a nonsensical 120%. 3 decimals gives 0.1-percentage-point display precision.
function roundWeight(value: number) {
  return Math.round(value * 1000) / 1000
}

function normalizeWeights(raw: Record<CriterionKey, number>): Record<CriterionKey, number> {
  const keys = Object.keys(raw) as CriterionKey[]
  const clamped = Object.fromEntries(keys.map((k) => [k, Math.max(0, raw[k] ?? 0)])) as Record<CriterionKey, number>
  const sum = keys.reduce((acc, k) => acc + clamped[k], 0)
  if (sum <= 0) {
    const equal = 1 / keys.length
    return Object.fromEntries(keys.map((k) => [k, equal])) as Record<CriterionKey, number>
  }
  return Object.fromEntries(keys.map((k) => [k, clamped[k] / sum])) as Record<CriterionKey, number>
}

export function rescoreRecommendation(
  recommendation: TravelRecommendation,
  rawWeights: Record<CriterionKey, number>,
): TravelRecommendation {
  const weights = normalizeWeights(rawWeights)
  const availableWeight = recommendation.breakdown
    .filter((entry) => entry.available)
    .reduce((sum, entry) => sum + (weights[entry.criterion] ?? 0), 0)

  let destinationScore = 0
  const breakdown: ScoredCriterion[] = recommendation.breakdown.map((entry) => {
    const effectiveWeight = entry.available && availableWeight > 0 ? (weights[entry.criterion] ?? 0) / availableWeight : 0
    const contribution = effectiveWeight * entry.score
    destinationScore += contribution
    return { ...entry, weight: roundWeight(effectiveWeight), contribution: round1(contribution) }
  })
  breakdown.sort((a, b) => b.contribution - a.contribution)

  const coverage = Math.max(0, Math.min(1, availableWeight))
  const confidenceScore = round1(coverage * 100)
  const combined = DESTINATION_SHARE * destinationScore + WINDOW_SHARE * recommendation.windowScore
  const confidenceMultiplier = MIN_CONFIDENCE_MULTIPLIER + (1 - MIN_CONFIDENCE_MULTIPLIER) * coverage
  const tripScore = combined * confidenceMultiplier

  const reasons = recommendation.highlights.length === 0 ? 'sem destaque dominante' : recommendation.highlights.join(', ')
  const summary = `${recommendation.countryName} — nota de viagem ${Math.round(tripScore)}: ${reasons}; confiança ${Math.round(confidenceScore)}%`

  return {
    ...recommendation,
    destinationScore: round1(destinationScore),
    tripScore: round1(tripScore),
    breakdown,
    summary,
    dataQuality: {
      ...recommendation.dataQuality,
      coverage: round1(coverage),
      confidenceScore,
    },
  }
}

export function rescoreAll(
  recommendations: TravelRecommendation[],
  weights: Record<CriterionKey, number>,
): TravelRecommendation[] {
  return recommendations
    .map((rec) => rescoreRecommendation(rec, weights))
    .sort((a, b) => b.tripScore - a.tripScore)
}

export function weightsEqual(a: Record<CriterionKey, number>, b: Record<CriterionKey, number>) {
  const keys = new Set([...Object.keys(a), ...Object.keys(b)]) as Set<CriterionKey>
  for (const key of keys) {
    if (Math.abs((a[key] ?? 0) - (b[key] ?? 0)) > 1e-6) return false
  }
  return true
}
