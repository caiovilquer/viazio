import { useId } from 'react'
import { cn } from '@/lib/utils'

/**
 * Viazio mark — a faceted, gem-cut "V" (two gold facets meeting at a crisp
 * spine) crowned by a discreet star. Abstract by design: no plane, pin, globe
 * or map. Hand-authored SVG, scales crisply.
 */
export function BrandMark({ className }: { className?: string }) {
  const raw = useId().replace(/:/g, '')
  const light = `vl-${raw}`
  const deep = `vd-${raw}`

  return (
    <svg
      viewBox="0 0 48 48"
      fill="none"
      className={cn('size-7', className)}
      role="img"
      aria-label="Viazio"
    >
      <defs>
        <linearGradient id={light} x1="14" y1="11" x2="22" y2="38" gradientUnits="userSpaceOnUse">
          <stop stopColor="#F2DA9E" />
          <stop offset="1" stopColor="#CFA862" />
        </linearGradient>
        <linearGradient id={deep} x1="34" y1="11" x2="26" y2="38" gradientUnits="userSpaceOnUse">
          <stop stopColor="#C6A05A" />
          <stop offset="1" stopColor="#8E6A33" />
        </linearGradient>
      </defs>
      <path
        d="M10 13 C 13 21 17 29 22.6 35.6 L 24 37 L 24 29.6 C 21.4 24.4 18.8 18.8 16.5 13 Z"
        fill={`url(#${light})`}
      />
      <path
        d="M38 13 C 35 21 31 29 25.4 35.6 L 24 37 L 24 29.6 C 26.6 24.4 29.2 18.8 31.5 13 Z"
        fill={`url(#${deep})`}
      />
      <path
        d="M38.4 4.4 C 38.8 7.4 39.6 8.2 42.6 8.6 C 39.6 9 38.8 9.8 38.4 12.8 C 38 9.8 37.2 9 34.2 8.6 C 37.2 8.2 38 7.4 38.4 4.4 Z"
        fill="#F1DCA8"
      />
    </svg>
  )
}

export function Brand({
  className,
  markClassName,
  showWordmark = true,
}: {
  className?: string
  markClassName?: string
  showWordmark?: boolean
}) {
  return (
    <span className={cn('inline-flex items-center gap-2.5', className)}>
      <BrandMark className={markClassName} />
      {showWordmark && (
        <span className="font-display text-[1.35rem] leading-none tracking-[-0.02em] text-foreground">
          Viazio
        </span>
      )}
    </span>
  )
}
