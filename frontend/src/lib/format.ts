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

export function scoreTone(score: number) {
  if (score >= 80) return 'excellent'
  if (score >= 60) return 'good'
  if (score >= 40) return 'fair'
  return 'poor'
}
