import { NavLink, Link } from "react-router-dom";
import { motion } from "framer-motion";
import { navItems } from "./nav-items";
import { useFavorites } from "@/lib/favorites";
import { spring } from "@/lib/motion";
import { cn } from "@/lib/utils";

export function Header() {
  const favoritesCount = useFavorites().length;

  return (
    <header className="sticky top-0 z-50 hidden border-b border-hairline bg-[#06101f] md:block">
      <div className="mx-auto flex h-20 max-w-6xl items-center justify-between px-6">
        <Link
          to="/"
          aria-label="Viazio — início"
          className="flex items-center transition-opacity hover:opacity-90"
        >
          <img
            src="/logo-horizontal.svg"
            alt="Viazio"
            className="h-20 w-auto"
          />
        </Link>

        <nav className="flex items-center gap-1">
          {navItems.map((item) => (
            <NavLink
              key={item.to}
              to={item.to}
              end={item.end}
              className="group px-3.5 py-2"
            >
              {({ isActive }) => (
                <span
                  className={cn(
                    "relative flex items-center gap-1.5 text-[0.9rem] font-medium tracking-tight transition-colors",
                    isActive
                      ? "text-foreground"
                      : "text-muted-foreground group-hover:text-foreground",
                  )}
                >
                  {item.label}
                  {item.to === "/salvos" && favoritesCount > 0 && (
                    <span className="flex h-4 min-w-4 items-center justify-center rounded-full bg-gold/15 px-1 text-[10px] font-semibold text-gold">
                      {favoritesCount > 9 ? "9+" : favoritesCount}
                    </span>
                  )}
                  {isActive && (
                    <motion.span
                      layoutId="nav-underline"
                      transition={spring.snappy}
                      className="absolute -bottom-2 left-0 right-0 h-px bg-gold"
                    />
                  )}
                </span>
              )}
            </NavLink>
          ))}
        </nav>
      </div>
    </header>
  );
}
