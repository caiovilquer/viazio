import type {
  BestWindowsQuery,
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

export const DEFAULT_FORM_WEIGHTS: Record<CriterionKey, number> = {
  weather: 0.25,
  cost: 0.25,
  distance: 0.25,
  festivities: 0.25,
};

export interface SearchFormState {
  from: string;
  to: string;
  originCountry: string;
  originCity: string | undefined;
  region: Region | null;
  countries: string[];
  profile: ProfileKey | null;
  customWeights: boolean;
  weights: Record<CriterionKey, number>;
  travelers: number;
  maxBudget: string;
}

/** Converte critérios da URL para o estado do formulário em `/buscar`. */
export function criteriaToFormState(criteria: SearchCriteria): SearchFormState {
  const customWeights =
    criteria.profile === null && Object.keys(criteria.weights).length > 0;

  return {
    from: criteria.from,
    to: criteria.to,
    originCountry: criteria.origin.countryCode ?? "BR",
    originCity: criteria.origin.city,
    region: criteria.region,
    countries: [...criteria.countries],
    profile: customWeights ? null : (criteria.profile ?? "equilibrado"),
    customWeights,
    weights: customWeights
      ? { ...DEFAULT_FORM_WEIGHTS, ...criteria.weights }
      : { ...DEFAULT_FORM_WEIGHTS },
    travelers: criteria.travelers,
    maxBudget: criteria.maxGroundBudgetBrl
      ? String(criteria.maxGroundBudgetBrl)
      : "",
  };
}

/** Link para `/buscar` com os mesmos filtros da busca atual. */
export function buildSearchPageHref(params: URLSearchParams): string {
  const qs = params.toString();
  return qs ? `/buscar?${qs}` : "/buscar";
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

export interface JanelasCriteria extends SearchCriteria {
  minDays: number;
}

export interface JanelasFormState {
  from: string;
  to: string;
  region: Region | null;
  countries: string[];
  profile: ProfileKey | null;
  customWeights: boolean;
  weights: Record<CriterionKey, number>;
  minDays: number;
}

/** Converte critérios da URL para o estado do formulário em `/janelas`. */
export function janelasCriteriaToFormState(
  criteria: JanelasCriteria,
): JanelasFormState {
  const base = criteriaToFormState(criteria);
  return {
    from: base.from,
    to: base.to,
    region: base.region,
    countries: base.countries,
    profile: base.profile,
    customWeights: base.customWeights,
    weights: base.weights,
    minDays: criteria.minDays,
  };
}

export function janelasCriteriaToSearchParams(
  criteria: JanelasCriteria,
): URLSearchParams {
  const params = criteriaToSearchParams(criteria);
  params.set("minDays", String(criteria.minDays));
  return params;
}

export function searchParamsToJanelasCriteria(
  params: URLSearchParams,
): JanelasCriteria | null {
  const base = searchParamsToCriteria(params);
  if (!base) return null;
  if (!base.region && base.countries.length === 0) return null;

  const minDays = Number(params.get("minDays") ?? "3");
  if (!Number.isFinite(minDays) || minDays < 1) return null;

  return { ...base, minDays };
}

export function janelasCriteriaToQuery(
  criteria: JanelasCriteria,
): BestWindowsQuery {
  const customWeights =
    criteria.profile === null && Object.keys(criteria.weights).length > 0;

  return {
    from: criteria.from,
    to: criteria.to,
    minDays: criteria.minDays,
    topWindows: 8,
    destinationsPerWindow: 3,
    region: criteria.region ?? undefined,
    countries: criteria.region ? undefined : criteria.countries,
    profile: customWeights ? undefined : (criteria.profile ?? undefined),
    weights: customWeights ? criteria.weights : undefined,
    originCountry: criteria.origin.countryCode ?? "BR",
  };
}

/** Link para `/janelas` com os mesmos filtros da busca atual. */
export function buildJanelasPageHref(params: URLSearchParams): string {
  const qs = params.toString();
  return qs ? `/janelas?${qs}` : "/janelas";
}
