import type { HTMLMotionProps } from "framer-motion";
import { ease } from "@/lib/motion";

/** Props do `motion.div` filho direto de `AnimatePresence` (não usar wrapper FC). */
export const pageTransitionMotionProps = {
  initial: { opacity: 0, y: 10 },
  animate: { opacity: 1, y: 0 },
  exit: { opacity: 0, y: -6 },
  transition: { duration: 0.3, ease: ease.out },
} satisfies HTMLMotionProps<"div">;
