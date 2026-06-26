import { motion, useReducedMotion } from 'framer-motion'
import type { ScoredCriterion } from '@/api/types'
import { ease } from '@/lib/motion'
import { cn } from '@/lib/utils'

/** Warm tier color — keeps coral rare (mid scores only), gold for the best. */
function tierColor(score: number) {
  if (score >= 80) return 'var(--gold)'
  if (score >= 60) return 'var(--primary)'
  if (score >= 40) return 'var(--chart-3)'
  return 'var(--chart-5)'
}

export function CriterionBreakdown({ breakdown }: { breakdown: ScoredCriterion[] }) {
  const reduce = useReducedMotion()

  return (
    <div className="space-y-4">
      {breakdown.map((item, i) => {
        const score = Math.max(0, Math.min(100, item.score))
        const color = tierColor(item.score)
        return (
          <div key={item.criterion} className={cn(!item.available && 'opacity-45')}>
            <div className="mb-1.5 flex items-baseline justify-between gap-3">
              <span className="flex items-center gap-2 text-sm font-medium">
                <span aria-hidden className="text-base leading-none">
                  {item.icon}
                </span>
                {item.label}
                <span className="rounded-full border border-hairline px-1.5 py-px text-[0.65rem] font-normal tracking-wide text-muted-foreground">
                  peso {Math.round(item.weight * 100)}%
                </span>
              </span>
              <span className="font-display text-sm tabular-nums" style={{ color }}>
                {item.available ? Math.round(item.score) : '—'}
              </span>
            </div>

            <div className="h-1.5 overflow-hidden rounded-full bg-surface-3/70">
              {reduce ? (
                <div
                  className="h-full rounded-full"
                  style={{ background: color, width: `${item.available ? score : 0}%` }}
                />
              ) : (
                <motion.div
                  className="h-full rounded-full"
                  style={{ background: color }}
                  initial={{ width: 0 }}
                  whileInView={{ width: `${item.available ? score : 0}%` }}
                  viewport={{ once: true, margin: '-40px' }}
                  transition={{ duration: 0.8, delay: i * 0.07, ease: ease.out }}
                />
              )}
            </div>

            {item.justification && (
              <p className="mt-1.5 text-xs leading-relaxed text-muted-foreground">{item.justification}</p>
            )}
          </div>
        )
      })}
    </div>
  )
}
