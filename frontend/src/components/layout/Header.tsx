import { NavLink, Link } from 'react-router-dom'
import { Plane } from 'lucide-react'
import { navItems } from './nav-items'
import { cn } from '@/lib/utils'

export function Header() {
  return (
    <header className="sticky top-0 z-50 hidden border-b border-border/60 bg-background/80 backdrop-blur-xl md:block">
      <div className="mx-auto flex h-16 max-w-6xl items-center justify-between px-6">
        <Link to="/" className="flex items-center gap-2 font-display text-lg font-semibold tracking-tight">
          <span className="flex size-8 items-center justify-center rounded-full bg-primary text-primary-foreground">
            <Plane className="size-4" strokeWidth={2.4} />
          </span>
          feriad<span className="text-primary">ão</span>
        </Link>
        <nav className="flex items-center gap-1">
          {navItems.map((item) => (
            <NavLink
              key={item.to}
              to={item.to}
              end={item.end}
              className={({ isActive }) =>
                cn(
                  'rounded-full px-4 py-2 text-sm font-medium transition-colors',
                  isActive
                    ? 'bg-primary/10 text-primary'
                    : 'text-muted-foreground hover:bg-secondary hover:text-foreground',
                )
              }
            >
              {item.label}
            </NavLink>
          ))}
        </nav>
      </div>
    </header>
  )
}
