import { useEffect, useState } from "react";
import { animate, useReducedMotion } from "framer-motion";
import { ease } from "@/lib/motion";

/** Anima 0 → alvo uma vez. Respeita prefers-reduced-motion e `enabled` (ambos pulam direto ao alvo). */
export function useCountUp(target: number, duration = 1.1, enabled = true) {
  const reduce = useReducedMotion();
  const [value, setValue] = useState(!enabled || reduce ? target : 0);

  useEffect(() => {
    if (!enabled || reduce) {
      setValue(target);
      return;
    }
    const controls = animate(0, target, {
      duration,
      ease: ease.out,
      onUpdate: (v) => setValue(v),
    });
    return () => controls.stop();
  }, [target, reduce, duration, enabled]);

  return Math.round(value);
}
