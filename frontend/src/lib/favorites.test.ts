import { describe, expect, it, beforeEach } from "vitest";
import {
  addFavorite,
  isFavorite,
  removeFavorite,
  toggleFavorite,
} from "./favorites";
import type { TravelRecommendation } from "@/api/types";

const recommendation = {
  countryCode: "PT",
  countryName: "Portugal",
  destinationScore: 80,
  windowScore: 70,
  tripScore: 78,
  dataQuality: {
    coverage: 1,
    confidenceScore: 100,
    availableCriteria: 4,
    totalCriteria: 4,
    missingCriteria: [],
  },
  breakdown: [],
  highlights: [],
  tradeoffs: [],
  summary: "Resumo",
  exchangeToBrl: null,
  profile: {
    flagEmoji: "🇵🇹",
    population: null,
    populationYear: null,
    description: null,
    extract: null,
    imageUrl: null,
    wikipediaUrl: null,
  },
  feasibility: null,
  climate: null,
} satisfies TravelRecommendation;

describe("favorites", () => {
  beforeEach(() => {
    localStorage.clear();
  });

  it("adiciona, consulta e remove favoritos", () => {
    expect(isFavorite("PT")).toBe(false);
    addFavorite(recommendation, { from: "2026-06-01", to: "2026-06-07" });
    expect(isFavorite("PT")).toBe(true);
    toggleFavorite(recommendation);
    expect(isFavorite("PT")).toBe(false);
    addFavorite(recommendation);
    removeFavorite("PT");
    expect(isFavorite("PT")).toBe(false);
  });

  it("migra chave legada feriadao para viazio", () => {
    localStorage.setItem(
      "feriadao:favorites",
      JSON.stringify([
        {
          countryCode: "PT",
          countryName: "Portugal",
          savedAt: "2026-01-01T00:00:00.000Z",
          recommendation,
        },
      ]),
    );

    expect(isFavorite("PT")).toBe(true);
    expect(localStorage.getItem("viazio:favorites")).not.toBeNull();
    expect(localStorage.getItem("feriadao:favorites")).toBeNull();
  });
});
