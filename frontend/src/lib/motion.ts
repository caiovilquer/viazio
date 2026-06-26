import type { Transition, Variants } from 'framer-motion'

/**
 * Viazio motion tokens — purposeful, restrained.
 * Animate transform/opacity only. All durations stay short; nothing decorative loops.
 */

export const ease = {
  /** smooth deceleration — the house easing */
  out: [0.22, 1, 0.36, 1] as const,
  inOut: [0.65, 0, 0.35, 1] as const,
}

export const duration = {
  fast: 0.18,
  base: 0.34,
  slow: 0.6,
}

export const spring = {
  /** gentle, for layout/position shifts */
  soft: { type: 'spring', stiffness: 240, damping: 30, mass: 0.9 } satisfies Transition,
  /** crisp, for indicators and small toggles */
  snappy: { type: 'spring', stiffness: 460, damping: 36 } satisfies Transition,
} as const

/** Fade + rise. Pair with <Reveal/> or whileInView. */
export const reveal: Variants = {
  hidden: { opacity: 0, y: 16 },
  show: {
    opacity: 1,
    y: 0,
    transition: { duration: duration.base, ease: ease.out },
  },
}

/** Container that staggers its direct children using `reveal`. */
export function staggerContainer(gap = 0.07, delay = 0): Variants {
  return {
    hidden: {},
    show: { transition: { staggerChildren: gap, delayChildren: delay } },
  }
}

/** Subtle entrance for hero elements (slightly larger travel). */
export const heroItem: Variants = {
  hidden: { opacity: 0, y: 22 },
  show: { opacity: 1, y: 0, transition: { duration: 0.55, ease: ease.out } },
}
