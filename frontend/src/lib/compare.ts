export type WinnerDirection = 'higher' | 'lower'

export function winnerIndices(values: Array<number | null | undefined>, direction: WinnerDirection): Set<number> {
  const valid = values
    .map((v, i) => ({ v, i }))
    .filter((x): x is { v: number; i: number } => x.v !== null && x.v !== undefined && !Number.isNaN(x.v))
  if (valid.length < 2) return new Set()

  const target = direction === 'higher' ? Math.max(...valid.map((x) => x.v)) : Math.min(...valid.map((x) => x.v))
  const winners = valid.filter((x) => Math.abs(x.v - target) < 1e-9)
  if (winners.length === valid.length) return new Set()
  return new Set(winners.map((x) => x.i))
}
