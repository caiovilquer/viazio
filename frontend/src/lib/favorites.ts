import { useSyncExternalStore } from "react";
import type {
  CriterionKey,
  ProfileKey,
  TravelRecommendation,
} from "@/api/types";
import { searchParamsToCriteria } from "@/lib/search-params";

const STORAGE_KEY = "feriadao:favorites";
const CHANGE_EVENT = "feriadao:favorites-changed";

/** Contexto de busca em que o destino foi salvo — para exibir e,
 *  ao comparar favoritos, re-pontuar numa janela comum. */
export interface FavoriteContext {
  from?: string;
  to?: string;
  profile?: ProfileKey | null;
  weights?: Partial<Record<CriterionKey, number>>;
  travelers?: number;
  originCountry?: string;
  originCity?: string;
}

export interface FavoriteEntry {
  countryCode: string;
  countryName: string;
  savedAt: string;
  recommendation: TravelRecommendation;
  context?: FavoriteContext;
}

/** Deriva um contexto salvável a partir da query string de busca (páginas resultados/destino). */
export function favoriteContextFromParams(
  params: URLSearchParams,
): FavoriteContext | undefined {
  const c = searchParamsToCriteria(params);
  if (!c) return undefined;
  return {
    from: c.from,
    to: c.to,
    profile: c.profile,
    weights: c.weights,
    travelers: c.travelers,
    originCountry: c.origin.countryCode,
    originCity: c.origin.city,
  };
}

let cachedRaw: string | null | undefined;
let cachedList: FavoriteEntry[] = [];

function readStorage(): string | null {
  try {
    return localStorage.getItem(STORAGE_KEY);
  } catch {
    return null;
  }
}

function getSnapshot(): FavoriteEntry[] {
  const raw = readStorage();
  if (raw === cachedRaw) return cachedList;
  cachedRaw = raw;
  try {
    const parsed = raw ? (JSON.parse(raw) as FavoriteEntry[]) : [];
    cachedList = parsed
      .slice()
      .sort((a, b) => b.savedAt.localeCompare(a.savedAt));
  } catch {
    cachedList = [];
  }
  return cachedList;
}

function writeAll(entries: FavoriteEntry[]) {
  try {
    localStorage.setItem(STORAGE_KEY, JSON.stringify(entries));
  } catch {
    // localStorage indisponível (modo privado, quota etc.) — falha silenciosa, sem persistência
  }
  window.dispatchEvent(new Event(CHANGE_EVENT));
}

export function isFavorite(code: string): boolean {
  return getSnapshot().some((f) => f.countryCode === code);
}

export function addFavorite(
  recommendation: TravelRecommendation,
  context?: FavoriteContext,
) {
  const entries = getSnapshot().filter(
    (f) => f.countryCode !== recommendation.countryCode,
  );
  entries.push({
    countryCode: recommendation.countryCode,
    countryName: recommendation.countryName,
    savedAt: new Date().toISOString(),
    recommendation,
    context,
  });
  writeAll(entries);
}

export function removeFavorite(code: string) {
  writeAll(getSnapshot().filter((f) => f.countryCode !== code));
}

export function toggleFavorite(
  recommendation: TravelRecommendation,
  context?: FavoriteContext,
) {
  if (isFavorite(recommendation.countryCode))
    removeFavorite(recommendation.countryCode);
  else addFavorite(recommendation, context);
}

function subscribe(callback: () => void) {
  window.addEventListener(CHANGE_EVENT, callback);
  window.addEventListener("storage", callback);
  return () => {
    window.removeEventListener(CHANGE_EVENT, callback);
    window.removeEventListener("storage", callback);
  };
}

const EMPTY: FavoriteEntry[] = [];

export function useFavorites(): FavoriteEntry[] {
  return useSyncExternalStore(subscribe, getSnapshot, () => EMPTY);
}

export function useIsFavorite(code: string): boolean {
  return useSyncExternalStore(
    subscribe,
    () => isFavorite(code),
    () => false,
  );
}
