import { useState } from 'react'
import { motion } from 'framer-motion'
import { PiggyBank, RotateCcw, Sparkles, Sun, Mountain, Landmark, SlidersHorizontal } from 'lucide-react'
import type { CriterionKey, CriterionOption, ProfileKey, ProfileOption } from '@/api/types'
import { WeightSliders } from '@/components/search/WeightSliders'
import {
  Sheet,
  SheetContent,
  SheetDescription,
  SheetHeader,
  SheetTitle,
} from '@/components/ui/sheet'
import { cn } from '@/lib/utils'

const profileIcons: Record<ProfileKey, typeof Sparkles> = {
  equilibrado: Sparkles,
  economico: PiggyBank,
  'clima-perfeito': Sun,
  aventura: Mountain,
  cultural: Landmark,
}

export function RefineBar({
  profiles,
  criteria,
  weights,
  activeProfile,
  custom,
  dirty,
  onSelectProfile,
  onCustomWeight,
  onReset,
}: {
  profiles: ProfileOption[]
  criteria: CriterionOption[]
  weights: Record<CriterionKey, number>
  activeProfile: ProfileKey | null
  custom: boolean
  dirty: boolean
  onSelectProfile: (key: ProfileKey) => void
  onCustomWeight: (criterion: CriterionKey, value: number) => void
  onReset: () => void
}) {
  const [sheetOpen, setSheetOpen] = useState(false)

  return (
    <motion.div
      initial={{ opacity: 0, y: 10 }}
      animate={{ opacity: 1, y: 0 }}
      className="mb-6 rounded-2xl border border-border bg-card/60 p-3 sm:p-4"
    >
      <div className="flex items-center justify-between gap-2">
        <p className="flex items-center gap-1.5 text-sm font-medium text-muted-foreground">
          <SlidersHorizontal className="size-3.5" />
          Refinar por prioridade
          {dirty && (
            <motion.span
              initial={{ opacity: 0, scale: 0.8 }}
              animate={{ opacity: 1, scale: 1 }}
              className="rounded-full bg-primary/10 px-2 py-0.5 text-[10px] font-semibold uppercase tracking-wide text-primary"
            >
              prévia ao vivo
            </motion.span>
          )}
        </p>
        {dirty && (
          <button
            type="button"
            onClick={onReset}
            className="flex items-center gap-1 text-xs font-medium text-muted-foreground transition-colors hover:text-primary"
          >
            <RotateCcw className="size-3" />
            Restaurar
          </button>
        )}
      </div>

      <div className="mt-3 flex gap-2 overflow-x-auto pb-1 no-scrollbar">
        {profiles.map((profile) => {
          const Icon = profileIcons[profile.key] ?? Sparkles
          const isActive = !custom && activeProfile === profile.key
          return (
            <motion.button
              key={profile.key}
              type="button"
              whileTap={{ scale: 0.95 }}
              onClick={() => onSelectProfile(profile.key)}
              className={cn(
                'flex shrink-0 items-center gap-1.5 rounded-full border px-3 py-1.5 text-xs font-medium transition-colors',
                isActive
                  ? 'border-primary bg-primary/10 text-primary'
                  : 'border-border bg-background text-muted-foreground hover:border-primary/40 hover:text-foreground',
              )}
            >
              <Icon className="size-3.5" strokeWidth={2.4} />
              {profile.label}
            </motion.button>
          )
        })}

        <Sheet open={sheetOpen} onOpenChange={setSheetOpen}>
          <motion.button
            type="button"
            whileTap={{ scale: 0.95 }}
            onClick={() => setSheetOpen(true)}
            className={cn(
              'flex shrink-0 items-center gap-1.5 rounded-full border px-3 py-1.5 text-xs font-medium transition-colors',
              custom
                ? 'border-primary bg-primary/10 text-primary'
                : 'border-border bg-background text-muted-foreground hover:border-primary/40 hover:text-foreground',
            )}
          >
            <SlidersHorizontal className="size-3.5" strokeWidth={2.4} />
            Personalizar
          </motion.button>
          <SheetContent className="flex flex-col gap-0 overflow-y-auto">
            <SheetHeader>
              <SheetTitle>Personalizar prioridades</SheetTitle>
              <SheetDescription>
                Ajuste os pesos e veja o ranking se reorganizar instantaneamente.
              </SheetDescription>
            </SheetHeader>
            <div className="px-4 pb-6">
              <WeightSliders criteria={criteria} weights={weights} onChange={onCustomWeight} />
            </div>
          </SheetContent>
        </Sheet>
      </div>
    </motion.div>
  )
}
