import { motion, useReducedMotion } from "framer-motion";

/**
 * Backdrop vivo e temático só da Landing — luz de fim de tarde à deriva e
 * rotas Atlas fluindo com um ponto viajando por elas (o mesmo motivo de
 * "trajeto" usado nos cards de destino, não decoração solta). Sem estrelas
 * ou partículas que não significam nada para o produto.
 * Fixo + não interativo + amigável à GPU (só transform/opacity).
 * Respeita prefers-reduced-motion congelando numa cena estática composta.
 */

/** Rotas Atlas — curvas geodésicas suaves atravessando o viewport. */
const ROUTES = [
  {
    id: "route-1",
    d: "M-60 250 C 360 80, 1080 90, 1500 320",
    opacity: 0.13,
    dur: "7s",
  },
  {
    id: "route-2",
    d: "M-60 540 C 420 380, 1020 430, 1500 250",
    opacity: 0.1,
    dur: "9.5s",
  },
  {
    id: "route-3",
    d: "M-60 720 C 380 650, 1060 770, 1500 600",
    opacity: 0.08,
    dur: "12s",
  },
];

/** Pontos viajando ao longo das rotas — a mesma metáfora de trajeto da marca. */
const TRAVELLERS = [
  { path: "#route-1", r: 3.2, dur: "17s" },
  { path: "#route-2", r: 2.6, dur: "24s" },
  { path: "#route-3", r: 2.2, dur: "21s" },
];

export function LandingBackdrop() {
  const reduce = Boolean(useReducedMotion());

  return (
    <div
      aria-hidden
      className="pointer-events-none fixed inset-0 -z-10 overflow-hidden"
    >
      {/* profundidade vertical base */}
      <div className="absolute inset-0 bg-[radial-gradient(120%_85%_at_50%_-10%,oklch(0.225_0.03_262)_0%,var(--background)_55%)]" />

      {/* luz de fim de tarde à deriva */}
      <motion.div
        className="absolute left-1/2 top-[-12%] h-[46rem] w-[62rem] rounded-full blur-[130px]"
        style={{
          background:
            "radial-gradient(closest-side, oklch(0.78 0.11 75 / 0.18), transparent 72%)",
        }}
        animate={
          reduce
            ? { x: "-50%" }
            : {
                x: ["-50%", "-43%", "-50%"],
                y: [0, 26, 0],
                scale: [1, 1.07, 1],
              }
        }
        transition={
          reduce
            ? undefined
            : { duration: 24, repeat: Infinity, ease: "easeInOut" }
        }
      />
      <motion.div
        className="absolute left-[-8%] top-[40%] h-[34rem] w-[40rem] rounded-full blur-[120px]"
        style={{
          background:
            "radial-gradient(closest-side, oklch(0.74 0.1 70 / 0.12), transparent 70%)",
        }}
        animate={
          reduce
            ? undefined
            : { x: [0, 54, 0], y: [0, -30, 0], scale: [1, 1.1, 1] }
        }
        transition={
          reduce
            ? undefined
            : { duration: 31, repeat: Infinity, ease: "easeInOut" }
        }
      />
      {/* sussurro coral discreto — acento emocional, raro */}
      <motion.div
        className="absolute right-[6%] top-[-6%] h-[26rem] w-[26rem] rounded-full blur-[120px]"
        style={{
          background:
            "radial-gradient(closest-side, oklch(0.7 0.16 38 / 0.10), transparent 70%)",
        }}
        animate={
          reduce
            ? undefined
            : { x: [0, -32, 0], y: [0, 30, 0], opacity: [0.65, 1, 0.65] }
        }
        transition={
          reduce
            ? undefined
            : { duration: 19, repeat: Infinity, ease: "easeInOut" }
        }
      />

      {/* rotas Atlas + ponto viajante */}
      <svg
        className="absolute inset-0 size-full mask-fade-edges text-gold"
        viewBox="0 0 1440 900"
        preserveAspectRatio="xMidYMid slice"
        fill="none"
        stroke="currentColor"
      >
        {ROUTES.map((r) => (
          <path
            key={r.id}
            id={r.id}
            d={r.d}
            strokeWidth={1}
            className="atlas-flow"
            style={{ strokeOpacity: r.opacity, animationDuration: r.dur }}
          />
        ))}
        {!reduce &&
          TRAVELLERS.map((t) => (
            <circle
              key={t.path}
              r={t.r}
              fill="currentColor"
              style={{
                filter: "drop-shadow(0 0 6px oklch(0.82 0.1 85 / 0.9))",
              }}
            >
              <animateMotion
                dur={t.dur}
                repeatCount="indefinite"
                calcMode="linear"
                keyPoints="0;1"
                keyTimes="0;1"
              >
                <mpath href={t.path} />
              </animateMotion>
            </circle>
          ))}
      </svg>

      {/* grid de pontos + grão discretos, desvanecendo nas bordas */}
      <div className="absolute inset-0 atlas-grid mask-fade-edges opacity-60" />
      <div className="absolute inset-0 bg-grain opacity-[0.04] mix-blend-soft-light" />
    </div>
  );
}
