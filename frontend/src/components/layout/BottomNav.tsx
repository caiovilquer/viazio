import { NavLink, useLocation } from 'react-router-dom'
import { motion } from 'framer-motion'
import { navItems } from './nav-items'
import { useFavorites } from '@/lib/favorites'
import { cn } from '@/lib/utils'

export function BottomNav() {
  const location = useLocation()
  const favoritesCount = useFavorites().length

  return (
    <nav className="fixed inset-x-0 bottom-0 z-50 border-t border-border/60 bg-background/80 pb-[env(safe-area-inset-bottom)] backdrop-blur-xl md:hidden">
      <ul className="mx-auto flex max-w-md items-stretch justify-around px-2">
        {navItems.map((item) => {
          const isActive = item.end
            ? location.pathname === item.to
            : location.pathname.startsWith(item.to)
          const Icon = item.icon
          return (
            <li key={item.to} className="relative flex-1">
              <NavLink
                to={item.to}
                className="relative flex flex-col items-center gap-1 px-3 py-2.5 text-xs font-medium"
              >
                {isActive && (
                  <motion.span
                    layoutId="bottom-nav-active"
                    className="absolute inset-x-3 top-0 h-0.5 rounded-full bg-primary"
                    transition={{ type: 'spring', stiffness: 500, damping: 35 }}
                  />
                )}
                <span className="relative">
                  <Icon
                    className={cn(
                      'size-5 transition-colors',
                      isActive ? 'text-primary' : 'text-muted-foreground',
                    )}
                    strokeWidth={isActive ? 2.4 : 2}
                  />
                  {item.to === '/salvos' && favoritesCount > 0 && (
                    <span className="absolute -right-1.5 -top-1.5 flex size-3.5 items-center justify-center rounded-full bg-primary text-[9px] font-bold text-primary-foreground">
                      {favoritesCount > 9 ? '9+' : favoritesCount}
                    </span>
                  )}
                </span>
                <span className={cn(isActive ? 'text-foreground' : 'text-muted-foreground')}>
                  {item.label}
                </span>
              </NavLink>
            </li>
          )
        })}
      </ul>
    </nav>
  )
}
