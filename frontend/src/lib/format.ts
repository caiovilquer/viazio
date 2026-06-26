export function formatBrl(value: number) {
  return new Intl.NumberFormat('pt-BR', { style: 'currency', currency: 'BRL' }).format(value)
}

export function formatDate(iso: string) {
  const date = new Date(`${iso}T00:00:00`)
  return new Intl.DateTimeFormat('pt-BR', { day: '2-digit', month: 'short' }).format(date)
}

export function formatDateLong(iso: string) {
  const date = new Date(`${iso}T00:00:00`)
  return new Intl.DateTimeFormat('pt-BR', { day: '2-digit', month: 'long', year: 'numeric' }).format(date)
}

export function formatDateRange(fromIso: string, toIso: string) {
  return `${formatDate(fromIso)} – ${formatDate(toIso)}`
}

export function formatScore(score: number) {
  return Math.round(score)
}

/** Real pt-BR pluralization — replaces backend "dia(s)" style placeholders. */
export function pluralize(count: number, singular: string, plural: string) {
  return `${count} ${count === 1 ? singular : plural}`
}

/** Human window summary from structured fields (no semicolons / "(s)"). */
export function describeWindow(window: {
  totalDays: number
  freeDays: number
  requiredLeaveDays: number
}) {
  const { totalDays, freeDays, requiredLeaveDays } = window
  if (requiredLeaveDays <= 0) {
    return `Os ${totalDays} dias já são livres — sem gastar férias.`
  }
  return `${freeDays} de ${totalDays} dias já são livres · requer ${pluralize(
    requiredLeaveDays,
    'dia',
    'dias',
  )} de férias.`
}

export function scoreTone(score: number) {
  if (score >= 80) return 'excellent'
  if (score >= 60) return 'good'
  if (score >= 40) return 'fair'
  return 'poor'
}

interface ExchangeLike {
  currency: string
  valueInReais: number
}

/**
 * Pick a quote unit (1 / 100 / 1.000) so the BRL amount stays readable for both
 * strong currencies (1 EUR = R$ 5,92) and tiny ones (1.000 COP = R$ 1,52) —
 * quoting per 1 unit would round small currencies to R$ 0,00. Returns null when
 * there's no usable rate (missing or zero).
 */
export function exchangeUnit(exchange?: ExchangeLike | null) {
  if (!exchange || !(exchange.valueInReais > 0)) return null
  const v = exchange.valueInReais
  const unit = v >= 0.1 ? 1 : v >= 0.01 ? 100 : 1000
  return {
    unit,
    unitLabel: unit.toLocaleString('pt-BR'),
    amount: v * unit,
    currency: exchange.currency,
  }
}

/** One-line exchange quote, e.g. "1 EUR = R$ 5,92" or "1.000 COP = R$ 1,52". */
export function formatExchange(exchange?: ExchangeLike | null): string | null {
  const e = exchangeUnit(exchange)
  if (!e) return null
  return `${e.unitLabel} ${e.currency} = ${formatBrl(e.amount)}`
}
