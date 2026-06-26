import { Link } from 'react-router-dom'
import { motion } from 'framer-motion'
import { exampleSearches, exampleSearchHref } from '@/lib/example-searches'

export function ExampleSearchChips() {
  return (
    <div className="mt-9 flex flex-col items-center gap-3">
      <p className="text-[0.7rem] font-medium uppercase tracking-[0.2em] text-muted-foreground/80">
        ou comece com um toque
      </p>
      {/* shrink-0 must sit on the DIRECT flex child (the motion.div), not the
          nested <Link>, or pills collapse on mobile (see frontend-react-stack memory). */}
      <div className="flex max-w-full gap-2 overflow-x-auto px-4 pb-1 no-scrollbar sm:flex-wrap sm:justify-center sm:overflow-visible">
        {exampleSearches.map((example, i) => (
          <motion.div
            key={example.key}
            initial={{ opacity: 0, y: 8 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ delay: 0.5 + i * 0.06 }}
            className="shrink-0"
          >
            <Link
              to={exampleSearchHref(example.key)}
              className="flex items-center gap-2 whitespace-nowrap rounded-full border border-hairline bg-surface/60 px-4 py-2 text-sm font-medium text-foreground/90 transition-[transform,background-color,border-color] duration-200 hover:-translate-y-0.5 hover:border-gold/30 hover:bg-surface-2"
            >
              <span aria-hidden className="opacity-90">
                {example.icon}
              </span>
              {example.label}
            </Link>
          </motion.div>
        ))}
      </div>
    </div>
  )
}
