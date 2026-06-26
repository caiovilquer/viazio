import { AnimatePresence, motion } from 'framer-motion'
import { Columns3, X } from 'lucide-react'
import type { TravelRecommendation } from '@/api/types'
import { Button } from '@/components/ui/button'
import { Flag } from '@/components/shared/Flag'
import { spring } from '@/lib/motion'

export function CompareBar({
  recommendations,
  visible,
  maxCompare,
  onRemove,
  onCompare,
}: {
  recommendations: TravelRecommendation[]
  visible: boolean
  maxCompare: number
  onRemove: (countryCode: string) => void
  onCompare: () => void
}) {
  return (
    <AnimatePresence>
      {visible && (
        <motion.div
          initial={{ opacity: 0, y: 40 }}
          animate={{ opacity: 1, y: 0 }}
          exit={{ opacity: 0, y: 40 }}
          transition={spring.soft}
          className="fixed inset-x-4 bottom-24 z-40 mx-auto max-w-xl md:bottom-6"
        >
          <div className="rounded-2xl border border-hairline glass p-3 elevate-lg">
            <div className="flex flex-col gap-2.5 sm:flex-row sm:items-center sm:gap-3">
              <div className="flex flex-wrap items-center gap-2 sm:flex-1">
                <AnimatePresence initial={false}>
                  {recommendations.map((rec) => (
                    <motion.div
                      key={rec.countryCode}
                      layout
                      initial={{ opacity: 0, scale: 0.8 }}
                      animate={{ opacity: 1, scale: 1 }}
                      exit={{ opacity: 0, scale: 0.8 }}
                      className="flex shrink-0 items-center gap-1.5 rounded-full border border-hairline bg-surface-2/70 py-1 pl-2 pr-1 text-xs font-medium"
                    >
                      <Flag code={rec.countryCode} className="h-3 w-4 shrink-0" />
                      <span className="max-w-[9rem] truncate">{rec.countryName}</span>
                      <button
                        type="button"
                        onClick={() => onRemove(rec.countryCode)}
                        className="ml-0.5 flex size-5 shrink-0 items-center justify-center rounded-full text-muted-foreground transition-colors hover:bg-foreground/10 hover:text-foreground"
                        aria-label={`Remover ${rec.countryName}`}
                      >
                        <X className="size-3" />
                      </button>
                    </motion.div>
                  ))}
                </AnimatePresence>
                {recommendations.length < maxCompare && (
                  <span className="px-1 text-xs text-muted-foreground">
                    {recommendations.length < 2
                      ? 'escolha +1 para comparar'
                      : `+${maxCompare - recommendations.length} possível`}
                  </span>
                )}
              </div>
              <Button
                size="sm"
                className="w-full shrink-0 gap-1.5 rounded-full glow-coral sm:w-auto"
                disabled={recommendations.length < 2}
                onClick={onCompare}
              >
                <Columns3 className="size-3.5" />
                Comparar
              </Button>
            </div>
          </div>
        </motion.div>
      )}
    </AnimatePresence>
  )
}
