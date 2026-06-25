import type { MouseEvent } from 'react'
import { motion } from 'framer-motion'
import { Heart } from 'lucide-react'
import { toast } from 'sonner'
import type { TravelRecommendation } from '@/api/types'
import { toggleFavorite, useIsFavorite } from '@/lib/favorites'
import { cn } from '@/lib/utils'

export function FavoriteButton({
  recommendation,
  className,
  size = 'md',
}: {
  recommendation: TravelRecommendation
  className?: string
  size?: 'sm' | 'md'
}) {
  const saved = useIsFavorite(recommendation.countryCode)

  function handleClick(e: MouseEvent) {
    e.preventDefault()
    e.stopPropagation()
    const willSave = !saved
    toggleFavorite(recommendation)
    toast.success(willSave ? `${recommendation.countryName} salvo` : `${recommendation.countryName} removido dos salvos`)
  }

  return (
    <motion.button
      type="button"
      whileTap={{ scale: 0.85 }}
      onClick={handleClick}
      aria-pressed={saved}
      aria-label={saved ? `Remover ${recommendation.countryName} dos salvos` : `Salvar ${recommendation.countryName}`}
      className={cn(
        'flex items-center justify-center rounded-full border-2 shadow backdrop-blur transition-colors',
        size === 'sm' ? 'size-7' : 'size-9',
        saved
          ? 'border-primary bg-primary text-primary-foreground'
          : 'border-white/80 bg-background/70 text-foreground hover:border-primary/60',
        className,
      )}
    >
      <Heart className={size === 'sm' ? 'size-3.5' : 'size-4'} fill={saved ? 'currentColor' : 'none'} />
    </motion.button>
  )
}
