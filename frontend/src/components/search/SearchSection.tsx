import { motion } from "framer-motion";
import type { ReactNode } from "react";
import { ease } from "@/lib/motion";

export function SearchSection({
  step,
  title,
  description,
  children,
}: {
  step: number;
  title: string;
  description?: string;
  children: ReactNode;
}) {
  return (
    <motion.section
      initial={{ opacity: 0, y: 14 }}
      animate={{ opacity: 1, y: 0 }}
      transition={{ duration: 0.4, delay: step * 0.05, ease: ease.out }}
    >
      <header className="flex items-baseline gap-4 border-b border-hairline pb-3">
        <span className="font-display text-[1.7rem] leading-none tabular-nums text-gold-gradient">
          {String(step).padStart(2, "0")}
        </span>
        <div className="space-y-0.5">
          <h2 className="font-display text-lg tracking-tight">{title}</h2>
          {description && (
            <p className="text-sm text-muted-foreground">{description}</p>
          )}
        </div>
      </header>
      <div className="mt-5">{children}</div>
    </motion.section>
  );
}
