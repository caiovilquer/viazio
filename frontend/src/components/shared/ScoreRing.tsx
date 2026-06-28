import { motion } from 'framer-motion'
import { useCountUp } from './CountUp'
import { ease } from '@/lib/motion'
import { scoreTone } from '@/lib/format'
import { cn } from '@/lib/utils'

// Warm tiers — never grey. Shared with other score-driven dataviz (e.g. ClimateChart).
export const scoreTierColor: Record<string, string> = {
  excellent: 'var(--gold)',
  good: 'var(--primary)',
  fair: 'var(--chart-3)',
  poor: 'var(--chart-5)',
}

const tierColor = scoreTierColor
const tierOf = scoreTone

export function ScoreRing({
  score,
  size = 64,
  strokeWidth = 6,
  label,
  animate = false,
  className,
}: {
  score: number
  size?: number
  strokeWidth?: number
  label?: string
  /** Draw the arc + count the number on mount. Use for a single prominent ring. */
  animate?: boolean
  className?: string
}) {
  const radius = (size - strokeWidth) / 2
  const circumference = 2 * Math.PI * radius
  const clamped = Math.max(0, Math.min(100, score))
  const tier = tierOf(clamped)
  const targetOffset = circumference - (clamped / 100) * circumference
  const display = useCountUp(Math.round(clamped), 1.1, animate)

  return (
    <div
      className={cn('relative flex shrink-0 items-center justify-center', className)}
      style={{ width: size, height: size }}
    >
      <svg width={size} height={size} className="-rotate-90">
        <circle
          cx={size / 2}
          cy={size / 2}
          r={radius}
          fill="none"
          stroke="var(--surface-3)"
          strokeWidth={strokeWidth}
        />
        {animate ? (
          <motion.circle
            cx={size / 2}
            cy={size / 2}
            r={radius}
            fill="none"
            stroke={tierColor[tier]}
            strokeWidth={strokeWidth}
            strokeLinecap="round"
            strokeDasharray={circumference}
            initial={{ strokeDashoffset: circumference }}
            animate={{ strokeDashoffset: targetOffset }}
            transition={{ duration: 1.1, ease: ease.out, delay: 0.1 }}
          />
        ) : (
          <circle
            cx={size / 2}
            cy={size / 2}
            r={radius}
            fill="none"
            stroke={tierColor[tier]}
            strokeWidth={strokeWidth}
            strokeLinecap="round"
            strokeDasharray={circumference}
            strokeDashoffset={targetOffset}
          />
        )}
      </svg>
      <div className="absolute flex flex-col items-center leading-none">
        <span
          className="font-display font-semibold tabular-nums text-foreground"
          style={{ fontSize: size * 0.3 }}
        >
          {display}
        </span>
        {label && (
          <span
            className="mt-0.5 uppercase tracking-[0.12em] text-muted-foreground"
            style={{ fontSize: Math.max(8, size * 0.12) }}
          >
            {label}
          </span>
        )}
      </div>
    </div>
  )
}
