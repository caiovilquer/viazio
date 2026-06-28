import { describe, expect, it } from "vitest";
import type { TravelRecommendation } from "@/api/types";
import { rescoreAll } from "./rescoring";

const recommendation: TravelRecommendation = {
  countryCode: "AR",
  countryName: "Argentina",
  destinationScore: 70,
  windowScore: 80,
  tripScore: 72,
  dataQuality: {
    coverage: 1,
    confidenceScore: 100,
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
      score: 80,
      weight: 0.25,
      contribution: 20,
      justification: "Bom clima",
    },
    {
      criterion: "cost",
      label: "Custo",
      icon: "💰",
      available: true,
      score: 60,
      weight: 0.25,
      contribution: 15,
      justification: "Custo médio",
    },
    {
      criterion: "distance",
      label: "Distância",
      icon: "✈️",
      available: true,
      score: 70,
      weight: 0.25,
      contribution: 17.5,
      justification: "Perto",
    },
    {
      criterion: "festivities",
      label: "Festividades",
      icon: "🎊",
      available: true,
      score: 50,
      weight: 0.25,
      contribution: 12.5,
      justification: "Poucos feriados",
    },
  ],
  highlights: [],
  tradeoffs: [],
  summary: "Resumo",
  exchangeToBrl: null,
  profile: {
    flagEmoji: "🇦🇷",
    population: null,
    populationYear: null,
    description: null,
    extract: null,
    imageUrl: null,
    wikipediaUrl: null,
  },
  feasibility: null,
  climate: null,
};

describe("rescoring", () => {
  it("reordena recomendações ao mudar pesos localmente", () => {
    const other: TravelRecommendation = {
      ...recommendation,
      countryCode: "CL",
      countryName: "Chile",
      breakdown: recommendation.breakdown.map((entry) =>
        entry.criterion === "weather"
          ? { ...entry, score: 40 }
          : { ...entry, score: 90 },
      ),
    };

    const climateFirst = rescoreAll([recommendation, other], {
      weather: 1,
      cost: 0,
      distance: 0,
      festivities: 0,
    });
    const costFirst = rescoreAll([recommendation, other], {
      weather: 0,
      cost: 1,
      distance: 0,
      festivities: 0,
    });

    expect(climateFirst[0]?.countryCode).toBe("AR");
    expect(costFirst[0]?.countryCode).toBe("CL");
    expect(climateFirst[0]?.tripScore).toBeGreaterThan(
      climateFirst[1]?.tripScore ?? 0,
    );
  });
});
