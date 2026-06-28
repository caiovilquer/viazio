import { cn } from "@/lib/utils";

/**
 * Marca Viazio — o "V" dourado autoral (swoosh facetado) coroado por uma estrela.
 * Paths do /public/icon.svg do usuário, recortados aos limites da arte para
 * encaixar junto ao wordmark. Dourado plano da marca (#dfae62).
 */
export function BrandMark({ className }: { className?: string }) {
  return (
    <svg
      viewBox="95 118 895 805"
      fill="#dfae62"
      className={cn("h-7 w-7", className)}
      role="img"
      aria-label="Viazio"
    >
      <path d="M712.562 207.226C712.562 207.226 752.382 208.135 772.178 187.792C791.974 167.448 791.745 125.852 791.745 125.852C791.745 125.852 795.052 165.185 814.416 185.4C833.78 205.615 869.203 206.712 869.203 206.712C869.203 206.712 831.13 206.51 811.831 226.529C792.532 246.548 792.787 286.63 792.787 286.63C792.787 286.63 793.054 249.599 773.193 229.709C753.331 209.819 712.562 207.226 712.562 207.226Z" />
      <path d="M474.056 486.36C343.368 186.472 99.9714 182.341 99.9714 182.341C99.9714 182.341 217.155 256.947 322.964 605.573C322.964 605.573 419.782 913.569 643.459 913.569C643.459 913.569 622.488 906.286 574.801 787.842C549.303 724.512 519.896 628.895 474.056 486.36Z" />
      <path d="M661.084 781.296C661.084 781.296 601.535 641.72 620.339 595.561C639.143 549.402 703.935 480.148 703.935 480.148C703.935 480.148 845.684 346.509 980.052 356.131C873.39 422.154 661.084 781.296 661.084 781.296Z" />
    </svg>
  );
}

/**
 * Wordmark Viazio — geométrico, hairline, caps, redesenhado como vetores para
 * combinar com o logo autoral ("A" sem travessa, "O" circular). `non-scaling-stroke`
 * mantém o hairline nítido em qualquer tamanho. Herda cor via currentColor.
 */
export function Wordmark({ className }: { className?: string }) {
  return (
    <svg
      viewBox="-6 -6 408 112"
      fill="none"
      stroke="currentColor"
      strokeWidth={1.25}
      strokeLinecap="butt"
      strokeLinejoin="miter"
      overflow="visible"
      className={cn("h-[0.92em] w-auto overflow-visible", className)}
      role="img"
      aria-label="Viazio"
    >
      <g vectorEffect="non-scaling-stroke">
        <path d="M0 0 L26 100 L52 0" vectorEffect="non-scaling-stroke" />
        <path d="M80 0 L80 100" vectorEffect="non-scaling-stroke" />
        <path d="M108 100 L134 0 L160 100" vectorEffect="non-scaling-stroke" />
        <path
          d="M188 0 L240 0 L188 100 L240 100"
          vectorEffect="non-scaling-stroke"
        />
        <path d="M268 0 L268 100" vectorEffect="non-scaling-stroke" />
        <circle cx="346" cy="50" r="50" vectorEffect="non-scaling-stroke" />
      </g>
    </svg>
  );
}

/**
 * Lockup horizontal (marca + wordmark). Escala com font-size; quem chama define
 * o tamanho com classe `text-[…]`. `showWordmark={false}` → só a marca.
 */
export function Brand({
  className,
  showWordmark = true,
}: {
  className?: string;
  showWordmark?: boolean;
}) {
  return (
    <span
      className={cn(
        "inline-flex items-center gap-[0.42em] text-foreground",
        className,
      )}
    >
      <BrandMark className="h-[1.5em] w-[1.5em]" />
      {showWordmark && <Wordmark />}
    </span>
  );
}
