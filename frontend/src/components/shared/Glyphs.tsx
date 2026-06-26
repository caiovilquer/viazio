/**
 * Abstract editorial glyphs — on-brand replacements for banned travel clichés
 * (no airplane / globe / pin). Stroke uses currentColor.
 */

/** A dotted arc between two points — a "route" without an airplane. */
export function RouteGlyph({ className }: { className?: string }) {
  return (
    <svg
      viewBox="0 0 24 24"
      fill="none"
      stroke="currentColor"
      strokeWidth="1.6"
      className={className}
      aria-hidden
    >
      <path d="M5 18C8 8 16 8 19 18" strokeDasharray="1.5 3" strokeLinecap="round" />
      <circle cx="5" cy="18" r="1.7" fill="currentColor" stroke="none" />
      <circle cx="19" cy="18" r="1.7" fill="currentColor" stroke="none" />
    </svg>
  )
}
