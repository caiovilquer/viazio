import type {
  CriterionKey,
  OriginInput,
  ProfileKey,
  RecommendationSearchRequest,
  Region,
} from "@/api/types";

export interface SearchCriteria {
  from: string;
  to: string;
  countries: string[];
  region: Region | null;
  profile: ProfileKey | null;
  weights: Partial<Record<CriterionKey, number>>;
  travelers: number;
  origin: OriginInput;
  maxGroundBudgetBrl?: number;
}

export function criteriaToSearchParams(
  criteria: SearchCriteria,
): URLSearchParams {
  const params = new URLSearchParams();
  params.set("from", criteria.from);
  params.set("to", criteria.to);
  if (criteria.region) {
    params.set("region", criteria.region);
  } else if (criteria.countries.length > 0) {
    params.set("countries", criteria.countries.join(","));
  }
  if (criteria.profile) params.set("profile", criteria.profile);
  const weightEntries = Object.entries(criteria.weights).filter(
    ([, v]) => v !== undefined,
  );
  if (weightEntries.length > 0) {
    params.set("weights", weightEntries.map(([k, v]) => `${k}:${v}`).join(","));
  }
  if (criteria.travelers > 1)
    params.set("travelers", String(criteria.travelers));
  if (criteria.origin.countryCode)
    params.set("originCountry", criteria.origin.countryCode);
  if (criteria.origin.city) params.set("originCity", criteria.origin.city);
  if (criteria.origin.latitude !== undefined)
    params.set("originLat", String(criteria.origin.latitude));
  if (criteria.origin.longitude !== undefined)
    params.set("originLng", String(criteria.origin.longitude));
  if (criteria.maxGroundBudgetBrl)
    params.set("maxBudget", String(criteria.maxGroundBudgetBrl));
  return params;
}

export function searchParamsToCriteria(
  params: URLSearchParams,
): SearchCriteria | null {
  const from = params.get("from");
  const to = params.get("to");
  if (!from || !to) return null;

  const weights: Partial<Record<CriterionKey, number>> = {};
  const weightsRaw = params.get("weights");
  if (weightsRaw) {
    for (const pair of weightsRaw.split(",")) {
      const [key, value] = pair.split(":");
      if (key && value) weights[key as CriterionKey] = Number(value);
    }
  }

  const countriesRaw = params.get("countries");
  const lat = params.get("originLat");
  const lng = params.get("originLng");

  return {
    from,
    to,
    countries: countriesRaw ? countriesRaw.split(",") : [],
    region: (params.get("region") as Region | null) ?? null,
    profile: (params.get("profile") as ProfileKey | null) ?? null,
    weights,
    travelers: Number(params.get("travelers") ?? "1"),
    origin: {
      countryCode: params.get("originCountry") ?? undefined,
      city: params.get("originCity") ?? undefined,
      latitude: lat ? Number(lat) : undefined,
      longitude: lng ? Number(lng) : undefined,
    },
    maxGroundBudgetBrl: params.get("maxBudget")
      ? Number(params.get("maxBudget"))
      : undefined,
  };
}

export function criteriaToRequest(
  criteria: SearchCriteria,
): RecommendationSearchRequest {
  return {
    from: criteria.from,
    to: criteria.to,
    countries: criteria.region ? undefined : criteria.countries,
    region: criteria.region ?? undefined,
    profile: criteria.profile ?? undefined,
    weights:
      Object.keys(criteria.weights).length > 0 ? criteria.weights : undefined,
    travelers: criteria.travelers,
    origin: criteria.origin,
    maxGroundBudgetBrl: criteria.maxGroundBudgetBrl,
  };
}
