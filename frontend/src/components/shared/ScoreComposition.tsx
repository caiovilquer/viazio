import type { ScoredCriterion } from '@/api/types'
import { scoreTierColor } from './ScoreRing'
import { scoreTone } from '@/lib/format'
import { cn } from '@/lib/utils'

/**
 * Visual breakdown of where a destination's score came from — a single stacked bar
 * (segment width = points contributed, segment color = how well that criterion did)
 * plus a plain-language legend. Built so a layperson never has to do the
 * weight × score math themselves: the bar and the numbers next to each icon already
 * are that math, laid out left to right by impact (the API pre-sorts `breakdown` by
 * contribution). Unavailable criteria still show up (dimmed, "—") so a visible gap in
 * the bar reads as "no data here", not "this scored zero".
 */
export function ScoreComposition({
  breakdown,
  size = 'md',
  showLabels = false,
  className,
}: {
  breakdown: ScoredCriterion[]
  size?: 'sm' | 'md'
  /** Show each criterion's name next to its icon in the legend, not just the points. */
  showLabels?: boolean
  className?: string
}) {
  const available = breakdown.filter((b) => b.available && b.contribution > 0)

  return (
    <div className={cn('space-y-1.5', className)}>
      <div
        className={cn(
          'flex w-full overflow-hidden rounded-full bg-surface-3/60',
          size === 'sm' ? 'h-2' : 'h-2.5',
        )}
        role="img"
        aria-label={`Composição da nota: ${breakdown
          .map((b) =>
            b.available
              ? `${b.label} contribuiu ${Math.round(b.contribution)} pontos`
              : `${b.label} sem dado disponível`,
          )
          .join(', ')}`}
      >
        {available.map((b) => (
          <div
            key={b.criterion}
            className="h-full transition-[width] duration-500"
            style={{ width: `${b.contribution}%`, background: scoreTierColor[scoreTone(b.score)] }}
            title={`${b.label}: ${Math.round(b.contribution)} pts (peso ${Math.round(b.weight * 100)}% × nota ${Math.round(b.score)})`}
          />
        ))}
      </div>

      <div
        className={cn(
          'flex flex-wrap items-center gap-x-2.5 gap-y-1',
          size === 'sm' ? 'text-[0.65rem]' : 'text-xs',
        )}
      >
        {breakdown.map((b) => (
          <span
            key={b.criterion}
            className={cn(
              'inline-flex items-center gap-1 tabular-nums',
              b.available ? 'text-muted-foreground' : 'text-muted-foreground/40',
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
            {b.available ? Math.round(b.contribution) : '—'}
          </span>
        ))}
      </div>
    </div>
  )
}
