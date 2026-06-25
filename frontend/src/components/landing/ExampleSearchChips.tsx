import { Link } from 'react-router-dom'
import { motion } from 'framer-motion'
import { exampleSearches, exampleSearchHref } from '@/lib/example-searches'

export function ExampleSearchChips() {
  return (
    <motion.div
      initial={{ opacity: 0 }}
      animate={{ opacity: 1 }}
      transition={{ delay: 0.4 }}
      className="mt-8 flex flex-col items-center gap-3"
    >
      <p className="text-xs font-medium text-muted-foreground">ou tente um exemplo de 1 toque</p>
      <div className="flex max-w-full gap-2 overflow-x-auto px-4 pb-1 no-scrollbar sm:flex-wrap sm:justify-center sm:overflow-visible">
        {exampleSearches.map((example, i) => (
          <motion.div
            key={example.key}
            initial={{ opacity: 0, y: 8 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ delay: 0.45 + i * 0.06 }}
            className="shrink-0"
          >
            <Link
              to={exampleSearchHref(example.key)}
              className="flex items-center gap-1.5 whitespace-nowrap rounded-full border border-border bg-card px-3.5 py-2 text-sm font-medium text-foreground shadow-sm transition-colors hover:border-primary/40 hover:bg-primary/5"
            >
              <span aria-hidden>{example.icon}</span>
              {example.label}
            </Link>
          </motion.div>
        ))}
      </div>
    </motion.div>
  )
}
