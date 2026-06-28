/**
 * Glifos editoriais abstratos — substitutos on-brand para clichês de viagem banidos
 * (sem avião / globo / pin). Traço usa currentColor.
 */

/** Arco pontilhado entre dois pontos — uma "rota" sem avião. */
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
      <path
        d="M5 18C8 8 16 8 19 18"
        strokeDasharray="1.5 3"
        strokeLinecap="round"
      />
      <circle cx="5" cy="18" r="1.7" fill="currentColor" stroke="none" />
      <circle cx="19" cy="18" r="1.7" fill="currentColor" stroke="none" />
    </svg>
  );
}
