import { motion } from 'framer-motion'
import { Sparkles, PiggyBank, Sun, Mountain, Landmark, SlidersHorizontal } from 'lucide-react'
import type { CriterionKey, CriterionOption, ProfileOption, ProfileKey } from '@/api/types'
import { cn } from '@/lib/utils'

const profileIcons: Record<ProfileKey, typeof Sparkles> = {
  equilibrado: Sparkles,
  economico: PiggyBank,
  'clima-perfeito': Sun,
  aventura: Mountain,
  cultural: Landmark,
}

function profileHint(
  weights: Record<CriterionKey, number>,
  criteria?: CriterionOption[],
): string | undefined {
  if (!criteria?.length) return undefined
  const entries = Object.entries(weights) as [CriterionKey, number][]
  if (entries.length === 0) return undefined
  const values = entries.map(([, v]) => v)
  const max = Math.max(...values)
  const min = Math.min(...values)
  if (max - min < 0.02) return 'Equilíbrio entre tudo'
  const topKey = entries.find(([, v]) => v === max)?.[0]
  const label = criteria.find((c) => c.key === topKey)?.label
  return label ? `Mais peso em ${label.toLowerCase()}` : undefined
}

function Tile({
  active,
  icon: Icon,
  label,
  hint,
  onClick,
}: {
  active: boolean
  icon: typeof Sparkles
  label: string
  hint?: string
  onClick: () => void
}) {
  return (
    <motion.button
      type="button"
      onClick={onClick}
      whileTap={{ scale: 0.97 }}
      className={cn(
        'group flex flex-col items-start gap-3 rounded-xl border p-4 text-left transition-[background-color,border-color]',
        active
          ? 'border-gold/40 bg-surface-3'
          : 'border-hairline bg-surface/50 hover:border-foreground/15 hover:bg-surface-2',
      )}
    >
      <span
        className={cn(
          'flex size-9 items-center justify-center rounded-lg transition-colors',
          active
            ? 'bg-gold/15 text-gold'
            : 'bg-surface-2 text-muted-foreground group-hover:text-foreground',
        )}
      >
        <Icon className="size-4.5" strokeWidth={2} />
      </span>
      <span className="space-y-0.5">
        <span className="block text-sm font-medium text-foreground">{label}</span>
        {hint && <span className="block text-xs text-muted-foreground">{hint}</span>}
      </span>
    </motion.button>
  )
}

export function ProfilePicker({
  profiles,
  criteria,
  value,
  custom,
  onSelect,
  onCustom,
}: {
  profiles: ProfileOption[]
  criteria?: CriterionOption[]
  value: ProfileKey | null
  custom: boolean
  onSelect: (profile: ProfileKey) => void
  onCustom: () => void
}) {
  return (
    <div className="grid grid-cols-2 gap-2.5 sm:grid-cols-3">
      {profiles.map((profile) => (
        <Tile
          key={profile.key}
          active={!custom && value === profile.key}
          icon={profileIcons[profile.key] ?? Sparkles}
          label={profile.label}
          hint={profileHint(profile.weights, criteria)}
          onClick={() => onSelect(profile.key)}
        />
      ))}
      <Tile
        active={custom}
        icon={SlidersHorizontal}
        label="Personalizado"
        hint="Ajuste os pesos"
        onClick={onCustom}
      />
    </div>
  )
}
