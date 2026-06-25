import { motion } from 'framer-motion'
import { cn } from '@/lib/utils'

const toneColor: Record<string, string> = {
  excellent: 'var(--chart-2)',
  good: 'var(--primary)',
  fair: 'var(--chart-3)',
  poor: 'var(--muted-foreground)',
}

function toneOf(score: number) {
  if (score >= 80) return 'excellent'
  if (score >= 60) return 'good'
  if (score >= 40) return 'fair'
  return 'poor'
}

export function ScoreRing({
  score,
  size = 64,
  strokeWidth = 6,
  label,
  className,
}: {
  score: number
  size?: number
  strokeWidth?: number
  label?: string
  className?: string
}) {
  const radius = (size - strokeWidth) / 2
  const circumference = 2 * Math.PI * radius
  const clamped = Math.max(0, Math.min(100, score))
  const tone = toneOf(clamped)

  return (
    <div className={cn('relative flex items-center justify-center', className)} style={{ width: size, height: size }}>
      <svg width={size} height={size} className="-rotate-90">
        <circle
          cx={size / 2}
          cy={size / 2}
          r={radius}
          fill="none"
          stroke="var(--border)"
          strokeWidth={strokeWidth}
        />
        <motion.circle
          cx={size / 2}
          cy={size / 2}
          r={radius}
          fill="none"
          stroke={toneColor[tone]}
          strokeWidth={strokeWidth}
          strokeLinecap="round"
          strokeDasharray={circumference}
          initial={{ strokeDashoffset: circumference }}
          animate={{ strokeDashoffset: circumference - (clamped / 100) * circumference }}
          transition={{ duration: 1, ease: [0.22, 1, 0.36, 1], delay: 0.1 }}
        />
      </svg>
      <div className="absolute flex flex-col items-center">
        <span className="font-display text-lg font-semibold leading-none tabular-nums">{Math.round(clamped)}</span>
        {label && <span className="text-[10px] uppercase tracking-wide text-muted-foreground">{label}</span>}
      </div>
    </div>
  )
}
