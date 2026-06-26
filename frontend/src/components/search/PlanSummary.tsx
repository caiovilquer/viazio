import { ArrowRight } from 'lucide-react'
import { Button } from '@/components/ui/button'
import { Flag } from '@/components/shared/Flag'
import { cn } from '@/lib/utils'

function Row({ label, value, muted }: { label: string; value: string; muted?: boolean }) {
  return (
    <div className="flex items-baseline justify-between gap-3">
      <dt className="shrink-0 text-[0.7rem] font-semibold uppercase tracking-[0.08em] text-muted-foreground">
        {label}
      </dt>
      <dd
        className={cn(
          'truncate text-right text-sm font-medium',
          muted ? 'text-muted-foreground/60' : 'text-foreground',
        )}
      >
        {value}
      </dd>
    </div>
  )
}

export function PlanSummary({
  originCode,
  originLabel,
  destinationLabel,
  dateRangeLabel,
  dayCount,
  profileLabel,
  travelers,
  budgetLabel,
  canSubmit,
  onSubmit,
}: {
  originCode: string
  originLabel: string
  destinationLabel: string | null
  dateRangeLabel: string
  dayCount: number
  profileLabel: string
  travelers: number
  budgetLabel?: string
  canSubmit: boolean
  onSubmit: () => void
}) {
  return (
    <div className="rounded-2xl border border-hairline bg-surface/60 p-6 elevate">
      <p className="text-[0.7rem] font-semibold uppercase tracking-[0.18em] text-gold/80">
        Seu plano
      </p>

      <dl className="mt-5 space-y-3.5">
        <div className="flex items-baseline justify-between gap-3">
          <dt className="shrink-0 text-[0.7rem] font-semibold uppercase tracking-[0.08em] text-muted-foreground">
            Origem
          </dt>
          <dd className="flex items-center gap-1.5 truncate text-right text-sm font-medium text-foreground">
            <Flag code={originCode} className="h-3 w-4" />
            {originLabel}
          </dd>
        </div>
        <Row
          label="Destino"
          value={destinationLabel ?? 'a escolher'}
          muted={!destinationLabel}
        />
        <Row label="Período" value={`${dateRangeLabel} · ${dayCount} ${dayCount === 1 ? 'dia' : 'dias'}`} />
        <Row label="Prioridade" value={profileLabel} />
        <Row label="Viajantes" value={`${travelers} ${travelers === 1 ? 'pessoa' : 'pessoas'}`} />
        <Row label="Orçamento" value={budgetLabel ?? 'sem limite'} muted={!budgetLabel} />
      </dl>

      <div className="mt-6 border-t border-hairline pt-5">
        <Button
          size="lg"
          className="w-full rounded-full glow-coral"
          disabled={!canSubmit}
          onClick={onSubmit}
        >
          Encontrar meus destinos
          <ArrowRight className="size-4" />
        </Button>
        {!canSubmit && (
          <p className="mt-3 text-center text-xs text-muted-foreground">
            Escolha um destino para continuar.
          </p>
        )}
      </div>
    </div>
  )
}
