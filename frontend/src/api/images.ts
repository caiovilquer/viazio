import { useQuery } from '@tanstack/react-query'

/**
 * Real destination photos, keyless: Wikipedia REST summary for the destination
 * city returns a lead image (cityscape/landmark) with open CORS. We reject
 * flags / coats of arms / SVGs so the card never falls back to a "flag photo".
 */

/**
 * Wikimedia only renders a fixed set of thumbnail widths on demand — empirically
 * 1280 / 1920 / 3840 (plus the summary's own small thumb). Any other width (640,
 * 1600, 2000, 2560…) returns HTTP 400. The 330px `thumbnail.source` is fine for a
 * small card but looks pixelated stretched across a full-bleed hero, so we upscale
 * the URL to the largest *allowed* bucket that the original actually supports.
 */
const BUCKET_WIDTHS = [1280, 1920, 3840] as const

function upscaleWikimedia(thumbSource: string, originalWidth: number | undefined, target: number) {
  const cap = originalWidth ?? 0
  const usable = BUCKET_WIDTHS.filter((w) => w <= target && w <= cap)
  if (usable.length === 0) return thumbSource // original too small to bucket — keep as-is
  return thumbSource.replace(/\/\d+px-/, `/${Math.max(...usable)}px-`)
}

async function fetchCityImage(city: string, targetWidth: number): Promise<string | null> {
  const res = await fetch(
    `https://en.wikipedia.org/api/rest_v1/page/summary/${encodeURIComponent(city)}`,
    { headers: { Accept: 'application/json' } },
  )
  if (!res.ok) return null
  const data = (await res.json()) as {
    thumbnail?: { source?: string }
    originalimage?: { source?: string; width?: number }
  }
  const src = data.thumbnail?.source
  if (!src) return null
  const lower = src.toLowerCase()
  if (lower.includes('flag') || lower.includes('coat_of_arms') || lower.endsWith('.svg')) {
    return null
  }
  return upscaleWikimedia(src, data.originalimage?.width, targetWidth)
}

/**
 * @param targetWidth desired display width. Defaults to a card-sized request
 * (keeps the lightweight ~330px thumb); pass a larger value (e.g. 1920) for heroes.
 */
export function useDestinationImage(city?: string | null, targetWidth = 640) {
  return useQuery({
    queryKey: ['destination-image', city, targetWidth],
    queryFn: () => fetchCityImage(city as string, targetWidth),
    enabled: Boolean(city),
    staleTime: Infinity,
    gcTime: 1000 * 60 * 60,
    retry: false,
  })
}
