/** O `imageUrl` do país na Wikipedia costuma ser só a bandeira — rejeitar esses casos. */
export function isLikelyFlag(url?: string | null): boolean {
  if (!url) return true;
  const normalized = url.toLowerCase();
  return normalized.includes("flag") || normalized.endsWith(".svg");
}

export function destinationPhotoUrl(imageUrl?: string | null): string | null {
  return !isLikelyFlag(imageUrl) ? imageUrl ?? null : null;
}
