import { useQuery } from "@tanstack/react-query";

/**
 * Fotos reais de destino, sem chave: o resumo REST da Wikipedia da cidade
 * retorna imagem principal (cityscape/marco) com CORS aberto. Rejeitamos
 * bandeiras / brasões / SVGs para o card nunca cair numa "foto de bandeira".
 */

/**
 * O Wikimedia só renderiza sob demanda um conjunto fixo de larguras de miniatura — empiricamente
 * 1280 / 1920 / 3840 (além do thumb pequeno do summary). Qualquer outra largura (640,
 * 1600, 2000, 2560…) retorna HTTP 400. O `thumbnail.source` de 330px serve num
 * card pequeno mas pixeliza esticado num destaque em largura total, então upscalamos
 * a URL para o maior bucket *permitido* que o original suporta.
 */
const BUCKET_WIDTHS = [1280, 1920, 3840] as const;

function upscaleWikimedia(
  thumbSource: string,
  originalWidth: number | undefined,
  target: number,
) {
  const cap = originalWidth ?? 0;
  const usable = BUCKET_WIDTHS.filter((w) => w <= target && w <= cap);
  if (usable.length === 0) return thumbSource; // original pequeno demais para bucket — mantém como está
  return thumbSource.replace(/\/\d+px-/, `/${Math.max(...usable)}px-`);
}

async function fetchCityImage(
  city: string,
  targetWidth: number,
): Promise<string | null> {
  const res = await fetch(
    `https://en.wikipedia.org/api/rest_v1/page/summary/${encodeURIComponent(city)}`,
    { headers: { Accept: "application/json" } },
  );
  if (!res.ok) return null;
  const data = (await res.json()) as {
    thumbnail?: { source?: string };
    originalimage?: { source?: string; width?: number };
  };
  const src = data.thumbnail?.source;
  if (!src) return null;
  const lower = src.toLowerCase();
  if (
    lower.includes("flag") ||
    lower.includes("coat_of_arms") ||
    lower.endsWith(".svg")
  ) {
    return null;
  }
  return upscaleWikimedia(src, data.originalimage?.width, targetWidth);
}

/**
 * @param targetWidth largura de exibição desejada. Padrão pede tamanho de card
 * (mantém o thumb leve ~330px); passe valor maior (ex.: 1920) para destaques em largura total.
 */
export function useDestinationImage(city?: string | null, targetWidth = 640) {
  return useQuery({
    queryKey: ["destination-image", city, targetWidth],
    queryFn: () => fetchCityImage(city as string, targetWidth),
    enabled: Boolean(city),
    staleTime: Infinity,
    gcTime: 1000 * 60 * 60,
    retry: false,
  });
}
