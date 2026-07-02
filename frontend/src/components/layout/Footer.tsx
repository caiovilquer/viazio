import { Link } from "react-router-dom";
import { navItems } from "./nav-items";

export function Footer() {
  const year = new Date().getFullYear();

  return (
    <footer className="relative mt-auto border-t border-hairline bg-[#06101f] pb-20 md:pb-0">
      <div className="mx-auto grid max-w-6xl gap-10 px-6 py-14 sm:grid-cols-[1.5fr_1fr] lg:py-16">
        <div className="max-w-sm">
          <Link
            to="/"
            aria-label="Viazio, início"
            className="inline-flex transition-opacity hover:opacity-90"
          >
            <img
              src="/logo-horizontal.svg"
              alt="Viazio"
              className="h-30 w-auto"
            />
          </Link>
          <p className="mt-4 text-sm leading-relaxed text-muted-foreground">
            Feriadões que rendem mais. A Viazio cruza clima, câmbio, custo de
            vida e festividades e explica, em uma nota clara, qual destino
            vale a viagem.
          </p>
          <p className="mt-5 flex items-center gap-2.5 text-[0.7rem] font-semibold uppercase tracking-[0.24em] text-gold/80">
            <span>Planeje</span>
            <span className="size-1 rounded-full bg-gold/60" />
            <span>Descubra</span>
            <span className="size-1 rounded-full bg-gold/60" />
            <span>Viva</span>
          </p>
        </div>

        <nav className="hidden sm:block" aria-label="Rodapé">
          <p className="mb-3.5 text-[0.7rem] font-semibold uppercase tracking-[0.22em] text-muted-foreground">
            Navegar
          </p>
          <ul className="space-y-2.5 text-sm">
            {navItems.map((item) => (
              <li key={item.to}>
                <Link
                  to={item.to}
                  className="text-foreground/80 transition-colors hover:text-foreground"
                >
                  {item.label}
                </Link>
              </li>
            ))}
          </ul>
        </nav>
      </div>

      <div className="border-t border-hairline">
        <div className="mx-auto flex max-w-6xl flex-col gap-1.5 px-6 py-5 text-xs text-muted-foreground sm:flex-row sm:items-center sm:justify-between">
          <span>© {year} Viazio</span>
          <span>Dados de fontes públicas e auditáveis.</span>
        </div>
      </div>
    </footer>
  );
}
