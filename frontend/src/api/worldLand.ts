import { useQuery } from "@tanstack/react-query";
import { feature } from "topojson-client";
import type { Topology } from "topojson-specification";
import type { GeoPermissibleObjects } from "d3-geo";
import landTopologyUrl from "world-atlas/land-110m.json?url";

/**
 * Silhueta continental para CandidatesMap — topologia de massa terrestre única (sem
 * fronteiras de países, lê como marca-d'água discreta, nunca mapa político),
 * buscada uma vez e cacheada na sessão.
 */
async function fetchWorldLand(): Promise<GeoPermissibleObjects> {
  const res = await fetch(landTopologyUrl);
  const topology = (await res.json()) as Topology;
  return feature(
    topology,
    topology.objects.land,
  ) as unknown as GeoPermissibleObjects;
}

export function useWorldLand() {
  return useQuery({
    queryKey: ["world-land-110m"],
    queryFn: fetchWorldLand,
    staleTime: Infinity,
    gcTime: Infinity,
    retry: false,
  });
}
