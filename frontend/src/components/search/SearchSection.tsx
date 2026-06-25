import { motion } from 'framer-motion'
import type { ReactNode } from 'react'

export function SearchSection({
  step,
  title,
  description,
  children,
}: {
  step: number
  title: string
  description?: string
  children: ReactNode
}) {
  return (
    <motion.section
      initial={{ opacity: 0, y: 16 }}
      animate={{ opacity: 1, y: 0 }}
      transition={{ duration: 0.4, delay: step * 0.05, ease: [0.22, 1, 0.36, 1] }}
      className="space-y-4"
    >
      <div className="flex items-baseline gap-3">
        <span className="font-display text-2xl text-primary/70">{String(step).padStart(2, '0')}</span>
        <div>
          <h2 className="font-display text-lg font-semibold">{title}</h2>
          {description && <p className="text-sm text-muted-foreground">{description}</p>}
        </div>
      </div>
      {children}
    </motion.section>
  )
}
