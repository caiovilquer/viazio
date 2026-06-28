import { motion, useReducedMotion } from "framer-motion";

/**
 * Backdrop ambiente do app: navy profundo + brilho suave de fim de tarde, com camada
 * Atlas discreta (arcos de graticule, grid de pontos) e grão fino. Fixo, não interativo,
 * amigável à GPU. Movimento é um único drift quase imperceptível (desligado com reduced motion).
 */
export function Backdrop() {
  const reduce = useReducedMotion();

  return (
    <div
      aria-hidden
      className="pointer-events-none fixed inset-0 -z-10 overflow-hidden"
    >
      {/* profundidade vertical base */}
      <div className="absolute inset-0 bg-[radial-gradient(120%_80%_at_50%_-10%,oklch(0.22_0.03_262)_0%,var(--background)_55%)]" />

      {/* brilho quente de fim de tarde (dourado, centro-superior) */}
      <motion.div
        className="absolute -top-40 left-1/2 h-[42rem] w-[58rem] -translate-x-1/2 rounded-full blur-[120px]"
        style={{
          background:
            "radial-gradient(closest-side, oklch(0.74 0.1 72 / 0.16), transparent 72%)",
        }}
        animate={
          reduce ? undefined : { opacity: [0.85, 1, 0.85], scale: [1, 1.05, 1] }
        }
        transition={{ duration: 16, repeat: Infinity, ease: "easeInOut" }}
      />
      {/* sussurro coral discreto (acento emocional, deslocado) */}
      <div
        className="absolute -top-24 right-[12%] h-[26rem] w-[26rem] rounded-full blur-[120px]"
        style={{
          background:
            "radial-gradient(closest-side, oklch(0.7 0.16 38 / 0.08), transparent 70%)",
        }}
      />

      {/* graticule Atlas — arcos concêntricos, mascarados para sumir nas bordas */}
      <svg
        className="absolute -bottom-1/3 -right-1/4 h-[140%] w-[90%] mask-fade-edges text-gold/[0.06]"
        viewBox="0 0 600 600"
        fill="none"
        stroke="currentColor"
      >
        {[80, 150, 220, 290, 360, 430].map((r) => (
          <circle key={r} cx="430" cy="430" r={r} strokeWidth="1" />
        ))}
      </svg>

      {/* grid de pontos, desvanecendo nas bordas */}
      <div className="absolute inset-0 atlas-grid mask-fade-edges opacity-70" />

      {/* grão fino */}
      <div className="absolute inset-0 bg-grain opacity-[0.035] mix-blend-soft-light" />
    </div>
  );
}
