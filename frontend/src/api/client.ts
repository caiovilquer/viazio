import axios, { type AxiosError } from "axios";
import type {
  ApiErrorBody,
  ApiMetaResponse,
  BestWindowsQuery,
  BestWindowsResponse,
  Country,
  Holiday,
  RecommendationSearchRequest,
  RecommendationResponse,
  TravelOverview,
} from "./types";

const baseURL = import.meta.env.VITE_API_BASE_URL ?? "/api/v1";

export const api = axios.create({ baseURL });

export class ApiError extends Error {
  status: number;
  body?: ApiErrorBody;

  constructor(message: string, status: number, body?: ApiErrorBody) {
    super(message);
    this.status = status;
    this.body = body;
  }
}

api.interceptors.response.use(
  (response) => response,
  (error: AxiosError<ApiErrorBody>) => {
    const status = error.response?.status ?? 0;
    const message = error.response?.data?.message ?? error.message;
    return Promise.reject(new ApiError(message, status, error.response?.data));
  },
);

function toCsv(values?: string[]) {
  return values && values.length > 0 ? values.join(",") : undefined;
}

function toWeightsParam(weights?: Partial<Record<string, number>>) {
  if (!weights) return undefined;
  const entries = Object.entries(weights).filter(([, v]) => v !== undefined);
  if (entries.length === 0) return undefined;
  return entries.map(([k, v]) => `${k}:${v}`).join(",");
}

export async function fetchMeta() {
  const { data } = await api.get<ApiMetaResponse>("/meta");
  return data;
}

export async function fetchRecommendations(
  request: RecommendationSearchRequest,
) {
  const { data } = await api.post<RecommendationResponse>(
    "/recommendations",
    request,
  );
  return data;
}

export async function fetchBestWindows(query: BestWindowsQuery) {
  const { data } = await api.get<BestWindowsResponse>(
    "/recommendations/best-windows",
    {
      params: {
        from: query.from,
        to: query.to,
        minDays: query.minDays,
        topWindows: query.topWindows,
        countries: toCsv(query.countries),
        region: query.region,
        destinationsPerWindow: query.destinationsPerWindow,
        profile: query.profile,
        weights: toWeightsParam(query.weights),
        exclude: toCsv(query.exclude),
        originCountry: query.originCountry,
        originSubdivision: query.originSubdivision,
        originLatitude: query.originLatitude,
        originLongitude: query.originLongitude,
        originCity: query.originCity,
        travelers: query.travelers,
        maxGroundBudget: query.maxGroundBudget,
      },
    },
  );
  return data;
}

export async function fetchCountry(countryCode: string) {
  const { data } = await api.get<Country>(`/countries/${countryCode}`);
  return data;
}

export async function fetchHolidays(countryCode: string) {
  const { data } = await api.get<Holiday[]>(`/holidays/${countryCode}`);
  return data;
}

export async function fetchTravelOverview(countryCode: string) {
  const { data } = await api.get<TravelOverview>(`/travel/${countryCode}`);
  return data;
}
