import { NavLink, useLocation } from "react-router-dom";
import { motion } from "framer-motion";
import { navItems } from "./nav-items";
import { useFavorites } from "@/lib/favorites";
import { spring } from "@/lib/motion";
import { cn } from "@/lib/utils";

export function BottomNav() {
  const location = useLocation();
  const favoritesCount = useFavorites().length;

  return (
    <nav className="fixed inset-x-0 bottom-0 z-50 border-t border-hairline glass pb-[env(safe-area-inset-bottom)] md:hidden">
      <ul className="mx-auto flex max-w-md items-stretch justify-around px-2">
        {navItems.map((item) => {
          const isActive = item.end
            ? location.pathname === item.to
            : location.pathname.startsWith(item.to);
          const Icon = item.icon;
          return (
            <li key={item.to} className="relative flex-1">
              <NavLink
                to={item.to}
                className="relative flex flex-col items-center gap-1 px-3 py-2.5 text-[0.7rem] font-medium tracking-tight"
              >
                {isActive && (
                  <motion.span
                    layoutId="bottom-nav-active"
                    className="absolute inset-x-4 top-0 h-px rounded-full bg-gold"
                    transition={spring.snappy}
                  />
                )}
                <span className="relative">
                  <Icon
                    className={cn(
                      "size-5 transition-colors",
                      isActive ? "text-gold" : "text-muted-foreground",
                    )}
                    strokeWidth={isActive ? 2.2 : 1.8}
                  />
                  {item.to === "/salvos" && favoritesCount > 0 && (
                    <span className="absolute -right-2 -top-1.5 flex h-3.5 min-w-3.5 items-center justify-center rounded-full bg-gold px-0.5 text-[8px] font-bold text-gold-foreground">
                      {favoritesCount > 9 ? "9+" : favoritesCount}
                    </span>
                  )}
                </span>
                <span
                  className={cn(
                    isActive ? "text-foreground" : "text-muted-foreground",
                  )}
                >
                  {item.label}
                </span>
              </NavLink>
            </li>
          );
        })}
      </ul>
    </nav>
  );
}
