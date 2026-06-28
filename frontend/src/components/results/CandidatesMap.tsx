import { useEffect, useMemo, useRef, useState } from "react";
import { geoGraticule10, geoNaturalEarth1, geoPath } from "d3-geo";
import { select } from "d3-selection";
import {
  zoom as d3zoom,
  zoomIdentity,
  type D3ZoomEvent,
  type ZoomBehavior,
  type ZoomTransform,
} from "d3-zoom";
import "d3-transition";
import type {
  Exchange,
  OriginReference,
  TravelRecommendation,
} from "@/api/types";
import { useWorldLand } from "@/api/worldLand";
import { scoreTierColor } from "@/components/shared/ScoreRing";
import { scoreTone, formatInOriginCurrency } from "@/lib/format";
import { Flag } from "@/components/shared/Flag";
import { Button } from "@/components/ui/button";
import { Minus, Plus, Maximize } from "lucide-react";
import { cn } from "@/lib/utils";

const WIDTH = 960;
const HEIGHT = 460;
const FIT_PADDING = 42;
/** Limite de quanto um cluster regional apertado pode dar zoom, como múltiplo da escala
 * mundial — evita que dois destinos muito próximos esticem a silhueta até virar um
 * borrão irreconhecível, mas ainda dá zoom perceptível numa região real. */
const MAX_ZOOM = 6;
/** Até onde o usuário pode dar pinch/scroll-zoom além do encaixe regional acima. */
const MAX_USER_ZOOM = 8;
const PAN_MARGIN = 160;

/** Ajusta a projeção à dispersão real de origem + destinos, para que uma busca
 * regionalmente agrupada (ex.: tudo nas Américas) dê zoom em vez de mostrar
 * sempre o mundo inteiro na mesma escala. */
function buildProjection(points: [number, number][]) {
  const worldProjection = geoNaturalEarth1().fitSize([WIDTH, HEIGHT], {
    type: "Sphere",
  } as never);
  if (points.length === 0) return worldProjection;

  const projection = geoNaturalEarth1().fitExtent(
    [
      [FIT_PADDING, FIT_PADDING],
      [WIDTH - FIT_PADDING, HEIGHT - FIT_PADDING],
    ],
    { type: "MultiPoint", coordinates: points } as never,
  );

  const maxScale = worldProjection.scale() * MAX_ZOOM;
  if (projection.scale() > maxScale) {
    const lons = points.map((p) => p[0]);
    const lats = points.map((p) => p[1]);
    const centerLon = (Math.min(...lons) + Math.max(...lons)) / 2;
    const centerLat = (Math.min(...lats) + Math.max(...lats)) / 2;
    projection
      .scale(maxScale)
      .center([centerLon, centerLat])
      .translate([WIDTH / 2, HEIGHT / 2]);
  }
  return projection;
}

/** Curva suave para cima entre dois pontos na tela — trajeto estilizado, não um círculo máximo real. */
function routeArc(x1: number, y1: number, x2: number, y2: number) {
  const mx = (x1 + x2) / 2;
  const bow = Math.min(Math.abs(x2 - x1) * 0.16, 60);
  const my = (y1 + y2) / 2 - bow;
  return `M ${x1} ${y1} Q ${mx} ${my} ${x2} ${y2}`;
}

interface MapPoint {
  recommendation: TravelRecommendation;
  rank: number;
  x: number;
  y: number;
}

export function CandidatesMap({
  recommendations,
  origin,
  originExchangeToBrl,
  hoveredCode,
  onHoverChange,
  onSelect,
  svgClassName,
}: {
  recommendations: TravelRecommendation[];
  origin: OriginReference;
  originExchangeToBrl?: Exchange | null;
  hoveredCode: string | null;
  onHoverChange: (code: string | null) => void;
  onSelect: (recommendation: TravelRecommendation) => void;
  svgClassName?: string;
}) {
  const { data: land } = useWorldLand();
  const [focused, setFocused] = useState<string | null>(null);
  const svgRef = useRef<SVGSVGElement>(null);
  const zoomBehaviorRef = useRef<ZoomBehavior<SVGSVGElement, unknown> | null>(
    null,
  );
  const [transform, setTransform] = useState<ZoomTransform>(zoomIdentity);

  const coords = useMemo<[number, number][]>(() => {
    const list: [number, number][] = [[origin.longitude, origin.latitude]];
    for (const recommendation of recommendations) {
      const destination = recommendation.feasibility?.destination;
      if (destination) list.push([destination.longitude, destination.latitude]);
    }
    return list;
  }, [origin, recommendations]);

  const projection = useMemo(() => buildProjection(coords), [coords]);
  const path = useMemo(() => geoPath(projection), [projection]);
  const sphereOutline = useMemo(
    () => path({ type: "Sphere" } as never) ?? undefined,
    [path],
  );
  const graticuleOutline = useMemo(
    () => path(geoGraticule10()) ?? undefined,
    [path],
  );
  const landPath = useMemo(
    () => (land ? (path(land) ?? undefined) : undefined),
    [land, path],
  );

  const originPoint = useMemo(
    () => projection([origin.longitude, origin.latitude]),
    [projection, origin],
  );

  const points = useMemo<MapPoint[]>(() => {
    return recommendations
      .map((recommendation, i) => {
        const destination = recommendation.feasibility?.destination;
        if (!destination) return null;
        const projected = projection([
          destination.longitude,
          destination.latitude,
        ]);
        if (!projected) return null;
        return {
          recommendation,
          rank: i + 1,
          x: projected[0],
          y: projected[1],
        };
      })
      .filter((p): p is MapPoint => p !== null);
  }, [recommendations, projection]);

  // Configura d3-zoom uma vez. Toque: um dedo sempre rola a página (touch-action:
  // pan-y abaixo + filtro rejeitando toque único); só pinch/arraste com 2 dedos move o
  // mapa — evita prender o scroll da página no mobile, a principal reclamação que isso corrige. Roda:
  // só com ctrl/cmd (pinch no trackpad define isso automaticamente) pelo mesmo motivo no
  // desktop; arrastar com o mouse e os botões +/- sempre funcionam.
  useEffect(() => {
    const svg = svgRef.current;
    if (!svg) return;

    const behavior = d3zoom<SVGSVGElement, unknown>()
      .scaleExtent([1, MAX_USER_ZOOM])
      .translateExtent([
        [-PAN_MARGIN, -PAN_MARGIN],
        [WIDTH + PAN_MARGIN, HEIGHT + PAN_MARGIN],
      ])
      .filter((event: Event) => {
        if (event.type === "wheel")
          return (event as WheelEvent).ctrlKey || (event as WheelEvent).metaKey;
        if (event.type === "touchstart" || event.type === "touchmove") {
          return (event as TouchEvent).touches.length > 1;
        }
        return !(event as MouseEvent).button;
      })
      .on("zoom", (event: D3ZoomEvent<SVGSVGElement, unknown>) =>
        setTransform(event.transform),
      );

    select(svg).call(behavior);
    zoomBehaviorRef.current = behavior;

    return () => {
      select(svg).on(".zoom", null);
    };
  }, []);

  // Nova busca / novo conjunto de candidatos: começa sem zoom na projeção recém-ajustada acima.
  useEffect(() => {
    const svg = svgRef.current;
    const behavior = zoomBehaviorRef.current;
    if (!svg || !behavior) return;
    select(svg).call(behavior.transform, zoomIdentity);
  }, [coords]);

  function zoomBy(factor: number) {
    const svg = svgRef.current;
    const behavior = zoomBehaviorRef.current;
    if (!svg || !behavior) return;
    select(svg).transition().duration(200).call(behavior.scaleBy, factor);
  }

  function resetZoom() {
    const svg = svgRef.current;
    const behavior = zoomBehaviorRef.current;
    if (!svg || !behavior) return;
    select(svg)
      .transition()
      .duration(250)
      .call(behavior.transform, zoomIdentity);
  }

  const activeCode = hoveredCode ?? focused;
  const active = points.find(
    (p) => p.recommendation.countryCode === activeCode,
  );
  const activeScreen = active
    ? {
        x: active.x * transform.k + transform.x,
        y: active.y * transform.k + transform.y,
      }
    : null;
  const inverseScale = 1 / transform.k;

  function setActive(code: string | null) {
    setFocused(code);
    onHoverChange(code);
  }

  return (
    <div className="relative overflow-hidden rounded-2xl border border-hairline bg-[radial-gradient(120%_140%_at_50%_0%,var(--surface-2),var(--surface-1))]">
      <div className="absolute inset-0 atlas-grid opacity-40 mask-fade-edges" />
      <svg
        ref={svgRef}
        viewBox={`0 0 ${WIDTH} ${HEIGHT}`}
        className={cn(
          "relative w-full cursor-grab touch-pan-y select-none active:cursor-grabbing",
          svgClassName ?? "h-72 sm:h-80 lg:h-96",
        )}
        style={{ touchAction: "pan-y" }}
        role="img"
        aria-label="Mapa dos destinos rankeados, com distância em relação à origem. Arraste para mover, use dois dedos ou ctrl/cmd + scroll para aplicar zoom."
      >
        <g
          transform={`translate(${transform.x} ${transform.y}) scale(${transform.k})`}
        >
          {sphereOutline && (
            <path
              d={sphereOutline}
              fill="none"
              stroke="var(--hairline)"
              strokeWidth={1}
              vectorEffect="non-scaling-stroke"
            />
          )}
          {graticuleOutline && (
            <path
              d={graticuleOutline}
              fill="none"
              stroke="var(--gold)"
              strokeOpacity={0.07}
              strokeWidth={0.75}
              vectorEffect="non-scaling-stroke"
            />
          )}
          {landPath && (
            <path
              d={landPath}
              fill="var(--surface-3)"
              fillOpacity={0.55}
              stroke="none"
            />
          )}

          {originPoint &&
            points.map((p) => {
              const isActive = p.recommendation.countryCode === activeCode;
              return (
                <path
                  key={`arc-${p.recommendation.countryCode}`}
                  d={routeArc(originPoint[0], originPoint[1], p.x, p.y)}
                  fill="none"
                  stroke="var(--gold)"
                  strokeOpacity={isActive ? 0.7 : 0.18}
                  strokeWidth={isActive ? 1.4 : 1}
                  vectorEffect="non-scaling-stroke"
                  className="atlas-flow transition-[stroke-opacity] duration-300"
                />
              );
            })}

          {originPoint && (
            <g>
              <circle
                cx={originPoint[0]}
                cy={originPoint[1]}
                r={7}
                fill="none"
                stroke="var(--primary)"
                strokeWidth={1.5}
                vectorEffect="non-scaling-stroke"
              />
              <circle
                cx={originPoint[0]}
                cy={originPoint[1]}
                r={2.5}
                fill="var(--primary)"
              />
            </g>
          )}

          {points.map((p) => {
            const tier = scoreTone(p.recommendation.tripScore);
            const color = scoreTierColor[tier];
            const isActive = p.recommendation.countryCode === activeCode;
            const radius = isActive ? 7 : p.rank <= 3 ? 5.5 : 4.2;
            return (
              <g
                key={p.recommendation.countryCode}
                tabIndex={0}
                role="button"
                aria-label={`${p.recommendation.countryName}, posição ${p.rank}, nota ${Math.round(p.recommendation.tripScore)}`}
                className="cursor-pointer outline-none"
                onMouseEnter={() => setActive(p.recommendation.countryCode)}
                onMouseLeave={() => setActive(null)}
                onFocus={() => setActive(p.recommendation.countryCode)}
                onBlur={() => setActive(null)}
                onClick={() => onSelect(p.recommendation)}
                onKeyDown={(e) => {
                  if (e.key === "Enter" || e.key === " ")
                    onSelect(p.recommendation);
                }}
              >
                {isActive && (
                  <circle
                    cx={p.x}
                    cy={p.y}
                    r={radius + 6}
                    fill={color}
                    opacity={0.18}
                  />
                )}
                <circle
                  cx={p.x}
                  cy={p.y}
                  r={radius}
                  fill={color}
                  stroke="var(--background)"
                  strokeWidth={1.5}
                  vectorEffect="non-scaling-stroke"
                  className="transition-[r] duration-200"
                />
                {p.rank <= 3 && (
                  <g
                    transform={`translate(${p.x} ${p.y - radius - 4}) scale(${inverseScale})`}
                  >
                    <text
                      textAnchor="middle"
                      className="fill-foreground/70 text-[9px] font-medium"
                    >
                      #{p.rank}
                    </text>
                  </g>
                )}
              </g>
            );
          })}
        </g>
      </svg>

      <div className="pointer-events-none absolute right-2.5 bottom-12 z-10 flex flex-col gap-1">
        <Button
          type="button"
          size="icon"
          variant="secondary"
          className="pointer-events-auto size-8 rounded-full shadow-md"
          aria-label="Aumentar zoom"
          onClick={() => zoomBy(1.6)}
        >
          <Plus className="size-4" />
        </Button>
        <Button
          type="button"
          size="icon"
          variant="secondary"
          className="pointer-events-auto size-8 rounded-full shadow-md"
          aria-label="Diminuir zoom"
          onClick={() => zoomBy(1 / 1.6)}
        >
          <Minus className="size-4" />
        </Button>
        <Button
          type="button"
          size="icon"
          variant="secondary"
          className="pointer-events-auto size-8 rounded-full shadow-md"
          aria-label="Restaurar visualização"
          onClick={resetZoom}
        >
          <Maximize className="size-3.5" />
        </Button>
      </div>

      {active && activeScreen && (
        <div
          className="pointer-events-none absolute z-10 w-48 -translate-x-1/2 rounded-xl border border-hairline bg-background/95 p-3 text-xs shadow-xl backdrop-blur"
          style={{
            left: `${(activeScreen.x / WIDTH) * 100}%`,
            top: `${(activeScreen.y / HEIGHT) * 100}%`,
            transform: `translate(-50%, ${activeScreen.y / HEIGHT > 0.55 ? "-115%" : "18px"})`,
          }}
        >
          <div className="flex items-center gap-2">
            <Flag
              code={active.recommendation.countryCode}
              className="h-3.5 w-5 shrink-0"
            />
            <p className="truncate font-display text-sm font-semibold">
              {active.recommendation.countryName}
            </p>
          </div>
          <div className="mt-1.5 flex items-center justify-between text-muted-foreground">
            <span>
              <span className="text-gold">#{active.rank}</span> · nota{" "}
              {Math.round(active.recommendation.tripScore)}
            </span>
          </div>
          {active.recommendation.feasibility && (
            <p className="mt-1 text-muted-foreground">
              {Math.round(
                active.recommendation.feasibility.travelEffort.distanceKm,
              ).toLocaleString("pt-BR")}{" "}
              km
              {active.recommendation.feasibility.groundCost &&
                active.recommendation.feasibility.groundCost
                  .estimatedDailyPerPerson > 0 && (
                  <>
                    {" "}
                    ·{" "}
                    {
                      formatInOriginCurrency(
                        active.recommendation.feasibility.groundCost
                          .estimatedDailyPerPerson,
                        originExchangeToBrl,
                        origin.countryCode,
                      ).formatted
                    }
                    /dia
                  </>
                )}
            </p>
          )}
        </div>
      )}

      <div className="relative flex flex-wrap items-center gap-x-4 gap-y-1.5 border-t border-hairline px-4 py-2.5 text-[0.7rem] text-muted-foreground">
        <span className="inline-flex items-center gap-1.5">
          <span className="size-2 rounded-full border border-primary" />
          Origem
        </span>
        {(["excellent", "good", "fair", "poor"] as const).map((tier) => (
          <span key={tier} className="inline-flex items-center gap-1.5">
            <span
              className="size-2 rounded-full"
              style={{ background: scoreTierColor[tier] }}
            />
            {tierLabel[tier]}
          </span>
        ))}
        <span className="ml-auto hidden text-muted-foreground/70 sm:inline">
          Arraste · pinça ou ctrl + scroll para zoom
        </span>
      </div>
    </div>
  );
}

const tierLabel: Record<string, string> = {
  excellent: "Ótimo",
  good: "Bom",
  fair: "Razoável",
  poor: "Desafiador",
};
