import { AnimatePresence, motion } from 'framer-motion'
import { Columns3, X } from 'lucide-react'
import type { TravelRecommendation } from '@/api/types'
import { Button } from '@/components/ui/button'

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
          transition={{ type: 'spring', stiffness: 400, damping: 32 }}
          className="fixed inset-x-4 bottom-24 z-40 mx-auto max-w-md md:bottom-6"
        >
          <div className="flex items-center gap-3 rounded-2xl border border-border bg-card/95 p-3 shadow-xl backdrop-blur-xl">
            <div className="flex flex-1 items-center gap-2 overflow-x-auto no-scrollbar">
              <AnimatePresence initial={false}>
                {recommendations.map((rec) => (
                  <motion.div
                    key={rec.countryCode}
                    layout
                    initial={{ opacity: 0, scale: 0.8 }}
                    animate={{ opacity: 1, scale: 1 }}
                    exit={{ opacity: 0, scale: 0.8 }}
                    className="flex shrink-0 items-center gap-1 rounded-full bg-secondary pl-2.5 pr-1 py-1 text-xs font-medium"
                  >
                    <span>{rec.profile.flagEmoji ?? '🌍'}</span>
                    <span className="max-w-20 truncate">{rec.countryName}</span>
                    <button
                      type="button"
                      onClick={() => onRemove(rec.countryCode)}
                      className="ml-0.5 flex size-5 items-center justify-center rounded-full hover:bg-foreground/10"
                      aria-label={`Remover ${rec.countryName}`}
                    >
                      <X className="size-3" />
                    </button>
                  </motion.div>
                ))}
              </AnimatePresence>
              {recommendations.length < maxCompare && (
                <span className="shrink-0 px-1 text-xs text-muted-foreground">
                  {recommendations.length < 2 ? 'escolha +1 para comparar' : `+${maxCompare - recommendations.length}`}
                </span>
              )}
            </div>
            <Button
              size="sm"
              className="shrink-0 gap-1.5 rounded-full"
              disabled={recommendations.length < 2}
              onClick={onCompare}
            >
              <Columns3 className="size-3.5" />
              Comparar
            </Button>
          </div>
        </motion.div>
      )}
    </AnimatePresence>
  )
}
