import { AnimatePresence, motion } from 'framer-motion'
import { Slider } from '@/components/ui/slider'
import type { CriterionKey, CriterionOption } from '@/api/types'

export function WeightSliders({
  criteria,
  weights,
  onChange,
}: {
  criteria: CriterionOption[]
  weights: Record<CriterionKey, number>
  onChange: (criterion: CriterionKey, value: number) => void
}) {
  const total = Object.values(weights).reduce((sum, v) => sum + v, 0) || 1

  return (
    <AnimatePresence>
      <motion.div
        initial={{ opacity: 0, height: 0 }}
        animate={{ opacity: 1, height: 'auto' }}
        exit={{ opacity: 0, height: 0 }}
        className="space-y-5 overflow-hidden"
      >
        {criteria.map((criterion) => {
          const pct = Math.round((weights[criterion.key] / total) * 100)
          return (
            <div key={criterion.key} className="space-y-2">
              <div className="flex items-center justify-between text-sm">
                <span className="flex items-center gap-2 font-medium">
                  <span aria-hidden>{criterion.icon}</span>
                  {criterion.label}
                </span>
                <span className="tabular-nums text-muted-foreground">{pct}%</span>
              </div>
              <Slider
                value={[weights[criterion.key]]}
                min={0}
                max={1}
                step={0.05}
                onValueChange={([v]) => onChange(criterion.key, v)}
              />
            </div>
          )
        })}
      </motion.div>
    </AnimatePresence>
  )
}
