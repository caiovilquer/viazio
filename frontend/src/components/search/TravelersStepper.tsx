import { Minus, Plus, Users } from 'lucide-react'
import { Button } from '@/components/ui/button'

export function TravelersStepper({
  value,
  max = 10,
  onChange,
}: {
  value: number
  max?: number
  onChange: (value: number) => void
}) {
  return (
    <div className="flex items-center justify-between rounded-xl border border-hairline bg-surface/50 px-4 py-3">
      <span className="flex items-center gap-2.5 text-sm font-medium">
        <Users className="size-4 text-muted-foreground" />
        Viajantes
      </span>
      <div className="flex items-center gap-3">
        <Button
          type="button"
          variant="outline"
          size="icon-sm"
          className="rounded-full"
          disabled={value <= 1}
          onClick={() => onChange(Math.max(1, value - 1))}
          aria-label="Remover viajante"
        >
          <Minus className="size-3.5" />
        </Button>
        <span className="w-6 text-center text-sm font-semibold tabular-nums">{value}</span>
        <Button
          type="button"
          variant="outline"
          size="icon-sm"
          className="rounded-full"
          disabled={value >= max}
          onClick={() => onChange(Math.min(max, value + 1))}
          aria-label="Adicionar viajante"
        >
          <Plus className="size-3.5" />
        </Button>
      </div>
    </div>
  )
}
