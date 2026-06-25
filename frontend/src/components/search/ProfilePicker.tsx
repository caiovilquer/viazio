import { motion } from 'framer-motion'
import { Sparkles, PiggyBank, Sun, Mountain, Landmark, SlidersHorizontal } from 'lucide-react'
import type { ProfileOption, ProfileKey } from '@/api/types'
import { cn } from '@/lib/utils'

const profileIcons: Record<ProfileKey, typeof Sparkles> = {
  equilibrado: Sparkles,
  economico: PiggyBank,
  'clima-perfeito': Sun,
  aventura: Mountain,
  cultural: Landmark,
}

export function ProfilePicker({
  profiles,
  value,
  custom,
  onSelect,
  onCustom,
}: {
  profiles: ProfileOption[]
  value: ProfileKey | null
  custom: boolean
  onSelect: (profile: ProfileKey) => void
  onCustom: () => void
}) {
  return (
    <div className="grid grid-cols-2 gap-3 sm:grid-cols-3">
      {profiles.map((profile) => {
        const Icon = profileIcons[profile.key] ?? Sparkles
        const isActive = !custom && value === profile.key
        return (
          <motion.button
            key={profile.key}
            type="button"
            onClick={() => onSelect(profile.key)}
            whileTap={{ scale: 0.96 }}
            className={cn(
              'flex flex-col items-start gap-2 rounded-2xl border p-4 text-left transition-colors',
              isActive
                ? 'border-primary bg-primary/8 ring-2 ring-primary/30'
                : 'border-border bg-card hover:border-primary/40',
            )}
          >
            <span
              className={cn(
                'flex size-9 items-center justify-center rounded-full',
                isActive ? 'bg-primary text-primary-foreground' : 'bg-secondary text-foreground',
              )}
            >
              <Icon className="size-4.5" strokeWidth={2.2} />
            </span>
            <span className="text-sm font-medium">{profile.label}</span>
          </motion.button>
        )
      })}
      <motion.button
        type="button"
        onClick={onCustom}
        whileTap={{ scale: 0.96 }}
        className={cn(
          'flex flex-col items-start gap-2 rounded-2xl border p-4 text-left transition-colors',
          custom
            ? 'border-primary bg-primary/8 ring-2 ring-primary/30'
            : 'border-border bg-card hover:border-primary/40',
        )}
      >
        <span
          className={cn(
            'flex size-9 items-center justify-center rounded-full',
            custom ? 'bg-primary text-primary-foreground' : 'bg-secondary text-foreground',
          )}
        >
          <SlidersHorizontal className="size-4.5" strokeWidth={2.2} />
        </span>
        <span className="text-sm font-medium">Personalizado</span>
      </motion.button>
    </div>
  )
}
