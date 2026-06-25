import { motion } from 'framer-motion'
import { Link } from 'react-router-dom'
import { CalendarRange, Briefcase } from 'lucide-react'
import type { WindowSuggestion } from '@/api/types'
import { ScoreRing } from '@/components/shared/ScoreRing'
import { formatDateRange } from '@/lib/format'

export function WindowCard({ window, index, searchQuery }: { window: WindowSuggestion; index: number; searchQuery: string }) {
  return (
    <motion.div
      initial={{ opacity: 0, y: 24 }}
      animate={{ opacity: 1, y: 0 }}
      transition={{ duration: 0.4, delay: Math.min(index * 0.08, 0.4), ease: [0.22, 1, 0.36, 1] }}
      className="rounded-3xl border border-border bg-card p-5 shadow-sm"
    >
      <div className="flex items-start justify-between gap-4">
        <div>
          <p className="font-display text-lg font-semibold">{window.label}</p>
          <p className="mt-1 flex items-center gap-1.5 text-sm text-muted-foreground">
            <CalendarRange className="size-4" />
            {formatDateRange(window.start, window.end)} · {window.totalDays} dias
          </p>
          <p className="mt-1 flex items-center gap-1.5 text-sm text-muted-foreground">
            <Briefcase className="size-4" />
            {window.requiredLeaveDays} dia(s) de férias · {window.bridgeDaysUsed} de ponte
          </p>
        </div>
        <ScoreRing score={window.timingScore} size={56} strokeWidth={5} label="timing" />
      </div>

      {window.topDestinations.length > 0 && (
        <div className="mt-4 flex gap-3 overflow-x-auto pb-1 no-scrollbar">
          {window.topDestinations.map((dest) => (
            <Link
              key={dest.countryCode}
              to={`/destino/${dest.countryCode}?${searchQuery}`}
              state={{ recommendation: dest }}
              className="flex min-w-[160px] items-center gap-2 rounded-2xl border border-border bg-background px-3 py-2 transition-colors hover:border-primary/40"
            >
              <span className="text-xl">{dest.profile.flagEmoji ?? '🌍'}</span>
              <div className="min-w-0">
                <p className="truncate text-sm font-medium">{dest.countryName}</p>
                <p className="text-xs text-muted-foreground">{Math.round(dest.tripScore)} pts</p>
              </div>
            </Link>
          ))}
        </div>
      )}
    </motion.div>
  )
}
