import { motion, useReducedMotion } from 'framer-motion'
import type { ClimateSnapshot } from '@/api/types'
import { ease } from '@/lib/motion'
import { scoreTierColor } from './ScoreRing'

// Mirrors WeatherStrategy.java (backend) so the gauge lines up with the score the
// engine actually computed — comfort band, deviation curve and rain thresholds.
const COMFORT_MIN = 18
const COMFORT_MAX = 28
const GAUGE_MIN = -10
const GAUGE_MAX = 40

function temperatureTier(avgTempC: number) {
  const deviation = avgTempC < COMFORT_MIN
    ? COMFORT_MIN - avgTempC
    : avgTempC > COMFORT_MAX
      ? avgTempC - COMFORT_MAX
      : 0
  const score = Math.max(0, 100 - deviation * 6)
  if (score >= 80) return 'excellent'
  if (score >= 60) return 'good'
  if (score >= 40) return 'fair'
  return 'poor'
}

function rainDescription(rainyDayProbability: number) {
  if (rainyDayProbability < 0.2) return 'Tempo seco'
  if (rainyDayProbability < 0.5) return 'Chuva ocasional'
  return 'Chuva frequente'
}

/** Maps a value in [GAUGE_MIN, GAUGE_MAX] to a gauge angle in [-90, 90] (top half-circle). */
function angleFor(value: number) {
  const clamped = Math.max(GAUGE_MIN, Math.min(GAUGE_MAX, value))
  return -90 + ((clamped - GAUGE_MIN) / (GAUGE_MAX - GAUGE_MIN)) * 180
}

function pointOnArc(cx: number, cy: number, r: number, angleDeg: number) {
  const rad = (angleDeg * Math.PI) / 180
  return { x: cx + r * Math.sin(rad), y: cy - r * Math.cos(rad) }
}

function arcPath(cx: number, cy: number, r: number, startDeg: number, endDeg: number) {
  const start = pointOnArc(cx, cy, r, startDeg)
  const end = pointOnArc(cx, cy, r, endDeg)
  const largeArc = endDeg - startDeg > 180 ? 1 : 0
  return `M ${start.x} ${start.y} A ${r} ${r} 0 ${largeArc} 1 ${end.x} ${end.y}`
}

function TemperatureGauge({ avgTempC, tempStdDevC }: { avgTempC: number; tempStdDevC: number }) {
  const reduce = useReducedMotion()
  const size = 168
  const margin = 22
  const canvasWidth = size + margin * 2
  const cx = canvasWidth / 2
  const cy = size / 2 + 6
  const r = size / 2 - 14
  const tier = temperatureTier(avgTempC)
  const color = scoreTierColor[tier]
  const valueAngle = angleFor(avgTempC)
  const tip = pointOnArc(cx, cy, r, valueAngle)

  return (
    <div className="flex flex-col items-center">
      <svg width={canvasWidth} height={size / 2 + 28} viewBox={`0 0 ${canvasWidth} ${size / 2 + 28}`}>
        <path
          d={arcPath(cx, cy, r, -90, 90)}
          fill="none"
          stroke="var(--surface-3)"
          strokeWidth={10}
          strokeLinecap="round"
        />
        <path
          d={arcPath(cx, cy, r, angleFor(COMFORT_MIN), angleFor(COMFORT_MAX))}
          fill="none"
          stroke="var(--gold)"
          strokeOpacity={0.25}
          strokeWidth={10}
          strokeLinecap="round"
        />
        {reduce ? (
          <path
            d={arcPath(cx, cy, r, -90, valueAngle)}
            fill="none"
            stroke={color}
            strokeWidth={10}
            strokeLinecap="round"
          />
        ) : (
          <motion.path
            d={arcPath(cx, cy, r, -90, valueAngle)}
            fill="none"
            stroke={color}
            strokeWidth={10}
            strokeLinecap="round"
            initial={{ pathLength: 0 }}
            whileInView={{ pathLength: 1 }}
            viewport={{ once: true, margin: '-40px' }}
            transition={{ duration: 0.9, ease: ease.out, delay: 0.1 }}
          />
        )}
        <circle cx={tip.x} cy={tip.y} r={5} fill={color} />
        <circle cx={tip.x} cy={tip.y} r={9} fill={color} opacity={0.18} />
        <text x={pointOnArc(cx, cy, r + 14, -90).x} y={pointOnArc(cx, cy, r + 14, -90).y + 4}
          textAnchor="middle" className="fill-muted-foreground text-[9px]">
          {GAUGE_MIN}°
        </text>
        <text x={pointOnArc(cx, cy, r + 14, 90).x} y={pointOnArc(cx, cy, r + 14, 90).y + 4}
          textAnchor="middle" className="fill-muted-foreground text-[9px]">
          {GAUGE_MAX}°
        </text>
      </svg>
      <div className="-mt-8 flex flex-col items-center leading-none">
        <span className="font-display text-3xl font-semibold tabular-nums">
          {Math.round(avgTempC)}°C
        </span>
        <span className="mt-1 text-xs text-muted-foreground">
          variação típica ±{Math.round(tempStdDevC)}°C
        </span>
      </div>
    </div>
  )
}

export function ClimateChart({ climate }: { climate: ClimateSnapshot }) {
  const dryScore = Math.max(0, Math.min(100, 100 - climate.rainyDayProbability * 100))
  const rainTier = dryScore >= 80 ? 'excellent' : dryScore >= 60 ? 'good' : dryScore >= 40 ? 'fair' : 'poor'

  return (
    <div className="rounded-2xl border border-hairline bg-surface-1/60 p-5 sm:p-6">
      <div className="mb-4 flex items-center justify-between gap-3">
        <p className="text-sm font-medium text-foreground/85">Médias do período</p>
        <span className="inline-flex shrink-0 items-center gap-1.5 rounded-full border border-hairline bg-surface-2/60 px-3 py-1 text-[0.7rem] text-muted-foreground">
          {climate.sourceType === 'FORECAST'
            ? 'Previsão'
            : `Climatologia · ${climate.sampledYears} anos`}
        </span>
      </div>

      <div className="grid gap-6 sm:grid-cols-2 sm:items-center">
        <TemperatureGauge avgTempC={climate.avgTempC} tempStdDevC={climate.tempStdDevC} />

        <div className="flex items-center justify-center gap-5 sm:justify-start">
          <RainGauge score={dryScore} color={scoreTierColor[rainTier]} />
          <div className="min-w-0">
            <p className="font-display text-lg leading-tight">{rainDescription(climate.rainyDayProbability)}</p>
            <p className="mt-1 text-xs leading-relaxed text-muted-foreground">
              {Math.round(climate.rainyDayProbability * 100)}% de chance de chuva num dia qualquer,
              {' '}≈{climate.avgDailyPrecipMm.toFixed(1)} mm/dia em média.
            </p>
          </div>
        </div>
      </div>
    </div>
  )
}

function RainGauge({ score, color }: { score: number; color: string }) {
  const reduce = useReducedMotion()
  const size = 76
  const strokeWidth = 7
  const radius = (size - strokeWidth) / 2
  const circumference = 2 * Math.PI * radius
  const targetOffset = circumference - (score / 100) * circumference

  return (
    <div className="relative flex shrink-0 items-center justify-center" style={{ width: size, height: size }}>
      <svg width={size} height={size} className="-rotate-90">
        <circle cx={size / 2} cy={size / 2} r={radius} fill="none" stroke="var(--surface-3)" strokeWidth={strokeWidth} />
        {reduce ? (
          <circle
            cx={size / 2}
            cy={size / 2}
            r={radius}
            fill="none"
            stroke={color}
            strokeWidth={strokeWidth}
            strokeLinecap="round"
            strokeDasharray={circumference}
            strokeDashoffset={targetOffset}
          />
        ) : (
          <motion.circle
            cx={size / 2}
            cy={size / 2}
            r={radius}
            fill="none"
            stroke={color}
            strokeWidth={strokeWidth}
            strokeLinecap="round"
            strokeDasharray={circumference}
            initial={{ strokeDashoffset: circumference }}
            whileInView={{ strokeDashoffset: targetOffset }}
            viewport={{ once: true, margin: '-40px' }}
            transition={{ duration: 0.9, ease: ease.out, delay: 0.15 }}
          />
        )}
      </svg>
      <span className="absolute font-display text-xs font-semibold tabular-nums">{Math.round(score)}</span>
    </div>
  )
}
