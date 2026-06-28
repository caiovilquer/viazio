import type { ProfileKey, Region } from "@/api/types";
import {
  criteriaToSearchParams,
  type SearchCriteria,
} from "@/lib/search-params";
import { toLocalIsoDate } from "@/lib/dates";

function offsetWindow(startOffsetDays: number, spanDays: number) {
  const start = new Date();
  start.setDate(start.getDate() + startOffsetDays);
  const end = new Date(start);
  end.setDate(end.getDate() + spanDays - 1);
  return { from: toLocalIsoDate(start), to: toLocalIsoDate(end) };
}

/** Próxima ocorrência futura do mês informado (0 = janeiro), começando no dia `day`. */
function nextMonthWindow(monthIndex: number, day: number, spanDays: number) {
  const now = new Date();
  let candidate = new Date(now.getFullYear(), monthIndex, day);
  if (candidate.getTime() <= now.getTime()) {
    candidate = new Date(now.getFullYear() + 1, monthIndex, day);
  }
  const end = new Date(candidate);
  end.setDate(end.getDate() + spanDays - 1);
  return { from: toLocalIsoDate(candidate), to: toLocalIsoDate(end) };
}

export interface ExampleSearch {
  key: string;
  label: string;
  icon: string;
  region: Region;
  profile: ProfileKey;
}

const EXAMPLES: Array<
  Omit<ExampleSearch, "icon"> & { window: { from: string; to: string } }
> = [
  {
    key: "americas-equilibrado",
    label: "Próximo feriadão nas Américas",
    region: "Americas",
    profile: "equilibrado",
    window: offsetWindow(14, 4),
  },
  {
    key: "europa-economica",
    label: "Europa econômica em outubro",
    region: "Europe",
    profile: "economico",
    window: nextMonthWindow(9, 5, 8),
  },
  {
    key: "asia-clima",
    label: "Clima perfeito na Ásia",
    region: "Asia",
    profile: "clima-perfeito",
    window: offsetWindow(45, 7),
  },
  {
    key: "oceania-aventura",
    label: "Aventura na Oceania",
    region: "Oceania",
    profile: "aventura",
    window: offsetWindow(75, 10),
  },
];

const PROFILE_ICON: Record<ProfileKey, string> = {
  equilibrado: "✨",
  economico: "💰",
  "clima-perfeito": "☀️",
  aventura: "🧭",
  cultural: "🏛️",
};

export const exampleSearches: ExampleSearch[] = EXAMPLES.map((example) => ({
  key: example.key,
  label: example.label,
  region: example.region,
  profile: example.profile,
  icon: PROFILE_ICON[example.profile],
}));

export function exampleSearchHref(key: string): string {
  const example = EXAMPLES.find((e) => e.key === key);
  if (!example) return "/buscar";
  const criteria: SearchCriteria = {
    from: example.window.from,
    to: example.window.to,
    countries: [],
    region: example.region,
    profile: example.profile,
    weights: {},
    travelers: 1,
    origin: { countryCode: "BR" },
  };
  return `/resultados?${criteriaToSearchParams(criteria).toString()}`;
}
