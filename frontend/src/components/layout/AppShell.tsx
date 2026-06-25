import { AnimatePresence } from 'framer-motion'
import { Outlet, useLocation } from 'react-router-dom'
import { Header } from './Header'
import { BottomNav } from './BottomNav'
import { PageTransition } from './PageTransition'

export function AppShell() {
  const location = useLocation()

  return (
    <div className="flex min-h-svh flex-col bg-background">
      <Header />
      <main className="flex-1 pb-24 md:pb-0">
        <AnimatePresence mode="wait" initial={false}>
          <PageTransition key={location.pathname}>
            <Outlet />
          </PageTransition>
        </AnimatePresence>
      </main>
      <BottomNav />
    </div>
  )
}
