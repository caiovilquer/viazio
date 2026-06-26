import type { CSSProperties } from 'react'
import { motion, useReducedMotion } from 'framer-motion'

/**
 * Living, thematic backdrop for the Landing only — drifting late-afternoon light,
 * flowing Atlas routes with a star travelling along them, and a twinkling
 * constellation. Fixed + non-interactive + GPU-friendly (transform/opacity only).
 * Honors prefers-reduced-motion by freezing into a still, composed scene.
 */

/** Atlas routes — gentle great-circle curves spanning the viewport. */
const ROUTES = [
  { id: 'route-1', d: 'M-60 250 C 360 80, 1080 90, 1500 320', opacity: 0.11, dur: '7s' },
  { id: 'route-2', d: 'M-60 540 C 420 380, 1020 430, 1500 250', opacity: 0.09, dur: '9.5s' },
  { id: 'route-3', d: 'M-60 720 C 380 650, 1060 770, 1500 600', opacity: 0.07, dur: '12s' },
]

/** Travelling "north stars" gliding along two of the routes. */
const TRAVELLERS = [
  { path: '#route-1', r: 3.2, dur: '17s' },
  { path: '#route-2', r: 2.6, dur: '24s' },
]

type Star = { x: number; y: number; r: number; dur: number; delay: number; bright?: boolean }

/** Deterministic constellation (no per-render randomness → no layout jitter). */
const STARS: Star[] = [
  { x: 8, y: 16, r: 1.2, dur: 5.5, delay: 0 },
  { x: 16, y: 70, r: 1.5, dur: 7, delay: 1.4 },
  { x: 24, y: 34, r: 1, dur: 6, delay: 0.6 },
  { x: 33, y: 82, r: 1.3, dur: 8, delay: 2.1 },
  { x: 41, y: 11, r: 1, dur: 6.5, delay: 1 },
  { x: 52, y: 89, r: 1.4, dur: 7.5, delay: 0.3 },
  { x: 61, y: 22, r: 1, dur: 5, delay: 1.8 },
  { x: 69, y: 76, r: 1.5, dur: 8.5, delay: 0.9 },
  { x: 77, y: 38, r: 1.1, dur: 6, delay: 2.4 },
  { x: 86, y: 17, r: 1.3, dur: 7, delay: 0.5 },
  { x: 92, y: 64, r: 1, dur: 6.5, delay: 1.6 },
  { x: 5, y: 47, r: 1, dur: 7.2, delay: 2 },
  { x: 30, y: 56, r: 1.1, dur: 7.8, delay: 1.1 },
  { x: 64, y: 52, r: 1, dur: 6.2, delay: 2.6 },
  { x: 88, y: 44, r: 1.2, dur: 7.4, delay: 0.8 },
  { x: 47, y: 30, r: 0.9, dur: 6.8, delay: 3 },
  // brighter "north stars" — rendered as sparkles (r = px size)
  { x: 19, y: 27, r: 13, dur: 6, delay: 0.4, bright: true },
  { x: 74, y: 15, r: 15, dur: 7.5, delay: 1.5, bright: true },
  { x: 57, y: 67, r: 11, dur: 8.5, delay: 2.2, bright: true },
]

function Sparkle({ style }: { style?: CSSProperties }) {
  return (
    <svg viewBox="0 0 24 24" fill="currentColor" style={style} aria-hidden>
      <path d="M12 0c.9 6.6 4.4 10.1 12 12-7.6 1.9-11.1 5.4-12 12-.9-6.6-4.4-10.1-12-12 7.6-1.9 11.1-5.4 12-12Z" />
    </svg>
  )
}

export function LandingBackdrop() {
  const reduce = Boolean(useReducedMotion())

  return (
    <div aria-hidden className="pointer-events-none fixed inset-0 -z-10 overflow-hidden">
      {/* base vertical depth */}
      <div className="absolute inset-0 bg-[radial-gradient(120%_85%_at_50%_-10%,oklch(0.225_0.03_262)_0%,var(--background)_55%)]" />

      {/* drifting late-afternoon light */}
      <motion.div
        className="absolute left-1/2 top-[-12%] h-[46rem] w-[62rem] rounded-full blur-[130px]"
        style={{ background: 'radial-gradient(closest-side, oklch(0.78 0.11 75 / 0.18), transparent 72%)' }}
        animate={reduce ? { x: '-50%' } : { x: ['-50%', '-43%', '-50%'], y: [0, 26, 0], scale: [1, 1.07, 1] }}
        transition={reduce ? undefined : { duration: 24, repeat: Infinity, ease: 'easeInOut' }}
      />
      <motion.div
        className="absolute left-[-8%] top-[40%] h-[34rem] w-[40rem] rounded-full blur-[120px]"
        style={{ background: 'radial-gradient(closest-side, oklch(0.74 0.1 70 / 0.12), transparent 70%)' }}
        animate={reduce ? undefined : { x: [0, 54, 0], y: [0, -30, 0], scale: [1, 1.1, 1] }}
        transition={reduce ? undefined : { duration: 31, repeat: Infinity, ease: 'easeInOut' }}
      />
      {/* faint coral whisper — emotional accent, rare */}
      <motion.div
        className="absolute right-[6%] top-[-6%] h-[26rem] w-[26rem] rounded-full blur-[120px]"
        style={{ background: 'radial-gradient(closest-side, oklch(0.7 0.16 38 / 0.10), transparent 70%)' }}
        animate={reduce ? undefined : { x: [0, -32, 0], y: [0, 30, 0], opacity: [0.65, 1, 0.65] }}
        transition={reduce ? undefined : { duration: 19, repeat: Infinity, ease: 'easeInOut' }}
      />

      {/* Atlas routes + travelling star */}
      <svg
        className="absolute inset-0 size-full mask-fade-edges text-gold"
        viewBox="0 0 1440 900"
        preserveAspectRatio="xMidYMid slice"
        fill="none"
        stroke="currentColor"
      >
        {ROUTES.map((r) => (
          <path
            key={r.id}
            id={r.id}
            d={r.d}
            strokeWidth={1}
            className="atlas-flow"
            style={{ strokeOpacity: r.opacity, animationDuration: r.dur }}
          />
        ))}
        {!reduce &&
          TRAVELLERS.map((t) => (
            <circle
              key={t.path}
              r={t.r}
              fill="currentColor"
              style={{ filter: 'drop-shadow(0 0 6px oklch(0.82 0.1 85 / 0.9))' }}
            >
              <animateMotion dur={t.dur} repeatCount="indefinite" calcMode="linear" keyPoints="0;1" keyTimes="0;1">
                <mpath href={t.path} />
              </animateMotion>
            </circle>
          ))}
      </svg>

      {/* twinkling constellation */}
      {STARS.map((s, i) => (
        <motion.div
          key={i}
          className="absolute text-gold"
          style={{ left: `${s.x}%`, top: `${s.y}%`, x: '-50%', y: '-50%' }}
          animate={
            reduce
              ? { opacity: s.bright ? 0.55 : 0.4 }
              : { opacity: s.bright ? [0.25, 0.85, 0.25] : [0.12, 0.7, 0.12], scale: [0.82, 1, 0.82] }
          }
          transition={reduce ? undefined : { duration: s.dur, repeat: Infinity, ease: 'easeInOut', delay: s.delay }}
        >
          {s.bright ? (
            <Sparkle style={{ width: s.r, height: s.r }} />
          ) : (
            <span className="block rounded-full bg-gold" style={{ width: s.r * 2, height: s.r * 2 }} />
          )}
        </motion.div>
      ))}

      {/* discreet dot-grid + grain, faded toward the edges */}
      <div className="absolute inset-0 atlas-grid mask-fade-edges opacity-60" />
      <div className="absolute inset-0 bg-grain opacity-[0.04] mix-blend-soft-light" />
    </div>
  )
}
