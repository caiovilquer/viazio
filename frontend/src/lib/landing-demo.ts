import type {
  DataSourceInfo,
  TravelRecommendation,
  WindowAssessment,
} from "@/api/types";
import { criteriaToSearchParams, type SearchCriteria } from "@/lib/search-params";
import { toLocalIsoDate } from "@/lib/dates";

/**
 * Exemplo da Landing — mockado (sem chamada à API na home), mas com os números
 * reais de uma busca real: 12 de outubro, saindo do Brasil, contra
 * Argentina/Chile/Uruguai. Os valores abaixo (notas, distância, clima, câmbio,
 * justificativas) são uma cópia congelada da resposta real de
 * `POST /recommendations` para esse período — não são inventados — só não são
 * recalculados a cada carregamento da home. Os links (`landingExampleHref`)
 * apontam para a busca real com os mesmos parâmetros.
 *
 * A origem é só o país (Brasil) — o catálogo de cidades da API não cobre o
 * Brasil como origem, então nunca enviamos uma cidade aqui.
 */

function nextOctober(day: number): Date {
  const now = new Date();
  let candidate = new Date(now.getFullYear(), 9, day);
  if (candidate.getTime() <= now.getTime()) {
    candidate = new Date(now.getFullYear() + 1, 9, day);
  }
  return candidate;
}

export const landingExampleCountries = ["AR", "CL", "UY"];
export const landingExampleProfile = "equilibrado" as const;
export const landingExampleOriginLabel = "Brasil";

export const demoWindow = {
  from: toLocalIsoDate(nextOctober(9)),
  to: toLocalIsoDate(nextOctober(12)),
  totalDays: 4,
  holidayName: "Nossa Senhora Aparecida",
  originLabel: landingExampleOriginLabel,
};

export const demoFilterChips = [
  "Saindo do Brasil",
  "12 out · 4 dias",
  "Até R$ 3.500",
  "Voo até 5h",
  "Sem visto",
];

/** Cópia congelada de `RecommendationResponse.window` para essa janela (origem Brasil). */
export const landingExampleWindow: WindowAssessment = {
  score: 65.0,
  totalDays: 4,
  freeDays: 3,
  requiredLeaveDays: 1,
  longWeekends: [
    {
      start: demoWindow.to,
      end: demoWindow.to,
      totalDays: 3,
      bridgeDaysUsed: 0,
      holidayName: "Our Lady of Aparecida",
    },
  ],
  explanation:
    "3 de 4 dias livres; requer 1 dia(s) útil(eis) de folga; melhor feriadão de 3 dias",
};

const notIncluded = [
  "passagens aéreas",
  "bagagem e taxas aeroportuárias",
  "seguro-viagem",
  "visto, vacinas e demais requisitos de entrada",
];

/** Cópia congelada de `RecommendationResponse.recommendations` para essa janela. */
export const landingExampleRecommendations: TravelRecommendation[] = [
  {
    countryCode: "CL",
    countryName: "Chile",
    destinationScore: 72.9,
    windowScore: 65.0,
    tripScore: 71.3,
    dataQuality: {
      coverage: 1.0,
      confidenceScore: 100.0,
      availableCriteria: 4,
      totalCriteria: 4,
      missingCriteria: [],
    },
    breakdown: [
      {
        criterion: "weather",
        label: "Clima",
        icon: "☀️",
        available: true,
        score: 88.7,
        weight: 0.25,
        contribution: 22.2,
        justification: "Clima ótimo: ~16°C, tempo seco (climatologia, 10 anos)",
      },
      {
        criterion: "distance",
        label: "Distância",
        icon: "✈️",
        available: true,
        score: 83.3,
        weight: 0.25,
        contribution: 20.8,
        justification: "Pertinho: ~3.011 km da origem",
      },
      {
        criterion: "festivities",
        label: "Festividades no destino",
        icon: "🎊",
        available: true,
        score: 75.0,
        weight: 0.25,
        contribution: 18.8,
        justification:
          "1 feriado(s) nacionais: Columbus Day; oportunidade cultural com possível alteração de horários",
      },
      {
        criterion: "cost",
        label: "Custo de vida",
        icon: "💰",
        available: true,
        score: 44.5,
        weight: 0.25,
        contribution: 11.1,
        justification:
          "Mais caro: nível de preços ~111% da origem (dados 2025/2025)",
      },
    ],
    highlights: [
      "clima ótimo",
      "deslocamento geográfico menor",
      "calendário local interessante",
    ],
    tradeoffs: [
      "Mais caro: nível de preços ~111% da origem (dados 2025/2025)",
    ],
    summary:
      "Chile, nota de viagem 71: clima ótimo, deslocamento geográfico menor, calendário local interessante; confiança 100%",
    exchangeToBrl: { currency: "CLP", valueInReais: 0.005606 },
    profile: {
      flagEmoji: "🇨🇱",
      population: null,
      populationYear: null,
      description: "país na América do Sul",
      extract: null,
      imageUrl: null,
      wikipediaUrl: null,
    },
    feasibility: {
      destination: {
        countryCode: "CL",
        name: "Santiago",
        latitude: -33.4375,
        longitude: -70.65,
        utcOffsets: [-4.0],
        primary: true,
      },
      travelEffort: {
        distanceKm: 3010.8,
        estimatedTravelHoursMin: 4.3,
        estimatedTravelHoursMax: 7.3,
        originUtcOffset: -3.0,
        destinationUtcOffset: -4.0,
        timeZoneDifferenceHours: 1.0,
        classification: "medium",
        estimated: true,
      },
      groundCost: null,
      notIncluded,
    },
    climate: {
      avgTempC: 15.83,
      avgDailyPrecipMm: 0.28,
      rainyDayProbability: 0.1,
      tempStdDevC: 3.09,
      sampledDays: 40,
      sampledYears: 10,
      sourceType: "CLIMATOLOGY",
      referenceFrom: "2016-10-09",
      referenceTo: "2025-10-12",
    },
  },
  {
    countryCode: "AR",
    countryName: "Argentina",
    destinationScore: 72.9,
    windowScore: 65.0,
    tripScore: 71.3,
    dataQuality: {
      coverage: 1.0,
      confidenceScore: 100.0,
      availableCriteria: 4,
      totalCriteria: 4,
      missingCriteria: [],
    },
    breakdown: [
      {
        criterion: "distance",
        label: "Distância",
        icon: "✈️",
        available: true,
        score: 87.0,
        weight: 0.25,
        contribution: 21.8,
        justification: "Pertinho: ~2.339 km da origem",
      },
      {
        criterion: "weather",
        label: "Clima",
        icon: "☀️",
        available: true,
        score: 77.6,
        weight: 0.25,
        contribution: 19.4,
        justification:
          "Clima agradável: ~17°C, chuva ocasional (climatologia, 10 anos)",
      },
      {
        criterion: "festivities",
        label: "Festividades no destino",
        icon: "🎊",
        available: true,
        score: 75.0,
        weight: 0.25,
        contribution: 18.8,
        justification:
          "1 feriado(s) nacionais: Day of Respect for Cultural Diversity; oportunidade cultural com possível alteração de horários",
      },
      {
        criterion: "cost",
        label: "Custo de vida",
        icon: "💰",
        available: true,
        score: 52.0,
        weight: 0.25,
        contribution: 13.0,
        justification:
          "Custo parecido: nível de preços ~96% da origem (dados 2025/2025)",
      },
    ],
    highlights: [
      "deslocamento geográfico menor",
      "clima agradável",
      "calendário local interessante",
    ],
    // Custo perto do da origem — não é destaque nem contraponto, só contribui no cálculo.
    tradeoffs: [],
    summary:
      "Argentina, nota de viagem 71: deslocamento geográfico menor, clima agradável, calendário local interessante; confiança 100%",
    exchangeToBrl: { currency: "ARS", valueInReais: 0.00348903 },
    profile: {
      flagEmoji: "🇦🇷",
      population: null,
      populationYear: null,
      description: "país na América do Sul",
      extract: null,
      imageUrl: null,
      wikipediaUrl: null,
    },
    feasibility: {
      destination: {
        countryCode: "AR",
        name: "Buenos Aires",
        latitude: -34.599722222,
        longitude: -58.381944444,
        utcOffsets: [-3.0],
        primary: true,
      },
      travelEffort: {
        distanceKm: 2339.1,
        estimatedTravelHoursMin: 3.6,
        estimatedTravelHoursMax: 6.3,
        originUtcOffset: -3.0,
        destinationUtcOffset: -3.0,
        timeZoneDifferenceHours: 0.0,
        classification: "short",
        estimated: true,
      },
      groundCost: null,
      notIncluded,
    },
    climate: {
      avgTempC: 17.21,
      avgDailyPrecipMm: 3.85,
      rainyDayProbability: 0.35,
      tempStdDevC: 2.94,
      sampledDays: 40,
      sampledYears: 10,
      sourceType: "CLIMATOLOGY",
      referenceFrom: "2016-10-09",
      referenceTo: "2025-10-12",
    },
  },
  {
    countryCode: "UY",
    countryName: "Uruguai",
    destinationScore: 65.1,
    windowScore: 65.0,
    tripScore: 65.0,
    dataQuality: {
      coverage: 1.0,
      confidenceScore: 100.0,
      availableCriteria: 4,
      totalCriteria: 4,
      missingCriteria: [],
    },
    breakdown: [
      {
        criterion: "distance",
        label: "Distância",
        icon: "✈️",
        available: true,
        score: 87.3,
        weight: 0.25,
        contribution: 21.8,
        justification: "Pertinho: ~2.281 km da origem",
      },
      {
        criterion: "weather",
        label: "Clima",
        icon: "☀️",
        available: true,
        score: 76.9,
        weight: 0.25,
        contribution: 19.2,
        justification:
          "Clima agradável: ~16°C, chuva ocasional (climatologia, 10 anos)",
      },
      {
        criterion: "festivities",
        label: "Festividades no destino",
        icon: "🎊",
        available: true,
        score: 75.0,
        weight: 0.25,
        contribution: 18.8,
        justification:
          "1 feriado(s) nacionais: Day of the race; oportunidade cultural com possível alteração de horários",
      },
      {
        criterion: "cost",
        label: "Custo de vida",
        icon: "💰",
        available: true,
        score: 21.0,
        weight: 0.25,
        contribution: 5.3,
        justification:
          "Bem mais caro: nível de preços ~158% da origem (dados 2024/2025)",
      },
    ],
    highlights: [
      "deslocamento geográfico menor",
      "clima agradável",
      "calendário local interessante",
    ],
    tradeoffs: [
      "Bem mais caro: nível de preços ~158% da origem (dados 2024/2025)",
    ],
    summary:
      "Uruguai, nota de viagem 65: deslocamento geográfico menor, clima agradável, calendário local interessante; confiança 100%",
    exchangeToBrl: { currency: "UYU", valueInReais: 0.1271955 },
    profile: {
      flagEmoji: "🇺🇾",
      population: null,
      populationYear: null,
      description: "país na América do Sul",
      extract: null,
      imageUrl: null,
      wikipediaUrl: null,
    },
    feasibility: {
      destination: {
        countryCode: "UY",
        name: "Montevideo",
        latitude: -34.905916666,
        longitude: -56.191311111,
        utcOffsets: [-3.0],
        primary: true,
      },
      travelEffort: {
        distanceKm: 2280.9,
        estimatedTravelHoursMin: 3.5,
        estimatedTravelHoursMax: 6.3,
        originUtcOffset: -3.0,
        destinationUtcOffset: -3.0,
        timeZoneDifferenceHours: 0.0,
        classification: "short",
        estimated: true,
      },
      groundCost: null,
      notIncluded,
    },
    climate: {
      avgTempC: 15.89,
      avgDailyPrecipMm: 3.11,
      rainyDayProbability: 0.325,
      tempStdDevC: 2.56,
      sampledDays: 40,
      sampledYears: 10,
      sourceType: "CLIMATOLOGY",
      referenceFrom: "2016-10-09",
      referenceTo: "2025-10-12",
    },
  },
];

export const landingExampleOriginExchangeToBrl = null;

function demoCriteria(countryCode?: string): SearchCriteria {
  return {
    from: demoWindow.from,
    to: demoWindow.to,
    countries: countryCode ? [countryCode] : landingExampleCountries,
    region: null,
    profile: landingExampleProfile,
    weights: {},
    travelers: 1,
    origin: { countryCode: "BR" },
  };
}

/** Link para a busca real (mesmas datas/origem/perfil do exemplo). */
export function landingExampleHref(countryCode?: string): string {
  return `/resultados?${criteriaToSearchParams(demoCriteria(countryCode)).toString()}`;
}

/** Catálogo real de fontes (espelha `ApiMetaService.build()` no backend) — usado como
 *  estado inicial enquanto `/meta` carrega, para a seção nunca aparecer vazia. */
export const dataSourcesFallback: DataSourceInfo[] = [
  { key: "countries", label: "mledoze/countries", mode: "STATIC", purpose: "países, moedas e regiões" },
  { key: "destinations", label: "Wikidata", mode: "STATIC", purpose: "capitais, coordenadas e offsets UTC" },
  { key: "holidays", label: "Nager.Date", mode: "LIVE", purpose: "feriados nacionais e por subdivisão" },
  { key: "weather", label: "Open-Meteo", mode: "LIVE_AND_HISTORICAL", purpose: "previsão e climatologia" },
  { key: "economy", label: "World Bank", mode: "LIVE_CACHED", purpose: "PPP, câmbio oficial e população" },
  { key: "exchange", label: "AwesomeAPI", mode: "LIVE_CACHED", purpose: "câmbio nominal informativo" },
  { key: "content", label: "Wikipedia", mode: "LIVE_CACHED", purpose: "resumo, imagem e link do destino" },
];
