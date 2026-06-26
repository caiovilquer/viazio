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

function chipClass(active: boolean) {
  return cn(
    'flex shrink-0 items-center gap-1.5 rounded-full border px-3.5 py-1.5 text-xs font-medium transition-[color,background-color,border-color]',
    active
      ? 'border-gold/40 bg-gold/10 text-foreground'
      : 'border-hairline bg-surface-2/50 text-muted-foreground hover:border-foreground/15 hover:text-foreground',
  )
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
    <div className="mb-6 rounded-2xl border border-hairline bg-surface/50 p-4">
      <div className="flex items-center justify-between gap-2">
        <p className="flex items-center gap-2 text-[0.7rem] font-semibold uppercase tracking-[0.12em] text-muted-foreground">
          <SlidersHorizontal className="size-3.5" />
          Refinar por prioridade
          {dirty && (
            <motion.span
              initial={{ opacity: 0, scale: 0.85 }}
              animate={{ opacity: 1, scale: 1 }}
              className="rounded-full bg-gold/15 px-2 py-0.5 text-[10px] font-semibold tracking-[0.08em] text-gold"
            >
              prévia ao vivo
            </motion.span>
          )}
        </p>
        {dirty && (
          <button
            type="button"
            onClick={onReset}
            className="flex items-center gap-1 text-xs font-medium text-muted-foreground transition-colors hover:text-foreground"
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
              whileTap={{ scale: 0.96 }}
              onClick={() => onSelectProfile(profile.key)}
              className={chipClass(isActive)}
            >
              <Icon className={cn('size-3.5', isActive && 'text-gold')} strokeWidth={2.2} />
              {profile.label}
            </motion.button>
          )
        })}

        <Sheet open={sheetOpen} onOpenChange={setSheetOpen}>
          <motion.button
            type="button"
            whileTap={{ scale: 0.96 }}
            onClick={() => setSheetOpen(true)}
            className={chipClass(custom)}
          >
            <SlidersHorizontal className={cn('size-3.5', custom && 'text-gold')} strokeWidth={2.2} />
            Personalizar
          </motion.button>
          <SheetContent className="flex flex-col gap-0 overflow-y-auto">
            <SheetHeader>
              <SheetTitle className="font-display text-xl tracking-tight">
                Personalizar prioridades
              </SheetTitle>
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
    </div>
  )
}
