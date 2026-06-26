import { useState } from 'react'
import { cn } from '@/lib/utils'

/**
 * Country flag as a real image (flagcdn, by ISO 3166-1 alpha-2 code).
 * Emoji flags (`flagEmoji`) don't render on Windows/WSL or Linux Chromium,
 * so we never rely on them. Contained + rounded — never stretched full-bleed.
 */
export function Flag({ code, className }: { code?: string | null; className?: string }) {
  const [failed, setFailed] = useState(false)

  if (!code || failed) {
    return (
      <span
        aria-hidden
        className={cn('inline-block h-3.5 w-5 rounded-[3px] bg-surface-3 ring-1 ring-hairline', className)}
      />
    )
  }

  return (
    <img
      src={`https://flagcdn.com/${code.toLowerCase()}.svg`}
      alt=""
      loading="lazy"
      onError={() => setFailed(true)}
      className={cn(
        'inline-block h-3.5 w-5 rounded-[3px] object-cover ring-1 ring-hairline',
        className,
      )}
    />
  )
}
