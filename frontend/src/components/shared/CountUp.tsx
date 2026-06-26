import { useEffect, useState } from 'react'
import { animate, useReducedMotion } from 'framer-motion'
import { ease } from '@/lib/motion'

/** Animates 0 → target once. Honors prefers-reduced-motion and `enabled` (both jump to target). */
export function useCountUp(target: number, duration = 1.1, enabled = true) {
  const reduce = useReducedMotion()
  const [value, setValue] = useState(!enabled || reduce ? target : 0)

  useEffect(() => {
    if (!enabled || reduce) {
      setValue(target)
      return
    }
    const controls = animate(0, target, {
      duration,
      ease: ease.out,
      onUpdate: (v) => setValue(v),
    })
    return () => controls.stop()
  }, [target, reduce, duration, enabled])

  return Math.round(value)
}

export function CountUp({
  value,
  duration,
  suffix,
  className,
}: {
  value: number
  duration?: number
  suffix?: string
  className?: string
}) {
  const v = useCountUp(value, duration)
  return (
    <span className={className}>
      {v}
      {suffix}
    </span>
  )
}
