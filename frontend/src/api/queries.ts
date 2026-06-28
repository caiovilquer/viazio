import { useQuery } from "@tanstack/react-query";
import {
  fetchBestWindows,
  fetchCountry,
  fetchHolidays,
  fetchMeta,
  fetchRecommendations,
  fetchTravelOverview,
} from "./client";
import type { BestWindowsQuery, RecommendationSearchRequest } from "./types";

export function useMeta() {
  return useQuery({
    queryKey: ["meta"],
    queryFn: fetchMeta,
    staleTime: 24 * 60 * 60 * 1000,
  });
}

export function useRecommendations(
  request: RecommendationSearchRequest | null,
) {
  return useQuery({
    queryKey: ["recommendations", request],
    queryFn: () => fetchRecommendations(request as RecommendationSearchRequest),
    enabled: request !== null,
    staleTime: 5 * 60 * 1000,
    retry: false,
  });
}

export function useBestWindows(query: BestWindowsQuery | null) {
  return useQuery({
    queryKey: ["best-windows", query],
    queryFn: () => fetchBestWindows(query as BestWindowsQuery),
    enabled: query !== null,
    staleTime: 5 * 60 * 1000,
    retry: false,
  });
}

export function useCountry(countryCode: string | undefined) {
  return useQuery({
    queryKey: ["country", countryCode],
    queryFn: () => fetchCountry(countryCode as string),
    enabled: Boolean(countryCode),
    staleTime: 60 * 60 * 1000,
  });
}

export function useHolidays(countryCode: string | undefined) {
  return useQuery({
    queryKey: ["holidays", countryCode],
    queryFn: () => fetchHolidays(countryCode as string),
    enabled: Boolean(countryCode),
    staleTime: 60 * 60 * 1000,
  });
}

export function useTravelOverview(countryCode: string | undefined) {
  return useQuery({
    queryKey: ["travel-overview", countryCode],
    queryFn: () => fetchTravelOverview(countryCode as string),
    enabled: Boolean(countryCode),
    staleTime: 60 * 60 * 1000,
  });
}
