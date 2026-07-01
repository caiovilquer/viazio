import { AnimatePresence, motion, useReducedMotion } from "framer-motion";
import { useLocation, useOutlet } from "react-router-dom";
import { Header } from "./Header";
import { BottomNav } from "./BottomNav";
import { Footer } from "./Footer";
import { pageTransitionMotionProps } from "./PageTransition";
import { Backdrop } from "@/components/shared/Backdrop";
import { LandingBackdrop } from "@/components/landing/LandingBackdrop";

export function AppShell() {
  const location = useLocation();
  const outlet = useOutlet();
  const reduce = useReducedMotion();
  const isLanding = location.pathname === "/";

  return (
    <div className="relative flex min-h-svh flex-col">
      {isLanding ? <LandingBackdrop /> : <Backdrop />}
      <Header />
      <main className="flex-1">
        <AnimatePresence mode="wait" initial={false}>
          {outlet &&
            (reduce ? (
              <div key={location.pathname}>{outlet}</div>
            ) : (
              <motion.div key={location.pathname} {...pageTransitionMotionProps}>
                {outlet}
              </motion.div>
            ))}
        </AnimatePresence>
      </main>
      <Footer />
      <BottomNav />
    </div>
  );
}
