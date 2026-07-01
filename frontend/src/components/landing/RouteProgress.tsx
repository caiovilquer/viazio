import { useEffect, useRef, type RefObject } from "react";
import {
  motion,
  useMotionValueEvent,
  useReducedMotion,
  useScroll,
  useSpring,
  useTransform,
  type MotionValue,
} from "framer-motion";

/**
 * A "rota" que amarra a página — uma linha pontilhada única, no fundo, que se
 * desenha conforme o scroll avança e liga hero → demonstração → ranking → CTA.
 * Não é parallax (nada se move fora do eixo do scroll) nem partícula solta:
 * é o mesmo motivo de trajeto do resto da marca (ver `LandingBackdrop`), só que
 * amarrado ao progresso real de leitura da página, não a um loop decorativo.
 *
 * Coordenadas do viewBox usam uma escala normalizada (0–1000 × 0–2600) que o
 * SVG estica para o tamanho real da página via `preserveAspectRatio="none"` —
 * então os waypoints abaixo são posições relativas (% da altura da página),
 * não pixels exatos de cada seção.
 */

const ROUTE_D =
  "M 500 20 " +
  "C 640 140, 700 260, 560 400 " +
  "C 430 530, 360 660, 520 830 " +
  "C 660 990, 760 1150, 540 1340 " +
  "C 380 1480, 300 1620, 470 1780 " +
  "C 640 1930, 760 2050, 560 2220 " +
  "C 440 2330, 420 2420, 500 2560";

/** Coordenadas iguais aos pontos-âncora de `ROUTE_D` (extremos das curvas), para o
 *  marcador cair exatamente sobre a linha. */
const WAYPOINTS = [
  { id: "hero", x: 500, y: 20, at: 0.02 },
  { id: "demo", x: 520, y: 830, at: 0.32 },
  { id: "ranking", x: 470, y: 1780, at: 0.68 },
  { id: "cta", x: 500, y: 2560, at: 0.98 },
];

function RouteWaypoint({
  x,
  y,
  at,
  progress,
}: {
  x: number;
  y: number;
  at: number;
  progress: MotionValue<number>;
}) {
  // Offsets do useTransform precisam ficar dentro de [0, 1] e crescentes —
  // a WAAPI usada pelo framer-motion para acelerar transforms de scroll exige isso.
  const lit = useTransform(progress, [Math.max(0, at - 0.05), at], [0, 1]);
  const scale = useTransform(lit, [0, 1], [0.6, 1]);
  const opacity = useTransform(lit, [0, 1], [0.28, 0.95]);

  return (
    <motion.circle
      cx={x}
      cy={y}
      r={7}
      fill="var(--background)"
      stroke="currentColor"
      strokeWidth={1.6}
      style={{ scale, opacity }}
    />
  );
}

export function RouteProgress({
  containerRef,
}: {
  containerRef: RefObject<HTMLElement | null>;
}) {
  const reduce = useReducedMotion();
  const pathRef = useRef<SVGPathElement>(null);
  const dotRef = useRef<SVGCircleElement>(null);

  const { scrollYProgress } = useScroll({
    target: containerRef,
    offset: ["start start", "end end"],
  });
  const smooth = useSpring(scrollYProgress, {
    stiffness: 90,
    damping: 32,
    mass: 0.4,
  });

  const positionTraveler = (progress: number) => {
    const path = pathRef.current;
    const dot = dotRef.current;
    if (!path || !dot) return;
    const length = path.getTotalLength();
    const point = path.getPointAtLength(length * Math.min(1, Math.max(0, progress)));
    dot.setAttribute("cx", String(point.x));
    dot.setAttribute("cy", String(point.y));
  };

  useMotionValueEvent(smooth, "change", (v) => {
    if (reduce) return;
    positionTraveler(v);
  });

  useEffect(() => {
    positionTraveler(reduce ? 1 : scrollYProgress.get());
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [reduce]);

  return (
    <svg
      aria-hidden
      viewBox="0 0 1000 2600"
      preserveAspectRatio="none"
      className="pointer-events-none absolute inset-0 -z-10 hidden h-full w-full text-gold sm:block"
    >
      {/* trilho — sempre visível, bem discreto */}
      <path
        d={ROUTE_D}
        fill="none"
        stroke="currentColor"
        strokeOpacity={0.14}
        strokeWidth={2}
        strokeLinecap="round"
        strokeDasharray="1 13"
      />

      {/* trecho já percorrido — mesma linha, mais presente */}
      <motion.path
        ref={pathRef}
        d={ROUTE_D}
        fill="none"
        stroke="currentColor"
        strokeOpacity={0.55}
        strokeWidth={2}
        strokeLinecap="round"
        strokeDasharray="1 13"
        style={{ pathLength: reduce ? 1 : smooth }}
      />

      {WAYPOINTS.map((w) => (
        <RouteWaypoint key={w.id} x={w.x} y={w.y} at={w.at} progress={smooth} />
      ))}

      {!reduce && (
        <circle
          ref={dotRef}
          r={4.5}
          fill="currentColor"
          style={{ filter: "drop-shadow(0 0 7px oklch(0.82 0.1 85 / 0.85))" }}
        />
      )}
    </svg>
  );
}
