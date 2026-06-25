import { motion } from 'framer-motion'
import type { ScoredCriterion } from '@/api/types'
import { cn } from '@/lib/utils'

export function CriterionBreakdown({ breakdown }: { breakdown: ScoredCriterion[] }) {
  return (
    <div className="space-y-3">
      {breakdown.map((item, i) => (
        <div key={item.criterion} className={cn('space-y-1.5', !item.available && 'opacity-50')}>
          <div className="flex items-center justify-between text-sm">
            <span className="flex items-center gap-2 font-medium">
              <span aria-hidden>{item.icon}</span>
              {item.label}
              <span className="text-xs text-muted-foreground">peso {Math.round(item.weight * 100)}%</span>
            </span>
            <span className="tabular-nums text-muted-foreground">{Math.round(item.score)}/100</span>
          </div>
          <div className="h-2 overflow-hidden rounded-full bg-muted">
            <motion.div
              initial={{ width: 0 }}
              animate={{ width: `${Math.max(0, Math.min(100, item.score))}%` }}
              transition={{ duration: 0.7, delay: i * 0.08, ease: [0.22, 1, 0.36, 1] }}
              className="h-full rounded-full bg-primary"
            />
          </div>
          <p className="text-xs text-muted-foreground">{item.justification}</p>
        </div>
      ))}
    </div>
  )
}
