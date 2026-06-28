import { useQuery } from '@tanstack/react-query'
import { feature } from 'topojson-client'
import type { Topology } from 'topojson-specification'
import type { GeoPermissibleObjects } from 'd3-geo'
import landTopologyUrl from 'world-atlas/land-110m.json?url'

/**
 * Continent silhouette for CandidatesMap — a single merged landmass topology (no
 * country borders, so it reads as a faint watermark, never a political map),
 * fetched once and cached for the session.
 */
async function fetchWorldLand(): Promise<GeoPermissibleObjects> {
  const res = await fetch(landTopologyUrl)
  const topology = (await res.json()) as Topology
  return feature(topology, topology.objects.land) as unknown as GeoPermissibleObjects
}

export function useWorldLand() {
  return useQuery({
    queryKey: ['world-land-110m'],
    queryFn: fetchWorldLand,
    staleTime: Infinity,
    gcTime: Infinity,
    retry: false,
  })
}
