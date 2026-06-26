import { AnimatePresence } from 'framer-motion'
import { Outlet, useLocation } from 'react-router-dom'
import { Header } from './Header'
import { BottomNav } from './BottomNav'
import { PageTransition } from './PageTransition'
import { Backdrop } from '@/components/shared/Backdrop'

export function AppShell() {
  const location = useLocation()

  return (
    <div className="relative flex min-h-svh flex-col">
      <Backdrop />
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
