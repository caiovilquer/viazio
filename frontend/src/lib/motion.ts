import type { Transition, Variants } from "framer-motion";

/**
 * Tokens de movimento Viazio — propositais e contidos.
 * Anima só transform/opacity. Durações curtas; nada decorativo em loop.
 */

export const ease = {
  /** desaceleração suave — easing padrão da marca */
  out: [0.22, 1, 0.36, 1] as const,
  inOut: [0.65, 0, 0.35, 1] as const,
};

export const duration = {
  fast: 0.18,
  base: 0.34,
  slow: 0.6,
};

export const spring = {
  /** suave, para mudanças de layout/posição */
  soft: {
    type: "spring",
    stiffness: 240,
    damping: 30,
    mass: 0.9,
  } satisfies Transition,
  /** firme, para indicadores e toggles pequenos */
  snappy: { type: "spring", stiffness: 460, damping: 36 } satisfies Transition,
} as const;

/** Esmaecimento + subida. Usar com <Reveal/> ou whileInView. */
export const reveal: Variants = {
  hidden: { opacity: 0, y: 16 },
  show: {
    opacity: 1,
    y: 0,
    transition: { duration: duration.base, ease: ease.out },
  },
};

/** Container que escalona os filhos diretos usando `reveal`. */
export function staggerContainer(gap = 0.07, delay = 0): Variants {
  return {
    hidden: {},
    show: { transition: { staggerChildren: gap, delayChildren: delay } },
  };
}

/** Entrada sutil para elementos de destaque (deslocamento um pouco maior). */
export const heroItem: Variants = {
  hidden: { opacity: 0, y: 22 },
  show: { opacity: 1, y: 0, transition: { duration: 0.55, ease: ease.out } },
};
