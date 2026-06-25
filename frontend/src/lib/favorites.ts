import { useSyncExternalStore } from 'react'
import type { TravelRecommendation } from '@/api/types'

const STORAGE_KEY = 'feriadao:favorites'
const CHANGE_EVENT = 'feriadao:favorites-changed'

export interface FavoriteEntry {
  countryCode: string
  countryName: string
  savedAt: string
  recommendation: TravelRecommendation
}

let cachedRaw: string | null | undefined
let cachedList: FavoriteEntry[] = []

function readStorage(): string | null {
  try {
    return localStorage.getItem(STORAGE_KEY)
  } catch {
    return null
  }
}

function getSnapshot(): FavoriteEntry[] {
  const raw = readStorage()
  if (raw === cachedRaw) return cachedList
  cachedRaw = raw
  try {
    const parsed = raw ? (JSON.parse(raw) as FavoriteEntry[]) : []
    cachedList = parsed.slice().sort((a, b) => b.savedAt.localeCompare(a.savedAt))
  } catch {
    cachedList = []
  }
  return cachedList
}

function writeAll(entries: FavoriteEntry[]) {
  try {
    localStorage.setItem(STORAGE_KEY, JSON.stringify(entries))
  } catch {
    // localStorage indisponível (modo privado, quota etc.) — falha silenciosa, sem persistência
  }
  window.dispatchEvent(new Event(CHANGE_EVENT))
}

export function isFavorite(code: string): boolean {
  return getSnapshot().some((f) => f.countryCode === code)
}

export function addFavorite(recommendation: TravelRecommendation) {
  const entries = getSnapshot().filter((f) => f.countryCode !== recommendation.countryCode)
  entries.push({
    countryCode: recommendation.countryCode,
    countryName: recommendation.countryName,
    savedAt: new Date().toISOString(),
    recommendation,
  })
  writeAll(entries)
}

export function removeFavorite(code: string) {
  writeAll(getSnapshot().filter((f) => f.countryCode !== code))
}

export function toggleFavorite(recommendation: TravelRecommendation) {
  if (isFavorite(recommendation.countryCode)) removeFavorite(recommendation.countryCode)
  else addFavorite(recommendation)
}

function subscribe(callback: () => void) {
  window.addEventListener(CHANGE_EVENT, callback)
  window.addEventListener('storage', callback)
  return () => {
    window.removeEventListener(CHANGE_EVENT, callback)
    window.removeEventListener('storage', callback)
  }
}

const EMPTY: FavoriteEntry[] = []

export function useFavorites(): FavoriteEntry[] {
  return useSyncExternalStore(subscribe, getSnapshot, () => EMPTY)
}

export function useIsFavorite(code: string): boolean {
  return useSyncExternalStore(subscribe, () => isFavorite(code), () => false)
}
